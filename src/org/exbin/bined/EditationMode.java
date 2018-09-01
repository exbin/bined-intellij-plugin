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
package org.exbin.bined;

/**
 * Enumeration of editation modes.
 *
 * @version 0.2.0 2017/04/02
 * @author ExBin Project (https://exbin.org)
 */
public enum EditationMode {

    /**
     * Document cannot be changed.
     */
    READ_ONLY,
    /**
     * Data are inserted at cursor position.
     *
     * Document is extended by size of the inserted data, data at cursor
     * position moved forward to provide space and then inserted data are stored
     * in this new space.
     */
    INSERT,
    /**
     * Data are replaced at cursor position.
     *
     * If size of data is greater than size of the document and editation is not
     * in "overwrite only" mode, document is extended so that inserted data will
     * fit in.
     */
    OVERWRITE,
    /**
     * Only overwrite editation mode is allowed and size of document cannot be
     * changed.
     */
    INPLACE;
}
