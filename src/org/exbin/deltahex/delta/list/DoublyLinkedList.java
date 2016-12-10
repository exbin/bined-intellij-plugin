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
package org.exbin.deltahex.delta.list;

import java.util.List;

/**
 * Doubly linked list of items.
 *
 * @version 0.1.1 2016/10/03
 * @author ExBin Project (http://exbin.org)
 * @param <T> doubly linked list item
 */
public interface DoublyLinkedList<T> extends List<T> {

    /**
     * Returns first item of the list.
     *
     * @return first item
     */
    T first();

    /**
     * Returns last item of the list.
     *
     * @return last item
     */
    T last();

    /**
     * Returns item next to given item.
     *
     * @param item item
     * @return next item or null
     */
    T nextTo(T item);

    /**
     * Returns item previous to given item.
     *
     * @param item item
     * @return previous item or null
     */
    T prevTo(T item);
}
