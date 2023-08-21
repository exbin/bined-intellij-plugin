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
package org.exbin.bined.intellij.bookmarks;

import org.exbin.bined.basic.BasicCodeAreaZone;
import org.exbin.bined.intellij.bookmarks.action.AddBookmarkAction;
import org.exbin.bined.intellij.bookmarks.action.EditBookmarkAction;
import org.exbin.bined.intellij.bookmarks.action.ManageBookmarksAction;
import org.exbin.bined.intellij.gui.BinEdComponentPanel;
import org.exbin.bined.intellij.main.BinEdFileManager;
import org.exbin.bined.intellij.main.BinEdManager;
import org.exbin.bined.swing.extended.ExtCodeArea;
import org.exbin.framework.api.Preferences;
import org.exbin.bined.intellij.main.BinEdFileHandler;
import org.exbin.framework.bined.bookmarks.BookmarksPositionColorModifier;
import org.exbin.framework.bined.bookmarks.gui.BookmarksManagerPanel;
import org.exbin.framework.bined.bookmarks.model.BookmarkRecord;
import org.exbin.framework.bined.bookmarks.preferences.BookmarkPreferences;
import org.exbin.framework.utils.ActionUtils;
import org.exbin.framework.utils.LanguageUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.Icon;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Bookmarks manager.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class BookmarksManager {

    private final ResourceBundle resourceBundle = LanguageUtils.getResourceBundleByClass(BookmarksManager.class);

    private final List<BookmarkRecord> bookmarkRecords = new ArrayList<>();
    private BookmarkPreferences bookmarkPreferences;
    private BookmarksPositionColorModifier bookmarksPositionColorModifier;

    private ManageBookmarksAction manageBookmarksAction = new ManageBookmarksAction();
    private AddBookmarkAction addBookmarkAction = new AddBookmarkAction();
    private EditBookmarkAction editBookmarkAction = new EditBookmarkAction();
    private BinEdFileHandler activeFile = null;

    public BookmarksManager() {
    }

    public void init() {
        manageBookmarksAction.setBookmarksManager(this);

        BinEdManager binEdManager = BinEdManager.getInstance();
        BinEdFileManager fileManager = binEdManager.getFileManager();
        Preferences preferences = binEdManager.getPreferences().getPreferences();
        bookmarkPreferences = new BookmarkPreferences(preferences);
        loadBookmarkRecords();
        updateBookmarksMenu();
        bookmarksPositionColorModifier = new BookmarksPositionColorModifier(bookmarkRecords);
        binEdManager.getFileManager().addPainterColorModifier(bookmarksPositionColorModifier);

        fileManager.addBinEdComponentExtension(new BinEdFileManager.BinEdFileExtension() {
            @Nonnull
            @Override
            public Optional<BinEdComponentPanel.BinEdComponentExtension> createComponentExtension(BinEdComponentPanel component) {
                return Optional.empty();
            }

            @Override
            public void onPopupMenuCreation(final JPopupMenu popupMenu,
                    final ExtCodeArea codeArea, String menuPostfix, int x, int y) {
                BasicCodeAreaZone positionZone = codeArea.getPainter().getPositionZone(x, y);

                if (positionZone == BasicCodeAreaZone.TOP_LEFT_CORNER || positionZone == BasicCodeAreaZone.HEADER
                        || positionZone == BasicCodeAreaZone.ROW_POSITIONS) {
                    return;
                }

                // TODO: Change position
                popupMenu.add(createBookmarksPopupMenu());
            }
        });
        binEdManager.setBookmarksSupport(new BinEdManager.BookmarksSupport() {

            @Nonnull
            @Override
            public JMenu createBookmarksPopupMenu() {
                return BookmarksManager.this.createBookmarksPopupMenu();
            }

            @Override
            public void registerBookmarksComponentActions(JComponent component) {
                BookmarksManager.this.registerBookmarksComponentActions(component);
            }

            @Override
            public void setActiveFile(@Nullable BinEdFileHandler activeFile) {
                BookmarksManager.this.setActiveFile(activeFile);
            }
        });
    }

    private void loadBookmarkRecords() {
        int bookmarksCount = bookmarkPreferences.getBookmarksCount();
        for (int i = 0; i < bookmarksCount; i++) {
            BookmarkRecord bookmarkRecord = bookmarkPreferences.getBookmarkRecord(i);
            bookmarkRecords.add(bookmarkRecord);
        }
    }

    private void saveBookmarkRecords() {
        int bookmarksCount = bookmarkRecords.size();
        bookmarkPreferences.setBookmarksCount(bookmarksCount);
        for (int i = 0; i < bookmarksCount; i++) {
            bookmarkPreferences.setBookmarkRecord(i, bookmarkRecords.get(i));
        }
    }

    @Nonnull
    public List<BookmarkRecord> getBookmarkRecords() {
        return bookmarkRecords;
    }

    @Nonnull
    public ManageBookmarksAction getManageBookmarksAction() {
        return manageBookmarksAction;
    }

    public void setBookmarkRecords(List<BookmarkRecord> records) {
        bookmarkRecords.clear();
        bookmarkRecords.addAll(records);
        saveBookmarkRecords();
        bookmarksPositionColorModifier.notifyBookmarksChanged();
        updateBookmarksMenu();
    }

    public Optional<BinEdFileHandler> getActiveFile() {
        return Optional.ofNullable(activeFile);
    }

    public void setActiveFile(@Nullable BinEdFileHandler activeFile) {
        this.activeFile = activeFile;
    }

    @Nonnull
    public BookmarksManagerPanel createBookmarksManagerPanel() {
        final BookmarksManagerPanel bookmarksManagerPanel = new BookmarksManagerPanel();
        bookmarksManagerPanel.setControl(new BookmarksManagerPanel.Control() {
            @Override
            public void addRecord() {
                addBookmarkAction.actionPerformed(new ActionEvent(bookmarksManagerPanel, 0, "COMMAND", 0));
                Optional<BookmarkRecord> bookmarkRecord = addBookmarkAction.getBookmarkRecord();
                if (bookmarkRecord.isPresent()) {
                    List<BookmarkRecord> records = bookmarksManagerPanel.getBookmarkRecords();
                    records.add(bookmarkRecord.get());
                    bookmarksManagerPanel.setBookmarkRecords(records);
                }
            }

            @Override
            public void editRecord() {
                BookmarkRecord selectedRecord = bookmarksManagerPanel.getSelectedRecord();
                int selectedRow = bookmarksManagerPanel.getTable().getSelectedRow();
                editBookmarkAction.setBookmarkRecord(new BookmarkRecord(selectedRecord));
                editBookmarkAction.actionPerformed(new ActionEvent(bookmarksManagerPanel, 0, "COMMAND", 0));
                Optional<BookmarkRecord> bookmarkRecord = editBookmarkAction.getBookmarkRecord();
                if (bookmarkRecord.isPresent()) {
                    bookmarksManagerPanel.updateRecord(bookmarkRecord.get(), selectedRow);
                }
            }

            @Override
            public void removeRecord() {
                int[] selectedRows = bookmarksManagerPanel.getTable().getSelectedRows();
                Arrays.sort(selectedRows);
                List<BookmarkRecord> records = bookmarksManagerPanel.getBookmarkRecords();
                for (int i = selectedRows.length - 1; i >= 0; i--) {
                    records.remove(selectedRows[i]);
                }
                bookmarksManagerPanel.setBookmarkRecords(records);
            }

            @Override
            public void selectAll() {
                bookmarksManagerPanel.getTable().selectAll();
            }

            @Override
            public void moveUp() {
                JTable table = bookmarksManagerPanel.getTable();
                int[] selectedRows = table.getSelectedRows();
                Arrays.sort(selectedRows);
                List<BookmarkRecord> records = bookmarksManagerPanel.getBookmarkRecords();
                ListSelectionModel selectionModel = table.getSelectionModel();
                for (int i = 0; i < selectedRows.length; i++) {
                    int index = selectedRows[i];
                    selectionModel.removeSelectionInterval(index, index);
                    BookmarkRecord movedRecord = records.remove(index - 1);
                    records.add(index, movedRecord);
                    table.addRowSelectionInterval(index - 1, index - 1);
                }
                bookmarksManagerPanel.updateBookmarkRecords(records);
            }

            @Override
            public void moveDown() {
                JTable table = bookmarksManagerPanel.getTable();
                int[] selectedRows = table.getSelectedRows();
                Arrays.sort(selectedRows);
                List<BookmarkRecord> records = bookmarksManagerPanel.getBookmarkRecords();
                ListSelectionModel selectionModel = table.getSelectionModel();
                for (int i = selectedRows.length - 1; i >= 0; i--) {
                    int index = selectedRows[i];
                    selectionModel.removeSelectionInterval(index, index);
                    BookmarkRecord movedRecord = records.remove(index);
                    records.add(index + 1, movedRecord);
                    table.addRowSelectionInterval(index + 1, index + 1);
                }
                bookmarksManagerPanel.updateBookmarkRecords(records);
            }
        });

        return bookmarksManagerPanel;
    }

    @Nonnull
    public JMenu createBookmarksPopupMenu() {
        JMenu bookmarksPopupMenu = new JMenu(resourceBundle.getString("bookmarksMenu.text"));
        updateBookmarksMenu(bookmarksPopupMenu);
        return bookmarksPopupMenu;
    }

    public void registerBookmarksComponentActions(JComponent component) {
        ActionMap actionMap = component.getActionMap();
        InputMap inputMap = component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        int metaMask = ActionUtils.getMetaMask();
        for (int i = 0; i < 10; i++) {
            final int bookmarkIndex = i;
            String goToActionKey = "go-to-bookmark-" + i;
            actionMap.put(goToActionKey, new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    goToBookmark(bookmarkIndex);
                }
            });
            inputMap.put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_0 + i, metaMask), goToActionKey);
            inputMap.put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_NUMPAD0 + i, metaMask),
                    goToActionKey);

            String addActionKey = "add-bookmark-" + i;
            actionMap.put(addActionKey, new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    addBookmark(bookmarkIndex);
                }
            });
            inputMap.put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_0 + i,
                    metaMask | KeyEvent.SHIFT_DOWN_MASK), addActionKey);
            inputMap.put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_NUMPAD0 + i,
                    metaMask | KeyEvent.SHIFT_DOWN_MASK), addActionKey);

            String clearActionKey = "clear-bookmark-" + i;
            actionMap.put(clearActionKey, new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    clearBookmark(bookmarkIndex);
                }
            });
            inputMap.put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_0 + i,
                    metaMask | KeyEvent.SHIFT_DOWN_MASK | KeyEvent.ALT_DOWN_MASK), clearActionKey);
            inputMap.put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_NUMPAD0 + i,
                    metaMask | KeyEvent.SHIFT_DOWN_MASK | KeyEvent.ALT_DOWN_MASK), clearActionKey);
        }
        component.setActionMap(actionMap);
        component.setInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW, inputMap);
    }

    public void goToBookmark(int bookmarkIndex) {
        if (bookmarkRecords.size() > bookmarkIndex) {
            BookmarkRecord record = bookmarkRecords.get(bookmarkIndex);
            if (record.isEmpty()) {
                return;
            }

            if (activeFile != null) {
                ExtCodeArea codeArea = activeFile.getCodeArea();
                codeArea.setCaretPosition(record.getStartPosition());
                codeArea.centerOnCursor();
            }
        }
    }

    public void addBookmark(int bookmarkIndex) {
        if (activeFile != null) {
            ExtCodeArea codeArea = activeFile.getCodeArea();
            long position = codeArea.getDataPosition();

            if (bookmarkRecords.size() <= bookmarkIndex) {
                int recordsToInsert = bookmarkIndex - bookmarkRecords.size() + 1;
                for (int i = 0; i < recordsToInsert; i++) {
                    bookmarkRecords.add(new BookmarkRecord());
                }
            }

            BookmarkRecord record = bookmarkRecords.get(bookmarkIndex);
            record.setStartPosition(position);
            record.setLength(1);
            saveBookmarkRecords();
            bookmarksPositionColorModifier.notifyBookmarksChanged();
            updateBookmarksMenu();
        }
    }

    public void clearBookmark(int bookmarkIndex) {
        if (bookmarkRecords.size() > bookmarkIndex) {
            if (bookmarkRecords.size() == bookmarkIndex + 1) {
                bookmarkRecords.remove(bookmarkIndex);
            } else {
                bookmarkRecords.get(bookmarkIndex).setEmpty();
            }
            saveBookmarkRecords();
            bookmarksPositionColorModifier.notifyBookmarksChanged();
            updateBookmarksMenu();
        }
    }

    public void updateBookmarksMenu() {
    }

    public void updateBookmarksMenu(@Nullable JMenu menu) {
        if (menu == null) {
            return;
        }

        menu.removeAll();

        int recordsLimit = Math.min(bookmarkRecords.size(), 10);
        int metaMask = ActionUtils.getMetaMask();
        String bookmarkActionName = resourceBundle.getString("bookmarkAction.text");
        for (int i = 0; i < recordsLimit; i++) {
            BookmarkRecord bookmarkRecord = bookmarkRecords.get(i);
            if (bookmarkRecord.isEmpty()) {
                continue;
            }

            Action bookmarkAction = new AbstractAction(bookmarkActionName + " " + (i + 1)) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    long startPosition = bookmarkRecord.getStartPosition();
                    if (activeFile != null) {
                        ExtCodeArea codeArea = activeFile.getCodeArea();
                        codeArea.setCaretPosition(startPosition);
                        codeArea.centerOnCursor();
                    }
                }
            };
            bookmarkAction.putValue(Action.ACCELERATOR_KEY,
                    javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_0 + i, metaMask));
            final Color bookmarkColor = bookmarkRecord.getColor();
            bookmarkAction.putValue(Action.SMALL_ICON, new Icon() {
                @Override
                public void paintIcon(Component c, Graphics g, int x, int y) {
                    g.setColor(bookmarkColor);
                    g.fillRect(x + 2, y + 2, 12, 12);
                    g.setColor(Color.BLACK);
                    g.drawRect(x + 2, y + 2, 12, 12);
                }

                @Override
                public int getIconWidth() {
                    return 16;
                }

                @Override
                public int getIconHeight() {
                    return 16;
                }
            });

            menu.add(ActionUtils.actionToMenuItem(bookmarkAction));
        }

        if (!bookmarkRecords.isEmpty()) {
            menu.addSeparator();
        }
        menu.add(ActionUtils.actionToMenuItem(new AbstractAction(
                resourceBundle.getString("manageBookmarksAction.text") + "...") {
            @Override
            public void actionPerformed(ActionEvent e) {
                manageBookmarksAction.actionPerformed(e);
            }
        }));
    }
}
