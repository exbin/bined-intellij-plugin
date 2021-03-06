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

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

/**
 * Byte array data source for debugger view.
 *
 * @author ExBin Project (http://exbin.org)
 * @version 0.1.6 2018/03/04
 */
@ParametersAreNonnullByDefault
public class ByteArrayPageProvider implements DebugViewData.PageProvider {

    private final ArrayReference arrayRef;

    public ByteArrayPageProvider(ArrayReference arrayRef) {
        this.arrayRef = arrayRef;
    }

    @Override
    public byte[] getPage(long pageIndex) {
        int startPos = (int) (pageIndex * DebugViewData.PAGE_SIZE);
        int length = DebugViewData.PAGE_SIZE;
        if (arrayRef.length() - startPos < DebugViewData.PAGE_SIZE) {
            length = arrayRef.length() - startPos;
        }
        final List<Value> values = arrayRef.getValues(startPos, length);
        byte[] result = new byte[length];
        for (int i = 0; i < values.size(); i++) {
            Value rawValue = values.get(i);
            byte value = 0;
            if (rawValue instanceof ObjectReference) {
                Field field = ((ObjectReference) rawValue).referenceType().fieldByName("value");
                rawValue = ((ObjectReference) rawValue).getValue(field);
            }

            if (rawValue instanceof ByteValue) {
                value = ((ByteValue) rawValue).value();
            }

            result[i] = value;
        }

        return result;
    }

    @Override
    public long getDocumentSize() {
        return arrayRef.length();
    }
}
