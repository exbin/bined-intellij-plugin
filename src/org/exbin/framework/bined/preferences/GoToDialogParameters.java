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
package org.exbin.framework.bined.preferences;

import org.exbin.framework.Preferences;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Hexadecimal editor preferences.
 *
 * @version 0.2.0 2019/03/15
 * @author ExBin Project (http://exbin.org)
 */
@ParametersAreNonnullByDefault
public class GoToDialogParameters {

    public static final String PREFERENCES_FILE_HANDLING_MODE = "fileHandlingMode";
    public static final String PREFERENCES_SHOW_VALUES_PANEL = "valuesPanel";
    public static final String PREFERENCES_STATUS_CURSOR_POSITION_FORMAT = "statusCursrPositionFormat";
    public static final String PREFERENCES_STATUS_DOCUMENT_SIZE_FORMAT = "statusDocumentSizeFormat";

    private final Preferences preferences;

    public GoToDialogParameters(Preferences preferences) {
        this.preferences = preferences;
    }

    @Nonnull
    public String getFileHandlingMode() {
        return preferences.get(PREFERENCES_FILE_HANDLING_MODE, "MEMORY");
    }

    public void setFileHandlingMode(String fileHandlingMode) {
        preferences.put(PREFERENCES_FILE_HANDLING_MODE, fileHandlingMode);
    }

    public boolean isShowValuesPanel() {
        return preferences.getBoolean(PREFERENCES_SHOW_VALUES_PANEL, true);
    }

    public void setShowValuesPanel(boolean showValuesPanel) {
        preferences.putBoolean(PREFERENCES_SHOW_VALUES_PANEL, showValuesPanel);
    }
    
    @Nonnull
    public String getStatusCursorPositionFormat() {
        return preferences.get(PREFERENCES_STATUS_CURSOR_POSITION_FORMAT, "HEX");
    }

    public void setStatusCursorPositionFormat(String statusCursorPositionFormat) {
        preferences.put(PREFERENCES_STATUS_CURSOR_POSITION_FORMAT, statusCursorPositionFormat);
    }

    @Nonnull
    public String getStatusDocumentSizeFormat() {
        return preferences.get(PREFERENCES_STATUS_DOCUMENT_SIZE_FORMAT, "HEX");
    }

    public void setStatusDocumentSizeFormat(String statusDocumentSizeFormat) {
        preferences.put(PREFERENCES_STATUS_DOCUMENT_SIZE_FORMAT, statusDocumentSizeFormat);
    }
}
