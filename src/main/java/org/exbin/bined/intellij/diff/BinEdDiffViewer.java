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
package org.exbin.bined.intellij.diff;

import com.intellij.diff.DiffContext;
import com.intellij.diff.requests.ContentDiffRequest;
import com.intellij.diff.tools.util.base.DiffViewerBase;
import com.intellij.openapi.progress.ProgressIndicator;
import org.exbin.bined.intellij.diff.gui.BinedDiffPanel;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.JComponent;

/**
 * BinEd diff support provider to compare binary files.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class BinEdDiffViewer extends DiffViewerBase {

    private final BinedDiffPanel diffPanel = new BinedDiffPanel();

    public BinEdDiffViewer(DiffContext context, ContentDiffRequest request) {
        super(context, request);

    }

    @Nonnull
    @Override
    protected Runnable performRediff(ProgressIndicator indicator) {
        return () -> {
            // no activity
        };
    }

    @Nonnull
    @Override
    public JComponent getComponent() {
        diffPanel.setDiffContent(myRequest);
        return diffPanel;
    }

    @Nonnull
    @Override
    public JComponent getPreferredFocusedComponent() {
        return diffPanel;
    }
}
