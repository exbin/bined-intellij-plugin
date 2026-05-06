/*
 * Copyright (C) ExBin Project, https://exbin.org
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
package org.exbin.bined.intellij;

import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.VetoableProjectManagerListener;
import com.intellij.openapi.vfs.VirtualFile;
import org.exbin.jaguif.App;
import org.exbin.bined.jaguif.document.BinaryFileDocument;
import org.exbin.jaguif.docking.multi.gui.ModifiedDocumentsPanel;
import org.exbin.jaguif.document.api.ComponentDocument;
import org.exbin.jaguif.document.api.Document;
import org.exbin.jaguif.document.api.DocumentModuleApi;
import org.exbin.jaguif.document.api.DocumentSource;
import org.exbin.jaguif.document.api.EditableDocument;
import org.exbin.jaguif.document.api.EmptyDocumentSource;
import org.exbin.jaguif.window.api.WindowHandler;
import org.exbin.jaguif.window.api.WindowModuleApi;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Vetoable variant of project listener for BinEd plugin.
 */
@ParametersAreNonnullByDefault
public class BinEdVetoableProjectListener implements VetoableProjectManagerListener {

    @Override
    public boolean canClose(Project project) {
        FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
        List<Document> fileDocuments = new ArrayList<>();
        VirtualFile[] openFiles = fileEditorManager.getOpenFiles();
        for (VirtualFile file : openFiles) {
            if (file instanceof BinEdVirtualFile && !((BinEdVirtualFile) file).isClosing()) {
                BinaryFileDocument fileDocument = ((BinEdVirtualFile) file).getEditorFile();
                if (fileDocument.isModified()) {
                    fileDocuments.add(fileDocument);
                }
            }
        }

        if (!fileDocuments.isEmpty()) {
            ComponentDocument document = (ComponentDocument) fileDocuments.get(0);
            boolean discardRest = showAskForSaveDialog(fileDocuments, document.getComponent());
            if (discardRest) {
                // Mark rest as already processed
                for (VirtualFile file : openFiles) {
                    if (file instanceof BinEdVirtualFile && !((BinEdVirtualFile) file).isClosing()) {
                        ((BinEdVirtualFile) file).setClosing(true);
                    }
                }

                return true;
            }

            return false;
        }

        return true;
    }

    public static boolean showAskForSaveDialog(@Nonnull List<Document> fileDocuments,
            @Nonnull Component parentComponent) {
        ModifiedDocumentsPanel modifiedDocumentsPanel = new ModifiedDocumentsPanel ();
        modifiedDocumentsPanel.setDocuments(fileDocuments);
        WindowModuleApi windowModule = App.getModule(WindowModuleApi.class);
        final boolean[] result = new boolean[1];
        final WindowHandler dialog = windowModule.createDialog(modifiedDocumentsPanel);
        windowModule.setWindowTitle(dialog, modifiedDocumentsPanel.getResourceBundle());
        modifiedDocumentsPanel.setController(new ModifiedDocumentsPanel.Controller() {
            @Override
            public boolean saveFile(@Nonnull Document document) {
                EditableDocument editableDocument = (EditableDocument) document;
                Optional<DocumentSource> optDocumentSource = editableDocument.getDocumentSource();
                if (optDocumentSource.isPresent() && !(optDocumentSource.get() instanceof EmptyDocumentSource)) {
                    editableDocument.saveTo(optDocumentSource.get());
                    return true;
                } else {
                    DocumentModuleApi documentModule = App.getModule(DocumentModuleApi.class);
                    Optional<DocumentSource> documentSource = documentModule.getMainDocumentManager().saveDocumentAs(document);
                    if (documentSource.isPresent()) {
                        editableDocument.saveTo(documentSource.get());
                        return true;
                    }
                }
                return false;
            }

            @Override
            public void discardAll(@Nonnull List<Document> fileDocuments) {
                result[0] = true;
                dialog.close();
            }

            @Override
            public void cancel() {
                result[0] = false;
                dialog.close();
            }
        });

        modifiedDocumentsPanel.assignGlobalKeys();
        dialog.showCentered(parentComponent);

        return result[0];
    }
}
