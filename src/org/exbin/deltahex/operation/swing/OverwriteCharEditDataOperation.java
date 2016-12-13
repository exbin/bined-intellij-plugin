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

import org.exbin.deltahex.swing.CodeArea;
import org.exbin.utils.binary_data.EditableBinaryData;

/**
 * Operation for editing data using overwrite mode.
 *
 * @version 0.1.1 2016/09/21
 * @author ExBin Project (http://exbin.org)
 */
public class OverwriteCharEditDataOperation extends CharEditDataOperation {

    private final long startPosition;
    private long length = 0;
    private EditableBinaryData undoData = null;

    public OverwriteCharEditDataOperation(CodeArea coreArea, long startPosition) {
        super(coreArea);
        this.startPosition = startPosition;
    }

    @Override
    public CodeAreaOperationType getType() {
        return CodeAreaOperationType.EDIT_DATA;
    }

    @Override
    public void execute() throws Exception {
        execute(false);
    }

    @Override
    public CodeAreaOperation executeWithUndo() throws Exception {
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
        if (editedDataPosition < data.getDataSize()) {
            long overwritten = data.getDataSize() - editedDataPosition;
            if (overwritten > bytes.length) {
                overwritten = bytes.length;
            }
            EditableBinaryData overwrittenData = (EditableBinaryData) data.copy(editedDataPosition, overwritten);
            if (undoData == null) {
                undoData = overwrittenData;
            } else {
                undoData.insert(undoData.getDataSize(), overwrittenData);
            }
            for (int i = 0; i < overwritten; i++) {
                data.setByte(editedDataPosition + i, bytes[i]);
            }
        }
        if (editedDataPosition + bytes.length > data.getDataSize()) {
            if (editedDataPosition == data.getDataSize()) {
                data.insert(editedDataPosition, bytes);
            } else {
                int inserted = (int) (editedDataPosition + bytes.length - data.getDataSize());
                long insertPosition = editedDataPosition + bytes.length - inserted;
                data.insert(insertPosition, inserted);
                for (int i = 0; i < inserted; i++) {
                    data.setByte(insertPosition + i, bytes[bytes.length - inserted + i]);
                }
            }
        }

        length += bytes.length;
        codeArea.getCaret().setCaretPosition(startPosition + length);
    }

    @Override
    public CodeAreaOperation[] generateUndo() {
        ModifyDataOperation modifyOperation = null;
        if (undoData != null && !undoData.isEmpty()) {
            modifyOperation = new ModifyDataOperation(codeArea, startPosition, undoData);
        }
        long undoDataSize = undoData == null ? 0 : undoData.getDataSize();
        RemoveDataOperation removeOperation = new RemoveDataOperation(codeArea, startPosition + undoDataSize, 0, length - undoDataSize);

        if (modifyOperation != null) {
            return new CodeAreaOperation[]{modifyOperation, removeOperation};
        }
        return new CodeAreaOperation[]{removeOperation};
    }

    public long getStartPosition() {
        return startPosition;
    }

    public long getLength() {
        return length;
    }
}
