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
package org.exbin.framework.bined.options.panel;

import java.awt.Color;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import org.exbin.bined.color.BasicCodeAreaDecorationColorType;
import org.exbin.bined.color.CodeAreaBasicColors;
import org.exbin.bined.color.CodeAreaColorType;
import org.exbin.bined.extended.color.CodeAreaUnprintablesColorType;
import org.exbin.bined.highlight.swing.color.CodeAreaColorizationColorType;
import org.exbin.bined.highlight.swing.color.CodeAreaMatchColorType;
import org.exbin.bined.swing.extended.color.ExtendedCodeAreaColorProfile;

/**
 * Table model for Color profile panel.
 *
 * @version 0.2.0 2019/03/12
 * @author ExBin Project (http://exbin.org)
 */
@ParametersAreNonnullByDefault
public class ColorProfileTableModel implements TableModel {

    private final List<TableModelListener> listeners = new ArrayList<>();

    private final List<ColorRow> rows = new ArrayList<>();
    private ExtendedCodeAreaColorProfile colorProfile;

    public ColorProfileTableModel() {
        init();
    }

    private void init() {
        rows.add(new ColorRow("Text color", CodeAreaBasicColors.TEXT_COLOR));
        rows.add(new ColorRow("Text background", CodeAreaBasicColors.TEXT_BACKGROUND));
        rows.add(new ColorRow("Selection text color", CodeAreaBasicColors.SELECTION_COLOR));
        rows.add(new ColorRow("Selection background", CodeAreaBasicColors.SELECTION_BACKGROUND));
        rows.add(new ColorRow("Selection mirror text color", CodeAreaBasicColors.SELECTION_MIRROR_COLOR));
        rows.add(new ColorRow("Selection mirror background", CodeAreaBasicColors.SELECTION_MIRROR_BACKGROUND));
        rows.add(new ColorRow("Alternate text color", CodeAreaBasicColors.ALTERNATE_COLOR));
        rows.add(new ColorRow("Alternate background", CodeAreaBasicColors.ALTERNATE_BACKGROUND));
        rows.add(new ColorRow("Cursor color", CodeAreaBasicColors.CURSOR_COLOR));
        rows.add(new ColorRow("Cursor negative color", CodeAreaBasicColors.CURSOR_NEGATIVE_COLOR));

        rows.add(new ColorRow("Decoration line color", BasicCodeAreaDecorationColorType.LINE));
        rows.add(new ColorRow("Control codes text color", CodeAreaColorizationColorType.CONTROL_CODES_COLOR));
        rows.add(new ColorRow("Control codes background", CodeAreaColorizationColorType.CONTROL_CODES_BACKGROUND));
        rows.add(new ColorRow("Upper codes text color", CodeAreaColorizationColorType.UPPER_CODES_COLOR));
        rows.add(new ColorRow("Upper codes background color", CodeAreaColorizationColorType.UPPER_CODES_BACKGROUND));

        rows.add(new ColorRow("Search match text color", CodeAreaMatchColorType.MATCH_COLOR));
        rows.add(new ColorRow("Search match background", CodeAreaMatchColorType.MATCH_BACKGROUND));
        rows.add(new ColorRow("Active match text color", CodeAreaMatchColorType.ACTIVE_MATCH_COLOR));
        rows.add(new ColorRow("Active match background", CodeAreaMatchColorType.ACTIVE_MATCH_BACKGROUND));

        rows.add(new ColorRow("Unprintable characters text color", CodeAreaUnprintablesColorType.UNPRINTABLES_COLOR));
        rows.add(new ColorRow("Unprintable characters background", CodeAreaUnprintablesColorType.UNPRINTABLES_BACKGROUND));
    }

    @Nullable
    public ExtendedCodeAreaColorProfile getColorProfile() {
        return colorProfile;
    }

    public void setColorProfile(ExtendedCodeAreaColorProfile colorProfile) {
        this.colorProfile = colorProfile;
    }

    @Override
    public int getRowCount() {
        return rows.size();
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public String getColumnName(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return "Color";
            case 1:
                return "Value";
        }

        throw new InvalidParameterException("Unexpected column index " + columnIndex);
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return String.class;
            case 1:
                return Color.class;
        }

        throw new InvalidParameterException("Unexpected column index " + columnIndex);
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        switch (columnIndex) {
            case 0:
                return false;
            case 1:
                return true;
        }

        throw new InvalidParameterException("Unexpected column index " + columnIndex);
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        switch (columnIndex) {
            case 0:
                return rows.get(rowIndex).colorName;
            case 1:
                return colorProfile == null ? null : colorProfile.getColor(rows.get(rowIndex).colorType);
        }

        throw new InvalidParameterException("Unexpected column index " + columnIndex);
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        switch (columnIndex) {
            case 1: {
                if (colorProfile == null) {
                    throw new IllegalStateException("Editing is not allowed when color profile was not set");
                }

                colorProfile.setColor(rows.get(rowIndex).colorType, (Color) aValue);
                notifyAllListeners(rowIndex);
                return;
            }
        }

        throw new InvalidParameterException("Unexpected column index " + columnIndex);
    }

    @Override
    public void addTableModelListener(TableModelListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeTableModelListener(TableModelListener listener) {
        listeners.remove(listener);
    }

    private void notifyAllListeners(int rowNumber) {
        listeners.forEach((listener) -> listener.tableChanged(new TableModelEvent(this, rowNumber, 1, TableModelEvent.UPDATE)));
    }

    private void notifyAllListeners() {
        listeners.forEach((listener) -> listener.tableChanged(new TableModelEvent(this, 0, rows.size() - 1, 1, TableModelEvent.UPDATE)));
    }

    private static class ColorRow {

        public ColorRow(String colorName, CodeAreaColorType colorType) {
            this.colorName = colorName;
            this.colorType = colorType;
        }

        String colorName;
        CodeAreaColorType colorType;
    }
}
