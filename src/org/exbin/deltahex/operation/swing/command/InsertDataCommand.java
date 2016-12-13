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

import org.exbin.deltahex.swing.CodeArea;
import org.exbin.deltahex.operation.swing.InsertDataOperation;
import org.exbin.utils.binary_data.EditableBinaryData;

/**
 * Command for inserting data.
 *
 * @version 0.1.1 2016/09/26
 * @author ExBin Project (http://exbin.org)
 */
public class InsertDataCommand extends OpCodeAreaCommand {

    private final long position;
    private final long dataLength;

    public InsertDataCommand(CodeArea codeArea, long position, EditableBinaryData data) {
        super(codeArea);
        this.position = position;
        dataLength = data.getDataSize();
        super.setOperation(new InsertDataOperation(codeArea, position, codeArea.getCaretPosition().getCodeOffset(), data));
    }

    @Override
    public CodeAreaCommandType getType() {
        return CodeAreaCommandType.DATA_INSERTED;
    }

    @Override
    public void redo() throws Exception {
        super.redo();
        codeArea.getCaretPosition().setDataPosition(position + dataLength);
    }

    @Override
    public void undo() throws Exception {
        super.undo();
        codeArea.getCaretPosition().setDataPosition(position);
    }
}
