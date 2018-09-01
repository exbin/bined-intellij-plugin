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
package org.exbin.bined.basic;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.exbin.bined.DataProvider;
import org.exbin.bined.ScrollBarVisibility;
import org.exbin.bined.capability.ScrollingCapable;

/**
 * Code area scrolling.
 *
 * @version 0.2.0 2018/08/15
 * @author ExBin Project (https://exbin.org)
 */
public class BasicCodeAreaScrolling {

    @Nonnull
    private final CodeAreaScrollPosition scrollPosition = new CodeAreaScrollPosition();
    @Nonnull
    private ScrollBarVerticalScale scrollBarVerticalScale = ScrollBarVerticalScale.NORMAL;

    @Nonnull
    private VerticalScrollUnit verticalScrollUnit = VerticalScrollUnit.PIXEL;
    @Nonnull
    private ScrollBarVisibility verticalScrollBarVisibility = ScrollBarVisibility.IF_NEEDED;
    @Nonnull
    private HorizontalScrollUnit horizontalScrollUnit = HorizontalScrollUnit.CHARACTER;
    @Nonnull
    private ScrollBarVisibility horizontalScrollBarVisibility = ScrollBarVisibility.IF_NEEDED;
    @Nonnull
    private final CodeAreaScrollPosition maximumScrollPosition = new CodeAreaScrollPosition();

    public void updateCache(@Nonnull DataProvider codeArea) {
        verticalScrollUnit = ((ScrollingCapable) codeArea).getVerticalScrollUnit();
        verticalScrollBarVisibility = ((ScrollingCapable) codeArea).getVerticalScrollBarVisibility();
        horizontalScrollUnit = ((ScrollingCapable) codeArea).getHorizontalScrollUnit();
        horizontalScrollBarVisibility = ((ScrollingCapable) codeArea).getHorizontalScrollBarVisibility();
    }

    public void updateHorizontalScrollBarValue(int scrollBarValue, int characterWidth) {
        if (horizontalScrollUnit == HorizontalScrollUnit.CHARACTER) {
            scrollPosition.setCharPosition(scrollBarValue);
        } else {
            if (characterWidth == 0) {
                scrollPosition.setCharPosition(0);
                scrollPosition.setCharOffset(0);
            } else if (horizontalScrollUnit == HorizontalScrollUnit.CHARACTER) {
                int charPosition = scrollBarValue / characterWidth;
                if (scrollBarValue % characterWidth > 0) {
                    charPosition++;
                }
                scrollPosition.setCharPosition(charPosition);
                scrollPosition.setCharOffset(0);
            } else {
                scrollPosition.setCharPosition(scrollBarValue / characterWidth);
                scrollPosition.setCharOffset(scrollBarValue % characterWidth);
            }
        }
    }

    public void updateVerticalScrollBarValue(int scrollBarValue, int rowHeight, int maxValue, long rowsPerDocumentToLastPage) {
        if (scrollBarVerticalScale == ScrollBarVerticalScale.SCALED) {
            long targetRow;
            if (scrollBarValue > 0 && rowsPerDocumentToLastPage > maxValue / scrollBarValue) {
                targetRow = scrollBarValue * (rowsPerDocumentToLastPage / maxValue);
                long rest = rowsPerDocumentToLastPage % maxValue;
                targetRow += (rest * scrollBarValue) / maxValue;
            } else {
                targetRow = (scrollBarValue * rowsPerDocumentToLastPage) / Integer.MAX_VALUE;
            }
            scrollPosition.setRowPosition(targetRow);
            if (verticalScrollUnit != VerticalScrollUnit.ROW) {
                scrollPosition.setRowOffset(0);
            }
        } else {
            if (rowHeight == 0) {
                scrollPosition.setRowPosition(0);
                scrollPosition.setRowOffset(0);
            } else if (verticalScrollUnit == VerticalScrollUnit.ROW) {
                int rowPosition = scrollBarValue / rowHeight;
                if (scrollBarValue % rowHeight > 0) {
                    rowPosition++;
                }
                scrollPosition.setRowPosition(rowPosition);
                scrollPosition.setRowOffset(0);
            } else {
                scrollPosition.setRowPosition(scrollBarValue / rowHeight);
                scrollPosition.setRowOffset(scrollBarValue % rowHeight);
            }
        }
    }

