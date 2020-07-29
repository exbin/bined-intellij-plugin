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
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.LocalTimeCounter;
import org.exbin.auxiliary.paged_data.BinaryData;
import org.exbin.bined.EditationMode;
import org.exbin.bined.intellij.gui.BinEdComponentFileApi;
import org.exbin.bined.intellij.gui.BinEdComponentPanel;
import org.exbin.bined.operation.BinaryDataCommand;
import org.exbin.bined.operation.BinaryDataOperationException;
import org.exbin.bined.operation.swing.CodeAreaUndoHandler;
import org.exbin.bined.swing.extended.ExtCodeArea;
import org.exbin.framework.bined.FileHandlingMode;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * File editor wrapper using BinEd editor component.
 *
 * @author ExBin Project (http://exbin.org)
 * @version 0.2.3 2020/07/29
 */
public class BinEdNativeFile implements BinEdComponentFileApi {

    public static final String ACTION_CLIPBOARD_CUT = "cut-to-clipboard";
    public static final String ACTION_CLIPBOARD_COPY = "copy-to-clipboard";
    public static final String ACTION_CLIPBOARD_PASTE = "paste-from-clipboard";

    private final BinEdComponentPanel componentPanel;

    private boolean opened = false;
    private VirtualFile virtualFile;
    private BinEdFileEditorState fileEditorState = new BinEdFileEditorState();

    public BinEdNativeFile(VirtualFile virtualFile) {
        this.virtualFile = virtualFile;
        componentPanel = new BinEdComponentPanel();
        ExtCodeArea codeArea = componentPanel.getCodeArea();
        CodeAreaUndoHandler undoHandler = new CodeAreaUndoHandlerWrapper(codeArea);
        componentPanel.setFileApi(this);
        componentPanel.setFileHandlingMode(FileHandlingMode.NATIVE);
        componentPanel.setUndoHandler(undoHandler);
        openFile(virtualFile);

        // TODO undoHandler = new BinaryUndoIntelliJHandler(codeArea, project, this);

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

    public void openFile(VirtualFile virtualFile) {
        boolean editable = virtualFile.isWritable();

        ExtCodeArea codeArea = componentPanel.getCodeArea();
        codeArea.setContentData(new BinEdFileDataWrapper(virtualFile));
        codeArea.setEditationMode(editable ? EditationMode.EXPANDING : EditationMode.READ_ONLY);

        opened = true;
//        documentOriginalSize = codeArea.getDataSize();
        updateModified();
//        updateCurrentMemoryMode();
        componentPanel.getUndoHandler().clear();
    }

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
    }

    @Override
    public void switchFileHandlingMode(FileHandlingMode newHandlingMode) {
        throw new IllegalArgumentException("Switching file handling is not allowed");
    }

    @Override
    public void closeData() {
        ExtCodeArea codeArea = componentPanel.getCodeArea();
        BinaryData data = codeArea.getContentData();
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

    @Nullable
    public VirtualFile getVirtualFile() {
        return virtualFile;
    }

    @Override
    public boolean isSaveSupported() {
        return true;
    }

    public JComponent getPreferredFocusedComponent() {
        return componentPanel.getCodeArea();
    }

    private static class CodeAreaUndoHandlerWrapper extends CodeAreaUndoHandler {
        public CodeAreaUndoHandlerWrapper(ExtCodeArea codeArea) {
            super(codeArea);
        }

        @Override
        public void execute(BinaryDataCommand command) throws BinaryDataOperationException {
            Application application = ApplicationManager.getApplication();
            application.runWriteAction(() -> {
                try {
                    super.execute(command);
                } catch (BinaryDataOperationException e) {
                    e.printStackTrace();
                }
            });
        }
    }
}
