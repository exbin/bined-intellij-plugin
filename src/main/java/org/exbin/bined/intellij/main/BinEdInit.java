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
package org.exbin.bined.intellij.main;

import org.exbin.bined.basic.BasicCodeAreaZone;
import org.exbin.bined.intellij.bookmarks.BookmarksManager;
import org.exbin.bined.intellij.gui.BinEdComponentPanel;
import org.exbin.bined.swing.extended.ExtCodeArea;
import org.exbin.framework.bined.BinEdFileHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import java.util.Optional;

/**
 * Initialization of BinEd
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class BinEdInit {

    private BinEdManager binEdManager = BinEdManager.getInstance();

    private BinEdInit() {
    }

    public static void init() {
        BinEdInit binEdInit = new BinEdInit();
        binEdInit.initBookmarks();
    }

    private void initBookmarks() {
        BookmarksManager bookmarksManager = new BookmarksManager();
        BinEdFileManager fileManager = binEdManager.getFileManager();
        fileManager.addBinEdComponentExtension(new BinEdFileManager.BinEdFileExtension() {
            @Nonnull
            @Override
            public Optional<BinEdComponentPanel.BinEdComponentExtension> createComponentExtension(BinEdComponentPanel component) {
                return Optional.empty();
            }

            @Override
            public void onPopupMenuCreation(final JPopupMenu popupMenu, final ExtCodeArea codeArea, String menuPostfix, int x, int y) {
                BasicCodeAreaZone positionZone = codeArea.getPainter().getPositionZone(x, y);

                if (positionZone == BasicCodeAreaZone.TOP_LEFT_CORNER || positionZone == BasicCodeAreaZone.HEADER || positionZone == BasicCodeAreaZone.ROW_POSITIONS) {
                    return;
                }

                // TODO: Change position
                popupMenu.add(bookmarksManager.createBookmarksPopupMenu());
            }
        });
        binEdManager.setBookmarksSupport(new BinEdManager.BookmarksSupport() {

            @Nonnull
            @Override
            public JMenu createBookmarksPopupMenu() {
                return bookmarksManager.createBookmarksPopupMenu();
            }

            @Override
            public void registerBookmarksComponentActions(JComponent component) {
                bookmarksManager.registerBookmarksComponentActions(component);
            }

            @Override
            public void setActiveFile(@Nullable BinEdFileHandler activeFile) {
                bookmarksManager.setActiveFile(activeFile);
            }
        });
    }
}
