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
package org.exbin.bined.intellij.debug.jdi;

import com.sun.jdi.*;
import org.exbin.bined.intellij.debug.DebugViewData;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * Float array data source for debugger view.
 *
 * @author ExBin Project (http://exbin.org)
 * @version 0.1.6 2018/03/03
 */
public class FloatArrayPageProvider implements DebugViewData.PageProvider {

    private final byte[] valuesCache = new byte[4];
    private final ByteBuffer byteBuffer = ByteBuffer.wrap(valuesCache);

    private final ArrayReference arrayRef;

    public FloatArrayPageProvider(ArrayReference arrayRef) {
        this.arrayRef = arrayRef;
    }

    @Override
    public byte[] getPage(long pageIndex) {
        int pageSize = DebugViewData.PAGE_SIZE / 4;
        int startPos = (int) (pageIndex * pageSize);
        int length = pageSize;
        if (arrayRef.length() - startPos < pageSize) {
            length = arrayRef.length() - startPos;
        }
        final List<Value> values = arrayRef.getValues(startPos, length);
        byte[] result = new byte[length * 4];
        for (int i = 0; i < values.size(); i++) {
            Value rawValue = values.get(i);
            if (rawValue instanceof ObjectReference) {
                Field field = ((ObjectReference) rawValue).referenceType().fieldByName("value");
                rawValue = ((ObjectReference) rawValue).getValue(field);
            }

            float value = rawValue instanceof FloatValue ? ((FloatValue) rawValue).value() : 0;

            byteBuffer.rewind();
            byteBuffer.putFloat(value);
            System.arraycopy(valuesCache, 0, result, i * 4, 4);
        }

        return result;
    }

    @Override
    public long getDocumentSize() {
        return arrayRef.length() * 4;
    }
}
