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
package org.exbin.bined.intellij.tool.content.action;

import org.exbin.auxiliary.paged_data.BinaryData;
import org.exbin.bined.intellij.main.BinEdManager;
import org.exbin.bined.swing.extended.ExtCodeArea;
import org.exbin.framework.bined.handler.CodeAreaPopupMenuHandler;
import org.exbin.framework.bined.tool.content.gui.DragDropContentPanel;
import org.exbin.framework.utils.WindowUtils;
import org.exbin.framework.utils.gui.CloseControlPanel;
import org.exbin.xbup.core.util.StreamUtils;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Drag and drop content action.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class DragDropContentAction implements ActionListener {

    public static final String ACTION_ID = "dragDropContentAction";

    private ResourceBundle resourceBundle;
    private final DragDropContentPanel dragDropContentPanel = new DragDropContentPanel();

    public DragDropContentAction() {
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        CloseControlPanel controlPanel = new CloseControlPanel();

        JPanel dialogPanel = WindowUtils.createDialogPanel(dragDropContentPanel, controlPanel);
        final WindowUtils.DialogWrapper dialog = WindowUtils.createDialog(dialogPanel, (Component) event.getSource(), "Drag&Drop Content", Dialog.ModalityType.APPLICATION_MODAL);
        dragDropContentPanel.setSaveAsFileAction(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser exportFileChooser = new JFileChooser();
                exportFileChooser.setAcceptAllFileFilterUsed(true);
                if (exportFileChooser.showSaveDialog(dialog.getWindow()) == JFileChooser.APPROVE_OPTION) {
                    BinaryData binaryData = dragDropContentPanel.getContentBinaryData().orElse(null);
                    if (binaryData == null) {
                        return;
                    }
                    InputStream dataInputStream = binaryData.getDataInputStream();

                    FileOutputStream fileStream;
                    try {
                        fileStream = new FileOutputStream(exportFileChooser.getSelectedFile().getAbsolutePath());
                        try {
                            StreamUtils.copyInputStreamToOutputStream(dataInputStream, fileStream);
                        } finally {
                            fileStream.close();
                        }
                    } catch (FileNotFoundException ex) {
                        Logger.getLogger(DragDropContentAction.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IOException ex) {
                        Logger.getLogger(DragDropContentAction.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        });

        dragDropContentPanel.setCodeAreaPopupMenuHandler(new CodeAreaPopupMenuHandler() {
            @NotNull @Override
            public JPopupMenu createPopupMenu(ExtCodeArea codeArea, String menuPostfix, int x, int y) {
                BinEdManager binEdManager = BinEdManager.getInstance();
                JPopupMenu popupMenu = new JPopupMenu();
                binEdManager.createContextMenu(dragDropContentPanel.getDataCodeArea(), popupMenu, BinEdManager.PopupMenuVariant.BASIC, x, y);
                return popupMenu;
            }

            @Override public void dropPopupMenu(String menuPostfix) {

            }
        });

        controlPanel.setHandler(() -> {
            dialog.close();
            dialog.dispose();
        });
        dialog.showCentered((Component) event.getSource());
    }
}
