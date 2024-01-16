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
package org.exbin.framework.bined.inspector.action;

import java.awt.event.ActionEvent;
import java.util.Optional;
import java.util.ResourceBundle;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.exbin.framework.api.XBApplication;
import org.exbin.framework.utils.ActionUtils;
import org.exbin.framework.editor.api.EditorProvider;
import org.exbin.framework.bined.BinEdFileHandler;
import org.exbin.framework.bined.gui.BinEdComponentPanel;
import org.exbin.framework.bined.inspector.BinEdComponentInspector;
import org.exbin.framework.file.api.FileDependentAction;
import org.exbin.framework.file.api.FileHandler;

/**
 * Show parsing panel action.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class ShowParsingPanelAction extends AbstractAction implements FileDependentAction {

    public static final String ACTION_ID = "showParsingPanelAction";

    private EditorProvider editorProvider;
    private XBApplication application;
    private ResourceBundle resourceBundle;

    public ShowParsingPanelAction() {
    }

    public void setup(XBApplication application, EditorProvider editorProvider, ResourceBundle resourceBundle) {
        this.application = application;
        this.editorProvider = editorProvider;
        this.resourceBundle = resourceBundle;

        ActionUtils.setupAction(this, resourceBundle, ACTION_ID);
        putValue(ActionUtils.ACTION_TYPE, ActionUtils.ActionType.CHECK);
    }

    @Override
    public void updateForActiveFile() {
        Optional<FileHandler> activeFile = editorProvider.getActiveFile();
        Boolean showParsingPanel = null;
        if (activeFile.isPresent()) {
            BinEdComponentPanel component = ((BinEdFileHandler) activeFile.get()).getComponent();
            BinEdComponentInspector componentExtension = component.getComponentExtension(BinEdComponentInspector.class);
            showParsingPanel = componentExtension.isShowParsingPanel();
        }
        setEnabled(activeFile.isPresent());
        if (showParsingPanel != null) {
            putValue(Action.SELECTED_KEY, showParsingPanel);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Optional<FileHandler> activeFile = editorProvider.getActiveFile();
        if (!activeFile.isPresent()) {
            throw new IllegalStateException();
        }

        BinEdComponentPanel component = ((BinEdFileHandler) activeFile.get()).getComponent();
        BinEdComponentInspector componentExtension = component.getComponentExtension(BinEdComponentInspector.class);
        setShowValuesPanel(!componentExtension.isShowParsingPanel());
    }

    public void setShowValuesPanel(boolean show) {
        Optional<FileHandler> activeFile = editorProvider.getActiveFile();
        if (!activeFile.isPresent()) {
            throw new IllegalStateException();
        }

        BinEdComponentPanel component = ((BinEdFileHandler) activeFile.get()).getComponent();
        BinEdComponentInspector componentExtension = component.getComponentExtension(BinEdComponentInspector.class);
        componentExtension.setShowParsingPanel(show);
        putValue(Action.SELECTED_KEY, show);
    }
}
