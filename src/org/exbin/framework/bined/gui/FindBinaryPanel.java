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
package org.exbin.framework.bined.gui;

import org.exbin.framework.bined.ReplaceParameters;
import org.exbin.framework.bined.SearchCondition;
import org.exbin.framework.bined.SearchParameters;
import org.exbin.framework.bined.SearchHistoryModel;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.ComboBoxEditor;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import org.exbin.bined.ScrollBarVisibility;
import org.exbin.bined.RowWrappingMode;
import org.exbin.bined.extended.layout.ExtendedCodeAreaLayoutProfile;
import org.exbin.bined.extended.theme.ExtendedBackgroundPaintMode;
import org.exbin.bined.swing.extended.ExtCodeArea;
import org.exbin.bined.swing.extended.theme.ExtendedCodeAreaThemeProfile;
import org.exbin.framework.bined.handler.CodeAreaPopupMenuHandler;
import org.exbin.framework.gui.utils.LanguageUtils;
import org.exbin.framework.gui.utils.WindowUtils;
import org.exbin.auxiliary.paged_data.ByteArrayEditableData;

/**
 * Find text/hexadecimal data panel.
 *
 * @version 0.2.1 2019/07/16
 * @author ExBin Project (http://exbin.org)
 */
@ParametersAreNonnullByDefault
public class FindBinaryPanel extends javax.swing.JPanel {

    private final java.util.ResourceBundle resourceBundle = LanguageUtils.getResourceBundleByClass(FindBinaryPanel.class);
    public static final String POPUP_MENU_POSTFIX = ".searchFindHexPanel";

    private final ExtCodeArea findCodeArea = new ExtCodeArea();
    private BinarySearchComboBoxPanel findComboBoxEditorComponent;
    private ComboBoxEditor findComboBoxEditor;
    private List<SearchCondition> searchHistory = new ArrayList<>();

    private final ExtCodeArea replaceCodeArea = new ExtCodeArea();
    private BinarySearchComboBoxPanel replaceComboBoxEditorComponent;
    private ComboBoxEditor replaceComboBoxEditor;
    private List<SearchCondition> replaceHistory = new ArrayList<>();

    private CodeAreaPopupMenuHandler codeAreaPopupMenuHandler;
    private MultilineEditorListener multilineEditorListener = null;

    public FindBinaryPanel() {
        initComponents();
        init();
    }

