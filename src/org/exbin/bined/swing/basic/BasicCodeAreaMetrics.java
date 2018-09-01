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
package org.exbin.bined.swing.basic;

import java.awt.FontMetrics;
import javax.annotation.Nullable;

/**
 * Basic code area component dimensions.
 *
 * @version 0.2.0 2017/08/29
 * @author ExBin Project (https://exbin.org)
 */
public class BasicCodeAreaMetrics {

    @Nullable
    private FontMetrics fontMetrics;
    private boolean monospaceFont;

    private int rowHeight;
    private int characterWidth;
    private int fontHeight;

    // TODO replace with computation
    private final int subFontSpace = 3;

    public void recomputeMetrics(@Nullable FontMetrics fontMetrics) {
        this.fontMetrics = fontMetrics;
        if (fontMetrics == null) {
            characterWidth = 0;
            fontHeight = 0;
        } else {
            fontHeight = fontMetrics.getHeight();

            /**
             * Use small 'w' character to guess normal font width.
             */
            characterWidth = fontMetrics.charWidth('w');
            /**
             * Compare it to small 'i' to detect if font is monospaced.
             *
             * TODO: Is there better way?
             */
            monospaceFont = characterWidth == fontMetrics.charWidth(' ') && characterWidth == fontMetrics.charWidth('i');
            int fontSize = fontMetrics.getFont().getSize();
            rowHeight = fontSize + subFontSpace;
        }

    }

    public boolean isInitialized() {
        return rowHeight != 0 && characterWidth != 0;
    }

    @Nullable
    public FontMetrics getFontMetrics() {
        return fontMetrics;
    }

    public int getCharWidth(char value) {
        return fontMetrics.charWidth(value);
    }

    public boolean isMonospaceFont() {
        return monospaceFont;
    }

    public int getRowHeight() {
        return rowHeight;
    }

    public int getCharacterWidth() {
        return characterWidth;
    }

    public int getFontHeight() {
        return fontHeight;
    }

    public int getSubFontSpace() {
        return subFontSpace;
    }
}
