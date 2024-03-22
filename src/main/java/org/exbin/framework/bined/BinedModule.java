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
package org.exbin.framework.bined;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;

import org.exbin.bined.basic.BasicCodeAreaZone;
import org.exbin.bined.intellij.main.BinEdManager;
import org.exbin.bined.swing.extended.ExtCodeArea;
import org.exbin.framework.api.XBApplicationModule;
import org.exbin.framework.bined.action.CodeAreaAction;
import org.exbin.framework.bined.handler.CodeAreaPopupMenuHandler;
import org.exbin.framework.utils.ActionUtils;
import org.exbin.framework.utils.LanguageUtils;

/**
 * Binary data editor module.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class BinedModule implements XBApplicationModule {

    private PopupMenuVariant popupMenuVariant = PopupMenuVariant.NORMAL;
    private BasicCodeAreaZone popupMenuPositionZone = BasicCodeAreaZone.UNKNOWN;

    private final List<CodeAreaAction> codeAreaActions = new ArrayList<>();

    public BinedModule() {
    }

    @Nonnull
    public BinEdFileManager getFileManager() {
        BinEdManager binEdManager = BinEdManager.getInstance();
        return binEdManager.getFileManager();
    }

    @Nonnull
    public PopupMenuVariant getPopupMenuVariant() {
        return popupMenuVariant;
    }

    @Nonnull
    public BasicCodeAreaZone getPopupMenuPositionZone() {
        return popupMenuPositionZone;
    }

    @Nonnull
    public CodeAreaPopupMenuHandler createCodeAreaPopupMenuHandler(PopupMenuVariant variant) {
        return new CodeAreaPopupMenuHandler() {
            @Override
            public JPopupMenu createPopupMenu(ExtCodeArea codeArea, String menuPostfix, int x, int y) {
                return BinedModule.createCodeAreaPopupMenu(codeArea, "popup");
            }

            @Override
            public void dropPopupMenu(String menuPostfix) {
            }
        };
    }

    @Nonnull
    public static JPopupMenu createCodeAreaPopupMenu(final ExtCodeArea codeArea, String menuPostfix) {
        ResourceBundle popupResourceBundle = LanguageUtils.getResourceBundleByBundleName("org/exbin/framework/popup/resources/DefaultPopupMenu");
        JPopupMenu popupMenu = new JPopupMenu();

        JMenuItem cutMenuItem = new JMenuItem(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                codeArea.cut();
            }

            @Override
            public boolean isEnabled() {
                return codeArea.isEditable() && codeArea.hasSelection();
            }
        });
        cutMenuItem.setText(popupResourceBundle.getString("popupCutAction.text"));
        cutMenuItem.setToolTipText(popupResourceBundle.getString("popupCutAction.shortDescription"));
        cutMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionUtils.getMetaMask()));
        popupMenu.add(cutMenuItem);
        JMenuItem copyMenuItem = new JMenuItem(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                codeArea.copy();
            }

            @Override
            public boolean isEnabled() {
                return codeArea.hasSelection();
            }
        });
        copyMenuItem.setText(popupResourceBundle.getString("popupCopyAction.text"));
        copyMenuItem.setToolTipText(popupResourceBundle.getString("popupCopyAction.shortDescription"));
        copyMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionUtils.getMetaMask()));
        popupMenu.add(copyMenuItem);
        JMenuItem pasteMenuItem = new JMenuItem(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                codeArea.paste();
            }

            @Override
            public boolean isEnabled() {
                return codeArea.isEditable() && codeArea.canPaste();
            }
        });
        pasteMenuItem.setText(popupResourceBundle.getString("popupPasteAction.text"));
        pasteMenuItem.setToolTipText(popupResourceBundle.getString("popupPasteAction.shortDescription"));
        pasteMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionUtils.getMetaMask()));
        popupMenu.add(pasteMenuItem);
        JMenuItem deleteMenuItem = new JMenuItem(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                codeArea.delete();
            }

            @Override
            public boolean isEnabled() {
                return codeArea.isEditable() && codeArea.hasSelection();
            }
        });
        deleteMenuItem.setText(popupResourceBundle.getString("popupDeleteAction.text"));
        deleteMenuItem.setToolTipText(popupResourceBundle.getString("popupDeleteAction.shortDescription"));
        deleteMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
        popupMenu.add(deleteMenuItem);
        JMenuItem selectAllMenuItem = new JMenuItem(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                codeArea.selectAll();
            }
        });
        selectAllMenuItem.setText(popupResourceBundle.getString("popupSelectAllAction.text"));
        selectAllMenuItem.setToolTipText(popupResourceBundle.getString("popupSelectAllAction.shortDescription"));
        selectAllMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionUtils.getMetaMask()));
        popupMenu.add(selectAllMenuItem);

        return popupMenu;
    }

    public void addCodeAreaAction(CodeAreaAction codeAreaAction) {
        codeAreaActions.add(codeAreaAction);
    }

    public void removeCodeAreaAction(CodeAreaAction codeAreaAction) {
        codeAreaActions.remove(codeAreaAction);
    }

    public enum PopupMenuVariant {
        BASIC, NORMAL, EDITOR
    }
}
