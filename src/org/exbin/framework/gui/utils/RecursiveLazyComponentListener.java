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
package org.exbin.framework.gui.utils;

import java.awt.Component;
import java.awt.Container;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Recursive interface for panels creating lazy components.
 *
 * @version 0.2.1 2019/07/13
 * @author ExBin Project (http://exbin.org)
 */
@ParametersAreNonnullByDefault
public class RecursiveLazyComponentListener implements LazyComponentListener {

    private final LazyComponentListener listener;

    public RecursiveLazyComponentListener(LazyComponentListener listener) {
        this.listener = listener;
    }

    @Override
    public void componentCreated(Component component) {
        fireListener(component);
    }

    protected void fireListener(Component component) {
        listener.componentCreated(component);

        if (component instanceof Container) {
            Component[] comps = ((Container) component).getComponents();
            for (Component child : comps) {
                fireListener(child);
            }
        }

        if (component instanceof LazyComponentsIssuable) {
            ((LazyComponentsIssuable) component).addChildComponentListener(this);
        }
    }
}
