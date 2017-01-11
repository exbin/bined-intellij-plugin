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

import com.intellij.openapi.ui.DialogWrapper;
import org.exbin.deltahex.swing.CodeArea;
import org.exbin.framework.deltahex.panel.GoToHexPanel;
import org.exbin.framework.gui.utils.LanguageUtils;
import org.exbin.framework.gui.utils.WindowUtils;
import org.exbin.framework.gui.utils.handler.DefaultControlHandler;
import org.exbin.framework.gui.utils.panel.DefaultControlPanel;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.ResourceBundle;

/**
 * Encodings handler.
 *
 * @version 0.1.4 2016/12/23
 * @author ExBin Project (http://exbin.org)
 */
public class GoToHandler {

    private final ResourceBundle resourceBundle;

    private CodeArea codeArea;
    private Action goToLineAction;

    public GoToHandler(CodeArea codeArea) {
        this.codeArea = codeArea;
        resourceBundle = LanguageUtils.getResourceBundleByClass(GoToHandler.class);
        init();
    }

    private void init() {
    }

    public Action getGoToLineAction() {
        if (goToLineAction == null) {
            goToLineAction = new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    final GoToHexPanel goToPanel = new GoToHexPanel();
                    DefaultControlPanel goToControlPanel = new DefaultControlPanel(goToPanel.getResourceBundle());
                    goToPanel.setCursorPosition(codeArea.getCaretPosition().getDataPosition());
                    goToPanel.setMaxPosition(codeArea.getDataSize());
                    goToPanel.setVisible(true);
                    JPanel dialogPanel = WindowUtils.createDialogPanel(goToPanel, goToControlPanel);
                    WindowUtils.assignGlobalKeyListener(dialogPanel, goToControlPanel.createOkCancelListener());
                    dialogPanel.setVisible(true);
                    final DialogWrapper dialog = DialogUtils.createDialog(dialogPanel, "Go To Position", goToPanel.getInitFocusComponent());
                    goToControlPanel.setHandler(new DefaultControlHandler() {
                        @Override
                        public void controlActionPerformed(DefaultControlHandler.ControlActionType actionType) {
                            if (actionType == DefaultControlHandler.ControlActionType.OK) {
                                goToPanel.acceptInput();
                                codeArea.setCaretPosition(goToPanel.getGoToPosition());
                            }

                            dialog.close(0);
                        }
                    });
                    dialog.showAndGet();
                }
            };
        }
        return goToLineAction;
    }
}
