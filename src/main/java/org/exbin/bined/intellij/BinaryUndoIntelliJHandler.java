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
package org.exbin.bined.intellij;

import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.command.impl.UndoManagerImpl;
import com.intellij.openapi.command.undo.DocumentReference;
import com.intellij.openapi.command.undo.DocumentReferenceManager;
import com.intellij.openapi.command.undo.UndoManager;
import com.intellij.openapi.command.undo.UndoableAction;
import com.intellij.openapi.project.Project;
import org.exbin.bined.operation.BinaryDataCommand;
import org.exbin.bined.operation.undo.BinaryDataUndoRedo;
import org.exbin.bined.operation.undo.BinaryDataUndoRedoChangeListener;
import org.exbin.bined.swing.extended.ExtCodeArea;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * TODO: Undo handler for binary editor using IntelliJ Idea's undo.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class BinaryUndoIntelliJHandler implements BinaryDataUndoRedo {

    private final ExtCodeArea codeArea;
    private final List<BinaryDataUndoRedoChangeListener> listeners = new ArrayList<>();
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
        setSyncPosition(0);
    }

    @Override
    public void execute(BinaryDataCommand command) {
        command.execute();
        commandAdded(command);
    }

    @Nonnull
    @Override
    public Optional<BinaryDataCommand> getTopUndoCommand() {
        throw new UnsupportedOperationException();
    }

    private void commandAdded(final BinaryDataCommand command) {
        UndoableAction action = new UndoableAction() {
            @Override
            public void undo() throws CannotUndoException {
                commandPosition--;
                try {
                    command.undo();
                } catch (Exception ex) {
                    Logger.getLogger(BinaryUndoIntelliJHandler.class.getName()).log(Level.SEVERE, null, ex);
                }
                undoUpdated();
            }

            @Override
            public void redo() throws CannotRedoException {
                commandPosition++;
                try {
                    command.redo();
                } catch (Exception ex) {
                    Logger.getLogger(BinaryUndoIntelliJHandler.class.getName()).log(Level.SEVERE, null, ex);
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
        commandProcessor.executeCommand(project, () -> undoManager.undoableActionPerformed(action), command.getName(), "BinEd");

        commandPosition++;
        undoUpdated();
        for (BinaryDataUndoRedoChangeListener listener : listeners) {
            listener.undoChanged();
        }
    }

    @Override
    public void performUndo() {
        performUndoInt();
        undoUpdated();
    }

    private void performUndoInt() {
        undoManager.undo(fileEditor);
    }

    @Override
    public void performRedo() {
        performRedoInt();
        undoUpdated();
    }

    private void performRedoInt() {
        undoManager.redo(fileEditor);
    }

    @Override
    public void performUndo(int count) {
        for (int i = 0; i < count; i++) {
            performUndo();
        }
    }

    @Override
    public void performRedo(int count) {
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
    public long getCommandPosition() {
        return commandPosition;
    }

    @Override
    public long getCommandsCount() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void performSync() {
        setCommandPosition(syncPointPosition);
    }

    @Override
    public long getSyncPosition() {
        return syncPointPosition;
    }

    @Override
    public void setSyncPosition(long syncPoint) {
        this.syncPointPosition = syncPoint;
    }

    @Override
    public void setSyncPosition() {
        this.syncPointPosition = commandPosition;
    }

    @Nonnull
    @Override
    public List<BinaryDataCommand> getCommandList() {
        throw new UnsupportedOperationException();
    }

    /**
     * Performs undo or redo operation to reach given position.
     *
     * @param targetPosition desired position
     */
    public void setCommandPosition(long targetPosition) {
        if (targetPosition < commandPosition) {
            performUndo((int) (commandPosition - targetPosition));
        } else if (targetPosition > commandPosition) {
            performRedo((int) (targetPosition - commandPosition));
        }
    }

    private void undoUpdated() {
        for (BinaryDataUndoRedoChangeListener listener : listeners) {
            listener.undoChanged();
        }
    }

    @Override
    public void addChangeListener(BinaryDataUndoRedoChangeListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeChangeListener(BinaryDataUndoRedoChangeListener listener) {
        listeners.remove(listener);
    }

    @Override
    public boolean isModified() {
        return commandPosition != syncPointPosition;
    }
}
