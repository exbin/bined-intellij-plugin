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
package org.exbin.deltahex.operation.undo;

import java.util.List;
import org.exbin.deltahex.operation.BinaryDataCommand;
import org.exbin.deltahex.operation.BinaryDataOperationException;

/**
 * Code area undo support handler.
 *
 * @version 0.1.2 2016/12/20
 * @author ExBin Project (http://exbin.org)
 */
public interface BinaryDataUndoHandler {

    boolean canRedo();

    boolean canUndo();

    void clear();

    /**
     * Performs revert to sync point.
     *
     * @throws BinaryDataOperationException for operation handling issues
     */
    void doSync() throws BinaryDataOperationException;

    /**
     * Adds new step into command list.
     *
     * @param command command
     * @throws BinaryDataOperationException for operation handling issues
     */
    void execute(BinaryDataCommand command) throws BinaryDataOperationException;

    /**
     * Adds new step into command list without executing it.
     *
     * @param command command
     */
    void addCommand(BinaryDataCommand command);

    List<BinaryDataCommand> getCommandList();

    long getCommandPosition();

    long getMaximumUndo();

    long getSyncPoint();

    long getUndoMaximumSize();

    long getUsedSize();

    /**
     * Performs single redo step.
     *
     * @throws BinaryDataOperationException for operation handling issues
     */
    void performRedo() throws BinaryDataOperationException;

    /**
     * Performs multiple redo step.
     *
     * @param count count of steps
     * @throws BinaryDataOperationException for operation handling issues
     */
    void performRedo(int count) throws BinaryDataOperationException;

    /**
     * Performs single undo step.
     *
     * @throws BinaryDataOperationException for operation handling issues
     */
    void performUndo() throws BinaryDataOperationException;

    /**
     * Performs multiple undo step.
     *
     * @param count count of steps
     * @throws BinaryDataOperationException for operation handling issues
     */
    void performUndo(int count) throws BinaryDataOperationException;

    /**
     * Performs undo or redo operation to reach given position.
     *
     * @param targetPosition desired position
     * @throws BinaryDataOperationException for operation handling issues
     */
    void setCommandPosition(long targetPosition) throws BinaryDataOperationException;

    void setSyncPoint(long syncPoint);

    void setSyncPoint();

    void addUndoUpdateListener(BinaryDataUndoUpdateListener listener);

    void removeUndoUpdateListener(BinaryDataUndoUpdateListener listener);
}
