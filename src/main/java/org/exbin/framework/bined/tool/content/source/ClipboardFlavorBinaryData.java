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
package org.exbin.framework.bined.tool.content.source;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import org.exbin.auxiliary.binary_data.BinaryData;
import org.exbin.auxiliary.binary_data.paged.PagedData;
import org.exbin.framework.utils.ClipboardUtils;
import org.exbin.xbup.core.util.StreamUtils;

/**
 * Binary data access for clipboard flavor.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class ClipboardFlavorBinaryData implements BinaryData {

	public static final String BROKEN_DATA_SOURCE = "Broken data source";
    public static final int PAGE_SIZE = 4096;

    private DataFlavor dataFlavor;
    private long dataSize = 0;

    private InputStream cacheInputStream = null;
    private long cachePosition = 0;
    private final DataPage[] cachePages = new DataPage[] { new DataPage(), new DataPage() };
    private int nextCachePage = 0;

    public ClipboardFlavorBinaryData() {
    }
    
    public void setDataFlavor(DataFlavor dataFlavor) throws ClassNotFoundException, UnsupportedFlavorException {
        this.dataFlavor = dataFlavor;
        if (!ClipboardUtils.getClipboard().isDataFlavorAvailable(dataFlavor)) {
            throw new UnsupportedFlavorException(dataFlavor);
        }

        InputStream inputStream = getDataInputStream();
        dataSize = 0;
        do {
            long skipped;
            try {
                skipped = inputStream.skip(Long.MAX_VALUE);
            } catch (IOException ex) {
                Logger.getLogger(ClipboardFlavorBinaryData.class.getName()).log(Level.SEVERE, null, ex);
                break;
            }

            if (skipped <= 0) {
                break;
            }
            dataSize += skipped;
        } while (dataSize > 0);
    }

    public void convertDataFlavor(DataFlavor sourceFlavor) throws ClassNotFoundException, UnsupportedFlavorException {
        setDataFlavor(new DataFlavor(sourceFlavor.getPrimaryType() + "/" + sourceFlavor.getSubType() + ";class=java.io.InputStream"));
    }

    @Override
    public boolean isEmpty() {
        return dataSize > 0;
    }

    @Override
    public long getDataSize() {
        return dataSize;
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
        ClipboardFlavorBinaryData copy = new ClipboardFlavorBinaryData();
        try {
            copy.convertDataFlavor(dataFlavor);
        } catch (ClassNotFoundException | UnsupportedFlavorException ex) {
            Logger.getLogger(ClipboardFlavorBinaryData.class.getName()).log(Level.SEVERE, null, ex);
        }
        return copy;
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
        try (InputStream inputStream = getDataInputStream()) {
            StreamUtils.copyInputStreamToOutputStream(inputStream, outputStream);
        }
    }

    @Nonnull
    @Override
    public InputStream getDataInputStream() {
        try {
            Object data = ClipboardUtils.getClipboard().getData(dataFlavor);
            return (InputStream) data;
        } catch (UnsupportedFlavorException | IOException ex) {
            throw new IllegalStateException(BROKEN_DATA_SOURCE, ex);
        }
    }

    @Override
    public void dispose() {
        resetCache();
    }

    public void resetCache() {
        if (cacheInputStream != null) {
            try {
                cacheInputStream.close();
            } catch (IOException ex) {
                Logger.getLogger(ClipboardFlavorBinaryData.class.getName()).log(Level.SEVERE, null, ex);
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
        if (cacheInputStream != null && position == cachePosition) {
            return cacheInputStream;
        } else if (cacheInputStream != null && position > cachePosition) {
            StreamUtils.skipInputStreamData(cacheInputStream, position - cachePosition);
            cachePosition = position;
        } else {
            if (cacheInputStream != null) {
                cacheInputStream.close();
            }
            cacheInputStream = getDataInputStream();
            StreamUtils.skipInputStreamData(cacheInputStream, position);
            cachePosition = position;
        }

        return cacheInputStream;
    }

    private int loadPage(long pageIndex) {
        int usedPage = nextCachePage;
        long position = pageIndex * PAGE_SIZE;
        try {
            InputStream inputStream = getInputStream(position);

            int done = 0;
            int remains = position + PAGE_SIZE > dataSize ? (int) (dataSize - position) : PAGE_SIZE;
            while (remains > 0) {
                int copied = inputStream.read(cachePages[usedPage].page, done, remains);
                if (copied < 0) {
                    throw new IllegalStateException(BROKEN_DATA_SOURCE);
                }
                cachePosition += copied;
                remains -= copied;
                done += copied;
            }

            cachePages[usedPage].pageIndex = pageIndex;
            nextCachePage = 1 - nextCachePage;
        } catch (IOException e) {
            throw new IllegalStateException(BROKEN_DATA_SOURCE, e);
        }

        return usedPage;
    }

    private static class DataPage {
        long pageIndex = -1;
        byte[] page = new byte[PAGE_SIZE];
    }
}
