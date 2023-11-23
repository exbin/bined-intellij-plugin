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
package org.exbin.bined.intellij.operation.action;

import org.exbin.bined.EditOperation;
import org.exbin.bined.capability.CaretCapable;
import org.exbin.bined.capability.EditModeCapable;
import org.exbin.bined.operation.BinaryDataOperationException;
import org.exbin.bined.operation.swing.CodeAreaOperationCommandHandler;
import org.exbin.bined.operation.swing.command.CodeAreaCommand;
import org.exbin.bined.swing.CodeAreaCore;
import org.exbin.bined.swing.extended.ExtCodeArea;
import org.exbin.framework.bined.operation.api.InsertDataMethod;
import org.exbin.framework.bined.operation.component.RandomDataMethod;
import org.exbin.framework.bined.operation.component.SimpleFillDataMethod;
import org.exbin.framework.bined.operation.gui.InsertDataPanel;
import org.exbin.framework.utils.WindowUtils;
import org.exbin.framework.utils.WindowUtils.DialogWrapper;
import org.exbin.framework.utils.gui.DefaultControlPanel;
import org.exbin.framework.utils.handler.DefaultControlHandler;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Insert data action.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class InsertDataAction implements ActionListener {

    private static final int PREVIEW_LENGTH_LIMIT = 4096;

    private final CodeAreaCore codeArea;
    private InsertDataMethod lastMethod = null;
    private final List<InsertDataMethod> insertDataComponents = new ArrayList<>();

    public InsertDataAction(ExtCodeArea codeArea) {
        this.codeArea = Objects.requireNonNull(codeArea);

        SimpleFillDataMethod simpleFillDataMethod = new SimpleFillDataMethod();
        addInsertDataComponent(simpleFillDataMethod);
        RandomDataMethod randomDataMethod = new RandomDataMethod();
        addInsertDataComponent(randomDataMethod);
    }

    public void addInsertDataComponent(InsertDataMethod insertDataComponent) {
        insertDataComponents.add(insertDataComponent);
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        final InsertDataPanel insertDataPanel = new InsertDataPanel();
        insertDataPanel.setController((previewCodeArea) -> {
            Optional<InsertDataMethod> optionalActiveMethod = insertDataPanel.getActiveMethod();
            if (optionalActiveMethod.isPresent()) {
                Component activeComponent = insertDataPanel.getActiveComponent().get();
                optionalActiveMethod.get().registerPreviewDataHandler((binaryData) -> {
                    previewCodeArea.setContentData(binaryData);
                }, activeComponent, PREVIEW_LENGTH_LIMIT);
            }
        });
        ResourceBundle panelResourceBundle = insertDataPanel.getResourceBundle();
        DefaultControlPanel controlPanel = new DefaultControlPanel();
        JPanel dialogPanel = WindowUtils.createDialogPanel(insertDataPanel, controlPanel);
        insertDataPanel.setComponents(insertDataComponents);
        insertDataPanel.selectActiveMethod(lastMethod);
//        BinedModule binedModule = application.getModuleRepository().getModuleByInterface(BinedModule.class);
//        insertDataPanel.setCodeAreaPopupMenuHandler(binedModule.createCodeAreaPopupMenuHandler(BinedModule.PopupMenuVariant.NORMAL));
        final DialogWrapper dialog = WindowUtils.createDialog(dialogPanel, (Component) event.getSource(), "Insert Data", Dialog.ModalityType.APPLICATION_MODAL);

        controlPanel.setHandler((DefaultControlHandler.ControlActionType actionType) -> {
            if (actionType == DefaultControlHandler.ControlActionType.OK) {
                Optional<InsertDataMethod> optionalActiveMethod = insertDataPanel.getActiveMethod();
                if (optionalActiveMethod.isPresent()) {
                    Component activeComponent = insertDataPanel.getActiveComponent().get();
                    InsertDataMethod activeMethod = optionalActiveMethod.get();
                    long dataPosition = ((CaretCapable) codeArea).getDataPosition();
                    EditOperation activeOperation = ((EditModeCapable) codeArea).getActiveOperation();
                    CodeAreaCommand command =
                            activeMethod.createInsertCommand(activeComponent, codeArea, dataPosition, activeOperation);

                    try {
                        ((CodeAreaOperationCommandHandler) codeArea.getCommandHandler()).getUndoHandler()
                                .execute(command);
                    } catch (BinaryDataOperationException ex) {
                        Logger.getLogger(InsertDataAction.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                lastMethod = optionalActiveMethod.orElse(null);
            }

            dialog.close();
            dialog.dispose();
        });
        SwingUtilities.invokeLater(insertDataPanel::initFocus);
        dialog.showCentered((Component) event.getSource());
    }
}
