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

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.wm.impl.IdeBackgroundUtil;
import com.intellij.ui.Graphics2DDelegate;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import org.exbin.auxiliary.paged_data.BinaryData;
import org.exbin.bined.CodeAreaCaretPosition;
import org.exbin.bined.EditMode;
import org.exbin.bined.EditOperation;
import org.exbin.bined.PositionCodeType;
import org.exbin.bined.basic.BasicCodeAreaZone;
import org.exbin.bined.capability.CharsetCapable;
import org.exbin.bined.extended.layout.ExtendedCodeAreaLayoutProfile;
import org.exbin.bined.highlight.swing.extended.ExtendedHighlightNonAsciiCodeAreaPainter;
import org.exbin.bined.intellij.BinEdApplyOptions;
import org.exbin.bined.intellij.BinEdIntelliJPlugin;
import org.exbin.bined.intellij.BinEdNativeFile;
import org.exbin.bined.intellij.action.CompareFilesAction;
import org.exbin.bined.intellij.action.EditSelectionAction;
import org.exbin.bined.intellij.action.GoToPositionAction;
import org.exbin.bined.intellij.IntelliJPreferencesWrapper;
import org.exbin.bined.intellij.action.SearchAction;
import org.exbin.bined.intellij.action.InsertDataAction;
import org.exbin.bined.intellij.options.IntegrationOptions;
import org.exbin.bined.operation.BinaryDataCommand;
import org.exbin.bined.operation.swing.CodeAreaOperationCommandHandler;
import org.exbin.bined.operation.undo.BinaryDataUndoHandler;
import org.exbin.bined.operation.undo.BinaryDataUndoUpdateListener;
import org.exbin.bined.swing.basic.color.CodeAreaColorsProfile;
import org.exbin.bined.swing.capability.FontCapable;
import org.exbin.bined.swing.extended.ExtCodeArea;
import org.exbin.bined.swing.extended.theme.ExtendedCodeAreaThemeProfile;
import org.exbin.framework.bined.BinEdFileHandler;
import org.exbin.framework.bined.BinaryStatusApi;
import org.exbin.framework.bined.FileHandlingMode;
import org.exbin.framework.bined.options.CodeAreaColorOptions;
import org.exbin.framework.bined.options.CodeAreaLayoutOptions;
import org.exbin.framework.bined.options.CodeAreaOptions;
import org.exbin.framework.bined.options.CodeAreaThemeOptions;
import org.exbin.framework.bined.options.EditorOptions;
import org.exbin.framework.bined.options.StatusOptions;
import org.exbin.framework.bined.options.impl.CodeAreaOptionsImpl;
import org.exbin.framework.bined.gui.BinaryStatusPanel;
import org.exbin.framework.bined.gui.ValuesPanel;
import org.exbin.framework.bined.preferences.BinaryEditorPreferences;
import org.exbin.framework.editor.text.EncodingsHandler;
import org.exbin.framework.editor.text.TextEncodingStatusApi;
import org.exbin.framework.editor.text.options.TextEncodingOptions;
import org.exbin.framework.editor.text.options.TextFontOptions;
import org.exbin.framework.editor.text.service.TextFontService;
import org.exbin.framework.about.gui.AboutPanel;
import org.exbin.framework.utils.ActionUtils;
import org.exbin.framework.utils.DesktopUtils;
import org.exbin.framework.utils.WindowUtils;
import org.exbin.framework.utils.handler.OptionsControlHandler;
import org.exbin.framework.utils.gui.CloseControlPanel;
import org.exbin.framework.utils.gui.OptionsControlPanel;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.GrayFilter;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.nio.charset.Charset;

