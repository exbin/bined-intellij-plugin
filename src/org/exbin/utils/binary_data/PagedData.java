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
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Encapsulation class for binary data blob.
 *
 * Data are stored using paging. Last page might be shorter than page size, but
 * not empty.
 *
 * @version 0.1.2 2016/12/19
 * @author ExBin Project (http://exbin.org)
 */
public class PagedData implements EditableBinaryData {

    public int DEFAULT_PAGE_SIZE = 4096;

    private int pageSize = DEFAULT_PAGE_SIZE;
    private final List<byte[]> data = new ArrayList<>();

    public PagedData() {
    }

    public PagedData(int pageSize) {
        this.pageSize = pageSize;
    }

    @Override
    public boolean isEmpty() {
        return data.isEmpty();
    }

    @Override
    public long getDataSize() {
        return (data.size() > 1 ? (data.size() - 1) * pageSize : 0) + (data.size() > 0 ? data.get(data.size() - 1).length : 0);
    }

    @Override
    public void setDataSize(long size) {
        long dataSize = getDataSize();
        if (size > dataSize) {
            int lastPage = (int) (dataSize / pageSize);
            int lastPageSize = (int) (dataSize % pageSize);
            long remaining = size - dataSize;
            // extend last page
            if (lastPageSize > 0) {
                byte[] page = getPage(lastPage);
                int nextPageSize = remaining + lastPageSize > pageSize ? pageSize : (int) remaining + lastPageSize;
                byte[] newPage = new byte[nextPageSize];
                System.arraycopy(page, 0, newPage, 0, lastPageSize);
                setPage(lastPage, newPage);
                remaining -= (nextPageSize - lastPageSize);
                lastPage++;
            }

            while (remaining > 0) {
                int nextPageSize = remaining > pageSize ? pageSize : (int) remaining;
                data.add(new byte[nextPageSize]);
                remaining -= nextPageSize;
            }
        } else if (size < dataSize) {
            int lastPage = (int) (size / pageSize);
            int lastPageSize = (int) (size % pageSize);
            // shrink last page
            if (lastPageSize > 0) {
                byte[] page = getPage(lastPage);
                byte[] newPage = new byte[lastPageSize];
                System.arraycopy(page, 0, newPage, 0, lastPageSize);
                setPage(lastPage, newPage);
                lastPage++;
            }

            for (int pageIndex = data.size() - 1; pageIndex >= lastPage; pageIndex--) {
                data.remove(pageIndex);
            }
        }
    }

    @Override
    public byte getByte(long position) {
        byte[] page = getPage((int) (position / pageSize));
        try {
            return page[(int) (position % pageSize)];
        } catch (ArrayIndexOutOfBoundsException ex) {
            throw new OutOfBoundsException(ex);
        }
    }

    @Override
    public void setByte(long position, byte value) {
        byte[] page;
        page = getPage((int) (position / pageSize));
        try {
            page[(int) (position % pageSize)] = value;
        } catch (ArrayIndexOutOfBoundsException ex) {
            throw new OutOfBoundsException(ex);
        }
    }

    @Override
    public void insertUninitialized(long startFrom, long length) {
        if (length < 0) {
            throw new IllegalArgumentException("Length of inserted block must be nonnegative");
        }
        if (startFrom < 0) {
            throw new IllegalArgumentException("Position of inserted block must be nonnegative");
        }
        long dataSize = getDataSize();
        if (startFrom > dataSize) {
            throw new OutOfBoundsException("Inserted block must be inside or directly after existing data");
        }

        if (startFrom >= dataSize) {
            setDataSize(startFrom + length);
        } else if (length > 0) {
            long copyLength = dataSize - startFrom;
            dataSize = dataSize + length;
            setDataSize(dataSize);
            long sourceEnd = dataSize - length;
            long targetEnd = dataSize;
            // Backward copy
            while (copyLength > 0) {
                byte[] sourcePage = getPage((int) (sourceEnd / pageSize));
                int sourceOffset = (int) (sourceEnd % pageSize);
                if (sourceOffset == 0) {
                    sourcePage = getPage((int) ((sourceEnd - 1) / pageSize));
                    sourceOffset = sourcePage.length;
                }

                byte[] targetPage = getPage((int) (targetEnd / pageSize));
                int targetOffset = (int) (targetEnd % pageSize);
                if (targetOffset == 0) {
                    targetPage = getPage((int) ((targetEnd - 1) / pageSize));
                    targetOffset = targetPage.length;
                }

                int copySize = sourceOffset > targetOffset ? targetOffset : sourceOffset;
                if (copySize > copyLength) {
                    copySize = (int) copyLength;
                }

                System.arraycopy(sourcePage, sourceOffset - copySize, targetPage, targetOffset - copySize, copySize);
                copyLength -= copySize;
                sourceEnd -= copySize;
                targetEnd -= copySize;
            }
        }
    }

    @Override
    public void insert(long startFrom, long length) {
        insertUninitialized(startFrom, length);
        fillData(startFrom, length);
    }

    @Override
    public void insert(long startFrom, BinaryData insertedData) {
        long length = insertedData.getDataSize();
        insertUninitialized(startFrom, length);
        replace(startFrom, insertedData, 0, length);
    }

    @Override
    public void insert(long startFrom, BinaryData insertedData, long insertedDataOffset, long insertedDataLength) {
        insertUninitialized(startFrom, insertedDataLength);
        replace(startFrom, insertedData, insertedDataOffset, insertedDataLength);
    }

    @Override
    public void insert(long startFrom, byte[] insertedData) {
        insert(startFrom, insertedData, 0, insertedData.length);
    }

    @Override
    public void insert(long startFrom, byte[] insertedData, int insertedDataOffset, int insertedDataLength) {
        if (insertedDataLength <= 0) {
            return;
        }

        insertUninitialized(startFrom, insertedDataLength);

        while (insertedDataLength > 0) {
            byte[] targetPage = getPage((int) (startFrom / pageSize));
            int targetOffset = (int) (startFrom % pageSize);
            int blockLength = pageSize - targetOffset;
            if (blockLength > insertedDataLength) {
                blockLength = insertedDataLength;
            }

            try {
                System.arraycopy(insertedData, insertedDataOffset, targetPage, targetOffset, blockLength);
            } catch (ArrayIndexOutOfBoundsException ex) {
                throw new OutOfBoundsException(ex);
            }
            insertedDataOffset += blockLength;
            insertedDataLength -= blockLength;
            startFrom += blockLength;
        }
    }

    @Override
    public void fillData(long startFrom, long length) {
        fillData(startFrom, length, (byte) 0);
    }

    @Override
    public void fillData(long startFrom, long length, byte fill) {
        if (length < 0) {
            throw new IllegalArgumentException("Length of filled block must be nonnegative");
        }
        if (startFrom < 0) {
            throw new IllegalArgumentException("Position of filler block must be nonnegative");
        }
        if (startFrom + length > getDataSize()) {
            throw new OutOfBoundsException("Filled block must be inside existing data");
        }

        while (length > 0) {
            byte[] page = getPage((int) (startFrom / pageSize));
            int pageOffset = (int) (startFrom % pageSize);
            int fillSize = page.length - pageOffset;
            if (fillSize > length) {
                fillSize = (int) length;
            }
            Arrays.fill(page, pageOffset, pageOffset + fillSize, fill);
            length -= fillSize;
            startFrom += fillSize;
        }
    }

    @Override
    public PagedData copy() {
        PagedData targetData = new PagedData();
        targetData.insert(0, this);
        return targetData;
    }

    @Override
    public PagedData copy(long startFrom, long length) {
        PagedData targetData = new PagedData();
        targetData.insertUninitialized(0, length);
        targetData.replace(0, this, startFrom, length);
        return targetData;
    }

    @Override
    public void copyToArray(long startFrom, byte[] target, int offset, int length) {
        while (length > 0) {
            byte[] page = getPage((int) (startFrom / pageSize));
            int pageOffset = (int) (startFrom % pageSize);
            int copySize = pageSize - pageOffset;
            if (copySize > length) {
                copySize = length;
            }

            try {
                System.arraycopy(page, pageOffset, target, offset, copySize);
            } catch (ArrayIndexOutOfBoundsException ex) {
                throw new OutOfBoundsException(ex);
            }
            length -= copySize;
            offset += copySize;
            startFrom += copySize;
        }
    }

    @Override
    public void remove(long startFrom, long length) {
        if (length < 0) {
            throw new IllegalArgumentException("Length of removed block must be nonnegative");
        }
        if (startFrom < 0) {
            throw new IllegalArgumentException("Position of removed block must be nonnegative");
        }
        if (startFrom + length > getDataSize()) {
            throw new OutOfBoundsException("Removed block must be inside existing data");
        }

        if (length > 0) {
            replace(startFrom, this, startFrom + length, getDataSize() - startFrom - length);
            setDataSize(getDataSize() - length);
        }
    }

    @Override
    public void clear() {
        data.clear();
    }

    public int getPagesCount() {
        return data.size();
    }

    public int getPageSize() {
        return pageSize;
    }

    /**
     * Gets data page allowing direct access to it.
     *
     * @param pageIndex page index
     * @return data page
     */
    public byte[] getPage(int pageIndex) {
        try {
            return data.get(pageIndex);
        } catch (IndexOutOfBoundsException ex) {
            throw new OutOfBoundsException(ex);
        }
    }

    /**
     * Sets data page replacing existing page by reference.
     *
     * @param pageIndex page index
     * @param dataPage data page
     */
    public void setPage(int pageIndex, byte[] dataPage) {
        try {
            data.set(pageIndex, dataPage);
        } catch (IndexOutOfBoundsException ex) {
            throw new OutOfBoundsException(ex);
        }
    }

    @Override
    public void replace(long targetPosition, BinaryData replacingData) {
        replace(targetPosition, replacingData, 0, replacingData.getDataSize());
    }

    @Override
    public void replace(long targetPosition, BinaryData replacingData, long startFrom, long length) {
        if (targetPosition + length > getDataSize()) {
            throw new OutOfBoundsException("Data can be replaced only inside or at the end");
        }

        if (replacingData instanceof PagedData) {
            if (replacingData != this || (startFrom > targetPosition) || (startFrom + length < targetPosition)) {
                while (length > 0) {
                    byte[] page = getPage((int) (targetPosition / pageSize));
                    int offset = (int) (targetPosition % pageSize);

                    byte[] sourcePage = ((PagedData) replacingData).getPage((int) (startFrom / ((PagedData) replacingData).getPageSize()));
                    int sourceOffset = (int) (startFrom % ((PagedData) replacingData).getPageSize());

                    int copySize = pageSize - offset;
                    if (copySize > ((PagedData) replacingData).getPageSize() - sourceOffset) {
                        copySize = ((PagedData) replacingData).getPageSize() - sourceOffset;
                    }
                    if (copySize > length) {
                        copySize = (int) length;
                    }

                    try {
                        System.arraycopy(sourcePage, sourceOffset, page, offset, copySize);
                    } catch (ArrayIndexOutOfBoundsException ex) {
                        throw new OutOfBoundsException(ex);
                    }
                    length -= copySize;
                    targetPosition += copySize;
                    startFrom += copySize;
                }
            } else {
                targetPosition += length - 1;
                startFrom += length - 1;
                while (length > 0) {
                    byte[] page = getPage((int) (targetPosition / pageSize));
                    int upTo = (int) (targetPosition % pageSize) + 1;

                    byte[] sourcePage = ((PagedData) replacingData).getPage((int) (startFrom / ((PagedData) replacingData).getPageSize()));
                    int sourceUpTo = (int) (startFrom % ((PagedData) replacingData).getPageSize()) + 1;

                    int copySize = upTo;
                    if (copySize > sourceUpTo) {
                        copySize = sourceUpTo;
                    }
                    if (copySize > length) {
                        copySize = (int) length;
                    }
                    int offset = upTo - copySize;
                    int sourceOffset = sourceUpTo - copySize;

                    System.arraycopy(sourcePage, sourceOffset, page, offset, copySize);
                    length -= copySize;
                    targetPosition -= copySize;
                    startFrom -= copySize;
                }
            }
        } else {
            while (length > 0) {
                byte[] page = getPage((int) (targetPosition / pageSize));
                int offset = (int) (targetPosition % pageSize);

                int copySize = pageSize - offset;
                if (copySize > length) {
                    copySize = (int) length;
                }

                replacingData.copyToArray(startFrom, page, offset, copySize);

                length -= copySize;
                targetPosition += copySize;
                startFrom += copySize;
            }
        }
    }

    @Override
    public void replace(long targetPosition, byte[] replacingData) {
        replace(targetPosition, replacingData, 0, replacingData.length);
    }

    @Override
    public void replace(long targetPosition, byte[] replacingData, int replacingDataOffset, int length) {
        if (targetPosition + length > getDataSize()) {
            throw new OutOfBoundsException("Data can be replaced only inside or at the end");
        }

        while (length > 0) {
            byte[] page = getPage((int) (targetPosition / pageSize));
            int offset = (int) (targetPosition % pageSize);

            int copySize = pageSize - offset;
            if (copySize > length) {
                copySize = length;
            }

            try {
                System.arraycopy(replacingData, replacingDataOffset, page, offset, copySize);
            } catch (ArrayIndexOutOfBoundsException ex) {
                throw new OutOfBoundsException(ex);
            }

            length -= copySize;
            targetPosition += copySize;
            replacingDataOffset += copySize;
        }
    }

    @Override
    public void loadFromStream(InputStream inputStream) throws IOException {
        data.clear();
        byte[] buffer = new byte[pageSize];
        int cnt;
        int offset = 0;
        while ((cnt = inputStream.read(buffer, offset, buffer.length - offset)) > 0) {
            if (cnt + offset < pageSize) {
                offset = offset + cnt;
            } else {
                data.add(buffer);
                buffer = new byte[pageSize];
                offset = 0;
            }
        }

        if (offset > 0) {
            byte[] tail = new byte[offset];
            System.arraycopy(buffer, 0, tail, 0, offset);
            data.add(tail);
        }
    }

    @Override
    public long insert(long startFrom, InputStream inputStream, long dataSize) throws IOException {
        if (startFrom > getDataSize()) {
            setDataSize(startFrom);
        }

        long loadedData = 0;
        int pageOffset = (int) (startFrom % pageSize);
        byte[] buffer = new byte[pageSize];
        while (dataSize == -1 || dataSize > 0) {
            int dataToRead = pageSize - pageOffset;
            if (dataSize >= 0 && dataSize < dataToRead) {
                dataToRead = (int) dataSize;
            }
            if (pageOffset > 0 && dataToRead > pageOffset) {
                // Align to data pages
                dataToRead = pageOffset;
            }

            int redLength = 0;
            while (dataToRead > 0) {
                int red = inputStream.read(buffer, redLength, dataToRead);
                if (red == -1) {
                    break;
                } else {
                    redLength += red;
                    dataToRead -= red;
                }
            }

            insert(startFrom, buffer, 0, redLength);
            startFrom += redLength;
            dataSize -= redLength;
            loadedData += redLength;
            pageOffset = 0;
        }
        return loadedData;
    }

    @Override
    public void saveToStream(OutputStream outputStream) throws IOException {
        for (byte[] dataPage : data) {
            outputStream.write(dataPage);
        }
    }

    @Override
    public OutputStream getDataOutputStream() {
        return new PagedDataOutputStream(this);
    }

    @Override
    public InputStream getDataInputStream() {
        return new PagedDataInputStream(this);
    }

    @Override
    public void dispose() {
    }
}
