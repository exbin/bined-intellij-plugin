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
import org.exbin.deltahex.operation.swing.CharEditDataOperation;
import org.exbin.deltahex.operation.swing.DeleteCharEditDataOperation;
import org.exbin.deltahex.operation.swing.CodeAreaOperation;
import org.exbin.deltahex.operation.swing.CodeAreaOperationEvent;
import org.exbin.deltahex.operation.swing.InsertCharEditDataOperation;
import org.exbin.deltahex.operation.swing.OverwriteCharEditDataOperation;
import org.exbin.xbup.operation.OperationListener;
import org.exbin.deltahex.operation.swing.CodeAreaOperationListener;

/**
 * Command for editing data in text mode.
 *
 * @version 0.1.1 2016/09/26
 * @author ExBin Project (http://exbin.org)
 */
public class EditCharDataCommand extends EditDataCommand {

    private final EditCommandType commandType;
    protected boolean operationPerformed = false;
    private CodeAreaOperation[] operations = null;

    public EditCharDataCommand(CodeArea codeArea, EditCommandType commandType, long position) {
        super(codeArea);
        this.commandType = commandType;
        CodeAreaOperation operation;
        switch (commandType) {
            case INSERT: {
                operation = new InsertCharEditDataOperation(codeArea, position);
                break;
            }
            case OVERWRITE: {
                operation = new OverwriteCharEditDataOperation(codeArea, position);
                break;
            }
            case DELETE: {
                operation = new DeleteCharEditDataOperation(codeArea, position);
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
    public void undo() throws Exception {
        if (operations.length == 1 && operations[0] instanceof CharEditDataOperation) {
            CharEditDataOperation operation = (CharEditDataOperation) operations[0];
            operations = operation.generateUndo();
            operation.dispose();
        }

        if (operationPerformed) {
            for (int i = operations.length - 1; i >= 0; i--) {
                CodeAreaOperation operation = operations[i];
                CodeAreaOperation redoOperation = operation.executeWithUndo();
                operation.dispose();
                if (codeArea instanceof OperationListener) {
                    ((CodeAreaOperationListener) codeArea).notifyChange(new CodeAreaOperationEvent(operations[i]));
                }
                operations[i] = redoOperation;
            }
            operationPerformed = false;
        } else {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    @Override
    public void redo() throws Exception {
        if (!operationPerformed) {
            for (int i = 0; i < operations.length; i++) {
                CodeAreaOperation operation = operations[i];
                CodeAreaOperation undoOperation = operation.executeWithUndo();
                operation.dispose();
                if (codeArea instanceof OperationListener) {
                    ((CodeAreaOperationListener) codeArea).notifyChange(new CodeAreaOperationEvent(operation));
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

    public void appendEdit(char value) {
        if (operations.length == 1 && operations[0] instanceof CharEditDataOperation) {
            ((CharEditDataOperation) operations[0]).appendEdit(value);
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
        return !(operations.length == 1 && operations[0] instanceof CharEditDataOperation);
    }

    @Override
    public void dispose() throws Exception {
        super.dispose();
        if (operations != null) {
            for (CodeAreaOperation operation : operations) {
                operation.dispose();
            }
        }
    }
}
