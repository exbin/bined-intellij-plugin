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
package org.exbin.framework.bined.options;

import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.framework.bined.preferences.BinaryAppearancePreferences;
import org.exbin.framework.gui.options.api.OptionsData;

/**
 * Binary component appearance options.
 *
 * @version 0.2.1 2019/07/20
 * @author ExBin Project (http://exbin.org)
 */
@ParametersAreNonnullByDefault
public class BinaryAppearanceOptions implements OptionsData {

    private boolean lineWrapping;
    private boolean showValuesPanel;
    private boolean multiTabMode;

    public boolean isLineWrapping() {
        return lineWrapping;
    }

    public void setLineWrapping(boolean lineWrapping) {
        this.lineWrapping = lineWrapping;
    }

    public boolean isShowValuesPanel() {
        return showValuesPanel;
    }

    public void setShowValuesPanel(boolean showValuesPanel) {
        this.showValuesPanel = showValuesPanel;
    }

    public boolean isMultiTabMode() {
        return multiTabMode;
    }

    public void setMultiTabMode(boolean multiTabMode) {
        this.multiTabMode = multiTabMode;
    }

    public void loadFromParameters(BinaryAppearancePreferences preferences) {
        lineWrapping = preferences.isLineWrapping();
        showValuesPanel = preferences.isShowValuesPanel();
        multiTabMode = preferences.isMultiTabMode();
    }

    public void saveToParameters(BinaryAppearancePreferences preferences) {
        preferences.setLineWrapping(lineWrapping);
        preferences.setShowValuesPanel(showValuesPanel);
        preferences.setMultiTabMode(multiTabMode);
    }
}
