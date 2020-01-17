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
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.messages.MessageBusConnection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;

/**
 * File editor using BinEd editor component.
 *
 * @author ExBin Project (http://exbin.org)
 * @version 0.2.2 2020/01/17
 */
@ParametersAreNonnullByDefault
public class BinEdFileEditor implements FileEditor {

    private final Project project;

    private final PropertyChangeSupport propertyChangeSupport;
    private String displayName;
    private BinEdVirtualFile virtualFile;
    private BinEdFileEditorState fileEditorState = new BinEdFileEditorState();

    public BinEdFileEditor(Project project, final BinEdVirtualFile virtualFile) {
        this.project = project;
        this.virtualFile = virtualFile;

        propertyChangeSupport = new PropertyChangeSupport(this);

        MessageBus messageBus = project.getMessageBus();
        MessageBusConnection connect = messageBus.connect();
        connect.subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, new FileEditorManagerListener() {
            @Override
            public void fileClosed(@Nonnull FileEditorManager source, @Nonnull VirtualFile virtualFile) {
                if (virtualFile instanceof BinEdVirtualFile && !((BinEdVirtualFile) virtualFile).isMoved() && !((BinEdVirtualFile) virtualFile).isClosed()) {
                    ((BinEdVirtualFile) virtualFile).setClosed(true);
                    BinEdEditorPanel editorPanel = ((BinEdVirtualFile) virtualFile).getEditorPanel();
                    if (!editorPanel.releaseFile()) {
                        // TODO Intercept close event instead of editor recreation
                        OpenFileDescriptor descriptor = new OpenFileDescriptor(project, virtualFile, 0);
                        FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
                        List<FileEditor> editors = fileEditorManager.openEditor(descriptor, true);
                        fileEditorManager.setSelectedEditor(virtualFile, BinEdFileEditorProvider.BINED_EDITOR_TYPE_ID);
//                        for (FileEditor fileEditor : editors) {
//                            if (fileEditor instanceof BinEdFileEditor) {
//                                // ((BinEdFileEditor) fileEditor).editorPanel.reopenFile(virtualFile);
//                            }
//                        }
                        // editorPanel.closeData(false);
                    } else {
                        Application application = ApplicationManager.getApplication();
                        ApplicationManager.getApplication().invokeLater(() -> {
                            editorPanel.closeData(true);
                        });
                    }
                }
            }
        });

//        FileDocumentManager fileDocumentManager = FileDocumentManager.getInstance();
//        connect.subscribe(AppTopics.FILE_DOCUMENT_SYNC, new FileDocumentManagerAdapter() {
//            @Override
//            public void beforeDocumentSaving(Document document) {
//                if (virtualFile != null) {
//                    BinEdEditorPanel editorPanel = virtualFile.getEditorPanel();
//                    if (!editorPanel.releaseFile()) {
//                        // TODO Intercept close event instead of editor recreation
//                        OpenFileDescriptor descriptor = new OpenFileDescriptor(project, virtualFile, 0);
//                        FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
//                        List<FileEditor> editors = fileEditorManager.openEditor(descriptor, true);
//                        fileEditorManager.setSelectedEditor(virtualFile, BinEdFileEditorProvider.BINED_EDITOR_TYPE_ID);
//                        for (FileEditor fileEditor : editors) {
//                            if (fileEditor instanceof BinEdFileEditor) {
//                                // ((BinEdFileEditor) fileEditor).reopenFile(virtualFile, codeArea.getContentData(), undoHandler);
//                            }
//                        }
//                        editorPanel.closeData(false);
//                    } else {
//                        editorPanel.closeData(true);
//                    }
//                }
//
////                virtualFile = null;
//            }
//        });

//        editorPanel.invalidate();
    }

    @Nonnull
    @Override
    public JComponent getComponent() {
        return virtualFile.getEditorPanel();
    }

    @Nullable
    @Override
    public JComponent getPreferredFocusedComponent() {
        return virtualFile.getEditorPanel();
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
        return virtualFile.getEditorPanel().isModified();
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

    public BinEdVirtualFile getVirtualFile() {
        return virtualFile;
    }

    public Project getProject() {
        return project;
    }
}
