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
package org.exbin.framework.bined;

import java.util.ArrayList;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.event.DocumentEvent;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.AbstractDocument;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoableEdit;

/**
 * Undo manager with compound operations.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class BinaryPanelCompoundUndoManager extends AbstractUndoableEdit implements UndoableEditListener {

    private DocumentEvent.EventType lastEditType = null;
    private final ArrayList<MyCompoundEdit> edits = new ArrayList<>();
    private MyCompoundEdit current;
    private int pointer = -1;
    private int lastOffset = -1;

    public BinaryPanelCompoundUndoManager() {
    }

    @Override
    public void undoableEditHappened(UndoableEditEvent editEvent) {
        UndoableEdit edit = editEvent.getEdit();
        if (edit instanceof AbstractDocument.DefaultDocumentEvent) {
            DocumentEvent.EventType editType = ((AbstractDocument.DefaultDocumentEvent) edit).getType();
            AbstractDocument.DefaultDocumentEvent event = (AbstractDocument.DefaultDocumentEvent) edit;
            int offset = event.getOffset();
            boolean isNeedStart = false;
            if (current == null) {
                isNeedStart = true;
            } else if (lastEditType == null || !lastEditType.equals(editType)) {
                isNeedStart = true;
            } else if (lastEditType == DocumentEvent.EventType.INSERT) {
                if (offset != lastOffset + 1) {
                    isNeedStart = true;
                }
            } else if (lastEditType == DocumentEvent.EventType.REMOVE) {
                if (offset != lastOffset - 1) {
                    isNeedStart = true;
                }
            }

            while (pointer < edits.size() - 1) {
                edits.remove(edits.size() - 1);
                isNeedStart = true;
            }
            if (isNeedStart) {
                createCompoundEdit();
            }
            current.addEdit(edit);
            lastEditType = editType;
            lastOffset = offset;
        }
    }

    public void createCompoundEdit() {
        if (current == null) {
            current = new MyCompoundEdit();
        } else if (current.getLength() > 0) {
            current = new MyCompoundEdit();
        }
        edits.add(current);
        pointer++;
    }

    @Override
    public void undo() throws CannotUndoException {
        if (!canUndo()) {
            throw new CannotUndoException();
        }
        MyCompoundEdit u = edits.get(pointer);
        u.undo();
        pointer--;

        lastOffset = -1;
        lastEditType = null;
    }

    @Override
    public void redo() throws CannotUndoException {
        if (!canRedo()) {
            throw new CannotUndoException();
        }
        pointer++;
        MyCompoundEdit u = edits.get(pointer);
        u.redo();

        lastOffset = -1;
        lastEditType = null;
    }

    @Override
    public boolean canUndo() {
        return pointer >= 0;
    }

    @Override
    public boolean canRedo() {
        return !edits.isEmpty() && pointer < edits.size() - 1;
    }

    class MyCompoundEdit extends CompoundEdit {

        boolean isUnDone = false;

        public int getLength() {
            return edits.size();
        }

        @Override
        public void undo() throws CannotUndoException {
            super.undo();
            isUnDone = true;
        }

        @Override
        public void redo() throws CannotUndoException {
            super.redo();
            isUnDone = false;
        }

        @Override
        public boolean canUndo() {
            return !edits.isEmpty() && !isUnDone;
        }

        @Override
        public boolean canRedo() {
            return !edits.isEmpty() && isUnDone;
        }
    }
}
