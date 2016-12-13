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
package org.exbin.deltahex.intellij;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
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
 * @version 0.1.0 2016/12/13
 */
public class OpenAsHexAction extends AnAction {

    public OpenAsHexAction() {
        super("Open As Hex");
    }

    @Override
    public void update(AnActionEvent event) {
        super.update(event);
        final Project project = event.getProject();
        if (project == null) {
            return;
        }

        event.getPresentation().setEnabled(true);
    }

    @Override
    public void actionPerformed(AnActionEvent event) {
        final Project project = event.getProject();
        if (project == null) {
            return;
        }
        VirtualFile virtualFile = event.getDataContext().getData(PlatformDataKeys.VIRTUAL_FILE);
        if (virtualFile != null && virtualFile.isValid() && !virtualFile.isDirectory()) {
            DeltaHexVirtualFile deltaHexVirtualFile = new DeltaHexVirtualFile(virtualFile);
            OpenFileDescriptor descriptor = new OpenFileDescriptor(project, deltaHexVirtualFile, 0);
            List<FileEditor> editors = FileEditorManager.getInstance(project).openEditor(descriptor, true);
            DeltaHexFileEditor fileEditor = ((DeltaHexFileEditor) editors.get(0));
            fileEditor.openFile(deltaHexVirtualFile);
        }
    }
}
