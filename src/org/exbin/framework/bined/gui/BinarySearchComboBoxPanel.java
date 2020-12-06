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
package org.exbin.framework.bined.gui;

import org.exbin.framework.bined.SearchCondition;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.event.KeyListener;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.exbin.bined.ScrollBarVisibility;
import org.exbin.bined.RowWrappingMode;
import org.exbin.bined.extended.layout.ExtendedCodeAreaLayoutProfile;
import org.exbin.bined.extended.theme.ExtendedBackgroundPaintMode;
import org.exbin.bined.swing.extended.ExtCodeArea;
import org.exbin.bined.swing.extended.theme.ExtendedCodeAreaThemeProfile;
import org.exbin.framework.bined.handler.CodeAreaPopupMenuHandler;
import org.exbin.auxiliary.paged_data.ByteArrayEditableData;
import org.exbin.auxiliary.paged_data.EditableBinaryData;

/**
 * Combo box panel supporting both binary and text values.
 *
 * @version 0.2.1 2018/12/11
 * @author ExBin Project (http://exbin.org)
 */
public class BinarySearchComboBoxPanel extends JPanel {

    public static final String TEXT_MODE = "text";
    public static final String BINARY_MODE = "binary";

    private final JTextField textField;
    private final ExtCodeArea codeArea = new ExtCodeArea();

    private final SearchCondition item = new SearchCondition();

    private boolean runningUpdate = false;
    private ValueChangedListener valueChangedListener = null;

    public BinarySearchComboBoxPanel() {
        super.setLayout(new CardLayout());
        Border comboBoxBorder = ((JComponent) (new JComboBox<>().getEditor().getEditorComponent())).getBorder();
        textField = new JTextField();
        textField.setBorder(comboBoxBorder);
        textField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                comboBoxValueChanged();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                comboBoxValueChanged();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                comboBoxValueChanged();
            }
        });

        super.add(textField, TEXT_MODE);

        {
            ExtendedCodeAreaLayoutProfile layoutProfile = codeArea.getLayoutProfile();
            layoutProfile.setShowHeader(false);
            layoutProfile.setShowRowPosition(false);
            codeArea.setLayoutProfile(layoutProfile);
        }
        codeArea.setRowWrapping(RowWrappingMode.WRAPPING);
        codeArea.setWrappingBytesGroupSize(0);
        {
            ExtendedCodeAreaThemeProfile themeProfile = codeArea.getThemeProfile();
            themeProfile.setBackgroundPaintMode(ExtendedBackgroundPaintMode.PLAIN);
            codeArea.setThemeProfile(themeProfile);
        }

        codeArea.setVerticalScrollBarVisibility(ScrollBarVisibility.NEVER);
        codeArea.setHorizontalScrollBarVisibility(ScrollBarVisibility.NEVER);
        codeArea.setContentData(new ByteArrayEditableData());
        codeArea.setBorder(comboBoxBorder);
        codeArea.addDataChangedListener(this::comboBoxValueChanged);
        super.add(codeArea, BINARY_MODE);
    }

    public SearchCondition getItem() {
        switch (item.getSearchMode()) {
            case TEXT: {
                item.setSearchText(textField.getText());
                break;
            }
            case BINARY: {
                item.setBinaryData((EditableBinaryData) codeArea.getContentData());
                break;
            }
        }

        return item;
    }

    public void setItem(SearchCondition item) {
        if (item == null) {
            item = new SearchCondition();
        }
        this.item.setSearchMode(item.getSearchMode());
        switch (item.getSearchMode()) {
            case TEXT: {
                this.item.setSearchText(item.getSearchText());
                this.item.setBinaryData(null);
                runningUpdate = true;
                textField.setText(item.getSearchText());
                runningUpdate = false;
                CardLayout layout = (CardLayout) getLayout();
                layout.show(this, TEXT_MODE);
                revalidate();
                break;
            }
            case BINARY: {
                this.item.setSearchText("");
                ByteArrayEditableData data = new ByteArrayEditableData();
                if (item.getBinaryData() != null) {
                    data.insert(0, item.getBinaryData());
                }
                this.item.setBinaryData(data);
                runningUpdate = true;
                codeArea.setContentData(data);
                runningUpdate = false;
                CardLayout layout = (CardLayout) getLayout();
                layout.show(this, BINARY_MODE);
                revalidate();
                break;
            }
        }
    }

    public void selectAll() {
        switch (item.getSearchMode()) {
            case TEXT: {
                textField.selectAll();
                break;
            }
            case BINARY: {
                codeArea.selectAll();
                break;
            }
        }
    }

    private void comboBoxValueChanged() {
        if (valueChangedListener != null && !runningUpdate) {
            valueChangedListener.valueChanged();
        }
    }

    public void addValueKeyListener(KeyListener editorKeyListener) {
        textField.addKeyListener(editorKeyListener);
        codeArea.addKeyListener(editorKeyListener);
    }

    public void setValueChangedListener(ValueChangedListener valueChangedListener) {
        this.valueChangedListener = valueChangedListener;
    }

    public void setRunningUpdate(boolean runningUpdate) {
        this.runningUpdate = runningUpdate;
    }

    @Override
    public void requestFocus() {
        super.requestFocus();
        switch (item.getSearchMode()) {
            case TEXT: {
                textField.requestFocus();
                break;
            }
            case BINARY: {
                codeArea.requestFocus();
                break;
            }
        }
    }

    public void setCodeAreaPopupMenuHandler(CodeAreaPopupMenuHandler codeAreaPopupMenuHandler, String postfix) {
        codeArea.setComponentPopupMenu(new JPopupMenu() {
            @Override
            public void show(Component invoker, int x, int y) {
                int clickedX = x;
                int clickedY = y;
                if (invoker instanceof JViewport) {
                    clickedX += ((JViewport) invoker).getParent().getX();
                    clickedY += ((JViewport) invoker).getParent().getY();
                }
                JPopupMenu popupMenu = codeAreaPopupMenuHandler.createPopupMenu(codeArea, ".search" + postfix, clickedX, clickedY);
                popupMenu.show(invoker, x, y);
            }
        });
    }

    /**
     * Listener for value change.
     */
    public static interface ValueChangedListener {

        void valueChanged();
    }
}
