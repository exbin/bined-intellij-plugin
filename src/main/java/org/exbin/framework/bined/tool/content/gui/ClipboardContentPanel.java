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
package org.exbin.framework.bined.tool.content.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Image;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.AbstractAction;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JViewport;
import javax.swing.border.BevelBorder;
import org.exbin.auxiliary.binary_data.BinaryData;
import org.exbin.auxiliary.binary_data.EmptyBinaryData;
import org.exbin.framework.bined.tool.content.source.ClipboardFlavorBinaryData;
import org.exbin.framework.bined.objectdata.ObjectValueConvertor;
import org.exbin.framework.bined.objectdata.PageProviderBinaryData;
import org.exbin.framework.bined.objectdata.source.ByteBufferPageProvider;
import org.exbin.framework.bined.objectdata.source.CharBufferPageProvider;
import org.exbin.framework.bined.objectdata.source.ReaderPageProvider;
import org.exbin.framework.bined.objectdata.property.gui.InspectComponentPanel;
import org.exbin.framework.bined.handler.CodeAreaPopupMenuHandler;
import org.exbin.framework.utils.ClipboardUtils;
import org.exbin.framework.utils.LanguageUtils;
import org.exbin.framework.utils.WindowUtils;

/**
 * Clipboard content panel.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class ClipboardContentPanel extends javax.swing.JPanel {

    public static final String POPUP_MENU_POSTFIX = ".clipboardContentPanel";
    private final java.util.ResourceBundle resourceBundle = LanguageUtils.getResourceBundleByClass(ClipboardContentPanel.class);

    private DataFlavor[] dataFlavors;
    private final DefaultComboBoxModel<String> dataListModel = new DefaultComboBoxModel<>();
    private final List<Object> dataContents = new ArrayList<>();
    private final ObjectValueConvertor objectValueConvertor = new ObjectValueConvertor();

    private CodeAreaPopupMenuHandler codeAreaPopupMenuHandler;
    private JComponent currentDataComponent = null;
    private InspectComponentPanel inspectComponentPanel = new InspectComponentPanel();
    private AbstractAction openAsTabAction = null;
    private AbstractAction saveAsFileAction = null;

    public ClipboardContentPanel() {
        initComponents();
        init();
    }

    private void init() {
        dataCodeArea.setBorder(new BevelBorder(BevelBorder.LOWERED));
        flavorsList.setModel(new DataFlavorsListModel());
        flavorsList.addListSelectionListener((e) -> {
            int selectedIndex = flavorsList.getSelectedIndex();
            if (selectedIndex >= 0) {
                DataFlavor dataFlavor = dataFlavors[selectedIndex];
                presentableNameTextField.setText(dataFlavor.getHumanPresentableName());
                stringTypeTextField.setText(dataFlavor.toString());
                mimeTypeTextField.setText(dataFlavor.getMimeType());
                primaryMimeTypeTextField.setText(dataFlavor.getPrimaryType());
                subMimeTypeTextField.setText(dataFlavor.getSubType());
                representationClassTextField.setText(dataFlavor.getRepresentationClass().getCanonicalName());

                dataListModel.removeAllElements();
                dataContents.clear();
                dataCodeArea.setContentData(EmptyBinaryData.INSTANCE);

                Clipboard clipboard = ClipboardUtils.getClipboard();
                try {
                    Object data = clipboard.getData(dataFlavor);

                    if (data != null) {
                        Optional<BinaryData> convBinaryData = objectValueConvertor.process(data);
                        Object contentData = null;
                        if (convBinaryData.isPresent()) {
                            contentData = convBinaryData.get();
                        } else {
                            if (data instanceof InputStream) {
                                try {
                                    ClipboardFlavorBinaryData cliboardFlavorBinaryData = new ClipboardFlavorBinaryData();
                                    cliboardFlavorBinaryData.setDataFlavor(dataFlavor);
                                    contentData = cliboardFlavorBinaryData;
                                } catch (ClassNotFoundException | UnsupportedFlavorException ex) {
                                }
                            }
                            if (data instanceof ByteBuffer) {
                                contentData = new PageProviderBinaryData(new ByteBufferPageProvider((ByteBuffer) data));
                            } else if (data instanceof CharBuffer) {
                                contentData = new PageProviderBinaryData(new CharBufferPageProvider((CharBuffer) data));
                            } else if (data instanceof Reader) {
                                contentData = new PageProviderBinaryData(new ReaderPageProvider(() -> {
                                    try {
                                        return (Reader) clipboard.getData(dataFlavor);
                                    } catch (UnsupportedFlavorException | IOException ex) {
                                        throw new IllegalStateException("Unable to get clipboard data");
                                    }
                                }));
                            }
                        }

                        if (contentData != null) {
                            dataContents.add(contentData);
                            dataListModel.addElement(java.text.MessageFormat.format(resourceBundle.getString("modelType.fromClass"), new Object[]{data.getClass().getCanonicalName()}));
                        }

                        if (data instanceof List<?>) {
                            dataContents.add(data);
                            dataListModel.addElement(java.text.MessageFormat.format(resourceBundle.getString("modelType.list"), new Object[]{data.getClass().getCanonicalName()}));
                        } else if (data instanceof String) {
                            dataContents.add(data);
                            dataListModel.addElement(java.text.MessageFormat.format(resourceBundle.getString("modelType.text"), new Object[]{data.getClass().getCanonicalName()}));
                        } else if (data instanceof Image) {
                            dataContents.add(data);
                            dataListModel.addElement(java.text.MessageFormat.format(resourceBundle.getString("modelType.image"), new Object[]{data.getClass().getCanonicalName()}));
                        }

                        dataContents.add(new PropertyClass(data));
                        dataListModel.addElement(java.text.MessageFormat.format(resourceBundle.getString("modelType.properties"), new Object[]{data.getClass().getCanonicalName()}));
                    }
                } catch (UnsupportedFlavorException | IOException ex) {
                }

                if (dataContents.isEmpty()) {
                    ClipboardFlavorBinaryData binaryData = new ClipboardFlavorBinaryData();
                    try {
                        binaryData.convertDataFlavor(dataFlavor);
                        dataContents.add(binaryData);
                        dataListModel.addElement(java.text.MessageFormat.format(resourceBundle.getString("modelType.requestedConversion"), new Object[]{}));
                    } catch (ClassNotFoundException | UnsupportedFlavorException ex) {
                    }
                }
            } else {
                presentableNameTextField.setText("");
                stringTypeTextField.setText("");
                mimeTypeTextField.setText("");
                primaryMimeTypeTextField.setText("");
                subMimeTypeTextField.setText("");
                representationClassTextField.setText("");

                dataListModel.removeAllElements();
                dataContents.clear();
            }
        });
        dataComboBox.setModel(dataListModel);
        dataComboBox.addItemListener((e) -> {
            if (currentDataComponent != null) {
                if (currentDataComponent == binaryDataPanel) {
                    dataCodeArea.setContentData(EmptyBinaryData.INSTANCE);
                } else if (currentDataComponent == dataListScrollPane) {
                    DefaultListModel<String> listModel = new DefaultListModel<>();
                    dataList.setModel(listModel);
                } else if (currentDataComponent == textDataScrollPane) {
                    textDataTextArea.setText("");
                } else if (currentDataComponent == imageScrollPane) {
                    imageLabel.setIcon(null);
                } else if (currentDataComponent == inspectComponentPanel) {
                    inspectComponentPanel.setComponent("", null);
                }

                dataContentPanel.remove(currentDataComponent);
                currentDataComponent = null;
            }

            int selectedIndex = dataComboBox.getSelectedIndex();
            if (selectedIndex >= 0) {
                Object dataComponent = dataContents.get(selectedIndex);
                if (dataComponent instanceof BinaryData) {
                    dataCodeArea.setContentData((BinaryData) dataComponent);
                    currentDataComponent = binaryDataPanel;
                } else if (dataComponent instanceof List<?>) {
                    List<?> listComponent = (List<?>) dataComponent;
                    DefaultListModel<String> listModel = new DefaultListModel<>();
                    for (int i = 0; i < listComponent.size(); i++) {
                        listModel.add(i, listComponent.get(i).toString());
                    }
                    dataList.setModel(listModel);
                    currentDataComponent = dataListScrollPane;
                } else if (dataComponent instanceof String) {
                    textDataTextArea.setText((String) dataComponent);
                    currentDataComponent = textDataScrollPane;
                } else if (dataComponent instanceof Image) {
                    imageLabel.setIcon(new ImageIcon((Image) dataComponent));
                    currentDataComponent = imageScrollPane;
                } else if (dataComponent instanceof PropertyClass) {
                    inspectComponentPanel.setComponent(((PropertyClass) dataComponent).classInst, null);
                    currentDataComponent = inspectComponentPanel;
                }
            }

            if (currentDataComponent == null) {
                currentDataComponent = noFlavorSelectedLabel;
            }

            dataContentPanel.add(currentDataComponent, BorderLayout.CENTER);
            dataContentPanel.revalidate();
            dataContentPanel.repaint();
        });
        currentDataComponent = noFlavorSelectedLabel;
        inspectComponentPanel.setShowPropertiesOnly(true);
    }

    public void loadFromClipboard() {
        dataFlavors = ClipboardUtils.getClipboard().getAvailableDataFlavors();
        ((DataFlavorsListModel) flavorsList.getModel()).setDataFlavors(dataFlavors);
    }

    @Nonnull
    public ResourceBundle getResourceBundle() {
        return resourceBundle;
    }

    @Nonnull
    public Optional<AbstractAction> getOpenAsTabAction() {
        return Optional.ofNullable(openAsTabAction);
    }

    public void setOpenAsTabAction(@Nullable AbstractAction openAsTabAction) {
        this.openAsTabAction = openAsTabAction;
        openAsTabButton.setEnabled(openAsTabAction != null);
    }

    @Nonnull
    public Optional<AbstractAction> getSaveAsFileAction() {
        return Optional.ofNullable(saveAsFileAction);
    }

    public void setSaveAsFileAction(@Nullable AbstractAction saveAsFileAction) {
        this.saveAsFileAction = saveAsFileAction;
        saveAsFileButton.setEnabled(saveAsFileAction != null);
    }

    @Nonnull
    public Optional<BinaryData> getContentBinaryData() {
        BinaryData contentData = dataCodeArea.getContentData();
        return Optional.ofNullable(contentData instanceof EmptyBinaryData ? null : contentData);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        dataListScrollPane = new javax.swing.JScrollPane();
        dataList = new javax.swing.JList<>();
        textDataScrollPane = new javax.swing.JScrollPane();
        textDataTextArea = new javax.swing.JTextArea();
        binaryDataPanel = new javax.swing.JPanel();
        dataCodeArea = new org.exbin.bined.swing.extended.ExtCodeArea();
        saveAsFileButton = new javax.swing.JButton();
        openAsTabButton = new javax.swing.JButton();
        imageScrollPane = new javax.swing.JScrollPane();
        imageLabel = new javax.swing.JLabel();
        availableFlavorsLabel = new javax.swing.JLabel();
        flavorsScrollPane = new javax.swing.JScrollPane();
        flavorsList = new javax.swing.JList<>();
        flavorContentPanel = new javax.swing.JPanel();
        flavorPanel = new javax.swing.JPanel();
        presentableNameLabel = new javax.swing.JLabel();
        presentableNameTextField = new javax.swing.JTextField();
        representationClassLabel = new javax.swing.JLabel();
        representationClassTextField = new javax.swing.JTextField();
        stringTypeLabel = new javax.swing.JLabel();
        stringTypeTextField = new javax.swing.JTextField();
        mimeTypeLabel = new javax.swing.JLabel();
        mimeTypeTextField = new javax.swing.JTextField();
        primaryMimeTypeLabel = new javax.swing.JLabel();
        primaryMimeTypeTextField = new javax.swing.JTextField();
        subMimeTypeLabel = new javax.swing.JLabel();
        subMimeTypeTextField = new javax.swing.JTextField();
        dataLabel = new javax.swing.JLabel();
        dataComboBox = new javax.swing.JComboBox<>();
        dataContentPanel = new javax.swing.JPanel();
        noFlavorSelectedLabel = new javax.swing.JLabel();

        dataListScrollPane.setViewportView(dataList);

        textDataTextArea.setEditable(false);
        textDataScrollPane.setViewportView(textDataTextArea);

        saveAsFileButton.setText(resourceBundle.getString("saveAsFileButton.text")); // NOI18N
        saveAsFileButton.setEnabled(false);
        saveAsFileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveAsFileButtonActionPerformed(evt);
            }
        });

        openAsTabButton.setText(resourceBundle.getString("openAsTabButton.text")); // NOI18N
        openAsTabButton.setEnabled(false);
        openAsTabButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openAsTabButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout binaryDataPanelLayout = new javax.swing.GroupLayout(binaryDataPanel);
        binaryDataPanel.setLayout(binaryDataPanelLayout);
        binaryDataPanelLayout.setHorizontalGroup(
            binaryDataPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, binaryDataPanelLayout.createSequentialGroup()
                .addGap(0, 735, Short.MAX_VALUE)
                .addComponent(saveAsFileButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(openAsTabButton))
            .addComponent(dataCodeArea, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        binaryDataPanelLayout.setVerticalGroup(
            binaryDataPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, binaryDataPanelLayout.createSequentialGroup()
                .addComponent(dataCodeArea, javax.swing.GroupLayout.DEFAULT_SIZE, 583, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(binaryDataPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(openAsTabButton)
                    .addComponent(saveAsFileButton)))
        );

        imageLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        imageScrollPane.setViewportView(imageLabel);

        availableFlavorsLabel.setText(resourceBundle.getString("availableFlavorsLabel.text")); // NOI18N

        flavorsList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        flavorsScrollPane.setViewportView(flavorsList);

        flavorContentPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceBundle.getString("flavorContentPanel.border.title"))); // NOI18N
        flavorContentPanel.setLayout(new java.awt.BorderLayout());

        presentableNameLabel.setText(resourceBundle.getString("presentableNameLabel.text")); // NOI18N

        presentableNameTextField.setEditable(false);

        representationClassLabel.setText(resourceBundle.getString("representationClassLabel.text")); // NOI18N
        representationClassLabel.setToolTipText("");

        representationClassTextField.setEditable(false);

        stringTypeLabel.setText(resourceBundle.getString("stringTypeLabel.text")); // NOI18N

        stringTypeTextField.setEditable(false);

        mimeTypeLabel.setText(resourceBundle.getString("mimeTypeLabel.text")); // NOI18N

        mimeTypeTextField.setEditable(false);

        primaryMimeTypeLabel.setText(resourceBundle.getString("primaryMimeTypeLabel.text")); // NOI18N

        primaryMimeTypeTextField.setEditable(false);

        subMimeTypeLabel.setText(resourceBundle.getString("subMimeTypeLabel.text")); // NOI18N

        subMimeTypeTextField.setEditable(false);

        dataLabel.setText(resourceBundle.getString("dataLabel.text")); // NOI18N

        dataContentPanel.setLayout(new java.awt.BorderLayout());

        noFlavorSelectedLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        noFlavorSelectedLabel.setText(resourceBundle.getString("noFlavorSelectedLabel.text")); // NOI18N
        dataContentPanel.add(noFlavorSelectedLabel, java.awt.BorderLayout.CENTER);

        javax.swing.GroupLayout flavorPanelLayout = new javax.swing.GroupLayout(flavorPanel);
        flavorPanel.setLayout(flavorPanelLayout);
        flavorPanelLayout.setHorizontalGroup(
            flavorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(flavorPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(flavorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(stringTypeTextField)
                    .addComponent(mimeTypeTextField)
                    .addGroup(flavorPanelLayout.createSequentialGroup()
                        .addGroup(flavorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(primaryMimeTypeLabel)
                            .addComponent(primaryMimeTypeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 166, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(flavorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(flavorPanelLayout.createSequentialGroup()
                                .addComponent(subMimeTypeLabel)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(subMimeTypeTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 429, Short.MAX_VALUE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, flavorPanelLayout.createSequentialGroup()
                        .addGroup(flavorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(presentableNameLabel)
                            .addComponent(presentableNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 166, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(flavorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(representationClassTextField, javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, flavorPanelLayout.createSequentialGroup()
                                .addComponent(representationClassLabel)
                                .addGap(0, 0, Short.MAX_VALUE))))
                    .addComponent(dataContentPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(dataComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(flavorPanelLayout.createSequentialGroup()
                        .addGroup(flavorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(stringTypeLabel)
                            .addComponent(mimeTypeLabel)
                            .addComponent(dataLabel))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        flavorPanelLayout.setVerticalGroup(
            flavorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(flavorPanelLayout.createSequentialGroup()
                .addGroup(flavorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(flavorPanelLayout.createSequentialGroup()
                        .addComponent(presentableNameLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(presentableNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(flavorPanelLayout.createSequentialGroup()
                        .addComponent(representationClassLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(representationClassTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(stringTypeLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(stringTypeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(flavorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(flavorPanelLayout.createSequentialGroup()
                        .addComponent(mimeTypeLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(mimeTypeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(primaryMimeTypeLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(primaryMimeTypeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(flavorPanelLayout.createSequentialGroup()
                        .addComponent(subMimeTypeLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(subMimeTypeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(dataLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(dataComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(dataContentPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 93, Short.MAX_VALUE)
                .addContainerGap())
        );

        flavorContentPanel.add(flavorPanel, java.awt.BorderLayout.CENTER);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(flavorsScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 344, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(availableFlavorsLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(flavorContentPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(flavorContentPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(availableFlavorsLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(flavorsScrollPane)))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void openAsTabButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openAsTabButtonActionPerformed
        openAsTabAction.actionPerformed(evt);
    }//GEN-LAST:event_openAsTabButtonActionPerformed

    private void saveAsFileButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveAsFileButtonActionPerformed
        saveAsFileAction.actionPerformed(evt);
    }//GEN-LAST:event_saveAsFileButtonActionPerformed

    /**
     * Test method for this panel.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        WindowUtils.invokeDialog(new ClipboardContentPanel());
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel availableFlavorsLabel;
    private javax.swing.JPanel binaryDataPanel;
    private org.exbin.bined.swing.extended.ExtCodeArea dataCodeArea;
    private javax.swing.JComboBox<String> dataComboBox;
    private javax.swing.JPanel dataContentPanel;
    private javax.swing.JLabel dataLabel;
    private javax.swing.JList<String> dataList;
    private javax.swing.JScrollPane dataListScrollPane;
    private javax.swing.JPanel flavorContentPanel;
    private javax.swing.JPanel flavorPanel;
    private javax.swing.JList<String> flavorsList;
    private javax.swing.JScrollPane flavorsScrollPane;
    private javax.swing.JLabel imageLabel;
    private javax.swing.JScrollPane imageScrollPane;
    private javax.swing.JLabel mimeTypeLabel;
    private javax.swing.JTextField mimeTypeTextField;
    private javax.swing.JLabel noFlavorSelectedLabel;
    private javax.swing.JButton openAsTabButton;
    private javax.swing.JLabel presentableNameLabel;
    private javax.swing.JTextField presentableNameTextField;
    private javax.swing.JLabel primaryMimeTypeLabel;
    private javax.swing.JTextField primaryMimeTypeTextField;
    private javax.swing.JLabel representationClassLabel;
    private javax.swing.JTextField representationClassTextField;
    private javax.swing.JButton saveAsFileButton;
    private javax.swing.JLabel stringTypeLabel;
    private javax.swing.JTextField stringTypeTextField;
    private javax.swing.JLabel subMimeTypeLabel;
    private javax.swing.JTextField subMimeTypeTextField;
    private javax.swing.JScrollPane textDataScrollPane;
    private javax.swing.JTextArea textDataTextArea;
    // End of variables declaration//GEN-END:variables

    public void setCodeAreaPopupMenuHandler(CodeAreaPopupMenuHandler codeAreaPopupMenuHandler) {
        this.codeAreaPopupMenuHandler = codeAreaPopupMenuHandler;
        dataCodeArea.setComponentPopupMenu(new JPopupMenu() {
            @Override
            public void show(Component invoker, int x, int y) {
                int clickedX = x;
                int clickedY = y;
                if (invoker instanceof JViewport) {
                    clickedX += ((JViewport) invoker).getParent().getX();
                    clickedY += ((JViewport) invoker).getParent().getY();
                }
                JPopupMenu popupMenu = codeAreaPopupMenuHandler.createPopupMenu(dataCodeArea, POPUP_MENU_POSTFIX, clickedX, clickedY);
                popupMenu.show(invoker, x, y);
                codeAreaPopupMenuHandler.dropPopupMenu(POPUP_MENU_POSTFIX);
            }
        });
    }

    public void detachMenu() {
        if (codeAreaPopupMenuHandler != null) {
            codeAreaPopupMenuHandler.dropPopupMenu(POPUP_MENU_POSTFIX);
        }
    }

    @ParametersAreNonnullByDefault
    private static class PropertyClass {

        Object classInst;

        public PropertyClass(Object classInst) {
            this.classInst = classInst;
        }
    }
}
