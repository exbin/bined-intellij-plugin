/*
 * Copyright (C) ExBin Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
