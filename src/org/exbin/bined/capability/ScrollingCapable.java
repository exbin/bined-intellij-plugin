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
package org.exbin.bined.capability;

import javax.annotation.Nonnull;
import org.exbin.bined.CaretPosition;
import org.exbin.bined.ScrollBarVisibility;
import org.exbin.bined.ScrollingListener;
import org.exbin.bined.basic.CodeAreaScrollPosition;
import org.exbin.bined.basic.HorizontalScrollUnit;
import org.exbin.bined.basic.ScrollingDirection;
import org.exbin.bined.basic.VerticalScrollUnit;

/**
 * Support for code type capability.
 *
 * @version 0.2.0 2018/01/29
 * @author ExBin Project (https://exbin.org)
 */
public interface ScrollingCapable {

    @Nonnull
    CodeAreaScrollPosition getScrollPosition();

    void setScrollPosition(@Nonnull CodeAreaScrollPosition scrollPosition);

    @Nonnull
    ScrollBarVisibility getVerticalScrollBarVisibility();

    void setVerticalScrollBarVisibility(@Nonnull ScrollBarVisibility verticalScrollBarVisibility);

    @Nonnull
    VerticalScrollUnit getVerticalScrollUnit();

    void setVerticalScrollUnit(@Nonnull VerticalScrollUnit verticalScrollUnit);

    @Nonnull
    ScrollBarVisibility getHorizontalScrollBarVisibility();

    void setHorizontalScrollBarVisibility(@Nonnull ScrollBarVisibility horizontalScrollBarVisibility);

    @Nonnull
    HorizontalScrollUnit getHorizontalScrollUnit();

    void setHorizontalScrollUnit(@Nonnull HorizontalScrollUnit horizontalScrollUnit);

    void notifyScrolled();

    void addScrollingListener(@Nonnull ScrollingListener scrollingListener);

    void removeScrollingListener(@Nonnull ScrollingListener scrollingListener);

    @Nonnull
    CodeAreaScrollPosition computeScrolling(@Nonnull CodeAreaScrollPosition startPosition, @Nonnull ScrollingDirection direction);

    void updateScrollBars();

    /**
     * Reveals scrolling area for current cursor position.
     */
    void revealCursor();

    /**
     * Reveals scrolling area for given caret position.
     *
     * @param caretPosition caret position
     */
    void revealPosition(@Nonnull CaretPosition caretPosition);

    /**
     * Scrolls scrolling area as centered as possible for current cursor
     * position.
     */
    void centerOnCursor();

    /**
     * Scrolls scrolling area as centered as possible for given caret position.
     *
     * @param caretPosition caret position
     */
    void centerOnPosition(@Nonnull CaretPosition caretPosition);

    public static class ScrollingCapability implements CodeAreaCapability {

    }
}
