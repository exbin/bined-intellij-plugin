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
import org.exbin.framework.document.api.DocumentModuleApi;
import org.exbin.framework.document.api.DocumentSource;
import org.exbin.framework.document.api.EditableDocument;
import org.exbin.framework.file.api.FileModuleApi;
import org.exbin.framework.file.api.SaveModifiedResult;
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
 * Documents docking mapping for IntelliJ BinEd plugin.
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
        if (document instanceof EditableDocument && ((EditableDocument) document).isModified()) {
            FileModuleApi fileModule = App.getModule(FileModuleApi.class);
            SaveModifiedResult result = fileModule.showSaveModified(((BinaryFileDocument) document).getComponent());
            switch (result) {
            case SAVE:
                DocumentModuleApi documentModule = App.getModule(DocumentModuleApi.class);
                Optional<DocumentSource> documentSource = documentModule.getMainDocumentManager().saveDocumentAs(document);
                if (documentSource.isPresent()) {
                    ((EditableDocument) document).saveTo(documentSource.get());
                    return true;
                }
                return false;
            case DISCARD:
                return true;
            case CANCEL:
                return false;
            }

            return false;
        }

        return true;
    }

    @Nonnull
    @Override
    public Component getComponent() {
        return null;
    }

    public void addDocument(BinaryFileDocument binaryDocument, BinaryStatusPanel statusPanel) {
        openDocuments.add(binaryDocument);
        statusPanels.put(binaryDocument, statusPanel);
    }

    public void removeDocument(BinaryFileDocument binaryDocument) {
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

        BinEdDataComponent dataComponent = null;
        if (activeDocument != null) {
            dataComponent = ((BinaryFileDocument) activeDocument).getDataComponent();
        }

        final Component parentComponent = dataComponent == null ? null : dataComponent.getCodeArea();

        contextManager.changeActiveState(ContextDocument.class, (ContextDocument) activeDocument);
        contextManager.changeActiveState(ContextFont.class, dataComponent);
        contextManager.changeActiveState(ContextEncoding.class, dataComponent);
        contextManager.changeActiveState(ContextComponent.class, dataComponent);
        contextManager.changeActiveState(DialogParentComponent.class, new DialogParentComponent() {
            @Nonnull
            @Override
            public Component getComponent() {
                return parentComponent;
            }
        });
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
