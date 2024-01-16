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
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.SwingUtilities;
import org.exbin.auxiliary.binary_data.BinaryData;
import org.exbin.auxiliary.binary_data.ByteArrayEditableData;
import org.exbin.auxiliary.binary_data.EditableBinaryData;
import org.exbin.bined.CodeAreaUtils;
import org.exbin.bined.SelectionRange;
import org.exbin.bined.capability.SelectionCapable;
import org.exbin.bined.operation.swing.command.CodeAreaCommand;
import org.exbin.bined.swing.CodeAreaCore;
import org.exbin.framework.api.XBApplication;
import org.exbin.framework.bined.operation.api.ConvertDataMethod;
import org.exbin.framework.utils.LanguageUtils;
import org.exbin.framework.bined.operation.api.PreviewDataHandler;
import org.exbin.framework.bined.operation.component.gui.BitSwappingDataPanel;
import org.exbin.framework.bined.operation.operation.ConversionDataProvider;
import org.exbin.framework.bined.operation.operation.ConvertDataOperation;

/**
 * Bit swapping data method.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class BitSwappingDataMethod implements ConvertDataMethod {

    private static final int BUFFER_SIZE = 4096;

    private java.util.ResourceBundle resourceBundle = LanguageUtils.getResourceBundleByClass(BitSwappingDataPanel.class);

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
        BitSwappingDataPanel component = new BitSwappingDataPanel();
        return component;
    }

    @Override
    public void initFocus(Component component) {
        ((BitSwappingDataPanel) component).initFocus();
    }

    @Nonnull
    @Override
    public CodeAreaCommand createConvertCommand(Component component, CodeAreaCore codeArea) {
        BitSwappingDataPanel panel = (BitSwappingDataPanel) component;
        Optional<OperationType> operationType = panel.getOperationType();

        long position;
        long length;
        SelectionRange selection = ((SelectionCapable) codeArea).getSelection();
        if (selection.isEmpty()) {
            position = 0;
            length = codeArea.getDataSize();
        } else {
            position = selection.getFirst();
            length = selection.getLength();
        }

        ConversionDataProvider conversionDataProvider = (EditableBinaryData binaryData, long sourcePosition, long sourceLength, long targetPosition) -> {
            convertData(binaryData, sourcePosition, sourceLength, operationType.get(), binaryData, targetPosition);
        };

        return new ConvertDataOperation.ConvertDataCommand(new ConvertDataOperation(codeArea, position, length, length, conversionDataProvider));
    }

    @Override
    public BinaryData performDirectConvert(Component component, CodeAreaCore codeArea) {
        BitSwappingDataPanel panel = (BitSwappingDataPanel) component;
        Optional<OperationType> operationType = panel.getOperationType();
        long position;
        long length;
        SelectionRange selection = ((SelectionCapable) codeArea).getSelection();
        if (selection.isEmpty()) {
            position = 0;
            length = codeArea.getDataSize();
        } else {
            position = selection.getFirst();
            length = selection.getLength();
        }

        EditableBinaryData binaryData = new ByteArrayEditableData();
        convertData(codeArea.getContentData(), position, length, operationType.get(), binaryData, 0);
        return binaryData;
    }

    public void convertData(BinaryData sourceBinaryData, long position, long length, OperationType operationType, EditableBinaryData targetBinaryData, long targetPosition) throws IllegalStateException {
        switch (operationType) {
            case INVERT_BITS: {
                int bufferSize = length < BUFFER_SIZE ? (int) length : BUFFER_SIZE;
                byte[] buffer = new byte[bufferSize];
                long remaining = length;
                while (remaining > 0) {
                    sourceBinaryData.copyToArray(position, buffer, 0, bufferSize);
                    for (int i = 0; i < buffer.length; i++) {
                        buffer[i] ^= 0xff;
                    }
                    remaining -= bufferSize;
                    targetBinaryData.insert(targetPosition, buffer);
                    targetPosition += bufferSize;
                }
                break;
            }
            case REVERT_BYTES_ORDER: {
                int bufferSize = length < BUFFER_SIZE ? (int) length : BUFFER_SIZE;
                byte[] buffer = new byte[bufferSize];
                long remaining = length;
                while (remaining > 0) {
                    sourceBinaryData.copyToArray(position, buffer, 0, bufferSize);
                    for (int i = 0; i < (buffer.length + 1) / 2; i++) {
                        byte swap = buffer[i];
                        buffer[i] = buffer[buffer.length - i - 1];
                        buffer[buffer.length - i - 1] = swap;
                    }
                    remaining -= bufferSize;
                    targetBinaryData.insert(targetPosition, buffer);
                }
                break;
            }
            case SWAP_PAIRS: {
                int bufferSize = length < BUFFER_SIZE ? (int) length : BUFFER_SIZE;
                byte[] buffer = new byte[bufferSize];
                long remaining = length;
                while (remaining > 0) {
                    sourceBinaryData.copyToArray(position, buffer, 0, bufferSize);
                    for (int i = 0; i < buffer.length / 2; i++) {
                        byte swap = buffer[i * 2];
                        buffer[i * 2] = buffer[i * 2 + 1];
                        buffer[i * 2 + 1] = swap;
                    }
                    remaining -= bufferSize;
                    targetBinaryData.insert(targetPosition, buffer);
                    targetPosition += bufferSize;
                }
                break;
            }
            default:
                throw CodeAreaUtils.getInvalidTypeException(operationType);
        }
    }

    @Override
    public void registerPreviewDataHandler(PreviewDataHandler previewDataHandler, Component component, CodeAreaCore codeArea, long lengthLimit) {
        this.previewDataHandler = previewDataHandler;
        this.previewLengthLimit = lengthLimit;
        BitSwappingDataPanel panel = (BitSwappingDataPanel) component;
        panel.setModeChangeListener(() -> {
            fillPreviewData(panel, codeArea);
        });
        fillPreviewData(panel, codeArea);
    }

    private void fillPreviewData(BitSwappingDataPanel panel, CodeAreaCore codeArea) {
        SwingUtilities.invokeLater(() -> {
            Optional<OperationType> operationType = panel.getOperationType();

            EditableBinaryData previewBinaryData = new ByteArrayEditableData();
            previewBinaryData.clear();
            if (operationType.isPresent()) {
                long position;
                long length;
                SelectionRange selection = ((SelectionCapable) codeArea).getSelection();
                if (selection.isEmpty()) {
                    position = 0;
                    length = codeArea.getDataSize();
                } else {
                    position = selection.getFirst();
                    length = selection.getLength();
                }
                convertData(codeArea.getContentData(), position, length, operationType.get(), previewBinaryData, 0);
                long previewDataSize = previewBinaryData.getDataSize();
                if (previewDataSize > previewLengthLimit) {
                    previewBinaryData.remove(previewLengthLimit, previewDataSize - previewLengthLimit);
                }
            }
            previewDataHandler.setPreviewData(previewBinaryData);
        });
    }

    public enum OperationType {
        INVERT_BITS,
        REVERT_BYTES_ORDER,
        SWAP_PAIRS
    }
}
