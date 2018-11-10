/*
 * Copyright (C) ExBin Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.exbin.bined.intellij;

import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Dialog utilities.
 *
 * @author ExBin Project (http://exbin.org)
 * @version 0.2.0 2018/11/10
 */
public class DialogUtils {

    public static final String DIALOG_MENUITEM_EXT = "...";

    public static DialogWrapper createDialog(JComponent dialogPanel, String dialogTitle) {
        return new BinEdDialogWrapper(dialogPanel, dialogTitle);
    }

    public static DialogWrapper createDialog(JComponent dialogPanel, String dialogTitle, JComponent focusedComponent) {
        return new BinEdDialogWrapper(dialogPanel, dialogTitle, focusedComponent);
    }

    public static class BinEdDialogWrapper extends DialogWrapper {

        private final JComponent dialogPanel;
        private final JComponent focusedComponent;

        public BinEdDialogWrapper(JComponent dialogPanel, String dialogTitle) {
            this(dialogPanel, dialogTitle, dialogPanel);
        }

        public BinEdDialogWrapper(JComponent dialogPanel, String dialogTitle, JComponent focusedComponent) {
            super(true);
            this.dialogPanel = dialogPanel;
            this.focusedComponent = focusedComponent;
            setTitle(dialogTitle);
            init();
        }

        @Nullable
        @Override
        public JComponent getPreferredFocusedComponent() {
            return focusedComponent;
        }

        @Nullable
        @Override
        protected JComponent createCenterPanel() {
            return dialogPanel;
        }

        @NotNull
        @Override
        protected Action[] createActions() {
            return new Action[0];
        }
    }
}
