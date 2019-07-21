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

import org.exbin.bined.capability.CharsetCapable;
import org.exbin.bined.swing.extended.ExtCodeArea;
import org.exbin.framework.bined.options.CodeAreaOptions;
import org.exbin.framework.bined.options.EditorOptions;
import org.exbin.framework.bined.options.StatusOptions;
import org.exbin.framework.editor.text.options.TextEncodingOptions;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.nio.charset.Charset;
import java.util.Objects;

/**
 * Options for apply operation.
 *
 * @version 0.2.1 2019/07/21
 * @author ExBin Project (http://exbin.org)
 */
@ParametersAreNonnullByDefault
public class BinEdApplyOptions {

    private CodeAreaOptions codeAreaOptions = new CodeAreaOptions();
    private TextEncodingOptions encodingOptions = new TextEncodingOptions();
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
    public TextEncodingOptions getEncodingOptions() {
        return encodingOptions;
    }

    public void setEncodingOptions(TextEncodingOptions encodingOptions) {
        this.encodingOptions = Objects.requireNonNull(encodingOptions);
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
        encodingOptions.setSelectedEncoding(((CharsetCapable) codeArea).getCharset().name());
    }

    public void applyToCodeArea(ExtCodeArea codeArea) {
        codeAreaOptions.applyToCodeArea(codeArea);
        ((CharsetCapable) codeArea).setCharset(Charset.forName(encodingOptions.getSelectedEncoding()));
    }
}
