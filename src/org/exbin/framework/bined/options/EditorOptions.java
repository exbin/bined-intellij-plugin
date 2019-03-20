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

import org.exbin.bined.intellij.FileHandlingMode;
import org.exbin.framework.bined.preferences.EditorParameters;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Hexadecimal editor preferences.
 *
 * @version 0.2.0 2019/03/16
 * @author ExBin Project (http://exbin.org)
 */
@ParametersAreNonnullByDefault
public class EditorOptions {

    private String fileHandlingMode = FileHandlingMode.DELTA.name();
    private boolean isShowValuesPanel = true;

    @Nonnull
    public String getFileHandlingMode() {
        return fileHandlingMode;
    }

    public void setFileHandlingMode(String fileHandlingMode) {
        this.fileHandlingMode = fileHandlingMode;
    }

    public boolean isIsShowValuesPanel() {
        return isShowValuesPanel;
    }

    public void setIsShowValuesPanel(boolean isShowValuesPanel) {
        this.isShowValuesPanel = isShowValuesPanel;
    }

    public void loadFromParameters(EditorParameters parameters) {
        fileHandlingMode = parameters.getFileHandlingMode();
        isShowValuesPanel = parameters.isShowValuesPanel();
    }

    public void saveToParameters(EditorParameters parameters) {
        parameters.setFileHandlingMode(fileHandlingMode);
        parameters.setShowValuesPanel(isShowValuesPanel);
    }

    public void setOptions(EditorOptions editorOptions) {
        fileHandlingMode = editorOptions.fileHandlingMode;
        isShowValuesPanel = editorOptions.isShowValuesPanel;
    }
}
