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

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.vcs.vfs.VcsFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileListener;
import com.intellij.openapi.vfs.VirtualFileSystem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

/**
 * File system for hexadecimal editor.
 *
 * @author ExBin Project (http://exbin.org)
 * @version 0.1.0 2016/12/11
 */
public class DeltaHexFileSystem extends VirtualFileSystem implements ApplicationComponent {

    private static final String PROTOCOL = "deltahex";

    public static DeltaHexFileSystem getInstance() {
        return ApplicationManager.getApplication().getComponent(DeltaHexFileSystem.class);
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
    public VirtualFile refreshAndFindFileByPath(@NotNull String s) {
        return null;
    }

    @Override
    public void addVirtualFileListener(@NotNull VirtualFileListener virtualFileListener) {

    }

    @Override
    public void removeVirtualFileListener(@NotNull VirtualFileListener virtualFileListener) {

    }

    @Override
    protected void deleteFile(Object o, @NotNull VirtualFile virtualFile) throws IOException {
        throw new RuntimeException(VcsFileSystem.COULD_NOT_IMPLEMENT_MESSAGE);
    }

    @Override
    protected void moveFile(Object o, @NotNull VirtualFile virtualFile, @NotNull VirtualFile virtualFile1) throws IOException {
        throw new RuntimeException(VcsFileSystem.COULD_NOT_IMPLEMENT_MESSAGE);
    }

    @Override
    protected void renameFile(Object o, @NotNull VirtualFile virtualFile, @NotNull String s) throws IOException {
        throw new RuntimeException(VcsFileSystem.COULD_NOT_IMPLEMENT_MESSAGE);
    }

    @NotNull
    @Override
    protected VirtualFile createChildFile(Object o, @NotNull VirtualFile virtualFile, @NotNull String s) throws IOException {
        throw new RuntimeException(VcsFileSystem.COULD_NOT_IMPLEMENT_MESSAGE);
    }

    @NotNull
    @Override
    protected VirtualFile createChildDirectory(Object o, @NotNull VirtualFile virtualFile, @NotNull String s) throws IOException {
        throw new RuntimeException(VcsFileSystem.COULD_NOT_IMPLEMENT_MESSAGE);
    }

    @NotNull
    @Override
    protected VirtualFile copyFile(Object o, @NotNull VirtualFile virtualFile, @NotNull VirtualFile virtualFile1, @NotNull String s) throws IOException {
        throw new RuntimeException(VcsFileSystem.COULD_NOT_IMPLEMENT_MESSAGE);
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    public void initComponent() {

    }

    @Override
    public void disposeComponent() {

    }

    @NotNull
    @Override
    public String getComponentName() {
        return "DeltaHex.DeltaHexFileSystem";
    }
}