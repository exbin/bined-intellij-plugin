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
package org.exbin.bined.intellij.data.array;

import org.exbin.bined.intellij.data.PageProvider;
import org.exbin.bined.intellij.data.PageProviderBinaryData;
import org.exbin.framework.bined.gui.ValuesPanel;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.math.BigInteger;

/**
 * Long array as binary data provider.
 *
 * @author ExBin Project (http://exbin.org)
 * @version 0.2.6 2022/05/18
 */
@ParametersAreNonnullByDefault
public class LongArrayPageProvider implements PageProvider {

    private final long[] arrayRef;

    public LongArrayPageProvider(long[] arrayRef) {
        this.arrayRef = arrayRef;
    }

    @Nonnull
    @Override
    public byte[] getPage(long pageIndex) {
        int pageSize = PageProviderBinaryData.PAGE_SIZE / 8;
        int startPos = (int) (pageIndex * pageSize);
        int length = Math.min(arrayRef.length - startPos, pageSize);
        byte[] result = new byte[length * 8];
        for (int i = 0; i < length; i++) {
            long value = arrayRef[startPos + i];

            BigInteger bigInteger = BigInteger.valueOf(value);
            for (int bit = 0; bit < 7; bit++) {
                BigInteger nextByte = bigInteger.and(ValuesPanel.BIG_INTEGER_BYTE_MASK);
                result[i * 8 + 7 - bit] = nextByte.byteValue();
                bigInteger = bigInteger.shiftRight(8);
            }
        }

        return result;
    }

    @Override
    public long getDocumentSize() {
        return arrayRef.length * 8L;
    }
}
