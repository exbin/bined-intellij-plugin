/*
 * Copyright (C) ExBin Project, https://exbin.org
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

import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileSystem;
import com.intellij.openapi.wm.impl.IdeBackgroundUtil;
import com.intellij.ui.Graphics2DDelegate;
import org.exbin.bined.EditMode;
import org.exbin.bined.capability.EditModeCapable;
import org.exbin.bined.intellij.gui.BinEdFilePanel;
import org.exbin.bined.intellij.gui.BinEdToolbarPanel;
import org.exbin.bined.jaguif.component.BinEdDataComponent;
import org.exbin.bined.jaguif.component.gui.BinEdComponentPanel;
import org.exbin.bined.jaguif.document.BinEdFileManager;
import org.exbin.bined.jaguif.document.BinaryFileDocument;
import org.exbin.bined.jaguif.document.BinedDocumentModule;
import org.exbin.bined.jaguif.document.settings.BinaryFileProcessingOptions;
import org.exbin.bined.swing.section.SectCodeArea;
import org.exbin.jaguif.App;
import org.exbin.jaguif.context.api.ActiveContextManagement;
import org.exbin.jaguif.docking.api.ContextDocking;
import org.exbin.jaguif.document.api.ContextDocument;
import org.exbin.jaguif.document.api.StreamDocumentSource;
import org.exbin.jaguif.file.api.FileDocumentSource;
import org.exbin.jaguif.frame.api.FrameModuleApi;
import org.exbin.jaguif.options.api.OptionsModuleApi;
import org.exbin.jaguif.options.api.OptionsStorage;
import org.exbin.jaguif.options.settings.api.OptionsSettingsManagement;
import org.exbin.jaguif.options.settings.api.OptionsSettingsModuleApi;
import org.exbin.jaguif.options.settings.api.SettingsOptionsProvider;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

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
 */
@NullMarked
public class BinEdVirtualFile extends VirtualFile implements DumbAware {

    public static final String PATH_PREFIX = "bined://";

    private final VirtualFile parentFile;
    private final BinEdFilePanel filePanel = new BinEdFilePanel();
    private final BinaryFileDocument fileDocument;
    private String displayName;
    private boolean closing = false;

    public BinEdVirtualFile(VirtualFile parentFile) {
        fileDocument = BinEdVirtualFile.createBinaryFileDocument();
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

        BinedDocumentModule binedDocumentModule = App.getModule(BinedDocumentModule.class);
        BinEdFileManager fileManager = binedDocumentModule.getFileManager();
        filePanel.setDocument(fileDocument);
        fileManager.initDataComponent(fileDocument.getDataComponent());
        fileManager.initCommandHandler(fileDocument.getDataComponent());

        OptionsModuleApi optionsModule = App.getModule(OptionsModuleApi.class);
        OptionsStorage optionsStorage = optionsModule.getAppOptions();
        fileDocument.setInitialProcessingMode(new BinaryFileProcessingOptions(optionsStorage).getFileProcessingMode());

        BinEdToolbarPanel toolbarPanel = filePanel.getToolbarPanel();
        toolbarPanel.setUndoHandler(fileDocument.getUndoHandler().get());
        toolbarPanel.setSaveAction(e -> {
            fileDocument.saveTo(fileDocument.getDocumentSource().get());
            FrameModuleApi frameModule = App.getModule(FrameModuleApi.class);
            BinEdIntelliJDocking docking = (BinEdIntelliJDocking) frameModule.getFrameController().getContextManager().getActiveState(
                    ContextDocking.class);
            docking.setActiveDocument(fileDocument);
        });
        toolbarPanel.loadFromOptions(optionsStorage);

        OptionsSettingsModuleApi optionsSettingsModule = App.getModule(OptionsSettingsModuleApi.class);
        OptionsSettingsManagement settingsManager = optionsSettingsModule.getMainSettingsManager();
        SettingsOptionsProvider settingsOptionsProvider = settingsManager.getSettingsOptionsProvider();
        fileDocument.applySettings(settingsOptionsProvider);
    }

