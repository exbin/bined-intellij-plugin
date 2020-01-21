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

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.impl.ActionToolbarImpl;
import org.exbin.bined.CodeType;
import org.exbin.bined.operation.undo.BinaryDataUndoHandler;
import org.exbin.bined.swing.extended.ExtCodeArea;
import org.exbin.framework.bined.preferences.BinaryEditorPreferences;
import org.exbin.framework.gui.menu.component.DropDownButton;
import org.exbin.framework.gui.utils.LanguageUtils;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Binary editor toolbar panel.
 *
 * @version 0.2.2 2020/01/21
 * @author ExBin Project (http://exbin.org)
 */
@ParametersAreNonnullByDefault
public class BinEdToolbarPanel extends javax.swing.JPanel {

    private final java.util.ResourceBundle resourceBundle = LanguageUtils.getResourceBundleByClass(BinEdToolbarPanel.class);

    private final BinaryEditorPreferences preferences;
    private final ExtCodeArea codeArea;
    private BinaryDataUndoHandler undoHandler;
    private final ActionToolbarImpl toolbar;
    final DefaultActionGroup actionGroup;

    private JComponent controlToolBar;
    private ActionListener saveAction = null;
    private final AnAction cycleCodeTypesAction;
    private final JRadioButtonMenuItem binaryCodeTypeAction;
    private final JRadioButtonMenuItem octalCodeTypeAction;
    private final JRadioButtonMenuItem decimalCodeTypeAction;
    private final JRadioButtonMenuItem hexadecimalCodeTypeAction;
    private final ButtonGroup codeTypeButtonGroup;
    private DropDownButton codeTypeDropDown;

    public BinEdToolbarPanel(BinaryEditorPreferences preferences, ExtCodeArea codeArea) {
        this.preferences = preferences;
        this.codeArea = codeArea;

        setLayout(new java.awt.BorderLayout());
        actionGroup = new DefaultActionGroup();
        toolbar = (ActionToolbarImpl) ActionManager.getInstance().createActionToolbar(ActionPlaces.UNKNOWN, actionGroup, true);
        add(toolbar, BorderLayout.CENTER);

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
        cycleCodeTypesAction = new AnAction(
                "Cycle thru code types",
                null,
                null
        ) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
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
// TODO        cycleCodeTypesAction.putValue(Action.SHORT_DESCRIPTION, "Cycle thru code types");
        JPopupMenu cycleCodeTypesPopupMenu = new JPopupMenu();
        cycleCodeTypesPopupMenu.add(binaryCodeTypeAction);
        cycleCodeTypesPopupMenu.add(octalCodeTypeAction);
        cycleCodeTypesPopupMenu.add(decimalCodeTypeAction);
        cycleCodeTypesPopupMenu.add(hexadecimalCodeTypeAction);
// TODO        codeTypeDropDown = new DropDownButton(cycleCodeTypesAction, cycleCodeTypesPopupMenu);
        updateCycleButtonName();
//        controlToolBar.add(codeTypeDropDown);
    }

    public void setUndoHandler(BinaryDataUndoHandler undoHandler) {
        this.undoHandler = undoHandler;

        toolbar.getPresentation(undoEditButton).setVisible(true);
        toolbar.getPresentation(redoEditButton).setVisible(true);
    }

    private void updateCycleButtonName() {
        CodeType codeType = codeArea.getCodeType();
        // TODO codeTypeDropDown.setActionText(codeType.name().substring(0, 3));
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
        setActionSelection(showUnprintablesToggleButton, codeArea.isShowUnprintables());
    }

    public void loadFromPreferences() {
        codeArea.setCodeType(preferences.getCodeAreaPreferences().getCodeType());
        updateCycleButtonName();
        setActionSelection(showUnprintablesToggleButton, preferences.getCodeAreaPreferences().isShowUnprintables());
    }

    public void updateUndoState() {
        toolbar.getPresentation(undoEditButton).setEnabled(undoHandler.canUndo());
        toolbar.getPresentation(redoEditButton).setEnabled(undoHandler.canRedo());
    }

    public void updateModified(boolean modified) {
        toolbar.getPresentation(saveFileButton).setEnabled(saveAction != null && modified);
        updateUndoState();
    }

    public void setSaveAction(ActionListener saveAction) {
        this.saveAction = saveAction;
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));

        controlToolBar = toolbar.getComponent();

        separator1 = new javax.swing.JToolBar.Separator();
        separator2 = new javax.swing.JToolBar.Separator();

        saveFileButton = new AnAction(
                "Save current file",
                null,
                new javax.swing.ImageIcon(getClass().getResource("/org/exbin/bined/intellij/resources/icons/document-save.png"))
        ) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
//                if (saveAction != null) saveAction.actionPerformed(AnActionEvent.anActionEvent.evt);
            }
        };
        actionGroup.add(saveFileButton);
        toolbar.getPresentation(saveFileButton).setEnabled(false);
        actionGroup.addSeparator();

        undoEditButton = new AnAction(
                "Undo last operation",
                null,
                new javax.swing.ImageIcon(getClass().getResource("/org/exbin/bined/intellij/resources/icons/edit-undo.png"))
        ) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
                undoEditButtonActionPerformed();
            }
        };
        actionGroup.add(undoEditButton);
        toolbar.getPresentation(undoEditButton).setEnabled(false);
        toolbar.getPresentation(undoEditButton).setVisible(false);

        redoEditButton = new AnAction(
                "Redo last undid operation",
                null,
                new javax.swing.ImageIcon(getClass().getResource("/org/exbin/bined/intellij/resources/icons/edit-redo.png"))
        ) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
                redoEditButtonActionPerformed();
            }
        };
        actionGroup.add(redoEditButton);
        toolbar.getPresentation(redoEditButton).setEnabled(false);
        toolbar.getPresentation(redoEditButton).setVisible(false);

        showUnprintablesToggleButton = new AnAction(
                resourceBundle.getString("showUnprintablesToggleButton.toolTipText"),
                null,
                new javax.swing.ImageIcon(getClass().getResource("/org/exbin/bined/intellij/resources/icons/insert-pilcrow_disabled.png"))
        ) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
                showUnprintablesToggleButtonActionPerformed(null);
            }
        };
        actionGroup.add(showUnprintablesToggleButton);
        toolbar.getPresentation(showUnprintablesToggleButton).setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/org/exbin/bined/intellij/resources/icons/insert-pilcrow.png")));

        actionGroup.addSeparator();
    }// </editor-fold>//GEN-END:initComponents

    private void showUnprintablesToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showUnprintablesToggleButtonActionPerformed
        codeArea.setShowUnprintables((Boolean) toolbar.getPresentation(showUnprintablesToggleButton).getClientProperty("selected"));
    }//GEN-LAST:event_showUnprintablesToggleButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToolBar.Separator separator1;
    private javax.swing.JToolBar.Separator separator2;
    private AnAction showUnprintablesToggleButton;
    private AnAction saveFileButton;
    private AnAction undoEditButton;
    private AnAction redoEditButton;
    // End of variables declaration//GEN-END:variables

    private void undoEditButtonActionPerformed() {
        try {
            undoHandler.performUndo();
            codeArea.repaint();
            updateUndoState();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void redoEditButtonActionPerformed() {
        try {
            undoHandler.performRedo();
            codeArea.repaint();
            updateUndoState();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setActionSelection(AnAction action, boolean selected) {
        toolbar.getPresentation(action).putClientProperty("selected", selected);
    }
}
