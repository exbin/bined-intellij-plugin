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
package org.exbin.framework.bined.search;

import java.awt.BorderLayout;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.bined.swing.extended.ExtCodeArea;
import org.exbin.framework.api.XBApplication;
import org.exbin.framework.bined.BinedModule;
import org.exbin.framework.bined.gui.BinEdComponentPanel;
import org.exbin.framework.bined.handler.CodeAreaPopupMenuHandler;
import org.exbin.framework.bined.preferences.BinaryEditorPreferences;
import org.exbin.framework.bined.search.gui.BinarySearchPanel;
import org.exbin.framework.bined.search.service.BinarySearchService;
import org.exbin.framework.bined.search.service.impl.BinarySearchServiceImpl;

/**
 * Bined component search.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class BinEdComponentSearch implements BinEdComponentPanel.BinEdComponentExtension {

    private BinEdComponentPanel componentPanel;
    private final BinarySearch binarySearch = new BinarySearch();
    private BinarySearchService binarySearchService;
    private boolean binarySearchPanelVisible = false;
    private XBApplication application;

    @Override
    public void onCreate(BinEdComponentPanel componentPanel) {
        this.componentPanel = componentPanel;
        ExtCodeArea codeArea = componentPanel.getCodeArea();

        binarySearchService = new BinarySearchServiceImpl(codeArea);
        binarySearch.setBinarySearchService(binarySearchService);
        binarySearch.setPanelClosingListener(this::hideSearchPanel);

        BinedModule binedModule = application.getModuleRepository().getModuleByInterface(BinedModule.class);

        binarySearch.setCodeAreaPopupMenuHandler(binedModule.createCodeAreaPopupMenuHandler(BinedModule.PopupMenuVariant.NORMAL));
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
    public void onInitFromPreferences(BinaryEditorPreferences preferences) {
    }

    @Override
    public void onClose() {
    }

    public void showSearchPanel(BinarySearchPanel.PanelMode panelMode) {
        if (!binarySearchPanelVisible) {
            componentPanel.add(binarySearch.getPanel(), BorderLayout.SOUTH);
            componentPanel.revalidate();
            binarySearchPanelVisible = true;
            binarySearch.getPanel().requestSearchFocus();
        }
        binarySearch.getPanel().switchPanelMode(panelMode);
    }

    public void hideSearchPanel() {
        if (binarySearchPanelVisible) {
            binarySearch.cancelSearch();
            binarySearch.clearSearch();
            componentPanel.remove(binarySearch.getPanel());
            componentPanel.revalidate();
            binarySearchPanelVisible = false;
        }
    }
    
    public void performSearchText(String text) {
        SearchParameters searchParameters = new SearchParameters();
        SearchCondition searchCondition = new SearchCondition();
        searchCondition.setSearchText(text);
        searchParameters.setCondition(searchCondition);
        binarySearchService.performFind(searchParameters, binarySearch.getSearchStatusListener());
    }

    public void performFindAgain() {
        if (binarySearchPanelVisible) {
            binarySearchService.performFindAgain(binarySearch.getSearchStatusListener());
        } else {
            showSearchPanel(BinarySearchPanel.PanelMode.FIND);
        }
    }

    public void setCodeAreaPopupMenuHandler(CodeAreaPopupMenuHandler codeAreaPopupMenuHandler) {
        binarySearch.getPanel().setCodeAreaPopupMenuHandler(codeAreaPopupMenuHandler);
    }

    @Override
    public void setApplication(XBApplication application) {
        this.application = application;
        binarySearch.setApplication(application);
    }
}
