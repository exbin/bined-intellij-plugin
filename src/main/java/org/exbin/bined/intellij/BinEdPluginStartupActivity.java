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
import com.intellij.diff.tools.fragmented.UnifiedDiffTool;
import com.intellij.diff.tools.simple.SimpleDiffTool;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.extensions.ExtensionPointAdapter;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.extensions.PluginDescriptor;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.startup.ProjectActivity;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.components.BorderLayoutPanel;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import org.exbin.auxiliary.binary_data.BinaryData;
import org.exbin.bined.intellij.api.BinaryViewData;
import org.exbin.bined.intellij.api.BinaryViewHandler;
import org.exbin.bined.intellij.debug.gui.DebugViewPanel;
import org.exbin.bined.intellij.diff.BinEdDiffTool;
import org.exbin.bined.intellij.main.BinEdManager;
import org.exbin.bined.intellij.main.BinEdNativeFile;
import org.exbin.bined.intellij.main.IntelliJPreferencesWrapper;
import org.exbin.bined.intellij.options.IntegrationOptions;
import org.exbin.bined.intellij.preferences.IntegrationPreferences;
import org.exbin.framework.bined.BinEdEditorComponent;
import org.exbin.framework.bined.BinEdFileHandler;
import org.exbin.framework.bined.BinEdFileManager;
import org.exbin.framework.bined.gui.BinEdComponentPanel;
import org.exbin.framework.bined.objectdata.ObjectValueConvertor;
import org.exbin.framework.options.model.LanguageRecord;
import org.exbin.framework.utils.LanguageUtils;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.Action;
import javax.swing.JComponent;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Post startup activity.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public final class BinEdPluginStartupActivity implements ProjectActivity, StartupActivity, StartupActivity.DumbAware {

    private static final ExtensionPointName<BinaryViewData> BINED_VIEW_DATA =
            ExtensionPointName.create("org.exbin.deltahex.intellij.viewBinaryData");
    private static final String BINED_DIFF_TOOL_ID = BinEdDiffTool.class.getCanonicalName();
    private static final List<IntegrationOptionsListener> INTEGRATION_OPTIONS_LISTENERS = new ArrayList<>();
    private static IntegrationOptions initialIntegrationOptions = null;

    private final BinaryViewHandler viewHandler = new MainBinaryViewHandler();
    private final List<BinaryViewData> initialized = new ArrayList<>();

    BinEdPluginStartupActivity() {
    }

    @Nullable
    @Override
    public Object execute(@NotNull Project project, @NotNull Continuation<? super Unit> continuation) {
        runActivity(project);
        return null;
    }

    @Override
    public void runActivity(Project project) {
        if (initialIntegrationOptions == null) {
            ProjectManager.getInstance().addProjectManagerListener(new BinEdVetoableProjectListener());

            try {
                BINED_VIEW_DATA.addExtensionPointListener(new ExtensionPointAdapter<>() {
                    @Override
                    public void extensionListChanged() {
                        initExtensions();
                    }
                }, null);
            } catch (Throwable ex) {
                Logger.getLogger(BinEdPluginStartupActivity.class.getName()).log(Level.SEVERE, "Extension initialization failed", ex);
            }
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
                new IntelliJPreferencesWrapper(PropertiesComponent.getInstance(), BinEdIntelliJPlugin.PLUGIN_PREFIX)
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
                    BinEdFileHandler fileHandler = ((BinEdVirtualFile) file).getEditorFile();
                    BinEdManager binedManager = BinEdManager.getInstance();
                    if (!binedManager.releaseFile(fileHandler)) {
                        ((BinEdVirtualFile) file).setClosing(false);
                        throw new ProcessCanceledException();
                    }
                    ((BinEdVirtualFile) file).setClosing(false);
                }
            }
        });
        connect.subscribe(VirtualFileManager.VFS_CHANGES, new BulkFileListener() {
            @Override
            public void after(@Nonnull List<? extends VFileEvent> events) {
                for (VFileEvent event : events) {
                    VirtualFile virtualFile = event.getFile();
                    if (event.isFromRefresh()) {
                        FileEditor[] allEditors = FileEditorManager.getInstance(project).getAllEditors(virtualFile);
                        for (FileEditor fileEditor : allEditors) {
                            if (fileEditor instanceof BinEdNativeFileEditor) {
                                BinEdNativeFile nativeFile = ((BinEdNativeFileEditor) fileEditor).getNativeFile();
                                nativeFile.reloadFile();
                            }
                        }
                    }
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
        Locale languageLocale = integrationOptions.getLanguageLocale();
        if (languageLocale.equals(Locale.ROOT)) {
            // Try to match to IDE locale
            Locale ideLocale = com.intellij.DynamicBundle.getLocale();
            List<Locale> locales = new ArrayList<>();
            for (LanguageRecord languageRecord : LanguageUtils.getLanguageRecords()) {
                locales.add(languageRecord.getLocale());
            }
            List<Locale.LanguageRange> localeRange = new ArrayList<>();
            String languageTag = ideLocale.toLanguageTag();
            if ("zh-CN".equals(languageTag)) {
                // TODO detect match to zh_Hans somehow
                languageTag = "zh";
            }
            localeRange.add(new Locale.LanguageRange(languageTag));
            List<Locale> match = Locale.filter(localeRange, locales);
            if (!match.isEmpty()) {
                LanguageUtils.setLanguageLocale(match.get(0));
            }
        } else {
            LanguageUtils.setLanguageLocale(languageLocale);
        }
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

        private final BinEdEditorComponent editorComponent;

        private DataDialog(Project project, @Nullable BinaryData binaryData) {
            super(project, false);
            setModal(false);
            setCancelButtonText("Close");
            getOKAction().setEnabled(false);
            setCrossClosesWindow(true);

            BinEdManager binEdManager = BinEdManager.getInstance();
            editorComponent = new BinEdEditorComponent();
            BinEdFileManager fileManager = binEdManager.getFileManager();
            BinEdComponentPanel componentPanel = editorComponent.getComponentPanel();
            fileManager.initComponentPanel(componentPanel);
            binEdManager.initEditorComponent(editorComponent);
            editorComponent.setContentData(binaryData);
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
            return editorComponent.getComponentPanel();
        }

        @Nullable
        @Override
        protected String getDimensionServiceKey() {
            return "#org.exbin.bined.intellij.debug.ViewBinaryAction";
        }

        @Nullable
        @Override
        protected JComponent createCenterPanel() {
            BorderLayoutPanel panel = JBUI.Panels.simplePanel(editorComponent.getStatusPanel());
            panel.setPreferredSize(JBUI.size(600, 400));
            return panel;
        }
    }

    @ParametersAreNonnullByDefault
    public interface IntegrationOptionsListener {
        void integrationInit(IntegrationOptions integrationOptions);
    }
}
