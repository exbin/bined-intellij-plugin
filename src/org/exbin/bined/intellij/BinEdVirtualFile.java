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

import com.intellij.openapi.fileEditor.impl.FileEditorManagerImpl;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileSystem;
import org.exbin.framework.bined.BinEdFileHandler;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.JComponent;
import javax.swing.JPanel;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Virtual file for binary editor.
 *
 * @author ExBin Project (http://exbin.org)
 * @version 0.2.4 2021/04/11
 */
@ParametersAreNonnullByDefault
public class BinEdVirtualFile extends VirtualFile implements DumbAware {

    public static final String PATH_PREFIX = "bined://";

    private final VirtualFile parentFile;
    private String displayName;
    private BinEdFileHandler editorFile;
    private boolean closed = false;

    public BinEdVirtualFile(VirtualFile parentFile) {
        if (parentFile.getPath().startsWith(PATH_PREFIX)) {
            this.parentFile = LocalFileSystem.getInstance().findFileByPath(parentFile.getPath().substring(PATH_PREFIX.length()));
        } else {
            this.parentFile = parentFile;
        }
        String path = parentFile.getPath();
        int lastIndexOf = path.lastIndexOf('/');
        if (lastIndexOf >= 0) {
            this.displayName = path.substring(lastIndexOf + 1);
        } else {
            this.displayName = "";
        }
    }

    @Nonnull
    public BinEdFileHandler getEditorFile() {
        if (editorFile == null) {
            editorFile = new BinEdFileHandler();
        }

        return editorFile;
    }

    @Nonnull
    public JPanel getEditorPanel() {
        return getEditorFile().getPanel();
    }

    @Nonnull
    @Override
    public String getName() {
        return parentFile.getName();
    }

    public String getDisplayName() {
        return displayName;
    }

    @Nonnull
    @Override
    public VirtualFileSystem getFileSystem() {
        return BinEdFileSystem.getInstance();
    }

    @Nonnull
    @Override
    public String getPath() {
        return PATH_PREFIX + parentFile.getPath();
    }

    @Override
    public boolean isWritable() {
        return parentFile.isWritable();
    }

    @Override
    public boolean isDirectory() {
        return parentFile.isDirectory();
    }

    @Override
    public boolean isValid() {
        return parentFile.isValid();
    }

    @Override
    public VirtualFile getParent() {
        return parentFile.getParent();
    }

    @Override
    public VirtualFile[] getChildren() {
        return parentFile.getChildren();
    }

    @Nonnull
    @Override
    public OutputStream getOutputStream(Object requester, long newModificationStamp, long newTimeStamp) throws IOException {
        return parentFile.getOutputStream(requester, newModificationStamp, newTimeStamp);
    }

    @Nonnull
    @Override
    public byte[] contentsToByteArray() throws IOException {
        return parentFile.contentsToByteArray();
    }

    @Override
    public long getTimeStamp() {
        return parentFile.getTimeStamp();
    }

    @Override
    public long getLength() {
        return parentFile.getLength();
    }

    @Override
    public void refresh(boolean asynchronous, boolean recursive, @Nullable Runnable postRunnable) {
        parentFile.refresh(asynchronous, recursive, postRunnable);
    }

    @Nonnull
    @Override
    public InputStream getInputStream() throws IOException {
        return parentFile.getInputStream();
    }

    public long getModificationStamp() {
        return parentFile.getModificationStamp();
    }

    @Override
    public long getModificationCount() {
        return parentFile.getModificationCount();
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BinEdVirtualFile that = (BinEdVirtualFile) o;
        String path = getPath();
        return path.equals(that.getPath());
    }

    @Override
    public int hashCode() {
        return getPath().hashCode();
    }

    public boolean isMoved() {
        Boolean closingToReopen = getUserData(FileEditorManagerImpl.CLOSING_TO_REOPEN);
        return closingToReopen != null && closingToReopen;
    }

    public boolean isClosed() {
        return closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    public JComponent getPreferredFocusedComponent() {
        return getEditorFile().getPreferredFocusedComponent();
    }
}