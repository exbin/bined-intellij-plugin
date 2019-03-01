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
package org.exbin.framework.bined.preferences;

import org.exbin.bined.CodeAreaViewMode;
import org.exbin.bined.CodeCharactersCase;
import org.exbin.bined.CodeType;
import org.exbin.bined.PositionCodeType;
import org.exbin.bined.intellij.EncodingsHandler;
import org.exbin.framework.editor.text.panel.TextFontOptionsPanel;

import java.awt.*;
import java.awt.font.TextAttribute;
import java.util.*;
import java.util.List;

/**
 * Code area preferences.
 *
 * @version 0.2.0 2019/03/01
 * @author ExBin Project (http://exbin.org)
 */
public class CodeAreaPreferences {

    public static final String PREFERENCES_CODE_TYPE = "codeType";
    public static final String PREFERENCES_LINE_WRAPPING = "lineWrapping";
    public static final String PREFERENCES_SHOW_UNPRINTABLES = "showNonpritables";
    public static final String PREFERENCES_ENCODING_SELECTED = "selectedEncoding";
    public static final String PREFERENCES_ENCODING_PREFIX = "textEncoding.";
    public static final String PREFERENCES_BYTES_PER_LINE = "bytesPerLine";
    public static final String PREFERENCES_LINE_NUMBERS_LENGTH_TYPE = "lineNumbersLengthType";
    public static final String PREFERENCES_LINE_NUMBERS_LENGTH = "lineNumbersLength";
    public static final String PREFERENCES_VIEW_MODE = "viewMode";
    public static final String PREFERENCES_PAINT_LINE_NUMBERS_BACKGROUND = "showLineNumbersBackground";
    public static final String PREFERENCES_POSITION_CODE_TYPE = "positionCodeType";
    public static final String PREFERENCES_HEX_CHARACTERS_CASE = "hexCharactersCase";
    public static final String PREFERENCES_CODE_COLORIZATION = "codeColorization";

    private final Preferences preferences;

    public CodeAreaPreferences(Preferences preferences) {
        this.preferences = preferences;
    }

    public String getSelectedEncoding() {
        return preferences.get(PREFERENCES_ENCODING_SELECTED, EncodingsHandler.ENCODING_UTF8);
    }

    public void setSelectedEncoding(String encodingName) {
        preferences.put(PREFERENCES_ENCODING_SELECTED, encodingName);
    }

    public Collection<String> getEncodings() {
        List<String> encodings = new ArrayList<>();
        String value;
        int i = 0;
        do {
            value = preferences.get(PREFERENCES_ENCODING_PREFIX + Integer.toString(i), null);
            if (value != null) {
                encodings.add(value);
                i++;
            }
        } while (value != null);

        return encodings;
    }

    public void setEncodings(List<String> encodings) {
        // Save encodings
        for (int i = 0; i < encodings.size(); i++) {
            preferences.put(PREFERENCES_ENCODING_PREFIX + Integer.toString(i), encodings.get(i));
        }
        preferences.remove(PREFERENCES_ENCODING_PREFIX + Integer.toString(encodings.size()));
    }

    public void setCodeType(CodeType codeType) {
        preferences.put(PREFERENCES_CODE_TYPE, codeType.name());
    }

