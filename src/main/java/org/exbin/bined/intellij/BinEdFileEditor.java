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

import com.intellij.codeHighlighting.BackgroundEditorHighlighter;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorLocation;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.fileEditor.FileEditorStateLevel;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.UserDataHolder;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileSystem;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.JComponent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * File editor using BinEd editor component.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class BinEdFileEditor implements FileEditor, DumbAware {

    private final Project project;
    private final UserDataHolder userDataHolder = new UserDataHolderBase();

    private final PropertyChangeSupport propertyChangeSupport;
    private String displayName;
    private BinEdVirtualFile virtualFile;
    private BinEdFileEditorState fileEditorState = new BinEdFileEditorState();
    private static VirtualFile NULL_VIRTUAL_FILE = new NullVirtualFile();

    public BinEdFileEditor(Project project, final BinEdVirtualFile virtualFile) {
        this.project = project;
        this.virtualFile = virtualFile;

        propertyChangeSupport = new PropertyChangeSupport(this);

        // editorPanel.invalidate();
    }

    @Nonnull
    @Override
    public JComponent getComponent() {
        return virtualFile.getEditorComponent();
    }

    @Nullable
    @Override
    public JComponent getPreferredFocusedComponent() {
        return virtualFile.getPreferredFocusedComponent();
    }

    @Nonnull
    @Override
    public String getName() {
        return displayName;
    }

    @Nonnull
    @Override
    public FileEditorState getState(FileEditorStateLevel level) {
        return fileEditorState;
    }

    @Override
    public void setState(FileEditorState state) {
    }

    @Override
    public boolean isModified() {
        return virtualFile.getEditorFile().isModified();
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
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
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
        virtualFile.dispose();
    }

    @Nullable
    @Override
    public <T> T getUserData(Key<T> key) {
        return userDataHolder.getUserData(key);
    }

    @Override
    public <T> void putUserData(Key<T> key, @Nullable T value) {
        userDataHolder.putUserData(key, value);
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Nonnull
    public BinEdVirtualFile getVirtualFile() {
        return virtualFile;
    }

    @Nonnull
    @Override
    public VirtualFile getFile() {
        return NULL_VIRTUAL_FILE;
    }

    @Nonnull
    public Project getProject() {
        return project;
    }

    @ParametersAreNonnullByDefault
    private static class NullVirtualFile extends VirtualFile {
        @Nonnull
        @Override
        public String getName() {
            return "NULL";
        }

        @Nonnull
        @Override
        public VirtualFileSystem getFileSystem() {
            return new BinEdFileSystem();
        }

        @Nonnull
        @Override
        public String getPath() {
            return "";
        }

        @Override
        public boolean isWritable() {
            return false;
        }

        @Override
        public boolean isDirectory() {
            return false;
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Nullable
        @Override
        public VirtualFile getParent() {
            return null;
        }

        @Nonnull
        @Override
        public VirtualFile[] getChildren() {
            return new VirtualFile[0];
        }

        @Nonnull
        @Override
        public OutputStream getOutputStream(Object requestor, long newModificationStamp, long newTimeStamp)
                throws IOException {
            throw new UnsupportedOperationException();
        }

        @Nonnull
        @Override
        public byte[] contentsToByteArray() throws IOException {
            return new byte[0];
        }

        @Override
        public long getTimeStamp() {
            return 0;
        }

        @Override
        public long getLength() {
            return 0;
        }

        @Override
        public void refresh(boolean asynchronous, boolean recursive, @Nullable Runnable postRunnable) {
        }

        @Override
        public InputStream getInputStream() throws IOException {
            throw new UnsupportedOperationException();
        }
    }
}
