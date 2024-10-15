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
package org.exbin.bined.intellij;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.ex.DocumentEx;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.LocalTimeCounter;
import org.exbin.auxiliary.binary_data.BinaryData;
import org.exbin.auxiliary.binary_data.paged.PagedData;
import org.exbin.bined.EditMode;
import org.exbin.bined.intellij.gui.BinEdFilePanel;
import org.exbin.bined.swing.section.SectCodeArea;
import org.exbin.framework.bined.BinEdFileHandler;
import org.exbin.framework.bined.FileHandlingMode;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.JComponent;
import java.awt.Font;
import java.io.IOException;

/**
 * File editor wrapper using BinEd editor component.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class BinEdNativeFile {

    private final BinEdFilePanel filePanel = new BinEdFilePanel();
    private final BinEdFileHandler editorFile = new BinEdFileHandler();

    private boolean opened = false;
    private VirtualFile virtualFile;
    private Font defaultFont;
    private long documentOriginalSize;

    public BinEdNativeFile(VirtualFile virtualFile) {
        this.virtualFile = virtualFile;
        filePanel.setFileHandler(editorFile);
//        BinedModule binedModule = App.getModule(BinedModule.class);
//        binedModule.getFileManager().initComponentPanel(componentPanel.getComponentPanel());
        // TODO binedModule.getFileManager().initFileHandler(this);

//        SectCodeArea codeArea = componentPanel.getCodeArea();
//        CodeAreaUndoHandler undoHandler = new CodeAreaUndoHandler(codeArea);
        editorFile.registerUndoHandler();
        openFile(virtualFile);

        // TODO undoHandler = new BinaryUndoIntelliJHandler(codeArea, project, this);

//        componentPanel.setModifiedChangeListener(() -> {
//            updateModified();
//        });
//        defaultFont = codeArea.getCodeFont();
        documentOriginalSize = virtualFile.getLength();
//        binedModule.getFileManager().initCommandHandler(componentPanel.getComponentPanel());
    }

    public boolean isModified() {
        return false; // TODO componentPanel.isModified();
    }

    @Nonnull
    public JComponent getComponent() {
        return filePanel;
    }

    @Nonnull
    public SectCodeArea getCodeArea() {
        return editorFile.getCodeArea();
    }

    @Nonnull
    public BinEdFileHandler getEditorFile() {
        return editorFile;
    }

    public void openFile(VirtualFile virtualFile) {
        boolean editable = virtualFile.isWritable();

        ApplicationManager.getApplication().runReadAction(() -> {
            try {
                byte[] fileContent = virtualFile.contentsToByteArray();
                PagedData binaryData = new PagedData();
                binaryData.insert(0, fileContent);
                editorFile.getCodeArea().setContentData(binaryData);
            } catch (IOException e) {
                throw createBrokenVirtualFileException(e);
            }
        });
        SectCodeArea codeArea = editorFile.getCodeArea();
        codeArea.addDataChangedListener(this::saveDocument);
        codeArea.setEditMode(editable ? EditMode.EXPANDING : EditMode.READ_ONLY);

        opened = true;
        documentOriginalSize = codeArea.getDataSize();
        updateModified();
//        Optional<BinaryDataUndoHandler> undoHandler = componentPanel.getUndoHandler();
//        if (undoHandler.isPresent()) {
//            undoHandler.get().clear();
//        }
    }

    public void saveDocument() {
        BinaryData contentData = editorFile.getCodeArea().getContentData();
        long dataSize = contentData.getDataSize();
        final byte[] fileContent = new byte[(int) dataSize];
        if (dataSize > 0) {
            contentData.copyToArray(0L, fileContent, 0, (int) dataSize);
        }
        Application application = ApplicationManager.getApplication();
        application.runWriteAction(() -> {
            try {
                virtualFile.setBinaryContent(fileContent);
            } catch (IOException e) {
                throw createBrokenVirtualFileException(e);
            }
        });
    }

    @Nonnull
    public FileHandlingMode getFileHandlingMode() {
        return FileHandlingMode.DIRECT;
    }

    public void reloadFile() {
        openFile(virtualFile);
    }

    private void updateModified() {
        boolean modified = false; // componentPanel.isModified();
//        // TODO: Trying to force "modified behavior"
        Document document = FileDocumentManager.getInstance().getDocument(virtualFile);
        if (document instanceof DocumentEx) {
            ((DocumentEx) document).setModificationStamp(LocalTimeCounter.currentTime());
        }
//        propertyChangeSupport.firePropertyChange(FileEditor.PROP_MODIFIED, !modified, modified);
    }

    @Nonnull
    private IllegalStateException createBrokenVirtualFileException(@Nullable Exception ex) {
        String filePath = virtualFile.getCanonicalPath();
        String message = "Broken virtual file" + (filePath != null ? ":" + filePath : "");
        return new IllegalStateException(message, ex);
    }

    @Nullable
    public VirtualFile getVirtualFile() {
        return virtualFile;
    }

    @Nullable
    public JComponent getPreferredFocusedComponent() {
        return filePanel;
    }
}
