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
package org.exbin.bined.delta.list;

import javax.annotation.Nullable;

/**
 * Interface for item of doubly linked list.
 *
 * @version 0.2.0 2017/05/31
 * @author ExBin Project (https://exbin.org)
 * @param <T> instance class
 */
public interface DoublyLinkedItem<T> {

    /**
     * Returns next linked item.
     *
     * @return next item
     */
    @Nullable
    T getNext();

    /**
     * Sets next linked item.
     *
     * @param next next item
     */
    @Nullable
    void setNext(T next);

    /**
     * Returns previous linked item.
     *
     * @return next item
     */
    @Nullable
    T getPrev();

    /**
     * Sets previous linked item.
     *
     * @param previous previous item
     */
    @Nullable
    void setPrev(T previous);
}
