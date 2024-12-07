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

import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.fileEditor.FileEditorStateLevel;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.UserDataHolder;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.openapi.vfs.VirtualFile;
import org.exbin.framework.App;
import org.exbin.framework.bined.BinedModule;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.JComponent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * Native file editor using BinEd editor component.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class BinEdNativeFileEditor implements FileEditor, DumbAware {

    private final Project project;
    private final UserDataHolder userDataHolder = new UserDataHolderBase();

    private final PropertyChangeSupport propertyChangeSupport;
    private String displayName;
    private BinEdNativeFile nativeFile;
    private BinEdFileEditorState fileEditorState = new BinEdFileEditorState();

    public BinEdNativeFileEditor(Project project, final VirtualFile virtualFile) {
        this.project = project;
        this.nativeFile = new BinEdNativeFile();
        nativeFile.openFile(virtualFile);
        BinaryUndoIntelliJHandler undoHandler = new BinaryUndoIntelliJHandler();
        undoHandler.setFileEditor(this);
        nativeFile.registerUndoRedo(undoHandler);

        propertyChangeSupport = new PropertyChangeSupport(this);
    }

    @Nonnull
    @Override
    public JComponent getComponent() {
        return nativeFile.getComponent();
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
    public FileEditorState getState(FileEditorStateLevel level) {
        return fileEditorState;
    }

    @Override
    public void setState(FileEditorState state) {
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
        BinedModule binedModule = App.getModule(BinedModule.class);
        ((BinEdIntelliJEditorProvider) binedModule.getEditorProvider()).setActiveFile(nativeFile.getEditorFile());
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

    @Override
    public void dispose() {
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
    public BinEdNativeFile getNativeFile() {
        return nativeFile;
    }

    @Nullable
    @Override
    public VirtualFile getFile() {
        return nativeFile.getVirtualFile();
    }

    @Nonnull
    public Project getProject() {
        return project;
    }
}
