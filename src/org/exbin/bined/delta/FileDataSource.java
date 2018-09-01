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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;

/**
 * Data source for access to file resource locking it for exclusive access.
 *
 * @version 0.2.0 2018/04/27
 * @author ExBin Project (https://exbin.org)
 */
public class FileDataSource {

    @Nonnull
    private final File file;
    @Nonnull
    private final RandomAccessFile accessFile;
    @Nonnull
    private final DeltaDataPageWindow window;
    private boolean closed = false;

    private final List<CacheClearListener> listeners = new ArrayList<>();

    public FileDataSource(@Nonnull File sourceFile, @Nonnull EditationMode editationMode) throws FileNotFoundException, IOException {
        file = sourceFile;
        accessFile = new RandomAccessFile(sourceFile, editationMode.getFileAccessMode());
        window = new DeltaDataPageWindow(this);
    }

    public FileDataSource(@Nonnull File sourceFile) throws FileNotFoundException, IOException {
        this(sourceFile, EditationMode.READ_WRITE);
    }

    public long getFileLength() throws IOException {
        checkClosed();
        return accessFile.length();
    }

    public void setFileLength(long length) throws IOException {
        checkClosed();
        accessFile.setLength(length);
    }

    @Nonnull
    public File getFile() {
        return file;
    }

    @Nonnull
    /* package */ RandomAccessFile getAccessFile() {
        checkClosed();
        return accessFile;
    }

    public byte getByte(long position) {
        checkClosed();
        return window.getByte(position);
    }

    /**
     * Clears cache windows.
     */
    public void clearCache() {
        for (CacheClearListener listener : listeners) {
            listener.clearCache();
        }
    }

    public void close() {
        checkClosed();
        try {
            accessFile.close();
        } catch (IOException ex) {
            Logger.getLogger(FileDataSource.class.getName()).log(Level.SEVERE, null, ex);
        }
        closed = true;
    }

    private void checkClosed() {
        if (closed) {
            throw new IllegalStateException("");
        }
    }

    public void addCacheClearListener(@Nonnull CacheClearListener listener) {
        listeners.add(listener);
    }

    public void removeCacheClearListener(@Nonnull CacheClearListener listener) {
        listeners.remove(listener);
    }

    public static interface CacheClearListener {

        public void clearCache();
    }

    public static enum EditationMode {
        READ_WRITE("rw"),
        READ_ONLY("r");

        @Nonnull
        private final String fileAccessMode;

        private EditationMode(@Nonnull String fileAccessMode) {
            this.fileAccessMode = fileAccessMode;
        }

        @Nonnull
        public String getFileAccessMode() {
            return fileAccessMode;
        }
    }
}
