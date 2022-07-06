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
package org.exbin.bined.intellij.debug.c;

import com.intellij.xdebugger.XDebuggerManager;
import com.intellij.xdebugger.evaluation.XDebuggerEvaluator;
import com.intellij.xdebugger.frame.XValuePlace;
import com.intellij.xdebugger.impl.XDebuggerManagerImpl;
import com.intellij.xdebugger.impl.ui.tree.nodes.XValueNodeImpl;
import com.jetbrains.cidr.execution.debugger.backend.LLValue;
import com.jetbrains.cidr.execution.debugger.evaluation.CidrElementValue;
import com.jetbrains.cidr.execution.debugger.evaluation.CidrLocalValue;
import com.jetbrains.cidr.execution.debugger.evaluation.CidrPhysicalValue;
import com.sun.jdi.*;
import org.exbin.bined.intellij.data.PageProvider;
import org.exbin.bined.intellij.data.PageProviderBinaryData;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * C character array data source for debugger view.
 *
 * TODO
 *
 * @author ExBin Project (http://exbin.org)
 * @version 0.2.3 2020/03/23
 */
@ParametersAreNonnullByDefault
public class CCharArrayPageProvider implements PageProvider {

    private final ArrayReference arrayRef;

    public CCharArrayPageProvider(XValueNodeImpl myDataNode, CidrPhysicalValue cidrValue) {
        byte[] data = cidrValue.getPreparedVarData().splitNumberAndData().second.getBytes(StandardCharsets.UTF_8);
        LLValue value = cidrValue.getPresentationVar();
//        ValueRenderer preparedRenderer = cidrValue.getPreparedRenderer();
//        preparedRenderer.getChildEvaluationExpression()
//
//        EvaluationContext evaluationContext = cidrValue.createEvaluationContext();
//
//        NSCollectionValueRenderer renderer = new NSCollectionValueRenderer(cidrValue, NSCollectionValueRenderer.Kind.ARRAY);
//        renderer.computeMayHaveChildren(evaluationContext);

        ((CidrLocalValue) cidrValue).computeValueChildren(myDataNode);
        CidrElementValue cidrElementValue = new CidrElementValue(value, "", cidrValue, 0, false);
        cidrElementValue.computePresentation(myDataNode, XValuePlace.TREE);
        LLValue var = cidrElementValue.getVar();
        cidrElementValue.computeChildren(myDataNode);
        XDebuggerManager debuggerManager = XDebuggerManagerImpl.getInstance(myDataNode.getTree().getProject());
        XDebuggerEvaluator evaluator = debuggerManager.getCurrentSession().getCurrentStackFrame().getEvaluator();
//        myDataNode.eva
        //        myDataNode.getChildCount()
//        KeyFMap userMap = var.getUserData(GDBDriver.LLVALUE_DATA);
//        LLValueData preparedVarData = cidrElementValue.getPreparedVarData();

//        cidrElementValue.getContainer().getPresentationVarData(cidrValue.getProcess().getDebuggerContext()); // cidrElementValue.createEvaluationContext()
//        cidrValue.computePresentation();
//        Object userData = cidrValue.getUserData(new Key<>("GDBDriver.LLVALUE_DATA"));
        this.arrayRef = null;
    }

    @Nonnull
    @Override
    public byte[] getPage(long pageIndex) {
        int startPos = (int) (pageIndex * PageProviderBinaryData.PAGE_SIZE);
        int length = PageProviderBinaryData.PAGE_SIZE;
        if (arrayRef.length() - startPos < PageProviderBinaryData.PAGE_SIZE) {
            length = arrayRef.length() - startPos;
        }
        final List<Value> values = arrayRef.getValues(startPos, length);
        byte[] result = new byte[length];
        for (int i = 0; i < values.size(); i++) {
            Value rawValue = values.get(i);
            byte value = 0;
            if (rawValue instanceof ObjectReference) {
                Field field = ((ObjectReference) rawValue).referenceType().fieldByName("value");
                rawValue = ((ObjectReference) rawValue).getValue(field);
            }

            if (rawValue instanceof ByteValue) {
                value = ((ByteValue) rawValue).value();
            }

            result[i] = value;
        }

        return result;
    }

    @Override
    public long getDocumentSize() {
        return arrayRef == null ? 0 : arrayRef.length();
    }
}
