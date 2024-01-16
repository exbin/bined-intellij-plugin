/*
 * Copyright (C) ExBin Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.exbin.bined.intellij.main;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.ex.DocumentEx;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.LocalTimeCounter;
import org.exbin.auxiliary.binary_data.BinaryData;
import org.exbin.auxiliary.binary_data.delta.DeltaDocument;
import org.exbin.auxiliary.binary_data.paged.PagedData;
import org.exbin.bined.EditMode;
import org.exbin.bined.operation.swing.CodeAreaUndoHandler;
import org.exbin.bined.operation.undo.BinaryDataUndoHandler;
import org.exbin.bined.swing.extended.ExtCodeArea;
import org.exbin.framework.bined.BinEdEditorComponent;
import org.exbin.framework.bined.FileHandlingMode;
import org.exbin.framework.bined.gui.BinEdComponentFileApi;
import org.exbin.framework.editor.text.TextFontApi;
import org.exbin.framework.file.api.FileHandler;
import org.exbin.framework.file.api.FileType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.JComponent;
import java.awt.Font;
import java.io.IOException;
import java.net.URI;
import java.util.Optional;

/**
 * File editor wrapper using BinEd editor component.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class BinEdNativeFile implements FileHandler, BinEdComponentFileApi, TextFontApi {

    private final BinEdEditorComponent componentPanel;

    private boolean opened = false;
    private VirtualFile virtualFile;
    private Font defaultFont;
    private long documentOriginalSize;

    public BinEdNativeFile(VirtualFile virtualFile) {
        this.virtualFile = virtualFile;
        BinEdManager binEdManager = BinEdManager.getInstance();
        componentPanel = new BinEdEditorComponent();
        binEdManager.initFileHandler(this);
        binEdManager.getFileManager().initComponentPanel(componentPanel.getComponentPanel());

        ExtCodeArea codeArea = componentPanel.getCodeArea();
        CodeAreaUndoHandler undoHandler = new CodeAreaUndoHandler(codeArea);
        componentPanel.setUndoHandler(undoHandler);
        openFile(virtualFile);

        // TODO undoHandler = new BinaryUndoIntelliJHandler(codeArea, project, this);

//        componentPanel.setModifiedChangeListener(() -> {
//            updateModified();
//        });
        defaultFont = codeArea.getCodeFont();
        documentOriginalSize = virtualFile.getLength();
    }

    public boolean isModified() {
        return componentPanel.isModified();
    }

    @Nonnull
    public JComponent getComponent() {
        return componentPanel.getComponent();
    }

    @Nonnull
    @Override
    public BinEdEditorComponent getEditorComponent() {
        return componentPanel;
    }

    @Nonnull
    public ExtCodeArea getCodeArea() {
        return componentPanel.getCodeArea();
    }

    @Override public long getDocumentOriginalSize() {
        return documentOriginalSize;
    }

    @Override
    public int getId() {
        return -1;
    }

    @Nonnull
    @Override
    public Optional<URI> getFileUri() {
        return Optional.empty();
    }

    @Nonnull
    @Override
    public String getTitle() {
        return virtualFile.getName();
    }

    @Nonnull
    @Override
    public Optional<FileType> getFileType() {
        return Optional.empty();
    }

    @Override
    public void setFileType(@Nullable FileType fileType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clearFile() {

    }

    public void openFile(VirtualFile virtualFile) {
        boolean editable = virtualFile.isWritable();

        ApplicationManager.getApplication().runReadAction(() -> {
            try {
                byte[] fileContent = virtualFile.contentsToByteArray();
                PagedData binaryData = new PagedData();
                binaryData.insert(0, fileContent);
                componentPanel.setContentData(binaryData);
            } catch (IOException e) {
                throw createBrokenVirtualFileException(e);
            }
        });
        ExtCodeArea codeArea = componentPanel.getCodeArea();
        codeArea.addDataChangedListener(this::saveDocument);
        codeArea.setEditMode(editable ? EditMode.EXPANDING : EditMode.READ_ONLY);

        opened = true;
        documentOriginalSize = codeArea.getDataSize();
        updateModified();
        Optional<BinaryDataUndoHandler> undoHandler = componentPanel.getUndoHandler();
        if (undoHandler.isPresent()) {
            undoHandler.get().clear();
        }
    }

    @Override
    public boolean isSaveSupported() {
        return true;
    }

    @Override
    public void saveDocument() {
        BinaryData contentData = componentPanel.getContentData();
        final byte[] fileContent = contentData == null ? new byte[0] : new byte[(int) contentData.getDataSize()];
        if (contentData != null) {
            contentData.copyToArray(0L, fileContent, 0, (int) contentData.getDataSize());
        }
        Application application = ApplicationManager.getApplication();
        application.runWriteAction(() -> {
            try {
                virtualFile.setBinaryContent(fileContent);
            } catch (IOException e) {
                throw createBrokenVirtualFileException(e);
            }
        });
    }

    @Override
    public void switchFileHandlingMode(FileHandlingMode fileHandlingMode) {
        // Ignore
    }

    @Nonnull
    public FileHandlingMode getFileHandlingMode() {
        return FileHandlingMode.NATIVE;
    }

    public void reloadFile() {
        openFile(virtualFile);
    }

    public void closeData() {
        PagedData contentData = (PagedData) componentPanel.getCodeArea().getContentData();
        if (contentData != null) {
            contentData.clear();
        }
        componentPanel.setContentData(null);
    }

    private void updateModified() {
        boolean modified = componentPanel.isModified();
//        // TODO: Trying to force "modified behavior"
        Document document = FileDocumentManager.getInstance().getDocument(virtualFile);
        if (document instanceof DocumentEx) {
            ((DocumentEx) document).setModificationStamp(LocalTimeCounter.currentTime());
        }
//        propertyChangeSupport.firePropertyChange(FileEditor.PROP_MODIFIED, !modified, modified);
    }

    @Nonnull
    private IllegalStateException createBrokenVirtualFileException(@Nullable Exception ex) {
        String filePath = virtualFile.getCanonicalPath();
        String message = "Broken virtual file" + (filePath != null ? ":" + filePath : "");
        return new IllegalStateException(message, ex);
    }

    @Nullable
    public VirtualFile getVirtualFile() {
        return virtualFile;
    }

    @Nullable
    public JComponent getPreferredFocusedComponent() {
        return componentPanel.getCodeArea();
    }

    @Override public void loadFromFile(URI fileUri, @org.jetbrains.annotations.Nullable FileType fileType) {
        throw new UnsupportedOperationException();
    }

    @Override public void saveFile() {
        throw new UnsupportedOperationException();
    }

    @Override public void saveToFile(URI fileUri, @org.jetbrains.annotations.Nullable FileType fileType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setCurrentFont(Font font) {
        getCodeArea().setCodeFont(font);
    }

    @Nonnull
    @Override
    public Font getCurrentFont() {
        return getCodeArea().getCodeFont();
    }

    @Nonnull
    @Override
    public Font getDefaultFont() {
        return defaultFont;
    }
}