    public int getVerticalScrollValue(int rowHeight, long rowsPerDocument) {
        if (scrollBarVerticalScale == ScrollBarVerticalScale.SCALED) {
            int scrollValue;
            if (scrollPosition.getCharPosition() < Long.MAX_VALUE / Integer.MAX_VALUE) {
                scrollValue = (int) ((scrollPosition.getRowPosition() * Integer.MAX_VALUE) / rowsPerDocument);
            } else {
                scrollValue = (int) (scrollPosition.getRowPosition() / (rowsPerDocument / Integer.MAX_VALUE));
            }
            return scrollValue;
        } else if (verticalScrollUnit == VerticalScrollUnit.ROW) {
            return (int) scrollPosition.getRowPosition() * rowHeight;
        } else {
            return (int) (scrollPosition.getRowPosition() * rowHeight + scrollPosition.getRowOffset());
        }
    }

    public int getHorizontalScrollValue(int characterWidth) {
        if (horizontalScrollUnit == HorizontalScrollUnit.CHARACTER) {
            return scrollPosition.getCharPosition() * characterWidth;
        } else {
            return scrollPosition.getCharPosition() * characterWidth + scrollPosition.getCharOffset();
        }
    }

    @Nonnull
    public CodeAreaScrollPosition computeScrolling(@Nonnull CodeAreaScrollPosition startPosition, @Nonnull ScrollingDirection direction, int rowsPerPage, long rowsPerDocument) {
        CodeAreaScrollPosition targetPosition = new CodeAreaScrollPosition();
        targetPosition.setScrollPosition(startPosition);

        switch (direction) {
            case UP: {
                if (startPosition.getRowPosition() == 0) {
                    targetPosition.setRowOffset(0);
                } else {
                    targetPosition.setRowPosition(startPosition.getRowPosition() - 1);
                }
                break;
            }
            case DOWN: {
                if (maximumScrollPosition.isRowPositionGreaterThan(startPosition)) {
                    targetPosition.setRowPosition(startPosition.getRowPosition() + 1);
                }
                break;
            }
            case LEFT: {
                if (startPosition.getCharPosition() == 0) {
                    targetPosition.setCharOffset(0);
                } else {
                    targetPosition.setCharPosition(startPosition.getCharPosition() - 1);
                }
                break;
            }
            case RIGHT: {
                if (maximumScrollPosition.isCharPositionGreaterThan(startPosition)) {
                    targetPosition.setCharPosition(startPosition.getCharPosition() + 1);
                }
                break;
            }
            case PAGE_UP: {
                if (startPosition.getRowPosition() < rowsPerPage) {
                    targetPosition.setRowPosition(0);
                    targetPosition.setRowOffset(0);
                } else {
                    targetPosition.setRowPosition(startPosition.getRowPosition() - rowsPerPage);
                }
                break;
            }
            case PAGE_DOWN: {
                if (startPosition.getRowPosition() <= rowsPerDocument - rowsPerPage * 2) {
                    targetPosition.setRowPosition(startPosition.getRowPosition() + rowsPerPage);
                } else if (rowsPerDocument > rowsPerPage) {
                    targetPosition.setRowPosition(rowsPerDocument - rowsPerPage);
                } else {
                    targetPosition.setRowPosition(0);
                }
                break;
            }
            default:
                throw new IllegalStateException("Unexpected scrolling direction type: " + direction.name());
        }

        return targetPosition;
    }

