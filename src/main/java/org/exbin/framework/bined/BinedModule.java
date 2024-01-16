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
package org.exbin.framework.bined;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.JPopupMenu;
import org.exbin.bined.basic.BasicCodeAreaZone;
import org.exbin.bined.intellij.main.BinEdManager;
import org.exbin.bined.swing.extended.ExtCodeArea;
import org.exbin.framework.api.XBApplicationModule;
import org.exbin.framework.bined.action.CodeAreaAction;
import org.exbin.framework.bined.handler.CodeAreaPopupMenuHandler;

/**
 * Binary data editor module.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class BinedModule implements XBApplicationModule {

    private PopupMenuVariant popupMenuVariant = PopupMenuVariant.NORMAL;
    private BasicCodeAreaZone popupMenuPositionZone = BasicCodeAreaZone.UNKNOWN;

    private final List<CodeAreaAction> codeAreaActions = new ArrayList<>();

    public BinedModule() {
    }

    @Nonnull
    public BinEdFileManager getFileManager() {
        BinEdManager binEdManager = BinEdManager.getInstance();
        return binEdManager.getFileManager();
    }

    @Nonnull
    public PopupMenuVariant getPopupMenuVariant() {
        return popupMenuVariant;
    }

    @Nonnull
    public BasicCodeAreaZone getPopupMenuPositionZone() {
        return popupMenuPositionZone;
    }

    @Nonnull
    public CodeAreaPopupMenuHandler createCodeAreaPopupMenuHandler(PopupMenuVariant variant) {
        return new CodeAreaPopupMenuHandler() {
            @Override
            public JPopupMenu createPopupMenu(ExtCodeArea codeArea, String menuPostfix, int x, int y) {
                return new JPopupMenu();
            }

            @Override
            public void dropPopupMenu(String menuPostfix) {
            }
        };
    }

    public void addCodeAreaAction(CodeAreaAction codeAreaAction) {
        codeAreaActions.add(codeAreaAction);
    }

    public void removeCodeAreaAction(CodeAreaAction codeAreaAction) {
        codeAreaActions.remove(codeAreaAction);
    }

    public enum PopupMenuVariant {
        BASIC, NORMAL, EDITOR
    }
}
