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
package org.exbin.bined.intellij.action;

import org.exbin.bined.SelectionRange;
import org.exbin.bined.swing.extended.ExtCodeArea;
import org.exbin.framework.bined.gui.EditSelectionPanel;
import org.exbin.framework.bined.gui.GoToBinaryPanel;
import org.exbin.framework.utils.LanguageUtils;
import org.exbin.framework.utils.WindowUtils;
import org.exbin.framework.utils.WindowUtils.DialogWrapper;
import org.exbin.framework.utils.gui.DefaultControlPanel;
import org.exbin.framework.utils.handler.DefaultControlHandler;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.JPanel;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Edit selection action.
 *
 * @version 0.2.1 2019/07/21
 * @author ExBin Project (http://exbin.org)
 */
@ParametersAreNonnullByDefault
public class EditSelectionAction implements ActionListener {

    private final ResourceBundle resourceBundle = LanguageUtils.getResourceBundleByClass(GoToBinaryPanel.class);
    private final ExtCodeArea codeArea;

    public EditSelectionAction(ExtCodeArea codeArea) {
        this.codeArea = Objects.requireNonNull(codeArea);
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        final EditSelectionPanel editSelectionPanel = new EditSelectionPanel();
        editSelectionPanel.setCursorPosition(codeArea.getDataPosition());
        editSelectionPanel.setMaxPosition(codeArea.getDataSize());
        editSelectionPanel.setSelectionRange(codeArea.getSelection());
        DefaultControlPanel controlPanel = new DefaultControlPanel(editSelectionPanel.getResourceBundle());
        JPanel dialogPanel = WindowUtils.createDialogPanel(editSelectionPanel, controlPanel);
        final DialogWrapper dialog = WindowUtils.createDialog(dialogPanel, (Component) event.getSource(), resourceBundle.getString("dialog.title"), Dialog.ModalityType.APPLICATION_MODAL);

        editSelectionPanel.initFocus();
        controlPanel.setHandler((DefaultControlHandler.ControlActionType actionType) -> {
            if (actionType == DefaultControlHandler.ControlActionType.OK) {
                editSelectionPanel.acceptInput();
                Optional<SelectionRange> selectionRange = editSelectionPanel.getSelectionRange();
                if (selectionRange.isPresent()) {
                    codeArea.setSelection(selectionRange.get());
                } else {
                    codeArea.clearSelection();
                }
                codeArea.revealCursor();
            }

            dialog.close();
        });
        dialog.showCentered((Component) event.getSource());
    }
}
