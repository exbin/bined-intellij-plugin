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
package org.exbin.framework.bined.operation;

import java.util.Random;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.auxiliary.paged_data.EditableBinaryData;
import org.exbin.bined.CodeAreaUtils;
import org.exbin.bined.capability.CaretCapable;
import org.exbin.bined.capability.ScrollingCapable;
import org.exbin.bined.operation.BinaryDataOperationException;
import org.exbin.bined.operation.swing.CodeAreaOperation;
import org.exbin.bined.operation.swing.CodeAreaOperationType;
import org.exbin.bined.operation.swing.RemoveDataOperation;
import org.exbin.bined.operation.swing.command.CodeAreaCommand;
import org.exbin.bined.operation.swing.command.CodeAreaCommandType;
import org.exbin.bined.swing.CodeAreaCore;

/**
 * Insert data operation.
 *
 * @version 0.2.1 2021/09/25
 * @author ExBin Project (http://exbin.org)
 */
@ParametersAreNonnullByDefault
public class InsertDataOperation extends CodeAreaOperation {

    private final long position;
    private final long length;
    private final FillWithType fillWithType;
    private final EditableBinaryData data;

    public InsertDataOperation(CodeAreaCore codeArea, long position, long length, FillWithType fillWithType, @Nullable EditableBinaryData data) {
        super(codeArea);
        this.position = position;
        this.length = length;
        this.fillWithType = fillWithType;
        this.data = data;
    }

    @Nonnull
    @Override
    public CodeAreaOperationType getType() {
        return CodeAreaOperationType.INSERT_DATA;
    }

    @Nullable
    @Override
    public void execute() throws BinaryDataOperationException {
        execute(false);
    }

    @Nullable
    @Override
    public CodeAreaOperation executeWithUndo() throws BinaryDataOperationException {
        return execute(true);
    }

    @Nullable
    private CodeAreaOperation execute(boolean withUndo) {
        CodeAreaOperation undoOperation = null;
        EditableBinaryData contentData = CodeAreaUtils.requireNonNull(((EditableBinaryData) codeArea.getContentData()));

        switch (fillWithType) {
            case EMPTY: {
                contentData.insert(position, length);
                break;
            }
            case SPACE: {
                contentData.insertUninitialized(position, length);
                for (long pos = position; pos < position + length; pos++) {
                    contentData.setByte(pos, (byte) 0x20);
                }
                break;
            }
            case RANDOM: {
                contentData.insertUninitialized(position, length);
                Random random = new Random();
                for (long pos = position; pos < position + length; pos++) {
                    contentData.setByte(pos, (byte) random.nextInt());
                }
                break;
            }
            case SAMPLE: {
                contentData.insertUninitialized(position, length);

                if (data == null || data.isEmpty()) {
                    for (long pos = position; pos < position + length; pos++) {
                        contentData.setByte(pos, (byte) 0xFF);
                    }
                } else {
                    long dataSize = data.getDataSize();
                    long pos = position;
                    long remain = length;
                    while (remain > 0) {
                        long seg = Math.min(remain, dataSize);
                        contentData.replace(pos, data, 0, seg);
                        pos += seg;
                        remain -= seg;
                    }
                }

                break;
            }
            default:
                throw CodeAreaUtils.getInvalidTypeException(fillWithType);
        }

        if (withUndo) {
            undoOperation = new RemoveDataOperation(codeArea, position, 0, length);
        }
        ((CaretCapable) codeArea).getCaret().setCaretPosition(position + length, 0);
        return undoOperation;
    }

    @Override
    public void dispose() throws BinaryDataOperationException {
        super.dispose();
        data.dispose();
    }

    public enum FillWithType {
        EMPTY,
        SPACE,
        RANDOM,
        SAMPLE
    }

    @ParametersAreNonnullByDefault
    public static class InsertDataCommand extends CodeAreaCommand {

        private final InsertDataOperation operation;
        private CodeAreaOperation undoOperation;

        public InsertDataCommand(InsertDataOperation operation) {
            super(operation.getCodeArea());
            this.operation = operation;
        }

        @Override
        public CodeAreaCommandType getType() {
            return CodeAreaCommandType.DATA_INSERTED;
        }

        @Override
        public void redo() throws BinaryDataOperationException {
            undoOperation = operation.executeWithUndo();
            ((ScrollingCapable) codeArea).revealCursor();
            codeArea.notifyDataChanged();
        }

        @Override
        public void undo() throws BinaryDataOperationException {
            undoOperation.execute();
            undoOperation.dispose();
            ((ScrollingCapable) codeArea).revealCursor();
            codeArea.notifyDataChanged();
        }

        @Override
        public boolean canUndo() {
            return true;
        }

        @Override
        public void dispose() throws BinaryDataOperationException {
            super.dispose();
            operation.dispose();
        }
    }
}
