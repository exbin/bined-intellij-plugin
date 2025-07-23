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
package org.exbin.bined.intellij.search.gui;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.actionSystem.ex.DefaultCustomComponentAction;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.AnActionButton;
import com.intellij.ui.components.JBPanel;
import org.exbin.auxiliary.binary_data.array.ByteArrayEditableData;
import org.exbin.bined.CodeAreaUtils;
import org.exbin.bined.RowWrappingMode;
import org.exbin.bined.ScrollBarVisibility;
import org.exbin.bined.color.CodeAreaBasicColors;
import org.exbin.bined.section.layout.SectionCodeAreaLayoutProfile;
import org.exbin.bined.section.theme.SectionBackgroundPaintMode;
import org.exbin.bined.swing.section.SectCodeArea;
import org.exbin.bined.swing.section.SectionCodeAreaPainter;
import org.exbin.bined.swing.section.color.SectionCodeAreaColorProfile;
import org.exbin.bined.swing.section.theme.SectionCodeAreaThemeProfile;
import org.exbin.framework.App;
import org.exbin.framework.bined.handler.CodeAreaPopupMenuHandler;
import org.exbin.framework.bined.search.ReplaceParameters;
import org.exbin.framework.bined.search.SearchCondition;
import org.exbin.framework.bined.search.SearchHistoryModel;
import org.exbin.framework.bined.search.SearchParameters;
import org.exbin.framework.bined.search.gui.BinarySearchComboBoxPanel;
import org.exbin.framework.bined.search.gui.BinarySearchPanel;
import org.exbin.framework.language.api.LanguageModuleApi;
import org.exbin.framework.utils.TestApplication;
import org.exbin.framework.utils.WindowUtils;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.AbstractAction;
import javax.swing.ComboBoxEditor;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.Objects;

