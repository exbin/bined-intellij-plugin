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

import org.exbin.framework.api.Preferences;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.bined.extended.theme.ExtendedBackgroundPaintMode;
import org.exbin.bined.swing.extended.layout.ExtendedCodeAreaDecorations;
import org.exbin.bined.swing.extended.theme.ExtendedCodeAreaThemeProfile;
import org.exbin.framework.bined.options.CodeAreaThemeOptions;

/**
 * Code area theme preferences.
 *
 * @version 0.2.0 2019/03/11
 * @author ExBin Project (http://exbin.org)
 */
@ParametersAreNonnullByDefault
public class CodeAreaThemePreferences implements CodeAreaThemeOptions {

    public static final String PREFERENCES_THEMES_COUNT = "themeProfilesCount";
    public static final String PREFERENCES_THEME_PROFILE_SELECTED = "themeProfilesSelected";
    public static final String PREFERENCES_THEME_NAME_PREFIX = "themeProfileName.";
    public static final String PREFERENCES_THEME_VALUE_PREFIX = "theme.";

    public static final String THEME_BACKGROUND_PAINT_MODE = "backgroundPaintMode";
    public static final String THEME_PAINT_ROWPOS_BACKGROUND = "paintRowPositionBackground";
    public static final String THEME_VERTICAL_LINE_BYTE_GROUP_SIZE = "verticalLineByteGroupSize";

    public static final String THEME_DECORATION_PREFIX = "decoration.";
    public static final String THEME_DECORATION_ROW_POSITION_LINE = THEME_DECORATION_PREFIX + ExtendedCodeAreaDecorations.ROW_POSITION_LINE.getId();
    public static final String THEME_DECORATION_HEADER_LINE = THEME_DECORATION_PREFIX + ExtendedCodeAreaDecorations.HEADER_LINE.getId();
    public static final String THEME_DECORATION_SPLIT_LINE = THEME_DECORATION_PREFIX + ExtendedCodeAreaDecorations.SPLIT_LINE.getId();
    public static final String THEME_DECORATION_BOX_LINES = THEME_DECORATION_PREFIX + ExtendedCodeAreaDecorations.BOX_LINES.getId();
    public static final String THEME_DECORATION_GROUP_LINES = THEME_DECORATION_PREFIX + ExtendedCodeAreaDecorations.GROUP_LINES.getId();

    private final Preferences preferences;

    public CodeAreaThemePreferences(Preferences preferences) {
        this.preferences = preferences;
    }

    @Nonnull
    public List<String> getThemeProfilesList() {
        List<String> profilesList = new ArrayList<>();
        int profilesCount = preferences.getInt(PREFERENCES_THEMES_COUNT, 0);

        for (int i = 0; i < profilesCount; i++) {
            String profileName = preferences.get(PREFERENCES_THEME_NAME_PREFIX + String.valueOf(i), "");
            profilesList.add(profileName);
        }

        return profilesList;
    }

    public void setThemeProfilesList(List<String> themesNames) {
        int themesCount = themesNames.size();
        preferences.putInt(PREFERENCES_THEMES_COUNT, themesCount);

        for (int i = 0; i < themesCount; i++) {
            preferences.put(PREFERENCES_THEME_NAME_PREFIX + String.valueOf(i), themesNames.get(i));
        }
    }

    @Override
    public int getSelectedProfile() {
        return preferences.getInt(PREFERENCES_THEME_PROFILE_SELECTED, -1);
    }

    @Override
    public void setSelectedProfile(int profileIndex) {
        preferences.putInt(PREFERENCES_THEME_PROFILE_SELECTED, profileIndex);
    }

