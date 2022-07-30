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

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.ex.DocumentEx;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.intellij.util.LocalTimeCounter;
import com.intellij.util.messages.MessageBusConnection;
import org.exbin.auxiliary.paged_data.BinaryData;
import org.exbin.bined.EditMode;
import org.exbin.bined.intellij.gui.BinEdComponentFileApi;
import org.exbin.bined.intellij.gui.BinEdComponentPanel;
import org.exbin.bined.operation.swing.CodeAreaUndoHandler;
import org.exbin.bined.swing.extended.ExtCodeArea;
import org.exbin.framework.bined.FileHandlingMode;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.List;

/**
 * File editor wrapper using BinEd editor component.
 *
 * @author ExBin Project (http://exbin.org)
 * @version 0.2.3 2020/07/30
 */
public class BinEdNativeFile implements BinEdComponentFileApi {

    public static final String ACTION_CLIPBOARD_CUT = "cut-to-clipboard";
    public static final String ACTION_CLIPBOARD_COPY = "copy-to-clipboard";
    public static final String ACTION_CLIPBOARD_PASTE = "paste-from-clipboard";

    private final BinEdComponentPanel componentPanel;

    private boolean opened = false;
    private VirtualFile virtualFile;
    private BinEdFileEditorState fileEditorState = new BinEdFileEditorState();
    private MessageBusConnection updateConnection = null;

    public BinEdNativeFile(VirtualFile virtualFile) {
        this.virtualFile = virtualFile;
        componentPanel = new BinEdComponentPanel();
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
    public JComponent getComponent() {
        return componentPanel;
    }

    public void openFile(VirtualFile virtualFile) {
        boolean editable = virtualFile.isWritable();

        componentPanel.setContentData(new BinEdFileDataWrapper(virtualFile));
        ExtCodeArea codeArea = componentPanel.getCodeArea();
        codeArea.setEditMode(editable ? EditMode.EXPANDING : EditMode.READ_ONLY);

        opened = true;
//        documentOriginalSize = codeArea.getDataSize();
        updateModified();
//        updateCurrentMemoryMode();
        componentPanel.getUndoHandler().clear();

        Project project = ProjectManager.getInstance().getDefaultProject();
        updateConnection = project.getMessageBus().connect();
        updateConnection.subscribe(VirtualFileManager.VFS_CHANGES, new BulkFileListener() {
            @Override
            public void after(@NotNull List<? extends VFileEvent> events) {
                for (VFileEvent event : events) {
                    if (virtualFile.equals(event.getFile())) {
                        BinEdFileDataWrapper contentData = (BinEdFileDataWrapper) codeArea.getContentData();
                        if (!contentData.isWriteInProgress()) {
                            contentData.resetCache();
                            SwingUtilities.invokeLater(codeArea::notifyDataChanged);
                            componentPanel.getUndoHandler().clear();
                            break;
                        }
                    }
                }
            }
        });
    }

    @Override
    public void saveDocument() {
        // Ignore
    }

    @Override
    public void switchFileHandlingMode(FileHandlingMode newHandlingMode) {
        // Ignore
    }

    @Override
    public void closeData() {
        BinaryData contentData = componentPanel.getCodeArea().getContentData();
        if (contentData instanceof BinEdFileDataWrapper) {
            ((BinEdFileDataWrapper) contentData).close();
        }
        componentPanel.setContentData(null);
        Project project = ProjectManager.getInstance().getDefaultProject();
        if (updateConnection != null) {
            updateConnection.disconnect();
        }
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
        return false;
    }

    public JComponent getPreferredFocusedComponent() {
        return componentPanel.getCodeArea();
    }
}
