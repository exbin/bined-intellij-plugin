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
package org.exbin.bined.intellij;

import com.intellij.diff.impl.DiffSettingsHolder;
import com.intellij.diff.tools.combined.CombinedDiffComponentFactory;
import com.intellij.diff.tools.combined.CombinedDiffMainUI;
import com.intellij.diff.tools.combined.CombinedDiffTool;
import com.intellij.diff.tools.combined.CombinedDiffViewer;
import com.intellij.diff.tools.fragmented.UnifiedDiffTool;
import com.intellij.diff.tools.simple.SimpleDiffTool;
import com.intellij.openapi.extensions.ExtensionPointAdapter;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.extensions.PluginDescriptor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.components.BorderLayoutPanel;
import org.exbin.auxiliary.paged_data.BinaryData;
import org.exbin.bined.intellij.api.BinaryViewData;
import org.exbin.bined.intellij.api.BinaryViewHandler;
import org.exbin.bined.intellij.data.ObjectValueConvertor;
import org.exbin.bined.intellij.debug.gui.DebugViewPanel;
import org.exbin.bined.intellij.diff.BinEdDiffTool;
import org.exbin.bined.intellij.gui.BinEdComponentFileApi;
import org.exbin.bined.intellij.gui.BinEdComponentPanel;
import org.exbin.bined.intellij.options.IntegrationOptions;
import org.exbin.bined.intellij.preferences.IntegrationPreferences;
import org.exbin.framework.bined.BinEdFileHandler;
import org.exbin.framework.bined.FileHandlingMode;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.Action;
import javax.swing.JComponent;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Post startup activity.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public final class BinEdPluginStartupActivity implements StartupActivity, DumbAware {

    private static final ExtensionPointName<BinaryViewData> BINED_VIEW_DATA =
            ExtensionPointName.create("org.exbin.deltahex.intellij.viewBinaryData");
    private static final String BINED_DIFF_TOOL_ID = BinEdDiffTool.class.getCanonicalName();
    private static final List<IntegrationOptionsListener> INTEGRATION_OPTIONS_LISTENERS = new ArrayList<>();
    private static IntegrationOptions initialIntegrationOptions = null;

    private final BinaryViewHandler viewHandler = new MainBinaryViewHandler();
    private final List<BinaryViewData> initialized = new ArrayList<>();

    BinEdPluginStartupActivity() {
    }

    @Override
    public void runActivity(Project project) {
        if (initialIntegrationOptions == null) {
            ProjectManager.getInstance().addProjectManagerListener(new BinEdVetoableProjectListener());

            BINED_VIEW_DATA.addExtensionPointListener(new ExtensionPointAdapter<>() {
                @Override
                public void extensionListChanged() {
                    initExtensions();
                }
            }, null);
            initExtensions();
            initIntegration();
        }

        projectOpened(project);
    }

    private void initExtensions() {
        BINED_VIEW_DATA.getExtensionList()
                .stream()
                .filter(binaryViewData -> !initialized.contains(binaryViewData))
                .forEach(binaryViewData -> {
                    binaryViewData.passHandler(viewHandler);
                    initialized.add(binaryViewData);
                });
    }

    private static void initIntegration() {
        initialIntegrationOptions = new IntegrationPreferences(
                new IntelliJPreferencesWrapper(BinEdComponentPanel.getPreferences(), BinEdIntelliJPlugin.PLUGIN_PREFIX)
        );
        applyIntegrationOptions(initialIntegrationOptions);
    }

    private static void projectOpened(Project project) {
        MessageBus messageBus = project.getMessageBus();
        MessageBusConnection connect = messageBus.connect();
        connect.subscribe(FileEditorManagerListener.Before.FILE_EDITOR_MANAGER, new FileEditorManagerListener.Before() {
            @Override
            public void beforeFileClosed(@Nonnull FileEditorManager source, @Nonnull VirtualFile file) {
                if (file instanceof BinEdVirtualFile && !((BinEdVirtualFile) file).isMoved()
                        && !((BinEdVirtualFile) file).isClosing()) {
                    ((BinEdVirtualFile) file).setClosing(true);
                    BinEdFileHandler editorPanel = ((BinEdVirtualFile) file).getEditorFile();
                    if (!editorPanel.releaseFile()) {
                        ((BinEdVirtualFile) file).setClosing(false);
                        throw new ProcessCanceledException();
                    }
                    ((BinEdVirtualFile) file).setClosing(false);
                }
            }
        });

        moveBinEdDiffToolToLastPosition();
    }

    private static void moveBinEdDiffToolToLastPosition() {
        try {
            DiffSettingsHolder.DiffSettings settings = DiffSettingsHolder.DiffSettings.getSettings();
            List<String> diffToolsOrder = settings.getDiffToolsOrder();
            if (diffToolsOrder.isEmpty()) {
                // Prefer basic and unified diff tools before added BinEd diff
                diffToolsOrder.add(SimpleDiffTool.class.getCanonicalName());
                diffToolsOrder.add(UnifiedDiffTool.class.getCanonicalName());
                diffToolsOrder.add(BINED_DIFF_TOOL_ID);
            } else {
                for (int i = 0; i < diffToolsOrder.size(); i++) {
                    if (BINED_DIFF_TOOL_ID.equals(diffToolsOrder.get(i))) {
                        diffToolsOrder.remove(i);
                        break;
                    }
                }
                // Add as last option
                diffToolsOrder.add(BINED_DIFF_TOOL_ID);
            }
            settings.setDiffToolsOrder(diffToolsOrder);
        } catch (Exception ex) {
            Logger.getLogger(BinEdPluginStartupActivity.class.getName()).log(Level.SEVERE, "Unable to move BinEd diff tool to lowest priority", ex);
        }
    }

    public static void addIntegrationOptionsListener(IntegrationOptionsListener integrationOptionsListener) {
        INTEGRATION_OPTIONS_LISTENERS.add(integrationOptionsListener);
        if (initialIntegrationOptions != null) {
            integrationOptionsListener.integrationInit(initialIntegrationOptions);
        }
    }

    public static void applyIntegrationOptions(IntegrationOptions integrationOptions) {
        for (IntegrationOptionsListener listener : INTEGRATION_OPTIONS_LISTENERS) {
            listener.integrationInit(integrationOptions);
        }
    }

    @ParametersAreNonnullByDefault
    private static class MainBinaryViewHandler implements BinaryViewHandler {
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
        public void setPluginDescriptor(PluginDescriptor pluginDescriptor) {
            // ignore
        }
    }

    @ParametersAreNonnullByDefault
    private static class DataDialog extends DialogWrapper {

        private final BinEdComponentPanel viewPanel;

        private DataDialog(Project project, @Nullable BinaryData binaryData) {
            super(project, false);
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
                    throw new IllegalStateException("Save not supported");
                }

                @Override
                public void switchFileHandlingMode(FileHandlingMode newHandlingMode) {
                    throw new IllegalStateException("Save not supported");
                }

                @Override
                public void closeData() {
                    // Ignore
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
            return new Action[] { getCancelAction() };
        }

        @Nullable
        @Override
        public JComponent getPreferredFocusedComponent() {
            return viewPanel;
        }

        @Nullable
        @Override
        protected String getDimensionServiceKey() {
            return "#org.exbin.bined.intellij.debug.ViewBinaryAction";
        }

        @Nullable
        @Override
        protected JComponent createCenterPanel() {
            BorderLayoutPanel panel = JBUI.Panels.simplePanel(viewPanel);
            panel.setPreferredSize(JBUI.size(600, 400));
            return panel;
        }
    }

    @ParametersAreNonnullByDefault
    public interface IntegrationOptionsListener {
        void integrationInit(IntegrationOptions integrationOptions);
    }
}