    @Nonnull
    public PositionScrollVisibility computePositionScrollVisibility(long rowPosition, int charPosition, int bytesPerRow, int previewCharPos, int rowsPerPage, int charactersPerPage, int dataViewWidth, int dataViewHeight, int characterWidth, int rowHeight) {
        boolean partial = false;

        PositionScrollVisibility topVisibility = checkTopScrollVisibility(rowPosition);
        if (topVisibility == PositionScrollVisibility.NOT_VISIBLE) {
            return PositionScrollVisibility.NOT_VISIBLE;
        }
        partial |= topVisibility == PositionScrollVisibility.PARTIAL;

        PositionScrollVisibility bottomVisibility = checkBottomScrollVisibility(rowPosition, rowsPerPage, dataViewHeight, rowHeight);
        if (bottomVisibility == PositionScrollVisibility.NOT_VISIBLE) {
            return PositionScrollVisibility.NOT_VISIBLE;
        }
        partial |= bottomVisibility == PositionScrollVisibility.PARTIAL;

        PositionScrollVisibility leftVisibility = checkLeftScrollVisibility(charPosition);
        if (leftVisibility == PositionScrollVisibility.NOT_VISIBLE) {
            return PositionScrollVisibility.NOT_VISIBLE;
        }
        partial |= leftVisibility == PositionScrollVisibility.PARTIAL;

        PositionScrollVisibility rightVisibility = checkRightScrollVisibility(charPosition, charactersPerPage, dataViewWidth, characterWidth);
        if (rightVisibility == PositionScrollVisibility.NOT_VISIBLE) {
            return PositionScrollVisibility.NOT_VISIBLE;
        }
        partial |= rightVisibility == PositionScrollVisibility.PARTIAL;

        return partial ? PositionScrollVisibility.PARTIAL : PositionScrollVisibility.VISIBLE;
    }

    @Nullable
    public CodeAreaScrollPosition computeRevealScrollPosition(long rowPosition, int charPosition, int bytesPerRow, int previewCharPos, int rowsPerPage, int charactersPerPage, int dataViewWidth, int dataViewHeight, int characterWidth, int rowHeight) {
        CodeAreaScrollPosition targetScrollPosition = new CodeAreaScrollPosition();
        targetScrollPosition.setScrollPosition(scrollPosition);

        boolean scrolled = false;
        if (checkBottomScrollVisibility(rowPosition, rowsPerPage, dataViewHeight, rowHeight) != PositionScrollVisibility.VISIBLE) {
            int bottomRowOffset;
            if (verticalScrollUnit == VerticalScrollUnit.ROW) {
                bottomRowOffset = 0;
            } else {
                if (dataViewHeight < rowHeight) {
                    bottomRowOffset = 0;
                } else {
                    bottomRowOffset = rowHeight - (dataViewHeight % rowHeight);
                }
            }

            long targetRowPosition = rowPosition - rowsPerPage;
            if (verticalScrollUnit == VerticalScrollUnit.ROW && (dataViewHeight % rowHeight) > 0) {
                targetRowPosition++;
            }
            targetScrollPosition.setRowPosition(targetRowPosition);
            targetScrollPosition.setRowOffset(bottomRowOffset);
            scrolled = true;
        }

        if (checkTopScrollVisibility(rowPosition) != PositionScrollVisibility.VISIBLE) {
            targetScrollPosition.setRowPosition(rowPosition);
            targetScrollPosition.setRowOffset(0);
            scrolled = true;
        }

        if (checkRightScrollVisibility(charPosition, charactersPerPage, dataViewWidth, characterWidth) != PositionScrollVisibility.VISIBLE) {
            int rightCharOffset;
            if (horizontalScrollUnit == HorizontalScrollUnit.CHARACTER) {
                rightCharOffset = 0;
            } else {
                if (dataViewWidth < characterWidth) {
                    rightCharOffset = 0;
                } else {
                    rightCharOffset = characterWidth - (dataViewWidth % characterWidth);
                }
            }

            // Scroll character right
            targetScrollPosition.setCharPosition(charPosition - charactersPerPage);
            targetScrollPosition.setCharOffset(rightCharOffset);
            scrolled = true;
        }

        if (checkLeftScrollVisibility(charPosition) != PositionScrollVisibility.VISIBLE) {
            targetScrollPosition.setCharPosition(charPosition);
            targetScrollPosition.setCharOffset(0);
            scrolled = true;
        }

        return scrolled ? targetScrollPosition : null;
    }

