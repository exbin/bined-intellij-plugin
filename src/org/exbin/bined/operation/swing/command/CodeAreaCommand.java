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
package org.exbin.bined.operation.swing.command;

import javax.annotation.Nonnull;
import org.exbin.bined.operation.BinaryDataAbstractCommand;
import org.exbin.bined.swing.CodeAreaCore;

/**
 * Abstract class for operation on code area component.
 *
 * @version 0.2.0 2018/02/14
 * @author ExBin Project (https://exbin.org)
 */
public abstract class CodeAreaCommand extends BinaryDataAbstractCommand {

    @Nonnull
    protected final CodeAreaCore codeArea;

    public CodeAreaCommand(@Nonnull CodeAreaCore codeArea) {
        this.codeArea = codeArea;
    }

    /**
     * Returns type of the command.
     *
     * @return command type
     */
    @Nonnull
    public abstract CodeAreaCommandType getType();

    @Nonnull
    @Override
    public String getCaption() {
        return getType().getCaption();
    }
}
