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
package org.exbin.deltahex.intellij;

import com.intellij.codeHighlighting.BackgroundEditorHighlighter;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorLocation;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import org.exbin.deltahex.CodeType;
import org.exbin.deltahex.operation.BinaryDataCommand;
import org.exbin.deltahex.operation.swing.CodeCommandHandler;
import org.exbin.deltahex.operation.undo.BinaryDataUndoHandler;
import org.exbin.deltahex.operation.undo.BinaryDataUndoUpdateListener;
import org.exbin.deltahex.swing.CodeArea;
import org.exbin.utils.binary_data.EditableBinaryData;
import org.exbin.utils.binary_data.PagedData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

/**
 * File editor using DeltaHex editor component.
 *
 * @author ExBin Project (http://exbin.org)
 * @version 0.1.1 2016/12/20
 */
public class DeltaHexFileEditor implements FileEditor {

    private final Project project;
    private JPanel editorPanel;
    private final CodeArea codeArea;
    private final BinaryDataUndoHandler undoHandler;
    private final int metaMask;
    private final PropertyChangeSupport propertyChangeSupport;

    private boolean modified = false;
    private String displayName;
    private DeltaHexVirtualFile virtualFile;

    public DeltaHexFileEditor(Project project) {
        this.project = project;
        editorPanel = new JPanel();
        initComponents();
        codeArea = new CodeArea();
        codeArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        codeArea.getCaret().setBlinkRate(300);

        propertyChangeSupport = new PropertyChangeSupport(this);
        // CodeAreaUndoHandler(codeArea);
        undoHandler = new HexUndoIntelliJHandler(codeArea, project, this);

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

        codeArea.setData(new PagedData());
        CodeCommandHandler commandHandler = new CodeCommandHandler(codeArea, undoHandler);
        codeArea.setCommandHandler(commandHandler);
        editorPanel.add(codeArea, BorderLayout.CENTER);

        codeArea.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                maybeShowPopup(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                maybeShowPopup(e);
            }

            private void maybeShowPopup(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    JPopupMenu popupMenu = createContextMenu();
                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });

//        setName(NbBundle.getMessage(HexEditorTopComponent.class, "CTL_HexEditorTopComponent"));
//        setToolTipText(NbBundle.getMessage(HexEditorTopComponent.class, "HINT_HexEditorTopComponent"));

        encodingComboBox.setModel(new DefaultComboBoxModel<>(getSupportedEncodings()));
        encodingComboBox.setSelectedItem(codeArea.getCharset().name());
        codeTypeComboBox.setSelectedIndex(codeArea.getCodeType().ordinal());

