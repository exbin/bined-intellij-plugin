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
import org.exbin.bined.jaguif.search.service.impl.BinarySearchServiceImpl;
import org.exbin.bined.swing.section.SectCodeArea;
import org.exbin.jaguif.App;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.JPopupMenu;
import java.awt.BorderLayout;

/**
 * BinEd component search.
 */
@ParametersAreNonnullByDefault
public class BinEdIntelliJComponentSearch implements BinEdComponentSearch {

    private BinEdComponentPanel componentPanel;
    private final BinarySearch binarySearch = new BinarySearch();
    private BinarySearchService binarySearchService;
    private boolean binarySearchPanelVisible = false;

    @Override
    public void onCreate(BinaryDataComponent dataComponent) {
        this.componentPanel = (BinEdComponentPanel) dataComponent.getComponent();
        SectCodeArea codeArea = (SectCodeArea) dataComponent.getCodeArea();

        binarySearchService = new BinarySearchServiceImpl(codeArea);
        binarySearch.setBinarySearchService(binarySearchService);
        binarySearch.setPanelClosingListener(this::hideSearchPanel);
        binarySearch.setTargetComponent(componentPanel);

        BinedComponentModule binedComponentModule = App.getModule(BinedComponentModule.class);
        binarySearch.setCodeAreaPopupMenu(binedComponentModule.createCodeAreaPopupMenu());
    }

    @Override
    public void onDataChange() {
        if (binarySearchPanelVisible) {
            binarySearch.dataChanged();
        }
    }

    @Override
    public void onUndoHandlerChange() {
    }

    @Override
    public void showSearchPanel(BinarySearchPanel.PanelMode panelMode) {
        if (!binarySearchPanelVisible) {
            componentPanel.add(binarySearch.getPanel(), BorderLayout.NORTH);
            componentPanel.revalidate();
            binarySearchPanelVisible = true;
            binarySearch.getPanel().requestSearchFocus();
        }
        binarySearch.getPanel().switchPanelMode(panelMode);
    }

    @Override
    public void hideSearchPanel() {
        if (binarySearchPanelVisible) {
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
        binarySearchService.performFind(searchParameters, binarySearch.getSearchStatusListener());
    }

    @Override
    public void performFindAgain() {
        if (binarySearchPanelVisible) {
            binarySearchService.performFindAgain(binarySearch.getSearchStatusListener());
        } else {
            showSearchPanel(BinarySearchPanel.PanelMode.FIND);
        }
    }

    @Override
    public void setCodeAreaPopupMenu(JPopupMenu popupMenu) {
        binarySearch.getPanel().setCodeAreaPopupMenu(popupMenu);
    }
}
