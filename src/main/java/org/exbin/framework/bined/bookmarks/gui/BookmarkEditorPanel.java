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
package org.exbin.framework.bined.bookmarks.gui;

import java.awt.Color;
import java.util.ResourceBundle;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.SwingUtilities;
import org.exbin.bined.CodeAreaSelection;
import org.exbin.framework.bined.bookmarks.model.BookmarkRecord;
import org.exbin.framework.bined.options.gui.ColorCellPanel;
import org.exbin.framework.utils.LanguageUtils;
import org.exbin.framework.utils.WindowUtils;

/**
 * Bookmark editor panel.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class BookmarkEditorPanel extends javax.swing.JPanel {

    private final ResourceBundle resourceBundle = LanguageUtils.getResourceBundleByClass(BookmarkEditorPanel.class);

    private Color color = null;
    private volatile boolean spinnerUpdate = false;

    private ColorCellPanel.ColorHandler colorHandler = new ColorCellPanel.ColorHandler() {
        @Nullable
        @Override
        public Color getColor() {
            return color;
        }

        @Override
        public void setColor(@Nullable Color color) {
            BookmarkEditorPanel.this.color = color;
        }
    };
    private CodeAreaSelection currentSelection;

    public BookmarkEditorPanel() {
        initComponents();
        init();
    }

    private void init() {
        colorCellPanel.setColorNullable(false);
        colorCellPanel.setColorHandler(colorHandler);
        startPositionSwitchableSpinnerPanel.setMinimum(0);
        startPositionSwitchableSpinnerPanel.addChangeListener((e) -> {
            if (spinnerUpdate) {
                return;
            }

            long startPosition = startPositionSwitchableSpinnerPanel.getValue();
            long length = lengthSwitchableSpinnerPanel.getValue();
            SwingUtilities.invokeLater(() -> {
                spinnerUpdate = true;
                endPositionSwitchableSpinnerPanel.setMinimum(startPosition);
                endPositionSwitchableSpinnerPanel.setValue(startPosition + length);
                spinnerUpdate = false;
            });
        });
        endPositionSwitchableSpinnerPanel.addChangeListener((e) -> {
            if (spinnerUpdate) {
                return;
            }

            long startPosition = startPositionSwitchableSpinnerPanel.getValue();
            long endPosition = endPositionSwitchableSpinnerPanel.getValue();
            SwingUtilities.invokeLater(() -> {
                spinnerUpdate = true;
                lengthSwitchableSpinnerPanel.setValue(endPosition - startPosition);
                spinnerUpdate = false;
            });
        });
        lengthSwitchableSpinnerPanel.setMinimum(0);
        lengthSwitchableSpinnerPanel.addChangeListener((e) -> {
            if (spinnerUpdate) {
                return;
            }

            long startPosition = startPositionSwitchableSpinnerPanel.getValue();
            long length = lengthSwitchableSpinnerPanel.getValue();
            SwingUtilities.invokeLater(() -> {
                spinnerUpdate = true;
                endPositionSwitchableSpinnerPanel.setValue(startPosition + length);
                spinnerUpdate = false;
            });
        });
    }

    @Nonnull
    public BookmarkRecord getBookmarkRecord() {
        return new BookmarkRecord(
                startPositionSwitchableSpinnerPanel.getValue(),
                lengthSwitchableSpinnerPanel.getValue(),
                color
        );
    }

    public void setBookmarkRecord(BookmarkRecord bookmarkRecord) {
        startPositionSwitchableSpinnerPanel.setValue(bookmarkRecord.getStartPosition());
        endPositionSwitchableSpinnerPanel.setMinimum(bookmarkRecord.getStartPosition());
        endPositionSwitchableSpinnerPanel.setValue(bookmarkRecord.getStartPosition() + bookmarkRecord.getLength());
        lengthSwitchableSpinnerPanel.setValue(bookmarkRecord.getLength());
        color = bookmarkRecord.getColor();
        colorCellPanel.setColorHandler(colorHandler);
    }

    public void setCurrentSelection(CodeAreaSelection selection) {
        currentSelection = selection;
        fromSelectionButton.setEnabled(!selection.isEmpty());
    }

    @Nonnull
    public ResourceBundle getResourceBundle() {
        return resourceBundle;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        startPositionLabel = new javax.swing.JLabel();
        startPositionSwitchableSpinnerPanel = new org.exbin.framework.bined.gui.BaseSwitchableSpinnerPanel();
        endPositionLabel = new javax.swing.JLabel();
        endPositionSwitchableSpinnerPanel = new org.exbin.framework.bined.gui.BaseSwitchableSpinnerPanel();
        lengthLabel = new javax.swing.JLabel();
        lengthSwitchableSpinnerPanel = new org.exbin.framework.bined.gui.BaseSwitchableSpinnerPanel();
        fromSelectionButton = new javax.swing.JButton();
        colorLabel = new javax.swing.JLabel();
        colorCellPanel = new org.exbin.framework.bined.options.gui.ColorCellPanel();

        startPositionLabel.setText(resourceBundle.getString("startPositionLabel.text")); // NOI18N

        endPositionLabel.setText(resourceBundle.getString("endPositionLabel.text")); // NOI18N

        lengthLabel.setText(resourceBundle.getString("lengthLabel.text")); // NOI18N

        fromSelectionButton.setText(resourceBundle.getString("fromSelectionButton.text")); // NOI18N
        fromSelectionButton.setEnabled(false);
        fromSelectionButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fromSelectionButtonActionPerformed(evt);
            }
        });

        colorLabel.setText(resourceBundle.getString("colorLabel.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(startPositionSwitchableSpinnerPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(endPositionSwitchableSpinnerPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lengthSwitchableSpinnerPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(colorCellPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(startPositionLabel)
                            .addComponent(endPositionLabel)
                            .addComponent(lengthLabel)
                            .addComponent(fromSelectionButton)
                            .addComponent(colorLabel))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(startPositionLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(startPositionSwitchableSpinnerPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(endPositionLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(endPositionSwitchableSpinnerPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lengthLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lengthSwitchableSpinnerPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(fromSelectionButton)
                .addGap(18, 18, 18)
                .addComponent(colorLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(colorCellPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void fromSelectionButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fromSelectionButtonActionPerformed
        startPositionSwitchableSpinnerPanel.setValue(currentSelection.getFirst());
        lengthSwitchableSpinnerPanel.setValue(currentSelection.getLength());
    }//GEN-LAST:event_fromSelectionButtonActionPerformed

    /**
     * Test method for this panel.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        WindowUtils.invokeDialog(new BookmarkEditorPanel());
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private org.exbin.framework.bined.options.gui.ColorCellPanel colorCellPanel;
    private javax.swing.JLabel colorLabel;
    private javax.swing.JLabel endPositionLabel;
    private org.exbin.framework.bined.gui.BaseSwitchableSpinnerPanel endPositionSwitchableSpinnerPanel;
    private javax.swing.JButton fromSelectionButton;
    private javax.swing.JLabel lengthLabel;
    private org.exbin.framework.bined.gui.BaseSwitchableSpinnerPanel lengthSwitchableSpinnerPanel;
    private javax.swing.JLabel startPositionLabel;
    private org.exbin.framework.bined.gui.BaseSwitchableSpinnerPanel startPositionSwitchableSpinnerPanel;
    // End of variables declaration//GEN-END:variables
}
