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

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.io.FileTooBigException;
import com.intellij.openapi.vfs.VirtualFile;
import org.exbin.auxiliary.paged_data.*;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * File data wrapper for IntelliJ Virtual File with caching.
 *
 * @author ExBin Project (http://exbin.org)
 * @version 0.2.3 2020/07/30
 */
@ParametersAreNonnullByDefault
public class BinEdFileDataWrapper implements EditableBinaryData {

    private static final int BUFFER_SIZE = 4096;
    public static final int PAGE_SIZE = 4096;

    private final VirtualFile file;

    private InputStream cacheInputStream = null;
    private long cachePosition = 0;
    private final DataPage[] cachePages = new DataPage[]{new DataPage(), new DataPage()};
    private int nextCachePage = 0;

    private volatile boolean writeInProgress = false;

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
    public synchronized byte getByte(long position) {
        long pageIndex = position / PAGE_SIZE;
        int pageOffset = (int) (position % PAGE_SIZE);

        if (cachePages[0].pageIndex == pageIndex) {
            return cachePages[0].page[pageOffset];
        }
        if (cachePages[1].pageIndex == pageIndex) {
            return cachePages[1].page[pageOffset];
        }
        int usedPage = loadPage(pageIndex);
        return cachePages[usedPage].page[pageOffset];
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
    public synchronized BinaryData copy(long startFrom, long length) {
        long pageIndex = startFrom / PAGE_SIZE;
        int pageOffset = (int) (startFrom % PAGE_SIZE);

        PagedData data = new PagedData();
        long dataPosition = 0;
        while (length > 0) {
            int pageLength = length > PAGE_SIZE - pageOffset ? PAGE_SIZE - pageOffset : (int) length;
            copyTo(data, dataPosition, pageIndex, pageOffset, pageLength);
            pageIndex++;
            pageOffset = 0;
            dataPosition += pageLength;
            length -= pageLength;
        }

        return data;
    }

    private void copyTo(PagedData data, long dataPosition, long pageIndex, int pageOffset, int pageLength) {
        if (cachePages[0].pageIndex == pageIndex) {
            data.insert(dataPosition, cachePages[0].page, pageOffset, pageLength);
        } else if (cachePages[1].pageIndex == pageIndex) {
            data.insert(dataPosition, cachePages[1].page, pageOffset, pageLength);
        } else {
            int usedPage = loadPage(pageIndex);
            data.insert(dataPosition, cachePages[usedPage].page, pageOffset, pageLength);
        }
    }

    @Override
    public synchronized void copyToArray(long startFrom, byte[] target, int offset, int length) {
        long pageIndex = startFrom / PAGE_SIZE;
        int pageOffset = (int) (startFrom % PAGE_SIZE);

        int dataPosition = offset;
        while (length > 0) {
            int pageLength = Math.min(length, PAGE_SIZE - pageOffset);
            copyTo(target, dataPosition, pageIndex, pageOffset, pageLength);
            pageIndex++;
            pageOffset = 0;
            dataPosition += pageLength;
            length -= pageLength;
        }
    }

    private void copyTo(byte[] data, int dataPosition, long pageIndex, int pageOffset, int pageLength) {
        if (cachePages[0].pageIndex == pageIndex) {
            System.arraycopy(cachePages[0].page, pageOffset, data, dataPosition, pageLength);
        } else if (cachePages[1].pageIndex == pageIndex) {
            System.arraycopy(cachePages[1].page, pageOffset, data, dataPosition, pageLength);
        } else {
            int usedPage = loadPage(pageIndex);
            System.arraycopy(cachePages[usedPage].page, pageOffset, data, dataPosition, pageLength);
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
        resetCache();
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
    public synchronized void setByte(long position, byte value) {
        writeAction(() -> {
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
        });
    }

    @Override
    public void insertUninitialized(long startFrom, long length) {
        insert(startFrom, length);
    }

    @Override
    public synchronized void insert(long startFrom, long length) {
        writeAction(() -> {
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
        });
    }

    @Override
    public void insert(long startFrom, byte[] insertedData) {
        insert(startFrom, insertedData, 0, insertedData.length);
    }

    @Override
    public synchronized void insert(long startFrom, byte[] insertedData, int insertedDataOffset, int insertedDataLength) {
        writeAction(() -> {
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
        });
    }

    @Override
    public synchronized void insert(long startFrom, BinaryData insertedData) {
        writeAction(() -> {
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
        });
    }

    @Override
    public synchronized void insert(long startFrom, BinaryData insertedData, final long insertedDataOffset, final long insertedDataLength) {
        writeAction(() -> {
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
        });
    }

    @Override
    public synchronized long insert(long startFrom, InputStream insertStream, long maximumDataSize) throws IOException {
        writeAction(() -> {
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
        });

        return maximumDataSize;
    }

    @Override
    public void replace(long targetPosition, BinaryData replacingData) {
        replace(targetPosition, replacingData, 0, replacingData.getDataSize());
    }

    @Override
    public synchronized void replace(long targetPosition, BinaryData replacingData, long startFrom, long replacingLength) {
        if (targetPosition + replacingLength > getDataSize()) {
            throw new OutOfBoundsException("Data can be replaced only inside or at the end");
        }

        writeAction(() -> {
            long fileLength = file.getLength();
            InputStream inputStream = file.getInputStream();
            OutputStream outputStream = file.getOutputStream(null);
            StreamUtils.copyFixedSizeInputStreamToOutputStream(inputStream, outputStream, targetPosition);
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
                StreamUtils.copyFixedSizeInputStreamToOutputStream(inputStream, outputStream, fileLength - targetPosition - replacingLength);
            }

            inputStream.close();
            outputStream.close();
        });
    }

    @Override
    public void replace(long targetPosition, byte[] replacingData) {
        replace(targetPosition, replacingData, 0, replacingData.length);
    }

    @Override
    public synchronized void replace(long targetPosition, byte[] replacingData, int replacingDataOffset, int length) {
        if (targetPosition + length > getDataSize()) {
            throw new OutOfBoundsException("Data can be replaced only inside or at the end");
        }

        writeAction(() -> {
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
        });
    }

    @Override
    public void fillData(long startFrom, long length) {
        fillData(startFrom, length, (byte) 0);
    }

    @Override
    public synchronized void fillData(long startFrom, long length, byte fill) {
        writeAction(() -> {
            long fileLength = file.getLength();
            InputStream inputStream = file.getInputStream();
            OutputStream outputStream = file.getOutputStream(null);
            StreamUtils.copyFixedSizeInputStreamToOutputStream(inputStream, outputStream, startFrom);
            for (int i = 0; i < length; i++) {
                outputStream.write(fill);
            }
            if (fileLength > startFrom + length) {
                StreamUtils.skipInputStreamData(inputStream, length);
                StreamUtils.copyFixedSizeInputStreamToOutputStream(inputStream, outputStream, fileLength - startFrom - length);
            }

            inputStream.close();
            outputStream.close();
        });
    }

    @Override
    public synchronized void remove(long startFrom, long length) {
        writeAction(() -> {
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
        });
    }

    @Override
    public synchronized void clear() {
        writeAction(() -> {
            OutputStream outputStream = file.getOutputStream(null);
            outputStream.close();
        });
    }

    public boolean isWriteInProgress() {
        return writeInProgress;
    }

    private void writeAction(WriteRunnable action) {
        Application application = ApplicationManager.getApplication();
        application.runWriteAction(() -> {
            writeInProgress = true;
            try {
                action.run();
            } catch (IOException ex) {
                if (ex instanceof FileTooBigException) {
//                    JBPopupFactory factory = JBPopupFactory.getInstance();
//                    BalloonBuilder builder = factory.createHtmlTextBalloonBuilder(ex.getMessage(), MessageType.INFO, null);
//                    Balloon balloon = builder.createBalloon();
//                    Project project = ProjectManager.getInstance().getDefaultProject();
//                    balloon.show(RelativePoint.getCenterOf(WindowManager.getInstance().getStatusBar(project).getComponent()), Balloon.Position.above);

                    ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
                        @Override
                        public void run() {
                            Notifications.Bus.notify(new Notification("org.exbin.deltahex.intellij", "Write Failed", "File too big: " + ex.getMessage(), NotificationType.ERROR));
                        }
                    });
                } else {
                    throw new IllegalStateException("Broken virtual file", ex);
                }
            }
            resetCache();
            writeInProgress = false;
        });
    }

    public void resetCache() {
        if (cacheInputStream != null) {
            try {
                cacheInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            cacheInputStream = null;
        }

        cachePages[0].pageIndex = -1;
        cachePages[1].pageIndex = -1;
    }

    public synchronized void close() {
        resetCache();
    }

    @Nonnull
    private InputStream getInputStream(long position) throws IOException {
        if (cacheInputStream != null && cachePosition <= position) {
            if (cachePosition < position) {
                StreamUtils.skipInputStreamData(cacheInputStream, position - cachePosition);
                cachePosition = position;
            }
        } else {
            if (cacheInputStream != null) {
                cacheInputStream.close();
            }
            cacheInputStream = file.getInputStream();
            StreamUtils.skipInputStreamData(cacheInputStream, position);
            cachePosition = position;
        }

        return cacheInputStream;
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

    private int loadPage(long pageIndex) {
        int usedPage = nextCachePage;
        long position = pageIndex * PAGE_SIZE;
        long dataSize = getDataSize();
        try {
            InputStream inputStream = getInputStream(position);

            int done = 0;
            int remains = position + PAGE_SIZE > dataSize ? (int) (dataSize - position) : PAGE_SIZE;
            while (remains > 0) {
                int copied = inputStream.read(cachePages[usedPage].page, done, remains);
                if (copied < 0) {
                    throw new IllegalStateException("Broken virtual file");
                }
                cachePosition += copied;
                remains -= copied;
                done += copied;
            }

            cachePages[usedPage].pageIndex = pageIndex;
            nextCachePage = 1 - nextCachePage;
        } catch (IOException e) {
            throw new IllegalStateException("Broken virtual file", e);
        }

        return usedPage;
    }

    private static class DataPage {
        long pageIndex = -1;
        byte[] page = new byte[PAGE_SIZE];
    }

    public interface WriteRunnable {
        void run() throws IOException;
    }
}