    private void init() {
        {
            ExtendedCodeAreaLayoutProfile layoutProfile = Objects.requireNonNull(findCodeArea.getLayoutProfile());
            layoutProfile.setShowHeader(false);
            layoutProfile.setShowRowPosition(false);
            findCodeArea.setLayoutProfile(layoutProfile);
        }
        findCodeArea.setRowWrapping(RowWrappingMode.WRAPPING);
        findCodeArea.setWrappingBytesGroupSize(0);
        {
            ExtendedCodeAreaThemeProfile themeProfile = findCodeArea.getThemeProfile();
            themeProfile.setBackgroundPaintMode(ExtendedBackgroundPaintMode.PLAIN);
            findCodeArea.setThemeProfile(themeProfile);
        }
        findCodeArea.setVerticalScrollBarVisibility(ScrollBarVisibility.NEVER);
        findCodeArea.setHorizontalScrollBarVisibility(ScrollBarVisibility.NEVER);
        findCodeArea.setContentData(new ByteArrayEditableData());

        findComboBoxEditorComponent = new BinarySearchComboBoxPanel();
        findComboBox.setRenderer(new ListCellRenderer<SearchCondition>() {
            private final JPanel emptyPanel = new JPanel();
            private final DefaultListCellRenderer listCellRenderer = new DefaultListCellRenderer();

            @Override
            public Component getListCellRendererComponent(JList<? extends SearchCondition> list, SearchCondition value, int index, boolean isSelected, boolean cellHasFocus) {
                if (value == null) {
                    return emptyPanel;
                }

                if (value.getSearchMode() == SearchCondition.SearchMode.TEXT) {
                    return listCellRenderer.getListCellRendererComponent(list, value.getSearchText(), index, isSelected, cellHasFocus);
                } else {
                    findCodeArea.setContentData(value.getBinaryData());
                    findCodeArea.setPreferredSize(new Dimension(200, 20));
                    Color backgroundColor;
                    if (isSelected) {
                        backgroundColor = list.getSelectionBackground();
                    } else {
                        backgroundColor = list.getBackground();
                    }
// TODO                    ColorsGroup mainColors = new ColorsGroup(findHexadecimalRenderer.getMainColors());
//                    mainColors.setBothBackgroundColors(backgroundColor);
//                    findHexadecimalRenderer.setMainColors(mainColors);
                    return findCodeArea;
                }
            }
        });
        findComboBoxEditor = new ComboBoxEditor() {

            @Override
            public Component getEditorComponent() {
                return findComboBoxEditorComponent;
            }

            @Override
            public void setItem(Object item) {
                findComboBoxEditorComponent.setItem((SearchCondition) item);
                updateFindStatus();
            }

            @Override
            public Object getItem() {
                return findComboBoxEditorComponent.getItem();
            }

            @Override
            public void selectAll() {
                findComboBoxEditorComponent.selectAll();
            }

            @Override
            public void addActionListener(ActionListener l) {
            }

            @Override
            public void removeActionListener(ActionListener l) {
            }
        };
        findComboBox.setEditor(findComboBoxEditor);
        findComboBox.setModel(new SearchHistoryModel(searchHistory));

        {
            ExtendedCodeAreaLayoutProfile layoutProfile = Objects.requireNonNull(replaceCodeArea.getLayoutProfile());
            layoutProfile.setShowHeader(false);
            layoutProfile.setShowRowPosition(false);
            replaceCodeArea.setLayoutProfile(layoutProfile);
        }
        replaceCodeArea.setRowWrapping(RowWrappingMode.WRAPPING);
        replaceCodeArea.setWrappingBytesGroupSize(0);
        {
            ExtendedCodeAreaThemeProfile themeProfile = replaceCodeArea.getThemeProfile();
            themeProfile.setBackgroundPaintMode(ExtendedBackgroundPaintMode.PLAIN);
            replaceCodeArea.setThemeProfile(themeProfile);
        }
        replaceCodeArea.setVerticalScrollBarVisibility(ScrollBarVisibility.NEVER);
        replaceCodeArea.setHorizontalScrollBarVisibility(ScrollBarVisibility.NEVER);
        replaceCodeArea.setContentData(new ByteArrayEditableData());

        replaceComboBoxEditorComponent = new BinarySearchComboBoxPanel();
        replaceComboBox.setRenderer(new ListCellRenderer<SearchCondition>() {
            private final JPanel emptyPanel = new JPanel();
            private final DefaultListCellRenderer listCellRenderer = new DefaultListCellRenderer();

            @Override
            public Component getListCellRendererComponent(JList<? extends SearchCondition> list, SearchCondition value, int index, boolean isSelected, boolean cellHasFocus) {
                if (value == null) {
                    return emptyPanel;
                }

                if (value.getSearchMode() == SearchCondition.SearchMode.TEXT) {
                    return listCellRenderer.getListCellRendererComponent(list, value.getSearchText(), index, isSelected, cellHasFocus);
                } else {
                    replaceCodeArea.setContentData(value.getBinaryData());
                    replaceCodeArea.setPreferredSize(new Dimension(200, 20));
                    Color backgroundColor;
                    if (isSelected) {
                        backgroundColor = list.getSelectionBackground();
                    } else {
                        backgroundColor = list.getBackground();
                    }
// TODO                    ColorsGroup mainColors = new ColorsGroup(replaceHexadecimalRenderer.getMainColors());
//                    mainColors.setBothBackgroundColors(backgroundColor);
//                    replaceHexadecimalRenderer.setMainColors(mainColors);
                    return replaceCodeArea;
                }
            }
        });
        replaceComboBoxEditor = new ComboBoxEditor() {

            @Override
            public Component getEditorComponent() {
                return replaceComboBoxEditorComponent;
            }

            @Override
            public void setItem(Object item) {
                replaceComboBoxEditorComponent.setItem((SearchCondition) item);
                updateReplaceStatus();
            }

            @Override
            public Object getItem() {
                return replaceComboBoxEditorComponent.getItem();
            }

            @Override
            public void selectAll() {
                replaceComboBoxEditorComponent.selectAll();
            }

            @Override
            public void addActionListener(ActionListener l) {
            }

            @Override
            public void removeActionListener(ActionListener l) {
            }
        };
        replaceComboBox.setEditor(replaceComboBoxEditor);
        replaceComboBox.setModel(new SearchHistoryModel(replaceHistory));
    }

