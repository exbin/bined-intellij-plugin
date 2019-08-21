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

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.components.JBScrollPane;
import org.exbin.bined.BasicCodeAreaZone;
import org.exbin.bined.CodeType;
import org.exbin.bined.EditationMode;
import org.exbin.bined.EditationOperation;
import org.exbin.bined.capability.CharsetCapable;
import org.exbin.bined.extended.layout.ExtendedCodeAreaLayoutProfile;
import org.exbin.bined.highlight.swing.extended.ExtendedHighlightNonAsciiCodeAreaPainter;
import org.exbin.bined.intellij.BinEdApplyOptions;
import org.exbin.bined.intellij.BinEdIntelliJPlugin;
import org.exbin.bined.intellij.DialogUtils;
import org.exbin.bined.intellij.GoToPositionAction;
import org.exbin.bined.swing.basic.DefaultCodeAreaCommandHandler;
import org.exbin.bined.swing.basic.color.CodeAreaColorsProfile;
import org.exbin.bined.swing.extended.ExtCodeArea;
import org.exbin.bined.swing.extended.theme.ExtendedCodeAreaThemeProfile;
import org.exbin.framework.bined.BinaryStatusApi;
import org.exbin.framework.bined.options.*;
import org.exbin.framework.bined.options.impl.CodeAreaOptionsImpl;
import org.exbin.framework.bined.panel.BinaryStatusPanel;
import org.exbin.framework.bined.preferences.BinaryEditorPreferences;
import org.exbin.framework.editor.text.EncodingsHandler;
import org.exbin.framework.editor.text.TextEncodingStatusApi;
import org.exbin.framework.editor.text.options.TextEncodingOptions;
import org.exbin.framework.editor.text.options.TextFontOptions;
import org.exbin.framework.gui.utils.ActionUtils;
import org.exbin.framework.preferences.PreferencesWrapper;
import org.exbin.utils.binary_data.BinaryData;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.nio.charset.Charset;

/**
 * Debugger value binary editor panel.
 *
 * @author ExBin Project (http://exbin.org)
 * @version 0.2.1 2019/07/21
 */
public class DebugViewPanel extends JPanel {

    private BinaryEditorPreferences preferences;
    private JPanel headerPanel;
    private ExtCodeArea codeArea;
    private ValuesPanel valuesPanel = null;
    private boolean valuesPanelVisible = false;
    private final ExtendedCodeAreaLayoutProfile defaultLayoutProfile;
    private final ExtendedCodeAreaThemeProfile defaultThemeProfile;
    private final CodeAreaColorsProfile defaultColorProfile;
    private final Font defaultFont;

    private BinaryStatusPanel statusPanel;
    private BinaryStatusApi binaryStatus;
    private TextEncodingStatusApi encodingStatus;
    private CharsetChangeListener charsetChangeListener = null;
    private GoToPositionAction goToRowAction;
    private EncodingsHandler encodingsHandler;
    private boolean findTextPanelVisible = false;
    private BinarySearchPanel binarySearchPanel = null;
    private JScrollPane valuesPanelScrollPane = null;

