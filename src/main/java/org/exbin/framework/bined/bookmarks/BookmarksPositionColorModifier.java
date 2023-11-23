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
package org.exbin.framework.bined.bookmarks;

import java.awt.Color;
import java.util.List;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.bined.CodeAreaSection;
import org.exbin.framework.bined.BinEdCodeAreaPainter;
import org.exbin.framework.bined.bookmarks.model.BookmarkRecord;

/**
 * Bookmarks position color modifier.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class BookmarksPositionColorModifier implements BinEdCodeAreaPainter.PositionColorModifier {

    private final List<BookmarkRecord> records;
    private final ColorCache colorCache = new ColorCache();

    public BookmarksPositionColorModifier(List<BookmarkRecord> records) {
        this.records = records;
    }

    @Nullable
    @Override
    public Color getPositionBackgroundColor(long rowDataPosition, int byteOnRow, int charOnRow, CodeAreaSection section, boolean unprintables) {
        long dataPosition = rowDataPosition + byteOnRow;
        if (colorCache.start < 0 || colorCache.start > dataPosition || (colorCache.end >= 0 && colorCache.end < dataPosition)) {
            colorCache.fullRange();
            for (BookmarkRecord record : records) {
                if (record.isEmpty()) {
                    continue;
                }

                long startPosition = record.getStartPosition();
                long endPosition = startPosition + record.getLength() - 1;
                if (startPosition <= dataPosition && endPosition >= dataPosition) {
                    colorCache.start = startPosition;
                    colorCache.end = endPosition;
                    colorCache.color = record.getColor();
                } else if (startPosition > dataPosition && startPosition > colorCache.start && (startPosition <= colorCache.end || colorCache.end == -1)) {
                    colorCache.end = startPosition - 1;
                } else if (endPosition < dataPosition && endPosition >= colorCache.start && (endPosition <= colorCache.end || colorCache.end == -1)) {
                    colorCache.start = endPosition + 1;
                }
            }
        }

        return colorCache.color;
    }

    @Nullable
    @Override
    public Color getPositionTextColor(long rowDataPosition, int byteOnRow, int charOnRow, CodeAreaSection section, boolean unprintables) {
        return null;
    }

    @Override
    public void resetColors() {
        // do nothing, maybe invert colors?
    }

    public void notifyBookmarksChanged() {
        colorCache.clear();
    }

    private static class ColorCache {

        long start;
        long end;
        @Nullable
        Color color;

        public ColorCache() {
            clear();
        }

        private void clear() {
            start = -1;
            end = -1;
            color = null;
        }

        private void fullRange() {
            start = 0;
            end = -1;
            color = null;
        }
    }
}
