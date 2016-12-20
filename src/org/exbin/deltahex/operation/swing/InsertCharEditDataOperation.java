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

import org.exbin.deltahex.operation.BinaryDataOperationException;
import org.exbin.deltahex.swing.CodeArea;
import org.exbin.utils.binary_data.EditableBinaryData;

/**
 * Operation for editing data unsing insert mode.
 *
 * @version 0.1.2 2016/12/20
 * @author ExBin Project (http://exbin.org)
 */
public class InsertCharEditDataOperation extends CharEditDataOperation {

    private final long startPosition;
    private long length;

    public InsertCharEditDataOperation(CodeArea coreArea, long startPosition) {
        super(coreArea);
        this.startPosition = startPosition;
    }

    @Override
    public CodeAreaOperationType getType() {
        return CodeAreaOperationType.EDIT_DATA;
    }

    @Override
    public void execute() throws BinaryDataOperationException {
        execute(false);
    }

    @Override
    public CodeAreaOperation executeWithUndo() throws BinaryDataOperationException {
        return execute(true);
    }

    private CodeAreaOperation execute(boolean withUndo) {
        throw new IllegalStateException("Cannot be executed");
    }

    @Override
    public void appendEdit(char value) {
        EditableBinaryData data = (EditableBinaryData) codeArea.getData();
        long editedDataPosition = startPosition + length;

        byte[] bytes = codeArea.charToBytes(value);
        data.insert(editedDataPosition, bytes);
        length += bytes.length;
        codeArea.getCaret().setCaretPosition(startPosition + length);
    }

    @Override
    public CodeAreaOperation[] generateUndo() {
        return new CodeAreaOperation[]{new RemoveDataOperation(codeArea, startPosition, 0, length)};
    }

    public long getStartPosition() {
        return startPosition;
    }

    public long getLength() {
        return length;
    }
}
