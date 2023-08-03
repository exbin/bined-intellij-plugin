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
package org.exbin.framework.bined.bookmarks.model;

import java.awt.Color;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Bookmark record.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class BookmarkRecord {

    private long startPosition;
    private long length;
    private Color color;

    public BookmarkRecord() {
        color = Color.GRAY;
    }

    public BookmarkRecord(long startPosition, long length, Color color) {
        this.startPosition = startPosition;
        this.length = length;
        this.color = color;
    }

    /**
     * Copy constructor.
     *
     * @param record record
     */
    public BookmarkRecord(BookmarkRecord record) {
        BookmarkRecord.this.setRecord(record);
    }

    public void setRecord(BookmarkRecord record) {
        startPosition = record.startPosition;
        length = record.length;
        color = record.color;
    }

    public long getStartPosition() {
        return startPosition;
    }

    public void setStartPosition(long startPosition) {
        this.startPosition = startPosition;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    @Nonnull
    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }
    
    public boolean isEmpty() {
        return length == 0;
    }
    
    public void setEmpty() {
        length = 0;
    }
}
