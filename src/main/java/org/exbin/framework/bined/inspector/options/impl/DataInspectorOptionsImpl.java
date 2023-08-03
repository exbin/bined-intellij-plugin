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
package org.exbin.framework.bined.inspector.options.impl;

import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.framework.bined.inspector.options.DataInspectorOptions;
import org.exbin.framework.bined.inspector.preferences.DataInspectorPreferences;
import org.exbin.framework.options.api.OptionsData;

/**
 * Data inspector options.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class DataInspectorOptionsImpl implements OptionsData, DataInspectorOptions {

    private boolean showParsingPanel;

    @Override
    public boolean isShowParsingPanel() {
        return showParsingPanel;
    }

    @Override
    public void setShowParsingPanel(boolean showParsingPanel) {
        this.showParsingPanel = showParsingPanel;
    }

    public void loadFromPreferences(DataInspectorPreferences preferences) {
        showParsingPanel = preferences.isShowParsingPanel();
    }

    public void saveToPreferences(DataInspectorPreferences preferences) {
        preferences.setShowParsingPanel(showParsingPanel);
    }
}
