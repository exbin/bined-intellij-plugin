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
package org.exbin.deltahex.intellij;

import com.intellij.debugger.engine.JavaValue;
import com.intellij.debugger.ui.impl.watch.ValueDescriptorImpl;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.CommonClassNames;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.components.BorderLayoutPanel;
import com.intellij.xdebugger.frame.XValue;
import com.intellij.xdebugger.impl.ui.tree.XDebuggerTree;
import com.intellij.xdebugger.impl.ui.tree.actions.XDebuggerTreeActionBase;
import com.intellij.xdebugger.impl.ui.tree.actions.XFetchValueActionBase;
import com.intellij.xdebugger.impl.ui.tree.nodes.XValueNodeImpl;
import com.sun.jdi.ArrayReference;
import com.sun.jdi.ArrayType;
import org.exbin.deltahex.intellij.debug.*;
import org.exbin.deltahex.intellij.panel.DebugViewPanel;
import org.exbin.utils.binary_data.BinaryData;
import org.exbin.utils.binary_data.ByteArrayData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.nio.charset.Charset;
import java.util.List;

/**
 * Show debugger value in hexadecimal editor action.
 *
 * @author ExBin Project (http://exbin.org)
 * @version 0.1.3 2017/03/20
 */
public class DebugViewHexAction extends XFetchValueActionBase {

    @Override
    protected void handle(Project project, String value, XDebuggerTree tree) {
    }

    @NotNull
    @Override
    protected ValueCollector createCollector(@NotNull AnActionEvent e) {
        XValueNodeImpl node = getStringNode(e);
        return new ValueCollector(XDebuggerTree.getTree(e.getDataContext())) {
            DataDialog dialog = null;

            @Override
            public void handleInCollector(Project project, String value, XDebuggerTree tree) {
                String text = StringUtil.unquoteString(value);
                if (dialog == null) {
                    dialog = new DataDialog(project, text, node);
                    dialog.setTitle("View as Hexadecimal Data");
                    dialog.show();
                }
                dialog.setText(text);
            }
        };
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
        if (getStringNode(e) != null) {
            e.getPresentation().setText("View Hex");
//            e.getPresentation().setIcon("/images/icon.png");
        }
    }

    private static XValueNodeImpl getStringNode(@NotNull AnActionEvent e) {
        List<XValueNodeImpl> selectedNodes = XDebuggerTreeActionBase.getSelectedNodes(e.getDataContext());
        if (selectedNodes.size() == 1) {
            XValueNodeImpl node = selectedNodes.get(0);
            XValue container = node.getValueContainer();
            if (container instanceof JavaValue && container.getModifier() != null) {
                ValueDescriptorImpl descriptor = ((JavaValue) container).getDescriptor();
                if (descriptor.isString() || descriptor.isArray()) {
                    return node;
                }
            }
        }
        return null;
    }

    private static class DataDialog extends DialogWrapper {
        private final DebugViewPanel viewPanel;
        private final XValueNodeImpl myDataNode;

        private DataDialog(Project project, @Nullable String initialValue, @Nullable XValueNodeImpl dataNode) {
            super(project, false);
            myDataNode = dataNode;
            setModal(false);
            setCancelButtonText("Close");
            setOKButtonText("Set");
            getOKAction().setEnabled(false);
            setCrossClosesWindow(true);

            viewPanel = new DebugViewPanel();

            if (myDataNode != null) {
                XValue container = myDataNode.getValueContainer();
                ValueDescriptorImpl descriptor = ((JavaValue) container).getDescriptor();
                BinaryData data = null;
                if (descriptor.isArray()) {
                    final ArrayReference arrayRef = (ArrayReference) descriptor.getValue();
                    final ArrayType type = (ArrayType) descriptor.getType();
                    if (type != null) {
                        final String componentType = type.componentTypeName();
                        switch (componentType) {
                            case CommonClassNames.JAVA_LANG_BYTE:
                            case "byte": {
                                data = new DebugViewDataSource(new ByteArrayPageProvider(arrayRef));
                                break;
                            }
                            case CommonClassNames.JAVA_LANG_SHORT:
                            case "short": {
                                data = new DebugViewDataSource(new ShortArrayPageProvider(arrayRef));
                                break;
                            }
                            case CommonClassNames.JAVA_LANG_INTEGER:
                            case "int": {
                                data = new DebugViewDataSource(new IntegerArrayPageProvider(arrayRef));
                                break;
                            }
                            case CommonClassNames.JAVA_LANG_LONG:
                            case "long": {
                                data = new DebugViewDataSource(new LongArrayPageProvider(arrayRef));
                                break;
                            }
                            case CommonClassNames.JAVA_LANG_FLOAT:
                            case "float": {
                                data = new DebugViewDataSource(new FloatArrayPageProvider(arrayRef));
                                break;
                            }
                            case CommonClassNames.JAVA_LANG_DOUBLE:
                            case "double": {
                                data = new DebugViewDataSource(new DoubleArrayPageProvider(arrayRef));
                                break;
                            }
                            // TODO
                        }
                    }
                } else {
                    data = new ByteArrayData(myDataNode.getRawValue().getBytes(Charset.defaultCharset()));
                }

                if (data == null) {
                    data = new ByteArrayData(new byte[0]);
                }

                viewPanel.setData(data);
            }

            init();
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

        @Override
        @NotNull
        protected Action[] createActions() {
            return myDataNode != null ? new Action[]{getOKAction(), getCancelAction()} : new Action[]{getCancelAction()};
        }

        @Nullable
        @Override
        public JComponent getPreferredFocusedComponent() {
            return viewPanel;
        }

        @Override
        protected String getDimensionServiceKey() {
            return "#org.exbin.deltahex.intellij.DebugViewHexAction";
        }

        @Override
        protected JComponent createCenterPanel() {
            BorderLayoutPanel panel = JBUI.Panels.simplePanel(viewPanel);
            panel.setPreferredSize(JBUI.size(600, 400));
            return panel;
        }
    }
}
