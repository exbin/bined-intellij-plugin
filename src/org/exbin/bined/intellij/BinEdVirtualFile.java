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

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileSystem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Virtual file for hexadecimal editor.
 *
 * @author ExBin Project (http://exbin.org)
 * @version 0.1.3 2017/03/20
 */
public class BinEdVirtualFile extends VirtualFile {

    private final VirtualFile parentFile;
    private String displayName;

    public BinEdVirtualFile(VirtualFile parentFile) {
        this.parentFile = parentFile;
        String path = parentFile.getPath();
        int lastIndexOf = path.lastIndexOf('/');
        if (lastIndexOf >= 0) {
            this.displayName = path.substring(lastIndexOf + 1);
        } else {
            this.displayName = "";
        }
    }

    @NotNull
    @Override
    public String getName() {
        return parentFile.getName();
    }

    public String getDisplayName() {
        return displayName;
    }

    @NotNull
    @Override
    public VirtualFileSystem getFileSystem() {
        return BinEdFileSystem.getInstance();
    }

    @NotNull
    @Override
    public String getPath() {
        return parentFile.getPath();
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

    @NotNull
    @Override
    public OutputStream getOutputStream(Object requestor, long newModificationStamp, long newTimeStamp) throws IOException {
        return parentFile.getOutputStream(requestor, newModificationStamp, newTimeStamp);
    }

    @NotNull
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BinEdVirtualFile that = (BinEdVirtualFile) o;
        String path = getPath();
        return path != null ? path.equals(that.getPath()) : that.getPath() == null;
    }

    @Override
    public int hashCode() {
        String path = getPath();
        return path != null ? path.hashCode() : 0;
    }
}