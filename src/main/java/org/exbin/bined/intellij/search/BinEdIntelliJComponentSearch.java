/*
 * Copyright (C) ExBin Project, https://exbin.org
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
package org.exbin.bined.intellij.search;

import org.exbin.bined.jaguif.component.BinaryDataComponent;
import org.exbin.bined.jaguif.component.BinedComponentModule;
import org.exbin.bined.jaguif.component.gui.BinEdComponentPanel;
import org.exbin.bined.jaguif.search.BinEdComponentSearch;
import org.exbin.bined.jaguif.search.SearchCondition;
import org.exbin.bined.jaguif.search.SearchParameters;
import org.exbin.bined.jaguif.search.gui.BinarySearchPanel;
import org.exbin.bined.jaguif.search.service.BinarySearchService;
import org.exbin.bined.jaguif.search.service.DefaultBinarySearchService;
import org.exbin.bined.swing.section.SectCodeArea;
import org.exbin.jaguif.App;

import org.jspecify.annotations.NullMarked;
import javax.swing.JPopupMenu;
import java.awt.BorderLayout;

/**
 * BinEd component search.
 */
@NullMarked
public class BinEdIntelliJComponentSearch implements BinEdComponentSearch {

    private BinEdComponentPanel componentPanel;
    private BinarySearch binarySearch;
    private BinarySearchService binarySearchService;
    private boolean binarySearchPanelVisible = false;

    @Override
    public void onCreate(BinaryDataComponent dataComponent) {
        this.componentPanel = (BinEdComponentPanel) dataComponent.getComponent();
        SectCodeArea codeArea = (SectCodeArea) dataComponent.getCodeArea();

        binarySearchService = new DefaultBinarySearchService(codeArea);
    }

    @Override
    public void onDataChange() {
        if (binarySearchPanelVisible) {
            getBinarySearch().dataChanged();
        }
    }

    private BinarySearch getBinarySearch() {
        if (binarySearch == null) {
            binarySearch = new BinarySearch();
            binarySearch.setBinarySearchService(binarySearchService);
            binarySearch.setPanelClosingListener(this::hideSearchPanel);
            BinedComponentModule binedComponentModule = App.getModule(BinedComponentModule.class);
            binarySearch.setCodeAreaPopupMenu(binedComponentModule.createCodeAreaPopupMenu());
        }

        return binarySearch;
    }

    @Override
    public void onUndoHandlerChange() {
    }

    @Override
    public void showSearchFindPanel() {
        if (!binarySearchPanelVisible) {
            getBinarySearch();
            componentPanel.add(binarySearch.getPanel(), BorderLayout.NORTH);
            componentPanel.revalidate();
            binarySearchPanelVisible = true;
            binarySearch.getPanel().requestSearchFocus();
        }
        binarySearch.getPanel().switchToFindMode();
    }

    @Override
    public void showSearchReplacePanel() {
        if (!binarySearchPanelVisible) {
            getBinarySearch();
            componentPanel.add(binarySearch.getPanel(), BorderLayout.NORTH);
            componentPanel.revalidate();
            binarySearchPanelVisible = true;
            binarySearch.getPanel().requestSearchFocus();
        }
        binarySearch.getPanel().switchToReplaceMode();
    }

    @Override
    public void hideSearchPanel() {
        if (binarySearchPanelVisible) {
            getBinarySearch();
            binarySearch.cancelSearch();
            binarySearch.clearSearch();
            componentPanel.remove(binarySearch.getPanel());
            componentPanel.revalidate();
            binarySearchPanelVisible = false;
        }
    }

    @Override
    public void performSearchText(String text) {
        SearchParameters searchParameters = new SearchParameters();
        SearchCondition searchCondition = new SearchCondition();
        searchCondition.setSearchText(text);
        searchParameters.setCondition(searchCondition);
        binarySearchService.performFind(searchParameters, getBinarySearch().getSearchStatusListener());
    }

    @Override
    public void performFindAgain() {
        if (binarySearchPanelVisible) {
            binarySearchService.performFindAgain(getBinarySearch().getSearchStatusListener());
        } else {
            showSearchFindPanel();
        }
    }

    @Override
    public void setCodeAreaPopupMenu(JPopupMenu popupMenu) {
        getBinarySearch().getPanel().setCodeAreaPopupMenu(popupMenu);
    }
}
