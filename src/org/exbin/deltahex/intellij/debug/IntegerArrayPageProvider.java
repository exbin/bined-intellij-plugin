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
package org.exbin.deltahex.intellij.debug;

import com.sun.jdi.ArrayReference;
import com.sun.jdi.IntegerValue;
import com.sun.jdi.Value;
import org.exbin.deltahex.intellij.DebugViewDataSource;

import java.util.List;

/**
 * Integer array data source for debugger view.
 *
 * @author ExBin Project (http://exbin.org)
 * @version 0.1.6 2018/03/03
 */
public class IntegerArrayPageProvider implements DebugViewDataSource.PageProvider {

    private final ArrayReference arrayRef;

    public IntegerArrayPageProvider(ArrayReference arrayRef) {
        this.arrayRef = arrayRef;
    }

    @Override
    public byte[] getPage(long pageIndex) {
        int startPos = (int) (pageIndex * DebugViewDataSource.PAGE_SIZE / 4);
        int length = DebugViewDataSource.PAGE_SIZE / 4;
        if (arrayRef.length() - startPos < DebugViewDataSource.PAGE_SIZE / 4) {
            length = arrayRef.length() - startPos;
        }
        final List<Value> values = arrayRef.getValues(startPos, length);
        byte[] result = new byte[length];
        for (int i = 0; i < values.size(); i++) {
            int value = ((IntegerValue) values.get(i)).value();
            result[i * 4] = (byte) (value >> 24);
            result[i * 4 + 1] = (byte) ((value >> 16) & 0xff);
            result[i * 4 + 2] = (byte) ((value >> 8) & 0xff);
            result[i * 4 + 3] = (byte) (value & 0xff);
        }

        return result;
    }

    @Override
    public long getDocumentSize() {
        return arrayRef.length() * 4;
    }
}
