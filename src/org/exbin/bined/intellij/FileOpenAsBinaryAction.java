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
import com.intellij.openapi.vfs.VirtualFile;
import org.exbin.framework.gui.utils.ActionUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Open file in binary editor action.
 *
 * @author ExBin Project (http://exbin.org)
 * @version 0.2.1 2019/08/22
 */
public class FileOpenAsBinaryAction extends AnAction implements DumbAware {

    public FileOpenAsBinaryAction() {
        super("Open As Binary" + ActionUtils.DIALOG_MENUITEM_EXT);
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        super.update(event);
        event.getPresentation().setEnabledAndVisible(true);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        if (project == null) {
            project = ProjectManager.getInstance().getDefaultProject();
        }

        FileChooserDescriptor chooserDescriptor = new FileChooserDescriptor(true, false, true, false, false, false);
        VirtualFile virtualFile = FileChooser.chooseFile(chooserDescriptor, project, null);
        if (virtualFile != null && virtualFile.isValid() && !virtualFile.isDirectory()) {
            BinEdVirtualFile binEdVirtualFile = new BinEdVirtualFile(virtualFile);
            OpenFileDescriptor descriptor = new OpenFileDescriptor(project, binEdVirtualFile, 0);
            FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
            List<FileEditor> editors = fileEditorManager.openEditor(descriptor, true);
            fileEditorManager.setSelectedEditor(binEdVirtualFile, BinEdFileEditorProvider.BINED_EDITOR_TYPE_ID);
            for (FileEditor fileEditor : editors) {
                if (fileEditor instanceof BinEdFileEditor) {
                    binEdVirtualFile.getEditorPanel().openFile(binEdVirtualFile);
//                    ((BinEdFileEditor) fileEditor).openFile(binEdVirtualFile);
                } else {
                    // TODO: Drop other editors
                    fileEditor.dispose();
                }
            }
        }
    }
}
