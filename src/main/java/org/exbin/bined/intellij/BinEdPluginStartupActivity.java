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
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.DocCommandGroupId;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import com.intellij.openapi.editor.actionSystem.EditorActionManager;
import com.intellij.openapi.extensions.ExtensionPointAdapter;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.fileEditor.impl.FileEditorManagerImpl;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.startup.ProjectActivity;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.openapi.ui.JBMenuItem;
import com.intellij.openapi.ui.JBPopupMenu;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.intellij.openapi.wm.impl.IdeBackgroundUtil;
import com.intellij.ui.Graphics2DDelegate;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBCheckBoxMenuItem;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBMenu;
import com.intellij.ui.components.JBRadioButton;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.messages.MessageBusConnection;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import org.exbin.bined.intellij.api.BinaryViewData;
import org.exbin.bined.intellij.api.BinaryViewHandler;
import org.exbin.bined.intellij.diff.BinEdDiffTool;
import org.exbin.bined.intellij.objectdata.MainBinaryViewHandler;
import org.exbin.bined.intellij.settings.IntegrationOptions;
import org.exbin.bined.intellij.settings.gui.IntegrationSettingsPanel;
import org.exbin.bined.intellij.preferences.IntelliJPreferencesWrapper;
import org.exbin.bined.intellij.search.BinEdIntelliJComponentSearch;
import org.exbin.bined.swing.CodeAreaCommandHandler;
import org.exbin.bined.swing.CodeAreaCore;
import org.exbin.framework.App;
import org.exbin.framework.Module;
import org.exbin.framework.ModuleProvider;
import org.exbin.framework.about.AboutModule;
import org.exbin.framework.about.api.AboutModuleApi;
import org.exbin.framework.action.ActionModule;
import org.exbin.framework.action.api.ActionModuleApi;
import org.exbin.framework.action.api.DialogParentComponent;
import org.exbin.framework.bined.BinEdFileHandler;
import org.exbin.framework.bined.BinEdFileManager;
import org.exbin.framework.bined.BinedModule;
import org.exbin.framework.bined.bookmarks.BinedBookmarksModule;
import org.exbin.framework.bined.compare.BinedCompareModule;
import org.exbin.framework.bined.editor.BinedEditorModule;
import org.exbin.framework.bined.inspector.BasicValuesInspector;
import org.exbin.framework.bined.inspector.BasicValuesInspectorProvider;
import org.exbin.framework.bined.inspector.BinEdInspector;
import org.exbin.framework.bined.inspector.BinEdInspectorComponentExtension;
import org.exbin.framework.bined.inspector.BinEdInspectorManager;
import org.exbin.framework.bined.inspector.BinedInspectorModule;
import org.exbin.framework.bined.inspector.gui.BasicValuesPanel;
import org.exbin.framework.bined.inspector.gui.InspectorPanel;
import org.exbin.framework.bined.macro.BinedMacroModule;
import org.exbin.framework.bined.objectdata.BinedObjectDataModule;
import org.exbin.framework.bined.operation.BinedOperationModule;
import org.exbin.framework.bined.operation.bouncycastle.BinedOperationBouncycastleModule;
import org.exbin.framework.bined.operation.code.BinedOperationCodeModule;
import org.exbin.framework.bined.search.BinedSearchModule;
import org.exbin.framework.bined.theme.BinedThemeModule;
import org.exbin.framework.bined.tool.content.BinedToolContentModule;
import org.exbin.framework.bined.viewer.BinedViewerModule;
import org.exbin.framework.component.ComponentModule;
import org.exbin.framework.component.api.ComponentModuleApi;
import org.exbin.framework.context.ContextModule;
import org.exbin.framework.context.api.ActiveContextManagement;
import org.exbin.framework.context.api.ContextModuleApi;
import org.exbin.framework.contribution.ContributionModule;
import org.exbin.framework.contribution.api.ContributionModuleApi;
import org.exbin.framework.contribution.api.GroupSequenceContributionRule;
import org.exbin.framework.contribution.api.PositionSequenceContributionRule;
import org.exbin.framework.contribution.api.SeparationSequenceContributionRule;
import org.exbin.framework.contribution.api.SequenceContribution;
import org.exbin.framework.editor.EditorModule;
import org.exbin.framework.editor.api.EditorModuleApi;
import org.exbin.framework.editor.api.EditorProvider;
import org.exbin.framework.file.FileModule;
import org.exbin.framework.file.api.FileModuleApi;
import org.exbin.framework.frame.FrameModule;
import org.exbin.framework.frame.api.FrameModuleApi;
import org.exbin.framework.help.HelpModule;
import org.exbin.framework.help.api.HelpModuleApi;
import org.exbin.framework.help.online.HelpOnlineModule;
import org.exbin.framework.language.LanguageModule;
import org.exbin.framework.language.api.IconSetProvider;
import org.exbin.framework.language.api.LanguageModuleApi;
import org.exbin.framework.language.api.LanguageProvider;
import org.exbin.framework.menu.MenuModule;
import org.exbin.framework.menu.api.MenuDefinitionManagement;
import org.exbin.framework.menu.api.MenuModuleApi;
import org.exbin.framework.operation.undo.OperationUndoModule;
import org.exbin.framework.operation.undo.api.OperationUndoModuleApi;
import org.exbin.framework.options.OptionsModule;
import org.exbin.framework.options.preferences.FilePreferencesFactory;
import org.exbin.framework.options.settings.OptionsSettingsModule;
import org.exbin.framework.options.settings.api.OptionsSettingsManagement;
import org.exbin.framework.options.settings.api.SettingsComponent;
import org.exbin.framework.options.api.OptionsModuleApi;
import org.exbin.framework.options.settings.api.OptionsSettingsModuleApi;
import org.exbin.framework.options.settings.api.SettingsComponentProvider;
import org.exbin.framework.options.settings.api.SettingsPanelType;
import org.exbin.framework.plugin.language.cs_CZ.LanguageCsCzModule;
import org.exbin.framework.plugin.language.de_DE.LanguageDeDeModule;
import org.exbin.framework.plugin.language.es_ES.LanguageEsEsModule;
import org.exbin.framework.plugin.language.fr_FR.LanguageFrFrModule;
import org.exbin.framework.plugin.language.it_IT.LanguageItItModule;
import org.exbin.framework.plugin.language.ja_JP.LanguageJaJpModule;
import org.exbin.framework.plugin.language.ko_KR.LanguageKoKrModule;
import org.exbin.framework.plugin.language.pl_PL.LanguagePlPlModule;
import org.exbin.framework.plugin.language.ru_RU.LanguageRuRuModule;
import org.exbin.framework.plugin.language.zh_Hans.LanguageZhHansModule;
import org.exbin.framework.plugin.language.zh_Hant.LanguageZhHantModule;
import org.exbin.framework.plugins.iconset.material.IconSetMaterialModule;
import org.exbin.framework.options.api.OptionsStorage;
import org.exbin.framework.toolbar.ToolBarModule;
import org.exbin.framework.toolbar.api.ToolBarModuleApi;
import org.exbin.framework.ui.UiModule;
import org.exbin.framework.ui.api.UiModuleApi;
import org.exbin.framework.ui.settings.gui.LanguageSettingsPanel;
import org.exbin.framework.ui.model.LanguageRecord;
import org.exbin.framework.ui.theme.UiThemeModule;
import org.exbin.framework.ui.theme.api.UiThemeModuleApi;
import org.exbin.framework.utils.UiUtils;
import org.exbin.framework.window.WindowModule;
import org.exbin.framework.window.api.WindowModuleApi;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * Post startup activity.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public final class BinEdPluginStartupActivity implements ProjectActivity, StartupActivity, StartupActivity.DumbAware {

    private static final String BINARY_PLUGIN_ID = "binary";
    private static final ExtensionPointName<BinaryViewData> BINED_VIEW_DATA =
            ExtensionPointName.create("org.exbin.deltahex.intellij.viewBinaryData");
    private static final String BINED_DIFF_TOOL_ID = BinEdDiffTool.class.getCanonicalName();
    private static final List<IntegrationOptionsListener> INTEGRATION_OPTIONS_LISTENERS = new ArrayList<>();
    private static IntegrationOptions initialIntegrationOptions = null;

    private static boolean initialized = false;
    private boolean extensionInitialized = false;
    private final BinaryViewHandler viewHandler = new MainBinaryViewHandler();
    private final List<BinaryViewData> initializedExtensions = new ArrayList<>();

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
        projectOpened(project);
    }

    public static void initialize() {
        if (!initialized) {
            initialized = true;
            AppModuleProvider appModuleProvider = new AppModuleProvider();
            appModuleProvider.createModules();
            App.setModuleProvider(appModuleProvider);
            appModuleProvider.init();

            // EditorActionHandler takes key and selection events for IDE and makes then inaccessible
            // This workaround passes them if editor is binary editor
            // No other method how to handle this was found so far...
            registerActionHandler(IdeActions.ACTION_EDITOR_MOVE_LINE_START, 0, KeyEvent.VK_HOME);
            registerActionHandler(IdeActions.ACTION_EDITOR_MOVE_LINE_START_WITH_SELECTION, KeyEvent.SHIFT_DOWN_MASK, KeyEvent.VK_HOME);
            registerActionHandler(IdeActions.ACTION_EDITOR_TEXT_START, KeyEvent.CTRL_DOWN_MASK | KeyEvent.META_DOWN_MASK, KeyEvent.VK_HOME);
            registerActionHandler(IdeActions.ACTION_EDITOR_TEXT_START_WITH_SELECTION, KeyEvent.SHIFT_DOWN_MASK | KeyEvent.CTRL_DOWN_MASK | KeyEvent.META_DOWN_MASK, KeyEvent.VK_HOME);
            registerActionHandler(IdeActions.ACTION_EDITOR_MOVE_LINE_END, 0, KeyEvent.VK_END);
            registerActionHandler(IdeActions.ACTION_EDITOR_MOVE_LINE_END_WITH_SELECTION, KeyEvent.SHIFT_DOWN_MASK, KeyEvent.VK_END);
            registerActionHandler(IdeActions.ACTION_EDITOR_TEXT_END, KeyEvent.CTRL_DOWN_MASK | KeyEvent.META_DOWN_MASK, KeyEvent.VK_END);
            registerActionHandler(IdeActions.ACTION_EDITOR_TEXT_END_WITH_SELECTION, KeyEvent.SHIFT_DOWN_MASK | KeyEvent.CTRL_DOWN_MASK | KeyEvent.META_DOWN_MASK, KeyEvent.VK_END);
            registerActionHandler(IdeActions.ACTION_EDITOR_BACKSPACE, 0, KeyEvent.VK_BACK_SPACE);
            registerActionHandler(IdeActions.ACTION_EDITOR_CUT, CodeAreaCommandHandler::cut);
            registerActionHandler(IdeActions.ACTION_EDITOR_COPY, CodeAreaCommandHandler::copy);
            registerActionHandler(IdeActions.ACTION_EDITOR_PASTE, CodeAreaCommandHandler::paste);
            registerActionHandler(IdeActions.ACTION_SELECT_ALL, CodeAreaCommandHandler::selectAll);
//            registerActionHandler(IdeActions.ACTION_FIND, KeyEvent.CTRL_DOWN_MASK | KeyEvent.META_DOWN_MASK, KeyEvent.VK_F);
//            registerActionHandler(IdeActions.ACTION_REPLACE, KeyEvent.CTRL_DOWN_MASK | KeyEvent.META_DOWN_MASK, KeyEvent.VK_H);
        }
    }

    private static void registerActionHandler(String actionId, int modifiers, int key) {
        registerActionHandlerCodeArea(actionId, codeAreaComponent ->
            codeAreaComponent.getCommandHandler().keyPressed(new KeyEvent(codeAreaComponent, 0, 0, modifiers, key, KeyEvent.CHAR_UNDEFINED))
        );
    }

    private static void registerActionHandler(String actionId, CodeAreaCommanderAction codeAreaCommanderAction) {
        registerActionHandlerCodeArea(actionId, codeAreaComponent -> codeAreaCommanderAction.perform(codeAreaComponent.getCommandHandler()));
    }

    private static void registerActionHandlerCodeArea(String actionId, CodeAreaCoreAction codeAreaCoreAction) {
        EditorActionManager actionManager = EditorActionManager.getInstance();
        EditorActionHandler actionHandler = actionManager.getActionHandler(actionId);
        if (actionHandler instanceof EditorActionHandler.ForEachCaret) {
            actionManager.setActionHandler(actionId,
                    new EditorActionHandler.ForEachCaret() {
                        @Override
                        protected boolean isEnabledForCaret(@NotNull Editor editor,
                                @NotNull Caret caret,
                                DataContext dataContext) {
                            JComponent component = editor.getComponent();
                            if (component instanceof CodeAreaCore) {
                                return super.isEnabledForCaret(editor, caret, dataContext);
                            }

                            return actionHandler.isEnabled(editor, caret, dataContext);
                        }

                        @Override
                        public boolean executeInCommand(@NotNull Editor editor, DataContext dataContext) {
                            JComponent component = editor.getComponent();
                            if (component instanceof CodeAreaCore) {
                                return super.executeInCommand(editor, dataContext);
                            }

                            return actionHandler.executeInCommand(editor, dataContext);
                        }

                        @Override
                        public boolean runForAllCarets() {
                            return actionHandler.runForAllCarets();
                        }

                        @Override
                        public DocCommandGroupId getCommandGroupId(@NotNull Editor editor) {
                            return actionHandler.getCommandGroupId(editor);
                        }

                        @Override
                        protected void doExecute(@NotNull Editor editor, @NotNull Caret caret, DataContext dataContext) {
                            JComponent component = editor.getComponent();
                            if (component instanceof CodeAreaCore) {
                                codeAreaCoreAction.perform((CodeAreaCore) component);
                                return;
                            }

                            // This should not be reached unless there will be some changes in EditorActionHandler
                            actionHandler.execute(editor, caret, dataContext);
                        }
                    }
            );
        } else {
            actionManager.setActionHandler(actionId,
                    new EditorActionHandler() {
                        @Override
                        protected boolean isEnabledForCaret(@NotNull Editor editor,
                                @NotNull Caret caret,
                                DataContext dataContext) {
                            JComponent component = editor.getComponent();
                            if (component instanceof CodeAreaCore) {
                                return super.isEnabledForCaret(editor, caret, dataContext);
                            }

                            return actionHandler.isEnabled(editor, caret, dataContext);
                        }

                        @Override
                        public boolean executeInCommand(@NotNull Editor editor, DataContext dataContext) {
                            JComponent component = editor.getComponent();
                            if (component instanceof CodeAreaCore) {
                                return super.executeInCommand(editor, dataContext);
                            }

                            return actionHandler.executeInCommand(editor, dataContext);
                        }

                        @Override
                        public boolean runForAllCarets() {
                            return actionHandler.runForAllCarets();
                        }

                        @Override
                        public DocCommandGroupId getCommandGroupId(@NotNull Editor editor) {
                            return actionHandler.getCommandGroupId(editor);
                        }

                        @Override
                        protected void doExecute(@NotNull Editor editor, @Nullable Caret caret, DataContext dataContext) {
                            JComponent component = editor.getComponent();
                            if (component instanceof CodeAreaCore) {
                                codeAreaCoreAction.perform((CodeAreaCore) component);
                                return;
                            }

                            // This should not be reached unless there will be some changes in EditorActionHandler
                            actionHandler.execute(editor, caret, dataContext);
                        }
                    }
            );
        }
    }

    private void initExtensions() {
        BINED_VIEW_DATA.getExtensionList()
                .stream()
                .filter(binaryViewData -> !initializedExtensions.contains(binaryViewData))
                .forEach(binaryViewData -> {
                    binaryViewData.passHandler(viewHandler);
                    initializedExtensions.add(binaryViewData);
                });
    }

    private void projectOpened(Project project) {
        BinEdPluginStartupActivity.initialize();

        if (!extensionInitialized) {
            ProjectManager.getInstance().addProjectManagerListener(new BinEdVetoableProjectListener());

            try {
                BINED_VIEW_DATA.addExtensionPointListener(ApplicationManager.getApplication(), new ExtensionPointAdapter<>() {
                    @Override
                    public void extensionListChanged() {
                        initExtensions();
                    }
                });
            } catch (Throwable ex) {
                Logger.getLogger(BinEdPluginStartupActivity.class.getName()).log(Level.SEVERE, "Extension initialization failed", ex);
            }
            initExtensions();
            extensionInitialized = true;
        }

        MessageBus messageBus = project.getMessageBus();
        MessageBusConnection connect = messageBus.connect();

        BinedModule binedModule = App.getModule(BinedModule.class);
        connect.subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, new FileEditorManagerListener() {
            @Override
            public void fileClosed(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
                if (file instanceof BinEdVirtualFile) {
                    // TODO: FileEditorManagerKeys.CLOSING_TO_REOPEN not available yet
                    Boolean userData = file.getUserData(FileEditorManagerImpl.CLOSING_TO_REOPEN);
                    if (userData == null || !userData) {
                        ((BinEdVirtualFile) file).dispose();
                    }
                }
            }

            @Override
            public void selectionChanged(@Nonnull FileEditorManagerEvent event) {
                BinEdIntelliJEditorProvider editorProvider =
                        (BinEdIntelliJEditorProvider) binedModule.getEditorProvider();
                BinEdFileHandler activeFile = null;
                FileEditor fileEditor = event.getNewEditor();
                if (fileEditor instanceof BinEdFileEditor) {
                    activeFile = ((BinEdFileEditor) fileEditor).getVirtualFile().getEditorFile();
                } else if (fileEditor instanceof BinEdNativeFileEditor) {
                    activeFile = ((BinEdNativeFileEditor) fileEditor).getNativeFile().getEditorFile();
                }
                editorProvider.setActiveFile(activeFile);
            }
        });

        connect.subscribe(FileEditorManagerListener.Before.FILE_EDITOR_MANAGER, new FileEditorManagerListener.Before() {

            private boolean passNext = false;

            @Override
            public void beforeFileClosed(@Nonnull FileEditorManager source, @Nonnull VirtualFile file) {
                if (passNext) {
                    passNext = false;
                    return;
                }

                if (file instanceof BinEdVirtualFile && !((BinEdVirtualFile) file).isClosing()) {
                    ((BinEdVirtualFile) file).setClosing(true);
                    BinEdFileHandler fileHandler = ((BinEdVirtualFile) file).getEditorFile();
                    if (fileHandler.isModified()) {
                        ApplicationManager.getApplication().invokeLater(() -> {
                            boolean released = binedModule.getEditorProvider().releaseFile(fileHandler);
                            ((BinEdVirtualFile) file).setClosing(false);
                            if (released) {
                                passNext = true;
                                FileEditorManager.getInstance(project).closeFile(file);
                            }
                        });
                        throw new ProcessCanceledException();
                    }
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
        LanguageModuleApi languageModule = App.getModule(LanguageModuleApi.class);
        Locale languageLocale = integrationOptions.getLanguageLocale();
        if (languageLocale.equals(Locale.ROOT)) {
            // Try to match to IDE locale
            Locale ideLocale = com.intellij.DynamicBundle.getLocale();
            List<Locale> locales = new ArrayList<>();
            for (LanguageProvider languageRecord : languageModule.getLanguagePlugins()) {
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
                Locale firstMatchLlocale = match.get(0);
                languageModule.switchToLanguage(firstMatchLlocale);
                BinEdIntelliJPlugin.setLocale(firstMatchLlocale);
            } else {
                languageModule.switchToLanguage(Locale.US);
                BinEdIntelliJPlugin.setLocale(Locale.ROOT);
            }
        } else {
            languageModule.switchToLanguage(languageLocale);
            BinEdIntelliJPlugin.setLocale("en-US".equals(languageLocale.toLanguageTag()) ? Locale.ROOT : languageLocale);
        }

        String iconSet = integrationOptions.getIconSet();
        if (!iconSet.isEmpty()) {
            languageModule.switchToIconSet(iconSet);
        }

        for (IntegrationOptionsListener listener : INTEGRATION_OPTIONS_LISTENERS) {
            listener.integrationInit(integrationOptions);
        }
    }

    @ParametersAreNonnullByDefault
    public interface IntegrationOptionsListener {
        void integrationInit(IntegrationOptions integrationOptions);
    }

    @ParametersAreNonnullByDefault
    private static class AppModuleProvider implements ModuleProvider {

        private final Map<Class<?>, Module> modules = new HashMap<>();

        private void createModules() {
            modules.put(LanguageModuleApi.class, new LanguageModule());
            modules.put(ContributionModuleApi.class, new ContributionModule());
            modules.put(ContextModuleApi.class, new ContextModule());
            modules.put(ActionModuleApi.class, new ActionModule());
            modules.put(OperationUndoModuleApi.class, new OperationUndoModule());
            modules.put(OptionsModuleApi.class, new org.exbin.framework.options.OptionsModule());
            modules.put(OptionsSettingsModuleApi.class, new OptionsSettingsModule());
            modules.put(UiModuleApi.class, new UiModule());
            modules.put(UiThemeModuleApi.class, new UiThemeModule());
            modules.put(HelpModuleApi.class, new HelpModule());
            modules.put(MenuModuleApi.class, new MenuModule());
            modules.put(ToolBarModuleApi.class, new ToolBarModule());
            modules.put(ComponentModuleApi.class, new ComponentModule());
            modules.put(WindowModuleApi.class, new WindowModule());
            modules.put(FrameModuleApi.class, new FrameModule());
            modules.put(FileModuleApi.class, new FileModule());
            modules.put(EditorModuleApi.class, new EditorModule());
            modules.put(HelpOnlineModule.class, new HelpOnlineModule());
            modules.put(BinedModule.class, new BinedModule());
            modules.put(BinedViewerModule.class, new BinedViewerModule());
            modules.put(BinedEditorModule.class, new BinedEditorModule());
            modules.put(BinedThemeModule.class, new BinedThemeModule());
            modules.put(BinedSearchModule.class, new BinedSearchModule());
            modules.put(BinedOperationModule.class, new BinedOperationModule());
            modules.put(BinedOperationCodeModule.class, new BinedOperationCodeModule());
            modules.put(BinedOperationBouncycastleModule.class, new BinedOperationBouncycastleModule());
            modules.put(BinedObjectDataModule.class, new BinedObjectDataModule());
            modules.put(BinedToolContentModule.class, new BinedToolContentModule());
            modules.put(BinedCompareModule.class, new BinedCompareModule());
            modules.put(BinedInspectorModule.class, new BinedInspectorModule());
            modules.put(BinedBookmarksModule.class, new BinedBookmarksModule());
            modules.put(BinedMacroModule.class, new BinedMacroModule());
            modules.put(AboutModuleApi.class, new AboutModule());

            // Language plugins
            modules.put(LanguageCsCzModule.class, new LanguageCsCzModule());
            modules.put(LanguageDeDeModule.class, new LanguageDeDeModule());
            modules.put(LanguageEsEsModule.class, new LanguageEsEsModule());
            modules.put(LanguageFrFrModule.class, new LanguageFrFrModule());
            modules.put(LanguageItItModule.class, new LanguageItItModule());
            modules.put(LanguageJaJpModule.class, new LanguageJaJpModule());
            modules.put(LanguageKoKrModule.class, new LanguageKoKrModule());
            modules.put(LanguagePlPlModule.class, new LanguagePlPlModule());
            modules.put(LanguageRuRuModule.class, new LanguageRuRuModule());
            modules.put(LanguageZhHansModule.class, new LanguageZhHansModule());
            modules.put(LanguageZhHantModule.class, new LanguageZhHantModule());

            // Iconset plugins
            modules.put(IconSetMaterialModule.class, new IconSetMaterialModule());
        }

        private void init() {
            OptionsModule optionsModule = (OptionsModule) App.getModule(OptionsModuleApi.class);
            optionsModule.setAppOptions(new IntelliJPreferencesWrapper(PropertiesComponent.getInstance(), BinEdIntelliJPlugin.PLUGIN_PREFIX));
            convertIncorrectPreferences();

            OptionsStorage preferences = optionsModule.getAppOptions();

            UiUtils.setMenuBuilder(new UiUtils.MenuBuilder() {
                @Nonnull
                public JMenu buildMenu() {
                    return new JBMenu();
                }

                @Nonnull
                public JPopupMenu buildPopupMenu() {
                    return new JBPopupMenu();
                }

                @Nonnull
                public JMenuItem buildMenuItem() {
                    return new JBMenuItem("");
                }

                @Nonnull
                public JCheckBoxMenuItem buildCheckBoxMenuItem() {
                    return new JBCheckBoxMenuItem();
                }

                @Nonnull
                public JRadioButtonMenuItem buildRadioButtonMenuItem() {
                    return new JRadioButtonMenuItem();
                }
            });

            App.getModule(LanguageCsCzModule.class).register();
            App.getModule(LanguageDeDeModule.class).register();
            App.getModule(LanguageEsEsModule.class).register();
            App.getModule(LanguageFrFrModule.class).register();
            App.getModule(LanguageItItModule.class).register();
            App.getModule(LanguageJaJpModule.class).register();
            App.getModule(LanguageKoKrModule.class).register();
            App.getModule(LanguagePlPlModule.class).register();
            App.getModule(LanguageRuRuModule.class).register();
            App.getModule(LanguageZhHansModule.class).register();
            App.getModule(LanguageZhHantModule.class).register();
            App.getModule(IconSetMaterialModule.class).register();

            BinedBookmarksModule binedBookmarksModule = App.getModule(BinedBookmarksModule.class);
            binedBookmarksModule.register();
            BinedMacroModule binedMacroModule = App.getModule(BinedMacroModule.class);
            binedMacroModule.register();
            BinedOperationCodeModule binedOperationCodeModule = App.getModule(BinedOperationCodeModule.class);
            binedOperationCodeModule.register();
            BinedOperationBouncycastleModule binedOperationBouncycastleModule = App.getModule(BinedOperationBouncycastleModule.class);
            binedOperationBouncycastleModule.register();

            LanguageModuleApi languageModule = App.getModule(LanguageModuleApi.class);
            ResourceBundle bundle = languageModule.getBundle(BinEdIntelliJPlugin.class);
            languageModule.setAppBundle(bundle);

            initialIntegrationOptions = new IntegrationOptions(preferences);
            applyIntegrationOptions(initialIntegrationOptions);

            UiModuleApi uiModule = App.getModule(UiModuleApi.class);
            uiModule.executePostInitActions();
            FrameModuleApi frameModule = App.getModule(FrameModuleApi.class);
            frameModule.init();
            ActionModuleApi actionModule = App.getModule(ActionModuleApi.class);
            MenuModuleApi menuModule = App.getModule(MenuModuleApi.class);

            WindowModuleApi windowModule = App.getModule(WindowModuleApi.class);
            windowModule.setHideHeaderPanels(true);

            AboutModuleApi aboutModule = App.getModule(AboutModuleApi.class);
            OptionsSettingsModuleApi optionsSettingsModule = App.getModule(OptionsSettingsModuleApi.class);
            optionsSettingsModule.setSettingsPanelType(SettingsPanelType.LIST);
            optionsSettingsModule.setOptionsRootCaption(App.getModule(LanguageModuleApi.class).getBundle(
                    IntegrationSettingsPanel.class).getString("options.caption"));
            // TODO Is currently stealing options action on macOS
            // optionsModule.registerMenuAction();

            HelpOnlineModule helpOnlineModule = App.getModule(HelpOnlineModule.class);
            try {
                helpOnlineModule.setOnlineHelpUrl(new URI(bundle.getString("online_help_url")).toURL());
                helpOnlineModule.registerOpeningHandler();
            } catch (MalformedURLException | URISyntaxException ex) {
                Logger.getLogger(BinEdPluginStartupActivity.class.getName()).log(Level.SEVERE, null, ex);
            }

            BinEdIntelliJEditorProvider editorProvider = new BinEdIntelliJEditorProvider();
            EditorModuleApi editorModule = App.getModule(EditorModuleApi.class);
            editorModule.registerEditor(BINARY_PLUGIN_ID, editorProvider);
            BinedModule binedModule = App.getModule(BinedModule.class);
            BinedViewerModule binedViewerModule = App.getModule(BinedViewerModule.class);
            BinedEditorModule binedEditorModule = App.getModule(BinedEditorModule.class);
            BinedThemeModule binedThemeModule = App.getModule(BinedThemeModule.class);
            binedModule.setEditorProvider(editorProvider);
            binedBookmarksModule.getBookmarksManager().setEditorProvider(editorProvider);
            binedMacroModule.setEditorProvider(editorProvider);

            BinedSearchModule binedSearchModule = App.getModule(BinedSearchModule.class);
            binedSearchModule.setEditorProvider(editorProvider);
            BinEdFileManager fileManager = binedModule.getFileManager();
            fileManager.addBinEdComponentExtension(component -> Optional.of(new BinEdIntelliJComponentSearch()));

            BinedOperationModule binedOperationModule = App.getModule(BinedOperationModule.class);
            binedOperationModule.addBasicMethods();

            BinedToolContentModule binedToolContentModule = App.getModule(BinedToolContentModule.class);

            BinedInspectorModule binedInspectorModule = App.getModule(BinedInspectorModule.class);
            BinEdInspectorManager binEdInspectorManager = binedInspectorModule.getBinEdInspectorManager();
            binedInspectorModule.setEditorProvider(editorProvider, new BinEdInspectorComponentExtension.ComponentsProvider() {
                @Nonnull
                public InspectorPanel createInspectorPanel() {
                    return new InspectorPanel() {
                        private Graphics2DDelegate graphicsCache = null;

                        @Nonnull
                        @Override
                        protected Graphics getComponentGraphics(Graphics g) {
                            if (g instanceof Graphics2DDelegate) {
                                return g;
                            }

                            if (graphicsCache != null && graphicsCache.getDelegate() == g) {
                                return graphicsCache;
                            }

                            if (graphicsCache != null) {
                                graphicsCache.dispose();
                            }

                            Graphics2D editorGraphics = IdeBackgroundUtil.withEditorBackground(g, this);
                            graphicsCache = editorGraphics instanceof Graphics2DDelegate ? (Graphics2DDelegate) editorGraphics : new Graphics2DDelegate(editorGraphics);
                            return graphicsCache;
                        }
                    };
                }

                @Nonnull
                public JScrollPane createScrollPane() {
                    return new JBScrollPane();
                }
            });
            binEdInspectorManager.removeAllInspectors();
            binEdInspectorManager.addInspector(new BasicValuesInspectorProvider() {
                @Nonnull
                @Override
                public BinEdInspector createInspector() {
                    return new BasicValuesInspector() {

                        @Nonnull
                        @Override
                        protected BasicValuesPanel createComponent() {
                            return new BasicValuesPanel() {
                                @Nonnull
                                @Override
                                protected JTextField createTextField() {
                                    return new JBTextField();
                                }

                                @Nonnull
                                @Override
                                protected JRadioButton createRadioButton() {
                                    return new JBRadioButton() {
                                        private Graphics2DDelegate graphicsCache = null;

                                        @Nonnull
                                        @Override
                                        protected Graphics getComponentGraphics(Graphics g) {
                                            if (g instanceof Graphics2DDelegate) {
                                                return g;
                                            }

                                            if (graphicsCache != null && graphicsCache.getDelegate() == g) {
                                                return graphicsCache;
                                            }

                                            if (graphicsCache != null) {
                                                graphicsCache.dispose();
                                            }

                                            Graphics2D editorGraphics = IdeBackgroundUtil.withEditorBackground(g, this);
                                            graphicsCache = editorGraphics instanceof Graphics2DDelegate ?
                                                    (Graphics2DDelegate) editorGraphics :
                                                    new Graphics2DDelegate(editorGraphics);
                                            return graphicsCache;
                                        }
                                    };
                                }

                                @Nonnull
                                @Override
                                protected JLabel createLabel() {
                                    return new JBLabel();
                                }

                                @Nonnull
                                @Override
                                protected JCheckBox createCheckBox() {
                                    return new JBCheckBox() {
                                        private Graphics2DDelegate graphicsCache = null;

                                        @Nonnull
                                        @Override
                                        protected Graphics getComponentGraphics(Graphics g) {
                                            if (g instanceof Graphics2DDelegate) {
                                                return g;
                                            }

                                            if (graphicsCache != null && graphicsCache.getDelegate() == g) {
                                                return graphicsCache;
                                            }

                                            if (graphicsCache != null) {
                                                graphicsCache.dispose();
                                            }

                                            Graphics2D editorGraphics = IdeBackgroundUtil.withEditorBackground(g, this);
                                            graphicsCache = editorGraphics instanceof Graphics2DDelegate ?
                                                    (Graphics2DDelegate) editorGraphics :
                                                    new Graphics2DDelegate(editorGraphics);
                                            return graphicsCache;
                                        }
                                    };
                                }
                            };
                        }
                    };
                }
            });
            binedInspectorModule.registerShowParsingPanelMenuActions();
            binedInspectorModule.registerShowParsingPanelPopupMenuActions();

            BinedCompareModule binedCompareModule = App.getModule(BinedCompareModule.class);
            binedCompareModule.registerToolsOptionsMenuActions();

            OptionsSettingsManagement settingsManager = optionsSettingsModule.getMainSettingsManager();
            settingsManager.registerOptionsSettings(IntegrationOptions.class, IntegrationOptions::new);
            settingsManager.registerComponent("integration",
                    new SettingsComponentProvider() {
                        @Nonnull
                        @Override
                        public SettingsComponent createComponent() {
                            IntegrationSettingsPanel panel = new IntegrationSettingsPanel();
                            ResourceBundle resourceBundle = App.getModule(LanguageModuleApi.class).getBundle(LanguageSettingsPanel.class);
                            panel.setDefaultLocaleName("<" + resourceBundle.getString("locale.defaultLanguage") + ">");
                            List<LanguageRecord> languageLocales = new ArrayList<>();
                            languageLocales.add(new LanguageRecord(Locale.ROOT, null));
                            languageLocales.add(new LanguageRecord(Locale.forLanguageTag("en-US"), new ImageIcon(getClass().getResource(resourceBundle.getString("locale.englishFlag")))));

                            List<LanguageRecord> languageRecords = new ArrayList<>();
                            List<LanguageProvider> languagePlugins = languageModule.getLanguagePlugins();
                            for (LanguageProvider languageProvider : languagePlugins) {
                                languageRecords.add(new LanguageRecord(languageProvider.getLocale(), languageProvider.getFlag().orElse(null)));
                            }
                            languageLocales.addAll(languageRecords);

                            List<String> iconSets = new ArrayList<>();
                            iconSets.add("");
                            List<String> iconSetNames = new ArrayList<>();
                            UiThemeModule themeModule = (UiThemeModule) App.getModule(UiThemeModuleApi.class);
                            ResourceBundle themeResourceBundle = themeModule.getResourceBundle();
                            iconSetNames.add(themeResourceBundle.getString("iconset.defaultTheme"));
                            List<IconSetProvider> providers = App.getModule(LanguageModuleApi.class).getIconSets();
                            for (IconSetProvider provider : providers) {
                                iconSets.add(provider.getId());
                                iconSetNames.add(provider.getName());
                            }

                            panel.setLanguageLocales(languageLocales);
                            panel.setIconSets(iconSets, iconSetNames);
                            return panel;
                        }
                    });
            binedModule.registerCodeAreaPopupMenu();
            binedViewerModule.registerCodeAreaPopupMenu();
            binedEditorModule.registerCodeAreaPopupMenu();
            editorModule.registerSettings();
            binedViewerModule.registerSettings();
            binedViewerModule.registerViewModeMenu();
            binedViewerModule.registerCodeTypeMenu();
            binedViewerModule.registerPositionCodeTypeMenu();
            binedViewerModule.registerHexCharactersCaseHandlerMenu();
            binedViewerModule.registerLayoutMenu();
            binedEditorModule.registerSettings();
            binedThemeModule.registerSettings();
            binedSearchModule.registerEditFindPopupMenuActions();
            binedOperationModule.registerBlockEditPopupMenuActions();
            binedToolContentModule.registerClipboardContentMenu();
            binedToolContentModule.registerDragDropContentMenu();
            binedInspectorModule.registerSettings();

            String toolsSubMenuId = BinEdIntelliJPlugin.PLUGIN_PREFIX + "toolsMenu";
            MenuDefinitionManagement menuManagement = menuModule.getMenuManager(BinedModule.CODE_AREA_POPUP_MENU_ID, BinedModule.MODULE_ID);
            Action toolsSubMenuAction = new AbstractAction(((FrameModule) frameModule).getResourceBundle().getString("toolsMenu.text")) {
                @Override
                public void actionPerformed(ActionEvent e) {
                }
            };
            // toolsSubMenuAction.putValue(Action.SHORT_DESCRIPTION, ((FrameModule) frameModule).getResourceBundle().getString("toolsMenu.shortDescription"));
            SequenceContribution contribution = menuManagement.registerMenuItem(toolsSubMenuId, toolsSubMenuAction);
            menuManagement.registerMenuRule(contribution, new PositionSequenceContributionRule(PositionSequenceContributionRule.PositionMode.BOTTOM_LAST));
            MenuDefinitionManagement subMenu = menuManagement.getSubMenu(toolsSubMenuId);
            contribution = subMenu.registerMenuItem(binedCompareModule.createCompareFilesAction());
            menuManagement.registerMenuRule(contribution, new PositionSequenceContributionRule(PositionSequenceContributionRule.PositionMode.TOP));
            contribution = subMenu.registerMenuItem(binedToolContentModule.createClipboardContentAction());
            menuManagement.registerMenuRule(contribution, new PositionSequenceContributionRule(PositionSequenceContributionRule.PositionMode.TOP));
            contribution = subMenu.registerMenuItem(binedToolContentModule.createDragDropContentAction());
            menuManagement.registerMenuRule(contribution, new PositionSequenceContributionRule(PositionSequenceContributionRule.PositionMode.TOP));

            String aboutMenuGroup = BinEdIntelliJPlugin.PLUGIN_PREFIX + "helpAboutMenuGroup";
            contribution = menuManagement.registerMenuGroup(aboutMenuGroup);
            menuManagement.registerMenuRule(contribution, new PositionSequenceContributionRule(PositionSequenceContributionRule.PositionMode.BOTTOM_LAST));
            menuManagement.registerMenuRule(contribution, new SeparationSequenceContributionRule(SeparationSequenceContributionRule.SeparationMode.ABOVE));
            contribution = menuManagement.registerMenuItem(helpOnlineModule.createOnlineHelpAction());
            menuManagement.registerMenuRule(contribution, new GroupSequenceContributionRule(aboutMenuGroup));
            contribution = menuManagement.registerMenuItem(aboutModule.createAboutAction());
            menuManagement.registerMenuRule(contribution, new GroupSequenceContributionRule(aboutMenuGroup));

            ActiveContextManagement contextManager =
                    frameModule.getFrameHandler().getContextManager();
            contextManager.changeActiveState(EditorProvider.class, editorProvider);
            contextManager.changeActiveState(DialogParentComponent.class, () -> frameModule.getFrame());
        }

        /**
         * Version 0.2.11.x had incorrect storage of the preferences.
         * <p>
         * If it exists import it.
         */
        private void convertIncorrectPreferences() {
            OptionsModuleApi optionsModule = App.getModule(OptionsModuleApi.class);
            OptionsStorage optionsStorage = optionsModule.getAppOptions();

            String versionKey = optionsStorage.get("version", "");
            if (!"0.2.12".equals(versionKey)) {
                String osName = System.getProperty("os.name").toLowerCase();
                Preferences prefsPreferences;
                if (osName.startsWith("win")) {
                    prefsPreferences = (new FilePreferencesFactory()).userNodeForPackage(BinEdIntelliJPlugin.class);
                } else {
                    prefsPreferences = Preferences.userNodeForPackage(BinEdIntelliJPlugin.class);
                }

                try {
                    String[] keys = prefsPreferences.keys();
                    for (String key : keys) {
                        String value = prefsPreferences.get(key, null);
                        if (value != null) {
                            optionsStorage.put(key, value);
                        }
                    }
                    optionsStorage.put("version", "0.2.12");
                    optionsStorage.flush();
                } catch (BackingStoreException e) {
                    // Can't process
                }
            }
        }

        @Nonnull
        @Override
        public Class getManifestClass() {
            return BinEdIntelliJPlugin.class;
        }

        @Override
        public void launch(Runnable runnable) {
        }

        @Override
        public void launch(String launchModuleId, String[] args) {
        }

        @Nonnull
        @Override
        public <T extends Module> T getModule(Class<T> moduleClass) {
            return (T) modules.get(moduleClass);
        }
    }

    private interface CodeAreaCoreAction {
        void perform(CodeAreaCore component);
    }

    private interface CodeAreaCommanderAction {
        void perform(CodeAreaCommandHandler commandHandler);
    }
}
