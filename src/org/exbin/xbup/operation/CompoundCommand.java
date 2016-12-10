/*
 * Copyright (C) ExBin Project
 *
 * This application or library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * This application or library is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along this application.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.exbin.xbup.operation;

import java.util.Collection;
import java.util.List;

/**
 * Interface for compound XBUP editor command.
 *
 * @version 0.1.25 2015/04/13
 * @author ExBin Project (http://exbin.org)
 */
public interface CompoundCommand extends Command {

    /**
     * Append command to the list of commands.
     *
     * @param command appended command
     */
    public void appendCommand(Command command);

    /**
     * Append list of commands to the list of commands.
     *
     * @param commands appended commands
     */
    public void appendCommands(Collection<Command> commands);

    /**
     * Returns list of commands.
     *
     * @return list of commands
     */
    public List<Command> getCommands();

    /**
     * Returns true if compound command is empty.
     *
     * @return true if command is empty
     */
    public boolean isEmpty();
}
