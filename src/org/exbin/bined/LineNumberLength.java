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

/**
 * Line number length.
 *
 * @version 0.2.0 2017/05/07
 * @author ExBin Project (https://exbin.org)
 */
public class LineNumberLength {

    @Nonnull
    private LineNumberType lineNumberType = LineNumberType.SPECIFIED;
    private int lineNumberLength = 8;

    public LineNumberLength() {
    }

    @Nonnull
    public LineNumberType getLineNumberType() {
        return lineNumberType;
    }

    public void setLineNumberType(@Nonnull LineNumberType lineNumberType) {
        if (lineNumberType == null) {
            throw new NullPointerException();
        }

        this.lineNumberType = lineNumberType;
    }

    public int getLineNumberLength() {
        return lineNumberLength;
    }

    public void setLineNumberLength(int lineNumberLength) {
        this.lineNumberLength = lineNumberLength;
    }

    /**
     * Enumeration of line number types.
     */
    public static enum LineNumberType {
        /**
         * Line number length is computed from data size and position code type.
         */
        AUTO,
        /**
         * Line number length is specified as fixed number of figures.
         */
        SPECIFIED
    }
}
