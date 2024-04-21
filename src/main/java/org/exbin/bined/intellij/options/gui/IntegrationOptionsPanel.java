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
package org.exbin.bined.intellij.options.gui;

import java.awt.Component;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JList;
import org.exbin.bined.intellij.options.impl.IntegrationOptionsImpl;
import org.exbin.framework.App;
import org.exbin.framework.language.api.LanguageModuleApi;
import org.exbin.framework.ui.model.LanguageRecord;
import org.exbin.framework.utils.TestApplication;
import org.exbin.framework.utils.WindowUtils;
import org.exbin.framework.options.api.OptionsComponent;
import org.exbin.framework.options.api.OptionsModifiedListener;

/**
 * Integration preference parameters panel.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class IntegrationOptionsPanel extends javax.swing.JPanel implements OptionsComponent<IntegrationOptionsImpl> {

    private final java.util.ResourceBundle resourceBundle = App.getModule(LanguageModuleApi.class).getBundle(IntegrationOptionsPanel.class);
    private OptionsModifiedListener optionsModifiedListener;
    private String defaultLocaleName = "";

    public IntegrationOptionsPanel() {
        initComponents();
    }

    @Nonnull
    @Override
    public ResourceBundle getResourceBundle() {
        return resourceBundle;
    }

    @Override
    public void saveToOptions(IntegrationOptionsImpl options) {
        options.setLanguageLocale(((LanguageRecord) languageComboBox.getSelectedItem()).getLocale());
        options.setRegisterFileMenuOpenAsBinary(openFileAsBinaryCheckBox.isSelected());
        options.setRegisterOpenFileAsBinaryViaToolbar(openFileToolbarBinaryCheckBox.isSelected());
        options.setRegisterContextOpenAsBinary(contextOpenAsBinaryCheckBox.isSelected());
        options.setRegisterContextOpenInBinaryEditor(binaryEditorInContextOpenInCheckBox.isSelected());
        options.setRegisterNativeBinaryFile(nativeBinaryFileCheckBox.isSelected());
        options.setRegisterDebugViewAsBinary(openAsBinaryInDebugViewCheckBox.isSelected());
        options.setRegisterByteToByteDiffTool(byteToByteDiffToolCheckBox.isSelected());
        options.setRegisterEditAsBinaryForDbColumn(editAsBinaryForDbColumnCheckBox.isSelected());
    }

    @Override
    public void loadFromOptions(IntegrationOptionsImpl options) {
        Locale languageLocale = options.getLanguageLocale();
        ComboBoxModel<LanguageRecord> languageComboBoxModel = languageComboBox.getModel();
        for (int i = 0; i < languageComboBoxModel.getSize(); i++) {
            LanguageRecord languageRecord = languageComboBoxModel.getElementAt(i);
            if (languageLocale.equals(languageRecord.getLocale())) {
                languageComboBox.setSelectedIndex(i);
                break;
            }
        }
        openFileAsBinaryCheckBox.setSelected(options.isRegisterFileMenuOpenAsBinary());
        openFileToolbarBinaryCheckBox.setSelected(options.isRegisterOpenFileAsBinaryViaToolbar());
        contextOpenAsBinaryCheckBox.setSelected(options.isRegisterContextOpenAsBinary());
        binaryEditorInContextOpenInCheckBox.setSelected(options.isRegisterContextOpenInBinaryEditor());
        nativeBinaryFileCheckBox.setSelected(options.isRegisterNativeBinaryFile());
        openAsBinaryInDebugViewCheckBox.setSelected(options.isRegisterDebugViewAsBinary());
        byteToByteDiffToolCheckBox.setSelected(options.isRegisterByteToByteDiffTool());
        editAsBinaryForDbColumnCheckBox.setSelected(options.isRegisterEditAsBinaryForDbColumn());
    }

    public void setLanguageLocales(List<LanguageRecord> languageLocales) {
        DefaultComboBoxModel<LanguageRecord> languageComboBoxModel = new DefaultComboBoxModel<>();
        languageLocales.forEach(languageComboBoxModel::addElement);
        languageComboBox.setModel(languageComboBoxModel);
        languageComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                DefaultListCellRenderer renderer = (DefaultListCellRenderer) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                LanguageRecord record = (LanguageRecord) value;
                String languageText = record.getText();
                if ("".equals(languageText)) {
                    languageText = defaultLocaleName;
                }
                renderer.setText(languageText);
                ImageIcon flag = record.getFlag();
                if (flag != null) {
                    renderer.setIcon(flag);
                }
                return renderer;
            }
        });
    }

    public void setDefaultLocaleName(String defaultLocaleName) {
        this.defaultLocaleName = defaultLocaleName;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        languageComboBox = new javax.swing.JComboBox<>();
        languageLabel = new javax.swing.JLabel();
        openFileAsBinaryCheckBox = new javax.swing.JCheckBox();
        openFileToolbarBinaryCheckBox = new javax.swing.JCheckBox();
        contextOpenAsBinaryCheckBox = new javax.swing.JCheckBox();
        binaryEditorInContextOpenInCheckBox = new javax.swing.JCheckBox();
        openAsBinaryInDebugViewCheckBox = new javax.swing.JCheckBox();
        nativeBinaryFileCheckBox = new javax.swing.JCheckBox();
        byteToByteDiffToolCheckBox = new javax.swing.JCheckBox();
        editAsBinaryForDbColumnCheckBox = new javax.swing.JCheckBox();

        languageComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                languageComboBoxItemStateChanged(evt);
            }
        });

        languageLabel.setText(resourceBundle.getString("languageLabel.text")); // NOI18N

        openFileAsBinaryCheckBox.setText(resourceBundle.getString("openFileAsBinaryCheckBox.text")); // NOI18N
        openFileAsBinaryCheckBox.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                openFileAsBinaryCheckBoxStateChanged(evt);
            }
        });

        openFileToolbarBinaryCheckBox.setSelected(true);
        openFileToolbarBinaryCheckBox.setText(resourceBundle.getString("openFileToolbarBinaryCheckBox.text")); // NOI18N
        openFileToolbarBinaryCheckBox.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                openFileToolbarBinaryCheckBoxStateChanged(evt);
            }
        });

        contextOpenAsBinaryCheckBox.setText(resourceBundle.getString("contextOpenAsBinaryCheckBox.text")); // NOI18N
        contextOpenAsBinaryCheckBox.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                contextOpenAsBinaryCheckBoxStateChanged(evt);
            }
        });

        binaryEditorInContextOpenInCheckBox.setSelected(true);
        binaryEditorInContextOpenInCheckBox.setText(resourceBundle.getString("binaryEditorInContextOpenInCheckBox.text")); // NOI18N
        binaryEditorInContextOpenInCheckBox.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                binaryEditorInContextOpenInCheckBoxStateChanged(evt);
            }
        });

        openAsBinaryInDebugViewCheckBox.setSelected(true);
        openAsBinaryInDebugViewCheckBox.setText(resourceBundle.getString("openAsBinaryInDebugViewCheckBox.text")); // NOI18N
        openAsBinaryInDebugViewCheckBox.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                openAsBinaryInDebugViewCheckBoxStateChanged(evt);
            }
        });

        nativeBinaryFileCheckBox.setSelected(true);
        nativeBinaryFileCheckBox.setText(resourceBundle.getString("nativeBinaryFileCheckBox.text")); // NOI18N
        nativeBinaryFileCheckBox.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                nativeBinaryFileCheckBoxStateChanged(evt);
            }
        });

        byteToByteDiffToolCheckBox.setSelected(true);
        byteToByteDiffToolCheckBox.setText(resourceBundle.getString("byteToByteDiffToolCheckBox.text")); // NOI18N
        byteToByteDiffToolCheckBox.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                byteToByteDiffToolCheckBoxStateChanged(evt);
            }
        });

        editAsBinaryForDbColumnCheckBox.setSelected(true);
        editAsBinaryForDbColumnCheckBox.setText(resourceBundle.getString("editAsBinaryForDbColumnCheckBox.text")); // NOI18N
        editAsBinaryForDbColumnCheckBox.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                editAsBinaryForDbColumnCheckBoxStateChanged(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(openFileAsBinaryCheckBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(openFileToolbarBinaryCheckBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(contextOpenAsBinaryCheckBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(binaryEditorInContextOpenInCheckBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(openAsBinaryInDebugViewCheckBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(byteToByteDiffToolCheckBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(editAsBinaryForDbColumnCheckBox, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(languageComboBox, javax.swing.GroupLayout.Alignment.TRAILING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(languageLabel)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(nativeBinaryFileCheckBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(languageLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(languageComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(openFileAsBinaryCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(openFileToolbarBinaryCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(contextOpenAsBinaryCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(binaryEditorInContextOpenInCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(openAsBinaryInDebugViewCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(nativeBinaryFileCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(byteToByteDiffToolCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(editAsBinaryForDbColumnCheckBox)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void openFileAsBinaryCheckBoxStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_openFileAsBinaryCheckBoxStateChanged
        notifyModified();
    }//GEN-LAST:event_openFileAsBinaryCheckBoxStateChanged

    private void openFileToolbarBinaryCheckBoxStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_openFileToolbarBinaryCheckBoxStateChanged
        notifyModified();
    }//GEN-LAST:event_openFileToolbarBinaryCheckBoxStateChanged

    private void contextOpenAsBinaryCheckBoxStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_contextOpenAsBinaryCheckBoxStateChanged
        notifyModified();
    }//GEN-LAST:event_contextOpenAsBinaryCheckBoxStateChanged

    private void binaryEditorInContextOpenInCheckBoxStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_binaryEditorInContextOpenInCheckBoxStateChanged
        notifyModified();
    }//GEN-LAST:event_binaryEditorInContextOpenInCheckBoxStateChanged

    private void openAsBinaryInDebugViewCheckBoxStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_openAsBinaryInDebugViewCheckBoxStateChanged
        notifyModified();
    }//GEN-LAST:event_openAsBinaryInDebugViewCheckBoxStateChanged

    private void byteToByteDiffToolCheckBoxStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_byteToByteDiffToolCheckBoxStateChanged
        notifyModified();
    }//GEN-LAST:event_byteToByteDiffToolCheckBoxStateChanged

    private void editAsBinaryForDbColumnCheckBoxStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_editAsBinaryForDbColumnCheckBoxStateChanged
        notifyModified();
    }//GEN-LAST:event_editAsBinaryForDbColumnCheckBoxStateChanged

    private void languageComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_languageComboBoxItemStateChanged
        notifyModified();
    }//GEN-LAST:event_languageComboBoxItemStateChanged

    private void nativeBinaryFileCheckBoxStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_nativeBinaryFileCheckBoxStateChanged
        notifyModified();
    }//GEN-LAST:event_nativeBinaryFileCheckBoxStateChanged

    /**
     * Test method for this panel.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        TestApplication.run(() -> WindowUtils.invokeWindow(new IntegrationOptionsPanel()));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox binaryEditorInContextOpenInCheckBox;
    private javax.swing.JCheckBox byteToByteDiffToolCheckBox;
    private javax.swing.JCheckBox contextOpenAsBinaryCheckBox;
    private javax.swing.JCheckBox editAsBinaryForDbColumnCheckBox;
    private javax.swing.JComboBox<LanguageRecord> languageComboBox;
    private javax.swing.JLabel languageLabel;
    private javax.swing.JCheckBox nativeBinaryFileCheckBox;
    private javax.swing.JCheckBox openAsBinaryInDebugViewCheckBox;
    private javax.swing.JCheckBox openFileAsBinaryCheckBox;
    private javax.swing.JCheckBox openFileToolbarBinaryCheckBox;
    // End of variables declaration//GEN-END:variables

    private void notifyModified() {
        if (optionsModifiedListener != null) {
            optionsModifiedListener.wasModified();
        }
    }

    @Override
    public void setOptionsModifiedListener(OptionsModifiedListener listener) {
        optionsModifiedListener = listener;
    }
}
