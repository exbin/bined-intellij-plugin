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
package org.exbin.framework.bined.options.impl;

import org.exbin.framework.bined.options.BinaryAppearanceOptions;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.framework.bined.preferences.BinaryAppearancePreferences;
import org.exbin.framework.options.api.OptionsData;

/**
 * Binary component appearance options.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class BinaryAppearanceOptionsImpl implements OptionsData, BinaryAppearanceOptions {

    private boolean lineWrapping;
    private boolean multiFileMode;

    @Override
    public boolean isLineWrapping() {
        return lineWrapping;
    }

    @Override
    public void setLineWrapping(boolean lineWrapping) {
        this.lineWrapping = lineWrapping;
    }

    @Override
    public boolean isMultiFileMode() {
        return multiFileMode;
    }

    @Override
    public void setMultiFileMode(boolean multiFileMode) {
        this.multiFileMode = multiFileMode;
    }

    public void loadFromPreferences(BinaryAppearancePreferences preferences) {
        lineWrapping = preferences.isLineWrapping();
        multiFileMode = preferences.isMultiFileMode();
    }

    public void saveToPreferences(BinaryAppearancePreferences preferences) {
        preferences.setLineWrapping(lineWrapping);
        preferences.setMultiFileMode(multiFileMode);
    }
}
