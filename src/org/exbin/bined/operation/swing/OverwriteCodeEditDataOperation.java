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
package org.exbin.bined.operation.swing;

import org.exbin.bined.CodeAreaUtils;
import org.exbin.bined.CodeType;
import org.exbin.bined.capability.CodeTypeCapable;
import org.exbin.bined.operation.BinaryDataOperationException;
import org.exbin.bined.swing.CodeAreaCore;
import org.exbin.utils.binary_data.EditableBinaryData;

/**
 * Operation for editing data using overwrite mode.
 *
 * @version 0.2.0 2018/02/14
 * @author ExBin Project (https://exbin.org)
 */
public class OverwriteCodeEditDataOperation extends CodeEditDataOperation {

    private final long startPosition;
    private final int startCodeOffset;
    private long length = 0;
    private EditableBinaryData undoData = null;
    private final CodeType codeType;

    private int codeOffset = 0;

    public OverwriteCodeEditDataOperation(CodeAreaCore codeArea, long startPosition, int startCodeOffset) {
        super(codeArea);
        this.startPosition = startPosition;
        this.startCodeOffset = startCodeOffset;
        this.codeOffset = startCodeOffset;
        this.codeType = ((CodeTypeCapable) codeArea).getCodeType();
        if (startCodeOffset > 0 && codeArea.getDataSize() > startPosition) {
            undoData = (EditableBinaryData) codeArea.getContentData().copy(startPosition, 1);
            length++;
        }
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

    @Override
    public CodeType getCodeType() {
        return codeType;
    }

    private CodeAreaOperation execute(boolean withUndo) {
        throw new IllegalStateException("Cannot be executed");
    }

    @Override
    public void appendEdit(byte value) {
        EditableBinaryData data = (EditableBinaryData) codeArea.getContentData();
        long editedDataPosition = startPosition + length;

        byte byteValue = 0;
        if (codeOffset > 0) {
            if (editedDataPosition <= data.getDataSize()) {
                byteValue = data.getByte(editedDataPosition - 1);
            }

            editedDataPosition--;
        } else {
            if (editedDataPosition < data.getDataSize()) {
                if (undoData == null) {
                    undoData = (EditableBinaryData) data.copy(editedDataPosition, 1);
                    byteValue = undoData.getByte(0);
                } else {
                    undoData.insert(undoData.getDataSize(), data, editedDataPosition, 1);
                }
            } else if (editedDataPosition > data.getDataSize()) {
                throw new IllegalStateException("Cannot overwrite outside of the document");
            } else {
                data.insertUninitialized(editedDataPosition, 1);
            }

            length++;
        }

        byteValue = CodeAreaUtils.setCodeValue(byteValue, value, codeOffset, codeType);

        data.setByte(editedDataPosition, byteValue);
        codeOffset++;
        if (codeOffset == codeType.getMaxDigitsForByte()) {
            codeOffset = 0;
        }
    }

    @Override
    public CodeAreaOperation[] generateUndo() {
        ModifyDataOperation modifyOperation = null;
        if (undoData != null && !undoData.isEmpty()) {
            modifyOperation = new ModifyDataOperation(codeArea, startPosition, undoData.copy());
        }
        long undoDataSize = undoData == null ? 0 : undoData.getDataSize();
        long removeLength = length - undoDataSize;
        if (removeLength == 0) {
            return new CodeAreaOperation[]{modifyOperation};
        }

        RemoveDataOperation removeOperation = new RemoveDataOperation(codeArea, startPosition + undoDataSize, startCodeOffset, removeLength);
        if (modifyOperation != null) {
            return new CodeAreaOperation[]{modifyOperation, removeOperation};
        }
        return new CodeAreaOperation[]{removeOperation};
    }

    public long getStartPosition() {
        return startPosition;
    }

    public int getStartCodeOffset() {
        return startCodeOffset;
    }

    public long getLength() {
        return length;
    }

    @Override
    public void dispose() throws BinaryDataOperationException {
        super.dispose();
        if (undoData != null) {
            undoData.dispose();
        }
    }
}
