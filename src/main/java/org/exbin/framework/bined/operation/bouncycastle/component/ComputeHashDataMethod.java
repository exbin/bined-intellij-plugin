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
package org.exbin.framework.bined.operation.bouncycastle.component;

import java.awt.Component;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.SwingUtilities;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.GOST3411Digest;
import org.bouncycastle.crypto.digests.KeccakDigest;
import org.bouncycastle.crypto.digests.MD2Digest;
import org.bouncycastle.crypto.digests.MD4Digest;
import org.bouncycastle.crypto.digests.MD5Digest;
import org.bouncycastle.crypto.digests.RIPEMD128Digest;
import org.bouncycastle.crypto.digests.RIPEMD160Digest;
import org.bouncycastle.crypto.digests.RIPEMD256Digest;
import org.bouncycastle.crypto.digests.RIPEMD320Digest;
import org.bouncycastle.crypto.digests.SHA1Digest;
import org.bouncycastle.crypto.digests.SHA224Digest;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.digests.SHA384Digest;
import org.bouncycastle.crypto.digests.SHA3Digest;
import org.bouncycastle.crypto.digests.SHA512Digest;
import org.bouncycastle.crypto.digests.SHAKEDigest;
import org.bouncycastle.crypto.digests.SM3Digest;
import org.bouncycastle.crypto.digests.TigerDigest;
import org.bouncycastle.crypto.digests.WhirlpoolDigest;
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
import org.exbin.framework.bined.operation.api.PreviewDataHandler;
import org.exbin.framework.bined.operation.bouncycastle.component.gui.ComputeHashDataPanel;
import org.exbin.framework.bined.operation.operation.ConversionDataProvider;
import org.exbin.framework.bined.operation.operation.ConvertDataOperation;
import org.exbin.framework.utils.LanguageUtils;

