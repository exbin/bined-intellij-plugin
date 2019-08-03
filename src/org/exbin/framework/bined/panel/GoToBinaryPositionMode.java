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
package org.exbin.framework.bined.panel;

/**
 * Mode for calculation of the go-to position in binary document.
 *
 * @version 0.2.1 2019/07/12
 * @author ExBin Project (http://exbin.org)
 */
public enum GoToBinaryPositionMode {
    /**
     * Count from start of the document.
     */
    FROM_START,
    /**
     * Count from end of the document.
     */
    FROM_END,
    /**
     * Count from current position of the cursor in the document.
     */
    FROM_CURSOR
}
