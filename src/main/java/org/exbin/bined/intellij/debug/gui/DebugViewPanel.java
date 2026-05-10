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
package org.exbin.bined.intellij.debug.gui;

import org.exbin.auxiliary.binary_data.BinaryData;
import org.exbin.bined.CodeAreaUtils;
import org.exbin.bined.CodeType;
import org.exbin.bined.EditMode;
import org.exbin.bined.capability.CharsetCapable;
import org.exbin.bined.highlight.swing.NonprintablesCodeAreaAssessor;
import org.exbin.bined.intellij.debug.DebugViewDataProvider;
import org.exbin.bined.intellij.gui.BinEdToolbarPanel;
import org.exbin.bined.jaguif.component.BinedComponentModule;
import org.exbin.bined.jaguif.document.BinedDocumentModule;
import org.exbin.bined.operation.swing.CodeAreaOperationCommandHandler;
import org.exbin.bined.section.layout.SectionCodeAreaLayoutProfile;
import org.exbin.bined.swing.CodeAreaSwingUtils;
import org.exbin.bined.swing.basic.color.CodeAreaColorsProfile;
import org.exbin.bined.swing.capability.ColorAssessorPainterCapable;
import org.exbin.bined.swing.capability.FontCapable;
import org.exbin.bined.swing.section.SectCodeArea;
import org.exbin.bined.swing.section.theme.SectionCodeAreaThemeProfile;
import org.exbin.jaguif.App;
import org.exbin.jaguif.action.api.ActionConsts;
import org.exbin.jaguif.action.api.ActionContextChange;
import org.exbin.jaguif.action.api.ActionContextRegistration;
import org.exbin.jaguif.action.api.ActionModuleApi;
import org.exbin.jaguif.context.api.ContextComponent;
import org.exbin.jaguif.action.api.DialogParentComponent;
import org.exbin.jaguif.action.api.clipboard.ClipboardController;
import org.exbin.bined.jaguif.component.BinEdDataComponent;
import org.exbin.bined.jaguif.document.BinEdFileManager;
import org.exbin.bined.jaguif.editor.settings.BinaryEditorOptions;
import org.exbin.bined.jaguif.component.gui.BinEdComponentPanel;
import org.exbin.bined.jaguif.viewer.settings.CodeAreaStatusOptions;
import org.exbin.bined.jaguif.theme.settings.CodeAreaColorOptions;
import org.exbin.bined.jaguif.theme.settings.CodeAreaLayoutOptions;
import org.exbin.bined.jaguif.theme.settings.CodeAreaThemeOptions;
import org.exbin.bined.jaguif.viewer.BinedViewerModule;
import org.exbin.bined.jaguif.viewer.settings.BinaryEncodingSettingsApplier;
import org.exbin.bined.jaguif.viewer.settings.CodeAreaOptions;
import org.exbin.bined.jaguif.viewer.settings.CodeAreaViewerSettingsApplier;
import org.exbin.jaguif.context.ActiveContextManager;
import org.exbin.jaguif.context.api.ActiveContextManagement;
import org.exbin.jaguif.context.api.ContextChangeRegistration;
import org.exbin.jaguif.context.api.ContextModuleApi;
import org.exbin.jaguif.context.api.ContextRegistration;
import org.exbin.jaguif.context.api.ContextUpdateManagement;
import org.exbin.jaguif.context.api.StateUpdateType;
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
import org.exbin.jaguif.text.font.ContextFont;
import org.exbin.jaguif.text.font.settings.TextFontOptions;
import org.exbin.jaguif.utils.DesktopUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JViewport;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel to show debug view.
 */
@ParametersAreNonnullByDefault
public class DebugViewPanel extends javax.swing.JPanel {

    protected final List<DebugViewDataProvider> providers = new ArrayList<>();
    protected int selectedProvider = 0;

    private final Font defaultFont;
    private final SectionCodeAreaLayoutProfile defaultLayoutProfile;
    private final SectionCodeAreaThemeProfile defaultThemeProfile;
    private final CodeAreaColorsProfile defaultColorProfile;

