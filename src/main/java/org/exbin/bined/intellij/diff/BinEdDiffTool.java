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
import com.intellij.diff.FrameDiffTool;
import com.intellij.diff.requests.ContentDiffRequest;
import com.intellij.diff.requests.DiffRequest;
import com.intellij.openapi.project.DumbAware;
import org.exbin.bined.intellij.BinEdPluginStartupActivity;
import org.jetbrains.annotations.Nls;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * BinEd diff support provider to compare binary files.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class BinEdDiffTool implements FrameDiffTool, DumbAware {

    private boolean actionVisible = true;

    public BinEdDiffTool() {
        BinEdPluginStartupActivity.addIntegrationOptionsListener(
                integrationOptions -> actionVisible = integrationOptions.isRegisterByteToByteDiffTool()
        );
    }

    @Nonnull
    @Nls(capitalization = Nls.Capitalization.Sentence)
    @Override
    public String getName() {
        return "Byte-to-byte compare (BinEd plugin)";
    }

    @Override
    public boolean canShow(DiffContext context, DiffRequest request) {
        return actionVisible && request instanceof ContentDiffRequest;
    }

    @Nonnull
    @Override
    public DiffViewer createComponent(DiffContext context, DiffRequest request) {
        return new BinEdDiffViewer(context, (ContentDiffRequest) request);
    }
}
