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
package org.exbin.xbup.core.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Utilities for stream data manipulations.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public final class StreamUtils {

    private static final int BUFFER_SIZE = 1024;

    private StreamUtils() {
    }

    /**
     * Copies all data from input stream to output stream using 1k buffer.
     *
     * @param source input stream
     * @param target output stream
     * @throws IOException if read or write fails
     */
    public static void copyInputStreamToOutputStream(InputStream source, OutputStream target) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        int bufferUsed = 0;

        int bytesRead;
        do {
            bytesRead = source.read(buffer, bufferUsed, BUFFER_SIZE - bufferUsed);
            if (bytesRead > 0) {
                bufferUsed += bytesRead;
                if (bufferUsed == BUFFER_SIZE) {
                    target.write(buffer, 0, BUFFER_SIZE);
                    bufferUsed = 0;
                }
            }
        } while (bytesRead > 0);

        if (bufferUsed > 0) {
            target.write(buffer, 0, bufferUsed);
        }
    }

    /**
     * Copies all data from input stream to output stream using 1k buffer with
     * maximum size limitation.
     *
     * @param source input stream
     * @param target output stream
     * @param maxAllowedSize data size limitation
     * @throws IOException if read or write fails or source contains more data
     * then limitation
     */
    public static void copyInputStreamToOutputStream(InputStream source, OutputStream target, long maxAllowedSize) throws IOException {
        long remain = maxAllowedSize;
        byte[] buffer = new byte[BUFFER_SIZE];
        int bufferUsed = 0;

        int bytesRead;
        do {
            bytesRead = source.read(buffer, bufferUsed, BUFFER_SIZE - bufferUsed);
            if (bytesRead > 0) {
                if (bytesRead > remain) {
                    throw new IOException("More data than limited to " + maxAllowedSize + " available.");
                }
                remain -= bytesRead;
                bufferUsed += bytesRead;
                if (bufferUsed == BUFFER_SIZE) {
                    target.write(buffer, 0, BUFFER_SIZE);
                    bufferUsed = 0;
                }
            }
        } while (bytesRead > 0);

        if (bufferUsed > 0) {
            target.write(buffer, 0, bufferUsed);
        }
    }

    /**
     * Copies data of given size from input stream to output stream using 1k
     * buffer.
     *
     * @param source input stream
     * @param target output stream
     * @param size data size
     * @throws IOException if read or write fails
     */
    public static void copyFixedSizeInputStreamToOutputStream(InputStream source, OutputStream target, long size) throws IOException {
        long remain = size;
        int bufferSize = size < BUFFER_SIZE ? (int) size : BUFFER_SIZE;
        byte[] buffer = new byte[bufferSize];
        int bufferUsed = 0;

        int bytesRead;
        do {
            bytesRead = source.read(buffer, bufferUsed, bufferSize - bufferUsed);
            if (bytesRead > 0) {
                bufferUsed += bytesRead;
                if (bufferUsed == bufferSize) {
                    remain -= bufferSize;
                    target.write(buffer, 0, bufferSize);
                    bufferUsed = 0;
                    if (remain == 0) {
                        break;
                    }
                    bufferSize = remain < BUFFER_SIZE ? (int) remain : BUFFER_SIZE;
                }
            }
        } while (bytesRead > 0);

        if (bufferUsed > 0) {
            target.write(buffer, 0, bufferUsed);
            remain -= bufferUsed;
        }

        if (remain > 0) {
            throw new IOException("Unexpected data processed - " + size + " expected, " + (size - remain) + " processed.");
        }
    }

    /**
     * Skips given amount of data from input stream.
     *
     * @param source input stream
     * @param skipBytes number of bytes to skip
     * @throws IOException if skip fails
     */
    public static void skipInputStreamData(InputStream source, long skipBytes) throws IOException {
        while (skipBytes > 0) {
            long skipped = source.skip(skipBytes > BUFFER_SIZE ? BUFFER_SIZE : skipBytes);
            if (skipped <= 0) {
                break;
            } else {
                skipBytes -= skipped;
            }
        }

        // Skip was not successful - read data instead
        if (skipBytes > 0) {
            int toRead = skipBytes < BUFFER_SIZE ? (int) skipBytes : BUFFER_SIZE;
            byte[] buffer = new byte[toRead];
            do {
                int bytesRead = source.read(buffer, 0, toRead);
                if (bytesRead <= 0) {
                    throw new IOException("Unable to skip data");
                }
                skipBytes -= bytesRead;
                toRead = skipBytes < BUFFER_SIZE ? (int) skipBytes : BUFFER_SIZE;
            } while (skipBytes > 0);
        }
    }

    /**
     * Copies all data from input stream to two output streams using 1k buffer.
     *
     * @param source input stream
     * @param target output stream
     * @param secondTarget second output stream
     * @throws IOException if read or write fails
     */
    public static void copyInputStreamToTwoOutputStreams(InputStream source, OutputStream target, OutputStream secondTarget) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        int bufferUsed = 0;

        int bytesRead;
        do {
            bytesRead = source.read(buffer, bufferUsed, BUFFER_SIZE - bufferUsed);
            if (bytesRead > 0) {
                bufferUsed += bytesRead;
                if (bufferUsed == BUFFER_SIZE) {
                    target.write(buffer, 0, BUFFER_SIZE);
                    secondTarget.write(buffer, 0, BUFFER_SIZE);
                    bufferUsed = 0;
                }
            }
        } while (bytesRead > 0);

        if (bufferUsed > 0) {
            target.write(buffer, 0, bufferUsed);
            secondTarget.write(buffer, 0, bufferUsed);
        }
    }

    /**
     * Compares two streams for matching data from current position till the
     * end.
     *
     * @param stream one stream
     * @param compStream other stream
     * @return true if both streams have same data and length
     * @throws IOException if read or write fails
     */
    public static boolean compareStreams(InputStream stream, InputStream compStream) throws IOException {
        byte[] dataBlob = new byte[2];

        int nextByte;
        do {
            nextByte = stream.read(dataBlob, 0, 1);
            int compNextByte = compStream.read(dataBlob, 1, 1);
            if (nextByte < 0) {
                return compNextByte < 0;
            }
            if (compNextByte < 0) {
                return false;
            }

            if (dataBlob[0] != dataBlob[1]) {
                return false;
            }
        } while (nextByte > 0);

        return true;
    }
}
