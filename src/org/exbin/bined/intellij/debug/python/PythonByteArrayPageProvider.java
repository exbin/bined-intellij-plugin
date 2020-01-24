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
package org.exbin.bined.intellij.debug.python;

import org.exbin.bined.intellij.debug.DebugViewData;
import org.exbin.auxiliary.paged_data.OutOfBoundsException;
import org.jetbrains.annotations.NotNull;

/**
 * Python bytes/bytearray data source for debugger view.
 * <p>
 * It seems that binary value is currently available only in text encoded form.
 *
 * @author ExBin Project (http://exbin.org)
 * @version 0.2.2 2020/01/24
 */
public class PythonByteArrayPageProvider implements DebugViewData.PageProvider {

    private static final String BYTEARRAY_PREFIX = "bytearray(";

    private final String value;
    private final long length;

    public PythonByteArrayPageProvider(@NotNull String value) {
        this.value = value;

        length = computeLength(value);
    }

    @Override
    public byte[] getPage(long pageIndex) {
        long documentSize = getDocumentSize();
        if (pageIndex > documentSize / DebugViewData.PAGE_SIZE)
            throw new OutOfBoundsException();

        int length;
        if (documentSize / DebugViewData.PAGE_SIZE == pageIndex) {
            length = (int) (documentSize % DebugViewData.PAGE_SIZE);
        } else {
            length = DebugViewData.PAGE_SIZE;
        }
        byte[] page = new byte[length];

        readByteData((int) (pageIndex * DebugViewData.PAGE_SIZE), page, length);

        return page;
    }

    private int hexCharToInt(char hexChar) {
        return hexChar <= '9' ? hexChar - '0' : 10 + (hexChar - 'a');
    }

    @Override
    public long getDocumentSize() {
        return length;
    }

    private long computeLength(String value) {
        int skipEnd = 1;
        long length = 0;
        int position = 0;
        if (value.startsWith(BYTEARRAY_PREFIX)) {
            position += BYTEARRAY_PREFIX.length();
            skipEnd = 2;
        }

        if (value.charAt(position) != 'b') {
            return 0;
        }
        position++;
        if (value.charAt(position) != '\'') {
            return 0;
        }
        position++;

        while (position < value.length() - skipEnd) {
            if (value.charAt(position) == '\\') {
                if (value.charAt(position + 1) == 'x') {
                    position += 4;
                } else {
                    return 0;
                }
            } else {
                position++;
            }

            length++;
        }

        return length;
    }

    private void readByteData(long bytePosition, byte[] targetArray, int length) {
        int skipEnd = 1;
        int position = 0;
        if (value.startsWith(BYTEARRAY_PREFIX)) {
            position += BYTEARRAY_PREFIX.length();
            skipEnd = 2;
        }

        if (value.charAt(position) != 'b') {
            return;
        }
        position++;
        if (value.charAt(position) != '\'') {
            return;
        }
        position++;

        int offset = 0;
        while (length > 0 && position < value.length() - skipEnd) {
            if (value.charAt(position) == '\\') {
                if (value.charAt(position + 1) == 'x') {
                    if (bytePosition == 0) {
                        byte byteValue = (byte) ((hexCharToInt(value.charAt(position + 2)) << 4) + hexCharToInt(value.charAt(position + 3)));
                        targetArray[offset] = byteValue;
                    }
                    position += 4;
                } else {
                    return;
                }
            } else {
                byte byteValue = (byte) value.charAt(position);
                targetArray[offset] = byteValue;

                position++;
            }

            if (bytePosition > 0) {
                bytePosition--;
            } else {
                length--;
                offset++;
            }
        }
    }
}
