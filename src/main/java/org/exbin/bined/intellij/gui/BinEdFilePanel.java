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
import com.intellij.openapi.wm.impl.IdeBackgroundUtil;
import com.intellij.ui.Graphics2DDelegate;
import com.intellij.ui.components.JBLabel;
import org.exbin.bined.CodeAreaUtils;
import org.exbin.bined.CodeType;
import org.exbin.bined.highlight.swing.NonprintablesCodeAreaAssessor;
import org.exbin.bined.intellij.BinEdIntelliJDocking;
import org.exbin.bined.intellij.utils.ActionUtils;
import org.exbin.bined.jaguif.component.BinedComponentModule;
import org.exbin.bined.swing.CodeAreaSwingUtils;
import org.exbin.bined.swing.capability.ColorAssessorPainterCapable;
import org.exbin.bined.swing.section.SectCodeArea;
import org.exbin.jaguif.App;
import org.exbin.jaguif.action.api.ActionConsts;
import org.exbin.jaguif.action.api.ActionContextRegistration;
import org.exbin.jaguif.action.api.ActionModuleApi;
import org.exbin.jaguif.context.api.ContextChange;
import org.exbin.jaguif.context.api.ContextComponent;
import org.exbin.jaguif.action.api.DialogParentComponent;
import org.exbin.bined.jaguif.document.BinEdFileManager;
import org.exbin.bined.jaguif.document.BinaryFileDocument;
import org.exbin.bined.jaguif.bookmarks.BinedBookmarksModule;
import org.exbin.bined.jaguif.component.gui.BinEdComponentPanel;
import org.exbin.bined.jaguif.macro.BinedMacroModule;
import org.exbin.bined.jaguif.search.BinedSearchModule;
import org.exbin.bined.jaguif.search.action.FindReplaceActions;
import org.exbin.bined.jaguif.viewer.BinedViewerModule;
import org.exbin.jaguif.context.ActiveContextManager;
import org.exbin.jaguif.context.api.ActiveContextManagement;
import org.exbin.jaguif.context.api.ContextModuleApi;
import org.exbin.jaguif.context.api.ContextRegistration;
import org.exbin.jaguif.context.api.ContextUpdateManagement;
import org.exbin.jaguif.docking.api.ContextDocking;
import org.exbin.jaguif.document.api.ContextDocument;
import org.exbin.jaguif.frame.api.FrameModuleApi;
import org.exbin.jaguif.language.api.LanguageModuleApi;
import org.exbin.jaguif.options.settings.action.SettingsAction;
import org.exbin.jaguif.options.settings.api.OptionsSettingsModuleApi;
import org.exbin.jaguif.statusbar.api.StatusBar;
import org.exbin.jaguif.statusbar.api.StatusBarModuleApi;
import org.exbin.jaguif.statusbar.gui.DefaultStatusBar;
import org.exbin.jaguif.text.encoding.EncodingsManager;
import org.exbin.jaguif.utils.DesktopUtils;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.plaf.basic.BasicArrowButton;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/**
 * Binary editor file panel.
 */
@ParametersAreNonnullByDefault
public class BinEdFilePanel extends JPanel {

    protected BinaryFileDocument fileDocument;
    protected BinEdToolbarPanel toolbarPanel = new BinEdToolbarPanel();
    protected StatusBar statusBar;

    public BinEdFilePanel() {
        super(new BorderLayout());
        add(toolbarPanel, BorderLayout.NORTH);

        FrameModuleApi frameModule = App.getModule(FrameModuleApi.class);
        StatusBarModuleApi statusBarModule = App.getModule(StatusBarModuleApi.class);
        ContextModuleApi contextModule = App.getModule(ContextModuleApi.class);
        ActiveContextManagement contextManager = frameModule.getFrameController().getContextManager();
        ContextUpdateManagement updateManager = frameModule.getFrameController().getUpdateManager();
        ContextRegistration contextRegistrator = contextModule.createContextRegistrator(FrameModuleApi.MAIN_STATUS_BAR_ID,  updateManager, contextManager);
        statusBar = statusBarModule.createStatusBar(BinedComponentModule.BINARY_STATUS_BAR_ID, contextRegistrator);
        /* statusBar = new BinaryStatusPanel() {

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
            @Override
            protected JLabel createLabel() {
                return new JBLabel();
            }

            @Nonnull
            @Override
            protected JLabel createEncodingLabel() {
                return new JBLabel() {
                    private final BasicArrowButton basicArrowButton = new BasicArrowButton(SwingConstants.NORTH);

                    @Override
                    protected void paintComponent(Graphics g) {
                        super.paintComponent(g);
                        Dimension areaSize = getSize();

                        int h = areaSize.height;
                        int w = areaSize.width;
                        int size = Math.min(Math.max((h - 4) / 4, 2), 10);
                        basicArrowButton.paintTriangle(g, w - size * 2, (h - size) / 2 - (h / 5), size, SwingConstants.NORTH, true);
                        basicArrowButton.paintTriangle(g, w - size * 2, (h - size) / 2 + (h / 5), size, SwingConstants.SOUTH, true);
                    }
                };
            }
        }; */
    }

