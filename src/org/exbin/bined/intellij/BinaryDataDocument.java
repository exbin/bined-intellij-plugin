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
package org.exbin.bined.intellij;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.RangeMarker;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.TextRange;
import org.exbin.utils.binary_data.EditableBinaryData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Binary data document wrapper.
 *
 * @author ExBin Project (http://exbin.org)
 * @version 0.2.0 2019/05/03
 */
public class BinaryDataDocument implements Document {

    private final List<DocumentListener> documentListeners = new ArrayList<>();
    private final List<PropertyChangeListener> propertyChangeListeners = new ArrayList<>();

    private final EditableBinaryData data;
    private boolean isReadOnly = false;

    public BinaryDataDocument(EditableBinaryData data) {
        this.data = data;
    }

    @NotNull
    @Override
    public String getText() {
        return "";
    }

    @NotNull
    @Override
    public String getText(@NotNull TextRange range) {
        return "";
    }

    @NotNull
    @Override
    public CharSequence getCharsSequence() {
        return null;
    }

    @NotNull
    @Override
    public CharSequence getImmutableCharSequence() {
        return null;
    }

    @NotNull
    @Override
    public char[] getChars() {
        return null;
    }

    @Override
    public int getTextLength() {
        return 0;
    }

    @Override
    public int getLineCount() {
        return 0;
    }

    @Override
    public int getLineNumber(int offset) {
        return 0;
    }

    @Override
    public int getLineStartOffset(int line) {
        return 0;
    }

    @Override
    public int getLineEndOffset(int line) {
        return 0;
    }

    @Override
    public void insertString(int offset, @NotNull CharSequence s) {

    }

    @Override
    public void deleteString(int startOffset, int endOffset) {

    }

    @Override
    public void replaceString(int startOffset, int endOffset, @NotNull CharSequence s) {

    }

    @Override
    public boolean isWritable() {
        return !isReadOnly;
    }

    @Override
    public long getModificationStamp() {
        return -1;
    }

    @Override
    public void fireReadOnlyModificationAttempt() {

    }

    @Override
    public void addDocumentListener(@NotNull DocumentListener listener) {
        documentListeners.add(listener);
    }

    @Override
    public void addDocumentListener(@NotNull DocumentListener listener, @NotNull Disposable parentDisposable) {
        documentListeners.add(listener);
    }

    @Override
    public void removeDocumentListener(@NotNull DocumentListener listener) {
        documentListeners.remove(listener);
    }

    @NotNull
    @Override
    public RangeMarker createRangeMarker(int startOffset, int endOffset) {
        return null;
    }

    @NotNull
    @Override
    public RangeMarker createRangeMarker(int startOffset, int endOffset, boolean surviveOnExternalChange) {
        return null;
    }

    @Override
    public void addPropertyChangeListener(@NotNull PropertyChangeListener listener) {
        propertyChangeListeners.add(listener);
    }

    @Override
    public void removePropertyChangeListener(@NotNull PropertyChangeListener listener) {
        propertyChangeListeners.remove(listener);
    }

    @Override
    public void setReadOnly(boolean isReadOnly) {
        this.isReadOnly = isReadOnly;
    }

    @NotNull
    @Override
    public RangeMarker createGuardedBlock(int startOffset, int endOffset) {
        return null;
    }

    @Override
    public void removeGuardedBlock(@NotNull RangeMarker block) {

    }

    @Nullable
    @Override
    public RangeMarker getOffsetGuard(int offset) {
        return null;
    }

    @Nullable
    @Override
    public RangeMarker getRangeGuard(int start, int end) {
        return null;
    }

    @Override
    public void startGuardedBlockChecking() {

    }

    @Override
    public void stopGuardedBlockChecking() {

    }

    @Override
    public void setCyclicBufferSize(int bufferSize) {

    }

    @Override
    public void setText(@NotNull CharSequence text) {

    }

    @NotNull
    @Override
    public RangeMarker createRangeMarker(@NotNull TextRange textRange) {
        return null;
    }

    @Override
    public int getLineSeparatorLength(int line) {
        return 0;
    }

    @Nullable
    @Override
    public <T> T getUserData(@NotNull Key<T> key) {
        return null;
    }

    @Override
    public <T> void putUserData(@NotNull Key<T> key, @Nullable T value) {
    }
}
