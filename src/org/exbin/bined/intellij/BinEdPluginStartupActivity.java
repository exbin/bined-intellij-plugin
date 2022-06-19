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
import org.exbin.bined.intellij.api.BinaryViewData;
import org.exbin.bined.intellij.api.BinaryViewHandler;
import org.exbin.bined.intellij.data.ObjectValueConvertor;
import org.exbin.bined.intellij.debug.gui.DebugViewPanel;
import org.exbin.bined.intellij.gui.BinEdComponentFileApi;
import org.exbin.bined.intellij.gui.BinEdComponentPanel;
import org.exbin.framework.bined.FileHandlingMode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.Action;
import javax.swing.JComponent;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Post startup activity.
 *
 * @author ExBin Project (http://exbin.org)
 * @version 0.2.6 2022/05/15
 */
@ParametersAreNonnullByDefault
public final class BinEdPluginStartupActivity implements StartupActivity, DumbAware {

    private static final ExtensionPointName<BinaryViewData> BINED_VIEW_DATA = ExtensionPointName.create("org.exbin.deltahex.intellij.viewBinaryData");
    private final BinaryViewHandler viewHandler;
    private final List<BinaryViewData> initialized = new ArrayList<>();

    BinEdPluginStartupActivity() {
        viewHandler = new BinaryViewHandler() {

            private final ObjectValueConvertor valueConvertor = new ObjectValueConvertor();

            @Nonnull
            @Override
            public Optional<BinaryData> instanceToBinaryData(Object instance) {
                return valueConvertor.process(instance);
            }

            @Nonnull
            @Override
            public JComponent createBinaryViewPanel(@Nullable BinaryData binaryData) {
                DebugViewPanel viewPanel = new DebugViewPanel();
                viewPanel.setContentData(binaryData);
                return viewPanel;
            }

            @Nonnull
            @Override
            public Optional<JComponent> createBinaryViewPanel(Object instance) {
                Optional<BinaryData> binaryData = valueConvertor.process(instance);
                DebugViewPanel viewPanel = new DebugViewPanel();
                if (binaryData.isPresent()) {
                    viewPanel.setContentData(binaryData.get());
                    return Optional.of(viewPanel);
                }

                return Optional.empty();
            }

            @Nonnull
            @Override
            public DialogWrapper createBinaryViewDialog(@Nullable BinaryData binaryData) {
                Project project = ProjectManager.getInstance().getDefaultProject();
                DataDialog dialog = new DataDialog(project, binaryData);
                dialog.setTitle("View Binary Data");
                return dialog;
            }

            @Nonnull
            @Override
            public DialogWrapper createBinaryViewDialog(Object instance) {
                Optional<BinaryData> binaryData = valueConvertor.process(instance);
                return createBinaryViewDialog(binaryData.orElse(null));
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
        BINED_VIEW_DATA.extensions().filter(binaryViewData -> !initialized.contains(binaryViewData)).forEach(binaryViewData -> {
            binaryViewData.passHandler(viewHandler);
            initialized.add(binaryViewData);
        });
    }

    @ParametersAreNonnullByDefault
    private static class DataDialog extends DialogWrapper {

        private final byte[] valuesCache = new byte[8];
        private final ByteBuffer byteBuffer = ByteBuffer.wrap(valuesCache);

        private final BinEdComponentPanel viewPanel;
        private BinaryData binaryData;

        private DataDialog(Project project, @Nullable BinaryData binaryData) {
            super(project, false);
            this.binaryData = binaryData;
            setModal(false);
            setCancelButtonText("Close");
            getOKAction().setEnabled(false);
            setCrossClosesWindow(true);

            viewPanel = new BinEdComponentPanel();
            viewPanel.setFileApi(new BinEdComponentFileApi() {
                @Override
                public boolean isSaveSupported() {
                    return false;
                }

                @Override
                public void saveDocument() {
                }

                @Override
                public void switchFileHandlingMode(FileHandlingMode newHandlingMode) {
                }

                @Override
                public void closeData() {
                }
            });
            viewPanel.setContentData(binaryData);
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