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
package org.exbin.bined.intellij.gui;

import com.intellij.openapi.actionSystem.IdeActions;
import org.exbin.bined.CodeAreaUtils;
import org.exbin.bined.CodeType;
import org.exbin.bined.highlight.swing.NonprintablesCodeAreaAssessor;
import org.exbin.bined.intellij.BinEdIntelliJDocking;
import org.exbin.bined.intellij.action.CompareFilesAction;
import org.exbin.bined.intellij.utils.ActionUtils;
import org.exbin.bined.jaguif.bookmarks.BinedBookmarksModule;
import org.exbin.bined.jaguif.compare.BinedCompareModule;
import org.exbin.bined.jaguif.component.BinedComponentModule;
import org.exbin.bined.jaguif.component.gui.BinEdComponentPanel;
import org.exbin.bined.jaguif.document.BinaryFileDocument;
import org.exbin.bined.jaguif.macro.BinedMacroModule;
import org.exbin.jaguif.action.api.ActionContextChange;
import org.exbin.jaguif.search.SearchModule;
import org.exbin.jaguif.search.action.FindReplaceActions;
import org.exbin.bined.jaguif.viewer.BinedViewerModule;
import org.exbin.bined.operation.command.BinaryDataUndoRedo;
import org.exbin.bined.swing.CodeAreaSwingUtils;
import org.exbin.bined.swing.capability.ColorAssessorPainterCapable;
import org.exbin.bined.swing.section.SectCodeArea;
import org.exbin.jaguif.App;
import org.exbin.jaguif.action.api.ActionConsts;
import org.exbin.jaguif.action.api.ActionModuleApi;
import org.exbin.jaguif.action.api.DialogParentComponent;
import org.exbin.jaguif.context.ActiveContextManager;
import org.exbin.jaguif.context.api.ActiveContextManagement;
import org.exbin.jaguif.context.api.ContextChange;
import org.exbin.jaguif.context.api.ContextChangeListener;
import org.exbin.jaguif.context.api.ContextComponent;
import org.exbin.jaguif.context.api.ContextModuleApi;
import org.exbin.jaguif.context.api.ContextRegistration;
import org.exbin.jaguif.context.api.ContextUpdateManagement;
import org.exbin.jaguif.context.api.StateUpdateType;
import org.exbin.jaguif.docking.api.ContextDocking;
import org.exbin.jaguif.document.api.ContextDocument;
import org.exbin.jaguif.frame.api.FrameModuleApi;
import org.exbin.jaguif.language.api.LanguageModuleApi;
import org.exbin.jaguif.options.settings.action.SettingsAction;
import org.exbin.jaguif.options.settings.api.OptionsSettingsModuleApi;
import org.exbin.jaguif.search.api.ContextSearch;
import org.exbin.jaguif.search.api.SearchModuleApi;
import org.exbin.jaguif.statusbar.api.StatusBar;
import org.exbin.jaguif.statusbar.api.StatusBarModuleApi;
import org.exbin.jaguif.text.encoding.ContextEncoding;
import org.exbin.jaguif.utils.DesktopUtils;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/**
 * Binary editor file panel.
 */
@NullMarked
public class BinEdFilePanel extends JPanel {

    @Nullable
    protected BinaryFileDocument fileDocument;
    protected BinEdToolbarPanel toolbarPanel = new BinEdToolbarPanel();
    protected ActiveContextManagement statusContextManager;
    protected ContextChangeListener contextChangeListener;
    protected StatusBar statusBar;

