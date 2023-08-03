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
package org.exbin.bined.intellij.debug.jdi;

import com.sun.jdi.*;
import org.exbin.framework.bined.objectdata.PageProvider;
import org.exbin.framework.bined.objectdata.PageProviderBinaryData;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.nio.ByteBuffer;
import java.util.List;

/**
 * Double array data source for debugger view.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class JdiDoubleArrayPageProvider implements PageProvider {

    private final byte[] valuesCache = new byte[8];
    private final ByteBuffer byteBuffer = ByteBuffer.wrap(valuesCache);

    private final ArrayReference arrayRef;

    public JdiDoubleArrayPageProvider(ArrayReference arrayRef) {
        this.arrayRef = arrayRef;
    }

    @Nonnull
    @Override
    public byte[] getPage(long pageIndex) {
        int pageSize = PageProviderBinaryData.PAGE_SIZE / 8;
        int startPos = (int) (pageIndex * pageSize);
        int length = Math.min(arrayRef.length() - startPos, pageSize);
        final List<Value> values = arrayRef.getValues(startPos, length);
        byte[] result = new byte[length * 8];
        for (int i = 0; i < values.size(); i++) {
            Value rawValue = values.get(i);
            if (rawValue instanceof ObjectReference) {
                Field field = ((ObjectReference) rawValue).referenceType().fieldByName("value");
                rawValue = ((ObjectReference) rawValue).getValue(field);
            }

            double value = rawValue instanceof DoubleValue ? ((DoubleValue) rawValue).value() : 0;

            byteBuffer.rewind();
            byteBuffer.putDouble(value);
            System.arraycopy(valuesCache, 0, result, i * 8, 8);
        }

        return result;
    }

    @Override
    public long getDocumentSize() {
        return arrayRef.length() * 8;
    }
}
