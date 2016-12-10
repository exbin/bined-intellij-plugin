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
package org.exbin.xbup.operation.basic;

/**
 * Document operation type enumeration.
 *
 * @version 0.2.0 2016/02/28
 * @author ExBin Project (http://exbin.org)
 */
public enum XBBasicOperationType {

    /**
     * Block added.
     */
    ADD_BLOCK("Add block"),
    /**
     * Block deleted.
     */
    DELETE_BLOCK("Delete block"),
    /**
     * Block modified.
     */
    MODIFY_BLOCK("Modify block"),
    /**
     * Block moved.
     */
    MOVE_BLOCK("Move block");

    private final String caption;

    private XBBasicOperationType(String caption) {
        this.caption = caption;
    }

    public String getCaption() {
        return caption;
    }
}
