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
package org.exbin.deltahex;

/**
 * Hexadecimal editor component utilities.
 *
 * @version 0.1.1 2016/08/31
 * @author ExBin Project (http://exbin.org)
 */
public class CodeAreaUtils {

    public static final char[] UPPER_HEX_CODES = "0123456789ABCDEF".toCharArray();
    public static final char[] LOWER_HEX_CODES = "0123456789abcdef".toCharArray();

    /**
     * Converts byte value to sequence of hexadecimal characters.
     *
     * @param value byte value
     * @return sequence of two hexadecimal chars with upper case
     */
    public static char[] byteToHexChars(byte value) {
        char[] result = new char[2];
        byteToHexChars(result, value);
        return result;
    }

    /**
     * Converts byte value to sequence of two hexadecimal characters.
     *
     * @param target target char array
     * @param value byte value
     */
    public static void byteToHexChars(char[] target, byte value) {
        target[0] = UPPER_HEX_CODES[(value >> 4) & 15];
        target[1] = UPPER_HEX_CODES[value & 15];
    }

    /**
     * Converts long value to sequence of hexadecimal character. No range
     * checking.
     *
     * @param value long value
     * @param length length of the target sequence
     * @return hexadecimal characters
     */
    public static char[] longToHexChars(long value, int length) {
        char[] result = new char[length];
        longToHexChars(result, value, length);
        return result;
    }

    /**
     * Converts long value to sequence of hexadecimal character. No range
     * checking.
     *
     * @param target target char array
     * @param value long value
     * @param length length of the target sequence
     */
    public static void longToHexChars(char[] target, long value, int length) {
        for (int i = length - 1; i >= 0; i--) {
            target[i] = UPPER_HEX_CODES[(int) (value & 15)];
            value = value >> 4;
        }
    }

    /**
     * Converts byte value to sequence of characters of given code type.
     *
     * @param dataByte byte value
     * @param codeType code type
     * @param targetData target array of characters
     * @param targetPosition target position in array of characters
     * @param charCase case type for alphabetical characters
     */
    public static void byteToCharsCode(byte dataByte, CodeType codeType, char[] targetData, int targetPosition, HexCharactersCase charCase) {
        char[] hexCharacters = charCase == HexCharactersCase.UPPER ? CodeAreaUtils.UPPER_HEX_CODES : CodeAreaUtils.LOWER_HEX_CODES;
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
                int codeValue0 = (dataByte >> 4) & 15;
                targetData[targetPosition] = hexCharacters[codeValue0];
                int codeValue1 = dataByte & 15;
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
    public static byte stringCodeToByte(String code, CodeType codeType) {
        if (code.length() > codeType.getMaxDigits()) {
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
     * @param target target characters array
     * @param targetOffset offset position in target array
     * @param value value
     * @param base target numerical base, supported values are 1 to 16
     * @param lengthLimit length limit
     * @param fillZeros flag if rest of the value should be filled with zeros
     * @param upperCase upper case for values greater than 9
     * @return offset of characters position
     */
    public static int longToBaseCode(char[] target, int targetOffset, long value, int base, int lengthLimit, boolean fillZeros, boolean upperCase) {
        char[] codes = upperCase ? UPPER_HEX_CODES : LOWER_HEX_CODES;
        for (int i = lengthLimit - 1; i >= 0; i--) {
            target[targetOffset + i] = codes[(int) (value % base)];
            if (!fillZeros && value == 0) {
                return i;
            }
            value = value / base;
        }

        return 0;
    }
}
