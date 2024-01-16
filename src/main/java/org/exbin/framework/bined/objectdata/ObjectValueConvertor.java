/*
 * Copyright (C) ExBin Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.exbin.framework.bined.objectdata;

import org.exbin.auxiliary.binary_data.BinaryData;
import org.exbin.auxiliary.binary_data.ByteArrayData;
import org.exbin.framework.bined.objectdata.array.BooleanArrayPageProvider;
import org.exbin.framework.bined.objectdata.array.BoxedBooleanArrayPageProvider;
import org.exbin.framework.bined.objectdata.array.BoxedByteArrayPageProvider;
import org.exbin.framework.bined.objectdata.array.BoxedCharArrayPageProvider;
import org.exbin.framework.bined.objectdata.array.BoxedDoubleArrayPageProvider;
import org.exbin.framework.bined.objectdata.array.BoxedFloatArrayPageProvider;
import org.exbin.framework.bined.objectdata.array.BoxedIntegerArrayPageProvider;
import org.exbin.framework.bined.objectdata.array.BoxedLongArrayPageProvider;
import org.exbin.framework.bined.objectdata.array.BoxedShortArrayPageProvider;
import org.exbin.framework.bined.objectdata.array.CharArrayPageProvider;
import org.exbin.framework.bined.objectdata.array.DoubleArrayPageProvider;
import org.exbin.framework.bined.objectdata.array.FloatArrayPageProvider;
import org.exbin.framework.bined.objectdata.array.IntegerArrayPageProvider;
import org.exbin.framework.bined.objectdata.array.LongArrayPageProvider;
import org.exbin.framework.bined.objectdata.array.ShortArrayPageProvider;
import org.exbin.framework.bined.objectdata.list.BooleanListPageProvider;
import org.exbin.framework.bined.objectdata.list.ByteListPageProvider;
import org.exbin.framework.bined.objectdata.list.CharListPageProvider;
import org.exbin.framework.bined.objectdata.list.DoubleListPageProvider;
import org.exbin.framework.bined.objectdata.list.FloatListPageProvider;
import org.exbin.framework.bined.objectdata.list.IntegerListPageProvider;
import org.exbin.framework.bined.objectdata.list.LongListPageProvider;
import org.exbin.framework.bined.objectdata.list.ShortListPageProvider;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

/**
 * Class value convertor.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class ObjectValueConvertor {

    public static final BigInteger BIG_INTEGER_BYTE_MASK = BigInteger.valueOf(255);

    private final byte[] valuesCache = new byte[8];
    private final ByteBuffer byteBuffer = ByteBuffer.wrap(valuesCache);

    public ObjectValueConvertor() {
    }

    @Nonnull
    public Optional<BinaryData> process(Object instance) {
        if (instance instanceof String) {
            return Optional.of(new ByteArrayData(((String) instance).getBytes(StandardCharsets.UTF_8)));
        } else if (instance instanceof BinaryData) {
            return Optional.of((BinaryData) instance);
        } else if (instance.getClass().isArray()) {
            return processArrayValue(instance);
        } else if (instance instanceof List) {
            return processListValue(instance);
        }

        return processSimpleValue(instance);
    }

    @Nonnull
    public Optional<BinaryData> processSimpleValue(Object instance) {
        String typeName = instance.getClass().getTypeName();
        byte[] byteArray = null;
        if (boolean.class.getName().equals(typeName)) {
            byteArray = new byte[1];
            byteArray[0] = (byte) instance;
        } else if (Boolean.class.getName().equals(typeName)) {
            byteArray = new byte[1];
            byteArray[0] = ((Boolean) instance) ? (byte) 1 : (byte) 0;
        } else if (byte.class.getName().equals(typeName)) {
            byteArray = new byte[1];
            byteArray[0] = (byte) instance;
        } else if (Byte.class.getName().equals(typeName)) {
            byteArray = new byte[1];
            byteArray[0] = (Byte) instance;
        } else if (int.class.getName().equals(typeName)) {
            byteArray = new byte[4];
            int value = (int) instance;
            byteArray[0] = (byte) (value >> 24);
            byteArray[1] = (byte) ((value >> 16) & 0xff);
            byteArray[2] = (byte) ((value >> 8) & 0xff);
            byteArray[3] = (byte) (value & 0xff);
        } else if (Integer.class.getName().equals(typeName)) {
            byteArray = new byte[4];
            int value = (Integer) instance;
            byteArray[0] = (byte) (value >> 24);
            byteArray[1] = (byte) ((value >> 16) & 0xff);
            byteArray[2] = (byte) ((value >> 8) & 0xff);
            byteArray[3] = (byte) (value & 0xff);
        } else if (long.class.getName().equals(typeName)) {
            byteArray = new byte[8];
            long value = (long) instance;
            BigInteger bigInteger = BigInteger.valueOf(value);
            for (int bit = 0; bit < 7; bit++) {
                BigInteger nextByte = bigInteger.and(BIG_INTEGER_BYTE_MASK);
                byteArray[7 - bit] = nextByte.byteValue();
                bigInteger = bigInteger.shiftRight(8);
            }
        } else if (Long.class.getName().equals(typeName)) {
            byteArray = new byte[8];
            long value = (Long) instance;
            BigInteger bigInteger = BigInteger.valueOf(value);
            for (int bit = 0; bit < 7; bit++) {
                BigInteger nextByte = bigInteger.and(BIG_INTEGER_BYTE_MASK);
                byteArray[7 - bit] = nextByte.byteValue();
                bigInteger = bigInteger.shiftRight(8);
            }
        } else if (short.class.getName().equals(typeName)) {
            byteArray = new byte[2];
            short value = (short) instance;
            byteArray[0] = (byte) (value >> 8);
            byteArray[1] = (byte) (value & 0xff);
        } else if (Short.class.getName().equals(typeName)) {
            byteArray = new byte[2];
            short value = (Short) instance;
            byteArray[0] = (byte) (value >> 8);
            byteArray[1] = (byte) (value & 0xff);
        } else if (char.class.getName().equals(typeName)) {
            byteArray = new byte[2];
            char value = (char) instance;
            byteBuffer.rewind();
            byteBuffer.putChar(value);
            System.arraycopy(valuesCache, 0, byteArray, 0, 2);
        } else if (Character.class.getName().equals(typeName)) {
            byteArray = new byte[2];
            char value = (Character) instance;
            byteBuffer.rewind();
            byteBuffer.putChar(value);
            System.arraycopy(valuesCache, 0, byteArray, 0, 2);
        } else if (double.class.getName().equals(typeName)) {
            byteArray = new byte[8];
            double value = (double) instance;
            byteBuffer.rewind();
            byteBuffer.putDouble(value);
            System.arraycopy(valuesCache, 0, byteArray, 0, 8);
        } else if (Double.class.getName().equals(typeName)) {
            byteArray = new byte[8];
            double value = (Double) instance;
            byteBuffer.rewind();
            byteBuffer.putDouble(value);
            System.arraycopy(valuesCache, 0, byteArray, 0, 8);
        } else if (float.class.getName().equals(typeName)) {
            byteArray = new byte[4];
            float value = (float) instance;
            byteBuffer.rewind();
            byteBuffer.putFloat(value);
            System.arraycopy(valuesCache, 0, byteArray, 0, 4);
        } else if (Float.class.getName().equals(typeName)) {
            byteArray = new byte[4];
            float value = (Float) instance;
            byteBuffer.rewind();
            byteBuffer.putFloat(value);
            System.arraycopy(valuesCache, 0, byteArray, 0, 4);
        }

        return byteArray != null ? Optional.of(new ByteArrayData(byteArray)) : Optional.empty();
    }

    @Nonnull
    public static Optional<BinaryData> processArrayValue(Object instance) {
        String typeName = instance.getClass().getComponentType().getTypeName();
        if (byte.class.getName().equals(typeName)) {
            return Optional.of(new ByteArrayData((byte[]) instance));
        }

        PageProvider pageProvider = null;
        if (boolean.class.getName().equals(typeName)) {
            pageProvider = new BooleanArrayPageProvider((boolean[]) instance);
        } else if (Boolean.class.getName().equals(typeName)) {
            pageProvider = new BoxedBooleanArrayPageProvider((Boolean[]) instance);
        } else if (Byte.class.getName().equals(typeName)) {
            pageProvider = new BoxedByteArrayPageProvider((Byte[]) instance);
        } else if (int.class.getName().equals(typeName)) {
            pageProvider = new IntegerArrayPageProvider((int[]) instance);
        } else if (Integer.class.getName().equals(typeName)) {
            pageProvider = new BoxedIntegerArrayPageProvider((Integer[]) instance);
        } else if (long.class.getName().equals(typeName)) {
            pageProvider = new LongArrayPageProvider((long[]) instance);
        } else if (Long.class.getName().equals(typeName)) {
            pageProvider = new BoxedLongArrayPageProvider((Long[]) instance);
        } else if (short.class.getName().equals(typeName)) {
            pageProvider = new ShortArrayPageProvider((short[]) instance);
        } else if (Short.class.getName().equals(typeName)) {
            pageProvider = new BoxedShortArrayPageProvider((Short[]) instance);
        } else if (char.class.getName().equals(typeName)) {
            pageProvider = new CharArrayPageProvider((char[]) instance);
        } else if (Character.class.getName().equals(typeName)) {
            pageProvider = new BoxedCharArrayPageProvider((Character[]) instance);
        } else if (double.class.getName().equals(typeName)) {
            pageProvider = new DoubleArrayPageProvider((double[]) instance);
        } else if (Double.class.getName().equals(typeName)) {
            pageProvider = new BoxedDoubleArrayPageProvider((Double[]) instance);
        } else if (float.class.getName().equals(typeName)) {
            pageProvider = new FloatArrayPageProvider((float[]) instance);
        } else if (Float.class.getName().equals(typeName)) {
            pageProvider = new BoxedFloatArrayPageProvider((Float[]) instance);
        }

        return pageProvider != null ? Optional.of(new PageProviderBinaryData(pageProvider)) : Optional.empty();
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    public static Optional<BinaryData> processListValue(Object instance) {
        List<?> listInstance = (List<?>) instance;
        if (listInstance.isEmpty()) {
            return Optional.of(new ByteArrayData());
        }
        Object firstValue = null;
        for (Object value : listInstance) {
            if (value != null) {
                firstValue = value;
                break;
            }
        }
        if (firstValue == null) {
            return Optional.empty();
        }

        String typeName = firstValue.getClass().getTypeName();
        PageProvider pageProvider = null;
        if (Boolean.class.getName().equals(typeName)) {
            pageProvider = new BooleanListPageProvider((List<Boolean>) instance);
        } else if (Byte.class.getName().equals(typeName)) {
            pageProvider = new ByteListPageProvider((List<Byte>) instance);
        } else if (Integer.class.getName().equals(typeName)) {
            pageProvider = new IntegerListPageProvider((List<Integer>) instance);
        } else if (Long.class.getName().equals(typeName)) {
            pageProvider = new LongListPageProvider((List<Long>) instance);
        } else if (Short.class.getName().equals(typeName)) {
            pageProvider = new ShortListPageProvider((List<Short>) instance);
        } else if (Character.class.getName().equals(typeName)) {
            pageProvider = new CharListPageProvider((List<Character>) instance);
        } else if (Double.class.getName().equals(typeName)) {
            pageProvider = new DoubleListPageProvider((List<Double>) instance);
        } else if (Float.class.getName().equals(typeName)) {
            pageProvider = new FloatListPageProvider((List<Float>) instance);
        }

        return pageProvider != null ? Optional.of(new PageProviderBinaryData(pageProvider)) : Optional.empty();
    }
}
