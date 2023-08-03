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
package org.exbin.framework.bined.objectdata.array;

import org.exbin.framework.bined.objectdata.PageProvider;
import org.exbin.framework.bined.objectdata.PageProviderBinaryData;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Boolean array as binary data provider.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class BoxedBooleanArrayPageProvider implements PageProvider {

    private final Boolean[] arrayRef;

    public BoxedBooleanArrayPageProvider(Boolean[] arrayRef) {
        this.arrayRef = arrayRef;
    }

    @Nonnull
    @Override
    public byte[] getPage(long pageIndex) {
        int startPos = (int) (pageIndex * PageProviderBinaryData.PAGE_SIZE * 8);
        int length = PageProviderBinaryData.PAGE_SIZE * 8;
        long documentSize = getDocumentSize() * 8;
        if (documentSize - startPos < PageProviderBinaryData.PAGE_SIZE * 8) {
            length = (int) (documentSize - startPos);
        }
        byte[] result = new byte[(length + 7) / 8];
        int bitMask = 0x80;
        int bytePos = 0;
        for (int i = 0; i < length; i++) {
            boolean value = arrayRef[startPos + i];

            if (value) {
                result[bytePos] += bitMask;
            }
            if (bitMask == 1) {
                bitMask = 0x80;
                bytePos++;
            } else {
                bitMask = bitMask >> 1;
            }
        }

        return result;
    }

    @Override
    public long getDocumentSize() {
        return (arrayRef.length + 7) / 8;
    }
}
