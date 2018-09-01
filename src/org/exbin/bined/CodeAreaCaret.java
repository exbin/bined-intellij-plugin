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
package org.exbin.bined;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Interface for code area caret.
 *
 * @version 0.2.0 2018/02/14
 * @author ExBin Project (https://exbin.org)
 */
public interface CodeAreaCaret {

    /**
     * Returns caret position.
     *
     * Returned value should not be cast for editing.
     *
     * @return caret position
     */
    @Nonnull
    CaretPosition getCaretPosition();

    /**
     * Sets current caret position to provided value.
     *
     * @param caretPosition caret position
     */
    void setCaretPosition(@Nullable CaretPosition caretPosition);

    /**
     * Sets current caret position to given position preserving section.
     *
     * @param dataPosition data position
     * @param codeOffset code offset
     */
    void setCaretPosition(long dataPosition, int codeOffset);

    /**
     * Sets current caret position to given position resetting offset and
     * preserving section.
     *
     * @param dataPosition data position
     */
    void setCaretPosition(long dataPosition);
}
