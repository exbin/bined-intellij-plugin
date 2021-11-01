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
import org.exbin.framework.api.Preferences;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;
import java.util.Optional;

/**
 * Wrapper for preferences.
 *
 * @version 0.2.4 2021/04/11
 * @author ExBin Project (http://exbin.org)
 */
@ParametersAreNonnullByDefault
public class IntelliJPreferencesWrapper implements Preferences {

    private final PropertiesComponent preferences;
    private final String prefix;

    public IntelliJPreferencesWrapper(PropertiesComponent preferences, String prefix) {
        this.preferences = Objects.requireNonNull(preferences);
        this.prefix = Objects.requireNonNull(prefix);
    }

    @Override
    public boolean exists(String key) {
        return preferences.isValueSet(prefix + key);
    }

    @Nonnull
    @Override
    public Optional<String> get(String key) {
        return exists(key) ? Optional.ofNullable(preferences.getValue(prefix + key)) : Optional.empty();
    }

    @Nonnull
    @Override
    public String get(String key, String def) {
        return preferences.getValue(prefix + key, Objects.requireNonNull(def));
    }

    @Override
    public void put(String key, @Nullable String value) {
        if (value == null) {
            preferences.unsetValue(prefix + key);
        } else {
            preferences.setValue(prefix + key, value);
        }
    }

    @Override
    public void remove(String key) {
        preferences.unsetValue(prefix + key);
    }

    @Override
    public void putInt(String key, int value) {
        preferences.setValue(prefix + key, value, value + 1);
    }

    @Override
    public int getInt(String key, int def) {
        return preferences.getInt(prefix + key, def);
    }

    @Override
    public void putLong(String key, long value) {
        preferences.setValue(prefix + key, (int) value, value + 1);
    }

    @Override
    public long getLong(String key, long defaultValue) {
        try {
            String value = preferences.getValue(prefix + key);
            return value == null ? defaultValue : Long.parseLong(value);
        }
        catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    @Override
    public void putBoolean(String key, boolean value) {
        preferences.setValue(prefix + key, value, !value);
    }

    @Override
    public boolean getBoolean(String key, boolean def) {
        return preferences.getBoolean(prefix + key, def);
    }

    @Override
    public void putFloat(String key, float value) {
        preferences.setValue(prefix + key, value, value + 1);
    }

    @Override
    public float getFloat(String key, float def) {
        return preferences.getFloat(prefix + key, def);
    }

    @Override
    public void putDouble(String key, double value) {
        preferences.setValue(prefix + key, (float) value, (float) (value + 1));
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
