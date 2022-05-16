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
package org.exbin.bined.intellij.data;

import com.intellij.psi.CommonClassNames;
import com.sun.jdi.ByteValue;
import com.sun.jdi.CharValue;
import com.sun.jdi.DoubleValue;
import com.sun.jdi.FloatValue;
import com.sun.jdi.IntegerValue;
import com.sun.jdi.LongValue;
import com.sun.jdi.ShortValue;
import org.exbin.auxiliary.paged_data.BinaryData;
import org.exbin.auxiliary.paged_data.ByteArrayData;
import org.exbin.framework.bined.gui.ValuesPanel;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.math.BigInteger;
import java.nio.ByteBuffer;

/**
 * Class value convertor.
 *
 * @author ExBin Project (http://exbin.org)
 * @version 0.2.6 2022/05/16
 */
@ParametersAreNonnullByDefault
public class ObjectValueConvertor {

    private final byte[] valuesCache = new byte[8];
    private final ByteBuffer byteBuffer = ByteBuffer.wrap(valuesCache);

    private static final String BYTE_TYPE = byte.class.getName();
    private static final String SHORT_TYPE = short.class.getName();
    private static final String INT_TYPE = int.class.getName();
    private static final String LONG_TYPE = long.class.getName();
    private static final String FLOAT_TYPE = float.class.getName();
    private static final String DOUBLE_TYPE = double.class.getName();
    private static final String CHAR_TYPE = char.class.getName();

    public ObjectValueConvertor() {
    }

    @Nullable
    private BinaryData process(Object instance) {
        if (instance.getClass().isArray()) {
            return processSimpleValue(instance);
        } else {
            return processArrayValue(instance);
        }
    }

    @Nullable
    private BinaryData processSimpleValue(Object instance) {
/*        String typeString = instance.getClass().getTypeName();
        switch (typeString) {
            case CommonClassNames.JAVA_LANG_BYTE:
            case "B":
            case "byte": {
                ByteValue value = (ByteValue) getPrimitiveValue(descriptor);
                byte[] byteArray = new byte[1];
                byteArray[0] = value.value();
                return new ByteArrayData(byteArray);
            }
            case CommonClassNames.JAVA_LANG_SHORT:
            case "S":
            case "short": {
                ShortValue valueRecord = (ShortValue) getPrimitiveValue(descriptor);
                byte[] byteArray = new byte[2];
                short value = valueRecord.value();
                byteArray[0] = (byte) (value >> 8);
                byteArray[1] = (byte) (value & 0xff);
                return new ByteArrayData(byteArray);
            }
            case CommonClassNames.JAVA_LANG_INTEGER:
            case "I":
            case "int": {
                IntegerValue valueRecord = (IntegerValue) getPrimitiveValue(descriptor);
                byte[] byteArray = new byte[4];
                int value = valueRecord.value();
                byteArray[0] = (byte) (value >> 24);
                byteArray[1] = (byte) ((value >> 16) & 0xff);
                byteArray[2] = (byte) ((value >> 8) & 0xff);
                byteArray[3] = (byte) (value & 0xff);
                return new ByteArrayData(byteArray);
            }
            case CommonClassNames.JAVA_LANG_LONG:
            case "J":
            case "long": {
                LongValue valueRecord = (LongValue) getPrimitiveValue(descriptor);
                byte[] byteArray = new byte[8];
                long value = valueRecord.value();
                BigInteger bigInteger = BigInteger.valueOf(value);
                for (int bit = 0; bit < 7; bit++) {
                    BigInteger nextByte = bigInteger.and(ValuesPanel.BIG_INTEGER_BYTE_MASK);
                    byteArray[7 - bit] = nextByte.byteValue();
                    bigInteger = bigInteger.shiftRight(8);
                }
                return new ByteArrayData(byteArray);
            }
            case CommonClassNames.JAVA_LANG_FLOAT:
            case "F":
            case "float": {
                FloatValue valueRecord = (FloatValue) getPrimitiveValue(descriptor);
                byte[] byteArray = new byte[4];
                float value = valueRecord.value();
                byteBuffer.rewind();
                byteBuffer.putFloat(value);
                System.arraycopy(valuesCache, 0, byteArray, 0, 4);
                return new ByteArrayData(byteArray);
            }
            case CommonClassNames.JAVA_LANG_DOUBLE:
            case "D":
            case "double": {
                DoubleValue valueRecord = (DoubleValue) getPrimitiveValue(descriptor);
                byte[] byteArray = new byte[8];
                double value = valueRecord.value();
                byteBuffer.rewind();
                byteBuffer.putDouble(value);
                System.arraycopy(valuesCache, 0, byteArray, 0, 8);
                return new ByteArrayData(byteArray);
            }
            case CommonClassNames.JAVA_LANG_CHARACTER:
            case "C":
            case "char": {
                CharValue valueRecord = (CharValue) getPrimitiveValue(descriptor);
                byte[] byteArray = new byte[2];
                char value = valueRecord.value();
                byteBuffer.rewind();
                byteBuffer.putChar(value);
                System.arraycopy(valuesCache, 0, byteArray, 0, 2);
                return new ByteArrayData(byteArray);
            }
        } */

        return null;
    }

    @Nullable
    private BinaryData processArrayValue(Object instance) {
        String typeName = instance.getClass().getComponentType().getTypeName();
        if (byte.class.getName().equals(typeName)) {
            return new ByteArrayData((byte[]) instance);
        } else if (int.class.getName().equals(typeName)) {

        }

        return null;
    }
}
