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
package org.exbin.framework.bined.service;

import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.bined.swing.extended.ExtCodeArea;
import org.exbin.framework.bined.ReplaceParameters;
import org.exbin.framework.bined.SearchParameters;

/**
 * Binary search service.
 *
 * @version 0.2.1 2019/07/16
 * @author ExBin Project (http://exbin.org)
 */
@ParametersAreNonnullByDefault
public interface BinarySearchService {

    void performFind(SearchParameters dialogSearchParameters, ExtCodeArea codeArea, SearchStatusListener searchStatusListener);

    void setMatchPosition(int matchPosition, ExtCodeArea codeArea);

    void performReplace(SearchParameters searchParameters, ReplaceParameters replaceParameters, ExtCodeArea codeArea);

    void clearMatches(ExtCodeArea codeArea);

    @ParametersAreNonnullByDefault
    public interface SearchStatusListener {

        void setStatus(FoundMatches foundMatches);

        void clearStatus();
    }

    public static class FoundMatches {

        private int matchesCount;
        private int matchPosition;

        public FoundMatches() {
            matchesCount = 0;
            matchPosition = -1;
        }

        public FoundMatches(int matchesCount, int matchPosition) {
            if (matchPosition >= matchesCount) {
                throw new IllegalStateException("Match position is out of range");
            }

            this.matchesCount = matchesCount;
            this.matchPosition = matchPosition;
        }

        public int getMatchesCount() {
            return matchesCount;
        }

        public int getMatchPosition() {
            return matchPosition;
        }

        public void setMatchesCount(int matchesCount) {
            this.matchesCount = matchesCount;
        }

        public void setMatchPosition(int matchPosition) {
            this.matchPosition = matchPosition;
        }

        public void next() {
            if (matchPosition == matchesCount - 1) {
                throw new IllegalStateException("Cannot step next on last match");
            }

            matchPosition++;
        }

        public void prev() {
            if (matchPosition == 0) {
                throw new IllegalStateException("Cannot step previous on first match");
            }

            matchPosition--;
        }
    }
}
