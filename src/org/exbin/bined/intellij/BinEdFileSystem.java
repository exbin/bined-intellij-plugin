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

import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileListener;
import com.intellij.openapi.vfs.VirtualFileSystem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNullableByDefault;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * File system for binary editor.
 *
 * @author ExBin Project (http://exbin.org)
 * @version 0.2.3 2020/12/11
 */
@ParametersAreNullableByDefault
public class BinEdFileSystem extends VirtualFileSystem implements DumbAware {

    private static final String PROTOCOL = "bined";
    private static final String ERROR_INVALID_OPERATION = "Invalid operation";
    private static final BinEdFileSystem INSTANCE = new BinEdFileSystem();
    private List<VirtualFileListener> fileListeners = new ArrayList<>();

    @NotNull
    public static BinEdFileSystem getInstance() {
        return INSTANCE;
    }

    @NotNull
    @Override
    public String getProtocol() {
        return PROTOCOL;
    }

    @Nullable
    @Override
    public VirtualFile findFileByPath(@NotNull String s) {
        return null;
    }

    @Override
    public void refresh(boolean b) {
    }

    @Nullable
    @Override
    public VirtualFile refreshAndFindFileByPath(String s) {
        return null;
    }

    @Override
    public void addVirtualFileListener(VirtualFileListener virtualFileListener) {
        fileListeners.add(virtualFileListener);
    }

    @Override
    public void removeVirtualFileListener(VirtualFileListener virtualFileListener) {
        fileListeners.remove(virtualFileListener);
    }

    @Override
    protected void deleteFile(Object o, VirtualFile virtualFile) throws IOException {
        throw new RuntimeException(ERROR_INVALID_OPERATION);
    }

    @Override
    protected void moveFile(Object o, VirtualFile virtualFile, VirtualFile virtualFile1) throws IOException {
        throw new RuntimeException(ERROR_INVALID_OPERATION);
    }

    @Override
    protected void renameFile(Object o, VirtualFile virtualFile, String s) throws IOException {
        throw new RuntimeException(ERROR_INVALID_OPERATION);
    }

    @Nonnull
    @Override
    protected VirtualFile createChildFile(Object o, VirtualFile virtualFile, String s) throws IOException {
        throw new RuntimeException(ERROR_INVALID_OPERATION);
    }

    @Nonnull
    @Override
    protected VirtualFile createChildDirectory(Object o, VirtualFile virtualFile, String s) throws IOException {
        throw new RuntimeException(ERROR_INVALID_OPERATION);
    }

    @Nonnull
    @Override
    protected VirtualFile copyFile(Object o, VirtualFile virtualFile, VirtualFile virtualFile1, String s) throws IOException {
        throw new RuntimeException(ERROR_INVALID_OPERATION);
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }
}