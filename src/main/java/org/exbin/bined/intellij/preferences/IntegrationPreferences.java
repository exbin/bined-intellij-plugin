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

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.bined.intellij.options.IntegrationOptions;

import java.util.Locale;

/**
 * Integration preferences.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class IntegrationPreferences implements IntegrationOptions {

    public static final String PREFERENCES_LOCALE_LANGUAGE = "locale.language";
    public static final String PREFERENCES_LOCALE_COUNTRY = "locale.country";
    public static final String PREFERENCES_LOCALE_VARIANT = "locale.variant";
    public static final String PREFERENCES_LOCALE_TAG = "locale.tag";
    public static final String PREFERENCES_REGISTER_FILE_MENU_OPEN_AS_BINARY = "registerFileMenuOpenAsBinary";
    public static final String PREFERENCES_REGISTER_OPEN_FILE_AS_BINARY_VIA_TOOLBAR = "registerOpenFileAsBinaryViaToolbar";
    public static final String PREFERENCES_REGISTER_CONTEXT_OPEN_AS_BINARY = "registerContextOpenAsBinary";
    public static final String PREFERENCES_REGISTER_CONTEXT_OPEN_IN_BINARY_EDITOR = "registerContextOpenInBinaryEditor";
    public static final String PREFERENCES_REGISTER_DEBUG_VIEW_AS_BINARY = "registerDebugVariablesAsBinary";
    public static final String PREFERENCES_REGISTER_BYTE_TO_BYTE_DIFF_TOOL = "registerByteToByteDiffTool";

    public static final String PREFERENCES_REGISTER_EDIT_AS_BINARY_FOR_DB_COLUMN = "registerEditAsBinaryForDbColumn";

    private final Preferences preferences;

    public IntegrationPreferences(Preferences preferences) {
        this.preferences = preferences;
    }

    @Nonnull
    public String getLocaleLanguage() {
        return preferences.get(PREFERENCES_LOCALE_LANGUAGE, "");
    }

    @Nonnull
    public String getLocaleCountry() {
        return preferences.get(PREFERENCES_LOCALE_COUNTRY, "");
    }

    @Nonnull
    public String getLocaleVariant() {
        return preferences.get(PREFERENCES_LOCALE_VARIANT, "");
    }

    @Nonnull
    public String getLocaleTag() {
        return preferences.get(PREFERENCES_LOCALE_TAG, "");
    }

    @Nonnull
    @Override
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
        preferences.put(PREFERENCES_LOCALE_LANGUAGE, language);
    }

    public void setLocaleCountry(String country) {
        preferences.put(PREFERENCES_LOCALE_COUNTRY, country);
    }

    public void setLocaleVariant(String variant) {
        preferences.put(PREFERENCES_LOCALE_VARIANT, variant);
    }

    public void setLocaleTag(String variant) {
        preferences.put(PREFERENCES_LOCALE_TAG, variant);
    }

    @Override
    public void setLanguageLocale(Locale locale) {
        setLocaleTag(locale.toLanguageTag());
        setLocaleLanguage(locale.getLanguage());
        setLocaleCountry(locale.getCountry());
        setLocaleVariant(locale.getVariant());
    }

    @Override
    public boolean isRegisterFileMenuOpenAsBinary() {
        return preferences.getBoolean(PREFERENCES_REGISTER_FILE_MENU_OPEN_AS_BINARY, false);
    }

    public void setRegisterFileMenuOpenAsBinary(boolean registerFileMenuOpenAsBinary) {
        preferences.putBoolean(PREFERENCES_REGISTER_FILE_MENU_OPEN_AS_BINARY, registerFileMenuOpenAsBinary);
    }

    @Override
    public boolean isRegisterOpenFileAsBinaryViaToolbar() {
        return preferences.getBoolean(PREFERENCES_REGISTER_OPEN_FILE_AS_BINARY_VIA_TOOLBAR, true);
    }

    public void setRegisterOpenFileAsBinaryViaToolbar(boolean registerOpenFileAsBinaryViaToolbar) {
        preferences.putBoolean(PREFERENCES_REGISTER_OPEN_FILE_AS_BINARY_VIA_TOOLBAR, registerOpenFileAsBinaryViaToolbar);
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
    public boolean isRegisterDebugViewAsBinary() {
        return preferences.getBoolean(PREFERENCES_REGISTER_DEBUG_VIEW_AS_BINARY, true);
    }

    public void setRegisterDebugViewAsBinary(boolean registerDebugViewAsBinary) {
        preferences.putBoolean(PREFERENCES_REGISTER_DEBUG_VIEW_AS_BINARY, registerDebugViewAsBinary);
    }

    @Override
    public boolean isRegisterByteToByteDiffTool() {
        return preferences.getBoolean(PREFERENCES_REGISTER_BYTE_TO_BYTE_DIFF_TOOL, true);
    }

    public void setRegisterByteToByteDiffTool(boolean registerByteToByteDiffTool) {
        preferences.putBoolean(PREFERENCES_REGISTER_BYTE_TO_BYTE_DIFF_TOOL, registerByteToByteDiffTool);
    }

    @Override
    public boolean isRegisterEditAsBinaryForDbColumn() {
        return preferences.getBoolean(PREFERENCES_REGISTER_EDIT_AS_BINARY_FOR_DB_COLUMN, true);
    }

    public void setRegisterEditAsBinaryForDbColumn(boolean registerEditAsBinaryForDbColumn) {
        preferences.putBoolean(PREFERENCES_REGISTER_EDIT_AS_BINARY_FOR_DB_COLUMN, registerEditAsBinaryForDbColumn);
    }
}