/**
 * Binary editor component panel.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class BinEdComponentPanel extends JBPanel implements DumbAware {

    public static final String ACTION_CLIPBOARD_CUT = "cut-to-clipboard";
    public static final String ACTION_CLIPBOARD_COPY = "copy-to-clipboard";
    public static final String ACTION_CLIPBOARD_PASTE = "paste-from-clipboard";

    private static final String BINED_TANGO_ICON_THEME_PREFIX = "/org/exbin/framework/bined/resources/icons/tango-icon-theme/16x16/actions/";
    private static final String FRAMEWORK_TANGO_ICON_THEME_PREFIX = "/org/exbin/framework/action/resources/icons/tango-icon-theme/16x16/actions/";
    private static final FileHandlingMode DEFAULT_FILE_HANDLING_MODE = FileHandlingMode.DELTA;
    private static final String ONLINE_HELP_URL = "https://bined.exbin.org/intellij-plugin/?manual";

    private BinEdComponentFileApi fileApi = null;
    private final BinaryEditorPreferences preferences;
    private final ExtCodeArea codeArea;
    private BinaryDataUndoHandler undoHandler;
    private final ExtendedCodeAreaLayoutProfile defaultLayoutProfile;
    private final ExtendedCodeAreaThemeProfile defaultThemeProfile;
    private final CodeAreaColorsProfile defaultColorProfile;

    private final BinEdToolbarPanel toolbarPanel;
    private final BinaryStatusPanel statusPanel;
    private BinaryStatusApi binaryStatus;
    private TextEncodingStatusApi encodingStatus;
    private CharsetChangeListener charsetChangeListener = null;
    private ModifiedStateListener modifiedChangeListener = null;
    private final GoToPositionAction goToPositionAction;
    private final InsertDataAction insertDataAction;
    private final EditSelectionAction editSelectionAction;
    private final CompareFilesAction compareFilesAction;
    private final AbstractAction reloadFileAction;
    private final AbstractAction showHeaderAction;
    private final AbstractAction showRowNumbersAction;
    private final SearchAction searchAction;
    private EncodingsHandler encodingsHandler;
    private ValuesPanel valuesPanel = null;
    private JBScrollPane valuesPanelScrollPane = null;
    private boolean valuesPanelVisible = false;

    private FileHandlingMode fileHandlingMode = DEFAULT_FILE_HANDLING_MODE;
    private final Font defaultFont;
    private long documentOriginalSize;

    public BinEdComponentPanel() {
        initComponents();

        preferences = new BinaryEditorPreferences(new IntelliJPreferencesWrapper(getPreferences(), BinEdIntelliJPlugin.PLUGIN_PREFIX));

        codeArea = new ExtCodeArea() {

            private Graphics2DDelegate graphicsCache = null;

            @Override
            protected Graphics getComponentGraphics(Graphics g) {
                if (g instanceof Graphics2DDelegate) {
                    return g;
                }

                if (graphicsCache != null && graphicsCache.getDelegate() == g) {
                    return graphicsCache;
                }

                if (graphicsCache != null) {
                    graphicsCache.dispose();
                }

                Graphics2D editorGraphics = IdeBackgroundUtil.withEditorBackground(g, this);
                graphicsCache = editorGraphics instanceof Graphics2DDelegate ? (Graphics2DDelegate) editorGraphics : new Graphics2DDelegate(editorGraphics);
                return graphicsCache;
            }
        };
        codeArea.setPainter(new ExtendedHighlightNonAsciiCodeAreaPainter(codeArea));
        defaultFont = new Font(Font.MONOSPACED, Font.PLAIN, 12);
        codeArea.setCodeFont(defaultFont);
        codeArea.getCaret().setBlinkRate(300);
        defaultLayoutProfile = codeArea.getLayoutProfile();
        defaultThemeProfile = codeArea.getThemeProfile();
        defaultColorProfile = codeArea.getColorsProfile();
        toolbarPanel = new BinEdToolbarPanel(preferences, codeArea,
                new AnAction() {
                    @Override
                    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
                        createOptionsAction().actionPerformed(new ActionEvent(BinEdComponentPanel.this, 0, "COMMAND", 0));
                    }
                },
                new AnAction() {
                    @Override
                    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
                        createOnlineHelpAction().actionPerformed(new ActionEvent(BinEdComponentPanel.this, 0, "COMMAND", 0));
                    }
                }
        );
        statusPanel = new BinaryStatusPanel();

        goToPositionAction = new GoToPositionAction(codeArea);
        insertDataAction = new InsertDataAction(codeArea);
        editSelectionAction = new EditSelectionAction(codeArea);
        compareFilesAction = new CompareFilesAction(codeArea);
        showHeaderAction = new AbstractAction() {
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
        };
        showRowNumbersAction = new AbstractAction() {
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
        };
        reloadFileAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (releaseFile()) {
                    if (fileApi instanceof BinEdFileHandler) {
                        ((BinEdFileHandler) fileApi).reloadFile();
                    } else if (fileApi instanceof BinEdNativeFile) {
                        ((BinEdNativeFile) fileApi).reloadFile();
                    }
                }
            }
        };
        searchAction = new SearchAction(codeArea, codeAreaPanel);

        init();
    }

    private void init() {
        this.add(toolbarPanel, BorderLayout.NORTH);
        registerEncodingStatus(statusPanel);
        encodingsHandler = new EncodingsHandler();
        encodingsHandler.setParentComponent(this);
        encodingsHandler.init();
        encodingsHandler.setTextEncodingStatus(new TextEncodingStatusApi() {
            @Nonnull
            @Override
            public String getEncoding() {
                return encodingStatus.getEncoding();
            }

            @Override
            public void setEncoding(String encodingName) {
                codeArea.setCharset(Charset.forName(encodingName));
                encodingStatus.setEncoding(encodingName);
                preferences.getEncodingPreferences().setSelectedEncoding(encodingName);
                charsetChangeListener.charsetChanged();
            }
        });

        registerBinaryStatus(statusPanel);

        initialLoadFromPreferences();

        this.add(statusPanel, BorderLayout.SOUTH);
        codeAreaPanel.add(codeArea, BorderLayout.CENTER);

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
                createContextMenu(this, clickedX, clickedY);
                super.show(invoker, x, y);
            }
        });

        codeArea.addDataChangedListener(() -> {
            searchAction.codeAreaDataChanged();
            updateCurrentDocumentSize();
        });

        toolbarPanel.applyFromCodeArea();

        codeArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent keyEvent) {
                if (keyEvent.getModifiersEx() == ActionUtils.getMetaMask()) {
                    int keyCode = keyEvent.getKeyCode();
                    switch (keyCode) {
                        case KeyEvent.VK_S: {
                            if (fileApi.isSaveSupported() && isModified()) {
                                saveDocument();
                            }
                            break;
                        }
                        case KeyEvent.VK_F: {
                            searchAction.actionPerformed(new ActionEvent(keyEvent.getSource(), keyEvent.getID(), ""));
                            searchAction.switchReplaceMode(BinarySearchPanel.SearchOperation.FIND);
                            break;
                        }
                        case KeyEvent.VK_G: {
                            goToPositionAction.actionPerformed(new ActionEvent(keyEvent.getSource(), keyEvent.getID(), ""));
                            break;
                        }
                    }
                }
            }
        });

        ActionMap actionMap = getActionMap();
        actionMap.put(ACTION_CLIPBOARD_COPY, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                codeArea.copy();
            }
        });
        actionMap.put(ACTION_CLIPBOARD_CUT, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                codeArea.cut();
            }
        });
        actionMap.put(ACTION_CLIPBOARD_PASTE, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                codeArea.paste();
            }
        });
        actionMap.put("reloadFile", reloadFileAction);
    }

    public void registerBinaryStatus(BinaryStatusApi binaryStatusApi) {
        this.binaryStatus = binaryStatusApi;
        codeArea.addCaretMovedListener((CodeAreaCaretPosition caretPosition) -> {
            binaryStatus.setCursorPosition(caretPosition);
        });
        codeArea.addSelectionChangedListener(() -> {
            binaryStatus.setSelectionRange(codeArea.getSelection());
        });

        codeArea.addEditModeChangedListener(binaryStatus::setEditMode);
        binaryStatus.setEditMode(codeArea.getEditMode(), codeArea.getActiveOperation());

        ((BinaryStatusPanel) binaryStatus).setStatusControlHandler(new BinaryStatusPanel.StatusControlHandler() {
            @Override
            public void changeEditOperation(EditOperation editOperation) {
                codeArea.setEditOperation(editOperation);
            }

            @Override
            public void changeCursorPosition() {
                goToPositionAction.actionPerformed(new ActionEvent(BinEdComponentPanel.this, 0, ""));
            }

            @Override
            public void cycleEncodings() {
                if (encodingsHandler != null) {
                    encodingsHandler.cycleEncodings();
                }
            }

            @Override
            public void encodingsPopupEncodingsMenu(MouseEvent mouseEvent) {
                if (encodingsHandler != null) {
                    encodingsHandler.popupEncodingsMenu(mouseEvent);
                }
            }

            @Override
            public void changeMemoryMode(BinaryStatusApi.MemoryMode memoryMode) {
                FileHandlingMode newHandlingMode = memoryMode == BinaryStatusApi.MemoryMode.DELTA_MODE ? FileHandlingMode.DELTA : FileHandlingMode.MEMORY;
                if (newHandlingMode != fileHandlingMode) {
                    fileApi.switchFileHandlingMode(newHandlingMode);
                    preferences.getEditorPreferences().setFileHandlingMode(newHandlingMode);
                }
            }
        });
    }

    @Nullable
    public BinEdComponentFileApi getFileApi() {
        return fileApi;
    }

    public void setFileApi(BinEdComponentFileApi fileApi) {
        this.fileApi = fileApi;

        if (fileApi.isSaveSupported()) {
            toolbarPanel.setSaveAction(new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    saveDocument();
                }
            });
        }
    }

    private void switchShowValuesPanel(boolean showValuesPanel) {
        if (showValuesPanel) {
            showValuesPanel();
        } else {
            hideValuesPanel();
        }
    }

    public void registerEncodingStatus(TextEncodingStatusApi encodingStatusApi) {
        this.encodingStatus = encodingStatusApi;
        setCharsetChangeListener(() -> {
            String selectedEncoding = codeArea.getCharset().name();
            encodingStatus.setEncoding(selectedEncoding);
        });
    }

    public void setCharsetChangeListener(CharsetChangeListener charsetChangeListener) {
        this.charsetChangeListener = charsetChangeListener;
    }

    public void setModifiedChangeListener(ModifiedStateListener modifiedChangeListener) {
        this.modifiedChangeListener = modifiedChangeListener;
    }

    public boolean isModified() {
        return undoHandler != null && undoHandler.getCommandPosition() != undoHandler.getSyncPoint();
    }

    /**
     * Attempts to release current file and warn if document was modified.
     *
     * @return true if successful
     */
    public boolean releaseFile() {
        if (fileApi == null)
            return true;

        while (isModified() && fileApi.isSaveSupported()) {
            Object[] options = {
                    "Save",
                    "Discard",
                    "Cancel"
            };
            int result = JOptionPane.showOptionDialog(this,
                    "Document was modified! Do you wish to save it?",
                    "Save File?",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null, options, options[0]);
            if (result == JOptionPane.NO_OPTION) {
                return true;
            }
            if (result == JOptionPane.CANCEL_OPTION || result == JOptionPane.CLOSED_OPTION) {
                return false;
            }

            saveDocument();
        }

        return true;
    }

    private void saveDocument() {
        fileApi.saveDocument();

        undoHandler.setSyncPoint();
        notifyModified();
        documentOriginalSize = codeArea.getDataSize();
        updateCurrentDocumentSize();
        updateCurrentMemoryMode();
    }

    public void updateCurrentDocumentSize() {
        long dataSize = codeArea.getDataSize();
        binaryStatus.setCurrentDocumentSize(dataSize, documentOriginalSize);
    }

    @Nonnull
    public FileHandlingMode getFileHandlingMode() {
        return fileHandlingMode;
    }

    public void setFileHandlingMode(FileHandlingMode fileHandlingMode) {
        this.fileHandlingMode = fileHandlingMode;
        updateCurrentMemoryMode();
    }

    private void updateCurrentMemoryMode() {
        BinaryStatusApi.MemoryMode memoryMode = BinaryStatusApi.MemoryMode.RAM_MEMORY;
        if (codeArea.getEditMode() == EditMode.READ_ONLY) {
            memoryMode = BinaryStatusApi.MemoryMode.READ_ONLY;
        } else if (fileHandlingMode == FileHandlingMode.DELTA) {
            memoryMode = BinaryStatusApi.MemoryMode.DELTA_MODE;
        } else if (fileHandlingMode == FileHandlingMode.NATIVE) {
            memoryMode = BinaryStatusApi.MemoryMode.NATIVE;
        }

        if (binaryStatus != null) {
            binaryStatus.setMemoryMode(memoryMode);
        }
    }

    private void notifyModified() {
        if (modifiedChangeListener != null) {
            modifiedChangeListener.modifiedChanged();
        }

        toolbarPanel.updateModified(isModified());
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">
    private void initComponents() {

        codeAreaPanel = new javax.swing.JPanel();

        setLayout(new java.awt.BorderLayout());

        codeAreaPanel.setLayout(new java.awt.BorderLayout());
        add(codeAreaPanel, java.awt.BorderLayout.CENTER);
    }// </editor-fold>

    /**
     * Test method for this panel.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        WindowUtils.invokeDialog(new BinEdComponentPanel());
    }


    // Variables declaration - do not modify
    private javax.swing.JPanel codeAreaPanel;
    // End of variables declaration

    private void createContextMenu(final JPopupMenu menu, int x, int y) {
        BasicCodeAreaZone positionZone = codeArea.getPainter().getPositionZone(x, y);

        switch (positionZone) {
            case TOP_LEFT_CORNER:
            case HEADER: {
                menu.add(createShowHeaderMenuItem());
                menu.add(createPositionCodeTypeMenuItem());
                break;
            }
            case ROW_POSITIONS: {
                menu.add(createShowRowPositionMenuItem());
                menu.add(createPositionCodeTypeMenuItem());
                menu.add(new JSeparator());
                menu.add(createGoToMenuItem());

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
                menu.addSeparator();

                final JMenuItem selectAllMenuItem = new JMenuItem("Select All");
                selectAllMenuItem.setIcon(new ImageIcon(getClass().getResource(FRAMEWORK_TANGO_ICON_THEME_PREFIX + "edit-select-all.png")));
                selectAllMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionUtils.getMetaMask()));
                selectAllMenuItem.addActionListener((ActionEvent e) -> {
                    codeArea.selectAll();
                    menu.setVisible(false);
                });
                menu.add(selectAllMenuItem);

                JMenuItem editSelectionMenuItem = createEditSelectionMenuItem();
                menu.add(editSelectionMenuItem);
                menu.addSeparator();

                JMenuItem insertDataMenuItem = createInsertDataMenuItem();
                menu.add(insertDataMenuItem);

                JMenuItem goToMenuItem = createGoToMenuItem();
                menu.add(goToMenuItem);

                final JMenuItem findMenuItem = new JMenuItem("Find...");
                findMenuItem.setIcon(new ImageIcon(getClass().getResource(BINED_TANGO_ICON_THEME_PREFIX + "edit-find.png")));
                findMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, ActionUtils.getMetaMask()));
                findMenuItem.addActionListener((ActionEvent e) -> {
                    searchAction.actionPerformed(e);
                    searchAction.switchReplaceMode(BinarySearchPanel.SearchOperation.FIND);
                });
                menu.add(findMenuItem);

                final JMenuItem replaceMenuItem = new JMenuItem("Replace...");
                replaceMenuItem.setIcon(new ImageIcon(getClass().getResource(BINED_TANGO_ICON_THEME_PREFIX + "edit-find-replace.png")));
                replaceMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, ActionUtils.getMetaMask()));
                replaceMenuItem.setEnabled(codeArea.isEditable());
                replaceMenuItem.addActionListener((ActionEvent e) -> {
                    searchAction.actionPerformed(e);
                    searchAction.switchReplaceMode(BinarySearchPanel.SearchOperation.REPLACE);
                });
                menu.add(replaceMenuItem);
            }
        }

        menu.addSeparator();

        switch (positionZone) {
            case TOP_LEFT_CORNER:
            case HEADER:
            case ROW_POSITIONS: {
                break;
            }
            default: {
                JMenu showMenu = new JMenu("Show");
                showMenu.add(createShowHeaderMenuItem());
                showMenu.add(createShowRowPositionMenuItem());
                menu.add(showMenu);
            }
        }

        JMenuItem compareFilesMenuItem = createCompareFilesMenuItem();
        menu.add(compareFilesMenuItem);
        JMenuItem reloadFileMenuItem = createReloadFileMenuItem();
        menu.add(reloadFileMenuItem);

        final JMenuItem optionsMenuItem = new JMenuItem("Options...");
        optionsMenuItem.setIcon(new ImageIcon(getClass().getResource("/org/exbin/framework/options/gui/resources/icons/Preferences16.gif")));
        optionsMenuItem.addActionListener(createOptionsAction());
        menu.add(optionsMenuItem);

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
                    //            dialog.setSize(650, 460);
                    dialog.showCentered((Component) e.getSource());
                });
                menu.add(aboutMenuItem);
            }
        }
    }

    @Nonnull
    private AbstractAction createOptionsAction() {
        return new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final BinEdOptionsPanelBorder optionsPanelWrapper = new BinEdOptionsPanelBorder();
                optionsPanelWrapper.setPreferredSize(new Dimension(700, 460));
                BinEdOptionsPanel optionsPanel = optionsPanelWrapper.getOptionsPanel();
                optionsPanel.setPreferences(preferences);
                optionsPanel.setTextFontService(new TextFontService() {
                    @Nonnull
                    @Override
                    public Font getCurrentFont() {
                        return codeArea.getCodeFont();
                    }

                    @Nonnull
                    @Override
                    public Font getDefaultFont() {
                        return defaultFont;
                    }

                    @Override
                    public void setCurrentFont(Font font) {
                        codeArea.setCodeFont(font);
                    }
                });
                optionsPanel.loadFromPreferences();
                updateApplyOptions(optionsPanel);
                OptionsControlPanel optionsControlPanel = new OptionsControlPanel();
                JPanel dialogPanel = WindowUtils.createDialogPanel(optionsPanelWrapper, optionsControlPanel);
                WindowUtils.DialogWrapper dialog = WindowUtils.createDialog(dialogPanel, (Component) e.getSource(), "Options", Dialog.ModalityType.APPLICATION_MODAL);
                optionsControlPanel.setHandler((OptionsControlHandler.ControlActionType actionType) -> {
                    if (actionType != OptionsControlHandler.ControlActionType.CANCEL) {
                        optionsPanel.applyToOptions();
                        if (actionType == OptionsControlHandler.ControlActionType.SAVE) {
                            optionsPanel.saveToPreferences();
                        }
                        applyOptions(optionsPanel);
                        fileApi.switchFileHandlingMode(optionsPanel.getEditorOptions().getFileHandlingMode());
                        codeArea.repaint();
                    }

                    dialog.close();
                });
                dialog.showCentered((Component) e.getSource());
                dialog.dispose();
            }
        };
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
    private JMenuItem createGoToMenuItem() {
        final JMenuItem goToMenuItem = new JMenuItem("Go To...");
        goToMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, ActionUtils.getMetaMask()));
        goToMenuItem.addActionListener(goToPositionAction);
        return goToMenuItem;
    }

    @Nonnull
    private JMenuItem createEditSelectionMenuItem() {
        final JMenuItem editSelectionMenuItem = new JMenuItem("Edit Selection...");
        editSelectionMenuItem.addActionListener(editSelectionAction);
        return editSelectionMenuItem;
    }

    @Nonnull
    private JMenuItem createInsertDataMenuItem() {
        final JMenuItem insertDataMenuItem = new JMenuItem("Insert Data...");
        insertDataMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, ActionUtils.getMetaMask()));
        insertDataMenuItem.addActionListener(insertDataAction);
        return insertDataMenuItem;
    }

    @Nonnull
    private JMenuItem createCompareFilesMenuItem() {
        final JMenuItem compareFilesMenuItem = new JMenuItem("Compare Files...");
        compareFilesMenuItem.addActionListener(compareFilesAction);
        return compareFilesMenuItem;
    }

    @Nonnull
    private JMenuItem createReloadFileMenuItem() {
        final JMenuItem reloadFileMenuItem = new JMenuItem("Reload File");
        reloadFileMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, ActionUtils.getMetaMask() + KeyEvent.ALT_DOWN_MASK));
        reloadFileMenuItem.addActionListener(reloadFileAction);
        return reloadFileMenuItem;
    }

    @Nonnull
    private JMenuItem createShowHeaderMenuItem() {
        final JCheckBoxMenuItem showHeader = new JCheckBoxMenuItem("Show Header");
        showHeader.setSelected(codeArea.getLayoutProfile().isShowHeader());
        showHeader.addActionListener(showHeaderAction);
        return showHeader;
    }

    @Nonnull
    private JMenuItem createShowRowPositionMenuItem() {
        final JCheckBoxMenuItem showRowPosition = new JCheckBoxMenuItem("Show Row Position");
        showRowPosition.setSelected(codeArea.getLayoutProfile().isShowRowPosition());
        showRowPosition.addActionListener(showRowNumbersAction);
        return showRowPosition;
    }

    @Nonnull
    private JMenuItem createPositionCodeTypeMenuItem() {
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

    private void updateApplyOptions(BinEdApplyOptions applyOptions) {
        CodeAreaOptionsImpl.applyFromCodeArea(applyOptions.getCodeAreaOptions(), codeArea);
        applyOptions.getEncodingOptions().setSelectedEncoding(((CharsetCapable) codeArea).getCharset().name());

        EditorOptions editorOptions = applyOptions.getEditorOptions();
        editorOptions.setShowValuesPanel(valuesPanelVisible);
        editorOptions.setFileHandlingMode(fileHandlingMode);
        if (codeArea.getCommandHandler() instanceof CodeAreaOperationCommandHandler) {
            editorOptions.setEnterKeyHandlingMode(((CodeAreaOperationCommandHandler) codeArea.getCommandHandler()).getEnterKeyHandlingMode());
        }

        // TODO applyOptions.getStatusOptions().loadFromPreferences(preferences.getStatusPreferences());
    }

    private void applyOptions(BinEdApplyOptions applyOptions) {
        CodeAreaOptionsImpl.applyToCodeArea(applyOptions.getCodeAreaOptions(), codeArea);

        ((CharsetCapable) codeArea).setCharset(Charset.forName(applyOptions.getEncodingOptions().getSelectedEncoding()));
        encodingsHandler.setEncodings(applyOptions.getEncodingOptions().getEncodings());
        ((FontCapable) codeArea).setCodeFont(applyOptions.getFontOptions().isUseDefaultFont() ? defaultFont : applyOptions.getFontOptions().getFont(defaultFont));

        EditorOptions editorOptions = applyOptions.getEditorOptions();
        switchShowValuesPanel(editorOptions.isShowValuesPanel());
        if (codeArea.getCommandHandler() instanceof CodeAreaOperationCommandHandler) {
            ((CodeAreaOperationCommandHandler) codeArea.getCommandHandler()).setEnterKeyHandlingMode(editorOptions.getEnterKeyHandlingMode());
        }

        StatusOptions statusOptions = applyOptions.getStatusOptions();
        statusPanel.setStatusOptions(statusOptions);
        toolbarPanel.applyFromCodeArea();

        CodeAreaLayoutOptions layoutOptions = applyOptions.getLayoutOptions();
        int selectedLayoutProfile = layoutOptions.getSelectedProfile();
        if (selectedLayoutProfile >= 0) {
            codeArea.setLayoutProfile(layoutOptions.getLayoutProfile(selectedLayoutProfile));
        } else {
            codeArea.setLayoutProfile(defaultLayoutProfile);
        }

        CodeAreaThemeOptions themeOptions = applyOptions.getThemeOptions();
        int selectedThemeProfile = themeOptions.getSelectedProfile();
        if (selectedThemeProfile >= 0) {
            codeArea.setThemeProfile(themeOptions.getThemeProfile(selectedThemeProfile));
        } else {
            codeArea.setThemeProfile(defaultThemeProfile);
        }

        CodeAreaColorOptions colorOptions = applyOptions.getColorOptions();
        int selectedColorProfile = colorOptions.getSelectedProfile();
        if (selectedColorProfile >= 0) {
            codeArea.setColorsProfile(colorOptions.getColorsProfile(selectedColorProfile));
        } else {
            codeArea.setColorsProfile(defaultColorProfile);
        }
    }

    public void showValuesPanel() {
        if (!valuesPanelVisible) {
            valuesPanelVisible = true;
            if (valuesPanel == null) {
                valuesPanel = new ValuesPanel();
                valuesPanel.setCodeArea(codeArea, undoHandler);
                valuesPanelScrollPane = new JBScrollPane(valuesPanel);
                valuesPanelScrollPane.setBorder(null);
            }
            this.add(valuesPanelScrollPane, BorderLayout.EAST);
            valuesPanel.enableUpdate();
            valuesPanel.updateValues();
            this.revalidate();
            revalidate();
        }
    }

    public void hideValuesPanel() {
        if (valuesPanelVisible) {
            valuesPanelVisible = false;
            valuesPanel.disableUpdate();
            this.remove(valuesPanelScrollPane);
            this.revalidate();
            revalidate();
        }
    }

    @Nonnull
    public ExtCodeArea getCodeArea() {
        return codeArea;
    }

    private void initialLoadFromPreferences() {
        applyOptions(new BinEdApplyOptions() {
            @Nonnull
            @Override
            public CodeAreaOptions getCodeAreaOptions() {
                return preferences.getCodeAreaPreferences();
            }

            @Nonnull
            @Override
            public TextEncodingOptions getEncodingOptions() {
                return preferences.getEncodingPreferences();
            }

            @Nonnull
            @Override
            public TextFontOptions getFontOptions() {
                return preferences.getFontPreferences();
            }

            @Nonnull
            @Override
            public IntegrationOptions getIntegrationOptions() {
                return preferences.getIntegrationPreferences();
            }

            @Nonnull
            @Override
            public EditorOptions getEditorOptions() {
                return preferences.getEditorPreferences();
            }

            @Nonnull
            @Override
            public StatusOptions getStatusOptions() {
                return preferences.getStatusPreferences();
            }

            @Nonnull
            @Override
            public CodeAreaLayoutOptions getLayoutOptions() {
                return preferences.getLayoutPreferences();
            }

            @Nonnull
            @Override
            public CodeAreaColorOptions getColorOptions() {
                return preferences.getColorPreferences();
            }

            @Nonnull
            @Override
            public CodeAreaThemeOptions getThemeOptions() {
                return preferences.getThemePreferences();
            }
        });

        encodingsHandler.loadFromPreferences(preferences.getEncodingPreferences());
        statusPanel.loadFromPreferences(preferences.getStatusPreferences());
        toolbarPanel.loadFromPreferences();

        fileHandlingMode = preferences.getEditorPreferences().getFileHandlingMode();
        updateCurrentMemoryMode();
    }

    public BinaryDataUndoHandler getUndoHandler() {
        return undoHandler;
    }

    public void setUndoHandler(BinaryDataUndoHandler undoHandler) {
        this.undoHandler = undoHandler;
        toolbarPanel.setUndoHandler(undoHandler);
        CodeAreaOperationCommandHandler commandHandler = new CodeAreaOperationCommandHandler(codeArea, undoHandler);
        codeArea.setCommandHandler(commandHandler);
        if (valuesPanel != null) {
            valuesPanel.setCodeArea(codeArea, undoHandler);
        }
        insertDataAction.setUndoHandler(undoHandler);
        // TODO set ENTER KEY mode in apply options

        undoHandler.addUndoUpdateListener(new BinaryDataUndoUpdateListener() {
            @Override
            public void undoCommandPositionChanged() {
                codeArea.repaint();
                updateCurrentDocumentSize();
                notifyModified();
            }

            @Override
            public void undoCommandAdded(@Nonnull final BinaryDataCommand command) {
                updateCurrentDocumentSize();
                notifyModified();
            }
        });
    }

    @Nullable
    public BinaryData getContentData() {
        return codeArea.getContentData();
    }

    public void setContentData(@Nullable BinaryData data) {
        codeArea.setContentData(data);

        documentOriginalSize = codeArea.getDataSize();
        updateCurrentDocumentSize();
        updateCurrentMemoryMode();

        // Autodetect encoding using IDE mechanism
//        final Charset charset = Charset.forName(FileEncodingQuery.getEncoding(dataObject.getPrimaryFile()).name());
//        if (charsetChangeListener != null) {
//            charsetChangeListener.charsetChanged();
//        }
//        codeArea.setCharset(charset);
    }

    @Nonnull
    public static PropertiesComponent getPreferences() {
        return PropertiesComponent.getInstance();
    }

    public interface CharsetChangeListener {

        void charsetChanged();
    }

    public interface ModifiedStateListener {

        void modifiedChanged();
    }
}