    @Nonnull
    @Override
    public ExtendedCodeAreaThemeProfile getThemeProfile(int profileIndex) {
        ExtendedCodeAreaThemeProfile themeProfile = new ExtendedCodeAreaThemeProfile();
        String themePrefix = PREFERENCES_THEME_VALUE_PREFIX + String.valueOf(profileIndex) + ".";
        themeProfile.setBackgroundPaintMode(ExtendedBackgroundPaintMode.valueOf(preferences.get(themePrefix + THEME_BACKGROUND_PAINT_MODE, themeProfile.getBackgroundPaintMode().name())));
        themeProfile.setPaintRowPosBackground(preferences.getBoolean(themePrefix + THEME_PAINT_ROWPOS_BACKGROUND, themeProfile.isPaintRowPosBackground()));
        themeProfile.setVerticalLineByteGroupSize(preferences.getInt(themePrefix + THEME_VERTICAL_LINE_BYTE_GROUP_SIZE, themeProfile.getVerticalLineByteGroupSize()));

        themeProfile.setDecoration(ExtendedCodeAreaDecorations.ROW_POSITION_LINE, preferences.getBoolean(themePrefix + THEME_DECORATION_ROW_POSITION_LINE, themeProfile.hasDecoration(ExtendedCodeAreaDecorations.ROW_POSITION_LINE)));
        themeProfile.setDecoration(ExtendedCodeAreaDecorations.HEADER_LINE, preferences.getBoolean(themePrefix + THEME_DECORATION_HEADER_LINE, themeProfile.hasDecoration(ExtendedCodeAreaDecorations.HEADER_LINE)));
        themeProfile.setDecoration(ExtendedCodeAreaDecorations.SPLIT_LINE, preferences.getBoolean(themePrefix + THEME_DECORATION_SPLIT_LINE, themeProfile.hasDecoration(ExtendedCodeAreaDecorations.SPLIT_LINE)));
        themeProfile.setDecoration(ExtendedCodeAreaDecorations.BOX_LINES, preferences.getBoolean(themePrefix + THEME_DECORATION_BOX_LINES, themeProfile.hasDecoration(ExtendedCodeAreaDecorations.BOX_LINES)));
        themeProfile.setDecoration(ExtendedCodeAreaDecorations.GROUP_LINES, preferences.getBoolean(themePrefix + THEME_DECORATION_GROUP_LINES, themeProfile.hasDecoration(ExtendedCodeAreaDecorations.GROUP_LINES)));

        return themeProfile;
    }

    @Override
    public void setThemeProfile(int profileIndex, ExtendedCodeAreaThemeProfile themeProfile) {
        String themePrefix = PREFERENCES_THEME_VALUE_PREFIX + String.valueOf(profileIndex) + ".";
        preferences.put(themePrefix + THEME_BACKGROUND_PAINT_MODE, themeProfile.getBackgroundPaintMode().name());
        preferences.putBoolean(themePrefix + THEME_PAINT_ROWPOS_BACKGROUND, themeProfile.isPaintRowPosBackground());
        preferences.putInt(themePrefix + THEME_VERTICAL_LINE_BYTE_GROUP_SIZE, themeProfile.getVerticalLineByteGroupSize());
        preferences.putBoolean(themePrefix + THEME_DECORATION_ROW_POSITION_LINE, themeProfile.hasDecoration(ExtendedCodeAreaDecorations.ROW_POSITION_LINE));
        preferences.putBoolean(themePrefix + THEME_DECORATION_HEADER_LINE, themeProfile.hasDecoration(ExtendedCodeAreaDecorations.HEADER_LINE));
        preferences.putBoolean(themePrefix + THEME_DECORATION_SPLIT_LINE, themeProfile.hasDecoration(ExtendedCodeAreaDecorations.SPLIT_LINE));
        preferences.putBoolean(themePrefix + THEME_DECORATION_BOX_LINES, themeProfile.hasDecoration(ExtendedCodeAreaDecorations.BOX_LINES));
        preferences.putBoolean(themePrefix + THEME_DECORATION_GROUP_LINES, themeProfile.hasDecoration(ExtendedCodeAreaDecorations.GROUP_LINES));
    }

    @Override
    public void removeThemeProfile(int profileIndex) {
        String themePrefix = PREFERENCES_THEME_VALUE_PREFIX + String.valueOf(profileIndex) + ".";
        preferences.remove(themePrefix + THEME_BACKGROUND_PAINT_MODE);
        preferences.remove(themePrefix + THEME_PAINT_ROWPOS_BACKGROUND);
        preferences.remove(themePrefix + THEME_VERTICAL_LINE_BYTE_GROUP_SIZE);

        preferences.remove(themePrefix + THEME_DECORATION_ROW_POSITION_LINE);
        preferences.remove(themePrefix + THEME_DECORATION_HEADER_LINE);
        preferences.remove(themePrefix + THEME_DECORATION_SPLIT_LINE);
        preferences.remove(themePrefix + THEME_DECORATION_BOX_LINES);
        preferences.remove(themePrefix + THEME_DECORATION_GROUP_LINES);
    }
}
