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

import com.intellij.codeHighlighting.BackgroundEditorHighlighter;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.messages.MessageBusConnection;
import org.exbin.bined.CodeAreaCaretPosition;
import org.exbin.bined.EditationMode;
import org.exbin.bined.EditationOperation;
import org.exbin.bined.delta.DeltaDocument;
import org.exbin.bined.delta.FileDataSource;
import org.exbin.bined.delta.SegmentsRepository;
import org.exbin.bined.extended.theme.ExtendedBackgroundPaintMode;
import org.exbin.bined.highlight.swing.extended.ExtendedHighlightNonAsciiCodeAreaPainter;
import org.exbin.bined.intellij.panel.BinEdOptionsPanelBorder;
import org.exbin.bined.intellij.panel.BinEdToolbarPanel;
import org.exbin.bined.intellij.panel.BinarySearchPanel;
import org.exbin.bined.intellij.panel.ValuesPanel;
import org.exbin.bined.operation.BinaryDataCommand;
import org.exbin.bined.operation.BinaryDataOperationException;
import org.exbin.bined.operation.swing.CodeAreaOperationCommandHandler;
import org.exbin.bined.operation.swing.CodeAreaUndoHandler;
import org.exbin.bined.operation.swing.command.InsertDataCommand;
import org.exbin.bined.operation.undo.BinaryDataUndoUpdateListener;
import org.exbin.bined.swing.extended.ExtCodeArea;
import org.exbin.framework.bined.BinaryStatusApi;
import org.exbin.framework.bined.options.CodeAreaOptions;
import org.exbin.framework.bined.options.EditorOptions;
import org.exbin.framework.bined.options.StatusOptions;
import org.exbin.framework.bined.panel.BinaryStatusPanel;
import org.exbin.framework.bined.preferences.BinaryEditorPreferences;
import org.exbin.framework.editor.text.TextEncodingStatusApi;
import org.exbin.framework.gui.about.panel.AboutPanel;
import org.exbin.framework.gui.utils.WindowUtils;
import org.exbin.framework.gui.utils.handler.OptionsControlHandler;
import org.exbin.framework.gui.utils.panel.CloseControlPanel;
import org.exbin.framework.gui.utils.panel.OptionsControlPanel;
import org.exbin.utils.binary_data.BinaryData;
import org.exbin.utils.binary_data.ByteArrayData;
import org.exbin.utils.binary_data.EditableBinaryData;
import org.exbin.utils.binary_data.PagedData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.*;
import java.nio.charset.Charset;
import java.util.List;

/**
 * File editor using BinEd editor component.
 *
 * @author ExBin Project (http://exbin.org)
 * @version 0.2.0 2019/03/20
 */
public class BinEdFileEditor implements FileEditor {

    public static final String ACTION_CLIPBOARD_CUT = "cut-to-clipboard";
    public static final String ACTION_CLIPBOARD_COPY = "copy-to-clipboard";
    public static final String ACTION_CLIPBOARD_PASTE = "paste-from-clipboard";
    private static final FileHandlingMode DEFAULT_FILE_HANDLING_MODE = FileHandlingMode.DELTA;

    private final BinaryEditorPreferences preferences;
    private final Project project;
    private JPanel editorPanel;
//    private JPanel headerPanel;
    private static SegmentsRepository segmentsRepository = null;
    private final ExtCodeArea codeArea;
    private final CodeAreaUndoHandler undoHandler;
    private final int metaMask;
    private final PropertyChangeSupport propertyChangeSupport;

    private BinEdToolbarPanel toolbarPanel;
    private BinaryStatusPanel statusPanel;
    private BinaryStatusApi binaryStatus;
    private TextEncodingStatusApi encodingStatus;
    private CharsetChangeListener charsetChangeListener = null;
    private GoToHandler goToHandler;
    private EncodingsHandler encodingsHandler;
    private JScrollPane valuesPanelScrollPane = null;
    private ValuesPanel valuesPanel = null;
    private boolean valuesPanelVisible = false;
    private final SearchAction searchAction;

    private boolean opened = false;
    private FileHandlingMode fileHandlingMode = DEFAULT_FILE_HANDLING_MODE;
    private String displayName;
    private long documentOriginalSize;
    private BinEdVirtualFile virtualFile;
    private BinEdFileEditorState fileEditorState = new BinEdFileEditorState();

