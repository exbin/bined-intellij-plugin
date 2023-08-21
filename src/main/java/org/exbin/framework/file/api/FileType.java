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

import javax.annotation.Nonnull;

/**
 * Interface for file types.
 *
 * @author ExBin Project (https://exbin.org)
 */
public interface FileType {

    /**
     * Returns file type Id - should be in following format
     * &lt;ModuleName&gt;.&lt;FileTypeName&gt;.
     *
     * @return file type ID
     */
    @Nonnull
    String getFileTypeId();
}
