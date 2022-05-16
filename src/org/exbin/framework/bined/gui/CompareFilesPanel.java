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
package org.exbin.framework.bined.gui;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.util.List;
import java.util.ResourceBundle;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JPopupMenu;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import org.exbin.auxiliary.paged_data.BinaryData;
import org.exbin.bined.swing.extended.ExtCodeArea;
import org.exbin.framework.bined.handler.CodeAreaPopupMenuHandler;
import org.exbin.framework.utils.LanguageUtils;
import org.exbin.framework.utils.WindowUtils;

/**
 * Compare files panel.
 *
 * @version 0.2.1 2021/11/02
 * @author ExBin Project (http://exbin.org)
 */
@ParametersAreNonnullByDefault
public class CompareFilesPanel extends javax.swing.JPanel {

    private final java.util.ResourceBundle resourceBundle = LanguageUtils.getResourceBundleByClass(CompareFilesPanel.class);

    private Controller controller;
    private FileRecord leftCustomFile;
    private FileRecord rightCustomFile;

    public CompareFilesPanel() {
        initComponents();
        init();
    }

    private void init() {
        leftComboBox.addItemListener((ItemEvent e) -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                int selectedIndex = leftComboBox.getSelectedIndex();
                if (selectedIndex < 0) {
                    return;
                }
                if (selectedIndex == 0) {
                    if (leftCustomFile == null) {
                        SwingUtilities.invokeLater(() -> {
                            leftOpenButtonActionPerformed(null);
                        });
                    } else {
                        switchToLeftCustomFile();
                    }
                } else {
                    setLeftFile(controller.getFileData(selectedIndex - 1));
                }
            }
        });
        rightComboBox.addItemListener((ItemEvent e) -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                int selectedIndex = rightComboBox.getSelectedIndex();
                if (selectedIndex < 0) {
                    return;
                }
                if (selectedIndex == 0) {
                    if (rightCustomFile == null) {
                        SwingUtilities.invokeLater(() -> {
                            rightOpenButtonActionPerformed(null);
                        });
                    } else {
                        switchToRightCustomFile();
                    }
                } else {
                    setRightFile(controller.getFileData(selectedIndex - 1));
                }
            }
        });
    }

    public void setLeftIndex(int index) {
        leftComboBox.setSelectedIndex(index);
    }

    public void setRightIndex(int index) {
        rightComboBox.setSelectedIndex(index);
    }

    @Nonnull
    public ResourceBundle getResourceBundle() {
        return resourceBundle;
    }

    public void setController(Controller controller) {
        this.controller = controller;
    }

    public void setAvailableFiles(List<String> availableFiles) {
        DefaultComboBoxModel<String> leftComboBoxModel = new DefaultComboBoxModel<>();
        leftComboBoxModel.addElement("Load file...");
        // addAll not in Java 8
        for (String fileName : availableFiles) {
            leftComboBoxModel.addElement(fileName);
        }
        leftComboBox.setModel(leftComboBoxModel);

        DefaultComboBoxModel<String> rightComboBoxModel = new DefaultComboBoxModel<>();
        rightComboBoxModel.addElement("Load file...");
        for (String fileName : availableFiles) {
            rightComboBoxModel.addElement(fileName);
        }
        rightComboBox.setModel(rightComboBoxModel);
    }

    public void setLeftFile(BinaryData contentData) {
        codeAreaDiffPanel.setLeftContentData(contentData);
    }

    public void setRightFile(BinaryData contentData) {
        codeAreaDiffPanel.setRightContentData(contentData);
    }

    public void setCodeAreaPopupMenu(CodeAreaPopupMenuHandler codeAreaPopupMenuHandler) {
        codeAreaDiffPanel.getLeftCodeArea().setComponentPopupMenu(createPopupMenu(codeAreaPopupMenuHandler, "compareLeft"));
        codeAreaDiffPanel.getRightCodeArea().setComponentPopupMenu(createPopupMenu(codeAreaPopupMenuHandler, "compareRight"));
    }

    @Nonnull
    private JPopupMenu createPopupMenu(final CodeAreaPopupMenuHandler codeAreaPopupMenuHandler, String popupMenuId) {
        return new JPopupMenu() {
            @Override
            public void show(Component invoker, int x, int y) {
                if (codeAreaPopupMenuHandler == null || invoker == null) {
                    return;
                }

                int clickedX = x;
                int clickedY = y;
                if (invoker instanceof JViewport) {
                    clickedX += ((JViewport) invoker).getParent().getX();
                    clickedY += ((JViewport) invoker).getParent().getY();
                }

                ExtCodeArea codeArea = invoker instanceof ExtCodeArea ? (ExtCodeArea) invoker
                        : (ExtCodeArea) ((JViewport) invoker).getParent().getParent();

                JPopupMenu popupMenu = codeAreaPopupMenuHandler.createPopupMenu(codeArea, popupMenuId, clickedX, clickedY);
                popupMenu.addPopupMenuListener(new PopupMenuListener() {
                    @Override
                    public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                    }

                    @Override
                    public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                        codeAreaPopupMenuHandler.dropPopupMenu(popupMenuId);
                    }

                    @Override
                    public void popupMenuCanceled(PopupMenuEvent e) {
                    }
                });
                popupMenu.show(invoker, x, y);
            }
        };
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        optionsPanel = new javax.swing.JPanel();
        leftLabel = new javax.swing.JLabel();
        leftComboBox = new javax.swing.JComboBox<>();
        leftOpenButton = new javax.swing.JButton();
        rightLabel = new javax.swing.JLabel();
        rightComboBox = new javax.swing.JComboBox<>();
        rightOpenButton = new javax.swing.JButton();
        codeAreaDiffPanel = new org.exbin.bined.swing.extended.diff.ExtCodeAreaDiffPanel();

        setLayout(new java.awt.BorderLayout());

        leftLabel.setText(resourceBundle.getString("leftLabel.text")); // NOI18N

        leftOpenButton.setText("...");
        leftOpenButton.setToolTipText(resourceBundle.getString("leftOpenButton1.toolTipText")); // NOI18N
        leftOpenButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                leftOpenButtonActionPerformed(evt);
            }
        });

        rightLabel.setText(resourceBundle.getString("rightLabel.text")); // NOI18N

        rightOpenButton.setText("...");
        rightOpenButton.setToolTipText(resourceBundle.getString("rightOpenButton.toolTipText")); // NOI18N
        rightOpenButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rightOpenButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout optionsPanelLayout = new javax.swing.GroupLayout(optionsPanel);
        optionsPanel.setLayout(optionsPanelLayout);
        optionsPanelLayout.setHorizontalGroup(
            optionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(optionsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(optionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(optionsPanelLayout.createSequentialGroup()
                        .addComponent(leftComboBox, 0, 470, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(leftOpenButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(rightComboBox, 0, 445, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(rightOpenButton))
                    .addGroup(optionsPanelLayout.createSequentialGroup()
                        .addComponent(leftLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(rightLabel)))
                .addContainerGap())
        );
        optionsPanelLayout.setVerticalGroup(
            optionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(optionsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(optionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(leftLabel)
                    .addComponent(rightLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(optionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(leftComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(rightComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(leftOpenButton)
                    .addComponent(rightOpenButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        add(optionsPanel, java.awt.BorderLayout.PAGE_START);

        add(codeAreaDiffPanel, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void rightOpenButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rightOpenButtonActionPerformed
        if (controller != null) {
            FileRecord file = controller.openFile();
            if (file != null) {
                rightCustomFile = file;
                rightComboBox.setSelectedIndex(0);
                rightComboBox.removeItemAt(0);
                rightComboBox.insertItemAt(file.getName(), 0);
                switchToRightCustomFile();
            }
        }
    }//GEN-LAST:event_rightOpenButtonActionPerformed

    private void leftOpenButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_leftOpenButtonActionPerformed
        if (controller != null) {
            FileRecord file = controller.openFile();
            if (file != null) {
                leftCustomFile = file;
                leftComboBox.setSelectedIndex(0);
                leftComboBox.removeItemAt(0);
                leftComboBox.insertItemAt(file.getName(), 0);
                switchToLeftCustomFile();
            }
        }
    }//GEN-LAST:event_leftOpenButtonActionPerformed

    private void switchToLeftCustomFile() {
        setLeftFile(leftCustomFile.getData());
    }

    private void switchToRightCustomFile() {
        setRightFile(rightCustomFile.getData());
    }

    /**
     * Test method for this panel.
     *
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        WindowUtils.invokeDialog(new CompareFilesPanel());
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private org.exbin.bined.swing.extended.diff.ExtCodeAreaDiffPanel codeAreaDiffPanel;
    private javax.swing.JComboBox<String> leftComboBox;
    private javax.swing.JLabel leftLabel;
    private javax.swing.JButton leftOpenButton;
    private javax.swing.JPanel optionsPanel;
    private javax.swing.JComboBox<String> rightComboBox;
    private javax.swing.JLabel rightLabel;
    private javax.swing.JButton rightOpenButton;
    // End of variables declaration//GEN-END:variables

    public interface Controller {

        @Nullable
        FileRecord openFile();

        @Nonnull
        BinaryData getFileData(int index);
    }

    @ParametersAreNonnullByDefault
    public static class FileRecord {

        private final String name;
        private final BinaryData data;

        public FileRecord(String name, BinaryData data) {
            this.name = name;
            this.data = data;
        }

        public String getName() {
            return name;
        }

        public BinaryData getData() {
            return data;
        }
    }
}
