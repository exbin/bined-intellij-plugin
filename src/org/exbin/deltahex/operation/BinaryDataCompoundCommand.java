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

import java.util.Collection;
import java.util.List;

/**
 * Interface for compound command.
 *
 * @version 0.1.2 2016/12/20
 * @author ExBin Project (http://exbin.org)
 */
public interface BinaryDataCompoundCommand extends BinaryDataCommand {

    /**
     * Append command to the list of commands.
     *
     * @param command appended command
     */
    public void appendCommand(BinaryDataCommand command);

    /**
     * Append list of commands to the list of commands.
     *
     * @param commands appended commands
     */
    public void appendCommands(Collection<BinaryDataCommand> commands);

    /**
     * Returns list of commands.
     *
     * @return list of commands
     */
    public List<BinaryDataCommand> getCommands();

    /**
     * Returns true if compound command is empty.
     *
     * @return true if command is empty
     */
    public boolean isEmpty();
}
