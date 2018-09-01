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
package org.exbin.bined.delta;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.annotation.Nonnull;
import org.exbin.utils.binary_data.BinaryData;
import org.exbin.utils.binary_data.EditableBinaryData;
import org.exbin.utils.binary_data.PagedData;

/**
 * Data source for binary data stored in memory.
 *
 * @version 0.2.0 2018/04/27
 * @author ExBin Project (https://exbin.org)
 */
public class MemoryDataSource implements EditableBinaryData {

    @Nonnull
    private final PagedData data;

    public MemoryDataSource() {
        data = new PagedData();
    }

    public MemoryDataSource(@Nonnull PagedData data) {
        this.data = data;
    }

    public MemoryDataSource(@Nonnull byte[] data) {
        this.data = new PagedData();
        this.data.insert(0, data);
    }

    @Override
    public void setDataSize(long size) {
        data.setDataSize(size);
    }

    @Override
    public void setByte(long position, byte value) {
        data.setByte(position, value);
    }

    @Override
    public void insert(long startFrom, long length) {
        data.insert(startFrom, length);
    }

    @Override
    public void insert(long startFrom, @Nonnull byte[] insertedData) {
        data.insert(startFrom, insertedData);
    }

    @Override
    public void insert(long startFrom, @Nonnull BinaryData insertedData) {
        data.insert(startFrom, insertedData);
    }

    @Override
    public void remove(long startFrom, long length) {
        data.remove(startFrom, length);
    }

    @Override
    public boolean isEmpty() {
        return data.isEmpty();
    }

    @Override
    public long getDataSize() {
        return data.getDataSize();
    }

    @Override
    public byte getByte(long position) {
        return data.getByte(position);
    }

    @Override
    public void saveToStream(@Nonnull OutputStream outputStream) throws IOException {
        data.saveToStream(outputStream);
    }

    @Nonnull
    @Override
    public BinaryData copy() {
        return data.copy();
    }

    @Nonnull
    @Override
    public BinaryData copy(long startFrom, long length) {
        return data.copy(startFrom, length);
    }

    @Override
    public void insertUninitialized(long startFrom, long length) {
        data.insertUninitialized(startFrom, length);
    }

    @Override
    public void insert(long startFrom, @Nonnull byte[] insertedData, int insertedDataOffset, int insertedDataLength) {
        data.insert(startFrom, insertedData, insertedDataOffset, insertedDataLength);
    }

    @Override
    public void insert(long startFrom, @Nonnull BinaryData insertedData, long insertedDataOffset, long insertedDataLength) {
        data.insert(startFrom, insertedData, insertedDataOffset, insertedDataLength);
    }

    @Override
    public long insert(long startFrom, @Nonnull InputStream inputStream, long length) throws IOException {
        return data.insert(startFrom, inputStream, length);
    }

    @Override
    public void replace(long targetPosition, @Nonnull BinaryData replacingData) {
        data.replace(targetPosition, replacingData);
    }

    @Override
    public void replace(long targetPosition, @Nonnull BinaryData replacingData, long startFrom, long length) {
        data.replace(targetPosition, replacingData, startFrom, length);
    }

    @Override
    public void replace(long targetPosition, @Nonnull byte[] replacingData) {
        data.replace(targetPosition, replacingData);
    }

    @Override
    public void replace(long targetPosition, @Nonnull byte[] replacingData, int replacingDataOffset, int length) {
        data.replace(targetPosition, replacingData, replacingDataOffset, length);
    }

    @Override
    public void fillData(long startFrom, long length) {
        data.fillData(startFrom, length);
    }

    @Override
    public void fillData(long startFrom, long length, byte fill) {
        data.fillData(startFrom, length, fill);
    }

    @Override
    public void clear() {
        data.clear();
    }

    @Override
    public void loadFromStream(@Nonnull InputStream inputStream) throws IOException {
        data.loadFromStream(inputStream);
    }

    @Nonnull
    @Override
    public OutputStream getDataOutputStream() {
        return data.getDataOutputStream();
    }

    @Override
    public void copyToArray(long startFrom, @Nonnull byte[] target, int offset, int length) {
        data.copyToArray(startFrom, target, offset, length);
    }

    @Nonnull
    @Override
    public InputStream getDataInputStream() {
        return data.getDataInputStream();
    }

    @Override
    public void dispose() {
        data.dispose();
    }
}
