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

import com.intellij.ide.highlighter.ArchiveFileType;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.util.NlsActions;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.ArchiveFileSystem;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.JOptionPane;
import java.util.List;

/**
 * Open file in binary editor action.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class OpenAsBinaryAction extends AnAction implements DumbAware {

    public OpenAsBinaryAction() {
        super(BinEdIntelliJPlugin.getResourceBundle().getString("action.BinEdEditor.OpenAsBinaryAction.name"));
    }

    public OpenAsBinaryAction(@Nullable @NlsActions.ActionText String text) {
        super(text);
    }

    @Nonnull
    @Override
    public ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void actionPerformed(AnActionEvent event) {
        Project project = event.getProject();
        if (project == null) {
            project = ProjectManager.getInstance().getDefaultProject();
        }

        VirtualFile virtualFile = event.getData(PlatformDataKeys.VIRTUAL_FILE);
        openVirtualFileAsBinary(project, virtualFile);
    }

    public static void openVirtualFileAsBinary(Project project, @Nullable VirtualFile virtualFile) {
        boolean isValid = virtualFile != null && virtualFile.isValid();
        if (isValid && virtualFile.isDirectory()) {
            isValid = false;
            if (virtualFile.getFileType() instanceof ArchiveFileType) {
                if (virtualFile.getFileSystem() instanceof JarFileSystem) {
                    virtualFile = ((JarFileSystem) virtualFile.getFileSystem()).getVirtualFileForJar(virtualFile);
                } else {
                    virtualFile = ((ArchiveFileSystem) virtualFile.getFileSystem()).getLocalByEntry(virtualFile);
                }
                isValid = virtualFile != null && virtualFile.isValid();
            }
        }

        if (isValid) {
            openValidVirtualFile(project, virtualFile);
        } else {
            JOptionPane.showMessageDialog(null,
                    BinEdIntelliJPlugin.getResourceBundle().getString("OpenAsBinaryAction.openFileFailed.message"),
                    BinEdIntelliJPlugin.getResourceBundle().getString("OpenAsBinaryAction.openFileFailed.title"),
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    @Nonnull
    public static BinEdVirtualFile openValidVirtualFile(Project project, VirtualFile virtualFile) {
        BinEdVirtualFile binEdVirtualFile = new BinEdVirtualFile(virtualFile);
        OpenFileDescriptor descriptor = new OpenFileDescriptor(project, binEdVirtualFile, 0);
        FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
        List<FileEditor> editors = fileEditorManager.openEditor(descriptor, true);
        fileEditorManager.setSelectedEditor(binEdVirtualFile, BinEdFileEditorProvider.BINED_EDITOR_TYPE_ID);
        for (FileEditor fileEditor : editors) {
            if (fileEditor instanceof BinEdFileEditor) {
                binEdVirtualFile.openFile(binEdVirtualFile.getEditorFile());
                break;
            } else {
                // TODO: Drop other editors
                fileEditor.dispose();
            }
        }
        return binEdVirtualFile;
    }
}
