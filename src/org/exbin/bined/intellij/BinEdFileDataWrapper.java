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

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.vfs.VirtualFile;
import org.exbin.auxiliary.paged_data.BinaryData;
import org.exbin.auxiliary.paged_data.ByteArrayData;
import org.exbin.auxiliary.paged_data.EditableBinaryData;
import org.exbin.auxiliary.paged_data.PagedData;
import org.exbin.bined.operation.BinaryDataOperationException;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.ParametersAreNullableByDefault;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * File data wrapper.
 *
 * @author ExBin Project (http://exbin.org)
 * @version 0.2.3 2020/07/29
 */
@ParametersAreNonnullByDefault
public class BinEdFileDataWrapper implements EditableBinaryData {

    private static final int BUFFER_SIZE = 4096;
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
            StreamUtils.copyFixedSizeInputStreamToOutputStream(inputStream, outputStream, length);
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
            StreamUtils.skipInputStreamData(inputStream, startFrom);
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
        long fileLength = file.getLength();
        if (size > fileLength) {
            insert(fileLength, size - fileLength);
        } else {
            remove(fileLength, fileLength - size);
        }
    }

    @Override
    public void setByte(long position, byte value) {
        Application application = ApplicationManager.getApplication();
        application.runWriteAction(() -> {
            try {
                long fileLength = file.getLength();
                InputStream inputStream = file.getInputStream();
                OutputStream outputStream = file.getOutputStream(null);
                StreamUtils.copyFixedSizeInputStreamToOutputStream(inputStream, outputStream, position);
                outputStream.write(value);
                if (fileLength > position + 1) {
                    StreamUtils.skipInputStreamData(inputStream, 1);
                    StreamUtils.copyFixedSizeInputStreamToOutputStream(inputStream, outputStream, fileLength - position - 1);
                }

                inputStream.close();
                outputStream.close();
            } catch (IOException e) {
                throw new IllegalStateException("Broken virtual file", e);
            }
        });
    }

    @Override
    public void insertUninitialized(long startFrom, long length) {
        insert(startFrom, length);
    }

    @Override
    public void insert(long startFrom, long length) {
        Application application = ApplicationManager.getApplication();
        application.runWriteAction(() -> {
            try {
                long fileLength = file.getLength();
                InputStream inputStream = file.getInputStream();
                OutputStream outputStream = file.getOutputStream(null);
                StreamUtils.copyFixedSizeInputStreamToOutputStream(inputStream, outputStream, startFrom);
                long remains = length;
                while (remains > 0) {
                    outputStream.write(0b0);
                    remains--;
                }
                if (fileLength > startFrom) {
                    StreamUtils.copyFixedSizeInputStreamToOutputStream(inputStream, outputStream, fileLength - startFrom);
                }

                inputStream.close();
                outputStream.close();
            } catch (IOException e) {
                throw new IllegalStateException("Broken virtual file", e);
            }
        });
    }

    @Override
    public void insert(long startFrom, byte[] insertedData) {
        insert(startFrom, insertedData, 0, insertedData.length);
    }

    @Override
    public void insert(long startFrom, byte[] insertedData, int insertedDataOffset, int insertedDataLength) {
        Application application = ApplicationManager.getApplication();
        application.runWriteAction(() -> {
            try {
                long fileLength = file.getLength();
                InputStream inputStream = file.getInputStream();
                OutputStream outputStream = file.getOutputStream(null);
                StreamUtils.copyFixedSizeInputStreamToOutputStream(inputStream, outputStream, startFrom);
                outputStream.write(insertedData, insertedDataOffset, insertedDataLength);
                if (fileLength > startFrom) {
                    StreamUtils.copyFixedSizeInputStreamToOutputStream(inputStream, outputStream, fileLength - startFrom);
                }

                inputStream.close();
                outputStream.close();
            } catch (IOException e) {
                throw new IllegalStateException("Broken virtual file", e);
            }
        });
    }

    @Override
    public void insert(long startFrom, BinaryData insertedData) {
        Application application = ApplicationManager.getApplication();
        application.runWriteAction(() -> {
            try {
                long fileLength = file.getLength();
                InputStream inputStream = file.getInputStream();
                OutputStream outputStream = file.getOutputStream(null);
                StreamUtils.copyFixedSizeInputStreamToOutputStream(inputStream, outputStream, startFrom);
                insertedData.saveToStream(outputStream);
                if (fileLength > startFrom) {
                    StreamUtils.copyFixedSizeInputStreamToOutputStream(inputStream, outputStream, fileLength - startFrom);
                }

                inputStream.close();
                outputStream.close();
            } catch (IOException e) {
                throw new IllegalStateException("Broken virtual file", e);
            }
        });
    }

    @Override
    public void insert(long startFrom, BinaryData insertedData, final long insertedDataOffset, final long insertedDataLength) {
        Application application = ApplicationManager.getApplication();
        application.runWriteAction(() -> {
            try {
                long fileLength = file.getLength();
                InputStream inputStream = file.getInputStream();
                OutputStream outputStream = file.getOutputStream(null);
                StreamUtils.copyFixedSizeInputStreamToOutputStream(inputStream, outputStream, startFrom);
                long length = insertedDataLength;
                long offset = insertedDataOffset;
                byte[] cache = new byte[length < BUFFER_SIZE ? (int) length : BUFFER_SIZE];
                while (length > 0) {
                    int toCopy = length > BUFFER_SIZE ? BUFFER_SIZE : (int) length;
                    insertedData.copyToArray(offset, cache, 0, toCopy);
                    outputStream.write(cache, 0, toCopy);
                    length -= toCopy;
                    offset += toCopy;
                }
                if (fileLength > startFrom) {
                    StreamUtils.copyFixedSizeInputStreamToOutputStream(inputStream, outputStream, fileLength - startFrom);
                }

                inputStream.close();
                outputStream.close();
            } catch (IOException e) {
                throw new IllegalStateException("Broken virtual file", e);
            }
        });
    }

    @Override
    public long insert(long startFrom, InputStream insertStream, long maximumDataSize) throws IOException {
        Application application = ApplicationManager.getApplication();
        application.runWriteAction(() -> {
            try {
                long fileLength = file.getLength();
                InputStream inputStream = file.getInputStream();
                OutputStream outputStream = file.getOutputStream(null);
                StreamUtils.copyFixedSizeInputStreamToOutputStream(inputStream, outputStream, startFrom);
                StreamUtils.copyFixedSizeInputStreamToOutputStream(insertStream, outputStream, maximumDataSize);
                if (fileLength > startFrom) {
                    StreamUtils.copyFixedSizeInputStreamToOutputStream(inputStream, outputStream, fileLength - startFrom);
                }

                inputStream.close();
                outputStream.close();
            } catch (IOException e) {
                throw new IllegalStateException("Broken virtual file", e);
            }
        });

        return maximumDataSize;
    }

    @Override
    public void replace(long targetPosition, BinaryData replacingData) {
        replace(targetPosition, replacingData, 0, replacingData.getDataSize());
    }

    @Override
    public void replace(long targetPosition, BinaryData replacingData, long startFrom, long replacingLength) {
        Application application = ApplicationManager.getApplication();
        application.runWriteAction(() -> {
            try {
                long fileLength = file.getLength();
                InputStream inputStream = file.getInputStream();
                OutputStream outputStream = file.getOutputStream(null);
                StreamUtils.copyFixedSizeInputStreamToOutputStream(inputStream, outputStream, startFrom);
                long length = replacingLength;
                long offset = startFrom;
                byte[] cache = new byte[length < BUFFER_SIZE ? (int) length : BUFFER_SIZE];
                while (length > 0) {
                    int toCopy = length > BUFFER_SIZE ? BUFFER_SIZE : (int) length;
                    replacingData.copyToArray(offset, cache, 0, toCopy);
                    outputStream.write(cache, 0, toCopy);
                    length -= toCopy;
                    offset += toCopy;
                }
                if (fileLength > startFrom + replacingLength) {
                    StreamUtils.skipInputStreamData(inputStream, replacingLength);
                    StreamUtils.copyFixedSizeInputStreamToOutputStream(inputStream, outputStream, fileLength - startFrom - replacingLength);
                }

                inputStream.close();
                outputStream.close();
            } catch (IOException e) {
                throw new IllegalStateException("Broken virtual file", e);
            }
        });
    }

    @Override
    public void replace(long targetPosition, byte[] replacingData) {
        replace(targetPosition, replacingData, 0, replacingData.length);
    }

    @Override
    public void replace(long targetPosition, byte[] replacingData, int replacingDataOffset, int length) {
        Application application = ApplicationManager.getApplication();
        application.runWriteAction(() -> {
            try {
                long fileLength = file.getLength();
                InputStream inputStream = file.getInputStream();
                OutputStream outputStream = file.getOutputStream(null);
                StreamUtils.copyFixedSizeInputStreamToOutputStream(inputStream, outputStream, targetPosition);
                outputStream.write(replacingData, replacingDataOffset, length);
                if (fileLength > targetPosition + length) {
                    StreamUtils.skipInputStreamData(inputStream, length);
                    StreamUtils.copyFixedSizeInputStreamToOutputStream(inputStream, outputStream, fileLength - targetPosition - length);
                }

                inputStream.close();
                outputStream.close();
            } catch (IOException e) {
                throw new IllegalStateException("Broken virtual file", e);
            }
        });
    }

    @Override
    public void fillData(long startFrom, long length) {
        fillData(startFrom, length, (byte) 0);
    }

    @Override
    public void fillData(long startFrom, long length, byte fill) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void remove(long startFrom, long length) {
        Application application = ApplicationManager.getApplication();
        application.runWriteAction(() -> {
            try {
                long fileLength = file.getLength();
                InputStream inputStream = file.getInputStream();
                OutputStream outputStream = file.getOutputStream(null);
                StreamUtils.copyFixedSizeInputStreamToOutputStream(inputStream, outputStream, startFrom);
                if (fileLength > startFrom + length) {
                    StreamUtils.skipInputStreamData(inputStream, length);
                    StreamUtils.copyFixedSizeInputStreamToOutputStream(inputStream, outputStream, fileLength - startFrom - length);
                }

                inputStream.close();
                outputStream.close();
            } catch (IOException e) {
                throw new IllegalStateException("Broken virtual file", e);
            }
        });
    }

    @Override
    public void clear() {
        Application application = ApplicationManager.getApplication();
        application.runWriteAction(() -> {
            try {
                OutputStream outputStream = file.getOutputStream(null);
                outputStream.close();
            } catch (IOException e) {
                throw new IllegalStateException("Broken virtual file", e);
            }
        });
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
