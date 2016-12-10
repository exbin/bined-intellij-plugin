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

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Input stream translation class which converts from input charset to target
 * charset.
 *
 * @version 0.1.1 2016/11/02
 * @author ExBin Project (http://exbin.org)
 */
public class CharsetStreamTranslator extends InputStream {

    public static final int BYTE_BUFFER_SIZE = 1024;

    private final CharsetEncoder encoder;
    private final CharsetDecoder decoder;
    private final InputStream source;

    private final ByteBuffer inputBuffer;
    private final ByteBuffer outputBuffer;
    private final CharBuffer charBuffer;
    private boolean endOfInput = false;

    private int maxInputCharSize;
    private int maxOutputCharSize;

    public CharsetStreamTranslator(Charset inputCharset, Charset outputCharset, InputStream source, int bufferSize) {
        this.source = source;
        decoder = inputCharset.newDecoder();
        encoder = outputCharset.newEncoder();
        maxInputCharSize = (int) decoder.maxCharsPerByte();
        if (maxInputCharSize < 0) {
            maxInputCharSize = 1;
        }
        maxOutputCharSize = (int) encoder.maxBytesPerChar();
        if (maxOutputCharSize < 0) {
            maxOutputCharSize = 1;
        }
        inputBuffer = ByteBuffer.allocate(bufferSize);
        // Use limit as mark of used bytes
        inputBuffer.limit(0);
        charBuffer = CharBuffer.allocate(bufferSize);
        outputBuffer = ByteBuffer.allocate(bufferSize * maxOutputCharSize);
        outputBuffer.limit(0);
    }

    public CharsetStreamTranslator(Charset inputCharset, Charset outputCharset, InputStream source) {
        this(inputCharset, outputCharset, source, BYTE_BUFFER_SIZE);
    }

    @Override
    public int read() throws IOException {
        boolean dataReady = outputBuffer.remaining() > 0;
        if (!dataReady) {
            if (endOfInput) {
                return -1;
            } else {
                processNext();
                if (outputBuffer.remaining() == 0) {
                    return -1;
                }
            }
        }

        byte byteData = outputBuffer.get();
        return byteData;
    }

    public void processNext() {
        byte[] buffer = inputBuffer.array();
        if (inputBuffer.remaining() > 0) {
            // Copy remaining data from previous processing
            int bufferOffset = inputBuffer.position();
            int length = inputBuffer.remaining();
            System.arraycopy(buffer, bufferOffset, buffer, 0, length);
            inputBuffer.rewind();
            inputBuffer.limit(length);
            inputBuffer.position(length);
        } else {
            inputBuffer.rewind();
            inputBuffer.limit(0);
        }

        int position = inputBuffer.position();
        int toRead = inputBuffer.capacity() - inputBuffer.position();
        inputBuffer.limit(position + toRead);
        int offset = position;
        while (toRead > 0) {
            try {
                int red = source.read(buffer, offset, toRead);
                if (red < 0) {
                    inputBuffer.limit(position + offset);
                    endOfInput = true;
                    break;
                }

                offset += red;
                toRead -= red;
            } catch (IOException ex) {
                Logger.getLogger(CharsetStreamTranslator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        inputBuffer.rewind();

        decoder.reset();
        charBuffer.limit(charBuffer.capacity());
        CoderResult decodeResult = decoder.decode(inputBuffer, charBuffer, endOfInput);
        // TODO process errors?

        encoder.reset();
        outputBuffer.limit(outputBuffer.capacity());
        outputBuffer.clear();
        int chars = charBuffer.position();
        charBuffer.rewind();
        charBuffer.limit(chars);
        CoderResult encodeResult = encoder.encode(charBuffer, outputBuffer, endOfInput);
        // TODO process errors?

        int length = outputBuffer.position();
        outputBuffer.rewind();
        outputBuffer.limit(length);
    }
}
