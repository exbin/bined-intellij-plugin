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

import com.intellij.openapi.command.undo.DocumentReference;
import com.intellij.openapi.command.undo.UndoManager;
import com.intellij.openapi.command.undo.UndoableAction;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Undo/redo manager for hexadecimal editor.
 *
 * @author ExBin Project (http://exbin.org)
 * @version 0.1.0 2016/12/11
 */
public class DeltaHexUndoManager extends UndoManager {

    @Override
    public void undoableActionPerformed(@NotNull UndoableAction action) {

    }

    @Override
    public void nonundoableActionPerformed(@NotNull DocumentReference ref, boolean isGlobal) {

    }

    @Override
    public boolean isUndoInProgress() {
        return false;
    }

    @Override
    public boolean isRedoInProgress() {
        return false;
    }

    @Override
    public void undo(@Nullable FileEditor editor) {

    }

    @Override
    public void redo(@Nullable FileEditor editor) {

    }

    @Override
    public boolean isUndoAvailable(@Nullable FileEditor editor) {
        return false;
    }

    @Override
    public boolean isRedoAvailable(@Nullable FileEditor editor) {
        return false;
    }

    @NotNull
    @Override
    public Pair<String, String> getUndoActionNameAndDescription(FileEditor editor) {
        return null;
    }

    @NotNull
    @Override
    public Pair<String, String> getRedoActionNameAndDescription(FileEditor editor) {
        return null;
    }
}