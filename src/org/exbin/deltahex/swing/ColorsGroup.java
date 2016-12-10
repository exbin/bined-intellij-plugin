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
package org.exbin.deltahex.swing;

import java.awt.Color;

/**
 * Set of colors for different sections of component rendering.
 *
 * @version 0.1.1 2016/08/31
 * @author ExBin Project (http://exbin.org)
 */
public class ColorsGroup {

    private Color textColor;
    private Color backgroundColor;
    private Color unprintablesColor;
    private Color unprintablesBackgroundColor;

    public ColorsGroup() {
    }

    /**
     * Copy constructor.
     *
     * @param colorsGroup colors group
     */
    public ColorsGroup(ColorsGroup colorsGroup) {
        setColorsFromGroup(colorsGroup);
    }

    private void setColorsFromGroup(ColorsGroup colorsGroup) {
        textColor = colorsGroup.getTextColor();
        backgroundColor = colorsGroup.getBackgroundColor();
        unprintablesColor = colorsGroup.getUnprintablesColor();
        unprintablesBackgroundColor = colorsGroup.getUnprintablesBackgroundColor();
    }

    public Color getTextColor() {
        return textColor;
    }

    public void setTextColor(Color textColor) {
        this.textColor = textColor;
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public Color getUnprintablesColor() {
        return unprintablesColor;
    }

    public void setUnprintablesColor(Color unprintablesColor) {
        this.unprintablesColor = unprintablesColor;
    }

    public Color getUnprintablesBackgroundColor() {
        return unprintablesBackgroundColor;
    }

    public void setUnprintablesBackgroundColor(Color unprintablesBackgroundColor) {
        this.unprintablesBackgroundColor = unprintablesBackgroundColor;
    }

    public void setBothBackgroundColors(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
        this.unprintablesBackgroundColor = backgroundColor;
    }

    public void setColors(ColorsGroup colorsGroup) {
        setColorsFromGroup(colorsGroup);
    }

    public Color getColor(ColorType colorType) {
        switch (colorType) {
            case TEXT:
                return textColor;
            case BACKGROUND:
                return backgroundColor;
            case UNPRINTABLES:
                return unprintablesColor;
            case UNPRINTABLES_BACKGROUND:
                return unprintablesBackgroundColor;
            default:
                throw new IllegalStateException();
        }
    }

    public void setColor(ColorType colorType, Color color) {
        switch (colorType) {
            case TEXT: {
                textColor = color;
                break;
            }
            case BACKGROUND: {
                backgroundColor = color;
                break;
            }
            case UNPRINTABLES: {
                unprintablesColor = color;
                break;
            }
            case UNPRINTABLES_BACKGROUND: {
                unprintablesBackgroundColor = color;
                break;
            }
            default:
                throw new IllegalStateException();
        }
    }

    /**
     * Enumeration of color types in ColorsGroup.
     */
    public static enum ColorType {
        TEXT,
        BACKGROUND,
        UNPRINTABLES,
        UNPRINTABLES_BACKGROUND
    }
}
