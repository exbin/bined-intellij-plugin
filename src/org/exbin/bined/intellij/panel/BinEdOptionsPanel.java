/*
 * Copyright (C) ExBin Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.exbin.bined.intellij.panel;

import com.intellij.ide.util.PropertiesComponent;
import org.exbin.bined.intellij.BinEdApplyOptions;
import org.exbin.bined.intellij.BinEdIntelliJPlugin;
import org.exbin.framework.bined.options.*;
import org.exbin.framework.bined.options.panel.*;
import org.exbin.framework.bined.preferences.BinaryEditorPreferences;
import org.exbin.framework.editor.text.options.TextEncodingOptions;
import org.exbin.framework.editor.text.options.panel.TextEncodingOptionsPanel;
import org.exbin.framework.editor.text.panel.AddEncodingPanel;
import org.exbin.framework.gui.utils.LanguageUtils;
import org.exbin.framework.gui.utils.WindowUtils;
import org.exbin.framework.gui.utils.WindowUtils.DialogWrapper;
import org.exbin.framework.gui.utils.handler.DefaultControlHandler;
import org.exbin.framework.gui.utils.panel.DefaultControlPanel;
import org.exbin.framework.preferences.PreferencesWrapper;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Binary editor options panel.
 *
 * @version 0.2.1 2019/07/21
 * @author ExBin Project (http://exbin.org)
 */
@ParametersAreNonnullByDefault
public class BinEdOptionsPanel extends javax.swing.JPanel {

    private final BinaryEditorPreferences preferences;
//    private final org.exbin.bined.intellij.panel.BinEdOptionsPanelController controller;
    private final java.util.ResourceBundle resourceBundle = LanguageUtils.getResourceBundleByClass(BinEdOptionsPanel.class);

    private DefaultListModel<CategoryItem> categoryModel = new DefaultListModel<>();
    private JPanel currentCategoryPanel = null;

    private final EditorOptions editorOptions = new EditorOptions();
    private final StatusOptions statusOptions = new StatusOptions();
    private final TextEncodingOptions encodingOptions = new TextEncodingOptions();
    private final CodeAreaOptions codeAreaOptions = new CodeAreaOptions();
    private final CodeAreaLayoutOptions layoutOptions = new CodeAreaLayoutOptions();
    private final CodeAreaColorOptions colorOptions = new CodeAreaColorOptions();
    private final CodeAreaThemeOptions themeOptions = new CodeAreaThemeOptions();

    private final EditorOptionsPanel editorParametersPanel = new EditorOptionsPanel();
    private final StatusOptionsPanel statusParametersPanel = new StatusOptionsPanel();
    private final CodeAreaOptionsPanel codeAreaParametersPanel = new CodeAreaOptionsPanel();
    private final TextEncodingOptionsPanel encodingParametersPanel = new TextEncodingOptionsPanel();
    private final LayoutProfilesPanel layoutProfilesPanel = new LayoutProfilesPanel();
    private final ProfileSelectionPanel layoutSelectionPanel = new ProfileSelectionPanel(layoutProfilesPanel);
    private final ThemeProfilesPanel themeProfilesPanel = new ThemeProfilesPanel();
    private final ProfileSelectionPanel themeSelectionPanel = new ProfileSelectionPanel(themeProfilesPanel);
    private final ColorProfilesPanel colorProfilesPanel = new ColorProfilesPanel();
    private final ProfileSelectionPanel colorSelectionPanel = new ProfileSelectionPanel(colorProfilesPanel);

