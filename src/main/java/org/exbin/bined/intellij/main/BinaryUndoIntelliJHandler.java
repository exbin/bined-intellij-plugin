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
package org.exbin.bined.intellij.main;

import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.command.impl.UndoManagerImpl;
import com.intellij.openapi.command.undo.DocumentReference;
import com.intellij.openapi.command.undo.DocumentReferenceManager;
import com.intellij.openapi.command.undo.UndoManager;
import com.intellij.openapi.command.undo.UndoableAction;
import com.intellij.openapi.project.Project;
import org.exbin.bined.intellij.BinEdFileEditor;
import org.exbin.bined.operation.BinaryDataCommand;
import org.exbin.bined.operation.BinaryDataOperationException;
import org.exbin.bined.operation.undo.BinaryDataUndoHandler;
import org.exbin.bined.operation.undo.BinaryDataUndoUpdateListener;
import org.exbin.bined.swing.extended.ExtCodeArea;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import java.util.ArrayList;
import java.util.List;

/**
 * TODO: Undo handler for binary editor using IntelliJ Idea's undo.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class BinaryUndoIntelliJHandler implements BinaryDataUndoHandler {

    private final ExtCodeArea codeArea;
    private final List<BinaryDataUndoUpdateListener> listeners = new ArrayList<>();
    private final UndoManager undoManager;
    private final BinEdFileEditor fileEditor;
    private final Project project;
    private DocumentReference documentReference;
    private long commandPosition;
    private long syncPointPosition = -1;

    /**
     * Creates a new instance.
     *
     * @param codeArea binary component
     */
    public BinaryUndoIntelliJHandler(ExtCodeArea codeArea, Project project, BinEdFileEditor fileEditor) {
        this.codeArea = codeArea;
        this.fileEditor = fileEditor;
        this.project = project;
        undoManager = UndoManager.getInstance(project);
        init();
    }

    private void init() {
        commandPosition = 0;
        setSyncPoint(0);
    }

    /**
     * Adds new step into revert list.
     *
     * @param command command
     * @throws BinaryDataOperationException if commands throws it
     */
    @Override
    public void execute(BinaryDataCommand command) throws BinaryDataOperationException {
        command.execute();

        commandAdded(command);
    }

    @Override
    public void addCommand(BinaryDataCommand command) {
        command.use();
        commandAdded(command);
    }

    private void commandAdded(final BinaryDataCommand command) {
        UndoableAction action = new UndoableAction() {
            @Override
            public void undo() throws CannotUndoException {
                commandPosition--;
                try {
                    command.undo();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                undoUpdated();
            }

            @Override
            public void redo() throws CannotRedoException {
                commandPosition++;
                try {
                    command.redo();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                undoUpdated();
            }

            @Nullable
            @Override
            public DocumentReference[] getAffectedDocuments() {
                return new DocumentReference[]{documentReference};
            }

            @Override
            public boolean isGlobal() {
                return false;
            }
        };
        CommandProcessor commandProcessor = CommandProcessor.getInstance();
        commandProcessor.executeCommand(project, () -> undoManager.undoableActionPerformed(action), command.getCaption(), "BinEd");

        commandPosition++;
        undoUpdated();
        for (BinaryDataUndoUpdateListener listener : listeners) {
            listener.undoCommandAdded(command);
        }
    }

    /**
     * Performs single undo step.
     *
     * @throws BinaryDataOperationException if commands throws it
     */
    @Override
    public void performUndo() throws BinaryDataOperationException {
        performUndoInt();
        undoUpdated();
    }

    private void performUndoInt() throws BinaryDataOperationException {
        undoManager.undo(fileEditor);
    }

    /**
     * Performs single redo step.
     *
     * @throws BinaryDataOperationException if commands throws it
     */
    @Override
    public void performRedo() throws BinaryDataOperationException {
        performRedoInt();
        undoUpdated();
    }

    private void performRedoInt() throws BinaryDataOperationException {
        undoManager.redo(fileEditor);
    }

    /**
     * Performs multiple undo step.
     *
     * @param count count of steps
     * @throws BinaryDataOperationException if commands throws it
     */
    @Override
    public void performUndo(int count) throws BinaryDataOperationException {
        for (int i = 0; i < count; i++) {
            performUndo();
        }
    }

    /**
     * Performs multiple redo step.
     *
     * @param count count of steps
     * @throws BinaryDataOperationException if commands throws it
     */
    @Override
    public void performRedo(int count) throws BinaryDataOperationException {
        for (int i = 0; i < count; i++) {
            performRedo();
        }
    }

    @Override
    public void clear() {
        documentReference = DocumentReferenceManager.getInstance().create(fileEditor.getVirtualFile());
        ((UndoManagerImpl) undoManager).invalidateActionsFor(documentReference);
        init();
    }

    @Override
    public boolean canUndo() {
        return undoManager.isUndoAvailable(fileEditor);
    }

    @Override
    public boolean canRedo() {
        return undoManager.isRedoAvailable(fileEditor);
    }

    @Override
    public long getMaximumUndo() {
        return 0;
    }

    @Override
    public long getCommandPosition() {
        return commandPosition;
    }

    /**
     * Performs revert to sync point.
     *
     * @throws BinaryDataOperationException if commands throws it
     */
    @Override
    public void doSync() throws BinaryDataOperationException {
        setCommandPosition(syncPointPosition);
    }

    public void setUndoMaxCount(long maxUndo) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public long getUndoMaximumSize() {
        return 0;
    }

    public void setUndoMaximumSize(long maxSize) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public long getUsedSize() {
        return 0;
    }

    @Override
    public long getSyncPoint() {
        return syncPointPosition;
    }

    @Override
    public void setSyncPoint(long syncPoint) {
        this.syncPointPosition = syncPoint;
    }

    @Override
    public void setSyncPoint() {
        this.syncPointPosition = commandPosition;
    }

    @Override
    public List<BinaryDataCommand> getCommandList() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Performs undo or redo operation to reach given position.
     *
     * @param targetPosition desired position
     * @throws BinaryDataOperationException if commands throws it
     */
    @Override
    public void setCommandPosition(long targetPosition) throws BinaryDataOperationException {
        if (targetPosition < commandPosition) {
            performUndo((int) (commandPosition - targetPosition));
        } else if (targetPosition > commandPosition) {
            performRedo((int) (targetPosition - commandPosition));
        }
    }

    private void undoUpdated() {
        for (BinaryDataUndoUpdateListener listener : listeners) {
            listener.undoCommandPositionChanged();
        }
    }

    @Override
    public void addUndoUpdateListener(BinaryDataUndoUpdateListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeUndoUpdateListener(BinaryDataUndoUpdateListener listener) {
        listeners.remove(listener);
    }
}
