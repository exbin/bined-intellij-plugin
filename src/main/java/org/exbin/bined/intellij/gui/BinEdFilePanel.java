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
package org.exbin.bined.intellij.gui;

import com.intellij.openapi.wm.impl.IdeBackgroundUtil;
import com.intellij.ui.Graphics2DDelegate;
import com.intellij.ui.components.JBLabel;
import org.exbin.bined.CodeAreaUtils;
import org.exbin.bined.CodeType;
import org.exbin.bined.EditOperation;
import org.exbin.bined.highlight.swing.NonprintablesCodeAreaAssessor;
import org.exbin.bined.intellij.BinEdIntelliJEditorProvider;
import org.exbin.bined.swing.CodeAreaSwingUtils;
import org.exbin.bined.swing.capability.ColorAssessorPainterCapable;
import org.exbin.bined.swing.section.SectCodeArea;
import org.exbin.framework.App;
import org.exbin.framework.bined.BinEdEditorProvider;
import org.exbin.framework.bined.BinEdFileHandler;
import org.exbin.framework.bined.BinEdFileManager;
import org.exbin.framework.bined.BinaryStatusApi;
import org.exbin.framework.bined.BinedModule;
import org.exbin.framework.bined.FileHandlingMode;
import org.exbin.framework.bined.action.GoToPositionAction;
import org.exbin.framework.bined.gui.BinEdComponentPanel;
import org.exbin.framework.bined.gui.BinaryStatusPanel;
import org.exbin.framework.bined.handler.CodeAreaPopupMenuHandler;
import org.exbin.framework.bined.editor.options.BinaryEditorOptions;
import org.exbin.framework.bined.viewer.BinedViewerModule;
import org.exbin.framework.text.encoding.EncodingsHandler;
import org.exbin.framework.file.api.FileHandler;
import org.exbin.framework.language.api.LanguageModuleApi;
import org.exbin.framework.options.api.OptionsModuleApi;
import org.exbin.framework.preferences.api.PreferencesModuleApi;
import org.exbin.framework.text.encoding.options.TextEncodingOptions;
import org.exbin.framework.utils.DesktopUtils;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.AbstractAction;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
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
import java.awt.event.MouseEvent;
import java.util.Optional;

