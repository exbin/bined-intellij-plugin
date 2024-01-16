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

import java.awt.Font;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.JOptionPane;
import org.exbin.auxiliary.binary_data.BinaryData;
import org.exbin.auxiliary.binary_data.ByteArrayEditableData;
import org.exbin.auxiliary.binary_data.EditableBinaryData;
import org.exbin.auxiliary.binary_data.EmptyBinaryData;
import org.exbin.auxiliary.binary_data.delta.DeltaDocument;
import org.exbin.auxiliary.binary_data.delta.FileDataSource;
import org.exbin.auxiliary.binary_data.delta.SegmentsRepository;
import org.exbin.auxiliary.binary_data.paged.PagedData;
import org.exbin.bined.operation.swing.CodeAreaUndoHandler;
import org.exbin.bined.operation.undo.BinaryDataUndoHandler;
import org.exbin.bined.swing.extended.ExtCodeArea;
import org.exbin.bined.swing.extended.color.ExtendedCodeAreaColorProfile;
import org.exbin.framework.api.XBApplication;
import org.exbin.framework.bined.gui.BinEdComponentFileApi;
import org.exbin.framework.bined.gui.BinEdComponentPanel;
import org.exbin.framework.bined.preferences.BinaryEditorPreferences;
import org.exbin.framework.editor.text.TextCharsetApi;
import org.exbin.framework.editor.text.TextFontApi;
import org.exbin.framework.file.api.FileType;
import org.exbin.framework.utils.ClipboardActionsHandler;
import org.exbin.framework.utils.ClipboardActionsUpdateListener;
import org.exbin.framework.file.api.FileHandler;
import org.exbin.framework.operation.undo.api.UndoFileHandler;
import org.exbin.xbup.operation.undo.XBUndoHandler;

