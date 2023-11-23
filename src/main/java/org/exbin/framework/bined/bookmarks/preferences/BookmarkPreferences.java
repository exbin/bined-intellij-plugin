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
package org.exbin.framework.bined.bookmarks.preferences;

import java.awt.Color;
import java.util.Optional;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.framework.api.Preferences;
import org.exbin.framework.bined.bookmarks.model.BookmarkRecord;
import org.exbin.framework.bined.bookmarks.options.BookmarkOptions;

/**
 * Code area bookmarks preferences.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class BookmarkPreferences implements BookmarkOptions {

    public static final String PREFERENCES_BOOKMARK_COUNT = "bookmarksCount";
    public static final String PREFERENCES_BOOKMARK_VALUE_PREFIX = "bookmark.";

    public static final String BOOKMARK_START_POSITION = "startPosition";
    public static final String BOOKMARK_LENGTH = "length";
    public static final String BOOKMARK_COLOR = "bookmarkColor";

    private final Preferences preferences;

    public BookmarkPreferences(Preferences preferences) {
        this.preferences = preferences;
    }

    @Override
    public int getBookmarksCount() {
        return preferences.getInt(PREFERENCES_BOOKMARK_COUNT, 0);
    }

    @Override
    public BookmarkRecord getBookmarkRecord(int index) {
        String prefix = PREFERENCES_BOOKMARK_VALUE_PREFIX + index + ".";
        long startPosition = preferences.getLong(prefix + BOOKMARK_START_POSITION, 0);
        long length = preferences.getLong(prefix + BOOKMARK_LENGTH, 0);
        Color color = textAsColor(preferences.get(prefix + BOOKMARK_COLOR));
        return new BookmarkRecord(startPosition, length, color);
    }

    @Override
    public void setBookmarksCount(int count) {
        preferences.putInt(PREFERENCES_BOOKMARK_COUNT, count);
    }

    @Override
    public void setBookmarkRecord(int index, BookmarkRecord record) {
        String prefix = PREFERENCES_BOOKMARK_VALUE_PREFIX + index + ".";
        preferences.putLong(prefix + BOOKMARK_START_POSITION, record.getStartPosition());
        preferences.putLong(prefix + BOOKMARK_LENGTH, record.getLength());
        preferences.put(prefix + BOOKMARK_COLOR, colorAsText(record.getColor()));
    }

    /**
     * Converts color to text.
     *
     * @param color color
     * @return color string in hex format, e.g. "#FFFFFF"
     */
    @Nullable
    private static String colorAsText(@Nullable Color color) {
        if (color == null) {
            return null;
        }
        int red = color.getRed();
        int green = color.getGreen();
        int blue = color.getBlue();

        return String.format("#%02x%02x%02x", red, green, blue);
    }

    /**
     * Converts text to color.
     *
     * @param colorStr e.g. "#FFFFFF"
     * @return color or null
     */
    @Nullable
    private static Color textAsColor(Optional<String> colorStr) {
        if (!colorStr.isPresent()) {
            return null;
        }
        return Color.decode(colorStr.get());
    }
}
