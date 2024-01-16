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
package org.exbin.framework.options.api;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.framework.api.Preferences;

/**
 * Interface for basic options page.
 *
 * @author ExBin Project (https://exbin.org)
 * @param <T> options data
 */
@ParametersAreNonnullByDefault
public interface OptionsPage<T extends OptionsData> {

    @Nonnull
    OptionsComponent<T> createPanel();

    @Nonnull
    T createOptions();

    void loadFromPreferences(Preferences preferences, T options);

    void saveToPreferences(Preferences preferences, T options);

    void applyPreferencesChanges(T options);
}
