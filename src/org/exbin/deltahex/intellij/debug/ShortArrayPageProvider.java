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
import com.sun.jdi.ByteValue;
import com.sun.jdi.Value;
import org.exbin.deltahex.intellij.DebugViewDataSource;

import java.util.List;

/**
 * Short array data source for debugger view.
 *
 * @author ExBin Project (http://exbin.org)
 * @version 0.1.6 2018/03/01
 */
public class ShortArrayPageProvider implements DebugViewDataSource.PageProvider {

    private final ArrayReference arrayRef;

    public ShortArrayPageProvider(ArrayReference arrayRef) {
        this.arrayRef = arrayRef;
    }

    @Override
    public byte[] getPage(long pageIndex) {
        int startPos = (int) (pageIndex * DebugViewDataSource.PAGE_SIZE);
        int length = DebugViewDataSource.PAGE_SIZE;
        if (arrayRef.length() - startPos < DebugViewDataSource.PAGE_SIZE) {
            length = arrayRef.length() - startPos;
        }
        final List<Value> values = arrayRef.getValues(startPos, length);
        byte[] result = new byte[length];
        for (int i = 0; i < values.size(); i++) {
            result[i] = ((ByteValue) values.get(i)).value();
        }

        return result;
    }

    @Override
    public long getDocumentSize() {
        return arrayRef.length();
    }

//    int size = arrayRef.length();
//    final List<Value> values = arrayRef.getValues(0, size);
//    byte[] result = new byte[size * 2];
//                                for (int i = 0; i < values.size(); i++) {
//        result[i * 2] = (byte) (((ShortValue) values.get(i)).value() >> 8);
//        result[i * 2 + 1] = (byte) (((ShortValue) values.get(i)).value() & 0xff);
//    }
//    data = new ByteArrayData(result);
}
