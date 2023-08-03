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
package org.exbin.framework.bined.preferences;

import org.exbin.framework.api.Preferences;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Binary appearance preferences.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class BinaryAppearancePreferences {

    public static final String PREFERENCES_TEXT_WORD_WRAPPING = "textAppearance.wordWrap";
    public static final String PREFERENCES_MULTIFILE_MODE = "multiFileMode";

    private final Preferences preferences;

    public BinaryAppearancePreferences(Preferences preferences) {
        this.preferences = preferences;
    }

    public boolean isLineWrapping() {
        return preferences.getBoolean(PREFERENCES_TEXT_WORD_WRAPPING, false);
    }

    public boolean isMultiFileMode() {
        return preferences.getBoolean(PREFERENCES_MULTIFILE_MODE, true);
    }

    public void setLineWrapping(boolean wrapping) {
        preferences.putBoolean(PREFERENCES_TEXT_WORD_WRAPPING, wrapping);
    }

    public void setMultiFileMode(boolean mode) {
        preferences.putBoolean(PREFERENCES_MULTIFILE_MODE, mode);
    }
}
