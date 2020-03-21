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
package org.exbin.bined.intellij.debug.php;

import org.exbin.bined.intellij.debug.DebugViewData;
import org.exbin.auxiliary.paged_data.OutOfBoundsException;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * PHP bytearray data source for debugger view.
 *
 * @author ExBin Project (http://exbin.org)
 * @version 0.2.3 2020/01/30
 */
public class PhpByteArrayPageProvider implements DebugViewData.PageProvider {

    private final Map<String, String> value;
    private int size = 0;

    public PhpByteArrayPageProvider(@NotNull Map<String, String> value) {
        this.value = value;

        int pos = 0;
        do {
            String child = value.get(String.valueOf(pos));
            try {
                Integer.parseInt(child);
            } catch (NumberFormatException ex) {
                break;
            }
            pos++;
            size++;
        } while (true);
    }

    @Override
    public byte[] getPage(long pageIndex) {
        long documentSize = getDocumentSize();
        if (pageIndex > documentSize / DebugViewData.PAGE_SIZE )
            throw new OutOfBoundsException();

        int length;
        if (documentSize / DebugViewData.PAGE_SIZE == pageIndex) {
            length = (int) (documentSize % DebugViewData.PAGE_SIZE);
        } else {
            length = DebugViewData.PAGE_SIZE;
        }
        byte[] page = new byte[length];

        int position = (int) pageIndex * DebugViewData.PAGE_SIZE;
        int offset = 0;
        while (offset < length) {
            int intValue = Integer.parseInt(value.get(String.valueOf(position)));
            byte byteValue = intValue > -128 && intValue < 256 ? (byte) intValue : 0;
            page[offset] = byteValue;
            offset++;
            position++;
        }

        return page;
    }

    @Override
    public long getDocumentSize() {
        return size;
    }
}
