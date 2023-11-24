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
package org.exbin.framework.bined;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.bined.CodeAreaSection;
import org.exbin.bined.CodeAreaSelection;
import org.exbin.bined.capability.SelectionCapable;
import org.exbin.bined.highlight.swing.extended.ExtendedHighlightNonAsciiCodeAreaPainter;
import org.exbin.bined.swing.CodeAreaCore;

/**
 * Specific painter for binary editor.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class BinEdCodeAreaPainter extends ExtendedHighlightNonAsciiCodeAreaPainter {

    private final List<PositionColorModifier> priorityColorModifiers = new ArrayList<>();
    private final List<PositionColorModifier> colorModifiers = new ArrayList<>();

    public BinEdCodeAreaPainter(CodeAreaCore codeArea) {
        super(codeArea);
    }

    public void addColorModifier(PositionColorModifier colorModifier) {
        colorModifiers.add(colorModifier);
    }

    public void removeColorModifier(PositionColorModifier colorModifier) {
        colorModifiers.remove(colorModifier);
    }

    public void addPriorityColorModifier(PositionColorModifier colorModifier) {
        priorityColorModifiers.add(colorModifier);
    }

    public void removePriorityColorModifier(PositionColorModifier colorModifier) {
        priorityColorModifiers.remove(colorModifier);
    }

    @Nullable
    @Override
    public Color getPositionBackgroundColor(long rowDataPosition, int byteOnRow, int charOnRow, CodeAreaSection section, boolean unprintables) {
        CodeAreaSelection selectionHandler = ((SelectionCapable) codeArea).getSelectionHandler();
        boolean inSelection = selectionHandler.isInSelection(rowDataPosition + byteOnRow);

        for (PositionColorModifier colorModifier : priorityColorModifiers) {
            Color positionBackgroundColor = colorModifier.getPositionBackgroundColor(rowDataPosition, byteOnRow, charOnRow, section, unprintables);
            if (positionBackgroundColor != null) {
                return positionBackgroundColor;
            }
        }

        if (!inSelection) {
            for (PositionColorModifier colorModifier : colorModifiers) {
                Color positionBackgroundColor = colorModifier.getPositionBackgroundColor(rowDataPosition, byteOnRow, charOnRow, section, unprintables);
                if (positionBackgroundColor != null) {
                    return positionBackgroundColor;
                }
            }
        }

        Color color = super.getPositionBackgroundColor(rowDataPosition, byteOnRow, charOnRow, section, unprintables);
//        if (color == null || inSelection) {
//            long dataPosition = rowDataPosition + byteOnRow;
//            if (dataPosition > 100 && dataPosition < 300) {
//                if (inSelection && color != null) {
//                    return new Color(
//                            (((int) (dataPosition * 17) % 255) + color.getRed()) / 2,
//                            (((int) (dataPosition * 37) % 255) + color.getGreen()) / 2,
//                            (((int) (dataPosition * 13) % 255) + color.getBlue()) / 2);
//                }
//                return new Color((int) (dataPosition * 17) % 255, (int) (dataPosition * 37) % 255, (int) (dataPosition * 13) % 255);
//            }
//        }

        return color;
    }

    @Nullable
    @Override
    public Color getPositionTextColor(long rowDataPosition, int byteOnRow, int charOnRow, CodeAreaSection section, boolean unprintables) {
        CodeAreaSelection selectionHandler = ((SelectionCapable) codeArea).getSelectionHandler();
        boolean inSelection = selectionHandler.isInSelection(rowDataPosition + byteOnRow);

        for (PositionColorModifier colorModifier : priorityColorModifiers) {
            Color positionTextColor = colorModifier.getPositionTextColor(rowDataPosition, byteOnRow, charOnRow, section, unprintables);
            if (positionTextColor != null) {
                return positionTextColor;
            }
        }

        if (!inSelection) {
            for (PositionColorModifier colorModifier : colorModifiers) {
                Color positionTextColor = colorModifier.getPositionTextColor(rowDataPosition, byteOnRow, charOnRow, section, unprintables);
                if (positionTextColor != null) {
                    return positionTextColor;
                }
            }
        }

        return super.getPositionTextColor(rowDataPosition, byteOnRow, charOnRow, section, unprintables);
    }

    @Override
    public void resetColors() {
        super.resetColors();

        for (PositionColorModifier colorModifier : priorityColorModifiers) {
            colorModifier.resetColors();
        }

        for (PositionColorModifier colorModifier : colorModifiers) {
            colorModifier.resetColors();
        }
    }

    @ParametersAreNonnullByDefault
    public interface PositionColorModifier {

        @Nullable
        Color getPositionBackgroundColor(long rowDataPosition, int byteOnRow, int charOnRow, CodeAreaSection section, boolean unprintables);

        @Nullable
        Color getPositionTextColor(long rowDataPosition, int byteOnRow, int charOnRow, CodeAreaSection section, boolean unprintables);

        void resetColors();
    }
}
