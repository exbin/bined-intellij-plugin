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

import java.util.Date;

/**
 * Abstract code area command class.
 *
 * @version 0.1.2 2016/12/20
 * @author ExBin Project (http://exbin.org)
 */
public abstract class BinaryDataAbstractCommand implements BinaryDataCommand {

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

    @Override
    public Date getExecutionTime() {
        return executionTime;
    }
}
