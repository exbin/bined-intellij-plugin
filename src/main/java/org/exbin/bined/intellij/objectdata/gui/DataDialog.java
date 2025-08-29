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
package org.exbin.bined.intellij.objectdata.gui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.components.BorderLayoutPanel;
import org.exbin.auxiliary.binary_data.BinaryData;
import org.exbin.auxiliary.binary_data.EditableBinaryData;
import org.exbin.bined.CodeAreaCaretPosition;
import org.exbin.bined.CodeAreaUtils;
import org.exbin.bined.CodeType;
import org.exbin.bined.EditMode;
import org.exbin.bined.EditOperation;
import org.exbin.bined.SelectionRange;
import org.exbin.bined.highlight.swing.NonprintablesCodeAreaAssessor;
import org.exbin.bined.intellij.gui.BinEdToolbarPanel;
import org.exbin.bined.swing.CodeAreaSwingUtils;
import org.exbin.bined.swing.capability.ColorAssessorPainterCapable;
import org.exbin.bined.swing.section.SectCodeArea;
import org.exbin.framework.App;
import org.exbin.framework.action.api.ActiveComponent;
import org.exbin.framework.action.api.ComponentActivationListener;
import org.exbin.framework.action.api.DialogParentComponent;
import org.exbin.framework.action.api.clipboard.ClipboardController;
import org.exbin.framework.bined.BinEdDataComponent;
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
import org.exbin.framework.frame.api.FrameModuleApi;
import org.exbin.framework.language.api.LanguageModuleApi;
import org.exbin.framework.preferences.api.PreferencesModuleApi;
import org.exbin.framework.text.encoding.EncodingsHandler;
import org.exbin.framework.text.encoding.options.TextEncodingOptions;
import org.exbin.framework.utils.DesktopUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JViewport;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.ResourceBundle;

