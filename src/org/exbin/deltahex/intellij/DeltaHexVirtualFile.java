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

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileSystem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Virtual file for hexadecimal editor.
 *
 * @author ExBin Project (http://exbin.org)
 * @version 0.1.0 2016/12/11
 */
public class DeltaHexVirtualFile extends VirtualFile {

    private String fileUrl;

    public DeltaHexVirtualFile(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    @NotNull
    @Override
    public String getName() {
        return "Test";
    }

    @NotNull
    @Override
    public VirtualFileSystem getFileSystem() {
        return DeltaHexFileSystem.getInstance();
    }

    @NotNull
    @Override
    public String getPath() {
        return fileUrl;
    }

    @Override
    public boolean isWritable() {
        return true;
    }

    @Override
    public boolean isDirectory() {
        return false;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public VirtualFile getParent() {
        return null;
    }

    @Override
    public VirtualFile[] getChildren() {
        return new VirtualFile[0];
    }

    @NotNull
    @Override
    public OutputStream getOutputStream(Object requestor, long newModificationStamp, long newTimeStamp) throws IOException {
        return new ByteArrayOutputStream();
    }

    @NotNull
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
        return null;
    }

    public long getModificationStamp() {
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DeltaHexVirtualFile that = (DeltaHexVirtualFile) o;

        if (fileUrl != null ? !fileUrl.equals(that.fileUrl) : that.fileUrl != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return fileUrl != null ? fileUrl.hashCode() : 0;
    }
}