    public void setDocument(BinaryFileDocument fileDocument) {
        this.fileDocument = fileDocument;
        BinEdComponentPanel componentPanel = fileDocument.getComponent();
        SectCodeArea codeArea = (SectCodeArea) fileDocument.getCodeArea();

        BinedSearchModule searchModule = App.getModule(BinedSearchModule.class);
        FindReplaceActions findReplaceActions = searchModule.getFindReplaceActions();
        ActionMap actionMap = codeArea.getActionMap();
        InputMap inputMap = codeArea.getInputMap();
        actionMap.put(IdeActions.ACTION_FIND, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FindReplaceActions.EditFindAction editFindAction = findReplaceActions.createEditFindAction();
                ContextModuleApi contextModule = App.getModule(ContextModuleApi.class);
                ActiveContextManagement contextManager = contextModule.createContextManager();
                contextManager.changeActiveState(ContextDocument.class, fileDocument);
                ActionModuleApi actionModule = App.getModule(ActionModuleApi.class);
//                ActionManagement actionManager = actionModule.createActionManager(contextManager);
//                actionManager.registerAction(editFindAction);
//                actionManager.initAction(editFindAction);
                editFindAction.actionPerformed(e);
            }
        });
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_DOWN_MASK, false), IdeActions.ACTION_FIND);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.META_DOWN_MASK, false), IdeActions.ACTION_FIND);
        actionMap.put(IdeActions.ACTION_REPLACE, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FindReplaceActions.EditReplaceAction editReplaceAction = findReplaceActions.createEditReplaceAction();
                ContextModuleApi contextModule = App.getModule(ContextModuleApi.class);
                ActiveContextManagement contextManager = contextModule.createContextManager();
                contextManager.changeActiveState(ContextDocument.class, fileDocument);
                ActionModuleApi actionModule = App.getModule(ActionModuleApi.class);
//                ActionManagement actionManager = actionModule.createActionManager(contextManager);
//                actionManager.registerAction(editReplaceAction);
//                actionManager.initAction(editReplaceAction);
                editReplaceAction.actionPerformed(e);
            }
        });
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_H, KeyEvent.CTRL_DOWN_MASK, false), IdeActions.ACTION_REPLACE);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_H, KeyEvent.META_DOWN_MASK, false), IdeActions.ACTION_REPLACE);

        toolbarPanel.setTargetComponent(componentPanel);
        toolbarPanel.setCodeAreaControl(new BinEdToolbarPanel.Control() {
            @Nonnull
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

        OptionsSettingsModuleApi optionsSettingsModule = App.getModule(OptionsSettingsModuleApi.class);
        SettingsAction settingsAction = (SettingsAction) optionsSettingsModule.createSettingsAction();
        FrameModuleApi frameModule = App.getModule(FrameModuleApi.class);
        settingsAction.setDialogParentComponent(() -> frameModule.getFrame());
        AbstractAction wrapperAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                settingsAction.actionPerformed(e);
                toolbarPanel.applyFromCodeArea();
            }
        };
        LanguageModuleApi languageModule = App.getModule(LanguageModuleApi.class);
        java.util.ResourceBundle optionsSettingsResourceBundle = languageModule.getBundle(org.exbin.jaguif.options.settings.OptionsSettingsModule.class);
        ActionModuleApi actionModule = App.getModule(ActionModuleApi.class);
        actionModule.initAction(wrapperAction, optionsSettingsResourceBundle, SettingsAction.ACTION_ID);
        wrapperAction.putValue(ActionConsts.ACTION_DIALOG_MODE, true);
        toolbarPanel.setOptionsAction(wrapperAction);

        BinEdIntelliJDocking docking = (BinEdIntelliJDocking) frameModule.getFrameController().getContextManager().getActiveState(ContextDocking.class);
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

                JPopupMenu popupMenu = binedComponentModule.createBinaryDocumentPopupMenu(); // codeAreaPopupMenuHandler.createPopupMenu(codeArea, popupMenuId, clickedX, clickedY);
                FrameModuleApi frameModule = App.getModule(FrameModuleApi.class);

                ActionUtils.replaceAction(popupMenu, SettingsAction.ACTION_ID, wrapperAction);
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

    @Nonnull
    public BinEdToolbarPanel getToolbarPanel() {
        return toolbarPanel;
    }

    @Nonnull
    public SectCodeArea getCodeArea() {
        return (SectCodeArea) fileDocument.getCodeArea();
    }

    @Nonnull
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
