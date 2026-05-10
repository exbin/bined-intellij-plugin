/*
 * Copyright (C) ExBin Project, https://exbin.org
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

import org.exbin.jaguif.App;
import org.exbin.jaguif.context.api.ContextComponent;
import org.exbin.jaguif.action.api.DialogParentComponent;
import org.exbin.bined.jaguif.component.BinEdDataComponent;
import org.exbin.bined.jaguif.document.BinaryFileDocument;
import org.exbin.jaguif.context.api.ActiveContextManagement;
import org.exbin.jaguif.docking.multi.api.MultiDocking;
import org.exbin.jaguif.document.api.ContextDocument;
import org.exbin.jaguif.document.api.Document;
import org.exbin.jaguif.document.api.DocumentModuleApi;
import org.exbin.jaguif.document.api.DocumentSource;
import org.exbin.jaguif.document.api.EditableDocument;
import org.exbin.jaguif.file.api.FileModuleApi;
import org.exbin.jaguif.file.api.SaveModifiedResult;
import org.exbin.jaguif.frame.api.FrameModuleApi;
import org.exbin.jaguif.operation.undo.api.ContextUndoRedo;
import org.exbin.jaguif.statusbar.api.StatusBar;
import org.exbin.jaguif.text.encoding.ContextEncoding;
import org.exbin.jaguif.text.font.ContextFont;

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
 */
@ParametersAreNonnullByDefault
public class BinEdIntelliJDocking implements MultiDocking {

    protected final List<Document> openDocuments = new ArrayList<>();
    @Nullable
    protected Document activeDocument = null;
    // TODO Temporary status panel map until status bar registration is available
    protected final Map<Document, StatusBar> statusBars = new HashMap<>();

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

    @Nonnull
    @Override
    public Optional<Document> openNewDocument() {
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

    public void addDocument(BinaryFileDocument binaryDocument, StatusBar statusBar) {
        openDocuments.add(binaryDocument);
        statusBars.put(binaryDocument, statusBar);
    }

    public void removeDocument(BinaryFileDocument binaryDocument) {
        boolean removed = openDocuments.remove(binaryDocument);
        if (!removed) {
            throw new IllegalStateException("Attempt to remove invalid document");
        } else {
            statusBars.remove(binaryDocument);
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
            activeDocumentChanged();
        }
    }

    public void activeDocumentChanged() {
        FrameModuleApi frameModule = App.getModule(FrameModuleApi.class);
        ActiveContextManagement contextManager =
                frameModule.getFrameController().getContextManager();

        BinEdDataComponent dataComponent = null;
        if (activeDocument != null) {
            dataComponent = ((BinaryFileDocument) activeDocument).getDataComponent();
        }

        final Component parentComponent = dataComponent == null ? null : dataComponent.getCodeArea();

        contextManager.changeActiveState(ContextDocument.class, (ContextDocument) activeDocument);
        contextManager.changeActiveState(ContextFont.class, dataComponent);
        contextManager.changeActiveState(ContextEncoding.class, dataComponent);
        contextManager.changeActiveState(ContextComponent.class, dataComponent);
        contextManager.changeActiveState(ContextUndoRedo.class, dataComponent);
        contextManager.changeActiveState(DialogParentComponent.class, new DialogParentComponent() {
            @Nonnull
            @Override
            public Component getComponent() {
                return parentComponent;
            }
        });

        /* if (this.undoHandler != null) {
            this.undoHandler.setActiveFile(this.activeFile);
        } */
    }

    @Nonnull
    @Override
    public List<Document> getDocuments() {
        return openDocuments;
    }
}
