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
package org.exbin.bined.intellij.debug;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.components.BorderLayoutPanel;
import com.intellij.xdebugger.impl.ui.tree.nodes.XValueNodeImpl;
import org.exbin.bined.intellij.debug.gui.DebugViewPanel;
import org.exbin.bined.intellij.debug.intellij.XValueNodeConvertor;
import org.exbin.framework.App;
import org.exbin.framework.language.api.LanguageModuleApi;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.Action;
import javax.swing.JComponent;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Debugger value dual page data source.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class DebugViewDataDialog extends DialogWrapper {

    private final Project project;
    private final java.util.ResourceBundle resourceBundle = App.getModule(LanguageModuleApi.class).getBundle(DebugViewDataDialog.class);

    private final DebugViewPanel viewPanel;
    private final XValueNodeImpl myDataNode;
    private final XValueNodeConvertor valueNodeConvertor = new XValueNodeConvertor();

    public DebugViewDataDialog(Project project, @Nullable String initialValue, @Nullable XValueNodeImpl dataNode) {
        super(project, false);
        this.project = project;
        myDataNode = dataNode;
        setModal(false);
        setCancelButtonText(resourceBundle.getString("cancelButton.text"));
        setOKButtonText(resourceBundle.getString("setButton.text"));
        getOKAction().setEnabled(false);
        setCrossClosesWindow(true);

        viewPanel = new DebugViewPanel();

        List<DebugViewDataProvider> debugViewDataProviders = valueNodeConvertor.identifyAvailableProviders(myDataNode, initialValue);
        for (DebugViewDataProvider provider : debugViewDataProviders) {
            viewPanel.addProvider(provider);
        }

        init();
    }

    @Nonnull
    public ResourceBundle getResourceBundle() {
        return resourceBundle;
    }

    public void setText(String text) {
        // TODO
    }

    @Override
    protected void doOKAction() {
//            if (myDataNode != null) {
//                DebuggerUIUtil.setTreeNodeValue(myDataNode,
//                        StringUtil.wrapWithDoubleQuote(DebuggerUtils.translateStringValue(codeArea.getData())),
//                        errorMessage -> Messages.showErrorDialog(myDataNode.getTree(), errorMessage));
//            }
        super.doOKAction();
    }

    @Nonnull
    @Override
    protected Action[] createActions() {
        return myDataNode != null ? new Action[]{getOKAction(), getCancelAction()} : new Action[]{getCancelAction()};
    }

    @Nullable
    @Override
    public JComponent getPreferredFocusedComponent() {
        return viewPanel;
    }

    @Nonnull
    @Override
    protected String getDimensionServiceKey() {
        return "#org.exbin.bined.intellij.debug.DebugViewBinaryAction";
    }

    @Nonnull
    @Override
    protected JComponent createCenterPanel() {
        BorderLayoutPanel panel = JBUI.Panels.simplePanel(viewPanel);
        panel.setPreferredSize(JBUI.size(600, 400));
        return panel;
    }
}
