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
import org.exbin.framework.frame.api.ApplicationFrameHandler;
import org.exbin.framework.frame.api.FrameModuleApi;
import org.exbin.framework.options.settings.OptionsSettingsModule;
import org.exbin.framework.options.settings.SettingsPage;
import org.exbin.framework.options.settings.SettingsPageReceiver;
import org.exbin.framework.options.settings.action.SettingsAction;
import org.exbin.framework.options.settings.api.OptionsSettingsManagement;
import org.exbin.framework.options.settings.api.OptionsSettingsModuleApi;
import org.exbin.framework.options.settings.api.SettingsOptionsProvider;
import org.exbin.framework.options.settings.gui.SettingsListPanel;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import javax.swing.JComponent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.Collection;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Settings component.
 *
 * @author ExBin Project (https://exbin.org)
 */
public class BinEdSettingsConfigurable implements Configurable, DumbAware {

    private SettingsListPanel settingsListPanel;
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
        SettingsAction.SettingsPagesProvider optionsPagesProvider = (SettingsPageReceiver optionsPageReceiver) -> {
            OptionsSettingsModule optionsSettingsModule = (OptionsSettingsModule) App.getModule(OptionsSettingsModuleApi.class);
            optionsSettingsModule.getMainSettingsManager().passSettingsPages(optionsPageReceiver);
        };
        settingsListPanel = new SettingsListPanel();
        optionsPagesProvider.registerSettingsPages(settingsListPanel);
        settingsListPanel.pagesFinished();
        loadAll(settingsListPanel.getSettingsPages());
        settingsListPanel.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                modified = true;
            }
        });

        return settingsListPanel;
    }

    @Override
    public boolean isModified() {
        return modified;
    }

    @Override
    public void apply() throws ConfigurationException {
        saveAndApplyAll(settingsListPanel.getSettingsPages());
    }

    private void loadAll(Collection<SettingsPage> pages) {
        OptionsSettingsModuleApi optionsSettingsModule = App.getModule(OptionsSettingsModuleApi.class);
        SettingsOptionsProvider settingsOptionsProvider = optionsSettingsModule.getMainSettingsManager().getSettingsOptionsProvider();

        for (SettingsPage page : pages) {
            try {
                page.loadFromOptions(settingsOptionsProvider, null);
            } catch (Exception ex) {
                Logger.getLogger(BinEdSettingsConfigurable.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void saveAndApplyAll(Collection<SettingsPage> pages) {
        OptionsSettingsModuleApi optionsSettingsModule = App.getModule(OptionsSettingsModuleApi.class);
        OptionsSettingsManagement mainSettingsManager = optionsSettingsModule.getMainSettingsManager();
        SettingsOptionsProvider settingsOptionsProvider = mainSettingsManager.getSettingsOptionsProvider();

        for (SettingsPage page : pages) {
            try {
                page.saveAndApply(settingsOptionsProvider, null);
            } catch (Exception ex) {
                Logger.getLogger(BinEdSettingsConfigurable.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        // TODO Run in top context
        FrameModuleApi frameModule = App.getModule(FrameModuleApi.class);
        ApplicationFrameHandler frameHandler = frameModule.getFrameHandler();
        mainSettingsManager.applyAllOptions(frameHandler.getContextManager(), mainSettingsManager.getSettingsOptionsProvider());
    }
}
