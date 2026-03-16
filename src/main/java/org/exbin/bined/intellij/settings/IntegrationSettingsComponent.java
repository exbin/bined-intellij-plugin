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
package org.exbin.bined.intellij.settings;

import org.exbin.bined.intellij.settings.gui.IntegrationSettingsPanel;
import org.exbin.framework.App;
import org.exbin.framework.language.api.IconSetProvider;
import org.exbin.framework.language.api.LanguageModuleApi;
import org.exbin.framework.language.api.LanguageProvider;
import org.exbin.framework.options.settings.api.SettingsComponent;
import org.exbin.framework.options.settings.api.SettingsComponentProvider;
import org.exbin.framework.ui.model.LanguageRecord;
import org.exbin.framework.ui.settings.gui.LanguageSettingsPanel;
import org.exbin.framework.ui.theme.UiThemeModule;
import org.exbin.framework.ui.theme.api.UiThemeModuleApi;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.ImageIcon;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Integration settings component provider.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class IntegrationSettingsComponent implements SettingsComponentProvider {
    @Nonnull
    @Override
    public SettingsComponent createComponent() {
        IntegrationSettingsPanel panel = new IntegrationSettingsPanel();
        ResourceBundle resourceBundle = App.getModule(LanguageModuleApi.class).getBundle(
                LanguageSettingsPanel.class);
        panel.setDefaultLocaleName("<" + resourceBundle.getString("locale.defaultLanguage") + ">");
        List<LanguageRecord> languageLocales = new ArrayList<>();
        languageLocales.add(new LanguageRecord(Locale.ROOT, null));
        languageLocales.add(new LanguageRecord(Locale.forLanguageTag("en-US"), new ImageIcon(getClass().getResource(resourceBundle.getString("locale.englishFlag")))));

        List<LanguageRecord> languageRecords = new ArrayList<>();
        LanguageModuleApi languageModule = App.getModule(LanguageModuleApi.class);
        List<LanguageProvider> languagePlugins = languageModule.getLanguagePlugins();
        for (LanguageProvider languageProvider : languagePlugins) {
            languageRecords.add(new LanguageRecord(languageProvider.getLocale(), languageProvider.getFlag().orElse(null)));
        }
        languageLocales.addAll(languageRecords);

        List<String> iconSets = new ArrayList<>();
        iconSets.add("");
        List<String> iconSetNames = new ArrayList<>();
        UiThemeModule themeModule = (UiThemeModule) App.getModule(UiThemeModuleApi.class);
        ResourceBundle themeResourceBundle = themeModule.getResourceBundle();
        iconSetNames.add(themeResourceBundle.getString("iconset.defaultTheme"));
        List<IconSetProvider> providers = App.getModule(LanguageModuleApi.class).getIconSets();
        for (IconSetProvider provider : providers) {
            iconSets.add(provider.getId());
            iconSetNames.add(provider.getName());
        }

        panel.setLanguageLocales(languageLocales);
        panel.setIconSets(iconSets, iconSetNames);
        return panel;
    }
}
