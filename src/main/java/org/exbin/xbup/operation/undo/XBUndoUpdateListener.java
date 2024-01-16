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
package org.exbin.xbup.operation.undo;

import javax.annotation.Nonnull;
import org.exbin.xbup.operation.Command;

/**
 * Undo update listener.
 *
 * @author ExBin Project (https://exbin.org)
 */
public interface XBUndoUpdateListener {

    /**
     * Notifies about change in undo state.
     */
    void undoCommandPositionChanged();

    /**
     * Reports new command added to undo sequence.
     *
     * @param command command
     */
    void undoCommandAdded(@Nonnull Command command);
}
