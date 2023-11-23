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
package org.exbin.framework.bined.tool.content.gui;

import java.awt.datatransfer.DataFlavor;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.AbstractListModel;

/**
 * List model for data flavors.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class DataFlavorsListModel extends AbstractListModel<String> {

    private DataFlavor[] dataFlavors = null;

    public void setDataFlavors(DataFlavor[] dataFlavors) {
        if (this.dataFlavors != null && this.dataFlavors.length > 0) {
            fireIntervalRemoved(this, 0, this.dataFlavors.length - 1);        
        }
        this.dataFlavors = dataFlavors;
        if (dataFlavors.length > 0) {
            fireIntervalAdded(this, 0, dataFlavors.length - 1);        
        }
    }

    @Override
    public int getSize() {
        return dataFlavors == null ? 0 : dataFlavors.length;
    }

    @Nonnull
    @Override
    public String getElementAt(int index) {
        DataFlavor dataFlavor = dataFlavors[index];
        String humanPresentableName = dataFlavor.getHumanPresentableName();
        String mimeType = dataFlavor.getMimeType();
        if (mimeType.startsWith(humanPresentableName)) {
            return mimeType;
        }
            
        return humanPresentableName;
    }
}
