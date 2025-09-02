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
package org.exbin.bined.intellij.diff.gui;

import com.intellij.diff.contents.DiffContent;
import com.intellij.diff.contents.DocumentContent;
import com.intellij.diff.contents.FileContent;
import com.intellij.diff.requests.ContentDiffRequest;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.wm.impl.IdeBackgroundUtil;
import com.intellij.ui.Graphics2DDelegate;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import org.exbin.auxiliary.binary_data.BinaryData;
import org.exbin.auxiliary.binary_data.array.ByteArrayData;
import org.exbin.auxiliary.binary_data.array.paged.ByteArrayPagedData;
import org.exbin.auxiliary.binary_data.paged.PagedData;
import org.exbin.bined.CodeAreaCaretPosition;
import org.exbin.bined.CodeAreaUtils;
import org.exbin.bined.CodeType;
import org.exbin.bined.EditOperation;
import org.exbin.bined.capability.CharsetCapable;
import org.exbin.bined.highlight.swing.NonprintablesCodeAreaAssessor;
import org.exbin.bined.intellij.BinEdIntelliJPlugin;
import org.exbin.bined.intellij.gui.BinEdToolbarPanel;
import org.exbin.bined.intellij.options.BinEdApplyOptions;
import org.exbin.bined.intellij.preferences.IntelliJPreferencesWrapper;
import org.exbin.bined.operation.swing.CodeAreaOperationCommandHandler;
import org.exbin.bined.section.layout.SectionCodeAreaLayoutProfile;
import org.exbin.bined.swing.CodeAreaPainter;
import org.exbin.bined.swing.CodeAreaSwingUtils;
import org.exbin.bined.swing.basic.color.CodeAreaColorsProfile;
import org.exbin.bined.swing.capability.CharAssessorPainterCapable;
import org.exbin.bined.swing.capability.ColorAssessorPainterCapable;
import org.exbin.bined.swing.capability.FontCapable;
import org.exbin.bined.swing.section.SectCodeArea;
import org.exbin.bined.swing.section.theme.SectionCodeAreaThemeProfile;
import org.exbin.framework.App;
import org.exbin.framework.action.api.ActiveComponent;
import org.exbin.framework.action.api.ComponentActivationListener;
import org.exbin.framework.action.api.DialogParentComponent;
import org.exbin.framework.action.api.clipboard.ClipboardController;
import org.exbin.framework.bined.BinEdCodeAreaAssessor;
import org.exbin.framework.bined.BinEdDataComponent;
import org.exbin.framework.bined.BinaryStatusApi;
import org.exbin.framework.bined.BinedModule;
import org.exbin.framework.bined.action.GoToPositionAction;
import org.exbin.framework.bined.editor.options.BinaryEditorOptions;
import org.exbin.framework.bined.gui.BinaryStatusPanel;
import org.exbin.framework.bined.handler.CodeAreaPopupMenuHandler;
import org.exbin.framework.bined.options.StatusOptions;
import org.exbin.framework.bined.theme.options.CodeAreaColorOptions;
import org.exbin.framework.bined.theme.options.CodeAreaLayoutOptions;
import org.exbin.framework.bined.theme.options.CodeAreaThemeOptions;
import org.exbin.framework.bined.viewer.options.CodeAreaOptions;
import org.exbin.framework.frame.api.FrameModuleApi;
import org.exbin.framework.language.api.LanguageModuleApi;
import org.exbin.framework.options.action.OptionsAction;
import org.exbin.framework.options.api.OptionsModuleApi;
import org.exbin.framework.text.encoding.EncodingsHandler;
import org.exbin.framework.text.encoding.TextEncodingStatusApi;
import org.exbin.framework.text.encoding.options.TextEncodingOptions;
import org.exbin.framework.text.font.options.TextFontOptions;
import org.exbin.framework.utils.DesktopUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.JViewport;
import javax.swing.SwingConstants;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.plaf.basic.BasicArrowButton;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * BinEd diff support provider to compare binary files.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class BinedDiffPanel extends JBPanel {

    private final IntelliJPreferencesWrapper preferences;
    private final SectCodeAreaDiffPanel diffPanel = new SectCodeAreaDiffPanel();

    private final Font defaultFont;
    private final SectionCodeAreaLayoutProfile defaultLayoutProfile;
    private final SectionCodeAreaThemeProfile defaultThemeProfile;
    private final CodeAreaColorsProfile defaultColorProfile;

    private final BinEdToolbarPanel toolbarPanel;
    private final BinaryStatusPanel leftStatusPanel;
    private final BinaryStatusPanel rightStatusPanel;
    private EncodingsHandler encodingsHandler;
    private GoToPositionAction goToPositionAction = new GoToPositionAction();

    public BinedDiffPanel() {
        setLayout(new java.awt.BorderLayout());

        preferences = new IntelliJPreferencesWrapper(getPreferences(), BinEdIntelliJPlugin.PLUGIN_PREFIX);
        defaultFont = new Font(Font.MONOSPACED, Font.PLAIN, 12);
        SectCodeArea leftCodeArea = diffPanel.getLeftCodeArea();
        SectCodeArea rightCodeArea = diffPanel.getRightCodeArea();

        CodeAreaPainter leftPainter = leftCodeArea.getPainter();
        BinEdCodeAreaAssessor codeAreaAssessor = new BinEdCodeAreaAssessor(((ColorAssessorPainterCapable)leftPainter).getColorAssessor(), ((CharAssessorPainterCapable)leftPainter).getCharAssessor());
        ((ColorAssessorPainterCapable)leftPainter).setColorAssessor(codeAreaAssessor);
        ((CharAssessorPainterCapable)leftPainter).setCharAssessor(codeAreaAssessor);
        CodeAreaPainter rightPainter = rightCodeArea.getPainter();
        codeAreaAssessor = new BinEdCodeAreaAssessor(((ColorAssessorPainterCapable)rightPainter).getColorAssessor(), ((CharAssessorPainterCapable)rightPainter).getCharAssessor());
        ((ColorAssessorPainterCapable)rightPainter).setColorAssessor(codeAreaAssessor);
        ((CharAssessorPainterCapable)rightPainter).setCharAssessor(codeAreaAssessor);

        defaultLayoutProfile = leftCodeArea.getLayoutProfile();
        defaultThemeProfile = leftCodeArea.getThemeProfile();
        defaultColorProfile = leftCodeArea.getColorsProfile();
        toolbarPanel = new BinEdToolbarPanel();
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
        OptionsModuleApi optionsModule = App.getModule(OptionsModuleApi.class);
        OptionsAction optionsAction = (OptionsAction) optionsModule.createOptionsAction();
        FrameModuleApi frameModule = App.getModule(FrameModuleApi.class);
        optionsAction.setDialogParentComponent(() -> frameModule.getFrame());
        toolbarPanel.setOptionsAction(optionsAction);
        toolbarPanel.setOnlineHelpAction(createOnlineHelpAction());
        leftStatusPanel = new BinaryStatusPanel() {

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
        rightStatusPanel = new BinaryStatusPanel() {

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

        init();
    }

    private void init() {
        this.add(toolbarPanel, BorderLayout.NORTH);
        encodingsHandler = new EncodingsHandler();
        encodingsHandler.init();
        encodingsHandler.setTextEncodingStatus(new TextEncodingStatusApi() {
            @Nonnull
            @Override
            public String getEncoding() {
                return leftStatusPanel.getEncoding();
            }

            @Override
            public void setEncoding(String encodingName) {
                diffPanel.getLeftCodeArea().setCharset(Charset.forName(encodingName));
                diffPanel.getRightCodeArea().setCharset(Charset.forName(encodingName));
                leftStatusPanel.setEncoding(encodingName);
                rightStatusPanel.setEncoding(encodingName);
                new TextEncodingOptions(preferences).setSelectedEncoding(encodingName);
            }
        });
        goToPositionAction.setup(App.getModule(LanguageModuleApi.class).getBundle(BinedModule.class));

        registerBinaryStatus(leftStatusPanel, diffPanel.getLeftCodeArea());
        registerBinaryStatus(rightStatusPanel, diffPanel.getRightCodeArea());

        initialLoadFromPreferences();

        diffPanel.getLeftPanel().add(leftStatusPanel, BorderLayout.SOUTH);
        diffPanel.getRightPanel().add(rightStatusPanel, BorderLayout.SOUTH);
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
                updateBinaryStatus(leftStatusPanel, diffPanel.getLeftCodeArea());
            }
            SectCodeArea leftCodeArea = diffPanel.getLeftCodeArea();
            leftCodeArea.setComponentPopupMenu(new JPopupMenu() {
                @Override
                public void show(Component invoker, int x, int y) {
                    FrameModuleApi frameModule = App.getModule(FrameModuleApi.class);
                    ComponentActivationListener componentActivationListener =
                            frameModule.getFrameHandler().getComponentActivationListener();

                    BinEdDataComponent leftBinEdDataComponent = new BinEdDataComponent(leftCodeArea);
                    componentActivationListener.updated(ActiveComponent.class, leftBinEdDataComponent);
                    componentActivationListener.updated(DialogParentComponent.class, () -> leftCodeArea);
                    componentActivationListener.updated(ClipboardController.class, leftBinEdDataComponent);

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
            });

            BinaryData rightData = getDiffBinaryData(request, 1);
            if (rightData != null) {
                diffPanel.setRightContentData(rightData);
                updateBinaryStatus(rightStatusPanel, diffPanel.getRightCodeArea());
            }
            SectCodeArea rightCodeArea = diffPanel.getRightCodeArea();
            rightCodeArea.setComponentPopupMenu(new JPopupMenu() {
                @Override
                public void show(Component invoker, int x, int y) {
                    FrameModuleApi frameModule = App.getModule(FrameModuleApi.class);
                    ComponentActivationListener componentActivationListener =
                            frameModule.getFrameHandler().getComponentActivationListener();

                    BinEdDataComponent rightBinEdDataComponent = new BinEdDataComponent(rightCodeArea);
                    componentActivationListener.updated(ActiveComponent.class, rightBinEdDataComponent);
                    componentActivationListener.updated(DialogParentComponent.class, () -> rightCodeArea);
                    componentActivationListener.updated(ClipboardController.class, rightBinEdDataComponent);

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
            });
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

    public void registerBinaryStatus(BinaryStatusApi binaryStatus, SectCodeArea codeArea) {
        codeArea.addCaretMovedListener((CodeAreaCaretPosition caretPosition) -> {
            binaryStatus.setCursorPosition(caretPosition);
        });
        codeArea.addSelectionChangedListener(() -> {
            binaryStatus.setSelectionRange(codeArea.getSelection());
        });

        codeArea.addEditModeChangedListener(binaryStatus::setEditMode);

        codeArea.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                updateBinaryStatus(binaryStatus, codeArea);
            }
        });

        updateBinaryStatus(binaryStatus, codeArea);

        ((BinaryStatusPanel) binaryStatus).setController(new BinaryStatusController());
    }

    private void updateBinaryStatus(BinaryStatusApi binaryStatus, SectCodeArea codeArea) {
        binaryStatus.setEditMode(codeArea.getEditMode(), codeArea.getActiveOperation());
        binaryStatus.setCursorPosition(codeArea.getActiveCaretPosition());
        binaryStatus.setSelectionRange(codeArea.getSelection());
        long dataSize = codeArea.getDataSize();
        binaryStatus.setCurrentDocumentSize(dataSize, dataSize);
        goToPositionAction.setCodeArea(codeArea);
    }

    private void initialLoadFromPreferences() {
        applyOptions(new BinEdApplyOptions() {
            @Nonnull
            @Override
            public CodeAreaOptions getCodeAreaOptions() {
                return new CodeAreaOptions(preferences);
            }

            @Nonnull
            @Override
            public TextEncodingOptions getEncodingOptions() {
                return new TextEncodingOptions(preferences);
            }

            @Nonnull
            @Override
            public TextFontOptions getFontOptions() {
                return new TextFontOptions(preferences);
            }

            @Nonnull
            @Override
            public BinaryEditorOptions getEditorOptions() {
                return new BinaryEditorOptions(preferences);
            }

            @Nonnull
            @Override
            public StatusOptions getStatusOptions() {
                return new StatusOptions(preferences);
            }

            @Nonnull
            @Override
            public CodeAreaLayoutOptions getLayoutOptions() {
                return new CodeAreaLayoutOptions(preferences);
            }

            @Nonnull
            @Override
            public CodeAreaColorOptions getColorOptions() {
                return new CodeAreaColorOptions(preferences);
            }

            @Nonnull
            @Override
            public CodeAreaThemeOptions getThemeOptions() {
                return new CodeAreaThemeOptions(preferences);
            }
        });

        encodingsHandler.loadFromOptions(new TextEncodingOptions(preferences));
        leftStatusPanel.loadFromOptions(new StatusOptions(preferences));
        rightStatusPanel.loadFromOptions(new StatusOptions(preferences));
        toolbarPanel.loadFromOptions(preferences);

        BinaryStatusApi.MemoryMode memoryMode = BinaryStatusApi.MemoryMode.READ_ONLY;
        leftStatusPanel.setMemoryMode(memoryMode);
        rightStatusPanel.setMemoryMode(memoryMode);
    }

    private void applyOptions(BinEdApplyOptions applyOptions) {
        applyOptions(applyOptions, diffPanel.getLeftCodeArea());
        applyOptions(applyOptions, diffPanel.getRightCodeArea());
    }

    private void applyOptions(BinEdApplyOptions applyOptions, SectCodeArea codeArea) {
        CodeAreaOptions.applyToCodeArea(applyOptions.getCodeAreaOptions(), codeArea);

        ((CharsetCapable) codeArea).setCharset(Charset.forName(applyOptions.getEncodingOptions()
                .getSelectedEncoding()));
        encodingsHandler.setEncodings(applyOptions.getEncodingOptions().getEncodings());
        ((FontCapable) codeArea).setCodeFont(applyOptions.getFontOptions().isUseDefaultFont() ?
                defaultFont :
                applyOptions.getFontOptions().getFont(defaultFont));

        BinaryEditorOptions editorOptions = applyOptions.getEditorOptions();
        //        switchShowValuesPanel(editorOptions.isShowValuesPanel());
        if (codeArea.getCommandHandler() instanceof CodeAreaOperationCommandHandler) {
            ((CodeAreaOperationCommandHandler) codeArea.getCommandHandler()).setEnterKeyHandlingMode(editorOptions.getEnterKeyHandlingMode());
        }

        StatusOptions statusOptions = applyOptions.getStatusOptions();
        leftStatusPanel.setStatusOptions(statusOptions);
        rightStatusPanel.setStatusOptions(statusOptions);
        toolbarPanel.applyFromCodeArea();

        CodeAreaLayoutOptions layoutOptions = applyOptions.getLayoutOptions();
        int selectedLayoutProfile = layoutOptions.getSelectedProfile();
        if (selectedLayoutProfile >= 0) {
            codeArea.setLayoutProfile(layoutOptions.getLayoutProfile(selectedLayoutProfile));
        } else {
            codeArea.setLayoutProfile(defaultLayoutProfile);
        }

        CodeAreaThemeOptions themeOptions = applyOptions.getThemeOptions();
        int selectedThemeProfile = themeOptions.getSelectedProfile();
        if (selectedThemeProfile >= 0) {
            codeArea.setThemeProfile(themeOptions.getThemeProfile(selectedThemeProfile));
        } else {
            codeArea.setThemeProfile(defaultThemeProfile);
        }

        CodeAreaColorOptions colorOptions = applyOptions.getColorOptions();
        int selectedColorProfile = colorOptions.getSelectedProfile();
        if (selectedColorProfile >= 0) {
            codeArea.setColorsProfile(colorOptions.getColorsProfile(selectedColorProfile));
        } else {
            codeArea.setColorsProfile(defaultColorProfile);
        }
    }

    @Nonnull
    public static PropertiesComponent getPreferences() {
        return PropertiesComponent.getInstance();
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

    private Icon load(String path) {
        return IconLoader.getIcon(path, getClass());
    }

    @ParametersAreNonnullByDefault
    private class BinaryStatusController implements BinaryStatusPanel.Controller, BinaryStatusPanel.EncodingsController, BinaryStatusPanel.MemoryModeController {
        @Override
        public void changeEditOperation(EditOperation editOperation) {
            SectCodeArea leftCodeArea = diffPanel.getLeftCodeArea();
            SectCodeArea rightCodeArea = diffPanel.getRightCodeArea();
            leftCodeArea.setEditOperation(editOperation);
            rightCodeArea.setEditOperation(editOperation);
        }

        @Override
        public void changeCursorPosition() {
            goToPositionAction.actionPerformed(new ActionEvent(BinedDiffPanel.this, 0, ""));
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
            // Ignore
        }
    }
}
