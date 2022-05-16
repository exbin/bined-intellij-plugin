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
package org.exbin.framework.bined.service.impl;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.bined.highlight.swing.extended.ExtendedHighlightNonAsciiCodeAreaPainter;
import org.exbin.bined.swing.extended.ExtCodeArea;
import org.exbin.framework.bined.ReplaceParameters;
import org.exbin.framework.bined.SearchCondition;
import org.exbin.framework.bined.SearchParameters;
import org.exbin.framework.bined.service.BinarySearchService;
import org.exbin.auxiliary.paged_data.BinaryData;
import org.exbin.auxiliary.paged_data.EditableBinaryData;
import org.exbin.bined.CodeAreaUtils;

/**
 * Binary search service.
 *
 * @version 0.2.1 2021/02/21
 * @author ExBin Project (http://exbin.org)
 */
@ParametersAreNonnullByDefault
public class BinarySearchServiceImpl implements BinarySearchService {

    private final ExtCodeArea codeArea;

    public BinarySearchServiceImpl(ExtCodeArea codeArea) {
        this.codeArea = codeArea;
    }

    @Override
    public void performFind(SearchParameters searchParameters, SearchStatusListener searchStatusListener) {
        ExtendedHighlightNonAsciiCodeAreaPainter painter = (ExtendedHighlightNonAsciiCodeAreaPainter) codeArea.getPainter();
        SearchCondition condition = searchParameters.getCondition();
        searchStatusListener.clearStatus();
        if (condition.isEmpty()) {
            painter.clearMatches();
            codeArea.repaint();
            return;
        }

        long position;
        if (searchParameters.isSearchFromCursor()) {
            position = codeArea.getCaretPosition().getDataPosition();
        } else {
            switch (searchParameters.getSearchDirection()) {
                case FORWARD: {
                    position = 0;
                    break;
                }
                case BACKWARD: {
                    position = codeArea.getDataSize() - 1;
                    break;
                }
                default:
                    throw CodeAreaUtils.getInvalidTypeException(searchParameters.getSearchDirection());
            }
        }
        searchParameters.setStartPosition(position);

        switch (condition.getSearchMode()) {
            case TEXT: {
                searchForText(searchParameters, searchStatusListener);
                break;
            }
            case BINARY: {
                searchForBinaryData(searchParameters, searchStatusListener);
                break;
            }
            default:
                throw CodeAreaUtils.getInvalidTypeException(condition.getSearchMode());
        }
    }

    /**
     * Performs search by binary data.
     */
    private void searchForBinaryData(SearchParameters searchParameters, SearchStatusListener searchStatusListener) {
        ExtendedHighlightNonAsciiCodeAreaPainter painter = (ExtendedHighlightNonAsciiCodeAreaPainter) codeArea.getPainter();
        SearchCondition condition = searchParameters.getCondition();
        long position = codeArea.getCaretPosition().getDataPosition();
        ExtendedHighlightNonAsciiCodeAreaPainter.SearchMatch currentMatch = painter.getCurrentMatch();

        if (currentMatch != null) {
            if (currentMatch.getPosition() == position) {
                position++;
            }
            painter.clearMatches();
        } else if (!searchParameters.isSearchFromCursor()) {
            position = 0;
        }

        BinaryData searchData = condition.getBinaryData();
        BinaryData data = codeArea.getContentData();

        List<ExtendedHighlightNonAsciiCodeAreaPainter.SearchMatch> foundMatches = new ArrayList<>();

        long dataSize = data.getDataSize();
        while (position < dataSize - searchData.getDataSize()) {
            int matchLength = 0;
            while (matchLength < searchData.getDataSize()) {
                if (data.getByte(position + matchLength) != searchData.getByte(matchLength)) {
                    break;
                }
                matchLength++;
            }

            if (matchLength == searchData.getDataSize()) {
                ExtendedHighlightNonAsciiCodeAreaPainter.SearchMatch match = new ExtendedHighlightNonAsciiCodeAreaPainter.SearchMatch();
                match.setPosition(position);
                match.setLength(searchData.getDataSize());
                foundMatches.add(match);

                if (foundMatches.size() == 100 || !searchParameters.isMultipleMatches()) {
                    break;
                }
            }

            position++;
        }

        painter.setMatches(foundMatches);
        if (!foundMatches.isEmpty()) {
            painter.setCurrentMatchIndex(0);
            ExtendedHighlightNonAsciiCodeAreaPainter.SearchMatch firstMatch = Objects.requireNonNull(painter.getCurrentMatch());
            codeArea.revealPosition(firstMatch.getPosition(), 0, codeArea.getActiveSection());
        }
        searchStatusListener.setStatus(new FoundMatches(foundMatches.size(), foundMatches.isEmpty() ? -1 : 0));
        codeArea.repaint();
    }

