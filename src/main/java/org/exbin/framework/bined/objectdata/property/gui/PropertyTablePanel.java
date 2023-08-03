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
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

/**
 * Panel for properties of the inspected instance.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class PropertyTablePanel extends javax.swing.JPanel {

    private final PropertyTableModel tableModel;
    private final PropertyTableCellRenderer valueCellRenderer;
    private final TableCellRenderer nameCellRenderer;
    private final PropertyTableCellEditor valueCellEditor;

    private boolean showStaticFields = false;

    public PropertyTablePanel() {
        tableModel = new PropertyTableModel();

        initComponents();

        TableColumnModel columns = propertiesTable.getColumnModel();
        columns.getColumn(0).setPreferredWidth(190);
        columns.getColumn(1).setPreferredWidth(190);
        columns.getColumn(0).setWidth(190);
        columns.getColumn(1).setWidth(190);
        nameCellRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JComponent component = (JComponent) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                PropertyTableItem tableItem = ((PropertyTableModel) table.getModel()).getRow(row);
                component.setToolTipText("(" + tableItem.getTypeName() + ") " + tableItem.getValueName());
                return component;
            }
        };
        columns.getColumn(0).setCellRenderer(nameCellRenderer);
        valueCellRenderer = new PropertyTableCellRenderer();
        columns.getColumn(1).setCellRenderer(valueCellRenderer);
        valueCellEditor = new PropertyTableCellEditor();
        columns.getColumn(1).setCellEditor(valueCellEditor);

        propertiesTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (component instanceof JComponent) {
                    ((JComponent) component).setBorder(noFocusBorder);
                }

                return component;
            }
        });
    }

    public void setObject(Object object) {
        if (propertiesTable.isEditing()) {
            propertiesTable.getCellEditor().cancelCellEditing();
        }

        tableModel.removeAll();

        Class<?> clazz = object.getClass();
        if (clazz.isArray()) {
            int length = Array.getLength(object);
            for (int i = 0; i < length; i++) {
                Object field = Array.get(object, i);
                PropertyTableItem item = new PropertyTableItem(String.valueOf(i), field == null ? "-" : field.getClass().getTypeName(), field);
                tableModel.addRow(item);
            }
        } else {
            while (clazz != null) {
                Field[] fields = clazz.getDeclaredFields();
                for (Field field : fields) {
                    if (!showStaticFields && java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                        continue;
                    }

                    Object value = accessField(field, object);
                    PropertyTableItem item = new PropertyTableItem(field.getName(), field.getGenericType().getTypeName(), value);
                    tableModel.addRow(item);
                }
                clazz = clazz.getSuperclass();
            }
        }
    }

    @Nullable
    private static Object accessField(Field field, Object object) {
        Object result = null;
        try {
            result = field.get(object);
        } catch (Throwable ex) {
            try {
                // Try to make field accessible
                field.setAccessible(true);
                try {
                    result = field.get(object);
                } catch (Throwable ex3) {
                }
                field.setAccessible(false);
            } catch (Throwable ex2) {
                // Can't set it back, just ignore it
            }
        }

        return result;
    }

    public boolean isShowStaticFields() {
        return showStaticFields;
    }

    public void setShowStaticFields(boolean showStaticFields) {
        this.showStaticFields = showStaticFields;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainScrollPane = new javax.swing.JScrollPane();
        propertiesTable = new javax.swing.JTable();

        setName("Form"); // NOI18N
        setLayout(new java.awt.BorderLayout());

        mainScrollPane.setName("mainScrollPane"); // NOI18N

        propertiesTable.setModel(tableModel);
        propertiesTable.setName("propertiesTable"); // NOI18N
        propertiesTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        mainScrollPane.setViewportView(propertiesTable);

        add(mainScrollPane, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane mainScrollPane;
    private javax.swing.JTable propertiesTable;
    // End of variables declaration//GEN-END:variables
}
