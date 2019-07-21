/*
 * Copyright (C) ExBin Project
 *
 * This application or library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * This application or library is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along this application.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.exbin.framework.bined;

import java.util.ArrayList;
import java.util.List;
import javax.swing.ComboBoxModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

/**
 * Search condition history model.
 *
 * @version 0.1.0 2016/07/19
 * @author ExBin Project (http://exbin.org)
 */
public class SearchHistoryModel implements ComboBoxModel<SearchCondition> {

    public static final int HISTORY_LIMIT = 10;
    private final List<SearchCondition> searchHistory;
    private final List<ListDataListener> listDataListeners = new ArrayList<>();
    private SearchCondition selectedItem = null;

    public SearchHistoryModel(List<SearchCondition> searchHistory) {
        this.searchHistory = searchHistory;
    }

    @Override
    public void setSelectedItem(Object selectedItem) {
        this.selectedItem = (SearchCondition) selectedItem;
    }

    @Override
    public Object getSelectedItem() {
        return selectedItem;
    }

    @Override
    public int getSize() {
        return searchHistory.size();
    }

    @Override
    public SearchCondition getElementAt(int index) {
        return searchHistory.get(index);
    }

    @Override
    public void addListDataListener(ListDataListener listDataListener) {
        listDataListeners.add(listDataListener);
    }

    @Override
    public void removeListDataListener(ListDataListener listDataListener) {
        listDataListeners.remove(listDataListener);
    }

    public void addSearchCondition(SearchCondition condition) {
        if (condition.isEmpty()) {
            return;
        }

        boolean replaced = false;
        for (int i = 0; i < searchHistory.size(); i++) {
            SearchCondition searchCondition = searchHistory.get(i);
            if (searchCondition.equals(condition)) {
                if (i == 0) {
                    return;
                }

                searchHistory.remove(i);
                replaced = true;
            }
        }
        if (searchHistory.size() == HISTORY_LIMIT && !replaced) {
            int removePosition = searchHistory.size() - 1;
            searchHistory.remove(removePosition);
        }

        searchHistory.add(0, new SearchCondition(condition));
        for (ListDataListener listDataListener : listDataListeners) {
            listDataListener.contentsChanged(new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, 0, 0));
        }
    }
}
