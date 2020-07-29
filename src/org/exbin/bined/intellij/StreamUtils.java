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
package org.exbin.bined.intellij;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Utilities for stream data manipulations.
 *
 * @version 0.2.1 2017/05/15
 * @author ExBin Project (http://exbin.org)
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

        while (source.available() > 0) {
            int bytesRed = source.read(buffer, bufferUsed, BUFFER_SIZE - bufferUsed);
            bufferUsed += bytesRed;
            if (bufferUsed == BUFFER_SIZE) {
                target.write(buffer, 0, BUFFER_SIZE);
                bufferUsed = 0;
            }
        }

        if (bufferUsed > 0) {
            target.write(buffer, 0, bufferUsed);
        }
    }

    /**
     * Copies all data from input stream to output stream using 1k buffer with
     * size limitation.
     *
     * @param source input stream
     * @param target output stream
     * @param size data size limitation
     * @throws IOException if read or write fails
     */
    public static void copyInputStreamToOutputStream(InputStream source, OutputStream target, long size) throws IOException {
        long remain = size;
        int bufferSize = size < BUFFER_SIZE ? (int) size : BUFFER_SIZE;
        byte[] buffer = new byte[bufferSize];
        int bufferUsed = 0;

        while (source.available() > 0) {
            if (remain == 0) {
                throw new IOException("More data than limited to " + size + " available.");
            }

            int bytesRed = source.read(buffer, bufferUsed, bufferSize - bufferUsed);
            bufferUsed += bytesRed;
            if (bufferUsed == bufferSize) {
                target.write(buffer, 0, bufferSize);
                remain -= bufferSize;
                bufferUsed = 0;
            }
        }

        if (bufferUsed > 0) {
            target.write(buffer, 0, bufferUsed);
            remain -= bufferUsed;
        }

        if (remain > 0) {
            throw new IOException("Unexpected data processed - " + size + " expected, " + (size - remain) + " processed.");
        }
    }

    /**
     * Copies data of given size from input stream to output stream using 1k
     * buffer with size limitation.
     *
     * @param source input stream
     * @param target output stream
     * @param size data size limitation
     * @throws IOException if read or write fails
     */
    public static void copyFixedSizeInputStreamToOutputStream(InputStream source, OutputStream target, long size) throws IOException {
        long remain = size;
        int bufferSize = size < BUFFER_SIZE ? (int) size : BUFFER_SIZE;
        byte[] buffer = new byte[bufferSize];
        int bufferUsed = 0;

        while ((source.available() > 0) && (bufferUsed != remain)) {

            int length = (bufferSize > remain ? (int) remain : bufferSize) - bufferUsed;
            int bytesRed = source.read(buffer, bufferUsed, length);
            bufferUsed += bytesRed;
            if (bufferUsed == bufferSize) {
                target.write(buffer, 0, bufferSize);
                remain -= bufferSize;
                bufferUsed = 0;
            }
        }

        if (bufferUsed > 0) {
            target.write(buffer, 0, bufferUsed);
            remain -= bufferUsed;
        }

        if (remain > 0) {
            throw new IOException("Unexpected data processed - " + size + " expected, " + (size - remain) + " processed.");
        }
    }

    /**
     * Skips all remaining data from input stream.
     *
     * @param source input stream
     * @throws IOException if read fails
     */
    public static void skipInputStreamData(InputStream source) throws IOException {
        while (source.available() > 0) {
            if (source.skip(BUFFER_SIZE) == -1) {
                break;
            }
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
            if (skipped == -1) {
                throw new IOException("Unable to skip data");
            } else {
                skipBytes -= skipped;
            }
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

        while (source.available() > 0) {
            int bytesRed = source.read(buffer, bufferUsed, BUFFER_SIZE - bufferUsed);
            bufferUsed += bytesRed;
            if (bufferUsed == BUFFER_SIZE) {
                target.write(buffer, 0, BUFFER_SIZE);
                secondTarget.write(buffer, 0, BUFFER_SIZE);
                bufferUsed = 0;
            }
        }

        if (bufferUsed > 0) {
            target.write(buffer, 0, bufferUsed);
            secondTarget.write(buffer, 0, bufferUsed);
        }
    }

    /**
     * Compares two streams for matching data.
     *
     * @param stream one stream
     * @param compStream other stream
     * @return true if both streams have same data and length
     * @throws IOException if read or write fails
     */
    public static boolean compareStreams(InputStream stream, InputStream compStream) throws IOException {
        byte[] dataBlob = new byte[1];
        byte[] compDataBlob = new byte[1];
        while (stream.available() > 0) {
            int nextByte = stream.read(dataBlob, 0, 1);
            if (nextByte < 0) {
                return false;
            }
            int compNextByte = compStream.read(compDataBlob, 0, 1);
            if (compNextByte < 0) {
                return false;
            }

            if (dataBlob[0] != compDataBlob[0]) {
                return false;
            }
        }

        return compStream.available() == 0;
    }
}