    public BinEdFilePanel() {
        super(new BorderLayout());
        add(toolbarPanel, BorderLayout.NORTH);

        FrameModuleApi frameModule = App.getModule(FrameModuleApi.class);
        StatusBarModuleApi statusBarModule = App.getModule(StatusBarModuleApi.class);
        ContextModuleApi contextModule = App.getModule(ContextModuleApi.class);
        ActiveContextManagement contextManager = frameModule.getFrameController().getContextManager();
        statusContextManager = contextModule.createChildContextManager(contextManager);
        contextChangeListener = new ContextChangeListener() {
            @Override
            public <T> void notifyStateChanged(Class<T> stateClass, @Nullable T activeState) {
                if (fileDocument == null) {
                    return;
                }

                if (stateClass == ContextDocument.class && activeState != fileDocument) {
                    return;
                }

                if (stateClass == ContextComponent.class && activeState != fileDocument.getDataComponent()) {
                    return;
                }

                if (stateClass == ContextEncoding.class && activeState != fileDocument.getDataComponent()) {
                    return;
                }

                statusContextManager.changeActiveState(stateClass, activeState);
            }

            @Override
            public <T> void notifyStateUpdated(Class<T> stateClass, T activeState, StateUpdateType stateUpdateType) {
                if (fileDocument == null) {
                    return;
                }

                if (stateClass == ContextDocument.class && activeState != fileDocument) {
                    return;
                }

                if (stateClass == ContextComponent.class && activeState != fileDocument.getDataComponent()) {
                    return;
                }

                if (stateClass == ContextEncoding.class && activeState != fileDocument.getDataComponent()) {
                    return;
                }

                statusContextManager.updateActiveState(stateClass, activeState, stateUpdateType);
            }
        };
        contextManager.addChangeListener(contextChangeListener);
        ContextUpdateManagement statusUpdateManager = contextModule.createContextUpdateManagement(statusContextManager);
        ContextRegistration statusContextRegistrator = contextModule.createContextRegistrator("",  statusUpdateManager, statusContextManager);
        statusBar = statusBarModule.createStatusBar(BinedComponentModule.BINARY_STATUS_BAR_ID, statusContextRegistrator);
    }

    public void detach() {
        FrameModuleApi frameModule = App.getModule(FrameModuleApi.class);
        ActiveContextManagement contextManager = frameModule.getFrameController().getContextManager();
        contextManager.removeChangeListener(contextChangeListener);
    }

