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

import org.exbin.bined.swing.section.SectCodeArea;
import org.exbin.framework.App;
import org.exbin.framework.action.api.ContextComponent;
import org.exbin.framework.action.api.DialogParentComponent;
import org.exbin.framework.action.api.clipboard.ClipboardController;
import org.exbin.framework.action.api.clipboard.TextClipboardController;
import org.exbin.framework.bined.BinEdDataComponent;
import org.exbin.framework.bined.BinaryFileDocument;
import org.exbin.framework.bined.BinaryStatusApi;
import org.exbin.framework.bined.gui.BinEdComponentPanel;
import org.exbin.framework.context.api.ActiveContextManagement;
import org.exbin.framework.docking.multi.api.MultiDocking;
import org.exbin.framework.document.api.Document;
import org.exbin.framework.frame.api.FrameModuleApi;
import org.exbin.framework.operation.undo.api.UndoRedoState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Documents docking mapping for IntelliJ BinEd.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class BinEdIntelliJDocking implements MultiDocking {

    protected final List<Document> openDocuments = new ArrayList<>();
    @Nullable
    protected Document activeFile = null;
    // protected BinaryStatusApi binaryStatus;

    public BinEdIntelliJDocking() {
        super();
    }

    @Override
    public void closeAllDocuments() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void closeOtherDocuments(Document document) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void saveAllDocuments() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasOpenedDocuments() {
        return !openDocuments.isEmpty();
    }

    @Override
    public void openDocument(Document document) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void closeDocument(Document document) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void openNewDocument() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean releaseDocument(Document document) {
        throw new UnsupportedOperationException();
    }

    @Nonnull
    @Override
    public Component getComponent() {
        return null;
    }

    public void addFile(BinaryFileDocument binaryDocument) {
        openDocuments.add(binaryDocument);
    }

    public void removeFile(BinaryFileDocument binaryDocument) {
        boolean removed = openDocuments.remove(binaryDocument);
        if (!removed) {
            throw new IllegalStateException("Attempt to remove invalid file handler");
        }
    }

    @Nonnull
    @Override
    public Optional<Document> getActiveDocument() {
        return Optional.ofNullable(activeFile);
    }

    public void setActiveFile(@Nullable BinaryFileDocument fileDocument) {
        if (activeFile != fileDocument) {
            activeFile = fileDocument;
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
            extCodeArea = (SectCodeArea) ((BinaryFileDocument) activeFile).getCodeArea();
            binEdDataComponent = new BinEdDataComponent(extCodeArea);
//            undoHandler = activeFile.getUndoRedo().orElse(null);
//            clipboardController = activeFile.getClipboardActionsController();
//            updateStatus();
        }

//        contextManager.changeActiveState(FileHandler.class, activeFile);
//        contextManager.changeActiveState(FileOperations.class, this);
        contextManager.changeActiveState(ContextComponent.class, binEdDataComponent);
//        contextManager.changeActiveState(DialogParentComponent.class, binEdDataComponent == null ? () -> frameModule.getFrame() : binEdDataComponent::getCodeArea);
        contextManager.changeActiveState(UndoRedoState.class, undoHandler);
        contextManager.changeActiveState(ClipboardController.class, clipboardController);

        /* if (this.undoHandler != null) {
            this.undoHandler.setActiveFile(this.activeFile);
        } */
    }

    public void updateStatus() {
        // TODO
    }

    @Nonnull
    @Override
    public List<Document> getDocuments() {
        return openDocuments;
    }
}
