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

import com.intellij.openapi.util.Pair;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.util.SmartList;
import com.intellij.xdebugger.frame.XCompositeNode;
import com.intellij.xdebugger.frame.XDebuggerTreeNodeHyperlink;
import com.intellij.xdebugger.frame.XValue;
import com.intellij.xdebugger.frame.XValueChildrenList;
import com.intellij.xdebugger.impl.ui.tree.nodes.XValueNodeImpl;
import org.exbin.bined.intellij.data.PageProvider;
import org.exbin.bined.intellij.data.PageProviderBinaryData;
import org.exbin.framework.bined.gui.ValuesPanel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.Icon;
import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;

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
    private ValueNodeSegment valueNodeSegment = null;

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
                    XValue valueNode = getValueNode(i / childValueType.valueByteSize);
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

    @Nonnull
    private XValue getValueNode(int position) {
        int segmentIndex = position / 100;
        int segmentPosition = segmentIndex * 100;
        int segmentOffset = position % 100;
        if (valueNodeSegment == null || (valueNodeSegment.getTargetPosition() != segmentPosition)) {
            valueNodeSegment = new ValueNodeSegment(valueNode, segmentPosition);

            valueNode.getValueContainer().computeChildren(valueNodeSegment);
            valueNodeSegment.waitFor();
        }
        return valueNodeSegment.childrenList.get(segmentOffset);
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

    private static class ValueNodeSegment implements XCompositeNode {
        public static final int TIMEOUT_MS = 25_000;

        private final int targetPosition;
        private final XValueNodeImpl valueNode;
        private int position = 0;
        private List<XValue> childrenList = new SmartList<>();
        private String myErrorMessage;
        private final Semaphore myFinished = new Semaphore(0);

        public ValueNodeSegment(XValueNodeImpl valueNode, int targetPosition) {
            this.valueNode = valueNode;
            this.targetPosition = targetPosition;
        }

        public int getTargetPosition() {
            return targetPosition;
        }

        @Override
        public void addChildren(@NotNull XValueChildrenList valueChildrenList, boolean b) {
            int listSize = valueChildrenList.size();
            for (int i = 0; i < listSize; i++) {
                if (position >= targetPosition) {
                    childrenList.add(valueChildrenList.getValue(i));
                }
                position++;
            }
        }

        public void tooManyChildren(int remaining) {
            myFinished.release();
        }

        @Override
        public void tooManyChildren(int remaining, @NotNull Runnable addNextChildren) {
            XCompositeNode.super.tooManyChildren(remaining, addNextChildren);
        }

        @Override
        public void setAlreadySorted(boolean b) {
        }

        public void setMessage(@NotNull String message, Icon icon, @NotNull final SimpleTextAttributes attributes, @Nullable XDebuggerTreeNodeHyperlink link) {
        }

        public void setErrorMessage(@NotNull String message, @Nullable XDebuggerTreeNodeHyperlink link) {
            setErrorMessage(message);
        }

        public void setErrorMessage(@NotNull String errorMessage) {
            myErrorMessage = errorMessage;
            myFinished.release();
        }

        @NotNull
        public Pair<List<XValue>, String> waitFor() {
            return waitFor(TIMEOUT_MS);
        }

        @NotNull
        public Pair<List<XValue>, String> waitFor(long timeoutMs) {
            return waitFor(timeoutMs, (semaphore, timeout) -> waitFor(myFinished, timeout));
        }

        @NotNull
        public Pair<List<XValue>, String> waitFor(long timeoutMs, BiFunction<? super Semaphore, ? super Long, Boolean> waitFunction) {
            if (!waitFunction.apply(myFinished, timeoutMs)) {
                throw new AssertionError("Waiting timed out");
            }

            return Pair.create(childrenList, myErrorMessage);
        }

        public static boolean waitFor(Semaphore semaphore, long timeoutInMillis) {
            long end = System.currentTimeMillis() + timeoutInMillis;
            long remaining = timeoutInMillis;
            do {
                try {
                    return semaphore.tryAcquire(remaining, TimeUnit.MILLISECONDS);
                }
                catch (InterruptedException ignored) {
                    remaining = end - System.currentTimeMillis();
                }
            } while (remaining > 0);
            return false;
        }
    }
}
