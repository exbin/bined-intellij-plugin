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
import java.nio.ByteBuffer;

/**
 * Byte buffer as binary data provider.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class ByteBufferPageProvider implements PageProvider {

    private final ByteBuffer byteBuffer;
    private int documentSize;

    public ByteBufferPageProvider(ByteBuffer byteBuffer) {
        this.byteBuffer = byteBuffer;
        documentSize = byteBuffer.remaining();
    }

    @Nonnull
    @Override
    public byte[] getPage(long pageIndex) {
        int pageSize = PageProviderBinaryData.PAGE_SIZE;
        int startPos = (int) (pageIndex * pageSize);
        int length = Math.min(documentSize - startPos, pageSize);
        byte[] result = new byte[length];
        byteBuffer.position(startPos);
        byteBuffer.get(result, 0, length);
        return result;
    }

    @Override
    public long getDocumentSize() {
        return documentSize;
    }
}
