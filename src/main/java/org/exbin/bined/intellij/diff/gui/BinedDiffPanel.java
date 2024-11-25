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
import com.intellij.ui.components.JBPanel;
import org.exbin.auxiliary.binary_data.BinaryData;
import org.exbin.auxiliary.binary_data.ByteArrayData;
import org.exbin.auxiliary.binary_data.paged.PagedData;
import org.exbin.bined.CodeAreaCaretPosition;
import org.exbin.bined.CodeAreaUtils;
import org.exbin.bined.CodeType;
import org.exbin.bined.EditOperation;
import org.exbin.bined.capability.CharsetCapable;
import org.exbin.bined.highlight.swing.NonprintablesCodeAreaAssessor;
import org.exbin.bined.intellij.BinEdIntelliJPlugin;
import org.exbin.bined.intellij.BinEdPluginStartupActivity;
import org.exbin.bined.intellij.gui.BinEdToolbarPanel;
import org.exbin.bined.intellij.options.BinEdApplyOptions;
import org.exbin.bined.intellij.options.IntegrationOptions;
import org.exbin.bined.intellij.preferences.IntelliJPreferencesWrapper;
import org.exbin.bined.operation.swing.CodeAreaOperationCommandHandler;
import org.exbin.bined.section.layout.SectionCodeAreaLayoutProfile;
import org.exbin.bined.swing.CodeAreaSwingUtils;
import org.exbin.bined.swing.basic.color.CodeAreaColorsProfile;
import org.exbin.bined.swing.capability.ColorAssessorPainterCapable;
import org.exbin.bined.swing.capability.FontCapable;
import org.exbin.bined.swing.section.SectCodeArea;
import org.exbin.bined.swing.section.diff.SectCodeAreaDiffPanel;
import org.exbin.bined.swing.section.theme.SectionCodeAreaThemeProfile;
import org.exbin.framework.App;
import org.exbin.framework.bined.BinaryStatusApi;
import org.exbin.framework.bined.BinedModule;
import org.exbin.framework.bined.action.GoToPositionAction;
import org.exbin.framework.bined.gui.BinaryStatusPanel;
import org.exbin.framework.bined.handler.CodeAreaPopupMenuHandler;
import org.exbin.framework.bined.inspector.options.DataInspectorOptions;
import org.exbin.framework.bined.options.CodeAreaColorOptions;
import org.exbin.framework.bined.options.CodeAreaLayoutOptions;
import org.exbin.framework.bined.options.CodeAreaOptions;
import org.exbin.framework.bined.options.CodeAreaThemeOptions;
import org.exbin.framework.bined.options.EditorOptions;
import org.exbin.framework.bined.options.StatusOptions;
import org.exbin.framework.bined.options.impl.CodeAreaOptionsImpl;
import org.exbin.framework.bined.preferences.BinaryEditorPreferences;
import org.exbin.framework.editor.text.EncodingsHandler;
import org.exbin.framework.editor.text.TextEncodingStatusApi;
import org.exbin.framework.editor.text.options.TextEncodingOptions;
import org.exbin.framework.editor.text.options.TextFontOptions;
import org.exbin.framework.language.api.LanguageModuleApi;
import org.exbin.framework.options.api.OptionsModuleApi;
import org.exbin.framework.utils.DesktopUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JPopupMenu;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
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

    private final BinaryEditorPreferences preferences;
    private final SectCodeAreaDiffPanel diffPanel = new SectCodeAreaDiffPanel();

    private final Font defaultFont;
    private final SectionCodeAreaLayoutProfile defaultLayoutProfile;
    private final SectionCodeAreaThemeProfile defaultThemeProfile;
    private final CodeAreaColorsProfile defaultColorProfile;

    private final BinEdToolbarPanel toolbarPanel;
    private final BinaryStatusPanel statusPanel;
    private EncodingsHandler encodingsHandler;
    private BinaryStatusApi binaryStatus;
    private TextEncodingStatusApi encodingStatus;
    private GoToPositionAction goToPositionAction = new GoToPositionAction();

    public BinedDiffPanel() {
        setLayout(new java.awt.BorderLayout());

        preferences = new BinaryEditorPreferences(new IntelliJPreferencesWrapper(getPreferences(),
                BinEdIntelliJPlugin.PLUGIN_PREFIX));
        defaultFont = new Font(Font.MONOSPACED, Font.PLAIN, 12);
        SectCodeArea leftCodeArea = diffPanel.getLeftCodeArea();
        SectCodeArea rightCodeArea = diffPanel.getRightCodeArea();
        defaultLayoutProfile = leftCodeArea.getLayoutProfile();
        defaultThemeProfile = leftCodeArea.getThemeProfile();
        defaultColorProfile = leftCodeArea.getColorsProfile();
        toolbarPanel = new BinEdToolbarPanel();
        toolbarPanel.setTargetComponent(diffPanel);
        toolbarPanel.setCodeAreaControl(new BinEdToolbarPanel.Control() {
            @Nonnull
            @Override public CodeType getCodeType() {
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
        toolbarPanel.setOptionsAction(optionsModule.createOptionsAction());
        toolbarPanel.setOnlineHelpAction(createOnlineHelpAction());
        statusPanel = new BinaryStatusPanel();

        init();
    }

    private void init() {
        this.add(toolbarPanel, BorderLayout.NORTH);
        registerEncodingStatus(statusPanel);
        encodingsHandler = new EncodingsHandler();
        encodingsHandler.setParentComponent(this);
        encodingsHandler.init();
        encodingsHandler.setTextEncodingStatus(new TextEncodingStatusApi() {
            @Nonnull
            @Override
            public String getEncoding() {
                return encodingStatus.getEncoding();
            }

            @Override
            public void setEncoding(String encodingName) {
                diffPanel.getLeftCodeArea().setCharset(Charset.forName(encodingName));
                diffPanel.getRightCodeArea().setCharset(Charset.forName(encodingName));
                encodingStatus.setEncoding(encodingName);
                preferences.getEncodingPreferences().setSelectedEncoding(encodingName);
            }
        });
        goToPositionAction.setup(App.getModule(LanguageModuleApi.class).getBundle(BinedModule.class));

        registerBinaryStatus(statusPanel);

        initialLoadFromPreferences();

        this.add(statusPanel, BorderLayout.SOUTH);
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
            if (leftData == null) {
                return;
            }
            diffPanel.setLeftContentData(leftData);
            SectCodeArea leftCodeArea = diffPanel.getLeftCodeArea();
            leftCodeArea.setComponentPopupMenu(new JPopupMenu() {
                @Override
                public void show(Component invoker, int x, int y) {
                    String popupMenuId = "BinDiffPanel.left";
                    BinedModule binedModule = App.getModule(BinedModule.class);
                    CodeAreaPopupMenuHandler codeAreaPopupMenuHandler =
                            binedModule.createCodeAreaPopupMenuHandler(BinedModule.PopupMenuVariant.NORMAL);
                    JPopupMenu popupMenu = codeAreaPopupMenuHandler.createPopupMenu(leftCodeArea, popupMenuId, x, y);
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
            }
            SectCodeArea rightCodeArea = diffPanel.getRightCodeArea();
            rightCodeArea.setComponentPopupMenu(new JPopupMenu() {
                @Override
                public void show(Component invoker, int x, int y) {
                    String popupMenuId = "BinDiffPanel.right";
                    BinedModule binedModule = App.getModule(BinedModule.class);
                    CodeAreaPopupMenuHandler codeAreaPopupMenuHandler =
                            binedModule.createCodeAreaPopupMenuHandler(BinedModule.PopupMenuVariant.NORMAL);
                    JPopupMenu popupMenu = codeAreaPopupMenuHandler.createPopupMenu(leftCodeArea, popupMenuId, x, y);
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
                PagedData pageData = new PagedData();
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

    public void registerBinaryStatus(BinaryStatusApi binaryStatusApi) {
        this.binaryStatus = binaryStatusApi;
        SectCodeArea leftCodeArea = diffPanel.getLeftCodeArea();
        SectCodeArea rightCodeArea = diffPanel.getRightCodeArea();
        leftCodeArea.addCaretMovedListener((CodeAreaCaretPosition caretPosition) -> {
            binaryStatus.setCursorPosition(caretPosition);
        });
        rightCodeArea.addCaretMovedListener((CodeAreaCaretPosition caretPosition) -> {
            binaryStatus.setCursorPosition(caretPosition);
        });
        leftCodeArea.addSelectionChangedListener(() -> {
            binaryStatus.setSelectionRange(leftCodeArea.getSelection());
        });
        rightCodeArea.addSelectionChangedListener(() -> {
            binaryStatus.setSelectionRange(rightCodeArea.getSelection());
        });

        leftCodeArea.addEditModeChangedListener(binaryStatus::setEditMode);
        rightCodeArea.addEditModeChangedListener(binaryStatus::setEditMode);

        leftCodeArea.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                updateBinaryStatus(leftCodeArea);
            }
        });
        rightCodeArea.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                updateBinaryStatus(rightCodeArea);
            }
        });

        updateBinaryStatus(leftCodeArea);

        ((BinaryStatusPanel) binaryStatus).setStatusControlHandler(new BinaryStatusPanel.StatusControlHandler() {
            @Override
            public void changeEditOperation(EditOperation editOperation) {
                leftCodeArea.setEditOperation(editOperation);
                rightCodeArea.setEditOperation(editOperation);
            }

            @Override
            public void changeCursorPosition() {
                goToPositionAction.actionPerformed(new ActionEvent(BinedDiffPanel.this, 0, ""));
            }

            @Override
            public void cycleEncodings() {
                if (encodingsHandler != null) {
                    encodingsHandler.cycleEncodings();
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
        });
    }

    private void updateBinaryStatus(SectCodeArea codeArea) {
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
                return preferences.getCodeAreaPreferences();
            }

            @Nonnull
            @Override
            public TextEncodingOptions getEncodingOptions() {
                return preferences.getEncodingPreferences();
            }

            @Nonnull
            @Override
            public TextFontOptions getFontOptions() {
                return preferences.getFontPreferences();
            }

            @Nonnull
            @Override
            public IntegrationOptions getIntegrationOptions() {
                return null; // TODO preferences.getIntegrationPreferences();
            }

            @Nonnull
            @Override
            public EditorOptions getEditorOptions() {
                return preferences.getEditorPreferences();
            }

            @Nonnull
            @Override
            public StatusOptions getStatusOptions() {
                return preferences.getStatusPreferences();
            }

            @Nonnull
            @Override
            public DataInspectorOptions getDataInspectorOptions() {
                return null; // TODO preferences.getDataInspectorPreferences();
            }

            @Nonnull
            @Override
            public CodeAreaLayoutOptions getLayoutOptions() {
                return preferences.getLayoutPreferences();
            }

            @Nonnull
            @Override
            public CodeAreaColorOptions getColorOptions() {
                return preferences.getColorPreferences();
            }

            @Nonnull
            @Override
            public CodeAreaThemeOptions getThemeOptions() {
                return preferences.getThemePreferences();
            }
        });

        encodingsHandler.loadFromPreferences(preferences.getEncodingPreferences());
        statusPanel.loadFromPreferences(preferences.getStatusPreferences());
        toolbarPanel.loadFromPreferences(new BinaryEditorPreferences(preferences.getPreferences()));

        updateCurrentMemoryMode();
    }

    private void updateCurrentMemoryMode() {
        BinaryStatusApi.MemoryMode memoryMode = BinaryStatusApi.MemoryMode.READ_ONLY;

        if (binaryStatus != null) {
            binaryStatus.setMemoryMode(memoryMode);
        }
    }

    public void registerEncodingStatus(TextEncodingStatusApi encodingStatusApi) {
        this.encodingStatus = encodingStatusApi;
        // TODO
//        setCharsetChangeListener(() -> {
//            String selectedEncoding = diffPanel.getLeftCodeArea().getCharset().name();
//            encodingStatus.setEncoding(selectedEncoding);
//        });
    }

    private void updateApplyOptions(BinEdApplyOptions applyOptions) {
        SectCodeArea leftCodeArea = diffPanel.getLeftCodeArea();
        SectCodeArea rightCodeArea = diffPanel.getRightCodeArea();
        CodeAreaOptionsImpl.applyFromCodeArea(applyOptions.getCodeAreaOptions(), leftCodeArea);
        CodeAreaOptionsImpl.applyFromCodeArea(applyOptions.getCodeAreaOptions(), rightCodeArea);
        applyOptions.getEncodingOptions().setSelectedEncoding(((CharsetCapable) leftCodeArea).getCharset().name());
        applyOptions.getEncodingOptions().setSelectedEncoding(((CharsetCapable) rightCodeArea).getCharset().name());

        EditorOptions editorOptions = applyOptions.getEditorOptions();
        if (leftCodeArea.getCommandHandler() instanceof CodeAreaOperationCommandHandler) {
            editorOptions.setEnterKeyHandlingMode(((CodeAreaOperationCommandHandler) leftCodeArea.getCommandHandler()).getEnterKeyHandlingMode());
        }
        if (rightCodeArea.getCommandHandler() instanceof CodeAreaOperationCommandHandler) {
            editorOptions.setEnterKeyHandlingMode(((CodeAreaOperationCommandHandler) rightCodeArea.getCommandHandler()).getEnterKeyHandlingMode());
        }

        // TODO applyOptions.getStatusOptions().loadFromPreferences(preferences.getStatusPreferences());
    }

    private void applyOptions(BinEdApplyOptions applyOptions) {
        applyOptions(applyOptions, diffPanel.getLeftCodeArea());
        applyOptions(applyOptions, diffPanel.getRightCodeArea());
    }

    private void applyOptions(BinEdApplyOptions applyOptions, SectCodeArea codeArea) {
        BinEdPluginStartupActivity.applyIntegrationOptions(applyOptions.getIntegrationOptions());
        CodeAreaOptionsImpl.applyToCodeArea(applyOptions.getCodeAreaOptions(), codeArea);

        ((CharsetCapable) codeArea).setCharset(Charset.forName(applyOptions.getEncodingOptions()
                .getSelectedEncoding()));
        encodingsHandler.setEncodings(applyOptions.getEncodingOptions().getEncodings());
        ((FontCapable) codeArea).setCodeFont(applyOptions.getFontOptions().isUseDefaultFont() ?
                defaultFont :
                applyOptions.getFontOptions().getFont(defaultFont));

        EditorOptions editorOptions = applyOptions.getEditorOptions();
        //        switchShowValuesPanel(editorOptions.isShowValuesPanel());
        if (codeArea.getCommandHandler() instanceof CodeAreaOperationCommandHandler) {
            ((CodeAreaOperationCommandHandler) codeArea.getCommandHandler()).setEnterKeyHandlingMode(editorOptions.getEnterKeyHandlingMode());
        }

        StatusOptions statusOptions = applyOptions.getStatusOptions();
        statusPanel.setStatusOptions(statusOptions);
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
}
