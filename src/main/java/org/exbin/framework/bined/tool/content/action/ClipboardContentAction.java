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
package org.exbin.framework.bined.tool.content.action;

import java.awt.event.ActionEvent;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import org.exbin.auxiliary.binary_data.BinaryData;
import org.exbin.framework.api.XBApplication;
import org.exbin.framework.bined.BinEdFileHandler;
import org.exbin.framework.bined.BinedModule;
import org.exbin.framework.bined.tool.content.gui.ClipboardContentControlPanel;
import org.exbin.framework.bined.tool.content.gui.ClipboardContentPanel;
import org.exbin.framework.editor.api.EditorProvider;
import org.exbin.framework.file.api.FileHandler;
import org.exbin.framework.frame.api.FrameModuleApi;
import org.exbin.framework.utils.ActionUtils;
import org.exbin.framework.utils.WindowUtils;
import org.exbin.xbup.core.util.StreamUtils;

/**
 * Clipboard content action.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class ClipboardContentAction extends AbstractAction {

    public static final String ACTION_ID = "clipboardContentAction";

    private XBApplication application;
    private ResourceBundle resourceBundle;
    private ClipboardContentPanel clipboardContentPanel = new ClipboardContentPanel();

    public ClipboardContentAction() {
    }

    public void setup(XBApplication application, ResourceBundle resourceBundle) {
        this.application = application;
        this.resourceBundle = resourceBundle;

        ActionUtils.setupAction(this, resourceBundle, ACTION_ID);
        putValue(ActionUtils.ACTION_DIALOG_MODE, true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        FrameModuleApi frameModule = application.getModuleRepository().getModuleByInterface(FrameModuleApi.class);
        clipboardContentPanel.loadFromClipboard();
        ClipboardContentControlPanel controlPanel = new ClipboardContentControlPanel();
        final WindowUtils.DialogWrapper dialog = frameModule.createDialog(clipboardContentPanel, controlPanel);
        clipboardContentPanel.setSaveAsFileAction(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser exportFileChooser = new JFileChooser();
                exportFileChooser.setAcceptAllFileFilterUsed(true);
                if (exportFileChooser.showSaveDialog(dialog.getWindow()) == JFileChooser.APPROVE_OPTION) {
                    BinaryData binaryData = clipboardContentPanel.getContentBinaryData().orElse(null);
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
                        Logger.getLogger(ClipboardContentAction.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IOException ex) {
                        Logger.getLogger(ClipboardContentAction.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        });
        BinedModule binedModule = application.getModuleRepository().getModuleByInterface(BinedModule.class);
        clipboardContentPanel.setCodeAreaPopupMenuHandler(binedModule.createCodeAreaPopupMenuHandler(BinedModule.PopupMenuVariant.BASIC));

        frameModule.setDialogTitle(dialog, clipboardContentPanel.getResourceBundle());
        controlPanel.setHandler((actionType) -> {
            switch (actionType) {
                case CLOSE: {
                    dialog.close();
                    dialog.dispose();

                    break;
                }
                case REFRESH: {
                    clipboardContentPanel.loadFromClipboard();
                    break;
                }
            }
        });
        WindowUtils.addHeaderPanel(dialog.getWindow(), clipboardContentPanel.getClass(), clipboardContentPanel.getResourceBundle());
        dialog.showCentered(frameModule.getFrame());
    }

    public void setEditorProvider(EditorProvider editorProvider) {
        clipboardContentPanel.setOpenAsTabAction(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Optional<BinaryData> optContentBinaryData = clipboardContentPanel.getContentBinaryData();
                if (optContentBinaryData.isPresent()) {
                    BinaryData contentBinaryData = optContentBinaryData.get();
                    editorProvider.newFile();
                    Optional<FileHandler> activeFile = editorProvider.getActiveFile();
                    if (activeFile.isPresent()) {
                        BinEdFileHandler fileHandler = (BinEdFileHandler) activeFile.get();
                        fileHandler.getCodeArea().setContentData(contentBinaryData);
                    }
                }
            }
        });
    }
}
