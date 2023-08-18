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
package org.exbin.bined.intellij.inspector.action;

import org.exbin.bined.intellij.gui.BinEdComponentPanel;
import org.exbin.bined.intellij.inspector.BinEdComponentInspector;

import javax.annotation.ParametersAreNonnullByDefault;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Show parsing panel action.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class ShowParsingPanelAction implements ActionListener {

    public static final String ACTION_ID = "showParsingPanelAction";
    private final BinEdComponentPanel binEdComponentPanel;

    public ShowParsingPanelAction(BinEdComponentPanel binEdComponentPanel) {
        this.binEdComponentPanel = binEdComponentPanel;
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        BinEdComponentInspector componentExtension = binEdComponentPanel.getComponentExtension(BinEdComponentInspector.class);
        setShowValuesPanel(!componentExtension.isShowParsingPanel());
    }

    public void setShowValuesPanel(boolean show) {
        BinEdComponentInspector componentExtension = binEdComponentPanel.getComponentExtension(BinEdComponentInspector.class);
        componentExtension.setShowParsingPanel(show);
    }
}
