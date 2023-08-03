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
package org.exbin.framework.bined.objectdata.source;

import java.io.IOException;
import java.io.Reader;
import org.exbin.framework.bined.objectdata.PageProvider;
import org.exbin.framework.bined.objectdata.PageProviderBinaryData;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Byte buffer as binary data provider.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class ReaderPageProvider implements PageProvider {

    private final ReaderProvider readerProvider;
    private int documentSize;

    public ReaderPageProvider(ReaderProvider readerProvider) {
        this.readerProvider = readerProvider;
        
        Reader reader = readerProvider.getReader();

        long skipped;
        do {
            try {
                skipped = reader.skip(Long.MAX_VALUE);
            } catch (IOException ex) {
                break;
            }
            if (skipped > 0) {
                documentSize += skipped;
            }
        } while (skipped > 0);
    }

    @Nonnull
    @Override
    public byte[] getPage(long pageIndex) {
        Reader reader = readerProvider.getReader();
        int pageSize = PageProviderBinaryData.PAGE_SIZE / 2;
        int startPos = (int) (pageIndex * pageSize);
        int length = Math.min(documentSize - startPos, pageSize);

        long skipped;
        while (startPos > 0) {
            try {
                skipped = reader.skip(startPos);
            } catch (IOException ex) {
                break;
            }
            if (skipped > 0) {
                startPos -= skipped;
            }
        }

        byte[] result = new byte[length * 2];
        char[] chars = new char[length];

        int offset = 0;
        int remaining = length;
        while (remaining > 0) {
            try {
                int red = reader.read(chars, offset, remaining);
                if (red > 0) {
                    offset += red;
                    remaining -= red;
                } else {
                    break;
                }
            } catch (IOException ex) {
                break;
            }
        }
        for (int i = 0; i < length; i++) {
            result[i * 2 + 1] = (byte) (chars[i] & 0xff);
            result[i * 2] = (byte) ((chars[i] >> 8) & 0xff);
        }
        return result;
    }

    @Override
    public long getDocumentSize() {
        return documentSize * 2;
    }
    
    public interface ReaderProvider {
        Reader getReader();
    }
}
