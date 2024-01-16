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
package org.exbin.framework.bined.operation.action;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.exbin.auxiliary.binary_data.BinaryData;
import org.exbin.bined.CodeAreaUtils;
import org.exbin.bined.operation.BinaryDataOperationException;
import org.exbin.bined.operation.swing.CodeAreaOperationCommandHandler;
import org.exbin.bined.operation.swing.command.CodeAreaCommand;
import org.exbin.bined.swing.CodeAreaCore;
import org.exbin.bined.swing.CodeAreaSwingUtils;
import org.exbin.bined.swing.basic.DefaultCodeAreaCommandHandler;
import org.exbin.framework.api.XBApplication;
import org.exbin.framework.bined.BinEdFileHandler;
import org.exbin.framework.bined.BinedModule;
import org.exbin.framework.utils.ActionUtils;
import org.exbin.framework.bined.action.CodeAreaAction;
import org.exbin.framework.bined.operation.api.ConvertDataMethod;
import org.exbin.framework.bined.operation.gui.ConvertDataControlHandler;
import org.exbin.framework.bined.operation.gui.ConvertDataControlPanel;
import org.exbin.framework.bined.operation.gui.ConvertDataPanel;
import org.exbin.framework.editor.api.EditorProvider;
import org.exbin.framework.file.api.FileHandler;
import org.exbin.framework.frame.api.FrameModuleApi;
import org.exbin.framework.utils.WindowUtils;

/**
 * Convert data action.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class ConvertDataAction extends AbstractAction implements CodeAreaAction {

    public static final String ACTION_ID = "convertDataAction";

    private static final int PREVIEW_LENGTH_LIMIT = 4096;

    private XBApplication application;
    private ResourceBundle resourceBundle;
    private CodeAreaCore codeArea;
    private EditorProvider editorProvider;
    private ConvertDataMethod lastMethod = null;
    private List<ConvertDataMethod> convertDataComponents;

    public ConvertDataAction() {

    }

    public void setup(XBApplication application, ResourceBundle resourceBundle) {
        this.application = application;
        this.resourceBundle = resourceBundle;

        ActionUtils.setupAction(this, resourceBundle, ACTION_ID);
        putValue(Action.ACCELERATOR_KEY, javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_M, ActionUtils.getMetaMask()));
        putValue(ActionUtils.ACTION_DIALOG_MODE, true);
    }

    @Override
    public void updateForActiveCodeArea(@Nullable CodeAreaCore codeArea) {
        this.codeArea = codeArea;
        setEnabled(codeArea != null);
    }

    public void setEditorProvider(EditorProvider editorProvider) {
        this.editorProvider = editorProvider;
    }

    public void setConvertDataComponents(List<ConvertDataMethod> convertDataComponents) {
        this.convertDataComponents = convertDataComponents;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
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
        convertDataPanel.setComponents(convertDataComponents);
        convertDataPanel.selectActiveMethod(lastMethod);
        BinedModule binedModule = application.getModuleRepository().getModuleByInterface(BinedModule.class);
        convertDataPanel.setCodeAreaPopupMenuHandler(binedModule.createCodeAreaPopupMenuHandler(BinedModule.PopupMenuVariant.NORMAL));
        FrameModuleApi frameModule = application.getModuleRepository().getModuleByInterface(FrameModuleApi.class);
        final WindowUtils.DialogWrapper dialog = WindowUtils.createDialog(dialogPanel, codeArea, "", Dialog.ModalityType.APPLICATION_MODAL);
        WindowUtils.addHeaderPanel(dialog.getWindow(), convertDataPanel.getClass(), panelResourceBundle);
        frameModule.setDialogTitle(dialog, panelResourceBundle);
        controlPanel.setHandler((ConvertDataControlHandler.ControlActionType actionType) -> {
            if (actionType != ConvertDataControlHandler.ControlActionType.CANCEL) {
                Optional<ConvertDataMethod> optionalActiveMethod = convertDataPanel.getActiveMethod();
                if (optionalActiveMethod.isPresent()) {
                    Component activeComponent = convertDataPanel.getActiveComponent().get();
                    ConvertDataMethod activeMethod = optionalActiveMethod.get();

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

                            if (editorProvider != null) {
                                editorProvider.newFile();
                                Optional<FileHandler> activeFile = editorProvider.getActiveFile();
                                if (activeFile.isPresent()) {
                                    BinEdFileHandler fileHandler = (BinEdFileHandler) activeFile.get();
                                    fileHandler.getCodeArea().setContentData(outputData);
                                }
                            }
                            break;
                        }
                        case CONVERT_TO_CLIPBOARD: {
                            try {
                                BinaryData outputData = activeMethod.performDirectConvert(activeComponent, codeArea);
                                DataFlavor binedDataFlavor = new DataFlavor(DefaultCodeAreaCommandHandler.BINED_CLIPBOARD_MIME_FULL);
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
        dialog.showCentered(codeArea);
        convertDataPanel.detachMenu();
    }
}
