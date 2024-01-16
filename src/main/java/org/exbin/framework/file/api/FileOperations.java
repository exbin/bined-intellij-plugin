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
import java.net.URISyntaxException;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Interface for file operations.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public interface FileOperations {

    /**
     * Calls new file creation.
     */
    void newFile();

    /**
     * Calls file opening operation.
     */
    void openFile();

    /**
     * Calls file saving operation.
     */
    void saveFile();

    /**
     * Calls file saving as operation.
     */
    void saveAsFile();

    /**
     * Checks if file can be saved.
     *
     * @return true if file can be saved
     */
    boolean canSave();

    /**
     * Calls file release operation to ask if file can be closed.
     *
     * @param fileHandler file handler
     * @return true if file approved for close
     */
    boolean releaseFile(FileHandler fileHandler);

    /**
     * Calls file release operation to ask if file can be closed.
     *
     * @return true if file approved for close
     */
    boolean releaseAllFiles();

    /**
     * Attempts to load given filename.
     *
     * @param fileName filename
     * @throws java.net.URISyntaxException issue with file name
     */
    void loadFromFile(String fileName) throws URISyntaxException;

    /**
     * Attempts to load given URI.
     *
     * @param fileUri file Uri
     * @param fileType file type
     */
    void loadFromFile(URI fileUri, @Nullable FileType fileType);
}
