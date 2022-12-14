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

//import com.intellij.database.datagrid.DataGrid;
//import com.intellij.database.datagrid.DataGridUtil;
//import com.intellij.database.run.actions.GridAction;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import org.jetbrains.annotations.NotNull;

/**
 * Edit cell value as binary data action.
 *
 * @author ExBin Project (https://exbin.org)
 */
public class DbEditBinaryAction extends AnAction implements DumbAware { // , GridAction

    public void actionPerformed(@NotNull AnActionEvent e) {
//        DataGrid grid = DataGridUtil.getDataGrid(e.getDataContext());
//        if (grid != null) {
//            grid.cancelEditing();
//            grid.setCells(grid.getSelectionModel().getSelectedRows(), grid.getSelectionModel().getSelectedColumns(), this.myValue);
//        }

    }
}
