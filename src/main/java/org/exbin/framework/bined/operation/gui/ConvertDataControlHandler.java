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
package org.exbin.framework.bined.operation.gui;

import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.framework.utils.handler.OkCancelService;

/**
 * Handler for convert data control panel.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public interface ConvertDataControlHandler {

    void controlActionPerformed(ControlActionType actionType);

    @ParametersAreNonnullByDefault
    public interface ClipboardContentControlService extends OkCancelService {

        void performClick(ControlActionType actionType);
    }

    public static enum ControlActionType {
        CONVERT, CANCEL,
        CONVERT_TO_NEW_FILE,
        CONVERT_TO_CLIPBOARD
    }
}
