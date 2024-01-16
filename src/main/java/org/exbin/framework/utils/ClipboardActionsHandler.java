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
package org.exbin.framework.utils;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Interface for clipboard handler for visual component / context menu.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public interface ClipboardActionsHandler {

    /**
     * Performs cut to clipboard operation.
     */
    void performCut();

    /**
     * Performs copy to clipboard operation.
     */
    void performCopy();

    /**
     * Performs paste from clipboard operation.
     */
    void performPaste();

    /**
     * Performs delete selection operation.
     */
    void performDelete();

    /**
     * Performs select all operation. (should include focus request)
     */
    void performSelectAll();

    /**
     * Returns if selection for clipboard operation is available.
     *
     * @return true if selection is available
     */
    boolean isSelection();

    /**
     * Returns whether it is possible to change components data using clipboard
     * operations.
     *
     * @return true if component is editable
     */
    boolean isEditable();

    /**
     * Returns whether it is possible to execute select all operation.
     *
     * @return true if can perform select all
     */
    boolean canSelectAll();

    /**
     * Returns whether it is possible to paste current content of the clipboard.
     *
     * @return true if can perform paste
     */
    boolean canPaste();

    /**
     * Returns whether it is possible to perform delete.
     *
     * @return true, if delete operation is allowed.
     */
    boolean canDelete();

    /**
     * Sets listener for clipboard actions related updates.
     *
     * @param updateListener update listener
     */
    void setUpdateListener(ClipboardActionsUpdateListener updateListener);
}
