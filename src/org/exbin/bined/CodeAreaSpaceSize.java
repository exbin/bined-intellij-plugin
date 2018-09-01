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
 * Empty space size definition.
 *
 * @version 0.2.0 2017/04/09
 * @author ExBin Project (https://exbin.org)
 */
public class CodeAreaSpaceSize {

    @Nonnull
    private SpaceSizeType spaceType = SpaceSizeType.ONE_UNIT;
    private int spaceSize;

    public CodeAreaSpaceSize() {
    }

    public CodeAreaSpaceSize(@Nonnull SpaceSizeType spaceType) {
        CodeAreaUtils.requireNonNull(spaceType);

        this.spaceType = spaceType;
    }

    @Nonnull
    public SpaceSizeType getSpaceType() {
        return spaceType;
    }

    public void setSpaceType(@Nonnull SpaceSizeType spaceType) {
        CodeAreaUtils.requireNonNull(spaceType);

        this.spaceType = spaceType;
    }

    public int getSpaceSize() {
        return spaceSize;
    }

    public void setSpaceSize(int spaceSize) {
        this.spaceSize = spaceSize;
    }

    /**
     * Enumeration of space size types.
     */
    public static enum SpaceSizeType {
        /**
         * Space is empty.
         */
        NONE,
        /**
         * Space size is specified as fixed value by pixels.
         */
        SPECIFIED,
        /**
         * 1/4 of space unit.
         */
        QUARTER_UNIT,
        /**
         * 1/2 of space unit.
         */
        HALF_UNIT,
        /**
         * One space unit.
         */
        ONE_UNIT,
        /**
         * 1.5 of space unit.
         */
        ONE_AND_HALF_UNIT,
        /**
         * 2 space units.
         */
        DOUBLE_UNIT
    }
}
