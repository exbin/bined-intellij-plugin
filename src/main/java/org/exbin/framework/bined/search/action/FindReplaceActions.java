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
package org.exbin.framework.bined.search.action;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenuItem;
import org.exbin.bined.basic.BasicCodeAreaZone;
import org.exbin.bined.swing.CodeAreaCommandHandler;
import org.exbin.bined.swing.extended.ExtCodeArea;
import org.exbin.framework.api.XBApplication;
import org.exbin.framework.bined.gui.BinEdComponentPanel;
import org.exbin.framework.utils.ActionUtils;
import org.exbin.framework.editor.api.EditorProvider;
import org.exbin.framework.bined.BinEdFileHandler;
import org.exbin.framework.bined.BinedModule;
import org.exbin.framework.bined.macro.operation.CodeAreaMacroCommandHandler;
import org.exbin.framework.bined.macro.operation.MacroStep;
import org.exbin.framework.bined.search.BinEdComponentSearch;
import org.exbin.framework.bined.search.gui.BinarySearchPanel;
import org.exbin.framework.file.api.FileDependentAction;
import org.exbin.framework.file.api.FileHandler;

/**
 * Find/replace actions.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class FindReplaceActions implements FileDependentAction {

    public static final String EDIT_FIND_ACTION_ID = "editFindAction";
    public static final String EDIT_FIND_AGAIN_ACTION_ID = "editFindAgainAction";
    public static final String EDIT_REPLACE_ACTION_ID = "editReplaceAction";

    private EditorProvider editorProvider;
    private XBApplication application;
    private ResourceBundle resourceBundle;

    private Action editFindAction;
    private Action editFindAgainAction;
    private Action editReplaceAction;

    private final List<FindAgainListener> findAgainListeners = new ArrayList<>();

    public FindReplaceActions() {
    }

    public void setup(XBApplication application, EditorProvider editorProvider, ResourceBundle resourceBundle) {
        this.application = application;
        this.editorProvider = editorProvider;
        this.resourceBundle = resourceBundle;
    }

    public void setEditorProvider(EditorProvider editorProvider) {
        this.editorProvider = editorProvider;
    }

    @Override
    public void updateForActiveFile() {
        Optional<FileHandler> activeFile = editorProvider.getActiveFile();
        if (editFindAction != null) {
            editFindAction.setEnabled(activeFile.isPresent());
        }
        if (editFindAgainAction != null) {
            editFindAgainAction.setEnabled(activeFile.isPresent());
        }
        if (editReplaceAction != null) {
            editReplaceAction.setEnabled(activeFile.isPresent());
        }
    }

    @Nonnull
    public Action getEditFindAction() {
        if (editFindAction == null) {
            editFindAction = new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Optional<FileHandler> activeFile = editorProvider.getActiveFile();
                    if (!activeFile.isPresent()) {
                        throw new IllegalStateException();
                    }

                    BinEdComponentPanel activePanel = ((BinEdFileHandler) activeFile.get()).getComponent();
                    BinEdComponentSearch componentExtension = activePanel.getComponentExtension(BinEdComponentSearch.class);
                    componentExtension.showSearchPanel(BinarySearchPanel.PanelMode.FIND);
                }
            };
            ActionUtils.setupAction(editFindAction, resourceBundle, EDIT_FIND_ACTION_ID);
            editFindAction.putValue(Action.ACCELERATOR_KEY, javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F, ActionUtils.getMetaMask()));
            editFindAction.putValue(ActionUtils.ACTION_DIALOG_MODE, true);
            editFindAction.putValue(ActionUtils.ACTION_MENU_CREATION, new ActionUtils.MenuCreation() {
                @Override
                public boolean shouldCreate(String menuId) {
                    BinedModule binedModule = application.getModuleRepository().getModuleByInterface(BinedModule.class);
                    BinedModule.PopupMenuVariant menuVariant = binedModule.getPopupMenuVariant();
                    BasicCodeAreaZone positionZone = binedModule.getPopupMenuPositionZone();
                    return menuVariant == BinedModule.PopupMenuVariant.EDITOR && !(positionZone == BasicCodeAreaZone.TOP_LEFT_CORNER || positionZone == BasicCodeAreaZone.HEADER || positionZone == BasicCodeAreaZone.ROW_POSITIONS);
                }

                @Override
                public void onCreate(JMenuItem menuItem, String menuId) {
                }
            });
        }
        return editFindAction;
    }

    @Nonnull
    public Action getEditFindAgainAction() {
        if (editFindAgainAction == null) {
            editFindAgainAction = new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Optional<FileHandler> activeFile = editorProvider.getActiveFile();
                    if (!activeFile.isPresent()) {
                        throw new IllegalStateException();
                    }

                    BinEdComponentPanel activePanel = ((BinEdFileHandler) activeFile.get()).getComponent();
                    BinEdComponentSearch componentExtension = activePanel.getComponentExtension(BinEdComponentSearch.class);
                    componentExtension.performFindAgain();

                    for (FindAgainListener findAgainListener : findAgainListeners) {
                        findAgainListener.performed();
                    }
                }
            };
            ActionUtils.setupAction(editFindAgainAction, resourceBundle, EDIT_FIND_AGAIN_ACTION_ID);
            editFindAgainAction.putValue(Action.ACCELERATOR_KEY, javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F3, 0));
            editFindAgainAction.putValue(ActionUtils.ACTION_MENU_CREATION, new ActionUtils.MenuCreation() {
                @Override
                public boolean shouldCreate(String menuId) {
                    BinedModule binedModule = application.getModuleRepository().getModuleByInterface(BinedModule.class);
                    BinedModule.PopupMenuVariant menuVariant = binedModule.getPopupMenuVariant();
                    BasicCodeAreaZone positionZone = binedModule.getPopupMenuPositionZone();
                    return menuVariant == BinedModule.PopupMenuVariant.EDITOR && !(positionZone == BasicCodeAreaZone.TOP_LEFT_CORNER || positionZone == BasicCodeAreaZone.HEADER || positionZone == BasicCodeAreaZone.ROW_POSITIONS);
                }

                @Override
                public void onCreate(JMenuItem menuItem, String menuId) {
                }
            });
        }
        return editFindAgainAction;
    }

    @Nonnull
    public Action getEditReplaceAction() {
        if (editReplaceAction == null) {
            editReplaceAction = new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Optional<FileHandler> activeFile = editorProvider.getActiveFile();
                    if (!activeFile.isPresent()) {
                        throw new IllegalStateException();
                    }

                    BinEdComponentPanel activePanel = ((BinEdFileHandler) activeFile.get()).getComponent();
                    BinEdComponentSearch componentExtension = activePanel.getComponentExtension(BinEdComponentSearch.class);
                    componentExtension.showSearchPanel(BinarySearchPanel.PanelMode.REPLACE);
                }
            };
            ActionUtils.setupAction(editReplaceAction, resourceBundle, EDIT_REPLACE_ACTION_ID);
            editReplaceAction.putValue(Action.ACCELERATOR_KEY, javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_H, ActionUtils.getMetaMask()));
            editReplaceAction.putValue(ActionUtils.ACTION_DIALOG_MODE, true);
            editReplaceAction.putValue(ActionUtils.ACTION_MENU_CREATION, new ActionUtils.MenuCreation() {
                @Override
                public boolean shouldCreate(String menuId) {
                    BinedModule binedModule = application.getModuleRepository().getModuleByInterface(BinedModule.class);
                    BinedModule.PopupMenuVariant menuVariant = binedModule.getPopupMenuVariant();
                    BasicCodeAreaZone positionZone = binedModule.getPopupMenuPositionZone();
                    return menuVariant == BinedModule.PopupMenuVariant.EDITOR && !(positionZone == BasicCodeAreaZone.TOP_LEFT_CORNER || positionZone == BasicCodeAreaZone.HEADER || positionZone == BasicCodeAreaZone.ROW_POSITIONS);
                }

                @Override
                public void onCreate(JMenuItem menuItem, String menuId) {
                }
            });
        }
        return editReplaceAction;
    }

    public void addFindAgainListener(FindAgainListener findAgainListener) {
        findAgainListeners.add(findAgainListener);
    }

    public void addFindAgainListener() {
        addFindAgainListener(() -> {
            Optional<FileHandler> activeFile = editorProvider.getActiveFile();
            if (activeFile.isPresent()) {
                BinEdFileHandler fileHandler = (BinEdFileHandler) activeFile.get();
                ExtCodeArea codeArea = fileHandler.getCodeArea();
                CodeAreaCommandHandler commandHandler = codeArea.getCommandHandler();
                if (commandHandler instanceof CodeAreaMacroCommandHandler && ((CodeAreaMacroCommandHandler) commandHandler).isMacroRecording()) {
                    ((CodeAreaMacroCommandHandler) commandHandler).appendMacroOperationStep(MacroStep.FIND_AGAIN);
                }
            }
        });
    }

    public void removeFindAgainListener(FindAgainListener findAgainListener) {
        findAgainListeners.remove(findAgainListener);
    }

    public static interface FindAgainListener {

        void performed();
    }
}
