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
package org.exbin.framework.bined.action;

import java.awt.event.ActionEvent;
import java.util.ResourceBundle;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.exbin.bined.extended.layout.ExtendedCodeAreaLayoutProfile;
import org.exbin.bined.swing.CodeAreaCore;
import org.exbin.bined.swing.extended.capability.LayoutProfileCapable;
import org.exbin.framework.api.XBApplication;
import org.exbin.framework.utils.ActionUtils;

/**
 * Show row position action.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class ShowRowPositionAction extends AbstractAction implements CodeAreaAction {

    public static final String ACTION_ID = "showRowPositionAction";

    private CodeAreaCore codeArea;
    private XBApplication application;
    private ResourceBundle resourceBundle;

    public ShowRowPositionAction() {
    }

    public void setup(XBApplication application, ResourceBundle resourceBundle) {
        this.application = application;
        this.resourceBundle = resourceBundle;

        ActionUtils.setupAction(this, resourceBundle, ACTION_ID);
        putValue(ActionUtils.ACTION_TYPE, ActionUtils.ActionType.CHECK);
    }

    @Override
    public void updateForActiveCodeArea(@Nullable CodeAreaCore codeArea) {
        this.codeArea = codeArea;
        setEnabled(codeArea != null);

        if (codeArea != null) {
            putValue(Action.SELECTED_KEY, ((LayoutProfileCapable) codeArea).getLayoutProfile().isShowRowPosition());
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ExtendedCodeAreaLayoutProfile layoutProfile = ((LayoutProfileCapable) codeArea).getLayoutProfile();
        layoutProfile.setShowRowPosition(!layoutProfile.isShowRowPosition());
        ((LayoutProfileCapable) codeArea).setLayoutProfile(layoutProfile);
    }
}
