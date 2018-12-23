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
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import java.util.List;

/**
 * Open file in hexadecimal editor action.
 *
 * @author ExBin Project (http://exbin.org)
 * @version 0.2.0 2018/12/23
 */
public class FileOpenAsBinaryAction extends AnAction {

    public FileOpenAsBinaryAction() {
        super("Open As Binary" + DialogUtils.DIALOG_MENUITEM_EXT);
    }

    @Override
    public void actionPerformed(AnActionEvent event) {
        final Project project = event.getProject();
        if (project == null) {
            return;
        }

        FileChooserDescriptor chooserDescriptor = new FileChooserDescriptor(true, false, true, false, false, false);
        VirtualFile virtualFile = FileChooser.chooseFile(chooserDescriptor, project, null);
        if (virtualFile != null && virtualFile.isValid() && !virtualFile.isDirectory()) {
            BinEdVirtualFile binEdVirtualFile = new BinEdVirtualFile(virtualFile);
            OpenFileDescriptor descriptor = new OpenFileDescriptor(project, binEdVirtualFile, 0);
            FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
            List<FileEditor> editors = fileEditorManager.openEditor(descriptor, true);
            fileEditorManager.setSelectedEditor(virtualFile, BinEdWindowProvider.BINED_EDITOR_TYPE_ID);
            for (FileEditor fileEditor : editors) {
                if (fileEditor instanceof BinEdFileEditor) {
                    ((BinEdFileEditor) fileEditor).openFile(binEdVirtualFile);
                }
            }
        }
    }
}
