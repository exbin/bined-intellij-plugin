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

import com.intellij.ide.scratch.ScratchFileService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vfs.VirtualFile;
import org.exbin.auxiliary.paged_data.BinaryData;
import org.exbin.bined.CodeAreaUtils;
import org.exbin.bined.intellij.BinEdVirtualFile;
import org.exbin.bined.intellij.BinaryRootType;
import org.exbin.bined.intellij.ContextOpenAsBinaryAction;
import org.exbin.bined.intellij.main.BinEdManager;
import org.exbin.bined.operation.BinaryDataOperationException;
import org.exbin.bined.operation.swing.CodeAreaOperationCommandHandler;
import org.exbin.bined.operation.swing.command.CodeAreaCommand;
import org.exbin.bined.swing.CodeAreaCore;
import org.exbin.bined.swing.CodeAreaSwingUtils;
import org.exbin.bined.swing.basic.DefaultCodeAreaCommandHandler;
import org.exbin.bined.swing.extended.ExtCodeArea;
import org.exbin.framework.bined.handler.CodeAreaPopupMenuHandler;
import org.exbin.framework.bined.operation.api.ConvertDataMethod;
import org.exbin.framework.bined.operation.bouncycastle.component.ComputeHashDataMethod;
import org.exbin.framework.bined.operation.gui.ConvertDataControlHandler;
import org.exbin.framework.bined.operation.gui.ConvertDataControlPanel;
import org.exbin.framework.bined.operation.gui.ConvertDataPanel;
import org.exbin.framework.utils.WindowUtils;
import org.exbin.framework.utils.WindowUtils.DialogWrapper;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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
    private final List<ConvertDataMethod> convertDataComponents = new ArrayList<>();

    public ConvertDataAction(ExtCodeArea codeArea) {
        this.codeArea = Objects.requireNonNull(codeArea);

        ComputeHashDataMethod computeHashDataMethod = new ComputeHashDataMethod();
        addConvertDataComponent(computeHashDataMethod);
    }

    public void addConvertDataComponent(ConvertDataMethod convertDataComponent) {
        convertDataComponents.add(convertDataComponent);
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
        ConvertDataControlPanel controlPanel = new ConvertDataControlPanel();
        JPanel dialogPanel = WindowUtils.createDialogPanel(convertDataPanel, controlPanel);
        convertDataPanel.setComponents(convertDataComponents);
        convertDataPanel.selectActiveMethod(lastMethod);
        convertDataPanel.setCodeAreaPopupMenuHandler(new CodeAreaPopupMenuHandler() {
            @Nonnull
            @Override
            public JPopupMenu createPopupMenu(ExtCodeArea codeArea, String menuPostfix, int x, int y) {
                BinEdManager binEdManager = BinEdManager.getInstance();
                JPopupMenu popupMenu = new JPopupMenu();
                binEdManager.createContextMenu(codeArea, popupMenu, BinEdManager.PopupMenuVariant.BASIC, x, y);
                return popupMenu;
            }

            @Override
            public void dropPopupMenu(String menuPostfix) {

            }
        });
        final DialogWrapper dialog = WindowUtils.createDialog(dialogPanel, (Component) event.getSource(), "Convert Data", Dialog.ModalityType.APPLICATION_MODAL);

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

                        // TODO doesn't work
                        ScratchFileService scratchFileService = ScratchFileService.getInstance();
                        try {
                            VirtualFile virtualFile = scratchFileService.findFile(BinaryRootType.getInstance(), "scratch.bin",
                                    ScratchFileService.Option.create_new_always);
                            Project project = ProjectManager.getInstance().getDefaultProject();
                            BinEdVirtualFile binEdVirtualFile = ContextOpenAsBinaryAction.openValidVirtualFile(project, virtualFile);
                            binEdVirtualFile.getEditorFile().getCodeArea().setContentData(outputData);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
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
        dialog.showCentered((Component) event.getSource());
    }
}