    public static BinaryFileDocument createBinaryFileDocument() {
        return new BinaryFileDocument(new BinEdDataComponent(new BinEdComponentPanelWrapper()));
    }

    private static File extractFile(BinEdVirtualFile virtualFile) {
        String path = virtualFile.getPath();
        if (path.startsWith(PATH_PREFIX)) {
            path = path.substring(8);
        }
        return new File(path);
    }

    public BinaryFileDocument getFileDocument() {
        return fileDocument;
    }

    public JComponent getEditorComponent() {
        // Beware: IntelliJ analysis component if it finds JTextComponent it overrides its document handling
        // Introduce component later
        return filePanel;
    }

    @Override
    public String getName() {
        return parentFile.getName();
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public VirtualFileSystem getFileSystem() {
        return BinEdFileSystem.getInstance();
    }

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

    @Override
    public OutputStream getOutputStream(Object requester, long newModificationStamp, long newTimeStamp) throws IOException {
        return parentFile.getOutputStream(requester, newModificationStamp, newTimeStamp);
    }

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

    public boolean isClosing() {
        return closing;
    }

    public void setClosing(boolean closing) {
        this.closing = closing;
    }

    public void dispose() {
        FrameModuleApi frameModule = App.getModule(FrameModuleApi.class);
        BinEdIntelliJDocking docking = (BinEdIntelliJDocking) frameModule.getFrameController().getContextManager().getActiveState(ContextDocking.class);
        filePanel.detach();
        docking.removeDocument(fileDocument);
    }

    public JComponent getPreferredFocusedComponent() {
        return fileDocument.getCodeArea();
    }

    public void openFile(BinaryFileDocument fileDocument) {
        if (!isDirectory() && isValid()) {
            File file = extractFile(this);
            fileDocument.clearFile();
            if (file.isFile() && file.exists()) {
                fileDocument.loadFrom(new FileDocumentSource(file));
                if (!file.canWrite()) {
                    ((EditModeCapable) fileDocument.getCodeArea()).setEditMode(EditMode.READ_ONLY);
                }
            } else {
                fileDocument.loadFrom(new VirtualFileDocumentSource());
            }
        }
        FrameModuleApi frameModule = App.getModule(FrameModuleApi.class);
        ActiveContextManagement contextManager = frameModule.getFrameController().getContextManager();
        BinEdIntelliJDocking docking = (BinEdIntelliJDocking) contextManager.getActiveState(ContextDocking.class);
        docking.setActiveDocument(fileDocument);
        fileDocument.fileSync();
        contextManager.updateActiveState(ContextDocument.class, fileDocument, BinaryFileDocument.UpdateType.ORIGINAL_SIZE);
    }

    public class VirtualFileDocumentSource implements StreamDocumentSource {
        @Override
        public String getDocumentTitle() {
            return getName();
        }

        @Override
        public InputStream openInputStream() {
            try {
                return getInputStream();
            } catch (IOException ex) {
                Logger.getLogger(BinEdVirtualFile.class.getName()).log(Level.SEVERE, null, ex);
            }

            throw new UnsupportedOperationException();
        }

        @Override
        public OutputStream openOutputStream() {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Wrapper forcing background image painting for component panel.
     */
    private static class BinEdComponentPanelWrapper extends BinEdComponentPanel {

        @Override
        protected SectCodeArea createCodeArea() {
            return new SectCodeArea() {
                private Graphics2DDelegate graphicsCache = null;

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
                    graphicsCache = editorGraphics instanceof Graphics2DDelegate ?
                            (Graphics2DDelegate) editorGraphics :
                            new Graphics2DDelegate(editorGraphics);
                    return graphicsCache;
                }
            };
        }
    }
}
