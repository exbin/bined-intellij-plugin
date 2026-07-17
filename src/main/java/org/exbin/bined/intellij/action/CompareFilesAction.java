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
package org.exbin.bined.intellij.action;

import org.exbin.auxiliary.binary_data.BinaryData;
import org.exbin.auxiliary.binary_data.array.paged.ByteArrayPagedData;
import org.exbin.auxiliary.binary_data.paged.PagedData;
import org.exbin.bined.intellij.diff.gui.CompareFilesPanel;
import org.exbin.bined.jaguif.document.BinaryFileDocument;
import org.exbin.jaguif.App;
import org.exbin.jaguif.action.api.DialogParentComponent;
import org.exbin.jaguif.docking.api.DocumentDocking;
import org.exbin.jaguif.docking.multi.api.MultiDocking;
import org.exbin.jaguif.document.api.Document;
import org.exbin.jaguif.file.api.AllFileTypes;
import org.exbin.jaguif.file.api.FileDialogsProvider;
import org.exbin.jaguif.file.api.FileDocument;
import org.exbin.jaguif.file.api.FileModuleApi;
import org.exbin.jaguif.file.api.OpenFileResult;
import org.exbin.jaguif.frame.api.FrameModuleApi;
import org.exbin.jaguif.help.api.HelpLink;
import org.exbin.jaguif.help.api.HelpModuleApi;
import org.exbin.jaguif.window.api.WindowHandler;
import org.exbin.jaguif.window.api.WindowModuleApi;
import org.exbin.jaguif.window.api.gui.CloseControlPanel;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import javax.swing.AbstractAction;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Compare files action.
 */
@NullMarked
public class CompareFilesAction extends AbstractAction {

    public static final String ACTION_ID = "compareFiles";
    public static final String HELP_ID = "compare-files";

    private DocumentDocking documentDocking;
    private DialogParentComponent dialogParentComponent;

    public CompareFilesAction() {
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final CompareFilesPanel compareFilesPanel = new CompareFilesPanel();
        ResourceBundle panelResourceBundle = compareFilesPanel.getResourceBundle();
        CloseControlPanel controlPanel = new CloseControlPanel(panelResourceBundle);
        HelpModuleApi helpModule = App.getModule(HelpModuleApi.class);
        helpModule.addLinkToControlPanel(controlPanel, new HelpLink(HELP_ID));

        WindowModuleApi windowModule = App.getModule(WindowModuleApi.class);
        final WindowHandler dialog = windowModule.createDialog(dialogParentComponent.getComponent(), Dialog.ModalityType.APPLICATION_MODAL, compareFilesPanel, controlPanel);
        windowModule.setWindowTitle(dialog, panelResourceBundle);
        Dimension preferredSize = dialog.getWindow().getPreferredSize();
        dialog.getWindow().setSize(new Dimension(preferredSize.width, preferredSize.height + 450));
        controlPanel.setController(dialog::close);
        Optional<Document> activeDocument = documentDocking.getActiveDocument();
        if (activeDocument.isPresent()) {
            compareFilesPanel.setLeftFile(((BinaryFileDocument) activeDocument.get()).getBinaryData());
        }

        List<Document> fileDocuments;
        if (documentDocking instanceof MultiDocking) {
            fileDocuments = ((MultiDocking) documentDocking).getDocuments();
            List<String> availableFiles = new ArrayList<>();
            for (Document document : fileDocuments) {
                if (!(document instanceof FileDocument)) {
                    continue;
                }
                Optional<URI> fileUri = ((FileDocument) document).getFileUri();
                availableFiles.add(fileUri.isPresent() ? fileUri.get().toString() : panelResourceBundle.getString("unsavedFile"));
            }
            compareFilesPanel.setAvailableFiles(availableFiles);
        } else {
            fileDocuments = new ArrayList<>();
            Document document = documentDocking.getActiveDocument().orElse(null);
            if (document != null) {
                String documentName = document instanceof FileDocument ? ((FileDocument) document).getFileUri().toString() : panelResourceBundle.getString("unsavedFile");
                List<String> availableFiles = new ArrayList<>();
                availableFiles.add(documentName);
                compareFilesPanel.setAvailableFiles(availableFiles);
            }
        }

        compareFilesPanel.setController(new CompareFilesPanel.Controller() {
            @Override
            public CompareFilesPanel.@Nullable FileRecord openFile() {
                FrameModuleApi frameModule = App.getModule(FrameModuleApi.class);        
                FileModuleApi fileModule = App.getModule(FileModuleApi.class);
                FileDialogsProvider dialogsProvider = fileModule.getFileDialogsProvider();
                OpenFileResult openFileResult = dialogsProvider.showOpenFileDialog(frameModule.getFrame(), new AllFileTypes(), null, null, null);
                if (openFileResult.getResultType() != OpenFileResult.ResultType.APPROVED) {
                    return null;
                }
                
                File file = openFileResult.getSelectedFile().get();
                try (FileInputStream stream = new FileInputStream(file)) {
                    PagedData pagedData = new ByteArrayPagedData();
                    pagedData.loadFromStream(stream);
                    return new CompareFilesPanel.FileRecord(file.getAbsolutePath(), pagedData);
                } catch (IOException ex) {
                    Logger.getLogger(CompareFilesAction.class.getName()).log(Level.SEVERE, null, ex);

                }
                return null;
            }

            @Override
            public BinaryData getFileData(int index) {
                return ((BinaryFileDocument) fileDocuments.get(index)).getCodeArea().getContentData();
            }
        });
        dialog.showCentered(dialogParentComponent.getComponent());
    }

    public void setDialogParentComponent(DialogParentComponent dialogParentComponent) {
        this.dialogParentComponent = dialogParentComponent;
    }

    public void setDocumentDocking(DocumentDocking documentDocking) {
        this.documentDocking = documentDocking;
    }
}
