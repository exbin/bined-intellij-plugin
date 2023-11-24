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
package org.exbin.framework.file.api;

import java.net.URI;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.JComponent;

/**
 * Interface for file handling.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public interface FileHandler extends FileLoading, FileSaving {

    /**
     * Returns unique identifier of the file.
     *
     * @return id
     */
    int getId();

    /**
     * Returns component for the file.
     *
     * @return component
     */
    @Nonnull
    JComponent getComponent();

    /**
     * Returns current file URI.
     *
     * @return URI
     */
    @Nonnull
    Optional<URI> getFileUri();

    /**
     * Returns title of the handled file.
     *
     * @return typically file filename name with extension or alternative title
     */
    @Nonnull
    String getTitle();

    /**
     * Returns currently used filetype.
     *
     * @return fileType file type
     */
    @Nonnull
    Optional<FileType> getFileType();

    /**
     * Sets currently used file type.
     *
     * @param fileType file type
     */
    void setFileType(@Nullable FileType fileType);

    /**
     * Clears content of the file.
     */
    void clearFile();

    /**
     * Returns flag if file in this panel was modified since last saving.
     *
     * @return true if file was modified
     */
    boolean isModified();
}
