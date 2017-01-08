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
package org.exbin.framework.gui.utils.handler;

import org.exbin.framework.gui.utils.WindowUtils;

/**
 * Handler for close control panel.
 *
 * @version 0.2.0 2016/12/27
 * @author ExBin Project (http://exbin.org)
 */
public interface CloseControlHandler {

    void controlActionPerformed();

    public interface CloseControlListener {

        void performCloseClick();

        WindowUtils.OkCancelListener createOkCancelListener();
    }
}
