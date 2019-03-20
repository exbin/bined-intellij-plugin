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
import javax.swing.table.TableCellRenderer;
import javax.swing.JTable;

/**
 * Table model for color profile panel.
 *
 * @version 0.2.0 2019/01/18
 * @author ExBin Project (http://exbin.org)
 */
@ParametersAreNonnullByDefault
public class ColorCellTableRenderer implements TableCellRenderer {

    public ColorCellTableRenderer() {
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        return new ColorCellPanel(new ColorCellPanel.ColorHandler() {
            @Override
            public Color getColor() {
                ColorProfileTableModel model = (ColorProfileTableModel) table.getModel();
                return (Color) model.getValueAt(row, column);
            }

            @Override
            public void setColor(Color color) {
                ColorProfileTableModel model = (ColorProfileTableModel) table.getModel();
                model.setValueAt(color, row, column);
            }
        });
    }
}
