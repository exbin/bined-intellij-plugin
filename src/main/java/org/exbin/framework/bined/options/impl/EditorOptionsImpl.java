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
package org.exbin.framework.bined.options.impl;

import org.exbin.framework.bined.options.EditorOptions;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.bined.basic.EnterKeyHandlingMode;
import org.exbin.bined.basic.TabKeyHandlingMode;
import org.exbin.framework.bined.FileHandlingMode;
import org.exbin.framework.bined.preferences.EditorPreferences;
import org.exbin.framework.options.api.OptionsData;

/**
 * Binary editor preferences.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class EditorOptionsImpl implements OptionsData, EditorOptions {

    private FileHandlingMode fileHandlingMode = FileHandlingMode.DELTA;
    private EnterKeyHandlingMode enterKeyHandlingMode = EnterKeyHandlingMode.PLATFORM_SPECIFIC;
    private TabKeyHandlingMode tabKeyHandlingMode = TabKeyHandlingMode.PLATFORM_SPECIFIC;

    @Nonnull
    @Override
    public FileHandlingMode getFileHandlingMode() {
        return fileHandlingMode;
    }

    @Override
    public void setFileHandlingMode(FileHandlingMode fileHandlingMode) {
        this.fileHandlingMode = fileHandlingMode;
    }

    @Nonnull
    @Override
    public EnterKeyHandlingMode getEnterKeyHandlingMode() {
        return enterKeyHandlingMode;
    }

    @Override
    public void setEnterKeyHandlingMode(EnterKeyHandlingMode enterKeyHandlingMode) {
        this.enterKeyHandlingMode = enterKeyHandlingMode;
    }

    @Nonnull
    @Override
    public TabKeyHandlingMode getTabKeyHandlingMode() {
        return tabKeyHandlingMode;
    }

    @Override
    public void setTabKeyHandlingMode(TabKeyHandlingMode tabKeyHandlingMode) {
        this.tabKeyHandlingMode = tabKeyHandlingMode;
    }

    public void loadFromPreferences(EditorPreferences preferences) {
        fileHandlingMode = preferences.getFileHandlingMode();
        enterKeyHandlingMode = preferences.getEnterKeyHandlingMode();
        tabKeyHandlingMode = preferences.getTabKeyHandlingMode();
    }

    public void saveToPreferences(EditorPreferences preferences) {
        preferences.setFileHandlingMode(fileHandlingMode);
        preferences.setEnterKeyHandlingMode(enterKeyHandlingMode);
        preferences.setTabKeyHandlingMode(tabKeyHandlingMode);
    }

    public void setOptions(EditorOptionsImpl editorOptions) {
        fileHandlingMode = editorOptions.fileHandlingMode;
        enterKeyHandlingMode = editorOptions.enterKeyHandlingMode;
        tabKeyHandlingMode = editorOptions.tabKeyHandlingMode;
    }
}
