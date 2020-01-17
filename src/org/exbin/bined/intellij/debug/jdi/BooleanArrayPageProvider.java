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

import java.util.List;

/**
 * Boolean array data source for debugger view.
 *
 * @author ExBin Project (http://exbin.org)
 * @version 0.2.2 2019/10/02
 */
public class BooleanArrayPageProvider implements DebugViewData.PageProvider {

    private final ArrayReference arrayRef;

    public BooleanArrayPageProvider(ArrayReference arrayRef) {
        this.arrayRef = arrayRef;
    }

    @Override
    public byte[] getPage(long pageIndex) {
        int startPos = (int) (pageIndex * DebugViewData.PAGE_SIZE * 8);
        int length = DebugViewData.PAGE_SIZE * 8;
        long documentSize = getDocumentSize();
        if (documentSize - startPos < DebugViewData.PAGE_SIZE * 8) {
            length = (int) (documentSize - startPos);
        }
        final List<Value> values = arrayRef.getValues(startPos, length);
        byte[] result = new byte[(length + 7) / 8];
        int bitMask = 0x80;
        int bytePos = 0;
        for (int i = 0; i < values.size(); i++) {
            Value rawValue = values.get(i);
            boolean value = false;
            if (rawValue instanceof ObjectReference) {
                Field field = ((ObjectReference) rawValue).referenceType().fieldByName("value");
                rawValue = ((ObjectReference) rawValue).getValue(field);
            }

            if (rawValue instanceof BooleanValue) {
                value = ((BooleanValue) rawValue).value();
            }

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
        return arrayRef.length();
    }
}
