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
package org.exbin.deltahex.operation.swing;

import org.exbin.deltahex.operation.BinaryDataOperation;
import org.exbin.deltahex.operation.BinaryDataOperationException;
import org.exbin.deltahex.swing.CodeArea;

/**
 * Abstract class for operation on code area component.
 *
 * @version 0.1.2 2017/01/01
 * @author ExBin Project (http://exbin.org)
 */
public abstract class CodeAreaOperation implements BinaryDataOperation {

    protected final CodeArea codeArea;

    public CodeAreaOperation(CodeArea codeArea) {
        this.codeArea = codeArea;
    }

    /**
     * Returns type of the operation.
     *
     * @return operation type
     */
    public abstract CodeAreaOperationType getType();

    public CodeArea getCodeArea() {
        return codeArea;
    }

    /**
     * Returns caption as text.
     *
     * @return text caption
     */
    @Override
    public String getCaption() {
        return getType().getCaption();
    }

    /**
     * Performs operation on given document.
     *
     * @throws BinaryDataOperationException for operation handling issues
     */
    @Override
    public abstract void execute() throws BinaryDataOperationException;

    /**
     * Performs operation on given document and returns undo operation.
     *
     * @return undo operation or null if not available
     * @throws BinaryDataOperationException for operation handling issues
     */
    @Override
    public abstract CodeAreaOperation executeWithUndo() throws BinaryDataOperationException;

    /**
     * Performs dispose of the operation.
     *
     * Default dispose is empty.
     *
     * @throws BinaryDataOperationException for operation handling issues
     */
    @Override
    public void dispose() throws BinaryDataOperationException {
    }
}
