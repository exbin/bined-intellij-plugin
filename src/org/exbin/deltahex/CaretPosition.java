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
package org.exbin.deltahex;

/**
 * Specifies caret position as combination of data position and code offset in
 * single byte representation.
 *
 * @version 0.1.0 2016/06/13
 * @author ExBin Project (http://exbin.org)
 */
public class CaretPosition {

    private long dataPosition = 0;
    private int codeOffset = 0;

    public CaretPosition() {
    }

    public CaretPosition(long dataPosition, int codeOffset) {
        this.dataPosition = dataPosition;
        this.codeOffset = codeOffset;
    }

    public long getDataPosition() {
        return dataPosition;
    }

    public void setDataPosition(long dataPosition) {
        this.dataPosition = dataPosition;
    }

    public int getCodeOffset() {
        return codeOffset;
    }

    public void setCodeOffset(int codeOffset) {
        this.codeOffset = codeOffset;
    }

    public void setPosition(CaretPosition position) {
        dataPosition = position.dataPosition;
        codeOffset = position.codeOffset;
    }
}
