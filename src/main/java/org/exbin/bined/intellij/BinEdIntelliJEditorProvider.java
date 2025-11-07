/*
 * Copyright (C) ExBin Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.exbin.bined.intellij;

import org.exbin.auxiliary.binary_data.delta.DeltaDocument;
import org.exbin.bined.CodeAreaCaretPosition;
import org.exbin.bined.EditMode;
import org.exbin.bined.EditOperation;
import org.exbin.bined.SelectionRange;
import org.exbin.bined.swing.section.SectCodeArea;
import org.exbin.framework.App;
import org.exbin.framework.action.api.ActionModuleApi;
import org.exbin.framework.action.api.ContextComponent;
import org.exbin.framework.action.api.DialogParentComponent;
import org.exbin.framework.action.api.clipboard.ClipboardController;
import org.exbin.framework.action.api.clipboard.TextClipboardController;
import org.exbin.framework.bined.BinEdDataComponent;
import org.exbin.framework.bined.BinEdEditorProvider;
import org.exbin.framework.bined.BinEdFileHandler;
import org.exbin.framework.bined.BinaryStatusApi;
import org.exbin.framework.bined.gui.BinEdComponentPanel;
import org.exbin.framework.context.api.ActiveContextManagement;
import org.exbin.framework.editor.api.EditorModuleApi;
import org.exbin.framework.editor.api.MultiEditorProvider;
import org.exbin.framework.file.api.EditableFileHandler;
import org.exbin.framework.file.api.FileHandler;
import org.exbin.framework.file.api.FileModuleApi;
import org.exbin.framework.file.api.FileOperations;
import org.exbin.framework.file.api.FileType;
import org.exbin.framework.frame.api.FrameModuleApi;
import org.exbin.framework.operation.undo.api.UndoRedoState;
import org.exbin.framework.options.api.OptionsModuleApi;
import org.exbin.framework.text.encoding.TextEncodingStatusApi;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Editor provider wrapper for IntelliJ BinEd editor.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class BinEdIntelliJEditorProvider implements MultiEditorProvider, BinEdEditorProvider {

    protected final List<FileHandler> fileHandlers = new ArrayList<>();
    @Nullable
    protected BinEdFileHandler activeFile = null;
    private BinaryStatusApi binaryStatus;
    private TextEncodingStatusApi textEncodingStatusApi;

    public BinEdIntelliJEditorProvider() {
        super();
    }

    @Nonnull
    @Override
    public String getName(FileHandler fileHandler) {
        return fileHandler.getTitle();
    }

    @Override
    public void saveFile(FileHandler fileHandler) {
        ((BinEdFileHandler) fileHandler).saveFile();
    }

    public void addFile(BinEdFileHandler fileHandler) {
        fileHandlers.add(fileHandler);

        SectCodeArea codeArea = fileHandler.getCodeArea();
        codeArea.addDataChangedListener(() -> {
            fileHandler.getComponent().notifyDataChanged();
            setActiveFile(fileHandler);
            updateCurrentDocumentSize();
        });

        codeArea.addSelectionChangedListener(() -> {
            binaryStatus.setSelectionRange(codeArea.getSelection());
            updateClipboardActionsStatus();
        });

        codeArea.addCaretMovedListener((CodeAreaCaretPosition caretPosition) -> {
            binaryStatus.setCursorPosition(caretPosition);
        });

        codeArea.addEditModeChangedListener((EditMode mode, EditOperation operation) -> {
            binaryStatus.setEditMode(mode, operation);
        });

        EditorModuleApi editorModule = App.getModule(EditorModuleApi.class);
        editorModule.notifyEditorComponentCreated(fileHandler.getComponent());

        OptionsModuleApi optionsModule = App.getModule(OptionsModuleApi.class);
        fileHandler.getComponent().onInitFromPreferences(optionsModule.getAppOptions());
    }

    public void removeFile(BinEdFileHandler fileHandler) {
        boolean removed = fileHandlers.remove(fileHandler);
        if (!removed) {
            throw new IllegalStateException("Attempt to remove invalid file handler");
        }
    }

    @Override
    public void saveAsFile(FileHandler fileHandler) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void closeFile() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void closeFile(FileHandler fileHandler) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void closeOtherFiles(FileHandler fileHandler) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void closeAllFiles() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void saveAllFiles() {
        throw new UnsupportedOperationException();
    }

    @Nonnull
    @Override
    public BinEdComponentPanel getEditorComponent() {
        if (activeFile != null) {
            return activeFile.getComponent();
        }

        throw new IllegalStateException("Unsupported file handler");
    }

    @Nonnull
    @Override
    public Optional<FileHandler> getActiveFile() {
        return Optional.ofNullable(activeFile);
    }

    public void setActiveFile(@Nullable FileHandler fileHandler) {
        if (activeFile != fileHandler) {
            activeFile = (BinEdFileHandler) fileHandler;
            activeFileChanged();
        }
    }

    public void activeFileChanged() {
/*        contextManager.updated(FileHandler.class, activeFile);
        if (activeFile instanceof EditorFileHandler) {
            ((EditorFileHandler) activeFile).componentActivated(contextManager);
        } */

        FrameModuleApi frameModule = App.getModule(FrameModuleApi.class);
        ActiveContextManagement contextManager =
                frameModule.getFrameHandler().getContextManager();

        SectCodeArea extCodeArea = null;
        BinEdDataComponent binEdDataComponent = null;
        TextClipboardController clipboardController = null;
        UndoRedoState undoHandler = null;
        if (activeFile != null) {
            extCodeArea = activeFile.getCodeArea();
            binEdDataComponent = new BinEdDataComponent(extCodeArea);
            undoHandler = activeFile.getUndoRedo().orElse(null);
            clipboardController = activeFile.getClipboardActionsController();
            updateStatus();
        }

        contextManager.changeActiveState(FileHandler.class, activeFile);
        contextManager.changeActiveState(FileOperations.class, this);
        contextManager.changeActiveState(ContextComponent.class, binEdDataComponent);
        contextManager.changeActiveState(DialogParentComponent.class, binEdDataComponent == null ? () -> frameModule.getFrame() : binEdDataComponent::getCodeArea);
        contextManager.changeActiveState(UndoRedoState.class, undoHandler);
        contextManager.changeActiveState(ClipboardController.class, clipboardController);

        /* if (this.undoHandler != null) {
            this.undoHandler.setActiveFile(this.activeFile);
        } */
    }

    @Override
    public void registerBinaryStatus(BinaryStatusApi binaryStatusApi) {
        this.binaryStatus = binaryStatusApi;
        updateStatus();
    }

    @Override
    public void updateStatus() {
        updateCurrentDocumentSize();
        updateCurrentCaretPosition();
        updateCurrentSelectionRange();
        updateCurrentMemoryMode();
        updateCurrentEditMode();

        if (textEncodingStatusApi != null) {
            updateCurrentEncoding();
        }
    }

    private void updateCurrentDocumentSize() {
        if (binaryStatus == null) {
            return;
        }

        SectCodeArea codeArea = activeFile.getCodeArea();
        long documentOriginalSize = activeFile.getDocumentOriginalSize();
        long dataSize = codeArea.getDataSize();
        binaryStatus.setCurrentDocumentSize(dataSize, documentOriginalSize);
    }

    private void updateCurrentCaretPosition() {
        if (binaryStatus == null) {
            return;
        }

        SectCodeArea codeArea = activeFile.getCodeArea();
        CodeAreaCaretPosition caretPosition = codeArea.getActiveCaretPosition();
        binaryStatus.setCursorPosition(caretPosition);
    }

    private void updateCurrentSelectionRange() {
        if (binaryStatus == null) {
            return;
        }

        SectCodeArea codeArea = activeFile.getCodeArea();
        SelectionRange selectionRange = codeArea.getSelection();
        binaryStatus.setSelectionRange(selectionRange);
    }

    private void updateCurrentMemoryMode() {
        if (binaryStatus == null) {
            return;
        }

        SectCodeArea codeArea = activeFile.getCodeArea();
        BinaryStatusApi.MemoryMode newMemoryMode = BinaryStatusApi.MemoryMode.RAM_MEMORY;
        if (codeArea.getEditMode() == EditMode.READ_ONLY) {
            newMemoryMode = BinaryStatusApi.MemoryMode.READ_ONLY;
        } else if (codeArea.getContentData() instanceof DeltaDocument) {
            newMemoryMode = BinaryStatusApi.MemoryMode.DELTA_MODE;
        }

        binaryStatus.setMemoryMode(newMemoryMode);
    }

    private void updateCurrentEditMode() {
        if (binaryStatus == null) {
            return;
        }

        SectCodeArea codeArea = activeFile.getCodeArea();
        binaryStatus.setEditMode(codeArea.getEditMode(), codeArea.getActiveOperation());
    }

    @Override
    public void registerEncodingStatus(TextEncodingStatusApi encodingStatus) {
        this.textEncodingStatusApi = encodingStatus;
        updateCurrentEncoding();
    }

    public void updateCurrentEncoding() {
        if (textEncodingStatusApi == null) {
            return;
        }

        textEncodingStatusApi.setEncoding(activeFile.getBinaryDataComponent().getCharset().name());
    }

    @Override
    public void registerUndoHandler() {
        throw new UnsupportedOperationException();
    }

    @Nonnull
    @Override
    public List<FileHandler> getFileHandlers() {
        return fileHandlers;
    }

    @Override
    public boolean releaseFile(FileHandler fileHandler) {
        if (fileHandler instanceof EditableFileHandler && ((EditableFileHandler) fileHandler).isModified()) {
            FileModuleApi fileModule = App.getModule(FileModuleApi.class);
            return fileModule.getFileActions().showAskForSaveDialog(fileHandler, null, this);
        }

        return true;
    }

    @Override
    public boolean releaseAllFiles() {
        throw new UnsupportedOperationException();
    }

    @Nonnull
    @Override
    public String getWindowTitle(String windowTitle) {
        if (activeFile == null) {
            return windowTitle;
        }

        return windowTitle + " - " + activeFile.getTitle();
    }

    @Override
    public void openFile(URI uri, FileType fileType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void newFile() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void openFile() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void saveFile() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void saveAsFile() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean canSave() {
        if (activeFile == null) {
            return false;
        }

        return activeFile.canSave();
    }

    @Override
    public void loadFromFile(String s) throws URISyntaxException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void loadFromFile(URI uri, @org.jetbrains.annotations.Nullable FileType fileType) {
        throw new UnsupportedOperationException();
    }

    @Nonnull
    @Override
    public Optional<File> getLastUsedDirectory() {
        return Optional.empty();
    }

    @Override
    public void setLastUsedDirectory(@org.jetbrains.annotations.Nullable File file) {
        // ignore
    }

    @Override
    public void updateRecentFilesList(URI uri, FileType fileType) {
        // ignore
    }

    private void updateClipboardActionsStatus() {
        ActionModuleApi actionModule = App.getModule(ActionModuleApi.class);
        // TODO ((ClipboardActionsUpdater) actionModule.getClipboardActions()).updateClipboardActions();
    }
}
