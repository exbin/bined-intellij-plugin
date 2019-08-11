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

import org.exbin.bined.intellij.BinEdApplyOptions;
import org.exbin.framework.bined.options.*;
import org.exbin.framework.bined.options.impl.*;
import org.exbin.framework.bined.options.panel.*;
import org.exbin.framework.bined.preferences.BinaryEditorPreferences;
import org.exbin.framework.editor.text.options.impl.TextEncodingOptionsImpl;
import org.exbin.framework.editor.text.options.panel.TextEncodingOptionsPanel;
import org.exbin.framework.editor.text.panel.AddEncodingPanel;
import org.exbin.framework.gui.utils.LanguageUtils;
import org.exbin.framework.gui.utils.WindowUtils;
import org.exbin.framework.gui.utils.WindowUtils.DialogWrapper;
import org.exbin.framework.gui.utils.handler.DefaultControlHandler;
import org.exbin.framework.gui.utils.panel.DefaultControlPanel;

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
public class BinEdOptionsPanel extends javax.swing.JPanel implements BinEdApplyOptions {

    private BinaryEditorPreferences preferences;
//    private final org.exbin.bined.intellij.panel.BinEdOptionsPanelController controller;
    private final java.util.ResourceBundle resourceBundle = LanguageUtils.getResourceBundleByClass(BinEdOptionsPanel.class);

    private DefaultListModel<CategoryItem> categoryModel = new DefaultListModel<>();
    private JPanel currentCategoryPanel = null;

    private final EditorOptionsImpl editorOptions = new EditorOptionsImpl();
    private final StatusOptionsImpl statusOptions = new StatusOptionsImpl();
    private final TextEncodingOptionsImpl encodingOptions = new TextEncodingOptionsImpl();
    private final CodeAreaOptionsImpl codeAreaOptions = new CodeAreaOptionsImpl();
    private final CodeAreaLayoutOptionsImpl layoutOptions = new CodeAreaLayoutOptionsImpl();
    private final CodeAreaColorOptionsImpl colorOptions = new CodeAreaColorOptionsImpl();
    private final CodeAreaThemeOptionsImpl themeOptions = new CodeAreaThemeOptionsImpl();

    private final EditorOptionsPanel editorOptionsPanel = new EditorOptionsPanel();
    private final StatusOptionsPanel statusOptionsPanel = new StatusOptionsPanel();
    private final CodeAreaOptionsPanel codeAreaOptionsPanel = new CodeAreaOptionsPanel();
    private final TextEncodingOptionsPanel encodingOptionsPanel = new TextEncodingOptionsPanel();
    private final LayoutProfilesPanel layoutProfilesPanel = new LayoutProfilesPanel();
    private final ProfileSelectionPanel layoutSelectionPanel = new ProfileSelectionPanel(layoutProfilesPanel);
    private final ThemeProfilesPanel themeProfilesPanel = new ThemeProfilesPanel();
    private final ProfileSelectionPanel themeSelectionPanel = new ProfileSelectionPanel(themeProfilesPanel);
    private final ColorProfilesPanel colorProfilesPanel = new ColorProfilesPanel();
    private final ProfileSelectionPanel colorSelectionPanel = new ProfileSelectionPanel(colorProfilesPanel);

    public BinEdOptionsPanel() {
        initComponents();

        categoryModel.addElement(new CategoryItem("Editor", editorOptionsPanel));
        categoryModel.addElement(new CategoryItem("Status Panel", statusOptionsPanel));
        categoryModel.addElement(new CategoryItem("Code Area", codeAreaOptionsPanel));
        categoryModel.addElement(new CategoryItem("Encoding", encodingOptionsPanel));
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

        encodingOptionsPanel.setAddEncodingsOperation((List<String> usedEncodings) -> {
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
        editorOptions.loadFromPreferences(preferences.getEditorPreferences());
        statusOptions.loadFromPreferences(preferences.getStatusPreferences());
        codeAreaOptions.loadFromPreferences(preferences.getCodeAreaPreferences());
        encodingOptions.loadFromPreferences(preferences.getEncodingPreferences());
        layoutOptions.loadFromPreferences(preferences.getLayoutPreferences());
        colorOptions.loadFromPreferences(preferences.getColorPreferences());
        themeOptions.loadFromPreferences(preferences.getThemePreferences());

        editorOptionsPanel.loadFromOptions(editorOptions);
        statusOptionsPanel.loadFromOptions(statusOptions);
        codeAreaOptionsPanel.loadFromOptions(codeAreaOptions);
        encodingOptionsPanel.loadFromOptions(encodingOptions);

        layoutProfilesPanel.loadFromOptions(layoutOptions);
        layoutSelectionPanel.setDefaultProfile(layoutOptions.getSelectedProfile());
        colorProfilesPanel.loadFromOptions(colorOptions);
        colorSelectionPanel.setDefaultProfile(colorOptions.getSelectedProfile());
        themeProfilesPanel.loadFromOptions(themeOptions);
        themeSelectionPanel.setDefaultProfile(themeOptions.getSelectedProfile());
    }

    public void saveToPreferences() {
        applyToOptions();

        editorOptions.saveToPreferences(preferences.getEditorPreferences());
        statusOptions.saveToPreferences(preferences.getStatusPreferences());
        codeAreaOptions.saveToPreferences(preferences.getCodeAreaPreferences());
        encodingOptions.saveToPreferences(preferences.getEncodingPreferences());

        layoutOptions.saveToPreferences(preferences.getLayoutPreferences());
        colorOptions.saveToPreferences(preferences.getColorPreferences());
        themeOptions.saveToPreferences(preferences.getThemePreferences());
    }

    public void applyToOptions() {
        editorOptionsPanel.saveToOptions(editorOptions);
        statusOptionsPanel.saveToOptions(statusOptions);
        codeAreaOptionsPanel.saveToOptions(codeAreaOptions);
        encodingOptionsPanel.saveToOptions(encodingOptions);

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

    @Override
    public CodeAreaOptions getCodeAreaOptions() {
        return codeAreaOptions;
    }

    @Override
    public TextEncodingOptionsImpl getEncodingOptions() {
        return encodingOptions;
    }

    @Override
    public EditorOptions getEditorOptions() {
        return editorOptions;
    }

    @Override
    public StatusOptions getStatusOptions() {
        return statusOptions;
    }

    @Override
    public CodeAreaLayoutOptions getLayoutOptions() {
        return layoutOptions;
    }

    @Override
    public CodeAreaColorOptions getColorOptions() {
        return colorOptions;
    }

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