    public void setSelected() {
        findComboBox.requestFocusInWindow();
        findComboBox.getEditor().selectAll();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        findPanel = new javax.swing.JPanel();
        findLabel = new javax.swing.JLabel();
        searchTypeButton = new javax.swing.JButton();
        findComboBox = new javax.swing.JComboBox<>();
        findMultilineButton = new javax.swing.JButton();
        searchFromCursorCheckBox = new javax.swing.JCheckBox();
        matchCaseCheckBox = new javax.swing.JCheckBox();
        multipleMatchesCheckBox = new javax.swing.JCheckBox();
        replacePanel = new javax.swing.JPanel();
        performReplaceCheckBox = new javax.swing.JCheckBox();
        replaceLabel = new javax.swing.JLabel();
        replaceTypeButton = new javax.swing.JButton();
        replaceComboBox = new javax.swing.JComboBox<>();
        replaceMultilineButton = new javax.swing.JButton();
        replaceAllMatchesCheckBox = new javax.swing.JCheckBox();

        setLayout(new java.awt.BorderLayout());

        findPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceBundle.getString("findPanel.border.title"))); // NOI18N

        findLabel.setText(resourceBundle.getString("findLabel.text")); // NOI18N

        searchTypeButton.setText(resourceBundle.getString("searchTypeButton.text")); // NOI18N
        searchTypeButton.setToolTipText(resourceBundle.getString("searchTypeButton.toolTipText")); // NOI18N
        searchTypeButton.setFocusable(false);
        searchTypeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchTypeButtonActionPerformed(evt);
            }
        });

        findComboBox.setEditable(true);
        findComboBox.setMinimumSize(new java.awt.Dimension(136, 30));
        findComboBox.setPreferredSize(new java.awt.Dimension(136, 30));

        findMultilineButton.setText(resourceBundle.getString("findMultilineButton.text")); // NOI18N
        findMultilineButton.setToolTipText(resourceBundle.getString("findMultilineButton.toolTipText")); // NOI18N
        findMultilineButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                findMultilineButtonActionPerformed(evt);
            }
        });

        searchFromCursorCheckBox.setSelected(true);
        searchFromCursorCheckBox.setText(resourceBundle.getString("searchFromCursorCheckBox.text")); // NOI18N

        matchCaseCheckBox.setText(resourceBundle.getString("matchCaseCheckBox.text")); // NOI18N

        multipleMatchesCheckBox.setSelected(true);
        multipleMatchesCheckBox.setText(resourceBundle.getString("multipleMatchesCheckBox.text")); // NOI18N

        javax.swing.GroupLayout findPanelLayout = new javax.swing.GroupLayout(findPanel);
        findPanel.setLayout(findPanelLayout);
        findPanelLayout.setHorizontalGroup(
            findPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(findPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(findPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(matchCaseCheckBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(searchFromCursorCheckBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(findPanelLayout.createSequentialGroup()
                        .addComponent(searchTypeButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(findComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(findMultilineButton))
                    .addComponent(multipleMatchesCheckBox, javax.swing.GroupLayout.DEFAULT_SIZE, 378, Short.MAX_VALUE)
                    .addGroup(findPanelLayout.createSequentialGroup()
                        .addComponent(findLabel)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        findPanelLayout.setVerticalGroup(
            findPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(findPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(findLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(findPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(findComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(findMultilineButton)
                    .addComponent(searchTypeButton))
                .addGap(18, 18, 18)
                .addComponent(searchFromCursorCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(matchCaseCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(multipleMatchesCheckBox)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        add(findPanel, java.awt.BorderLayout.PAGE_START);

        replacePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceBundle.getString("replacePanel.border.title"))); // NOI18N

        performReplaceCheckBox.setText(resourceBundle.getString("performReplaceCheckBox.text")); // NOI18N
        performReplaceCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                performReplaceCheckBoxActionPerformed(evt);
            }
        });

        replaceLabel.setText(resourceBundle.getString("textToReplaceLabel.text")); // NOI18N
        replaceLabel.setEnabled(false);

        replaceTypeButton.setText(resourceBundle.getString("replaceTypeButton.text")); // NOI18N
        replaceTypeButton.setToolTipText(resourceBundle.getString("replaceTypeButton.toolTipText")); // NOI18N
        replaceTypeButton.setEnabled(false);
        replaceTypeButton.setFocusable(false);
        replaceTypeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                replaceTypeButtonActionPerformed(evt);
            }
        });

        replaceComboBox.setEditable(true);
        replaceComboBox.setEnabled(false);
        replaceComboBox.setMinimumSize(new java.awt.Dimension(136, 30));
        replaceComboBox.setPreferredSize(new java.awt.Dimension(136, 30));

        replaceMultilineButton.setText(resourceBundle.getString("replaceMultilineButton.text")); // NOI18N
        replaceMultilineButton.setToolTipText(resourceBundle.getString("replaceMultilineButton.toolTipText")); // NOI18N
        replaceMultilineButton.setEnabled(false);
        replaceMultilineButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                replaceMultilineButtonActionPerformed(evt);
            }
        });

        replaceAllMatchesCheckBox.setText(resourceBundle.getString("replaceAllMatchesCheckBox.text")); // NOI18N
        replaceAllMatchesCheckBox.setEnabled(false);

        javax.swing.GroupLayout replacePanelLayout = new javax.swing.GroupLayout(replacePanel);
        replacePanel.setLayout(replacePanelLayout);
        replacePanelLayout.setHorizontalGroup(
            replacePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(replacePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(replacePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(performReplaceCheckBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(replacePanelLayout.createSequentialGroup()
                        .addComponent(replaceLabel)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(replacePanelLayout.createSequentialGroup()
                        .addComponent(replaceTypeButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(replaceComboBox, 0, 283, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(replaceMultilineButton))
                    .addComponent(replaceAllMatchesCheckBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        replacePanelLayout.setVerticalGroup(
            replacePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(replacePanelLayout.createSequentialGroup()
                .addComponent(performReplaceCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(replaceLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(replacePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(replaceComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(replaceMultilineButton)
                    .addComponent(replaceTypeButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(replaceAllMatchesCheckBox)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        add(replacePanel, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void findMultilineButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_findMultilineButtonActionPerformed
        if (multilineEditorListener != null) {
            SearchCondition condition = multilineEditorListener.multilineEdit((SearchCondition) findComboBoxEditor.getItem());
            if (condition != null) {
                findComboBoxEditorComponent.setItem(condition);
            }
        }
    }//GEN-LAST:event_findMultilineButtonActionPerformed

    private void searchTypeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchTypeButtonActionPerformed
        SearchCondition condition = (SearchCondition) findComboBoxEditor.getItem();
        if (condition.getSearchMode() == SearchCondition.SearchMode.TEXT) {
            condition.setSearchMode(SearchCondition.SearchMode.BINARY);
        } else {
            condition.setSearchMode(SearchCondition.SearchMode.TEXT);
        }
        findComboBoxEditor.setItem(condition);
        findComboBox.setEditor(findComboBoxEditor);
        updateFindStatus();
        findComboBox.repaint();
    }//GEN-LAST:event_searchTypeButtonActionPerformed

    private void performReplaceCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_performReplaceCheckBoxActionPerformed
        updateReplaceEnablement();
    }//GEN-LAST:event_performReplaceCheckBoxActionPerformed

    private void replaceTypeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_replaceTypeButtonActionPerformed
        SearchCondition condition = (SearchCondition) replaceComboBoxEditor.getItem();
        if (condition.getSearchMode() == SearchCondition.SearchMode.TEXT) {
            condition.setSearchMode(SearchCondition.SearchMode.BINARY);
        } else {
            condition.setSearchMode(SearchCondition.SearchMode.TEXT);
        }
        replaceComboBoxEditor.setItem(condition);
        replaceComboBox.setEditor(replaceComboBoxEditor);
        updateReplaceStatus();
        replaceComboBox.repaint();
    }//GEN-LAST:event_replaceTypeButtonActionPerformed

    private void replaceMultilineButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_replaceMultilineButtonActionPerformed
        if (multilineEditorListener != null) {
            SearchCondition condition = multilineEditorListener.multilineEdit((SearchCondition) replaceComboBoxEditor.getItem());
            if (condition != null) {
                replaceComboBoxEditorComponent.setItem(condition);
            }
        }
    }//GEN-LAST:event_replaceMultilineButtonActionPerformed

    private void updateFindStatus() {
        SearchCondition condition = (SearchCondition) findComboBoxEditor.getItem();
        if (condition.getSearchMode() == SearchCondition.SearchMode.TEXT) {
            searchTypeButton.setText("T");
            matchCaseCheckBox.setEnabled(true);
        } else {
            searchTypeButton.setText("B");
            matchCaseCheckBox.setEnabled(false);
        }
    }

    private void updateReplaceStatus() {
        SearchCondition condition = (SearchCondition) replaceComboBoxEditor.getItem();
        if (condition.getSearchMode() == SearchCondition.SearchMode.TEXT) {
            replaceTypeButton.setText("T");
        } else {
            replaceTypeButton.setText("B");
        }
    }

    /**
     * Test method for this panel.
     *
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        WindowUtils.invokeDialog(new FindBinaryPanel());
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<SearchCondition> findComboBox;
    private javax.swing.JLabel findLabel;
    private javax.swing.JButton findMultilineButton;
    private javax.swing.JPanel findPanel;
    private javax.swing.JCheckBox matchCaseCheckBox;
    private javax.swing.JCheckBox multipleMatchesCheckBox;
    private javax.swing.JCheckBox performReplaceCheckBox;
    private javax.swing.JCheckBox replaceAllMatchesCheckBox;
    private javax.swing.JComboBox<SearchCondition> replaceComboBox;
    private javax.swing.JLabel replaceLabel;
    private javax.swing.JButton replaceMultilineButton;
    private javax.swing.JPanel replacePanel;
    private javax.swing.JButton replaceTypeButton;
    private javax.swing.JCheckBox searchFromCursorCheckBox;
    private javax.swing.JButton searchTypeButton;
    // End of variables declaration//GEN-END:variables

    public ResourceBundle getResourceBundle() {
        return resourceBundle;
    }

    public String getFindText() {
        return (String) findComboBox.getEditor().getItem();
    }

    public boolean getShallReplace() {
        return performReplaceCheckBox.isSelected();
    }

    public SearchParameters getSearchParameters() {
        SearchParameters result = new SearchParameters();
        result.setCondition((SearchCondition) findComboBox.getEditor().getItem());
        result.setSearchFromCursor(searchFromCursorCheckBox.isSelected());
        result.setMatchCase(matchCaseCheckBox.isSelected());
        result.setMultipleMatches(multipleMatchesCheckBox.isSelected());
        return result;
    }

    public void setSearchParameters(SearchParameters parameters) {
        searchFromCursorCheckBox.setSelected(parameters.isSearchFromCursor());
        matchCaseCheckBox.setSelected(parameters.isMatchCase());
        multipleMatchesCheckBox.setSelected(parameters.isMultipleMatches());
        findComboBoxEditorComponent.setItem(parameters.getCondition());
        findComboBox.setEditor(findComboBoxEditor);
        findComboBox.repaint();
        updateFindStatus();
    }

    public ReplaceParameters getReplaceParameters() {
        ReplaceParameters result = new ReplaceParameters();
        result.setCondition((SearchCondition) replaceComboBox.getEditor().getItem());
        result.setPerformReplace(performReplaceCheckBox.isSelected());
        result.setReplaceAll(replaceAllMatchesCheckBox.isSelected());
        return result;
    }

    public void setReplaceParameters(ReplaceParameters parameters) {
        performReplaceCheckBox.setSelected(parameters.isPerformReplace());
        replaceAllMatchesCheckBox.setSelected(parameters.isReplaceAll());
        replaceComboBoxEditorComponent.setItem(parameters.getCondition());
        replaceComboBox.setEditor(replaceComboBoxEditor);
        replaceComboBox.repaint();
        updateReplaceStatus();
        updateReplaceEnablement();
    }

    public void setSearchHistory(List<SearchCondition> searchHistory) {
        this.searchHistory = searchHistory;
        findComboBox.setModel(new SearchHistoryModel(searchHistory));
    }

    public void setReplaceHistory(List<SearchCondition> replaceHistory) {
        this.replaceHistory = replaceHistory;
        replaceComboBox.setModel(new SearchHistoryModel(replaceHistory));
    }

    public void setCodeAreaPopupMenuHandler(CodeAreaPopupMenuHandler codeAreaPopupMenuHandler) {
        this.codeAreaPopupMenuHandler = codeAreaPopupMenuHandler;
        findComboBoxEditorComponent.setCodeAreaPopupMenuHandler(codeAreaPopupMenuHandler, "FindHexPanel");
    }

    public void detachMenu() {
        codeAreaPopupMenuHandler.dropPopupMenu(POPUP_MENU_POSTFIX);
    }

    private void updateReplaceEnablement() {
        boolean replaceEnabled = performReplaceCheckBox.isSelected();
        replaceTypeButton.setEnabled(replaceEnabled);
        replaceComboBox.setEnabled(replaceEnabled);
        replaceMultilineButton.setEnabled(replaceEnabled);
        replaceAllMatchesCheckBox.setEnabled(replaceEnabled);
        replaceLabel.setEnabled(replaceEnabled);
    }

    public void setMultilineEditorListener(MultilineEditorListener multilineEditorListener) {
        this.multilineEditorListener = multilineEditorListener;
    }

    @ParametersAreNonnullByDefault
    public static interface MultilineEditorListener {

        @Nullable
        SearchCondition multilineEdit(SearchCondition condition);
    }
}
