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

import org.exbin.auxiliary.paged_data.BinaryData;
import org.exbin.bined.CodeAreaUtils;
import org.exbin.bined.EditOperation;
import org.exbin.bined.capability.CaretCapable;
import org.exbin.bined.capability.EditModeCapable;
import org.exbin.bined.operation.BinaryDataOperationException;
import org.exbin.bined.operation.swing.CodeAreaOperationCommandHandler;
import org.exbin.bined.operation.swing.command.CodeAreaCommand;
import org.exbin.bined.swing.CodeAreaCore;
import org.exbin.bined.swing.CodeAreaSwingUtils;
import org.exbin.bined.swing.basic.DefaultCodeAreaCommandHandler;
import org.exbin.bined.swing.extended.ExtCodeArea;
import org.exbin.framework.bined.operation.api.ConvertDataMethod;
import org.exbin.framework.bined.operation.api.InsertDataMethod;
import org.exbin.framework.bined.operation.gui.ConvertDataControlHandler;
import org.exbin.framework.bined.operation.gui.ConvertDataControlPanel;
import org.exbin.framework.bined.operation.gui.ConvertDataPanel;
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
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Convert data action.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class ConvertDataAction implements ActionListener {

    private static final int PREVIEW_LENGTH_LIMIT = 4096;

    private final CodeAreaCore codeArea;
    private ConvertDataMethod lastMethod = null;

    public ConvertDataAction(ExtCodeArea codeArea) {
        this.codeArea = Objects.requireNonNull(codeArea);
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        final ConvertDataPanel convertDataPanel = new ConvertDataPanel();
        convertDataPanel.setController((previewCodeArea) -> {
            Optional<ConvertDataMethod> optionalActiveMethod = convertDataPanel.getActiveMethod();
            if (optionalActiveMethod.isPresent()) {
                Component activeComponent = convertDataPanel.getActiveComponent().get();
                optionalActiveMethod.get().registerPreviewDataHandler((binaryData) -> {
                    previewCodeArea.setContentData(binaryData);
                }, activeComponent, codeArea, PREVIEW_LENGTH_LIMIT);
            }
        });
        ResourceBundle panelResourceBundle = convertDataPanel.getResourceBundle();
        ConvertDataControlPanel controlPanel = new ConvertDataControlPanel();
        JPanel dialogPanel = WindowUtils.createDialogPanel(convertDataPanel, controlPanel);
//        BinedOperationModule binedBlockEditModule = application.getModuleRepository().getModuleByInterface(BinedOperationModule.class);
//        convertDataPanel.setComponents(binedBlockEditModule.getInsertDataComponents());
        convertDataPanel.selectActiveMethod(lastMethod);
//        BinedModule binedModule = application.getModuleRepository().getModuleByInterface(BinedModule.class);
//        convertDataPanel.setCodeAreaPopupMenuHandler(binedModule.createCodeAreaPopupMenuHandler(BinedModule.PopupMenuVariant.NORMAL));
        final DialogWrapper dialog = WindowUtils.createDialog(dialogPanel, (Component) event.getSource(), "Convert Data", Dialog.ModalityType.APPLICATION_MODAL);

        controlPanel.setHandler((ConvertDataControlHandler.ControlActionType actionType) -> {
            if (actionType != ConvertDataControlHandler.ControlActionType.CANCEL) {
                Optional<ConvertDataMethod> optionalActiveMethod = convertDataPanel.getActiveMethod();
                if (optionalActiveMethod.isPresent()) {
                    Component activeComponent = convertDataPanel.getActiveComponent().get();
                    ConvertDataMethod activeMethod = optionalActiveMethod.get();
                    long dataPosition = ((CaretCapable) codeArea).getDataPosition();

                    switch (actionType) {
                    case CONVERT: {
                        CodeAreaCommand command = activeMethod.createConvertCommand(activeComponent, codeArea);

                        try {
                            ((CodeAreaOperationCommandHandler) codeArea.getCommandHandler()).getUndoHandler().execute(command);
                        } catch (BinaryDataOperationException ex) {
                            Logger.getLogger(ConvertDataAction.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        break;
                    }
                    case CONVERT_TO_NEW_FILE: {
                        BinaryData outputData = activeMethod.performDirectConvert(activeComponent, codeArea);

                        throw new UnsupportedOperationException("Not supported yet.");
                        // TODO
//                        if (editorProvider != null) {
//                            editorProvider.newFile();
//                            Optional<FileHandler> activeFile = editorProvider.getActiveFile();
//                            if (activeFile.isPresent()) {
//                                BinEdFileHandler fileHandler = (BinEdFileHandler) activeFile.get();
//                                fileHandler.getCodeArea().setContentData(outputData);
//                            }
//                        }
//                        break;
                    }
                    case CONVERT_TO_CLIPBOARD: {
                        try {
                            BinaryData outputData = activeMethod.performDirectConvert(activeComponent, codeArea);
                            DataFlavor binedDataFlavor = new DataFlavor(DefaultCodeAreaCommandHandler.BINED_CLIPBOARD_MIME_FULL);;
                            DataFlavor binaryDataFlavor = new DataFlavor(CodeAreaUtils.MIME_CLIPBOARD_BINARY);
                            Clipboard clipboard = CodeAreaSwingUtils.getClipboard();
                            CodeAreaSwingUtils.BinaryDataClipboardData binaryData = new CodeAreaSwingUtils.BinaryDataClipboardData(outputData, binedDataFlavor, binaryDataFlavor, null);
                            clipboard.setContents(binaryData, binaryData);
                        } catch (ClassNotFoundException ex) {
                            Logger.getLogger(ConvertDataAction.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        break;
                    }
                    }
                }
                lastMethod = optionalActiveMethod.orElse(null);
            }

            dialog.close();
            dialog.dispose();
        });
        SwingUtilities.invokeLater(convertDataPanel::initFocus);
        dialog.showCentered((Component) event.getSource());
    }
}
