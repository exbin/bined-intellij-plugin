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
package org.exbin.framework.bined;

import java.awt.event.MouseEvent;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.bined.CodeAreaCaretPosition;
import org.exbin.bined.EditationMode;
import org.exbin.bined.EditationOperation;
import org.exbin.bined.SelectionRange;

/**
 * Binary editor status interface.
 *
 * @version 0.2.1 2020/01/24
 * @author ExBin Project (http://exbin.org)
 */
@ParametersAreNonnullByDefault
public interface BinaryStatusApi {

    /**
     * Reports cursor position.
     *
     * @param cursorPosition cursor position
     */
    void setCursorPosition(CodeAreaCaretPosition cursorPosition);

    /**
     * Reports cursor position.
     *
     * @param selectionRange current selection
     */
    void setSelectionRange(SelectionRange selectionRange);

    /**
     * Reports currently active editation mode.
     *
     * @param mode editation mode
     * @param operation editation operation
     */
    void setEditationMode(EditationMode mode, EditationOperation operation);

    /**
     * Sets control handler for status operations.
     *
     * @param statusControlHandler status control handler
     */
    void setControlHandler(StatusControlHandler statusControlHandler);

    /**
     * Sets current document size.
     *
     * @param documentSize document size
     * @param initialDocumentSize document size when file was opened
     */
    void setCurrentDocumentSize(long documentSize, long initialDocumentSize);

    /**
     * Sets current memory mode.
     *
     * @param memoryMode memory mode
     */
    void setMemoryMode(MemoryMode memoryMode);

    @ParametersAreNonnullByDefault
    public static interface StatusControlHandler {

        /**
         * Requests change of editation mode from given mode.
         *
         * @param operation editation operation
         */
        void changeEditationOperation(EditationOperation operation);

        /**
         * Requests change of cursor position using go-to dialog.
         */
        void changeCursorPosition();

        /**
         * Switches to next encoding in defined list.
         */
        void cycleEncodings();

        /**
         * Handles encodings popup menu.
         *
         * @param mouseEvent mouse event
         */
        void encodingsPopupEncodingsMenu(MouseEvent mouseEvent);

        /**
         * Requests change of memory mode.
         *
         * @param memoryMode memory mode
         */
        void changeMemoryMode(MemoryMode memoryMode);
    }

    @ParametersAreNonnullByDefault
    public static enum MemoryMode {

        READ_ONLY("R", "read_only"),
        RAM_MEMORY("M", "ram"),
        DELTA_MODE("\u0394", "delta");

        private final String displayChar;
        private final String value;

        private MemoryMode(String displayChar, String preferencesValue) {
            this.displayChar = displayChar;
            this.value = preferencesValue;
        }

        @Nonnull
        public String getDisplayChar() {
            return displayChar;
        }

        @Nonnull
        public String getPreferencesValue() {
            return value;
        }

        @Nullable
        public static MemoryMode findByPreferencesValue(String matchValue) {
            for (MemoryMode value : values()) {
                if (value.getPreferencesValue().equals(matchValue)) {
                    return value;
                }
            }
            return null;
        }
    }
}
