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
package org.exbin.bined.intellij.main;

import org.exbin.bined.intellij.gui.BinEdComponentPanel;
import org.exbin.bined.swing.CodeAreaCore;
import org.exbin.bined.swing.extended.ExtCodeArea;
import org.exbin.framework.bined.BinEdCodeAreaPainter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.JPopupMenu;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * File manager for binary editor.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class BinEdFileManager {

    private final List<BinEdFileExtension> binEdComponentExtensions = new ArrayList<>();
    private final List<ActionStatusUpdateListener> actionStatusUpdateListeners = new ArrayList<>();
    private final List<BinEdCodeAreaPainter.PositionColorModifier> painterPositionColorModifiers = new ArrayList<>();
    private final List<BinEdCodeAreaPainter.PositionColorModifier> painterPriorityPositionColorModifiers = new ArrayList<>();

    public BinEdFileManager() {
    }

    public void addPainterColorModifier(BinEdCodeAreaPainter.PositionColorModifier modifier) {
        painterPositionColorModifiers.add(modifier);
    }

    public void removePainterColorModifier(BinEdCodeAreaPainter.PositionColorModifier modifier) {
        painterPositionColorModifiers.remove(modifier);
    }

    public void addPainterPriorityColorModifier(BinEdCodeAreaPainter.PositionColorModifier modifier) {
        painterPriorityPositionColorModifiers.add(modifier);
    }

    public void removePainterPriorityColorModifier(BinEdCodeAreaPainter.PositionColorModifier modifier) {
        painterPriorityPositionColorModifiers.remove(modifier);
    }

    public void updateActionStatus(@Nullable CodeAreaCore codeArea) {
        for (ActionStatusUpdateListener listener : actionStatusUpdateListeners) {
            listener.updateActionStatus(codeArea);
        }
    }

    @Nonnull
    public Iterable<BinEdFileExtension> getBinEdComponentExtensions() {
        return binEdComponentExtensions;
    }

    @ParametersAreNonnullByDefault
    public interface BinEdFileExtension {

        @Nonnull
        Optional<BinEdComponentPanel.BinEdComponentExtension> createComponentExtension(BinEdComponentPanel component);

        void onPopupMenuCreation(final JPopupMenu popupMenu, final ExtCodeArea codeArea, String menuPostfix, int x, int y);
    }

    @ParametersAreNonnullByDefault
    public interface ActionStatusUpdateListener {

        void updateActionStatus(CodeAreaCore codeArea);
    }
}
