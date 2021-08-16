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

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.ex.DocumentEx;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.util.LocalTimeCounter;
import org.exbin.auxiliary.paged_data.BinaryData;
import org.exbin.auxiliary.paged_data.ByteArrayData;
import org.exbin.auxiliary.paged_data.EditableBinaryData;
import org.exbin.auxiliary.paged_data.PagedData;
import org.exbin.auxiliary.paged_data.delta.DeltaDocument;
import org.exbin.auxiliary.paged_data.delta.FileDataSource;
import org.exbin.auxiliary.paged_data.delta.SegmentsRepository;
import org.exbin.bined.EditMode;
import org.exbin.bined.intellij.gui.BinEdComponentFileApi;
import org.exbin.bined.intellij.gui.BinEdComponentPanel;
import org.exbin.bined.operation.swing.CodeAreaUndoHandler;
import org.exbin.bined.swing.extended.ExtCodeArea;
import org.exbin.framework.bined.FileHandlingMode;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.util.Objects;

/**
 * File editor wrapper using BinEd editor component.
 *
 * @author ExBin Project (http://exbin.org)
 * @version 0.2.2 2020/01/24
 */
public class BinEdFile implements BinEdComponentFileApi, DumbAware {

    public static final String ACTION_CLIPBOARD_CUT = "cut-to-clipboard";
    public static final String ACTION_CLIPBOARD_COPY = "copy-to-clipboard";
    public static final String ACTION_CLIPBOARD_PASTE = "paste-from-clipboard";

    private static SegmentsRepository segmentsRepository = null;

    private final BinEdComponentPanel componentPanel;

    private boolean opened = false;
    private BinEdVirtualFile virtualFile;
    private BinEdFileEditorState fileEditorState = new BinEdFileEditorState();

    public BinEdFile() {
        componentPanel = new BinEdComponentPanel();
        ExtCodeArea codeArea = componentPanel.getCodeArea();
        CodeAreaUndoHandler undoHandler = new CodeAreaUndoHandler(codeArea);
        componentPanel.setFileApi(this);
        componentPanel.setUndoHandler(undoHandler);

        // TODO undoHandler = new BinaryUndoIntelliJHandler(codeArea, project, this);

        getSegmentsRepository();

        componentPanel.setModifiedChangeListener(() -> {
            updateModified();
        });

        ActionMap actionMap = componentPanel.getActionMap();
        actionMap.put(ACTION_CLIPBOARD_COPY, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                codeArea.copy();
            }
        });
        actionMap.put(ACTION_CLIPBOARD_CUT, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                codeArea.cut();
            }
        });
        actionMap.put(ACTION_CLIPBOARD_PASTE, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                codeArea.paste();
            }
        });
    }

    public boolean isModified() {
        return componentPanel.isModified();
    }

    public boolean releaseFile() {
        return componentPanel.releaseFile();
    }

    @Nonnull
    public JPanel getPanel() {
        return componentPanel;
    }

    public static synchronized SegmentsRepository getSegmentsRepository() {
        if (segmentsRepository == null) {
            segmentsRepository = new SegmentsRepository();
        }

        return segmentsRepository;
    }

    public void openFile(BinEdVirtualFile virtualFile) {
        if (!virtualFile.isDirectory() && virtualFile.isValid()) {
            this.virtualFile = virtualFile;
            boolean editable = virtualFile.isWritable();
            File file = extractFile(virtualFile);
            if (file.isFile() && file.exists()) {
                try {
                    openDocument(file, editable);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            } else {
                try (InputStream stream = virtualFile.getInputStream()) {
                    if (stream != null) {
                        closeData();
                        openDocument(stream, editable);
                    }
                } catch (IOException ex) {
                    // Exceptions.printStackTrace(ex);
                }
            }

            opened = true;
//            documentOriginalSize = codeArea.getDataSize();
            updateModified();
//            updateCurrentMemoryMode();
            componentPanel.getUndoHandler().clear();
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

//    public void reopenFile(@Nonnull BinEdVirtualFile virtualFile) {
//        this.virtualFile = virtualFile;
//        BinaryData data = codeArea.getContentData();
//        boolean editable = virtualFile.isWritable();
//        codeArea.setEditMode(editable ? EditMode.EXPANDING : EditMode.READ_ONLY);
//
//        switchFileHandlingMode(data instanceof DeltaDocument ? FileHandlingMode.DELTA : FileHandlingMode.MEMORY);
//        if (data instanceof DeltaDocument) {
//            DeltaDocument document = (DeltaDocument) codeArea.getContentData();
//            document.setFileSource(((DeltaDocument) data).getFileSource());
//        }
//
//        opened = true;
//        documentOriginalSize = codeArea.getDataSize();
//        updateModified();
//        updateCurrentMemoryMode();
//
//        this.undoHandler.clear();
//        // TODO migrate undo
//        try {
//            this.undoHandler.execute(new InsertDataCommand(codeArea, 0, (EditableBinaryData) CMSObjectIdentifiers.data));
//        } catch (BinaryDataOperationException e) {
//            e.printStackTrace();
//        }
//    }

    @Override
    public void saveDocument() {
        Application application = ApplicationManager.getApplication();
        application.runWriteAction(new Runnable() {
            @Override
            public void run() {
                saveFile();
            }
        });
        updateModified();
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
                            ex.printStackTrace();
                        }
                    } else {
                        try (OutputStream stream = virtualFile.getOutputStream(this)) {
                            BinaryData contentData = codeArea.getContentData();
                            if (contentData != null) {
                                contentData.saveToStream(stream);
                            }

                            stream.flush();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        });
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

    public void closeData(boolean closeFileSource) {
        if (closeFileSource) {
            closeData();
        } else {
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

        virtualFile = null;
    }

    @Override
    public void closeData() {
        ExtCodeArea codeArea = componentPanel.getCodeArea();
        BinaryData data = codeArea.getContentData();
        componentPanel.setContentData(new ByteArrayData());
        if (data instanceof DeltaDocument) {
            FileDataSource fileSource = Objects.requireNonNull(((DeltaDocument) data).getFileSource());
            data.dispose();
            segmentsRepository.detachFileSource(fileSource);
            segmentsRepository.closeFileSource(fileSource);
        } else {
            if (data != null) {
                data.dispose();
            }
        }
        componentPanel.setContentData(null);
    }

    @Nonnull
    private File extractFile(BinEdVirtualFile virtualFile) {
        String path = virtualFile.getPath();
        if (path.startsWith("bined://")) {
            path = path.substring(8);
        }
        return new File(path);
    }

    private void updateModified() {
        boolean modified = componentPanel.isModified();
//        // TODO: Trying to force "modified behavior"
        Document document = FileDocumentManager.getInstance().getDocument(virtualFile);
        if (document instanceof DocumentEx) {
            ((DocumentEx) document).setModificationStamp(LocalTimeCounter.currentTime());
        }
//        propertyChangeSupport.firePropertyChange(FileEditor.PROP_MODIFIED, !modified, modified);
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
        ExtCodeArea codeArea = componentPanel.getCodeArea();
        FileHandlingMode fileHandlingMode = componentPanel.getFileHandlingMode();
        if (fileHandlingMode == FileHandlingMode.DELTA) {
            componentPanel.setContentData(segmentsRepository.createDocument());
        } else {
            componentPanel.setContentData(new PagedData());
        }
    }

    public JComponent getPreferredFocusedComponent() {
        return componentPanel.getCodeArea();
    }
}
