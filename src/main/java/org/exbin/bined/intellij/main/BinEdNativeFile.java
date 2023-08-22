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
package org.exbin.bined.intellij.main;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.ex.DocumentEx;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.LocalTimeCounter;
import org.exbin.auxiliary.paged_data.BinaryData;
import org.exbin.auxiliary.paged_data.PagedData;
import org.exbin.bined.EditMode;
import org.exbin.bined.operation.swing.CodeAreaUndoHandler;
import org.exbin.bined.swing.extended.ExtCodeArea;
import org.exbin.framework.bined.FileHandlingMode;
import org.exbin.framework.bined.gui.BinEdComponentFileApi;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.JComponent;
import java.io.IOException;

/**
 * File editor wrapper using BinEd editor component.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class BinEdNativeFile implements BinEdComponentFileApi {

    private final BinEdEditorComponent componentPanel;

    private boolean opened = false;
    private VirtualFile virtualFile;

    public BinEdNativeFile(VirtualFile virtualFile) {
        this.virtualFile = virtualFile;
        BinEdManager binEdManager = BinEdManager.getInstance();
        componentPanel = binEdManager.createBinEdEditor();
        binEdManager.getFileManager().initComponentPanel(componentPanel.getComponentPanel());

        ExtCodeArea codeArea = componentPanel.getCodeArea();
        CodeAreaUndoHandler undoHandler = new CodeAreaUndoHandler(codeArea);
        componentPanel.setFileApi(this);
        componentPanel.setFileHandlingMode(FileHandlingMode.NATIVE);
        componentPanel.setUndoHandler(undoHandler);
        openFile(virtualFile);

        // TODO undoHandler = new BinaryUndoIntelliJHandler(codeArea, project, this);

        componentPanel.setModifiedChangeListener(() -> {
            updateModified();
        });
    }

    public boolean isModified() {
        return componentPanel.isModified();
    }

    public boolean releaseFile() {
        return componentPanel.releaseFile();
    }

    @Nonnull
    public JComponent getComponent() {
        return componentPanel.getComponent();
    }

    public void openFile(VirtualFile virtualFile) {
        boolean editable = virtualFile.isWritable();

        ApplicationManager.getApplication().runReadAction(() -> {
            try {
                byte[] fileContent = virtualFile.contentsToByteArray();
                PagedData binaryData = new PagedData();
                binaryData.insert(0, fileContent);
                componentPanel.setContentData(binaryData);
            } catch (IOException e) {
                throw createBrokenVirtualFileException(e);
            }
        });
        ExtCodeArea codeArea = componentPanel.getCodeArea();
        codeArea.addDataChangedListener(this::saveDocument);
        codeArea.setEditMode(editable ? EditMode.EXPANDING : EditMode.READ_ONLY);

        opened = true;
//        documentOriginalSize = codeArea.getDataSize();
        updateModified();
//        updateCurrentMemoryMode();
        componentPanel.getUndoHandler().clear();
    }

    @Override
    public void saveDocument() {
        BinaryData contentData = componentPanel.getContentData();
        final byte[] fileContent = contentData == null ? new byte[0] : new byte[(int) contentData.getDataSize()];
        if (contentData != null) {
            contentData.copyToArray(0L, fileContent, 0, (int) contentData.getDataSize());
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

    @Override
    public void switchFileHandlingMode(FileHandlingMode newHandlingMode) {
        throw new IllegalStateException("File handling change not supported");
    }

    public void reloadFile() {
        openFile(virtualFile);

    }

    @Override
    public void closeData() {
        PagedData contentData = (PagedData) componentPanel.getCodeArea().getContentData();
        if (contentData != null) {
            contentData.clear();
        }
        componentPanel.setContentData(null);
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

    @Override
    public boolean isSaveSupported() {
        // Automatic save
        return false;
    }

    @Nullable
    public JComponent getPreferredFocusedComponent() {
        return componentPanel.getCodeArea();
    }
}
