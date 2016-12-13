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
 * Operation for deleting child block.
 *
 * @version 0.1.1 2016/09/26
 * @author ExBin Project (http://exbin.org)
 */
public class RemoveDataOperation extends CodeAreaOperation {

    private final long position;
    private final int codeOffset;
    private final long size;

    public RemoveDataOperation(CodeArea codeArea, long position, int codeOffset, long size) {
        super(codeArea);
        this.position = position;
        this.codeOffset = codeOffset;
        this.size = size;
    }

    @Override
    public CodeAreaOperationType getType() {
        return CodeAreaOperationType.REMOVE_DATA;
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
        CodeAreaOperation undoOperation = null;
        if (withUndo) {
            EditableBinaryData undoData = (EditableBinaryData) codeArea.getData().copy(position, size);
            undoOperation = new InsertDataOperation(codeArea, position, codeOffset, undoData);
        }
        ((EditableBinaryData) codeArea.getData()).remove(position, size);
        codeArea.getCaret().setCaretPosition(position, codeOffset);
        return undoOperation;
    }
}
