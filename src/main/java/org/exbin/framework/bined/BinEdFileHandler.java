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
package org.exbin.framework.bined;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.DumbAware;
import org.exbin.auxiliary.paged_data.BinaryData;
import org.exbin.auxiliary.paged_data.ByteArrayData;
import org.exbin.auxiliary.paged_data.EditableBinaryData;
import org.exbin.auxiliary.paged_data.PagedData;
import org.exbin.auxiliary.paged_data.delta.DeltaDocument;
import org.exbin.auxiliary.paged_data.delta.FileDataSource;
import org.exbin.auxiliary.paged_data.delta.SegmentsRepository;
import org.exbin.bined.EditMode;
import org.exbin.bined.intellij.BinEdFileEditorState;
import org.exbin.bined.intellij.BinEdVirtualFile;
import org.exbin.bined.intellij.gui.BinEdComponentFileApi;
import org.exbin.bined.intellij.gui.BinEdComponentPanel;
import org.exbin.bined.operation.swing.CodeAreaUndoHandler;
import org.exbin.bined.swing.extended.ExtCodeArea;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.JComponent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * File editor wrapper using BinEd editor component.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class BinEdFileHandler implements BinEdComponentFileApi, DumbAware {

    private static SegmentsRepository segmentsRepository = null;

    private final BinEdComponentPanel componentPanel;

    private boolean opened = false;
    private BinEdVirtualFile virtualFile;
    private BinEdFileEditorState fileEditorState = new BinEdFileEditorState();
    private CodeAreaUndoHandler undoHandler;
    private long documentOriginalSize;

    public BinEdFileHandler() {
        componentPanel = new BinEdComponentPanel();
        init();
    }

    private void init() {
        ExtCodeArea codeArea = componentPanel.getCodeArea();
        undoHandler = new CodeAreaUndoHandler(codeArea);
        componentPanel.setFileApi(this);
        componentPanel.setUndoHandler(undoHandler);

        // TODO undoHandler = new BinaryUndoIntelliJHandler(codeArea, project, this);

        getSegmentsRepository();
    }

    public boolean isModified() {
        return componentPanel.isModified();
    }

    public boolean releaseFile() {
        return componentPanel.releaseFile();
    }

    @Nonnull
    public BinEdComponentPanel getComponent() {
        return componentPanel;
    }

    @Nonnull
    public ExtCodeArea getCodeArea() {
        return componentPanel.getCodeArea();
    }

    public static synchronized SegmentsRepository getSegmentsRepository() {
        if (segmentsRepository == null) {
            segmentsRepository = new SegmentsRepository();
        }

        return segmentsRepository;
    }

    public void openFile(BinEdVirtualFile virtualFile) {
        if (!virtualFile.isDirectory() && virtualFile.isValid()) {
            if (this.virtualFile != null) {
                closeData();
            }
            this.virtualFile = virtualFile;
            boolean editable = virtualFile.isWritable();
            File file = extractFile(virtualFile);
            if (file.isFile() && file.exists()) {
                try {
                    openDocument(file, editable);
                } catch (IOException ex) {
                    Logger.getLogger(BinEdFileHandler.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                try (InputStream stream = virtualFile.getInputStream()) {
                    openDocument(stream, editable);
                } catch (IOException ex) {
                    Logger.getLogger(BinEdFileHandler.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            opened = true;

            componentPanel.getUndoHandler().clear();
            fileSync();
        }
    }

    public void openDocument(File file, boolean editable) throws IOException {
        ExtCodeArea codeArea = componentPanel.getCodeArea();
        FileHandlingMode fileHandlingMode = componentPanel.getFileHandlingMode();

        BinaryData oldData = codeArea.getContentData();
        if (fileHandlingMode == FileHandlingMode.DELTA) {
            FileDataSource fileSource = segmentsRepository.openFileSource(file, editable ? FileDataSource.EditMode.READ_WRITE : FileDataSource.EditMode.READ_ONLY);
            DeltaDocument document = segmentsRepository.createDocument(fileSource);
            componentPanel.setContentData(document);
            if (oldData != null) {
                oldData.dispose();
            }
        } else {
            try (FileInputStream fileStream = new FileInputStream(file)) {
                BinaryData data = codeArea.getContentData();
                if (!(data instanceof PagedData)) {
                    data = new PagedData();
                    if (oldData != null) {
                        oldData.dispose();
                    }
                }
                ((EditableBinaryData) data).loadFromStream(fileStream);
                componentPanel.setContentData(data);
            }
        }
        codeArea.setEditMode(editable ? EditMode.EXPANDING : EditMode.READ_ONLY);
    }

    public void openDocument(InputStream stream, boolean editable) throws IOException {
        ExtCodeArea codeArea = componentPanel.getCodeArea();
        setNewData();
        EditableBinaryData data = Objects.requireNonNull((EditableBinaryData) codeArea.getContentData());
        data.loadFromStream(stream);
        codeArea.setEditMode(editable ? EditMode.EXPANDING : EditMode.READ_ONLY);
        componentPanel.setContentData(data);
    }

    @Override
    public void saveDocument() {
        Application application = ApplicationManager.getApplication();
        application.runWriteAction(() -> saveFile());
    }

    public void saveFile() {
        ExtCodeArea codeArea = componentPanel.getCodeArea();
        BinaryData data = codeArea.getContentData();
        Application application = ApplicationManager.getApplication();
        ApplicationManager.getApplication().invokeLater(() -> {
            application.runWriteAction(new Runnable() {
                @Override
                public void run() {
                    if (data instanceof DeltaDocument) {
                        try {
                            segmentsRepository.saveDocument((DeltaDocument) data);
                        } catch (IOException ex) {
                            Logger.getLogger(BinEdFileHandler.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    } else {
                        try (OutputStream stream = virtualFile.getOutputStream(this)) {
                            BinaryData contentData = codeArea.getContentData();
                            if (contentData != null) {
                                contentData.saveToStream(stream);
                            }

                            stream.flush();
                        } catch (IOException ex) {
                            Logger.getLogger(BinEdFileHandler.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            });
        });
        fileSync();
    }

    private void fileSync() {
        documentOriginalSize = getCodeArea().getDataSize();
        undoHandler.setSyncPoint();
    }

    public long getDocumentOriginalSize() {
        return documentOriginalSize;
    }

    public void reloadFile() {
        openFile(virtualFile);
    }

    @Override
    public void switchFileHandlingMode(FileHandlingMode newHandlingMode) {
        FileHandlingMode fileHandlingMode = componentPanel.getFileHandlingMode();
        ExtCodeArea codeArea = componentPanel.getCodeArea();
        if (newHandlingMode != fileHandlingMode) {
            // Switch memory mode
            if (virtualFile != null) {
                // If document is connected to file, attempt to release first if modified and then simply reload
                if (isModified()) {
                    if (releaseFile()) {
                        openFile(virtualFile);
                        codeArea.clearSelection();
                        codeArea.setCaretPosition(0);
                        componentPanel.setFileHandlingMode(newHandlingMode);
                    }
                } else {
                    componentPanel.setFileHandlingMode(newHandlingMode);
                    openFile(virtualFile);
                }
            } else {
                // If document unsaved in memory, switch data in code area
                if (codeArea.getContentData() instanceof DeltaDocument) {
                    BinaryData oldData = codeArea.getContentData();
                    PagedData data = new PagedData();
                    data.insert(0, codeArea.getContentData());
                    componentPanel.setContentData(data);
                    if (oldData != null) {
                        oldData.dispose();
                    }
                } else {
                    BinaryData oldData = codeArea.getContentData();
                    DeltaDocument document = segmentsRepository.createDocument();
                    if (oldData != null) {
                        document.insert(0, oldData);
                        oldData.dispose();
                    }
                    componentPanel.setContentData(document);
                }
                componentPanel.getUndoHandler().clear();
                componentPanel.setFileHandlingMode(newHandlingMode);
            }
        }
    }

    public void disposeData() {
        ExtCodeArea codeArea = componentPanel.getCodeArea();
        BinaryData data = codeArea.getContentData();
        componentPanel.setContentData(new ByteArrayData());
        if (data instanceof DeltaDocument) {
            data.dispose();
        } else {
            if (data != null) {
                data.dispose();
            }
        }
    }

    @Override
    public void closeData() {
        disposeData();
        componentPanel.setContentData(null);
        virtualFile = null;
    }

    @Nonnull
    private File extractFile(BinEdVirtualFile virtualFile) {
        String path = virtualFile.getPath();
        if (path.startsWith("bined://")) {
            path = path.substring(8);
        }
        return new File(path);
    }

    @Nullable
    public BinEdVirtualFile getVirtualFile() {
        return virtualFile;
    }

    @Override
    public boolean isSaveSupported() {
        return true;
    }

    private void setNewData() {
        FileHandlingMode fileHandlingMode = componentPanel.getFileHandlingMode();
        if (fileHandlingMode == FileHandlingMode.DELTA) {
            componentPanel.setContentData(segmentsRepository.createDocument());
        } else {
            componentPanel.setContentData(new PagedData());
        }
    }

    @Nonnull
    public JComponent getPreferredFocusedComponent() {
        return componentPanel.getCodeArea();
    }
}
