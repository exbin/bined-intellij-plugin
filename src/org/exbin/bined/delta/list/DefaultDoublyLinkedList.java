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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Default implementation of doubly linked list of items.
 *
 * @version 0.2.0 2017/05/31
 * @author ExBin Project (https://exbin.org)
 * @param <T> doubly linked list item
 */
public class DefaultDoublyLinkedList<T extends DoublyLinkedItem<T>> implements DoublyLinkedList<T> {

    private T first;

    /* Cached values. */
    private T last;
    private int size = 0;

    @Override
    @Nullable
    public T first() {
        return first;
    }

    @Override
    @Nullable
    public T last() {
        return last;
    }

    @Override
    @Nullable
    public T nextTo(@Nonnull T item) {
        return item.getNext();
    }

    @Override
    @Nullable
    public T prevTo(@Nonnull T item) {
        return item.getPrev();
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    @Nullable
    public T get(int index) {
        T item = first;
        while (index > 0) {
            item = nextTo(item);
            index--;
        }

        return item;
    }

    @Override
    @Nonnull
    public T set(int index, @Nonnull T element) {
        T item = first;
        while (index > 0) {
            item = nextTo(item);
            index--;
        }

        T itemPrev = item.getPrev();
        T itemNext = item.getNext();
        item.setNext(null);
        item.setPrev(null);
        element.setPrev(itemPrev);
        element.setNext(itemNext);
        if (last == item) {
            last = element;
        }

        return item;
    }

    @Override
    public boolean contains(Object o) {
        T item = first;
        while (item != null) {
            if (item.equals(o)) {
                return true;
            }
            item = nextTo(item);
        }

        return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {

            private T current = first;

            @Override
            public boolean hasNext() {
                return current != null;
            }

            @Override
            public T next() {
                T result = current;
                current = result.getNext();
                return result;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
    }

    @Override
    @Nonnull
    public Object[] toArray() {
        if (last == null) {
            return new Object[0];
        }
        int count = indexOf(last);
        Object[] result = new Object[count];
        int index = 0;
        T item = first;
        while (item != null) {
            result[index] = item;
            item = nextTo(item);
            index++;
        }
        return result;
    }

    @Override
    public <T> T[] toArray(T[] template) {
        throw new UnsupportedOperationException("Not supported yet.");
//        int count = last == null ? 0 : indexOf(last);
//        T[] result = template.length >= count ? template : Arrays.copyOf(template, count);
//        int index = 0;
//        T item = first;
//        while (item != null) {
//            result[index] = item;
//            item = nextTo(item);
//            index++;
//        }
//        return result;
    }

    @Override
    public boolean add(@Nonnull T e) {
        if (last != null) {
            last.setNext(e);
            e.setPrev(last);
            e.setNext(null);
            last = e;
        } else {
            first = e;
            last = e;
            e.setNext(null);
            e.setPrev(null);
        }

        size++;
        return true;
    }

    @Override
    public void add(int index, @Nonnull T element) {
        if (index == 0 && size == 0) {
            add(element);
            return;
        }

        if (size == 0) {
            T item = first;
            first = element;
            element.setNext(item);
            element.setPrev(null);
            item.setPrev(element);
        } else if (index == size) {
            last.setNext(element);
            element.setPrev(last);
            element.setNext(null);
            last = element;
        } else if (index == 0) {
            if (first == null) {
                last = element;
            } else {
                first.setPrev(element);
            }
            element.setNext(first);
            first = element;
        } else {
            T item = get(index);
            T prevItem = item.getPrev();
            element.setPrev(prevItem);
            element.setNext(item);
            prevItem.setNext(element);
            item.setPrev(element);
        }
        size++;
    }

    public void addAfter(@Nonnull T positionItem, @Nonnull T element) {
        T next = positionItem.getNext();
        if (next == null) {
            positionItem.setNext(element);
            element.setPrev(positionItem);
            element.setNext(null);
            last = element;
        } else {
            positionItem.setNext(element);
            next.setPrev(element);
            element.setPrev(positionItem);
            element.setNext(next);
        }
        size++;
    }

    public void addBefore(@Nonnull T positionItem, @Nonnull T element) {
        T prev = positionItem.getPrev();
        if (prev == null) {
            positionItem.setPrev(element);
            element.setNext(positionItem);
            element.setPrev(null);
            first = element;
        } else {
            positionItem.setPrev(element);
            prev.setNext(element);
            element.setNext(positionItem);
            element.setPrev(prev);
        }
        size++;
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        boolean result = false;
        for (T t : c) {
            result |= add(t);
        }

        return result;
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        for (T t : c) {
            add(index, t);
            index++;
        }

        return true;
    }

    @Override
    public int indexOf(Object o) {
        T item = first;
        int index = 0;
        while (item != null) {
            if (item.equals(o)) {
                return index;
            }
            item = item.getNext();
            index++;
        }

        return -1;
    }

    @Override
    @Nullable
    public T remove(int index) {
        T item = get(index);
        if (item != null) {
            removeItem(item);
            return item;
        }

        return null;
    }

    @Override
    public boolean remove(Object o) {
        @SuppressWarnings("unchecked")
        T item = (T) o;
        if (item != null) {
            removeItem(item);
            return true;
        }

        return false;
    }

    private void removeItem(@Nonnull T item) {
        if (item == first) {
            first = item.getNext();
            if (first != null) {
                first.setPrev(null);
            } else {
                last = null;
            }
        } else {
            T prev = item.getPrev();
            T next = item.getNext();
            prev.setNext(next);
            if (next != null) {
                next.setPrev(prev);
            } else {
                last = prev;
            }
        }

        size--;
        item.setPrev(null);
        item.setNext(null);
    }

    @Override
    public boolean removeAll(Collection<?> collection) {
        boolean result = false;
        for (Object item : collection) {
            @SuppressWarnings("unchecked")
            T itemList = (T) item;
            result |= remove(itemList);
        }

        return result;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void clear() {
        first = null;
        last = null;
        size = 0;
    }

    @Override
    public int lastIndexOf(Object o) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ListIterator<T> listIterator() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
