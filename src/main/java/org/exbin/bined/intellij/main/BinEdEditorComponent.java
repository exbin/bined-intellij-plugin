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

import com.intellij.ui.components.JBPanel;
import org.exbin.auxiliary.paged_data.BinaryData;
import org.exbin.auxiliary.paged_data.delta.DeltaDocument;
import org.exbin.bined.CodeAreaCaretPosition;
import org.exbin.bined.CodeType;
import org.exbin.bined.EditMode;
import org.exbin.bined.EditOperation;
import org.exbin.bined.capability.CharsetCapable;
import org.exbin.bined.extended.layout.ExtendedCodeAreaLayoutProfile;
import org.exbin.bined.intellij.BinEdPluginStartupActivity;
import org.exbin.bined.intellij.gui.BinEdComponentPanel;
import org.exbin.bined.intellij.gui.BinEdToolbarPanel;
import org.exbin.bined.intellij.options.IntegrationOptions;
import org.exbin.bined.operation.BinaryDataCommand;
import org.exbin.bined.operation.swing.CodeAreaOperationCommandHandler;
import org.exbin.bined.operation.undo.BinaryDataUndoHandler;
import org.exbin.bined.operation.undo.BinaryDataUndoUpdateListener;
import org.exbin.bined.swing.basic.color.CodeAreaColorsProfile;
import org.exbin.bined.swing.capability.FontCapable;
import org.exbin.bined.swing.extended.ExtCodeArea;
import org.exbin.bined.swing.extended.theme.ExtendedCodeAreaThemeProfile;
import org.exbin.framework.bined.BinaryStatusApi;
import org.exbin.framework.bined.FileHandlingMode;
import org.exbin.framework.bined.gui.BinEdComponentFileApi;
import org.exbin.framework.bined.gui.BinaryStatusPanel;
import org.exbin.framework.bined.inspector.options.DataInspectorOptions;
import org.exbin.framework.bined.options.CodeAreaColorOptions;
import org.exbin.framework.bined.options.CodeAreaLayoutOptions;
import org.exbin.framework.bined.options.CodeAreaOptions;
import org.exbin.framework.bined.options.CodeAreaThemeOptions;
import org.exbin.framework.bined.options.EditorOptions;
import org.exbin.framework.bined.options.StatusOptions;
import org.exbin.framework.bined.options.impl.CodeAreaOptionsImpl;
import org.exbin.framework.bined.options.impl.StatusOptionsImpl;
import org.exbin.framework.bined.preferences.BinaryEditorPreferences;
import org.exbin.framework.editor.text.EncodingsHandler;
import org.exbin.framework.editor.text.TextEncodingStatusApi;
import org.exbin.framework.editor.text.options.TextEncodingOptions;
import org.exbin.framework.editor.text.options.TextFontOptions;
import org.exbin.framework.utils.ActionUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.nio.charset.Charset;

