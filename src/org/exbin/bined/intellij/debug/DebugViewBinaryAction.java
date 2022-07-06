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
package org.exbin.bined.intellij.debug;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.xdebugger.impl.ui.tree.XDebuggerTree;
import com.intellij.xdebugger.impl.ui.tree.actions.XFetchValueActionBase;
import com.intellij.xdebugger.impl.ui.tree.nodes.XValueNodeImpl;
import org.exbin.bined.intellij.debug.intellij.XValueNodeConvertor;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Show debugger value in hexadecimal editor action.
 *
 * @author ExBin Project (http://exbin.org)
 * @version 0.2.2 2022/05/15
 */
@ParametersAreNonnullByDefault
public class DebugViewBinaryAction extends XFetchValueActionBase implements DumbAware {

    @Override
    protected void handle(Project project, String value, XDebuggerTree tree) {
    }

    @Nonnull
    @Override
    protected ValueCollector createCollector(AnActionEvent e) {
        XValueNodeImpl node = XValueNodeConvertor.getDataNode(e).orElse(null);
        return new ValueCollector(XDebuggerTree.getTree(e.getDataContext())) {
            DebugViewDataDialog dialog = null;

            @Override
            public void handleInCollector(Project project, String value, XDebuggerTree tree) {
                String text = StringUtil.unquoteString(value);
                if (dialog == null) {
                    ApplicationManager.getApplication().invokeLater(() -> {
                        dialog = new DebugViewDataDialog(project, text, node);
                        dialog.setTitle("View as Binary");
                        dialog.setText(text);
                        dialog.show();
                    });
                } else {
                    dialog.setText(text);
                }
            }
        };
    }

    @Override
    public void update(AnActionEvent event) {
        super.update(event);
//        if (XValueNodeConvertor.getDataNode(event).isPresent()) {
//            event.getPresentation().setText("View as Binary");
//        }
    }
}
