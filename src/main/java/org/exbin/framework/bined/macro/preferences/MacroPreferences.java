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
package org.exbin.framework.bined.macro.preferences;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.framework.api.Preferences;
import org.exbin.framework.bined.macro.model.MacroRecord;
import org.exbin.framework.bined.macro.options.MacroOptions;

/**
 * Macro preferences.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class MacroPreferences implements MacroOptions {

    public static final String PREFERENCES_MACROS_COUNT = "macrosCount";
    public static final String PREFERENCES_MACRO_VALUE_PREFIX = "macro.";

    public static final String MACRO_NAME = "name";
    public static final String STEP = "step";

    private final Preferences preferences;

    public MacroPreferences(Preferences preferences) {
        this.preferences = preferences;
    }

    @Override
    public int getMacrosCount() {
        return preferences.getInt(PREFERENCES_MACROS_COUNT, 0);
    }

    @Override
    public MacroRecord getMacroRecord(int index) {
        String prefix = PREFERENCES_MACRO_VALUE_PREFIX + index + ".";
        String name = preferences.get(prefix + MACRO_NAME, "");
        MacroRecord macroRecord = new MacroRecord(name);

        List<String> steps = new ArrayList<>();
        int stepIndex = 1;
        while (true) {
            String line = preferences.get(prefix + STEP + "." + stepIndex, "");
            if (!line.trim().isEmpty()) {
                steps.add(line);
                stepIndex++;
            } else {
                break;
            }
        }
        macroRecord.setSteps(steps);

        return macroRecord;
    }

    @Override
    public void setMacrosCount(int count) {
        preferences.putInt(PREFERENCES_MACROS_COUNT, count);
    }

    @Override
    public void setMacroRecord(int index, MacroRecord record) {
        String prefix = PREFERENCES_MACRO_VALUE_PREFIX + index + ".";
        preferences.put(prefix + MACRO_NAME, record.getName());

        List<String> steps = record.getSteps();
        int stepIndex = 1;
        for (String step : steps) {
            preferences.put(prefix + STEP + "." + stepIndex, step);
            stepIndex++;
        }

        String oldLine;
        do {
            oldLine = preferences.get(prefix + STEP + "." + stepIndex, "");
            preferences.remove(prefix + STEP + "." + stepIndex);
        } while (!oldLine.trim().isEmpty());
    }
}
