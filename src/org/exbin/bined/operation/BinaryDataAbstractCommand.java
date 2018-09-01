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
package org.exbin.bined.operation;

import java.util.Date;
import javax.annotation.Nullable;

/**
 * Abstract code area command class.
 *
 * @version 0.2.0 2018/02/13
 * @author ExBin Project (https://exbin.org)
 */
public abstract class BinaryDataAbstractCommand implements BinaryDataCommand {

    @Nullable
    private Date executionTime = null;

    /**
     * Default execution method performs simply redo operation.
     *
     * @throws BinaryDataOperationException for operation handling issues
     */
    @Override
    public void execute() throws BinaryDataOperationException {
        use();
        redo();
    }

    /**
     * Performs update of command use information.
     */
    @Override
    public void use() {
        executionTime = new Date();
    }

    /**
     * Default dispose method do nothing.
     *
     * @throws BinaryDataOperationException for operation handling issues
     */
    @Override
    public void dispose() throws BinaryDataOperationException {
    }

    @Nullable
    @Override
    public Date getExecutionTime() {
        return executionTime;
    }
}
