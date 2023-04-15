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
package org.exbin.framework.bined.preferences;

import org.exbin.framework.api.Preferences;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.bined.swing.extended.layout.DefaultExtendedCodeAreaLayoutProfile;
import org.exbin.framework.bined.options.CodeAreaLayoutOptions;

/**
 * Code area layout preferences.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class CodeAreaLayoutPreferences implements CodeAreaLayoutOptions {

    public static final String PREFERENCES_LAYOUT_PROFILES_COUNT = "layoutProfilesCount";
    public static final String PREFERENCES_LAYOUT_PROFILE_SELECTED = "layoutProfileSelected";
    public static final String PREFERENCES_LAYOUT_PROFILE_NAME_PREFIX = "layoutProfileName.";
    public static final String PREFERENCES_LAYOUT_VALUE_PREFIX = "layouts.";

    public static final String LAYOUT_SHOW_HEADER = "showHeader";
    public static final String LAYOUT_SHOW_ROW_POSITION = "showRowPosition";

    public static final String LAYOUT_TOP_HEADER_SPACE = "topHeaderSpace";
    public static final String LAYOUT_BOTTOM_HEADER_SPACE = "bottomHeaderSpace";
    public static final String LAYOUT_LEFT_ROW_POSITION_SPACE = "leftRowPositionSpace";
    public static final String LAYOUT_RIGHT_ROW_POSITION_SPACE = "rightRowPositionSpace";

    public static final String LAYOUT_HALF_SPACE_GROUP_SIZE = "halfSpaceGroupSize";
    public static final String LAYOUT_SPACE_GROUP_SIZE = "spaceGroupSize";
    public static final String LAYOUT_DOUBLE_SPACE_GROUP_SIZE = "doubleSpaceGroupSize";

    private final Preferences preferences;

    public CodeAreaLayoutPreferences(Preferences preferences) {
        this.preferences = preferences;
    }

    @Nonnull
    public List<String> getLayoutProfilesList() {
        List<String> profilesList = new ArrayList<>();
        int profilesCount = preferences.getInt(PREFERENCES_LAYOUT_PROFILES_COUNT, 0);

        for (int i = 0; i < profilesCount; i++) {
            String profileName = preferences.get(PREFERENCES_LAYOUT_PROFILE_NAME_PREFIX + String.valueOf(i), "");
            profilesList.add(profileName);
        }

        return profilesList;
    }

    public void setLayoutProfilesList(List<String> layoutNames) {
        int themesCount = layoutNames.size();
        preferences.putInt(PREFERENCES_LAYOUT_PROFILES_COUNT, themesCount);

        for (int i = 0; i < themesCount; i++) {
            preferences.put(PREFERENCES_LAYOUT_PROFILE_NAME_PREFIX + String.valueOf(i), layoutNames.get(i));
        }
    }

    @Override
    public int getSelectedProfile() {
        return preferences.getInt(PREFERENCES_LAYOUT_PROFILE_SELECTED, -1);
    }

    @Override
    public void setSelectedProfile(int profileIndex) {
        preferences.putInt(PREFERENCES_LAYOUT_PROFILE_SELECTED, profileIndex);
    }

    @Nonnull
    @Override
    public DefaultExtendedCodeAreaLayoutProfile getLayoutProfile(int profileIndex) {
        DefaultExtendedCodeAreaLayoutProfile layoutProfile = new DefaultExtendedCodeAreaLayoutProfile();
        String layoutPrefix = PREFERENCES_LAYOUT_VALUE_PREFIX + String.valueOf(profileIndex) + ".";
        layoutProfile.setShowHeader(preferences.getBoolean(layoutPrefix + LAYOUT_SHOW_HEADER, layoutProfile.isShowHeader()));
        layoutProfile.setShowRowPosition(preferences.getBoolean(layoutPrefix + LAYOUT_SHOW_ROW_POSITION, layoutProfile.isShowRowPosition()));

        layoutProfile.setTopHeaderSpace(preferences.getInt(layoutPrefix + LAYOUT_TOP_HEADER_SPACE, layoutProfile.getTopHeaderSpace()));
        layoutProfile.setBottomHeaderSpace(preferences.getInt(layoutPrefix + LAYOUT_BOTTOM_HEADER_SPACE, layoutProfile.getBottomHeaderSpace()));
        layoutProfile.setLeftRowPositionSpace(preferences.getInt(layoutPrefix + LAYOUT_LEFT_ROW_POSITION_SPACE, layoutProfile.getLeftRowPositionSpace()));
        layoutProfile.setRightRowPositionSpace(preferences.getInt(layoutPrefix + LAYOUT_RIGHT_ROW_POSITION_SPACE, layoutProfile.getRightRowPositionSpace()));

        layoutProfile.setHalfSpaceGroupSize(preferences.getInt(layoutPrefix + LAYOUT_HALF_SPACE_GROUP_SIZE, layoutProfile.getHalfSpaceGroupSize()));
        layoutProfile.setSpaceGroupSize(preferences.getInt(layoutPrefix + LAYOUT_SPACE_GROUP_SIZE, layoutProfile.getSpaceGroupSize()));
        layoutProfile.setDoubleSpaceGroupSize(preferences.getInt(layoutPrefix + LAYOUT_DOUBLE_SPACE_GROUP_SIZE, layoutProfile.getDoubleSpaceGroupSize()));

        return layoutProfile;
    }

    @Override
    public void setLayoutProfile(int profileIndex, DefaultExtendedCodeAreaLayoutProfile layoutProfile) {
        String layoutPrefix = PREFERENCES_LAYOUT_VALUE_PREFIX + String.valueOf(profileIndex) + ".";
        preferences.putBoolean(layoutPrefix + LAYOUT_SHOW_HEADER, layoutProfile.isShowHeader());
        preferences.putBoolean(layoutPrefix + LAYOUT_SHOW_ROW_POSITION, layoutProfile.isShowRowPosition());

        preferences.putInt(layoutPrefix + LAYOUT_TOP_HEADER_SPACE, layoutProfile.getTopHeaderSpace());
        preferences.putInt(layoutPrefix + LAYOUT_BOTTOM_HEADER_SPACE, layoutProfile.getBottomHeaderSpace());
        preferences.putInt(layoutPrefix + LAYOUT_LEFT_ROW_POSITION_SPACE, layoutProfile.getLeftRowPositionSpace());
        preferences.putInt(layoutPrefix + LAYOUT_RIGHT_ROW_POSITION_SPACE, layoutProfile.getRightRowPositionSpace());

        preferences.putInt(layoutPrefix + LAYOUT_HALF_SPACE_GROUP_SIZE, layoutProfile.getHalfSpaceGroupSize());
        preferences.putInt(layoutPrefix + LAYOUT_SPACE_GROUP_SIZE, layoutProfile.getSpaceGroupSize());
        preferences.putInt(layoutPrefix + LAYOUT_DOUBLE_SPACE_GROUP_SIZE, layoutProfile.getDoubleSpaceGroupSize());
    }

    @Override
    public void removeLayoutProfile(int profileIndex) {
        String layoutPrefix = PREFERENCES_LAYOUT_VALUE_PREFIX + String.valueOf(profileIndex) + ".";
        preferences.remove(layoutPrefix + LAYOUT_SHOW_HEADER);
        preferences.remove(layoutPrefix + LAYOUT_SHOW_ROW_POSITION);

        preferences.remove(layoutPrefix + LAYOUT_TOP_HEADER_SPACE);
        preferences.remove(layoutPrefix + LAYOUT_BOTTOM_HEADER_SPACE);
        preferences.remove(layoutPrefix + LAYOUT_LEFT_ROW_POSITION_SPACE);
        preferences.remove(layoutPrefix + LAYOUT_RIGHT_ROW_POSITION_SPACE);

        preferences.remove(layoutPrefix + LAYOUT_HALF_SPACE_GROUP_SIZE);
        preferences.remove(layoutPrefix + LAYOUT_SPACE_GROUP_SIZE);
        preferences.remove(layoutPrefix + LAYOUT_DOUBLE_SPACE_GROUP_SIZE);
    }
}
