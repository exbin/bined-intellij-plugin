/*
 * Copyright (C) ExBin Project
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
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.exbin.framework.App;
import org.exbin.framework.action.api.ActionConsts;
import org.exbin.framework.action.api.ActionModuleApi;

/**
 * Utilities for action manipulations.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public final class ActionUtils {

    public static void replaceAction(JPopupMenu menu, String actionId, Action action) {
        for (int i = 0; i < menu.getComponentCount(); i++) {
            Component component = menu.getComponent(i);
            if (component instanceof JMenuItem) {
                Action componentAction = ((JMenuItem) component).getAction();
                if (componentAction != null && actionId.equals(componentAction.getValue(ActionConsts.ACTION_ID))) {
                    menu.remove(i);
                    ActionModuleApi actionModule = App.getModule(ActionModuleApi.class);
                    menu.add(actionModule.actionToMenuItem(action), i);
                    break;
                }
            }
        }
    }
}
