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
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.exbin.bined.PositionCodeType;
import org.exbin.bined.extended.capability.PositionCodeTypeCapable;
import org.exbin.bined.swing.CodeAreaCore;
import org.exbin.framework.api.XBApplication;
import org.exbin.framework.utils.ActionUtils;

/**
 * Position code type actions.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class PositionCodeTypeActions implements CodeAreaAction {

    public static final String OCTAL_POSITION_CODE_TYPE_ACTION_ID = "octalPositionCodeTypeAction";
    public static final String DECIMAL_POSITION_CODE_TYPE_ACTION_ID = "decimalPositionCodeTypeAction";
    public static final String HEXADECIMAL_POSITION_CODE_TYPE_ACTION_ID = "hexadecimalPositionCodeTypeAction";

    public static final String POSITION_CODE_TYPE_RADIO_GROUP_ID = "positionCodeTypeRadioGroup";

    private CodeAreaCore codeArea;
    private XBApplication application;
    private ResourceBundle resourceBundle;

    private Action octalPositionCodeTypeAction;
    private Action decimalPositionCodeTypeAction;
    private Action hexadecimalPositionCodeTypeAction;

    private PositionCodeType positionCodeType = PositionCodeType.HEXADECIMAL;

    public PositionCodeTypeActions() {
    }

    public void setup(XBApplication application, ResourceBundle resourceBundle) {
        this.application = application;
        this.resourceBundle = resourceBundle;
    }

    @Override
    public void updateForActiveCodeArea(@Nullable CodeAreaCore codeArea) {
        this.codeArea = codeArea;
        PositionCodeType activePositionCodeType = codeArea != null ? ((PositionCodeTypeCapable) codeArea).getPositionCodeType() : null;
        if (activePositionCodeType != null) {
            positionCodeType = activePositionCodeType;
        }

        if (octalPositionCodeTypeAction != null) {
            octalPositionCodeTypeAction.setEnabled(codeArea != null);
            if (activePositionCodeType == PositionCodeType.OCTAL) {
                octalPositionCodeTypeAction.putValue(Action.SELECTED_KEY, true);
            }
        }
        if (decimalPositionCodeTypeAction != null) {
            decimalPositionCodeTypeAction.setEnabled(codeArea != null);
            if (activePositionCodeType == PositionCodeType.DECIMAL) {
                decimalPositionCodeTypeAction.putValue(Action.SELECTED_KEY, true);
            }
        }
        if (hexadecimalPositionCodeTypeAction != null) {
            hexadecimalPositionCodeTypeAction.setEnabled(codeArea != null);
            if (activePositionCodeType == PositionCodeType.HEXADECIMAL) {
                hexadecimalPositionCodeTypeAction.putValue(Action.SELECTED_KEY, true);
            }
        }
    }

    public void setCodeType(PositionCodeType codeType) {
        this.positionCodeType = codeType;
        ((PositionCodeTypeCapable) codeArea).setPositionCodeType(codeType);
    }

    @Nonnull
    public Action getOctalCodeTypeAction() {
        if (octalPositionCodeTypeAction == null) {
            octalPositionCodeTypeAction = new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    setCodeType(PositionCodeType.OCTAL);
                }
            };
            ActionUtils.setupAction(octalPositionCodeTypeAction, resourceBundle, OCTAL_POSITION_CODE_TYPE_ACTION_ID);
            octalPositionCodeTypeAction.putValue(ActionUtils.ACTION_TYPE, ActionUtils.ActionType.RADIO);
            octalPositionCodeTypeAction.putValue(ActionUtils.ACTION_RADIO_GROUP, POSITION_CODE_TYPE_RADIO_GROUP_ID);
            octalPositionCodeTypeAction.putValue(Action.SELECTED_KEY, positionCodeType == PositionCodeType.OCTAL);
        }

        return octalPositionCodeTypeAction;
    }

    @Nonnull
    public Action getDecimalCodeTypeAction() {
        if (decimalPositionCodeTypeAction == null) {
            decimalPositionCodeTypeAction = new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    setCodeType(PositionCodeType.DECIMAL);
                }
            };
            ActionUtils.setupAction(decimalPositionCodeTypeAction, resourceBundle, DECIMAL_POSITION_CODE_TYPE_ACTION_ID);
            decimalPositionCodeTypeAction.putValue(ActionUtils.ACTION_RADIO_GROUP, POSITION_CODE_TYPE_RADIO_GROUP_ID);
            decimalPositionCodeTypeAction.putValue(ActionUtils.ACTION_TYPE, ActionUtils.ActionType.RADIO);
            decimalPositionCodeTypeAction.putValue(Action.SELECTED_KEY, positionCodeType == PositionCodeType.DECIMAL);
        }
        return decimalPositionCodeTypeAction;
    }

    @Nonnull
    public Action getHexadecimalCodeTypeAction() {
        if (hexadecimalPositionCodeTypeAction == null) {
            hexadecimalPositionCodeTypeAction = new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    setCodeType(PositionCodeType.HEXADECIMAL);
                }
            };
            ActionUtils.setupAction(hexadecimalPositionCodeTypeAction, resourceBundle, HEXADECIMAL_POSITION_CODE_TYPE_ACTION_ID);
            hexadecimalPositionCodeTypeAction.putValue(ActionUtils.ACTION_TYPE, ActionUtils.ActionType.RADIO);
            hexadecimalPositionCodeTypeAction.putValue(ActionUtils.ACTION_RADIO_GROUP, POSITION_CODE_TYPE_RADIO_GROUP_ID);
            hexadecimalPositionCodeTypeAction.putValue(Action.SELECTED_KEY, positionCodeType == PositionCodeType.HEXADECIMAL);
        }
        return hexadecimalPositionCodeTypeAction;
    }
}
