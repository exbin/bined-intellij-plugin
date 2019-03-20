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
import java.awt.Component;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

/**
 * Table model for color profile panel.
 *
 * @version 0.2.0 2019/03/01
 * @author ExBin Project (http://exbin.org)
 */
@ParametersAreNonnullByDefault
public class ColorCellTableEditor extends AbstractCellEditor implements TableCellEditor {

    private Color currentColor = null;

    public ColorCellTableEditor() {
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        currentColor = (Color) value;
        return new ColorCellPanel(new ColorCellPanel.ColorHandler() {
            @Override
            public Color getColor() {
                return currentColor;
            }

            @Override
            public void setColor(Color color) {
                currentColor = color;

                ColorProfileTableModel model = (ColorProfileTableModel) table.getModel();
                model.setValueAt(color, row, column);
            }
        });
    }

    @Override
    public Object getCellEditorValue() {
        return currentColor;
    }
}
