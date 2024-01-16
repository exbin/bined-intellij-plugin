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
package org.exbin.framework.bined.operation.component;

import java.awt.Component;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.auxiliary.binary_data.BinaryData;
import org.exbin.bined.operation.swing.command.CodeAreaCommand;
import org.exbin.bined.swing.CodeAreaCore;
import org.exbin.framework.api.XBApplication;
import org.exbin.framework.bined.operation.api.ConvertDataMethod;
import org.exbin.framework.utils.LanguageUtils;
import org.exbin.framework.bined.operation.api.PreviewDataHandler;
import org.exbin.framework.bined.operation.component.gui.ExportAsCodeDataPanel;

/**
 * Export as code data method.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class ExportAsCodeDataMethod implements ConvertDataMethod {

    private java.util.ResourceBundle resourceBundle = LanguageUtils.getResourceBundleByClass(ExportAsCodeDataPanel.class);

    private XBApplication application;
    private PreviewDataHandler previewDataHandler;
    private long previewLengthLimit = 0;

    public void setApplication(XBApplication application) {
        this.application = application;
    }

    @Nonnull
    @Override
    public String getName() {
        return resourceBundle.getString("component.name");
    }

    @Nonnull
    @Override
    public Component getComponent() {
        return new ExportAsCodeDataPanel();
    }

    @Override
    public void initFocus(Component component) {
        ((ExportAsCodeDataPanel) component).initFocus();
    }

    @Override
    public CodeAreaCommand createConvertCommand(Component component, CodeAreaCore codeArea) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public BinaryData performDirectConvert(Component component, CodeAreaCore codeArea) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void registerPreviewDataHandler(PreviewDataHandler previewDataHandler, Component component, CodeAreaCore codeArea, long lengthLimit) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
