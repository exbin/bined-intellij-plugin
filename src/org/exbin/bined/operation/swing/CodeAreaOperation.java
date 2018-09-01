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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.exbin.bined.CaretPosition;
import org.exbin.bined.CodeAreaCaretPosition;
import org.exbin.bined.operation.BinaryDataOperation;
import org.exbin.bined.operation.BinaryDataOperationException;
import org.exbin.bined.swing.CodeAreaCore;

/**
 * Abstract class for operation on code area component.
 *
 * @version 0.2.0 2018/08/11
 * @author ExBin Project (https://exbin.org)
 */
public abstract class CodeAreaOperation implements BinaryDataOperation {

    @Nonnull
    protected final CodeAreaCore codeArea;
    @Nonnull
    protected final CodeAreaCaretPosition backPosition = new CodeAreaCaretPosition();

    public CodeAreaOperation(@Nonnull CodeAreaCore codeArea) {
        this(codeArea, null);
    }

    public CodeAreaOperation(@Nonnull CodeAreaCore codeArea, @Nullable CaretPosition backPosition) {
        this.codeArea = codeArea;
        if (backPosition != null) {
            this.backPosition.setPosition(backPosition);
        }
    }

    /**
     * Returns type of the operation.
     *
     * @return operation type
     */
    @Nonnull
    public abstract CodeAreaOperationType getType();

    @Nonnull
    public CodeAreaCore getCodeArea() {
        return codeArea;
    }

    /**
     * Returns caption as text.
     *
     * @return text caption
     */
    @Nonnull
    @Override
    public String getCaption() {
        return getType().getCaption();
    }

    @Nonnull
    public CaretPosition getBackPosition() {
        return backPosition;
    }

    public void setBackPosition(@Nonnull CaretPosition backPosition) {
        this.backPosition.setPosition(backPosition);
    }

    /**
     * Performs operation on given document.
     *
     * @throws BinaryDataOperationException for operation handling issues
     */
    @Override
    public void execute() throws BinaryDataOperationException {
        execute(ExecutionType.NORMAL);
    }

    /**
     * Performs operation on given document and returns undo operation.
     *
     * @return undo operation or null if not available
     * @throws BinaryDataOperationException for operation handling issues
     */
    @Nullable
    @Override
    public CodeAreaOperation executeWithUndo() throws BinaryDataOperationException {
        return execute(ExecutionType.WITH_UNDO);
    }

    /**
     * Default empty execution method supporting both modes ready for override.
     *
     * @param executionType if undo should be included
     * @return undo operation or null if not available
     */
    @Nullable
    protected CodeAreaOperation execute(@Nonnull ExecutionType executionType) {
        return null;
    }

    /**
     * Performs dispose of the operation.
     *
     * Default dispose is empty.
     *
     * @throws BinaryDataOperationException for operation handling issues
     */
    @Override
    public void dispose() throws BinaryDataOperationException {
    }

    public enum ExecutionType {
        NORMAL, WITH_UNDO
    };
}
