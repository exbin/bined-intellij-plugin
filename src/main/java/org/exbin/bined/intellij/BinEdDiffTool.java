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
package org.exbin.bined.intellij;

import com.intellij.diff.DiffContext;
import com.intellij.diff.FrameDiffTool;
import com.intellij.diff.requests.ContentDiffRequest;
import com.intellij.diff.requests.DiffRequest;
import com.intellij.diff.tools.util.base.DiffViewerBase;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.vfs.VirtualFile;
import org.exbin.bined.intellij.action.SearchAction;
import org.exbin.bined.swing.extended.ExtCodeArea;
import org.exbin.bined.swing.extended.diff.ExtCodeAreaDiffPanel;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.JComponent;
import java.util.List;

/**
 * BinEd diff support provider to compare binary files.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class BinEdDiffTool implements FrameDiffTool, DumbAware {

    private boolean actionVisible = true;

    public BinEdDiffTool() {
        BinEdPluginStartupActivity.addIntegrationOptionsListener(integrationOptions -> actionVisible = integrationOptions.isRegisterByteToByteDiffTool());
    }

    @Nonnull
    @Nls(capitalization = Nls.Capitalization.Sentence)
    @Override
    public String getName() {
        return "Byte-to-byte compare (BinEd plugin)";
    }

    @Override
    public boolean canShow(DiffContext context, DiffRequest request) {
        return actionVisible;
    }

    @Nonnull
    @Override
    public DiffViewer createComponent(DiffContext context, DiffRequest request) {
        return new DiffViewerBase(context, (ContentDiffRequest) request) {
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
                final ExtCodeAreaDiffPanel diffPanel = new ExtCodeAreaDiffPanel();
                List<VirtualFile> filesToRefresh = request.getFilesToRefresh();
                VirtualFile virtualFile = filesToRefresh.get(0);
                diffPanel.setLeftContentData(new BinEdFileDataWrapper(virtualFile));
                ExtCodeArea leftCodeArea = diffPanel.getLeftCodeArea();
                leftCodeArea.setComponentPopupMenu(SearchAction.createCodeAreaPopupMenu(leftCodeArea, "left"));
                if (filesToRefresh.size() > 1) {
                    VirtualFile secondFile = filesToRefresh.get(1);
                    diffPanel.setRightContentData(new BinEdFileDataWrapper(secondFile));
                }
                ExtCodeArea rightCodeArea = diffPanel.getRightCodeArea();
                rightCodeArea.setComponentPopupMenu(SearchAction.createCodeAreaPopupMenu(rightCodeArea, "right"));

                return diffPanel;
            }

            @Nullable
            @Override
            public JComponent getPreferredFocusedComponent() {
                return null;
            }
        };
    }
}
