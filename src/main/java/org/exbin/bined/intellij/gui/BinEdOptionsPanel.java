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

import org.exbin.bined.extended.theme.ExtendedBackgroundPaintMode;
import org.exbin.bined.intellij.main.BinEdApplyOptions;
import org.exbin.bined.intellij.options.IntegrationOptions;
import org.exbin.bined.intellij.options.gui.IntegrationOptionsPanel;
import org.exbin.bined.intellij.options.impl.IntegrationOptionsImpl;
import org.exbin.bined.swing.extended.color.ExtendedCodeAreaColorProfile;
import org.exbin.bined.swing.extended.layout.DefaultExtendedCodeAreaLayoutProfile;
import org.exbin.bined.swing.extended.theme.ExtendedCodeAreaThemeProfile;
import org.exbin.framework.bined.inspector.options.DataInspectorOptions;
import org.exbin.framework.bined.inspector.options.gui.DataInspectorOptionsPanel;
import org.exbin.framework.bined.inspector.options.impl.DataInspectorOptionsImpl;
import org.exbin.framework.bined.options.CodeAreaColorOptions;
import org.exbin.framework.bined.options.CodeAreaLayoutOptions;
import org.exbin.framework.bined.options.CodeAreaOptions;
import org.exbin.framework.bined.options.CodeAreaThemeOptions;
import org.exbin.framework.bined.options.EditorOptions;
import org.exbin.framework.bined.options.StatusOptions;
import org.exbin.framework.bined.options.gui.CodeAreaOptionsPanel;
import org.exbin.framework.bined.options.gui.ColorProfilePanel;
import org.exbin.framework.bined.options.gui.ColorProfilesPanel;
import org.exbin.framework.bined.options.gui.ColorTemplatePanel;
import org.exbin.framework.bined.options.gui.EditorOptionsPanel;
import org.exbin.framework.bined.options.gui.LayoutProfilePanel;
import org.exbin.framework.bined.options.gui.LayoutProfilesPanel;
import org.exbin.framework.bined.options.gui.LayoutTemplatePanel;
import org.exbin.framework.bined.options.gui.NamedProfilePanel;
import org.exbin.framework.bined.options.gui.ProfileSelectionPanel;
import org.exbin.framework.bined.options.gui.StatusOptionsPanel;
import org.exbin.framework.bined.options.gui.ThemeProfilePanel;
import org.exbin.framework.bined.options.gui.ThemeProfilesPanel;
import org.exbin.framework.bined.options.gui.ThemeTemplatePanel;
import org.exbin.framework.bined.options.impl.CodeAreaColorOptionsImpl;
import org.exbin.framework.bined.options.impl.CodeAreaLayoutOptionsImpl;
import org.exbin.framework.bined.options.impl.CodeAreaOptionsImpl;
import org.exbin.framework.bined.options.impl.CodeAreaThemeOptionsImpl;
import org.exbin.framework.bined.options.impl.EditorOptionsImpl;
import org.exbin.framework.bined.options.impl.StatusOptionsImpl;
import org.exbin.framework.bined.preferences.BinaryEditorPreferences;
import org.exbin.framework.editor.text.gui.AddEncodingPanel;
import org.exbin.framework.editor.text.gui.TextFontPanel;
import org.exbin.framework.editor.text.options.TextFontOptions;
import org.exbin.framework.editor.text.options.gui.TextEncodingOptionsPanel;
import org.exbin.framework.editor.text.options.gui.TextFontOptionsPanel;
import org.exbin.framework.editor.text.options.impl.TextEncodingOptionsImpl;
import org.exbin.framework.editor.text.options.impl.TextFontOptionsImpl;
import org.exbin.framework.editor.text.service.TextFontService;
import org.exbin.framework.options.model.LanguageRecord;
import org.exbin.framework.utils.LanguageUtils;
import org.exbin.framework.utils.WindowUtils;
import org.exbin.framework.utils.WindowUtils.DialogWrapper;
import org.exbin.framework.utils.gui.DefaultControlPanel;
import org.exbin.framework.utils.handler.DefaultControlHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Binary editor options panel.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class BinEdOptionsPanel extends javax.swing.JPanel implements BinEdApplyOptions {

    private BinaryEditorPreferences preferences;
//    private final org.exbin.bined.intellij.panel.BinEdOptionsPanelController controller;
    private final java.util.ResourceBundle resourceBundle = LanguageUtils.getResourceBundleByBundleName("org/exbin/framework/options/resources/MainOptionsManager");
    private final java.util.ResourceBundle managerResourceBundle = LanguageUtils.getResourceBundleByBundleName("org/exbin/framework/bined/resources/BinedOptionsManager");

    private DefaultListModel<CategoryItem> categoryModel = new DefaultListModel<>();
    private JPanel currentCategoryPanel = null;

    private final IntegrationOptionsImpl integrationOptions = new IntegrationOptionsImpl();
    private final EditorOptionsImpl editorOptions = new EditorOptionsImpl();
    private final StatusOptionsImpl statusOptions = new StatusOptionsImpl();
    private final TextEncodingOptionsImpl encodingOptions = new TextEncodingOptionsImpl();
    private final TextFontOptionsImpl fontOptions = new TextFontOptionsImpl();
    private final CodeAreaOptionsImpl codeAreaOptions = new CodeAreaOptionsImpl();
    private final DataInspectorOptionsImpl dataInspectorOptions = new DataInspectorOptionsImpl();
    private final CodeAreaLayoutOptionsImpl layoutOptions = new CodeAreaLayoutOptionsImpl();
    private final CodeAreaColorOptionsImpl colorOptions = new CodeAreaColorOptionsImpl();
    private final CodeAreaThemeOptionsImpl themeOptions = new CodeAreaThemeOptionsImpl();

    private final IntegrationOptionsPanel integrationOptionsPanel = new IntegrationOptionsPanel();
    private final EditorOptionsPanel editorOptionsPanel = new EditorOptionsPanel();
    private final StatusOptionsPanel statusOptionsPanel = new StatusOptionsPanel();
    private final CodeAreaOptionsPanel codeAreaOptionsPanel = new CodeAreaOptionsPanel();
    private final TextEncodingOptionsPanel encodingOptionsPanel = new TextEncodingOptionsPanel();
    private final TextFontOptionsPanel fontOptionsPanel = new TextFontOptionsPanel();
    private final DataInspectorOptionsPanel dataInspectorOptionsPanel = new DataInspectorOptionsPanel();
    private final LayoutProfilesPanel layoutProfilesPanel = new LayoutProfilesPanel();
    private final ProfileSelectionPanel layoutSelectionPanel = new ProfileSelectionPanel(layoutProfilesPanel);
    private final ThemeProfilesPanel themeProfilesPanel = new ThemeProfilesPanel();
    private final ProfileSelectionPanel themeSelectionPanel = new ProfileSelectionPanel(themeProfilesPanel);
    private final ColorProfilesPanel colorProfilesPanel = new ColorProfilesPanel();
    private final ProfileSelectionPanel colorSelectionPanel = new ProfileSelectionPanel(colorProfilesPanel);

    public BinEdOptionsPanel() {
        initComponents();

        integrationOptionsPanel.setDefaultLocaleName("<" + resourceBundle.getString("locale.defaultLanguage") + ">");
        List<LanguageRecord> languageLocales = new ArrayList<>();
        languageLocales.add(new LanguageRecord(Locale.ROOT, null));
        languageLocales.add(new LanguageRecord(new Locale("en", "US"), new ImageIcon(getClass().getResource(
                resourceBundle.getString("locale.englishFlag")))));
        languageLocales.addAll(LanguageUtils.getLanguageRecords());
        integrationOptionsPanel.setLanguageLocales(languageLocales);
        List<String> fileHandlingModes = new ArrayList<>();
        fileHandlingModes.add(managerResourceBundle.getString("fileHandlingMode.memory"));
        fileHandlingModes.add(managerResourceBundle.getString("fileHandlingMode.delta"));
        editorOptionsPanel.setFileHandlingModes(fileHandlingModes);
        List<String> enderKeyHandlingModes = new ArrayList<>();
        enderKeyHandlingModes.add(managerResourceBundle.getString("enterKeyHandlingMode.platformSpecific"));
        enderKeyHandlingModes.add(managerResourceBundle.getString("enterKeyHandlingMode.cr"));
        enderKeyHandlingModes.add(managerResourceBundle.getString("enterKeyHandlingMode.lf"));
        enderKeyHandlingModes.add(managerResourceBundle.getString("enterKeyHandlingMode.crlf"));
        enderKeyHandlingModes.add(managerResourceBundle.getString("enterKeyHandlingMode.ignore"));
        editorOptionsPanel.setEnterKeyHandlingModes(enderKeyHandlingModes);
        List<String> tabKeyHandlingModes = new ArrayList<>();
        tabKeyHandlingModes.add(managerResourceBundle.getString("tabKeyHandlingMode.platformSpecific"));
        tabKeyHandlingModes.add(managerResourceBundle.getString("tabKeyHandlingMode.insertTab"));
        tabKeyHandlingModes.add(managerResourceBundle.getString("tabKeyHandlingMode.insertSpaces"));
        tabKeyHandlingModes.add(managerResourceBundle.getString("tabKeyHandlingMode.cycleToNextSection"));
        tabKeyHandlingModes.add(managerResourceBundle.getString("tabKeyHandlingMode.cycleToPreviousSection"));
        tabKeyHandlingModes.add(managerResourceBundle.getString("tabKeyHandlingMode.ignore"));
        editorOptionsPanel.setTabKeyHandlingModes(tabKeyHandlingModes);

        List<String> viewModes = new ArrayList<>();
        viewModes.add(managerResourceBundle.getString("codeAreaViewMode.dual"));
        viewModes.add(managerResourceBundle.getString("codeAreaViewMode.codeMatrix"));
        viewModes.add(managerResourceBundle.getString("codeAreaViewMode.textPreview"));
        codeAreaOptionsPanel.setViewModes(viewModes);

        List<String> codeTypes = new ArrayList<>();
        codeTypes.add(managerResourceBundle.getString("codeAreaCodeType.binary"));
        codeTypes.add(managerResourceBundle.getString("codeAreaCodeType.octal"));
        codeTypes.add(managerResourceBundle.getString("codeAreaCodeType.decimal"));
        codeTypes.add(managerResourceBundle.getString("codeAreaCodeType.hexadecimal"));
        codeAreaOptionsPanel.setCodeTypes(codeTypes);

        List<String> positionCodeTypes = new ArrayList<>();
        positionCodeTypes.add(managerResourceBundle.getString("positionCodeAreaCodeType.octal"));
        positionCodeTypes.add(managerResourceBundle.getString("positionCodeAreaCodeType.decimal"));
        positionCodeTypes.add(managerResourceBundle.getString("positionCodeAreaCodeType.hexadecimal"));
        codeAreaOptionsPanel.setPositionCodeTypes(positionCodeTypes);

        List<String> charactersCases = new ArrayList<>();
        charactersCases.add(managerResourceBundle.getString("codeAreaCharactersCase.lower"));
        charactersCases.add(managerResourceBundle.getString("codeAreaCharactersCase.higher"));
        codeAreaOptionsPanel.setCharactersCases(charactersCases);

        List<String> cursorPositionCodeTypes = new ArrayList<>();
        cursorPositionCodeTypes.add(managerResourceBundle.getString("cursorPositionCodeType.octal"));
        cursorPositionCodeTypes.add(managerResourceBundle.getString("cursorPositionCodeType.decimal"));
        cursorPositionCodeTypes.add(managerResourceBundle.getString("cursorPositionCodeType.hexadecimal"));
        statusOptionsPanel.setCursorPositionCodeTypes(cursorPositionCodeTypes);

        List<String> documentSizeCodeTypes = new ArrayList<>();
        documentSizeCodeTypes.add(managerResourceBundle.getString("documentSizeCodeType.octal"));
        documentSizeCodeTypes.add(managerResourceBundle.getString("documentSizeCodeType.decimal"));
        documentSizeCodeTypes.add(managerResourceBundle.getString("documentSizeCodeType.hexadecimal"));
        statusOptionsPanel.setDocumentSizeCodeTypes(documentSizeCodeTypes);

        categoryModel.addElement(new CategoryItem(integrationOptionsPanel.getResourceBundle().getString("options.caption"), integrationOptionsPanel));
        categoryModel.addElement(new CategoryItem(editorOptionsPanel.getResourceBundle().getString("options.caption"), editorOptionsPanel));
        categoryModel.addElement(new CategoryItem(statusOptionsPanel.getResourceBundle().getString("options.caption"), statusOptionsPanel));
        categoryModel.addElement(new CategoryItem(codeAreaOptionsPanel.getResourceBundle().getString("options.caption"), codeAreaOptionsPanel));
        categoryModel.addElement(new CategoryItem(fontOptionsPanel.getResourceBundle().getString("options.caption"), fontOptionsPanel));
        categoryModel.addElement(new CategoryItem(encodingOptionsPanel.getResourceBundle().getString("options.caption"), encodingOptionsPanel));
        categoryModel.addElement(new CategoryItem(LanguageUtils.getResourceBundleByBundleName("org/exbin/framework/bined/options/gui/resources/LayoutProfilesOptionsPanel").getString("options.caption"), layoutSelectionPanel));
        categoryModel.addElement(new CategoryItem(LanguageUtils.getResourceBundleByBundleName("org/exbin/framework/bined/options/gui/resources/ThemeProfilesOptionsPanel").getString("options.caption"), themeSelectionPanel));
        categoryModel.addElement(new CategoryItem(LanguageUtils.getResourceBundleByBundleName("org/exbin/framework/bined/options/gui/resources/ColorProfilesOptionsPanel").getString("options.caption"), colorSelectionPanel));
        categoryModel.addElement(new CategoryItem(dataInspectorOptionsPanel.getResourceBundle().getString("options.caption"), dataInspectorOptionsPanel));
        categoriesList.setModel(categoryModel);

        categoriesList.addListSelectionListener((ListSelectionEvent e) -> {
            int selectedIndex = categoriesList.getSelectedIndex();
            if (selectedIndex >= 0) {
                CategoryItem categoryItem = categoryModel.get(selectedIndex);
                currentCategoryPanel = categoryItem.getCategoryPanel();
                mainPane.setViewportView(currentCategoryPanel);
                mainPane.invalidate();
                revalidate();
                mainPane.repaint();
            }
        });
        categoriesList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component component = super.getListCellRendererComponent(list, ((CategoryItem) value).categoryName, index, isSelected, cellHasFocus);
                return component;
            }
        });
        categoriesList.setSelectedIndex(0);

        encodingOptionsPanel.setAddEncodingsOperation((List<String> usedEncodings) -> {
            final List<String> result = new ArrayList<>();
            final AddEncodingPanel addEncodingPanel = new AddEncodingPanel();
            addEncodingPanel.setUsedEncodings(usedEncodings);
            DefaultControlPanel encodingsControlPanel = new DefaultControlPanel(addEncodingPanel.getResourceBundle());
            JPanel dialogPanel = WindowUtils.createDialogPanel(addEncodingPanel, encodingsControlPanel);
            final DialogWrapper addEncodingDialog = WindowUtils.createDialog(dialogPanel, this, addEncodingPanel.getResourceBundle().getString("dialog.title"), Dialog.ModalityType.APPLICATION_MODAL);
            encodingsControlPanel.setHandler((DefaultControlHandler.ControlActionType actionType) -> {
                if (actionType == DefaultControlHandler.ControlActionType.OK) {
                    result.addAll(addEncodingPanel.getEncodings());
                }

                addEncodingDialog.close();
            });
            addEncodingDialog.show();
            return result;
        });

        fontOptionsPanel.setFontChangeAction(new TextFontOptionsPanel.FontChangeAction() {
            @Override
            public Font changeFont(Font currentFont) {
                final FontResult result = new FontResult();
                final TextFontPanel fontPanel = new TextFontPanel();
                fontPanel.setStoredFont(currentFont);
                DefaultControlPanel controlPanel = new DefaultControlPanel();
                JPanel dialogPanel = WindowUtils.createDialogPanel(fontPanel, controlPanel);
                final DialogWrapper dialog = WindowUtils.createDialog(dialogPanel, BinEdOptionsPanel.this, fontPanel.getResourceBundle().getString("dialog.title"), java.awt.Dialog.ModalityType.APPLICATION_MODAL);
                controlPanel.setHandler((DefaultControlHandler.ControlActionType actionType) -> {
                    if (actionType != DefaultControlHandler.ControlActionType.CANCEL) {
                        result.font = fontPanel.getStoredFont();
                    }

                    dialog.close();
                    dialog.dispose();
                });
                dialog.showCentered(BinEdOptionsPanel.this);

                return result.font;
            }

            class FontResult {

                Font font;
            }
        });

        layoutProfilesPanel.setAddProfileOperation((JComponent parentComponent, String profileName) -> {
            LayoutProfilePanel layoutProfilePanel = new LayoutProfilePanel();
            layoutProfilePanel.setLayoutProfile(new DefaultExtendedCodeAreaLayoutProfile());
            NamedProfilePanel namedProfilePanel = new NamedProfilePanel(layoutProfilePanel);
            namedProfilePanel.setProfileName(profileName);
            DefaultControlPanel controlPanel = new DefaultControlPanel();
            JPanel dialogPanel = WindowUtils.createDialogPanel(namedProfilePanel, controlPanel);

            LayoutProfileResult result = new LayoutProfileResult();
            final DialogWrapper dialog = WindowUtils.createDialog(dialogPanel, parentComponent, "Add Layout Profile", Dialog.ModalityType.APPLICATION_MODAL);
            controlPanel.setHandler((DefaultControlHandler.ControlActionType actionType) -> {
                if (actionType != DefaultControlHandler.ControlActionType.CANCEL) {
                    if (!isValidProfileName(namedProfilePanel.getProfileName())) {
                        JOptionPane.showMessageDialog(parentComponent, "Invalid profile name", "Profile Edit Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    result.profile = new LayoutProfilesPanel.LayoutProfile(
                            namedProfilePanel.getProfileName(), layoutProfilePanel.getLayoutProfile()
                    );
                }

                dialog.close();
                dialog.dispose();
            });
            dialog.showCentered(parentComponent);

            return result.profile;
        });
        layoutProfilesPanel.setEditProfileOperation((JComponent parentComponent, LayoutProfilesPanel.LayoutProfile profileRecord) -> {
            LayoutProfilePanel layoutProfilePanel = new LayoutProfilePanel();
            NamedProfilePanel namedProfilePanel = new NamedProfilePanel(layoutProfilePanel);
            DefaultControlPanel controlPanel = new DefaultControlPanel();
            JPanel dialogPanel = WindowUtils.createDialogPanel(namedProfilePanel, controlPanel);

            LayoutProfileResult result = new LayoutProfileResult();
            final DialogWrapper dialog = WindowUtils.createDialog(dialogPanel, parentComponent, "Edit Layout Profile", Dialog.ModalityType.APPLICATION_MODAL);
            namedProfilePanel.setProfileName(profileRecord.getProfileName());
            layoutProfilePanel.setLayoutProfile(profileRecord.getLayoutProfile());
            controlPanel.setHandler((DefaultControlHandler.ControlActionType actionType) -> {
                if (actionType != DefaultControlHandler.ControlActionType.CANCEL) {
                    if (!isValidProfileName(namedProfilePanel.getProfileName())) {
                        JOptionPane.showMessageDialog(parentComponent, "Invalid profile name", "Profile Edit Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    result.profile = new LayoutProfilesPanel.LayoutProfile(
                            namedProfilePanel.getProfileName(), layoutProfilePanel.getLayoutProfile()
                    );
                }

                dialog.close();
                dialog.dispose();
            });
            dialog.showCentered(parentComponent);

            return result.profile;
        });
        layoutProfilesPanel.setCopyProfileOperation((JComponent parentComponent, LayoutProfilesPanel.LayoutProfile profileRecord) -> {
            LayoutProfilePanel layoutProfilePanel = new LayoutProfilePanel();
            layoutProfilePanel.setLayoutProfile(new DefaultExtendedCodeAreaLayoutProfile());
            NamedProfilePanel namedProfilePanel = new NamedProfilePanel(layoutProfilePanel);
            DefaultControlPanel controlPanel = new DefaultControlPanel();
            JPanel dialogPanel = WindowUtils.createDialogPanel(namedProfilePanel, controlPanel);

            LayoutProfileResult result = new LayoutProfileResult();
            final DialogWrapper dialog = WindowUtils.createDialog(dialogPanel, parentComponent, "Copy Layout Profile", Dialog.ModalityType.APPLICATION_MODAL);
            namedProfilePanel.setProfileName(profileRecord.getProfileName() + " #copy");
            layoutProfilePanel.setLayoutProfile(profileRecord.getLayoutProfile());
            controlPanel.setHandler((DefaultControlHandler.ControlActionType actionType) -> {
                if (actionType != DefaultControlHandler.ControlActionType.CANCEL) {
                    if (!isValidProfileName(namedProfilePanel.getProfileName())) {
                        JOptionPane.showMessageDialog(parentComponent, "Invalid profile name", "Profile Edit Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    result.profile = new LayoutProfilesPanel.LayoutProfile(
                            namedProfilePanel.getProfileName(), layoutProfilePanel.getLayoutProfile()
                    );
                }

                dialog.close();
                dialog.dispose();
            });
            dialog.showCentered(parentComponent);

            return result.profile;
        });
        layoutProfilesPanel.setTemplateProfileOperation((JComponent parentComponent) -> {
            LayoutTemplatePanel layoutTemplatePanel = new LayoutTemplatePanel();
            NamedProfilePanel namedProfilePanel = new NamedProfilePanel(layoutTemplatePanel);
            namedProfilePanel.setProfileName("");
            layoutTemplatePanel.addListSelectionListener((e) -> {
                LayoutTemplatePanel.LayoutProfile selectedTemplate = layoutTemplatePanel.getSelectedTemplate();
                namedProfilePanel.setProfileName(selectedTemplate != null ? selectedTemplate.getProfileName() : "");
            });
            DefaultControlPanel controlPanel = new DefaultControlPanel();
            JPanel dialogPanel = WindowUtils.createDialogPanel(namedProfilePanel, controlPanel);

            LayoutProfileResult result = new LayoutProfileResult();
            final DialogWrapper dialog = WindowUtils.createDialog(dialogPanel, parentComponent, "Add Layout Template", Dialog.ModalityType.APPLICATION_MODAL);
            controlPanel.setHandler((DefaultControlHandler.ControlActionType actionType) -> {
                if (actionType != DefaultControlHandler.ControlActionType.CANCEL) {
                    if (!isValidProfileName(namedProfilePanel.getProfileName())) {
                        JOptionPane.showMessageDialog(parentComponent, "Invalid profile name", "Profile Template Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    LayoutTemplatePanel.LayoutProfile selectedTemplate = layoutTemplatePanel.getSelectedTemplate();
                    if (selectedTemplate == null) {
                        JOptionPane.showMessageDialog(parentComponent, "No template selected", "Profile Template Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    result.profile = new LayoutProfilesPanel.LayoutProfile(
                            namedProfilePanel.getProfileName(), selectedTemplate.getLayoutProfile()
                    );
                }

                dialog.close();
                dialog.dispose();
            });
            dialog.showCentered(parentComponent);
            return result.profile;
        });

        themeProfilesPanel.setAddProfileOperation((JComponent parentComponent, String profileName) -> {
            ThemeProfilePanel themeProfilePanel = createThemeProfilePanel();
            themeProfilePanel.setThemeProfile(new ExtendedCodeAreaThemeProfile());
            NamedProfilePanel namedProfilePanel = new NamedProfilePanel(themeProfilePanel);
            namedProfilePanel.setProfileName(profileName);
            DefaultControlPanel controlPanel = new DefaultControlPanel();
            JPanel dialogPanel = WindowUtils.createDialogPanel(namedProfilePanel, controlPanel);

            ThemeProfileResult result = new ThemeProfileResult();
            final DialogWrapper dialog = WindowUtils.createDialog(dialogPanel, parentComponent, "Add Theme Profile", Dialog.ModalityType.APPLICATION_MODAL);
            controlPanel.setHandler((DefaultControlHandler.ControlActionType actionType) -> {
                if (actionType != DefaultControlHandler.ControlActionType.CANCEL) {
                    if (!isValidProfileName(namedProfilePanel.getProfileName())) {
                        JOptionPane.showMessageDialog(parentComponent, "Invalid profile name", "Profile Edit Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    result.profile = new ThemeProfilesPanel.ThemeProfile(
                            namedProfilePanel.getProfileName(), themeProfilePanel.getThemeProfile()
                    );
                }

                dialog.close();
                dialog.dispose();
            });
            dialog.showCentered(parentComponent);

            return result.profile;
        });
        themeProfilesPanel.setEditProfileOperation((JComponent parentComponent, ThemeProfilesPanel.ThemeProfile profileRecord) -> {
            ThemeProfilePanel themeProfilePanel = createThemeProfilePanel();
            NamedProfilePanel namedProfilePanel = new NamedProfilePanel(themeProfilePanel);
            DefaultControlPanel controlPanel = new DefaultControlPanel();
            JPanel dialogPanel = WindowUtils.createDialogPanel(namedProfilePanel, controlPanel);

            ThemeProfileResult result = new ThemeProfileResult();
            final DialogWrapper dialog = WindowUtils.createDialog(dialogPanel, parentComponent, "Edit Theme Profile", Dialog.ModalityType.APPLICATION_MODAL);
            namedProfilePanel.setProfileName(profileRecord.getProfileName());
            themeProfilePanel.setThemeProfile(profileRecord.getThemeProfile());
            controlPanel.setHandler((DefaultControlHandler.ControlActionType actionType) -> {
                if (actionType != DefaultControlHandler.ControlActionType.CANCEL) {
                    if (!isValidProfileName(namedProfilePanel.getProfileName())) {
                        JOptionPane.showMessageDialog(parentComponent, "Invalid profile name", "Profile Edit Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    result.profile = new ThemeProfilesPanel.ThemeProfile(
                            namedProfilePanel.getProfileName(), themeProfilePanel.getThemeProfile()
                    );
                }

                dialog.close();
                dialog.dispose();
            });
            dialog.showCentered(parentComponent);

            return result.profile;
        });
        themeProfilesPanel.setCopyProfileOperation((JComponent parentComponent, ThemeProfilesPanel.ThemeProfile profileRecord) -> {
            ThemeProfilePanel themeProfilePanel = createThemeProfilePanel();
            themeProfilePanel.setThemeProfile(new ExtendedCodeAreaThemeProfile());
            NamedProfilePanel namedProfilePanel = new NamedProfilePanel(themeProfilePanel);
            DefaultControlPanel controlPanel = new DefaultControlPanel();
            JPanel dialogPanel = WindowUtils.createDialogPanel(namedProfilePanel, controlPanel);

            ThemeProfileResult result = new ThemeProfileResult();
            final DialogWrapper dialog = WindowUtils.createDialog(dialogPanel, parentComponent, "Copy Theme Profile", Dialog.ModalityType.APPLICATION_MODAL);
            namedProfilePanel.setProfileName(profileRecord.getProfileName() + " #copy");
            themeProfilePanel.setThemeProfile(profileRecord.getThemeProfile());
            controlPanel.setHandler((DefaultControlHandler.ControlActionType actionType) -> {
                if (actionType != DefaultControlHandler.ControlActionType.CANCEL) {
                    if (!isValidProfileName(namedProfilePanel.getProfileName())) {
                        JOptionPane.showMessageDialog(parentComponent, "Invalid profile name", "Profile Edit Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    result.profile = new ThemeProfilesPanel.ThemeProfile(
                            namedProfilePanel.getProfileName(), themeProfilePanel.getThemeProfile()
                    );
                }

                dialog.close();
                dialog.dispose();
            });
            dialog.showCentered(parentComponent);

            return result.profile;
        });
        themeProfilesPanel.setTemplateProfileOperation((JComponent parentComponent) -> {
            ThemeTemplatePanel themeTemplatePanel = new ThemeTemplatePanel();
            NamedProfilePanel namedProfilePanel = new NamedProfilePanel(themeTemplatePanel);
            namedProfilePanel.setProfileName("");
            themeTemplatePanel.addListSelectionListener((e) -> {
                ThemeTemplatePanel.ThemeProfile selectedTemplate = themeTemplatePanel.getSelectedTemplate();
                namedProfilePanel.setProfileName(selectedTemplate != null ? selectedTemplate.getProfileName() : "");
            });
            DefaultControlPanel controlPanel = new DefaultControlPanel();
            JPanel dialogPanel = WindowUtils.createDialogPanel(namedProfilePanel, controlPanel);

            ThemeProfileResult result = new ThemeProfileResult();
            final DialogWrapper dialog = WindowUtils.createDialog(dialogPanel, parentComponent, "Add Theme Template", Dialog.ModalityType.APPLICATION_MODAL);
            WindowUtils.addHeaderPanel(dialog.getWindow(), themeTemplatePanel.getClass(), themeTemplatePanel.getResourceBundle());
            controlPanel.setHandler((DefaultControlHandler.ControlActionType actionType) -> {
                if (actionType != DefaultControlHandler.ControlActionType.CANCEL) {
                    if (!isValidProfileName(namedProfilePanel.getProfileName())) {
                        JOptionPane.showMessageDialog(parentComponent, "Invalid profile name", "Profile Template Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    ThemeTemplatePanel.ThemeProfile selectedTemplate = themeTemplatePanel.getSelectedTemplate();
                    if (selectedTemplate == null) {
                        JOptionPane.showMessageDialog(parentComponent, "No template selected", "Profile Template Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    result.profile = new ThemeProfilesPanel.ThemeProfile(
                            namedProfilePanel.getProfileName(), selectedTemplate.getThemeProfile()
                    );
                }

                dialog.close();
                dialog.dispose();
            });
            dialog.showCentered(parentComponent);
            return result.profile;
        });

        colorProfilesPanel.setAddProfileOperation((JComponent parentComponent, String profileName) -> {
            ColorProfilePanel colorProfilePanel = new ColorProfilePanel();
            colorProfilePanel.setColorProfile(new ExtendedCodeAreaColorProfile());
            NamedProfilePanel namedProfilePanel = new NamedProfilePanel(colorProfilePanel);
            namedProfilePanel.setProfileName(profileName);
            DefaultControlPanel controlPanel = new DefaultControlPanel();
            JPanel dialogPanel = WindowUtils.createDialogPanel(namedProfilePanel, controlPanel);

            ColorProfileResult result = new ColorProfileResult();
            final DialogWrapper dialog = WindowUtils.createDialog(dialogPanel, parentComponent, "Add Colors Profile", Dialog.ModalityType.APPLICATION_MODAL);
            controlPanel.setHandler((DefaultControlHandler.ControlActionType actionType) -> {
                if (actionType != DefaultControlHandler.ControlActionType.CANCEL) {
                    if (!isValidProfileName(namedProfilePanel.getProfileName())) {
                        JOptionPane.showMessageDialog(parentComponent, "Invalid profile name", "Profile Edit Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    result.profile = new ColorProfilesPanel.ColorProfile(
                            namedProfilePanel.getProfileName(), colorProfilePanel.getColorProfile()
                    );
                }

                dialog.close();
                dialog.dispose();
            });
            dialog.showCentered(parentComponent);
            return result.profile;
        });
        colorProfilesPanel.setEditProfileOperation((JComponent parentComponent, ColorProfilesPanel.ColorProfile profileRecord) -> {
            ColorProfilePanel colorProfilePanel = new ColorProfilePanel();
            NamedProfilePanel namedProfilePanel = new NamedProfilePanel(colorProfilePanel);
            DefaultControlPanel controlPanel = new DefaultControlPanel();
            JPanel dialogPanel = WindowUtils.createDialogPanel(namedProfilePanel, controlPanel);

            ColorProfileResult result = new ColorProfileResult();
            final DialogWrapper dialog = WindowUtils.createDialog(dialogPanel, parentComponent, "Edit Colors Profile", Dialog.ModalityType.APPLICATION_MODAL);
            namedProfilePanel.setProfileName(profileRecord.getProfileName());
            colorProfilePanel.setColorProfile(profileRecord.getColorProfile());
            controlPanel.setHandler((DefaultControlHandler.ControlActionType actionType) -> {
                if (actionType != DefaultControlHandler.ControlActionType.CANCEL) {
                    if (!isValidProfileName(namedProfilePanel.getProfileName())) {
                        JOptionPane.showMessageDialog(parentComponent, "Invalid profile name", "Profile Edit Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    result.profile = new ColorProfilesPanel.ColorProfile(
                            namedProfilePanel.getProfileName(), colorProfilePanel.getColorProfile()
                    );
                }

                dialog.close();
                dialog.dispose();
            });
            dialog.showCentered(parentComponent);

            return result.profile;
        });
        colorProfilesPanel.setCopyProfileOperation((JComponent parentComponent, ColorProfilesPanel.ColorProfile profileRecord) -> {
            ColorProfilePanel colorProfilePanel = new ColorProfilePanel();
            colorProfilePanel.setColorProfile(new ExtendedCodeAreaColorProfile());
            NamedProfilePanel namedProfilePanel = new NamedProfilePanel(colorProfilePanel);
            DefaultControlPanel controlPanel = new DefaultControlPanel();
            JPanel dialogPanel = WindowUtils.createDialogPanel(namedProfilePanel, controlPanel);

            ColorProfileResult result = new ColorProfileResult();
            final DialogWrapper dialog = WindowUtils.createDialog(dialogPanel, parentComponent, "Copy Colors Profile", Dialog.ModalityType.APPLICATION_MODAL);
            namedProfilePanel.setProfileName(profileRecord.getProfileName() + " #copy");
            colorProfilePanel.setColorProfile(profileRecord.getColorProfile());
            controlPanel.setHandler((DefaultControlHandler.ControlActionType actionType) -> {
                if (actionType != DefaultControlHandler.ControlActionType.CANCEL) {
                    if (!isValidProfileName(namedProfilePanel.getProfileName())) {
                        JOptionPane.showMessageDialog(parentComponent, "Invalid profile name", "Profile Edit Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    result.profile = new ColorProfilesPanel.ColorProfile(
                            namedProfilePanel.getProfileName(), colorProfilePanel.getColorProfile()
                    );
                }

                dialog.close();
                dialog.dispose();
            });
            dialog.showCentered(parentComponent);

            return result.profile;
        });
        colorProfilesPanel.setTemplateProfileOperation((JComponent parentComponent) -> {
            ColorTemplatePanel colorTemplatePanel = new ColorTemplatePanel();
            NamedProfilePanel namedProfilePanel = new NamedProfilePanel(colorTemplatePanel);
            namedProfilePanel.setProfileName("");
            colorTemplatePanel.addListSelectionListener((e) -> {
                ColorTemplatePanel.ColorProfile selectedTemplate = colorTemplatePanel.getSelectedTemplate();
                namedProfilePanel.setProfileName(selectedTemplate != null ? selectedTemplate.getProfileName() : "");
            });
            DefaultControlPanel controlPanel = new DefaultControlPanel();
            JPanel dialogPanel = WindowUtils.createDialogPanel(namedProfilePanel, controlPanel);

            ColorProfileResult result = new ColorProfileResult();
            final DialogWrapper dialog = WindowUtils.createDialog(dialogPanel, parentComponent, "Add Colors Template", Dialog.ModalityType.APPLICATION_MODAL);
            WindowUtils.addHeaderPanel(dialog.getWindow(), colorTemplatePanel.getClass(), colorTemplatePanel.getResourceBundle());
            controlPanel.setHandler((DefaultControlHandler.ControlActionType actionType) -> {
                if (actionType != DefaultControlHandler.ControlActionType.CANCEL) {
                    if (!isValidProfileName(namedProfilePanel.getProfileName())) {
                        JOptionPane.showMessageDialog(parentComponent, "Invalid profile name", "Profile Template Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    ColorTemplatePanel.ColorProfile selectedTemplate = colorTemplatePanel.getSelectedTemplate();
                    if (selectedTemplate == null) {
                        JOptionPane.showMessageDialog(parentComponent, "No template selected", "Profile Template Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    result.profile = new ColorProfilesPanel.ColorProfile(
                            namedProfilePanel.getProfileName(), selectedTemplate.getColorProfile()
                    );
                }

                dialog.close();
                dialog.dispose();
            });
            dialog.showCentered(parentComponent);
            return result.profile;
        });
    }

    public void setPreferences(BinaryEditorPreferences preferences) {
        this.preferences = preferences;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        categoriesLabel = new javax.swing.JLabel();
        categoriesScrollPane = new javax.swing.JScrollPane();
        categoriesList = new javax.swing.JList<>();
        mainPane = new javax.swing.JScrollPane();

        categoriesLabel.setText("Categories:");

        categoriesList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        categoriesScrollPane.setViewportView(categoriesList);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(categoriesLabel)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addComponent(categoriesScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 169, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(mainPane, javax.swing.GroupLayout.DEFAULT_SIZE, 20, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(categoriesLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(categoriesScrollPane)
                    .addComponent(mainPane)))
        );
    }// </editor-fold>//GEN-END:initComponents

    public void loadFromPreferences() {
        integrationOptions.loadFromPreferences(preferences.getIntegrationPreferences());
        editorOptions.loadFromPreferences(preferences.getEditorPreferences());
        statusOptions.loadFromPreferences(preferences.getStatusPreferences());
        codeAreaOptions.loadFromPreferences(preferences.getCodeAreaPreferences());
        encodingOptions.loadFromPreferences(preferences.getEncodingPreferences());
        fontOptions.loadFromPreferences(preferences.getFontPreferences());
        layoutOptions.loadFromPreferences(preferences.getLayoutPreferences());
        colorOptions.loadFromPreferences(preferences.getColorPreferences());
        themeOptions.loadFromPreferences(preferences.getThemePreferences());

        integrationOptionsPanel.loadFromOptions(integrationOptions);
        editorOptionsPanel.loadFromOptions(editorOptions);
        statusOptionsPanel.loadFromOptions(statusOptions);
        codeAreaOptionsPanel.loadFromOptions(codeAreaOptions);
        encodingOptionsPanel.loadFromOptions(encodingOptions);
        fontOptionsPanel.loadFromOptions(fontOptions);

        layoutProfilesPanel.loadFromOptions(layoutOptions);
        layoutSelectionPanel.setDefaultProfile(layoutOptions.getSelectedProfile());
        colorProfilesPanel.loadFromOptions(colorOptions);
        colorSelectionPanel.setDefaultProfile(colorOptions.getSelectedProfile());
        themeProfilesPanel.loadFromOptions(themeOptions);
        themeSelectionPanel.setDefaultProfile(themeOptions.getSelectedProfile());
    }

    public void saveToPreferences() {
        applyToOptions();

        integrationOptions.saveToPreferences(preferences.getIntegrationPreferences());
        editorOptions.saveToPreferences(preferences.getEditorPreferences());
        statusOptions.saveToPreferences(preferences.getStatusPreferences());
        codeAreaOptions.saveToPreferences(preferences.getCodeAreaPreferences());
        encodingOptions.saveToPreferences(preferences.getEncodingPreferences());
        fontOptions.saveToPreferences(preferences.getFontPreferences());

        layoutOptions.saveToPreferences(preferences.getLayoutPreferences());
        colorOptions.saveToPreferences(preferences.getColorPreferences());
        themeOptions.saveToPreferences(preferences.getThemePreferences());
    }

    public void applyToOptions() {
        integrationOptionsPanel.saveToOptions(integrationOptions);
        editorOptionsPanel.saveToOptions(editorOptions);
        statusOptionsPanel.saveToOptions(statusOptions);
        codeAreaOptionsPanel.saveToOptions(codeAreaOptions);
        encodingOptionsPanel.saveToOptions(encodingOptions);
        fontOptionsPanel.saveToOptions(fontOptions);

        layoutProfilesPanel.saveToOptions(layoutOptions);
        layoutOptions.setSelectedProfile(layoutSelectionPanel.getDefaultProfile());
        colorProfilesPanel.saveToOptions(colorOptions);
        colorOptions.setSelectedProfile(colorSelectionPanel.getDefaultProfile());
        themeProfilesPanel.saveToOptions(themeOptions);
        themeOptions.setSelectedProfile(themeSelectionPanel.getDefaultProfile());
    }

    boolean valid() {
        // TODO check whether form is consistent and complete
        return true;
    }

    public void setTextFontService(TextFontService textFontService) {
        fontOptionsPanel.setTextFontService(textFontService);
    }

    @Nonnull
    @Override
    public CodeAreaOptions getCodeAreaOptions() {
        return codeAreaOptions;
    }

    @Nonnull
    @Override
    public TextEncodingOptionsImpl getEncodingOptions() {
        return encodingOptions;
    }

    @Nonnull
    @Override
    public TextFontOptions getFontOptions() {
        return fontOptions;
    }

    @Nonnull
    @Override
    public IntegrationOptions getIntegrationOptions() {
        return integrationOptions;
    }

    @Nonnull
    @Override
    public EditorOptions getEditorOptions() {
        return editorOptions;
    }

    @Nonnull
    @Override
    public StatusOptions getStatusOptions() {
        return statusOptions;
    }

    @Nonnull
    @Override public DataInspectorOptions getDataInspectorOptions() {
        return dataInspectorOptions;
    }

    @Nonnull
    @Override
    public CodeAreaLayoutOptions getLayoutOptions() {
        return layoutOptions;
    }

    @Nonnull
    @Override
    public CodeAreaColorOptions getColorOptions() {
        return colorOptions;
    }

    @Nonnull
    @Override
    public CodeAreaThemeOptions getThemeOptions() {
        return themeOptions;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel categoriesLabel;
    private javax.swing.JList<CategoryItem> categoriesList;
    private javax.swing.JScrollPane categoriesScrollPane;
    private javax.swing.JScrollPane mainPane;
    // End of variables declaration//GEN-END:variables

    private boolean isValidProfileName(@Nullable String profileName) {
        return profileName != null && !"".equals(profileName.trim());
    }

    private static final class ThemeProfileResult {

        ThemeProfilesPanel.ThemeProfile profile;
    }

    private static final class LayoutProfileResult {

        LayoutProfilesPanel.LayoutProfile profile;
    }

    private static final class ColorProfileResult {

        ColorProfilesPanel.ColorProfile profile;
    }

    @Nonnull
    private ThemeProfilePanel createThemeProfilePanel() {
        ThemeProfilePanel themeProfilePanel = new ThemeProfilePanel();
        List<String> backgroundModes = new ArrayList<>();
        for (ExtendedBackgroundPaintMode mode : ExtendedBackgroundPaintMode.values()) {
            backgroundModes.add(managerResourceBundle.getString("backgroundPaintMode." + mode.name().toLowerCase()));
        }
        themeProfilePanel.setBackgroundModes(backgroundModes);
        return themeProfilePanel;
    }

    @ParametersAreNonnullByDefault
    private static class CategoryItem {

        String categoryName;
        JPanel categoryPanel;

        public CategoryItem(String categoryName, JPanel categoryPanel) {
            this.categoryName = categoryName;
            this.categoryPanel = categoryPanel;
        }

        public String getCategoryName() {
            return categoryName;
        }

        public JPanel getCategoryPanel() {
            return categoryPanel;
        }
    }
}
