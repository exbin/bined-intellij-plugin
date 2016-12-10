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
package org.exbin.deltahex.swing;

/**
 * Empty space definition.
 *
 * @version 0.1.0 2016/06/20
 * @author ExBin Project (http://exbin.org)
 */
public class CodeAreaSpace {

    private SpaceType spaceType = SpaceType.ONE_UNIT;
    private int spaceSize;

    public CodeAreaSpace() {
    }

    public CodeAreaSpace(SpaceType spaceType) {
        this.spaceType = spaceType;
    }

    public SpaceType getSpaceType() {
        return spaceType;
    }

    public void setSpaceType(SpaceType spaceType) {
        this.spaceType = spaceType;
    }

    public int getSpaceSize() {
        return spaceSize;
    }

    public void setSpaceSize(int spaceSize) {
        this.spaceSize = spaceSize;
    }

    public static enum SpaceType {
        NONE,
        SPECIFIED,
        QUARTER_UNIT,
        HALF_UNIT,
        ONE_UNIT,
        ONE_AND_HALF_UNIT,
        DOUBLE_UNIT
    }
}
