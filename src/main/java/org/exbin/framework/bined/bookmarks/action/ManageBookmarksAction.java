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
package org.exbin.framework.bined.bookmarks.action;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.AbstractAction;
import org.exbin.framework.api.XBApplication;
import org.exbin.framework.utils.ActionUtils;
import org.exbin.framework.utils.WindowUtils;
import org.exbin.framework.editor.api.EditorProvider;
import org.exbin.framework.bined.bookmarks.BookmarksManager;
import org.exbin.framework.bined.bookmarks.gui.BookmarksManagerPanel;
import org.exbin.framework.bined.bookmarks.model.BookmarkRecord;
import org.exbin.framework.frame.api.FrameModuleApi;
import org.exbin.framework.utils.gui.DefaultControlPanel;

/**
 * Manage bookmarks action.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class ManageBookmarksAction extends AbstractAction {

    public static final String ACTION_ID = "manageBookmarksAction";

    private EditorProvider editorProvider;
    private XBApplication application;
    private ResourceBundle resourceBundle;
    private BookmarksManager bookmarksManager;

    public ManageBookmarksAction() {
    }

    public void setup(XBApplication application, EditorProvider editorProvider, ResourceBundle resourceBundle) {
        this.application = application;
        this.editorProvider = editorProvider;
        this.resourceBundle = resourceBundle;

        ActionUtils.setupAction(this, resourceBundle, ACTION_ID);
        putValue(ActionUtils.ACTION_DIALOG_MODE, true);
    }

    public void setBookmarksManager(BookmarksManager bookmarksManager) {
        this.bookmarksManager = bookmarksManager;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final BookmarksManagerPanel bookmarksPanel = bookmarksManager.createBookmarksManagerPanel();
        List<BookmarkRecord> records = new ArrayList<>();
        for (BookmarkRecord record : bookmarksManager.getBookmarkRecords()) {
            records.add(new BookmarkRecord(record));
        }
        bookmarksPanel.setBookmarkRecords(records);
        ResourceBundle panelResourceBundle = bookmarksPanel.getResourceBundle();
        DefaultControlPanel controlPanel = new DefaultControlPanel(panelResourceBundle);

        FrameModuleApi frameModule = application.getModuleRepository().getModuleByInterface(FrameModuleApi.class);
        final WindowUtils.DialogWrapper dialog = frameModule.createDialog(editorProvider.getEditorComponent(), Dialog.ModalityType.APPLICATION_MODAL, bookmarksPanel, controlPanel);
        WindowUtils.addHeaderPanel(dialog.getWindow(), bookmarksPanel.getClass(), bookmarksPanel.getResourceBundle());
        frameModule.setDialogTitle(dialog, panelResourceBundle);
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

        dialog.showCentered(editorProvider.getEditorComponent());
    }
}
