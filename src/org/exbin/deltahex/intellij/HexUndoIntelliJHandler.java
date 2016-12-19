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
package org.exbin.deltahex.intellij;

import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.command.undo.DocumentReference;
import com.intellij.openapi.command.undo.DocumentReferenceManager;
import com.intellij.openapi.command.undo.UndoManager;
import com.intellij.openapi.command.undo.UndoableAction;
import com.intellij.openapi.project.Project;
import org.exbin.deltahex.swing.CodeArea;
import org.exbin.xbup.operation.Command;
import org.exbin.xbup.operation.undo.XBUndoHandler;
import org.exbin.xbup.operation.undo.XBUndoUpdateListener;
import org.jetbrains.annotations.Nullable;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import java.util.ArrayList;
import java.util.List;

/**
 * Undo handler for hexadecimal editor using IntelliJ Idea's undo.
 *
 * @author ExBin Project (http://exbin.org)
 * @version 0.1.1 2016/12/19
 */
public class HexUndoIntelliJHandler implements XBUndoHandler {

    private final CodeArea codeArea;
    private final List<XBUndoUpdateListener> listeners = new ArrayList<>();
    private final UndoManager undoManager;
    private final DeltaHexFileEditor fileEditor;
    private final Project project;
    private DocumentReference documentReference;
    private long commandPosition;
    private long syncPointPosition = -1;

    /**
     * Creates a new instance.
     *
     * @param codeArea hexadecimal component
     */
    public HexUndoIntelliJHandler(CodeArea codeArea, Project project, DeltaHexFileEditor fileEditor) {
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
     * @throws java.lang.Exception if commands throws it
     */
    @Override
    public void execute(Command command) throws Exception {
        command.execute();

        commandAdded(command);
    }

    @Override
    public void addCommand(Command command) {
        command.use();
        commandAdded(command);
    }

    private void commandAdded(final Command command) {
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
                if (documentReference == null) {
                    documentReference = DocumentReferenceManager.getInstance().create(fileEditor.getVirtualFile());
                }

                return new DocumentReference[]{documentReference};
            }

            @Override
            public boolean isGlobal() {
                return false;
            }
        };
        CommandProcessor commandProcessor = CommandProcessor.getInstance();
        commandProcessor.executeCommand(project, new Runnable() {
            @Override
            public void run() {
                undoManager.undoableActionPerformed(action);
            }
        }, command.getCaption(), "DeltaHex");

        commandPosition++;
        undoUpdated();
        for (XBUndoUpdateListener listener : listeners) {
            listener.undoCommandAdded(command);
        }
    }

    /**
     * Performs single undo step.
     *
     * @throws java.lang.Exception if commands throws it
     */
    @Override
    public void performUndo() throws Exception {
        performUndoInt();
        undoUpdated();
    }

    private void performUndoInt() throws Exception {
        undoManager.undo(fileEditor);
    }

    /**
     * Performs single redo step.
     *
     * @throws java.lang.Exception if commands throws it
     */
    @Override
    public void performRedo() throws Exception {
        performRedoInt();
        undoUpdated();
    }

    private void performRedoInt() throws Exception {
        undoManager.redo(fileEditor);
    }

    /**
     * Performs multiple undo step.
     *
     * @param count count of steps
     * @throws Exception if commands throws it
     */
    @Override
    public void performUndo(int count) throws Exception {
        for (int i = 0; i < count; i++) {
            performUndo();
        }
    }

    /**
     * Performs multiple redo step.
     *
     * @param count count of steps
     * @throws Exception if commands throws it
     */
    @Override
    public void performRedo(int count) throws Exception {
        for (int i = 0; i < count; i++) {
            performRedo();
        }
    }

    @Override
    public void clear() {
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
     * @throws java.lang.Exception if commands throws it
     */
    @Override
    public void doSync() throws Exception {
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
    public List<Command> getCommandList() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Performs undo or redo operation to reach given position.
     *
     * @param targetPosition desired position
     * @throws java.lang.Exception if commands throws it
     */
    @Override
    public void setCommandPosition(long targetPosition) throws Exception {
        if (targetPosition < commandPosition) {
            performUndo((int) (commandPosition - targetPosition));
        } else if (targetPosition > commandPosition) {
            performRedo((int) (targetPosition - commandPosition));
        }
    }

    private void undoUpdated() {
        for (XBUndoUpdateListener listener : listeners) {
            listener.undoCommandPositionChanged();
        }
    }

    @Override
    public void addUndoUpdateListener(XBUndoUpdateListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeUndoUpdateListener(XBUndoUpdateListener listener) {
        listeners.remove(listener);
    }
}
