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
package org.exbin.bined.intellij.settings;

import com.intellij.ide.util.PropertiesComponent;
import org.exbin.framework.options.api.OptionsStorage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;
import java.util.Optional;

/**
 * Wrapper for preferences.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class IntelliJOptionsStorage implements OptionsStorage {

    private final PropertiesComponent properties;
    private final String prefix;

    public IntelliJOptionsStorage(PropertiesComponent properties, String prefix) {
        this.properties = Objects.requireNonNull(properties);
        this.prefix = Objects.requireNonNull(prefix);
    }

    @Override
    public boolean exists(String key) {
        return properties.isValueSet(prefix + key);
    }

    @Nonnull
    @Override
    public Optional<String> get(String key) {
        return exists(key) ? Optional.ofNullable(properties.getValue(prefix + key)) : Optional.empty();
    }

    @Nonnull
    @Override
    public String get(String key, String def) {
        return properties.getValue(prefix + key, Objects.requireNonNull(def));
    }

    @Override
    public void put(String key, @Nullable String value) {
        if (value == null) {
            properties.unsetValue(prefix + key);
        } else {
            properties.setValue(prefix + key, value);
        }
    }

    @Override
    public void remove(String key) {
        properties.unsetValue(prefix + key);
    }

    @Override
    public void putInt(String key, int value) {
        properties.setValue(prefix + key, value, value + 1);
    }

    @Override
    public int getInt(String key, int def) {
        return properties.getInt(prefix + key, def);
    }

    @Override
    public void putLong(String key, long value) {
        properties.setValue(prefix + key, String.valueOf(value));
    }

    @Override
    public long getLong(String key, long defaultValue) {
        try {
            String value = properties.getValue(prefix + key);
            return value == null ? defaultValue : Long.parseLong(value);
        }
        catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    @Override
    public void putBoolean(String key, boolean value) {
        properties.setValue(prefix + key, value, !value);
    }

    @Override
    public boolean getBoolean(String key, boolean def) {
        return properties.getBoolean(prefix + key, def);
    }

    @Override
    public void putFloat(String key, float value) {
        properties.setValue(prefix + key, value, value + 1);
    }

    @Override
    public float getFloat(String key, float def) {
        return properties.getFloat(prefix + key, def);
    }

    @Override
    public void putDouble(String key, double value) {
        properties.setValue(prefix + key, (float) value, (float) (value + 1));
    }

    @Override
    public double getDouble(String key, double def) {
        return properties.getFloat(prefix + key, (float) def);
    }

    @Override
    public void putByteArray(String key, byte[] value) {
        // preferences.setValue(prefix + key, value);
        throw new UnsupportedOperationException();
    }

    @Override
    public byte[] getByteArray(String key, byte[] def) {
        // return preferences.getValue(prefix + key, def);
        throw new UnsupportedOperationException();
    }

    @Override
    public void flush() {
        // empty
    }

    @Override
    public void sync() {
        // empty
    }
}