/**
 * Component for BinEd editor instances.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class BinEdEditorComponent {

    public static final String ACTION_CLIPBOARD_CUT = "cut-to-clipboard";
    public static final String ACTION_CLIPBOARD_COPY = "copy-to-clipboard";
    public static final String ACTION_CLIPBOARD_PASTE = "paste-from-clipboard";

    private BinEdComponentPanel componentPanel = new BinEdComponentPanel();
    private JBPanel wrapperPanel = new JBPanel(new BorderLayout());
    private final BinEdToolbarPanel toolbarPanel;
    private final BinaryStatusPanel statusPanel;

    private BinEdComponentFileApi fileApi = null;

    private final ExtendedCodeAreaLayoutProfile defaultLayoutProfile;
    private final ExtendedCodeAreaThemeProfile defaultThemeProfile;
    private final CodeAreaColorsProfile defaultColorProfile;

    private BinaryStatusApi binaryStatus;
    private TextEncodingStatusApi encodingStatus;
    private CharsetChangeListener charsetChangeListener = null;
    private ModifiedStateListener modifiedChangeListener = null;
    private EncodingsHandler encodingsHandler;

    private final Font defaultFont;
    private long documentOriginalSize;
    private FileHandlingMode fileHandlingMode = FileHandlingMode.DELTA;

    public BinEdEditorComponent() {
        wrapperPanel.add(componentPanel, BorderLayout.CENTER);
        ExtCodeArea codeArea = componentPanel.getCodeArea();
        toolbarPanel = new BinEdToolbarPanel(codeArea,
                new BinEdToolbarPanel.Control() {
                    @Nonnull
                    @Override
                    public CodeType getCodeType() {
                        return codeArea.getCodeType();
                    }

                    @Override
                    public void setCodeType(CodeType codeType) {
                        codeArea.setCodeType(codeType);
                    }

                    @Override
                    public boolean isShowUnprintables() {
                        return codeArea.isShowUnprintables();
                    }

                    @Override
                    public void setShowUnprintables(boolean showUnprintables) {
                        codeArea.setShowUnprintables(showUnprintables);
                    }

                    @Override
                    public void repaint() {
                        codeArea.repaint();
                    }
                }
        );
        statusPanel = new BinaryStatusPanel();

        defaultLayoutProfile = codeArea.getLayoutProfile();
        defaultThemeProfile = codeArea.getThemeProfile();
        defaultColorProfile = codeArea.getColorsProfile();

        defaultFont = new Font(Font.MONOSPACED, Font.PLAIN, 12);
        codeArea.setCodeFont(defaultFont);

        wrapperPanel.add(toolbarPanel, BorderLayout.NORTH);
        registerEncodingStatus(statusPanel);
        encodingsHandler = new EncodingsHandler();
        encodingsHandler.setParentComponent(componentPanel);
        encodingsHandler.init();
        encodingsHandler.setTextEncodingStatus(new TextEncodingStatusApi() {
            @Nonnull
            @Override
            public String getEncoding() {
                return encodingStatus.getEncoding();
            }

            @Override
            public void setEncoding(String encodingName) {
                codeArea.setCharset(Charset.forName(encodingName));
                encodingStatus.setEncoding(encodingName);
                // TODO preferences.getEncodingPreferences().setSelectedEncoding(encodingName);
                charsetChangeListener.charsetChanged();
            }
        });

        registerBinaryStatus(statusPanel);

        wrapperPanel.add(statusPanel, BorderLayout.SOUTH);

        codeArea.addDataChangedListener(() -> {
            // TODO searchAction.codeAreaDataChanged();
            updateCurrentDocumentSize();
        });

        toolbarPanel.applyFromCodeArea();

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
        // TODO actionMap.put("reloadFile", reloadFileAction);
        // TODO actionMap.put("saveDocument", saveDocumentAction);

        InputMap inputMap = componentPanel.getInputMap();
        inputMap.put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_X, ActionUtils.getMetaMask()), ACTION_CLIPBOARD_CUT);
        inputMap.put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, ActionUtils.getMetaMask()), ACTION_CLIPBOARD_COPY);
        inputMap.put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_V, ActionUtils.getMetaMask()), ACTION_CLIPBOARD_PASTE);
        inputMap.put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, ActionUtils.getMetaMask()), "saveDocument");
        inputMap.put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, ActionUtils.getMetaMask() | InputEvent.SHIFT_DOWN_MASK), "saveDocument");
    }

    public void registerBinaryStatus(BinaryStatusApi binaryStatusApi) {
        this.binaryStatus = binaryStatusApi;
        ExtCodeArea codeArea = componentPanel.getCodeArea();
        codeArea.addCaretMovedListener((CodeAreaCaretPosition caretPosition) -> {
            binaryStatus.setCursorPosition(caretPosition);
        });
        codeArea.addSelectionChangedListener(() -> {
            binaryStatus.setSelectionRange(codeArea.getSelection());
        });

        codeArea.addEditModeChangedListener(binaryStatus::setEditMode);
        binaryStatus.setEditMode(codeArea.getEditMode(), codeArea.getActiveOperation());

        ((BinaryStatusPanel) binaryStatus).setStatusControlHandler(new BinaryStatusPanel.StatusControlHandler() {
            @Override
            public void changeEditOperation(EditOperation editOperation) {
                codeArea.setEditOperation(editOperation);
            }

            @Override
            public void changeCursorPosition() {
                throw new UnsupportedOperationException("Not supported yet.");
                // TODO goToPositionAction.actionPerformed(new ActionEvent(BinEdEditorComponent.this, 0, ""));
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
                if (fileHandlingMode == FileHandlingMode.NATIVE && memoryMode != BinaryStatusApi.MemoryMode.NATIVE) {
                    throw new IllegalStateException("Cannot change from native mode");
                }

                FileHandlingMode newHandlingMode = memoryMode == BinaryStatusApi.MemoryMode.DELTA_MODE ? FileHandlingMode.DELTA : FileHandlingMode.MEMORY;
                if (newHandlingMode != getFileHandlingMode()) {
                    fileApi.switchFileHandlingMode(newHandlingMode);
                    // TODO preferences.getEditorPreferences().setFileHandlingMode(newHandlingMode);
                    setFileHandlingMode(newHandlingMode);
                }
            }
        });
    }

    @Nonnull
    public JComponent getComponent() {
        return wrapperPanel;
    }

    @Nonnull
    public BinEdComponentPanel getComponentPanel() {
        return componentPanel;
    }

    @Nonnull
    public ExtCodeArea getCodeArea() {
        return componentPanel.getCodeArea();
    }

    @Nullable
    public BinaryDataUndoHandler getUndoHandler() {
        return componentPanel.getUndoHandler();
    }

    @Nullable
    public BinEdComponentFileApi getFileApi() {
        return fileApi;
    }

    public void setFileApi(BinEdComponentFileApi fileApi) {
        this.fileApi = fileApi;

        if (fileApi.isSaveSupported()) {
            toolbarPanel.setSaveAction(new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    saveDocument();
                }
            });
        }
    }

    @Nonnull
    public Font getDefaultFont() {
        return defaultFont;
    }

    public void updateTextEncodingStatus(EncodingsHandler encodingsHandler) {
        if (statusPanel != null) {
            encodingsHandler.setTextEncodingStatus(statusPanel);
        }
    }

    public void applyPreferencesChanges(StatusOptionsImpl options) {
        statusPanel.setStatusOptions(options);
    }

    public void setStatusControlHandler(BinaryStatusPanel.StatusControlHandler statusControlHandler) {
        statusPanel.setStatusControlHandler(statusControlHandler);
    }

    public void registerEncodingStatus(TextEncodingStatusApi encodingStatusApi) {
        ExtCodeArea codeArea = componentPanel.getCodeArea();
        this.encodingStatus = encodingStatusApi;
        setCharsetChangeListener(() -> {
            String selectedEncoding = codeArea.getCharset().name();
            encodingStatus.setEncoding(selectedEncoding);
        });
    }

    public void setCharsetChangeListener(CharsetChangeListener charsetChangeListener) {
        this.charsetChangeListener = charsetChangeListener;
    }

    public void setModifiedChangeListener(ModifiedStateListener modifiedChangeListener) {
        this.modifiedChangeListener = modifiedChangeListener;
    }

    public boolean isModified() {
        BinaryDataUndoHandler undoHandler = getUndoHandler();
        return undoHandler != null && undoHandler.getCommandPosition() != undoHandler.getSyncPoint();
    }

    @Nonnull
    public BinaryStatusPanel getBinaryStatusPanel() {
        return statusPanel;
    }

    @Nonnull
    public BinEdToolbarPanel getToolbarPanel() {
        return toolbarPanel;
    }

    /**
     * Attempts to release current file and warn if document was modified.
     *
     * @return true if successful
     */
    public boolean releaseFile() {
        if (fileApi == null)
            return true;

        while (isModified() && fileApi.isSaveSupported()) {
            Object[] options = {
                    "Save",
                    "Discard",
                    "Cancel"
            };
            int result = JOptionPane.showOptionDialog(componentPanel,
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

            saveDocument();
        }

        return true;
    }

    public void saveDocument() {
        ExtCodeArea codeArea = componentPanel.getCodeArea();
        fileApi.saveDocument();

        notifyModified();
        documentOriginalSize = codeArea.getDataSize();
        updateCurrentDocumentSize();
        updateCurrentMemoryMode();
    }

    public void updateCurrentDocumentSize() {
        ExtCodeArea codeArea = componentPanel.getCodeArea();
        long dataSize = codeArea.getDataSize();
        binaryStatus.setCurrentDocumentSize(dataSize, documentOriginalSize);
    }

    @Nonnull
    public FileHandlingMode getFileHandlingMode() {
        return fileHandlingMode;
    }

    public void setFileHandlingMode(FileHandlingMode fileHandlingMode) {
        this.fileHandlingMode = fileHandlingMode;
        updateCurrentMemoryMode();
    }

    private void updateCurrentMemoryMode() {
        ExtCodeArea codeArea = componentPanel.getCodeArea();
        BinaryStatusApi.MemoryMode memoryMode = BinaryStatusApi.MemoryMode.RAM_MEMORY;
        if (fileHandlingMode == FileHandlingMode.NATIVE) {
            memoryMode = BinaryStatusApi.MemoryMode.NATIVE;
        } else if (codeArea.getEditMode() == EditMode.READ_ONLY) {
            memoryMode = BinaryStatusApi.MemoryMode.READ_ONLY;
        } else if (codeArea.getContentData() instanceof DeltaDocument) {
            memoryMode = BinaryStatusApi.MemoryMode.DELTA_MODE;
        }

        if (binaryStatus != null) {
            binaryStatus.setMemoryMode(memoryMode);
        }
    }

    private void notifyModified() {
        if (modifiedChangeListener != null) {
            modifiedChangeListener.modifiedChanged();
        }

        toolbarPanel.updateModified(isModified());
    }

    public void updateApplyOptions(BinEdApplyOptions applyOptions) {
        ExtCodeArea codeArea = componentPanel.getCodeArea();
        CodeAreaOptionsImpl.applyFromCodeArea(applyOptions.getCodeAreaOptions(), codeArea);
        applyOptions.getEncodingOptions().setSelectedEncoding(((CharsetCapable) codeArea).getCharset().name());

//        TODO DataInspectorOptions dataInspectorOptions = applyOptions.getDataInspectorOptions();
//        dataInspectorOptions.setShowParsingPanel(isShowInspectorPanel());
        EditorOptions editorOptions = applyOptions.getEditorOptions();
        editorOptions.setFileHandlingMode(fileHandlingMode);
        if (codeArea.getCommandHandler() instanceof CodeAreaOperationCommandHandler) {
            editorOptions.setEnterKeyHandlingMode(((CodeAreaOperationCommandHandler) codeArea.getCommandHandler()).getEnterKeyHandlingMode());
        }

        // TODO applyOptions.getStatusOptions().loadFromPreferences(preferences.getStatusPreferences());
    }

    public void setUndoHandler(BinaryDataUndoHandler undoHandler) {
        componentPanel.setUndoHandler(undoHandler);
        ExtCodeArea codeArea = componentPanel.getCodeArea();
        toolbarPanel.setUndoHandler(undoHandler);
        CodeAreaOperationCommandHandler commandHandler = new CodeAreaOperationCommandHandler(codeArea, undoHandler);
        codeArea.setCommandHandler(commandHandler);
        // TODO set ENTER KEY mode in apply options

        undoHandler.addUndoUpdateListener(new BinaryDataUndoUpdateListener() {
            @Override
            public void undoCommandPositionChanged() {
                codeArea.repaint();
                updateCurrentDocumentSize();
                notifyModified();
            }

            @Override
            public void undoCommandAdded(@Nonnull final BinaryDataCommand command) {
                updateCurrentDocumentSize();
                notifyModified();
            }
        });
    }

    @Nullable
    public BinaryData getContentData() {
        ExtCodeArea codeArea = componentPanel.getCodeArea();
        return codeArea.getContentData();
    }

    public void setContentData(@Nullable BinaryData data) {
        ExtCodeArea codeArea = componentPanel.getCodeArea();
        codeArea.setContentData(data);

        documentOriginalSize = codeArea.getDataSize();
        updateCurrentDocumentSize();
        updateCurrentMemoryMode();

        // Autodetect encoding using IDE mechanism
        //        final Charset charset = Charset.forName(FileEncodingQuery.getEncoding(dataObject.getPrimaryFile()).name());
        //        if (charsetChangeListener != null) {
        //            charsetChangeListener.charsetChanged();
        //        }
        //        codeArea.setCharset(charset);
    }

    public void initialLoadFromPreferences(BinaryEditorPreferences preferences) {
        applyOptions(new BinEdApplyOptions() {
            @Nonnull
            @Override
            public CodeAreaOptions getCodeAreaOptions() {
                return preferences.getCodeAreaPreferences();
            }

            @Nonnull
            @Override
            public TextEncodingOptions getEncodingOptions() {
                return preferences.getEncodingPreferences();
            }

            @Nonnull
            @Override
            public TextFontOptions getFontOptions() {
                return preferences.getFontPreferences();
            }

            @Nonnull
            @Override
            public IntegrationOptions getIntegrationOptions() {
                return preferences.getIntegrationPreferences();
            }

            @Nonnull
            @Override
            public EditorOptions getEditorOptions() {
                return preferences.getEditorPreferences();
            }

            @Nonnull
            @Override
            public StatusOptions getStatusOptions() {
                return preferences.getStatusPreferences();
            }

            @Nonnull
            @Override
            public DataInspectorOptions getDataInspectorOptions() {
                return preferences.getDataInspectorPreferences();
            }

            @Nonnull
            @Override
            public CodeAreaLayoutOptions getLayoutOptions() {
                return preferences.getLayoutPreferences();
            }

            @Nonnull
            @Override
            public CodeAreaColorOptions getColorOptions() {
                return preferences.getColorPreferences();
            }

            @Nonnull
            @Override
            public CodeAreaThemeOptions getThemeOptions() {
                return preferences.getThemePreferences();
            }
        });

        encodingsHandler.loadFromPreferences(preferences.getEncodingPreferences());
        statusPanel.loadFromPreferences(preferences.getStatusPreferences());
        toolbarPanel.loadFromPreferences(preferences);

        FileHandlingMode newFileHandlingMode = preferences.getEditorPreferences().getFileHandlingMode();
        setFileHandlingMode(newFileHandlingMode);
    }

    public void applyOptions(BinEdApplyOptions applyOptions) {
        ExtCodeArea codeArea = componentPanel.getCodeArea();
        BinEdPluginStartupActivity.applyIntegrationOptions(applyOptions.getIntegrationOptions());
        CodeAreaOptionsImpl.applyToCodeArea(applyOptions.getCodeAreaOptions(), codeArea);

        ((CharsetCapable) codeArea).setCharset(Charset.forName(applyOptions.getEncodingOptions().getSelectedEncoding()));
        encodingsHandler.setEncodings(applyOptions.getEncodingOptions().getEncodings());
        ((FontCapable) codeArea).setCodeFont(applyOptions.getFontOptions().isUseDefaultFont() ? defaultFont : applyOptions.getFontOptions().getFont(defaultFont));

//        DataInspectorOptions dataInspectorOptions = applyOptions.getDataInspectorOptions();
//        setShowInspectorPanel(dataInspectorOptions.isShowParsingPanel());
        EditorOptions editorOptions = applyOptions.getEditorOptions();
        if (codeArea.getCommandHandler() instanceof CodeAreaOperationCommandHandler) {
            ((CodeAreaOperationCommandHandler) codeArea.getCommandHandler()).setEnterKeyHandlingMode(editorOptions.getEnterKeyHandlingMode());
        }

        StatusOptions statusOptions = applyOptions.getStatusOptions();
        statusPanel.setStatusOptions(statusOptions);
        toolbarPanel.applyFromCodeArea();

        CodeAreaLayoutOptions layoutOptions = applyOptions.getLayoutOptions();
        int selectedLayoutProfile = layoutOptions.getSelectedProfile();
        if (selectedLayoutProfile >= 0) {
            codeArea.setLayoutProfile(layoutOptions.getLayoutProfile(selectedLayoutProfile));
        } else {
            codeArea.setLayoutProfile(defaultLayoutProfile);
        }

        CodeAreaThemeOptions themeOptions = applyOptions.getThemeOptions();
        int selectedThemeProfile = themeOptions.getSelectedProfile();
        if (selectedThemeProfile >= 0) {
            codeArea.setThemeProfile(themeOptions.getThemeProfile(selectedThemeProfile));
        } else {
            codeArea.setThemeProfile(defaultThemeProfile);
        }

        CodeAreaColorOptions colorOptions = applyOptions.getColorOptions();
        int selectedColorProfile = colorOptions.getSelectedProfile();
        if (selectedColorProfile >= 0) {
            codeArea.setColorsProfile(colorOptions.getColorsProfile(selectedColorProfile));
        } else {
            codeArea.setColorsProfile(defaultColorProfile);
        }
    }

    public interface CharsetChangeListener {

        void charsetChanged();
    }

    public interface ModifiedStateListener {

        void modifiedChanged();
    }
}