    @Nonnull
    private PositionScrollVisibility checkTopScrollVisibility(long rowPosition) {
        if (verticalScrollUnit == VerticalScrollUnit.ROW) {
            return rowPosition < scrollPosition.getRowPosition() ? PositionScrollVisibility.NOT_VISIBLE : PositionScrollVisibility.VISIBLE;
        }

        if (rowPosition > scrollPosition.getRowPosition() || (rowPosition == scrollPosition.getRowPosition() && scrollPosition.getRowOffset() == 0)) {
            return PositionScrollVisibility.VISIBLE;
        }
        if (rowPosition == scrollPosition.getRowPosition() && scrollPosition.getRowOffset() > 0) {
            return PositionScrollVisibility.PARTIAL;
        }

        return PositionScrollVisibility.NOT_VISIBLE;
    }

    @Nonnull
    private PositionScrollVisibility checkBottomScrollVisibility(long rowPosition, int rowsPerPage, int dataViewHeight, int rowHeight) {
        int rowOffset = dataViewHeight % rowHeight;
        int sumOffset = scrollPosition.getRowOffset() + rowOffset;

        long lastFullRow = scrollPosition.getRowPosition() + rowsPerPage;
        if (rowOffset > 0) {
            lastFullRow--;
        }
        if (sumOffset >= rowHeight) {
            lastFullRow++;
        }

        if (rowPosition <= lastFullRow) {
            return PositionScrollVisibility.VISIBLE;
        }
        if (sumOffset > 0 && sumOffset != rowHeight && rowPosition == lastFullRow + 1) {
            return PositionScrollVisibility.PARTIAL;
        }

        return PositionScrollVisibility.NOT_VISIBLE;
    }

    @Nonnull
    private PositionScrollVisibility checkLeftScrollVisibility(int charPosition) {
        if (horizontalScrollUnit == HorizontalScrollUnit.CHARACTER) {
            return charPosition < scrollPosition.getCharPosition() ? PositionScrollVisibility.NOT_VISIBLE : PositionScrollVisibility.VISIBLE;
        }

        if (charPosition > scrollPosition.getCharPosition() || (charPosition == scrollPosition.getCharPosition() && scrollPosition.getCharOffset() == 0)) {
            return PositionScrollVisibility.VISIBLE;
        }
        if (charPosition == scrollPosition.getCharPosition() && scrollPosition.getCharOffset() > 0) {
            return PositionScrollVisibility.PARTIAL;
        }

        return PositionScrollVisibility.NOT_VISIBLE;
    }

    @Nonnull
    private PositionScrollVisibility checkRightScrollVisibility(int charPosition, int charactersPerPage, int dataViewWidth, int characterWidth) {
        int charOffset = dataViewWidth % characterWidth;
        int sumOffset = scrollPosition.getCharOffset() + charOffset;

        long lastFullChar = scrollPosition.getCharPosition() + charactersPerPage;
        if (charOffset > 0) {
            lastFullChar--;
        }
        if (sumOffset >= characterWidth) {
            lastFullChar++;
        }

        if (charPosition <= lastFullChar) {
            return PositionScrollVisibility.VISIBLE;
        }
        if (sumOffset > 0 && sumOffset != characterWidth && charPosition == lastFullChar + 1) {
            return PositionScrollVisibility.PARTIAL;
        }

        return PositionScrollVisibility.NOT_VISIBLE;
    }

