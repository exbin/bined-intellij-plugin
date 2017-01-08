/*
 * Copyright (C) ExBin Project
 *
 * This application or library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * This application or library is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along this application.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.exbin.framework.deltahex.panel;

import java.awt.CardLayout;
import java.awt.event.KeyListener;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.exbin.deltahex.DataChangedListener;
import org.exbin.deltahex.ScrollBarVisibility;
import org.exbin.deltahex.swing.CodeArea;
import org.exbin.framework.deltahex.CodeAreaPopupMenuHandler;
import org.exbin.utils.binary_data.ByteArrayEditableData;
import org.exbin.utils.binary_data.EditableBinaryData;

/**
 * Combo box panel supporting both binary and text values.
 *
 * @version 0.1.0 2016/07/21
 * @author ExBin Project (http://exbin.org)
 */
public class HexSearchComboBoxPanel extends JPanel {

    public static final String TEXT_MODE = "text";
    public static final String BINARY_MODE = "binary";

    private final JTextField textField;
    private final CodeArea hexadecimalEditor = new CodeArea();

    private final SearchCondition item = new SearchCondition();

    private boolean runningUpdate = false;
    private ValueChangedListener valueChangedListener = null;

    public HexSearchComboBoxPanel() {
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

        hexadecimalEditor.setShowHeader(false);
        hexadecimalEditor.setShowLineNumbers(false);
        hexadecimalEditor.setWrapMode(true);
        hexadecimalEditor.setBackgroundMode(CodeArea.BackgroundMode.PLAIN);
        hexadecimalEditor.setVerticalScrollBarVisibility(ScrollBarVisibility.NEVER);
        hexadecimalEditor.setHorizontalScrollBarVisibility(ScrollBarVisibility.NEVER);
        hexadecimalEditor.setData(new ByteArrayEditableData());
        hexadecimalEditor.setBorder(comboBoxBorder);
        hexadecimalEditor.addDataChangedListener(new DataChangedListener() {
            @Override
            public void dataChanged() {
                comboBoxValueChanged();
            }
        });
        super.add(hexadecimalEditor, BINARY_MODE);
    }

    public SearchCondition getItem() {
        switch (item.getSearchMode()) {
            case TEXT: {
                item.setSearchText(textField.getText());
                break;
            }
            case BINARY: {
                item.setBinaryData((EditableBinaryData) hexadecimalEditor.getData());
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
                hexadecimalEditor.setData(data);
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
                hexadecimalEditor.selectAll();
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
        hexadecimalEditor.addKeyListener(editorKeyListener);
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
                hexadecimalEditor.requestFocus();
                break;
            }
        }
    }

    public void setHexCodePopupMenuHandler(CodeAreaPopupMenuHandler hexCodePopupMenuHandler, String postfix) {
        hexadecimalEditor.setComponentPopupMenu(hexCodePopupMenuHandler.createPopupMenu(hexadecimalEditor, ".search" + postfix));
    }

    /**
     * Listener for value change.
     */
    public static interface ValueChangedListener {

        void valueChanged();
    }
}
