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

import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.VetoableProjectManagerListener;
import com.intellij.openapi.vfs.VirtualFile;
import org.exbin.bined.intellij.main.BinEdFileHandler;
import org.exbin.framework.editor.gui.UnsavedFilesPanel;
import org.exbin.framework.file.api.FileHandler;
import org.exbin.framework.utils.WindowUtils;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.awt.Component;
import java.awt.Dialog;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Vetoable variant of project listener for BinEd plugin.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class BinEdVetoableProjectListener implements VetoableProjectManagerListener {

    @Override
    public boolean canClose(Project project) {
        FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
        List<FileHandler> fileHandlers = new ArrayList<>();
        VirtualFile[] openFiles = fileEditorManager.getOpenFiles();
        for (VirtualFile file : openFiles) {
            if (file instanceof BinEdVirtualFile && !((BinEdVirtualFile) file).isClosing()) {
                FileHandler fileHandler = ((BinEdVirtualFile) file).getEditorFile();
                if (fileHandler.isModified()) {
                    fileHandlers.add(fileHandler);
                }
            }
        }

        if (!fileHandlers.isEmpty()) {
            boolean discardRest = showAskForSaveDialog(fileHandlers, fileHandlers.get(0).getComponent());
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

    public static boolean showAskForSaveDialog(@Nonnull List<FileHandler> fileHandlers,
            @Nonnull Component parentComponent) {
        UnsavedFilesPanel unsavedFilesPanel = new UnsavedFilesPanel();
        unsavedFilesPanel.setUnsavedFiles(fileHandlers);
        ResourceBundle resourceBundle = unsavedFilesPanel.getResourceBundle();
        final boolean[] result = new boolean[1];
        final WindowUtils.DialogWrapper dialog = WindowUtils.createDialog(unsavedFilesPanel,
                parentComponent,
                resourceBundle.getString("dialog.title"),
                Dialog.ModalityType.APPLICATION_MODAL);
        unsavedFilesPanel.setController(new UnsavedFilesPanel.Controller() {
            @Override
            public boolean saveFile(@Nonnull FileHandler fileHandler) {
                fileHandler.saveFile();
                return !fileHandler.isModified();
            }

            @Override
            public void discardAll(@Nonnull List<FileHandler> fileHandlers) {
                result[0] = true;
                dialog.close();
            }

            @Override
            public void cancel() {
                result[0] = false;
                dialog.close();
            }
        });

        unsavedFilesPanel.assignGlobalKeys();
        dialog.show();

        return result[0];
    }
}
