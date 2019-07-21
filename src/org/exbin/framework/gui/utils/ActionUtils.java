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

import java.awt.AWTEvent;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.util.ResourceBundle;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.text.JTextComponent;

/**
 * Some simple static methods usable for actions, menus and toolbars.
 *
 * @version 0.2.1 2019/07/18
 * @author ExBin Project (http://exbin.org)
 */
@ParametersAreNonnullByDefault
public class ActionUtils {

    public static final String DIALOG_MENUITEM_EXT = "...";

    /**
     * Action type like or check, radio.
     *
     * Value is ActionType.
     */
    public static final String ACTION_TYPE = "type";
    /**
     * Radio group name value.
     *
     * Value is String.
     */
    public static final String ACTION_RADIO_GROUP = "radioGroup";
    /**
     * Action mode for actions opening dialogs.
     *
     * Value is Boolean.
     */
    public static final String ACTION_DIALOG_MODE = "dialogMode";

    public static final String ACTION_ID = "actionId";
    public static final String ACTION_NAME_POSTFIX = ".text";
    public static final String ACTION_SHORT_DESCRIPTION_POSTFIX = ".shortDescription";
    public static final String ACTION_SMALL_ICON_POSTFIX = ".smallIcon";
    public static final String ACTION_SMALL_LARGE_POSTFIX = ".largeIcon";

    private ActionUtils() {
    }

    /**
     * Sets action values according to values specified by resource bundle.
     *
     * @param action modified action
     * @param bundle source bundle
     * @param actionId action identifier and bundle key prefix
     */
    public static void setupAction(Action action, ResourceBundle bundle, String actionId) {
        setupAction(action, bundle, action.getClass(), actionId);
    }

    /**
     * Sets action values according to values specified by resource bundle.
     *
     * @param action modified action
     * @param bundle source bundle
     * @param resourceClass resourceClass
     * @param actionId action identifier and bundle key prefix
     */
    public static void setupAction(Action action, ResourceBundle bundle, Class resourceClass, String actionId) {
        action.putValue(Action.NAME, bundle.getString(actionId + ACTION_NAME_POSTFIX));
        action.putValue(ACTION_ID, actionId);

        // TODO keystroke from string with meta mask translation
        if (bundle.containsKey(actionId + ACTION_SHORT_DESCRIPTION_POSTFIX)) {
            action.putValue(Action.SHORT_DESCRIPTION, bundle.getString(actionId + ACTION_SHORT_DESCRIPTION_POSTFIX));
        }
        if (bundle.containsKey(actionId + ACTION_SMALL_ICON_POSTFIX)) {
            action.putValue(Action.SMALL_ICON, new javax.swing.ImageIcon(resourceClass.getResource(bundle.getString(actionId + ACTION_SMALL_ICON_POSTFIX))));
        }
        if (bundle.containsKey(actionId + ACTION_SMALL_LARGE_POSTFIX)) {
            action.putValue(Action.LARGE_ICON_KEY, new javax.swing.ImageIcon(resourceClass.getResource(bundle.getString(actionId + ACTION_SMALL_LARGE_POSTFIX))));
        }
    }

    public static int getMetaMask() {
        return java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
    }

    /**
     * Invokes action of given name on text component.
     *
     * @param textComponent component
     * @param actionName action name
     */
    public static void invokeTextAction(JTextComponent textComponent, String actionName) {
        ActionMap textActionMap = textComponent.getActionMap().getParent();
        long eventTime = EventQueue.getMostRecentEventTime();
        int eventMods = getCurrentEventModifiers();
        ActionEvent actionEvent = new ActionEvent(textComponent, ActionEvent.ACTION_PERFORMED, actionName, eventTime, eventMods);
        textActionMap.get(actionName).actionPerformed(actionEvent);
    }

    /**
     * This method was lifted from JTextComponent.java.
     */
    private static int getCurrentEventModifiers() {
        int modifiers = 0;
        AWTEvent currentEvent = EventQueue.getCurrentEvent();
        if (currentEvent instanceof InputEvent) {
            modifiers = ((InputEvent) currentEvent).getModifiers();
        } else if (currentEvent instanceof ActionEvent) {
            modifiers = ((ActionEvent) currentEvent).getModifiers();
        }
        return modifiers;
    }

    /**
     * Enumeration of action types.
     */
    public enum ActionType {
        /**
         * Single click / activation action.
         */
        PUSH,
        /**
         * Checkbox type action.
         */
        CHECK,
        /**
         * Radion type checking, where only one item in radio group can be
         * checked.
         */
        RADIO
    }
}
