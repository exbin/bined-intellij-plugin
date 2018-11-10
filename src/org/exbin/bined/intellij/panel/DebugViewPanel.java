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
import org.exbin.bined.*;
import org.exbin.bined.basic.BasicBackgroundPaintMode;
import org.exbin.bined.capability.RowWrappingCapable;
import org.exbin.bined.capability.RowWrappingCapable.RowWrappingMode;
import org.exbin.bined.highlight.swing.HighlightNonAsciiCodeAreaPainter;
import org.exbin.bined.intellij.BinEdFileEditor;
import org.exbin.bined.intellij.DialogUtils;
import org.exbin.bined.intellij.EncodingsHandler;
import org.exbin.bined.intellij.GoToHandler;
import org.exbin.bined.swing.extended.ExtCodeArea;
import org.exbin.framework.bined.HexStatusApi;
import org.exbin.framework.bined.panel.HexStatusPanel;
import org.exbin.framework.editor.text.TextEncodingStatusApi;
import org.exbin.framework.editor.text.panel.TextFontOptionsPanel;
import org.exbin.utils.binary_data.BinaryData;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.font.TextAttribute;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * Debugger value hexadecimal editor panel.
 *
 * @author ExBin Project (http://exbin.org)
 * @version 0.2.0 2018/11/10
 */
public class DebugViewPanel extends JPanel {

    private PropertiesComponent preferences;
    private JPanel headerPanel;
    private ExtCodeArea codeArea;
    private ValuesPanel valuesPanel = null;
    private boolean valuesPanelVisible = false;

    private final int metaMask;
    private HexStatusPanel statusPanel;
    private HexStatusApi hexStatus;
    private TextEncodingStatusApi encodingStatus;
    private CharsetChangeListener charsetChangeListener = null;
    private GoToHandler goToHandler;
    private EncodingsHandler encodingsHandler;
    private boolean findTextPanelVisible = false;
    private HexSearchPanel hexSearchPanel = null;
    private JScrollPane valuesPanelScrollPane = null;

    public DebugViewPanel() {
        setLayout(new BorderLayout());
        preferences = BinEdFileEditor.getPreferences();

        initComponents();
        codeArea = new ExtCodeArea();
        codeArea.setEditationMode(EditationMode.READ_ONLY);

        codeArea.setPainter(new HighlightNonAsciiCodeAreaPainter(codeArea));
        codeArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        codeArea.getCaret().setBlinkRate(300);

        statusPanel = new HexStatusPanel(false);
        registerEncodingStatus(statusPanel);
        encodingsHandler = new EncodingsHandler(new TextEncodingStatusApi() {
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

        registerHexStatus(statusPanel);

        loadFromPreferences();

        goToHandler = new GoToHandler(codeArea);

        applyFromCodeArea();

        int metaMaskValue;
        try {
            metaMaskValue = java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        } catch (java.awt.HeadlessException ex) {
            metaMaskValue = java.awt.Event.CTRL_MASK;
        }

        metaMask = metaMaskValue;

        codeArea.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                maybeShowPopup(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                maybeShowPopup(e);
            }

            private void maybeShowPopup(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    JPopupMenu popupMenu = createContextMenu();
                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });

        codeTypeComboBox.setSelectedIndex(codeArea.getCodeType().ordinal());

        codeArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent keyEvent) {
                int modifiers = keyEvent.getModifiers();
                if (modifiers == metaMask) {
                    int keyCode = keyEvent.getKeyCode();
                    switch (keyCode) {
                        case KeyEvent.VK_F: {
//                            showSearchPanel(false);
                            break;
                        }
                        case KeyEvent.VK_G: {
                            goToHandler.getGoToLineAction().actionPerformed(null);
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
    private javax.swing.JButton saveFileButton;
    private javax.swing.JButton undoEditButton;
    private javax.swing.JButton redoEditButton;
    private javax.swing.JToggleButton lineWrappingToggleButton;
    private javax.swing.JToggleButton showUnprintablesToggleButton;

    private void initComponents() {
        infoToolbar = new javax.swing.JPanel();
        controlToolBar = new javax.swing.JToolBar();
        lineWrappingToggleButton = new javax.swing.JToggleButton();
        showUnprintablesToggleButton = new javax.swing.JToggleButton();
        jSeparator3 = new javax.swing.JToolBar.Separator();
        codeTypeComboBox = new ComboBox<>();

        controlToolBar.setBorder(null);
        controlToolBar.setFloatable(false);
        controlToolBar.setRollover(true);

        lineWrappingToggleButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/exbin/bined/intellij/resources/icons/bined-linewrap.png")));
        lineWrappingToggleButton.setToolTipText("Wrap line to window size");
        lineWrappingToggleButton.addActionListener(this::lineWrappingToggleButtonActionPerformed);
        controlToolBar.add(lineWrappingToggleButton);

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

    private JPopupMenu createContextMenu() {
        final JPopupMenu result = new JPopupMenu();

        final JMenuItem copyMenuItem = new JMenuItem("Copy");
        copyMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, metaMask));
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
        selectAllMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, metaMask));
        selectAllMenuItem.addActionListener(e -> {
            codeArea.selectAll();
            result.setVisible(false);
        });
        result.add(selectAllMenuItem);
        result.addSeparator();

        final JMenuItem goToMenuItem = new JMenuItem("Go To" + DialogUtils.DIALOG_MENUITEM_EXT);
        goToMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, metaMask));
        goToMenuItem.addActionListener(e -> goToHandler.getGoToLineAction().actionPerformed(null));
        result.add(goToMenuItem);

        return result;
    }

    private void lineWrappingToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {
        codeArea.setRowWrapping(lineWrappingToggleButton.isSelected() ? RowWrappingCapable.RowWrappingMode.WRAPPING : RowWrappingCapable.RowWrappingMode.NO_WRAPPING);
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
        lineWrappingToggleButton.setSelected(codeArea.getRowWrapping() == RowWrappingCapable.RowWrappingMode.WRAPPING);
    }

    public void registerHexStatus(HexStatusApi hexStatusApi) {
        this.hexStatus = hexStatusApi;
        codeArea.addCaretMovedListener(caretPosition -> {
            String position = String.valueOf(caretPosition.getDataPosition());
            position += ":" + caretPosition.getCodeOffset();
            hexStatus.setCursorPosition(position);
        });

        hexStatus.setControlHandler(new HexStatusApi.StatusControlHandler() {
            @Override
            public void changeEditationMode(EditationMode editationMode) {
                codeArea.setEditationMode(editationMode);
            }

            @Override
            public void changeCursorPosition() {
                goToHandler.getGoToLineAction().actionPerformed(null);
            }

            @Override
            public void cycleEncodings() {
                if (encodingsHandler != null) {
                    encodingsHandler.cycleEncodings();
                }
            }

            @Override
            public void popupEncodingsMenu(MouseEvent mouseEvent) {
                if (encodingsHandler != null) {
                    encodingsHandler.popupEncodingsMenu(mouseEvent);
                }
            }

            @Override
            public void changeMemoryMode(HexStatusApi.MemoryMode memoryMode) {
            }
        });
    }

    private void loadFromPreferences() {
        CodeType codeType = CodeType.valueOf(preferences.getValue(BinEdFileEditor.PREFERENCES_CODE_TYPE, "HEXADECIMAL"));
        codeArea.setCodeType(codeType);
        codeTypeComboBox.setSelectedIndex(codeType.ordinal());
        String selectedEncoding = preferences.getValue(BinEdFileEditor.PREFERENCES_ENCODING_SELECTED, "UTF-8");
        statusPanel.setEncoding(selectedEncoding);
        codeArea.setCharset(Charset.forName(selectedEncoding));
        int bytesPerLine = preferences.getInt(BinEdFileEditor.PREFERENCES_BYTES_PER_LINE, 16);
        // TODO codeArea.setLineLength(bytesPerLine);

        boolean showNonprintables = preferences.getBoolean(BinEdFileEditor.PREFERENCES_SHOW_UNPRINTABLES, false);
        showUnprintablesToggleButton.setSelected(showNonprintables);
        codeArea.setShowUnprintables(showNonprintables);

        boolean lineWrapping = preferences.getBoolean(BinEdFileEditor.PREFERENCES_LINE_WRAPPING, false);
        codeArea.setRowWrapping(lineWrapping ? RowWrappingMode.WRAPPING : RowWrappingMode.NO_WRAPPING);
        lineWrappingToggleButton.setSelected(lineWrapping);

        encodingsHandler.loadFromPreferences(preferences);

        // Layout
        /* TODO codeArea.setShowHeader(preferences.getBoolean(BinEdFileEditor.PREFERENCES_SHOW_HEADER, true));
        String headerSpaceTypeName = preferences.getValue(BinEdFileEditor.PREFERENCES_HEADER_SPACE_TYPE, CodeAreaSpace.SpaceType.HALF_UNIT.name());
        codeArea.setHeaderSpaceType(CodeAreaSpace.SpaceType.valueOf(headerSpaceTypeName));
        codeArea.setHeaderSpaceSize(preferences.getInt(BinEdFileEditor.PREFERENCES_HEADER_SPACE, 0));
        codeArea.setShowLineNumbers(preferences.getBoolean(BinEdFileEditor.PREFERENCES_SHOW_LINE_NUMBERS, true));
        String lineNumbersSpaceTypeName = preferences.getValue(BinEdFileEditor.PREFERENCES_LINE_NUMBERS_SPACE_TYPE, CodeAreaSpace.SpaceType.ONE_UNIT.name());
        codeArea.setLineNumberSpaceType(CodeAreaSpace.SpaceType.valueOf(lineNumbersSpaceTypeName));
        codeArea.setLineNumberSpaceSize(preferences.getInt(BinEdFileEditor.PREFERENCES_LINE_NUMBERS_SPACE, 8));
        String lineNumbersLengthTypeName = preferences.getValue(BinEdFileEditor.PREFERENCES_LINE_NUMBERS_LENGTH_TYPE, CodeAreaLineNumberLength.LineNumberType.SPECIFIED.name());
        codeArea.setLineNumberType(CodeAreaLineNumberLength.LineNumberType.valueOf(lineNumbersLengthTypeName));
        codeArea.setLineNumberSpecifiedLength(preferences.getInt(BinEdFileEditor.PREFERENCES_LINE_NUMBERS_LENGTH, 8));
        codeArea.setByteGroupSize(preferences.getInt(BinEdFileEditor.PREFERENCES_BYTE_GROUP_SIZE, 1));
        codeArea.setSpaceGroupSize(preferences.getInt(BinEdFileEditor.PREFERENCES_SPACE_GROUP_SIZE, 0)); */

        // Mode
        codeArea.setViewMode(CodeAreaViewMode.valueOf(preferences.getValue(BinEdFileEditor.PREFERENCES_VIEW_MODE, CodeAreaViewMode.DUAL.name())));
        codeArea.setCodeType(CodeType.valueOf(preferences.getValue(BinEdFileEditor.PREFERENCES_CODE_TYPE, CodeType.HEXADECIMAL.name())));
        ((HighlightNonAsciiCodeAreaPainter) codeArea.getPainter()).setNonAsciiHighlightingEnabled(preferences.getBoolean(BinEdFileEditor.PREFERENCES_CODE_COLORIZATION, true));
        // Memory mode handled from outside by isDeltaMemoryMode() method, worth fixing?

        // Decoration
        codeArea.setBackgroundPaintMode(convertBackgroundPaintMode(preferences.getValue(BinEdFileEditor.PREFERENCES_BACKGROUND_MODE, BasicBackgroundPaintMode.STRIPED.name())));
        /* TODO codeArea.setLineNumberBackground(preferences.getBoolean(BinEdFileEditor.PREFERENCES_PAINT_LINE_NUMBERS_BACKGROUND, true));
        int decorationMode = (preferences.getBoolean(BinEdFileEditor.PREFERENCES_DECORATION_HEADER_LINE, true) ? CodeArea.DECORATION_HEADER_LINE : 0)
                + (preferences.getBoolean(BinEdFileEditor.PREFERENCES_DECORATION_PREVIEW_LINE, true) ? CodeArea.DECORATION_PREVIEW_LINE : 0)
                + (preferences.getBoolean(BinEdFileEditor.PREFERENCES_DECORATION_BOX, false) ? CodeArea.DECORATION_BOX : 0)
                + (preferences.getBoolean(BinEdFileEditor.PREFERENCES_DECORATION_LINENUM_LINE, true) ? CodeArea.DECORATION_LINENUM_LINE : 0);
        codeArea.setDecorationMode(decorationMode); */
        codeArea.setCodeCharactersCase(CodeCharactersCase.valueOf(preferences.getValue(BinEdFileEditor.PREFERENCES_HEX_CHARACTERS_CASE, CodeCharactersCase.UPPER.name())));
        // TODO codeArea.setPositionCodeType(PositionCodeType.valueOf(preferences.getValue(BinEdFileEditor.PREFERENCES_POSITION_CODE_TYPE, PositionCodeType.HEXADECIMAL.name())));

        // Font
        Boolean useDefaultColor = Boolean.valueOf(preferences.getValue(TextFontOptionsPanel.PREFERENCES_TEXT_FONT_DEFAULT, Boolean.toString(true)));

        if (!useDefaultColor) {
            String value;
            Map<TextAttribute, Object> attribs = new HashMap<>();
            value = preferences.getValue(TextFontOptionsPanel.PREFERENCES_TEXT_FONT_FAMILY, "MONOSPACED");
            attribs.put(TextAttribute.FAMILY, value);
            value = preferences.getValue(TextFontOptionsPanel.PREFERENCES_TEXT_FONT_SIZE, "12");
            attribs.put(TextAttribute.SIZE, new Integer(value).floatValue());
            if (Boolean.valueOf(preferences.getValue(TextFontOptionsPanel.PREFERENCES_TEXT_FONT_UNDERLINE, "FALSE"))) {
                attribs.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_LOW_ONE_PIXEL);
            }
            if (Boolean.valueOf(preferences.getValue(TextFontOptionsPanel.PREFERENCES_TEXT_FONT_STRIKETHROUGH, "FALSE"))) {
                attribs.put(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
            }
            if (Boolean.valueOf(preferences.getValue(TextFontOptionsPanel.PREFERENCES_TEXT_FONT_STRONG, "FALSE"))) {
                attribs.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD);
            }
            if (Boolean.valueOf(preferences.getValue(TextFontOptionsPanel.PREFERENCES_TEXT_FONT_ITALIC, "FALSE"))) {
                attribs.put(TextAttribute.POSTURE, TextAttribute.POSTURE_OBLIQUE);
            }
            if (Boolean.valueOf(preferences.getValue(TextFontOptionsPanel.PREFERENCES_TEXT_FONT_SUBSCRIPT, "FALSE"))) {
                attribs.put(TextAttribute.SUPERSCRIPT, TextAttribute.SUPERSCRIPT_SUB);
            }
            if (Boolean.valueOf(preferences.getValue(TextFontOptionsPanel.PREFERENCES_TEXT_FONT_SUPERSCRIPT, "FALSE"))) {
                attribs.put(TextAttribute.SUPERSCRIPT, TextAttribute.SUPERSCRIPT_SUPER);
            }
            Font derivedFont = codeArea.getFont().deriveFont(attribs);
            codeArea.setFont(derivedFont);
        }
        boolean showValuesPanel = preferences.getBoolean(BinEdFileEditor.PREFERENCES_SHOW_VALUES_PANEL, true);
        if (showValuesPanel) {
            showValuesPanel();
        }
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

    private static BasicBackgroundPaintMode convertBackgroundPaintMode(String value) {
        if ("STRIPPED".equals(value))
            return BasicBackgroundPaintMode.STRIPED;
        return BasicBackgroundPaintMode.valueOf(value);
    }

    public interface CharsetChangeListener {

        void charsetChanged();
    }

    public void setData(BinaryData data) {
        codeArea.setContentData(data);
    }
}
