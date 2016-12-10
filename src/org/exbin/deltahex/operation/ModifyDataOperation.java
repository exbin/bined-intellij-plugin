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

import org.exbin.deltahex.swing.CodeArea;
import org.exbin.utils.binary_data.BinaryData;
import org.exbin.utils.binary_data.EditableBinaryData;

/**
 * Operation for modifying data.
 *
 * @version 0.1.0 2016/11/05
 * @author ExBin Project (http://exbin.org)
 */
public class ModifyDataOperation extends CodeAreaOperation {

    private final long position;
    private final BinaryData data;

    public ModifyDataOperation(CodeArea codeArea, long position, BinaryData data) {
        super(codeArea);
        this.position = position;
        this.data = data;
    }

    @Override
    public CodeAreaOperationType getType() {
        return CodeAreaOperationType.MODIFY_DATA;
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
            BinaryData undoData = codeArea.getData().copy(position, data.getDataSize());
            undoOperation = new ModifyDataOperation(codeArea, position, undoData);
        }
        ((EditableBinaryData) codeArea.getData()).replace(position, data);
        return undoOperation;
    }

    public void appendData(BinaryData appendData) {
        ((EditableBinaryData) data).insert(data.getDataSize(), appendData);
    }
}
