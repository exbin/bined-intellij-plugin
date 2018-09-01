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
package org.exbin.bined;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Objects;
import javax.annotation.Nonnull;
import org.exbin.utils.binary_data.BinaryData;
import org.exbin.utils.binary_data.EditableBinaryData;

/**
 * Hexadecimal editor component utilities.
 *
 * @version 0.2.0 2018/06/24
 * @author ExBin Project (https://exbin.org)
 */
public class CodeAreaUtils {

    public static final char[] UPPER_HEX_CODES = "0123456789ABCDEF".toCharArray();
    public static final char[] LOWER_HEX_CODES = "0123456789abcdef".toCharArray();
    private static final int CODE_BUFFER_LENGTH = 16;

    public static final String MIME_CLIPBOARD_BINARY = "application/octet-stream";
    public static final String DEFAULT_ENCODING = "UTF-8";

    private CodeAreaUtils() {
    }

    /**
     * Converts byte value to sequence of hexadecimal characters.
     *
     * @param value byte value
     * @return sequence of two hexadecimal chars with upper case
     */
    @Nonnull
    public static char[] byteToHexChars(byte value) {
        char[] result = new char[2];
        byteToHexChars(result, value);
        return result;
    }

    /**
     * Converts byte value to sequence of two hexadecimal characters.
     *
     * @param target target char array (output parameter)
     * @param value byte value
     */
    public static void byteToHexChars(@Nonnull char[] target, byte value) {
        target[0] = UPPER_HEX_CODES[(value >> 4) & 0xf];
        target[1] = UPPER_HEX_CODES[value & 0xf];
    }

    /**
     * Converts long value to sequence of hexadecimal character. No range
     * checking.
     *
     * @param value long value
     * @param length length of the target sequence
     * @return hexadecimal characters
     */
    @Nonnull
    public static char[] longToHexChars(long value, int length) {
        char[] result = new char[length];
        longToHexChars(result, value, length);
        return result;
    }

    /**
     * Converts long value to sequence of hexadecimal character. No range
     * checking.
     *
     * @param target target char array (output parameter)
     * @param value long value
     * @param length length of the target sequence
     */
    public static void longToHexChars(@Nonnull char[] target, long value, int length) {
        for (int i = length - 1; i >= 0; i--) {
            target[i] = UPPER_HEX_CODES[(int) (value & 0xf)];
            value = value >> 4;
        }
    }

    /**
     * Converts byte value to sequence of characters of given code type.
     *
     * @param dataByte byte value
     * @param codeType code type
     * @param targetData target array of characters (output parameter)
     * @param targetPosition target position in array of characters
     * @param charCase case type for alphabetical characters
     */
    public static void byteToCharsCode(byte dataByte, @Nonnull CodeType codeType, char[] targetData, int targetPosition, @Nonnull CodeCharactersCase charCase) {
        char[] hexCharacters = charCase == CodeCharactersCase.UPPER ? CodeAreaUtils.UPPER_HEX_CODES : CodeAreaUtils.LOWER_HEX_CODES;
        switch (codeType) {
            case BINARY: {
                int bitMask = 0x80;
                for (int i = 0; i < 8; i++) {
                    int codeValue = (dataByte & bitMask) > 0 ? 1 : 0;
                    targetData[targetPosition + i] = hexCharacters[codeValue];
                    bitMask = bitMask >> 1;
                }
                break;
            }
            case DECIMAL: {
                int value = dataByte & 0xff;
                int codeValue0 = value / 100;
                targetData[targetPosition] = hexCharacters[codeValue0];
                int codeValue1 = (value / 10) % 10;
                targetData[targetPosition + 1] = hexCharacters[codeValue1];
                int codeValue2 = value % 10;
                targetData[targetPosition + 2] = hexCharacters[codeValue2];
                break;
            }
            case OCTAL: {
                int value = dataByte & 0xff;
                int codeValue0 = value / 64;
                targetData[targetPosition] = hexCharacters[codeValue0];
                int codeValue1 = (value / 8) & 7;
                targetData[targetPosition + 1] = hexCharacters[codeValue1];
                int codeValue2 = value % 8;
                targetData[targetPosition + 2] = hexCharacters[codeValue2];
                break;
            }
            case HEXADECIMAL: {
                int codeValue0 = (dataByte >> 4) & 0xf;
                targetData[targetPosition] = hexCharacters[codeValue0];
                int codeValue1 = dataByte & 0xf;
                targetData[targetPosition + 1] = hexCharacters[codeValue1];
                break;
            }
            default:
                throw new IllegalStateException("Unexpected code type: " + codeType.name());
        }
    }