/**
 * Compute Hash digest data component.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class ComputeHashDataMethod implements ConvertDataMethod {

    private java.util.ResourceBundle resourceBundle = LanguageUtils.getResourceBundleByClass(ComputeHashDataPanel.class);

    private XBApplication application;
    private PreviewDataHandler previewDataHandler;
    private long previewLengthLimit = 0;
    private HashType lastHashType = null;
    private static final int BUFFER_SIZE = 4096;

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
        ComputeHashDataPanel component = new ComputeHashDataPanel();
        component.setHashTypeChangeListener(() -> {
            HashType hashType = component.getHashType().orElse(null);
            if (lastHashType != hashType) {
                lastHashType = hashType;
                component.setBitSizes(HashType.BIT_SIZES.get(hashType));
            }
        });
        return component;
    }

    @Override
    public void initFocus(Component component) {
        ((ComputeHashDataPanel) component).initFocus();
    }

    @Nonnull
    @Override
    public CodeAreaCommand createConvertCommand(Component component, CodeAreaCore codeArea) {
        ComputeHashDataPanel panel = (ComputeHashDataPanel) component;
        Optional<HashType> hashType = panel.getHashType();
        int bitSize = panel.getBitSize();
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
            convertData(binaryData, sourcePosition, sourceLength, hashType.get(), bitSize, binaryData, targetPosition);
        };

        long convertedDataLength = computeDigestLength(hashType.get(), bitSize);
        return new ConvertDataOperation.ConvertDataCommand(new ConvertDataOperation(codeArea, position, length, convertedDataLength, conversionDataProvider));
    }

    @Nonnull
    @Override
    public BinaryData performDirectConvert(Component component, CodeAreaCore codeArea) {
        ComputeHashDataPanel panel = (ComputeHashDataPanel) component;
        Optional<HashType> hashType = panel.getHashType();
        int bitSize = panel.getBitSize();
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
        convertData(codeArea.getContentData(), position, length, hashType.get(), bitSize, binaryData, 0);
        return binaryData;
    }

    public void convertData(BinaryData sourceBinaryData, long position, long length, HashType hashType, int bitSize, EditableBinaryData targetBinaryData, long targetPosition) throws IllegalStateException {
        Digest digest = getDigest(hashType, bitSize);
        digest.reset();

        int bufferSize = length < BUFFER_SIZE ? (int) length : BUFFER_SIZE;
        byte[] buffer = new byte[bufferSize];
        long remaining = length;
        while (remaining > 0) {
            sourceBinaryData.copyToArray(position, buffer, 0, bufferSize);
            digest.update(buffer, 0, bufferSize);
            remaining -= bufferSize;
        }
        int digestSize = digest.getDigestSize();
        byte[] output = new byte[digestSize];
        digest.doFinal(output, 0);
        targetBinaryData.insert(targetPosition, output);
    }

    public long computeDigestLength(HashType hashType, int bitSize) {
        Digest digest = getDigest(hashType, bitSize);
        return digest.getDigestSize();
    }

    @Nonnull
    private Digest getDigest(HashType hashType, int bitSize) {
        switch (hashType) {
            case GOST3411:
                return new GOST3411Digest();
            case KECCAK:
                return new KeccakDigest(bitSize);
            case MD2:
                return new MD2Digest();
            case MD4:
                return new MD4Digest();
            case MD5:
                return new MD5Digest();
            case RIPEMD: {
                switch (bitSize) {
                    case 128:
                        return new RIPEMD128Digest();
                    case 160:
                        return new RIPEMD160Digest();
                    case 256:
                        return new RIPEMD256Digest();
                    case 320:
                        return new RIPEMD320Digest();
                }
            }
            case SHA1:
                return new SHA1Digest();
            case SHA224:
                return new SHA224Digest();
            case SHA256:
                return new SHA256Digest();
            case SHA384:
                return new SHA384Digest();
            case SHA512:
                return new SHA512Digest();
            case SHA3:
                return new SHA3Digest(bitSize);
            case SHAKE:
                return new SHAKEDigest(bitSize);
            case SM3:
                return new SM3Digest();
            case TIGER:
                return new TigerDigest();
            case WHIRLPOOL:
                return new WhirlpoolDigest();
            default:
                throw CodeAreaUtils.getInvalidTypeException(hashType);
        }
    }

    @Override
    public void registerPreviewDataHandler(PreviewDataHandler previewDataHandler, Component component, CodeAreaCore codeArea, long lengthLimit) {
        this.previewDataHandler = previewDataHandler;
        this.previewLengthLimit = lengthLimit;
        ComputeHashDataPanel panel = (ComputeHashDataPanel) component;
        panel.setModeChangeListener(() -> {
            fillPreviewData(panel, codeArea);
        });
        fillPreviewData(panel, codeArea);
    }

    private void fillPreviewData(ComputeHashDataPanel panel, CodeAreaCore codeArea) {
        SwingUtilities.invokeLater(() -> {
            Optional<HashType> hashType = panel.getHashType();
            int bitSize = panel.getBitSize();

            EditableBinaryData previewBinaryData = new ByteArrayEditableData();
            previewBinaryData.clear();
            if (hashType.isPresent()) {
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
                convertData(codeArea.getContentData(), position, length, hashType.get(), bitSize, previewBinaryData, 0);
                long previewDataSize = previewBinaryData.getDataSize();
                if (previewDataSize > previewLengthLimit) {
                    previewBinaryData.remove(previewLengthLimit, previewDataSize - previewLengthLimit);
                }
            }
            previewDataHandler.setPreviewData(previewBinaryData);
        });
    }

    public enum HashType {
        KECCAK,
        MD2,
        MD4,
        MD5,
        RIPEMD,
        SHA1,
        SHA224,
        SHA256,
        SHA384,
        SHA512,
        SHA3,
        SHAKE,
        SM3,
        TIGER,
        GOST3411,
        WHIRLPOOL;

        public static Map<HashType, List<Integer>> BIT_SIZES = new HashMap<HashType, List<Integer>>() {
            {
                put(KECCAK, Arrays.asList(224, 256, 288, 384, 512));
                put(RIPEMD, Arrays.asList(128, 160, 256, 320));
                put(SHA3, Arrays.asList(224, 256, 384, 512));
                put(SHAKE, Arrays.asList(128, 256));
            }
        };
    }
}
