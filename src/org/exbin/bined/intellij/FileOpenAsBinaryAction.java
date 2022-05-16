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

import com.intellij.ide.highlighter.ArchiveFileType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.ArchiveFileSystem;
import org.exbin.framework.utils.ActionUtils;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.JOptionPane;
import java.util.List;

/**
 * Open file in binary editor action.
 *
 * @author ExBin Project (http://exbin.org)
 * @version 0.2.1 2021/08/26
 */
@ParametersAreNonnullByDefault
public class FileOpenAsBinaryAction extends AnAction implements DumbAware {

    public FileOpenAsBinaryAction() {
        super("Open As Binary" + ActionUtils.DIALOG_MENUITEM_EXT);
    }

    @Override
    public void update(AnActionEvent event) {
        super.update(event);
        event.getPresentation().setEnabledAndVisible(true);
    }

    @Override
    public void actionPerformed(AnActionEvent event) {
        Project project = event.getProject();
        if (project == null) {
            project = ProjectManager.getInstance().getDefaultProject();
        }

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
            BinEdVirtualFile binEdVirtualFile = new BinEdVirtualFile(virtualFile);
            OpenFileDescriptor descriptor = new OpenFileDescriptor(project, binEdVirtualFile, 0);
            FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
            List<FileEditor> editors = fileEditorManager.openEditor(descriptor, true);
            fileEditorManager.setSelectedEditor(binEdVirtualFile, BinEdFileEditorProvider.BINED_EDITOR_TYPE_ID);
            for (FileEditor fileEditor : editors) {
                if (fileEditor instanceof BinEdFileEditor) {
                    binEdVirtualFile.getEditorFile().openFile(binEdVirtualFile);
//                    ((BinEdFileEditor) fileEditor).openFile(binEdVirtualFile);
                } else {
                    // TODO: Drop other editors
                    fileEditor.dispose();
                }
            }
        } else {
            JOptionPane.showMessageDialog(null, "File reported as invalid", "Unable to open file", JOptionPane.ERROR_MESSAGE);
        }
    }
}
