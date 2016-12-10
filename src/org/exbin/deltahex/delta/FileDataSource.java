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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data source for access to file resource locking it for exclusive access.
 *
 * @version 0.1.1 2016/09/21
 * @author ExBin Project (http://exbin.org)
 */
public class FileDataSource {

    private final File file;
    private final RandomAccessFile accessFile;
    private final DeltaDataPageWindow window;
    private boolean closed = false;

    private List<CacheClearListener> listeners = new ArrayList<>();

    public FileDataSource(File sourceFile) throws FileNotFoundException, IOException {
        file = sourceFile;
        accessFile = new RandomAccessFile(sourceFile, "rw");
        window = new DeltaDataPageWindow(this);
    }

    public long getFileLength() throws IOException {
        checkClosed();
        return accessFile.length();
    }

    public File getFile() {
        return file;
    }

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

    public void addCacheClearListener(CacheClearListener listener) {
        listeners.add(listener);
    }
            
    public void removeCacheClearListener(CacheClearListener listener) {
        listeners.remove(listener);
    }
            
    public static interface CacheClearListener {

        public void clearCache();
    }
}
