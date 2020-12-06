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
package org.exbin.framework.editor.text.gui;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.font.TextAttribute;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import org.exbin.framework.gui.utils.LanguageUtils;
import org.exbin.framework.gui.utils.WindowUtils;

/**
 * Font selection panel.
 *
 * @version 0.2.0 2019/06/08
 * @author ExBin Project (http://exbin.org)
 */
public class TextFontPanel extends javax.swing.JPanel {

    private final ResourceBundle resourceBundle = LanguageUtils.getResourceBundleByClass(TextFontPanel.class);

    private static String[] fontNames;
    private static String[] fontSizes;

    public TextFontPanel() {
        initComponents();

        ActionListener actionListener = (ActionEvent e) -> {
            updatePreview();
        };
        InputListPanel.ChangeListener changeListener = this::updatePreview;
        boldCheckBox.addActionListener(actionListener);
        italicCheckBox.addActionListener(actionListener);
        underlineCheckBox.addActionListener(actionListener);
        strikethroughCheckBox.addActionListener(actionListener);
        subscriptCheckBox.addActionListener(actionListener);
        superscriptCheckBox.addActionListener(actionListener);

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        fontNames = ge.getAvailableFontFamilyNames();
        fontFamilyInputList.setItems(fontNames);
        fontFamilyInputList.setChangeListener(changeListener);

        fontSizes = new String[]{"8", "9", "10", "11", "12", "14", "16",
            "18", "20", "22", "24", "26", "28", "36", "48", "72"};
        fontSizeInputList.setItems(fontSizes);
        fontSizeInputList.setChangeListener(changeListener);
    }

    private void updatePreview() {
        previewTextField.setFont(getStoredFont());
        previewTextField.repaint();
        subscriptCheckBox.setEnabled(!superscriptCheckBox.isSelected() || (superscriptCheckBox.isSelected() && subscriptCheckBox.isSelected()));
        superscriptCheckBox.setEnabled(!subscriptCheckBox.isSelected());
    }

