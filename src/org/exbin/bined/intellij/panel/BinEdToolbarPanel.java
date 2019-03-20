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

import org.exbin.bined.CodeType;
import org.exbin.bined.capability.RowWrappingCapable;
import org.exbin.bined.swing.extended.ExtCodeArea;
import org.exbin.framework.bined.preferences.BinaryEditorPreferences;
import org.exbin.framework.gui.utils.LanguageUtils;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Hexadecimal editor toolbar panel.
 *
 * @version 0.2.0 2019/03/17
 * @author ExBin Project (http://exbin.org)
 */
@ParametersAreNonnullByDefault
public class BinEdToolbarPanel extends javax.swing.JPanel {

    private final java.util.ResourceBundle resourceBundle = LanguageUtils.getResourceBundleByClass(BinEdToolbarPanel.class);

    private final BinaryEditorPreferences preferences;
    private final ExtCodeArea codeArea;

    public BinEdToolbarPanel(BinaryEditorPreferences preferences, ExtCodeArea codeArea) {
        this.preferences = preferences;
        this.codeArea = codeArea;
        initComponents();
    }

    public void applyFromCodeArea() {
        codeTypeComboBox.setSelectedIndex(codeArea.getCodeType().ordinal());
        showUnprintablesToggleButton.setSelected(codeArea.isShowUnprintables());
        rowWrappingToggleButton.setSelected(codeArea.getRowWrapping() == RowWrappingCapable.RowWrappingMode.WRAPPING);
    }

    public void loadFromPreferences() {
        codeTypeComboBox.setSelectedIndex(preferences.getCodeAreaParameters().getCodeType().ordinal());
        showUnprintablesToggleButton.setSelected(preferences.getCodeAreaParameters().isShowNonprintables());
        rowWrappingToggleButton.setSelected(preferences.getCodeAreaParameters().isRowWrapping());
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        controlToolBar = new javax.swing.JToolBar();
        rowWrappingToggleButton = new javax.swing.JToggleButton();
        showUnprintablesToggleButton = new javax.swing.JToggleButton();
        separator1 = new javax.swing.JToolBar.Separator();
        codeTypeComboBox = new javax.swing.JComboBox<>();

        controlToolBar.setBorder(null);
        controlToolBar.setFloatable(false);
        controlToolBar.setRollover(true);

        rowWrappingToggleButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/exbin/bined/intellij/resources/icons/bined-linewrap.png"))); // NOI18N
        rowWrappingToggleButton.setToolTipText(resourceBundle.getString("rowWrappingToggleButton.toolTipText")); // NOI18N
        rowWrappingToggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rowWrappingToggleButtonActionPerformed(evt);
            }
        });
        controlToolBar.add(rowWrappingToggleButton);

        showUnprintablesToggleButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/exbin/bined/intellij/resources/icons/insert-pilcrow.png"))); // NOI18N
        showUnprintablesToggleButton.setToolTipText(resourceBundle.getString("showUnprintablesToggleButton.toolTipText")); // NOI18N
        showUnprintablesToggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showUnprintablesToggleButtonActionPerformed(evt);
            }
        });
        controlToolBar.add(showUnprintablesToggleButton);
        controlToolBar.add(separator1);

        codeTypeComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "BIN", "OCT", "DEC", "HEX" }));
        codeTypeComboBox.setSelectedIndex(3);
        codeTypeComboBox.setToolTipText(resourceBundle.getString("codeTypeComboBox.toolTipText")); // NOI18N
        codeTypeComboBox.setMaximumSize(new java.awt.Dimension(58, 25));
        codeTypeComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                codeTypeComboBoxActionPerformed(evt);
            }
        });
        controlToolBar.add(codeTypeComboBox);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(controlToolBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 252, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(controlToolBar, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void rowWrappingToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rowWrappingToggleButtonActionPerformed
        codeArea.setRowWrapping(rowWrappingToggleButton.isSelected() ? RowWrappingCapable.RowWrappingMode.WRAPPING : RowWrappingCapable.RowWrappingMode.NO_WRAPPING);
        preferences.getCodeAreaParameters().setRowWrapping(rowWrappingToggleButton.isSelected());
    }//GEN-LAST:event_rowWrappingToggleButtonActionPerformed

    private void showUnprintablesToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showUnprintablesToggleButtonActionPerformed
        codeArea.setShowUnprintables(showUnprintablesToggleButton.isSelected());
        preferences.getCodeAreaParameters().setShowUnprintables(rowWrappingToggleButton.isSelected());
    }//GEN-LAST:event_showUnprintablesToggleButtonActionPerformed

    private void codeTypeComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_codeTypeComboBoxActionPerformed
        CodeType codeType = CodeType.values()[codeTypeComboBox.getSelectedIndex()];
        codeArea.setCodeType(codeType);
        preferences.getCodeAreaParameters().setCodeType(codeType);
    }//GEN-LAST:event_codeTypeComboBoxActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<String> codeTypeComboBox;
    private javax.swing.JToolBar controlToolBar;
    private javax.swing.JToggleButton rowWrappingToggleButton;
    private javax.swing.JToolBar.Separator separator1;
    private javax.swing.JToggleButton showUnprintablesToggleButton;
    // End of variables declaration//GEN-END:variables

}
