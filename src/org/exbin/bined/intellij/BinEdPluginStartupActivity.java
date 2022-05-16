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
package org.exbin.bined.intellij;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.extensions.ExtensionPointAdapter;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.extensions.PluginDescriptor;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.components.BorderLayoutPanel;
import org.exbin.auxiliary.paged_data.BinaryData;
import org.exbin.bined.intellij.api.BinEdViewData;
import org.exbin.bined.intellij.api.BinEdViewHandler;
import org.exbin.bined.intellij.debug.gui.DebugViewPanel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.Action;
import javax.swing.JComponent;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Post startup activity.
 *
 * @author ExBin Project (http://exbin.org)
 * @version 0.2.6 2022/05/15
 */
@ParametersAreNonnullByDefault
public final class BinEdPluginStartupActivity implements StartupActivity, DumbAware {

    private static final ExtensionPointName<BinEdViewData> BINED_VIEW_DATA = ExtensionPointName.create("org.exbin.deltahex.intellij.viewBinaryData");
    private final BinEdViewHandler viewHandler;
    private final List<BinEdViewData> initialized = new ArrayList<>();

    BinEdPluginStartupActivity() {
        viewHandler = new BinEdViewHandler() {
            @Override
            public void showBinEdViewDialog(@Nullable BinaryData binaryData) {
                Project project = ProjectManager.getInstance().getDefaultProject();
                ApplicationManager.getApplication().invokeLater(() -> {
                    DataDialog dialog = new DataDialog(project, binaryData);
                    dialog.setTitle("View Binary Data");
                    dialog.show();
                });
            }

            @Override
            public void showBinEdViewDialog(Object object) {
                BinaryData data = null;
                Class<?> objectClass = object.getClass();
                if (objectClass.isArray()) {
                    Class<?> componentType = objectClass.getComponentType();
//                    objectClass.getTy
//                    ((ArrayClass) objectClass)
//                    processArrayData();
                } else {

                }
                showBinEdViewDialog(data);
            }

            @Override
            public void setPluginDescriptor(@NotNull PluginDescriptor pluginDescriptor) {
                // ignore
            }
        };
    }

    @Override
    public void runActivity(@NotNull Project project) {
        BINED_VIEW_DATA.addExtensionPointListener(new ExtensionPointAdapter<>() {
            @Override
            public void extensionListChanged() {
                initExtensions();
            }
        }, null);
        initExtensions();
    }

    private void initExtensions() {
        BINED_VIEW_DATA.extensions().filter(binEdViewData -> !initialized.contains(binEdViewData)).forEach(binEdViewData -> {
            binEdViewData.passHandler(viewHandler);
            initialized.add(binEdViewData);
        });
    }

    @ParametersAreNonnullByDefault
    private static class DataDialog extends DialogWrapper {

        private final byte[] valuesCache = new byte[8];
        private final ByteBuffer byteBuffer = ByteBuffer.wrap(valuesCache);

        private final DebugViewPanel viewPanel;
        private BinaryData binaryData;

        private DataDialog(Project project, @Nullable BinaryData binaryData) {
            super(project, false);
            this.binaryData = binaryData;
            setModal(false);
            setCancelButtonText("Close");
            getOKAction().setEnabled(false);
            setCrossClosesWindow(true);

            viewPanel = new DebugViewPanel();

            init();
        }

        @Override
        protected void doOKAction() {
            super.doOKAction();
        }

        @Nonnull
        @Override
        protected Action[] createActions() {
            return new Action[]{getCancelAction()};
        }

        @Nullable
        @Override
        public JComponent getPreferredFocusedComponent() {
            return viewPanel;
        }

        @Override
        protected String getDimensionServiceKey() {
            return "#org.exbin.bined.intellij.debug.ViewBinaryAction";
        }

        @Override
        protected JComponent createCenterPanel() {
            BorderLayoutPanel panel = JBUI.Panels.simplePanel(viewPanel);
            panel.setPreferredSize(JBUI.size(600, 400));
            return panel;
        }
    }
}