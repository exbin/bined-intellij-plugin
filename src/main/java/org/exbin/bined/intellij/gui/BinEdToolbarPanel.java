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
package org.exbin.bined.intellij.gui;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.actionSystem.Toggleable;
import com.intellij.openapi.actionSystem.impl.ActionToolbarImpl;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.Key;
import com.intellij.ui.components.JBPanel;
import org.exbin.bined.CodeType;
import org.exbin.bined.intellij.action.CodeTypeSplitAction;
import org.exbin.bined.operation.undo.BinaryDataUndoHandler;
import org.exbin.framework.bined.preferences.BinaryEditorPreferences;
import org.exbin.framework.utils.LanguageUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JComponent;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Binary editor toolbar panel.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class BinEdToolbarPanel extends JBPanel {

    private final java.util.ResourceBundle resourceBundle = LanguageUtils.getResourceBundleByBundleName("org/exbin/framework/bined/resources/BinedModule");
    private final java.util.ResourceBundle fileResourceBundle = LanguageUtils.getResourceBundleByBundleName("org/exbin/framework/file/resources/FileModule");
    private final java.util.ResourceBundle optionsResourceBundle = LanguageUtils.getResourceBundleByBundleName("org/exbin/framework/options/resources/OptionsModule");
    private final java.util.ResourceBundle onlineHelpResourceBundle = LanguageUtils.getResourceBundleByBundleName("org/exbin/framework/help/online/action/resources/OnlineHelpAction");
    private final java.util.ResourceBundle operationUndoResourceBundle = LanguageUtils.getResourceBundleByBundleName("org/exbin/framework/operation/undo/resources/OperationUndoModule");

    private static final String TOOLBAR_PLACE = "BinEdPluginMainToolbar";
    private static final Key<Boolean> SELECTED_PROPERTY_KEY = Key.create(Toggleable.SELECTED_PROPERTY);

    private final Control codeAreaControl;
    private AbstractAction optionsAction;
    private AnAction onlineHelpAction;
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

    public BinEdToolbarPanel(JComponent targetComponent, Control codeAreaControl) {
        this.codeAreaControl = codeAreaControl;

        setLayout(new java.awt.BorderLayout());
        actionGroup = new DefaultActionGroup();
        toolbar = (ActionToolbarImpl) ActionManager.getInstance().createActionToolbar(TOOLBAR_PLACE, actionGroup, true);
        toolbar.setTargetComponent(targetComponent);
        add(toolbar, BorderLayout.CENTER);

        binaryCodeTypeAction = new AnAction(
                resourceBundle.getString("binaryCodeTypeAction.text"),
                resourceBundle.getString("binaryCodeTypeAction.shortDescription"),
                load("/org/exbin/bined/intellij/resources/icons/codetype-bin.png")
        ) {
            @NotNull
            @Override
            public ActionUpdateThread getActionUpdateThread() {
                return ActionUpdateThread.BGT;
            }

            @Override
            public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
                codeAreaControl.setCodeType(CodeType.BINARY);
                updateCycleButtonState();
            }
        };

        octalCodeTypeAction = new AnAction(
                resourceBundle.getString("octalCodeTypeAction.text"),
                resourceBundle.getString("octalCodeTypeAction.shortDescription"),
                load("/org/exbin/bined/intellij/resources/icons/codetype-oct.png")
        ) {
            @NotNull
            @Override
            public ActionUpdateThread getActionUpdateThread() {
                return ActionUpdateThread.BGT;
            }

            @Override
            public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
                codeAreaControl.setCodeType(CodeType.OCTAL);
                updateCycleButtonState();
            }
        };
        decimalCodeTypeAction = new AnAction(
                resourceBundle.getString("decimalCodeTypeAction.text"),
                resourceBundle.getString("decimalCodeTypeAction.shortDescription"),
                load("/org/exbin/bined/intellij/resources/icons/codetype-dec.png")
        ) {
            @NotNull
            @Override
            public ActionUpdateThread getActionUpdateThread() {
                return ActionUpdateThread.BGT;
            }

            @Override
            public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
                codeAreaControl.setCodeType(CodeType.DECIMAL);
                updateCycleButtonState();
            }
        };
        hexadecimalCodeTypeAction = new AnAction(
                resourceBundle.getString("hexadecimalCodeTypeAction.text"),
                resourceBundle.getString("hexadecimalCodeTypeAction.shortDescription"),
                load("/org/exbin/bined/intellij/resources/icons/codetype-hex.png")
        ) {
            @NotNull
            @Override
            public ActionUpdateThread getActionUpdateThread() {
                return ActionUpdateThread.BGT;
            }

            @Override
            public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
                codeAreaControl.setCodeType(CodeType.HEXADECIMAL);
                updateCycleButtonState();
            }
        };

        cycleActionGroup = new ActionGroup(resourceBundle.getString("cycleCodeTypesAction.shortDescription"), false) {
            @NotNull
            @Override
            public AnAction[] getChildren(@Nullable AnActionEvent anActionEvent) {
                return new AnAction[]{binaryCodeTypeAction, octalCodeTypeAction, decimalCodeTypeAction, hexadecimalCodeTypeAction};
            }
        };
        cycleCodeTypesSplitAction = new CodeTypeSplitAction(cycleActionGroup);

        initComponents();
    }

    public void setUndoHandler(BinaryDataUndoHandler undoHandler) {
        this.undoHandler = undoHandler;

        setActionVisible(undoEditButton, true);
        setActionVisible(redoEditButton, true);
    }

    public void setOptionsAction(AbstractAction optionsAction) {
        this.optionsAction = optionsAction;
    }

    public void setOnlineHelpAction(AnAction onlineHelpAction) {
        this.onlineHelpAction = onlineHelpAction;
    }

    private void updateCycleButtonState() {
        CodeType codeType = codeAreaControl.getCodeType();

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

    public void applyFromCodeArea() {
        updateCycleButtonState();
        updateUnprintables();
    }

    public void loadFromPreferences(BinaryEditorPreferences preferences) {
        codeAreaControl.setCodeType(preferences.getCodeAreaPreferences().getCodeType());
        updateCycleButtonState();
        updateUnprintables();
    }

    public void updateUndoState() {
        toolbar.getPresentation(undoEditButton).setEnabled(undoHandler != null && undoHandler.canUndo());
        toolbar.getPresentation(redoEditButton).setEnabled(undoHandler != null && undoHandler.canRedo());
        if (saveAction != null) {
            boolean modified = undoHandler != null && undoHandler.getCommandPosition() != undoHandler.getSyncPoint();
            toolbar.getPresentation(saveFileButton).setEnabled(modified);
        }
    }

    public void updateUnprintables() {
        boolean showUnprintables = codeAreaControl.isShowUnprintables();
        setActionSelection(showUnprintablesToggleButton, showUnprintables);
    }

    public void setSaveAction(ActionListener saveAction) {
        this.saveAction = saveAction;
        setActionVisible(saveFileButton, true);
        updateUndoState();
    }

    public void saveFile() {
        if (saveAction != null) {
            saveAction.actionPerformed(new ActionEvent(BinEdToolbarPanel.this, 0, ""));
        }
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));

        saveFileButton = new AnAction(
                fileResourceBundle.getString("saveFileAction.text"),
                fileResourceBundle.getString("saveFileAction.shortDescription"),
                new javax.swing.ImageIcon(getClass().getResource("/org/exbin/bined/intellij/resources/icons/document-save.png"))
        ) {
            @NotNull
            @Override
            public ActionUpdateThread getActionUpdateThread() {
                return ActionUpdateThread.BGT;
            }

            @Override
            public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
                if (saveAction != null) saveAction.actionPerformed(new ActionEvent(BinEdToolbarPanel.this, 0, ""));
            }

            @Override
            public void update(@NotNull AnActionEvent e) {
                Presentation presentation = e.getPresentation();
                presentation.setVisible(saveAction != null);
                boolean modified = undoHandler != null && undoHandler.getCommandPosition() != undoHandler.getSyncPoint();
                presentation.setEnabled(modified);
            }
        };
        actionGroup.addAction(saveFileButton);
        setActionVisible(saveFileButton, false);
        actionGroup.addSeparator();

        undoEditButton = new AnAction(
                operationUndoResourceBundle.getString("editUndoAction.text"),
                operationUndoResourceBundle.getString("editUndoAction.shortDescription"),
                new javax.swing.ImageIcon(getClass().getResource("/org/exbin/bined/intellij/resources/icons/edit-undo.png"))
        ) {
            @NotNull
            @Override
            public ActionUpdateThread getActionUpdateThread() {
                return ActionUpdateThread.BGT;
            }

            @Override
            public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
                undoEditButtonActionPerformed();
            }

            @Override
            public void update(@NotNull AnActionEvent e) {
                Presentation presentation = e.getPresentation();
                presentation.setVisible(undoHandler != null);
                presentation.setEnabled(undoHandler != null && undoHandler.canUndo());
            }
        };
        actionGroup.addAction(undoEditButton);
        setActionVisible(undoEditButton, false);

        redoEditButton = new AnAction(
                operationUndoResourceBundle.getString("editRedoAction.text"),
                operationUndoResourceBundle.getString("editRedoAction.shortDescription"),
                new javax.swing.ImageIcon(getClass().getResource("/org/exbin/bined/intellij/resources/icons/edit-redo.png"))
        ) {
            @NotNull
            @Override
            public ActionUpdateThread getActionUpdateThread() {
                return ActionUpdateThread.BGT;
            }

            @Override
            public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
                redoEditButtonActionPerformed();
            }

            @Override
            public void update(@NotNull AnActionEvent e) {
                Presentation presentation = e.getPresentation();
                presentation.setVisible(undoHandler != null);
                presentation.setEnabled(undoHandler != null && undoHandler.canRedo());
            }
        };
        actionGroup.addAction(redoEditButton);
        setActionVisible(redoEditButton, false);

        showUnprintablesToggleButton = new ToggleAction(
                resourceBundle.getString("viewUnprintablesAction.text"),
                resourceBundle.getString("viewUnprintablesAction.shortDescription"),
                new javax.swing.ImageIcon(getClass().getResource("/org/exbin/bined/intellij/resources/icons/insert-pilcrow.png"))
        ) {
            @NotNull
            @Override
            public ActionUpdateThread getActionUpdateThread() {
                return ActionUpdateThread.BGT;
            }

            @Override
            public boolean isSelected(@NotNull AnActionEvent anActionEvent) {
                return codeAreaControl.isShowUnprintables();
            }

            @Override
            public void setSelected(@NotNull AnActionEvent anActionEvent, boolean selected) {
                codeAreaControl.setShowUnprintables(selected);
                updateUnprintables();
            }
        };
        actionGroup.addAction(showUnprintablesToggleButton);

        actionGroup.addAction(cycleCodeTypesSplitAction);
        updateCycleButtonState();

        actionGroup.addSeparator();
        AnAction settingsAction = new AnAction(
                optionsResourceBundle.getString("optionsAction.text"),
                optionsResourceBundle.getString("optionsAction.shortDescription"),
                new javax.swing.ImageIcon(getClass().getResource("/org/exbin/framework/options/gui/resources/icons/Preferences16.gif"))
        ) {
            @NotNull
            @Override
            public ActionUpdateThread getActionUpdateThread() {
                return ActionUpdateThread.BGT;
            }

            @Override
            public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
                optionsAction.actionPerformed(null);
            }
        };
        actionGroup.addAction(settingsAction);

        AnAction onlineHelpToolbarAction = new AnAction(
                onlineHelpResourceBundle.getString("onlineHelpAction.text"),
                onlineHelpResourceBundle.getString("onlineHelpAction.shortDescription"),
                new javax.swing.ImageIcon(getClass().getResource("/org/exbin/framework/bined/resources/icons/open_icon_library/icons/png/16x16/actions/help.png"))
        ) {
            @NotNull
            @Override
            public ActionUpdateThread getActionUpdateThread() {
                return ActionUpdateThread.BGT;
            }

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
            codeAreaControl.repaint();
            updateUndoState();
        } catch (Exception ex) {
            Logger.getLogger(BinEdToolbarPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void redoEditButtonActionPerformed() {
        try {
            undoHandler.performRedo();
            codeAreaControl.repaint();
            updateUndoState();
        } catch (Exception ex) {
            Logger.getLogger(BinEdToolbarPanel.class.getName()).log(Level.SEVERE, null, ex);
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

    @ParametersAreNonnullByDefault
    public interface Control {

        @NotNull
        CodeType getCodeType();

        void setCodeType(CodeType codeType);

        boolean isShowUnprintables();

        void setShowUnprintables(boolean showUnprintables);

        void repaint();
    }
}
