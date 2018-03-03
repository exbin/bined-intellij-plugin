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
import com.sun.jdi.LongValue;
import com.sun.jdi.Value;
import org.exbin.deltahex.intellij.DebugViewDataSource;
import org.exbin.deltahex.intellij.panel.ValuesPanel;

import java.math.BigInteger;
import java.util.List;

/**
 * Integer array data source for debugger view.
 *
 * @author ExBin Project (http://exbin.org)
 * @version 0.1.6 2018/03/03
 */
public class LongArrayPageProvider implements DebugViewDataSource.PageProvider {

    private final ArrayReference arrayRef;

    public LongArrayPageProvider(ArrayReference arrayRef) {
        this.arrayRef = arrayRef;
    }

    @Override
    public byte[] getPage(long pageIndex) {
        int startPos = (int) (pageIndex * DebugViewDataSource.PAGE_SIZE / 8);
        int length = DebugViewDataSource.PAGE_SIZE / 8;
        if (arrayRef.length() - startPos < DebugViewDataSource.PAGE_SIZE / 8) {
            length = arrayRef.length() - startPos;
        }
        final List<Value> values = arrayRef.getValues(startPos, length);
        byte[] result = new byte[length];
        for (int i = 0; i < values.size(); i++) {
            long value = ((LongValue) values.get(i)).value();
            BigInteger bigInteger = BigInteger.valueOf(value);
            for (int bit = 0; bit < 7; bit++) {
                BigInteger nextByte = bigInteger.and(ValuesPanel.BIG_INTEGER_BYTE_MASK);
                result[i * 8 + bit] = nextByte.byteValue();
                bigInteger = bigInteger.shiftRight(8);
            }
        }

        return result;
    }

    @Override
    public long getDocumentSize() {
        return arrayRef.length() * 8;
    }
}
