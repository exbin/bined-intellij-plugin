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
package org.exbin.framework;

import com.intellij.ide.util.PropertiesComponent;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;

/**
 * Wrapper for preferences.
 *
 * @version 0.2.0 2019/03/22
 * @author ExBin Project (http://exbin.org)
 */
@ParametersAreNonnullByDefault
public class PreferencesWrapper implements Preferences {

    private final PropertiesComponent preferences;
    private final String prefix;

    public PreferencesWrapper(PropertiesComponent preferences, String prefix) {
        this.preferences = Objects.requireNonNull(preferences);
        this.prefix = Objects.requireNonNull(prefix);
    }

    @Override
    public void put(String key, @Nullable String value) {
        if (value == null) {
            preferences.unsetValue(prefix + key);
        } else {
            preferences.setValue(prefix + key, value);
        }
    }

    @Nullable
    @Override
    public String get(String key, @Nullable String def) {
        String value = preferences.getValue(prefix + key, def == null ? "" : def);
        return "".equals(value) ? null : value;
    }

    @Override
    public void remove(String key) {
        preferences.unsetValue(prefix + key);
    }

    @Override
    public void putInt(String key, int value) {
        preferences.setValue(prefix + key, value, -1);
    }

    @Override
    public int getInt(String key, int def) {
        return preferences.getInt(prefix + key, def);
    }

    @Override
    public void putLong(String key, long value) {
        preferences.setValue(prefix + key, (int) value, -1);
    }

    @Override
    public long getLong(String key, long def) {
        return preferences.getOrInitLong(prefix + key, def);
    }

    @Override
    public void putBoolean(String key, boolean value) {
        preferences.setValue(prefix + key, value);
    }

    @Override
    public boolean getBoolean(String key, boolean def) {
        return preferences.getBoolean(prefix + key, def);
    }

    @Override
    public void putFloat(String key, float value) {
        preferences.setValue(prefix + key, value, 0f);
    }

    @Override
    public float getFloat(String key, float def) {
        return preferences.getFloat(prefix + key, def);
    }

    @Override
    public void putDouble(String key, double value) {
        preferences.setValue(prefix + key, (float) value, 0f);
    }

    @Override
    public double getDouble(String key, double def) {
        return preferences.getFloat(prefix + key, (float) def);
    }

    @Override
    public void putByteArray(String key, byte[] value) {
        throw new UnsupportedOperationException("Not supported yet.");
        //preferences.setValue(prefix + key, value);
    }

    @Override
    public byte[] getByteArray(String key, byte[] def) {
        throw new UnsupportedOperationException("Not supported yet.");
        // return preferences.getValue(prefix + key, def);
    }

    @Override
    public void flush() {
    }

    @Override
    public void sync() {
    }
}
