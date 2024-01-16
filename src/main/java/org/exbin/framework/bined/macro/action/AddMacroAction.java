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

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.util.Optional;
import java.util.ResourceBundle;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.AbstractAction;
import org.exbin.framework.api.XBApplication;
import org.exbin.framework.bined.macro.gui.MacroEditorPanel;
import org.exbin.framework.bined.macro.model.MacroRecord;
import org.exbin.framework.utils.ActionUtils;
import org.exbin.framework.frame.api.FrameModuleApi;
import org.exbin.framework.utils.WindowUtils;
import org.exbin.framework.utils.gui.DefaultControlPanel;

/**
 * Add macro record action.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class AddMacroAction extends AbstractAction {

    public static final String ACTION_ID = "addMacroAction";

    private XBApplication application;
    private ResourceBundle resourceBundle;
    private MacroRecord macroRecord = null;

    public AddMacroAction() {
    }

    public void setup(XBApplication application, ResourceBundle resourceBundle) {
        this.application = application;
        this.resourceBundle = resourceBundle;

        ActionUtils.setupAction(this, resourceBundle, ACTION_ID);
        putValue(ActionUtils.ACTION_DIALOG_MODE, true);
    }

    @Nonnull
    public Optional<MacroRecord> getMacroRecord() {
        return Optional.ofNullable(macroRecord);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final MacroEditorPanel macroEditorPanel = new MacroEditorPanel();
        macroEditorPanel.setMacroRecord(new MacroRecord());
        ResourceBundle panelResourceBundle = macroEditorPanel.getResourceBundle();
        DefaultControlPanel controlPanel = new DefaultControlPanel(panelResourceBundle);

        FrameModuleApi frameModule = application.getModuleRepository().getModuleByInterface(FrameModuleApi.class);
        final WindowUtils.DialogWrapper dialog = frameModule.createDialog(frameModule.getFrame(), Dialog.ModalityType.APPLICATION_MODAL, macroEditorPanel, controlPanel);
        frameModule.setDialogTitle(dialog, panelResourceBundle);
        controlPanel.setHandler((actionType) -> {
            switch (actionType) {
                case OK: {
                    macroRecord = macroEditorPanel.getMacroRecord();
                    break;
                }
                case CANCEL: {
                    macroRecord = null;
                    break;
                }
            }
            dialog.close();
        });

        dialog.showCentered(frameModule.getFrame());
    }
}