/**
 * Data dialog for binary data.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public final class DataDialog extends DialogWrapper {

    private final java.util.ResourceBundle resourceBundle = App.getModule(LanguageModuleApi.class).getBundle(DataDialog.class);

    private final JPanel panel;
    private BinEdToolbarPanel toolbarPanel = new BinEdToolbarPanel();
    private BinaryStatusPanel statusPanel = new BinaryStatusPanel();
    private BinaryStatusApi binaryStatus;
    private final BinEdDocumentView editorComponent;
    private final SetDataListener setDataListener;
    private long documentOriginalSize = 0;
    private final boolean editable;

    public DataDialog(Project project, @Nullable BinaryData binaryData) {
        this(project, null, binaryData);
    }
    public DataDialog(Project project, @Nullable SetDataListener setDataListener, @Nullable BinaryData binaryData) {
        super(project, false);
        this.setDataListener = setDataListener;
        editable = binaryData instanceof EditableBinaryData || (setDataListener != null);

        setModal(false);
        setCancelButtonText(resourceBundle.getString("cancelButton.text"));
        setOKButtonText(resourceBundle.getString("setButton.text"));
        getOKAction().setEnabled(false);
        setOKActionEnabled(editable);
        setCrossClosesWindow(true);

        panel = new JPanel(new BorderLayout());
        editorComponent = new BinEdDocumentView();
        BinedModule binedModule = App.getModule(BinedModule.class);
        BinedViewerModule binedViewerModule = App.getModule(BinedViewerModule.class);
        BinEdFileManager fileManager = binedModule.getFileManager();
        BinEdComponentPanel componentPanel = (BinEdComponentPanel) editorComponent.getComponent();
        fileManager.initComponentPanel(componentPanel);

        PreferencesModuleApi preferencesModule = App.getModule(PreferencesModuleApi.class);

        SectCodeArea codeArea = editorComponent.getCodeArea();
        BinEdDataComponent binEdDataComponent = new BinEdDataComponent(codeArea);
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

        CodeAreaPopupMenuHandler codeAreaPopupMenuHandler =
                binedModule.createCodeAreaPopupMenuHandler(BinedModule.PopupMenuVariant.NORMAL);
        codeArea.setComponentPopupMenu(new JPopupMenu() {
            @Override
            public void show(Component invoker, int x, int y) {
                FrameModuleApi frameModule = App.getModule(FrameModuleApi.class);
                ComponentActivationListener componentActivationListener =
                        frameModule.getFrameHandler().getComponentActivationListener();

                componentActivationListener.updated(ActiveComponent.class, binEdDataComponent);
                componentActivationListener.updated(DialogParentComponent.class, () -> codeArea);
                componentActivationListener.updated(ClipboardController.class, binEdDataComponent);

                String popupMenuId = "DataDialog.popup";
                int clickedX = x;
                int clickedY = y;
                if (invoker instanceof JViewport) {
                    clickedX += invoker.getParent().getX();
                    clickedY += invoker.getParent().getY();
                }
                JPopupMenu popupMenu = codeAreaPopupMenuHandler.createPopupMenu(codeArea, popupMenuId, clickedX, clickedY);
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
        statusPanel.setController(new BinaryStatusController());
        statusPanel.loadFromOptions(new StatusOptions(preferencesModule.getAppPreferences()));
        registerBinaryStatus(statusPanel);

        panel.add(toolbarPanel, BorderLayout.NORTH);
        panel.add(statusPanel, BorderLayout.SOUTH);
        panel.add(editorComponent.getComponent(), BorderLayout.CENTER);
        panel.revalidate();
        panel.repaint();

        editorComponent.setContentData(binaryData);
        long dataSize = editorComponent.getContentData().getDataSize();
        documentOriginalSize = dataSize;
        statusPanel.setCurrentDocumentSize(dataSize, documentOriginalSize);
        if (!editable) {
            codeArea.setEditMode(EditMode.READ_ONLY);
        }
        init();
    }

    @Nonnull
    public ResourceBundle getResourceBundle() {
        return resourceBundle;
    }

    @Override
    protected void doOKAction() {
        super.doOKAction();

        if (setDataListener != null) {
            setDataListener.setData(editorComponent.getContentData());
        }
    }

    @Nonnull
    @Override
    protected Action[] createActions() {
        if (editable) {
            return new Action[] { getOKAction(), getCancelAction() };
        }

        return new Action[] { getCancelAction() };
    }

    @Nonnull
    @Override
    public JComponent getPreferredFocusedComponent() {
        return editorComponent.getCodeArea();
    }

    @Nonnull
    @Override
    protected String getDimensionServiceKey() {
        return "#org.exbin.bined.intellij.debug.ViewBinaryAction";
    }

    @Nonnull
    @Override
    protected JComponent createCenterPanel() {
        BorderLayoutPanel centerPanel = JBUI.Panels.simplePanel();
        centerPanel.setPreferredSize(JBUI.size(600, 400));
        centerPanel.add(panel, BorderLayout.CENTER);
        return centerPanel;
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

    public interface SetDataListener {

        void setData(@Nullable BinaryData data);
    }

    @ParametersAreNonnullByDefault
    private class BinaryStatusController implements BinaryStatusPanel.Controller, BinaryStatusPanel.EncodingsController, BinaryStatusPanel.MemoryModeController {
        @Override
        public void changeEditOperation(EditOperation editOperation) {
            editorComponent.getCodeArea().setEditOperation(editOperation);
        }

        @Override
        public void changeCursorPosition() {
            GoToPositionAction action = new GoToPositionAction();
            action.setCodeArea(editorComponent.getCodeArea());
            action.actionPerformed(null);
        }

        @Override
        public void cycleNextEncoding() {
            BinedViewerModule binedViewerModule = App.getModule(BinedViewerModule.class);
            EncodingsHandler encodingsHandler = binedViewerModule.getEncodingsHandler();
            encodingsHandler.cycleNextEncoding();
        }

        @Override
        public void cyclePreviousEncoding() {
            BinedViewerModule binedViewerModule = App.getModule(BinedViewerModule.class);
            EncodingsHandler encodingsHandler = binedViewerModule.getEncodingsHandler();
            encodingsHandler.cyclePreviousEncoding();
        }

        @Override
        public void encodingsPopupEncodingsMenu(MouseEvent mouseEvent) {
            BinedViewerModule binedViewerModule = App.getModule(BinedViewerModule.class);
            EncodingsHandler encodingsHandler = binedViewerModule.getEncodingsHandler();
            encodingsHandler.popupEncodingsMenu(mouseEvent);
        }

        @Override
        public void changeMemoryMode(BinaryStatusApi.MemoryMode memoryMode) {
            // Ignore
        }
    }
}
