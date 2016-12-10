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

import java.util.Date;

/**
 * Abstract command class.
 *
 * @version 0.2.0 2016/05/17
 * @author ExBin Project (http://exbin.org)
 */
public abstract class AbstractCommand implements Command {

    private Date executionTime = null;

    /**
     * Default execution method performs simply redo operation.
     *
     * @throws Exception
     */
    @Override
    public void execute() throws Exception {
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
     * @throws Exception
     */
    @Override
    public void dispose() throws Exception {
    }

    @Override
    public Date getExecutionTime() {
        return executionTime;
    }
}
