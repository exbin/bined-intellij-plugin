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
package org.exbin.framework.bined.search.gui;

import org.exbin.framework.bined.search.ReplaceParameters;
import org.exbin.framework.bined.search.SearchCondition;
import org.exbin.framework.bined.search.SearchParameters;
import org.exbin.framework.bined.search.SearchHistoryModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
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
import org.exbin.framework.api.XBApplication;
import org.exbin.framework.bined.handler.CodeAreaPopupMenuHandler;
import org.exbin.framework.utils.LanguageUtils;
import org.exbin.framework.utils.WindowUtils;
import org.exbin.auxiliary.binary_data.ByteArrayEditableData;
import org.exbin.bined.CodeAreaUtils;
import org.exbin.bined.color.CodeAreaBasicColors;
import org.exbin.bined.swing.extended.ExtendedCodeAreaPainter;
import org.exbin.bined.swing.extended.color.ExtendedCodeAreaColorProfile;

/**
 * Binary editor search panel.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class BinarySearchPanel extends javax.swing.JPanel {

    private final java.util.ResourceBundle resourceBundle = LanguageUtils.getResourceBundleByClass(BinarySearchPanel.class);

    private Control control = null;

    private final ExtCodeArea searchCodeArea = new ExtCodeArea();

    private PanelMode panelMode = PanelMode.REPLACE;
    private ComboBoxEditor findComboBoxEditor;
    private BinarySearchComboBoxPanel findComboBoxEditorComponent;
    private ComboBoxEditor replaceComboBoxEditor;
    private BinarySearchComboBoxPanel replaceComboBoxEditorComponent;

    private XBApplication application;

    public BinarySearchPanel() {
        initComponents();
        init();
    }

    private void init() {
        ExtendedCodeAreaLayoutProfile layoutProfile = Objects.requireNonNull(searchCodeArea.getLayoutProfile());
        layoutProfile.setShowHeader(false);
        layoutProfile.setShowRowPosition(false);

        searchCodeArea.setLayoutProfile(layoutProfile);
        ExtendedCodeAreaThemeProfile themeProfile = searchCodeArea.getThemeProfile();
        themeProfile.setBackgroundPaintMode(ExtendedBackgroundPaintMode.PLAIN);
        searchCodeArea.setThemeProfile(themeProfile);

        searchCodeArea.setBorder(null);
        searchCodeArea.setLayoutProfile(layoutProfile);
        searchCodeArea.setRowWrapping(RowWrappingMode.WRAPPING);
        searchCodeArea.setWrappingBytesGroupSize(0);
        searchCodeArea.setVerticalScrollBarVisibility(ScrollBarVisibility.NEVER);
        searchCodeArea.setHorizontalScrollBarVisibility(ScrollBarVisibility.NEVER);
        searchCodeArea.setContentData(new ByteArrayEditableData(new byte[]{1, 2, 3}));

        final KeyAdapter editorKeyListener = new KeyAdapter() {
            @Override
            public void keyPressed(@Nonnull KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    control.performEscape();
                }
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    control.performFind();
                }
            }
        };

        findComboBoxEditorComponent = new BinarySearchComboBoxPanel();
        findComboBox.setRenderer(new ListCellRenderer<SearchCondition>() {
            private final JPanel panel = new JPanel();
            private final DefaultListCellRenderer listCellRenderer = new DefaultListCellRenderer();

            @Nonnull
            @Override
            public Component getListCellRendererComponent(JList<? extends SearchCondition> list, @Nullable SearchCondition value, int index, boolean isSelected, boolean cellHasFocus) {
                if (value == null) {
                    return panel;
                }

                if (value.getSearchMode() == SearchCondition.SearchMode.TEXT) {
                    return listCellRenderer.getListCellRendererComponent(list, value.getSearchText(), index, isSelected, cellHasFocus);
                } else {
                    searchCodeArea.setContentData(value.getBinaryData());
                    searchCodeArea.setPreferredSize(new Dimension(200, 20));
                    Color backgroundColor;
                    if (isSelected) {
                        backgroundColor = list.getSelectionBackground();
                    } else {
                        backgroundColor = list.getBackground();
                    }
                    ExtendedCodeAreaPainter painter = (ExtendedCodeAreaPainter) searchCodeArea.getPainter();
                    ExtendedCodeAreaColorProfile colorsProfile = (ExtendedCodeAreaColorProfile) painter.getColorsProfile();
                    colorsProfile.setColor(CodeAreaBasicColors.TEXT_BACKGROUND, backgroundColor);
                    return searchCodeArea;
                }
            }
        });
        findComboBoxEditor = new ComboBoxEditor() {

            @Override
            public Component getEditorComponent() {
                return findComboBoxEditorComponent;
            }

            @Override
            public void setItem(@Nullable Object item) {
                SearchCondition condition;
                if (item == null || item instanceof String) {
                    condition = new SearchCondition();
                    condition.setSearchMode(SearchCondition.SearchMode.TEXT);
                    if (item != null) {
                        condition.setSearchText((String) item);
                    }
                } else {
                    condition = (SearchCondition) item;
                }
                SearchCondition currentItem = findComboBoxEditorComponent.getItem();
                if (item != currentItem) {
                    findComboBoxEditorComponent.setItem(condition);
                    updateFindStatus();
                }
            }

            @Nonnull
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

        findComboBoxEditorComponent.setValueChangedListener(this::comboBoxValueChanged);
        findComboBoxEditorComponent.addValueKeyListener(editorKeyListener);

        replaceComboBoxEditorComponent = new BinarySearchComboBoxPanel();
        replaceComboBox.setRenderer(new ListCellRenderer<SearchCondition>() {
            private final JPanel panel = new JPanel();
            private final DefaultListCellRenderer listCellRenderer = new DefaultListCellRenderer();

            @Nonnull
            @Override
            public Component getListCellRendererComponent(@Nonnull JList<? extends SearchCondition> list, @Nullable SearchCondition value, int index, boolean isSelected, boolean cellHasFocus) {
                if (value == null) {
                    return panel;
                }

                if (value.getSearchMode() == SearchCondition.SearchMode.TEXT) {
                    return listCellRenderer.getListCellRendererComponent(list, value.getSearchText(), index, isSelected, cellHasFocus);
                } else {
                    searchCodeArea.setContentData(value.getBinaryData());
                    searchCodeArea.setPreferredSize(new Dimension(200, 20));
                    Color backgroundColor;
                    if (isSelected) {
                        backgroundColor = list.getSelectionBackground();
                    } else {
                        backgroundColor = list.getBackground();
                    }
                    ExtendedCodeAreaPainter painter = (ExtendedCodeAreaPainter) searchCodeArea.getPainter();
                    ExtendedCodeAreaColorProfile colorsProfile = (ExtendedCodeAreaColorProfile) painter.getColorsProfile();
                    colorsProfile.setColor(CodeAreaBasicColors.TEXT_BACKGROUND, backgroundColor);
                    return searchCodeArea;
                }
            }
        });
        replaceComboBoxEditor = new ComboBoxEditor() {
            @Nonnull
            @Override
            public Component getEditorComponent() {
                return replaceComboBoxEditorComponent;
            }

            @Override
            public void setItem(@Nullable Object item) {
                SearchCondition condition;
                if (item == null || item instanceof String) {
                    condition = new SearchCondition();
                    condition.setSearchMode(SearchCondition.SearchMode.TEXT);
                    if (item != null) {
                        condition.setSearchText((String) item);
                    }
                } else {
                    condition = (SearchCondition) item;
                }
                SearchCondition currentItem = replaceComboBoxEditorComponent.getItem();
                if (item != currentItem) {
                    replaceComboBoxEditorComponent.setItem(condition);
                    updateReplaceStatus();
                }
            }

            @Nonnull
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

        replaceComboBoxEditorComponent.addValueKeyListener(editorKeyListener);
    }

    public void setControl(Control control) {
        this.control = control;
    }

    public void setInfoLabel(String text) {
        infoLabel.setText(text);
    }

    @Nonnull
    public SearchParameters getSearchParameters() {
        SearchParameters searchParameters = new SearchParameters();
        searchParameters.setMatchCase(matchCaseToggleButton.isSelected());
        searchParameters.setMatchMode(SearchParameters.MatchMode.fromBoolean(multipleMatchesToggleButton.isSelected()));
        SearchParameters.SearchDirection searchDirection = control.getSearchDirection();
        searchParameters.setSearchDirection(searchDirection);

        long startPosition;
        if (searchParameters.isSearchFromCursor()) {
            startPosition = searchCodeArea.getCaretPosition().getDataPosition();
        } else {
            switch (searchDirection) {
                case FORWARD: {
                    startPosition = 0;
                    break;
                }
                case BACKWARD: {
                    startPosition = searchCodeArea.getDataSize() - 1;
                    break;
                }
                default:
                    throw CodeAreaUtils.getInvalidTypeException(searchDirection);
            }
        }
        searchParameters.setStartPosition(startPosition);

        searchParameters.setCondition(new SearchCondition(findComboBoxEditorComponent.getItem()));
        return searchParameters;
    }

    @Nonnull
    public ReplaceParameters getReplaceParameters() {
        ReplaceParameters replaceParameters = new ReplaceParameters();
        replaceParameters.setCondition(new SearchCondition(replaceComboBoxEditorComponent.getItem()));
        return replaceParameters;
    }

    public void updateMatchStatus(boolean hasMatches, boolean prevMatchAvailable, boolean nextMatchAvailable) {
        prevMatchButton.setEnabled(prevMatchAvailable);
        nextMatchButton.setEnabled(nextMatchAvailable);
        replaceButton.setEnabled(hasMatches);
        replaceAllButton.setEnabled(hasMatches);
    }

    public void setSearchHistory(List<SearchCondition> history) {
        findComboBox.setModel(new SearchHistoryModel(history));
    }

    public void setReplaceHistory(List<SearchCondition> history) {
        replaceComboBox.setModel(new SearchHistoryModel(history));
    }

    public void switchPanelMode(PanelMode panelMode) {
        if (this.panelMode != panelMode) {
            this.panelMode = panelMode;
            if (panelMode == PanelMode.REPLACE) {
                add(replacePanel, BorderLayout.SOUTH);
            } else {
                remove(replacePanel);
            }
            revalidate();
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        topSeparator = new javax.swing.JSeparator();
        findPanel = new javax.swing.JPanel();
        findLabel = new javax.swing.JLabel();
        findTypeToolBar = new javax.swing.JToolBar();
        findTypeButton = new javax.swing.JButton();
        findComboBox = new javax.swing.JComboBox<>();
        findToolBar = new javax.swing.JToolBar();
        prevMatchButton = new javax.swing.JButton();
        nextMatchButton = new javax.swing.JButton();
        matchCaseToggleButton = new javax.swing.JToggleButton();
        multipleMatchesToggleButton = new javax.swing.JToggleButton();
        separator1 = new javax.swing.JToolBar.Separator();
        optionsButton = new javax.swing.JButton();
        infoLabel = new javax.swing.JLabel();
        closeToolBar = new javax.swing.JToolBar();
        closeButton = new javax.swing.JButton();
        replacePanel = new javax.swing.JPanel();
        replaceLabel = new javax.swing.JLabel();
        replaceTypeToolBar = new javax.swing.JToolBar();
        replaceTypeButton = new javax.swing.JButton();
        replaceComboBox = new javax.swing.JComboBox<>();
        replaceToolBar = new javax.swing.JToolBar();
        replaceButton = new javax.swing.JButton();
        replaceAllButton = new javax.swing.JButton();

        setName("Form"); // NOI18N
        setLayout(new java.awt.BorderLayout());

        topSeparator.setName("topSeparator"); // NOI18N
        add(topSeparator, java.awt.BorderLayout.NORTH);

        findPanel.setName("findPanel"); // NOI18N

        findLabel.setText(resourceBundle.getString("findLabel.text")); // NOI18N
        findLabel.setName("findLabel"); // NOI18N

        findTypeToolBar.setBorder(null);
        findTypeToolBar.setRollover(true);
        findTypeToolBar.setFocusable(false);
        findTypeToolBar.setName("findTypeToolBar"); // NOI18N

        findTypeButton.setText(resourceBundle.getString("inputType.text")); // NOI18N
        findTypeButton.setToolTipText(resourceBundle.getString("findTypeButton.toolTipText")); // NOI18N
        findTypeButton.setFocusable(false);
        findTypeButton.setMaximumSize(new java.awt.Dimension(27, 27));
        findTypeButton.setMinimumSize(new java.awt.Dimension(27, 27));
        findTypeButton.setName("findTypeButton"); // NOI18N
        findTypeButton.setPreferredSize(new java.awt.Dimension(27, 27));
        findTypeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                findTypeButtonActionPerformed(evt);
            }
        });
        findTypeToolBar.add(findTypeButton);

        findComboBox.setEditable(true);
        findComboBox.setName("findComboBox"); // NOI18N

        findToolBar.setBorder(null);
        findToolBar.setRollover(true);
        findToolBar.setFocusable(false);
        findToolBar.setName("findToolBar"); // NOI18N

        prevMatchButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/exbin/framework/bined/search/resources/icons/open_icon_library/icons/png/16x16/actions/arrow-left.png"))); // NOI18N
        prevMatchButton.setToolTipText(resourceBundle.getString("prevMatchButton.toolTipText")); // NOI18N
        prevMatchButton.setEnabled(false);
        prevMatchButton.setFocusable(false);
        prevMatchButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        prevMatchButton.setName("prevMatchButton"); // NOI18N
        prevMatchButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        prevMatchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                prevMatchButtonActionPerformed(evt);
            }
        });
        findToolBar.add(prevMatchButton);

        nextMatchButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/exbin/framework/bined/search/resources/icons/open_icon_library/icons/png/16x16/actions/arrow-right.png"))); // NOI18N
        nextMatchButton.setToolTipText(resourceBundle.getString("nextMatchButton.toolTipText")); // NOI18N
        nextMatchButton.setEnabled(false);
        nextMatchButton.setFocusable(false);
        nextMatchButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        nextMatchButton.setName("nextMatchButton"); // NOI18N
        nextMatchButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        nextMatchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nextMatchButtonActionPerformed(evt);
            }
        });
        findToolBar.add(nextMatchButton);

        matchCaseToggleButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/exbin/framework/bined/search/resources/icons/case_sensitive.gif"))); // NOI18N
        matchCaseToggleButton.setToolTipText(resourceBundle.getString("matchCaseToggleButton.toolTipText")); // NOI18N
        matchCaseToggleButton.setFocusable(false);
        matchCaseToggleButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        matchCaseToggleButton.setName("matchCaseToggleButton"); // NOI18N
        matchCaseToggleButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        matchCaseToggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                matchCaseToggleButtonActionPerformed(evt);
            }
        });
        findToolBar.add(matchCaseToggleButton);

        multipleMatchesToggleButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/exbin/framework/bined/search/resources/icons/mark_occurrences.png"))); // NOI18N
        multipleMatchesToggleButton.setSelected(true);
        multipleMatchesToggleButton.setToolTipText(resourceBundle.getString("multipleMatchesToggleButton.toolTipText")); // NOI18N
        multipleMatchesToggleButton.setFocusable(false);
        multipleMatchesToggleButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        multipleMatchesToggleButton.setName("multipleMatchesToggleButton"); // NOI18N
        multipleMatchesToggleButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        multipleMatchesToggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                multipleMatchesToggleButtonActionPerformed(evt);
            }
        });
        findToolBar.add(multipleMatchesToggleButton);

        separator1.setName("separator1"); // NOI18N
        findToolBar.add(separator1);

        optionsButton.setText(resourceBundle.getString("optionsButton.text")); // NOI18N
        optionsButton.setFocusable(false);
        optionsButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        optionsButton.setName("optionsButton"); // NOI18N
        optionsButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        optionsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                optionsButtonActionPerformed(evt);
            }
        });
        findToolBar.add(optionsButton);

        infoLabel.setEnabled(false);
        infoLabel.setName("infoLabel"); // NOI18N

        closeToolBar.setBorder(null);
        closeToolBar.setRollover(true);
        closeToolBar.setName("closeToolBar"); // NOI18N

        closeButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/exbin/framework/bined/search/resources/icons/open_icon_library/icons/png/16x16/actions/dialog-cancel-3.png"))); // NOI18N
        closeButton.setToolTipText(resourceBundle.getString("closeButton.toolTipText")); // NOI18N
        closeButton.setFocusable(false);
        closeButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        closeButton.setName("closeButton"); // NOI18N
        closeButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        });
        closeToolBar.add(closeButton);

        javax.swing.GroupLayout findPanelLayout = new javax.swing.GroupLayout(findPanel);
        findPanel.setLayout(findPanelLayout);
        findPanelLayout.setHorizontalGroup(
            findPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, findPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(findLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(findTypeToolBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(findComboBox, 0, 519, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(findToolBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(infoLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(closeToolBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        findPanelLayout.setVerticalGroup(
            findPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(closeToolBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(infoLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(findToolBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(findTypeToolBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(findLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(findComboBox)
        );

        add(findPanel, java.awt.BorderLayout.CENTER);

        replacePanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 0, 0, 0));
        replacePanel.setName("replacePanel"); // NOI18N
        replacePanel.setPreferredSize(new java.awt.Dimension(1015, 28));

        replaceLabel.setText(resourceBundle.getString("replaceLabel.text")); // NOI18N
        replaceLabel.setName("replaceLabel"); // NOI18N

        replaceTypeToolBar.setBorder(null);
        replaceTypeToolBar.setRollover(true);
        replaceTypeToolBar.setFocusable(false);
        replaceTypeToolBar.setName("replaceTypeToolBar"); // NOI18N

        replaceTypeButton.setText(resourceBundle.getString("inputType.text")); // NOI18N
        replaceTypeButton.setToolTipText(resourceBundle.getString("replaceTypeButton.toolTipText")); // NOI18N
        replaceTypeButton.setDefaultCapable(false);
        replaceTypeButton.setFocusable(false);
        replaceTypeButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        replaceTypeButton.setMaximumSize(new java.awt.Dimension(27, 27));
        replaceTypeButton.setMinimumSize(new java.awt.Dimension(27, 27));
        replaceTypeButton.setName("replaceTypeButton"); // NOI18N
        replaceTypeButton.setPreferredSize(new java.awt.Dimension(27, 27));
        replaceTypeButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        replaceTypeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                replaceTypeButtonActionPerformed(evt);
            }
        });
        replaceTypeToolBar.add(replaceTypeButton);

        replaceComboBox.setEditable(true);
        replaceComboBox.setName("replaceComboBox"); // NOI18N

        replaceToolBar.setBorder(null);
        replaceToolBar.setRollover(true);
        replaceToolBar.setFocusable(false);
        replaceToolBar.setName("replaceToolBar"); // NOI18N

        replaceButton.setText(resourceBundle.getString("replaceButton.text")); // NOI18N
        replaceButton.setEnabled(false);
        replaceButton.setFocusable(false);
        replaceButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        replaceButton.setName("replaceButton"); // NOI18N
        replaceButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        replaceButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                replaceButtonActionPerformed(evt);
            }
        });
        replaceToolBar.add(replaceButton);

        replaceAllButton.setText(resourceBundle.getString("replaceAllButton.text")); // NOI18N
        replaceAllButton.setEnabled(false);
        replaceAllButton.setFocusable(false);
        replaceAllButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        replaceAllButton.setName("replaceAllButton"); // NOI18N
        replaceAllButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        replaceAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                replaceAllButtonActionPerformed(evt);
            }
        });
        replaceToolBar.add(replaceAllButton);

        javax.swing.GroupLayout replacePanelLayout = new javax.swing.GroupLayout(replacePanel);
        replacePanel.setLayout(replacePanelLayout);
        replacePanelLayout.setHorizontalGroup(
            replacePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(replacePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(replaceLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(replaceTypeToolBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(replaceComboBox, 0, 734, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(replaceToolBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        replacePanelLayout.setVerticalGroup(
            replacePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(replaceTypeToolBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(replaceComboBox)
            .addComponent(replaceLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(replaceToolBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        add(replacePanel, java.awt.BorderLayout.SOUTH);
    }// </editor-fold>//GEN-END:initComponents

    private void optionsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_optionsButtonActionPerformed
        control.searchOptions();
    }//GEN-LAST:event_optionsButtonActionPerformed

    private void prevMatchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_prevMatchButtonActionPerformed
        control.prevMatch();
    }//GEN-LAST:event_prevMatchButtonActionPerformed

    private void nextMatchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nextMatchButtonActionPerformed
        control.nextMatch();
    }//GEN-LAST:event_nextMatchButtonActionPerformed

    private void multipleMatchesToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_multipleMatchesToggleButtonActionPerformed
        control.notifySearchChanged();
    }//GEN-LAST:event_multipleMatchesToggleButtonActionPerformed

    private void matchCaseToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_matchCaseToggleButtonActionPerformed
        control.notifySearchChanged();
    }//GEN-LAST:event_matchCaseToggleButtonActionPerformed

    private void findTypeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_findTypeButtonActionPerformed
        SearchCondition condition = findComboBoxEditorComponent.getItem();
        if (condition.getSearchMode() == SearchCondition.SearchMode.TEXT) {
            condition.setSearchMode(SearchCondition.SearchMode.BINARY);
        } else {
            condition.setSearchMode(SearchCondition.SearchMode.TEXT);
        }

        findComboBoxEditorComponent.setItem(condition);
        findComboBox.setEditor(findComboBoxEditor);
        findComboBox.invalidate();
        findComboBox.repaint();
        updateFindStatus();
        control.notifySearchChanged();
    }//GEN-LAST:event_findTypeButtonActionPerformed

    public void updateFindStatus() {
        SearchCondition condition = findComboBoxEditorComponent.getItem();
        if (condition.getSearchMode() == SearchCondition.SearchMode.TEXT) {
            findTypeButton.setText(resourceBundle.getString("inputType.text"));
            matchCaseToggleButton.setEnabled(true);
        } else {
            findTypeButton.setText(resourceBundle.getString("inputType.binary"));
            matchCaseToggleButton.setEnabled(false);
        }
    }

    private void updateReplaceStatus() {
        SearchCondition condition = replaceComboBoxEditorComponent.getItem();
        if (condition.getSearchMode() == SearchCondition.SearchMode.TEXT) {
            replaceTypeButton.setText(resourceBundle.getString("inputType.text"));
        } else {
            replaceTypeButton.setText(resourceBundle.getString("inputType.binary"));
        }
    }

    public void clearSearch() {
        findComboBoxEditorComponent.clear();
    }

    private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
        control.close();
    }//GEN-LAST:event_closeButtonActionPerformed

    private void replaceTypeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_replaceTypeButtonActionPerformed
        SearchCondition condition = replaceComboBoxEditorComponent.getItem();
        if (condition.getSearchMode() == SearchCondition.SearchMode.TEXT) {
            condition.setSearchMode(SearchCondition.SearchMode.BINARY);
        } else {
            condition.setSearchMode(SearchCondition.SearchMode.TEXT);
        }

        replaceComboBoxEditorComponent.setItem(condition);
        replaceComboBox.setEditor(replaceComboBoxEditor);
        replaceComboBox.invalidate();
        replaceComboBox.repaint();
        updateReplaceStatus();
    }//GEN-LAST:event_replaceTypeButtonActionPerformed

    private void replaceButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_replaceButtonActionPerformed
        control.performReplace();
    }//GEN-LAST:event_replaceButtonActionPerformed

    private void replaceAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_replaceAllButtonActionPerformed
        control.performReplaceAll();
    }//GEN-LAST:event_replaceAllButtonActionPerformed

    /**
     * Test method for this panel.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        WindowUtils.invokeDialog(new BinarySearchPanel());
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton closeButton;
    private javax.swing.JToolBar closeToolBar;
    private javax.swing.JComboBox<SearchCondition> findComboBox;
    private javax.swing.JLabel findLabel;
    private javax.swing.JPanel findPanel;
    private javax.swing.JToolBar findToolBar;
    private javax.swing.JButton findTypeButton;
    private javax.swing.JToolBar findTypeToolBar;
    private javax.swing.JLabel infoLabel;
    private javax.swing.JToggleButton matchCaseToggleButton;
    private javax.swing.JToggleButton multipleMatchesToggleButton;
    private javax.swing.JButton nextMatchButton;
    private javax.swing.JButton optionsButton;
    private javax.swing.JButton prevMatchButton;
    private javax.swing.JButton replaceAllButton;
    private javax.swing.JButton replaceButton;
    private javax.swing.JComboBox<SearchCondition> replaceComboBox;
    private javax.swing.JLabel replaceLabel;
    private javax.swing.JPanel replacePanel;
    private javax.swing.JToolBar replaceToolBar;
    private javax.swing.JButton replaceTypeButton;
    private javax.swing.JToolBar replaceTypeToolBar;
    private javax.swing.JToolBar.Separator separator1;
    private javax.swing.JSeparator topSeparator;
    // End of variables declaration//GEN-END:variables

    private void comboBoxValueChanged() {
        control.notifySearchChanging();
    }

    public void setApplication(XBApplication application) {
        this.application = application;
    }

    public void requestSearchFocus() {
        findComboBox.requestFocus();
        findComboBoxEditorComponent.requestFocus();
    }

    public void setCodeAreaPopupMenuHandler(CodeAreaPopupMenuHandler codeAreaPopupMenuHandler) {
        findComboBoxEditorComponent.setCodeAreaPopupMenuHandler(codeAreaPopupMenuHandler, "");
    }

    public void updateSearchHistory(SearchCondition condition) {
        findComboBoxEditorComponent.exclusiveUpdate(() -> ((SearchHistoryModel) findComboBox.getModel()).addSearchCondition(condition));
    }

    public interface Control {

        void prevMatch();

        void nextMatch();

        void performEscape();

        void performFind();

        void performReplace();

        void performReplaceAll();

        /**
         * Parameters of search have changed.
         */
        void notifySearchChanged();

        /**
         * Parameters of search are changing which might not lead to immediate
         * search change.
         * <p>
         * Typically, text typing.
         */
        void notifySearchChanging();

        @Nonnull
        SearchParameters.SearchDirection getSearchDirection();

        void searchOptions();

        void close();
    }

    public enum PanelMode {
        FIND, REPLACE
    }
}
