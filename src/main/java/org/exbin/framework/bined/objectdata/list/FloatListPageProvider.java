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
package org.exbin.framework.bined.objectdata.list;

import org.exbin.framework.bined.objectdata.PageProvider;
import org.exbin.framework.bined.objectdata.PageProviderBinaryData;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.nio.ByteBuffer;
import java.util.List;

/**
 * Float list as binary data provider.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class FloatListPageProvider implements PageProvider {

    private final byte[] valuesCache = new byte[4];
    private final ByteBuffer byteBuffer = ByteBuffer.wrap(valuesCache);

    private final List<Float> listRef;

    public FloatListPageProvider(List<Float> listRef) {
        this.listRef = listRef;
    }

    @Nonnull
    @Override
    public byte[] getPage(long pageIndex) {
        int pageSize = PageProviderBinaryData.PAGE_SIZE / 4;
        int startPos = (int) (pageIndex * pageSize);
        int length = Math.min(listRef.size() - startPos, pageSize);
        byte[] result = new byte[length * 4];
        for (int i = 0; i < length; i++) {
            float value = listRef.get(startPos + i);

            byteBuffer.rewind();
            byteBuffer.putFloat(value);
            System.arraycopy(valuesCache, 0, result, i * 4, 4);
        }

        return result;
    }

    @Override
    public long getDocumentSize() {
        return listRef.size() * 4L;
    }
}
