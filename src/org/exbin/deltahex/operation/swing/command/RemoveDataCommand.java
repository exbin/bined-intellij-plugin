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
package org.exbin.deltahex.operation.swing.command;

import org.exbin.deltahex.operation.swing.RemoveDataOperation;
import org.exbin.deltahex.swing.CodeArea;

/**
 * Command for deleting data.
 *
 * @version 0.1.0 2016/06/13
 * @author ExBin Project (http://exbin.org)
 */
public class RemoveDataCommand extends OpCodeAreaCommand {

    public RemoveDataCommand(CodeArea codeArea, long position, int codeOffset, long size) {
        super(codeArea);
        super.setOperation(new RemoveDataOperation(codeArea, position, codeOffset, size));
    }

    @Override
    public CodeAreaCommandType getType() {
        return CodeAreaCommandType.DATA_REMOVED;
    }
}