    public CodeAreaScrollPosition computeCenterOnScrollPosition(long rowPosition, int charPosition, int bytesPerRow, int previewCharPos, int rowsPerRect, int charactersPerRect, int dataViewWidth, int dataViewHeight, int characterWidth, int rowHeight) {
        CodeAreaScrollPosition targetScrollPosition = new CodeAreaScrollPosition();
        targetScrollPosition.setScrollPosition(scrollPosition);

        long centerRowPosition = rowPosition - rowsPerRect / 2;
        int rowCorrection = (rowsPerRect & 1) == 0 ? rowHeight : 0;
        int heightDiff = (rowsPerRect * rowHeight + rowCorrection - dataViewHeight) / 2;
        int rowOffset;
        if (verticalScrollUnit == VerticalScrollUnit.ROW) {
            rowOffset = 0;
        } else {
            if (heightDiff > 0) {
                rowOffset = heightDiff;
            } else {
                rowOffset = 0;
            }
        }
        if (centerRowPosition < 0) {
            centerRowPosition = 0;
            rowOffset = 0;
        } else if (centerRowPosition > maximumScrollPosition.getRowPosition() || (centerRowPosition == maximumScrollPosition.getRowPosition() && rowOffset > maximumScrollPosition.getRowOffset())) {
            centerRowPosition = maximumScrollPosition.getRowPosition();
            rowOffset = maximumScrollPosition.getRowOffset();
        }
        targetScrollPosition.setRowPosition(centerRowPosition);
        targetScrollPosition.setRowOffset(rowOffset);

        int centerCharPosition = charPosition - charactersPerRect / 2;
        int charCorrection = (charactersPerRect & 1) == 0 ? rowHeight : 0;
        int widthDiff = (charactersPerRect * characterWidth + charCorrection - dataViewWidth) / 2;
        int charOffset;
        if (horizontalScrollUnit == HorizontalScrollUnit.CHARACTER) {
            charOffset = 0;
        } else {
            if (widthDiff > 0) {
                charOffset = widthDiff;
            } else {
                charOffset = 0;
            }
        }
        if (centerCharPosition < 0) {
            centerCharPosition = 0;
            charOffset = 0;
        } else if (centerCharPosition > maximumScrollPosition.getCharPosition() || (centerCharPosition == maximumScrollPosition.getCharPosition() && charOffset > maximumScrollPosition.getCharOffset())) {
            centerCharPosition = maximumScrollPosition.getCharPosition();
            charOffset = maximumScrollPosition.getCharOffset();
        }
        targetScrollPosition.setCharPosition(centerCharPosition);
        targetScrollPosition.setCharOffset(charOffset);
        return targetScrollPosition;
    }

    @Nonnull
    public void updateMaximumScrollPosition(long rowsPerDocument, int rowsPerPage, int charactersPerRow, int charactersPerPage, int lastCharOffset, int lastRowOffset) {
        maximumScrollPosition.reset();
        if (rowsPerDocument > rowsPerPage) {
            maximumScrollPosition.setRowPosition(rowsPerDocument - rowsPerPage);
        }
        if (verticalScrollUnit == VerticalScrollUnit.PIXEL) {
            maximumScrollPosition.setRowOffset(lastRowOffset);
        }

        if (charactersPerRow > charactersPerPage) {
            maximumScrollPosition.setCharPosition(charactersPerRow - charactersPerPage);
        }
        if (horizontalScrollUnit == HorizontalScrollUnit.PIXEL) {
            maximumScrollPosition.setCharOffset(lastCharOffset);
        }
    }

    @Nonnull
    public CodeAreaScrollPosition getScrollPosition() {
        return scrollPosition;
    }

    public void setScrollPosition(@Nonnull CodeAreaScrollPosition scrollPosition) {
        this.scrollPosition.setScrollPosition(scrollPosition);
    }

    @Nonnull
    public ScrollBarVerticalScale getScrollBarVerticalScale() {
        return scrollBarVerticalScale;
    }

    public void setScrollBarVerticalScale(ScrollBarVerticalScale scrollBarVerticalScale) {
        this.scrollBarVerticalScale = scrollBarVerticalScale;
    }

    @Nonnull
    public VerticalScrollUnit getVerticalScrollUnit() {
        return verticalScrollUnit;
    }

    @Nonnull
    public ScrollBarVisibility getVerticalScrollBarVisibility() {
        return verticalScrollBarVisibility;
    }

    @Nonnull
    public HorizontalScrollUnit getHorizontalScrollUnit() {
        return horizontalScrollUnit;
    }

    @Nonnull
    public ScrollBarVisibility getHorizontalScrollBarVisibility() {
        return horizontalScrollBarVisibility;
    }

    @Nonnull
    public CodeAreaScrollPosition getMaximumScrollPosition() {
        return maximumScrollPosition;
    }
}
