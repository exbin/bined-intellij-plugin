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
package org.exbin.bined.intellij.options;

import org.exbin.framework.bined.options.CodeAreaColorOptions;
import org.exbin.framework.bined.options.CodeAreaLayoutOptions;
import org.exbin.framework.bined.options.CodeAreaOptions;
import org.exbin.framework.bined.options.CodeAreaThemeOptions;
import org.exbin.framework.bined.options.EditorOptions;
import org.exbin.framework.bined.options.StatusOptions;
import org.exbin.framework.editor.text.options.TextEncodingOptions;
import org.exbin.framework.editor.text.options.TextFontOptions;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Options for apply operation.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public interface BinEdApplyOptions {

    @Nonnull
    CodeAreaOptions getCodeAreaOptions();

    @Nonnull
    TextEncodingOptions getEncodingOptions();

    @Nonnull
    TextFontOptions getFontOptions();

    @Nonnull
    EditorOptions getEditorOptions();

    @Nonnull
    StatusOptions getStatusOptions();

    @Nonnull
    CodeAreaLayoutOptions getLayoutOptions();

    @Nonnull
    CodeAreaColorOptions getColorOptions();

    @Nonnull
    CodeAreaThemeOptions getThemeOptions();
}