/**
 * Binary editor file panel.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class BinEdFilePanel extends JPanel {

    private BinEdFileHandler fileHandler;
    private BinEdToolbarPanel toolbarPanel = new BinEdToolbarPanel();
    private BinaryStatusPanel statusPanel;

    public BinEdFilePanel() {
        super(new BorderLayout());
        add(toolbarPanel, BorderLayout.NORTH);
    }

    public void setFileHandler(BinEdFileHandler fileHandler) {
        this.fileHandler = fileHandler;
        BinEdComponentPanel componentPanel = fileHandler.getComponent();
        SectCodeArea codeArea = fileHandler.getCodeArea();
        toolbarPanel.setTargetComponent(componentPanel);
        toolbarPanel.setCodeAreaControl(new BinEdToolbarPanel.Control() {
            @Nonnull
            @Override public CodeType getCodeType() {
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

        OptionsModuleApi optionsModule = App.getModule(OptionsModuleApi.class);
        toolbarPanel.setOptionsAction(optionsModule.createOptionsAction());

        BinedModule binedModule = App.getModule(BinedModule.class);
        BinedViewerModule binedViewerModule = App.getModule(BinedViewerModule.class);
        BinEdIntelliJEditorProvider editorProvider = (BinEdIntelliJEditorProvider) binedModule.getEditorProvider();
        CodeAreaPopupMenuHandler codeAreaPopupMenuHandler = binedModule.createCodeAreaPopupMenuHandler(BinedModule.PopupMenuVariant.EDITOR);
        codeArea.setComponentPopupMenu(new JPopupMenu() {
            @Override
            public void show(Component invoker, int x, int y) {
                String popupMenuId = "BinEdFilePanel.popup";
                JPopupMenu popupMenu = codeAreaPopupMenuHandler.createPopupMenu(codeArea, popupMenuId, x, y);
                popupMenu.addPopupMenuListener(new PopupMenuListener() {
                    @Override
                    public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                    }

                    @Override
                    public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                        codeAreaPopupMenuHandler.dropPopupMenu(popupMenuId);
                    }

                    @Override
                    public void popupMenuCanceled(PopupMenuEvent e) {
                    }
                });
                popupMenu.show(invoker, x, y);
            }
        });

        editorProvider.addFile(fileHandler);
        editorProvider.setActiveFile(fileHandler);

        BinEdFileManager fileManager = binedModule.getFileManager();
        EncodingsHandler encodingsHandler = binedViewerModule.getEncodingsHandler();
        fileManager.registerStatusBar(new BinaryStatusPanel() {

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

        });
        fileManager.setStatusControlHandler(new BinaryStatusPanel.StatusControlHandler() {
            @Override
            public void changeEditOperation(EditOperation editOperation) {
                Optional<FileHandler> activeFile = editorProvider.getActiveFile();
                if (activeFile.isPresent()) {
                    ((BinEdFileHandler) activeFile.get()).getCodeArea().setEditOperation(editOperation);
                }
            }

            @Override
            public void changeCursorPosition() {
                GoToPositionAction action = new GoToPositionAction();
                action.setCodeArea(fileHandler.getCodeArea());
                action.actionPerformed(null);
            }

            @Override
            public void cycleNextEncoding() {
                if (encodingsHandler != null) {
                    encodingsHandler.cycleNextEncoding();
                }
            }

            @Override
            public void cyclePreviousEncoding() {
                if (encodingsHandler != null) {
                    encodingsHandler.cyclePreviousEncoding();
                }
            }

            @Override
            public void encodingsPopupEncodingsMenu(MouseEvent mouseEvent) {
                if (encodingsHandler != null) {
                    encodingsHandler.popupEncodingsMenu(mouseEvent);
                }
            }

            @Override
            public void changeMemoryMode(BinaryStatusApi.MemoryMode memoryMode) {
                Optional<FileHandler> activeFile = editorProvider.getActiveFile();
                if (activeFile.isPresent()) {
                    BinEdFileHandler fileHandler = (BinEdFileHandler) activeFile.get();
                    FileHandlingMode fileHandlingMode = fileHandler.getFileHandlingMode();
                    FileHandlingMode newHandlingMode = memoryMode == BinaryStatusApi.MemoryMode.DELTA_MODE ? FileHandlingMode.DELTA : FileHandlingMode.MEMORY;
                    if (newHandlingMode != fileHandlingMode) {
                        PreferencesModuleApi preferencesModule = App.getModule(PreferencesModuleApi.class);
                        BinaryEditorOptions options = new BinaryEditorOptions(preferencesModule.getAppPreferences());
                        if (editorProvider.releaseFile(fileHandler)) {
                            fileHandler.switchFileHandlingMode(newHandlingMode);
                            options.setFileHandlingMode(newHandlingMode);
                        }
                        ((BinEdEditorProvider) editorProvider).updateStatus();
                    }
                }
            }
        });

        PreferencesModuleApi preferencesModule = App.getModule(PreferencesModuleApi.class);
        encodingsHandler.loadFromOptions(new TextEncodingOptions(preferencesModule.getAppPreferences()));
        statusPanel = fileManager.getBinaryStatusPanel();
        add(statusPanel, BorderLayout.SOUTH);

        add(componentPanel, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    @Nonnull
    public BinEdToolbarPanel getToolbarPanel() {
        return toolbarPanel;
    }

    @Nonnull
    public BinaryStatusPanel getStatusPanel() {
        return statusPanel;
    }

    @Nonnull
    public SectCodeArea getCodeArea() {
        return fileHandler.getCodeArea();
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
