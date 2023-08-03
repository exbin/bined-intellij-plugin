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
package org.exbin.bined.intellij.bookmarks.action;

import org.exbin.bined.intellij.bookmarks.BookmarksManager;
import org.exbin.framework.bined.bookmarks.gui.BookmarksManagerPanel;
import org.exbin.framework.bined.bookmarks.model.BookmarkRecord;
import org.exbin.framework.utils.WindowUtils;
import org.exbin.framework.utils.gui.DefaultControlPanel;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.JPanel;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Manage bookmarks action.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class ManageBookmarksAction implements ActionListener {

    private ResourceBundle resourceBundle;
    private BookmarksManager bookmarksManager;

    public ManageBookmarksAction() {
    }

    public BookmarksManager getBookmarksManager() {
        return bookmarksManager;
    }

    public void setBookmarksManager(BookmarksManager bookmarksManager) {
        this.bookmarksManager = bookmarksManager;
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        final BookmarksManagerPanel bookmarksPanel = bookmarksManager.createBookmarksManagerPanel();
        List<BookmarkRecord> records = new ArrayList<>();
        for (BookmarkRecord record : bookmarksManager.getBookmarkRecords()) {
            records.add(new BookmarkRecord(record));
        }
        bookmarksPanel.setBookmarkRecords(records);
        ResourceBundle panelResourceBundle = bookmarksPanel.getResourceBundle();
        DefaultControlPanel controlPanel = new DefaultControlPanel(panelResourceBundle);

        JPanel dialogPanel = WindowUtils.createDialogPanel(bookmarksPanel, controlPanel);
        final WindowUtils.DialogWrapper dialog = WindowUtils.createDialog(dialogPanel, (Component) event.getSource(), resourceBundle.getString("dialog.title"), Dialog.ModalityType.APPLICATION_MODAL);
        WindowUtils.addHeaderPanel(dialog.getWindow(), bookmarksPanel.getClass(), bookmarksPanel.getResourceBundle());
        Dimension preferredSize = dialog.getWindow().getPreferredSize();
        dialog.getWindow().setPreferredSize(new Dimension(preferredSize.width, preferredSize.height + 450));
        controlPanel.setHandler((actionType) -> {
            switch (actionType) {
                case OK: {
                    List<BookmarkRecord> bookmarkRecords = bookmarksPanel.getBookmarkRecords();
                    bookmarksManager.setBookmarkRecords(bookmarkRecords);
                    dialog.close();
                    break;
                }
                case CANCEL: {
                    dialog.close();
                    break;
                }
            }
        });

        dialog.showCentered((Component) event.getSource());
    }
}