    public BinEdFileEditor(Project project) {
        this.project = project;
        editorPanel = new JPanel();
        initComponents();

        preferences = new BinaryEditorPreferences(new org.exbin.bined.intellij.PreferencesWrapper(getPreferences()));

        codeArea = new ExtCodeArea();
        codeArea.setPainter(new ExtendedHighlightNonAsciiCodeAreaPainter(codeArea));
        codeArea.setCodeFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        codeArea.getCaret().setBlinkRate(300);

        toolbarPanel = new BinEdToolbarPanel(preferences, codeArea);
        statusPanel = new BinaryStatusPanel();
        editorPanel.add(toolbarPanel, BorderLayout.NORTH);
        registerEncodingStatus(statusPanel);
        encodingsHandler = new EncodingsHandler(new TextEncodingStatusApi() {
            @Override
            public String getEncoding() {
                return encodingStatus.getEncoding();
            }

            @Override
            public void setEncoding(String encodingName) {
                codeArea.setCharset(Charset.forName(encodingName));
                encodingStatus.setEncoding(encodingName);
                preferences.getCodeAreaParameters().setSelectedEncoding(encodingName);
            }
        });

        propertyChangeSupport = new PropertyChangeSupport(this);
        // CodeAreaUndoHandler(codeArea);
        // undoHandler = new BinaryUndoIntelliJHandler(codeArea, project, this);
        undoHandler = new CodeAreaUndoHandler(codeArea);
        loadFromPreferences();

        undoHandler.addUndoUpdateListener(new BinaryDataUndoUpdateListener() {
            @Override
            public void undoCommandPositionChanged() {
                codeArea.repaint();
                updateUndoState();
                notifyModified();
            }

            @Override
            public void undoCommandAdded(final BinaryDataCommand command) {
                updateUndoState();
                notifyModified();
            }
        });
        updateUndoState();

        getSegmentsRepository();
        setNewData();
        CodeAreaOperationCommandHandler commandHandler = new CodeAreaOperationCommandHandler(codeArea, undoHandler);
        codeArea.setCommandHandler(commandHandler);
        editorPanel.add(codeArea, BorderLayout.CENTER);
        editorPanel.add(statusPanel, BorderLayout.SOUTH);
        registerBinaryStatus(statusPanel);
        goToHandler = new GoToHandler(codeArea);

        int metaMaskValue;
        try {
            metaMaskValue = java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        } catch (java.awt.HeadlessException ex) {
            metaMaskValue = java.awt.Event.CTRL_MASK;
        }
        metaMask = metaMaskValue;

        searchAction = new SearchAction(codeArea, codeAreaPanel, metaMask);
        codeArea.addDataChangedListener(() -> {
            searchAction.codeAreaDataChanged();
            updateCurrentDocumentSize();
        });

        codeArea.setComponentPopupMenu(new JPopupMenu() {
            @Override
            public void show(Component invoker, int x, int y) {
                JPopupMenu popupMenu = createContextMenu();
                popupMenu.show(invoker, x, y);
            }
        });

// TODO        codeTypeComboBox.setSelectedIndex(codeArea.getCodeType().ordinal());

        toolbarPanel.applyFromCodeArea();

        editorPanel.getActionMap().put(ACTION_CLIPBOARD_COPY, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                codeArea.copy();
            }
        });
        editorPanel.getActionMap().put(ACTION_CLIPBOARD_CUT, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                codeArea.cut();
            }
        });
        editorPanel.getActionMap().put(ACTION_CLIPBOARD_PASTE, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                codeArea.paste();
            }
        });

        codeArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent keyEvent) {
                int modifiers = keyEvent.getModifiers();
                if (modifiers == metaMask) {
                    int keyCode = keyEvent.getKeyCode();
                    switch (keyCode) {
                        case KeyEvent.VK_F: {
                            searchAction.actionPerformed(null);
                            searchAction.switchReplaceMode(BinarySearchPanel.SearchOperation.FIND);
                            break;
                        }
                        case KeyEvent.VK_G: {
                            goToHandler.getGoToRowAction().actionPerformed(null);
                            break;
                        }
                        case KeyEvent.VK_S: {
                            saveFileButtonActionPerformed(null);
                            break;
                        }
                    }
                }

                if (modifiers == InputEvent.CTRL_MASK && keyEvent.getKeyCode() == KeyEvent.VK_Z) {
                    try {
                        if (undoHandler.canUndo()) {
                            undoHandler.performUndo();
                        }
                    } catch (BinaryDataOperationException e) {
                        e.printStackTrace();
                    }
                } else if (modifiers == (InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK) && keyEvent.getKeyCode() == KeyEvent.VK_Z) {
                    try {
                        if (undoHandler.canRedo()) {
                            undoHandler.performRedo();
                        }
                    } catch (BinaryDataOperationException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        MessageBus messageBus = project.getMessageBus();
        MessageBusConnection connect = messageBus.connect();
        connect.subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, new FileEditorManagerListener() {
            @Override
            public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
            }

            @Override
            public void fileClosed(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
                if (virtualFile != null) {
                    if (!releaseFile()) {
                        // TODO Intercept close event instead of editor recreation
                        OpenFileDescriptor descriptor = new OpenFileDescriptor(project, virtualFile, 0);
                        FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
                        List<FileEditor> editors = fileEditorManager.openEditor(descriptor, true);
                        fileEditorManager.setSelectedEditor(virtualFile, BinEdWindowProvider.BINED_EDITOR_TYPE_ID);
                        for (FileEditor fileEditor : editors) {
                            if (fileEditor instanceof BinEdFileEditor) {
                                ((BinEdFileEditor) fileEditor).reopenFile(virtualFile, codeArea.getContentData(), undoHandler);
                            }
                        }
                        closeData(false);
                    } else {
                        closeData(true);
                    }
                }

                virtualFile = null;
            }

            @Override
            public void selectionChanged(@NotNull FileEditorManagerEvent event) {
            }
        });
        editorPanel.invalidate();
    }

    public static PropertiesComponent getPreferences() {
        return PropertiesComponent.getInstance();
    }

    private javax.swing.JPanel codeAreaPanel;

