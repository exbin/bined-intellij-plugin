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

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.ex.DocumentEx;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.util.LocalTimeCounter;
import org.exbin.auxiliary.paged_data.BinaryData;
import org.exbin.auxiliary.paged_data.ByteArrayData;
import org.exbin.auxiliary.paged_data.EditableBinaryData;
import org.exbin.auxiliary.paged_data.PagedData;
import org.exbin.auxiliary.paged_data.delta.DeltaDocument;
import org.exbin.bined.EditationMode;
import org.exbin.bined.intellij.panel.BinEdComponentPanel;
import org.exbin.bined.operation.swing.CodeAreaUndoHandler;
import org.exbin.bined.swing.extended.ExtCodeArea;

import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * File editor using BinEd editor component.
 *
 * @author ExBin Project (http://exbin.org)
 * @version 0.2.2 2020/01/17
 */
public class BinEdEditorPanel extends JPanel {

    public static final String ACTION_CLIPBOARD_CUT = "cut-to-clipboard";
    public static final String ACTION_CLIPBOARD_COPY = "copy-to-clipboard";
    public static final String ACTION_CLIPBOARD_PASTE = "paste-from-clipboard";

    private final BinEdComponentPanel componentPanel;

    private boolean opened = false;
    private BinEdVirtualFile virtualFile;
    private BinEdFileEditorState fileEditorState = new BinEdFileEditorState();

    public BinEdEditorPanel() {
        super();
        initComponents();

        componentPanel = new BinEdComponentPanel();
        ExtCodeArea codeArea = componentPanel.getCodeArea();
        CodeAreaUndoHandler undoHandler = new CodeAreaUndoHandler(codeArea);

        componentPanel.setUndoHandler(undoHandler);
        add(componentPanel, BorderLayout.CENTER);

        // CodeAreaUndoHandler(codeArea);
        // undoHandler = new BinaryUndoIntelliJHandler(codeArea, project, this);

        componentPanel.setModifiedChangeListener(() -> {
            updateModified();
        });

        componentPanel.setSaveAction(this::saveFileButtonActionPerformed);

        this.getActionMap().put(ACTION_CLIPBOARD_COPY, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                codeArea.copy();
            }
        });
        this.getActionMap().put(ACTION_CLIPBOARD_CUT, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                codeArea.cut();
            }
        });
        this.getActionMap().put(ACTION_CLIPBOARD_PASTE, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                codeArea.paste();
            }
        });
    }

    public static PropertiesComponent getPreferences() {
        return PropertiesComponent.getInstance();
    }

    private JPanel codeAreaPanel;

    private void initComponents() {
        codeAreaPanel = new JPanel();
        codeAreaPanel.setLayout(new BorderLayout());

        this.setLayout(new BorderLayout());
        this.add(codeAreaPanel, BorderLayout.CENTER);
    }

    public boolean isModified() {
        return componentPanel.isModified();
    }

    public boolean releaseFile() {
        return componentPanel.releaseFile();
    }

