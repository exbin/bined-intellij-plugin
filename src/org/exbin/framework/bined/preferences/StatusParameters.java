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

import javax.annotation.Nonnull;
import org.exbin.framework.Preferences;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.bined.PositionCodeType;
import org.exbin.framework.bined.options.StatusOptions;

/**
 * Status panel parameters.
 *
 * @version 0.2.0 2019/03/16
 * @author ExBin Project (http://exbin.org)
 */
@ParametersAreNonnullByDefault
public class StatusParameters {

    public static final String PREFERENCES_CURSOR_POSITION_CODE_TYPE = "statusCursorPositionFormat";
    public static final String PREFERENCES_CURSOR_POSITION_SHOW_OFFSET = "statusCursorShowOffset";
    public static final String PREFERENCES_DOCUMENT_SIZE_CODE_TYPE = "statusDocumentSizeFormat";
    public static final String PREFERENCES_DOCUMENT_SIZE_SHOW_RELATIVE = "statusDocumentShowRelative";
    public static final String PREFERENCES_OCTAL_SPACE_GROUP_SIZE = "statusOctalSpaceGroupSize";
    public static final String PREFERENCES_DECIMAL_SPACE_GROUP_SIZE = "statusDecimalSpaceGroupSize";
    public static final String PREFERENCES_HEXADECIMAL_SPACE_GROUP_SIZE = "statusHexadecimalSpaceGroupSize";

    private final Preferences preferences;

    public StatusParameters(Preferences preferences) {
        this.preferences = preferences;
    }

    @Nonnull
    public PositionCodeType getCursorPositionCodeType() {
        try {
            return PositionCodeType.valueOf(preferences.get(PREFERENCES_CURSOR_POSITION_CODE_TYPE, PositionCodeType.DECIMAL.name()));
        } catch (Exception ex) {
            return PositionCodeType.DECIMAL;
        }
    }

    public void setCursorPositionCodeType(PositionCodeType statusCursorPositionCodeType) {
        preferences.put(PREFERENCES_CURSOR_POSITION_CODE_TYPE, statusCursorPositionCodeType.name());
    }

    public boolean isCursorShowOffset() {
        return preferences.getBoolean(PREFERENCES_CURSOR_POSITION_SHOW_OFFSET, true);
    }

    public void setCursorShowOffset(boolean statusCursorShowOffset) {
        preferences.putBoolean(PREFERENCES_CURSOR_POSITION_SHOW_OFFSET, statusCursorShowOffset);
    }

    @Nonnull
    public PositionCodeType getDocumentSizeCodeType() {
        try {
            return PositionCodeType.valueOf(preferences.get(PREFERENCES_DOCUMENT_SIZE_CODE_TYPE, PositionCodeType.DECIMAL.name()));
        } catch (Exception ex) {
            return PositionCodeType.DECIMAL;
        }
    }

    public void setDocumentSizeCodeType(PositionCodeType statusDocumentSizeCodeType) {
        preferences.put(PREFERENCES_DOCUMENT_SIZE_CODE_TYPE, statusDocumentSizeCodeType.name());
    }

    public boolean isDocumentSizeShowRelative() {
        return preferences.getBoolean(PREFERENCES_DOCUMENT_SIZE_SHOW_RELATIVE, true);
    }

    public void setDocumentSizeShowRelative(boolean statusDocumentSizeShowRelative) {
        preferences.putBoolean(PREFERENCES_DOCUMENT_SIZE_SHOW_RELATIVE, statusDocumentSizeShowRelative);
    }

    public int getOctalSpaceGroupSize() {
        return preferences.getInt(PREFERENCES_OCTAL_SPACE_GROUP_SIZE, StatusOptions.DEFAULT_OCTAL_SPACE_GROUP_SIZE);
    }

    public void setOctalSpaceSize(int octalSpaceSize) {
        preferences.putInt(PREFERENCES_OCTAL_SPACE_GROUP_SIZE, octalSpaceSize);
    }

    public int getDecimalSpaceGroupSize() {
        return preferences.getInt(PREFERENCES_DECIMAL_SPACE_GROUP_SIZE, StatusOptions.DEFAULT_DECIMAL_SPACE_GROUP_SIZE);
    }

    public void setDecimalSpaceSize(int decimalSpaceSize) {
        preferences.putInt(PREFERENCES_DECIMAL_SPACE_GROUP_SIZE, decimalSpaceSize);
    }

    public int getHexadecimalSpaceGroupSize() {
        return preferences.getInt(PREFERENCES_HEXADECIMAL_SPACE_GROUP_SIZE, StatusOptions.DEFAULT_HEXADECIMAL_SPACE_GROUP_SIZE);
    }

    public void setHexadecimalSpaceSize(int hexadecimalSpaceSize) {
        preferences.putInt(PREFERENCES_HEXADECIMAL_SPACE_GROUP_SIZE, hexadecimalSpaceSize);
    }
}
