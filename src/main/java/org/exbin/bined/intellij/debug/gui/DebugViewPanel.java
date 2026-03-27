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
import org.exbin.bined.CodeAreaUtils;
import org.exbin.bined.CodeType;
import org.exbin.bined.EditMode;
import org.exbin.bined.highlight.swing.NonprintablesCodeAreaAssessor;
import org.exbin.bined.intellij.debug.DebugViewDataProvider;
import org.exbin.bined.intellij.gui.BinEdToolbarPanel;
import org.exbin.bined.swing.CodeAreaSwingUtils;
import org.exbin.bined.swing.capability.ColorAssessorPainterCapable;
import org.exbin.bined.swing.section.SectCodeArea;
import org.exbin.framework.App;
import org.exbin.framework.action.api.ActionContextRegistration;
import org.exbin.framework.action.api.ActionManagement;
import org.exbin.framework.action.api.ActionModuleApi;
import org.exbin.framework.action.api.ContextComponent;
import org.exbin.framework.action.api.DialogParentComponent;
import org.exbin.framework.action.api.clipboard.ClipboardController;
import org.exbin.framework.bined.BinEdDataComponent;
import org.exbin.framework.bined.BinEdFileManager;
import org.exbin.framework.bined.BinaryFileDocument;
import org.exbin.framework.bined.BinaryStatus;
import org.exbin.framework.bined.BinedModule;
import org.exbin.framework.bined.gui.BinEdComponentPanel;
import org.exbin.framework.bined.gui.BinaryStatusPanel;
import org.exbin.framework.bined.handler.CodeAreaPopupMenuHandler;
import org.exbin.framework.bined.settings.CodeAreaStatusOptions;
import org.exbin.framework.bined.viewer.BinaryStatusController;
import org.exbin.framework.bined.viewer.BinedViewerModule;
import org.exbin.framework.context.ActiveContextManager;
import org.exbin.framework.context.api.ActiveContextManagement;
import org.exbin.framework.docking.api.DocumentDocking;
import org.exbin.framework.frame.api.FrameModuleApi;
import org.exbin.framework.language.api.LanguageModuleApi;
import org.exbin.framework.options.api.OptionsModuleApi;
import org.exbin.framework.options.settings.action.SettingsAction;
import org.exbin.framework.options.settings.api.OptionsSettingsModuleApi;
import org.exbin.framework.text.encoding.ContextEncoding;
import org.exbin.framework.text.encoding.EncodingsManager;
import org.exbin.framework.text.font.ContextFont;
import org.exbin.framework.utils.DesktopUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.AbstractAction;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JViewport;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel to show debug view.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class DebugViewPanel extends javax.swing.JPanel {

    protected final List<DebugViewDataProvider> providers = new ArrayList<>();
    protected int selectedProvider = 0;

    protected final JPanel panel;
    protected BinEdToolbarPanel toolbarPanel = new BinEdToolbarPanel();
    protected BinaryStatus binaryStatus;
    protected final BinEdDataComponent dataComponent;

    public DebugViewPanel() {
        panel = new JPanel(new BorderLayout());
        dataComponent = new BinEdDataComponent(new BinEdComponentPanel());

        initComponents();
        init();
    }

    private void init() {
        BinedModule binedModule = App.getModule(BinedModule.class);
        BinEdFileManager fileManager = binedModule.getFileManager();
        fileManager.initDataComponent(dataComponent);

        BinEdComponentPanel componentPanel = (BinEdComponentPanel) dataComponent.getComponent();
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
                NonprintablesCodeAreaAssessor nonprintablesCodeAreaAssessor =
                        CodeAreaSwingUtils.findColorAssessor(painter, NonprintablesCodeAreaAssessor.class);
                return CodeAreaUtils.requireNonNull(nonprintablesCodeAreaAssessor).isShowNonprintables();
            }

            @Override
            public void setShowNonprintables(boolean showNonprintables) {
                ColorAssessorPainterCapable painter = (ColorAssessorPainterCapable) codeArea.getPainter();
                NonprintablesCodeAreaAssessor nonprintablesCodeAreaAssessor =
                        CodeAreaSwingUtils.findColorAssessor(painter, NonprintablesCodeAreaAssessor.class);
                CodeAreaUtils.requireNonNull(nonprintablesCodeAreaAssessor).setShowNonprintables(showNonprintables);
            }

            @Override
            public void repaint() {
                codeArea.repaint();
            }
        });
        toolbarPanel.setOnlineHelpAction(createOnlineHelpAction());

        OptionsSettingsModuleApi optionsSettingsModule = App.getModule(OptionsSettingsModuleApi.class);
        SettingsAction settingsAction = (SettingsAction) optionsSettingsModule.createSettingsAction();
        FrameModuleApi frameModule = App.getModule(FrameModuleApi.class);
        settingsAction.setDialogParentComponent(() -> frameModule.getFrame());
        BinedViewerModule binedViewerModule = App.getModule(BinedViewerModule.class);

        AbstractAction wrapperAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                settingsAction.actionPerformed(e);
                toolbarPanel.applyFromCodeArea();
                binaryStatus.updateStatus();
            }
        };
        toolbarPanel.setOptionsAction(wrapperAction);

        CodeAreaPopupMenuHandler codeAreaPopupMenuHandler =
                binedModule.createCodeAreaPopupMenuHandler(BinedModule.PopupMenuVariant.NORMAL);
        codeArea.setComponentPopupMenu(new JPopupMenu() {
            @Override
            public void show(Component invoker, int x, int y) {
                FrameModuleApi frameModule = App.getModule(FrameModuleApi.class);
                ActiveContextManagement contextManager =
                        frameModule.getFrameHandler().getContextManager();

                contextManager.changeActiveState(ContextComponent.class, dataComponent);
                contextManager.changeActiveState(ContextFont.class, dataComponent);
                contextManager.changeActiveState(ContextEncoding.class, dataComponent);
                contextManager.changeActiveState(DialogParentComponent.class, () -> dataComponent.getCodeArea());
                contextManager.changeActiveState(ClipboardController.class, dataComponent);

                String popupMenuId = "DebugViewPanel.popup";
                int clickedX = x;
                int clickedY = y;
                if (invoker instanceof JViewport) {
                    clickedX += invoker.getParent().getX();
                    clickedY += invoker.getParent().getY();
                }

                JPopupMenu popupMenu =
                        codeAreaPopupMenuHandler.createPopupMenu(codeArea, popupMenuId, clickedX, clickedY);
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

        EncodingsManager encodingsManager = new EncodingsManager();
        encodingsManager.init();

        {
            ActionModuleApi actionModule = App.getModule(ActionModuleApi.class);
            ActiveContextManagement contextManagement = new ActiveContextManager();
            contextManagement.changeActiveState(ContextEncoding.class, dataComponent);
            ActionManagement actionManager = actionModule.createActionManager(contextManagement);
            ActionContextRegistration actionContextRegistrar = actionModule.createActionContextRegistrar(actionManager);
            actionContextRegistrar.registerActionContext(encodingsManager.getToolsEncodingMenu().getAction());
            actionContextRegistrar.registerActionContext(encodingsManager.getManageEncodingsAction());
        }

        binaryStatus = new BinaryStatus() {
            @Nullable
            @Override
            public BinaryFileDocument getActiveDocument() {
                return null;
            }

            @Nullable
            @Override
            public BinEdDataComponent getActiveComponent() {
                return dataComponent;
            }

            @Nullable
            @Override
            public DocumentDocking getActiveDocking() {
                return null;
            }
        };
        BinaryStatusPanel binaryStatusPanel = new BinaryStatusPanel();
        binaryStatus.setBinaryStatusPanel(binaryStatusPanel);
        binaryStatus.setBinaryStatusController(new BinaryStatusController(binaryStatus, encodingsManager));

        OptionsModuleApi optionsModule = App.getModule(OptionsModuleApi.class);
        binaryStatusPanel.loadFromOptions(new CodeAreaStatusOptions(optionsModule.getAppOptions()));
        // statusPanel.setMinimumSize(new Dimension(0, getMinimumSize().height));
        binaryStatus.attachCodeArea(dataComponent);

        panel.add(toolbarPanel, BorderLayout.NORTH);
        panel.add(binaryStatusPanel, BorderLayout.SOUTH);
        panel.add(dataComponent.getComponent(), BorderLayout.CENTER);
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
        dataComponent.getCodeArea().setContentData(data);
        long dataSize = data == null ? 0 : data.getDataSize();
        binaryStatus.getBinaryStatusPanel().setCurrentDocumentSize(dataSize, dataSize);
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
}