    /**
     * Converts string of characters to byte value.
     *
     * @param code source text string
     * @param codeType code type
     * @return byte value
     * @throws IllegalArgumentException if code is invalid
     */
    public static byte stringCodeToByte(@Nonnull String code, @Nonnull CodeType codeType) {
        if (code.length() > codeType.getMaxDigitsForByte()) {
            throw new IllegalArgumentException("String code is too long");
        }
        byte result = 0;
        switch (codeType) {
            case BINARY: {
                int bitMask = 1;
                for (int i = code.length() - 1; i >= 0; i--) {
                    switch (code.charAt(i)) {
                        case '0':
                            break;
                        case '1':
                            result |= bitMask;
                            break;
                        default:
                            throw new IllegalArgumentException("Invalid character " + code.charAt(i));
                    }
                    bitMask = bitMask << 1;
                }
                break;
            }
            case OCTAL: {
                int bitMask = 1;
                int resultInt = 0;
                for (int i = code.length() - 1; i >= 0; i--) {
                    char codeChar = code.charAt(i);
                    if (codeChar >= '0' && codeChar <= '7') {
                        resultInt += bitMask * (codeChar - '0');
                    } else {
                        throw new IllegalArgumentException("Invalid character " + codeChar);
                    }

                    bitMask = bitMask << 3;
                }

                if (resultInt > 255) {
                    throw new IllegalArgumentException("Number is too big " + resultInt);
                }
                result = (byte) resultInt;
                break;
            }
            case DECIMAL: {
                int bitMask = 1;
                int resultInt = 0;
                for (int i = code.length() - 1; i >= 0; i--) {
                    char codeChar = code.charAt(i);
                    if (codeChar >= '0' && codeChar <= '9') {
                        resultInt += bitMask * (codeChar - '0');
                    } else {
                        throw new IllegalArgumentException("Invalid character " + codeChar);
                    }

                    bitMask = bitMask * 10;
                }

                if (resultInt > 255) {
                    throw new IllegalArgumentException("Number is too big " + resultInt);
                }
                result = (byte) resultInt;
                break;
            }
            case HEXADECIMAL: {
                int bitMask = 1;
                for (int i = code.length() - 1; i >= 0; i--) {
                    char codeChar = code.charAt(i);
                    if (codeChar >= '0' && codeChar <= '9') {
                        result |= bitMask * (codeChar - '0');
                    } else if (codeChar >= 'a' && codeChar <= 'f') {
                        result |= bitMask * (codeChar + 10 - 'a');
                    } else if (codeChar >= 'A' && codeChar <= 'F') {
                        result |= bitMask * (codeChar + 10 - 'A');
                    } else {
                        throw new IllegalArgumentException("Invalid character " + codeChar);
                    }

                    bitMask = bitMask << 4;
                }
                break;
            }
            default:
                throw new IllegalStateException("Unexpected code type: " + codeType.name());
        }

        return result;
    }

    /**
     * Converts long value to code of given base and length limit.
     *
     * Optionally fills rest of the value with zeros.
     *
     * @param target target characters array (output parameter)
     * @param targetOffset offset position in target array
     * @param value value value
     * @param base target numerical base, supported values are 1 to 16
     * @param lengthLimit length limit
     * @param fillZeros flag if rest of the value should be filled with zeros
     * @param characterCase upper case for values greater than 9
     * @return offset of characters position
     */
    public static int longToBaseCode(@Nonnull char[] target, int targetOffset, long value, int base, int lengthLimit, boolean fillZeros, @Nonnull CodeCharactersCase characterCase) {
        char[] codes = characterCase == CodeCharactersCase.UPPER ? UPPER_HEX_CODES : LOWER_HEX_CODES;
        for (int i = lengthLimit - 1; i >= 0; i--) {
            target[targetOffset + i] = codes[(int) (value % base)];
            if (!fillZeros && value == 0) {
                return i;
            }
            value = value / base;
        }

        return 0;
    }

