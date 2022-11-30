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
package org.exbin.bined.intellij.debug.intellij;

import com.intellij.xdebugger.impl.ui.tree.nodes.XValueNodeImpl;
import org.exbin.bined.intellij.data.PageProvider;
import org.exbin.bined.intellij.data.PageProviderBinaryData;
import org.exbin.framework.bined.gui.ValuesPanel;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.tree.TreeNode;
import java.math.BigInteger;

/**
 * Generic reader for debugger view child nodes.
 *
 * @author ExBin Project (http://exbin.org)
 */
@ParametersAreNonnullByDefault
public class ChildNodesPageProvider implements PageProvider {

    private final XValueNodeImpl valueNode;
    private final ValueType childValueType;
    private final long childrenCount;

    private byte[] pageCache = null;
    private long pageCacheIndex = -1;
    private TreeNode positionCacheNode = null;
    private long positionCacheDepth = -1;

    public ChildNodesPageProvider(XValueNodeImpl valueNode, ValueType childValueType, long childrenCount) {
        this.childValueType = childValueType;
        this.valueNode = valueNode;
        this.childrenCount = childrenCount;
    }

    @Nonnull
    @Override
    public byte[] getPage(long pageIndex) {
        if (pageCacheIndex == pageIndex) {
            return pageCache;
        }

        int startPos = (int) (pageIndex * PageProviderBinaryData.PAGE_SIZE);
        int length = PageProviderBinaryData.PAGE_SIZE;
        long documentSize = getDocumentSize();
        if (documentSize - startPos < PageProviderBinaryData.PAGE_SIZE) {
            length = (int) (documentSize - startPos);
        }

        byte[] result = new byte[length];
        if (childValueType == ValueType.BYTE) {
            for (int i = 0; i < result.length; i++) {
                try {
                    result[i] = Byte.parseByte(getValueNode(startPos + i).toString());
                } catch (NumberFormatException ex) {
                    result[i] = 0;
                }
            }
        } else {
            byte[] childValue = new byte[childValueType.valueByteSize];
            for (int i = 0; i < result.length; i++) {
                int offset = i % childValueType.valueByteSize;
                if (offset == 0) {
                    TreeNode valueNode = getValueNode(i / childValueType.valueByteSize);
                    switch (childValueType) {
                        case SHORT: {
                            short value = Short.parseShort(valueNode.toString());
                            childValue[0] = (byte) (value >> 8);
                            childValue[1] = (byte) (value & 0xff);
                            break;
                        }
                        case INTEGER: {
                            int value = Integer.parseInt(valueNode.toString());
                            childValue[0] = (byte) (value >> 24);
                            childValue[1] = (byte) ((value >> 16) & 0xff);
                            childValue[2] = (byte) ((value >> 8) & 0xff);
                            childValue[3] = (byte) (value & 0xff);
                            break;
                        }
                        case LONG: {
                            long value = Long.parseLong(valueNode.toString());
                            BigInteger bigInteger = BigInteger.valueOf(value);
                            for (int bit = 0; bit < 7; bit++) {
                                BigInteger nextByte = bigInteger.and(ValuesPanel.BIG_INTEGER_BYTE_MASK);
                                childValue[7 - bit] = nextByte.byteValue();
                                bigInteger = bigInteger.shiftRight(8);
                            }
                            break;
                        }
                    }
                }
                result[i] = childValue[offset];
            }
        }

        pageCache = result;
        pageCacheIndex = pageIndex;
        return result;
    }

    private TreeNode getValueNode(long position) {
        long parentNodeDepth = position / 100;
        int parentNodeOffset = (int) (position % 100);
        if (parentNodeDepth != positionCacheDepth) {
            if ((parentNodeDepth < positionCacheDepth) || (positionCacheDepth == -1)) {
                positionCacheDepth = 0;
                positionCacheNode = valueNode;
            }
            while (positionCacheDepth < parentNodeDepth) {
                positionCacheNode = positionCacheNode.getChildAt(100);
                parentNodeDepth++;
            }
        }
        return positionCacheNode.getChildAt(parentNodeOffset);
    }

    @Override
    public long getDocumentSize() {
        return childValueType.valueByteSize * childrenCount;
    }

    public enum ValueType {
        BYTE(1),
        SHORT(2),
        INTEGER(4),
        LONG(8);

        private int valueByteSize;

        ValueType(int valueByteSize) {
            this.valueByteSize = valueByteSize;
        }
    }
}