    public DebugViewPanel() {
        setLayout(new BorderLayout());
        preferences = new BinaryEditorPreferences(new PreferencesWrapper(getPreferences(), BinEdIntelliJPlugin.PLUGIN_PREFIX));

        initComponents();
        codeArea = new ExtCodeArea();
        codeArea.setEditationMode(EditationMode.READ_ONLY);

        codeArea.setPainter(new ExtendedHighlightNonAsciiCodeAreaPainter(codeArea));
        defaultFont = new Font(Font.MONOSPACED, Font.PLAIN, 12);
        codeArea.setCodeFont(defaultFont);
        codeArea.getCaret().setBlinkRate(300);
        defaultLayoutProfile = codeArea.getLayoutProfile();
        defaultThemeProfile = codeArea.getThemeProfile();
        defaultColorProfile = codeArea.getColorsProfile();

        statusPanel = new BinaryStatusPanel();
        registerEncodingStatus(statusPanel);
        encodingsHandler = new EncodingsHandler();
        encodingsHandler.setParentComponent(this);
        encodingsHandler.init();
        encodingsHandler.setTextEncodingStatus(new TextEncodingStatusApi() {
            @Override
            public String getEncoding() {
                return encodingStatus.getEncoding();
            }

            @Override
            public void setEncoding(String encodingName) {
                codeArea.setCharset(Charset.forName(encodingName));
                encodingStatus.setEncoding(encodingName);
            }
        });

        add(codeArea, BorderLayout.CENTER);
        add(headerPanel, java.awt.BorderLayout.NORTH);
        add(statusPanel, java.awt.BorderLayout.SOUTH);

        registerBinaryStatus(statusPanel);

        initialLoadFromPreferences();

        goToRowAction = new GoToPositionAction(codeArea);

        applyFromCodeArea();

        codeArea.setComponentPopupMenu(new JPopupMenu() {
            @Override
            public void show(Component invoker, int x, int y) {
                int clickedX = x;
                int clickedY = y;
                if (invoker instanceof JViewport) {
                    clickedX += ((JViewport) invoker).getParent().getX();
                    clickedY += ((JViewport) invoker).getParent().getY();
                }
                JPopupMenu popupMenu = createContextMenu(clickedX, clickedY);
                popupMenu.show(invoker, x, y);
            }
        });

        codeTypeComboBox.setSelectedIndex(codeArea.getCodeType().ordinal());

        codeArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent keyEvent) {
                int modifiers = keyEvent.getModifiers();
                if (modifiers == ActionUtils.getMetaMask()) {
                    int keyCode = keyEvent.getKeyCode();
                    switch (keyCode) {
                        case KeyEvent.VK_F: {
//                            showSearchPanel(false);
                            break;
                        }
                        case KeyEvent.VK_G: {
                            goToRowAction.actionPerformed(new ActionEvent(keyEvent.getSource(), keyEvent.getID(), ""));
                            break;
                        }
                    }
                }
            }
        });

    }

    private ComboBox<String> codeTypeComboBox;
    private javax.swing.JToolBar controlToolBar;
    private javax.swing.JPanel infoToolbar;
    private javax.swing.JToolBar.Separator jSeparator3;
    private javax.swing.JToggleButton showUnprintablesToggleButton;

    private void initComponents() {
        infoToolbar = new javax.swing.JPanel();
        controlToolBar = new javax.swing.JToolBar();
        showUnprintablesToggleButton = new javax.swing.JToggleButton();
        jSeparator3 = new javax.swing.JToolBar.Separator();
        codeTypeComboBox = new ComboBox<>();

        controlToolBar.setBorder(null);
        controlToolBar.setFloatable(false);
        controlToolBar.setRollover(true);

        showUnprintablesToggleButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/exbin/bined/intellij/resources/icons/insert-pilcrow.png")));
        showUnprintablesToggleButton.setToolTipText("Show symbols for unprintable/whitespace characters");
        showUnprintablesToggleButton.addActionListener(this::showUnprintablesToggleButtonActionPerformed);
        controlToolBar.add(showUnprintablesToggleButton);
        controlToolBar.add(jSeparator3);

        JPanel spacePanel = new JPanel();
        spacePanel.setLayout(new BorderLayout());
        codeTypeComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{"BIN", "OCT", "DEC", "HEX"}));
        codeTypeComboBox.addActionListener(this::codeTypeComboBoxActionPerformed);
        spacePanel.add(codeTypeComboBox, BorderLayout.WEST);
        controlToolBar.add(spacePanel);

        javax.swing.GroupLayout infoToolbarLayout = new javax.swing.GroupLayout(infoToolbar);
        infoToolbar.setLayout(infoToolbarLayout);
        infoToolbarLayout.setHorizontalGroup(
                infoToolbarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(infoToolbarLayout.createSequentialGroup()
                                .addComponent(controlToolBar, javax.swing.GroupLayout.DEFAULT_SIZE, 366, Short.MAX_VALUE))
        );
        infoToolbarLayout.setVerticalGroup(
                infoToolbarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(controlToolBar, javax.swing.GroupLayout.PREFERRED_SIZE, 25, Short.MAX_VALUE)
        );

        headerPanel = new JPanel();
        headerPanel.setLayout(new java.awt.BorderLayout());
        headerPanel.add(infoToolbar, java.awt.BorderLayout.CENTER);
    }

    private JPopupMenu createContextMenu(int x, int y) {
        final JPopupMenu result = new JPopupMenu();

        BasicCodeAreaZone positionZone = codeArea.getPositionZone(x, y);

        final JMenuItem copyMenuItem = new JMenuItem("Copy");
        copyMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionUtils.getMetaMask()));
        copyMenuItem.setEnabled(codeArea.hasSelection());
        copyMenuItem.addActionListener(e -> {
            codeArea.copy();
            result.setVisible(false);
        });
        result.add(copyMenuItem);

        final JMenuItem copyAsCodeMenuItem = new JMenuItem("Copy as Code");
        copyAsCodeMenuItem.setEnabled(codeArea.hasSelection());
        copyAsCodeMenuItem.addActionListener(e -> {
            codeArea.copyAsCode();
            result.setVisible(false);
        });
        result.add(copyAsCodeMenuItem);

        result.addSeparator();

        final JMenuItem selectAllMenuItem = new JMenuItem("Select All");
        selectAllMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionUtils.getMetaMask()));
        selectAllMenuItem.addActionListener(e -> {
            codeArea.selectAll();
            result.setVisible(false);
        });
        result.add(selectAllMenuItem);
        result.addSeparator();

        final JMenuItem goToMenuItem = new JMenuItem("Go To" + DialogUtils.DIALOG_MENUITEM_EXT);
        goToMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, ActionUtils.getMetaMask()));
        goToMenuItem.addActionListener(goToRowAction);
        result.add(goToMenuItem);

        return result;
    }

    private void showUnprintablesToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {
        codeArea.setShowUnprintables(showUnprintablesToggleButton.isSelected());
    }

    private void codeTypeComboBoxActionPerformed(java.awt.event.ActionEvent evt) {
        CodeType codeType = CodeType.values()[codeTypeComboBox.getSelectedIndex()];
        codeArea.setCodeType(codeType);
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

    private void applyFromCodeArea() {
        codeTypeComboBox.setSelectedIndex(codeArea.getCodeType().ordinal());
        showUnprintablesToggleButton.setSelected(codeArea.isShowUnprintables());
    }

    public void registerBinaryStatus(BinaryStatusApi binaryStatusApi) {
        this.binaryStatus = binaryStatusApi;
        codeArea.addCaretMovedListener(caretPosition -> {
            binaryStatus.setCursorPosition(caretPosition);
        });

        binaryStatus.setControlHandler(new BinaryStatusApi.StatusControlHandler() {
            @Override
            public void changeEditationOperation(EditationOperation editationOperation) {
                codeArea.setEditationOperation(editationOperation);
            }

            @Override
            public void changeCursorPosition() {
                goToRowAction.actionPerformed(null);
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
            }
        });
    }

    private void initialLoadFromPreferences() {
        applyOptions(new BinEdApplyOptions() {
            @Override
            public CodeAreaOptions getCodeAreaOptions() {
                return preferences.getCodeAreaPreferences();
            }

            @Override
            public TextEncodingOptions getEncodingOptions() {
                return preferences.getEncodingPreferences();
            }

            @Override
            public TextFontOptions getFontOptions() {
                return preferences.getFontPreferences();
            }

            @Override
            public EditorOptions getEditorOptions() {
                return preferences.getEditorPreferences();
            }

            @Override
            public StatusOptions getStatusOptions() {
                return preferences.getStatusPreferences();
            }

            @Override
            public CodeAreaLayoutOptions getLayoutOptions() {
                return preferences.getLayoutPreferences();
            }

            @Override
            public CodeAreaColorOptions getColorOptions() {
                return preferences.getColorPreferences();
            }

            @Override
            public CodeAreaThemeOptions getThemeOptions() {
                return preferences.getThemePreferences();
            }
        });

        encodingsHandler.loadFromPreferences(preferences.getEncodingPreferences());
        statusPanel.loadFromPreferences(preferences.getStatusPreferences());
        toolbarPanelLoadFromPreferences();
    }

    private void applyOptions(BinEdApplyOptions applyOptions) {
        CodeAreaOptionsImpl.applyToCodeArea(applyOptions.getCodeAreaOptions(), codeArea);

        ((CharsetCapable) codeArea).setCharset(Charset.forName(applyOptions.getEncodingOptions().getSelectedEncoding()));
        encodingsHandler.setEncodings(applyOptions.getEncodingOptions().getEncodings());

        EditorOptions editorOptions = applyOptions.getEditorOptions();
        if (editorOptions.isShowValuesPanel()) {
            showValuesPanel();
        }
        ((DefaultCodeAreaCommandHandler) codeArea.getCommandHandler()).setEnterKeyHandlingMode(editorOptions.getEnterKeyHandlingMode());

        StatusOptions statusOptions = applyOptions.getStatusOptions();
        statusPanel.setStatusOptions(statusOptions);
        toolbarPanelLoadFromPreferences();

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

    private void toolbarPanelLoadFromPreferences() {
        codeTypeComboBox.setSelectedIndex(preferences.getCodeAreaPreferences().getCodeType().ordinal());
        showUnprintablesToggleButton.setSelected(preferences.getCodeAreaPreferences().isShowUnprintables());
    }

    public void showValuesPanel() {
        if (!valuesPanelVisible) {
            valuesPanelVisible = true;
            if (valuesPanel == null) {
                valuesPanel = new ValuesPanel();
                valuesPanel.setCodeArea(codeArea, null);
                valuesPanelScrollPane = new JBScrollPane(valuesPanel);
            }
            add(valuesPanelScrollPane, BorderLayout.EAST);
            valuesPanel.enableUpdate();
            valuesPanel.updateValues();
            valuesPanelScrollPane.revalidate();
            valuesPanel.revalidate();
            revalidate();
        }
    }

    public void hideValuesPanel() {
        if (valuesPanelVisible) {
            valuesPanelVisible = false;
            valuesPanel.disableUpdate();
            remove(valuesPanelScrollPane);
            revalidate();
        }
    }

    public static PropertiesComponent getPreferences() {
        return PropertiesComponent.getInstance();
    }

    public interface CharsetChangeListener {

        void charsetChanged();
    }

    public void setData(BinaryData data) {
        codeArea.setContentData(data);
    }
}
