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
package org.exbin.deltahex.operation;

import org.exbin.deltahex.CodeType;
import org.exbin.deltahex.swing.CodeArea;
import org.exbin.utils.binary_data.EditableBinaryData;

/**
 * Operation for editing data using overwrite mode.
 *
 * @version 0.1.1 2016/09/21
 * @author ExBin Project (http://exbin.org)
 */
public class OverwriteCodeEditDataOperation extends CodeEditDataOperation {

    private final long startPosition;
    private final int startCodeOffset;
    private long length = 0;
    private EditableBinaryData undoData = null;
    private final CodeType codeType;

    private int codeOffset = 0;

    public OverwriteCodeEditDataOperation(CodeArea codeArea, long startPosition, int startCodeOffset) {
        super(codeArea);
        this.startPosition = startPosition;
        this.startCodeOffset = startCodeOffset;
        this.codeOffset = startCodeOffset;
        this.codeType = codeArea.getCodeType();
        if (startCodeOffset > 0 && codeArea.getDataSize() > startPosition) {
            undoData = (EditableBinaryData) codeArea.getData().copy(startPosition, 1);
            length++;
        }
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

    @Override
    public CodeType getCodeType() {
        return codeType;
    }

    private CodeAreaOperation execute(boolean withUndo) {
        throw new IllegalStateException("Cannot be executed");
    }

    @Override
    public void appendEdit(byte value) {
        EditableBinaryData data = (EditableBinaryData) codeArea.getData();
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
            } else {
                throw new IllegalStateException("Cannot overwrite outside of the document");
            }

            length++;
        }

        switch (codeType) {
            case BINARY: {
                int bitMask = 0x80 >> codeOffset;
                byteValue = (byte) (byteValue & (0xff - bitMask) | (value << (7 - codeOffset)));
                break;
            }
            case DECIMAL: {
                int newValue = byteValue & 0xff;
                switch (codeOffset) {
                    case 0: {
                        newValue = (newValue % 100) + value * 100;
                        if (newValue > 255) {
                            newValue = 200;
                        }
                        break;
                    }
                    case 1: {
                        newValue = (newValue / 100) * 100 + value * 10 + (newValue % 10);
                        if (newValue > 255) {
                            newValue -= 200;
                        }
                        break;
                    }
                    case 2: {
                        newValue = (newValue / 10) * 10 + value;
                        if (newValue > 255) {
                            newValue -= 200;
                        }
                        break;
                    }
                }

                byteValue = (byte) newValue;
                break;
            }
            case OCTAL: {
                int newValue = byteValue & 0xff;
                switch (codeOffset) {
                    case 0: {
                        newValue = (newValue % 64) + value * 64;
                        break;
                    }
                    case 1: {
                        newValue = (newValue / 64) * 64 + value * 8 + (newValue % 8);
                        break;
                    }
                    case 2: {
                        newValue = (newValue / 8) * 8 + value;
                        break;
                    }
                }

                byteValue = (byte) newValue;
                break;
            }
            case HEXADECIMAL: {
                if (codeOffset == 1) {
                    byteValue = (byte) ((byteValue & 0xf0) | value);
                } else {
                    byteValue = (byte) ((byteValue & 0xf) | (value << 4));
                }
                break;
            }
            default:
                throw new IllegalStateException("Unexpected code type " + codeType.name());
        }

        data.setByte(editedDataPosition, byteValue);
        codeOffset++;
        if (codeOffset == codeType.getMaxDigits()) {
            codeOffset = 0;
        }
    }

    @Override
    public CodeAreaOperation[] generateUndo() {
        ModifyDataOperation modifyOperation = null;
        if (undoData != null && !undoData.isEmpty()) {
            modifyOperation = new ModifyDataOperation(codeArea, startPosition, undoData);
        }
        long undoDataSize = undoData == null ? 0 : undoData.getDataSize();
        long removeLength = length - undoDataSize;
        RemoveDataOperation removeOperation;
        if (removeLength == 0) {
            return new CodeAreaOperation[]{modifyOperation};
        }
        removeOperation = new RemoveDataOperation(codeArea, startPosition + undoDataSize, startCodeOffset, removeLength);

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
}
