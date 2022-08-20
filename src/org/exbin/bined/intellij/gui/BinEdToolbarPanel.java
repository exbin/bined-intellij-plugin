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
package org.exbin.bined.intellij.gui;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.impl.ActionToolbarImpl;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.Key;
import com.intellij.ui.components.JBPanel;
import org.exbin.bined.CodeType;
import org.exbin.bined.operation.undo.BinaryDataUndoHandler;
import org.exbin.bined.swing.extended.ExtCodeArea;
import org.exbin.framework.bined.preferences.BinaryEditorPreferences;
import org.exbin.framework.utils.LanguageUtils;
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
 * @version 0.2.5 2021/08/17
 */
@ParametersAreNonnullByDefault
public class BinEdToolbarPanel extends JBPanel {

    private final java.util.ResourceBundle resourceBundle = LanguageUtils.getResourceBundleByClass(BinEdToolbarPanel.class);
    private static final String TOOLBAR_PLACE = "BinEdPluginMainToolbar";
    private static final Key<Boolean> SELECTED_PROPERTY_KEY = Key.create(Toggleable.SELECTED_PROPERTY);

    private final BinaryEditorPreferences preferences;
    private final ExtCodeArea codeArea;
    private final AnAction optionsAction;
    private final AnAction onlineHelpAction;
    private BinaryDataUndoHandler undoHandler;

    private final DefaultActionGroup actionGroup;
    private final ActionGroup cycleActionGroup;
    private final ActionToolbarImpl toolbar;

    private ActionListener saveAction = null;
    private final CodeTypeSplitAction cycleCodeTypesSplitAction;
    private final AnAction binaryCodeTypeAction;
    private final AnAction octalCodeTypeAction;
    private final AnAction decimalCodeTypeAction;
    private final AnAction hexadecimalCodeTypeAction;
    private boolean modified = false;

    public BinEdToolbarPanel(BinaryEditorPreferences preferences, ExtCodeArea codeArea, AnAction optionsAction, AnAction onlineHelpAction) {
        this.preferences = preferences;
        this.codeArea = codeArea;
        this.optionsAction = optionsAction;
        this.onlineHelpAction = onlineHelpAction;

        setLayout(new java.awt.BorderLayout());
        actionGroup = new DefaultActionGroup();
        toolbar = (ActionToolbarImpl) ActionManager.getInstance().createActionToolbar(TOOLBAR_PLACE, actionGroup, true);
        toolbar.setTargetComponent(codeArea);
        add(toolbar, BorderLayout.CENTER);

        binaryCodeTypeAction = new AnAction(
                "Binary",
                null,
                load("/org/exbin/bined/intellij/resources/icons/codetype-bin.png")
        ) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
                codeArea.setCodeType(CodeType.BINARY);
                updateCycleButtonState();
            }
        };

        octalCodeTypeAction = new AnAction(
                "Octal",
                null,
                load("/org/exbin/bined/intellij/resources/icons/codetype-oct.png")
        ) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
                codeArea.setCodeType(CodeType.OCTAL);
                updateCycleButtonState();
            }
        };
        decimalCodeTypeAction = new AnAction(
                "Decimal",
                null,
                load("/org/exbin/bined/intellij/resources/icons/codetype-dec.png")
        ) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
                codeArea.setCodeType(CodeType.DECIMAL);
                updateCycleButtonState();
            }
        };
        hexadecimalCodeTypeAction = new AnAction(
                "Hexadecimal",
                null,
                load("/org/exbin/bined/intellij/resources/icons/codetype-hex.png")
        ) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
                codeArea.setCodeType(CodeType.HEXADECIMAL);
                updateCycleButtonState();
            }
        };

        cycleActionGroup = new ActionGroup("Cycle thru code types", false) {
            @NotNull
            @Override
            public AnAction[] getChildren(@Nullable AnActionEvent anActionEvent) {
                return new AnAction[]{binaryCodeTypeAction, octalCodeTypeAction, decimalCodeTypeAction, hexadecimalCodeTypeAction};
            }
        };
        cycleCodeTypesSplitAction = new CodeTypeSplitAction(cycleActionGroup);

        initComponents();
        init();
    }

    private void init() {
// TODO        cycleCodeTypesAction.putValue(Action.SHORT_DESCRIPTION, "Cycle thru code types");
//        JPopupMenu cycleCodeTypesPopupMenu = new JPopupMenu();
//        cycleCodeTypesPopupMenu.add(binaryCodeTypeAction);
//        cycleCodeTypesPopupMenu.add(octalCodeTypeAction);
//        cycleCodeTypesPopupMenu.add(decimalCodeTypeAction);
//        cycleCodeTypesPopupMenu.add(hexadecimalCodeTypeAction);
// TODO        codeTypeDropDown = new DropDownButton(cycleCodeTypesAction, cycleCodeTypesPopupMenu);
//        updateCycleButtonName();
//        controlToolBar.add(codeTypeDropDown);
    }

    public void setUndoHandler(BinaryDataUndoHandler undoHandler) {
        this.undoHandler = undoHandler;

        setActionVisible(undoEditButton, true);
        setActionVisible(redoEditButton, true);
    }

    private void updateCycleButtonState() {
        CodeType codeType = codeArea.getCodeType();

        switch (codeType) {
            case BINARY: {
                cycleCodeTypesSplitAction.setSelectedIndex(0);
                break;
            }
            case OCTAL: {
                cycleCodeTypesSplitAction.setSelectedIndex(1);
                break;
            }
            case DECIMAL: {
                cycleCodeTypesSplitAction.setSelectedIndex(2);
                break;
            }
            case HEXADECIMAL: {
                cycleCodeTypesSplitAction.setSelectedIndex(3);
                break;
            }
        }
    }

