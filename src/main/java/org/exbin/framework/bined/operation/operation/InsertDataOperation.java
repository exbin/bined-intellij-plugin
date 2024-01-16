/*
 * Copyright (C) ExBin Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.exbin.framework.bined.operation.operation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.auxiliary.binary_data.EditableBinaryData;
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
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class InsertDataOperation extends CodeAreaOperation {

    private final long position;
    private final long length;
    private final InsertionDataProvider dataOperationDataProvider;

    public InsertDataOperation(CodeAreaCore codeArea, long position, long length, InsertionDataProvider dataOperationDataProvider) {
        super(codeArea);
        this.position = position;
        this.length = length;
        this.dataOperationDataProvider = dataOperationDataProvider;
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
        EditableBinaryData contentData = (EditableBinaryData) codeArea.getContentData();

        contentData.insertUninitialized(position, length);
        dataOperationDataProvider.provideData(contentData, position);

        if (withUndo) {
            undoOperation = new RemoveDataOperation(codeArea, position, 0, length);
        }
        ((CaretCapable) codeArea).getCaret().setCaretPosition(position + length, 0);
        return undoOperation;
    }

    @Override
    public void dispose() throws BinaryDataOperationException {
        super.dispose();
    }

    @ParametersAreNonnullByDefault
    public static class InsertDataCommand extends CodeAreaCommand {

        private final InsertDataOperation operation;
        private CodeAreaOperation undoOperation;

        public InsertDataCommand(InsertDataOperation operation) {
            super(operation.getCodeArea());
            this.operation = operation;
        }

        @Nonnull
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
