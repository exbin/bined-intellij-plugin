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
package org.exbin.framework.bined.macro.action;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.Optional;
import java.util.ResourceBundle;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import org.exbin.bined.swing.CodeAreaCommandHandler;
import org.exbin.bined.swing.CodeAreaCore;
import org.exbin.framework.api.XBApplication;
import org.exbin.framework.bined.BinEdFileHandler;
import org.exbin.framework.bined.action.CodeAreaAction;
import org.exbin.framework.bined.macro.MacroManager;
import org.exbin.framework.bined.macro.operation.CodeAreaMacroCommandHandler;
import org.exbin.framework.editor.api.EditorProvider;
import org.exbin.framework.file.api.FileHandler;
import org.exbin.framework.utils.ActionUtils;

/**
 * Execute last macro record action.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class ExecuteLastMacroAction extends AbstractAction implements CodeAreaAction {

    public static final String ACTION_ID = "executeLastMacroAction";

    private XBApplication application;
    private EditorProvider editorProvider;
    private ResourceBundle resourceBundle;
    private MacroManager macroManager;

    public ExecuteLastMacroAction() {
    }

    public void setup(XBApplication application, EditorProvider editorProvider, ResourceBundle resourceBundle) {
        this.application = application;
        this.editorProvider = editorProvider;
        this.resourceBundle = resourceBundle;

        ActionUtils.setupAction(this, resourceBundle, ACTION_ID);
        putValue(Action.ACCELERATOR_KEY, javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_P, ActionUtils.getMetaMask()));
    }

    @Override
    public void updateForActiveCodeArea(@Nullable CodeAreaCore codeArea) {
        boolean enabled = false;
        if (codeArea != null) {
            CodeAreaCommandHandler commandHandler = codeArea.getCommandHandler();
            enabled = commandHandler instanceof CodeAreaMacroCommandHandler && !((CodeAreaMacroCommandHandler) commandHandler).isMacroRecording() && (macroManager.getLastActiveMacro() >= 0);
        }
        setEnabled(enabled);
    }

    public void setMacroManager(MacroManager macroManager) {
        this.macroManager = macroManager;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Optional<FileHandler> activeFile = editorProvider.getActiveFile();
        if (activeFile.isPresent()) {
            BinEdFileHandler fileHandler = (BinEdFileHandler) activeFile.get();
            try {
                macroManager.executeMacro(fileHandler.getCodeArea(), macroManager.getLastActiveMacro());
            } catch (Exception ex) {
                String message = ex.getMessage();
                if (message == null || message.isEmpty()) {
                    message = ex.toString();
                } else if (ex.getCause() != null) {
                    message += ex.getCause().getMessage();
                }
                JOptionPane.showMessageDialog((Component) e.getSource(), message, resourceBundle.getString("macroExecutionFailed"), JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
