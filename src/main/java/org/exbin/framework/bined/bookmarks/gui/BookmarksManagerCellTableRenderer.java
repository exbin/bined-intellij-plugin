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
package org.exbin.framework.bined.bookmarks.gui;

import java.awt.Color;
import java.awt.Component;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.JPanel;
import javax.swing.table.TableCellRenderer;
import javax.swing.JTable;
import javax.swing.border.BevelBorder;
import org.exbin.framework.bined.bookmarks.model.BookmarksTableModel;

/**
 * Table model for color value.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class BookmarksManagerCellTableRenderer implements TableCellRenderer {

    public BookmarksManagerCellTableRenderer() {
    }

    @Nonnull
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        JPanel renderComponent = new JPanel();
        renderComponent.setBorder(new BevelBorder(1));
        BookmarksTableModel model = (BookmarksTableModel) table.getModel();
        renderComponent.setBackground((Color) model.getValueAt(row, column));
        return renderComponent;
    }
}
