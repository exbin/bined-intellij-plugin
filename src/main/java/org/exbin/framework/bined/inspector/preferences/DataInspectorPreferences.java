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
package org.exbin.framework.bined.inspector.preferences;

import org.exbin.framework.api.Preferences;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.framework.bined.inspector.options.DataInspectorOptions;

/**
 * Data inspector preferences.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class DataInspectorPreferences implements DataInspectorOptions {

    public static final String PREFERENCES_SHOW_PARSING_PANEL = "showValuesPanel";

    private final Preferences preferences;

    public DataInspectorPreferences(Preferences preferences) {
        this.preferences = preferences;
    }

    @Override
    public boolean isShowParsingPanel() {
        return preferences.getBoolean(PREFERENCES_SHOW_PARSING_PANEL, true);
    }

    @Override
    public void setShowParsingPanel(boolean show) {
        preferences.putBoolean(PREFERENCES_SHOW_PARSING_PANEL, show);
    }
}
