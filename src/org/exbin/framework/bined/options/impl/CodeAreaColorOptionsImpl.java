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
package org.exbin.framework.bined.options.impl;

import org.exbin.framework.bined.options.CodeAreaColorOptions;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;
import org.exbin.bined.swing.extended.color.ExtendedCodeAreaColorProfile;
import org.exbin.framework.bined.preferences.CodeAreaColorPreferences;
import org.exbin.framework.options.api.OptionsData;

/**
 * Code area theme options.
 *
 * @version 0.2.1 2019/07/20
 * @author ExBin Project (http://exbin.org)
 */
@ParametersAreNonnullByDefault
public class CodeAreaColorOptionsImpl implements OptionsData, CodeAreaColorOptions {

    private CodeAreaColorPreferences preferences;
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
    public ExtendedCodeAreaColorProfile getColorsProfile(int index) {
        ProfileRecord record = profileRecords.get(index);
        if (record.profile == null) {
            // Lazy loading
            record = new ProfileRecord(record.name, preferences.getColorsProfile(index));
            profileRecords.set(index, record);
        }

        return record.profile;
    }

    @Override
    public void setColorsProfile(int index, ExtendedCodeAreaColorProfile colorProfile) {
        ProfileRecord record = profileRecords.get(index);
        record = new ProfileRecord(record.name, colorProfile);
        profileRecords.set(index, record);
    }

    @Override
    public void removeColorsProfile(int index) {
        // Load all lazy records after changed index
        for (int i = index + 1; i < profileRecords.size(); i++) {
            ProfileRecord record = profileRecords.get(i);
            if (record.profile == null) {
                record = new ProfileRecord(record.name, preferences.getColorsProfile(i));
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
                record = new ProfileRecord(record.name, preferences.getColorsProfile(i));
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

    public void addProfile(String profileName, ExtendedCodeAreaColorProfile colorProfile) {
        profileRecords.add(new ProfileRecord(profileName, colorProfile));
    }

    public void loadFromPreferences(CodeAreaColorPreferences preferences) {
        this.preferences = preferences;
        profileRecords.clear();
        List<String> colorProfilesList = preferences.getColorProfilesList();
        colorProfilesList.forEach((name) -> {
            profileRecords.add(new ProfileRecord(name, null));
        });
        selectedProfile = preferences.getSelectedProfile();
    }

    public void saveToPreferences(CodeAreaColorPreferences preferences) {
        preferences.setSelectedProfile(selectedProfile);
        preferences.setColorProfilesList(getProfileNames());
        for (int i = 0; i < profileRecords.size(); i++) {
            ProfileRecord record = profileRecords.get(i);
            ExtendedCodeAreaColorProfile profile = record.profile;
            if (profile != null) {
                preferences.setColorsProfile(i, record.profile);
            }
        }
    }

    @Immutable
    @ParametersAreNonnullByDefault
    public static class ProfileRecord {

        private final String name;
        private final ExtendedCodeAreaColorProfile profile;

        public ProfileRecord(String name, ExtendedCodeAreaColorProfile profile) {
            this.name = name;
            this.profile = profile;
        }

        @Nonnull
        public String getName() {
            return name;
        }

        @Nonnull
        public ExtendedCodeAreaColorProfile getProfile() {
            return profile;
        }
    }
}
