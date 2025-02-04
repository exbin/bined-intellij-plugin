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
import com.intellij.openapi.wm.impl.IdeBackgroundUtil;
import com.intellij.ui.Graphics2DDelegate;
import com.intellij.util.LocalTimeCounter;
import org.exbin.auxiliary.binary_data.BinaryData;
import org.exbin.auxiliary.binary_data.paged.ByteArrayPagedData;
import org.exbin.auxiliary.binary_data.paged.PagedData;
import org.exbin.bined.EditMode;
import org.exbin.bined.intellij.gui.BinEdFilePanel;
import org.exbin.bined.intellij.gui.BinEdToolbarPanel;
import org.exbin.bined.swing.section.SectCodeArea;
import org.exbin.framework.App;
import org.exbin.framework.bined.BinEdEditorComponent;
import org.exbin.framework.bined.BinEdFileHandler;
import org.exbin.framework.bined.BinedModule;
import org.exbin.framework.bined.FileHandlingMode;
import org.exbin.framework.bined.gui.BinEdComponentPanel;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.JComponent;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.IOException;

/**
 * File editor wrapper using BinEd editor component.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class BinEdNativeFile {

    private final BinEdFilePanel filePanel = new BinEdFilePanel();
    private final BinEdFileHandler fileHandler;

    private boolean opened = false;
    private VirtualFile virtualFile;

    public BinEdNativeFile() {
        fileHandler = BinEdVirtualFile.createBinEdFileHandler();
        filePanel.setFileHandler(fileHandler);
        BinedModule binedModule = App.getModule(BinedModule.class);
        binedModule.getFileManager().initFileHandler(fileHandler);
        BinaryUndoIntelliJHandler undoHandler = new BinaryUndoIntelliJHandler();
        fileHandler.setUndoHandler(undoHandler);

        SectCodeArea codeArea = filePanel.getCodeArea();

        //        componentPanel.setModifiedChangeListener(() -> {
//            updateModified();
//        });
//        defaultFont = codeArea.getCodeFont();
        // TODO editorFile.fileSync();
        // TODO filePanel.getToolbarPanel().documentOriginalSize = virtualFile.getLength();
//        binedModule.getFileManager().initCommandHandler(componentPanel.getComponentPanel());
    }

    public void registerUndoRedo(BinaryUndoIntelliJHandler undoIntelliJHandler) {
        fileHandler.registerUndoHandler();
        BinEdToolbarPanel toolbarPanel = filePanel.getToolbarPanel();
        toolbarPanel.setUndoHandler(fileHandler.getCodeAreaUndoHandler().get());
        // TODO fileHandler.setUndoHandler(undoIntelliJHandler);
    }

    public boolean isModified() {
        return fileHandler.isModified();
    }

    @Nonnull
    public JComponent getComponent() {
        // Beware: IntelliJ analysis component if it finds JTextComponent it overrides its document handling
        // Introduce component later
        return filePanel;
    }

    @Nonnull
    public BinEdFileHandler getEditorFile() {
        return fileHandler;
    }

    public void openFile(VirtualFile virtualFile) {
        if (this.virtualFile != null) {
            throw new IllegalStateException("Unable to reopen native file");
        }

        this.virtualFile = virtualFile;
        boolean editable = virtualFile.isWritable();

        ApplicationManager.getApplication().runReadAction(() -> {
            try {
                byte[] fileContent = virtualFile.contentsToByteArray();
                PagedData binaryData = new ByteArrayPagedData();
                binaryData.insert(0, fileContent);
                fileHandler.getCodeArea().setContentData(binaryData);
            } catch (IOException e) {
                throw createBrokenVirtualFileException(e);
            }
        });
        SectCodeArea codeArea = fileHandler.getCodeArea();
        codeArea.addDataChangedListener(this::saveDocument);
        codeArea.setEditMode(editable ? EditMode.EXPANDING : EditMode.READ_ONLY);

        opened = true;
        // TODO fileSync / documentOriginalSize = codeArea.getDataSize();
        updateModified();
    }

    public void saveDocument() {
        BinaryData contentData = fileHandler.getCodeArea().getContentData();
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
        boolean modified = fileHandler.isModified();
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
