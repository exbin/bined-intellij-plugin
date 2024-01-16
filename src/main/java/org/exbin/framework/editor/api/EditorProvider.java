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
package org.exbin.framework.editor.api;

import java.net.URI;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.JComponent;
import org.exbin.framework.file.api.FileType;
import org.exbin.framework.file.api.FileOperations;
import org.exbin.framework.file.api.FileHandler;
import org.exbin.framework.file.api.UsedDirectoryApi;

/**
 * XBUP framework editor interface.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public interface EditorProvider extends FileOperations, UsedDirectoryApi {

    /**
     * Returns main editor component.
     *
     * @return component
     */
    @Nonnull
    JComponent getEditorComponent();

    /**
     * Returns active file.
     *
     * @return acftive file
     */
    @Nonnull
    Optional<FileHandler> getActiveFile();

    /**
     * Gets window title related to last opened or saved file.
     *
     * @param parentTitle title of window/frame
     * @return title related to last opened file
     */
    @Nonnull
    String getWindowTitle(String parentTitle);

    /**
     * Opens file from given file parameters.
     *
     * @param fileUri file Uri
     * @param fileType file type
     */
    void openFile(URI fileUri, FileType fileType);

    /**
     * Sets modification listener.
     *
     * @param editorModificationListener editor modification listener
     */
    void setModificationListener(EditorModificationListener editorModificationListener);

    /**
     * Interface for editor modifications listener.
     */
    public static interface EditorModificationListener {

        void modified();
    }
}
