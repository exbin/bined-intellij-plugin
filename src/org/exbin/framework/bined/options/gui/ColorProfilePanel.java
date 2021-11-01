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
package org.exbin.framework.bined.options.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.Objects;
import java.util.ResourceBundle;
import javax.annotation.Nonnull;
import org.exbin.bined.swing.extended.ExtCodeArea;
import org.exbin.bined.swing.extended.color.ExtendedCodeAreaColorProfile;
import org.exbin.framework.gui.utils.LanguageUtils;
import org.exbin.framework.gui.utils.WindowUtils;

/**
 * Color profile panel.
 *
 * @version 0.2.1 2021/09/21
 * @author ExBin Project (http://exbin.org)
 */
public class ColorProfilePanel extends javax.swing.JPanel {

    private final java.util.ResourceBundle resourceBundle = LanguageUtils.getResourceBundleByClass(ColorProfilePanel.class);

    private final PreviewPanel previewPanel = new PreviewPanel(PreviewPanel.PreviewType.WITH_SEARCH);
    private final ColorProfileTableModel colorTableModel = new ColorProfileTableModel();

    public ColorProfilePanel() {
        initComponents();
        init();
    }

    private void init() {
        colorsTable.setDefaultRenderer(Color.class, new ColorCellTableRenderer());
        colorsTable.setDefaultEditor(Color.class, new ColorCellTableEditor());

        add(previewPanel, BorderLayout.CENTER);
        ExtCodeArea codeArea = previewPanel.getCodeArea();
        colorTableModel.setColorProfile((ExtendedCodeAreaColorProfile) codeArea.getColorsProfile());
    }

    @Nonnull
    public ResourceBundle getResourceBundle() {
        return resourceBundle;
    }

    public void setColorProfile(@Nonnull ExtendedCodeAreaColorProfile colorProfile) {
        ExtendedCodeAreaColorProfile newColorProfile = colorProfile.createCopy();
        ExtCodeArea codeArea = previewPanel.getCodeArea();
        codeArea.setColorsProfile(newColorProfile);
        colorTableModel.setColorProfile(newColorProfile);
    }

    @Nonnull
    public ExtendedCodeAreaColorProfile getColorProfile() {
        ExtCodeArea codeArea = previewPanel.getCodeArea();
        ExtendedCodeAreaColorProfile profile = (ExtendedCodeAreaColorProfile) codeArea.getColorsProfile();
        return Objects.requireNonNull(profile).createCopy();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        preferencesPanel = new javax.swing.JPanel();
        colorsScrollPane = new javax.swing.JScrollPane();
        colorsTable = new javax.swing.JTable();

        setLayout(new java.awt.BorderLayout());

        preferencesPanel.setLayout(new java.awt.BorderLayout());

        colorsTable.setModel(colorTableModel);
        colorsScrollPane.setViewportView(colorsTable);

        preferencesPanel.add(colorsScrollPane, java.awt.BorderLayout.CENTER);

        add(preferencesPanel, java.awt.BorderLayout.WEST);
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Test method for this panel.
     *
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        WindowUtils.invokeDialog(new ColorProfilePanel());
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane colorsScrollPane;
    private javax.swing.JTable colorsTable;
    private javax.swing.JPanel preferencesPanel;
    // End of variables declaration//GEN-END:variables
}
