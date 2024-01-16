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
package org.exbin.bined.intellij.debug.intellij;

import com.intellij.xdebugger.frame.XDebuggerTreeNodeHyperlink;
import com.intellij.xdebugger.frame.XValue;
import com.intellij.xdebugger.frame.XValueNode;
import com.intellij.xdebugger.frame.XValuePlace;
import com.intellij.xdebugger.impl.ui.XValueTextProvider;
import com.intellij.xdebugger.impl.ui.tree.nodes.MessageTreeNode;
import com.intellij.xdebugger.impl.ui.tree.nodes.XValueContainerNode;
import com.intellij.xdebugger.impl.ui.tree.nodes.XValueGroupNodeImpl;
import com.intellij.xdebugger.impl.ui.tree.nodes.XValueNodeImpl;
import org.exbin.auxiliary.binary_data.BinaryData;
import org.exbin.auxiliary.binary_data.ByteArrayEditableData;
import org.exbin.framework.bined.inspector.gui.BasicValuesPanel;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.JButton;
import javax.swing.tree.TreeNode;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;

/**
 * Generic reader for debugger view child nodes.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class ChildNodesPageProvider implements BinaryData {

    private final XValueNodeImpl valueNode;
    private final ValueType childValueType;
    private final ValueExtractor valueExtractor;
    private final long childrenCount;

    private byte[] dataCache = new byte[0];
    private long dataCachePosition = 0;

    public ChildNodesPageProvider(XValueNodeImpl valueNode, ValueType childValueType, long childrenCount, ValueExtractor valueExtractor) {
        this.childValueType = childValueType;
        this.valueNode = valueNode;
        this.childrenCount = childrenCount;
        this.valueExtractor = valueExtractor;
    }

    @Nonnull
    private XValue getValueNode(int position) {
        int childCount = valueNode.getChildCount();
        TreeNode firstChild = valueNode.getChildAt(0);
        if (firstChild instanceof XValueGroupNodeImpl) {
            int groupIndex = position / 100;
            TreeNode groupNode = valueNode.getChildAt(groupIndex);
            TreeNode firstGroupChild = groupNode.getChildAt(0);
            if (firstGroupChild instanceof MessageTreeNode) {
                XDebuggerTreeNodeHyperlink link = ((MessageTreeNode) firstGroupChild).getLink();
                if (link != null) {
                    link.onClick(new MouseEvent(new JButton("CLICK"), MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(), 0, 0, 0, 2, false));
                    //                int limit = 100;
                    //                while (limit > 0 && childCount <= valueNode.getChildCount()) {
                    //                    try {
                    //                        Thread.sleep(100);
                    //                    } catch (InterruptedException e) {
                    //                        e.printStackTrace();
                    //                    }
                    //                    limit--;
                    //                }
                }
            }
            try {
                TreeNode child = groupNode.getChildAt(position % 100);
                return ((XValueContainerNode<XValue>) child).getValueContainer();
            } catch (IndexOutOfBoundsException | ClassCastException ex) {
                return new XValue() {
                    @Override
                    public void computePresentation(@Nonnull XValueNode xValueNode, @Nonnull XValuePlace xValuePlace) {
                        xValueNode.setPresentation(null, "0", "0", false);
                    }

                    @Nonnull
                    @Override
                    public String toString() {
                        return "0";
                    }
                };
            }
        } else {
            while (position >= childCount - 1) {
                // Emulate click on last item
                try {
                    MessageTreeNode lastChild = (MessageTreeNode) valueNode.getChildAt(childCount - 1);
                    XDebuggerTreeNodeHyperlink link = lastChild.getLink();
                    if (link != null) {
                        link.onClick(new MouseEvent(new JButton("CLICK"), MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(), 0, 0, 0, 2, false));
                        //                int limit = 100;
                        //                while (limit > 0 && childCount <= valueNode.getChildCount()) {
                        //                    try {
                        //                        Thread.sleep(100);
                        //                    } catch (InterruptedException e) {
                        //                        e.printStackTrace();
                        //                    }
                        //                    limit--;
                        //                }
                    }
                } catch (ClassCastException ex) {
                    // Cannot call children loader
                }
                int newChildCount = valueNode.getChildCount();
                if (newChildCount <= childCount) {
                    //                throw new IllegalStateException("Broken loading at position " + newChildCount);
                    break;
                } else {
                    childCount = newChildCount;
                }
            }
            try {
                TreeNode child = valueNode.getChildAt(position);
                return ((XValueContainerNode<XValue>) child).getValueContainer();
            } catch (IndexOutOfBoundsException | ClassCastException ex) {
                return new XValue() {
                    @Override
                    public void computePresentation(@Nonnull XValueNode xValueNode, @Nonnull XValuePlace xValuePlace) {
                        xValueNode.setPresentation(null, "0", "0", false);
                    }

                    @Nonnull
                    @Override
                    public String toString() {
                        return "0";
                    }
                };
            }
        }
    }

    @Override
    public boolean isEmpty() {
        return childrenCount == 0;
    }

    @Override
    public long getDataSize() {
        if (childValueType.valueByteSize == 0) {
            switch (childValueType) {
                case BOOLEAN: {
                    return (childrenCount + 7) / 8;
                }
            }
        }
        return childValueType.valueByteSize * childrenCount;
    }

    @Override
    public byte getByte(long position) {
        switch (childValueType) {
            case BOOLEAN: {
                try {
                    byte result = 0;
                    for (int i = 0; i < 8; i++) {
                        XValue valueContainer = getValueNode((int) position * 8 + i);
                        String valueText = valueExtractor.getValueText(valueContainer);
                        result = (byte) ((result << 1) + (Boolean.parseBoolean(valueText) ? 1 : 0));
                    }
                    return result;
                } catch (NumberFormatException ex) {
                    return 0;
                }
            }
            case BYTE: {
                XValue valueContainer = getValueNode((int) position);
                String valueText = valueExtractor.getValueText(valueContainer);
                try {
                    return (byte) Short.parseShort(valueText);
                } catch (NumberFormatException ex) {
                    return 0;
                }
            }
        }

        if (dataCachePosition <= position && dataCachePosition + dataCache.length > position) {
            return dataCache[(int) (position - dataCachePosition)];
        }

        if (dataCache.length == 0) {
            dataCache = new byte[childValueType.valueByteSize];
        }

        int offset = (int) (position % childValueType.valueByteSize);
        dataCachePosition = position - offset;
        XValue valueContainer = getValueNode((int) (position / childValueType.valueByteSize));
        String valueText = valueExtractor.getValueText(valueContainer);
        switch (childValueType) {
            case SHORT: {
                short value = Short.parseShort(valueText);
                dataCache[0] = (byte) (value >> 8);
                dataCache[1] = (byte) (value & 0xff);
                break;
            }
            case INTEGER: {
                int value = Integer.parseInt(valueText);
                dataCache[0] = (byte) (value >> 24);
                dataCache[1] = (byte) ((value >> 16) & 0xff);
                dataCache[2] = (byte) ((value >> 8) & 0xff);
                dataCache[3] = (byte) (value & 0xff);
                break;
            }
            case LONG: {
                long value = Long.parseLong(valueText);
                BigInteger bigInteger = BigInteger.valueOf(value);
                for (int bit = 0; bit < 7; bit++) {
                    BigInteger nextByte = bigInteger.and(BasicValuesPanel.BIG_INTEGER_BYTE_MASK);
                    dataCache[7 - bit] = nextByte.byteValue();
                    bigInteger = bigInteger.shiftRight(8);
                }
                break;
            }
        }

        return dataCache[offset];
    }

    @Nonnull
    @Override
    public BinaryData copy() {
        return copy(0, getDataSize());
    }

    @Nonnull
    @Override
    public BinaryData copy(long startFrom, long length) {
        ByteArrayEditableData result = new ByteArrayEditableData();
        result.insertUninitialized(0, length);
        for (int i = 0; i < length; i++) {
            result.setByte(i, getByte(startFrom + i));
        }
        return result;
    }

    @Override
    public void copyToArray(long startFrom, byte[] target, int offset, int length) {
        for (int i = 0; i < length; i++) {
            target[offset + i] = getByte(startFrom + i);
        }
    }

    @Override
    public void saveToStream(OutputStream outputStream) throws IOException {
        throw new UnsupportedOperationException("Save to stream is not supported");
    }

    @Nonnull
    @Override
    public InputStream getDataInputStream() {
        throw new UnsupportedOperationException("Save to stream is not supported");
    }

    @Override
    public void dispose() {
    }

    @Nonnull
    public static String getValueText(XValue valueContainer) {
        try {
            return ((XValueTextProvider) valueContainer).getValueText();
        } catch (ClassCastException ex) {
            return valueContainer.toString();
        }
    }

    public interface ValueExtractor {
        @Nonnull
        String getValueText(XValue valueContainer);
    }

    public enum ValueType {
        BOOLEAN(0),
        BYTE(1),
        SHORT(2),
        INTEGER(4),
        LONG(8);

        private int valueByteSize;

        ValueType(int valueByteSize) {
            this.valueByteSize = valueByteSize;
        }
    }

//    private static class ValueNodeSegment implements XCompositeNode {
//        public static final int TIMEOUT_MS = 25_000;
//
//        private final int targetPosition;
//        private final XValueNodeImpl valueNode;
//        private int position = 0;
//        private List<XValue> childrenList = new SmartList<>();
//        private String myErrorMessage;
//        private final Semaphore myFinished = new Semaphore(0);
//
//        public ValueNodeSegment(XValueNodeImpl valueNode, int targetPosition) {
//            this.valueNode = valueNode;
//            this.targetPosition = targetPosition;
//        }
//
//        public int getTargetPosition() {
//            return targetPosition;
//        }
//
//        @Override
//        public void addChildren(@NotNull XValueChildrenList valueChildrenList, boolean b) {
//            int listSize = valueChildrenList.size();
//            for (int i = 0; i < listSize; i++) {
//                if (position >= targetPosition) {
//                    childrenList.add(valueChildrenList.getValue(i));
//                }
//                position++;
//            }
//        }
//
//        public void tooManyChildren(int remaining) {
//            myFinished.release();
//        }
//
//        @Override
//        public void tooManyChildren(int remaining, @NotNull Runnable addNextChildren) {
//            XCompositeNode.super.tooManyChildren(remaining, addNextChildren);
//        }
//
//        @Override
//        public void setAlreadySorted(boolean b) {
//        }
//
//        public void setMessage(@NotNull String message, Icon icon, @NotNull final SimpleTextAttributes attributes, @Nullable XDebuggerTreeNodeHyperlink link) {
//        }
//
//        public void setErrorMessage(@NotNull String message, @Nullable XDebuggerTreeNodeHyperlink link) {
//            setErrorMessage(message);
//        }
//
//        public void setErrorMessage(@NotNull String errorMessage) {
//            myErrorMessage = errorMessage;
//            myFinished.release();
//        }
//
//        @NotNull
//        public Pair<List<XValue>, String> waitFor() {
//            return waitFor(TIMEOUT_MS);
//        }
//
//        @NotNull
//        public Pair<List<XValue>, String> waitFor(long timeoutMs) {
//            return waitFor(timeoutMs, (semaphore, timeout) -> waitFor(myFinished, timeout));
//        }
//
//        @NotNull
//        public Pair<List<XValue>, String> waitFor(long timeoutMs, BiFunction<? super Semaphore, ? super Long, Boolean> waitFunction) {
//            if (!waitFunction.apply(myFinished, timeoutMs)) {
//                throw new AssertionError("Waiting timed out");
//            }
//
//            return Pair.create(childrenList, myErrorMessage);
//        }
//
//        public static boolean waitFor(Semaphore semaphore, long timeoutInMillis) {
//            long end = System.currentTimeMillis() + timeoutInMillis;
//            long remaining = timeoutInMillis;
//            do {
//                try {
//                    return semaphore.tryAcquire(remaining, TimeUnit.MILLISECONDS);
//                }
//                catch (InterruptedException ignored) {
//                    remaining = end - System.currentTimeMillis();
//                }
//            } while (remaining > 0);
//            return false;
//        }
//    }
}
