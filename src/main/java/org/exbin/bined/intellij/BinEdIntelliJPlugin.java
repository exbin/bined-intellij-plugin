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

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.openapi.extensions.PluginId;
import org.exbin.framework.language.LanguageModule;
import org.exbin.framework.language.api.IconSetProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * Anchor class for BinEd plugin preferences root.
 *
 * @author ExBin Project (https://exbin.org)
 */
public class BinEdIntelliJPlugin {

    public static final String PLUGIN_ID = "org.exbin.deltahex.intellij";
    public static final String PLUGIN_PREFIX = "BinEdPlugin.";
    private static LanguageResourceBundle resourceBundle = null;

    private BinEdIntelliJPlugin() {
    }

    @Nonnull
    public static ResourceBundle getResourceBundle() {
        if (resourceBundle == null) {
            // LanguageModuleApi languageModule = App.getModule(LanguageModuleApi.class);
            // resourceBundle = languageModule.getBundle(BinEdIntelliJPlugin.class);
            // resourceBundle = ResourceBundle.getBundle("org.exbin.bined.intellij.resources.Plugin")
            // resourceBundle = ResourceBundle.getBundle("org.exbin.bined.intellij.resources.BinEdIntelliJPlugin");
            resourceBundle = new LanguageResourceBundle("org.exbin.bined.intellij.resources.BinEdIntelliJPlugin");
            // com.intellij.DynamicBundle.getResourceBundle(BinEdIntelliJPlugin.class.getClassLoader(), "org.exbin.bined.intellij.resources.Plugin");
            // IdeaPluginDescriptor pluginDescriptor = PluginManagerCore.getPlugin(PluginId.getId(PLUGIN_ID));
            // resourceBundle = com.intellij.DynamicBundle.getResourceBundle(pluginDescriptor.getPluginClassLoader(), pluginDescriptor.getResourceBundleBaseName());
        }
        return resourceBundle;
    }

    public static void setLocale(Locale locale) {
        resourceBundle.setLocale(locale);
    }

    @ParametersAreNonnullByDefault
    private static class LanguageResourceBundle extends ResourceBundle {
        private final ResourceBundle mainResourceBundle;
        private ResourceBundle languageResourceBundle;
        private IconSetProvider iconSetProvider = null;
        private String baseName;
        private String prefix;

        public LanguageResourceBundle(String baseName) {
            this.baseName = baseName;
            this.prefix = baseName.replace("/", ".") + ".";
            this.mainResourceBundle = ResourceBundle.getBundle(baseName, Locale.ROOT);
            this.languageResourceBundle = ResourceBundle.getBundle(baseName);
        }

        public void setLocale(Locale locale) {
            languageResourceBundle = ResourceBundle.getBundle(baseName, locale);
        }

        public void setIconSet(@Nullable IconSetProvider iconSetProvider) {
            this.iconSetProvider = iconSetProvider;
        }

        @Nullable
        protected Object handleGetObject(String key) {
            Object object = this.iconSetProvider != null ? this.iconSetProvider.getIconKey(this.prefix + key) : null;
            if (object == null) {
                object = this.languageResourceBundle.getObject(key);
            }

            if (object == null) {
                object = this.mainResourceBundle.getObject(key);
            }

            return object;
        }

        @Nonnull
        public Enumeration<String> getKeys() {
            Set<String> keys = new HashSet<>();
            keys.addAll(Collections.list(this.languageResourceBundle.getKeys()));
            keys.addAll(Collections.list(this.mainResourceBundle.getKeys()));
            return Collections.enumeration(keys);
        }
    }
}
