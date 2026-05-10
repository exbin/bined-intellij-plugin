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
package org.exbin.bined.intellij.diff.gui;

import com.intellij.diff.contents.DiffContent;
import com.intellij.diff.contents.DocumentContent;
import com.intellij.diff.contents.FileContent;
import com.intellij.diff.requests.ContentDiffRequest;
import com.intellij.openapi.editor.Document;
import com.intellij.ui.components.JBPanel;
import org.exbin.auxiliary.binary_data.BinaryData;
import org.exbin.auxiliary.binary_data.array.ByteArrayData;
import org.exbin.auxiliary.binary_data.array.paged.ByteArrayPagedData;
import org.exbin.auxiliary.binary_data.paged.PagedData;
import org.exbin.bined.CodeAreaCaretPosition;
import org.exbin.bined.CodeAreaUtils;
import org.exbin.bined.CodeCharactersCase;
import org.exbin.bined.CodeType;
import org.exbin.bined.EditMode;
import org.exbin.bined.EditOperation;
import org.exbin.bined.PositionCodeType;
import org.exbin.bined.capability.CaretCapable;
import org.exbin.bined.capability.CharsetCapable;
import org.exbin.bined.capability.EditModeCapable;
import org.exbin.bined.capability.SelectionCapable;
import org.exbin.bined.highlight.swing.NonprintablesCodeAreaAssessor;
import org.exbin.bined.intellij.gui.BinEdToolbarPanel;
import org.exbin.bined.jaguif.component.BinEdCodeAreaAssessor;
import org.exbin.bined.jaguif.component.BinEdComponentExtension;
import org.exbin.bined.jaguif.component.BinEdDataComponent.UpdateType;
import org.exbin.bined.jaguif.component.BinaryDataComponent;
import org.exbin.bined.jaguif.component.BinedComponentModule;
import org.exbin.bined.jaguif.component.action.GoToPositionAction;
import org.exbin.bined.jaguif.editor.settings.BinaryEditorOptions;
import org.exbin.bined.jaguif.theme.settings.CodeAreaColorOptions;
import org.exbin.bined.jaguif.theme.settings.CodeAreaLayoutOptions;
import org.exbin.bined.jaguif.theme.settings.CodeAreaThemeOptions;
import org.exbin.bined.jaguif.viewer.settings.BinaryEncodingSettingsApplier;
import org.exbin.bined.jaguif.viewer.settings.CodeAreaOptions;
import org.exbin.bined.jaguif.viewer.settings.CodeAreaStatusOptions;
import org.exbin.bined.jaguif.viewer.settings.CodeAreaViewerSettingsApplier;
import org.exbin.bined.operation.command.BinaryDataUndoRedo;
import org.exbin.bined.operation.swing.CodeAreaOperationCommandHandler;
import org.exbin.bined.section.layout.SectionCodeAreaLayoutProfile;
import org.exbin.bined.swing.CodeAreaCore;
import org.exbin.bined.swing.CodeAreaPainter;
import org.exbin.bined.swing.CodeAreaSwingUtils;
import org.exbin.bined.swing.basic.color.CodeAreaColorsProfile;
import org.exbin.bined.swing.capability.CharAssessorPainterCapable;
import org.exbin.bined.swing.capability.ColorAssessorPainterCapable;
import org.exbin.bined.swing.capability.FontCapable;
import org.exbin.bined.swing.section.SectCodeArea;
import org.exbin.bined.swing.section.theme.SectionCodeAreaThemeProfile;
import org.exbin.jaguif.App;
import org.exbin.jaguif.context.ActiveContextManager;
import org.exbin.jaguif.context.api.ActiveContextManagement;
import org.exbin.jaguif.context.api.ContextComponent;
import org.exbin.jaguif.context.api.ContextModuleApi;
import org.exbin.jaguif.context.api.ContextRegistration;
import org.exbin.jaguif.context.api.ContextUpdateManagement;
import org.exbin.jaguif.frame.api.FrameModuleApi;
import org.exbin.jaguif.language.api.LanguageModuleApi;
import org.exbin.jaguif.options.api.OptionsModuleApi;
import org.exbin.jaguif.options.api.OptionsStorage;
import org.exbin.jaguif.options.settings.action.SettingsAction;
import org.exbin.jaguif.options.settings.api.OptionsSettingsModuleApi;
import org.exbin.jaguif.statusbar.api.StatusBar;
import org.exbin.jaguif.statusbar.api.StatusBarModuleApi;
import org.exbin.jaguif.text.encoding.CharsetEncodingState;
import org.exbin.jaguif.text.encoding.ContextEncoding;
import org.exbin.jaguif.text.encoding.EncodingsManager;
import org.exbin.jaguif.text.encoding.settings.TextEncodingOptions;
import org.exbin.jaguif.text.font.settings.TextFontOptions;
import org.exbin.jaguif.utils.DesktopUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.AbstractAction;
import javax.swing.JPopupMenu;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * BinEd diff support provider to compare binary files.
 */
