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
import org.exbin.bined.operation.undo.BinaryDataUndoHandler;
import org.exbin.bined.swing.extended.ExtCodeArea;
import org.exbin.framework.bined.preferences.BinaryEditorPreferences;
import org.exbin.framework.gui.menu.component.DropDownButton;
import org.exbin.framework.gui.utils.LanguageUtils;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Binary editor toolbar panel.
 *
 * @version 0.2.2 2020/01/16
 * @author ExBin Project (http://exbin.org)
 */
@ParametersAreNonnullByDefault
public class BinEdToolbarPanel extends javax.swing.JPanel {

    private final java.util.ResourceBundle resourceBundle = LanguageUtils.getResourceBundleByClass(BinEdToolbarPanel.class);

    private final BinaryEditorPreferences preferences;
    private final ExtCodeArea codeArea;
    private BinaryDataUndoHandler undoHandler;

    private JPanel controlToolBar;
    private ActionListener saveAction = null;
    private final AbstractAction cycleCodeTypesAction;
    private final JRadioButtonMenuItem binaryCodeTypeAction;
    private final JRadioButtonMenuItem octalCodeTypeAction;
    private final JRadioButtonMenuItem decimalCodeTypeAction;
    private final JRadioButtonMenuItem hexadecimalCodeTypeAction;
    private final ButtonGroup codeTypeButtonGroup;
    private DropDownButton codeTypeDropDown;

    public BinEdToolbarPanel(BinaryEditorPreferences preferences, ExtCodeArea codeArea) {
        this.preferences = preferences;
        this.codeArea = codeArea;

        codeTypeButtonGroup = new ButtonGroup();
        binaryCodeTypeAction = new JRadioButtonMenuItem(new AbstractAction("Binary") {
            @Override
            public void actionPerformed(ActionEvent e) {
                codeArea.setCodeType(CodeType.BINARY);
                updateCycleButtonName();
            }
        });
        codeTypeButtonGroup.add(binaryCodeTypeAction);
        octalCodeTypeAction = new JRadioButtonMenuItem(new AbstractAction("Octal") {
            @Override
            public void actionPerformed(ActionEvent e) {
                codeArea.setCodeType(CodeType.OCTAL);
                updateCycleButtonName();
            }
        });
        codeTypeButtonGroup.add(octalCodeTypeAction);
        decimalCodeTypeAction = new JRadioButtonMenuItem(new AbstractAction("Decimal") {
            @Override
            public void actionPerformed(ActionEvent e) {
                codeArea.setCodeType(CodeType.DECIMAL);
                updateCycleButtonName();
            }
        });
        codeTypeButtonGroup.add(decimalCodeTypeAction);
        hexadecimalCodeTypeAction = new JRadioButtonMenuItem(new AbstractAction("Hexadecimal") {
            @Override
            public void actionPerformed(ActionEvent e) {
                codeArea.setCodeType(CodeType.HEXADECIMAL);
                updateCycleButtonName();
            }
        });
        codeTypeButtonGroup.add(hexadecimalCodeTypeAction);
        cycleCodeTypesAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int codeTypePos = codeArea.getCodeType().ordinal();
                CodeType[] values = CodeType.values();
                CodeType next = codeTypePos + 1 >= values.length ? values[0] : values[codeTypePos + 1];
                codeArea.setCodeType(next);
                updateCycleButtonName();
            }
        };

        initComponents();
        init();
    }

    private void init() {
        cycleCodeTypesAction.putValue(Action.SHORT_DESCRIPTION, "Cycle thru code types");
        JPopupMenu cycleCodeTypesPopupMenu = new JPopupMenu();
        cycleCodeTypesPopupMenu.add(binaryCodeTypeAction);
        cycleCodeTypesPopupMenu.add(octalCodeTypeAction);
        cycleCodeTypesPopupMenu.add(decimalCodeTypeAction);
        cycleCodeTypesPopupMenu.add(hexadecimalCodeTypeAction);
        codeTypeDropDown = new DropDownButton(cycleCodeTypesAction, cycleCodeTypesPopupMenu);
        updateCycleButtonName();
        controlToolBar.add(codeTypeDropDown);
    }

    public void setUndoHandler(BinaryDataUndoHandler undoHandler) {
        this.undoHandler = undoHandler;

        int position = controlToolBar.getComponentZOrder(showUnprintablesToggleButton);
        controlToolBar.add(separator2, null, position);
        controlToolBar.add(redoEditButton, null, position);
        controlToolBar.add(undoEditButton, null, position);
    }

    private void updateCycleButtonName() {
        CodeType codeType = codeArea.getCodeType();
        codeTypeDropDown.setActionText(codeType.name().substring(0, 3));
        switch (codeType) {
            case BINARY: {
                if (!binaryCodeTypeAction.isSelected()) {
                    binaryCodeTypeAction.setSelected(true);
                }
                break;
            }
            case OCTAL: {
                if (!octalCodeTypeAction.isSelected()) {
                    octalCodeTypeAction.setSelected(true);
                }
                break;
            }
            case DECIMAL: {
                if (!decimalCodeTypeAction.isSelected()) {
                    decimalCodeTypeAction.setSelected(true);
                }
                break;
            }
            case HEXADECIMAL: {
                if (!hexadecimalCodeTypeAction.isSelected()) {
                    hexadecimalCodeTypeAction.setSelected(true);
                }
                break;
            }
        }
    }

    public void applyFromCodeArea() {
        updateCycleButtonName();
        showUnprintablesToggleButton.setSelected(codeArea.isShowUnprintables());
    }

    public void loadFromPreferences() {
        codeArea.setCodeType(preferences.getCodeAreaPreferences().getCodeType());
        updateCycleButtonName();
        showUnprintablesToggleButton.setSelected(preferences.getCodeAreaPreferences().isShowUnprintables());
    }

    public void updateUndoState() {
        undoEditButton.setEnabled(undoHandler.canUndo());
        redoEditButton.setEnabled(undoHandler.canRedo());
    }

    public void updateModified(boolean modified) {
        saveFileButton.setEnabled(saveAction != null && modified);
        updateUndoState();
    }

    public void setSaveAction(ActionListener saveAction) {
        this.saveAction = saveAction;
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));

        controlToolBar = this;

        saveFileButton = new JButton();
        undoEditButton = new JButton();
        redoEditButton = new JButton();
        showUnprintablesToggleButton = new JToggleButton();
        separator1 = new javax.swing.JToolBar.Separator();
        separator2 = new javax.swing.JToolBar.Separator();

        saveFileButton.setFocusable(false);
        saveFileButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/exbin/bined/intellij/resources/icons/document-save.png")));
        saveFileButton.setToolTipText("Save current file");
        saveFileButton.addActionListener(this::saveFileButtonActionPerformed);
        saveFileButton.setEnabled(false);
        controlToolBar.add(saveFileButton);

        controlToolBar.add(separator1);

        undoEditButton.setFocusable(false);
        undoEditButton.setEnabled(false);
        undoEditButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/exbin/bined/intellij/resources/icons/edit-undo.png")));
        undoEditButton.setToolTipText("Undo last operation");
        undoEditButton.addActionListener(this::undoEditButtonActionPerformed);

        redoEditButton.setFocusable(false);
        redoEditButton.setEnabled(false);
        redoEditButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/exbin/bined/intellij/resources/icons/edit-redo.png")));
        redoEditButton.setToolTipText("Redo last undid operation");
        redoEditButton.addActionListener(this::redoEditButtonActionPerformed);

        showUnprintablesToggleButton.setFocusable(false);
        showUnprintablesToggleButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/exbin/bined/intellij/resources/icons/insert-pilcrow_disabled.png"))); // NOI18N
        showUnprintablesToggleButton.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/org/exbin/bined/intellij/resources/icons/insert-pilcrow.png"))); // NOI18N
        showUnprintablesToggleButton.setToolTipText(resourceBundle.getString("showUnprintablesToggleButton.toolTipText")); // NOI18N
        showUnprintablesToggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showUnprintablesToggleButtonActionPerformed(evt);
            }
        });
        controlToolBar.add(showUnprintablesToggleButton);
        controlToolBar.add(separator1);
    }// </editor-fold>//GEN-END:initComponents

    private void showUnprintablesToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showUnprintablesToggleButtonActionPerformed
        codeArea.setShowUnprintables(showUnprintablesToggleButton.isSelected());
    }//GEN-LAST:event_showUnprintablesToggleButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToolBar.Separator separator1;
    private javax.swing.JToolBar.Separator separator2;
    private JToggleButton showUnprintablesToggleButton;
    private javax.swing.JButton saveFileButton;
    private javax.swing.JButton undoEditButton;
    private javax.swing.JButton redoEditButton;
    // End of variables declaration//GEN-END:variables

    private void undoEditButtonActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            undoHandler.performUndo();
            codeArea.repaint();
            updateUndoState();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void redoEditButtonActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            undoHandler.performRedo();
            codeArea.repaint();
            updateUndoState();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveFileButtonActionPerformed(java.awt.event.ActionEvent evt) {
        if (saveAction != null) saveAction.actionPerformed(evt);
    }
}
