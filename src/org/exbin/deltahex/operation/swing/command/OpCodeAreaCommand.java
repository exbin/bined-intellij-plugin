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
package org.exbin.deltahex.operation.swing.command;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.exbin.deltahex.swing.CodeArea;
import org.exbin.deltahex.operation.swing.CodeAreaOperation;
import org.exbin.deltahex.operation.swing.CodeAreaOperationEvent;
import org.exbin.xbup.operation.OperationListener;
import org.exbin.deltahex.operation.swing.CodeAreaOperationListener;

/**
 * Abstract class for operation on hexadecimal document.
 *
 * @version 0.1.1 2016/09/26
 * @author ExBin Project (http://exbin.org)
 */
public abstract class OpCodeAreaCommand extends CodeAreaCommand {

    protected CodeAreaOperation operation;
    protected boolean operationPerformed = false;

    public OpCodeAreaCommand(CodeArea codeArea) {
        super(codeArea);
    }

    public CodeAreaOperation getOperation() {
        return operation;
    }

    public void setOperation(CodeAreaOperation operation) {
        if (this.operation != null) {
            try {
                this.operation.dispose();
            } catch (Exception ex) {
                Logger.getLogger(OpCodeAreaCommand.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        this.operation = operation;
    }

    @Override
    public boolean canUndo() {
        return true;
    }

    @Override
    public void undo() throws Exception {
        if (operationPerformed) {
            CodeAreaOperation redoOperation = operation.executeWithUndo();
            operation.dispose();
            if (codeArea instanceof OperationListener) {
                ((CodeAreaOperationListener) codeArea).notifyChange(new CodeAreaOperationEvent(operation));
            }

            operation = redoOperation;
            operationPerformed = false;
        } else {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    @Override
    public void redo() throws Exception {
        if (!operationPerformed) {
            CodeAreaOperation undoOperation = operation.executeWithUndo();
            operation.dispose();
            if (codeArea instanceof OperationListener) {
                ((CodeAreaOperationListener) codeArea).notifyChange(new CodeAreaOperationEvent(operation));
            }

            operation = undoOperation;
            operationPerformed = true;
        } else {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    @Override
    public void dispose() throws Exception {
        super.dispose();
        if (operation != null) {
            operation.dispose();
        }
    }
}
