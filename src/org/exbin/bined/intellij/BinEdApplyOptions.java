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

import org.exbin.bined.swing.extended.ExtCodeArea;
import org.exbin.framework.bined.options.CharsetOptions;
import org.exbin.framework.bined.options.CodeAreaOptions;
import org.exbin.framework.bined.options.EditorOptions;
import org.exbin.framework.bined.options.StatusOptions;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;

/**
 * Options for apply operation.
 *
 * @version 0.2.0 2019/03/16
 * @author ExBin Project (http://exbin.org)
 */
@ParametersAreNonnullByDefault
public class BinEdApplyOptions {

    private CodeAreaOptions codeAreaOptions = new CodeAreaOptions();
    private CharsetOptions charsetOptions = new CharsetOptions();
    private EditorOptions editorOptions = new EditorOptions();
    private StatusOptions statusOptions = new StatusOptions();

    @Nonnull
    public CodeAreaOptions getCodeAreaOptions() {
        return codeAreaOptions;
    }

    public void setCodeAreaOptions(CodeAreaOptions codeAreaOptions) {
        this.codeAreaOptions = Objects.requireNonNull(codeAreaOptions);
    }

    @Nonnull
    public CharsetOptions getCharsetOptions() {
        return charsetOptions;
    }

    public void setCharsetOptions(CharsetOptions charsetOptions) {
        this.charsetOptions = Objects.requireNonNull(charsetOptions);
    }

    @Nonnull
    public EditorOptions getEditorOptions() {
        return editorOptions;
    }

    public void setEditorOptions(EditorOptions editorOptions) {
        this.editorOptions = Objects.requireNonNull(editorOptions);
    }

    @Nonnull
    public StatusOptions getStatusOptions() {
        return statusOptions;
    }

    public void setStatusOptions(StatusOptions statusOptions) {
        this.statusOptions = Objects.requireNonNull(statusOptions);
    }

    public void applyFromCodeArea(ExtCodeArea codeArea) {
        codeAreaOptions.applyFromCodeArea(codeArea);
        charsetOptions.applyFromCodeArea(codeArea);
    }

    public void applyToCodeArea(ExtCodeArea codeArea) {
        codeAreaOptions.applyToCodeArea(codeArea);
        charsetOptions.applyToCodeArea(codeArea);
    }
}
