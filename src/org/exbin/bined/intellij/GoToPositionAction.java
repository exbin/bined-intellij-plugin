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

import org.exbin.bined.swing.extended.ExtCodeArea;
import org.exbin.framework.bined.panel.GoToBinaryPanel;
import org.exbin.framework.gui.utils.LanguageUtils;
import org.exbin.framework.gui.utils.WindowUtils;
import org.exbin.framework.gui.utils.WindowUtils.DialogWrapper;
import org.exbin.framework.gui.utils.handler.DefaultControlHandler;
import org.exbin.framework.gui.utils.panel.DefaultControlPanel;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * Go to handler.
 *
 * @version 0.2.0 2019/03/22
 * @author ExBin Project (http://exbin.org)
 */
@ParametersAreNonnullByDefault
public class GoToPositionAction implements ActionListener {

    private final ResourceBundle resourceBundle = LanguageUtils.getResourceBundleByClass(GoToBinaryPanel.class);
    private final ExtCodeArea codeArea;

    public GoToPositionAction(ExtCodeArea codeArea) {
        this.codeArea = Objects.requireNonNull(codeArea);
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        final GoToBinaryPanel goToPanel = new GoToBinaryPanel();
        DefaultControlPanel goToControlPanel = new DefaultControlPanel(goToPanel.getResourceBundle());
        goToPanel.setCursorPosition(codeArea.getCaretPosition().getDataPosition());
        goToPanel.setMaxPosition(codeArea.getDataSize());
        JPanel dialogPanel = WindowUtils.createDialogPanel(goToPanel, goToControlPanel);
        final DialogWrapper dialog = WindowUtils.createDialog(dialogPanel, null, resourceBundle.getString("dialog.title"), Dialog.ModalityType.APPLICATION_MODAL);

        goToPanel.initFocus();
        goToControlPanel.setHandler((DefaultControlHandler.ControlActionType actionType) -> {
            if (actionType == DefaultControlHandler.ControlActionType.OK) {
                goToPanel.acceptInput();
                codeArea.setCaretPosition(goToPanel.getGoToPosition());
            }

            dialog.close();
        });
        WindowUtils.assignGlobalKeyListener(dialog.getWindow(), goToControlPanel.createOkCancelListener());
        dialog.show();
    }
}
