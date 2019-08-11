package org.exbin.framework.editor.text.options;

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
import java.awt.Font;
import java.awt.font.TextAttribute;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Text font options.
 *
 * @version 0.2.1 2019/07/19
 * @author ExBin Project (http://exbin.org)
 */
@ParametersAreNonnullByDefault
public interface TextFontOptions {

    @Nonnull
    Font getFont(Font initialFont);

    @Nullable
    Map<TextAttribute, Object> getFontAttributes();

    boolean isUseDefaultFont();

    void setFontAttributes(@Nullable Map<TextAttribute, Object> fontAttributes);

    void setUseDefaultFont(boolean useDefaultFont);
}
