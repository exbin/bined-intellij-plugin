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

import org.exbin.bined.CodeType;
import org.exbin.bined.swing.CodeAreaCore;

/**
 * Abstract operation for editing data.
 *
 * @version 0.1.0 2015/05/17
 * @author ExBin Project (https://exbin.org)
 */
public abstract class CodeEditDataOperation extends CodeAreaOperation {

    public CodeEditDataOperation(CodeAreaCore coreArea) {
        super(coreArea);
    }

    /**
     * Code type used for this edit operation.
     *
     * @return code type
     */
    public abstract CodeType getCodeType();

    /**
     * Appends next hexadecimal value in editing action sequence.
     *
     * @param value half-byte value (0..15)
     */
    public abstract void appendEdit(byte value);

    /**
     * Generates undo operation for combined editing action.
     *
     * @return hexadecimal operation
     */
    public abstract CodeAreaOperation[] generateUndo();
}
