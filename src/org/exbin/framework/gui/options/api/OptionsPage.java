/*
 * Copyright (C) ExBin Project
 *
 * This application or library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * This application or library is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along this application.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.exbin.framework.gui.options.api;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.framework.api.Preferences;

/**
 * Interface for basic options page.
 *
 * @version 0.2.1 2019/07/20
 * @author ExBin Project (http://exbin.org)
 * @param <T> options data
 */
@ParametersAreNonnullByDefault
public interface OptionsPage<T extends OptionsData> {

    @Nonnull
    OptionsCapable<T> createPanel();

    @Nonnull
    T createOptions();

    void loadFromPreferences(Preferences preferences, T options);

    void saveToPreferences(Preferences preferences, T options);

    void applyPreferencesChanges(T options);
}
