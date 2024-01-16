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
package org.exbin.framework.bined.objectdata.property.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionListener;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.event.ListSelectionEvent;
import org.exbin.auxiliary.binary_data.BinaryData;
import org.exbin.bined.EditMode;
import org.exbin.bined.swing.extended.ExtCodeArea;
import org.exbin.framework.bined.objectdata.ObjectValueConvertor;

import org.exbin.framework.utils.LanguageUtils;

/**
 * Inspection panel for instance component.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class InspectComponentPanel extends javax.swing.JPanel {

    private final java.util.ResourceBundle resourceBundle = LanguageUtils.getResourceBundleByClass(InspectComponentPanel.class);

    private final ComponentParentsListModel componentParentsListModel = new ComponentParentsListModel();
    private final PropertyTablePanel propertyTablePanel = new PropertyTablePanel();
    private final ObjectValueConvertor objectValueConvertor = new ObjectValueConvertor();
    private final ExtCodeArea codeArea = new ExtCodeArea();
    private Object component;
    private int currentlyShown = 0;
    private boolean showPropertiesOnly = false;

    public InspectComponentPanel() {
        initComponents();
        init();
    }

    private void init() {
        codeArea.setEditMode(EditMode.READ_ONLY);
        codeArea.setFocusTraversalKeysEnabled(false);
    }

    public void setComponent(Object component, @Nullable String componentName) {
        this.component = component;

        componentClassTextField.setText(componentName == null ? "-" : componentName);
        parentsList.getSelectionModel().addListSelectionListener((ListSelectionEvent e) -> {
            int selectedIndex = parentsList.getSelectedIndex();
            if (selectedIndex >= 0 && currentlyShown != selectedIndex) {
                currentlyShown = selectedIndex;
                Object itemObject = componentParentsListModel.getItemObject(currentlyShown);
                propertyTablePanel.setObject(itemObject);
            }
        });

        updateParentsList();

        propertyTablePanel.setObject(component);
        instanceSplitPane.setBottomComponent(propertyTablePanel);

        boolean binarySupported = false;
        JComponent binaryComponent = null;
        if (!showPropertiesOnly) {
            try {
                Object binaryData = objectValueConvertor.process(component).orElse(null);
                if (binaryData != null) {
                    codeArea.setContentData((BinaryData) binaryData);
                    binaryComponent = codeArea;
                    binarySupported = true;
                }
            } catch (Throwable ex) {
                // TODO: Throws NoClassDef even when BinEd plugin present
                // cannot access BinEd plugin
                binarySupported = false;
            }
        }

        Object basicType = PropertyTableItem.convertToBasicType(component);
        if (!showPropertiesOnly && basicType instanceof String || binarySupported) {
            JTabbedPane tabbedPane = new JTabbedPane();
            tabbedPane.add(resourceBundle.getString("propertyTab.instance"), instanceSplitPane);
            if (basicType instanceof String) {
                JTextArea textArea = new JTextArea((String) basicType);
                textArea.setEditable(false);
                tabbedPane.add(resourceBundle.getString("propertyTab.text"), textArea);
            }
            if (binarySupported) {
                tabbedPane.add(resourceBundle.getString("propertyTab.binary"), binaryComponent);
            }
            mainPanel.add(tabbedPane, BorderLayout.CENTER);
        } else {
            mainPanel.add(instanceSplitPane, BorderLayout.CENTER);
        }
    }

    private void updateParentsList() {
        componentParentsListModel.clear();

        componentParentsListModel.add(getClassName(component.getClass()), component);
        if (component instanceof Component) {
            Component comp = ((Component) component).getParent();
            while (comp != null) {
                String name = getClassName(comp.getClass());
                componentParentsListModel.add(name, comp);
                comp = comp.getParent();
            }
        }
    }

    @Nonnull
    private String getClassName(Class<?> clazz) {
        String name = clazz.getCanonicalName();
        if (name == null) {
            name = clazz.getTypeName();
            if (name == null) {
                name = "<anonymous>";
            }
        }

        return name;
    }

    public boolean isShowPropertiesOnly() {
        return showPropertiesOnly;
    }

    public void setShowPropertiesOnly(boolean showPropertiesOnly) {
        this.showPropertiesOnly = showPropertiesOnly;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        instanceSplitPane = new javax.swing.JSplitPane();
        parentsListScrollPane = new javax.swing.JScrollPane();
        parentsList = new javax.swing.JList<>();
        mainPanel = new javax.swing.JPanel();
        componentLabel = new javax.swing.JLabel();
        componentClassTextField = new javax.swing.JTextField();
        closeButton = new javax.swing.JButton();
        showStaticFieldsCheckBox = new javax.swing.JCheckBox();

        instanceSplitPane.setDividerLocation(230);

        parentsList.setModel(componentParentsListModel);
        parentsList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        parentsListScrollPane.setViewportView(parentsList);

        instanceSplitPane.setLeftComponent(parentsListScrollPane);

        mainPanel.setLayout(new java.awt.BorderLayout());

        componentLabel.setText(resourceBundle.getString("componentLabel.text")); // NOI18N

        componentClassTextField.setEditable(false);

        closeButton.setText(resourceBundle.getString("closeButton.text")); // NOI18N

        showStaticFieldsCheckBox.setText(resourceBundle.getString("showStaticFieldsCheckBox.text")); // NOI18N
        showStaticFieldsCheckBox.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                showStaticFieldsCheckBoxStateChanged(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(mainPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(componentLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(componentClassTextField))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(showStaticFieldsCheckBox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 535, Short.MAX_VALUE)
                        .addComponent(closeButton)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(componentLabel)
                    .addComponent(componentClassTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(mainPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 367, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(closeButton)
                    .addComponent(showStaticFieldsCheckBox))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void showStaticFieldsCheckBoxStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_showStaticFieldsCheckBoxStateChanged
        propertyTablePanel.setShowStaticFields(showStaticFieldsCheckBox.isSelected());
        Object itemObject = componentParentsListModel.getItemObject(currentlyShown);
        propertyTablePanel.setObject(itemObject);
    }//GEN-LAST:event_showStaticFieldsCheckBoxStateChanged


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton closeButton;
    private javax.swing.JTextField componentClassTextField;
    private javax.swing.JLabel componentLabel;
    private javax.swing.JSplitPane instanceSplitPane;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JList<String> parentsList;
    private javax.swing.JScrollPane parentsListScrollPane;
    private javax.swing.JCheckBox showStaticFieldsCheckBox;
    // End of variables declaration//GEN-END:variables

    public void setCloseActionListener(ActionListener listener) {
        closeButton.addActionListener(listener);
    }
}
