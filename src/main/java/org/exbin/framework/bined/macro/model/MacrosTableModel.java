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
package org.exbin.framework.bined.macro.model;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.table.AbstractTableModel;
import org.exbin.framework.utils.LanguageUtils;

/**
 * Table model for macros.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class MacrosTableModel extends AbstractTableModel {

    private final java.util.ResourceBundle resourceBundle = LanguageUtils.getResourceBundleByClass(MacrosTableModel.class);
    private List<MacroRecord> records;
    private ChangeListener changeListener = null;

    private final String[] columnNames;
    private Class[] columnTypes = new Class[]{
        java.lang.Integer.class, java.lang.String.class, java.lang.Integer.class
    };
    private final boolean[] columnsEditable = new boolean[]{false, false, false};

    public MacrosTableModel() {
        columnNames = new String[]{
            resourceBundle.getString("macrosTableModel.index"),
            resourceBundle.getString("macrosTableModel.name"),
            resourceBundle.getString("macrosTableModel.size"),};
        records = new ArrayList<>();
    }

    @Nonnull
    public List<MacroRecord> getRecords() {
        return records;
    }

    public void setRecords(List<MacroRecord> records) {
        this.records = records;
        fireDataChanged();
    }

    public void updateRecord(MacroRecord record, int index) {
        records.set(index, record);
        fireTableRowsUpdated(index, index);
    }

    @Override
    public int getRowCount() {
        return records.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Nonnull
    @Override
    public String getColumnName(int columnIndex) {
        return columnNames[columnIndex];
    }

    @Nonnull
    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return getTypes()[columnIndex];
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnsEditable[columnIndex];
    }

    @Nullable
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        MacroRecord record = records.get(rowIndex);
        if (record == null) {
            return null;
        }

        if (columnIndex == 0) {
            return rowIndex;
        }

        switch (columnIndex) {
            case 1: {
                return record.getName();
            }
            case 2: {
                return record.getSteps().size();
            }
            default:
                return null;
        }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        throw new IllegalStateException();
    }

    @Nonnull
    public Class[] getTypes() {
        return columnTypes;
    }

    public void setTypes(Class[] types) {
        this.columnTypes = types;
    }

    public void fireDataChanged() {
        if (changeListener != null) {
            changeListener.valueChanged();
        }
    }

    public void attachChangeListener(ChangeListener listener) {
        changeListener = listener;
    }

    public interface ChangeListener {

        void valueChanged();
    }
}