        editorPanel.getActionMap().put("copy-to-clipboard", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                codeArea.copy();
            }
        });
        editorPanel.getActionMap().put("cut-to-clipboard", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                codeArea.cut();
            }
        });
        editorPanel.getActionMap().put("paste-from-clipboard", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                codeArea.paste();
            }
        });

        int metaMaskValue;
        try {
            metaMaskValue = java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        } catch (java.awt.HeadlessException ex) {
            metaMaskValue = java.awt.Event.CTRL_MASK;
        }
        metaMask = metaMaskValue;
    }

    private javax.swing.JComboBox<String> codeTypeComboBox;
    private javax.swing.JToolBar controlToolBar;
    private javax.swing.JComboBox<String> encodingComboBox;
    private javax.swing.JLabel encodingLabel;
    private javax.swing.JPanel infoToolbar;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JToolBar.Separator jSeparator2;
    private javax.swing.JToolBar.Separator jSeparator3;
    private javax.swing.JButton saveFileButton;
    private javax.swing.JButton undoEditButton;
    private javax.swing.JButton redoEditButton;
    private javax.swing.JToggleButton lineWrappingToggleButton;
    private javax.swing.JToggleButton showUnprintablesToggleButton;

    private void initComponents() {

        infoToolbar = new javax.swing.JPanel();
        encodingLabel = new javax.swing.JLabel();
        encodingComboBox = new javax.swing.JComboBox<String>();
        controlToolBar = new javax.swing.JToolBar();
        saveFileButton = new javax.swing.JButton();
        undoEditButton = new javax.swing.JButton();
        redoEditButton = new javax.swing.JButton();
        lineWrappingToggleButton = new javax.swing.JToggleButton();
        showUnprintablesToggleButton = new javax.swing.JToggleButton();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        jSeparator2 = new javax.swing.JToolBar.Separator();
        jSeparator3 = new javax.swing.JToolBar.Separator();
        codeTypeComboBox = new javax.swing.JComboBox<String>();

        editorPanel.setLayout(new java.awt.BorderLayout());

        encodingLabel.setText("Encoding");

        encodingComboBox.setPreferredSize(new java.awt.Dimension(200, 20));
        encodingComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                encodingComboBoxActionPerformed(evt);
            }
        });

        controlToolBar.setBorder(null);
        controlToolBar.setFloatable(false);
        controlToolBar.setRollover(true);

        saveFileButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/exbin/deltahex/intellij/resources/icons/document-save.png")));
        saveFileButton.setToolTipText("Save current file");
        saveFileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveFileButtonActionPerformed(evt);
            }
        });
        saveFileButton.setEnabled(false);
        controlToolBar.add(saveFileButton);
        controlToolBar.add(jSeparator1);

        undoEditButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/exbin/deltahex/intellij/resources/icons/edit-undo.png")));
        undoEditButton.setToolTipText("Undo last operation");
        undoEditButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                undoEditButtonActionPerformed(evt);
            }
        });
        controlToolBar.add(undoEditButton);

        redoEditButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/exbin/deltahex/intellij/resources/icons/edit-redo.png")));
        redoEditButton.setToolTipText("Redo last undid operation");
        redoEditButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                redoEditButtonActionPerformed(evt);
            }
        });
        controlToolBar.add(redoEditButton);
        controlToolBar.add(jSeparator2);

        lineWrappingToggleButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/exbin/deltahex/intellij/resources/icons/deltahex-linewrap.png")));
        lineWrappingToggleButton.setToolTipText("Wrap line to window size");
        lineWrappingToggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                lineWrappingToggleButtonActionPerformed(evt);
            }
        });
        controlToolBar.add(lineWrappingToggleButton);

        showUnprintablesToggleButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/exbin/deltahex/intellij/resources/icons/insert-pilcrow.png")));
        showUnprintablesToggleButton.setToolTipText("Show symbols for unprintable/whitespace characters");
        showUnprintablesToggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showUnprintablesToggleButtonActionPerformed(evt);
            }
        });
        controlToolBar.add(showUnprintablesToggleButton);
        controlToolBar.add(jSeparator3);

        codeTypeComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{"BIN", "OCT", "DEC", "HEX"}));
        codeTypeComboBox.setMaximumSize(new java.awt.Dimension(58, 25));
        codeTypeComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                codeTypeComboBoxActionPerformed(evt);
            }
        });
        controlToolBar.add(codeTypeComboBox);

        javax.swing.GroupLayout infoToolbarLayout = new javax.swing.GroupLayout(infoToolbar);
        infoToolbar.setLayout(infoToolbarLayout);
        infoToolbarLayout.setHorizontalGroup(
                infoToolbarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(infoToolbarLayout.createSequentialGroup()
                                .addComponent(controlToolBar, javax.swing.GroupLayout.DEFAULT_SIZE, 366, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(encodingLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(encodingComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap())
        );
        infoToolbarLayout.setVerticalGroup(
                infoToolbarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(controlToolBar, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                        .addGroup(infoToolbarLayout.createSequentialGroup()
                                .addComponent(encodingComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))
                        .addComponent(encodingLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        editorPanel.add(infoToolbar, java.awt.BorderLayout.NORTH);
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

    @Override
    public void setState(@NotNull FileEditorState state) {

    }

    @Override
    public boolean isModified() {
        return modified;
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
        if (modified != this.modified) {
            this.modified = modified;
            propertyChangeSupport.firePropertyChange(FileEditor.PROP_MODIFIED, !modified, modified);
        }
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

    private void encodingComboBoxActionPerformed(java.awt.event.ActionEvent evt) {
        codeArea.setCharset(Charset.forName(encodingComboBox.getSelectedItem().toString()));
    }

    private void saveFileButtonActionPerformed(java.awt.event.ActionEvent evt) {
        Application application = ApplicationManager.getApplication();
        application.runWriteAction(new Runnable() {
            @Override
            public void run() {
                try (OutputStream stream = virtualFile.getOutputStream(this)) {
                    codeArea.getData().saveToStream(stream);
                    undoHandler.setSyncPoint();
                    updateUndoState();
                    saveFileButton.setEnabled(false);
                } catch (IOException e) {
                    e.printStackTrace();
                }
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

    private void lineWrappingToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {
        codeArea.setWrapMode(lineWrappingToggleButton.isSelected());
    }

    private void showUnprintablesToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {
        codeArea.setShowUnprintableCharacters(showUnprintablesToggleButton.isSelected());
    }

    private void codeTypeComboBoxActionPerformed(java.awt.event.ActionEvent evt) {
        codeArea.setCodeType(CodeType.values()[codeTypeComboBox.getSelectedIndex()]);
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    private String[] getSupportedEncodings() {
        return Charset.availableCharsets().keySet().toArray(new String[0]);
    }

    public void openFile(DeltaHexVirtualFile virtualFile) {
        if (!virtualFile.isDirectory() && virtualFile.isValid()) {
            this.virtualFile = virtualFile;
            try (InputStream stream = virtualFile.getInputStream()) {
                ;
                if (stream != null) {
                    ((EditableBinaryData) codeArea.getData()).loadFromStream(stream);
                    codeArea.setEditable(virtualFile.isWritable());
                }
            } catch (IOException ex) {
                // Exceptions.printStackTrace(ex);
            }
        }
    }

    public DeltaHexVirtualFile getVirtualFile() {
        return virtualFile;
    }

    private JPopupMenu createContextMenu() {
        final JPopupMenu result = new JPopupMenu();

        final JMenuItem cutMenuItem = new JMenuItem("Cut");
        cutMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, metaMask));
        cutMenuItem.setEnabled(codeArea.hasSelection());
        cutMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                codeArea.cut();
                result.setVisible(false);
            }
        });
        result.add(cutMenuItem);

        final JMenuItem copyMenuItem = new JMenuItem("Copy");
        copyMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, metaMask));
        copyMenuItem.setEnabled(codeArea.hasSelection());
        copyMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                codeArea.copy();
                result.setVisible(false);
            }
        });
        result.add(copyMenuItem);

        final JMenuItem copyAsCodeMenuItem = new JMenuItem("Copy as Code");
        copyAsCodeMenuItem.setEnabled(codeArea.hasSelection());
        copyAsCodeMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                codeArea.copyAsCode();
                result.setVisible(false);
            }
        });
        result.add(copyAsCodeMenuItem);

        final JMenuItem pasteMenuItem = new JMenuItem("Paste");
        pasteMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, metaMask));
        pasteMenuItem.setEnabled(codeArea.canPaste());
        pasteMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                codeArea.paste();
                result.setVisible(false);
            }
        });
        result.add(pasteMenuItem);

        final JMenuItem pasteFromCodeMenuItem = new JMenuItem("Paste from Code");
        pasteFromCodeMenuItem.setEnabled(codeArea.canPaste());
        pasteFromCodeMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    codeArea.pasteFromCode();
                } catch (IllegalArgumentException ex) {
                    JOptionPane.showMessageDialog(codeArea, ex.getMessage(), "Unable to Paste Code", JOptionPane.ERROR_MESSAGE);
                }
                result.setVisible(false);
            }
        });
        result.add(pasteFromCodeMenuItem);

        final JMenuItem deleteMenuItem = new JMenuItem("Delete");
        deleteMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
        deleteMenuItem.setEnabled(codeArea.hasSelection());
        deleteMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                codeArea.delete();
                result.setVisible(false);
            }
        });
        result.add(deleteMenuItem);
        result.addSeparator();

        final JMenuItem selectAllMenuItem = new JMenuItem("Select All");
        selectAllMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, metaMask));
        selectAllMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                codeArea.selectAll();
                result.setVisible(false);
            }
        });
        result.add(selectAllMenuItem);

        return result;
    }
}
