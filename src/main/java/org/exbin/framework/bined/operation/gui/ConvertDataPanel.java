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
package org.exbin.framework.bined.operation.gui;

import java.awt.Component;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.JViewport;
import org.exbin.auxiliary.binary_data.ByteArrayEditableData;
import org.exbin.bined.EditMode;
import org.exbin.bined.swing.CodeAreaCore;
import org.exbin.bined.swing.extended.ExtCodeArea;
import org.exbin.framework.utils.LanguageUtils;
import org.exbin.framework.utils.WindowUtils;
import org.exbin.framework.bined.operation.api.ConvertDataMethod;
import org.exbin.framework.bined.handler.CodeAreaPopupMenuHandler;

/**
 * Convert data panel.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class ConvertDataPanel extends javax.swing.JPanel {

    private static final String POPUP_MENU_POSTFIX = ".convertDataPanel";

    private final java.util.ResourceBundle resourceBundle = LanguageUtils.getResourceBundleByClass(ConvertDataPanel.class);

    private Controller controller;
    private ExtCodeArea previewCodeArea = new ExtCodeArea();
    private CodeAreaPopupMenuHandler codeAreaPopupMenuHandler;
    private ConvertDataMethod activeMethod;
    private Component activeComponent;

    public ConvertDataPanel() {
        initComponents();
        init();
    }

    private void init() {
        optionsList.setModel(new DefaultListModel<>());
        optionsList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, @Nullable Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if (value == null) {
                    return super.getListCellRendererComponent(list, null, index, isSelected, cellHasFocus);
                }
                return super.getListCellRendererComponent(list, ((ConvertDataMethod) value).getName(), index, isSelected, cellHasFocus);
            }
        });
        optionsList.addListSelectionListener((e) -> {
            activeMethod = optionsList.getSelectedValue();
            activeComponent = activeMethod != null ? activeMethod.getComponent() : null;
            ByteArrayEditableData previewBinaryData = (ByteArrayEditableData) previewCodeArea.getContentData();
            previewBinaryData.clear();
            if (controller != null) {
                controller.updatePreviewData(previewCodeArea);
            }
            componentScrollPane.getViewport().setView(activeComponent);
            if (activeMethod != null) {
                try {
                    activeMethod.initFocus(activeComponent);
                } catch (Throwable ex) {
                    Logger.getLogger(ConvertDataPanel.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        previewCodeArea.setContentData(new ByteArrayEditableData());
        previewCodeArea.setEditMode(EditMode.READ_ONLY);
        previewCodeArea.setFocusTraversalKeysEnabled(false);
        previewPanel.add(previewCodeArea);
    }

    @Nonnull
    public ResourceBundle getResourceBundle() {
        return resourceBundle;
    }

    public void setController(Controller controller) {
        this.controller = controller;
    }

    public void setComponents(List<ConvertDataMethod> dataComponents) {
        DefaultListModel<ConvertDataMethod> listModel = (DefaultListModel<ConvertDataMethod>) optionsList.getModel();
        for (ConvertDataMethod dataComponent : dataComponents) {
            listModel.addElement(dataComponent);
        }
    }

    @Nonnull
    public Optional<ConvertDataMethod> getActiveMethod() {
        return Optional.ofNullable(activeMethod);
    }

    @Nonnull
    public Optional<Component> getActiveComponent() {
        return Optional.ofNullable(activeComponent);
    }

    public void setCodeAreaPopupMenuHandler(CodeAreaPopupMenuHandler codeAreaPopupMenuHandler) {
        this.codeAreaPopupMenuHandler = codeAreaPopupMenuHandler;
        if (previewCodeArea != null) {
            attachPopupMenu();
        }
    }

    private void attachPopupMenu() {
        previewCodeArea.setComponentPopupMenu(new JPopupMenu() {
            @Override
            public void show(@Nonnull Component invoker, int x, int y) {
                int clickedX = x;
                int clickedY = y;
                if (invoker instanceof JViewport) {
                    clickedX += ((JViewport) invoker).getParent().getX();
                    clickedY += ((JViewport) invoker).getParent().getY();
                }
                JPopupMenu popupMenu = codeAreaPopupMenuHandler.createPopupMenu(previewCodeArea, POPUP_MENU_POSTFIX, clickedX, clickedY);
                popupMenu.show(invoker, x, y);
                codeAreaPopupMenuHandler.dropPopupMenu(POPUP_MENU_POSTFIX);
            }
        });
    }

    public void detachMenu() {
        codeAreaPopupMenuHandler.dropPopupMenu(POPUP_MENU_POSTFIX);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        methodLabel = new javax.swing.JLabel();
        optionsScrollPane = new javax.swing.JScrollPane();
        optionsList = new javax.swing.JList<>();
        splitPane = new javax.swing.JSplitPane();
        componentScrollPane = new javax.swing.JScrollPane();
        previewPanel = new javax.swing.JPanel();

        methodLabel.setText(resourceBundle.getString("methodLabel.text")); // NOI18N

        optionsList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        optionsScrollPane.setViewportView(optionsList);

        splitPane.setDividerLocation(400);
        splitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        splitPane.setLeftComponent(componentScrollPane);

        previewPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceBundle.getString("previewPanel.border.title"))); // NOI18N
        previewPanel.setLayout(new javax.swing.BoxLayout(previewPanel, javax.swing.BoxLayout.X_AXIS));
        splitPane.setBottomComponent(previewPanel);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(methodLabel)
                    .addComponent(optionsScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 276, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(splitPane, javax.swing.GroupLayout.DEFAULT_SIZE, 763, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(splitPane, javax.swing.GroupLayout.DEFAULT_SIZE, 619, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(methodLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(optionsScrollPane)))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    public void initFocus() {
        // TODO
    }

    public void selectActiveMethod(@Nullable ConvertDataMethod method) {
        DefaultListModel<ConvertDataMethod> listModel = (DefaultListModel<ConvertDataMethod>) optionsList.getModel();
        if (method == null && !listModel.isEmpty()) {
            optionsList.setSelectedIndex(0);
        } else {
            int methodIndex = listModel.indexOf(method);
            if (methodIndex >= 0) {
                optionsList.setSelectedIndex(methodIndex);
            }
        }
    }

    /**
     * Test method for this panel.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        WindowUtils.invokeDialog(new ConvertDataPanel());
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane componentScrollPane;
    private javax.swing.JLabel methodLabel;
    private javax.swing.JList<org.exbin.framework.bined.operation.api.ConvertDataMethod> optionsList;
    private javax.swing.JScrollPane optionsScrollPane;
    private javax.swing.JPanel previewPanel;
    private javax.swing.JSplitPane splitPane;
    // End of variables declaration//GEN-END:variables

    public interface Controller {

        void updatePreviewData(@Nonnull CodeAreaCore previewCodeArea);
    }
}
