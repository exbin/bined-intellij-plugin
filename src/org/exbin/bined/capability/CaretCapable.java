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
import javax.annotation.Nullable;
import org.exbin.bined.BasicCodeAreaZone;
import org.exbin.bined.CaretMovedListener;
import org.exbin.bined.CaretPosition;
import org.exbin.bined.CodeAreaCaret;
import org.exbin.bined.PositionOverflowMode;
import org.exbin.bined.basic.MovementDirection;

/**
 * Support for caret / cursor capability.
 *
 * @version 0.2.0 2018/08/11
 * @author ExBin Project (https://exbin.org)
 */
public interface CaretCapable {

    /**
     * Returns handler for caret.
     *
     * @return caret handler
     */
    @Nonnull
    CodeAreaCaret getCaret();

    /**
     * Computes position for movement action.
     *
     * @param position source position
     * @param direction movement direction
     * @return target position
     */
    @Nonnull
    CaretPosition computeMovePosition(@Nonnull CaretPosition position, @Nonnull MovementDirection direction);

    /**
     * Computes closest caret position for given relative component position.
     *
     * @param positionX x-coordinate
     * @param positionY y-coordinate
     * @param overflowMode overflow mode
     * @return mouse position
     */
    @Nullable
    CaretPosition mousePositionToClosestCaretPosition(int positionX, int positionY, @Nonnull PositionOverflowMode overflowMode);

    /**
     * Returns if cursor should be visible in other sections.
     *
     * @return true if cursor should be mirrored
     */
    boolean isShowMirrorCursor();

    /**
     * Sets if cursor should be visible in other sections.
     *
     * @param showMirrorCursor true if cursor should be mirrored
     */
    void setShowMirrorCursor(boolean showMirrorCursor);

    void notifyCaretMoved();

    void notifyCaretChanged();

    /**
     * Returns cursor shape type for given position.
     *
     * TODO: Not part of caret?
     *
     * @param x x-coordinate
     * @param y y-coordinate
     * @return cursor type from java.awt.Cursor
     */
    int getMouseCursorShape(int x, int y);

    /**
     * Returns zone type for given position.
     *
     * TODO: Not part of caret?
     *
     * @param positionX x-coordinate
     * @param positionY y-coordinate
     * @return specific zone in component
     */
    @Nonnull
    BasicCodeAreaZone getPositionZone(int positionX, int positionY);

    void addCaretMovedListener(@Nullable CaretMovedListener caretMovedListener);

    void removeCaretMovedListener(@Nullable CaretMovedListener caretMovedListener);

    public static class CaretCapability implements CodeAreaCapability {

    }
}
