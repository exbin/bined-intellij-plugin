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

import java.util.Objects;
import javax.annotation.Nonnull;

/**
 * Specifies caret position as combination of data position, section and code
 * offset of code representation.
 *
 * @version 0.2.0 2018/02/17
 * @author ExBin Project (https://exbin.org)
 */
public class CodeAreaCaretPosition implements CaretPosition {

    private long dataPosition = 0;
    private int codeOffset = 0;
    private int section = 0;

    public CodeAreaCaretPosition() {
    }

    public CodeAreaCaretPosition(long dataPosition, int codeOffset, int section) {
        this.dataPosition = dataPosition;
        this.codeOffset = codeOffset;
        this.section = section;
    }

    @Override
    public long getDataPosition() {
        return dataPosition;
    }

    public void setDataPosition(long dataPosition) {
        this.dataPosition = dataPosition;
    }

    @Override
    public int getCodeOffset() {
        return codeOffset;
    }

    public void setCodeOffset(int codeOffset) {
        this.codeOffset = codeOffset;
    }

    @Override
    public int getSection() {
        return section;
    }

    public void setSection(int section) {
        this.section = section;
    }

    /**
     * Sets caret position according to given position.
     *
     * @param position source position
     */
    public void setPosition(@Nonnull CaretPosition position) {
        dataPosition = position.getDataPosition();
        codeOffset = position.getCodeOffset();
        section = position.getSection();
    }

    public void clear() {
        this.dataPosition = 0;
        this.codeOffset = 0;
        this.section = 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(dataPosition, codeOffset, section);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        final CodeAreaCaretPosition other = (CodeAreaCaretPosition) obj;
        return Objects.equals(this.dataPosition, other.dataPosition)
                && Objects.equals(this.codeOffset, other.codeOffset)
                && Objects.equals(this.section, other.section);
    }
}
