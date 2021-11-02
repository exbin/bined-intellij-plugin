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
package org.exbin.bined.intellij.action;

import com.intellij.ide.highlighter.ArchiveFileType;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.ArchiveFileSystem;
import org.exbin.auxiliary.paged_data.BinaryData;
import org.exbin.auxiliary.paged_data.PagedData;
import org.exbin.bined.swing.extended.ExtCodeArea;
import org.exbin.framework.bined.gui.CompareFilesPanel;
import org.exbin.framework.gui.utils.WindowUtils;
import org.exbin.framework.gui.utils.gui.CloseControlPanel;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Compare files action.
 *
 * @version 0.2.5 2021/11/02
 * @author ExBin Project (http://exbin.org)
 */
@ParametersAreNonnullByDefault
public class CompareFilesAction implements ActionListener {

    private final ExtCodeArea codeArea;

    public CompareFilesAction(ExtCodeArea codeArea) {
        this.codeArea = Objects.requireNonNull(codeArea);
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        final CompareFilesPanel compareFilesPanel = new CompareFilesPanel();
        ResourceBundle panelResourceBundle = compareFilesPanel.getResourceBundle();
        CloseControlPanel controlPanel = new CloseControlPanel(panelResourceBundle);
        JPanel dialogPanel = WindowUtils.createDialogPanel(compareFilesPanel, controlPanel);
        Dimension preferredSize = dialogPanel.getPreferredSize();
        dialogPanel.setPreferredSize(new Dimension(preferredSize.width, preferredSize.height + 450));
        final WindowUtils.DialogWrapper dialog = WindowUtils.createDialog(dialogPanel, (Component) event.getSource(), panelResourceBundle.getString("dialog.title"), Dialog.ModalityType.APPLICATION_MODAL);
        controlPanel.setHandler(dialog::close);

        List<String> availableFiles = new ArrayList<>();
        availableFiles.add("Current File");
        compareFilesPanel.setControl(new CompareFilesPanel.Control() {
            @Nullable
            @Override
            public CompareFilesPanel.FileRecord openFile() {
                Project project = ProjectManager.getInstance().getDefaultProject();

                FileChooserDescriptor chooserDescriptor = new FileChooserDescriptor(true, false, true, false, false, false);
                VirtualFile virtualFile = FileChooser.chooseFile(chooserDescriptor, project, null);
                boolean isValid = virtualFile != null && virtualFile.isValid();
                if (isValid && virtualFile.isDirectory()) {
                    isValid = false;
                    if (virtualFile.getFileType() instanceof ArchiveFileType) {
                        if (virtualFile.getFileSystem() instanceof JarFileSystem) {
                            virtualFile = ((JarFileSystem) virtualFile.getFileSystem()).getVirtualFileForJar(virtualFile);
                            isValid = virtualFile != null && virtualFile.isValid();
                        } else {
                            virtualFile = ((ArchiveFileSystem) virtualFile.getFileSystem()).getLocalByEntry(virtualFile);
                            isValid = virtualFile != null && virtualFile.isValid();
                        }
                    }
                }
                if (isValid) {
                    try (InputStream stream = virtualFile.getInputStream()) {
                        PagedData pagedData = new PagedData();
                        pagedData.loadFromStream(stream);
                        return new CompareFilesPanel.FileRecord(virtualFile.getName(), pagedData);
                    } catch (IOException ex) {
                        Logger.getLogger(CompareFilesAction.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else {
                    JOptionPane.showMessageDialog(null,  "File reported as invalid", "Unable to open file", JOptionPane.ERROR_MESSAGE);
                }

                return null;
            }

            @Nonnull
            @Override
            public BinaryData getFileData(int index) {
                return codeArea.getContentData();
            }
        });
        compareFilesPanel.setAvailableFiles(availableFiles);
        compareFilesPanel.setLeftIndex(1);
        dialog.showCentered((Component) event.getSource());
    }
}
