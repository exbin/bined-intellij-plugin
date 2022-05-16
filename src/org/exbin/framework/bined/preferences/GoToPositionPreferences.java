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

import java.util.logging.Level;
import java.util.logging.Logger;
import org.exbin.framework.api.Preferences;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.bined.PositionCodeType;
import org.exbin.framework.bined.gui.RelativePositionMode;

/**
 * Binary editor preferences.
 *
 * @version 0.2.1 2019/07/30
 * @author ExBin Project (http://exbin.org)
 */
@ParametersAreNonnullByDefault
public class GoToPositionPreferences {

    public static final String PREFERENCES_GO_TO_BINARY_POSITION_MODE = "goToBinaryPositionMode";
    public static final String PREFERENCES_GO_TO_BINARY_POSITION_VALUE_TYPE = "goToBinaryPositionValueType";

    private final Preferences preferences;

    public GoToPositionPreferences(Preferences preferences) {
        this.preferences = preferences;
    }

    @Nonnull
    public RelativePositionMode getPositionMode() {
        RelativePositionMode defaultMode = RelativePositionMode.FROM_START;
        try {
            return RelativePositionMode.valueOf(preferences.get(PREFERENCES_GO_TO_BINARY_POSITION_MODE, defaultMode.name()));
        } catch (Exception ex) {
            Logger.getLogger(GoToPositionPreferences.class.getName()).log(Level.SEVERE, null, ex);
            return defaultMode;
        }
    }

    public void setPositionMode(RelativePositionMode positionMode) {
        preferences.put(PREFERENCES_GO_TO_BINARY_POSITION_MODE, positionMode.name());
    }

    @Nonnull
    public PositionCodeType getGoToBinaryPositionValueType() {
        PositionCodeType defaultCodeType = PositionCodeType.DECIMAL;
        try {
            return PositionCodeType.valueOf(preferences.get(PREFERENCES_GO_TO_BINARY_POSITION_VALUE_TYPE, defaultCodeType.name()));
        } catch (Exception ex) {
            Logger.getLogger(GoToPositionPreferences.class.getName()).log(Level.SEVERE, null, ex);
            return defaultCodeType;
        }
    }

    public void setGoToBinaryPositionValueType(PositionCodeType goToBinaryPositionValueType) {
        preferences.put(PREFERENCES_GO_TO_BINARY_POSITION_VALUE_TYPE, goToBinaryPositionValueType.name());
    }
}
