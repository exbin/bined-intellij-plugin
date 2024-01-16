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
import org.exbin.auxiliary.binary_data.BinaryData;
import org.exbin.bined.operation.swing.command.CodeAreaCommand;
import org.exbin.bined.swing.CodeAreaCore;

/**
 * Interface for convert data component.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public interface ConvertDataMethod {

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
     * @return generated command
     */
    @Nonnull
    CodeAreaCommand createConvertCommand(Component component, CodeAreaCore codeArea);

    /**
     * Performs direct convert of the selected data to target binary data
     *
     * @param component visual component
     * @param codeArea code area
     * @return binary data
     */
    @Nonnull
    BinaryData performDirectConvert(Component component, CodeAreaCore codeArea);

    /**
     * Sets editable data target for preview.
     *
     * @param previewDataHandler preview data handler
     * @param component visual component
     * @param codeArea source code area
     * @param lengthLimit limit to length of set data
     */
    @Nonnull
    void registerPreviewDataHandler(PreviewDataHandler previewDataHandler, Component component, CodeAreaCore codeArea, long lengthLimit);
}
