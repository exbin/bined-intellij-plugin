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
package org.exbin.framework.editor.text.service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;
import javax.swing.JTextArea;

/**
 * Text handling service.
 *
 * @version 0.2.1 2019/07/17
 * @author ExBin Project (http://exbin.org)
 */
public interface TextSearchService {

    @Nullable
    FoundMatch findText(JTextArea textArea, FindTextParameters findTextParameters);

    @ParametersAreNonnullByDefault
    public static class FindTextParameters {

        private int startFrom;
        private boolean shallReplace;
        private String findText;
        private boolean searchFromStart;
        private String replaceText;

        public int getStartFrom() {
            return startFrom;
        }

        public boolean isSearchFromStart() {
            return searchFromStart;
        }

        @Nonnull
        public String getFindText() {
            return findText;
        }

        public boolean isShallReplace() {
            return shallReplace;
        }

        @Nullable
        public String getReplaceText() {
            return replaceText;
        }

        public void setStartFrom(int startFrom) {
            this.startFrom = startFrom;
        }

        public void setShallReplace(boolean shallReplace) {
            this.shallReplace = shallReplace;
        }

        public void setFindText(String findText) {
            this.findText = findText;
        }

        public void setSearchFromStart(boolean searchFromStart) {
            this.searchFromStart = searchFromStart;
        }

        public void setReplaceText(@Nullable String replaceText) {
            this.replaceText = replaceText;
        }
    }

    @Immutable
    public static class FoundMatch {

        private final int from;
        private final int to;

        public FoundMatch(int from, int to) {
            this.from = from;
            this.to = to;
        }

        public int getFrom() {
            return from;
        }

        public int getTo() {
            return to;
        }
    }
}
