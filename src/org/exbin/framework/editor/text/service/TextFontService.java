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
package org.exbin.framework.editor.text.service;

import java.awt.Font;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Text font panel frame interface.
 *
 * @version 0.2.0 2016/01/23
 * @author ExBin Project (http://exbin.org)
 */
@ParametersAreNonnullByDefault
public interface TextFontService {

    /**
     * Returns current font used in application frame.
     *
     * @return font font value
     */
    @Nonnull
    Font getCurrentFont();

    /**
     * Returns default colors used in application frame.
     *
     * @return font font value
     */
    @Nonnull
    Font getDefaultFont();

    /**
     * Sets current colors used in application frame.
     *
     * @param font font to set
     */
    void setCurrentFont(Font font);
}
