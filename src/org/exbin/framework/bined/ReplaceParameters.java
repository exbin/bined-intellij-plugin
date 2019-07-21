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
 * Parameters for action to replace for occurences of text or data.
 *
 * @version 0.2.0 2016/12/21
 * @author ExBin Project (http://exbin.org)
 */
public class ReplaceParameters {

    private SearchCondition condition = new SearchCondition();
    private boolean performReplace;
    private boolean replaceAll;

    public ReplaceParameters() {
    }

    public SearchCondition getCondition() {
        return condition;
    }

    public void setCondition(SearchCondition condition) {
        this.condition = condition;
    }

    public void setFromParameters(ReplaceParameters replaceParameters) {
        condition = replaceParameters.getCondition();
    }

    public boolean isPerformReplace() {
        return performReplace;
    }

    public void setPerformReplace(boolean performReplace) {
        this.performReplace = performReplace;
    }

    public boolean isReplaceAll() {
        return replaceAll;
    }

    public void setReplaceAll(boolean replaceAll) {
        this.replaceAll = replaceAll;
    }

    public static enum ReplaceMode {
        TEXT, BINARY
    }
}
