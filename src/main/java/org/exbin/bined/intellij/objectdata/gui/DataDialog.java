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
package org.exbin.bined.intellij.objectdata.gui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.components.BorderLayoutPanel;
import org.exbin.auxiliary.binary_data.BinaryData;
import org.exbin.auxiliary.binary_data.EditableBinaryData;
import org.exbin.bined.CodeAreaUtils;
import org.exbin.bined.CodeType;
import org.exbin.bined.EditMode;
import org.exbin.bined.highlight.swing.NonprintablesCodeAreaAssessor;
import org.exbin.bined.intellij.gui.BinEdToolbarPanel;
import org.exbin.bined.jaguif.component.BinEdDataComponent;
import org.exbin.bined.jaguif.component.BinedComponentModule;
import org.exbin.bined.jaguif.component.gui.BinEdComponentPanel;
import org.exbin.bined.jaguif.document.BinEdFileManager;
import org.exbin.bined.jaguif.document.BinedDocumentModule;
import org.exbin.bined.jaguif.viewer.BinedViewerModule;
import org.exbin.bined.jaguif.viewer.settings.BinaryEncodingSettingsApplier;
import org.exbin.bined.swing.CodeAreaSwingUtils;
import org.exbin.bined.swing.capability.ColorAssessorPainterCapable;
import org.exbin.bined.swing.section.SectCodeArea;
import org.exbin.jaguif.App;
import org.exbin.jaguif.action.api.ActionConsts;
import org.exbin.jaguif.action.api.ActionContextChange;
import org.exbin.jaguif.context.ActiveContextManager;
import org.exbin.jaguif.context.api.ActiveContextManagement;
import org.exbin.jaguif.context.api.ContextChangeRegistration;
import org.exbin.jaguif.context.api.ContextComponent;
import org.exbin.jaguif.context.api.ContextModuleApi;
import org.exbin.jaguif.context.api.ContextRegistration;
import org.exbin.jaguif.context.api.ContextUpdateManagement;
import org.exbin.jaguif.context.api.StateUpdateType;
import org.exbin.jaguif.language.api.LanguageModuleApi;
import org.exbin.jaguif.options.api.OptionsModuleApi;
import org.exbin.jaguif.options.settings.api.OptionsSettingsModuleApi;
import org.exbin.jaguif.statusbar.api.StatusBar;
import org.exbin.jaguif.statusbar.api.StatusBarModuleApi;
import org.exbin.jaguif.text.encoding.CharsetEncodingState;
import org.exbin.jaguif.text.encoding.ContextEncoding;
import org.exbin.jaguif.text.encoding.EncodingsManager;
import org.exbin.jaguif.utils.DesktopUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.ResourceBundle;

/**
 * Data dialog for binary data.
 */
@ParametersAreNonnullByDefault
public final class DataDialog extends DialogWrapper {

    private final java.util.ResourceBundle resourceBundle = App.getModule(LanguageModuleApi.class).getBundle(DataDialog.class);

    private final JPanel panel;
    private BinEdToolbarPanel toolbarPanel = new BinEdToolbarPanel();
    private StatusBar statusBar;
    private final BinEdDataComponent dataComponent;
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
        BinEdComponentPanel componentPanel = new BinEdComponentPanel();
        dataComponent = new BinEdDataComponent(componentPanel);
        BinedDocumentModule binedDocumentModule = App.getModule(BinedDocumentModule.class);
        BinedViewerModule binedViewerModule = App.getModule(BinedViewerModule.class);
        BinEdFileManager fileManager = binedDocumentModule.getFileManager();
        fileManager.initDataComponent(dataComponent);

        OptionsModuleApi optionsModule = App.getModule(OptionsModuleApi.class);

        SectCodeArea codeArea = (SectCodeArea) dataComponent.getCodeArea();
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

        BinedComponentModule binedComponentModule = App.getModule(BinedComponentModule.class);
        JPopupMenu codeAreaPopupMenu = binedComponentModule.createCodeAreaPopupMenu();
        codeArea.setComponentPopupMenu(codeAreaPopupMenu);

        EncodingsManager encodingsManager = new EncodingsManager();
        encodingsManager.init();

        StatusBarModuleApi statusBarModule = App.getModule(StatusBarModuleApi.class);
        ContextRegistration contextRegistrator = null; // TODO
        statusBar = statusBarModule.createStatusBar(BinedComponentModule.BINARY_STATUS_BAR_ID, contextRegistrator);

        // TODO Temporary workaround for unfinished rework of actions
        {
            ContextModuleApi contextModule = App.getModule(ContextModuleApi.class);
            ActiveContextManagement contextManagement = new ActiveContextManager();
            ContextUpdateManagement updateManagement = contextModule.createContextUpdateManagement(contextManagement);
            contextManagement.changeActiveState(ContextComponent.class, dataComponent);
            contextManagement.changeActiveState(ContextEncoding.class, dataComponent);
            /* ContextRegistration contextRegistrar = contextModule.createContextRegistrator("", updateManagement, contextManagement);

            Action action = new AbstractAction() {
                public void actionPerformed(ActionEvent ae) {
                    // ignore
                }
            };
            action.putValue(ActionConsts.ACTION_CONTEXT_CHANGE, (ActionContextChange) (ContextChangeRegistration registrar) -> {
                registrar.registerStateUpdateListener(ContextEncoding.class, (ContextEncoding instance, StateUpdateType changeType) -> {
                    if (CharsetEncodingState.UpdateType.ENCODING.equals(changeType)) {
                        statusBar.updateEncodingState();
                    }
                });
            });
            contextRegistrar.registerActionContext(action);
            contextRegistrar.registerActionContext(encodingsManager.getToolsEncodingMenu().getAction());
            contextRegistrar.registerActionContext(encodingsManager.getManageEncodingsAction()); */
            dataComponent.setContextManager(contextManagement);

            OptionsSettingsModuleApi optionsSettingsModule = App.getModule(OptionsSettingsModuleApi.class);
            BinaryEncodingSettingsApplier settingsApplier = new BinaryEncodingSettingsApplier();
            settingsApplier.applySettings(
                    contextManagement,
                    optionsSettingsModule.getMainSettingsManager().getSettingsOptionsProvider());
        }

        panel.add(toolbarPanel, BorderLayout.NORTH);
        panel.add(statusBar.getComponent(), BorderLayout.SOUTH);
        panel.add(dataComponent.getComponent(), BorderLayout.CENTER);
        panel.revalidate();
        panel.repaint();

        dataComponent.getCodeArea().setContentData(binaryData);
        long dataSize = dataComponent.getCodeArea().getContentData().getDataSize();
        documentOriginalSize = dataSize;
        // TODO statusBar.getBinaryStatusPanel().setCurrentDocumentSize(dataSize, documentOriginalSize);
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
            setDataListener.setData(dataComponent.getCodeArea().getContentData());
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
        return dataComponent.getCodeArea();
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

    public interface SetDataListener {

        void setData(@Nullable BinaryData data);
    }
}