/**
 * File handler for binary editor.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class BinEdFileHandler implements FileHandler, UndoFileHandler, BinEdComponentFileApi, ClipboardActionsHandler, TextFontApi, TextCharsetApi {

    private SegmentsRepository segmentsRepository;

    @Nonnull
    private final BinEdEditorComponent editorComponent;
    private XBUndoHandler undoHandlerWrapper;
    private int id = 0;
    private URI fileUri = null;
    private FileType fileType;
    private String title;
    private Font defaultFont;
    private ExtendedCodeAreaColorProfile defaultColors;
    private long documentOriginalSize;

    public BinEdFileHandler() {
        editorComponent = new BinEdEditorComponent();
        init();
    }

    public BinEdFileHandler(int id) {
        this();
        this.id = id;
    }

    private void init() {
        final ExtCodeArea codeArea = getCodeArea();
        CodeAreaUndoHandler undoHandler = new CodeAreaUndoHandler(editorComponent.getCodeArea());
        editorComponent.setUndoHandler(undoHandler);
        defaultFont = codeArea.getCodeFont();
        defaultColors = (ExtendedCodeAreaColorProfile) codeArea.getColorsProfile();
    }

    public void setApplication(XBApplication application) {
        editorComponent.setApplication(application);
    }
    
    public void onInitFromPreferences(BinaryEditorPreferences preferences) {
        editorComponent.onInitFromPreferences(preferences);
    }

    @Override
    public void loadFromFile(URI fileUri, FileType fileType) {
        loadFromFile(fileUri, fileType, getFileHandlingMode());
    }

    private void loadFromFile(URI fileUri, FileType fileType, FileHandlingMode fileHandlingMode) {
        this.fileType = fileType;
        File file = new File(fileUri);
        if (!file.isFile()) {
            JOptionPane.showOptionDialog(editorComponent.getComponentPanel(),
                    "File not found",
                    "Unable to load file",
                    JOptionPane.CLOSED_OPTION,
                    JOptionPane.ERROR_MESSAGE,
                    null, null, null);
            return;
        }

        try {
            BinaryData oldData = editorComponent.getContentData();
            if (fileHandlingMode == FileHandlingMode.DELTA) {
                FileDataSource openFileSource = segmentsRepository.openFileSource(file);
                DeltaDocument document = segmentsRepository.createDocument(openFileSource);
                editorComponent.setContentData(document);
                this.fileUri = fileUri;
                oldData.dispose();
            } else {
                try ( FileInputStream fileStream = new FileInputStream(file)) {
                    BinaryData data = editorComponent.getContentData();
                    if (!(data instanceof PagedData)) {
                        data = new PagedData();
                        oldData.dispose();
                    }
                    ((EditableBinaryData) data).loadFromStream(fileStream);
                    editorComponent.setContentData(data);
                    this.fileUri = fileUri;
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(BinEdFileHandler.class.getName()).log(Level.SEVERE, null, ex);
        }

        undoHandlerWrapper.clear();
        fileSync();
    }

    @Override
    public void saveToFile(URI fileUri, FileType fileType) {
        File file = new File(fileUri);
        try {
            BinaryData contentData = editorComponent.getContentData();
            if (contentData instanceof EmptyBinaryData) {
                clearFile();
                contentData = editorComponent.getContentData();
            }
            if (contentData instanceof DeltaDocument) {
                // TODO freezes window / replace with progress bar
                DeltaDocument document = (DeltaDocument) contentData;
                FileDataSource fileSource = document.getFileSource();
                if (fileSource == null || !file.equals(fileSource.getFile())) {
                    fileSource = segmentsRepository.openFileSource(file);
                    document.setFileSource(fileSource);
                }
                segmentsRepository.saveDocument(document);
                this.fileUri = fileUri;
            } else {
                try ( FileOutputStream outputStream = new FileOutputStream(file)) {
                    Objects.requireNonNull(contentData).saveToStream(outputStream);
                    this.fileUri = fileUri;
                }
            }
            // TODO
//            documentOriginalSize = codeArea.getDataSize();
//            updateCurrentDocumentSize();
//            updateCurrentMemoryMode();
        } catch (IOException ex) {
            Logger.getLogger(BinEdFileHandler.class.getName()).log(Level.SEVERE, null, ex);
        }

        fileSync();
    }

    private void fileSync() {
        documentOriginalSize = getCodeArea().getDataSize();
        undoHandlerWrapper.setSyncPoint();
    }

    public void loadFromStream(InputStream stream) throws IOException {
        BinaryData contentData = editorComponent.getContentData();
        if (!(contentData instanceof EditableBinaryData)) {
            contentData = new ByteArrayEditableData();
            // TODO: stream to binary data
        }

        EditableBinaryData data = Objects.requireNonNull((EditableBinaryData) contentData);
        data.loadFromStream(stream);
        editorComponent.setContentData(contentData);
    }

    public void loadFromStream(InputStream stream, long dataSize) throws IOException {
        BinaryData contentData = editorComponent.getContentData();
        if (!(contentData instanceof EditableBinaryData)) {
            contentData = new ByteArrayEditableData();
        }

        EditableBinaryData data = Objects.requireNonNull((EditableBinaryData) contentData);
        data.clear();
        data.insert(0, stream, dataSize);
        editorComponent.setContentData(contentData);
    }

    public void saveToStream(OutputStream stream) throws IOException {
        BinaryData data = Objects.requireNonNull((BinaryData) editorComponent.getContentData());
        data.saveToStream(stream);
    }

    @Nonnull
    @Override
    public Optional<URI> getFileUri() {
        return Optional.ofNullable(fileUri);
    }

    @Override
    public void clearFile() {
        FileHandlingMode fileHandlingMode = getFileHandlingMode();
        closeData();
        ExtCodeArea codeArea = editorComponent.getCodeArea();
        BinaryData data = codeArea.getContentData();
        if (data instanceof DeltaDocument) {
            segmentsRepository.dropDocument(Objects.requireNonNull((DeltaDocument) codeArea.getContentData()));
        }
        setNewData(fileHandlingMode);
        fileUri = null;
        if (undoHandlerWrapper != null) {
            undoHandlerWrapper.clear();
        }
    }

    @Override
    public int getId() {
        return id;
    }

    @Nonnull
    @Override
    public String getTitle() {
        if (fileUri != null) {
            String path = fileUri.getPath();
            int lastSegment = path.lastIndexOf("/");
            String fileName = lastSegment < 0 ? path : path.substring(lastSegment + 1);
            return fileName == null ? "" : fileName;
        }

        return title == null ? "" : title;
    }

    public void setTitle(@Nullable String title) {
        this.title = title;
    }

    @Nonnull
    public String getWindowTitle(String windowTitle) {
        if (fileUri != null) {
            String path = fileUri.getPath();
            int lastIndexOf = path.lastIndexOf("/");
            if (lastIndexOf < 0) {
                return path + " - " + windowTitle;
            }
            return path.substring(lastIndexOf + 1) + " - " + windowTitle;
        }

        return windowTitle;
    }

    public long getDocumentOriginalSize() {
        return documentOriginalSize;
    }

    @Override
    public void saveFile() {
        ExtCodeArea codeArea = editorComponent.getCodeArea();
        BinaryData data = codeArea.getContentData();
        if (data instanceof DeltaDocument) {
            try {
                segmentsRepository.saveDocument((DeltaDocument) data);
            } catch (IOException ex) {
                Logger.getLogger(BinEdFileHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            File file = new File(fileUri);
            try ( OutputStream stream = new FileOutputStream(file)) {
                BinaryData contentData = codeArea.getContentData();
                contentData.saveToStream(stream);
                stream.flush();
            } catch (IOException ex) {
                Logger.getLogger(BinEdFileHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void reloadFile() {
        if (fileUri != null) {
            loadFromFile(fileUri, fileType, getFileHandlingMode());
        }
    }

    @Override
    public void closeData() {
        ExtCodeArea codeArea = editorComponent.getCodeArea();
        BinaryData data = codeArea.getContentData();
        editorComponent.setContentData(EmptyBinaryData.INSTANCE);
        if (data instanceof DeltaDocument) {
            FileDataSource fileSource = ((DeltaDocument) data).getFileSource();
            data.dispose();
            if (fileSource != null) {
                segmentsRepository.detachFileSource(fileSource);
                segmentsRepository.closeFileSource(fileSource);
            }
        } else {
            data.dispose();
        }
    }

    @Override
    public void saveDocument() {
        if (fileUri == null) {
            return;
        }

        saveFile();
    }

    @Override
    public void switchFileHandlingMode(FileHandlingMode handlingMode) {
        FileHandlingMode oldFileHandlingMode = getFileHandlingMode();
        ExtCodeArea codeArea = editorComponent.getCodeArea();
        if (handlingMode != oldFileHandlingMode) {
            if (fileUri != null) {
                loadFromFile(fileUri, null, handlingMode);
            } else {
                BinaryData oldData = codeArea.getContentData();
                if (oldData instanceof DeltaDocument) {
                    PagedData data = new PagedData();
                    data.insert(0, oldData);
                    editorComponent.setContentData(data);
                } else {
                    DeltaDocument document = segmentsRepository.createDocument();
                    document.insert(0, oldData);
                    editorComponent.setContentData(document);
                }

                if (undoHandlerWrapper != null) {
                    undoHandlerWrapper.clear();
                }

                oldData.dispose();
            }
        }
    }

    @Nonnull
    public FileHandlingMode getFileHandlingMode() {
        return getCodeArea().getContentData() instanceof DeltaDocument ? FileHandlingMode.DELTA : FileHandlingMode.MEMORY;
    }

    @Nonnull
    @Override
    public BinEdComponentPanel getComponent() {
        return editorComponent.getComponentPanel();
    }

    @Nonnull
    public BinEdEditorComponent getEditorComponent() {
        return editorComponent;
    }

    @Nonnull
    @Override
    public ExtCodeArea getCodeArea() {
        return editorComponent.getCodeArea();
    }

    @Nonnull
    @Override
    public Optional<FileType> getFileType() {
        return Optional.empty();
    }

    @Override
    public void setFileType(FileType fileType) {
    }

    @Override
    public boolean isModified() {
        return undoHandlerWrapper.getCommandPosition() != undoHandlerWrapper.getSyncPoint();
    }

    public void setNewData(FileHandlingMode fileHandlingMode) {
        if (fileHandlingMode == FileHandlingMode.DELTA) {
            editorComponent.setContentData(segmentsRepository.createDocument());
        } else {
            editorComponent.setContentData(new PagedData());
        }
    }

    public void setSegmentsRepository(SegmentsRepository segmentsRepository) {
        this.segmentsRepository = segmentsRepository;
    }

    public void requestFocus() {
        editorComponent.getCodeArea().requestFocus();
    }

    @Nonnull
    @Override
    public XBUndoHandler getUndoHandler() {
        if (undoHandlerWrapper == null) {
            undoHandlerWrapper = new UndoHandlerWrapper();
            ((UndoHandlerWrapper) undoHandlerWrapper).setHandler(editorComponent.getUndoHandler().orElse(null));
        }
        return undoHandlerWrapper;
    }

    @Nonnull
    public Optional<BinaryDataUndoHandler> getCodeAreaUndoHandler() {
        return editorComponent.getUndoHandler();
    }

    @Override
    public boolean isSaveSupported() {
        return true;
    }

    @Override
    public void performCut() {
        getCodeArea().cut();
    }

    @Override
    public void performCopy() {
        getCodeArea().copy();
    }

    @Override
    public void performPaste() {
        getCodeArea().paste();
    }

    @Override
    public void performDelete() {
        getCodeArea().delete();
    }

    @Override
    public void performSelectAll() {
        getCodeArea().selectAll();
    }

    @Override
    public boolean isSelection() {
        return getCodeArea().hasSelection();
    }

    @Override
    public boolean isEditable() {
        return getCodeArea().isEditable();
    }

    @Override
    public boolean canSelectAll() {
        return true;
    }

    @Override
    public boolean canPaste() {
        return getCodeArea().canPaste();
    }

    @Override
    public boolean canDelete() {
        return true;
    }

    @Override
    public void setCurrentFont(Font font) {
        getCodeArea().setCodeFont(font);
    }

    @Nonnull
    @Override
    public Font getCurrentFont() {
        return getCodeArea().getCodeFont();
    }

    @Nonnull
    @Override
    public Font getDefaultFont() {
        return defaultFont;
    }

    @Nonnull
    public ExtendedCodeAreaColorProfile getDefaultColors() {
        return defaultColors;
    }

    @Nonnull
    @Override
    public Charset getCharset() {
        return getCodeArea().getCharset();
    }

    @Override
    public void setCharset(Charset charset) {
        getCodeArea().setCharset(charset);
    }

    @Override
    public void setUpdateListener(ClipboardActionsUpdateListener updateListener) {
        // componentPanel.setUpdateListener(updateListener);
    }
}
