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
import java.util.List;

/**
 * Integer list as binary data provider.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class IntegerListPageProvider implements PageProvider {

    private final List<Integer> listRef;

    public IntegerListPageProvider(List<Integer> listRef) {
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
            int value = listRef.get(startPos + i);

            result[i * 4] = (byte) (value >> 24);
            result[i * 4 + 1] = (byte) ((value >> 16) & 0xff);
            result[i * 4 + 2] = (byte) ((value >> 8) & 0xff);
            result[i * 4 + 3] = (byte) (value & 0xff);
        }

        return result;
    }

    @Override
    public long getDocumentSize() {
        return listRef.size() * 4L;
    }
}