//    private void switchFileHandlingMode(FileHandlingMode newHandlingMode) {
//        if (newHandlingMode != fileHandlingMode) {
//            // Switch memory mode
//            if (virtualFile != null) {
//                // If document is connected to file, attempt to release first if modified and then simply reload
//                if (isModified()) {
//                    if (releaseFile()) {
//                        fileHandlingMode = newHandlingMode;
//                        openFile(virtualFile);
//                        codeArea.clearSelection();
//                        codeArea.setCaretPosition(0);
//                    }
//                } else {
//                    fileHandlingMode = newHandlingMode;
//                    openFile(virtualFile);
//                }
//            } else {
//                // If document unsaved in memory, switch data in code area
//                if (codeArea.getContentData() instanceof DeltaDocument) {
//                    PagedData data = new PagedData();
//                    data.insert(0, codeArea.getContentData());
//                    codeArea.setContentData(data);
//                    codeArea.getContentData().dispose();
//                } else {
//                    BinaryData oldData = codeArea.getContentData();
//                    DeltaDocument document = segmentsRepository.createDocument();
//                    document.insert(0, oldData);
//                    codeArea.setContentData(document);
//                    oldData.dispose();
//                }
//                undoHandler.clear();
//                codeArea.notifyDataChanged();
//                updateCurrentMemoryMode();
//                fileHandlingMode = newHandlingMode;
//            }
//            fileHandlingMode = newHandlingMode;
//        }
//    }

    public void closeData(boolean closeFileSource) {
        if (closeFileSource) {
            componentPanel.closeData();
        } else {
            ExtCodeArea codeArea = componentPanel.getCodeArea();
            BinaryData data = codeArea.getContentData();
            codeArea.setContentData(new ByteArrayData());
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

    private void saveFileButtonActionPerformed(ActionEvent evt) {
        Application application = ApplicationManager.getApplication();
        application.runWriteAction(new Runnable() {
            @Override
            public void run() {
                ExtCodeArea codeArea = componentPanel.getCodeArea();
                BinaryData data = codeArea.getContentData();
                if (data instanceof DeltaDocument) {
                    try {
                        componentPanel.saveDocument();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                } else {
                    try (OutputStream stream = virtualFile.getOutputStream(this)) {
                        componentPanel.saveDocument(stream);
                        stream.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        updateModified();
    }

    public void openFile(BinEdVirtualFile virtualFile) {
        if (!virtualFile.isDirectory() && virtualFile.isValid()) {
            this.virtualFile = virtualFile;
            boolean editable = virtualFile.isWritable();
            File file = new File(virtualFile.getPath());
            if (file.isFile() && file.exists()) {
                try {
                    componentPanel.openDocument(file, editable);
//                    codeArea.setEditationMode(editable ? EditationMode.EXPANDING : EditationMode.READ_ONLY);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            } else {
                try (InputStream stream = virtualFile.getInputStream()) {
                    if (stream != null) {
                        ExtCodeArea codeArea = componentPanel.getCodeArea();
                        codeArea.setEditationMode(editable ? EditationMode.EXPANDING : EditationMode.READ_ONLY);
                        if (codeArea.getContentData() instanceof DeltaDocument) {
                            codeArea.getContentData().dispose();
                            codeArea.setContentData(new PagedData());
                        }
                        ((EditableBinaryData) codeArea.getContentData()).loadFromStream(stream);
                    }
                } catch (IOException ex) {
                    // Exceptions.printStackTrace(ex);
                }
            }

            opened = true;
//            documentOriginalSize = codeArea.getDataSize();
            updateModified();
//            updateCurrentMemoryMode();
//            undoHandler.clear();
        }
    }

//    public void saveFile(BinEdVirtualFile virtualFile) throws IOException {
//        Application application = ApplicationManager.getApplication();
//        ApplicationManager.getApplication().invokeLater(() -> {
//            application.runWriteAction(new Runnable() {
//                @Override
//                public void run() {
//                    BinaryData data = codeArea.getContentData();
//                    if (data instanceof DeltaDocument) {
//                        try {
//                            segmentsRepository.saveDocument((DeltaDocument) data);
//                        } catch (IOException ex) {
//                            ex.printStackTrace();
//                        }
//                    } else {
//                        try (OutputStream stream = virtualFile.getOutputStream(this)) {
//                            data.saveToStream(stream);
//                            stream.flush();
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                    notifyModified();
//                    documentOriginalSize = codeArea.getDataSize();
//                    updateModified();
//                    updateCurrentMemoryMode();
//                }
//            });
//        });
//
//        undoHandler.setSyncPoint();
//        toolbarPanel.updateUndoState();
//        toolbarPanel.updateModified(false);
//    }

//    public void reopenFile(@Nonnull BinEdVirtualFile virtualFile) {
//        this.virtualFile = virtualFile;
//        BinaryData data = codeArea.getContentData();
//        boolean editable = virtualFile.isWritable();
//        codeArea.setEditationMode(editable ? EditationMode.EXPANDING : EditationMode.READ_ONLY);
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

    private void updateModified() {
//        boolean modified = undoHandler.getCommandPosition() != undoHandler.getSyncPoint();
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

//    private void updateApplyOptions(BinEdApplyOptions applyOptions) {
//        CodeAreaOptionsImpl.applyFromCodeArea(applyOptions.getCodeAreaOptions(), codeArea);
//        applyOptions.getEncodingOptions().setSelectedEncoding(((CharsetCapable) codeArea).getCharset().name());
//
//        EditorOptions editorOptions = applyOptions.getEditorOptions();
//        editorOptions.setShowValuesPanel(valuesPanelVisible);
//        editorOptions.setFileHandlingMode(fileHandlingMode);
//        editorOptions.setEnterKeyHandlingMode(((CodeAreaOperationCommandHandler) codeArea.getCommandHandler()).getEnterKeyHandlingMode());
//
//        // TODO applyOptions.getStatusOptions().initialLoadFromPreferences(preferences.getStatusPreferences());
//    }
}