    /**
     * Converts provided character into byte array for given charset.
     *
     * @param value character value
     * @param charset charset
     * @return byte array
     */
    public static byte[] characterToBytes(char value, @Nonnull Charset charset) {
        ByteBuffer buffer = charset.encode(Character.toString(value));
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes, 0, bytes.length);
        return bytes;
    }

    /**
     * Inserts text encoded data of given code type into given binary data.
     *
     * @param insertedString inserted text in format of code type
     * @param data data output (output parameter)
     * @param codeType type of code to use
     */
    public static void insertHexStringIntoData(@Nonnull String insertedString, @Nonnull EditableBinaryData data, @Nonnull CodeType codeType) {
        int maxDigits = codeType.getMaxDigitsForByte();
        byte[] buffer = new byte[CODE_BUFFER_LENGTH];
        int bufferUsage = 0;
        int offset = 0;
        for (int i = 0; i < insertedString.length(); i++) {
            char charAt = insertedString.charAt(i);
            if ((charAt == ' ' || charAt == '\t') && offset == i) {
                offset++;
            } else if (charAt == ' ' || charAt == '\t' || charAt == ',' || charAt == ';' || charAt == ':') {
                byte value = CodeAreaUtils.stringCodeToByte(insertedString.substring(offset, i), codeType);
                if (bufferUsage < CODE_BUFFER_LENGTH) {
                    buffer[bufferUsage] = value;
                    bufferUsage++;
                } else {
                    data.insert(data.getDataSize(), buffer, 0, bufferUsage);
                    bufferUsage = 0;
                }
                offset = i + 1;
            } else if (i == offset + maxDigits) {
                byte value = CodeAreaUtils.stringCodeToByte(insertedString.substring(offset, i), codeType);
                if (bufferUsage < CODE_BUFFER_LENGTH) {
                    buffer[bufferUsage] = value;
                    bufferUsage++;
                } else {
                    data.insert(data.getDataSize(), buffer, 0, bufferUsage);
                    bufferUsage = 0;
                }
                offset = i;
            }
        }

        if (offset < insertedString.length()) {
            byte value = CodeAreaUtils.stringCodeToByte(insertedString.substring(offset), codeType);
            if (bufferUsage < CODE_BUFFER_LENGTH) {
                buffer[bufferUsage] = value;
                bufferUsage++;
            } else {
                data.insert(data.getDataSize(), buffer, 0, bufferUsage);
                bufferUsage = 0;
            }
        }

        if (bufferUsage > 0) {
            data.insert(data.getDataSize(), buffer, 0, bufferUsage);
        }
    }

    /**
     * Return true if provided character is valid for given code type and
     * position.
     *
     * @param keyValue keyboard key value
     * @param codeOffset current code offset
     * @param codeType current code type
     * @return true if key value value is valid
     */
    public static boolean isValidCodeKeyValue(@Nonnull char keyValue, int codeOffset, @Nonnull CodeType codeType) {
        boolean validKey = false;
        switch (codeType) {
            case BINARY: {
                validKey = keyValue >= '0' && keyValue <= '1';
                break;
            }
            case DECIMAL: {
                validKey = codeOffset == 0
                        ? keyValue >= '0' && keyValue <= '2'
                        : keyValue >= '0' && keyValue <= '9';
                break;
            }
            case OCTAL: {
                validKey = codeOffset == 0
                        ? keyValue >= '0' && keyValue <= '3'
                        : keyValue >= '0' && keyValue <= '7';
                break;
            }
            case HEXADECIMAL: {
                validKey = (keyValue >= '0' && keyValue <= '9')
                        || (keyValue >= 'a' && keyValue <= 'f') || (keyValue >= 'A' && keyValue <= 'F');
                break;
            }
            default:
                throw new IllegalStateException("Unexpected code type " + codeType.name());
        }
        return validKey;
    }

    /**
     * Returns modified byte value after single code value is applied.
     *
     * @param byteValue original byte value
     * @param value code value
     * @param codeOffset code offset
     * @param codeType code type
     * @return modified byte value
     */
    public static byte setCodeValue(byte byteValue, int value, int codeOffset, @Nonnull CodeType codeType) {
        switch (codeType) {
            case BINARY: {
                int bitMask = 0x80 >> codeOffset;
                byteValue = (byte) (byteValue & (0xff - bitMask) | (value << (7 - codeOffset)));
                break;
            }
            case DECIMAL: {
                int newValue = byteValue & 0xff;
                switch (codeOffset) {
                    case 0: {
                        newValue = (newValue % 100) + value * 100;
                        if (newValue > 255) {
                            newValue = 200;
                        }
                        break;
                    }
                    case 1: {
                        newValue = (newValue / 100) * 100 + value * 10 + (newValue % 10);
                        if (newValue > 255) {
                            newValue -= 200;
                        }
                        break;
                    }
                    case 2: {
                        newValue = (newValue / 10) * 10 + value;
                        if (newValue > 255) {
                            newValue -= 200;
                        }
                        break;
                    }
                }

                byteValue = (byte) newValue;
                break;
            }
            case OCTAL: {
                int newValue = byteValue & 0xff;
                switch (codeOffset) {
                    case 0: {
                        newValue = (newValue % 64) + value * 64;
                        break;
                    }
                    case 1: {
                        newValue = (newValue / 64) * 64 + value * 8 + (newValue % 8);
                        break;
                    }
                    case 2: {
                        newValue = (newValue / 8) * 8 + value;
                        break;
                    }
                }

                byteValue = (byte) newValue;
                break;
            }
            case HEXADECIMAL: {
                if (codeOffset == 1) {
                    byteValue = (byte) ((byteValue & 0xf0) | value);
                } else {
                    byteValue = (byte) ((byteValue & 0xf) | (value << 4));
                }
                break;
            }
            default:
                throw new IllegalStateException("Unexpected code type " + codeType.name());
        }

        return byteValue;
    }

    public static void requireNonNull(Object... objects) {
        for (Object object : objects) {
            Objects.requireNonNull(object, "Field cannot be null");
        }
    }

    public static boolean canPaste(@Nonnull Clipboard clipboard, @Nonnull DataFlavor binaryDataFlavor) {
        try {
            return clipboard.isDataFlavorAvailable(binaryDataFlavor) || clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor);
        } catch (IllegalStateException ex) {
            return false;
        }
    }

    public static class BinaryDataClipboardData implements ClipboardData {

        private final BinaryData data;
        private final DataFlavor binaryDataFlavor;

        public BinaryDataClipboardData(@Nonnull BinaryData data, @Nonnull DataFlavor binaryDataFlavor) {
            this.data = data;
            this.binaryDataFlavor = binaryDataFlavor;
        }

        @Nonnull
        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[]{binaryDataFlavor, DataFlavor.stringFlavor};
        }

        @Override
        public boolean isDataFlavorSupported(@Nonnull DataFlavor flavor) {
            return flavor.equals(binaryDataFlavor) || flavor.equals(DataFlavor.stringFlavor);
        }

        @Nonnull
        @Override
        public Object getTransferData(@Nonnull DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if (flavor.equals(binaryDataFlavor)) {
                return data;
            } else {
                ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
                data.saveToStream(byteArrayStream);
                return byteArrayStream.toString(DEFAULT_ENCODING);
            }
        }

        @Override
        public void lostOwnership(@Nonnull Clipboard clipboard, @Nonnull Transferable contents) {
            // do nothing
        }

        @Override
        public void dispose() {
            data.dispose();
        }
    }

    public static class CodeDataClipboardData implements ClipboardData {

        private final BinaryData data;
        private final DataFlavor binaryDataFlavor;
        private final CodeType codeType;
        private final CodeCharactersCase charactersCase;

        public CodeDataClipboardData(@Nonnull BinaryData data, @Nonnull DataFlavor binaryDataFlavor, @Nonnull CodeType codeType, @Nonnull CodeCharactersCase charactersCase) {
            this.data = data;
            this.binaryDataFlavor = binaryDataFlavor;
            this.codeType = codeType;
            this.charactersCase = charactersCase;
        }

        @Nonnull
        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[]{binaryDataFlavor, DataFlavor.stringFlavor};
        }

        @Override
        public boolean isDataFlavorSupported(@Nonnull DataFlavor flavor) {
            return flavor.equals(binaryDataFlavor) || flavor.equals(DataFlavor.stringFlavor);
        }

        @Nonnull
        @Override
        public Object getTransferData(@Nonnull DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if (flavor.equals(binaryDataFlavor)) {
                return data;
            } else {
                int charsPerByte = codeType.getMaxDigitsForByte() + 1;
                int textLength = (int) (data.getDataSize() * charsPerByte);
                if (textLength > 0) {
                    textLength--;
                }

                char[] targetData = new char[textLength];
                Arrays.fill(targetData, ' ');
                for (int i = 0; i < data.getDataSize(); i++) {
                    CodeAreaUtils.byteToCharsCode(data.getByte(i), codeType, targetData, i * charsPerByte, charactersCase);
                }
                return new String(targetData);
//                return new ByteArrayInputStream(new String(dataTarget).getBytes(textPlainUnicodeFlavor.getParameter(MIME_CHARSET)));
            }
        }

        @Override
        public void lostOwnership(@Nonnull Clipboard clipboard, @Nonnull Transferable contents) {
            // do nothing
        }

        @Override
        public void dispose() {
            data.dispose();
        }
    }

    public static interface ClipboardData extends Transferable, ClipboardOwner {

        void dispose();
    }
}
