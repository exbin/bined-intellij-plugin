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

import org.exbin.auxiliary.paged_data.ByteArrayEditableData;
import org.exbin.auxiliary.paged_data.EditableBinaryData;
import org.exbin.bined.CodeAreaUtils;
import org.exbin.bined.EditOperation;
import org.exbin.bined.operation.BinaryDataOperationException;
import org.exbin.bined.operation.swing.CodeAreaUndoHandler;
import org.exbin.bined.operation.swing.command.CodeAreaCommand;
import org.exbin.bined.operation.undo.BinaryDataUndoHandler;
import org.exbin.bined.swing.extended.ExtCodeArea;
import org.exbin.framework.bined.BinEdFileHandler;
import org.exbin.framework.bined.SearchCondition;
import org.exbin.framework.bined.gui.BinaryMultilinePanel;
import org.exbin.framework.bined.gui.GoToBinaryPanel;
import org.exbin.framework.bined.gui.InsertDataPanel;
import org.exbin.framework.bined.operation.InsertDataOperation;
import org.exbin.framework.bined.operation.ReplaceDataOperation;
import org.exbin.framework.gui.utils.LanguageUtils;
import org.exbin.framework.gui.utils.WindowUtils;
import org.exbin.framework.gui.utils.WindowUtils.DialogWrapper;
import org.exbin.framework.gui.utils.gui.DefaultControlPanel;
import org.exbin.framework.gui.utils.handler.DefaultControlHandler;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Insert data action.
 *
 * @version 0.2.1 2019/07/21
 * @author ExBin Project (http://exbin.org)
 */
@ParametersAreNonnullByDefault
public class InsertDataAction implements ActionListener {

    private final ResourceBundle resourceBundle = LanguageUtils.getResourceBundleByClass(GoToBinaryPanel.class);
    private final ExtCodeArea codeArea;
    private final BinaryDataUndoHandler undoHandler;

    public InsertDataAction(ExtCodeArea codeArea, BinaryDataUndoHandler undoHandler) {
        this.codeArea = Objects.requireNonNull(codeArea);
        this.undoHandler = undoHandler;
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        final EditableBinaryData sampleBinaryData = new ByteArrayEditableData();
        final InsertDataPanel insertDataPanel = new InsertDataPanel();
        DefaultControlPanel controlPanel = new DefaultControlPanel(insertDataPanel.getResourceBundle());
        JPanel dialogPanel = WindowUtils.createDialogPanel(insertDataPanel, controlPanel);
        final DialogWrapper dialog = WindowUtils.createDialog(dialogPanel, (Component) event.getSource(), "", Dialog.ModalityType.APPLICATION_MODAL);
        insertDataPanel.setControl(() -> {
            final BinaryMultilinePanel multilinePanel = new BinaryMultilinePanel();
            SearchCondition searchCondition = new SearchCondition();
            EditableBinaryData conditionData = new ByteArrayEditableData();
            conditionData.insert(0, sampleBinaryData);
            searchCondition.setBinaryData(conditionData);
            searchCondition.setSearchMode(SearchCondition.SearchMode.BINARY);
            multilinePanel.setCondition(searchCondition);
            DefaultControlPanel controlPanel1 = new DefaultControlPanel();
            JPanel dialogPanel1 = WindowUtils.createDialogPanel(multilinePanel, controlPanel1);
            final DialogWrapper multilineDialog = WindowUtils.createDialog(dialog.getWindow(), Dialog.ModalityType.APPLICATION_MODAL, dialogPanel1);
            //WindowUtils.setDialogTitle(multilineDialog, multilinePanel.getResourceBundle());
            controlPanel1.setHandler((DefaultControlHandler.ControlActionType actionType) -> {
                if (actionType == DefaultControlHandler.ControlActionType.OK) {
                    SearchCondition condition = multilinePanel.getCondition();
                    sampleBinaryData.clear();
                    sampleBinaryData.insert(0, condition.getBinaryData());
                    insertDataPanel.setFillWith(InsertDataOperation.FillWithType.SAMPLE);
                    long dataLength = insertDataPanel.getDataLength();
                    if (dataLength < sampleBinaryData.getDataSize()) {
                        insertDataPanel.setDataLength(sampleBinaryData.getDataSize());
                    }
                }

                multilineDialog.close();
                multilineDialog.dispose();
            });
            multilineDialog.showCentered(dialog.getWindow());
//                    multilinePanel.detachMenu();
        });
        WindowUtils.addHeaderPanel(dialog.getWindow(), insertDataPanel.getClass(), insertDataPanel.getResourceBundle());
        // frameModule.setDialogTitle(dialog, insertDataPanel.getResourceBundle());
        controlPanel.setHandler((DefaultControlHandler.ControlActionType actionType) -> {
            if (actionType == DefaultControlHandler.ControlActionType.OK) {
                insertDataPanel.acceptInput();
                long dataLength = insertDataPanel.getDataLength();
                InsertDataOperation.FillWithType fillWithType = insertDataPanel.getFillWithType();
                EditOperation activeOperation = codeArea.getActiveOperation();
                CodeAreaCommand command;
                switch (activeOperation) {
                    case INSERT: {
                        InsertDataOperation operation = new InsertDataOperation(codeArea, codeArea.getDataPosition(), dataLength, fillWithType, sampleBinaryData);
                        command = new InsertDataOperation.InsertDataCommand(operation);
                        break;
                    }
                    case OVERWRITE: {
                        ReplaceDataOperation operation = new ReplaceDataOperation(codeArea, codeArea.getDataPosition(), dataLength, fillWithType, sampleBinaryData);
                        command = new ReplaceDataOperation.ReplaceDataCommand(operation);
                        break;
                    }
                    default:
                        throw CodeAreaUtils.getInvalidTypeException(activeOperation);
                }
                try {
                    undoHandler.execute(command);
                } catch (BinaryDataOperationException ex) {
                    Logger.getLogger(InsertDataAction.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            dialog.close();
            dialog.dispose();
        });
        SwingUtilities.invokeLater(insertDataPanel::initFocus);
        dialog.showCentered((Component) event.getSource());
    }
}
