/*
 * Copyright (C) ExBin Project, https://exbin.org
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
import org.exbin.bined.intellij.search.BinEdIntelliJComponentSearch;
import org.exbin.bined.intellij.settings.IntegrationOptions;
import org.exbin.bined.intellij.settings.IntegrationSettingsComponent;
import org.exbin.bined.intellij.settings.IntelliJOptionsStorage;
import org.exbin.bined.intellij.settings.gui.IntegrationSettingsPanel;
import org.exbin.bined.jaguif.bookmarks.BinedBookmarksModule;
import org.exbin.bined.jaguif.compare.BinedCompareModule;
import org.exbin.bined.jaguif.compare.action.CompareFilesAction;
import org.exbin.bined.jaguif.component.BinedComponentModule;
import org.exbin.bined.jaguif.document.BinEdFileManager;
import org.exbin.bined.jaguif.document.BinaryFileDocument;
import org.exbin.bined.jaguif.document.BinedDocumentModule;
import org.exbin.bined.jaguif.editor.BinedEditorModule;
import org.exbin.bined.jaguif.inspector.BasicValuesInspector;
import org.exbin.bined.jaguif.inspector.BasicValuesInspectorProvider;
import org.exbin.bined.jaguif.inspector.BinEdInspector;
import org.exbin.bined.jaguif.inspector.BinEdInspectorManager;
import org.exbin.bined.jaguif.inspector.BinedInspectorModule;
import org.exbin.bined.jaguif.inspector.gui.BasicValuesPanel;
import org.exbin.bined.jaguif.inspector.settings.DataInspectorFontContextInference;
import org.exbin.bined.jaguif.inspector.settings.DataInspectorFontInference;
import org.exbin.bined.jaguif.macro.BinedMacroModule;
import org.exbin.bined.jaguif.objectdata.BinedObjectDataModule;
import org.exbin.bined.jaguif.operation.bouncycastle.BinedOperationBouncycastleModule;
import org.exbin.bined.jaguif.operation.code.BinedOperationCodeModule;
import org.exbin.bined.jaguif.operation.method.BinedOperationMethodModule;
import org.exbin.bined.jaguif.search.BinedSearchModule;
import org.exbin.bined.jaguif.theme.BinedThemeModule;
import org.exbin.bined.jaguif.tool.content.BinedToolContentModule;
import org.exbin.bined.jaguif.tool.content.action.ClipboardContentAction;
import org.exbin.bined.jaguif.tool.content.action.DragDropContentAction;
import org.exbin.bined.jaguif.viewer.BinedViewerModule;
import org.exbin.bined.swing.CodeAreaCommandHandler;
import org.exbin.bined.swing.CodeAreaCore;
import org.exbin.jaguif.App;
import org.exbin.jaguif.Module;
import org.exbin.jaguif.ModuleProvider;
import org.exbin.jaguif.about.AboutModule;
import org.exbin.jaguif.about.action.AboutAction;
import org.exbin.jaguif.about.api.AboutModuleApi;
import org.exbin.jaguif.action.ActionModule;
import org.exbin.jaguif.action.api.ActionModuleApi;
import org.exbin.jaguif.action.api.DialogParentComponent;
import org.exbin.jaguif.component.ComponentModule;
import org.exbin.jaguif.component.api.ComponentModuleApi;
import org.exbin.jaguif.context.ContextModule;
import org.exbin.jaguif.context.api.ActiveContextManagement;
import org.exbin.jaguif.context.api.ContextModuleApi;
import org.exbin.jaguif.contribution.ContributionModule;
import org.exbin.jaguif.contribution.api.ContributionModuleApi;
import org.exbin.jaguif.contribution.api.GroupSequenceContributionRule;
import org.exbin.jaguif.contribution.api.PositionSequenceContributionRule;
import org.exbin.jaguif.contribution.api.SeparationSequenceContributionRule;
import org.exbin.jaguif.contribution.api.SequenceContribution;
import org.exbin.jaguif.docking.DockingModule;
import org.exbin.jaguif.docking.api.ContextDocking;
import org.exbin.jaguif.docking.api.DockingModuleApi;
import org.exbin.jaguif.document.DocumentModule;
import org.exbin.jaguif.document.api.DocumentModuleApi;
import org.exbin.jaguif.file.FileModule;
import org.exbin.jaguif.file.api.FileModuleApi;
import org.exbin.jaguif.frame.FrameModule;
import org.exbin.jaguif.frame.api.FrameModuleApi;
import org.exbin.jaguif.help.HelpModule;
import org.exbin.jaguif.help.api.HelpModuleApi;
import org.exbin.jaguif.help.online.HelpOnlineModule;
import org.exbin.jaguif.help.online.action.OnlineHelpAction;
import org.exbin.jaguif.language.LanguageModule;
import org.exbin.jaguif.language.api.LanguageModuleApi;
import org.exbin.jaguif.language.api.LanguageProvider;
import org.exbin.jaguif.menu.MenuModule;
import org.exbin.jaguif.menu.api.DefaultActionMenuContribution;
import org.exbin.jaguif.menu.api.MenuBuilder;
import org.exbin.jaguif.menu.api.MenuDefinitionManagement;
import org.exbin.jaguif.menu.api.MenuModuleApi;
import org.exbin.jaguif.menu.api.MenuShowMethod;
import org.exbin.jaguif.operation.undo.OperationUndoModule;
import org.exbin.jaguif.operation.undo.api.OperationUndoModuleApi;
import org.exbin.jaguif.options.OptionsModule;
import org.exbin.jaguif.options.api.OptionsModuleApi;
import org.exbin.jaguif.options.api.OptionsStorage;
import org.exbin.jaguif.options.preferences.FilePreferencesFactory;
import org.exbin.jaguif.options.settings.OptionsSettingsModule;
import org.exbin.jaguif.options.settings.api.OptionsSettingsManagement;
import org.exbin.jaguif.options.settings.api.OptionsSettingsModuleApi;
import org.exbin.jaguif.options.settings.api.SettingsPageContribution;
import org.exbin.jaguif.options.settings.api.SettingsPanelType;
import org.exbin.jaguif.plugin.language.cs_CZ.LanguageCsCzModule;
import org.exbin.jaguif.plugin.language.de_DE.LanguageDeDeModule;
import org.exbin.jaguif.plugin.language.es_ES.LanguageEsEsModule;
import org.exbin.jaguif.plugin.language.fr_FR.LanguageFrFrModule;
import org.exbin.jaguif.plugin.language.it_IT.LanguageItItModule;
import org.exbin.jaguif.plugin.language.ja_JP.LanguageJaJpModule;
import org.exbin.jaguif.plugin.language.ko_KR.LanguageKoKrModule;
import org.exbin.jaguif.plugin.language.pl_PL.LanguagePlPlModule;
import org.exbin.jaguif.plugin.language.ru_RU.LanguageRuRuModule;
import org.exbin.jaguif.plugin.language.zh_Hans.LanguageZhHansModule;
import org.exbin.jaguif.plugin.language.zh_Hant.LanguageZhHantModule;
import org.exbin.jaguif.plugins.iconset.material.IconSetMaterialModule;
import org.exbin.jaguif.statusbar.StatusBarModule;
import org.exbin.jaguif.statusbar.api.StatusBarModuleApi;
import org.exbin.jaguif.text.encoding.settings.TextEncodingContextInference;
import org.exbin.jaguif.text.encoding.settings.TextEncodingInference;
import org.exbin.jaguif.text.encoding.settings.TextEncodingsContextInference;
import org.exbin.jaguif.text.encoding.settings.TextEncodingsInference;
import org.exbin.jaguif.text.font.settings.TextFontContextInference;
import org.exbin.jaguif.text.font.settings.TextFontInference;
import org.exbin.jaguif.toolbar.ToolBarModule;
import org.exbin.jaguif.toolbar.api.ToolBarModuleApi;
import org.exbin.jaguif.ui.UiModule;
import org.exbin.jaguif.ui.api.UiModuleApi;
import org.exbin.jaguif.ui.theme.UiThemeModule;
import org.exbin.jaguif.ui.theme.api.UiThemeModuleApi;
import org.exbin.jaguif.window.WindowModule;
import org.exbin.jaguif.window.api.WindowModuleApi;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.AbstractAction;
import javax.swing.Action;
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
import java.awt.Component;
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

        connect.subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, new FileEditorManagerListener() {
/*            @Override
            public void fileClosed(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
                if (file instanceof BinEdVirtualFile) {
                    // TODO: FileEditorManagerKeys.CLOSING_TO_REOPEN not available yet
                    Boolean userData = file.getUserData(FileEditorManagerImpl.CLOSING_TO_REOPEN);
                    if (userData == null || !userData) {
                        ((BinEdVirtualFile) file).dispose();
                    }
                }
            } */

            @Override
            public void selectionChanged(@Nonnull FileEditorManagerEvent event) {
                FrameModuleApi frameModule = App.getModule(FrameModuleApi.class);
                BinEdIntelliJDocking docking = (BinEdIntelliJDocking) frameModule.getFrameController().getContextManager().getActiveState(ContextDocking.class);
                BinaryFileDocument activeFile = null;
                FileEditor fileEditor = event.getNewEditor();
                if (fileEditor instanceof BinEdFileEditor) {
                    activeFile = ((BinEdFileEditor) fileEditor).getVirtualFile().getEditorFile();
                } else if (fileEditor instanceof BinEdNativeFileEditor) {
                    activeFile = ((BinEdNativeFileEditor) fileEditor).getNativeFile().getDocument();
                } else if (fileEditor != null) {
                    // Ignore for now
                    return;
                }
                docking.setActiveDocument(activeFile);
            }
        });

        connect.subscribe(FileEditorManagerListener.Before.FILE_EDITOR_MANAGER, new FileEditorManagerListener.Before() {

            private boolean discardAllowed = false;

            @Override
            public void beforeFileClosed(@Nonnull FileEditorManager source, @Nonnull VirtualFile file) {
                // TODO: FileEditorManagerKeys.CLOSING_TO_REOPEN not available yet
                Boolean userData = file.getUserData(FileEditorManagerImpl.CLOSING_TO_REOPEN);
                if (userData != null && userData) {
                    return;
                }

                if (!(file instanceof BinEdVirtualFile)) {
                    return;
                }

                if (discardAllowed) {
                    discardAllowed = false;
                    ((BinEdVirtualFile) file).dispose();
                    return;
                }

                if (!((BinEdVirtualFile) file).isClosing()) {
                    ((BinEdVirtualFile) file).setClosing(true);
                    BinaryFileDocument binaryDocument = ((BinEdVirtualFile) file).getEditorFile();
                    if (binaryDocument.isModified()) {
                        ApplicationManager.getApplication().invokeLater(() -> {
                            FrameModuleApi frameModule = App.getModule(FrameModuleApi.class);
                            BinEdIntelliJDocking docking = (BinEdIntelliJDocking) frameModule.getFrameController().getContextManager().getActiveState(ContextDocking.class);
                            boolean released = docking.releaseDocument(binaryDocument);
                            ((BinEdVirtualFile) file).setClosing(false);
                            if (released) {
                                discardAllowed = true;
                                // Invoke closing
                                FileEditorManager.getInstance(project).closeFile(file);
                            }
                        });
                        throw new ProcessCanceledException();
                    }
                }
                ((BinEdVirtualFile) file).dispose();
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
            modules.put(OptionsModuleApi.class, new org.exbin.jaguif.options.OptionsModule());
            modules.put(OptionsSettingsModuleApi.class, new OptionsSettingsModule());
            modules.put(UiModuleApi.class, new UiModule());
            modules.put(UiThemeModuleApi.class, new UiThemeModule());
            modules.put(HelpModuleApi.class, new HelpModule());
            modules.put(MenuModuleApi.class, new MenuModule());
            modules.put(ToolBarModuleApi.class, new ToolBarModule());
            modules.put(StatusBarModuleApi.class, new StatusBarModule());
            modules.put(ComponentModuleApi.class, new ComponentModule());
            modules.put(WindowModuleApi.class, new WindowModule());
            modules.put(FrameModuleApi.class, new FrameModule());
            modules.put(DocumentModuleApi.class, new DocumentModule());
            modules.put(FileModuleApi.class, new FileModule());
            modules.put(DockingModuleApi.class, new DockingModule());
            modules.put(HelpOnlineModule.class, new HelpOnlineModule());
            modules.put(BinedComponentModule.class, new BinedComponentModule());
            modules.put(BinedViewerModule.class, new BinedViewerModule());
            modules.put(BinedEditorModule.class, new BinedEditorModule());
            modules.put(BinedDocumentModule.class, new BinedDocumentModule());
            modules.put(BinedThemeModule.class, new BinedThemeModule());
            modules.put(BinedSearchModule.class, new BinedSearchModule());
            modules.put(BinedOperationMethodModule.class, new BinedOperationMethodModule());
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
            App.setAppBundle(ResourceBundle.getBundle("org.exbin.bined.intellij.resources.BinEdIntelliJApp",
                    Locale.ROOT));

            OptionsModule optionsModule = (OptionsModule) App.getModule(OptionsModuleApi.class);
            optionsModule.setAppOptions(new IntelliJOptionsStorage(PropertiesComponent.getInstance(),
                    BinEdIntelliJPlugin.PLUGIN_PREFIX));
            convertIncorrectPreferences();

            OptionsStorage preferences = optionsModule.getAppOptions();

            MenuModuleApi menuModule = App.getModule(MenuModuleApi.class);
            menuModule.setMenuBuilder(new MenuBuilder() {
                @Nonnull
                @Override
                public JMenu createMenu() {
                    return new JBMenu();
                }

                @Nonnull
                @Override
                public JPopupMenu createPopupMenu() {
                    return new JBPopupMenu();
                }

                @Nonnull
                @Override
                public JMenuItem createMenuItem() {
                    return new JBMenuItem("");
                }

                @Nonnull
                @Override
                public JCheckBoxMenuItem createCheckBoxMenuItem() {
                    return new JBCheckBoxMenuItem();
                }

                @Nonnull
                @Override
                public JRadioButtonMenuItem createRadioButtonMenuItem() {
                    return new JRadioButtonMenuItem();
                }

                @Nonnull
                @Override
                public JPopupMenu createPopupMenu(MenuShowMethod showMethod) {
                    return new JBPopupMenu() {
                        @Override
                        public void show(@Nullable Component invoker, int x, int y) {
                            showMethod.show(invoker, x, y);
                        }
                    };
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
            BinedOperationBouncycastleModule binedOperationBouncycastleModule =
                    App.getModule(BinedOperationBouncycastleModule.class);
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

            FileModuleApi fileModule = App.getModule(FileModuleApi.class);
            fileModule.registerFileProviders();
            ActionModuleApi actionModule = App.getModule(ActionModuleApi.class);

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

            BinEdIntelliJDocking docking = new BinEdIntelliJDocking();
            DocumentModule documentModule = (DocumentModule) App.getModule(DocumentModuleApi.class);
            BinedComponentModule binedComponentModule = App.getModule(BinedComponentModule.class);
            BinedViewerModule binedViewerModule = App.getModule(BinedViewerModule.class);
            BinedEditorModule binedEditorModule = App.getModule(BinedEditorModule.class);
            BinedDocumentModule binedDocumentModule = App.getModule(BinedDocumentModule.class);
            BinedThemeModule binedThemeModule = App.getModule(BinedThemeModule.class);

            BinedSearchModule binedSearchModule = App.getModule(BinedSearchModule.class);
            BinEdFileManager fileManager = binedDocumentModule.getFileManager();
            fileManager.addBinEdComponentExtension(component -> Optional.of(new BinEdIntelliJComponentSearch()));

            BinedOperationMethodModule binedOperationModule = App.getModule(BinedOperationMethodModule.class);
            binedOperationModule.addBasicMethods();

            BinedToolContentModule binedToolContentModule = App.getModule(BinedToolContentModule.class);

            BinedInspectorModule binedInspectorModule = App.getModule(BinedInspectorModule.class);
            binedInspectorModule.registerBasicInspector();
            BinEdInspectorManager inspectorManager = binedInspectorModule.getBinEdInspectorManager();
            inspectorManager.removeAllInspectors();
            inspectorManager.addInspector(new BasicValuesInspectorProviderWrapper(binedInspectorModule.getResourceBundle()));
            binedInspectorModule.registerShowParsingPanelMenuActions();
            binedInspectorModule.registerShowParsingPanelPopupMenuActions();

            BinedCompareModule binedCompareModule = App.getModule(BinedCompareModule.class);
            binedCompareModule.registerToolsOptionsMenuActions();

            OptionsSettingsManagement settingsManager = optionsSettingsModule.getMainSettingsManager();
            settingsManager.registerSettingsOptions(IntegrationOptions.class, IntegrationOptions::new);
            settingsManager.registerComponent("integration", new IntegrationSettingsComponent());
            SettingsPageContribution pageContribution =
                    new SettingsPageContribution("document", documentModule.getResourceBundle());
            settingsManager.registerPage(pageContribution);

            binedComponentModule.registerCodeAreaPopupMenu();
            binedViewerModule.registerCodeAreaPopupMenu();
            binedEditorModule.registerCodeAreaPopupMenu();
            binedDocumentModule.registerDocument();
            binedViewerModule.registerFrameStatusBar();
            binedDocumentModule.registerStatusBar();
            binedDocumentModule.registerEncodings();
            binedViewerModule.registerViewModeMenu();
            binedViewerModule.registerCodeTypeMenu();
            binedViewerModule.registerPositionCodeTypeMenu();
            binedViewerModule.registerHexCharactersCaseHandlerMenu();
            binedViewerModule.registerLayoutMenu();
            binedViewerModule.registerSettings();
            binedEditorModule.registerSettings();
            binedDocumentModule.registerSettings();
            binedThemeModule.registerSettings();
            binedSearchModule.registerEditFindPopupMenuActions();
            binedOperationModule.registerBlockEditPopupMenuActions();
            binedToolContentModule.registerClipboardContentMenu();
            binedToolContentModule.registerDragDropContentMenu();
            binedInspectorModule.registerSettings();

            FrameModuleApi frameModuleApi = App.getModule(FrameModuleApi.class);
            ActiveContextManagement contextManagement = frameModuleApi.getFrameController().getContextManager();
            settingsManager.registerInferenceOptions(TextEncodingInference.class, new TextEncodingContextInference(contextManagement));
            settingsManager.registerInferenceOptions(TextEncodingsInference.class, new TextEncodingsContextInference(contextManagement));
            settingsManager.registerInferenceOptions(TextFontInference.class, new TextFontContextInference((contextManagement)));
            settingsManager.registerInferenceOptions(DataInspectorFontInference.class, new DataInspectorFontContextInference(contextManagement));

            String toolsSubMenuId = BinEdIntelliJPlugin.PLUGIN_PREFIX + "toolsMenu";
            MenuDefinitionManagement menuManagement = menuModule.getMainMenuDefinition(BinedComponentModule.CODE_AREA_POPUP_MENU_ID, BinedComponentModule.MODULE_ID);
            Action toolsSubMenuAction = new AbstractAction(((FrameModule) frameModule).getResourceBundle().getString("toolsMenu.text")) {
                @Override
                public void actionPerformed(ActionEvent e) {
                }
            };
            // toolsSubMenuAction.putValue(Action.SHORT_DESCRIPTION, ((FrameModule) frameModule).getResourceBundle().getString("toolsMenu.shortDescription"));
            SequenceContribution contribution = menuManagement.registerMenuItem(toolsSubMenuId, toolsSubMenuAction);
            menuManagement.registerMenuRule(contribution, new PositionSequenceContributionRule(PositionSequenceContributionRule.PositionMode.BOTTOM_LAST));
            MenuDefinitionManagement subMenu = menuManagement.getSubMenu(toolsSubMenuId);
            contribution = new DefaultActionMenuContribution(CompareFilesAction.ACTION_ID, binedCompareModule::createCompareFilesAction);
            subMenu.registerMenuContribution(contribution);
            menuManagement.registerMenuRule(contribution, new PositionSequenceContributionRule(PositionSequenceContributionRule.PositionMode.TOP));
            contribution = new DefaultActionMenuContribution(ClipboardContentAction.ACTION_ID, binedToolContentModule::createClipboardContentAction);
            subMenu.registerMenuContribution(contribution);
            menuManagement.registerMenuRule(contribution, new PositionSequenceContributionRule(PositionSequenceContributionRule.PositionMode.TOP));
            contribution = new DefaultActionMenuContribution(DragDropContentAction.ACTION_ID, binedToolContentModule::createDragDropContentAction);
            subMenu.registerMenuContribution(contribution);
            menuManagement.registerMenuRule(contribution, new PositionSequenceContributionRule(PositionSequenceContributionRule.PositionMode.TOP));

            String aboutMenuGroup = BinEdIntelliJPlugin.PLUGIN_PREFIX + "helpAboutMenuGroup";
            contribution = menuManagement.registerMenuGroup(aboutMenuGroup);
            menuManagement.registerMenuRule(contribution, new PositionSequenceContributionRule(PositionSequenceContributionRule.PositionMode.BOTTOM_LAST));
            menuManagement.registerMenuRule(contribution, new SeparationSequenceContributionRule(SeparationSequenceContributionRule.SeparationMode.ABOVE));
            contribution = new DefaultActionMenuContribution(OnlineHelpAction.ACTION_ID, helpOnlineModule::createOnlineHelpAction);
            menuManagement.registerMenuContribution(contribution);
            menuManagement.registerMenuRule(contribution, new GroupSequenceContributionRule(aboutMenuGroup));
            contribution = new DefaultActionMenuContribution(AboutAction.ACTION_ID, aboutModule::createAboutAction);
            menuManagement.registerMenuContribution(contribution);
            menuManagement.registerMenuRule(contribution, new GroupSequenceContributionRule(aboutMenuGroup));

            ActiveContextManagement contextManager =
                    frameModule.getFrameController().getContextManager();
            contextManager.changeActiveState(ContextDocking.class, docking);
            contextManager.changeActiveState(DialogParentComponent.class, () -> frameModule.getFrame());

            optionsSettingsModule.initialLoadFromPreferences();
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

    /**
     * Wrapper forcing background image painting for data inspector.
     */
    private static class BasicValuesInspectorProviderWrapper extends BasicValuesInspectorProvider {

        public BasicValuesInspectorProviderWrapper(ResourceBundle resourceBundle) {
            super(resourceBundle);
        }

        @Nonnull
        @Override
        public BinEdInspector createInspector() {
            return new BasicValuesInspector() {
                @Nonnull
                @Override
                protected BasicValuesPanel createComponent() {
                    return new BasicValuesPanel() {
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

                        @Nonnull
                        public JScrollPane createScrollPane() {
                            return new JBScrollPane();
                        }

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
    }
}
