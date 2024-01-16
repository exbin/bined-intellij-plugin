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

import java.awt.Dialog;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.JPanel;
import org.exbin.auxiliary.binary_data.ByteArrayEditableData;
import org.exbin.auxiliary.binary_data.EditableBinaryData;
import org.exbin.framework.api.XBApplication;
import org.exbin.framework.bined.handler.CodeAreaPopupMenuHandler;
import org.exbin.framework.bined.search.gui.BinaryMultilinePanel;
import org.exbin.framework.bined.search.gui.BinarySearchPanel;
import org.exbin.framework.bined.search.gui.FindBinaryPanel;
import org.exbin.framework.bined.search.service.BinarySearchService;
import org.exbin.framework.bined.search.service.BinarySearchService.FoundMatches;
import org.exbin.framework.frame.api.FrameModuleApi;
import org.exbin.framework.utils.LanguageUtils;
import org.exbin.framework.utils.WindowUtils;
import org.exbin.framework.utils.gui.DefaultControlPanel;
import org.exbin.framework.utils.handler.DefaultControlHandler;

/**
 * Binary search.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class BinarySearch {

    private final ResourceBundle resourceBundle = LanguageUtils.getResourceBundleByClass(BinarySearch.class);
    private static final int DEFAULT_DELAY = 500;

    private InvokeSearchThread invokeSearchThread;
    private SearchThread searchThread;

    private SearchOperation currentSearchOperation = SearchOperation.FIND;
    private SearchParameters.SearchDirection currentSearchDirection = SearchParameters.SearchDirection.FORWARD;
    private final SearchParameters currentSearchParameters = new SearchParameters();
    private final ReplaceParameters currentReplaceParameters = new ReplaceParameters();
    private FoundMatches foundMatches = new FoundMatches();

    private final List<SearchCondition> searchHistory = new ArrayList<>();
    private final List<SearchCondition> replaceHistory = new ArrayList<>();

    private CodeAreaPopupMenuHandler codeAreaPopupMenuHandler;
    private PanelClosingListener panelClosingListener = null;
    private BinarySearchService binarySearchService;
    private final BinarySearchService.SearchStatusListener searchStatusListener;
    private final BinarySearchPanel binarySearchPanel = new BinarySearchPanel();

    private XBApplication application;

    public BinarySearch() {
        searchStatusListener = new BinarySearchService.SearchStatusListener() {
            @Override
            public void setStatus(@Nonnull BinarySearchService.FoundMatches foundMatches, @Nonnull SearchParameters.MatchMode matchMode) {
                BinarySearch.this.foundMatches = foundMatches;
                switch (foundMatches.getMatchesCount()) {
                    case 0:
                        binarySearchPanel.setInfoLabel(resourceBundle.getString("searchStatus.noMatch"));
                        break;
                    case 1:
                        binarySearchPanel.setInfoLabel(
                                matchMode == SearchParameters.MatchMode.MULTIPLE ? resourceBundle.getString("searchStatus.singleMatch") : resourceBundle.getString("searchStatus.matchFound")
                        );
                        break;
                    default:
                        binarySearchPanel.setInfoLabel(
                                java.text.MessageFormat.format(resourceBundle.getString("searchStatus.foundMatches"), foundMatches.getMatchPosition() + 1, foundMatches.getMatchesCount())
                        );
                        break;
                }
                updateMatchStatus();
            }

            @Override
            public void clearStatus() {
                binarySearchPanel.setInfoLabel("");
                BinarySearch.this.foundMatches = new BinarySearchService.FoundMatches();
                updateMatchStatus();
            }

            private void updateMatchStatus() {
                int matchesCount = foundMatches.getMatchesCount();
                int matchPosition = foundMatches.getMatchPosition();
                binarySearchPanel.updateMatchStatus(matchesCount > 0,
                        matchesCount > 1 && matchPosition > 0,
                        matchPosition < matchesCount - 1
                );
            }
        };
        binarySearchPanel.setControl(new BinarySearchPanel.Control() {
            @Override
            public void prevMatch() {
                foundMatches.prev();
                binarySearchService.setMatchPosition(foundMatches.getMatchPosition());
                searchStatusListener.setStatus(foundMatches, binarySearchService.getLastSearchParameters().getMatchMode());
            }

            @Override
            public void nextMatch() {
                foundMatches.next();
                binarySearchService.setMatchPosition(foundMatches.getMatchPosition());
                searchStatusListener.setStatus(foundMatches, binarySearchService.getLastSearchParameters().getMatchMode());
            }

            @Override
            public void performEscape() {
                cancelSearch();
                close();
                clearSearch();
            }

            @Override
            public void performFind() {
                invokeSearch(SearchOperation.FIND);
                binarySearchPanel.updateSearchHistory(currentSearchParameters.getCondition());
            }

            @Override
            public void performReplace() {
                invokeSearch(SearchOperation.REPLACE);
                binarySearchPanel.updateSearchHistory(currentSearchParameters.getCondition());
            }

            @Override
            public void performReplaceAll() {
                invokeSearch(SearchOperation.REPLACE_ALL);
                binarySearchPanel.updateSearchHistory(currentSearchParameters.getCondition());
            }

            @Override
            public void notifySearchChanged() {
                if (currentSearchOperation == SearchOperation.FIND) {
                    invokeSearch(SearchOperation.FIND);
                }
            }

            @Override
            public void notifySearchChanging() {
                if (currentSearchOperation != SearchOperation.FIND) {
                    return;
                }

                SearchCondition condition = currentSearchParameters.getCondition();
                SearchCondition updatedSearchCondition = binarySearchPanel.getSearchParameters().getCondition();

                switch (updatedSearchCondition.getSearchMode()) {
                    case TEXT: {
                        String searchText = updatedSearchCondition.getSearchText();
                        if (searchText.isEmpty()) {
                            condition.setSearchText(searchText);
                            clearSearch();
                            return;
                        }

                        if (searchText.equals(condition.getSearchText())) {
                            return;
                        }

                        condition.setSearchText(searchText);
                        break;
                    }
                    case BINARY: {
                        EditableBinaryData searchData = (EditableBinaryData) updatedSearchCondition.getBinaryData();
                        if (searchData == null || searchData.isEmpty()) {
                            condition.setBinaryData(null);
                            clearSearch();
                            return;
                        }

                        if (searchData.equals(condition.getBinaryData())) {
                            return;
                        }

                        ByteArrayEditableData data = new ByteArrayEditableData();
                        data.insert(0, searchData);
                        condition.setBinaryData(data);
                        break;
                    }
                }
                BinarySearch.this.invokeSearch(SearchOperation.FIND, DEFAULT_DELAY);
            }

            @Override
            public void searchOptions() {
                cancelSearch();
                FrameModuleApi frameModule = application.getModuleRepository().getModuleByInterface(FrameModuleApi.class);
                final FindBinaryPanel findBinaryPanel = new FindBinaryPanel();
                findBinaryPanel.setSelected();
                findBinaryPanel.setSearchHistory(searchHistory);
                findBinaryPanel.setSearchParameters(currentSearchParameters);
                findBinaryPanel.setReplaceParameters(currentReplaceParameters);
                findBinaryPanel.setCodeAreaPopupMenuHandler(codeAreaPopupMenuHandler);
                DefaultControlPanel controlPanel = new DefaultControlPanel(findBinaryPanel.getResourceBundle());
                final WindowUtils.DialogWrapper dialog = frameModule.createDialog(findBinaryPanel, controlPanel);
                frameModule.setDialogTitle(dialog, findBinaryPanel.getResourceBundle());
                WindowUtils.addHeaderPanel(dialog.getWindow(), findBinaryPanel.getClass(), findBinaryPanel.getResourceBundle());
                findBinaryPanel.setMultilineEditorListener(new FindBinaryPanel.MultilineEditorListener() {
                    @Override
                    public SearchCondition multilineEdit(SearchCondition condition) {
                        final BinaryMultilinePanel multilinePanel = new BinaryMultilinePanel();
                        multilinePanel.setCodeAreaPopupMenuHandler(codeAreaPopupMenuHandler);
                        multilinePanel.setCondition(condition);
                        DefaultControlPanel controlPanel = new DefaultControlPanel();
                        JPanel dialogPanel = WindowUtils.createDialogPanel(multilinePanel, controlPanel);
                        FrameModuleApi frameModule = application.getModuleRepository().getModuleByInterface(FrameModuleApi.class);
                        final WindowUtils.DialogWrapper multilineDialog = frameModule.createDialog(dialog.getWindow(), Dialog.ModalityType.APPLICATION_MODAL, dialogPanel);
                        WindowUtils.addHeaderPanel(multilineDialog.getWindow(), multilinePanel.getClass(), multilinePanel.getResourceBundle());
                        frameModule.setDialogTitle(multilineDialog, multilinePanel.getResourceBundle());
                        final SearchConditionResult result = new SearchConditionResult();
                        controlPanel.setHandler((DefaultControlHandler.ControlActionType actionType) -> {
                            if (actionType == DefaultControlHandler.ControlActionType.OK) {
                                result.searchCondition = multilinePanel.getCondition();
                                binarySearchPanel.updateFindStatus();
                            }

                            multilineDialog.close();
                            multilineDialog.dispose();
                        });
                        multilineDialog.showCentered(dialog.getWindow());
                        multilinePanel.detachMenu();
                        return result.searchCondition;
                    }

                    class SearchConditionResult {

                        SearchCondition searchCondition = null;
                    }
                });
                controlPanel.setHandler((DefaultControlHandler.ControlActionType actionType) -> {
                    if (actionType == DefaultControlHandler.ControlActionType.OK) {
                        SearchParameters dialogSearchParameters = findBinaryPanel.getSearchParameters();
                        dialogSearchParameters.setFromParameters(dialogSearchParameters);
                        currentSearchDirection = dialogSearchParameters.getSearchDirection();

                        ReplaceParameters dialogReplaceParameters = new ReplaceParameters();
                        dialogReplaceParameters.setFromParameters(findBinaryPanel.getReplaceParameters());
                        boolean performReplace = dialogReplaceParameters.isPerformReplace();
                        binarySearchPanel.switchPanelMode(performReplace ? BinarySearchPanel.PanelMode.REPLACE : BinarySearchPanel.PanelMode.FIND);
                        invokeSearch(performReplace ? SearchOperation.REPLACE : SearchOperation.FIND, dialogSearchParameters, dialogReplaceParameters);
                    }
                    findBinaryPanel.detachMenu();
                    dialog.close();
                    dialog.dispose();
                });
                dialog.showCentered(WindowUtils.getWindow(binarySearchPanel));
            }

            @Nonnull
            @Override
            public SearchParameters.SearchDirection getSearchDirection() {
                return currentSearchDirection;
            }

            @Override
            public void close() {
                if (panelClosingListener != null) {
                    clearSearch();
                    panelClosingListener.closed();
                }
            }
        });
        binarySearchPanel.setSearchHistory(searchHistory);
        binarySearchPanel.setReplaceHistory(replaceHistory);
    }

    public void setApplication(XBApplication application) {
        this.application = application;
        binarySearchPanel.setApplication(application);
    }

    public void setBinarySearchService(BinarySearchService binarySearchService) {
        this.binarySearchService = binarySearchService;
    }

    public void setPanelClosingListener(PanelClosingListener panelClosingListener) {
        this.panelClosingListener = panelClosingListener;
    }

    public void setCodeAreaPopupMenuHandler(CodeAreaPopupMenuHandler codeAreaPopupMenuHandler) {
        this.codeAreaPopupMenuHandler = codeAreaPopupMenuHandler;
        binarySearchPanel.setCodeAreaPopupMenuHandler(codeAreaPopupMenuHandler);
    }

    @Nonnull
    public BinarySearchService.SearchStatusListener getSearchStatusListener() {
        return searchStatusListener;
    }

    private void invokeSearch(SearchOperation searchOperation) {
        invokeSearch(searchOperation, binarySearchPanel.getSearchParameters(), binarySearchPanel.getReplaceParameters(), 0);
    }

    private void invokeSearch(SearchOperation searchOperation, final int delay) {
        invokeSearch(searchOperation, binarySearchPanel.getSearchParameters(), binarySearchPanel.getReplaceParameters(), delay);
    }

    private void invokeSearch(SearchOperation searchOperation, SearchParameters searchParameters, @Nullable ReplaceParameters replaceParameters) {
        invokeSearch(searchOperation, searchParameters, replaceParameters, 0);
    }

    private void invokeSearch(SearchOperation searchOperation, SearchParameters searchParameters, @Nullable ReplaceParameters replaceParameters, final int delay) {
        if (invokeSearchThread != null) {
            invokeSearchThread.interrupt();
        }
        invokeSearchThread = new InvokeSearchThread();
        invokeSearchThread.delay = delay;
        currentSearchOperation = searchOperation;
        currentSearchParameters.setFromParameters(searchParameters);
        currentReplaceParameters.setFromParameters(replaceParameters);
        invokeSearchThread.start();
    }

    public void cancelSearch() {
        if (invokeSearchThread != null) {
            invokeSearchThread.interrupt();
        }
        if (searchThread != null) {
            searchThread.interrupt();
        }
    }

    public void clearSearch() {
        SearchCondition condition = currentSearchParameters.getCondition();
        condition.clear();
        binarySearchPanel.clearSearch();
        binarySearchService.clearMatches();
        searchStatusListener.clearStatus();
    }

    @Nonnull
    public BinarySearchPanel getPanel() {
        return binarySearchPanel;
    }

    public void dataChanged() {
        binarySearchService.clearMatches();
        invokeSearch(currentSearchOperation, DEFAULT_DELAY);
    }

    private class InvokeSearchThread extends Thread {

        private int delay = DEFAULT_DELAY;

        public InvokeSearchThread() {
            super("InvokeSearchThread");
        }

        @Override
        public void run() {
            try {
                Thread.sleep(delay);
                if (searchThread != null) {
                    searchThread.interrupt();
                }
                searchThread = new SearchThread();
                searchThread.start();
            } catch (InterruptedException ex) {
                // don't search
            }
        }
    }

    private class SearchThread extends Thread {

        public SearchThread() {
            super("SearchThread");
        }

        @Override
        public void run() {
            switch (currentSearchOperation) {
                case FIND:
                    binarySearchService.performFind(currentSearchParameters, searchStatusListener);
                    break;
                case FIND_AGAIN:
                    binarySearchService.performFindAgain(searchStatusListener);
                    break;
                case REPLACE:
                    binarySearchService.performReplace(currentSearchParameters, currentReplaceParameters);
                    break;
                default:
                    throw new UnsupportedOperationException("Not supported yet.");
            }
        }
    }

    public interface PanelClosingListener {

        void closed();
    }

    private enum SearchOperation {
        FIND,
        FIND_AGAIN,
        REPLACE,
        REPLACE_ALL
    }
}
