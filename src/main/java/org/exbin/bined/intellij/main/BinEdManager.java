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
package org.exbin.bined.intellij.main;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.exbin.bined.PositionCodeType;
import org.exbin.bined.basic.BasicCodeAreaZone;
import org.exbin.bined.extended.layout.ExtendedCodeAreaLayoutProfile;
import org.exbin.bined.intellij.BinEdIntelliJPlugin;
import org.exbin.bined.intellij.action.EditSelectionAction;
import org.exbin.bined.intellij.action.GoToPositionAction;
import org.exbin.bined.intellij.action.OptionsAction;
import org.exbin.bined.intellij.operation.action.ConvertDataAction;
import org.exbin.bined.intellij.operation.action.InsertDataAction;
import org.exbin.bined.intellij.bookmarks.BookmarksManager;
import org.exbin.bined.intellij.tool.content.action.ClipboardContentAction;
import org.exbin.bined.intellij.compare.action.CompareFilesAction;
import org.exbin.bined.intellij.gui.BinEdComponentPanel;
import org.exbin.bined.intellij.gui.BinEdToolbarPanel;
import org.exbin.bined.intellij.inspector.BinEdInspectorManager;
import org.exbin.bined.intellij.search.gui.BinarySearchPanel;
import org.exbin.bined.intellij.inspector.action.ShowParsingPanelAction;
import org.exbin.bined.intellij.search.action.SearchAction;
import org.exbin.bined.intellij.tool.content.action.DragDropContentAction;
import org.exbin.bined.swing.CodeAreaCore;
import org.exbin.bined.swing.extended.ExtCodeArea;
import org.exbin.framework.about.gui.AboutPanel;
import org.exbin.framework.bined.FileHandlingMode;
import org.exbin.framework.bined.gui.BinEdComponentFileApi;
import org.exbin.framework.bined.preferences.BinaryEditorPreferences;
import org.exbin.framework.utils.ActionUtils;
import org.exbin.framework.utils.DesktopUtils;
import org.exbin.framework.utils.WindowUtils;
import org.exbin.framework.utils.gui.CloseControlPanel;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.AbstractAction;
import javax.swing.GrayFilter;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Manager for binary editor.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class BinEdManager {

    private static BinEdManager instance;

    private static final String BINED_TANGO_ICON_THEME_PREFIX = "/org/exbin/framework/bined/resources/icons/tango-icon-theme/16x16/actions/";
    private static final String FRAMEWORK_TANGO_ICON_THEME_PREFIX = "/org/exbin/framework/action/resources/icons/tango-icon-theme/16x16/actions/";
    private static final FileHandlingMode DEFAULT_FILE_HANDLING_MODE = FileHandlingMode.DELTA;
    private static final String ONLINE_HELP_URL = "https://bined.exbin.org/intellij-plugin/?manual";

    private final BinaryEditorPreferences preferences;
    private BinEdFileManager fileManager = new BinEdFileManager();

    private volatile boolean initialized;
    private BookmarksSupport bookmarksSupport;
    private InspectorSupport inspectorSupport;

    private BinEdManager() {
        preferences = new BinaryEditorPreferences(new IntelliJPreferencesWrapper(PropertiesComponent.getInstance(), BinEdIntelliJPlugin.PLUGIN_PREFIX));
    }

    @Nonnull
    public static synchronized BinEdManager getInstance() {
        if (instance == null) {
            instance = new BinEdManager();
        }

        return instance;
    }

    @Nonnull
    public BinEdEditorComponent createBinEdEditor() {
        if (!initialized) {
            initialized = true;
            new BookmarksManager().init();
            new BinEdInspectorManager().init();
        }

        BinEdEditorComponent binEdEditorComponent = new BinEdEditorComponent();
        binEdEditorComponent.initialLoadFromPreferences(preferences);
        BinEdComponentPanel componentPanel = binEdEditorComponent.getComponentPanel();
        ExtCodeArea codeArea = componentPanel.getCodeArea();

        codeArea.setComponentPopupMenu(new JPopupMenu() {
            @Override
            public void show(Component invoker, int x, int y) {
                int clickedX = x;
                int clickedY = y;
                if (invoker instanceof JViewport) {
                    clickedX += invoker.getParent().getX();
                    clickedY += invoker.getParent().getY();
                }
                removeAll();
                createContextMenu(binEdEditorComponent.getCodeArea(), binEdEditorComponent.getFileApi(), binEdEditorComponent, this, PopupMenuVariant.EDITOR, clickedX, clickedY);
                super.show(invoker, x, y);
            }
        });

        codeArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent keyEvent) {
                if (keyEvent.getModifiersEx() == ActionUtils.getMetaMask()) {
                    int keyCode = keyEvent.getKeyCode();
                    switch (keyCode) {
                    case KeyEvent.VK_F: {
                        SearchAction searchAction =
                                new SearchAction(codeArea, binEdEditorComponent.getComponentPanel());
                        searchAction.actionPerformed(new ActionEvent(keyEvent.getSource(), keyEvent.getID(), ""));
                        searchAction.switchReplaceMode(BinarySearchPanel.SearchOperation.FIND);
                        break;
                    }
                    case KeyEvent.VK_G: {
                        if (codeArea.isEditable()) {
                            GoToPositionAction goToPositionAction = new GoToPositionAction(codeArea);
                            goToPositionAction.actionPerformed(new ActionEvent(keyEvent.getSource(),
                                    keyEvent.getID(),
                                    ""));
                        }
                        break;
                    }
                    case KeyEvent.VK_I: {
                        if (codeArea.isEditable()) {
                            InsertDataAction insertDataAction = new InsertDataAction(codeArea);
                            insertDataAction.actionPerformed(new ActionEvent(keyEvent.getSource(),
                                    keyEvent.getID(),
                                    ""));
                        }
                        break;
                    }
                    case KeyEvent.VK_M: {
                        if (codeArea.isEditable()) {
                            ConvertDataAction convertDataAction = new ConvertDataAction(codeArea);
                            convertDataAction.actionPerformed(new ActionEvent(keyEvent.getSource(),
                                    keyEvent.getID(),
                                    ""));
                        }
                        break;
                    }
                    }
                }
            }
        });

        BinEdToolbarPanel toolbarPanel = binEdEditorComponent.getToolbarPanel();
        toolbarPanel.setOptionsAction(
                new AnAction() {
                    @Override
                    public void actionPerformed(@Nonnull AnActionEvent anActionEvent) {
                        createOptionsAction(binEdEditorComponent).actionPerformed(new ActionEvent(BinEdManager.this, 0, "COMMAND", 0));
                    }
                }
        );
        toolbarPanel.setOnlineHelpAction(
                new AnAction() {
                    @Override
                    public void actionPerformed(@Nonnull AnActionEvent anActionEvent) {
                        createOnlineHelpAction().actionPerformed(new ActionEvent(BinEdManager.this, 0, "COMMAND", 0));
                    }
                }
        );

        bookmarksSupport.registerBookmarksComponentActions(codeArea);
        binEdEditorComponent.setGoToPositionAction(new GoToPositionAction(codeArea));

        return binEdEditorComponent;
    }

    @Nonnull
    public BinEdFileManager getFileManager() {
        return fileManager;
    }

    public void createContextMenu(ExtCodeArea codeArea, final JPopupMenu menu, PopupMenuVariant variant, int x, int y) {
        createContextMenu(codeArea, null, null, menu, variant, x, y);
    }

    public void createContextMenu(ExtCodeArea codeArea, @Nullable BinEdComponentFileApi fileApi, @Nullable BinEdEditorComponent editorComponent, final JPopupMenu menu, PopupMenuVariant variant, int x, int y) {
        BasicCodeAreaZone positionZone = codeArea.getPainter().getPositionZone(x, y);

        bookmarksSupport.setActiveCodeArea(codeArea);

        if (variant == PopupMenuVariant.EDITOR) {
            switch (positionZone) {
            case TOP_LEFT_CORNER:
            case HEADER:
            case ROW_POSITIONS: {
                break;
            }
            default: {
                JMenu showMenu = new JMenu("Show");
                showMenu.add(createShowHeaderMenuItem(codeArea));
                showMenu.add(createShowRowPositionMenuItem(codeArea));
                showMenu.add(createShowInspectorPanel(editorComponent.getComponentPanel()));
                menu.add(showMenu);
                menu.addSeparator();
            }
            }
        }

        switch (positionZone) {
        case TOP_LEFT_CORNER:
        case HEADER: {
            if (variant != PopupMenuVariant.BASIC) {
                menu.add(createShowHeaderMenuItem(codeArea));
                menu.add(createPositionCodeTypeMenuItem(codeArea));
            }
            break;
        }
        case ROW_POSITIONS: {
            if (variant != PopupMenuVariant.BASIC) {
                menu.add(createShowRowPositionMenuItem(codeArea));
                menu.add(createPositionCodeTypeMenuItem(codeArea));
                menu.add(new JSeparator());
                menu.add(createGoToMenuItem(codeArea));
            }

            break;
        }
        default: {
            final JMenuItem cutMenuItem = new JMenuItem("Cut");
            ImageIcon cutMenuItemIcon = new ImageIcon(getClass().getResource(FRAMEWORK_TANGO_ICON_THEME_PREFIX + "edit-cut.png"));
            cutMenuItem.setIcon(cutMenuItemIcon);
            cutMenuItem.setDisabledIcon(new ImageIcon(GrayFilter.createDisabledImage(cutMenuItemIcon.getImage())));
            cutMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionUtils.getMetaMask()));
            cutMenuItem.setEnabled(codeArea.hasSelection() && codeArea.isEditable());
            cutMenuItem.addActionListener((ActionEvent e) -> {
                codeArea.cut();
                menu.setVisible(false);
            });
            menu.add(cutMenuItem);

            final JMenuItem copyMenuItem = new JMenuItem("Copy");
            ImageIcon copyMenuItemIcon = new ImageIcon(getClass().getResource(FRAMEWORK_TANGO_ICON_THEME_PREFIX + "edit-copy.png"));
            copyMenuItem.setIcon(copyMenuItemIcon);
            copyMenuItem.setDisabledIcon(new ImageIcon(GrayFilter.createDisabledImage(copyMenuItemIcon.getImage())));
            copyMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionUtils.getMetaMask()));
            copyMenuItem.setEnabled(codeArea.hasSelection());
            copyMenuItem.addActionListener((ActionEvent e) -> {
                codeArea.copy();
                menu.setVisible(false);
            });
            menu.add(copyMenuItem);

            final JMenuItem copyAsCodeMenuItem = new JMenuItem("Copy as Code");
            copyAsCodeMenuItem.setEnabled(codeArea.hasSelection());
            copyAsCodeMenuItem.addActionListener((ActionEvent e) -> {
                codeArea.copyAsCode();
                menu.setVisible(false);
            });
            menu.add(copyAsCodeMenuItem);

            final JMenuItem pasteMenuItem = new JMenuItem("Paste");
            ImageIcon pasteMenuItemIcon = new ImageIcon(getClass().getResource(FRAMEWORK_TANGO_ICON_THEME_PREFIX + "edit-paste.png"));
            pasteMenuItem.setIcon(pasteMenuItemIcon);
            pasteMenuItem.setDisabledIcon(new ImageIcon(GrayFilter.createDisabledImage(pasteMenuItemIcon.getImage())));
            pasteMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionUtils.getMetaMask()));
            pasteMenuItem.setEnabled(codeArea.canPaste() && codeArea.isEditable());
            pasteMenuItem.addActionListener((ActionEvent e) -> {
                codeArea.paste();
                menu.setVisible(false);
            });
            menu.add(pasteMenuItem);

            final JMenuItem pasteFromCodeMenuItem = new JMenuItem("Paste from Code");
            pasteFromCodeMenuItem.setEnabled(codeArea.canPaste() && codeArea.isEditable());
            pasteFromCodeMenuItem.addActionListener((ActionEvent e) -> {
                try {
                    codeArea.pasteFromCode();
                } catch (IllegalArgumentException ex) {
                    JOptionPane.showMessageDialog(codeArea, ex.getMessage(), "Unable to Paste Code", JOptionPane.ERROR_MESSAGE);
                }
                menu.setVisible(false);
            });
            menu.add(pasteFromCodeMenuItem);

            final JMenuItem deleteMenuItem = new JMenuItem("Delete");
            ImageIcon deleteMenuItemIcon = new ImageIcon(getClass().getResource(FRAMEWORK_TANGO_ICON_THEME_PREFIX + "edit-delete.png"));
            deleteMenuItem.setIcon(deleteMenuItemIcon);
            deleteMenuItem.setDisabledIcon(new ImageIcon(GrayFilter.createDisabledImage(deleteMenuItemIcon.getImage())));
            deleteMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
            deleteMenuItem.setEnabled(codeArea.hasSelection() && codeArea.isEditable());
            deleteMenuItem.addActionListener((ActionEvent e) -> {
                codeArea.delete();
                menu.setVisible(false);
            });
            menu.add(deleteMenuItem);

            final JMenuItem selectAllMenuItem = new JMenuItem("Select All");
            selectAllMenuItem.setIcon(new ImageIcon(getClass().getResource(FRAMEWORK_TANGO_ICON_THEME_PREFIX + "edit-select-all.png")));
            selectAllMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionUtils.getMetaMask()));
            selectAllMenuItem.addActionListener((ActionEvent e) -> {
                codeArea.selectAll();
                menu.setVisible(false);
            });
            menu.add(selectAllMenuItem);

            menu.add(createEditSelectionMenuItem(codeArea));

            menu.addSeparator();

            if (editorComponent != null) {
                JMenuItem insertDataMenuItem = createInsertDataMenuItem(editorComponent);
                insertDataMenuItem.setEnabled(codeArea.isEditable());
                menu.add(insertDataMenuItem);
                JMenuItem convertDataMenuItem = createConvertDataMenuItem(editorComponent);
                convertDataMenuItem.setEnabled(codeArea.isEditable());
                menu.add(convertDataMenuItem);
            }

            menu.addSeparator();

            final JMenuItem findMenuItem = new JMenuItem("Find...");
            findMenuItem.setIcon(new ImageIcon(getClass().getResource(BINED_TANGO_ICON_THEME_PREFIX + "edit-find.png")));
            findMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, ActionUtils.getMetaMask()));
            findMenuItem.addActionListener((ActionEvent e) -> {
                SearchAction searchAction = new SearchAction(codeArea, editorComponent.getComponentPanel());
                searchAction.actionPerformed(e);
                searchAction.switchReplaceMode(BinarySearchPanel.SearchOperation.FIND);
            });
            menu.add(findMenuItem);

            final JMenuItem replaceMenuItem = new JMenuItem("Replace...");
            replaceMenuItem.setIcon(new ImageIcon(getClass().getResource(BINED_TANGO_ICON_THEME_PREFIX + "edit-find-replace.png")));
            replaceMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, ActionUtils.getMetaMask()));
            replaceMenuItem.setEnabled(codeArea.isEditable());
            replaceMenuItem.addActionListener((ActionEvent e) -> {
                SearchAction searchAction = new SearchAction(codeArea, editorComponent.getComponentPanel());
                searchAction.actionPerformed(e);
                searchAction.switchReplaceMode(BinarySearchPanel.SearchOperation.REPLACE);
            });
            menu.add(replaceMenuItem);

            JMenuItem goToMenuItem = createGoToMenuItem(codeArea);
            menu.add(goToMenuItem);

            menu.add(bookmarksSupport.createBookmarksPopupMenu());
        }
        }

        menu.addSeparator();

        JMenu toolsMenu = new JMenu("Tools");
        toolsMenu.add(createCompareFilesMenuItem(codeArea));
        toolsMenu.add(createClipboardContentMenuItem());
        toolsMenu.add(createDragDropContentMenuItem());
        menu.add(toolsMenu);

        if (editorComponent != null) {
            if (fileApi instanceof BinEdFileHandler || fileApi instanceof BinEdNativeFile) {
                JMenuItem reloadFileMenuItem = createReloadFileMenuItem(editorComponent);
                menu.add(reloadFileMenuItem);
            }
        }

        if (editorComponent != null) {
            final JMenuItem optionsMenuItem = new JMenuItem("Options...");
            optionsMenuItem.setIcon(new ImageIcon(getClass().getResource(
                    "/org/exbin/framework/options/gui/resources/icons/Preferences16.gif")));
            optionsMenuItem.addActionListener(createOptionsAction(editorComponent));
            menu.add(optionsMenuItem);
        }

        switch (positionZone) {
        case TOP_LEFT_CORNER:
        case HEADER:
        case ROW_POSITIONS: {
            break;
        }
        default: {
            menu.addSeparator();

            final JMenuItem onlineHelpMenuItem = new JMenuItem("Online Help...");
            onlineHelpMenuItem.setIcon(new ImageIcon(getClass().getResource("/org/exbin/framework/bined/resources/icons/open_icon_library/icons/png/16x16/actions/help.png")));
            onlineHelpMenuItem.addActionListener(createOnlineHelpAction());
            menu.add(onlineHelpMenuItem);

            final JMenuItem aboutMenuItem = new JMenuItem("About...");
            aboutMenuItem.addActionListener((ActionEvent e) -> {
                AboutPanel aboutPanel = new AboutPanel();
                aboutPanel.setupFields();
                CloseControlPanel closeControlPanel = new CloseControlPanel();
                JPanel dialogPanel = WindowUtils.createDialogPanel(aboutPanel, closeControlPanel);
                WindowUtils.DialogWrapper dialog = WindowUtils.createDialog(dialogPanel, (Component) e.getSource(), "About Plugin", Dialog.ModalityType.APPLICATION_MODAL);
                closeControlPanel.setHandler(() -> {
                    dialog.close();
                });
                dialog.showCentered((Component) e.getSource());
            });
            menu.add(aboutMenuItem);
        }
        }
    }

    @Nonnull
    private AbstractAction createOptionsAction(BinEdEditorComponent editorComponent) {
        return new OptionsAction(editorComponent, preferences);
    }

    @Nonnull
    private AbstractAction createOnlineHelpAction() {
        return new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DesktopUtils.openDesktopURL(ONLINE_HELP_URL);
            }
        };
    }

    @Nonnull
    private JMenuItem createGoToMenuItem(ExtCodeArea codeArea) {
        final JMenuItem goToMenuItem = new JMenuItem("Go To...");
        goToMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, ActionUtils.getMetaMask()));
        goToMenuItem.addActionListener(new GoToPositionAction(codeArea));
        return goToMenuItem;
    }

    @Nonnull
    private JMenuItem createEditSelectionMenuItem(ExtCodeArea codeArea) {
        final JMenuItem editSelectionMenuItem = new JMenuItem("Edit Selection...");
        editSelectionMenuItem.addActionListener(new EditSelectionAction(codeArea));
        return editSelectionMenuItem;
    }

    @Nonnull
    private JMenuItem createInsertDataMenuItem(BinEdEditorComponent editorComponent) {
        final JMenuItem insertDataMenuItem = new JMenuItem("Insert Data...");
        insertDataMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, ActionUtils.getMetaMask()));
        InsertDataAction insertDataAction = new InsertDataAction(editorComponent.getCodeArea());
        insertDataMenuItem.addActionListener(insertDataAction);
        return insertDataMenuItem;
    }

    @Nonnull
    private JMenuItem createConvertDataMenuItem(BinEdEditorComponent editorComponent) {
        final JMenuItem convertDataMenuItem = new JMenuItem("Convert Data...");
        convertDataMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, ActionUtils.getMetaMask()));
        ConvertDataAction convertDataAction = new ConvertDataAction(editorComponent.getCodeArea());
        convertDataMenuItem.addActionListener(convertDataAction);
        return convertDataMenuItem;
    }

    @Nonnull
    private JMenuItem createCompareFilesMenuItem(ExtCodeArea codeArea) {
        final JMenuItem compareFilesMenuItem = new JMenuItem("Compare Files...");
        compareFilesMenuItem.addActionListener(new CompareFilesAction(codeArea));
        return compareFilesMenuItem;
    }

    @Nonnull
    private JMenuItem createReloadFileMenuItem(BinEdEditorComponent editorComponent) {
        final JMenuItem reloadFileMenuItem = new JMenuItem("Reload File");
        reloadFileMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, ActionUtils.getMetaMask() + KeyEvent.ALT_DOWN_MASK));
        reloadFileMenuItem.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                BinEdComponentFileApi fileApi = editorComponent.getFileApi();
                if (editorComponent.releaseFile()) {
                    if (fileApi instanceof BinEdFileHandler) {
                        ((BinEdFileHandler) fileApi).reloadFile();
                    } else if (fileApi instanceof BinEdNativeFile) {
                        ((BinEdNativeFile) fileApi).reloadFile();
                    }
                }
            }
        });
        return reloadFileMenuItem;
    }

    @Nonnull
    private JMenuItem createShowHeaderMenuItem(ExtCodeArea codeArea) {
        final JCheckBoxMenuItem showHeader = new JCheckBoxMenuItem("Show Header");
        showHeader.setSelected(codeArea.getLayoutProfile().isShowHeader());
        showHeader.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ExtendedCodeAreaLayoutProfile layoutProfile = codeArea.getLayoutProfile();
                if (layoutProfile == null) {
                    throw new IllegalStateException();
                }
                boolean showHeader = layoutProfile.isShowHeader();
                layoutProfile.setShowHeader(!showHeader);
                codeArea.setLayoutProfile(layoutProfile);
            }
        });
        return showHeader;
    }

    @Nonnull
    private JMenuItem createShowRowPositionMenuItem(ExtCodeArea codeArea) {
        final JCheckBoxMenuItem showRowPosition = new JCheckBoxMenuItem("Show Row Position");
        showRowPosition.setSelected(codeArea.getLayoutProfile().isShowRowPosition());
        showRowPosition.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ExtendedCodeAreaLayoutProfile layoutProfile = codeArea.getLayoutProfile();
                if (layoutProfile == null) {
                    throw new IllegalStateException();
                }
                boolean showRowPosition = layoutProfile.isShowRowPosition();
                layoutProfile.setShowRowPosition(!showRowPosition);
                codeArea.setLayoutProfile(layoutProfile);
            }
        });
        return showRowPosition;
    }

    @Nonnull
    private JMenuItem createPositionCodeTypeMenuItem(ExtCodeArea codeArea) {
        JMenu menu = new JMenu("Position Code Type");
        PositionCodeType codeType = codeArea.getPositionCodeType();

        final JRadioButtonMenuItem octalCodeTypeMenuItem = new JRadioButtonMenuItem("Octal");
        octalCodeTypeMenuItem.setSelected(codeType == PositionCodeType.OCTAL);
        octalCodeTypeMenuItem.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                codeArea.setPositionCodeType(PositionCodeType.OCTAL);
                preferences.getCodeAreaPreferences().setPositionCodeType(PositionCodeType.OCTAL);
            }
        });
        menu.add(octalCodeTypeMenuItem);

        final JRadioButtonMenuItem decimalCodeTypeMenuItem = new JRadioButtonMenuItem("Decimal");
        decimalCodeTypeMenuItem.setSelected(codeType == PositionCodeType.DECIMAL);
        decimalCodeTypeMenuItem.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                codeArea.setPositionCodeType(PositionCodeType.DECIMAL);
                preferences.getCodeAreaPreferences().setPositionCodeType(PositionCodeType.DECIMAL);
            }
        });
        menu.add(decimalCodeTypeMenuItem);

        final JRadioButtonMenuItem hexadecimalCodeTypeMenuItem = new JRadioButtonMenuItem("Hexadecimal");
        hexadecimalCodeTypeMenuItem.setSelected(codeType == PositionCodeType.HEXADECIMAL);
        hexadecimalCodeTypeMenuItem.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                codeArea.setPositionCodeType(PositionCodeType.HEXADECIMAL);
                preferences.getCodeAreaPreferences().setPositionCodeType(PositionCodeType.HEXADECIMAL);
            }
        });
        menu.add(hexadecimalCodeTypeMenuItem);

        return menu;
    }

    public JMenuItem createClipboardContentMenuItem() {
        JMenuItem clipboardContentMenuItem = new JMenuItem("Clipboard Content...");
        clipboardContentMenuItem.addActionListener(new ClipboardContentAction());
        return clipboardContentMenuItem;
    }

    public JMenuItem createDragDropContentMenuItem() {
        JMenuItem dragDropContentMenuItem = new JMenuItem("Drag&Drop Content...");
        dragDropContentMenuItem.addActionListener(new DragDropContentAction());
        return dragDropContentMenuItem;
    }

    public JMenuItem createShowInspectorPanel(BinEdComponentPanel binEdComponentPanel) {
        JCheckBoxMenuItem clipboardContentMenuItem = new JCheckBoxMenuItem("Inspector Panel");
        clipboardContentMenuItem.setSelected(inspectorSupport.isShowParsingPanel(binEdComponentPanel));
        clipboardContentMenuItem.addActionListener(event -> {
            inspectorSupport.showParsingPanelAction(binEdComponentPanel).actionPerformed(event);
        });
        return clipboardContentMenuItem;
    }

    @Nonnull
    public BinaryEditorPreferences getPreferences() {
        return preferences;
    }

    public void setBookmarksSupport(BookmarksSupport bookmarksSupport) {
        this.bookmarksSupport = bookmarksSupport;
    }

    public void setInspectorSupport(InspectorSupport inspectorSupport) {
        this.inspectorSupport = inspectorSupport;
    }

    public enum PopupMenuVariant {
        BASIC, NORMAL, EDITOR
    }

    @ParametersAreNonnullByDefault
    public interface BookmarksSupport {
        @Nonnull
        JMenu createBookmarksPopupMenu();

        void registerBookmarksComponentActions(JComponent component);

        void setActiveCodeArea(@Nullable CodeAreaCore codeArea);
    }

    @ParametersAreNonnullByDefault
    public interface InspectorSupport {

        boolean isShowParsingPanel(BinEdComponentPanel binEdComponentPanel);

        @Nonnull
        ShowParsingPanelAction showParsingPanelAction(BinEdComponentPanel binEdComponentPanel);
    }
}
