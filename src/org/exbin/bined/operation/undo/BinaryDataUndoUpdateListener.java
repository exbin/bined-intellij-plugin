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
package org.exbin.bined.operation.undo;

import javax.annotation.Nonnull;
import org.exbin.bined.operation.BinaryDataCommand;

/**
 * Code area undo update listener.
 *
 * @version 0.2.0 2018/02/13
 * @author ExBin Project (https://exbin.org)
 */
public interface BinaryDataUndoUpdateListener {

    /**
     * Notifies about change in undo state.
     */
    void undoCommandPositionChanged();

    /**
     * Reports new command added to undo sequence.
     *
     * @param command added command
     */
    void undoCommandAdded(@Nonnull BinaryDataCommand command);
}
