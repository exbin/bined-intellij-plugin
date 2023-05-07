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
package org.exbin.bined.intellij.options.impl;

import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.bined.intellij.options.IntegrationOptions;
import org.exbin.bined.intellij.preferences.IntegrationPreferences;
import org.exbin.framework.options.api.OptionsData;

/**
 * Binary integration preferences.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class IntegrationOptionsImpl implements OptionsData, IntegrationOptions {

    private boolean registerOpenFileAsBinary = false;
    private boolean registerOpenFileToolbarBinary = true;
    private boolean registerContextOpenAsBinary = false;
    private boolean registerContextOpenInBinaryEditor = true;
    private boolean registerDebugVariablesAsBinary = true;

    @Override
    public boolean isRegisterOpenFileAsBinary() {
        return registerOpenFileAsBinary;
    }

    public void setRegisterOpenFileAsBinary(boolean registerOpenFileAsBinary) {
        this.registerOpenFileAsBinary = registerOpenFileAsBinary;
    }

    @Override
    public boolean isRegisterOpenFileToolbarBinary() {
        return registerOpenFileToolbarBinary;
    }

    public void setRegisterOpenFileToolbarBinary(boolean registerOpenFileToolbarBinary) {
        this.registerOpenFileToolbarBinary = registerOpenFileToolbarBinary;
    }

    @Override
    public boolean isRegisterContextOpenAsBinary() {
        return registerContextOpenAsBinary;
    }

    public void setRegisterContextOpenAsBinary(boolean registerContextOpenAsBinary) {
        this.registerContextOpenAsBinary = registerContextOpenAsBinary;
    }

    @Override
    public boolean isRegisterContextOpenInBinaryEditor() {
        return registerContextOpenInBinaryEditor;
    }

    public void setRegisterContextOpenInBinaryEditor(boolean registerContextOpenInBinaryEditor) {
        this.registerContextOpenInBinaryEditor = registerContextOpenInBinaryEditor;
    }

    @Override
    public boolean isRegisterDebugVariablesAsBinary() {
        return registerDebugVariablesAsBinary;
    }

    public void setRegisterDebugVariablesAsBinary(boolean registerDebugVariablesAsBinary) {
        this.registerDebugVariablesAsBinary = registerDebugVariablesAsBinary;
    }

    public void loadFromPreferences(IntegrationPreferences preferences) {
        registerOpenFileAsBinary = preferences.isRegisterOpenFileAsBinary();
        registerOpenFileToolbarBinary = preferences.isRegisterOpenFileToolbarBinary();
        registerContextOpenAsBinary = preferences.isRegisterContextOpenAsBinary();
        registerContextOpenInBinaryEditor = preferences.isRegisterContextOpenInBinaryEditor();
        registerDebugVariablesAsBinary = preferences.isRegisterDebugVariablesAsBinary();
    }

    public void saveToPreferences(IntegrationPreferences preferences) {
        preferences.setRegisterOpenFileAsBinary(registerOpenFileAsBinary);
        preferences.setRegisterOpenFileToolbarBinary(registerOpenFileToolbarBinary);
        preferences.setRegisterContextOpenAsBinary(registerContextOpenAsBinary);
        preferences.setRegisterContextOpenInBinaryEditor(registerContextOpenInBinaryEditor);
        preferences.setRegisterDebugVariablesAsBinary(registerDebugVariablesAsBinary);
    }

    public void setOptions(IntegrationOptionsImpl options) {
        registerOpenFileAsBinary = options.isRegisterOpenFileAsBinary();
        registerOpenFileToolbarBinary = options.isRegisterOpenFileToolbarBinary();
        registerContextOpenAsBinary = options.isRegisterContextOpenAsBinary();
        registerContextOpenInBinaryEditor = options.isRegisterContextOpenInBinaryEditor();
        registerDebugVariablesAsBinary = options.isRegisterDebugVariablesAsBinary();
    }
}
