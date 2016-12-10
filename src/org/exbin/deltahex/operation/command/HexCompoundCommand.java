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
package org.exbin.deltahex.operation.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.exbin.deltahex.swing.CodeArea;
import org.exbin.xbup.operation.Command;
import org.exbin.xbup.operation.CompoundCommand;

/**
 * Class for compound command on hexadecimal document.
 *
 * @version 0.1.1 2016/09/26
 * @author ExBin Project (http://exbin.org)
 */
public class HexCompoundCommand extends CodeAreaCommand implements CompoundCommand {

    private final List<Command> commands = new ArrayList<>();

    public HexCompoundCommand(CodeArea codeArea) {
        super(codeArea);
    }

    public static CodeAreaCommand buildCompoundCommand(CodeArea codeArea, CodeAreaCommand... commands) {
        CodeAreaCommand resultCommand = null;
        for (CodeAreaCommand command : commands) {
            if (command != null) {
                if (resultCommand == null) {
                    resultCommand = command;
                } else if (resultCommand instanceof HexCompoundCommand) {
                    ((HexCompoundCommand) resultCommand).appendCommand(command);
                } else {
                    HexCompoundCommand compoundCommand = new HexCompoundCommand(codeArea);
                    compoundCommand.appendCommand(resultCommand);
                    compoundCommand.appendCommand(command);
                    resultCommand = compoundCommand;
                }
            }
        }

        return resultCommand;
    }

    @Override
    public CodeAreaCommandType getType() {
        return CodeAreaCommandType.COMPOUND;
    }

    @Override
    public void execute() throws Exception {
        for (Command command : commands) {
            command.execute();
        }
    }

    @Override
    public void redo() throws Exception {
        for (Command command : commands) {
            command.redo();
        }
    }

    @Override
    public void undo() throws Exception {
        for (int i = commands.size() - 1; i >= 0; i--) {
            Command command = commands.get(i);
            command.undo();
        }
    }

    @Override
    public boolean canUndo() {
        boolean canUndo = true;
        for (Command command : commands) {
            if (!command.canUndo()) {
                canUndo = false;
                break;
            }
        }

        return canUndo;
    }

    @Override
    public void appendCommand(Command command) {
        commands.add(command);
    }

    @Override
    public void appendCommands(Collection<Command> commands) {
        commands.addAll(commands);
    }

    @Override
    public List<Command> getCommands() {
        return commands;
    }

    @Override
    public boolean isEmpty() {
        return commands.isEmpty();
    }

    @Override
    public void dispose() throws Exception {
        super.dispose();
        for (Command command : commands) {
            command.dispose();
        }
    }
}