    public Font getCodeFont(Font initialFont) {
        String value;
        Map<TextAttribute, Object> attribs = new HashMap<>();
        value = preferences.get(TextFontOptionsPanel.PREFERENCES_TEXT_FONT_FAMILY, null);
        if (value != null) {
            attribs.put(TextAttribute.FAMILY, value);
        }
        value = preferences.get(TextFontOptionsPanel.PREFERENCES_TEXT_FONT_SIZE, null);
        if (value != null) {
            attribs.put(TextAttribute.SIZE, new Integer(value).floatValue());
        }
        if (Boolean.valueOf(preferences.get(TextFontOptionsPanel.PREFERENCES_TEXT_FONT_UNDERLINE, null))) {
            attribs.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_LOW_ONE_PIXEL);
        }
        if (Boolean.valueOf(preferences.get(TextFontOptionsPanel.PREFERENCES_TEXT_FONT_STRIKETHROUGH, null))) {
            attribs.put(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
        }
        if (Boolean.valueOf(preferences.get(TextFontOptionsPanel.PREFERENCES_TEXT_FONT_STRONG, null))) {
            attribs.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD);
        }
        if (Boolean.valueOf(preferences.get(TextFontOptionsPanel.PREFERENCES_TEXT_FONT_ITALIC, null))) {
            attribs.put(TextAttribute.POSTURE, TextAttribute.POSTURE_OBLIQUE);
        }
        if (Boolean.valueOf(preferences.get(TextFontOptionsPanel.PREFERENCES_TEXT_FONT_SUBSCRIPT, null))) {
            attribs.put(TextAttribute.SUPERSCRIPT, TextAttribute.SUPERSCRIPT_SUB);
        }
        if (Boolean.valueOf(preferences.get(TextFontOptionsPanel.PREFERENCES_TEXT_FONT_SUPERSCRIPT, null))) {
            attribs.put(TextAttribute.SUPERSCRIPT, TextAttribute.SUPERSCRIPT_SUPER);
        }
        Font font = initialFont.deriveFont(attribs);
        return font;
    }

    public void setCodeFont(Font font) {
        Map<TextAttribute, ?> attribs = font.getAttributes();
        String value = (String) attribs.get(TextAttribute.FAMILY);
        if (value != null) {
            preferences.put(TextFontOptionsPanel.PREFERENCES_TEXT_FONT_FAMILY, value);
        } else {
            preferences.remove(TextFontOptionsPanel.PREFERENCES_TEXT_FONT_FAMILY);
        }
        Float fontSize = (Float) attribs.get(TextAttribute.SIZE);
        if (fontSize != null) {
            preferences.put(TextFontOptionsPanel.PREFERENCES_TEXT_FONT_SIZE, Integer.toString((int) (float) fontSize));
        } else {
            preferences.remove(TextFontOptionsPanel.PREFERENCES_TEXT_FONT_SIZE);
        }
        preferences.put(TextFontOptionsPanel.PREFERENCES_TEXT_FONT_UNDERLINE, Boolean.toString(TextAttribute.UNDERLINE_LOW_ONE_PIXEL.equals(attribs.get(TextAttribute.UNDERLINE))));
        preferences.put(TextFontOptionsPanel.PREFERENCES_TEXT_FONT_STRIKETHROUGH, Boolean.toString(TextAttribute.STRIKETHROUGH_ON.equals(attribs.get(TextAttribute.STRIKETHROUGH))));
        preferences.put(TextFontOptionsPanel.PREFERENCES_TEXT_FONT_STRONG, Boolean.toString(TextAttribute.WEIGHT_BOLD.equals(attribs.get(TextAttribute.WEIGHT))));
        preferences.put(TextFontOptionsPanel.PREFERENCES_TEXT_FONT_ITALIC, Boolean.toString(TextAttribute.POSTURE_OBLIQUE.equals(attribs.get(TextAttribute.POSTURE))));
        preferences.put(TextFontOptionsPanel.PREFERENCES_TEXT_FONT_SUBSCRIPT, Boolean.toString(TextAttribute.SUPERSCRIPT_SUB.equals(attribs.get(TextAttribute.SUPERSCRIPT))));
        preferences.put(TextFontOptionsPanel.PREFERENCES_TEXT_FONT_SUPERSCRIPT, Boolean.toString(TextAttribute.SUPERSCRIPT_SUPER.equals(attribs.get(TextAttribute.SUPERSCRIPT))));
    }

    public boolean isRowWrapping() {
        return preferences.getBoolean(PREFERENCES_LINE_WRAPPING, false);
    }

    public void setRowWrapping(boolean rowWrapping) {
        preferences.putBoolean(PREFERENCES_LINE_WRAPPING, rowWrapping);
    }

    public CodeType getCodeType() {
        return CodeType.valueOf(preferences.get(PREFERENCES_CODE_TYPE, CodeType.HEXADECIMAL.name()));
    }

    public boolean isShowNonprintables() {
        return preferences.getBoolean(PREFERENCES_SHOW_UNPRINTABLES, false);
    }

    public void setShowUnprintables(boolean showUnprintables) {
        preferences.putBoolean(PREFERENCES_SHOW_UNPRINTABLES, showUnprintables);
    }

    public CodeCharactersCase getCodeCharactersCase() {
        return CodeCharactersCase.valueOf(preferences.get(PREFERENCES_HEX_CHARACTERS_CASE, CodeCharactersCase.UPPER.name()));
    }

    public void setCodeCharactersCase(CodeCharactersCase codeCharactersCase) {
        preferences.put(PREFERENCES_HEX_CHARACTERS_CASE, codeCharactersCase.name());
    }

    public PositionCodeType getPositionCodeType() {
        return PositionCodeType.valueOf(preferences.get(PREFERENCES_POSITION_CODE_TYPE, PositionCodeType.HEXADECIMAL.name()));
    }

    public CodeAreaViewMode getViewMode() {
        return CodeAreaViewMode.valueOf(preferences.get(PREFERENCES_VIEW_MODE, CodeAreaViewMode.DUAL.name()));
    }

    public void setViewMode(CodeAreaViewMode viewMode) {
        preferences.put(PREFERENCES_VIEW_MODE, viewMode.name());
    }

    public boolean isPaintRowPosBackground() {
        return preferences.getBoolean(PREFERENCES_PAINT_LINE_NUMBERS_BACKGROUND, true);
    }

    public boolean isCodeColorization() {
        return preferences.getBoolean(PREFERENCES_CODE_COLORIZATION, true);
    }

    public void setCodeColorization(boolean codeColorization) {
        preferences.putBoolean(PREFERENCES_CODE_COLORIZATION, codeColorization);
    }

    public Boolean isUseDefaultFont() {
        return Boolean.valueOf(preferences.get(TextFontOptionsPanel.PREFERENCES_TEXT_FONT_DEFAULT, Boolean.toString(true)));
    }

    public void setUseDefaultFont(boolean useDefaultFont) {
        preferences.put(TextFontOptionsPanel.PREFERENCES_TEXT_FONT_DEFAULT, Boolean.toString(useDefaultFont));
    }

    public void setPositionCodeType(PositionCodeType positionCodeType) {
        preferences.put(PREFERENCES_POSITION_CODE_TYPE, positionCodeType.name());
    }
}