    protected final JPanel panel;
    protected BinEdToolbarPanel toolbarPanel = new BinEdToolbarPanel();
    protected StatusBar statusBar;
    protected final BinEdDataComponent dataComponent;

    public DebugViewPanel() {
        panel = new JPanel(new BorderLayout());
        dataComponent = new BinEdDataComponent(new BinEdComponentPanel());

        SectCodeArea codeArea = (SectCodeArea) dataComponent.getCodeArea();
        defaultFont = new Font(Font.MONOSPACED, Font.PLAIN, 12);
        defaultLayoutProfile = codeArea.getLayoutProfile();
        defaultThemeProfile = codeArea.getThemeProfile();
        defaultColorProfile = codeArea.getColorsProfile();

        initComponents();
        init();
    }

    private void init() {
        BinedDocumentModule binedDocumentModule = App.getModule(BinedDocumentModule.class);
        BinEdFileManager fileManager = binedDocumentModule.getFileManager();
        fileManager.initDataComponent(dataComponent);

        BinEdComponentPanel componentPanel = (BinEdComponentPanel) dataComponent.getComponent();
        SectCodeArea codeArea = componentPanel.getCodeArea();
        codeArea.setEditMode(EditMode.READ_ONLY);

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
                NonprintablesCodeAreaAssessor nonprintablesCodeAreaAssessor =
                        CodeAreaSwingUtils.findColorAssessor(painter, NonprintablesCodeAreaAssessor.class);
                return CodeAreaUtils.requireNonNull(nonprintablesCodeAreaAssessor).isShowNonprintables();
            }

            @Override
            public void setShowNonprintables(boolean showNonprintables) {
                ColorAssessorPainterCapable painter = (ColorAssessorPainterCapable) codeArea.getPainter();
                NonprintablesCodeAreaAssessor nonprintablesCodeAreaAssessor =
                        CodeAreaSwingUtils.findColorAssessor(painter, NonprintablesCodeAreaAssessor.class);
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
        BinedViewerModule binedViewerModule = App.getModule(BinedViewerModule.class);

        AbstractAction wrapperAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                settingsAction.actionPerformed(e);
                toolbarPanel.applyFromCodeArea();
            }
        };
        toolbarPanel.setOptionsAction(wrapperAction);

        BinedComponentModule binedComponentModule = App.getModule(BinedComponentModule.class);
        JPopupMenu codeAreaPopupMenu = binedComponentModule.createCodeAreaPopupMenu();
        codeArea.setComponentPopupMenu(codeAreaPopupMenu);

        EncodingsManager encodingsManager = new EncodingsManager();
        encodingsManager.init();

//        OptionsModuleApi optionsModule = App.getModule(OptionsModuleApi.class);
//        binaryStatusPanel.loadFromOptions(new CodeAreaStatusOptions(optionsModule.getAppOptions()));
        // statusPanel.setMinimumSize(new Dimension(0, getMinimumSize().height));
