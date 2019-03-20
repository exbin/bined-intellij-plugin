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

import org.exbin.framework.Preferences;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.framework.editor.text.EncodingsHandler;

/**
 * Code area preferences.
 *
 * @version 0.2.0 2019/03/01
 * @author ExBin Project (http://exbin.org)
 */
@ParametersAreNonnullByDefault
public class CharsetParameters {

    public static final String PREFERENCES_ENCODING_SELECTED = "selectedEncoding";
    public static final String PREFERENCES_ENCODING_PREFIX = "textEncoding.";

    private final Preferences preferences;

    public CharsetParameters(Preferences preferences) {
        this.preferences = preferences;
    }

    @Nonnull
    public String getSelectedEncoding() {
        return preferences.get(PREFERENCES_ENCODING_SELECTED, EncodingsHandler.ENCODING_UTF8);
    }

    public void setSelectedEncoding(String encodingName) {
        preferences.put(PREFERENCES_ENCODING_SELECTED, encodingName);
    }

    @Nonnull
    public List<String> getEncodings() {
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
}
