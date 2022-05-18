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
package org.exbin.bined.intellij.api;

import com.intellij.openapi.extensions.PluginAware;
import com.intellij.openapi.project.DumbAware;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * BinEd View Data Extension Point Interface.
 *
 * @author ExBin Project (http://exbin.org)
 * @version 0.2.6 2022/05/16
 */
@ParametersAreNonnullByDefault
public interface BinaryViewData extends PluginAware, DumbAware {

    /**
     * Passes handler to available actions.
     *
     * @param handler handler
     */
    void passHandler(BinaryViewHandler handler);
}