    public BinEdOptionsPanel() {
        initComponents();
        preferences = new BinaryEditorPreferences(new PreferencesWrapper(PropertiesComponent.getInstance(), BinEdIntelliJPlugin.PLUGIN_PREFIX));

        categoryModel.addElement(new CategoryItem("Editor", editorParametersPanel));
        categoryModel.addElement(new CategoryItem("Status Panel", statusParametersPanel));
        categoryModel.addElement(new CategoryItem("Code Area", codeAreaParametersPanel));
        categoryModel.addElement(new CategoryItem("Charset", encodingParametersPanel));
        categoryModel.addElement(new CategoryItem("Layout Profiles", layoutSelectionPanel));
        categoryModel.addElement(new CategoryItem("Theme Profiles", themeSelectionPanel));
        categoryModel.addElement(new CategoryItem("Colors Profiles", colorSelectionPanel));
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

        encodingParametersPanel.setAddEncodingsOperation((List<String> usedEncodings) -> {
            final List<String> result = new ArrayList<>();
            final AddEncodingPanel addEncodingPanel = new AddEncodingPanel();
            addEncodingPanel.setUsedEncodings(usedEncodings);
            DefaultControlPanel encodingsControlPanel = new DefaultControlPanel(addEncodingPanel.getResourceBundle());
            JPanel dialogPanel = WindowUtils.createDialogPanel(addEncodingPanel, encodingsControlPanel);
            final DialogWrapper addEncodingDialog = WindowUtils.createDialog(dialogPanel, this, "Add Encodings", Dialog.ModalityType.APPLICATION_MODAL);
            encodingsControlPanel.setHandler((DefaultControlHandler.ControlActionType actionType) -> {
                if (actionType == DefaultControlHandler.ControlActionType.OK) {
                    result.addAll(addEncodingPanel.getEncodings());
                }

                addEncodingDialog.close();
            });
            addEncodingDialog.show();
            return result;
        });
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

    public void load() {
        editorOptions.loadFromParameters(preferences.getEditorPreferences());
        statusOptions.loadFromParameters(preferences.getStatusPreferences());
        codeAreaOptions.loadFromParameters(preferences.getCodeAreaPreferences());
        encodingOptions.loadFromParameters(preferences.getEncodingPreferences());
        layoutOptions.loadFromParameters(preferences.getLayoutPreferences());
        colorOptions.loadFromParameters(preferences.getColorPreferences());
        themeOptions.loadFromParameters(preferences.getThemePreferences());

        editorParametersPanel.loadFromOptions(editorOptions);
        statusParametersPanel.loadFromOptions(statusOptions);
        codeAreaParametersPanel.loadFromOptions(codeAreaOptions);
        encodingParametersPanel.loadFromOptions(encodingOptions);

        layoutProfilesPanel.loadFromOptions(layoutOptions);
        layoutSelectionPanel.setDefaultProfile(preferences.getLayoutPreferences().getSelectedProfile());
        colorProfilesPanel.loadFromOptions(colorOptions);
        colorSelectionPanel.setDefaultProfile(preferences.getColorPreferences().getSelectedProfile());
        themeProfilesPanel.loadFromOptions(themeOptions);
        themeSelectionPanel.setDefaultProfile(preferences.getThemePreferences().getSelectedProfile());
    }

    public void store() {
        editorParametersPanel.saveToOptions(editorOptions);
        statusParametersPanel.saveToOptions(statusOptions);
        codeAreaParametersPanel.saveToOptions(codeAreaOptions);
        encodingParametersPanel.saveToOptions(encodingOptions);

        editorOptions.saveToParameters(preferences.getEditorPreferences());
        statusOptions.saveToParameters(preferences.getStatusPreferences());
        codeAreaOptions.saveToParameters(preferences.getCodeAreaPreferences());
        encodingOptions.saveToParameters(preferences.getEncodingPreferences());

        layoutProfilesPanel.saveToOptions(layoutOptions);
        preferences.getLayoutPreferences().setSelectedProfile(layoutSelectionPanel.getDefaultProfile());
        colorProfilesPanel.saveToOptions(colorOptions);
        preferences.getColorPreferences().setSelectedProfile(colorSelectionPanel.getDefaultProfile());
        themeProfilesPanel.saveToOptions(themeOptions);
        preferences.getThemePreferences().setSelectedProfile(themeSelectionPanel.getDefaultProfile());

        layoutOptions.saveToParameters(preferences.getLayoutPreferences());
        colorOptions.saveToParameters(preferences.getColorPreferences());
        themeOptions.saveToParameters(preferences.getThemePreferences());
    }

    public void setApplyOptions(BinEdApplyOptions applyOptions) {
        codeAreaOptions.setOptions(applyOptions.getCodeAreaOptions());
        encodingOptions.setOptions(applyOptions.getEncodingOptions());
        editorOptions.setOptions(applyOptions.getEditorOptions());
        statusOptions.setOptions(applyOptions.getStatusOptions());
    }

    @Nonnull
    public BinEdApplyOptions getApplyOptions() {
        BinEdApplyOptions options = new BinEdApplyOptions();
        options.setCodeAreaOptions(codeAreaOptions);
        options.setEncodingOptions(encodingOptions);
        options.setEditorOptions(editorOptions);
        options.setStatusOptions(statusOptions);

        return options;
    }

    boolean valid() {
        // TODO check whether form is consistent and complete
        return true;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel categoriesLabel;
    private javax.swing.JList<CategoryItem> categoriesList;
    private javax.swing.JScrollPane categoriesScrollPane;
    private javax.swing.JScrollPane mainPane;
    // End of variables declaration//GEN-END:variables

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
