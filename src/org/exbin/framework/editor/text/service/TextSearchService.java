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
package org.exbin.framework.editor.text.service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;
import javax.swing.JTextArea;

/**
 * Text handling service.
 *
 * @author ExBin Project (https://exbin.org)
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
