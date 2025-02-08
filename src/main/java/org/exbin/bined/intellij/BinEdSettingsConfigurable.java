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
package org.exbin.bined.intellij;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.DumbAware;
import org.exbin.framework.App;
import org.exbin.framework.options.action.OptionsAction;
import org.exbin.framework.options.api.OptionsModuleApi;
import org.exbin.framework.options.api.OptionsPageReceiver;
import org.exbin.framework.options.gui.OptionsListPanel;
import org.exbin.framework.preferences.api.PreferencesModuleApi;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import javax.swing.JComponent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.ResourceBundle;

/**
 * Settings component.
 *
 * @author ExBin Project (https://exbin.org)
 */
public class BinEdSettingsConfigurable implements Configurable, DumbAware {

    private OptionsListPanel optionsListPanel;
    private boolean modified = true;
    private ResourceBundle resourceBundle = BinEdIntelliJPlugin.getResourceBundle();

    public BinEdSettingsConfigurable() {
    }

    @Nonnull
    @Override
    public String getDisplayName() {
        return resourceBundle.getString("BinEdSettingsConfigurable.displayName");
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        PreferencesModuleApi preferencesModule = App.getModule(PreferencesModuleApi.class);
        OptionsAction.OptionsPagesProvider optionsPagesProvider = (OptionsPageReceiver optionsTreePanel) -> {
            OptionsModuleApi optionsModule = App.getModule(OptionsModuleApi.class);
            optionsModule.passOptionsPages(optionsTreePanel);
        };
        optionsListPanel = new OptionsListPanel();
        optionsPagesProvider.registerOptionsPages(optionsListPanel);
        optionsListPanel.setPreferences(preferencesModule.getAppPreferences());
        optionsListPanel.pagesFinished();
        optionsListPanel.loadAllFromPreferences();
        optionsListPanel.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                modified = true;
            }
        });

        return optionsListPanel;
    }

    @Override
    public boolean isModified() {
        return modified;
    }

    @Override
    public void apply() throws ConfigurationException {
        optionsListPanel.saveAndApplyAll();
    }
}
