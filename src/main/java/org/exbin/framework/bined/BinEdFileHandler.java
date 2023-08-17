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
import org.exbin.bined.intellij.gui.BinEdComponentPanel;
import org.exbin.bined.intellij.main.BinEdEditorComponent;
import org.exbin.bined.intellij.main.BinEdManager;
import org.exbin.bined.operation.swing.CodeAreaUndoHandler;
import org.exbin.bined.swing.extended.ExtCodeArea;
import org.exbin.framework.bined.gui.BinEdComponentFileApi;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.JComponent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
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

    private static SegmentsRepository segmentsRepository;

    private BinEdEditorComponent editorComponent;

    private boolean opened = false;
    private BinEdVirtualFile virtualFile;
    private BinEdFileEditorState fileEditorState = new BinEdFileEditorState();
    private CodeAreaUndoHandler undoHandler;
    private long documentOriginalSize;

    public BinEdFileHandler() {
        init();
    }

    private void init() {
        BinEdManager binEdManager = BinEdManager.getInstance();
        editorComponent = binEdManager.createBinEdEditor();
        ExtCodeArea codeArea = editorComponent.getCodeArea();
        undoHandler = new CodeAreaUndoHandler(codeArea);
        editorComponent.setFileApi(this);
        editorComponent.setUndoHandler(undoHandler);

        // TODO undoHandler = new BinaryUndoIntelliJHandler(codeArea, project, this);

        getSegmentsRepository();
    }

    public boolean isModified() {
        return editorComponent.isModified();
    }

    public boolean releaseFile() {
        return editorComponent.releaseFile();
    }

    @Nonnull
    public BinEdComponentPanel getComponent() {
        return editorComponent.getComponentPanel();
    }

    @Nonnull
    public ExtCodeArea getCodeArea() {
        return editorComponent.getCodeArea();
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

            editorComponent.getUndoHandler().clear();
            fileSync();
        }
    }

    public void openDocument(File file, boolean editable) throws IOException {
        ExtCodeArea codeArea = editorComponent.getCodeArea();
        FileHandlingMode fileHandlingMode = editorComponent.getFileHandlingMode();

        BinaryData oldData = codeArea.getContentData();
        if (fileHandlingMode == FileHandlingMode.DELTA) {
            FileDataSource fileSource = segmentsRepository.openFileSource(file, editable ? FileDataSource.EditMode.READ_WRITE : FileDataSource.EditMode.READ_ONLY);
            DeltaDocument document = segmentsRepository.createDocument(fileSource);
            editorComponent.setContentData(document);
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
                editorComponent.setContentData(data);
            }
        }
        codeArea.setEditMode(editable ? EditMode.EXPANDING : EditMode.READ_ONLY);
    }

    public void openDocument(InputStream stream, boolean editable) throws IOException {
        ExtCodeArea codeArea = editorComponent.getCodeArea();
        setNewData();
        EditableBinaryData data = Objects.requireNonNull((EditableBinaryData) codeArea.getContentData());
        data.loadFromStream(stream);
        codeArea.setEditMode(editable ? EditMode.EXPANDING : EditMode.READ_ONLY);
        editorComponent.setContentData(data);
    }

    @Override
    public void saveDocument() {
        Application application = ApplicationManager.getApplication();
        application.runWriteAction(() -> saveFile());
    }

    public void saveFile() {
        ExtCodeArea codeArea = editorComponent.getCodeArea();
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
        FileHandlingMode fileHandlingMode = editorComponent.getFileHandlingMode();
        ExtCodeArea codeArea = editorComponent.getCodeArea();
        if (newHandlingMode != fileHandlingMode) {
            // Switch memory mode
            if (virtualFile != null) {
                // If document is connected to file, attempt to release first if modified and then simply reload
                if (isModified()) {
                    if (releaseFile()) {
                        openFile(virtualFile);
                        codeArea.clearSelection();
                        codeArea.setCaretPosition(0);
                        editorComponent.setFileHandlingMode(newHandlingMode);
                    }
                } else {
                    editorComponent.setFileHandlingMode(newHandlingMode);
                    openFile(virtualFile);
                }
            } else {
                // If document unsaved in memory, switch data in code area
                if (codeArea.getContentData() instanceof DeltaDocument) {
                    BinaryData oldData = codeArea.getContentData();
                    PagedData data = new PagedData();
                    data.insert(0, codeArea.getContentData());
                    editorComponent.setContentData(data);
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
                    editorComponent.setContentData(document);
                }
                editorComponent.getUndoHandler().clear();
                editorComponent.setFileHandlingMode(newHandlingMode);
            }
        }
    }

    public void disposeData() {
        ExtCodeArea codeArea = editorComponent.getCodeArea();
        BinaryData data = codeArea.getContentData();
        editorComponent.setContentData(new ByteArrayData());
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
        editorComponent.setContentData(null);
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
        FileHandlingMode fileHandlingMode = editorComponent.getFileHandlingMode();
        if (fileHandlingMode == FileHandlingMode.DELTA) {
            editorComponent.setContentData(segmentsRepository.createDocument());
        } else {
            editorComponent.setContentData(new PagedData());
        }
    }

    public void setSegmentsRepository(SegmentsRepository segmentsRepository) {
        this.segmentsRepository = segmentsRepository;
    }

    @Nonnull
    public JComponent getPreferredFocusedComponent() {
        return editorComponent.getCodeArea();
    }

    @Nonnull
    public Charset getCharset() {
        return getCodeArea().getCharset();
    }

    public void setCharset(Charset charset) {
        getCodeArea().setCharset(charset);
    }
}
