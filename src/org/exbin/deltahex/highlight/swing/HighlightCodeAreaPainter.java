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
package org.exbin.deltahex.highlight.swing;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;
import org.exbin.deltahex.swing.ColorsGroup;
import org.exbin.deltahex.Section;
import org.exbin.deltahex.swing.DefaultCodeAreaPainter;
import org.exbin.deltahex.swing.CodeArea;

/**
 * Hexadecimal component painter supporting search matches highlighting.
 *
 * @version 0.1.0 2016/06/26
 * @author ExBin Project (http://exbin.org)
 */
public class HighlightCodeAreaPainter extends DefaultCodeAreaPainter {

    /**
     * Matches must be ordered by position.
     */
    private final List<SearchMatch> matches = new ArrayList<>();
    private int currentMatchIndex = -1;
    private int matchIndex = 0;
    private long matchPosition = -1;

    private ColorsGroup foundMatchesColors;
    private ColorsGroup currentMatchColors;

    public HighlightCodeAreaPainter(CodeArea codeArea) {
        super(codeArea);

        foundMatchesColors = codeArea.getMainColors();
        foundMatchesColors.setBackgroundColor(new Color(180, 255, 180));
        currentMatchColors = codeArea.getMainColors();
        currentMatchColors.setBackgroundColor(new Color(255, 210, 180));
    }

    @Override
    public void paintMainArea(Graphics g) {
        matchIndex = 0;
        super.paintMainArea(g);
    }

    @Override
    public Color getPositionColor(int byteOnLine, int charOnLine, Section section, ColorsGroup.ColorType colorType, PaintData paintData) {
        if (!matches.isEmpty() && section == Section.TEXT_PREVIEW || charOnLine < paintData.getCharsPerLine() - 1) {
            long dataPosition = paintData.getLineDataPosition() + byteOnLine;
            if (currentMatchIndex >= 0) {
                SearchMatch currentMatch = matches.get(currentMatchIndex);
                if (dataPosition >= currentMatch.position && dataPosition < currentMatch.position + currentMatch.length
                        && (section == Section.TEXT_PREVIEW || charOnLine != ((currentMatch.position + currentMatch.length) - paintData.getLineDataPosition()) * paintData.getCharsPerLine() - 1)) {
                    return currentMatchColors.getColor(colorType);
                }
            }

            if (matchPosition < paintData.getLineDataPosition()) {
                matchIndex = 0;
            }
            int lineMatchIndex = matchIndex;
            while (lineMatchIndex < matches.size()) {
                SearchMatch match = matches.get(lineMatchIndex);
                if (dataPosition >= match.position && dataPosition < match.position + match.length
                        && (section == Section.TEXT_PREVIEW || charOnLine != ((match.position + match.length) - paintData.getLineDataPosition()) * paintData.getCharsPerLine() - 1)) {
                    if (byteOnLine == 0) {
                        matchIndex = lineMatchIndex;
                        matchPosition = match.position;
                    }
                    return foundMatchesColors.getColor(colorType);
                }

                if (match.position > dataPosition) {
                    break;
                }

                if (byteOnLine == 0) {
                    matchIndex = lineMatchIndex;
                    matchPosition = match.position;
                }
                lineMatchIndex++;
            }
        }

        return super.getPositionColor(byteOnLine, charOnLine, section, colorType, paintData);
    }

    public List<SearchMatch> getMatches() {
        return matches;
    }

    public void setMatches(List<SearchMatch> matches) {
        this.matches.clear();
        this.matches.addAll(matches);
        currentMatchIndex = -1;
    }

    public void clearMatches() {
        this.matches.clear();
        currentMatchIndex = -1;
    }

    public SearchMatch getCurrentMatch() {
        if (currentMatchIndex >= 0) {
            return matches.get(currentMatchIndex);
        }

        return null;
    }

    public int getCurrentMatchIndex() {
        return currentMatchIndex;
    }

    public void setCurrentMatchIndex(int currentMatchIndex) {
        this.currentMatchIndex = currentMatchIndex;
    }

    public Color getFoundMatchesBackgroundColor() {
        return foundMatchesColors.getBackgroundColor();
    }

    public void setFoundMatchesBackgroundColor(Color foundMatchesBackgroundColor) {
        this.foundMatchesColors.setBackgroundColor(foundMatchesBackgroundColor);
    }

    public Color getCurrentMatchBackgroundColor() {
        return currentMatchColors.getBackgroundColor();
    }

    public void setCurrentMatchBackgroundColor(Color currentMatchBackgroundColor) {
        this.currentMatchColors.setBackgroundColor(currentMatchBackgroundColor);
    }

    /**
     * Simple POJO class for search match.
     */
    public static class SearchMatch {

        private long position;
        private long length;

        public SearchMatch() {
        }

        public SearchMatch(long position, long length) {
            this.position = position;
            this.length = length;
        }

        public long getPosition() {
            return position;
        }

        public void setPosition(long position) {
            this.position = position;
        }

        public long getLength() {
            return length;
        }

        public void setLength(long length) {
            this.length = length;
        }
    }
}
