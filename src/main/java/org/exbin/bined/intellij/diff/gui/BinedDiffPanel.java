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
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.components.JBPanel;
import org.exbin.auxiliary.paged_data.BinaryData;
import org.exbin.auxiliary.paged_data.ByteArrayData;
import org.exbin.bined.CodeAreaCaretPosition;
import org.exbin.bined.CodeType;
import org.exbin.bined.EditOperation;
import org.exbin.bined.capability.CharsetCapable;
import org.exbin.bined.extended.layout.ExtendedCodeAreaLayoutProfile;
import org.exbin.bined.intellij.BinEdApplyOptions;
import org.exbin.bined.intellij.BinEdFileDataWrapper;
import org.exbin.bined.intellij.BinEdIntelliJPlugin;
import org.exbin.bined.intellij.BinEdPluginStartupActivity;
import org.exbin.bined.intellij.IntelliJPreferencesWrapper;
import org.exbin.bined.intellij.action.SearchAction;
import org.exbin.bined.intellij.gui.BinEdComponentPanel;
import org.exbin.bined.intellij.gui.BinEdOptionsPanel;
import org.exbin.bined.intellij.gui.BinEdOptionsPanelBorder;
import org.exbin.bined.intellij.gui.BinEdToolbarPanel;
import org.exbin.bined.intellij.options.IntegrationOptions;
import org.exbin.bined.operation.swing.CodeAreaOperationCommandHandler;
import org.exbin.bined.swing.basic.color.CodeAreaColorsProfile;
import org.exbin.bined.swing.capability.FontCapable;
import org.exbin.bined.swing.extended.ExtCodeArea;
import org.exbin.bined.swing.extended.diff.ExtCodeAreaDiffPanel;
import org.exbin.bined.swing.extended.theme.ExtendedCodeAreaThemeProfile;
import org.exbin.framework.bined.BinaryStatusApi;
import org.exbin.framework.bined.gui.BinaryStatusPanel;
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
import org.exbin.framework.editor.text.service.TextFontService;
import org.exbin.framework.utils.DesktopUtils;
import org.exbin.framework.utils.WindowUtils;
import org.exbin.framework.utils.gui.OptionsControlPanel;
import org.exbin.framework.utils.handler.OptionsControlHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseEvent;
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

    private static final String ONLINE_HELP_URL = "https://bined.exbin.org/intellij-plugin/?manual";

    private final BinaryEditorPreferences preferences;
    private final ExtCodeAreaDiffPanel diffPanel = new ExtCodeAreaDiffPanel();

    private final Font defaultFont;
    private final ExtendedCodeAreaLayoutProfile defaultLayoutProfile;
    private final ExtendedCodeAreaThemeProfile defaultThemeProfile;
    private final CodeAreaColorsProfile defaultColorProfile;

    private final BinEdToolbarPanel toolbarPanel;
    private final BinaryStatusPanel statusPanel;
    private EncodingsHandler encodingsHandler;
    private BinaryStatusApi binaryStatus;
    private TextEncodingStatusApi encodingStatus;
    private BinEdComponentPanel.CharsetChangeListener charsetChangeListener = null;

    public BinedDiffPanel() {
        setLayout(new java.awt.BorderLayout());

        preferences = new BinaryEditorPreferences(new IntelliJPreferencesWrapper(getPreferences(),
                BinEdIntelliJPlugin.PLUGIN_PREFIX));
        defaultFont = new Font(Font.MONOSPACED, Font.PLAIN, 12);
        ExtCodeArea leftCodeArea = diffPanel.getLeftCodeArea();
        ExtCodeArea rightCodeArea = diffPanel.getRightCodeArea();
        defaultLayoutProfile = leftCodeArea.getLayoutProfile();
        defaultThemeProfile = leftCodeArea.getThemeProfile();
        defaultColorProfile = leftCodeArea.getColorsProfile();
        toolbarPanel = new BinEdToolbarPanel(preferences, diffPanel,
                new BinEdToolbarPanel.Control() {
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
                    public boolean isShowUnprintables() {
                        return leftCodeArea.isShowUnprintables();
                    }

                    @Override
                    public void setShowUnprintables(boolean showUnprintables) {
                        leftCodeArea.setShowUnprintables(showUnprintables);
                        rightCodeArea.setShowUnprintables(showUnprintables);
                    }

                    @Override
                    public void repaint() {
                        diffPanel.repaint();
                    }
                },
                new AnAction() {
                    @Override
                    public void actionPerformed(@Nonnull AnActionEvent anActionEvent) {
                        createOptionsAction().actionPerformed(new ActionEvent(BinedDiffPanel.this, 0, "COMMAND", 0));
                    }
                },
                new AnAction() {
                    @Override
                    public void actionPerformed(@Nonnull AnActionEvent anActionEvent) {
                        createOnlineHelpAction().actionPerformed(new ActionEvent(BinedDiffPanel.this, 0, "COMMAND", 0));
                    }
                }
        );
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
                charsetChangeListener.charsetChanged();
            }
        });

        registerBinaryStatus(statusPanel);

        initialLoadFromPreferences();

        this.add(statusPanel, BorderLayout.SOUTH);
        this.add(diffPanel, BorderLayout.CENTER);
    }

    public void setDiffContent(ContentDiffRequest request) {
        List<DiffContent> contents = request.getContents();
        if (!contents.isEmpty()) {
            BinaryData leftData = getDiffBinaryData(request, 0);
            if (leftData == null) {
                return;
            }
            diffPanel.setLeftContentData(leftData);
            ExtCodeArea leftCodeArea = diffPanel.getLeftCodeArea();
            leftCodeArea.setComponentPopupMenu(new JPopupMenu() {
                @Override
                public void show(Component invoker, int x, int y) {
                    JPopupMenu popupMenu = SearchAction.createCodeAreaPopupMenu(leftCodeArea, "left");
                    popupMenu.show(invoker, x, y);
                }
            });
            BinaryData rightData = getDiffBinaryData(request, 1);
            if (rightData != null) {
                diffPanel.setRightContentData(rightData);
            }
            ExtCodeArea rightCodeArea = diffPanel.getRightCodeArea();
            rightCodeArea.setComponentPopupMenu(new JPopupMenu() {
                @Override
                public void show(Component invoker, int x, int y) {
                    JPopupMenu popupMenu = SearchAction.createCodeAreaPopupMenu(rightCodeArea, "right");
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
                return new BinEdFileDataWrapper(((FileContent) diffContent).getFile());
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
        ExtCodeArea leftCodeArea = diffPanel.getLeftCodeArea();
        ExtCodeArea rightCodeArea = diffPanel.getRightCodeArea();
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
                // TODO goToPositionAction.actionPerformed(new ActionEvent(BinedDiffPanel.this, 0, ""));
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

    private void updateBinaryStatus(ExtCodeArea codeArea) {
        binaryStatus.setEditMode(codeArea.getEditMode(), codeArea.getActiveOperation());
        binaryStatus.setCursorPosition(codeArea.getCaretPosition());
        binaryStatus.setSelectionRange(codeArea.getSelection());
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
                return preferences.getIntegrationPreferences();
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
                return preferences.getDataInspectorPreferences();
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
        toolbarPanel.loadFromPreferences();

        updateCurrentMemoryMode();
    }

    private void updateCurrentMemoryMode() {
        BinaryStatusApi.MemoryMode memoryMode = BinaryStatusApi.MemoryMode.READ_ONLY;

        if (binaryStatus != null) {
            binaryStatus.setMemoryMode(memoryMode);
        }
    }

    @Nonnull
    private AbstractAction createOptionsAction() {
        return new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final BinEdOptionsPanelBorder optionsPanelWrapper = new BinEdOptionsPanelBorder();
                optionsPanelWrapper.setPreferredSize(new Dimension(700, 460));
                BinEdOptionsPanel optionsPanel = optionsPanelWrapper.getOptionsPanel();
                optionsPanel.setPreferences(preferences);
                optionsPanel.setTextFontService(new TextFontService() {
                    @Nonnull
                    @Override
                    public Font getCurrentFont() {
                        return diffPanel.getLeftCodeArea().getCodeFont();
                    }

                    @Nonnull
                    @Override
                    public Font getDefaultFont() {
                        return defaultFont;
                    }

                    @Override
                    public void setCurrentFont(Font font) {
                        diffPanel.getLeftCodeArea().setCodeFont(font);
                        diffPanel.getRightCodeArea().setCodeFont(font);
                    }
                });
                optionsPanel.loadFromPreferences();
                updateApplyOptions(optionsPanel);
                OptionsControlPanel optionsControlPanel = new OptionsControlPanel();
                JPanel dialogPanel = WindowUtils.createDialogPanel(optionsPanelWrapper, optionsControlPanel);
                WindowUtils.DialogWrapper dialog = WindowUtils.createDialog(dialogPanel,
                        (Component) e.getSource(),
                        "Options",
                        Dialog.ModalityType.APPLICATION_MODAL);
                optionsControlPanel.setHandler((OptionsControlHandler.ControlActionType actionType) -> {
                    if (actionType != OptionsControlHandler.ControlActionType.CANCEL) {
                        optionsPanel.applyToOptions();
                        if (actionType == OptionsControlHandler.ControlActionType.SAVE) {
                            optionsPanel.saveToPreferences();
                        }
                        applyOptions(optionsPanel);
                        diffPanel.getLeftCodeArea().repaint();
                        diffPanel.getRightCodeArea().repaint();
                    }

                    dialog.close();
                });
                dialog.showCentered((Component) e.getSource());
                dialog.dispose();
            }
        };
    }

    public void registerEncodingStatus(TextEncodingStatusApi encodingStatusApi) {
        this.encodingStatus = encodingStatusApi;
        setCharsetChangeListener(() -> {
            String selectedEncoding = diffPanel.getLeftCodeArea().getCharset().name();
            encodingStatus.setEncoding(selectedEncoding);
        });
    }

    public void setCharsetChangeListener(BinEdComponentPanel.CharsetChangeListener charsetChangeListener) {
        this.charsetChangeListener = charsetChangeListener;
    }

    private void updateApplyOptions(BinEdApplyOptions applyOptions) {
        ExtCodeArea leftCodeArea = diffPanel.getLeftCodeArea();
        ExtCodeArea rightCodeArea = diffPanel.getRightCodeArea();
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

    private void applyOptions(BinEdApplyOptions applyOptions, ExtCodeArea codeArea) {
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
                DesktopUtils.openDesktopURL(ONLINE_HELP_URL);
            }
        };
    }

    private Icon load(String path) {
        return IconLoader.getIcon(path, getClass());
    }
}