//    private ComboBox<String> codeTypeComboBox;
//    private javax.swing.JToolBar controlToolBar;
//    private javax.swing.JPanel infoToolbar;
//    private javax.swing.JToolBar.Separator separator1;
//    private javax.swing.JToolBar.Separator separator2;
//    private javax.swing.JToolBar.Separator separator3;
    private javax.swing.JButton saveFileButton;
    private javax.swing.JButton undoEditButton;
    private javax.swing.JButton redoEditButton;
//    private javax.swing.JToggleButton lineWrappingToggleButton;
//    private javax.swing.JToggleButton showUnprintablesToggleButton;

    private void initComponents() {
        codeAreaPanel = new javax.swing.JPanel();
        codeAreaPanel.setLayout(new java.awt.BorderLayout());

        editorPanel.setLayout(new java.awt.BorderLayout());
        editorPanel.add(codeAreaPanel, java.awt.BorderLayout.CENTER);
//        infoToolbar = new javax.swing.JPanel();
//        controlToolBar = new javax.swing.JToolBar();
        saveFileButton = new javax.swing.JButton();
        undoEditButton = new javax.swing.JButton();
        redoEditButton = new javax.swing.JButton();
//        lineWrappingToggleButton = new javax.swing.JToggleButton();
//        showUnprintablesToggleButton = new javax.swing.JToggleButton();
//        separator1 = new javax.swing.JToolBar.Separator();
//        separator2 = new javax.swing.JToolBar.Separator();
//        separator3 = new javax.swing.JToolBar.Separator();
//        codeTypeComboBox = new ComboBox<>();
//
//        editorPanel.setLayout(new java.awt.BorderLayout());
//
//        controlToolBar.setBorder(null);
//        controlToolBar.setFloatable(false);
//        controlToolBar.setRollover(true);
//
//        saveFileButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/exbin/bined/intellij/resources/icons/document-save.png")));
//        saveFileButton.setToolTipText("Save current file");
//        saveFileButton.addActionListener(this::saveFileButtonActionPerformed);
//        saveFileButton.setEnabled(false);
//        controlToolBar.add(saveFileButton);
//        controlToolBar.add(separator1);
//
//        undoEditButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/exbin/bined/intellij/resources/icons/edit-undo.png")));
//        undoEditButton.setToolTipText("Undo last operation");
//        undoEditButton.addActionListener(this::undoEditButtonActionPerformed);
//        controlToolBar.add(undoEditButton);
//
//        redoEditButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/exbin/bined/intellij/resources/icons/edit-redo.png")));
//        redoEditButton.setToolTipText("Redo last undid operation");
//        redoEditButton.addActionListener(this::redoEditButtonActionPerformed);
//        controlToolBar.add(redoEditButton);
//        controlToolBar.add(separator2);
//
//        lineWrappingToggleButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/exbin/bined/intellij/resources/icons/bined-linewrap.png")));
//        lineWrappingToggleButton.setToolTipText("Wrap line to window size");
//        lineWrappingToggleButton.addActionListener(this::lineWrappingToggleButtonActionPerformed);
//        controlToolBar.add(lineWrappingToggleButton);
//
//        showUnprintablesToggleButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/exbin/bined/intellij/resources/icons/insert-pilcrow.png")));
//        showUnprintablesToggleButton.setToolTipText("Show symbols for unprintable/whitespace characters");
//        showUnprintablesToggleButton.addActionListener(this::showUnprintablesToggleButtonActionPerformed);
//        controlToolBar.add(showUnprintablesToggleButton);
//        controlToolBar.add(separator3);
//
//        JPanel spacePanel = new JPanel();
//        spacePanel.setLayout(new BorderLayout());
//        codeTypeComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{"BIN", "OCT", "DEC", "HEX"}));
//        codeTypeComboBox.addActionListener(this::codeTypeComboBoxActionPerformed);
//        spacePanel.add(codeTypeComboBox, BorderLayout.WEST);
//        controlToolBar.add(spacePanel);
//
//        javax.swing.GroupLayout infoToolbarLayout = new javax.swing.GroupLayout(infoToolbar);
//        infoToolbar.setLayout(infoToolbarLayout);
//        infoToolbarLayout.setHorizontalGroup(
//                infoToolbarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
//                        .addGroup(infoToolbarLayout.createSequentialGroup()
//                                .addComponent(controlToolBar, javax.swing.GroupLayout.DEFAULT_SIZE, 366, Short.MAX_VALUE))
//        );
//        infoToolbarLayout.setVerticalGroup(
//                infoToolbarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
//                        .addComponent(controlToolBar, javax.swing.GroupLayout.PREFERRED_SIZE, 25, Short.MAX_VALUE)
//        );
//
//        headerPanel = new JPanel();
//        headerPanel.setLayout(new java.awt.BorderLayout());
//        headerPanel.add(infoToolbar, java.awt.BorderLayout.CENTER);
//        editorPanel.add(headerPanel, java.awt.BorderLayout.NORTH);
    }

    @NotNull
    @Override
    public JComponent getComponent() {
        return editorPanel;
    }

    @Nullable
    @Override
    public JComponent getPreferredFocusedComponent() {
        return editorPanel;
    }

    @NotNull
    @Override
    public String getName() {
        return displayName;
    }

    @NotNull
    @Override
    public FileEditorState getState(@NotNull FileEditorStateLevel level) {
        return fileEditorState;
    }

    @Override
    public void setState(@NotNull FileEditorState state) {
    }

    @Override
    public boolean isModified() {
        return undoHandler.getCommandPosition() != undoHandler.getSyncPoint();
    }

    private void setNewData() {
        if (fileHandlingMode == FileHandlingMode.DELTA) {
            codeArea.setContentData(segmentsRepository.createDocument());
        } else {
            codeArea.setContentData(new PagedData());
        }
    }

    /**
     * Attempts to release current file and warn if document was modified.
     *
     * @return true if successful
     */
    public boolean releaseFile() {

        if (virtualFile == null) {
            return true;
        }

        while (isModified()) {
            Object[] options = {
                    "Save",
                    "Discard",
                    "Cancel"
            };
            int result = JOptionPane.showOptionDialog(editorPanel,
                    "Document was modified! Do you wish to save it?",
                    "Save File?",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null, options, options[0]);
            if (result == JOptionPane.NO_OPTION) {
                return true;
            }
            if (result == JOptionPane.CANCEL_OPTION || result == JOptionPane.CLOSED_OPTION) {
                return false;
            }

            try {
                saveFile(virtualFile);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        return true;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void selectNotify() {

    }

    @Override
    public void deselectNotify() {

    }

    public void registerBinaryStatus(BinaryStatusApi binaryStatusApi) {
        this.binaryStatus = binaryStatusApi;
        codeArea.addCaretMovedListener((CodeAreaCaretPosition caretPosition) -> {
            binaryStatus.setCursorPosition(caretPosition);
        });

        codeArea.addEditationModeChangedListener((mode, operation) -> binaryStatus.setEditationMode(mode, operation));
        binaryStatus.setEditationMode(codeArea.getEditationMode(), codeArea.getEditationOperation());

        binaryStatus.setControlHandler(new BinaryStatusApi.StatusControlHandler() {
            @Override
            public void changeEditationOperation(EditationOperation editationOperation) {
                codeArea.setEditationOperation(editationOperation);
            }

            @Override
            public void changeCursorPosition() {
                goToHandler.getGoToRowAction().actionPerformed(null);
            }

            @Override
            public void cycleEncodings() {
                if (encodingsHandler != null) {
                    encodingsHandler.cycleEncodings();
                }
            }

            @Override
            public void encodingsPopupEncodingsMenu(MouseEvent mouseEvent) {
                if (encodingsHandler != null) {
                    encodingsHandler.popupEncodingsMenu(mouseEvent);
                }
            }

            @Override
            public void changeMemoryMode(BinaryStatusApi.MemoryMode memoryMode) {
                FileHandlingMode newHandlingMode = memoryMode == BinaryStatusApi.MemoryMode.DELTA_MODE ? FileHandlingMode.DELTA : FileHandlingMode.MEMORY;
                if (newHandlingMode != fileHandlingMode) {
                    switchDeltaMemoryMode(newHandlingMode);
                    preferences.getEditorParameters().setFileHandlingMode(newHandlingMode.name());
                }
            }
        });
    }

    private void switchShowValuesPanel(boolean showValuesPanel) {
        if (showValuesPanel) {
            showValuesPanel();
        } else {
            hideValuesPanel();
        }
    }

    private void switchDeltaMemoryMode(FileHandlingMode newHandlingMode) {
        if (newHandlingMode != fileHandlingMode) {
            // Switch memory mode
            if (virtualFile != null) {
                // If document is connected to file, attempt to release first if modified and then simply reload
                if (isModified()) {
                    if (releaseFile()) {
                        fileHandlingMode = newHandlingMode;
                        openFile(virtualFile);
                        codeArea.clearSelection();
                        codeArea.setCaretPosition(0);
                    }
                } else {
                    fileHandlingMode = newHandlingMode;
                    openFile(virtualFile);
                }
            } else {
                // If document unsaved in memory, switch data in code area
                if (codeArea.getContentData() instanceof DeltaDocument) {
                    PagedData data = new PagedData();
                    data.insert(0, codeArea.getContentData());
                    codeArea.setContentData(data);
                    codeArea.getContentData().dispose();
                } else {
                    BinaryData oldData = codeArea.getContentData();
                    DeltaDocument document = segmentsRepository.createDocument();
                    document.insert(0, oldData);
                    codeArea.setContentData(document);
                    oldData.dispose();
                }
                undoHandler.clear();
                codeArea.notifyDataChanged();
                updateCurrentMemoryMode();
                fileHandlingMode = newHandlingMode;
            }
            fileHandlingMode = newHandlingMode;
        }
    }

    private void closeData(boolean closeFileSource) {
        BinaryData data = codeArea.getContentData();
        codeArea.setContentData(new ByteArrayData());
        if (data instanceof DeltaDocument) {
            FileDataSource fileSource = ((DeltaDocument) data).getFileSource();
            data.dispose();
            if (closeFileSource) {
                segmentsRepository.detachFileSource(fileSource);
                segmentsRepository.closeFileSource(fileSource);
            }
        } else {
            data.dispose();
        }

        virtualFile = null;
    }

    public void registerEncodingStatus(TextEncodingStatusApi encodingStatusApi) {
        this.encodingStatus = encodingStatusApi;
        setCharsetChangeListener(() -> {
            String selectedEncoding = codeArea.getCharset().name();
            encodingStatus.setEncoding(selectedEncoding);
        });
    }

    public void setCharsetChangeListener(CharsetChangeListener charsetChangeListener) {
        this.charsetChangeListener = charsetChangeListener;
    }

    @Override
    public void addPropertyChangeListener(@NotNull PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(@NotNull PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    private void notifyModified() {
        boolean modified = undoHandler.getCommandPosition() != undoHandler.getSyncPoint();
        // TODO: Trying to force "modified behavior"
//        Document document = FileDocumentManager.getInstance().getDocument(virtualFile);
//        if (document instanceof DocumentEx) {
//            ((DocumentEx) document).setModificationStamp(LocalTimeCounter.currentTime());
//        }
//        propertyChangeSupport.firePropertyChange(FileEditor.PROP_MODIFIED, !modified, modified);

        saveFileButton.setEnabled(modified);
    }

    private void updateUndoState() {
        undoEditButton.setEnabled(undoHandler.canUndo());
        redoEditButton.setEnabled(undoHandler.canRedo());
    }

    @Nullable
    @Override
    public BackgroundEditorHighlighter getBackgroundHighlighter() {
        return null;
    }

    @Nullable
    @Override
    public FileEditorLocation getCurrentLocation() {
        return null;
//        return new TextEditorLocation(codeArea.getCaretPosition(), this);
    }

    @Override
    public void dispose() {
    }

    @Nullable
    @Override
    public <T> T getUserData(@NotNull Key<T> key) {
        return null;
    }

    @Override
    public <T> void putUserData(@NotNull Key<T> key, @Nullable T value) {
    }

    private void saveFileButtonActionPerformed(java.awt.event.ActionEvent evt) {
        Application application = ApplicationManager.getApplication();
        application.runWriteAction(new Runnable() {
            @Override
            public void run() {
                BinaryData data = codeArea.getContentData();
                if (data instanceof DeltaDocument) {
                    try {
                        segmentsRepository.saveDocument((DeltaDocument) data);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                } else {
                    try (OutputStream stream = virtualFile.getOutputStream(this)) {
                        codeArea.getContentData().saveToStream(stream);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                undoHandler.setSyncPoint();
                updateUndoState();
                saveFileButton.setEnabled(false);
            }
        });
    }

    private void undoEditButtonActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            undoHandler.performUndo();
            codeArea.repaint();
            updateUndoState();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void redoEditButtonActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            undoHandler.performRedo();
            codeArea.repaint();
            updateUndoState();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    private void lineWrappingToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {
//        codeArea.setRowWrapping(lineWrappingToggleButton.isSelected() ? RowWrappingCapable.RowWrappingMode.WRAPPING : RowWrappingCapable.RowWrappingMode.NO_WRAPPING);
//// TODO        preferences.setValue(BinEdFileEditor.PREFERENCES_LINE_WRAPPING, lineWrappingToggleButton.isSelected());
//    }
//
//    private void showUnprintablesToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {
//        codeArea.setShowUnprintables(showUnprintablesToggleButton.isSelected());
//// TODO        preferences.setValue(BinEdFileEditor.PREFERENCES_SHOW_UNPRINTABLES, lineWrappingToggleButton.isSelected());
//    }
//
//    private void codeTypeComboBoxActionPerformed(java.awt.event.ActionEvent evt) {
//        CodeType codeType = CodeType.values()[codeTypeComboBox.getSelectedIndex()];
//        codeArea.setCodeType(codeType);
//// TODO        preferences.setValue(BinEdFileEditor.PREFERENCES_CODE_TYPE, codeType.name());
//    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void openFile(BinEdVirtualFile virtualFile) {
        if (!virtualFile.isDirectory() && virtualFile.isValid()) {
            this.virtualFile = virtualFile;
            boolean editable = virtualFile.isWritable();
            File file = new File(virtualFile.getPath());
            if (file.isFile() && file.exists()) {
                try {
                    codeArea.setEditationMode(editable ? EditationMode.EXPANDING : EditationMode.READ_ONLY);
                    BinaryData oldData = codeArea.getContentData();
                    if (fileHandlingMode == FileHandlingMode.DELTA) {
                        FileDataSource fileSource = segmentsRepository.openFileSource(file, editable ? FileDataSource.EditationMode.READ_WRITE : FileDataSource.EditationMode.READ_ONLY);
                        DeltaDocument document = segmentsRepository.createDocument(fileSource);
                        codeArea.setContentData(document);
                        oldData.dispose();
                    } else {
                        try (FileInputStream fileStream = new FileInputStream(file)) {
                            BinaryData data = codeArea.getContentData();
                            if (!(data instanceof PagedData)) {
                                data = new PagedData();
                                oldData.dispose();
                            }
                            ((EditableBinaryData) data).loadFromStream(fileStream);
                            codeArea.setContentData(data);
                        }
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            } else {
                try (InputStream stream = virtualFile.getInputStream()) {
                    if (stream != null) {
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
            documentOriginalSize = codeArea.getDataSize();
            updateCurrentDocumentSize();
            updateCurrentMemoryMode();
            undoHandler.clear();
        }
    }

    public void saveFile(BinEdVirtualFile virtualFile) throws IOException {
        BinaryData data = codeArea.getContentData();
        if (data instanceof DeltaDocument) {
            segmentsRepository.saveDocument((DeltaDocument) data);
            undoHandler.setSyncPoint();
        } else {
            try (OutputStream stream = virtualFile.getOutputStream(this)) {
                codeArea.getContentData().saveToStream(stream);
                stream.flush();
                undoHandler.setSyncPoint();
                updateUndoState();
                saveFileButton.setEnabled(false);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        notifyModified();
        documentOriginalSize = codeArea.getDataSize();
        updateCurrentDocumentSize();
        updateCurrentMemoryMode();
    }

    private void reopenFile(@NotNull BinEdVirtualFile virtualFile, @NotNull BinaryData data, @NotNull CodeAreaUndoHandler undoHandler) {
        this.virtualFile = virtualFile;
        boolean editable = virtualFile.isWritable();
        codeArea.setEditationMode(editable ? EditationMode.EXPANDING : EditationMode.READ_ONLY);

        switchDeltaMemoryMode(data instanceof DeltaDocument ? FileHandlingMode.DELTA : FileHandlingMode.MEMORY);
        if (data instanceof DeltaDocument) {
            DeltaDocument document = (DeltaDocument) codeArea.getContentData();
            document.setFileSource(((DeltaDocument) data).getFileSource());
        }

        opened = true;
        documentOriginalSize = codeArea.getDataSize();
        updateCurrentDocumentSize();
        updateCurrentMemoryMode();

        this.undoHandler.clear();
        // TODO migrate undo
        try {
            this.undoHandler.execute(new InsertDataCommand(codeArea, 0, (EditableBinaryData) data));
        } catch (BinaryDataOperationException e) {
            e.printStackTrace();
        }
    }

    private void updateCurrentDocumentSize() {
        long dataSize = codeArea.getContentData().getDataSize();
        binaryStatus.setCurrentDocumentSize(dataSize, documentOriginalSize);
    }

    @Nonnull
    public FileHandlingMode getFileHandlingMode() {
        return fileHandlingMode;
    }

    public void setFileHandlingMode(FileHandlingMode fileHandlingMode) {
        this.fileHandlingMode = fileHandlingMode;
    }

    private void updateCurrentMemoryMode() {
        BinaryStatusApi.MemoryMode memoryMode = BinaryStatusApi.MemoryMode.RAM_MEMORY;
        if (codeArea.getEditationMode() == EditationMode.READ_ONLY) {
            memoryMode = BinaryStatusApi.MemoryMode.READ_ONLY;
        } else if (codeArea.getContentData() instanceof DeltaDocument) {
            memoryMode = BinaryStatusApi.MemoryMode.DELTA_MODE;
        }

        if (binaryStatus != null) {
            binaryStatus.setMemoryMode(memoryMode);
        }
    }

    public BinEdVirtualFile getVirtualFile() {
        return virtualFile;
    }

    public static synchronized SegmentsRepository getSegmentsRepository() {
        if (segmentsRepository == null) {
            segmentsRepository = new SegmentsRepository();
        }

        return segmentsRepository;
    }

    private JPopupMenu createContextMenu() {
        final JPopupMenu result = new JPopupMenu();

        final JMenuItem cutMenuItem = new JMenuItem("Cut");
        cutMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, metaMask));
        cutMenuItem.setEnabled(codeArea.hasSelection() && codeArea.isEditable());
        cutMenuItem.addActionListener(e -> {
            codeArea.cut();
            result.setVisible(false);
        });
        result.add(cutMenuItem);

        final JMenuItem copyMenuItem = new JMenuItem("Copy");
        copyMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, metaMask));
        copyMenuItem.setEnabled(codeArea.hasSelection());
        copyMenuItem.addActionListener(e -> {
            codeArea.copy();
            result.setVisible(false);
        });
        result.add(copyMenuItem);

        final JMenuItem copyAsCodeMenuItem = new JMenuItem("Copy as Code");
        copyAsCodeMenuItem.setEnabled(codeArea.hasSelection());
        copyAsCodeMenuItem.addActionListener(e -> {
            codeArea.copyAsCode();
            result.setVisible(false);
        });
        result.add(copyAsCodeMenuItem);

        final JMenuItem pasteMenuItem = new JMenuItem("Paste");
        pasteMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, metaMask));
        pasteMenuItem.setEnabled(codeArea.canPaste() && codeArea.isEditable());
        pasteMenuItem.addActionListener(e -> {
            codeArea.paste();
            result.setVisible(false);
        });
        result.add(pasteMenuItem);

        final JMenuItem pasteFromCodeMenuItem = new JMenuItem("Paste from Code");
        pasteFromCodeMenuItem.setEnabled(codeArea.canPaste() && codeArea.isEditable());
        pasteFromCodeMenuItem.addActionListener(e -> {
            try {
                codeArea.pasteFromCode();
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(editorPanel, ex.getMessage(), "Unable to Paste Code", JOptionPane.ERROR_MESSAGE);
            }
            result.setVisible(false);
        });
        result.add(pasteFromCodeMenuItem);

        final JMenuItem deleteMenuItem = new JMenuItem("Delete");
        deleteMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
        deleteMenuItem.setEnabled(codeArea.hasSelection() && codeArea.isEditable());
        deleteMenuItem.addActionListener(e -> {
            codeArea.delete();
            result.setVisible(false);
        });
        result.add(deleteMenuItem);
        result.addSeparator();

        final JMenuItem selectAllMenuItem = new JMenuItem("Select All");
        selectAllMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, metaMask));
        selectAllMenuItem.addActionListener(e -> {
            codeArea.selectAll();
            result.setVisible(false);
        });
        result.add(selectAllMenuItem);
        result.addSeparator();

        final JMenuItem goToMenuItem = new JMenuItem("Go To" + DialogUtils.DIALOG_MENUITEM_EXT);
        goToMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, metaMask));
        goToMenuItem.addActionListener(e -> goToHandler.getGoToRowAction().actionPerformed(null));
        result.add(goToMenuItem);

        final JMenuItem findMenuItem = new JMenuItem("Find" + DialogUtils.DIALOG_MENUITEM_EXT);
        findMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, metaMask));
        findMenuItem.addActionListener((ActionEvent e) -> {
            searchAction.actionPerformed(e);
            searchAction.switchReplaceMode(BinarySearchPanel.SearchOperation.FIND);
        });
        result.add(findMenuItem);

        final JMenuItem replaceMenuItem = new JMenuItem("Replace" + DialogUtils.DIALOG_MENUITEM_EXT);
        replaceMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, metaMask));
        replaceMenuItem.setEnabled(codeArea.isEditable());
        replaceMenuItem.addActionListener((ActionEvent e) -> {
            searchAction.actionPerformed(e);
            searchAction.switchReplaceMode(BinarySearchPanel.SearchOperation.REPLACE);
        });
        result.add(replaceMenuItem);
        result.addSeparator();
        final JMenuItem optionsMenuItem = new JMenuItem("Options" + DialogUtils.DIALOG_MENUITEM_EXT);
        optionsMenuItem.addActionListener(e -> {
            final BinEdOptionsPanelBorder optionsPanel = new BinEdOptionsPanelBorder();
            optionsPanel.load();
            optionsPanel.setApplyOptions(getApplyOptions());
            optionsPanel.setPreferredSize(new Dimension(640, 480));
            OptionsControlPanel optionsControlPanel = new OptionsControlPanel();
            JPanel dialogPanel = WindowUtils.createDialogPanel(optionsPanel, optionsControlPanel);
            final DialogWrapper dialog = DialogUtils.createDialog(dialogPanel, "Options");
            WindowUtils.assignGlobalKeyListener(dialogPanel, optionsControlPanel.createOkCancelListener());
            optionsControlPanel.setHandler(actionType -> {
                if (actionType == OptionsControlHandler.ControlActionType.SAVE) {
                    optionsPanel.store();
                }
                if (actionType != OptionsControlHandler.ControlActionType.CANCEL) {
                    setApplyOptions(optionsPanel.getApplyOptions());
                    codeArea.repaint();
                }

                dialog.close(0);
            });
            dialog.setSize(650, 460);
            dialog.showAndGet();
        });
        result.add(optionsMenuItem);
        result.addSeparator();

        final JMenuItem aboutMenuItem = new JMenuItem("About...");
        aboutMenuItem.addActionListener((ActionEvent e) -> {
            AboutPanel aboutPanel = new AboutPanel();
            aboutPanel.setupFields();
            CloseControlPanel closeControlPanel = new CloseControlPanel();
            JPanel dialogPanel = WindowUtils.createDialogPanel(aboutPanel, closeControlPanel);
            final DialogWrapper dialog = DialogUtils.createDialog(dialogPanel, "About Plugin");
            WindowUtils.assignGlobalKeyListener(dialogPanel, closeControlPanel.createOkCancelListener());
            closeControlPanel.setHandler(() -> {
                dialog.close(0);
            });
            dialog.setSize(650, 460);
            dialog.showAndGet();
        });
        result.add(aboutMenuItem);

        return result;
    }

    @Nonnull
    private BinEdApplyOptions getApplyOptions() {
        BinEdApplyOptions applyOptions = new BinEdApplyOptions();
        applyOptions.applyFromCodeArea(codeArea);
        EditorOptions editorOptions = applyOptions.getEditorOptions();
        editorOptions.setIsShowValuesPanel(valuesPanelVisible);
        editorOptions.setFileHandlingMode(fileHandlingMode.name());
        applyOptions.getStatusOptions().loadFromParameters(preferences.getStatusParameters());
        return applyOptions;
    }

    private void setApplyOptions(BinEdApplyOptions applyOptions) {
        applyOptions.applyToCodeArea(codeArea);
        EditorOptions editorOptions = applyOptions.getEditorOptions();
        switchShowValuesPanel(editorOptions.isIsShowValuesPanel());

        FileHandlingMode newFileHandlingMode;
        try {
            newFileHandlingMode = FileHandlingMode.valueOf(editorOptions.getFileHandlingMode());
        } catch (Exception ex) {
            newFileHandlingMode = DEFAULT_FILE_HANDLING_MODE;
        }
        switchDeltaMemoryMode(newFileHandlingMode);

        StatusOptions statusOptions = applyOptions.getStatusOptions();
        statusPanel.setStatusOptions(statusOptions);
        toolbarPanel.applyFromCodeArea();

        int selectedLayoutProfile = preferences.getLayoutParameters().getSelectedProfile();
        if (selectedLayoutProfile >= 0) {
            codeArea.setLayoutProfile(preferences.getLayoutParameters().getLayoutProfile(selectedLayoutProfile));
        }

        int selectedThemeProfile = preferences.getThemeParameters().getSelectedProfile();
        if (selectedThemeProfile >= 0) {
            codeArea.setThemeProfile(preferences.getThemeParameters().getThemeProfile(selectedThemeProfile));
        }

        int selectedColorProfile = preferences.getColorParameters().getSelectedProfile();
        if (selectedColorProfile >= 0) {
            codeArea.setColorsProfile(preferences.getColorParameters().getColorsProfile(selectedColorProfile));
        }
    }

    public void showValuesPanel() {
        if (!valuesPanelVisible) {
            valuesPanelVisible = true;
            if (valuesPanel == null) {
                valuesPanel = new ValuesPanel();
                valuesPanel.setCodeArea(codeArea, undoHandler);
                valuesPanelScrollPane = new JBScrollPane(valuesPanel);
            }
            editorPanel.add(valuesPanelScrollPane, BorderLayout.EAST);
            valuesPanel.enableUpdate();
            valuesPanel.updateValues();
            valuesPanelScrollPane.revalidate();
            valuesPanel.revalidate();
            editorPanel.revalidate();
        }
    }

    public void hideValuesPanel() {
        if (valuesPanelVisible) {
            valuesPanelVisible = false;
            valuesPanel.disableUpdate();
            editorPanel.remove(valuesPanelScrollPane);
            editorPanel.revalidate();
        }
    }

    private void loadFromPreferences() {
        try {
            fileHandlingMode = FileHandlingMode.valueOf(preferences.getEditorParameters().getFileHandlingMode());
        } catch (Exception ex) {
            fileHandlingMode = DEFAULT_FILE_HANDLING_MODE;
        }
        CodeAreaOptions codeAreaOptions = new CodeAreaOptions();
        codeAreaOptions.loadFromParameters(preferences.getCodeAreaParameters());
        codeAreaOptions.applyToCodeArea(codeArea);
        String selectedEncoding = preferences.getCodeAreaParameters().getSelectedEncoding();
        statusPanel.setEncoding(selectedEncoding);
        statusPanel.loadFromPreferences(preferences.getStatusParameters());
        toolbarPanel.loadFromPreferences();

        codeArea.setCharset(Charset.forName(selectedEncoding));
        encodingsHandler.loadFromPreferences(preferences);

        int selectedLayoutProfile = preferences.getLayoutParameters().getSelectedProfile();
        if (selectedLayoutProfile >= 0) {
            codeArea.setLayoutProfile(preferences.getLayoutParameters().getLayoutProfile(selectedLayoutProfile));
        }

        int selectedThemeProfile = preferences.getThemeParameters().getSelectedProfile();
        if (selectedThemeProfile >= 0) {
            codeArea.setThemeProfile(preferences.getThemeParameters().getThemeProfile(selectedThemeProfile));
        }

        int selectedColorProfile = preferences.getColorParameters().getSelectedProfile();
        if (selectedColorProfile >= 0) {
            codeArea.setColorsProfile(preferences.getColorParameters().getColorsProfile(selectedColorProfile));
        }

        // Memory mode handled from outside by isDeltaMemoryMode() method, worth fixing?
        boolean showValuesPanel = preferences.getEditorParameters().isShowValuesPanel();
        if (showValuesPanel) {
            showValuesPanel();
        }
    }

    public Project getProject() {
        return project;
    }

    private static ExtendedBackgroundPaintMode convertBackgroundPaintMode(String value) {
        if ("STRIPPED".equals(value))
            return ExtendedBackgroundPaintMode.STRIPED;
        return ExtendedBackgroundPaintMode.valueOf(value);
    }

    public interface CharsetChangeListener {

        void charsetChanged();
    }
}
