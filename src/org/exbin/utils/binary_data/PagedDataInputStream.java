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

/**
 * Paged data input stream.
 *
 * @version 0.1.0 2016/05/24
 * @author ExBin Project (http://exbin.org)
 */
public class PagedDataInputStream extends InputStream implements SeekableStream, FinishableStream {

    private final PagedData data;
    private long position = 0;

    public PagedDataInputStream(PagedData data) {
        this.data = data;
    }

    @Override
    public int read() throws IOException {
        try {
            return data.getByte(position++);
        } catch (ArrayIndexOutOfBoundsException ex) {
            return -1;
        }
    }

    @Override
    public void close() throws IOException {
        finish();
    }

    @Override
    public int available() throws IOException {
        return (int) (data.getDataSize() - position);
    }

    @Override
    public int read(byte[] output, int off, int len) throws IOException {
        if (output.length == 0 || len == 0) {
            return 0;
        }

        int length = len;
        int offset = off;
        while (length > 0) {
            int pageIndex = (int) (position / data.getPageSize());
            if (pageIndex >= data.getPagesCount()) {
                return offset == off ? -1 : offset - off;
            }

            byte[] page = data.getPage(pageIndex);
            int srcPos = (int) (position % data.getPageSize());
            int copyLength = page.length - srcPos;
            if (copyLength > length) {
                copyLength = length;
            }

            if (copyLength == 0) {
                return len == length ? -1 : len - length;
            }

            System.arraycopy(page, srcPos, output, offset, copyLength);
            length -= copyLength;
            position += copyLength;
            offset += copyLength;
        }

        return len;
    }

    @Override
    public void seek(long position) throws IOException {
        this.position = position;
    }

    @Override
    public long finish() throws IOException {
        position = data.getDataSize();
        return position;
    }

    @Override
    public long getLength() {
        return position;
    }

    @Override
    public long getStreamSize() {
        return data.getDataSize();
    }
}
