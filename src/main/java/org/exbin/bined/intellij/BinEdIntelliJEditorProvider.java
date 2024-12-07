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

import org.exbin.bined.swing.CodeAreaCore;
import org.exbin.bined.swing.section.SectCodeArea;
import org.exbin.framework.App;
import org.exbin.framework.action.api.ComponentActivationListener;
import org.exbin.framework.bined.BinEdFileHandler;
import org.exbin.framework.bined.BinaryMultiEditorProvider;
import org.exbin.framework.editor.api.MultiEditorProvider;
import org.exbin.framework.file.api.FileHandler;
import org.exbin.framework.file.api.FileType;
import org.exbin.framework.frame.api.FrameModuleApi;
import org.exbin.framework.operation.undo.api.UndoRedoControl;
import org.exbin.framework.operation.undo.api.UndoRedoState;
import org.exbin.framework.utils.ClipboardActionsHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.JComponent;
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
public class BinEdIntelliJEditorProvider extends BinaryMultiEditorProvider {

    @Nullable
    protected FileHandler activeFile = null;

    public BinEdIntelliJEditorProvider() {
    }

    @Nonnull
    @Override
    public List<FileHandler> getFileHandlers() {
        List<FileHandler> result = new ArrayList<>();
        if (activeFile != null) {
            result.add(activeFile);
        }
        return result;
    }

    @Nonnull
    @Override
    public String getName(FileHandler fileHandler) {
        return "";
    }

    @Override
    public void saveFile(FileHandler fileHandler) {

    }

    @Override
    public void saveAsFile(FileHandler fileHandler) {

    }

    @Override
    public void closeFile() {

    }

    @Override
    public void closeFile(FileHandler fileHandler) {

    }

    @Override
    public void closeOtherFiles(FileHandler fileHandler) {

    }

    @Override
    public void closeAllFiles() {

    }

    @Override
    public void saveAllFiles() {

    }

    @Nonnull
    @Override
    public JComponent getEditorComponent() {
        if (activeFile instanceof BinEdFileHandler) {
            return ((BinEdFileHandler) activeFile).getComponent();
        }

        return null;
//        TODO throw new IllegalStateException("Unsupported file handler");
    }

    @Nonnull
    @Override
    public Optional<FileHandler> getActiveFile() {
        return Optional.ofNullable(activeFile);
    }

    public void setActiveFile(@Nullable FileHandler fileHandler) {
        activeFile = fileHandler;
        activeFileChanged();
    }

    public void activeFileChanged() {
        FrameModuleApi frameModule = App.getModule(FrameModuleApi.class);
        ComponentActivationListener componentActivationListener =
                frameModule.getFrameHandler().getComponentActivationListener();

        SectCodeArea extCodeArea = null;
        ClipboardActionsHandler clipboardActionsHandler = null;
        UndoRedoControl undoHandler = null;
        if (activeFile instanceof BinEdFileHandler) {
            BinEdFileHandler binEdFileHandler = (BinEdFileHandler) activeFile;
            extCodeArea = binEdFileHandler.getCodeArea();
            undoHandler = binEdFileHandler.getUndoRedo();
            clipboardActionsHandler = binEdFileHandler;
        }

        componentActivationListener.updated(FileHandler.class, activeFile);
        componentActivationListener.updated(CodeAreaCore.class, extCodeArea);
        componentActivationListener.updated(UndoRedoState.class, undoHandler);
        componentActivationListener.updated(ClipboardActionsHandler.class, clipboardActionsHandler);

        //        if (this.undoHandler != null) {
//            this.undoHandler.setActiveFile(this.activeFile);
//        }
    }

    @Nonnull
    @Override
    public String getWindowTitle(String s) {
        return "";
    }

    @Override
    public void openFile(URI uri, FileType fileType) {

    }

    @Override
    public void setModificationListener(EditorModificationListener editorModificationListener) {

    }

    @Override
    public void newFile() {

    }

    @Override
    public void openFile() {

    }

    @Override
    public void saveFile() {

    }

    @Override
    public void saveAsFile() {

    }

    @Override
    public boolean canSave() {
        return false;
    }

    @Override
    public boolean releaseFile(FileHandler fileHandler) {
        return false;
    }

    @Override
    public boolean releaseAllFiles() {
        return false;
    }

    @Override
    public void loadFromFile(String s) throws URISyntaxException {

    }

    @Override
    public void loadFromFile(URI uri, @org.jetbrains.annotations.Nullable FileType fileType) {

    }

    @Nonnull
    @Override
    public Optional<File> getLastUsedDirectory() {
        return Optional.empty();
    }

    @Override
    public void setLastUsedDirectory(@org.jetbrains.annotations.Nullable File file) {

    }

    @Override
    public void updateRecentFilesList(URI uri, FileType fileType) {

    }
}
