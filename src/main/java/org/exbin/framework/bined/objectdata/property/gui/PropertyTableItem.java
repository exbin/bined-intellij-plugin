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
package org.exbin.framework.bined.objectdata.property.gui;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Parameters list table item record.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class PropertyTableItem {

    private String valueName;
    private String typeName;
    private Object value;

    public PropertyTableItem(String valueName, String typeName, @Nullable Object value) {
        this.valueName = valueName;
        this.typeName = typeName;
        this.value = value;
    }

    @Nonnull
    public String getValueName() {
        return valueName;
    }

    public void setValueName(String valueName) {
        this.valueName = valueName;
    }

    @Nonnull
    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    @Nullable
    public Object getValue() {
        return value;
    }

    public void setValue(@Nullable Object value) {
        this.value = value;
    }

    @Nullable
    public Object asBasicType() {
        return convertToBasicType(value);
    }

    @Nullable
    public static Object convertToBasicType(@Nullable Object value) {
        if (value == null)
            return null;

        if (value instanceof String) {
            return value;
        } else if (value instanceof Integer) {
            return Integer.toString((Integer) value);
        } else if (value instanceof Long) {
            return Long.toString((Long) value);
        } else if (value instanceof Float) {
            return Float.toString((Float) value);
        } else if (value instanceof Double) {
            return Double.toString((Double) value);
        } else if (value instanceof Byte) {
            return Byte.toString((Byte) value);
        } else if (value instanceof Character) {
            return Character.toString((Character) value);
        } else if (value instanceof Boolean) {
            return Boolean.toString((Boolean) value);
        } else if (value instanceof Short) {
            return Short.toString((Short) value);
        }

        return value;
    }
}
