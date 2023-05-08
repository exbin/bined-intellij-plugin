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
package org.exbin.framework.editor.action;

import org.exbin.framework.bined.BinEdFileHandler;
import org.exbin.framework.editor.gui.UnsavedFilesPanel;
import org.exbin.framework.utils.WindowUtils;

import javax.annotation.ParametersAreNonnullByDefault;
import java.awt.Component;
import java.awt.Dialog;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Editor actions.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class EditorActions {

    public EditorActions() {
    }

    public boolean showAskForSaveDialog(List<BinEdFileHandler> fileHandlers, Component parentComponent) {
        UnsavedFilesPanel unsavedFilesPanel = new UnsavedFilesPanel();
        unsavedFilesPanel.setUnsavedFiles(fileHandlers);
        ResourceBundle resourceBundle = unsavedFilesPanel.getResourceBundle();
        final boolean[] result = new boolean[1];
        final WindowUtils.DialogWrapper dialog = WindowUtils.createDialog(unsavedFilesPanel, parentComponent, resourceBundle.getString("dialog.title"), Dialog.ModalityType.APPLICATION_MODAL);
        unsavedFilesPanel.setController(new UnsavedFilesPanel.Controller() {
            @Override
            public boolean saveFile(BinEdFileHandler fileHandler) {
                fileHandler.saveFile();
                return !fileHandler.isModified();
            }

            @Override
            public void discardAll(List<BinEdFileHandler> fileHandlers) {
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
