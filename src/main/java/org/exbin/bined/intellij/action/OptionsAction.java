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
package org.exbin.bined.intellij.action;

import org.exbin.bined.intellij.gui.BinEdOptionsPanel;
import org.exbin.bined.intellij.gui.BinEdOptionsPanelBorder;
import org.exbin.bined.intellij.main.BinEdEditorComponent;
import org.exbin.bined.swing.extended.ExtCodeArea;
import org.exbin.framework.bined.gui.BinEdComponentFileApi;
import org.exbin.framework.bined.preferences.BinaryEditorPreferences;
import org.exbin.framework.editor.text.service.TextFontService;
import org.exbin.framework.utils.WindowUtils;
import org.exbin.framework.utils.gui.OptionsControlPanel;
import org.exbin.framework.utils.handler.OptionsControlHandler;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.AbstractAction;
import javax.swing.JPanel;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;

/**
 * Go to position action.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class OptionsAction extends AbstractAction {

    private final BinEdEditorComponent editorComponent;
    private final BinaryEditorPreferences preferences;

    public OptionsAction(BinEdEditorComponent editorComponent, BinaryEditorPreferences preferences) {
        this.editorComponent = editorComponent;
        this.preferences = preferences;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ExtCodeArea codeArea = editorComponent.getCodeArea();
        BinEdComponentFileApi fileApi = editorComponent.getFileApi();
        final BinEdOptionsPanelBorder optionsPanelWrapper = new BinEdOptionsPanelBorder();
        optionsPanelWrapper.setPreferredSize(new Dimension(700, 460));
        BinEdOptionsPanel optionsPanel = optionsPanelWrapper.getOptionsPanel();
        optionsPanel.setPreferences(preferences);
        optionsPanel.setTextFontService(new TextFontService() {
            @Nonnull
            @Override
            public Font getCurrentFont() {
                return codeArea.getCodeFont();
            }

            @Nonnull
            @Override
            public Font getDefaultFont() {
                return editorComponent.getDefaultFont();
            }

            @Override
            public void setCurrentFont(Font font) {
                codeArea.setCodeFont(font);
            }
        });
        optionsPanel.loadFromPreferences();
        editorComponent.updateApplyOptions(optionsPanel);
        OptionsControlPanel optionsControlPanel = new OptionsControlPanel();
        JPanel dialogPanel = WindowUtils.createDialogPanel(optionsPanelWrapper, optionsControlPanel);
        WindowUtils.DialogWrapper dialog = WindowUtils.createDialog(dialogPanel, editorComponent.getComponentPanel(), "Options", Dialog.ModalityType.APPLICATION_MODAL);
        optionsControlPanel.setHandler((OptionsControlHandler.ControlActionType actionType) -> {
            if (actionType != OptionsControlHandler.ControlActionType.CANCEL) {
                optionsPanel.applyToOptions();
                if (actionType == OptionsControlHandler.ControlActionType.SAVE) {
                    optionsPanel.saveToPreferences();
                }
                editorComponent.applyOptions(optionsPanel);
                fileApi.switchFileHandlingMode(optionsPanel.getEditorOptions().getFileHandlingMode());
                codeArea.repaint();
            }

            dialog.close();
        });
        dialog.showCentered(editorComponent.getComponentPanel());
        dialog.dispose();
    }
}
