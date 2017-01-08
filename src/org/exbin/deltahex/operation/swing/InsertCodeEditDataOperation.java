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

import org.exbin.deltahex.CodeType;
import org.exbin.deltahex.operation.BinaryDataOperationException;
import org.exbin.deltahex.swing.CodeArea;
import org.exbin.utils.binary_data.EditableBinaryData;

/**
 * Operation for editing data unsing insert mode.
 *
 * @version 0.1.2 2016/12/20
 * @author ExBin Project (http://exbin.org)
 */
public class InsertCodeEditDataOperation extends CodeEditDataOperation {

    private final long startPosition;
    private final int startCodeOffset;
    private boolean trailing = false;
    private EditableBinaryData trailingValue = null;
    private final CodeType codeType;

    private long length;
    private int codeOffset = 0;

    public InsertCodeEditDataOperation(CodeArea codeArea, long startPosition, int startCodeOffset) {
        super(codeArea);
        codeType = codeArea.getCodeType();
        this.startPosition = startPosition;
        this.startCodeOffset = startCodeOffset;
        this.codeOffset = startCodeOffset;
        if (codeOffset > 0) {
            length = 1;
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
        EditableBinaryData data = (EditableBinaryData) codeArea.getData();
        long editedDataPosition = startPosition + length;

        byte byteValue = 0;
        if (codeOffset > 0) {
            byteValue = data.getByte(editedDataPosition - 1);
            byte byteRest = 0;
            switch (codeType) {
                case BINARY: {
                    byteRest = (byte) (byteValue & (0xff >> codeOffset));
                    break;
                }
                case DECIMAL: {
                    byteRest = (byte) (byteValue % (codeOffset == 1 ? 100 : 10));
                    break;
                }
                case OCTAL: {
                    byteRest = (byte) (byteValue % (codeOffset == 1 ? 64 : 8));
                    break;
                }
                case HEXADECIMAL: {
                    byteRest = (byte) (byteValue & 0xf);
                    break;
                }
                default:
                    throw new IllegalStateException("Unexpected code type " + codeType.name());
            }
            if (byteRest > 0) {
                if (trailing) {
                    throw new IllegalStateException("Unexpected trailing flag");
                }
                trailingValue = (EditableBinaryData) data.copy(editedDataPosition - 1, 1);
                data.insert(editedDataPosition, 1);
                data.setByte(editedDataPosition, byteRest);
                byteValue -= byteRest;
                trailing = true;
            }
            editedDataPosition--;
        } else {
            data.insert(editedDataPosition, 1);
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
        if (trailing) {
            ModifyDataOperation modifyDataOperation = new ModifyDataOperation(codeArea, startPosition, trailingValue);
            return new CodeAreaOperation[]{modifyDataOperation, new RemoveDataOperation(codeArea, startPosition, startCodeOffset, length)};
        }

        return new CodeAreaOperation[]{new RemoveDataOperation(codeArea, startPosition, startCodeOffset, length)};
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
        if (trailingValue != null) {
            trailingValue.dispose();
        }
    }
}
