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
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import org.exbin.auxiliary.binary_data.BinaryData;
import org.exbin.auxiliary.binary_data.ByteArrayData;
import org.exbin.auxiliary.binary_data.ByteArrayEditableData;
import org.exbin.auxiliary.binary_data.EditableBinaryData;
import org.exbin.bined.intellij.BinEdPluginStartupActivity;
import org.exbin.bined.intellij.objectdata.gui.DataDialog;
import org.exbin.framework.bined.objectdata.ObjectValueConvertor;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;
import java.util.ResourceBundle;
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

    @Nonnull
    @Override
    public ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
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
                    DataDialog.SetDataListener setDataListener = new DataDialog.SetDataListener() {
                        @Override
                        public void setData(@Nullable BinaryData contentData) {
                            try {
                                SelectionModel<GridRow, GridColumn> selectionModel = grid.getSelectionModel();
                                grid.cancelEditing();
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
                    };
                    DataDialog dialog = new DataDialog(project, setDataListener, binaryData);
                    boolean editable = binaryData instanceof EditableBinaryData;
                    ResourceBundle resourceBundle = dialog.getResourceBundle();
                    dialog.setTitle(editable ? resourceBundle.getString("dialog.title.edit") : resourceBundle.getString("dialog.title"));
                    dialog.show();
                });
            }
        }
    }

    @Override
    public void update(AnActionEvent event) {
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
}
