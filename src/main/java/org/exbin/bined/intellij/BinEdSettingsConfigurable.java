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
import org.exbin.bined.intellij.gui.BinEdOptionsPanel;
import org.exbin.bined.intellij.gui.BinEdOptionsPanelBorder;
import org.exbin.bined.intellij.main.BinEdManager;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import javax.swing.JComponent;

/**
 * Settings component.
 *
 * @author ExBin Project (https://exbin.org)
 */
public class BinEdSettingsConfigurable implements Configurable, DumbAware {

    private BinEdOptionsPanelBorder optionsPanelWrapper;
    private boolean modified = true;

    public BinEdSettingsConfigurable() {
    }

    @Nonnull
    @Override
    public String getDisplayName() {
        return "BinEd Plugin";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        optionsPanelWrapper = new BinEdOptionsPanelBorder();
        BinEdOptionsPanel optionsPanel = optionsPanelWrapper.getOptionsPanel();
        BinEdManager binEdManager = BinEdManager.getInstance();
        optionsPanel.setPreferences(binEdManager.getPreferences());
        optionsPanel.loadFromPreferences();
//        optionsPanelWrapper.addFocusListener(new FocusAdapter() {
//            @Override
//            public void focusGained(FocusEvent e) {
//                modified = true;
//                optionsPanelWrapper.firePropertyChange("modified", false, true);
//            }
//        });
        return optionsPanelWrapper;
    }

    @Override
    public boolean isModified() {
        return modified;
    }

    @Override
    public void apply() throws ConfigurationException {
        BinEdOptionsPanel optionsPanel = optionsPanelWrapper.getOptionsPanel();
        optionsPanel.saveToPreferences();
    }
}
