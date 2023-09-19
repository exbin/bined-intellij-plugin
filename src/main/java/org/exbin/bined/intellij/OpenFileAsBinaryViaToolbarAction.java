/*
 * Copyright (C) ExBin Project
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

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileTypes.DirectoryFileType;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vfs.VirtualFile;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Open file in binary editor action.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class OpenFileAsBinaryViaToolbarAction extends AnAction implements DumbAware {

    private boolean actionVisible = true;
    private boolean internalChooser = false;

    public OpenFileAsBinaryViaToolbarAction() {
        super("Open As Binary");
        BinEdPluginStartupActivity.addIntegrationOptionsListener(integrationOptions -> actionVisible =
                integrationOptions.isRegisterOpenFileAsBinaryViaToolbar());
    }

    @Override
    public void update(AnActionEvent event) {
        super.update(event);
        event.getPresentation().setEnabledAndVisible(actionVisible && !internalChooser);
    }

    @Override
    public void actionPerformed(AnActionEvent event) {
        Project project = event.getProject();
        if (project == null) {
            project = ProjectManager.getInstance().getDefaultProject();
        }

        VirtualFile virtualFile = event.getData(PlatformDataKeys.VIRTUAL_FILE);

        if (virtualFile == null || !virtualFile.isValid()) {
            virtualFile = chooseVirtualFile(project, null);
            if (virtualFile == null || !virtualFile.isValid())
                return;
        } else if (virtualFile.isDirectory() || virtualFile.getFileType() instanceof DirectoryFileType) {
            virtualFile = chooseVirtualFile(project, virtualFile);
            if (virtualFile == null || !virtualFile.isValid())
                return;
        }

        OpenAsBinaryAction.openVirtualFileAsBinary(project, virtualFile);
    }

    @Nullable
    private VirtualFile chooseVirtualFile(Project project, @Nullable VirtualFile directory) {
        FileChooserDescriptor chooserDescriptor = new FileChooserDescriptor(true, false, true, true, false, false);
        chooserDescriptor.setTitle("Open File in Binary Editor");
        if (directory != null) {
            chooserDescriptor.setRoots(directory);
        }
        internalChooser = true;
        VirtualFile virtualFile = FileChooser.chooseFile(chooserDescriptor, project, null);
        internalChooser = false;

        return virtualFile;
    }
}
