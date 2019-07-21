/*
 * Copyright (C) ExBin Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.exbin.framework.bined.options;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.bined.basic.EnterKeyHandlingMode;
import org.exbin.framework.bined.FileHandlingMode;
import org.exbin.framework.bined.preferences.EditorPreferences;
import org.exbin.framework.gui.options.api.OptionsData;

/**
 * Binary editor preferences.
 *
 * @version 0.2.1 2019/07/20
 * @author ExBin Project (http://exbin.org)
 */
@ParametersAreNonnullByDefault
public class EditorOptions implements OptionsData {

    private FileHandlingMode fileHandlingMode = FileHandlingMode.DELTA;
    private boolean isShowValuesPanel = true;
    private EnterKeyHandlingMode enterKeyHandlingMode = EnterKeyHandlingMode.PLATFORM_SPECIFIC;

    @Nonnull
    public FileHandlingMode getFileHandlingMode() {
        return fileHandlingMode;
    }

    public void setFileHandlingMode(FileHandlingMode fileHandlingMode) {
        this.fileHandlingMode = fileHandlingMode;
    }

    public boolean isIsShowValuesPanel() {
        return isShowValuesPanel;
    }

    public void setIsShowValuesPanel(boolean isShowValuesPanel) {
        this.isShowValuesPanel = isShowValuesPanel;
    }

    @Nonnull
    public EnterKeyHandlingMode getEnterKeyHandlingMode() {
        return enterKeyHandlingMode;
    }

    public void setEnterKeyHandlingMode(EnterKeyHandlingMode enterKeyHandlingMode) {
        this.enterKeyHandlingMode = enterKeyHandlingMode;
    }

    public void loadFromParameters(EditorPreferences preferences) {
        fileHandlingMode = preferences.getFileHandlingMode();
        isShowValuesPanel = preferences.isShowValuesPanel();
        enterKeyHandlingMode = preferences.getEnterKeyHandlingMode();
    }

    public void saveToParameters(EditorPreferences preferences) {
        preferences.setFileHandlingMode(fileHandlingMode);
        preferences.setShowValuesPanel(isShowValuesPanel);
        preferences.setEnterKeyHandlingMode(enterKeyHandlingMode);
    }

    public void setOptions(EditorOptions editorOptions) {
        fileHandlingMode = editorOptions.fileHandlingMode;
        isShowValuesPanel = editorOptions.isShowValuesPanel;
        enterKeyHandlingMode = editorOptions.enterKeyHandlingMode;
    }
}
