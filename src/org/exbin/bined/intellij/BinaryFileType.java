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

import com.intellij.icons.AllIcons;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.vfs.CharsetToolkit;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNullableByDefault;
import javax.swing.*;

/**
 * File type for binary files.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNullableByDefault
public class BinaryFileType implements FileType, DumbAware {

    public static final String DEFAULT_EXTENSION = "bin";
    public static final BinaryFileType INSTANCE = new BinaryFileType();

    private BinaryFileType() {
    }

    @NotNull
    @Override
    public String getName() {
        return "Binary File";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Binary File (opened by BinEd Plugin)";
    }

    @NotNull
    @Override
    public String getDefaultExtension() {
        return DEFAULT_EXTENSION;
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return AllIcons.FileTypes.Any_type;
    }

    @Override
    public boolean isBinary() {
        return true;
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Nullable
    @Override
    public String getCharset(@NotNull VirtualFile file, @NotNull byte[] content) {
        return "US-ASCII";
    }
}
