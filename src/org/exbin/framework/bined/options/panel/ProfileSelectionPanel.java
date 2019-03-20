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
package org.exbin.framework.bined.options.panel;

import java.awt.BorderLayout;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JPanel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import org.exbin.framework.gui.utils.LanguageUtils;
import org.exbin.framework.gui.utils.WindowUtils;

/**
 * Wrapper panel for named profile.
 *
 * @version 0.2.0 2019/03/18
 * @author ExBin Project (http://exbin.org)
 */
@ParametersAreNonnullByDefault
public class ProfileSelectionPanel extends javax.swing.JPanel {

    private final java.util.ResourceBundle resourceBundle = LanguageUtils.getResourceBundleByClass(ProfileSelectionPanel.class);

    public ProfileSelectionPanel(JPanel profilePanel) {
        initComponents();
        addPanel(profilePanel);

        if (profilePanel instanceof ProfileListPanel) {
            final ProfileListPanel listPanel = (ProfileListPanel) profilePanel;
            listPanel.addProfileListPanelListener(new ListDataListener() {
                @Override
                public void intervalAdded(@Nonnull ListDataEvent event) {
                    List<String> profileNames = listPanel.getProfileNames();
                    int startIndex = event.getIndex0();
                    int endIndex = event.getIndex1();
                    for (int index = startIndex; index <= endIndex; index++) {
                        DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>) defaultProfileComboBox.getModel();
                        int selectedIndex = defaultProfileComboBox.getSelectedIndex();
                        String profileName = profileNames.get(index);
                        model.insertElementAt(profileName, index);
                        if (model.getSize() == 1) {
                            defaultProfileComboBox.setSelectedIndex(0);
                        } else if (index < selectedIndex) {
                            defaultProfileComboBox.setSelectedIndex(selectedIndex + 1);
                        }
                    }
                }

                @Override
                public void intervalRemoved(@Nonnull ListDataEvent event) {
                    int startIndex = event.getIndex0();
                    int endIndex = event.getIndex1();
                    for (int index = endIndex; index >= startIndex; index--) {
                        DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>) defaultProfileComboBox.getModel();
                        int selectedIndex = defaultProfileComboBox.getSelectedIndex();
                        model.removeElementAt(index);
                        if (selectedIndex == index && model.getSize() > 0) {
                            defaultProfileComboBox.setSelectedIndex(index == 0 ? 0 : index - 1);
                        }
                    }
                }

                @Override
                public void contentsChanged(@Nonnull ListDataEvent event) {
                    List<String> profileNames = listPanel.getProfileNames();
                    int startIndex = event.getIndex0();
                    int endIndex = event.getIndex1();
                    for (int index = startIndex; index <= endIndex; index++) {
                        DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>) defaultProfileComboBox.getModel();
                        boolean selected = defaultProfileComboBox.getSelectedIndex() == index;
                        model.removeElementAt(index);
                        model.insertElementAt(profileNames.get(index), index);
                        if (selected) {
                            defaultProfileComboBox.setSelectedIndex(index);
                        }
                    }
                }
            });
        }
    }

    private void addPanel(JPanel profilePanel) {
        add(profilePanel, BorderLayout.CENTER);
    }

    public void setProfiles(List<String> profiles) {
        DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>) defaultProfileComboBox.getModel();
        model.removeAllElements();
        profiles.forEach((profile) -> model.addElement(profile));
    }

    public void setDefaultProfile(int profileIndex) {
        if (profileIndex >= 0) {
            defaultProfileComboBox.setSelectedIndex(profileIndex);
        }
    }

    @Nonnull
    public int getDefaultProfile() {
        return defaultProfileComboBox.getSelectedIndex();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        defaultProfilePanel = new javax.swing.JPanel();
        defaultProfileLabel = new javax.swing.JLabel();
        defaultProfileComboBox = new javax.swing.JComboBox<>();

        setLayout(new java.awt.BorderLayout());

        defaultProfileLabel.setText(resourceBundle.getString("defaultProfileLabel.text")); // NOI18N

        javax.swing.GroupLayout defaultProfilePanelLayout = new javax.swing.GroupLayout(defaultProfilePanel);
        defaultProfilePanel.setLayout(defaultProfilePanelLayout);
        defaultProfilePanelLayout.setHorizontalGroup(
            defaultProfilePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(defaultProfilePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(defaultProfileLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(defaultProfileComboBox, 0, 1, Short.MAX_VALUE)
                .addContainerGap())
        );
        defaultProfilePanelLayout.setVerticalGroup(
            defaultProfilePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(defaultProfilePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(defaultProfilePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(defaultProfileLabel)
                    .addComponent(defaultProfileComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, Short.MAX_VALUE))
        );

        add(defaultProfilePanel, java.awt.BorderLayout.NORTH);
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Test method for this panel.
     *
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        WindowUtils.invokeDialog(new ProfileSelectionPanel(new JPanel()));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<String> defaultProfileComboBox;
    private javax.swing.JLabel defaultProfileLabel;
    private javax.swing.JPanel defaultProfilePanel;
    // End of variables declaration//GEN-END:variables
}