    /**
     * Performs search by text/characters.
     */
    private void searchForText(SearchParameters searchParameters, SearchStatusListener searchStatusListener) {
        ExtendedHighlightNonAsciiCodeAreaPainter painter = (ExtendedHighlightNonAsciiCodeAreaPainter) codeArea.getPainter();
        SearchCondition condition = searchParameters.getCondition();

        long position = searchParameters.getStartPosition();
        String findText;
        if (searchParameters.isMatchCase()) {
            findText = condition.getSearchText();
        } else {
            findText = condition.getSearchText().toLowerCase();
        }
        BinaryData data = codeArea.getContentData();

        List<ExtendedHighlightNonAsciiCodeAreaPainter.SearchMatch> foundMatches = new ArrayList<>();

        Charset charset = codeArea.getCharset();
        CharsetEncoder encoder = charset.newEncoder();
        int maxBytesPerChar = (int) encoder.maxBytesPerChar();
        byte[] charData = new byte[maxBytesPerChar];
        long dataSize = data.getDataSize();
        while (position <= dataSize - findText.length()) {
            int matchCharLength = 0;
            int matchLength = 0;
            while (matchCharLength < findText.length()) {
                long searchPosition = position + matchLength;
                int bytesToUse = maxBytesPerChar;
                if (searchPosition + bytesToUse > dataSize) {
                    bytesToUse = (int) (dataSize - searchPosition);
                }
                data.copyToArray(searchPosition, charData, 0, bytesToUse);
                char singleChar = new String(charData, charset).charAt(0);
                String singleCharString = String.valueOf(singleChar);
                int characterLength = singleCharString.getBytes(charset).length;

                if (searchParameters.isMatchCase()) {
                    if (singleChar != findText.charAt(matchCharLength)) {
                        break;
                    }
                } else if (singleCharString.toLowerCase().charAt(0) != findText.charAt(matchCharLength)) {
                    break;
                }
                matchCharLength++;
                matchLength += characterLength;
            }

            if (matchCharLength == findText.length()) {
                ExtendedHighlightNonAsciiCodeAreaPainter.SearchMatch match = new ExtendedHighlightNonAsciiCodeAreaPainter.SearchMatch();
                match.setPosition(position);
                match.setLength(matchLength);
                foundMatches.add(match);

                if (foundMatches.size() == 100 || !searchParameters.isMultipleMatches()) {
                    break;
                }
            }

            switch (searchParameters.getSearchDirection()) {
                case FORWARD: {
                    position++;
                    break;
                }
                case BACKWARD: {
                    position--;
                    break;
                }
                default:
                    throw CodeAreaUtils.getInvalidTypeException(searchParameters.getSearchDirection());
            }
        }

        painter.setMatches(foundMatches);
        if (!foundMatches.isEmpty()) {
            painter.setCurrentMatchIndex(0);
            ExtendedHighlightNonAsciiCodeAreaPainter.SearchMatch firstMatch = painter.getCurrentMatch();
            codeArea.revealPosition(firstMatch.getPosition(), 0, codeArea.getActiveSection());
        }
        searchStatusListener.setStatus(new FoundMatches(foundMatches.size(), foundMatches.isEmpty() ? -1 : 0));
        codeArea.repaint();
    }

    @Override
    public void setMatchPosition(int matchPosition) {
        ExtendedHighlightNonAsciiCodeAreaPainter painter = (ExtendedHighlightNonAsciiCodeAreaPainter) codeArea.getPainter();
        painter.setCurrentMatchIndex(matchPosition);
        ExtendedHighlightNonAsciiCodeAreaPainter.SearchMatch currentMatch = painter.getCurrentMatch();
        codeArea.revealPosition(currentMatch.getPosition(), 0, codeArea.getActiveSection());
        codeArea.repaint();
    }

    @Override
    public void performReplace(SearchParameters searchParameters, ReplaceParameters replaceParameters) {
        SearchCondition replaceCondition = replaceParameters.getCondition();
        ExtendedHighlightNonAsciiCodeAreaPainter painter = (ExtendedHighlightNonAsciiCodeAreaPainter) codeArea.getPainter();
        ExtendedHighlightNonAsciiCodeAreaPainter.SearchMatch currentMatch = painter.getCurrentMatch();
        if (currentMatch != null) {
            EditableBinaryData editableData = ((EditableBinaryData) codeArea.getContentData());
            editableData.remove(currentMatch.getPosition(), currentMatch.getLength());
            if (replaceCondition.getSearchMode() == SearchCondition.SearchMode.BINARY) {
                editableData.insert(currentMatch.getPosition(), replaceCondition.getBinaryData());
            } else {
                editableData.insert(currentMatch.getPosition(), replaceCondition.getSearchText().getBytes(codeArea.getCharset()));
            }
            painter.getMatches().remove(currentMatch);
            codeArea.repaint();
        }
    }

    @Override
    public void clearMatches() {
        ExtendedHighlightNonAsciiCodeAreaPainter painter = (ExtendedHighlightNonAsciiCodeAreaPainter) codeArea.getPainter();
        painter.clearMatches();
    }
}
