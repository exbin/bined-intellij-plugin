/*
 * Copyright (C) ExBin Project
 *
 * This application or library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * This application or library is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along this application.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.exbin.framework.bined;

import java.awt.event.MouseEvent;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.bined.CodeAreaCaretPosition;
import org.exbin.bined.EditationMode;
import org.exbin.bined.EditationOperation;

/**
 * Hexadecimal editor status interface.
 *
 * @version 0.2.1 2019/06/16
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