@ParametersAreNonnullByDefault
public class BinedDiffPanel extends JBPanel {

    protected final SectCodeAreaDiffPanel diffPanel = new SectCodeAreaDiffPanel();

    protected final Font defaultFont;
    protected final SectionCodeAreaLayoutProfile defaultLayoutProfile;
    protected final SectionCodeAreaThemeProfile defaultThemeProfile;
    protected final CodeAreaColorsProfile defaultColorProfile;
    protected List<String> encodings = new ArrayList<>();

    protected ContextComponent leftContextComponent;
    protected ContextComponent rightContextComponent;
    protected ContextRegistration leftContextRegistrator;
    protected ContextRegistration rightContextRegistrator;
    protected final BinEdToolbarPanel toolbarPanel;
    protected final StatusBar leftStatusBar;
    protected final StatusBar rightStatusBar;
    protected EncodingsManager encodingsManager;
    protected GoToPositionAction goToPositionAction = new GoToPositionAction();

    public BinedDiffPanel() {
        setLayout(new java.awt.BorderLayout());

        defaultFont = new Font(Font.MONOSPACED, Font.PLAIN, 12);
        SectCodeArea leftCodeArea = diffPanel.getLeftCodeArea();
        SectCodeArea rightCodeArea = diffPanel.getRightCodeArea();

        CodeAreaPainter leftPainter = leftCodeArea.getPainter();
        BinEdCodeAreaAssessor codeAreaAssessor = new BinEdCodeAreaAssessor(((ColorAssessorPainterCapable) leftPainter).getColorAssessor(), ((CharAssessorPainterCapable) leftPainter).getCharAssessor());
        ((ColorAssessorPainterCapable) leftPainter).setColorAssessor(codeAreaAssessor);
        ((CharAssessorPainterCapable) leftPainter).setCharAssessor(codeAreaAssessor);
        CodeAreaPainter rightPainter = rightCodeArea.getPainter();
        codeAreaAssessor = new BinEdCodeAreaAssessor(((ColorAssessorPainterCapable) rightPainter).getColorAssessor(), ((CharAssessorPainterCapable) rightPainter).getCharAssessor());
        ((ColorAssessorPainterCapable) rightPainter).setColorAssessor(codeAreaAssessor);
        ((CharAssessorPainterCapable) rightPainter).setCharAssessor(codeAreaAssessor);

        defaultLayoutProfile = leftCodeArea.getLayoutProfile();
        defaultThemeProfile = leftCodeArea.getThemeProfile();
        defaultColorProfile = leftCodeArea.getColorsProfile();
        toolbarPanel = new BinEdToolbarPanel();
        leftContextComponent = new DiffContextComponent(leftCodeArea);
        rightContextComponent = new DiffContextComponent(rightCodeArea);
        StatusBarModuleApi statusBarModule = App.getModule(StatusBarModuleApi.class);
        ContextModuleApi contextModule = App.getModule(ContextModuleApi.class);
        ActiveContextManagement leftContextManager = contextModule.createContextManager();
        ActiveContextManagement rightContextManager = contextModule.createContextManager();
        attachContext(leftCodeArea, leftContextComponent, leftContextManager);
        attachContext(rightCodeArea, rightContextComponent, rightContextManager);
        ContextUpdateManagement leftUpdateManagement = contextModule.createContextUpdateManagement(leftContextManager);
        leftUpdateManagement.addGroup(BinedComponentModule.BINARY_STATUS_BAR_ID);
        ContextUpdateManagement rightUpdateManagement = contextModule.createContextUpdateManagement(rightContextManager);
        rightUpdateManagement.addGroup(BinedComponentModule.BINARY_STATUS_BAR_ID);
        leftContextRegistrator = contextModule.createContextRegistrator(BinedComponentModule.BINARY_STATUS_BAR_ID, leftUpdateManagement, leftContextManager);
        rightContextRegistrator = contextModule.createContextRegistrator(BinedComponentModule.BINARY_STATUS_BAR_ID, rightUpdateManagement, rightContextManager);
        leftStatusBar = statusBarModule.createStatusBar(BinedComponentModule.BINARY_STATUS_BAR_ID, leftContextRegistrator);
        rightStatusBar = statusBarModule.createStatusBar(BinedComponentModule.BINARY_STATUS_BAR_ID, rightContextRegistrator);
        toolbarPanel.setTargetComponent(diffPanel);
        toolbarPanel.setCodeAreaControl(new BinEdToolbarPanel.Control() {
            @Nonnull
            @Override
            public CodeType getCodeType() {
                return leftCodeArea.getCodeType();
            }

            @Override
            public void setCodeType(CodeType codeType) {
                leftCodeArea.setCodeType(codeType);
                rightCodeArea.setCodeType(codeType);
            }

            @Override
            public boolean isShowNonprintables() {
                ColorAssessorPainterCapable painter = (ColorAssessorPainterCapable) leftCodeArea.getPainter();
                NonprintablesCodeAreaAssessor nonprintablesCodeAreaAssessor = CodeAreaSwingUtils.findColorAssessor(painter, NonprintablesCodeAreaAssessor.class);
                return CodeAreaUtils.requireNonNull(nonprintablesCodeAreaAssessor).isShowNonprintables();
            }

            @Override
            public void setShowNonprintables(boolean showNonprintables) {
                ColorAssessorPainterCapable leftPainter = (ColorAssessorPainterCapable) leftCodeArea.getPainter();
                NonprintablesCodeAreaAssessor leftNonprintablesCodeAreaAssessor = CodeAreaSwingUtils.findColorAssessor(leftPainter, NonprintablesCodeAreaAssessor.class);
                CodeAreaUtils.requireNonNull(leftNonprintablesCodeAreaAssessor).setShowNonprintables(showNonprintables);
                ColorAssessorPainterCapable rightPainter = (ColorAssessorPainterCapable) rightCodeArea.getPainter();
                NonprintablesCodeAreaAssessor rightNonprintablesCodeAreaAssessor = CodeAreaSwingUtils.findColorAssessor(rightPainter, NonprintablesCodeAreaAssessor.class);
                CodeAreaUtils.requireNonNull(rightNonprintablesCodeAreaAssessor).setShowNonprintables(showNonprintables);
            }

            @Override
            public void repaint() {
                diffPanel.repaint();
            }
        });
        OptionsSettingsModuleApi optionsSettingsModule = App.getModule(OptionsSettingsModuleApi.class);
        SettingsAction settingsAction = (SettingsAction) optionsSettingsModule.createSettingsAction();
        FrameModuleApi frameModule = App.getModule(FrameModuleApi.class);
        settingsAction.setDialogParentComponent(() -> frameModule.getFrame());
        AbstractAction wrapperAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                settingsAction.actionPerformed(e);
                // TODO Options are not applied due to no active file handler is present
                toolbarPanel.applyFromCodeArea();
            }
        };
        toolbarPanel.setOptionsAction(wrapperAction);
        toolbarPanel.setOnlineHelpAction(createOnlineHelpAction());
