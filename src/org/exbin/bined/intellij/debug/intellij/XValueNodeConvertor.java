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

import com.google.common.util.concurrent.AbstractFuture;
import com.intellij.debugger.engine.JavaValue;
import com.intellij.debugger.ui.impl.watch.ValueDescriptorImpl;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.psi.CommonClassNames;
import com.intellij.xdebugger.frame.XFullValueEvaluator;
import com.intellij.xdebugger.frame.XValue;
import com.intellij.xdebugger.frame.XValuePlace;
import com.intellij.xdebugger.impl.ui.tree.actions.XDebuggerTreeActionBase;
import com.intellij.xdebugger.impl.ui.tree.nodes.XValueNodeImpl;
import com.jetbrains.cidr.execution.debugger.evaluation.CidrPhysicalValue;
import com.jetbrains.cidr.execution.debugger.evaluation.CidrValue;
import com.jetbrains.php.debug.common.PhpNavigatableValue;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.jetbrains.python.debugger.PyDebugValue;
import com.sun.jdi.ArrayReference;
import com.sun.jdi.ArrayType;
import com.sun.jdi.ByteValue;
import com.sun.jdi.CharValue;
import com.sun.jdi.DoubleValue;
import com.sun.jdi.Field;
import com.sun.jdi.FloatValue;
import com.sun.jdi.IntegerValue;
import com.sun.jdi.LongValue;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ShortValue;
import com.sun.jdi.Type;
import com.sun.jdi.Value;
import org.exbin.auxiliary.paged_data.BinaryData;
import org.exbin.auxiliary.paged_data.ByteArrayData;
import org.exbin.bined.intellij.data.PageProviderBinaryData;
import org.exbin.bined.intellij.debug.DebugViewDataProvider;
import org.exbin.bined.intellij.debug.DefaultDebugViewDataProvider;
import org.exbin.bined.intellij.debug.c.CCharArrayPageProvider;
import org.exbin.bined.intellij.debug.jdi.JdiBooleanArrayPageProvider;
import org.exbin.bined.intellij.debug.jdi.JdiByteArrayPageProvider;
import org.exbin.bined.intellij.debug.jdi.JdiCharArrayPageProvider;
import org.exbin.bined.intellij.debug.jdi.JdiDoubleArrayPageProvider;
import org.exbin.bined.intellij.debug.jdi.JdiFloatArrayPageProvider;
import org.exbin.bined.intellij.debug.jdi.JdiIntegerArrayPageProvider;
import org.exbin.bined.intellij.debug.jdi.JdiLongArrayPageProvider;
import org.exbin.bined.intellij.debug.jdi.JdiShortArrayPageProvider;
import org.exbin.bined.intellij.debug.php.PhpByteArrayPageProvider;
import org.exbin.bined.intellij.debug.python.PythonByteArrayPageProvider;
import org.exbin.framework.bined.gui.ValuesPanel;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.awt.Font;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Debug values convertor.
 *
 * @author ExBin Project (http://exbin.org)
 * @version 0.2.6 2022/05/15
 */
@ParametersAreNonnullByDefault
public class XValueNodeConvertor {

    private static boolean classesDetected = false;
    private static boolean javaValueClassAvailable = false;
    private static boolean pythonValueClassAvailable = false;
    private static boolean cValueClassAvailable = false;
    private static boolean dotNetValueClassAvailable = false;

    private static final String JAVA_VALUE_CLASS = "com.intellij.debugger.engine.JavaValue";
    private static final String PYTHON_VALUE_CLASS = "com.jetbrains.python.debugger.PyDebugValue";
    private static final String PHP_VALUE_CLASS = "com.jetbrains.php.debug.xdebug.debugger.XdebugValue";
    private static final String C_VALUE_CLASS = "com.jetbrains.cidr.execution.debugger.evaluation.CidrValue";
    private static final String DOTNET_VALUE_CLASS = "com.jetbrains.rider.debugger.DotNetNamedValue";

    private final byte[] valuesCache = new byte[8];
    private final ByteBuffer byteBuffer = ByteBuffer.wrap(valuesCache);

    private static void detectClasses() {
        classesDetected = true;

        try {
            Class.forName(JAVA_VALUE_CLASS);
            javaValueClassAvailable = true;
        } catch (ClassNotFoundException ignore) {
        }

        try {
            Class.forName(PYTHON_VALUE_CLASS);
            pythonValueClassAvailable = true;
        } catch (ClassNotFoundException ignore) {
        }

        try {
            Class.forName(C_VALUE_CLASS);
            cValueClassAvailable = true;
        } catch (ClassNotFoundException ignore) {
        }

        try {
            Class.forName(DOTNET_VALUE_CLASS);
            dotNetValueClassAvailable = true;
        } catch (ClassNotFoundException ignore) {
        }
    }

    public XValueNodeConvertor() {
    }

    @Nonnull
    public List<DebugViewDataProvider> identifyAvailableProviders(XValueNodeImpl myDataNode, @Nullable String initialValue) {
        if (!classesDetected) detectClasses();

        List<DebugViewDataProvider> providers = new ArrayList<>();

        if (myDataNode != null) {
            XValue container = myDataNode.getValueContainer();
            if (javaValueClassAvailable && container instanceof JavaValue) {
                ValueDescriptorImpl descriptor = ((JavaValue) container).getDescriptor();
                if (descriptor.isPrimitive() || isBasicType(descriptor) || !descriptor.isNull()) {
                    if (descriptor.isArray()) {
                        BinaryData data = processArrayData(descriptor);
                        if (data != null)
                            providers.add(new DefaultDebugViewDataProvider("binary sequence from array", data));
                    } else {
                        BinaryData data = processSimpleValue(descriptor);
                        if (data != null) {
                            providers.add(new DefaultDebugViewDataProvider("binary value", data));
                        }
                    }
                }
            }

//                if (dotNetValueClassAvailable && container instanceof DotNetNamedValue) {
//                    DotNetNamedValue namedValue = (DotNetNamedValue) container;
//                    ObjectProxy objectProxy = namedValue.getObjectProxy();
//                    DotNetValue value = new DotNetValue(namedValue.getFrame(), objectProxy, namedValue.getLifetime(), namedValue.getSessionId());
////                    value.
//                    ObjectPropertiesProxy properties = objectProxy.getProperties();
//                    if (properties.isArray()) {
//                        switch (properties.getType()) {
//
//                        }
//                    }
//                }

            if (pythonValueClassAvailable && container instanceof PyDebugValue) {
                String dataType = ((PyDebugValue) container).getType();
                switch (dataType) {
                    case "bytearray":
                    case "bytes": {
                        // Very primitive and inefficient data reading using existing readers via string
                        try {
                            String fullValue = myDataNode.getRawValue();
                            if (initialValue != null && !initialValue.isEmpty()) {
                                fullValue = initialValue;
                            } else if (fullValue == null || fullValue.isEmpty()) {
                                PyValueFuture value = new PyValueFuture(myDataNode);
                                fullValue = value.get();
//                            PyDebugValue debugValue = (PyDebugValue) container;
//                            XDebuggerTree parentTree = myDataNode.getTree();
//                            XSourcePosition sourcePosition = debugValue.getFrameAccessor().getSourcePositionForType(debugValue.getType());
//                            XDebuggerTree tree = new XDebuggerTree(project, parentTree.getEditorsProvider(), sourcePosition, "XDebugger.Inspect.Tree.Popup", parentTree.getValueMarkers());
//                            XValueNodeImpl fullValueNode = new XValueNodeImpl(parentTree, (XDebuggerTreeNode)null, debugValue.getName(), debugValue);
//                            debugValue.computePresentation(fullValueNode, XValuePlace.TREE);
//                            PyFullValueEvaluator fullValueEvaluator = new PyFullValueEvaluator(debugValue.getFrameAccessor(), debugValue.getEvaluationExpression());
//                            fullValueNode.getRawValue()
                            }
                            BinaryData data = new PageProviderBinaryData(new PythonByteArrayPageProvider(fullValue));
                            providers.add(new DefaultDebugViewDataProvider("Python bytearray value", data));
                        } catch (Exception e) {
                            Logger.getLogger(XValueNodeConvertor.class.getName()).log(Level.SEVERE, null, e);
                        }
                    }
                }
            }

            if (cValueClassAvailable && container instanceof CidrValue) {
                String dataType = ((CidrValue) container).getEvaluationExpression(true);
                switch (dataType) {
                    case "byteArray": {
                        BinaryData data = new PageProviderBinaryData(new CCharArrayPageProvider(myDataNode, (CidrPhysicalValue) container));
                        providers.add(new DefaultDebugViewDataProvider("C bytearray value", data));
                        break;
                    }
                    default: {

                    }
                }
            }

            String valueCanonicalName = container.getClass().getCanonicalName();
            if (PHP_VALUE_CLASS.equals(valueCanonicalName)) {
                try {
                    PhpType dataType = ((PhpNavigatableValue) container).getType();

                    switch (dataType.toString()) {
                        case "array": {
                            Map<String, String> value = ((PhpNavigatableValue) container).getLoadedChildren();
                            BinaryData data = new PageProviderBinaryData(new PhpByteArrayPageProvider(value));
                            providers.add(new DefaultDebugViewDataProvider("PHP bytearray value", data));
                            break;
                        }
                    }
                } catch (Exception e) {
                    Logger.getLogger(XValueNodeConvertor.class.getName()).log(Level.SEVERE, null, e);
                }
            }

//                else if (phpValueClassAvailable && container instanceof XdebugValue) {
//                    PhpType dataType = ((XdebugValue) container).getType();
//                    switch (dataType) {
//                        case
//                    }
//                }

            String rawValue = myDataNode.getRawValue();
            if (rawValue != null) {
                BinaryData data = new ByteArrayData(rawValue.getBytes(Charset.defaultCharset()));
                providers.add(new DefaultDebugViewDataProvider("RAW value", data));
            }
        }

        providers.add(new DebugViewDataProvider() {
            @Nonnull
            @Override
            public String getName() {
                return "toString()";
            }

            @Nonnull
            @Override
            public BinaryData getData() {
                if (initialValue != null) {
                    return new ByteArrayData(initialValue.getBytes(Charset.defaultCharset()));
                } else {
                    return new ByteArrayData();
                }
            }
        });

        return providers;
    }

    @Nonnull
    public static Optional<XValueNodeImpl> getDataNode(AnActionEvent event) {
        if (!classesDetected) detectClasses();

        List<XValueNodeImpl> selectedNodes = XDebuggerTreeActionBase.getSelectedNodes(event.getDataContext());
        if (selectedNodes.size() == 1) {
            XValueNodeImpl node = selectedNodes.get(0);
            XValue container = node.getValueContainer();
            if (javaValueClassAvailable && container instanceof JavaValue) {
                ValueDescriptorImpl descriptor = ((JavaValue) container).getDescriptor();
                if (descriptor.isString() || descriptor.isArray() || descriptor.isPrimitive() || isBasicType(descriptor)) {
                    return Optional.of(node);
                }
            }

            if (pythonValueClassAvailable && container instanceof PyDebugValue) {
                return Optional.of(node);
            }

//            if (dotNetValueClassAvailable && container instanceof DotNetNamedValue) {
//                return Optional.of(node);
//            }

            if (cValueClassAvailable && container instanceof CidrValue) {
                return Optional.of(node);
            }

            String valueCanonicalName = container.getClass().getCanonicalName();
            if (PHP_VALUE_CLASS.equals(valueCanonicalName)) {
                return Optional.of(node);
            }
        }
        return Optional.empty();
    }

    @Nullable
    private static BinaryData processArrayData(ValueDescriptorImpl descriptor) {
        final ArrayReference arrayRef = (ArrayReference) descriptor.getValue();
        final ArrayType arrayType = (ArrayType) descriptor.getType();
        if (arrayType != null) {
            final String componentType = arrayType.componentTypeName();
            switch (componentType) {
                case CommonClassNames.JAVA_LANG_BOOLEAN:
                case "boolean": {
                    return new PageProviderBinaryData(new JdiBooleanArrayPageProvider(arrayRef));
                }
                case CommonClassNames.JAVA_LANG_BYTE:
                case "byte": {
                    return new PageProviderBinaryData(new JdiByteArrayPageProvider(arrayRef));
                }
                case CommonClassNames.JAVA_LANG_SHORT:
                case "short": {
                    return new PageProviderBinaryData(new JdiShortArrayPageProvider(arrayRef));
                }
                case CommonClassNames.JAVA_LANG_INTEGER:
                case "int": {
                    return new PageProviderBinaryData(new JdiIntegerArrayPageProvider(arrayRef));
                }
                case CommonClassNames.JAVA_LANG_LONG:
                case "long": {
                    return new PageProviderBinaryData(new JdiLongArrayPageProvider(arrayRef));
                }
                case CommonClassNames.JAVA_LANG_FLOAT:
                case "float": {
                    return new PageProviderBinaryData(new JdiFloatArrayPageProvider(arrayRef));
                }
                case CommonClassNames.JAVA_LANG_DOUBLE:
                case "double": {
                    return new PageProviderBinaryData(new JdiDoubleArrayPageProvider(arrayRef));
                }
                case CommonClassNames.JAVA_LANG_CHARACTER:
                case "char": {
                    return new PageProviderBinaryData(new JdiCharArrayPageProvider(arrayRef));
                }
            }
        }

        return null;
    }

    @Nullable
    private BinaryData processSimpleValue(ValueDescriptorImpl descriptor) {
        String typeString = descriptor.getDeclaredType();
        if (typeString == null) {
            Type type = descriptor.getType();
            if (type == null)
                return null;

            typeString = type.signature();
        }

        switch (typeString) {
            case CommonClassNames.JAVA_LANG_BYTE:
            case "B":
            case "byte": {
                ByteValue value = (ByteValue) getPrimitiveValue(descriptor);
                byte[] byteArray = new byte[1];
                byteArray[0] = value.value();
                return new ByteArrayData(byteArray);
            }
            case CommonClassNames.JAVA_LANG_SHORT:
            case "S":
            case "short": {
                ShortValue valueRecord = (ShortValue) getPrimitiveValue(descriptor);
                byte[] byteArray = new byte[2];
                short value = valueRecord.value();
                byteArray[0] = (byte) (value >> 8);
                byteArray[1] = (byte) (value & 0xff);
                return new ByteArrayData(byteArray);
            }
            case CommonClassNames.JAVA_LANG_INTEGER:
            case "I":
            case "int": {
                IntegerValue valueRecord = (IntegerValue) getPrimitiveValue(descriptor);
                byte[] byteArray = new byte[4];
                int value = valueRecord.value();
                byteArray[0] = (byte) (value >> 24);
                byteArray[1] = (byte) ((value >> 16) & 0xff);
                byteArray[2] = (byte) ((value >> 8) & 0xff);
                byteArray[3] = (byte) (value & 0xff);
                return new ByteArrayData(byteArray);
            }
            case CommonClassNames.JAVA_LANG_LONG:
            case "J":
            case "long": {
                LongValue valueRecord = (LongValue) getPrimitiveValue(descriptor);
                byte[] byteArray = new byte[8];
                long value = valueRecord.value();
                BigInteger bigInteger = BigInteger.valueOf(value);
                for (int bit = 0; bit < 7; bit++) {
                    BigInteger nextByte = bigInteger.and(ValuesPanel.BIG_INTEGER_BYTE_MASK);
                    byteArray[7 - bit] = nextByte.byteValue();
                    bigInteger = bigInteger.shiftRight(8);
                }
                return new ByteArrayData(byteArray);
            }
            case CommonClassNames.JAVA_LANG_FLOAT:
            case "F":
            case "float": {
                FloatValue valueRecord = (FloatValue) getPrimitiveValue(descriptor);
                byte[] byteArray = new byte[4];
                float value = valueRecord.value();
                byteBuffer.rewind();
                byteBuffer.putFloat(value);
                System.arraycopy(valuesCache, 0, byteArray, 0, 4);
                return new ByteArrayData(byteArray);
            }
            case CommonClassNames.JAVA_LANG_DOUBLE:
            case "D":
            case "double": {
                DoubleValue valueRecord = (DoubleValue) getPrimitiveValue(descriptor);
                byte[] byteArray = new byte[8];
                double value = valueRecord.value();
                byteBuffer.rewind();
                byteBuffer.putDouble(value);
                System.arraycopy(valuesCache, 0, byteArray, 0, 8);
                return new ByteArrayData(byteArray);
            }
            case CommonClassNames.JAVA_LANG_CHARACTER:
            case "C":
            case "char": {
                CharValue valueRecord = (CharValue) getPrimitiveValue(descriptor);
                byte[] byteArray = new byte[2];
                char value = valueRecord.value();
                byteBuffer.rewind();
                byteBuffer.putChar(value);
                System.arraycopy(valuesCache, 0, byteArray, 0, 2);
                return new ByteArrayData(byteArray);
            }
        }

        return null;
    }

    @Nonnull
    private static Value getPrimitiveValue(ValueDescriptorImpl descriptor) {
        if (descriptor.isPrimitive())
            return descriptor.getValue();

        Field field = ((ObjectReference) descriptor.getValue()).referenceType().fieldByName("value");
        Value value = ((ObjectReference) descriptor.getValue()).getValue(field);
        return value;
    }

    private static boolean isBasicType(ValueDescriptorImpl descriptor) {
        final String type = descriptor.getDeclaredType();
        return CommonClassNames.JAVA_LANG_BOOLEAN.equals(type)
                || CommonClassNames.JAVA_LANG_BYTE.equals(type)
                || CommonClassNames.JAVA_LANG_SHORT.equals(type)
                || CommonClassNames.JAVA_LANG_INTEGER.equals(type)
                || CommonClassNames.JAVA_LANG_LONG.equals(type)
                || CommonClassNames.JAVA_LANG_FLOAT.equals(type)
                || CommonClassNames.JAVA_LANG_DOUBLE.equals(type)
                || CommonClassNames.JAVA_LANG_CHARACTER.equals(type);
    }

    private static class PyValueFuture extends AbstractFuture<String> {
        public PyValueFuture(@Nonnull XValueNodeImpl dataNode) {
            super();

            XFullValueEvaluator fullValueEvaluator = dataNode.getFullValueEvaluator();
            if (fullValueEvaluator == null) {
                dataNode.getValueContainer().computePresentation(dataNode, XValuePlace.TREE);
                fullValueEvaluator = dataNode.getFullValueEvaluator();

                if (fullValueEvaluator == null) {
                    throw new UnsupportedOperationException("Unable to create value evaluator");
                    // TODO: Extend PyFullValueEvaluator instead?
//                    String expression = ((PyDebugValue) dataNode.getValueContainer()).getEvaluationExpression();
//                    PyFrameAccessor myFrameAccessor = ((PyDebugValue) dataNode.getValueContainer()).getFrameAccessor();
//                    fullValueEvaluator = new PyFullValueEvaluator(myFrameAccessor, expression);
                }
            }
            fullValueEvaluator.startEvaluation(new XFullValueEvaluator.XFullValueEvaluationCallback() {
                public boolean isObsolete() {
                    return false;
                }

                @Override
                public void evaluated(@Nonnull String s) {
                    set(s);
                }

                @Override
                public void evaluated(@Nonnull String s, @Nullable Font font) {
                    set(s);
                }

                @Override
                public void errorOccurred(@Nonnull String s) {
                    set(null);
                }
            });
        }
    }
}
