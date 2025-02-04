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

import com.intellij.openapi.fileEditor.impl.FileEditorManagerImpl;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileSystem;
import com.intellij.openapi.wm.impl.IdeBackgroundUtil;
import com.intellij.ui.Graphics2DDelegate;
import org.exbin.bined.intellij.gui.BinEdFilePanel;
import org.exbin.bined.intellij.gui.BinEdToolbarPanel;
import org.exbin.bined.swing.section.SectCodeArea;
import org.exbin.framework.App;
import org.exbin.framework.bined.BinEdEditorComponent;
import org.exbin.framework.bined.BinEdFileHandler;
import org.exbin.framework.bined.BinedModule;
import org.exbin.framework.bined.gui.BinEdComponentPanel;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.ActionMap;
import javax.swing.JComponent;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Virtual file for binary editor.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class BinEdVirtualFile extends VirtualFile implements DumbAware {

    public static final String PATH_PREFIX = "bined://";

    private final VirtualFile parentFile;
    private String displayName;
    private final BinEdFilePanel filePanel = new BinEdFilePanel();
    private final BinEdFileHandler fileHandler;
    private boolean closing = false;

    public BinEdVirtualFile(VirtualFile parentFile) {
        fileHandler = BinEdVirtualFile.createBinEdFileHandler();
        if (parentFile.getPath().startsWith(PATH_PREFIX)) {
            this.parentFile = LocalFileSystem.getInstance().findFileByPath(parentFile.getPath().substring(PATH_PREFIX.length()));
        } else {
            this.parentFile = parentFile;
        }
        String path = parentFile.getPath();
        int lastIndexOf = path.lastIndexOf('/');
        if (lastIndexOf >= 0) {
            this.displayName = path.substring(lastIndexOf + 1);
        } else {
            this.displayName = "";
        }

        BinedModule binedModule = App.getModule(BinedModule.class);
        binedModule.getFileManager().initComponentPanel(fileHandler.getComponent());
        binedModule.getFileManager().initFileHandler(fileHandler);
        filePanel.setFileHandler(fileHandler);
        fileHandler.registerUndoHandler();

        BinEdToolbarPanel toolbarPanel = filePanel.getToolbarPanel();
        toolbarPanel.setUndoHandler(fileHandler.getCodeAreaUndoHandler().get());
        toolbarPanel.setSaveAction(e -> fileHandler.saveFile());
    }

    @Nonnull
    public BinEdFileHandler getEditorFile() {
        return fileHandler;
    }

    @Nonnull
    public JComponent getEditorComponent() {
        // Beware: IntelliJ analysis component if it finds JTextComponent it overrides its document handling
        // Introduce component later
        return filePanel;
    }

    @Nonnull
    @Override
    public String getName() {
        return parentFile.getName();
    }

    @Nonnull
    public String getDisplayName() {
        return displayName;
    }

    @Nonnull
    @Override
    public VirtualFileSystem getFileSystem() {
        return BinEdFileSystem.getInstance();
    }

    @Nonnull
    @Override
    public String getPath() {
        return PATH_PREFIX + parentFile.getPath();
    }

    @Override
    public boolean isWritable() {
        return parentFile.isWritable();
    }

    @Override
    public boolean isDirectory() {
        return parentFile.isDirectory();
    }

    @Override
    public boolean isValid() {
        return parentFile.isValid();
    }

    @Nullable
    @Override
    public VirtualFile getParent() {
        return parentFile.getParent();
    }

    @Nullable
    @Override
    public VirtualFile[] getChildren() {
        return parentFile.getChildren();
    }

    @Nonnull
    @Override
    public OutputStream getOutputStream(Object requester, long newModificationStamp, long newTimeStamp) throws IOException {
        return parentFile.getOutputStream(requester, newModificationStamp, newTimeStamp);
    }

    @Nonnull
    @Override
    public byte[] contentsToByteArray() throws IOException {
        return parentFile.contentsToByteArray();
    }

    @Override
    public long getTimeStamp() {
        return parentFile.getTimeStamp();
    }

    @Override
    public long getLength() {
        return parentFile.getLength();
    }

    @Override
    public void refresh(boolean asynchronous, boolean recursive, @Nullable Runnable postRunnable) {
        parentFile.refresh(asynchronous, recursive, postRunnable);
    }

    @Nonnull
    @Override
    public InputStream getInputStream() throws IOException {
        return parentFile.getInputStream();
    }

    @Override
    public long getModificationStamp() {
        return parentFile.getModificationStamp();
    }

    @Override
    public long getModificationCount() {
        return parentFile.getModificationCount();
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BinEdVirtualFile that = (BinEdVirtualFile) o;
        String path = getPath();
        return path.equals(that.getPath());
    }

    @Override
    public int hashCode() {
        return getPath().hashCode();
    }

    public boolean isMoved() {
        Boolean closingToReopen = getUserData(FileEditorManagerImpl.CLOSING_TO_REOPEN);
        return closingToReopen != null && closingToReopen;
    }

    public boolean isClosing() {
        return closing;
    }

    public void setClosing(boolean closing) {
        this.closing = closing;
    }

    public void dispose() {
        BinedModule binedModule = App.getModule(BinedModule.class);
        ((BinEdIntelliJEditorProvider) binedModule.getEditorProvider()).removeFile(fileHandler);
        fileHandler.closeData();
    }

    @Nonnull
    public JComponent getPreferredFocusedComponent() {
        return getEditorFile().getCodeArea();
    }

    public void openFile(BinEdFileHandler fileHandler) {
        if (!isDirectory() && isValid()) {
            File file = extractFile(this);
            if (file.isFile() && file.exists()) {
                fileHandler.clearFile();
                fileHandler.loadFromFile(file.toURI(), null);
            } else {
                try (InputStream stream = getInputStream()) {
                    fileHandler.loadFromStream(stream);
                } catch (IOException ex) {
                    Logger.getLogger(BinEdFileHandler.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    @Nonnull
    public static BinEdFileHandler createBinEdFileHandler() {
        return new BinEdFileHandler() {
            @Nonnull
            @Override
            protected BinEdEditorComponent createEditorComponent() {
                return new BinEdEditorComponent() {
                    @Nonnull
                    @Override
                    protected BinEdComponentPanel createComponentPanel() {
                        return new BinEdComponentPanel() {
                            @Nonnull
                            @Override
                            protected SectCodeArea createCodeArea() {
                                return new SectCodeArea() {

                                    private Graphics2DDelegate graphicsCache = null;

                                    @Nonnull
                                    @Override
                                    protected Graphics getComponentGraphics(Graphics g) {
                                        if (g instanceof Graphics2DDelegate) {
                                            return g;
                                        }

                                        if (graphicsCache != null && graphicsCache.getDelegate() == g) {
                                            return graphicsCache;
                                        }

                                        if (graphicsCache != null) {
                                            graphicsCache.dispose();
                                        }

                                        Graphics2D editorGraphics = IdeBackgroundUtil.withEditorBackground(g, this);
                                        graphicsCache = editorGraphics instanceof Graphics2DDelegate ? (Graphics2DDelegate) editorGraphics : new Graphics2DDelegate(editorGraphics);
                                        return graphicsCache;
                                    }
                                };
                            }
                        };
                    }
                };
            }
        };
    }

    @Nonnull
    private static File extractFile(BinEdVirtualFile virtualFile) {
        String path = virtualFile.getPath();
        if (path.startsWith(PATH_PREFIX)) {
            path = path.substring(8);
        }
        return new File(path);
    }
}