    public void setDocument(BinaryFileDocument fileDocument) {
        this.fileDocument = fileDocument;
        statusContextManager.changeActiveState(ContextDocument.class, fileDocument);
        statusContextManager.changeActiveState(ContextComponent.class, fileDocument.getDataComponent());
        statusContextManager.changeActiveState(ContextEncoding.class, fileDocument.getDataComponent());
        statusContextManager.changeActiveState(ContextSearch.class, fileDocument.getDataComponent().getSearchController().orElse(null));

        BinEdComponentPanel componentPanel = fileDocument.getComponent();
        SectCodeArea codeArea = (SectCodeArea) fileDocument.getCodeArea();

        SearchModule searchModule = (SearchModule) App.getModule(SearchModuleApi.class);
        FindReplaceActions findReplaceActions = searchModule.getFindReplaceActions();
        ActionMap actionMap = codeArea.getActionMap();
        InputMap inputMap = codeArea.getInputMap();
        actionMap.put(IdeActions.ACTION_FIND, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Action editFindAction = findReplaceActions.createEditFindAction();
                ContextModuleApi contextModule = App.getModule(ContextModuleApi.class);
                ContextRegistration contextRegistrator = contextModule.createContextRegistrator(statusContextManager);
                contextRegistrator.registerContextChange((ActionContextChange) editFindAction);
                contextRegistrator.finish();
                editFindAction.actionPerformed(e);
            }
        });
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_DOWN_MASK, false), IdeActions.ACTION_FIND);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.META_DOWN_MASK, false), IdeActions.ACTION_FIND);
        actionMap.put(IdeActions.ACTION_REPLACE, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Action editReplaceAction = findReplaceActions.createEditReplaceAction();
                ContextModuleApi contextModule = App.getModule(ContextModuleApi.class);
                ContextRegistration contextRegistrator = contextModule.createContextRegistrator(statusContextManager);
                contextRegistrator.registerContextChange((ActionContextChange) editReplaceAction);
                contextRegistrator.finish();
                editReplaceAction.actionPerformed(e);
            }
        });
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_H, KeyEvent.CTRL_DOWN_MASK, false), IdeActions.ACTION_REPLACE);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_H, KeyEvent.META_DOWN_MASK, false), IdeActions.ACTION_REPLACE);
        actionMap.put(IdeActions.ACTION_UNDO, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                BinaryDataUndoRedo undoRedo = fileDocument.getUndoHandler().orElse(null);
                if (undoRedo != null) {
                    undoRedo.performUndo();
                }
            }
        });
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK, false), IdeActions.ACTION_UNDO);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.META_DOWN_MASK, false), IdeActions.ACTION_UNDO);
        actionMap.put(IdeActions.ACTION_REDO, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                BinaryDataUndoRedo undoRedo = fileDocument.getUndoHandler().orElse(null);
                if (undoRedo != null) {
                    undoRedo.performRedo();
                }
            }
        });
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.SHIFT_DOWN_MASK | KeyEvent.CTRL_DOWN_MASK, false), IdeActions.ACTION_REDO);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.SHIFT_DOWN_MASK | KeyEvent.META_DOWN_MASK, false), IdeActions.ACTION_REDO);

        toolbarPanel.setTargetComponent(componentPanel);
        toolbarPanel.setCodeAreaControl(new BinEdToolbarPanel.Control() {
            @Override
            public CodeType getCodeType() {
                return codeArea.getCodeType();
            }

            @Override
            public void setCodeType(CodeType codeType) {
                codeArea.setCodeType(codeType);
            }

            @Override
            public boolean isShowNonprintables() {
                ColorAssessorPainterCapable painter = (ColorAssessorPainterCapable) codeArea.getPainter();
                NonprintablesCodeAreaAssessor nonprintablesCodeAreaAssessor = CodeAreaSwingUtils.findColorAssessor(painter, NonprintablesCodeAreaAssessor.class);
                return CodeAreaUtils.requireNonNull(nonprintablesCodeAreaAssessor).isShowNonprintables();
            }

            @Override
            public void setShowNonprintables(boolean showNonprintables) {
                ColorAssessorPainterCapable painter = (ColorAssessorPainterCapable) codeArea.getPainter();
                NonprintablesCodeAreaAssessor nonprintablesCodeAreaAssessor = CodeAreaSwingUtils.findColorAssessor(painter, NonprintablesCodeAreaAssessor.class);
                CodeAreaUtils.requireNonNull(nonprintablesCodeAreaAssessor).setShowNonprintables(showNonprintables);
            }

            @Override
            public void repaint() {
                codeArea.repaint();
            }
        });
        toolbarPanel.setOnlineHelpAction(createOnlineHelpAction());

        FrameModuleApi frameModule = App.getModule(FrameModuleApi.class);
        BinEdIntelliJDocking docking = (BinEdIntelliJDocking) frameModule.getFrameController().getContextManager().getActiveState(ContextDocking.class);
        OptionsSettingsModuleApi optionsSettingsModule = App.getModule(OptionsSettingsModuleApi.class);
        SettingsAction settingsAction = (SettingsAction) optionsSettingsModule.createSettingsAction();
        settingsAction.setDialogParentComponent(() -> frameModule.getFrame());
        AbstractAction wrapperSettingsAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                settingsAction.actionPerformed(e);
                toolbarPanel.applyFromCodeArea();
            }
        };
        AbstractAction wrapperCompareAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                CompareFilesAction compareFilesAction = new CompareFilesAction();
                compareFilesAction.setDialogParentComponent(() -> frameModule.getFrame());
                compareFilesAction.setDocumentDocking(docking);
                compareFilesAction.actionPerformed(e);
            }
        };
        LanguageModuleApi languageModule = App.getModule(LanguageModuleApi.class);
        java.util.ResourceBundle optionsSettingsResourceBundle = languageModule.getBundle(org.exbin.jaguif.options.settings.OptionsSettingsModule.class);
        ActionModuleApi actionModule = App.getModule(ActionModuleApi.class);
        actionModule.initAction(wrapperSettingsAction, optionsSettingsResourceBundle, SettingsAction.ACTION_ID);
        wrapperSettingsAction.putValue(ActionConsts.ACTION_DIALOG_MODE, true);
        toolbarPanel.setOptionsAction(wrapperSettingsAction);
        BinedCompareModule compareModule = App.getModule(BinedCompareModule.class);
        actionModule.initAction(wrapperCompareAction, compareModule.getResourceBundle(), CompareFilesAction.ACTION_ID);
        wrapperCompareAction.putValue(ActionConsts.ACTION_DIALOG_MODE, true);

        BinedComponentModule binedComponentModule = App.getModule(BinedComponentModule.class);
        BinedViewerModule binedViewerModule = App.getModule(BinedViewerModule.class);
        codeArea.setComponentPopupMenu(new JPopupMenu() {
            @Override
            public void show(Component invoker, int x, int y) {
                String popupMenuId = "BinEdFilePanel.popup";
                int clickedX = x;
                int clickedY = y;
                if (invoker instanceof JViewport) {
                    clickedX += invoker.getParent().getX();
                    clickedY += invoker.getParent().getY();
                }

                docking.setActiveDocument(fileDocument);

                // TODO Temporary workaround for unfinished rework of actions
                {
                    ContextModuleApi contextModule = App.getModule(ContextModuleApi.class);
                    ActiveContextManagement contextManagement = new ActiveContextManager();
                    ContextUpdateManagement updateManagement = contextModule.createContextUpdateManagement(contextManagement);
                    contextManagement.changeActiveState(ContextComponent.class, fileDocument.getDataComponent());
                    contextManagement.changeActiveState(DialogParentComponent.class, () -> frameModule.getFrame());

                    BinedBookmarksModule binedBookmarksModule = App.getModule(BinedBookmarksModule.class);
                    AbstractAction manageBookmarksAction = binedBookmarksModule.getManageBookmarksAction();
                    ContextRegistration contextRegistrar = contextModule.createContextRegistrator("", updateManagement, contextManagement);
                    contextRegistrar.registerContextChange((ContextChange) manageBookmarksAction.getValue(ActionConsts.ACTION_CONTEXT_CHANGE));

                    BinedMacroModule binedMacroModule = App.getModule(BinedMacroModule.class);
                    // ContextUpdateManagement updateManagement = frameModule.getFrameController().getUpdateManager();
                    contextRegistrar.registerContextChange((ContextChange) binedMacroModule.getMacroManager().getMacrosMenu().getAction().getValue(ActionConsts.ACTION_CONTEXT_CHANGE));
                    contextRegistrar.finish();
                }

                JPopupMenu popupMenu = binedComponentModule.createBinaryDocumentPopupMenu(codeArea, clickedX, clickedY);
                ActionUtils.replaceAction(popupMenu, SettingsAction.ACTION_ID + "Action", wrapperSettingsAction);
                ActionUtils.replaceAction(popupMenu, CompareFilesAction.ACTION_ID + "Action", wrapperCompareAction);
                popupMenu.show(invoker, x, y);
            }
        });

        docking.addDocument(fileDocument, statusBar);
        docking.setActiveDocument(fileDocument);

        add(statusBar.getComponent(), BorderLayout.SOUTH);

        add(componentPanel, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    public BinEdToolbarPanel getToolbarPanel() {
        return toolbarPanel;
    }

    public SectCodeArea getCodeArea() {
        return (SectCodeArea) fileDocument.getCodeArea();
    }

    private AbstractAction createOnlineHelpAction() {
        return new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                LanguageModuleApi languageModuleApi = App.getModule(LanguageModuleApi.class);
                DesktopUtils.openDesktopURL(languageModuleApi.getAppBundle().getString("online_help_url"));
            }
        };
    }
}
