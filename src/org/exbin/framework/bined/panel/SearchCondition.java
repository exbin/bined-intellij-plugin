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
package org.exbin.framework.bined.panel;

import java.util.Objects;
import org.exbin.utils.binary_data.BinaryData;
import org.exbin.utils.binary_data.ByteArrayEditableData;
import org.exbin.utils.binary_data.EditableBinaryData;

/**
 * Parameters for action to search for occurences of text or data.
 *
 * @version 0.1.0 2016/07/18
 * @author ExBin Project (http://exbin.org)
 */
public class SearchCondition {

    private SearchMode searchMode = SearchMode.TEXT;
    private String searchText = "";
    private EditableBinaryData binaryData;

    public SearchCondition() {
    }

    /**
     * This is copy constructor.
     *
     * @param source source condition
     */
    public SearchCondition(SearchCondition source) {
        searchMode = source.getSearchMode();
        searchText = source.getSearchText();
        binaryData = new ByteArrayEditableData();
        if (source.getBinaryData() != null) {
            binaryData.insert(0, source.getBinaryData());
        }
    }

    public SearchMode getSearchMode() {
        return searchMode;
    }

    public void setSearchMode(SearchMode searchMode) {
        this.searchMode = searchMode;
    }

    public String getSearchText() {
        return searchText;
    }

    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }

    public BinaryData getBinaryData() {
        return binaryData;
    }

    public void setBinaryData(EditableBinaryData binaryData) {
        this.binaryData = binaryData;
    }

    public boolean isEmpty() {
        switch (searchMode) {
            case TEXT: {
                return searchText == null || searchText.isEmpty();
            }
            case BINARY: {
                return binaryData == null || binaryData.isEmpty();
            }
            default:
                throw new IllegalStateException("Unexpected search mode " + searchMode.name());
        }
    }

    @Override
    public int hashCode() {
        int hash = 3;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SearchCondition other = (SearchCondition) obj;
        if (this.searchMode != other.searchMode) {
            return false;
        }
        if (searchMode == SearchMode.TEXT) {
            return Objects.equals(this.searchText, other.searchText);
        } else {
            return Objects.equals(this.binaryData, other.binaryData);
        }
    }

    public void clear() {
        searchText = "";
        if (binaryData != null) {
            binaryData.clear();
        }
    }

    public static enum SearchMode {
        TEXT, BINARY
    }
}
