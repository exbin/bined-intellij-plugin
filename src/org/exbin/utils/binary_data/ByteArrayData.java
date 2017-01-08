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
package org.exbin.utils.binary_data;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * Basic implementation of binary data interface using byte array.
 *
 * @version 0.1.0 2016/05/24
 * @author ExBin Project (http://exbin.org)
 */
public class ByteArrayData implements BinaryData {

    protected byte[] data = new byte[0];

    public ByteArrayData() {
    }

    public ByteArrayData(byte[] data) {
        if (data == null) {
            throw new NullPointerException();
        }
        this.data = data;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        if (data == null) {
            throw new NullPointerException();
        }

        this.data = data;
    }

    @Override
    public boolean isEmpty() {
        return data.length == 0;
    }

    @Override
    public long getDataSize() {
        return data.length;
    }

    @Override
    public byte getByte(long position) {
        try {
            return data[(int) position];
        } catch (IndexOutOfBoundsException ex) {
            throw new OutOfBoundsException(ex);
        }
    }

    @Override
    public BinaryData copy() {
        byte[] copy = Arrays.copyOf(data, data.length);
        return new ByteArrayData(copy);
    }

    @Override
    public BinaryData copy(long startFrom, long length) {
        if (startFrom + length > data.length) {
            throw new OutOfBoundsException("Attemt to copy outside of data");
        }

        byte[] copy = Arrays.copyOfRange(data, (int) startFrom, (int) (startFrom + length));
        return new ByteArrayData(copy);
    }

    @Override
    public void copyToArray(long startFrom, byte[] target, int offset, int length) {
        try {
            System.arraycopy(data, (int) startFrom, target, offset, length);
        } catch (IndexOutOfBoundsException ex) {
            throw new OutOfBoundsException(ex);
        }
    }

    @Override
    public void saveToStream(OutputStream outputStream) throws IOException {
        outputStream.write(data);
    }

    @Override
    public InputStream getDataInputStream() {
        return new ByteArrayDataInputStream(this);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            if (obj instanceof BinaryData) {
                // TODO Simple byte comparision, optimize ussing buffer later
                BinaryData other = (BinaryData) obj;
                if (other.getDataSize() != getDataSize()) {
                    return false;
                }

                for (long position = 0; position < getDataSize(); position++) {
                    if (other.getByte(position) != getByte(position)) {
                        return false;
                    }
                }

                return true;
            }

            return false;
        }
        final ByteArrayData other = (ByteArrayData) obj;
        return Arrays.equals(this.data, other.data);
    }

    @Override
    public void dispose() {
    }
}
