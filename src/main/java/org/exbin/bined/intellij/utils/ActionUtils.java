/*
 * Copyright (C) ExBin Project, https://exbin.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.exbin.bined.intellij.utils;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import org.jspecify.annotations.NullMarked;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.exbin.jaguif.App;
import org.exbin.jaguif.action.api.ActionConsts;
import org.exbin.jaguif.menu.api.MenuModuleApi;

/**
 * Utilities for action manipulations.
 */
@NullMarked
public final class ActionUtils {

    public static void replaceAction(JPopupMenu menu, String actionId, Action action) {
        for (int i = 0; i < menu.getComponentCount(); i++) {
            Component component = menu.getComponent(i);
            if (component instanceof JMenu) {
                replaceAction((JMenu) component, actionId, action);
            } else if (component instanceof JMenuItem) {
                Action componentAction = ((JMenuItem) component).getAction();
                if (componentAction != null && actionId.equals(componentAction.getValue(ActionConsts.ACTION_ID))) {
                    menu.remove(i);
                    MenuModuleApi actionModule = App.getModule(MenuModuleApi.class);
                    menu.add(actionModule.actionToMenuItem(action), i);
                    break;
                }
            }
        }
    }

    public static void replaceAction(JMenu menu, String actionId, Action action) {
        for (int i = 0; i < menu.getItemCount(); i++) {
            JMenuItem component = menu.getItem(i);
            if (component instanceof JMenu) {
                replaceAction((JMenu) component, actionId, action);
            } else if (component != null) {
                Action componentAction = component.getAction();
                if (componentAction != null && actionId.equals(componentAction.getValue(ActionConsts.ACTION_ID))) {
                    menu.remove(i);
                    MenuModuleApi actionModule = App.getModule(MenuModuleApi.class);
                    menu.add(actionModule.actionToMenuItem(action), i);
                    break;
                }
            }
        }
    }
}
