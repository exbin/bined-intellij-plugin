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

/**
 * Interface for XBUP editor operation.
 *
 * @version 0.1.25 2015/04/26
 * @author ExBin Project (http://exbin.org)
 */
public interface Operation {

    /**
     * Returns caption as text.
     *
     * @return text caption
     */
    public String getCaption();

    /**
     * Performs operation on given document.
     *
     * @throws java.lang.Exception
     */
    public void execute() throws Exception;

    /**
     * Performs operation on given document and returns undo operation.
     *
     * @return undo operation or null if not available
     * @throws java.lang.Exception
     */
    public Operation executeWithUndo() throws Exception;

    /**
     * Disposes command.
     *
     * @throws java.lang.Exception
     */
    public void dispose() throws Exception;
}
