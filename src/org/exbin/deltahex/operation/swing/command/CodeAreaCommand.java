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
package org.exbin.deltahex.operation.swing.command;

import org.exbin.deltahex.swing.CodeArea;
import org.exbin.xbup.operation.AbstractCommand;

/**
 * Abstract class for operation on code area component.
 *
 * @version 0.1.0 2016/06/13
 * @author ExBin Project (http://exbin.org)
 */
public abstract class CodeAreaCommand extends AbstractCommand {

    protected final CodeArea codeArea;

    public CodeAreaCommand(CodeArea codeArea) {
        this.codeArea = codeArea;
    }

    /**
     * Returns type of the command.
     *
     * @return command type
     */
    public abstract CodeAreaCommandType getType();

    @Override
    public String getCaption() {
        return getType().getCaption();
    }
}