/**
 * Binary editor search panel.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class BinarySearchIntelliJPanel extends JPanel {

    private final java.util.ResourceBundle resourceBundle = App.getModule(LanguageModuleApi.class).getBundle(BinarySearchPanel.class);

    private Controller controller = null;

    private final SectCodeArea searchCodeArea = new SectCodeArea();

    private BinarySearchPanel.PanelMode panelMode = BinarySearchPanel.PanelMode.REPLACE;
    private ComboBoxEditor findComboBoxEditor;
    private BinarySearchComboBoxPanel findComboBoxEditorComponent;
    private ComboBoxEditor replaceComboBoxEditor;
    private BinarySearchComboBoxPanel replaceComboBoxEditorComponent;

    private static final String TOOLBAR_PLACE = "BinEdBinarySearchPanel";
    private final DefaultActionGroup findToolbarActionGroup;
    private final ActionToolbar findToolbar;
    private final DefaultActionGroup closeToolbarActionGroup;
    private final ActionToolbar closeToolbar;
    private final DefaultActionGroup replaceToolbarActionGroup;
    private final ActionToolbar replaceToolbar;

    private final DefaultCustomComponentAction optionsAction;
    private final AnActionButton prevMatchAction;
    private final AnActionButton nextMatchAction;
    private final ToggleAction matchCaseToggleAction;
    private boolean matchCase = false;
    private boolean matchCaseEnabled = true;
    private final ToggleAction multipleMatchesToggle;
    private boolean multipleMatches = true;
    private final JButton replaceButton;
    private final DefaultCustomComponentAction replaceAction;
    private final JButton replaceAllButton;
    private final DefaultCustomComponentAction replaceAllAction;

    public BinarySearchIntelliJPanel() {
        findToolbarActionGroup = new DefaultActionGroup();
        findToolbar = ActionManager.getInstance().createActionToolbar(TOOLBAR_PLACE + ".find",
                findToolbarActionGroup, true);
        closeToolbarActionGroup = new DefaultActionGroup();
        closeToolbar = ActionManager.getInstance().createActionToolbar(TOOLBAR_PLACE + ".close",
                closeToolbarActionGroup, true);
        replaceToolbarActionGroup = new DefaultActionGroup();
        replaceToolbar = ActionManager.getInstance().createActionToolbar(TOOLBAR_PLACE + ".replace",
                replaceToolbarActionGroup, true);

        optionsAction = new DefaultCustomComponentAction(
                () -> new JButton(new AbstractAction(resourceBundle.getString("optionsButton.text")) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        controller.searchOptions();
                    }
                })
        ) {
            @Nonnull
            @Override
            public ActionUpdateThread getActionUpdateThread() {
                return ActionUpdateThread.BGT;
            }
        };

        prevMatchAction = new AnActionButton(
                resourceBundle.getString("prevMatchButton.toolTipText"),
                null,
                new javax.swing.ImageIcon(getClass().getResource(resourceBundle.getString("prevMatchButton.icon")))
        ) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                controller.prevMatch();
            }

            @Nonnull
            @Override
            public ActionUpdateThread getActionUpdateThread() {
                return ActionUpdateThread.BGT;
            }
        };
        prevMatchAction.setEnabled(false);

        nextMatchAction = new AnActionButton(
                resourceBundle.getString("nextMatchButton.toolTipText"),
                null,
                new javax.swing.ImageIcon(getClass().getResource(resourceBundle.getString("nextMatchButton.icon")))
        ) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                controller.nextMatch();
            }

            @Nonnull
            @Override
            public ActionUpdateThread getActionUpdateThread() {
                return ActionUpdateThread.BGT;
            }
        };
        nextMatchAction.setEnabled(false);

        replaceButton = new JButton(new AbstractAction(resourceBundle.getString("replaceButton.text")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.performReplace();
            }
        });
        replaceAction = new DefaultCustomComponentAction(
                () -> replaceButton
        ) {
            @Nonnull
            @Override
            public ActionUpdateThread getActionUpdateThread() {
                return ActionUpdateThread.BGT;
            }
        };

        replaceAllButton = new JButton(new AbstractAction(resourceBundle.getString("replaceAllButton.text")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.performReplaceAll();
            }
        });
        // TODO
        replaceAllButton.setEnabled(false);
        replaceAllAction = new DefaultCustomComponentAction(
                () -> replaceAllButton
        ) {
            @Nonnull
            @Override
            public ActionUpdateThread getActionUpdateThread() {
                return ActionUpdateThread.BGT;
            }
        };

        matchCaseToggleAction = new ToggleAction(
                resourceBundle.getString("matchCaseToggleButton.toolTipText"),
                null,
                new javax.swing.ImageIcon(getClass().getResource(resourceBundle.getString("matchCaseToggleButton.icon")))
        ) {
            @NotNull
            @Override
            public ActionUpdateThread getActionUpdateThread() {
                return ActionUpdateThread.BGT;
            }

            @Override
            public boolean isSelected(@NotNull AnActionEvent e) {
                return matchCase;
            }

            @Override
            public void setSelected(@NotNull AnActionEvent anActionEvent, boolean selected) {
                matchCase = selected;
                controller.notifySearchChanged();
            }

            @Override
            public void update(@NotNull AnActionEvent e) {
                super.update(e);
                e.getPresentation().setEnabled(matchCaseEnabled);
            }
        };

        multipleMatchesToggle = new ToggleAction(
                resourceBundle.getString("multipleMatchesToggleButton.toolTipText"),
                null,
                new javax.swing.ImageIcon(getClass().getResource(resourceBundle.getString("multipleMatchesToggleButton.icon")))
        ) {
            @NotNull
            @Override
            public ActionUpdateThread getActionUpdateThread() {
                return ActionUpdateThread.BGT;
            }

            @Override
            public boolean isSelected(@NotNull AnActionEvent e) {
                return multipleMatches;
            }

            @Override
            public void setSelected(@NotNull AnActionEvent anActionEvent, boolean selected) {
                multipleMatches = selected;
                controller.notifySearchChanged();
            }
        };

        initComponents();
        init();
    }

    public void setTargetComponent(JComponent targetComponent) {
        findToolbar.setTargetComponent(targetComponent);
        closeToolbar.setTargetComponent(targetComponent);
        replaceToolbar.setTargetComponent(targetComponent);
    }

    private void init() {
        findToolbarActionGroup.addSeparator();
        findToolbarActionGroup.addAction(optionsAction);
        findPanel.add(findToolbar.getComponent(), BorderLayout.CENTER);

        closeToolbarActionGroup.addAction(new AnAction(
                resourceBundle.getString("closeButton.toolTipText"),
                null,
                new javax.swing.ImageIcon(getClass().getResource(resourceBundle.getString("closeButton.icon")))
        ) {
            @Override
            public void actionPerformed(@Nonnull AnActionEvent e) {
                controller.close();
            }

            @Nonnull
            @Override
            public ActionUpdateThread getActionUpdateThread() {
                return ActionUpdateThread.BGT;
            }
        });
        add(closeToolbar.getComponent(), BorderLayout.EAST);

        SectionCodeAreaLayoutProfile layoutProfile = Objects.requireNonNull(searchCodeArea.getLayoutProfile());
        layoutProfile.setShowHeader(false);
        layoutProfile.setShowRowPosition(false);

        searchCodeArea.setLayoutProfile(layoutProfile);
        SectionCodeAreaThemeProfile themeProfile = searchCodeArea.getThemeProfile();
        themeProfile.setBackgroundPaintMode(SectionBackgroundPaintMode.PLAIN);
        searchCodeArea.setThemeProfile(themeProfile);

        searchCodeArea.setBorder(null);
        searchCodeArea.setRowWrapping(RowWrappingMode.WRAPPING);
        searchCodeArea.setWrappingBytesGroupSize(0);
        searchCodeArea.setVerticalScrollBarVisibility(ScrollBarVisibility.NEVER);
        searchCodeArea.setHorizontalScrollBarVisibility(ScrollBarVisibility.NEVER);
        searchCodeArea.setContentData(new ByteArrayEditableData(new byte[0]));

        final KeyAdapter editorKeyListener = new KeyAdapter() {
            @Override
            public void keyPressed(@Nonnull KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    controller.performEscape();
                }
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    controller.performFind();
                }
            }
        };

        findComboBoxEditorComponent = new BinarySearchComboBoxPanel();
        findComboBox.setMinimumSize(new Dimension(300, 27));
        findComboBox.setRenderer(new ListCellRenderer<>() {
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
                    SectionCodeAreaPainter painter = (SectionCodeAreaPainter) searchCodeArea.getPainter();
                    SectionCodeAreaColorProfile colorsProfile = (SectionCodeAreaColorProfile) painter.getColorsProfile();
                    colorsProfile.setColor(CodeAreaBasicColors.TEXT_BACKGROUND, backgroundColor);
                    return searchCodeArea;
                }
            }
        });
        findComboBoxEditor = new ComboBoxEditor() {
            @Nonnull
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
        replaceComboBox.setMinimumSize(new Dimension(300, 27));
        replaceComboBox.setRenderer(new ListCellRenderer<>() {
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
                    SectionCodeAreaPainter painter = (SectionCodeAreaPainter) searchCodeArea.getPainter();
                    SectionCodeAreaColorProfile colorsProfile = (SectionCodeAreaColorProfile) painter.getColorsProfile();
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

    public void setController(Controller controller) {
        this.controller = controller;
    }

    public void setInfoLabel(String text) {
        infoLabel.setText(text);
    }

    @Nonnull
    public SearchParameters getSearchParameters() {
        SearchParameters searchParameters = new SearchParameters();
        searchParameters.setMatchCase(matchCase);
        searchParameters.setMatchMode(SearchParameters.MatchMode.fromBoolean(multipleMatches));
        SearchParameters.SearchDirection searchDirection = controller.getSearchDirection();
        searchParameters.setSearchDirection(searchDirection);

        long startPosition;
        if (searchParameters.isSearchFromCursor()) {
            startPosition = searchCodeArea.getDataPosition();
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
        prevMatchAction.setEnabled(prevMatchAvailable);
        nextMatchAction.setEnabled(nextMatchAvailable);
        replaceButton.setEnabled(hasMatches);
//        replaceAllButton.setEnabled(hasMatches);
    }

    public void setSearchHistory(List<SearchCondition> history) {
        findComboBox.setModel(new SearchHistoryModel(history));
    }

    public void setReplaceHistory(List<SearchCondition> history) {
        replaceComboBox.setModel(new SearchHistoryModel(history));
    }

    public void switchPanelMode(BinarySearchPanel.PanelMode panelMode) {
        if (this.panelMode != panelMode) {
            this.panelMode = panelMode;
            if (panelMode == BinarySearchPanel.PanelMode.REPLACE) {
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
        findPanel = new JBPanel<>();
        findPanel.setLayout(new BorderLayout());
        findLabel = new javax.swing.JLabel();
        findTypeButton = new javax.swing.JButton();
        findComboBox = new ComboBox<>();
        infoLabel = new javax.swing.JLabel();
        replacePanel = new JBPanel<>();
        replacePanel.setLayout(new BorderLayout());
        replaceLabel = new javax.swing.JLabel();
        replaceTypeButton = new javax.swing.JButton();
        replaceComboBox = new ComboBox<>();

        setName("Form"); // NOI18N
        setLayout(new java.awt.BorderLayout());

        topSeparator.setName("topSeparator"); // NOI18N

        findPanel.setName("findPanel"); // NOI18N

        findLabel.setText(resourceBundle.getString("findLabel.text")); // NOI18N
        findLabel.setName("findLabel"); // NOI18N
        findToolbarActionGroup.addAction(new DefaultCustomComponentAction(() -> findLabel));

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
        findToolbarActionGroup.addAction(new DefaultCustomComponentAction(() -> findTypeButton));

        findComboBox.setEditable(true);
        findComboBox.setName("findComboBox"); // NOI18N
        findToolbarActionGroup.addAction(new DefaultCustomComponentAction(() -> findComboBox));

        findToolbarActionGroup.addAction(prevMatchAction);
        findToolbarActionGroup.addAction(nextMatchAction);
        findToolbarActionGroup.addAction(matchCaseToggleAction);
        findToolbarActionGroup.addAction(multipleMatchesToggle);

        infoLabel.setEnabled(false);
        infoLabel.setName("infoLabel"); // NOI18N
        infoLabel.setMinimumSize(new Dimension(50, 27));
        findToolbarActionGroup.addAction(new DefaultCustomComponentAction(() -> infoLabel));

        add(findPanel, java.awt.BorderLayout.CENTER);

        replacePanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 0, 0, 0));
        replacePanel.setName("replacePanel"); // NOI18N
        replacePanel.setPreferredSize(new java.awt.Dimension(1015, 28));

        replaceLabel.setText(resourceBundle.getString("replaceLabel.text")); // NOI18N
        replaceLabel.setName("replaceLabel"); // NOI18N
        replaceToolbarActionGroup.addAction(new DefaultCustomComponentAction(() -> replaceLabel));

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
        replaceToolbarActionGroup.addAction(new DefaultCustomComponentAction(() -> replaceTypeButton));

        replaceComboBox.setEditable(true);
        replaceComboBox.setName("replaceComboBox"); // NOI18N
        replaceToolbarActionGroup.addAction(new DefaultCustomComponentAction(() -> replaceComboBox));
        replaceToolbarActionGroup.addSeparator();
        replaceToolbarActionGroup.addAction(replaceAction);
        replaceToolbarActionGroup.addAction(replaceAllAction);

        replacePanel.add(replaceToolbar.getComponent(), BorderLayout.CENTER);

        add(replacePanel, java.awt.BorderLayout.SOUTH);
    }// </editor-fold>//GEN-END:initComponents

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
        controller.notifySearchChanged();
    }//GEN-LAST:event_findTypeButtonActionPerformed

    public void updateFindStatus() {
        SearchCondition condition = findComboBoxEditorComponent.getItem();
        if (condition.getSearchMode() == SearchCondition.SearchMode.TEXT) {
            findTypeButton.setText(resourceBundle.getString("inputType.text"));
            matchCaseEnabled = true;
        } else {
            findTypeButton.setText(resourceBundle.getString("inputType.binary"));
            matchCaseEnabled = false;
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

    /**
     * Test method for this panel.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        TestApplication.run(() -> WindowUtils.invokeWindow(new BinarySearchIntelliJPanel()));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JComboBox<SearchCondition> findComboBox;
    private javax.swing.JLabel findLabel;
    private JPanel findPanel;
    private javax.swing.JButton findTypeButton;
    private javax.swing.JLabel infoLabel;
    private JComboBox<SearchCondition> replaceComboBox;
    private javax.swing.JLabel replaceLabel;
    private JPanel replacePanel;
    private javax.swing.JButton replaceTypeButton;
    private javax.swing.JSeparator topSeparator;
    // End of variables declaration//GEN-END:variables

    private void comboBoxValueChanged() {
        controller.notifySearchChanging();
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

    public interface Controller {

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
}
