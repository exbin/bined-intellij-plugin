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

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeConsumer;
import com.intellij.openapi.fileTypes.FileTypeFactory;
import com.intellij.openapi.fileTypes.FileTypeManager;
import org.jetbrains.annotations.NotNull;

/**
 * File type factory for generic binary file.
 *
 * @author ExBin Project (http://exbin.org)
 * @version 0.2.0 2019/04/06
 */
public class BinEdFileTypeFactory extends FileTypeFactory {

    @NotNull
    public static FileType getFileType() {
        return FileTypeManager.getInstance().getFileTypeByExtension(BinEdFileType.DEFAULT_EXTENSION);
    }

    @Override
    public void createFileTypes(@NotNull FileTypeConsumer consumer) {
        final FileType fileType = new BinEdFileType();
        consumer.consume(fileType, BinEdFileType.DEFAULT_EXTENSION);
    }
}