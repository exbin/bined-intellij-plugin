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
package org.exbin.framework.bined.action;

import java.awt.event.ActionEvent;
import java.util.Optional;
import java.util.ResourceBundle;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.AbstractAction;
import org.exbin.framework.api.XBApplication;
import org.exbin.framework.bined.BinEdFileHandler;
import org.exbin.framework.editor.api.EditorProvider;
import org.exbin.framework.file.api.FileDependentAction;
import org.exbin.framework.file.api.FileHandler;
import org.exbin.framework.utils.ActionUtils;

/**
 * Reload content of the currently active file.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class ReloadFileAction extends AbstractAction implements FileDependentAction {

    public static final String ACTION_ID = "reloadFileAction";

    private EditorProvider editorProvider;
    private XBApplication application;
    private ResourceBundle resourceBundle;

    public ReloadFileAction() {
    }

    public void setup(XBApplication application, EditorProvider editorProvider, ResourceBundle resourceBundle) {
        this.application = application;
        this.editorProvider = editorProvider;
        this.resourceBundle = resourceBundle;

        ActionUtils.setupAction(this, resourceBundle, ACTION_ID);
    }

    @Override
    public void updateForActiveFile() {
        Optional<FileHandler> activeFile = editorProvider.getActiveFile();
        boolean canBeReloaded = activeFile.isPresent();
        if (canBeReloaded) {
            BinEdFileHandler fileHandler = (BinEdFileHandler) activeFile.get();
            canBeReloaded = fileHandler.getFileUri().isPresent();
        }
        setEnabled(canBeReloaded);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Optional<FileHandler> optActiveFile = editorProvider.getActiveFile();
        if (optActiveFile.isPresent()) {
            BinEdFileHandler fileHandler = (BinEdFileHandler) optActiveFile.get();
            if (editorProvider.releaseFile(fileHandler)) {
                if (fileHandler.getFileUri().isPresent()) {
                    fileHandler.reloadFile();
                }
            }
        }
    }
}
