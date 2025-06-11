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
package org.exbin.bined.intellij.debug.gui;

import org.exbin.auxiliary.binary_data.BinaryData;
import org.exbin.bined.CodeAreaCaretPosition;
import org.exbin.bined.CodeAreaUtils;
import org.exbin.bined.CodeType;
import org.exbin.bined.EditMode;
import org.exbin.bined.EditOperation;
import org.exbin.bined.SelectionRange;
import org.exbin.bined.highlight.swing.NonprintablesCodeAreaAssessor;
import org.exbin.bined.intellij.debug.DebugViewDataProvider;
import org.exbin.bined.intellij.gui.BinEdToolbarPanel;
import org.exbin.bined.swing.CodeAreaCore;
import org.exbin.bined.swing.CodeAreaSwingUtils;
import org.exbin.bined.swing.capability.ColorAssessorPainterCapable;
import org.exbin.bined.swing.section.SectCodeArea;
import org.exbin.framework.App;
import org.exbin.framework.action.api.ComponentActivationListener;
import org.exbin.framework.bined.BinEdDocumentView;
import org.exbin.framework.bined.BinEdFileManager;
import org.exbin.framework.bined.BinaryStatusApi;
import org.exbin.framework.bined.BinedModule;
import org.exbin.framework.bined.action.GoToPositionAction;
import org.exbin.framework.bined.gui.BinEdComponentPanel;
import org.exbin.framework.bined.gui.BinaryStatusPanel;
import org.exbin.framework.bined.handler.CodeAreaPopupMenuHandler;
import org.exbin.framework.bined.options.StatusOptions;
import org.exbin.framework.bined.viewer.BinedViewerModule;
import org.exbin.framework.text.encoding.EncodingsHandler;
import org.exbin.framework.frame.api.FrameModuleApi;
import org.exbin.framework.language.api.LanguageModuleApi;
import org.exbin.framework.options.api.OptionsModuleApi;
import org.exbin.framework.preferences.api.PreferencesModuleApi;
import org.exbin.framework.text.encoding.options.TextEncodingOptions;
import org.exbin.framework.utils.ClipboardActionsHandler;
import org.exbin.framework.utils.ClipboardActionsUpdateListener;
import org.exbin.framework.utils.DesktopUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.AbstractAction;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel to show debug view.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class DebugViewPanel extends javax.swing.JPanel {

    private final List<DebugViewDataProvider> providers = new ArrayList<>();
    private int selectedProvider = 0;

    private final JPanel panel;
    private BinEdToolbarPanel toolbarPanel = new BinEdToolbarPanel();
    private BinaryStatusPanel statusPanel = new BinaryStatusPanel();
    private BinaryStatusApi binaryStatus;
    private final BinEdDocumentView editorComponent;
    private long documentOriginalSize = 0;

    public DebugViewPanel() {
        panel = new JPanel(new BorderLayout());
        editorComponent = new BinEdDocumentView();

        initComponents();
        init();
    }

    private void init() {
        BinedModule binedModule = App.getModule(BinedModule.class);
        BinedViewerModule binedViewerModule = App.getModule(BinedViewerModule.class);
        BinEdFileManager fileManager = binedModule.getFileManager();
        BinEdComponentPanel componentPanel = (BinEdComponentPanel) editorComponent.getComponent();
        fileManager.initComponentPanel(componentPanel);

        PreferencesModuleApi preferencesModule = App.getModule(PreferencesModuleApi.class);
        // TODO editorComponent.onInitFromOptions(new BinaryEditorOptions(preferencesModule.getAppPreferences()));

        SectCodeArea codeArea = componentPanel.getCodeArea();
        codeArea.setEditMode(EditMode.READ_ONLY);

        toolbarPanel.setTargetComponent(componentPanel);
        toolbarPanel.setCodeAreaControl(new BinEdToolbarPanel.Control() {
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
            public boolean isShowNonprintables() {
                ColorAssessorPainterCapable painter = (ColorAssessorPainterCapable) codeArea.getPainter();
                NonprintablesCodeAreaAssessor nonprintablesCodeAreaAssessor = CodeAreaSwingUtils.findColorAssessor(painter, NonprintablesCodeAreaAssessor.class);
                return CodeAreaUtils.requireNonNull(nonprintablesCodeAreaAssessor).isShowNonprintables();
            }

            @Override
            public void setShowNonprintables(boolean showNonprintables) {
                ColorAssessorPainterCapable painter = (ColorAssessorPainterCapable) codeArea.getPainter();
                NonprintablesCodeAreaAssessor nonprintablesCodeAreaAssessor = CodeAreaSwingUtils.findColorAssessor(painter, NonprintablesCodeAreaAssessor.class);
                CodeAreaUtils.requireNonNull(nonprintablesCodeAreaAssessor).setShowNonprintables(showNonprintables);
            }

            @Override
            public void repaint() {
                codeArea.repaint();
            }
        });
        toolbarPanel.setOnlineHelpAction(createOnlineHelpAction());

        OptionsModuleApi optionsModule = App.getModule(OptionsModuleApi.class);
        toolbarPanel.setOptionsAction(optionsModule.createOptionsAction());

        CodeAreaPopupMenuHandler codeAreaPopupMenuHandler =
                binedModule.createCodeAreaPopupMenuHandler(BinedModule.PopupMenuVariant.NORMAL);
        codeArea.setComponentPopupMenu(new JPopupMenu() {
            @Override
            public void show(Component invoker, int x, int y) {
                FrameModuleApi frameModule = App.getModule(FrameModuleApi.class);
                ComponentActivationListener componentActivationListener =
                        frameModule.getFrameHandler().getComponentActivationListener();

                componentActivationListener.updated(CodeAreaCore.class, codeArea);
                componentActivationListener.updated(ClipboardActionsHandler.class, new ClipboardActionsHandler() {
                    public void performCut() {
                        codeArea.cut();
                    }

                    public void performCopy() {
                        codeArea.copy();
                    }

                    public void performPaste() {
                        codeArea.paste();
                    }

                    public void performDelete() {
                        codeArea.delete();
                    }

                    public void performSelectAll() {
                        codeArea.selectAll();
                    }

                    public boolean isSelection() {
                        return codeArea.hasSelection();
                    }

                    public boolean isEditable() {
                        return codeArea.isEditable();
                    }

                    public boolean canSelectAll() {
                        return true;
                    }

                    public boolean canPaste() {
                        return codeArea.canPaste();
                    }

                    public boolean canDelete() {
                        return true;
                    }

                    @Override
                    public void setUpdateListener(ClipboardActionsUpdateListener clipboardActionsUpdateListener) {

                    }
                });

                String popupMenuId = "DebugViewPanel.popup";
                JPopupMenu popupMenu = codeAreaPopupMenuHandler.createPopupMenu(codeArea, popupMenuId, x, y);
                popupMenu.addPopupMenuListener(new PopupMenuListener() {
                    @Override
                    public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                    }

                    @Override
                    public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                        codeAreaPopupMenuHandler.dropPopupMenu(popupMenuId);
                    }

                    @Override
                    public void popupMenuCanceled(PopupMenuEvent e) {
                    }
                });

                popupMenu.show(invoker, x, y);
            }
        });

        EncodingsHandler encodingsHandler = binedViewerModule.getEncodingsHandler();
        encodingsHandler.loadFromOptions(new TextEncodingOptions(preferencesModule.getAppPreferences()));
        statusPanel.setStatusControlHandler(new BinaryStatusPanel.StatusControlHandler() {
            @Override
            public void changeEditOperation(EditOperation editOperation) {
                codeArea.setEditOperation(editOperation);
            }

            @Override
            public void changeCursorPosition() {
                GoToPositionAction action = new GoToPositionAction();
                action.setCodeArea(codeArea);
                action.actionPerformed(null);
            }

            @Override
            public void cycleNextEncoding() {
                if (encodingsHandler != null) {
                    encodingsHandler.cycleNextEncoding();
                }
            }

            @Override
            public void cyclePreviousEncoding() {
                if (encodingsHandler != null) {
                    encodingsHandler.cyclePreviousEncoding();
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
                // Ignore
            }
        });
        statusPanel.loadFromOptions(new StatusOptions(preferencesModule.getAppPreferences()));
        registerBinaryStatus(statusPanel);

        panel.add(toolbarPanel, BorderLayout.NORTH);
        panel.add(statusPanel, BorderLayout.SOUTH);
        panel.add(editorComponent.getComponent(), BorderLayout.CENTER);
        panel.revalidate();
        panel.repaint();

        this.add(panel, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">
    private void initComponents() {

        providerComboBox = new javax.swing.JComboBox<>();

        setLayout(new java.awt.BorderLayout());

        providerComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                providerComboBoxItemStateChanged(evt);
            }
        });
    }// </editor-fold>

    private void providerComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {
        int selectedIndex = providerComboBox.getSelectedIndex();
        if (selectedProvider != selectedIndex) {
            selectedProvider = selectedIndex;
            setContentData(providers.get(selectedProvider).getData());
        }
    }

    // Variables declaration - do not modify
    private javax.swing.JComboBox<String> providerComboBox;
    // End of variables declaration

    public void addProvider(DebugViewDataProvider provider) {
        if (providers.isEmpty()) {
            setContentData(provider.getData());
            add(providerComboBox, java.awt.BorderLayout.PAGE_START);
        }

        providers.add(provider);
        providerComboBox.addItem(provider.getName());
    }

    public void setContentData(@Nullable BinaryData data) {
        editorComponent.setContentData(data);
        long dataSize = data == null ? 0 : data.getDataSize();
        documentOriginalSize = dataSize;
        statusPanel.setCurrentDocumentSize(dataSize, documentOriginalSize);
    }

    @Nonnull
    private AbstractAction createOnlineHelpAction() {
        return new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                LanguageModuleApi languageModuleApi = App.getModule(LanguageModuleApi.class);
                DesktopUtils.openDesktopURL(languageModuleApi.getAppBundle().getString("online_help_url"));
            }
        };
    }

    public void registerBinaryStatus(BinaryStatusApi binaryStatus) {
        this.binaryStatus = binaryStatus;

        SectCodeArea codeArea = editorComponent.getCodeArea();
        codeArea.addDataChangedListener(() -> {
            updateCurrentDocumentSize();
        });

        codeArea.addSelectionChangedListener(() -> {
            binaryStatus.setSelectionRange(codeArea.getSelection());
        });

        codeArea.addCaretMovedListener((CodeAreaCaretPosition caretPosition) -> {
            binaryStatus.setCursorPosition(caretPosition);
        });

        codeArea.addEditModeChangedListener((EditMode mode, EditOperation operation) -> {
            binaryStatus.setEditMode(mode, operation);
        });

        updateStatus();
    }

    public void updateStatus() {
        updateCurrentDocumentSize();
        updateCurrentCaretPosition();
        updateCurrentSelectionRange();
        // updateCurrentMemoryMode();
        updateCurrentEditMode();
    }

    private void updateCurrentDocumentSize() {
        if (binaryStatus == null) {
            return;
        }

        SectCodeArea codeArea = editorComponent.getCodeArea();
        long dataSize = codeArea.getDataSize();
        binaryStatus.setCurrentDocumentSize(dataSize, documentOriginalSize);
    }

    private void updateCurrentCaretPosition() {
        if (binaryStatus == null) {
            return;
        }

        SectCodeArea codeArea = editorComponent.getCodeArea();
        CodeAreaCaretPosition caretPosition = codeArea.getActiveCaretPosition();
        binaryStatus.setCursorPosition(caretPosition);
    }

    private void updateCurrentSelectionRange() {
        if (binaryStatus == null) {
            return;
        }

        SectCodeArea codeArea = editorComponent.getCodeArea();
        SelectionRange selectionRange = codeArea.getSelection();
        binaryStatus.setSelectionRange(selectionRange);
    }

    private void updateCurrentEditMode() {
        if (binaryStatus == null) {
            return;
        }

        SectCodeArea codeArea = editorComponent.getCodeArea();
        binaryStatus.setEditMode(codeArea.getEditMode(), codeArea.getActiveOperation());
    }
}
