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

import com.intellij.openapi.vfs.VirtualFile;
import org.exbin.auxiliary.paged_data.BinaryData;
import org.exbin.auxiliary.paged_data.ByteArrayData;
import org.exbin.auxiliary.paged_data.EditableBinaryData;
import org.exbin.auxiliary.paged_data.PagedData;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * File data wrapper.
 *
 * @author ExBin Project (http://exbin.org)
 * @version 0.2.3 2020/07/29
 */
public class BinEdFileDataWrapper  implements EditableBinaryData {

    private final VirtualFile file;

    public BinEdFileDataWrapper(VirtualFile virtualFile) {
        this.file = virtualFile;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public long getDataSize() {
        return file.getLength();
    }

    @Override
    public byte getByte(long position) {
        try {
            PagedData data = new PagedData();
            InputStream inputStream = file.getInputStream();
            StreamUtils.skipInputStreamData(inputStream, position);
            int read = inputStream.read();
            if (read < 0) {
                throw new IllegalStateException("Broken virtual file");
            }
            inputStream.close();

            return (byte) read;
        } catch (IOException e) {
            throw new IllegalStateException("Broken virtual file", e);
        }
    }

    @Nonnull
    @Override
    public BinaryData copy() {
        try {
            return new ByteArrayData(file.contentsToByteArray());
        } catch (IOException e) {
            throw new IllegalStateException("Broken virtual file", e);
        }
    }

    @Nonnull
    @Override
    public BinaryData copy(long startFrom, long length) {
        try {
            PagedData data = new PagedData();
            InputStream inputStream = file.getInputStream();
            OutputStream outputStream = data.getDataOutputStream();
            StreamUtils.skipInputStreamData(inputStream, startFrom);
            StreamUtils.copyInputStreamToOutputStream(inputStream, outputStream);
            outputStream.close();
            inputStream.close();

            return data;
        } catch (IOException e) {
            throw new IllegalStateException("Broken virtual file", e);
        }
    }

    @Override
    public void copyToArray(long startFrom, byte[] target, int offset, int length) {
        try {
            InputStream inputStream = file.getInputStream();
            int done = 0;
            int remains = length;
            while (remains > 0) {
                int copied = inputStream.read(target, offset + done, remains);
                if (copied < 0) {
                    throw new IllegalStateException("Broken virtual file");
                }
                remains -= copied;
                done += copied;
            }

            inputStream.close();
        } catch (IOException e) {
            throw new IllegalStateException("Broken virtual file", e);
        }
    }

    @Override
    public void saveToStream(OutputStream outputStream) throws IOException {
        try {
            InputStream inputStream = file.getInputStream();
            StreamUtils.copyInputStreamToOutputStream(inputStream, outputStream);
            outputStream.close();
            inputStream.close();
        } catch (IOException e) {
            throw new IllegalStateException("Broken virtual file", e);
        }
    }

    @Nonnull
    @Override
    public InputStream getDataInputStream() {
        try {
            return file.getInputStream();
        } catch (IOException e) {
            throw new IllegalStateException("Broken virtual file", e);
        }
    }

    @Override
    public void dispose() {
    }

    @Override
    public void setDataSize(long size) {
        long length = file.getLength();
        if (size > length) {
            insert(length, size - length);
        } else {
            remove(length, length - size);
        }
    }

    @Override
    public void setByte(long position, byte value) {
        try {
            long length = file.getLength();
            InputStream inputStream = file.getInputStream();
            OutputStream outputStream = file.getOutputStream(null);
            StreamUtils.copyFixedSizeInputStreamToOutputStream(inputStream, outputStream, position);
            outputStream.write(value);
            if (length > position + 1) {
                StreamUtils.skipInputStreamData(inputStream, 1);
                StreamUtils.copyFixedSizeInputStreamToOutputStream(inputStream, outputStream, length - position - 1);
            }

            inputStream.close();
            outputStream.close();
        } catch (IOException e) {
            throw new IllegalStateException("Broken virtual file", e);
        }
    }

    @Override
    public void insertUninitialized(long startFrom, long length) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void insert(long startFrom, long length) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void insert(long startFrom, byte[] insertedData) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void insert(long startFrom, byte[] insertedData, int insertedDataOffset, int insertedDataLength) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void insert(long startFrom, BinaryData insertedData) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void insert(long startFrom, BinaryData insertedData, long insertedDataOffset, long insertedDataLength) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public long insert(long startFrom, InputStream inputStream, long maximumDataSize) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
//        return 0;
    }

    @Override
    public void replace(long targetPosition, BinaryData replacingData) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void replace(long targetPosition, BinaryData replacingData, long startFrom, long length) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void replace(long targetPosition, byte[] replacingData) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void replace(long targetPosition, byte[] replacingData, int replacingDataOffset, int length) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void fillData(long startFrom, long length) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void fillData(long startFrom, long length, byte fill) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void remove(long startFrom, long length) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void loadFromStream(InputStream inputStream) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Nonnull
    @Override
    public OutputStream getDataOutputStream() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
