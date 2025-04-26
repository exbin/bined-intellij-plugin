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
package org.exbin.bined.intellij.options;

import org.exbin.framework.options.api.OptionsData;
import org.exbin.framework.preferences.api.OptionsStorage;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Locale;

/**
 * BinEd plugin options.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class IntegrationOptions implements OptionsData {

    private final OptionsStorage storage;

    public static final String PREFERENCES_LOCALE_LANGUAGE = "locale.language";
    public static final String PREFERENCES_LOCALE_COUNTRY = "locale.country";
    public static final String PREFERENCES_LOCALE_VARIANT = "locale.variant";
    public static final String PREFERENCES_LOCALE_TAG = "locale.tag";
    public static final String PREFERENCES_ICONSET = "iconset";
    public static final String PREFERENCES_REGISTER_FILE_MENU_OPEN_AS_BINARY = "registerFileMenuOpenAsBinary";
    public static final String PREFERENCES_REGISTER_OPEN_FILE_AS_BINARY_VIA_TOOLBAR = "registerOpenFileAsBinaryViaToolbar";
    public static final String PREFERENCES_REGISTER_CONTEXT_OPEN_AS_BINARY = "registerContextOpenAsBinary";
    public static final String PREFERENCES_REGISTER_CONTEXT_OPEN_IN_BINARY_EDITOR = "registerContextOpenInBinaryEditor";
    public static final String PREFERENCES_REGISTER_NATIVE_BINARY_FILE = "registerNativeBinaryFile";
    public static final String PREFERENCES_REGISTER_DEBUG_VIEW_AS_BINARY = "registerDebugVariablesAsBinary";
    public static final String PREFERENCES_REGISTER_BYTE_TO_BYTE_DIFF_TOOL = "registerByteToByteDiffTool";

    public static final String PREFERENCES_REGISTER_EDIT_AS_BINARY_FOR_DB_COLUMN = "registerEditAsBinaryForDbColumn";

    public IntegrationOptions(OptionsStorage storage) {
        this.storage = storage;
    }

    @Nonnull
    public String getLocaleLanguage() {
        return storage.get(PREFERENCES_LOCALE_LANGUAGE, "");
    }

    @Nonnull
    public String getLocaleCountry() {
        return storage.get(PREFERENCES_LOCALE_COUNTRY, "");
    }

    @Nonnull
    public String getLocaleVariant() {
        return storage.get(PREFERENCES_LOCALE_VARIANT, "");
    }

    @Nonnull
    public String getLocaleTag() {
        return storage.get(PREFERENCES_LOCALE_TAG, "");
    }

    @Nonnull
    public Locale getLanguageLocale() {
        String localeTag = getLocaleTag();
        if (!localeTag.trim().isEmpty()) {
            try {
                return Locale.forLanguageTag(localeTag);
            } catch (SecurityException ex) {
                // Ignore it in java webstart
            }
        }

        String localeLanguage = getLocaleLanguage();
        String localeCountry = getLocaleCountry();
        String localeVariant = getLocaleVariant();
        try {
            return new Locale(localeLanguage, localeCountry, localeVariant);
        } catch (SecurityException ex) {
            // Ignore it in java webstart
        }

        return Locale.ROOT;
    }

    public void setLocaleLanguage(String language) {
        storage.put(PREFERENCES_LOCALE_LANGUAGE, language);
    }

    public void setLocaleCountry(String country) {
        storage.put(PREFERENCES_LOCALE_COUNTRY, country);
    }

    public void setLocaleVariant(String variant) {
        storage.put(PREFERENCES_LOCALE_VARIANT, variant);
    }

    public void setLocaleTag(String variant) {
        storage.put(PREFERENCES_LOCALE_TAG, variant);
    }

    public void setLanguageLocale(Locale locale) {
        setLocaleTag(locale.toLanguageTag());
        setLocaleLanguage(locale.getLanguage());
        setLocaleCountry(locale.getCountry());
        setLocaleVariant(locale.getVariant());
    }

    @Nonnull
    public String getIconSet() {
        return storage.get(PREFERENCES_ICONSET, "");
    }

    public void setIconSet(String iconSet) {
        storage.put(PREFERENCES_ICONSET, iconSet);
    }

    public boolean isRegisterFileMenuOpenAsBinary() {
        return storage.getBoolean(PREFERENCES_REGISTER_FILE_MENU_OPEN_AS_BINARY, false);
    }

    public void setRegisterFileMenuOpenAsBinary(boolean registerFileMenuOpenAsBinary) {
        storage.putBoolean(PREFERENCES_REGISTER_FILE_MENU_OPEN_AS_BINARY, registerFileMenuOpenAsBinary);
    }

    public boolean isRegisterOpenFileAsBinaryViaToolbar() {
        return storage.getBoolean(PREFERENCES_REGISTER_OPEN_FILE_AS_BINARY_VIA_TOOLBAR, true);
    }

    public void setRegisterOpenFileAsBinaryViaToolbar(boolean registerOpenFileAsBinaryViaToolbar) {
        storage.putBoolean(PREFERENCES_REGISTER_OPEN_FILE_AS_BINARY_VIA_TOOLBAR, registerOpenFileAsBinaryViaToolbar);
    }

    public boolean isRegisterContextOpenAsBinary() {
        return storage.getBoolean(PREFERENCES_REGISTER_CONTEXT_OPEN_AS_BINARY, false);
    }

    public void setRegisterContextOpenAsBinary(boolean registerContextOpenAsBinary) {
        storage.putBoolean(PREFERENCES_REGISTER_CONTEXT_OPEN_AS_BINARY, registerContextOpenAsBinary);
    }

    public boolean isRegisterContextOpenInBinaryEditor() {
        return storage.getBoolean(PREFERENCES_REGISTER_CONTEXT_OPEN_IN_BINARY_EDITOR, true);
    }

    public void setRegisterContextOpenInBinaryEditor(boolean registerContextOpenInBinaryEditor) {
        storage.putBoolean(PREFERENCES_REGISTER_CONTEXT_OPEN_IN_BINARY_EDITOR, registerContextOpenInBinaryEditor);
    }

    public boolean isRegisterNativeBinaryFile() {
        return storage.getBoolean(PREFERENCES_REGISTER_NATIVE_BINARY_FILE, true);
    }

    public void setRegisterNativeBinaryFile(boolean registerNativeBinaryFile) {
        storage.putBoolean(PREFERENCES_REGISTER_NATIVE_BINARY_FILE, registerNativeBinaryFile);
    }

    public boolean isRegisterDebugViewAsBinary() {
        return storage.getBoolean(PREFERENCES_REGISTER_DEBUG_VIEW_AS_BINARY, true);
    }

    public void setRegisterDebugViewAsBinary(boolean registerDebugViewAsBinary) {
        storage.putBoolean(PREFERENCES_REGISTER_DEBUG_VIEW_AS_BINARY, registerDebugViewAsBinary);
    }

    public boolean isRegisterByteToByteDiffTool() {
        return storage.getBoolean(PREFERENCES_REGISTER_BYTE_TO_BYTE_DIFF_TOOL, true);
    }

    public void setRegisterByteToByteDiffTool(boolean registerByteToByteDiffTool) {
        storage.putBoolean(PREFERENCES_REGISTER_BYTE_TO_BYTE_DIFF_TOOL, registerByteToByteDiffTool);
    }

    public boolean isRegisterEditAsBinaryForDbColumn() {
        return storage.getBoolean(PREFERENCES_REGISTER_EDIT_AS_BINARY_FOR_DB_COLUMN, true);
    }

    public void setRegisterEditAsBinaryForDbColumn(boolean registerEditAsBinaryForDbColumn) {
        storage.putBoolean(PREFERENCES_REGISTER_EDIT_AS_BINARY_FOR_DB_COLUMN, registerEditAsBinaryForDbColumn);
    }

    @Override
    public void copyTo(OptionsData options) {
        IntegrationOptions with = (IntegrationOptions) options;
        with.setLanguageLocale(getLanguageLocale());
        with.setIconSet(getIconSet());
        with.setRegisterFileMenuOpenAsBinary(isRegisterFileMenuOpenAsBinary());
        with.setRegisterOpenFileAsBinaryViaToolbar(isRegisterOpenFileAsBinaryViaToolbar());
        with.setRegisterContextOpenAsBinary(isRegisterContextOpenAsBinary());
        with.setRegisterContextOpenInBinaryEditor(isRegisterContextOpenInBinaryEditor());
        with.setRegisterNativeBinaryFile(isRegisterNativeBinaryFile());
        with.setRegisterDebugViewAsBinary(isRegisterDebugViewAsBinary());
        with.setRegisterByteToByteDiffTool(isRegisterByteToByteDiffTool());
        with.setRegisterEditAsBinaryForDbColumn(isRegisterEditAsBinaryForDbColumn());
    }
}
