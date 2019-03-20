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
package org.exbin.bined.intellij;

import com.intellij.ide.util.PropertiesComponent;
import org.exbin.framework.Preferences;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Wrapper for preferences.
 *
 * @version 0.2.0 2019/03/20
 * @author ExBin Project (http://exbin.org)
 */
@ParametersAreNonnullByDefault
public class PreferencesWrapper implements Preferences {

    private final PropertiesComponent preferences;
    private static final String PLUGIN_PREFIX = "BinEdPlugin.";

    public PreferencesWrapper(PropertiesComponent preferences) {
        this.preferences = preferences;
    }

    @Override
    public void put(String key, @Nullable String value) {
        if (value == null) {
            preferences.unsetValue(PLUGIN_PREFIX + key);
        } else {
            preferences.setValue(PLUGIN_PREFIX + key, value);
        }
    }

    @Override
    public String get(String key, @Nullable String def) {
        String value = preferences.getValue(PLUGIN_PREFIX + key, def == null ? "" : def);
        return "".equals(value) ? null : value;
    }

    @Override
    public void remove(String key) {
        preferences.unsetValue(PLUGIN_PREFIX + key);
    }

    @Override
    public void putInt(String key, int value) {
        preferences.setValue(PLUGIN_PREFIX + key, value, -1);
    }

    @Override
    public int getInt(String key, int def) {
        return preferences.getInt(PLUGIN_PREFIX + key, def);
    }

    @Override
    public void putLong(String key, long value) {
        preferences.setValue(PLUGIN_PREFIX + key, (int) value, -1);
    }

    @Override
    public long getLong(String key, long def) {
        return preferences.getOrInitLong(PLUGIN_PREFIX + key, def);
    }

    @Override
    public void putBoolean(String key, boolean value) {
        preferences.setValue(PLUGIN_PREFIX + key, value);
    }

    @Override
    public boolean getBoolean(String key, boolean def) {
        return preferences.getBoolean(PLUGIN_PREFIX + key, def);
    }

    @Override
    public void putFloat(String key, float value) {
        preferences.setValue(PLUGIN_PREFIX + key, value, 0f);
    }

    @Override
    public float getFloat(String key, float def) {
        return preferences.getFloat(PLUGIN_PREFIX + key, def);
    }

    @Override
    public void putDouble(String key, double value) {
        preferences.setValue(PLUGIN_PREFIX + key, (float) value, 0f);
    }

    @Override
    public double getDouble(String key, double def) {
        return preferences.getFloat(PLUGIN_PREFIX + key, (float) def);
    }

    @Override
    public void putByteArray(String key, byte[] value) {
        throw new UnsupportedOperationException("Not supported yet.");
        //preferences.setValue(PLUGIN_PREFIX + key, value);
    }

    @Override
    public byte[] getByteArray(String key, byte[] def) {
        throw new UnsupportedOperationException("Not supported yet.");
        // return preferences.getValue(PLUGIN_PREFIX + key, def);
    }

    @Override
    public void flush() {
    }

    @Override
    public void sync() {
    }
}
