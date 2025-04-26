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
import com.intellij.openapi.actionSystem.ActionToolbar;
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
import org.exbin.bined.operation.undo.BinaryDataUndoRedo;
import org.exbin.framework.App;
import org.exbin.framework.bined.options.BinaryEditorOptions;
import org.exbin.framework.language.api.LanguageModuleApi;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
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

    private final java.util.ResourceBundle resourceBundle;
    private final java.util.ResourceBundle fileResourceBundle;
    private final java.util.ResourceBundle optionsResourceBundle;
    private final java.util.ResourceBundle onlineHelpResourceBundle;
    private final java.util.ResourceBundle operationUndoResourceBundle;

    private static final String TOOLBAR_PLACE = "BinEdPluginMainToolbar";
    private static final Key<Boolean> SELECTED_PROPERTY_KEY = Key.create(Toggleable.SELECTED_PROPERTY);

    private Control codeAreaControl;
    private ActionListener optionsAction;
    private ActionListener onlineHelpAction;
    private BinaryDataUndoRedo undoRedo;

    private final DefaultActionGroup actionGroup;
    private final ActionGroup cycleActionGroup;
    private final ActionToolbar toolbar;

    private ActionListener saveAction = null;
    private final CodeTypeSplitAction cycleCodeTypesSplitAction;
    private final AnAction settingsAction;
    private final AnAction onlineHelpToolbarAction;
    private final AnAction binaryCodeTypeAction;
    private final AnAction octalCodeTypeAction;
    private final AnAction decimalCodeTypeAction;
    private final AnAction hexadecimalCodeTypeAction;

    public BinEdToolbarPanel() {
        LanguageModuleApi languageModule = App.getModule(LanguageModuleApi.class);
        resourceBundle = languageModule.getBundle(org.exbin.framework.bined.BinedModule.class);
        fileResourceBundle = languageModule.getBundle(org.exbin.framework.file.FileModule.class);
        optionsResourceBundle = languageModule.getBundle(org.exbin.framework.options.OptionsModule.class);
        onlineHelpResourceBundle = languageModule.getBundle(org.exbin.framework.help.online.action.OnlineHelpAction.class);
        operationUndoResourceBundle = languageModule.getBundle(org.exbin.framework.operation.undo.OperationUndoModule.class);

        setLayout(new java.awt.BorderLayout());
        actionGroup = new DefaultActionGroup();
        toolbar = ActionManager.getInstance().createActionToolbar(TOOLBAR_PLACE, actionGroup, true);
        add(toolbar.getComponent(), BorderLayout.CENTER);

        settingsAction = new AnAction(
                optionsResourceBundle.getString("optionsAction.text"),
                optionsResourceBundle.getString("optionsAction.shortDescription"),
                new javax.swing.ImageIcon(getClass().getResource(optionsResourceBundle.getString("optionsAction.smallIcon")))
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

        onlineHelpToolbarAction = new AnAction(
                onlineHelpResourceBundle.getString("onlineHelpAction.text"),
                onlineHelpResourceBundle.getString("onlineHelpAction.shortDescription"),
                new javax.swing.ImageIcon(getClass().getResource("/org/exbin/bined/intellij/resources/icons/help.png"))
        ) {
            @NotNull
            @Override
            public ActionUpdateThread getActionUpdateThread() {
                return ActionUpdateThread.BGT;
            }

            @Override
            public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
                onlineHelpAction.actionPerformed(null);
            }
        };

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

    public void setTargetComponent(JComponent targetComponent) {
        toolbar.setTargetComponent(targetComponent);
    }

    public void setCodeAreaControl(Control codeAreaControl) {
        this.codeAreaControl = codeAreaControl;
        updateCycleButtonState();
    }

    public void setUndoHandler(BinaryDataUndoRedo undoRedo) {
        this.undoRedo = undoRedo;
        updateActionsState();
    }

    public void setOptionsAction(ActionListener optionsAction) {
        if (this.optionsAction == null) {
            actionGroup.remove(onlineHelpToolbarAction);
            actionGroup.addAction(settingsAction);
            actionGroup.addAction(onlineHelpToolbarAction);
        }
        this.optionsAction = optionsAction;
    }

    public void setOnlineHelpAction(ActionListener onlineHelpAction) {
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
        updateActionsState();
    }

    public void loadFromPreferences(BinaryEditorOptions options) {
        codeAreaControl.setCodeType(options.getCodeAreaOptions().getCodeType());
        updateCycleButtonState();
        updateActionsState();
    }

    public void updateActionsState() {
        // Is supposed to be updated automatically
        // toolbar.updateActionsImmediately();
    }

    public void setSaveAction(ActionListener saveAction) {
        this.saveAction = saveAction;
        updateActionsState();
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
                new javax.swing.ImageIcon(getClass().getResource(fileResourceBundle.getString("saveFileAction.smallIcon")))
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
                boolean modified = undoRedo != null && undoRedo.getCommandPosition() != undoRedo.getSyncPosition();
                presentation.setEnabled(modified);
            }
        };
        actionGroup.addAction(saveFileButton);
        actionGroup.addSeparator();

        undoEditButton = new AnAction(
                operationUndoResourceBundle.getString("editUndoAction.text"),
                operationUndoResourceBundle.getString("editUndoAction.shortDescription"),
                new javax.swing.ImageIcon(getClass().getResource(operationUndoResourceBundle.getString("editUndoAction.smallIcon")))
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
                presentation.setVisible(undoRedo != null);
                presentation.setEnabled(undoRedo != null && undoRedo.canUndo());
            }
        };
        actionGroup.addAction(undoEditButton);

        redoEditButton = new AnAction(
                operationUndoResourceBundle.getString("editRedoAction.text"),
                operationUndoResourceBundle.getString("editRedoAction.shortDescription"),
                new javax.swing.ImageIcon(getClass().getResource(operationUndoResourceBundle.getString("editRedoAction.smallIcon")))
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
                presentation.setVisible(undoRedo != null);
                presentation.setEnabled(undoRedo != null && undoRedo.canRedo());
            }
        };
        actionGroup.addAction(redoEditButton);

        showNonprintablesToggleButton = new ToggleAction(
                resourceBundle.getString("viewNonprintablesAction.text"),
                resourceBundle.getString("viewNonprintablesAction.shortDescription"),
                new javax.swing.ImageIcon(getClass().getResource(resourceBundle.getString("viewNonprintablesToolbarAction.smallIcon")))
        ) {
            @NotNull
            @Override
            public ActionUpdateThread getActionUpdateThread() {
                return ActionUpdateThread.BGT;
            }

            @Override
            public boolean isSelected(@NotNull AnActionEvent anActionEvent) {
                return codeAreaControl.isShowNonprintables();
            }

            @Override
            public void setSelected(@NotNull AnActionEvent anActionEvent, boolean selected) {
                codeAreaControl.setShowNonprintables(selected);
            }

            @Override
            public void update(@NotNull AnActionEvent e) {
                Presentation presentation = e.getPresentation();
                presentation.putClientProperty(SELECTED_PROPERTY_KEY, codeAreaControl.isShowNonprintables());
            }
        };
        actionGroup.addAction(showNonprintablesToggleButton);

        actionGroup.addAction(cycleCodeTypesSplitAction);

        actionGroup.addSeparator();

        actionGroup.addAction(onlineHelpToolbarAction);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private ToggleAction showNonprintablesToggleButton;
    private AnAction saveFileButton;
    private AnAction undoEditButton;
    private AnAction redoEditButton;
    // End of variables declaration//GEN-END:variables

    private void undoEditButtonActionPerformed() {
        try {
            undoRedo.performUndo();
            codeAreaControl.repaint();
            updateActionsState();
        } catch (Exception ex) {
            Logger.getLogger(BinEdToolbarPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void redoEditButtonActionPerformed() {
        try {
            undoRedo.performRedo();
            codeAreaControl.repaint();
            updateActionsState();
        } catch (Exception ex) {
            Logger.getLogger(BinEdToolbarPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private Icon load(String path) {
        return IconLoader.getIcon(path, getClass());
    }

    @ParametersAreNonnullByDefault
    public interface Control {

        @NotNull
        CodeType getCodeType();

        void setCodeType(CodeType codeType);

        boolean isShowNonprintables();

        void setShowNonprintables(boolean showNonprintables);

        void repaint();
    }
}
