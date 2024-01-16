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
package org.exbin.framework.bined.macro.operation;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Enumeration of currently supported operations.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public enum MacroStep {
    KEY_PRESSED("key-press"),
    ENTER_KEY("enter-key"),
    BACKSPACE_KEY("backspace-key"),
    DELETE_KEY("delete-key"),
    CARET_SET("caret-set"),
    CARET_MOVE("caret-move"),
    SELECTION_UPDATE("selection-update"),
    SELECTION_SET("selection-set"),
    CLIPBOARD_CUT("clipboard-cut"),
    CLIPBOARD_COPY("clipboard-copy"),
    CLIPBOARD_COPY_AS_CODE("clipboard-copy-as-code"),
    CLIPBOARD_PASTE("clipboard-paste"),
    CLIPBOARD_PASTE_FROM_CODE("clipboard-paste-from-code"),
    CLIPBOARD_DELETE("clipboard-delete"),
    SELECTION_SELECT_ALL("selection-select-all"),
    SELECTION_CLEAR("selection-clear"),
    EDIT_OPERATION_CHANGE("edit-operation-change"),
    FIND_TEXT("search-find-text"),
    FIND_AGAIN("search-find-again");

    private final String operationCode;
    public static final Map<String, MacroStep> MAP = new HashMap<>();

    static {
        for (MacroStep macroStep : MacroStep.values()) {
            MAP.put(macroStep.operationCode, macroStep);
        }
    }

    private MacroStep(String operationCode) {
        this.operationCode = operationCode;
    }

    @Nonnull
    public String getOperationCode() {
        return operationCode;
    }

    @Nonnull
    public static Optional<MacroStep> findByCode(String operationCode) {
        return Optional.ofNullable(MAP.get(operationCode));
    }
}
