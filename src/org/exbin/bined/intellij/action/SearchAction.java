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
package org.exbin.bined.intellij.action;

import org.exbin.bined.DefaultCodeAreaCaretPosition;
import org.exbin.bined.highlight.swing.extended.ExtendedHighlightCodeAreaPainter;
import org.exbin.bined.intellij.gui.BinarySearchPanel;
import org.exbin.bined.intellij.gui.BinarySearchPanelApi;
import org.exbin.bined.swing.extended.ExtCodeArea;
import org.exbin.framework.bined.ReplaceParameters;
import org.exbin.framework.bined.SearchCondition;
import org.exbin.framework.bined.SearchParameters;
import org.exbin.framework.bined.handler.CodeAreaPopupMenuHandler;
import org.exbin.framework.gui.utils.ActionUtils;
import org.exbin.auxiliary.paged_data.BinaryData;
import org.exbin.auxiliary.paged_data.EditableBinaryData;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Search action.
 *
 * @version 0.2.1 2019/07/21
 * @author ExBin Project (http://exbin.org)
 */
@ParametersAreNonnullByDefault
public final class SearchAction implements ActionListener {

    private static final int FIND_MATCHES_LIMIT = 100;

    private boolean findTextPanelVisible = false;
    private BinarySearchPanel binarySearchPanel = null;
    private final JPanel codeAreaPanel;
    private final ExtCodeArea codeArea;

    public SearchAction(ExtCodeArea codeArea, JPanel codeAreaPanel) {
        this.codeArea = codeArea;
        this.codeAreaPanel = codeAreaPanel;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (binarySearchPanel == null) {
            binarySearchPanel = new BinarySearchPanel(new BinarySearchPanelApi() {
                @Override
                public void performFind(SearchParameters searchParameters) {
                    ExtendedHighlightCodeAreaPainter painter = (ExtendedHighlightCodeAreaPainter) codeArea.getPainter();
                    SearchCondition condition = searchParameters.getCondition();
                    binarySearchPanel.clearStatus();
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
                                throw new IllegalStateException("Illegal search type " + searchParameters.getSearchDirection().name());
                        }
                    }
                    searchParameters.setStartPosition(position);

                    switch (condition.getSearchMode()) {
                        case TEXT: {
                            searchForText(searchParameters);
                            break;
                        }
                        case BINARY: {
                            searchForBinaryData(searchParameters);
                            break;
                        }
                        default:
                            throw new IllegalStateException("Unexpected search mode " + condition.getSearchMode().name());
                    }
                }

                @Override
                public void setMatchPosition(int matchPosition) {
                    ExtendedHighlightCodeAreaPainter painter = (ExtendedHighlightCodeAreaPainter) codeArea.getPainter();
                    painter.setCurrentMatchIndex(matchPosition);
                    ExtendedHighlightCodeAreaPainter.SearchMatch currentMatch = painter.getCurrentMatch();
                    codeArea.revealPosition(new DefaultCodeAreaCaretPosition(currentMatch.getPosition(), 0, codeArea.getActiveSection()));
                    codeArea.repaint();
                }

                @Override
                public void updatePosition() {
                    binarySearchPanel.updatePosition(codeArea.getCaretPosition().getDataPosition(), codeArea.getDataSize());
                }

                @Override
                public void performReplace(SearchParameters searchParameters, ReplaceParameters replaceParameters) {
                    SearchCondition replaceCondition = replaceParameters.getCondition();
                    ExtendedHighlightCodeAreaPainter painter = (ExtendedHighlightCodeAreaPainter) codeArea.getPainter();
                    ExtendedHighlightCodeAreaPainter.SearchMatch currentMatch = painter.getCurrentMatch();
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
                    ExtendedHighlightCodeAreaPainter painter = (ExtendedHighlightCodeAreaPainter) codeArea.getPainter();
                    painter.clearMatches();
                }
            });
            binarySearchPanel.setBinaryCodePopupMenuHandler(new CodeAreaPopupMenuHandler() {
                @Override
                public JPopupMenu createPopupMenu(ExtCodeArea codeArea, String menuPostfix, int x, int y) {
                    return createCodeAreaPopupMenu(codeArea, menuPostfix);
                }

                @Override
                public void dropPopupMenu(String menuPostfix) {
                }
            });
            binarySearchPanel.setClosePanelListener(this::hideSearchPanel);
        }

        if (!findTextPanelVisible) {
            codeAreaPanel.add(binarySearchPanel, BorderLayout.NORTH);
            codeAreaPanel.revalidate();
//            revalidate();
            findTextPanelVisible = true;
            binarySearchPanel.requestSearchFocus();
        }
    }
    
    public void switchReplaceMode(BinarySearchPanel.SearchOperation searchOperation) {
        binarySearchPanel.switchReplaceMode(searchOperation);
    }

    public void hideSearchPanel() {
        if (findTextPanelVisible) {
            binarySearchPanel.cancelSearch();
            binarySearchPanel.clearSearch();
            codeAreaPanel.remove(binarySearchPanel);
            codeAreaPanel.revalidate();
//            revalidate();
            findTextPanelVisible = false;
        }
    }

    /**
     * Performs search by text/characters.
     */
    private void searchForText(SearchParameters searchParameters) {
        ExtendedHighlightCodeAreaPainter painter = (ExtendedHighlightCodeAreaPainter) codeArea.getPainter();
        SearchCondition condition = searchParameters.getCondition();

        long position = searchParameters.getStartPosition();
        String findText;
        if (searchParameters.isMatchCase()) {
            findText = condition.getSearchText();
        } else {
            findText = condition.getSearchText().toLowerCase();
        }
        BinaryData data = codeArea.getContentData();

        List<ExtendedHighlightCodeAreaPainter.SearchMatch> foundMatches = new ArrayList<>();

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
                ExtendedHighlightCodeAreaPainter.SearchMatch match = new ExtendedHighlightCodeAreaPainter.SearchMatch();
                match.setPosition(position);
                match.setLength(matchLength);
                foundMatches.add(match);

                if (foundMatches.size() == FIND_MATCHES_LIMIT || !searchParameters.isMultipleMatches()) {
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
                    throw new IllegalStateException("Illegal search type " + searchParameters.getSearchDirection().name());
            }
        }

        painter.setMatches(foundMatches);
        if (foundMatches.size() > 0) {
            painter.setCurrentMatchIndex(0);
            ExtendedHighlightCodeAreaPainter.SearchMatch firstMatch = painter.getCurrentMatch();
            codeArea.revealPosition(new DefaultCodeAreaCaretPosition(firstMatch.getPosition(), 0, codeArea.getActiveSection()));
        }
        binarySearchPanel.setStatus(foundMatches.size(), foundMatches.isEmpty() ? -1 : 0);
        codeArea.repaint();
    }

    /**
     * Performs search by binary data.
     */
    private void searchForBinaryData(SearchParameters searchParameters) {
        ExtendedHighlightCodeAreaPainter painter = (ExtendedHighlightCodeAreaPainter) codeArea.getPainter();
        SearchCondition condition = searchParameters.getCondition();
        long position = codeArea.getCaretPosition().getDataPosition();
        ExtendedHighlightCodeAreaPainter.SearchMatch currentMatch = painter.getCurrentMatch();

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

        List<ExtendedHighlightCodeAreaPainter.SearchMatch> foundMatches = new ArrayList<>();

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
                ExtendedHighlightCodeAreaPainter.SearchMatch match = new ExtendedHighlightCodeAreaPainter.SearchMatch();
                match.setPosition(position);
                match.setLength(searchData.getDataSize());
                foundMatches.add(match);

                if (foundMatches.size() == FIND_MATCHES_LIMIT || !searchParameters.isMultipleMatches()) {
                    break;
                }
            }

            position++;
        }

        painter.setMatches(foundMatches);
        if (foundMatches.size() > 0) {
            painter.setCurrentMatchIndex(0);
            ExtendedHighlightCodeAreaPainter.SearchMatch firstMatch = painter.getCurrentMatch();
            codeArea.revealPosition(new DefaultCodeAreaCaretPosition(firstMatch.getPosition(), 0, codeArea.getActiveSection()));
        }
        binarySearchPanel.setStatus(foundMatches.size(), foundMatches.isEmpty() ? -1 : 0);
        codeArea.repaint();
    }

    public void codeAreaDataChanged() {
        if (binarySearchPanel != null && binarySearchPanel.isVisible()) {
            binarySearchPanel.dataChanged();
        }
    }

    private JPopupMenu createCodeAreaPopupMenu(final ExtCodeArea codeArea, String menuPostfix) {
        JPopupMenu popupMenu = new JPopupMenu();

        JMenuItem cutMenuItem = new JMenuItem(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                codeArea.cut();
            }

            @Override
            public boolean isEnabled() {
                return codeArea.hasSelection();
            }
        });
        cutMenuItem.setText("Cut");
        cutMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionUtils.getMetaMask()));
        popupMenu.add(cutMenuItem);
        JMenuItem copyMenuItem = new JMenuItem(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                codeArea.copy();
            }

            @Override
            public boolean isEnabled() {
                return codeArea.hasSelection();
            }
        });
        copyMenuItem.setText("Copy");
        copyMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionUtils.getMetaMask()));
        popupMenu.add(copyMenuItem);
        JMenuItem pasteMenuItem = new JMenuItem(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                codeArea.paste();
            }

            @Override
            public boolean isEnabled() {
                return codeArea.canPaste();
            }
        });
        pasteMenuItem.setText("Paste");
        pasteMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionUtils.getMetaMask()));
        popupMenu.add(pasteMenuItem);
        JMenuItem deleteMenuItem = new JMenuItem(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                codeArea.delete();
            }

            @Override
            public boolean isEnabled() {
                return codeArea.hasSelection();
            }
        });
        deleteMenuItem.setText("Delete");
        deleteMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
        popupMenu.add(deleteMenuItem);
        JMenuItem selectAllMenuItem = new JMenuItem(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                codeArea.selectAll();
            }
        });
        selectAllMenuItem.setText("Select All");
        selectAllMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionUtils.getMetaMask()));
        popupMenu.add(selectAllMenuItem);

        return popupMenu;
    }
}
