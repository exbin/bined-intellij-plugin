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
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Hexadecimal editor preferences.
 *
 * @version 0.2.0 2019/03/16
 * @author ExBin Project (http://exbin.org)
 */
@ParametersAreNonnullByDefault
public class EditorParameters {

    public static final String PREFERENCES_FILE_HANDLING_MODE = "fileHandlingMode";
    public static final String PREFERENCES_SHOW_VALUES_PANEL = "valuesPanel";

    private final Preferences preferences;

    public EditorParameters(Preferences preferences) {
        this.preferences = preferences;
    }

    @Nonnull
    public String getFileHandlingMode() {
        return preferences.get(PREFERENCES_FILE_HANDLING_MODE, "DELTA");
    }

    public void setFileHandlingMode(String fileHandlingMode) {
        preferences.put(PREFERENCES_FILE_HANDLING_MODE, fileHandlingMode);
    }

    public boolean isShowValuesPanel() {
        return preferences.getBoolean(PREFERENCES_SHOW_VALUES_PANEL, true);
    }

    public void setShowValuesPanel(boolean showValuesPanel) {
        preferences.putBoolean(PREFERENCES_SHOW_VALUES_PANEL, showValuesPanel);
    }
}
