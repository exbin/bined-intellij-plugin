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
import org.exbin.framework.bined.BinEdFileManager;
import org.exbin.framework.bined.BinaryFileDocument;
import org.exbin.framework.bined.BinaryStatus;
import org.exbin.framework.bined.BinedModule;
import org.exbin.framework.bined.gui.BinaryStatusPanel;
import org.exbin.framework.context.api.ActiveContextManagement;
import org.exbin.framework.docking.multi.api.MultiDocking;
import org.exbin.framework.document.api.ContextDocument;
import org.exbin.framework.document.api.Document;
import org.exbin.framework.frame.api.FrameModuleApi;
import org.exbin.framework.operation.undo.api.UndoRedoState;
import org.exbin.framework.text.encoding.ContextEncoding;
import org.exbin.framework.text.font.ContextFont;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.awt.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    protected Document activeDocument = null;
    // TODO Temporary status panel map until status bar registration is available
    protected final Map<Document, BinaryStatusPanel> statusPanels = new HashMap<>();

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

    public void addFile(BinaryFileDocument binaryDocument, BinaryStatusPanel statusPanel) {
        openDocuments.add(binaryDocument);
        statusPanels.put(binaryDocument, statusPanel);
    }

    public void removeFile(BinaryFileDocument binaryDocument) {
        boolean removed = openDocuments.remove(binaryDocument);
        if (!removed) {
            throw new IllegalStateException("Attempt to remove invalid document");
        } else {
            statusPanels.remove(binaryDocument);
        }
    }

    @Nonnull
    @Override
    public Optional<Document> getActiveDocument() {
        return Optional.ofNullable(activeDocument);
    }

    public void setActiveDocument(@Nullable BinaryFileDocument fileDocument) {
        if (activeDocument != fileDocument) {
            activeDocument = fileDocument;
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
        if (activeDocument != null) {
            extCodeArea = (SectCodeArea) ((BinaryFileDocument) activeDocument).getCodeArea();
            binEdDataComponent = new BinEdDataComponent(extCodeArea);
//            undoHandler = activeFile.getUndoRedo().orElse(null);
//            clipboardController = activeFile.getClipboardActionsController();
        }

        final Component parentComponent = binEdDataComponent == null ? null : binEdDataComponent.getCodeArea();

        contextManager.changeActiveState(ContextDocument.class, (ContextDocument) activeDocument);
        contextManager.changeActiveState(ContextFont.class, binEdDataComponent);
        contextManager.changeActiveState(ContextEncoding.class, binEdDataComponent);
        contextManager.changeActiveState(ContextComponent.class, binEdDataComponent);
        contextManager.changeActiveState(DialogParentComponent.class, new DialogParentComponent() {
            @Nonnull
            @Override
            public Component getComponent() {
                return parentComponent;
            }
        });
        contextManager.changeActiveState(UndoRedoState.class, undoHandler);
        contextManager.changeActiveState(ClipboardController.class, clipboardController);
        updateStatus();

        /* if (this.undoHandler != null) {
            this.undoHandler.setActiveFile(this.activeFile);
        } */
    }

    public void updateStatus() {
        BinaryStatusPanel binaryStatusPanel = statusPanels.get(activeDocument);
        if (binaryStatusPanel != null) {
            BinedModule binedModule = App.getModule(BinedModule.class);
            BinEdFileManager fileManager = binedModule.getFileManager();
            BinaryStatus binaryStatus = fileManager.getBinaryStatus();
            binaryStatus.setBinaryStatusPanel(binaryStatusPanel);
            binaryStatus.updateStatus();
        }
    }

    @Nonnull
    @Override
    public List<Document> getDocuments() {
        return openDocuments;
    }
}
