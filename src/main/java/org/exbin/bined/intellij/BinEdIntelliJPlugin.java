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

import javax.annotation.Nonnull;
import java.util.ResourceBundle;

/**
 * Anchor class for BinEd plugin preferences root.
 *
 * @author ExBin Project (https://exbin.org)
 */
public class BinEdIntelliJPlugin {

    public static final String PLUGIN_ID = "org.exbin.deltahex.intellij";
    public static final String PLUGIN_PREFIX = "BinEdPlugin.";
    private static ResourceBundle resourceBundle = null;

    private BinEdIntelliJPlugin() {
    }

    @Nonnull
    public static ResourceBundle getResourceBundle() {
        if (resourceBundle == null) {
            resourceBundle = ResourceBundle.getBundle("org.exbin.bined.intellij.resources.Plugin");
        }
        return resourceBundle;
    }
}
