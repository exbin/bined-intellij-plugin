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
package org.exbin.framework.gui.utils;

import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.framework.api.Preferences;

/**
 * Structure for window position.
 *
 * @version 0.2.0 2016/12/04
 * @author ExBin Project (http://exbin.org)
 */
@ParametersAreNonnullByDefault
public class WindowPosition {

    public static final String PREFERENCES_SCREEN_INDEX = "screenIndex";
    public static final String PREFERENCES_SCREEN_WIDTH = "screenWidth";
    public static final String PREFERENCES_SCREEN_HEIGHT = "screenHeight";
    public static final String PREFERENCES_POSITION_X = "positionX";
    public static final String PREFERENCES_POSITION_Y = "positionY";
    public static final String PREFERENCES_WIDTH = "width";
    public static final String PREFERENCES_HEIGHT = "height";
    public static final String PREFERENCES_MAXIMIZED = "maximized";

    private int screenIndex;
    private int screenWidth;
    private int screenHeight;
    private int relativeX;
    private int relativeY;
    private int width;
    private int height;
    private boolean maximized = false;

    public int getScreenIndex() {
        return screenIndex;
    }

    public void setScreenIndex(int screenIndex) {
        this.screenIndex = screenIndex;
    }

    public int getScreenWidth() {
        return screenWidth;
    }

    public void setScreenWidth(int screenWidth) {
        this.screenWidth = screenWidth;
    }

    public int getScreenHeight() {
        return screenHeight;
    }

    public void setScreenHeight(int screenHeight) {
        this.screenHeight = screenHeight;
    }

    public int getRelativeX() {
        return relativeX;
    }

    public void setRelativeX(int relativeX) {
        this.relativeX = relativeX;
    }

    public int getRelativeY() {
        return relativeY;
    }

    public void setRelativeY(int relativeY) {
        this.relativeY = relativeY;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public boolean isMaximized() {
        return maximized;
    }

    public void setMaximized(boolean maximized) {
        this.maximized = maximized;
    }

    public void saveToPreferences(Preferences pref, String prefix) {
        pref.putInt(prefix + PREFERENCES_SCREEN_INDEX, screenIndex);
        pref.putInt(prefix + PREFERENCES_SCREEN_WIDTH, screenWidth);
        pref.putInt(prefix + PREFERENCES_SCREEN_HEIGHT, screenHeight);
        pref.putInt(prefix + PREFERENCES_POSITION_X, relativeX);
        pref.putInt(prefix + PREFERENCES_POSITION_Y, relativeY);
        pref.putInt(prefix + PREFERENCES_WIDTH, width);
        pref.putInt(prefix + PREFERENCES_HEIGHT, height);
        pref.putBoolean(prefix + PREFERENCES_MAXIMIZED, maximized);
    }

    public void loadFromPreferences(Preferences pref, String prefix) {
        screenIndex = pref.getInt(prefix + PREFERENCES_SCREEN_INDEX, 0);
        screenWidth = pref.getInt(prefix + PREFERENCES_SCREEN_WIDTH, 0);
        screenHeight = pref.getInt(prefix + PREFERENCES_SCREEN_HEIGHT, 0);
        relativeX = pref.getInt(prefix + PREFERENCES_POSITION_X, 0);
        relativeY = pref.getInt(prefix + PREFERENCES_POSITION_Y, 0);
        width = pref.getInt(prefix + PREFERENCES_WIDTH, 0);
        height = pref.getInt(prefix + PREFERENCES_HEIGHT, 0);
        maximized = pref.getBoolean(prefix + PREFERENCES_MAXIMIZED, false);
    }

    public boolean preferencesExists(Preferences pref, String prefix) {
        return pref.exists(prefix + PREFERENCES_SCREEN_INDEX);
    }
}
