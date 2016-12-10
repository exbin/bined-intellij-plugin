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
package org.exbin.deltahex.delta;

import java.io.IOException;
import java.io.InputStream;
import org.exbin.utils.binary_data.FinishableStream;
import org.exbin.utils.binary_data.SeekableStream;

/**
 * Delta document input stream.
 *
 * @version 0.1.1 2016/11/02
 * @author ExBin Project (http://exbin.org)
 */
public class DeltaDocumentInputStream extends InputStream implements SeekableStream, FinishableStream {

    private final DeltaDocumentWindow data;
    private long position = 0;

    public DeltaDocumentInputStream(DeltaDocument document) {
        this.data = new DeltaDocumentWindow(document);
    }

    @Override
    public int read() throws IOException {
        if (position >= data.getDataSize()) {
            return -1;
        }

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

        long dataSize = data.getDataSize();
        if (position >= dataSize) {
            return -1;
        }

        if (position + len > dataSize) {
            len = (int) (dataSize - position);
        }

        data.copyToArray(position, output, off, len);
        position += len;
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
