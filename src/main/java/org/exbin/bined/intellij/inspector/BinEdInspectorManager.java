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
package org.exbin.bined.intellij.inspector;

import org.exbin.bined.intellij.gui.BinEdComponentPanel;
import org.exbin.bined.intellij.inspector.action.ShowParsingPanelAction;
import org.exbin.bined.intellij.main.BinEdFileManager;
import org.exbin.bined.intellij.main.BinEdManager;
import org.exbin.bined.swing.extended.ExtCodeArea;
import org.exbin.framework.api.Preferences;
import org.exbin.framework.bined.inspector.BasicValuesPositionColorModifier;
import org.exbin.framework.bined.inspector.options.impl.DataInspectorOptionsImpl;
import org.exbin.framework.options.api.DefaultOptionsPage;
import org.exbin.framework.utils.LanguageUtils;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.JPopupMenu;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Binary editor data inspector manager.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class BinEdInspectorManager {

    private java.util.ResourceBundle resourceBundle = null;

    private BasicValuesPositionColorModifier basicValuesColorModifier = new BasicValuesPositionColorModifier();

    private DefaultOptionsPage<DataInspectorOptionsImpl> dataInspectorOptionsPage;

    public BinEdInspectorManager() {
    }

    public void init() {
        BinEdManager binEdManager = BinEdManager.getInstance();
        Preferences preferences = binEdManager.getPreferences().getPreferences();
        BinEdFileManager fileManager = binEdManager.getFileManager();

        fileManager.addPainterColorModifier(basicValuesColorModifier);
        fileManager.addBinEdComponentExtension(new BinEdFileManager.BinEdFileExtension() {
            @Nonnull
            @Override
            public Optional<BinEdComponentPanel.BinEdComponentExtension> createComponentExtension(BinEdComponentPanel component) {
                BinEdComponentInspector binEdComponentInspector = new BinEdComponentInspector();
                binEdComponentInspector.setBasicValuesColorModifier(basicValuesColorModifier);
                return Optional.of(binEdComponentInspector);
            }

            @Override
            public void onPopupMenuCreation(JPopupMenu popupMenu,
                    ExtCodeArea codeArea, String menuPostfix, int x, int y) {
            }
        });

        binEdManager.setInspectorSupport(new BinEdManager.InspectorSupport() {
            @Nonnull
            public ShowParsingPanelAction showParsingPanelAction(BinEdComponentPanel binEdComponentPanel) {
                return new ShowParsingPanelAction(binEdComponentPanel);
            }
        });
    }

    @Nonnull
    public ResourceBundle getResourceBundle() {
        if (resourceBundle == null) {
            resourceBundle = LanguageUtils.getResourceBundleByClass(BinEdInspectorManager.class);
        }

        return resourceBundle;
    }

//    public void registerOptionsPanels() {
//        OptionsModuleApi optionsModule = application.getModuleRepository().getModuleByInterface(OptionsModuleApi.class);
//
//        dataInspectorOptionsPage = new DefaultOptionsPage<DataInspectorOptionsImpl>() {
//
//            private DataInspectorOptionsPanel panel;
//
//            @Nonnull
//            @Override
//            public OptionsCapable<DataInspectorOptionsImpl> createPanel() {
//                if (panel == null) {
//                    panel = new DataInspectorOptionsPanel();
//                }
//
//                return panel;
//            }
//
//            @Nonnull
//            @Override
//            public ResourceBundle getResourceBundle() {
//                return LanguageUtils.getResourceBundleByClass(DataInspectorOptionsPanel.class);
//            }
//
//            @Nonnull
//            @Override
//            public DataInspectorOptionsImpl createOptions() {
//                return new DataInspectorOptionsImpl();
//            }
//
//            @Override
//            public void loadFromPreferences(Preferences preferences, DataInspectorOptionsImpl options) {
//                options.loadFromPreferences(new DataInspectorPreferences(preferences));
//            }
//
//            @Override
//            public void saveToPreferences(Preferences preferences, DataInspectorOptionsImpl options) {
//                options.saveToPreferences(new DataInspectorPreferences(preferences));
//            }
//
//            @Override
//            public void applyPreferencesChanges(DataInspectorOptionsImpl options) {
//                getShowParsingPanelAction().setShowValuesPanel(options.isShowParsingPanel());
//            }
//        };
//        optionsModule.addOptionsPage(dataInspectorOptionsPage);
//    }
}
