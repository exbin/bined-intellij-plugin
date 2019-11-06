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

import org.exbin.bined.intellij.debug.DebugViewDataSource;
import org.exbin.utils.binary_data.OutOfBoundsException;
import org.jetbrains.annotations.NotNull;

/**
 * Python bytearray data source for debugger view.
 * <p>
 * It seems that binary value is currently available only in text encoded form.
 *
 * TODO: Rework to use something different as direct mode fails for higher than 127 character anyway...
 *
 * @author ExBin Project (http://exbin.org)
 * @version 0.2.2 2019/11/06
 */
public class PythonByteArrayPageProvider implements DebugViewDataSource.PageProvider {

    private final Mode mode;
    private final String value;
    private final String prefix;

    public PythonByteArrayPageProvider(@NotNull String value, String prefix) {
        this.value = value;
        this.prefix = prefix;
        mode = value.startsWith(prefix) ? Mode.DESCRIPTION_STRING : Mode.DIRECT_STRING;
    }

    @Override
    public byte[] getPage(long pageIndex) {
        long documentSize = getDocumentSize();
        if (pageIndex > documentSize / DebugViewDataSource.PAGE_SIZE)
            throw new OutOfBoundsException();

        int length;
        if (documentSize / DebugViewDataSource.PAGE_SIZE == pageIndex) {
            length = (int) (documentSize % DebugViewDataSource.PAGE_SIZE);
        } else {
            length = DebugViewDataSource.PAGE_SIZE;
        }
        byte[] page = new byte[length];

        switch (mode) {
            case DIRECT_STRING: {
                int position = (int) (pageIndex * DebugViewDataSource.PAGE_SIZE);
                int offset = 0;
                while (offset < length) {
                    byte byteValue = (byte) value.charAt(position);
                    page[offset] = byteValue;
                    offset++;
                    position++;
                }

                break;
            }
            case DESCRIPTION_STRING: {
                int position = (int) (prefix.length() + 3 + (pageIndex * DebugViewDataSource.PAGE_SIZE * 4));
                int offset = 0;
                while (offset < length) {
                    byte byteValue = (byte) ((hexCharToInt(value.charAt(position + 2)) << 4) + hexCharToInt(value.charAt(position + 3)));
                    page[offset] = byteValue;
                    offset++;
                    position += 4;
                }

                break;
            }
        }

        return page;
    }

    private int hexCharToInt(char hexChar) {
        return hexChar <= '9' ? hexChar - '0' : 10 + (hexChar - 'a');
    }

    @Override
    public long getDocumentSize() {
        switch (mode) {
            case DESCRIPTION_STRING:
                return (value.length() - prefix.length() - 4) / 4;
            case DIRECT_STRING:
                return value.length();
        }

        return 0;
    }

    private enum Mode {
        DESCRIPTION_STRING,
        DIRECT_STRING
    }
}