    public Font getStoredFont() {
        String fontName = fontFamilyInputList.getSelectedValue();
        String fontSize = fontSizeInputList.getSelectedValue();
        int size = -1;
        try {
            size = Integer.parseInt(fontSize);
        } catch (NumberFormatException ex) {
            // Ignore
        }

        if (size <= 0) {
            return null;
        }

        Map<TextAttribute, Object> attribs = new HashMap<>();

        attribs.put(TextAttribute.FAMILY, fontName);
        attribs.put(TextAttribute.SIZE, (float) size);

        if (boldCheckBox.isSelected()) {
            attribs.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD);
        }
        if (italicCheckBox.isSelected()) {
            attribs.put(TextAttribute.POSTURE, TextAttribute.POSTURE_OBLIQUE);
        }
        if (underlineCheckBox.isSelected()) {
            attribs.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_LOW_ONE_PIXEL);
        }
        if (strikethroughCheckBox.isSelected()) {
            attribs.put(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
        }
        if (subscriptCheckBox.isSelected()) {
            attribs.put(TextAttribute.SUPERSCRIPT, TextAttribute.SUPERSCRIPT_SUB);
        }
        if (superscriptCheckBox.isSelected()) {
            attribs.put(TextAttribute.SUPERSCRIPT, TextAttribute.SUPERSCRIPT_SUPER);
        }

        return new Font(attribs);
    }

    public void setStoredFont(Font font) {
        Map<TextAttribute, ?> attribs = font.getAttributes();

        fontFamilyInputList.setSelectedValue((String) attribs.get(TextAttribute.FAMILY));

        Float fontSize = (Float) attribs.get(TextAttribute.SIZE);
        if (fontSize != null) {
            fontSizeInputList.setSelectedValue(String.valueOf((int) (float) fontSize));
        } else {
            fontSizeInputList.setSelectedValue(String.valueOf(12));
        }

        underlineCheckBox.setSelected(TextAttribute.UNDERLINE_LOW_ONE_PIXEL.equals(attribs.get(TextAttribute.UNDERLINE)));
        strikethroughCheckBox.setSelected(TextAttribute.STRIKETHROUGH_ON.equals(attribs.get(TextAttribute.STRIKETHROUGH)));
        boldCheckBox.setSelected(TextAttribute.WEIGHT_BOLD.equals(attribs.get(TextAttribute.WEIGHT)));
        italicCheckBox.setSelected(TextAttribute.POSTURE_OBLIQUE.equals(attribs.get(TextAttribute.POSTURE)));
        subscriptCheckBox.setSelected(TextAttribute.SUPERSCRIPT_SUB.equals(attribs.get(TextAttribute.SUPERSCRIPT)));
        superscriptCheckBox.setSelected(TextAttribute.SUPERSCRIPT_SUPER.equals(attribs.get(TextAttribute.SUPERSCRIPT)));

        updatePreview();
    }

    public void initFont() {
        setStoredFont(previewTextField.getFont());
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        fontPanel = new javax.swing.JPanel();
        fontFamilyPanel = new javax.swing.JPanel();
        fontFamilyLabel = new javax.swing.JLabel();
        fontFamilyInputList = new org.exbin.framework.editor.text.gui.InputListPanel();
        fontSizePanel = new javax.swing.JPanel();
        fontSizeLabel = new javax.swing.JLabel();
        fontSizeInputList = new org.exbin.framework.editor.text.gui.InputListPanel();
        fontStylePanel = new javax.swing.JPanel();
        boldCheckBox = new javax.swing.JCheckBox();
        italicCheckBox = new javax.swing.JCheckBox();
        underlineCheckBox = new javax.swing.JCheckBox();
        strikethroughCheckBox = new javax.swing.JCheckBox();
        subscriptCheckBox = new javax.swing.JCheckBox();
        superscriptCheckBox = new javax.swing.JCheckBox();
        previewPanel = new javax.swing.JPanel();
        previewTextField = new javax.swing.JTextField();

        setName("Form"); // NOI18N

        fontPanel.setName("fontPanel"); // NOI18N
        fontPanel.setLayout(new java.awt.BorderLayout());

        fontFamilyPanel.setName("fontFamilyPanel"); // NOI18N

        fontFamilyLabel.setText(resourceBundle.getString("TextFontPanel.fontFamilyLabel.text")); // NOI18N
        fontFamilyLabel.setName("fontFamilyLabel"); // NOI18N

        fontFamilyInputList.setName("fontFamilyInputList"); // NOI18N

        javax.swing.GroupLayout fontFamilyPanelLayout = new javax.swing.GroupLayout(fontFamilyPanel);
        fontFamilyPanel.setLayout(fontFamilyPanelLayout);
        fontFamilyPanelLayout.setHorizontalGroup(
            fontFamilyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(fontFamilyPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(fontFamilyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(fontFamilyInputList, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(fontFamilyLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 279, Short.MAX_VALUE)))
        );
        fontFamilyPanelLayout.setVerticalGroup(
            fontFamilyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(fontFamilyPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(fontFamilyLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(fontFamilyInputList, javax.swing.GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE))
        );

        fontPanel.add(fontFamilyPanel, java.awt.BorderLayout.CENTER);

        fontSizePanel.setName("fontSizePanel"); // NOI18N

        fontSizeLabel.setText(resourceBundle.getString("TextFontPanel.fontSizeLabel.text")); // NOI18N
        fontSizeLabel.setName("fontSizeLabel"); // NOI18N

        fontSizeInputList.setName("fontSizeInputList"); // NOI18N

        javax.swing.GroupLayout fontSizePanelLayout = new javax.swing.GroupLayout(fontSizePanel);
        fontSizePanel.setLayout(fontSizePanelLayout);
        fontSizePanelLayout.setHorizontalGroup(
            fontSizePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, fontSizePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(fontSizePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(fontSizeInputList, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(fontSizeLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 124, Short.MAX_VALUE))
                .addContainerGap())
        );
        fontSizePanelLayout.setVerticalGroup(
            fontSizePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(fontSizePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(fontSizeLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(fontSizeInputList, javax.swing.GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE))
        );

        fontPanel.add(fontSizePanel, java.awt.BorderLayout.EAST);

        fontStylePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceBundle.getString("TextFontPanel.fontStylePanel.border.title"))); // NOI18N
        fontStylePanel.setName("fontStylePanel"); // NOI18N
        fontStylePanel.setLayout(new java.awt.GridLayout(2, 3));

        boldCheckBox.setMnemonic('b');
        boldCheckBox.setText(resourceBundle.getString("TextFontPanel.boldCheckBox.text")); // NOI18N
        boldCheckBox.setName("boldCheckBox"); // NOI18N
        fontStylePanel.add(boldCheckBox);

        italicCheckBox.setMnemonic('i');
        italicCheckBox.setText(resourceBundle.getString("TextFontPanel.italicCheckBox.text")); // NOI18N
        italicCheckBox.setName("italicCheckBox"); // NOI18N
        fontStylePanel.add(italicCheckBox);

        underlineCheckBox.setMnemonic('u');
        underlineCheckBox.setText(resourceBundle.getString("TextFontPanel.underlineCheckBox.text")); // NOI18N
        underlineCheckBox.setName("underlineCheckBox"); // NOI18N
        fontStylePanel.add(underlineCheckBox);

        strikethroughCheckBox.setMnemonic('s');
        strikethroughCheckBox.setText(resourceBundle.getString("TextFontPanel.strikethroughCheckBox.text")); // NOI18N
        strikethroughCheckBox.setName("strikethroughCheckBox"); // NOI18N
        fontStylePanel.add(strikethroughCheckBox);

        subscriptCheckBox.setMnemonic('t');
        subscriptCheckBox.setText(resourceBundle.getString("TextFontPanel.subscriptCheckBox.text")); // NOI18N
        subscriptCheckBox.setName("subscriptCheckBox"); // NOI18N
        fontStylePanel.add(subscriptCheckBox);

        superscriptCheckBox.setMnemonic('p');
        superscriptCheckBox.setText(resourceBundle.getString("TextFontPanel.superscriptCheckBox.text")); // NOI18N
        superscriptCheckBox.setName("superscriptCheckBox"); // NOI18N
        fontStylePanel.add(superscriptCheckBox);

        previewPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceBundle.getString("TextFontPanel.previewPanel.border.title"))); // NOI18N
        previewPanel.setName("previewPanel"); // NOI18N

        previewTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        previewTextField.setText(resourceBundle.getString("TextFontPanel.previewTextField.text")); // NOI18N
        previewTextField.setName("previewTextField"); // NOI18N

        javax.swing.GroupLayout previewPanelLayout = new javax.swing.GroupLayout(previewPanel);
        previewPanel.setLayout(previewPanelLayout);
        previewPanelLayout.setHorizontalGroup(
            previewPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(previewTextField)
        );
        previewPanelLayout.setVerticalGroup(
            previewPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(previewTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 69, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(fontPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(previewPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(fontStylePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(fontPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(fontStylePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(previewPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Test method for this panel.
     *
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        WindowUtils.invokeDialog(new TextFontPanel());
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox boldCheckBox;
    private org.exbin.framework.editor.text.gui.InputListPanel fontFamilyInputList;
    private javax.swing.JLabel fontFamilyLabel;
    private javax.swing.JPanel fontFamilyPanel;
    private javax.swing.JPanel fontPanel;
    private org.exbin.framework.editor.text.gui.InputListPanel fontSizeInputList;
    private javax.swing.JLabel fontSizeLabel;
    private javax.swing.JPanel fontSizePanel;
    private javax.swing.JPanel fontStylePanel;
    private javax.swing.JCheckBox italicCheckBox;
    private javax.swing.JPanel previewPanel;
    private javax.swing.JTextField previewTextField;
    private javax.swing.JCheckBox strikethroughCheckBox;
    private javax.swing.JCheckBox subscriptCheckBox;
    private javax.swing.JCheckBox superscriptCheckBox;
    private javax.swing.JCheckBox underlineCheckBox;
    // End of variables declaration//GEN-END:variables

    public ResourceBundle getResourceBundle() {
        return resourceBundle;
    }
}
