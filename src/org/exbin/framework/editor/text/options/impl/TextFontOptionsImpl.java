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
package org.exbin.framework.editor.text.options.impl;

import java.awt.Font;
import java.awt.font.TextAttribute;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.framework.editor.text.options.TextFontOptions;
import org.exbin.framework.editor.text.preferences.TextFontPreferences;
import org.exbin.framework.options.api.OptionsData;

/**
 * Text font options.
 *
 * @version 0.2.1 2019/07/19
 * @author ExBin Project (http://exbin.org)
 */
@ParametersAreNonnullByDefault
public class TextFontOptionsImpl implements TextFontOptions, OptionsData {

    private boolean useDefaultFont = true;
    @Nullable
    private Map<TextAttribute, Object> fontAttributes = null;

    @Override
    public boolean isUseDefaultFont() {
        return useDefaultFont;
    }

    @Override
    public void setUseDefaultFont(boolean useDefaultFont) {
        this.useDefaultFont = useDefaultFont;
    }

    @Nullable
    @Override
    public Map<TextAttribute, Object> getFontAttributes() {
        return fontAttributes;
    }

    @Override
    public void setFontAttributes(@Nullable Map<TextAttribute, Object> fontAttributes) {
        this.fontAttributes = fontAttributes;
    }

    public void loadFromPreferences(TextFontPreferences preferences) {
        useDefaultFont = preferences.isUseDefaultFont();
        fontAttributes = preferences.getFontAttributes();
    }

    public void saveToPreferences(TextFontPreferences preferences) {
        preferences.setUseDefaultFont(useDefaultFont);
        preferences.setFontAttributes(fontAttributes);
    }

    public void setOptions(TextFontOptionsImpl options) {
        useDefaultFont = options.useDefaultFont;
        fontAttributes = new HashMap<>();
        fontAttributes.putAll(options.fontAttributes);
    }

    @Nonnull
    @Override
    public Font getFont(Font initialFont) {
        Map<TextAttribute, Object> attribs = getFontAttributes();
        Font font = initialFont.deriveFont(attribs);
        return font;
    }
}
