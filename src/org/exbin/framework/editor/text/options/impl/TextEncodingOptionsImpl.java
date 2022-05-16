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

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.framework.editor.text.options.TextEncodingOptions;
import org.exbin.framework.editor.text.preferences.TextEncodingPreferences;
import org.exbin.framework.options.api.OptionsData;
import org.exbin.xbup.core.util.StringUtils;

/**
 * Text encoding options.
 *
 * @version 0.2.1 2020/08/17
 * @author ExBin Project (http://exbin.org)
 */
@ParametersAreNonnullByDefault
public class TextEncodingOptionsImpl implements TextEncodingOptions, OptionsData {

    private String selectedEncoding = StringUtils.ENCODING_UTF8;
    private List<String> encodings = new ArrayList<>();

    @Nonnull
    @Override
    public String getSelectedEncoding() {
        return selectedEncoding;
    }

    @Nonnull
    @Override
    public void setSelectedEncoding(String selectedEncoding) {
        this.selectedEncoding = selectedEncoding;
    }

    @Nonnull
    @Override
    public List<String> getEncodings() {
        return encodings;
    }

    @Override
    public void setEncodings(List<String> encodings) {
        this.encodings = encodings;
    }

    public void loadFromPreferences(TextEncodingPreferences preferences) {
        selectedEncoding = preferences.getSelectedEncoding();
        encodings = preferences.getEncodings();
    }

    public void saveToPreferences(TextEncodingPreferences preferences) {
        preferences.setSelectedEncoding(selectedEncoding);
        preferences.setEncodings(encodings);
    }

    public void setOptions(TextEncodingOptionsImpl options) {
        selectedEncoding = options.selectedEncoding;
        encodings = new ArrayList<>();
        encodings.addAll(options.encodings);
    }
}
