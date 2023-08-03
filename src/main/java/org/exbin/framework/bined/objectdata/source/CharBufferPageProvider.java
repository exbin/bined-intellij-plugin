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

import org.exbin.framework.bined.objectdata.PageProvider;
import org.exbin.framework.bined.objectdata.PageProviderBinaryData;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.nio.CharBuffer;

/**
 * Char buffer as binary data provider.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class CharBufferPageProvider implements PageProvider {

    private final CharBuffer charBuffer;
    private int documentSize;

    public CharBufferPageProvider(CharBuffer charBuffer) {
        this.charBuffer = charBuffer;
        documentSize = charBuffer.remaining() * 2;
    }

    @Nonnull
    @Override
    public byte[] getPage(long pageIndex) {
        int pageSize = PageProviderBinaryData.PAGE_SIZE / 2;
        int startPos = (int) (pageIndex * pageSize);
        int length = Math.min((documentSize / 2) - startPos, pageSize);
        char[] chars = new char[length];
        charBuffer.position(startPos);
        charBuffer.get(chars, 0, length);

        byte[] result = new byte[length * 2];
        for (int i = 0; i < length; i++) {
            result[i * 2 + 1] = (byte) (chars[i] & 0xff);
            result[i * 2] = (byte) ((chars[i] >> 8) & 0xff);
        }
        return result;
    }

    @Override
    public long getDocumentSize() {
        return documentSize;
    }
}
