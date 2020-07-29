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

import com.intellij.codeHighlighting.BackgroundEditorHighlighter;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorLocation;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.fileEditor.FileEditorStateLevel;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * Native file editor using BinEd editor component.
 *
 * @author ExBin Project (http://exbin.org)
 * @version 0.2.2 2020/07/29
 */
@ParametersAreNonnullByDefault
public class BinEdNativeFileEditor implements FileEditor, DumbAware {

    private final Project project;

    private final PropertyChangeSupport propertyChangeSupport;
    private String displayName;
    private BinEdNativeFile nativeFile;
    private BinEdFileEditorState fileEditorState = new BinEdFileEditorState();

    public BinEdNativeFileEditor(Project project, final VirtualFile virtualFile) {
        this.project = project;
        this.nativeFile = new BinEdNativeFile(virtualFile);

        propertyChangeSupport = new PropertyChangeSupport(this);
    }

    @Nonnull
    @Override
    public JComponent getComponent() {
        return nativeFile.getPanel();
    }

    @Nullable
    @Override
    public JComponent getPreferredFocusedComponent() {
        return nativeFile.getPreferredFocusedComponent();
    }

    @Nonnull
    @Override
    public String getName() {
        return displayName;
    }

    @Nonnull
    @Override
    public FileEditorState getState(@Nonnull FileEditorStateLevel level) {
        return fileEditorState;
    }

    @Override
    public void setState(@Nonnull FileEditorState state) {
    }

    @Override
    public boolean isModified() {
        return nativeFile.isModified();
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void selectNotify() {

    }

    @Override
    public void deselectNotify() {

    }

    @Override
    public void addPropertyChangeListener(@Nonnull PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(@Nonnull PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    @Nullable
    @Override
    public BackgroundEditorHighlighter getBackgroundHighlighter() {
        return null;
    }

    @Nullable
    @Override
    public FileEditorLocation getCurrentLocation() {
        return null;
//        return new TextEditorLocation(codeArea.getCaretPosition(), this);
    }

    @Override
    public void dispose() {
    }

    @Nullable
    @Override
    public <T> T getUserData(@Nonnull Key<T> key) {
        return null;
    }

    @Override
    public <T> void putUserData(@Nonnull Key<T> key, @Nullable T value) {
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public BinEdNativeFile getNativeFile() {
        return nativeFile;
    }

    public Project getProject() {
        return project;
    }
}