/*        leftStatusBar = new BinaryStatusPanel() {

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
        };
        leftStatusBar.setMinimumSize(new Dimension(0, getMinimumSize().height));
        rightStatusBar = new BinaryStatusPanel() {

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
        };
        rightStatusBar.setMinimumSize(new Dimension(0, getMinimumSize().height)); */

        init();
    }

    private void init() {
        this.add(toolbarPanel, BorderLayout.NORTH);
        encodingsManager = new EncodingsManager();
        encodingsManager.init();
        goToPositionAction.init(App.getModule(LanguageModuleApi.class).getBundle(BinedComponentModule.class));

        initialLoadFromPreferences();
        BinedComponentModule binedComponentModule = App.getModule(BinedComponentModule.class);
        JPopupMenu codeAreaPopupMenu = binedComponentModule.createCodeAreaPopupMenu();
        diffPanel.getLeftCodeArea().setComponentPopupMenu(codeAreaPopupMenu);
        diffPanel.getRightCodeArea().setComponentPopupMenu(codeAreaPopupMenu);

        diffPanel.getLeftPanel().add(leftStatusBar.getComponent(), BorderLayout.SOUTH);
        diffPanel.getRightPanel().add(rightStatusBar.getComponent(), BorderLayout.SOUTH);
        this.add(diffPanel, BorderLayout.CENTER);
        diffPanel.revalidate();
        diffPanel.repaint();
        revalidate();
        repaint();
    }

    public void setDiffContent(ContentDiffRequest request) {
        List<DiffContent> contents = request.getContents();
        if (!contents.isEmpty()) {
            BinaryData leftData = getDiffBinaryData(request, 0);
            if (leftData != null) {
                diffPanel.setLeftContentData(leftData);
            }
            SectCodeArea leftCodeArea = diffPanel.getLeftCodeArea();
            /*leftCodeArea.setComponentPopupMenu(new JPopupMenu() {
                @Override
                public void show(Component invoker, int x, int y) {
                    FrameModuleApi frameModule = App.getModule(FrameModuleApi.class);
                    ActiveContextManagement contextManager =
                            frameModule.getFrameController().getContextManager();

                    BinEdDataComponent leftDataComponent = new BinEdDataComponent(leftCodeArea);
                    contextManager.changeActiveState(ContextFont.class, leftDataComponent);
                    contextManager.changeActiveState(ContextEncoding.class, leftDataComponent);
                    contextManager.changeActiveState(ContextComponent.class, leftDataComponent);
                    contextManager.changeActiveState(DialogParentComponent.class, () -> leftCodeArea);
                    contextManager.changeActiveState(ClipboardController.class, leftDataComponent);

                    String popupMenuId = "BinDiffPanel.left";
                    int clickedX = x;
                    int clickedY = y;
                    if (invoker instanceof JViewport) {
                        clickedX += invoker.getParent().getX();
                        clickedY += invoker.getParent().getY();
                    }
                    BinedModule binedModule = App.getModule(BinedModule.class);
                    CodeAreaPopupMenuHandler codeAreaPopupMenuHandler =
                            binedModule.createCodeAreaPopupMenuHandler(BinedModule.PopupMenuVariant.NORMAL);
                    JPopupMenu popupMenu = codeAreaPopupMenuHandler.createPopupMenu(leftCodeArea, popupMenuId, clickedX, clickedY);
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
            }); */

            BinaryData rightData = getDiffBinaryData(request, 1);
            if (rightData != null) {
                diffPanel.setRightContentData(rightData);
            }
            SectCodeArea rightCodeArea = diffPanel.getRightCodeArea();
            /* rightCodeArea.setComponentPopupMenu(new JPopupMenu() {
                @Override
                public void show(Component invoker, int x, int y) {
                    FrameModuleApi frameModule = App.getModule(FrameModuleApi.class);
                    ActiveContextManagement contextManager =
                            frameModule.getFrameController().getContextManager();

                    BinEdDataComponent rightDataComponent = new BinEdDataComponent(rightCodeArea);
                    contextManager.changeActiveState(ContextFont.class, rightDataComponent);
                    contextManager.changeActiveState(ContextEncoding.class, rightDataComponent);
                    contextManager.changeActiveState(ContextComponent.class, rightDataComponent);
                    contextManager.changeActiveState(DialogParentComponent.class, () -> rightCodeArea);
                    contextManager.changeActiveState(ClipboardController.class, rightDataComponent);

                    String popupMenuId = "BinDiffPanel.right";
                    int clickedX = x;
                    int clickedY = y;
                    if (invoker instanceof JViewport) {
                        clickedX += invoker.getParent().getX();
                        clickedY += invoker.getParent().getY();
                    }
                    BinedModule binedModule = App.getModule(BinedModule.class);
                    CodeAreaPopupMenuHandler codeAreaPopupMenuHandler =
                            binedModule.createCodeAreaPopupMenuHandler(BinedModule.PopupMenuVariant.NORMAL);
                    JPopupMenu popupMenu = codeAreaPopupMenuHandler.createPopupMenu(rightCodeArea, popupMenuId, clickedX, clickedY);
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
            }); */
        }
    }

    @Nullable
    private static BinaryData getDiffBinaryData(ContentDiffRequest request, int index) {
        List<DiffContent> contents = request.getContents();
        if (contents.size() > index) {
            DiffContent diffContent = contents.get(index);
            if (diffContent instanceof FileContent) {
                PagedData pageData = new ByteArrayPagedData();
                try {
                    byte[] fileContent = ((FileContent) diffContent).getFile().contentsToByteArray();
                    pageData.insert(0, fileContent);
                    return pageData;
                } catch (IOException e) {
                    throw new IllegalStateException("Unable to read file content", e);
                }
            }
            if (diffContent instanceof DocumentContent) {
                Document document = ((DocumentContent) diffContent).getDocument();
                return new ByteArrayData(document.getText().getBytes(StandardCharsets.UTF_8));
            }
        }

        return null;
    }

    private static void attachContext(SectCodeArea codeArea, ContextComponent contextComponent, ActiveContextManagement contextManagement) {
        contextManagement.changeActiveState(ContextComponent.class, contextComponent);
        codeArea.addDataChangedListener(() -> {
            contextManagement.updateActiveState(ContextComponent.class, contextComponent, UpdateType.DATA_CHANGED);
        });

        ((SelectionCapable) codeArea).addSelectionChangedListener(() -> {
            contextManagement.updateActiveState(ContextComponent.class, contextComponent, UpdateType.SELECTION_CHANGED);
        });

        ((CaretCapable) codeArea).addCaretMovedListener((CodeAreaCaretPosition caretPosition) -> {
            contextManagement.updateActiveState(ContextComponent.class, contextComponent, UpdateType.CURSOR_MOVED);
        });

        ((EditModeCapable) codeArea).addEditModeChangedListener((EditMode mode, EditOperation operation) -> {
            contextManagement.updateActiveState(ContextComponent.class, contextComponent, UpdateType.EDIT_MODE_CHANGED);
        });

        OptionsSettingsModuleApi optionsSettingsModule = App.getModule(OptionsSettingsModuleApi.class);
        BinaryEncodingSettingsApplier settingsApplier = new BinaryEncodingSettingsApplier();
        settingsApplier.applySettings(
                contextManagement,
                optionsSettingsModule.getMainSettingsManager().getSettingsOptionsProvider());
    }

    private void initialLoadFromPreferences() {
        OptionsModuleApi optionsModule = App.getModule(OptionsModuleApi.class);
        OptionsStorage preferences = optionsModule.getAppOptions();

        applyOptions(preferences, diffPanel.getLeftCodeArea());
        applyOptions(preferences, diffPanel.getRightCodeArea());

        CodeAreaStatusOptions statusOptions = new CodeAreaStatusOptions(preferences);
//        leftStatusPanel.loadFromOptions(statusOptions);
//        rightStatusPanel.loadFromOptions(statusOptions);
        toolbarPanel.applyFromCodeArea();
        toolbarPanel.loadFromOptions(preferences);

//        BinaryStatusApi.MemoryMode memoryMode = BinaryStatusApi.MemoryMode.READ_ONLY;
//        leftStatusPanel.setMemoryMode(memoryMode);
//        rightStatusPanel.setMemoryMode(memoryMode);
    }

    private void applyOptions(OptionsStorage optionsStorage, SectCodeArea codeArea) {
        CodeAreaViewerSettingsApplier.applyToCodeArea(new CodeAreaOptions(optionsStorage), codeArea);

        TextEncodingOptions encodingOptions = new TextEncodingOptions(optionsStorage);
        ((CharsetCapable) codeArea).setCharset(Charset.forName(encodingOptions
                .getSelectedEncoding()));
        encodings = encodingOptions.getEncodings();
        TextFontOptions fontOptions = new TextFontOptions(optionsStorage);
        ((FontCapable) codeArea).setCodeFont(fontOptions.isUseDefaultFont() ?
                defaultFont :
                fontOptions.getFont(defaultFont));

        BinaryEditorOptions editorOptions = new BinaryEditorOptions(optionsStorage);
        //        switchShowValuesPanel(editorOptions.isShowValuesPanel());
        if (codeArea.getCommandHandler() instanceof CodeAreaOperationCommandHandler) {
            ((CodeAreaOperationCommandHandler) codeArea.getCommandHandler()).setEnterKeyHandlingMode(editorOptions.getEnterKeyHandlingMode());
        }

        CodeAreaLayoutOptions layoutOptions = new CodeAreaLayoutOptions(optionsStorage);
        int selectedLayoutProfile = layoutOptions.getSelectedProfile();
        if (selectedLayoutProfile >= 0) {
            codeArea.setLayoutProfile(layoutOptions.getLayoutProfile(selectedLayoutProfile));
        } else {
            codeArea.setLayoutProfile(defaultLayoutProfile);
        }

        CodeAreaThemeOptions themeOptions = new CodeAreaThemeOptions(optionsStorage);
        int selectedThemeProfile = themeOptions.getSelectedProfile();
        if (selectedThemeProfile >= 0) {
            codeArea.setThemeProfile(themeOptions.getThemeProfile(selectedThemeProfile));
        } else {
            codeArea.setThemeProfile(defaultThemeProfile);
        }

        CodeAreaColorOptions colorOptions = new CodeAreaColorOptions(optionsStorage);
        int selectedColorProfile = colorOptions.getSelectedProfile();
        if (selectedColorProfile >= 0) {
            codeArea.setColorsProfile(colorOptions.getColorsProfile(selectedColorProfile));
        } else {
            codeArea.setColorsProfile(defaultColorProfile);
        }
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

    public static class DiffContextComponent implements BinaryDataComponent, CharsetEncodingState {

        SectCodeArea codeArea;

        public DiffContextComponent(SectCodeArea codeArea) {
            this.codeArea = codeArea;
        }

        @Nonnull
        @Override
        public CodeAreaCore getCodeArea() {
            return codeArea;
        }

        @Override
        public Optional<BinaryDataUndoRedo> getUndoRedo() {
            return Optional.empty();
        }

        @Override
        public <T extends BinEdComponentExtension> T getComponentExtension(Class<T> clazz) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Component getComponent() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public CodeType getCodeType() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setCodeType(CodeType codeType) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public PositionCodeType getPositionCodeType() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setPositionCodeType(PositionCodeType positionCodeType) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public CodeCharactersCase getCodeCharactersCase() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setCodeCharactersCase(CodeCharactersCase codeCharactersCase) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean isShowNonprintables() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setShowNonprintables(boolean showNonprintables) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Nonnull
        @Override
        public String getEncoding() {
            return ((CharsetCapable) getCodeArea()).getCharset().toString();
        }

        @Override
        public void setEncoding(String encodingName) {
            ((CharsetCapable) getCodeArea()).setCharset(Charset.forName(encodingName));
        }
    }
}
