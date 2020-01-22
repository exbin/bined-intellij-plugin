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

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.impl.ActionToolbarImpl;
import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.IconManager;
import org.exbin.bined.CodeType;
import org.exbin.bined.operation.undo.BinaryDataUndoHandler;
import org.exbin.bined.swing.extended.ExtCodeArea;
import org.exbin.framework.bined.preferences.BinaryEditorPreferences;
import org.exbin.framework.gui.menu.component.DropDownButton;
import org.exbin.framework.gui.utils.LanguageUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Binary editor toolbar panel.
 *
 * @author ExBin Project (http://exbin.org)
 * @version 0.2.2 2020/01/22
 */
@ParametersAreNonnullByDefault
public class BinEdToolbarPanel extends javax.swing.JPanel {

    private final java.util.ResourceBundle resourceBundle = LanguageUtils.getResourceBundleByClass(BinEdToolbarPanel.class);

    private final BinaryEditorPreferences preferences;
    private final ExtCodeArea codeArea;
    private final AnAction optionsAction;
    private BinaryDataUndoHandler undoHandler;
    private final ActionToolbarImpl toolbar;
    final DefaultActionGroup actionGroup;

    private JComponent controlToolBar;
    private ActionListener saveAction = null;
    private final SplitButtonAction cycleCodeTypesAction;
    private final AnAction binaryCodeTypeAction;
    private final AnAction octalCodeTypeAction;
    private final AnAction decimalCodeTypeAction;
    private final AnAction hexadecimalCodeTypeAction;
    private final ButtonGroup codeTypeButtonGroup;
//    private DropDownButton codeTypeDropDown;

    public BinEdToolbarPanel(BinaryEditorPreferences preferences, ExtCodeArea codeArea, AnAction optionsAction) {
        this.preferences = preferences;
        this.codeArea = codeArea;
        this.optionsAction = optionsAction;

        setLayout(new java.awt.BorderLayout());
        actionGroup = new DefaultActionGroup();
        toolbar = (ActionToolbarImpl) ActionManager.getInstance().createActionToolbar(ActionPlaces.UNKNOWN, actionGroup, true);
        add(toolbar, BorderLayout.CENTER);

        codeTypeButtonGroup = new ButtonGroup();
        binaryCodeTypeAction = new AnAction(
                "Binary",
                null,
                load("/org/exbin/bined/intellij/resources/icons/codetype-bin.svg")
        ) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
                codeArea.setCodeType(CodeType.BINARY);
                updateCycleButtonName();
            }
        };

//        codeTypeButtonGroup.add(binaryCodeTypeAction);
        octalCodeTypeAction = new AnAction(
                "Octal",
                null,
                load("/org/exbin/bined/intellij/resources/icons/codetype-oct.svg")
        ) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
                codeArea.setCodeType(CodeType.OCTAL);
                updateCycleButtonName();
            }
        };
        decimalCodeTypeAction = new AnAction(
                "Decimal",
                null,
                load("/org/exbin/bined/intellij/resources/icons/codetype-dec.svg")
        ) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
                codeArea.setCodeType(CodeType.DECIMAL);
                updateCycleButtonName();
            }
        };
        hexadecimalCodeTypeAction = new AnAction(
                "Hexadecimal",
                null,
                load("/org/exbin/bined/intellij/resources/icons/codetype-hex.svg")
        ) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
                codeArea.setCodeType(CodeType.HEXADECIMAL);
                updateCycleButtonName();
            }
        };
        cycleCodeTypesAction = new SplitButtonAction(new ActionGroup("Cycle thru code types", false) {
            @NotNull
            @Override
            public AnAction[] getChildren(@Nullable AnActionEvent anActionEvent) {
                return new AnAction[]{binaryCodeTypeAction, octalCodeTypeAction, decimalCodeTypeAction, hexadecimalCodeTypeAction};
            }
        }) {
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
//        cycleCodeTypesPopupMenu.add(binaryCodeTypeAction);
//        cycleCodeTypesPopupMenu.add(octalCodeTypeAction);
//        cycleCodeTypesPopupMenu.add(decimalCodeTypeAction);
//        cycleCodeTypesPopupMenu.add(hexadecimalCodeTypeAction);
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
//        switch (codeType) {
//            case BINARY: {
//                if (!binaryCodeTypeAction.isSelected()) {
//                    binaryCodeTypeAction.setSelected(true);
//                }
//                break;
//            }
//            case OCTAL: {
//                if (!octalCodeTypeAction.isSelected()) {
//                    octalCodeTypeAction.setSelected(true);
//                }
//                break;
//            }
//            case DECIMAL: {
//                if (!decimalCodeTypeAction.isSelected()) {
//                    decimalCodeTypeAction.setSelected(true);
//                }
//                break;
//            }
//            case HEXADECIMAL: {
//                if (!hexadecimalCodeTypeAction.isSelected()) {
//                    hexadecimalCodeTypeAction.setSelected(true);
//                }
//                break;
//            }
//        }
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

        saveFileButton = new AnAction(
                "Save current file",
                null,
                new javax.swing.ImageIcon(getClass().getResource("/org/exbin/bined/intellij/resources/icons/document-save.png"))
        ) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
                if (saveAction != null) saveAction.actionPerformed(new ActionEvent(BinEdToolbarPanel.this, 0, ""));
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

        showUnprintablesToggleButton = new ToggleAction(
                resourceBundle.getString("showUnprintablesToggleButton.toolTipText"),
                null,
                new javax.swing.ImageIcon(getClass().getResource("/org/exbin/bined/intellij/resources/icons/insert-pilcrow_disabled.png"))
        ) {
            @Override
            public boolean isSelected(@NotNull AnActionEvent anActionEvent) {
                return codeArea.isShowUnprintables();
            }

            @Override
            public void setSelected(@NotNull AnActionEvent anActionEvent, boolean selected) {
                codeArea.setShowUnprintables(selected);
                toolbar.getPresentation(showUnprintablesToggleButton).setIcon(
                        selected ?
                                new javax.swing.ImageIcon(getClass().getResource("/org/exbin/bined/intellij/resources/icons/insert-pilcrow.png"))
                                : new javax.swing.ImageIcon(getClass().getResource("/org/exbin/bined/intellij/resources/icons/insert-pilcrow_disabled.png"))
                );
            }
        };
        actionGroup.add(showUnprintablesToggleButton);
        actionGroup.add(cycleCodeTypesAction);

        actionGroup.addSeparator();
        AnAction settingsAction = new AnAction(
                "Options",
                null,
                new javax.swing.ImageIcon(getClass().getResource("/org/exbin/framework/gui/options/resources/icons/Preferences16.gif"))
        ) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
                optionsAction.actionPerformed(anActionEvent);
            }
        };
        actionGroup.add(settingsAction);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private ToggleAction showUnprintablesToggleButton;
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

    private static Icon load(String path) {
        return IconLoader.getIcon(path);
    }
}
