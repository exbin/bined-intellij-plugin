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
package org.exbin.framework.bined.objectdata.property.gui;

import java.awt.Component;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.DefaultCellEditor;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;

/**
 * Property table cell renderer.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class PropertyTableCellEditor extends DefaultCellEditor {

    public PropertyTableCellEditor() {
        super(new JTextField());
        setClickCountToStart(0);
    }

    @Nonnull
    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        Object fieldValue = ((PropertyTableItem) value).asBasicType();
        if (fieldValue == null) {
            final JTextComponent component = (JTextComponent) super.getTableCellEditorComponent(table, "", isSelected, row, column);
            component.setBackground(table.getBackground());
            component.setBorder(null);
            component.setEditable(false);
            SwingUtilities.invokeLater(() -> {
                component.repaint();
                component.selectAll();
            });
            return component;
        }

        PropertyTableCellPanel cellPanel;
        final JTextComponent defaultComponent = (JTextComponent) super.getTableCellEditorComponent(table, fieldValue instanceof String ? (String) fieldValue : ((PropertyTableItem) value).getTypeName(), isSelected, row, column);
        defaultComponent.setBackground(table.getBackground());
        defaultComponent.setBorder(null);
        defaultComponent.setEditable(false);
        SwingUtilities.invokeLater(() -> {
            defaultComponent.repaint();
            defaultComponent.selectAll();
        });
        cellPanel = new PropertyTableCellPanel(defaultComponent, ((PropertyTableItem) value).getValue(), ((PropertyTableItem) value).getValueName());

        cellPanel.setBackground(table.getSelectionBackground());
        cellPanel.getCellComponent().setBorder(null);
        return cellPanel;
    }

    @Override
    public boolean stopCellEditing() {
        return super.stopCellEditing();
    }
}