//    private void changeCycleAction(AnAction action) {
//        DataManager instance = DataManager.getInstance();
//        DataContext context = instance != null ? instance.getDataContext((Component) cycleActionButton) : DataContext.EMPTY_CONTEXT;
//        AnActionEvent event = AnActionEvent.createFromAnAction(binaryCodeTypeAction, null, ActionPlaces.UNKNOWN, context);
//        toolbar.getPresentation(cycleCodeTypesAction).setIcon(toolbar.getPresentation(action).getIcon());
//        cycleActionButton.afterActionPerformed(cycleCodeTypesSplitAction, context, event);
//        cycleActionButton.afterActionPerformed(cycleCodeTypesAction, context, event);
//    }

    public void applyFromCodeArea() {
        updateCycleButtonState();
        updateUnprintables();
    }

    public void loadFromPreferences() {
        codeArea.setCodeType(preferences.getCodeAreaPreferences().getCodeType());
        updateCycleButtonState();
        updateUnprintables();
    }

    public void updateUndoState() {
        toolbar.getPresentation(undoEditButton).setEnabled(undoHandler != null && undoHandler.canUndo());
        toolbar.getPresentation(redoEditButton).setEnabled(undoHandler != null && undoHandler.canRedo());
    }

    public void updateUnprintables() {
        boolean showUnprintables = codeArea.isShowUnprintables();
        setActionSelection(showUnprintablesToggleButton, showUnprintables);
    }

    public void updateModified(boolean modified) {
        this.modified = modified;
        toolbar.getPresentation(saveFileButton).setEnabled(modified);
        updateUndoState();
    }

    public void setSaveAction(ActionListener saveAction) {
        this.saveAction = saveAction;
        setActionVisible(saveFileButton, true);
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));

        saveFileButton = new AnAction(
                "Save current file",
                null,
                new javax.swing.ImageIcon(getClass().getResource("/org/exbin/bined/intellij/resources/icons/document-save.png"))
        ) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
                if (saveAction != null) saveAction.actionPerformed(new ActionEvent(BinEdToolbarPanel.this, 0, ""));
            }

            @Override
            public void update(@NotNull AnActionEvent e) {
                super.update(e);
                Presentation presentation = e.getPresentation();
                presentation.setVisible(saveAction != null);
                presentation.setEnabled(modified);
            }
        };
        actionGroup.addAction(saveFileButton);
        setActionVisible(saveFileButton, false);
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

            @Override
            public void update(@NotNull AnActionEvent e) {
                super.update(e);
                Presentation presentation = e.getPresentation();
                presentation.setVisible(undoHandler != null);
                presentation.setEnabled(undoHandler != null && undoHandler.canUndo());
            }
        };
        actionGroup.addAction(undoEditButton);
        setActionVisible(undoEditButton, false);

        redoEditButton = new AnAction(
                "Redo last undid operation",
                null,
                new javax.swing.ImageIcon(getClass().getResource("/org/exbin/bined/intellij/resources/icons/edit-redo.png"))
        ) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
                redoEditButtonActionPerformed();
            }

            @Override
            public void update(@NotNull AnActionEvent e) {
                super.update(e);
                Presentation presentation = e.getPresentation();
                presentation.setVisible(undoHandler != null);
                presentation.setEnabled(undoHandler != null && undoHandler.canRedo());
            }
        };
        actionGroup.addAction(redoEditButton);
        setActionVisible(redoEditButton, false);

        showUnprintablesToggleButton = new ToggleAction(
                resourceBundle.getString("showUnprintablesToggleButton.toolTipText"),
                null,
                new javax.swing.ImageIcon(getClass().getResource("/org/exbin/bined/intellij/resources/icons/insert-pilcrow.png"))
        ) {
            @Override
            public boolean isSelected(@NotNull AnActionEvent anActionEvent) {
                return codeArea.isShowUnprintables();
            }

            @Override
            public void setSelected(@NotNull AnActionEvent anActionEvent, boolean selected) {
                codeArea.setShowUnprintables(selected);
                updateUnprintables();
            }
        };
        actionGroup.addAction(showUnprintablesToggleButton);

        actionGroup.addAction(cycleCodeTypesSplitAction);
        updateCycleButtonState();

        actionGroup.addSeparator();
        AnAction settingsAction = new AnAction(
                "Options",
                null,
                new javax.swing.ImageIcon(getClass().getResource("/org/exbin/framework/options/gui/resources/icons/Preferences16.gif"))
        ) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
                optionsAction.actionPerformed(anActionEvent);
            }
        };
        actionGroup.addAction(settingsAction);

        AnAction onlineHelpToolbarAction = new AnAction(
                "Online Help",
                null,
                new javax.swing.ImageIcon(getClass().getResource("/org/exbin/framework/bined/resources/icons/open_icon_library/icons/png/16x16/actions/help.png"))
        ) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
                onlineHelpAction.actionPerformed(anActionEvent);
            }
        };
        actionGroup.addAction(onlineHelpToolbarAction);
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

    private void setActionVisible(AnAction action, boolean enabled) {
        toolbar.getPresentation(action).setVisible(enabled);
    }

    private void setActionSelection(AnAction action, boolean selected) {
        toolbar.getPresentation(action).putClientProperty(SELECTED_PROPERTY_KEY, selected);
    }

    private Icon load(String path) {
        return IconLoader.getIcon(path, getClass());
    }
}