//        binaryStatus.attachCodeArea(dataComponent);
        StatusBarModuleApi statusBarModule = App.getModule(StatusBarModuleApi.class);

        // TODO Temporary workaround for unfinished rework of actions
        {
            ContextModuleApi contextModule = App.getModule(ContextModuleApi.class);
            ActiveContextManagement contextManagement = new ActiveContextManager();
            ContextUpdateManagement updateManagement = contextModule.createContextUpdateManagement(contextManagement);
            ContextRegistration contextRegistrar = contextModule.createContextRegistrator("", updateManagement, contextManagement);
            contextManagement.changeActiveState(ContextComponent.class, dataComponent);
            contextManagement.changeActiveState(ContextEncoding.class, dataComponent);
            statusBar = statusBarModule.createStatusBar(BinedComponentModule.BINARY_STATUS_BAR_ID, contextRegistrar);
            /* ActionManagement actionManager = actionModule.createActionManager(contextManagement);

            Action action = new AbstractAction() {
                public void actionPerformed(ActionEvent ae) {
                    // ignore
                }
            };
            action.putValue(ActionConsts.ACTION_CONTEXT_CHANGE, (ActionContextChange) (ContextChangeRegistration registrar) -> {
                registrar.registerStateChangeListener(ContextEncoding.class, (ContextEncoding instance, StateUpdateType changeType) -> {
                    if (CharsetEncodingState.ChangeType.ENCODING.equals(changeType)) {
                        statusBar.updateEncodingState();
                    }
                });
            });
            actionContextRegistrar.registerActionContext(action);
            actionContextRegistrar.registerActionContext(encodingsManager.getToolsEncodingMenu().getAction());
            actionContextRegistrar.registerActionContext(encodingsManager.getManageEncodingsAction()); */
            dataComponent.setContextManager(contextManagement);

            BinaryEncodingSettingsApplier settingsApplier = new BinaryEncodingSettingsApplier();
            settingsApplier.applySettings(
                    contextManagement,
                    optionsSettingsModule.getMainSettingsManager().getSettingsOptionsProvider());
        }

        initialLoadFromPreferences();

        panel.add(toolbarPanel, BorderLayout.NORTH);
        panel.add(statusBar.getComponent(), BorderLayout.SOUTH);
        panel.add(dataComponent.getComponent(), BorderLayout.CENTER);
        panel.revalidate();
        panel.repaint();

        this.add(panel, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private void initialLoadFromPreferences() {
        OptionsModuleApi optionsModule = App.getModule(OptionsModuleApi.class);
        OptionsStorage preferences = optionsModule.getAppOptions();
        SectCodeArea codeArea = (SectCodeArea) dataComponent.getCodeArea();

        applyOptions(preferences, codeArea);

//        BinaryStatusPanel statusPanel = statusBar.getBinaryStatusPanel();
//        CodeAreaStatusOptions statusOptions = new CodeAreaStatusOptions(preferences);
//        statusPanel.loadFromOptions(statusOptions);
        toolbarPanel.applyFromCodeArea();
        toolbarPanel.loadFromOptions(preferences);

//        BinaryStatusApi.MemoryMode memoryMode = BinaryStatusApi.MemoryMode.READ_ONLY;
//        statusPanel.setMemoryMode(memoryMode);
    }

    private void applyOptions(OptionsStorage optionsStorage, SectCodeArea codeArea) {
        CodeAreaViewerSettingsApplier.applyToCodeArea(new CodeAreaOptions(optionsStorage), codeArea);

        TextEncodingOptions encodingOptions = new TextEncodingOptions(optionsStorage);
        ((CharsetCapable) codeArea).setCharset(Charset.forName(encodingOptions
                .getSelectedEncoding()));
        // TODO encodingsManager.setEncodings(optionsSettings.getEncodingOptions().getEncodings());
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

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">
    private void initComponents() {

        providerComboBox = new javax.swing.JComboBox<>();

        setLayout(new java.awt.BorderLayout());

        providerComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                providerComboBoxItemStateChanged(evt);
            }
        });
    }// </editor-fold>

    private void providerComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {
        int selectedIndex = providerComboBox.getSelectedIndex();
        if (selectedProvider != selectedIndex) {
            selectedProvider = selectedIndex;
            setContentData(providers.get(selectedProvider).getData());
        }
    }

    // Variables declaration - do not modify
    private javax.swing.JComboBox<String> providerComboBox;
    // End of variables declaration

    public void addProvider(DebugViewDataProvider provider) {
        if (providers.isEmpty()) {
            setContentData(provider.getData());
            add(providerComboBox, java.awt.BorderLayout.PAGE_START);
        }

        providers.add(provider);
        providerComboBox.addItem(provider.getName());
    }

    public void setContentData(@Nullable BinaryData data) {
        dataComponent.getCodeArea().setContentData(data);
        long dataSize = data == null ? 0 : data.getDataSize();
        // TODO statusBar.getBinaryStatusPanel().setCurrentDocumentSize(dataSize, dataSize);
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
