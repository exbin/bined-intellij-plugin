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

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.JComponent;
import org.exbin.framework.utils.WindowUtils;

/**
 * Properties table cell panel.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class PropertyTableCellPanel extends ComponentPropertyTableCellPanel {

    private final String name;
    @Nullable
    private final Object value;

    public PropertyTableCellPanel(JComponent cellComponent, @Nullable Object value, String name) {
        super(cellComponent);

        this.value = value;
        this.name = name;
        init();
    }

    public PropertyTableCellPanel(@Nullable Object value, String name) {
        super();

        this.value = value;
        this.name = name;
        init();
    }

    private void init() {
        setEditorAction((ActionEvent e) -> {
            if (value == null) {
                return;
            }
            InspectComponentPanel inspectComponentPanel = new InspectComponentPanel();
            inspectComponentPanel.setComponent(value, name);
            final WindowUtils.DialogWrapper dialog = WindowUtils.createDialog(inspectComponentPanel, this, "Inspect Component", Dialog.ModalityType.MODELESS);
            inspectComponentPanel.setCloseActionListener(e1 -> dialog.close());
            dialog.show();
        });
    }
}
