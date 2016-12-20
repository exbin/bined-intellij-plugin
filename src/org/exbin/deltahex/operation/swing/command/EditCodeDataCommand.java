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

import org.exbin.deltahex.operation.BinaryDataOperationException;
import org.exbin.deltahex.operation.BinaryDataOperationListener;
import org.exbin.deltahex.operation.swing.CodeAreaOperation;
import org.exbin.deltahex.operation.swing.CodeAreaOperationEvent;
import org.exbin.deltahex.operation.swing.CodeAreaOperationListener;
import org.exbin.deltahex.operation.swing.CodeEditDataOperation;
import org.exbin.deltahex.operation.swing.DeleteCodeEditDataOperation;
import org.exbin.deltahex.operation.swing.InsertCodeEditDataOperation;
import org.exbin.deltahex.operation.swing.OverwriteCodeEditDataOperation;
import org.exbin.deltahex.swing.CodeArea;

/**
 * Command for editing data in hexadecimal mode.
 *
 * @version 0.1.2 2016/12/20
 * @author ExBin Project (http://exbin.org)
 */
public class EditCodeDataCommand extends EditDataCommand {

    private final EditCommandType commandType;
    protected boolean operationPerformed = false;
    private CodeAreaOperation[] operations = null;

    public EditCodeDataCommand(CodeArea codeArea, EditCommandType commandType, long position, int positionCodeOffset) {
        super(codeArea);
        this.commandType = commandType;
        CodeAreaOperation operation;
        switch (commandType) {
            case INSERT: {
                operation = new InsertCodeEditDataOperation(codeArea, position, positionCodeOffset);
                break;
            }
            case OVERWRITE: {
                operation = new OverwriteCodeEditDataOperation(codeArea, position, positionCodeOffset);
                break;
            }
            case DELETE: {
                operation = new DeleteCodeEditDataOperation(codeArea, position);
                break;
            }
            default: {
                throw new IllegalStateException("Unsupported command type " + commandType.name());
            }
        }
        operations = new CodeAreaOperation[]{operation};
        operationPerformed = true;
    }

    @Override
    public void undo() throws BinaryDataOperationException {
        if (operations.length == 1 && operations[0] instanceof CodeEditDataOperation) {
            CodeEditDataOperation operation = (CodeEditDataOperation) operations[0];
            operations = operation.generateUndo();
            operation.dispose();
        }

        if (operationPerformed) {
            for (int i = operations.length - 1; i >= 0; i--) {
                CodeAreaOperation operation = operations[i];
                CodeAreaOperation redoOperation = operation.executeWithUndo();
                operation.dispose();
                if (codeArea instanceof BinaryDataOperationListener) {
                    ((CodeAreaOperationListener) codeArea).notifyChange(new CodeAreaOperationEvent(operation));
                }
                operations[i] = redoOperation;
            }
            operationPerformed = false;
        } else {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    @Override
    public void redo() throws BinaryDataOperationException {
        if (!operationPerformed) {
            for (int i = 0; i < operations.length; i++) {
                CodeAreaOperation operation = operations[i];
                CodeAreaOperation undoOperation = operation.executeWithUndo();
                operation.dispose();
                if (codeArea instanceof BinaryDataOperationListener) {
                    ((CodeAreaOperationListener) codeArea).notifyChange(new CodeAreaOperationEvent(operations[i]));
                }

                operations[i] = undoOperation;
            }
            operationPerformed = true;
        } else {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    @Override
    public CodeAreaCommandType getType() {
        return CodeAreaCommandType.DATA_EDITED;
    }

    @Override
    public boolean canUndo() {
        return true;
    }

    /**
     * Appends next hexadecimal value in editing action sequence.
     *
     * @param value half-byte value (0..15)
     */
    public void appendEdit(byte value) {
        if (operations.length == 1 && operations[0] instanceof CodeEditDataOperation) {
            ((CodeEditDataOperation) operations[0]).appendEdit(value);
        } else {
            throw new IllegalStateException("Cannot append edit on reverted command");
        }
    }

    @Override
    public EditCommandType getCommandType() {
        return commandType;
    }

    @Override
    public boolean wasReverted() {
        return !(operations.length == 1 && operations[0] instanceof CodeEditDataOperation);
    }

    @Override
    public void dispose() throws BinaryDataOperationException {
        super.dispose();
        if (operations != null) {
            for (CodeAreaOperation operation : operations) {
                operation.dispose();
            }
        }
    }
}
