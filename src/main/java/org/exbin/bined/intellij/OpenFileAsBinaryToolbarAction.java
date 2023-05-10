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

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.fileTypes.DirectoryFileType;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vfs.VirtualFile;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Open file in binary editor action.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class OpenFileAsBinaryToolbarAction extends OpenFileAsBinaryAction {

    private boolean actionVisible = true;

    @Override
    public void update(AnActionEvent event) {
        super.update(event);
        VirtualFile virtualFile = event.getData(PlatformDataKeys.VIRTUAL_FILE);

        Presentation presentation = event.getPresentation();
        if (actionVisible) {
            presentation.setEnabled(virtualFile != null && virtualFile.isValid() && !(virtualFile.isDirectory()
                    || virtualFile.getFileType() instanceof DirectoryFileType));
            presentation.setDisabledIcon(IconLoader.getDisabledIcon(presentation.getIcon()));
        }
        presentation.setVisible(actionVisible);
    }

    public void setActionVisible(boolean actionVisible) {
        this.actionVisible = actionVisible;
    }
}
