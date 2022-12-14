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
package org.exbin.framework.bined.options.impl;

import org.exbin.framework.bined.options.CodeAreaThemeOptions;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;
import org.exbin.bined.swing.extended.theme.ExtendedCodeAreaThemeProfile;
import org.exbin.framework.bined.preferences.CodeAreaThemePreferences;
import org.exbin.framework.options.api.OptionsData;

/**
 * Code area theme options.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class CodeAreaThemeOptionsImpl implements OptionsData, CodeAreaThemeOptions {

    private CodeAreaThemePreferences preferences;
    private final List<ProfileRecord> profileRecords = new ArrayList<>();
    private int selectedProfile = -1;

    @Nonnull
    public List<String> getProfileNames() {
        List<String> profilesNames = new ArrayList<>();
        profileRecords.forEach((profile) -> {
            profilesNames.add(profile.getName());
        });
        return profilesNames;
    }

    @Nonnull
    @Override
    public ExtendedCodeAreaThemeProfile getThemeProfile(int index) {
        ProfileRecord record = profileRecords.get(index);
        if (record.profile == null) {
            // Lazy loading
            record = new ProfileRecord(record.name, preferences.getThemeProfile(index));
            profileRecords.set(index, record);
        }

        return record.profile;
    }

    @Override
    public void setThemeProfile(int index, ExtendedCodeAreaThemeProfile themeProfile) {
        ProfileRecord record = profileRecords.get(index);
        record = new ProfileRecord(record.name, themeProfile);
        profileRecords.set(index, record);
    }

    @Override
    public void removeThemeProfile(int index) {
        // Load all lazy records after changed index
        for (int i = index + 1; i < profileRecords.size(); i++) {
            ProfileRecord record = profileRecords.get(i);
            if (record.profile == null) {
                record = new ProfileRecord(record.name, preferences.getThemeProfile(i));
                profileRecords.set(i, record);
            }
        }
        if (selectedProfile == index) {
            selectedProfile = -1;
        } else if (selectedProfile > index) {
            selectedProfile--;
        }
        profileRecords.remove(index);
    }

    public void fullyLoad() {
        for (int i = 0; i < profileRecords.size(); i++) {
            ProfileRecord record = profileRecords.get(i);
            if (record.profile == null) {
                record = new ProfileRecord(record.name, preferences.getThemeProfile(i));
                profileRecords.set(i, record);
            }
        }
    }

    @Override
    public int getSelectedProfile() {
        return selectedProfile;
    }

    @Override
    public void setSelectedProfile(int profileIndex) {
        selectedProfile = profileIndex;
    }

    public void clearProfiles() {
        profileRecords.clear();
    }

    public void addProfile(String profileName, ExtendedCodeAreaThemeProfile themeProfile) {
        profileRecords.add(new ProfileRecord(profileName, themeProfile));
    }

    public void loadFromPreferences(CodeAreaThemePreferences preferences) {
        this.preferences = preferences;
        profileRecords.clear();
        List<String> themeProfilesList = preferences.getThemeProfilesList();
        themeProfilesList.forEach((name) -> {
            profileRecords.add(new ProfileRecord(name, null));
        });
        selectedProfile = preferences.getSelectedProfile();
    }

    public void saveToPreferences(CodeAreaThemePreferences preferences) {
        preferences.setSelectedProfile(selectedProfile);
        preferences.setThemeProfilesList(getProfileNames());
        for (int i = 0; i < profileRecords.size(); i++) {
            ProfileRecord record = profileRecords.get(i);
            ExtendedCodeAreaThemeProfile profile = record.profile;
            if (profile != null) {
                preferences.setThemeProfile(i, record.profile);
            }
        }
    }

    @Immutable
    @ParametersAreNonnullByDefault
    public static class ProfileRecord {

        private final String name;
        private final ExtendedCodeAreaThemeProfile profile;

        public ProfileRecord(String name, ExtendedCodeAreaThemeProfile profile) {
            this.name = name;
            this.profile = profile;
        }

        @Nonnull
        public String getName() {
            return name;
        }

        @Nonnull
        public ExtendedCodeAreaThemeProfile getProfile() {
            return profile;
        }
    }
}
