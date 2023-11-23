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
package org.exbin.framework.bined.operation.api;

import java.awt.Component;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.bined.EditOperation;
import org.exbin.bined.operation.swing.command.CodeAreaCommand;
import org.exbin.bined.swing.CodeAreaCore;

/**
 * Interface for insert data component.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public interface InsertDataMethod {

    @Nonnull
    String getName();

    @Nonnull
    Component getComponent();

    void initFocus(Component component);

    /**
     * Creates command operation for given component and code area.
     *
     * @param component visual component
     * @param codeArea code area
     * @param position position in code area
     * @param editOperation insert operation type
     * @return
     */
    @Nonnull
    CodeAreaCommand createInsertCommand(Component component, CodeAreaCore codeArea, long position, EditOperation editOperation);

    /**
     * Sets editable data target for preview.
     *
     * @param previewDataHandler preview data handler
     * @param component visual component
     * @param lengthLimit limit to length of set data
     */
    void registerPreviewDataHandler(PreviewDataHandler previewDataHandler, Component component, long lengthLimit);
}
