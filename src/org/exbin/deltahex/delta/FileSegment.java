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
package org.exbin.deltahex.delta;

/**
 * Data segment pointing to file.
 *
 * @version 0.1.0 2016/06/21
 * @author ExBin Project (http://exbin.org)
 */
public class FileSegment extends DataSegment {

    private final FileDataSource source;
    private long startPosition;
    private long length;

    public FileSegment(FileDataSource source, long startPosition, long length) {
        this.source = source;
        this.startPosition = startPosition;
        this.length = length;
    }

    public FileDataSource getSource() {
        return source;
    }

    @Override
    public long getStartPosition() {
        return startPosition;
    }

    public void setStartPosition(long startPosition) {
        this.startPosition = startPosition;
    }

    @Override
    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }
    
    public byte getByte(long position) {
        return source.getByte(position);
    }

    @Override
    public DataSegment copy() {
        return new FileSegment(null, startPosition, length);
    }
}
