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
package org.exbin.bined.intellij.objectdata.gui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.components.BorderLayoutPanel;
import org.exbin.auxiliary.binary_data.BinaryData;
import org.exbin.auxiliary.binary_data.EditableBinaryData;
import org.exbin.bined.EditMode;
import org.exbin.bined.intellij.main.BinEdManager;
import org.exbin.framework.bined.BinEdEditorComponent;
import org.exbin.framework.bined.BinEdFileManager;
import org.exbin.framework.utils.LanguageUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.Action;
import javax.swing.JComponent;
import java.util.ResourceBundle;

/**
 * Data dialog for binary data.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public final class DataDialog extends DialogWrapper {

    private final java.util.ResourceBundle resourceBundle = LanguageUtils.getResourceBundleByClass(DataDialog.class);

    private final BinEdEditorComponent editorComponent;
    private final SetDataListener setDataListener;
    private final boolean editable;

    public DataDialog(Project project, @Nullable BinaryData binaryData) {
        this(project, null, binaryData);
    }
    public DataDialog(Project project, @Nullable SetDataListener setDataListener, @Nullable BinaryData binaryData) {
        super(project, false);
        this.setDataListener = setDataListener;
        editable = binaryData instanceof EditableBinaryData || (setDataListener != null);

        setModal(false);
        setCancelButtonText(resourceBundle.getString("cancelButton.text"));
        setOKButtonText(resourceBundle.getString("setButton.text"));
        getOKAction().setEnabled(false);
        setOKActionEnabled(editable);
        setCrossClosesWindow(true);

        editorComponent = new BinEdEditorComponent();
        BinEdManager binEdManager = BinEdManager.getInstance();
        BinEdFileManager fileManager = binEdManager.getFileManager();
        fileManager.initComponentPanel(editorComponent.getComponentPanel());
        binEdManager.initEditorComponent(editorComponent);

        editorComponent.setContentData(binaryData);
        if (!editable) {
            editorComponent.getCodeArea().setEditMode(EditMode.READ_ONLY);
        }
        init();
    }

    @Nonnull
    public ResourceBundle getResourceBundle() {
        return resourceBundle;
    }

    @Override
    protected void doOKAction() {
        super.doOKAction();

        if (setDataListener != null) {
            setDataListener.setData(editorComponent.getContentData());
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

    @Nonnull
    @Override
    public JComponent getPreferredFocusedComponent() {
        return editorComponent.getComponentPanel();
    }

    @Nonnull
    @Override
    protected String getDimensionServiceKey() {
        return "#org.exbin.bined.intellij.debug.ViewBinaryAction";
    }

    @Nonnull
    @Override
    protected JComponent createCenterPanel() {
        BorderLayoutPanel panel = JBUI.Panels.simplePanel(editorComponent.getStatusPanel());
        panel.setPreferredSize(JBUI.size(600, 400));
        return panel;
    }

    public interface SetDataListener {

        void setData(@Nullable BinaryData data);
    }
}
