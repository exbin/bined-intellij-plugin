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
package org.exbin.bined.intellij.preferences;

import org.exbin.framework.api.Preferences;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.bined.intellij.options.IntegrationOptions;

/**
 * Integration preferences.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class IntegrationPreferences implements IntegrationOptions {

    public static final String PREFERENCES_REGISTER_OPEN_FILE_AS_BINARY = "registerOpenFileAsBinary";
    public static final String PREFERENCES_REGISTER_OPEN_FILE_TOOLBAR_BINARY = "registerOpenFileToolbarBinary";
    public static final String PREFERENCES_REGISTER_CONTEXT_OPEN_AS_BINARY = "registerContextOpenAsBinary";
    public static final String PREFERENCES_REGISTER_CONTEXT_OPEN_IN_BINARY_EDITOR = "registerContextOpenInBinaryEditor";
    public static final String PREFERENCES_REGISTER_DEBUG_VARIABLES_AS_BINARY = "registerDebugVariablesAsBinary";

    private final Preferences preferences;

    public IntegrationPreferences(Preferences preferences) {
        this.preferences = preferences;
    }

    @Override
    public boolean isRegisterOpenFileAsBinary() {
        return preferences.getBoolean(PREFERENCES_REGISTER_OPEN_FILE_AS_BINARY, false);
    }

    public void setRegisterOpenFileAsBinary(boolean registerOpenFileAsBinary) {
        preferences.putBoolean(PREFERENCES_REGISTER_OPEN_FILE_AS_BINARY, registerOpenFileAsBinary);
    }

    @Override
    public boolean isRegisterOpenFileToolbarBinary() {
        return preferences.getBoolean(PREFERENCES_REGISTER_OPEN_FILE_TOOLBAR_BINARY, true);
    }

    public void setRegisterOpenFileToolbarBinary(boolean registerOpenFileToolbarBinary) {
        preferences.putBoolean(PREFERENCES_REGISTER_OPEN_FILE_TOOLBAR_BINARY, registerOpenFileToolbarBinary);
    }

    @Override
    public boolean isRegisterContextOpenAsBinary() {
        return preferences.getBoolean(PREFERENCES_REGISTER_CONTEXT_OPEN_AS_BINARY, false);
    }

    public void setRegisterContextOpenAsBinary(boolean registerContextOpenAsBinary) {
        preferences.putBoolean(PREFERENCES_REGISTER_CONTEXT_OPEN_AS_BINARY, registerContextOpenAsBinary);
    }

    @Override
    public boolean isRegisterContextOpenInBinaryEditor() {
        return preferences.getBoolean(PREFERENCES_REGISTER_CONTEXT_OPEN_IN_BINARY_EDITOR, true);
    }

    public void setRegisterContextOpenInBinaryEditor(boolean registerContextOpenInBinaryEditor) {
        preferences.putBoolean(PREFERENCES_REGISTER_CONTEXT_OPEN_IN_BINARY_EDITOR, registerContextOpenInBinaryEditor);
    }

    @Override
    public boolean isRegisterDebugVariablesAsBinary() {
        return preferences.getBoolean(PREFERENCES_REGISTER_DEBUG_VARIABLES_AS_BINARY, true);
    }

    public void setRegisterDebugVariablesAsBinary(boolean registerDebugVariablesAsBinary) {
        preferences.putBoolean(PREFERENCES_REGISTER_DEBUG_VARIABLES_AS_BINARY, registerDebugVariablesAsBinary);
    }
}
