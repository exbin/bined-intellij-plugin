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

/**
 * Parameters for action to search for occurences of text or data.
 *
 * @version 0.1.0 2016/07/21
 * @author ExBin Project (http://exbin.org)
 */
public class SearchParameters {

    private SearchCondition condition = new SearchCondition();
    private long startPosition;
    private boolean searchFromCursor;
    private boolean matchCase = true;
    private boolean multipleMatches = true;
    private SearchDirection searchDirection = SearchDirection.FORWARD;

    public SearchParameters() {
    }

    public SearchCondition getCondition() {
        return condition;
    }

    public void setCondition(SearchCondition condition) {
        this.condition = condition;
    }

    public long getStartPosition() {
        return startPosition;
    }

    public void setStartPosition(long startPosition) {
        this.startPosition = startPosition;
    }

    public boolean isSearchFromCursor() {
        return searchFromCursor;
    }

    public void setSearchFromCursor(boolean searchFromCursor) {
        this.searchFromCursor = searchFromCursor;
    }

    public boolean isMatchCase() {
        return matchCase;
    }

    public void setMatchCase(boolean matchCase) {
        this.matchCase = matchCase;
    }

    public boolean isMultipleMatches() {
        return multipleMatches;
    }

    public void setMultipleMatches(boolean multipleMatches) {
        this.multipleMatches = multipleMatches;
    }

    public SearchDirection getSearchDirection() {
        return searchDirection;
    }

    public void setSearchDirection(SearchDirection searchDirection) {
        this.searchDirection = searchDirection;
    }

    public void setFromParameters(SearchParameters searchParameters) {
        condition = searchParameters.getCondition();
        startPosition = searchParameters.getStartPosition();
        searchFromCursor = searchParameters.isSearchFromCursor();
        matchCase = searchParameters.isMatchCase();
        multipleMatches = searchParameters.isMultipleMatches();
        searchDirection = searchParameters.getSearchDirection();
    }

    public static enum SearchMode {
        TEXT, BINARY
    }

    public static enum SearchDirection {
        FORWARD, BACKWARD
    }
}
