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
package org.exbin.utils.binary_data;

import java.io.IOException;

/**
 * Interface for finishable stream.
 *
 * @version 0.1.0 2016/05/24
 * @author ExBin Project (http://exbin.org)
 */
public interface FinishableStream {

    /**
     * Reads remaining data and returns size of all data processed by this
     * stream.
     *
     * @return size of data in bytes
     * @throws IOException if input/output error occurs
     */
    public long finish() throws IOException;

    /**
     * Returns size of data processed so far.
     *
     * @return size of data in bytes
     */
    public long getLength();
}
