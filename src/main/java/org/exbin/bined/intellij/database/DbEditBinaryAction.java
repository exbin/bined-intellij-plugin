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
package org.exbin.bined.intellij.database;

import com.intellij.database.datagrid.DataGrid;
import com.intellij.database.datagrid.DataGridUtil;
import com.intellij.database.datagrid.GridColumn;
import com.intellij.database.datagrid.GridModel;
import com.intellij.database.datagrid.GridRow;
import com.intellij.database.datagrid.SelectionModel;
import com.intellij.database.extractors.ExtractorsUtil;
import com.intellij.database.extractors.TextInfo;
import com.intellij.database.run.actions.GridAction;
import com.intellij.database.run.ui.DataAccessType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.components.BorderLayoutPanel;
import org.exbin.auxiliary.paged_data.BinaryData;
import org.exbin.auxiliary.paged_data.ByteArrayData;
import org.exbin.auxiliary.paged_data.ByteArrayEditableData;
import org.exbin.auxiliary.paged_data.EditableBinaryData;
import org.exbin.bined.EditMode;
import org.exbin.bined.intellij.BinEdPluginStartupActivity;
import org.exbin.bined.intellij.data.ObjectValueConvertor;
import org.exbin.bined.intellij.gui.BinEdComponentFileApi;
import org.exbin.bined.intellij.gui.BinEdComponentPanel;
import org.exbin.framework.bined.FileHandlingMode;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.Action;
import javax.swing.JComponent;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Edit database cell value as binary data action.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class DbEditBinaryAction extends AnAction implements DumbAware, GridAction {

    private boolean actionVisible = true;
    private final ObjectValueConvertor objectValueConvertor = new ObjectValueConvertor();

    public DbEditBinaryAction() {
        BinEdPluginStartupActivity.addIntegrationOptionsListener(integrationOptions -> actionVisible =
                integrationOptions.isRegisterEditAsBinaryForDbColumn());
    }

    public void actionPerformed(AnActionEvent event) {
        DataGrid grid = DataGridUtil.getDataGrid(event.getDataContext());
        if (grid != null) {
            SelectionModel<GridRow, GridColumn> selectionModel = grid.getSelectionModel();
            GridModel<GridRow, GridColumn> dataModel = grid.getDataModel(DataAccessType.DATA_WITH_MUTATIONS);
            GridColumn column = Objects.requireNonNull(dataModel.getColumn(selectionModel.getSelectedColumn()));
            GridRow row = Objects.requireNonNull(dataModel.getRow(selectionModel.getSelectedRow()));
            Object value = column.getValue(row);

            boolean isBlobType;
            // CoreGridCellEditorHelper.get(grid).guessJdbcTypeForEditing() is obsolete
            switch (ExtractorsUtil.guessJdbcType(column,
                    dataModel.getValueAt(selectionModel.getSelectedRow(), selectionModel.getSelectedColumn()),
                    DataGridUtil.getDbms(grid))) {
            case -4:
            case -3:
            case -2:
            case 2004:
                isBlobType = true;
                break;
            default:
                isBlobType = false;
            }

            BinaryData binaryData;
            if (value instanceof byte[]) {
                binaryData = new ByteArrayEditableData((byte[]) value);
            } else if (value instanceof TextInfo) {
                binaryData = new ByteArrayEditableData(((TextInfo) value).bytes);
            } else if (value == null) {
                binaryData = isBlobType ? new ByteArrayEditableData() : new ByteArrayData();
            } else {
                binaryData = objectValueConvertor.process(value).orElse(null);
            }

            if (binaryData != null) {
                Project project = grid.getProject();
                ApplicationManager.getApplication().invokeLater(() -> {
                    DataDialog dialog = new DataDialog(project, grid, binaryData);
                    boolean editable = binaryData instanceof EditableBinaryData;
                    dialog.setTitle(editable ? "Edit Binary Data" : "View Binary Data");
                    dialog.show();
                });
            }
        }
    }

    @Override
    public void update(AnActionEvent event) {
        super.update(event);
        Presentation presentation = event.getPresentation();
        presentation.setVisible(actionVisible);
        if (actionVisible) {
            boolean enabled = false;
            DataGrid grid = DataGridUtil.getDataGrid(event.getDataContext());
            if (grid != null) {
                SelectionModel<GridRow, GridColumn> selectionModel = grid.getSelectionModel();
                enabled = selectionModel.getSelectedColumnCount() == 1 && selectionModel.getSelectedRowCount() == 1;
            }
            presentation.setEnabled(enabled);
        }
    }

    @ParametersAreNonnullByDefault
    private static class DataDialog extends DialogWrapper {

        private final BinEdComponentPanel viewPanel;
        private final DataGrid grid;
        private final boolean editable;

        private DataDialog(Project project, DataGrid grid, @Nullable BinaryData binaryData) {
            super(project, false);
            this.grid = grid;
            editable = binaryData instanceof EditableBinaryData;

            setModal(false);
            setCancelButtonText("Close");
            setOKButtonText("Set");
            setOKActionEnabled(editable);
            setCrossClosesWindow(true);

            viewPanel = new BinEdComponentPanel();
            viewPanel.setFileApi(new BinEdComponentFileApi() {
                @Override
                public boolean isSaveSupported() {
                    return false;
                }

                @Override
                public void saveDocument() {
                    throw new IllegalStateException("Save not supported");
                }

                @Override
                public void switchFileHandlingMode(FileHandlingMode newHandlingMode) {
                    // Ignore
                }

                @Override
                public void closeData() {
                    // Ignore
                }
            });
            viewPanel.setContentData(binaryData);
            if (!editable) {
                viewPanel.getCodeArea().setEditMode(EditMode.READ_ONLY);
            }
            init();
        }

        @Override
        protected void doOKAction() {
            super.doOKAction();

            try {
                SelectionModel<GridRow, GridColumn> selectionModel = grid.getSelectionModel();
                grid.cancelEditing();
                BinaryData contentData = viewPanel.getContentData();
                int size = contentData != null ? (int) contentData.getDataSize() : 0;
                byte[] resultData = new byte[size];
                if (size > 0) {
                    contentData.copyToArray(0, resultData, 0, size);
                }
                grid.setCells(
                        selectionModel.getSelectedRows(),
                        selectionModel.getSelectedColumns(),
                        resultData
                );
            } catch (Exception ex) {
                Logger.getLogger(DbEditBinaryAction.class.getName()).log(Level.SEVERE, "Unable to set value", ex);
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

        @Nullable
        @Override
        public JComponent getPreferredFocusedComponent() {
            return viewPanel;
        }

        @Nullable
        @Override
        protected String getDimensionServiceKey() {
            return "#org.exbin.bined.intellij.database.ViewBinaryAction";
        }

        @Nullable
        @Override
        protected JComponent createCenterPanel() {
            BorderLayoutPanel panel = JBUI.Panels.simplePanel(viewPanel);
            panel.setPreferredSize(JBUI.size(600, 400));
            return panel;
        }
    }
}
