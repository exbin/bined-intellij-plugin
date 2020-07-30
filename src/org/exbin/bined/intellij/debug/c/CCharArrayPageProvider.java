package org.exbin.bined.intellij.debug.c;

import com.intellij.xdebugger.frame.XValuePlace;
import com.intellij.xdebugger.impl.ui.tree.nodes.XValueNodeImpl;
import com.jetbrains.cidr.execution.debugger.backend.LLValue;
import com.jetbrains.cidr.execution.debugger.backend.LLValueData;
import com.jetbrains.cidr.execution.debugger.evaluation.CidrElementValue;
import com.jetbrains.cidr.execution.debugger.evaluation.CidrLocalValue;
import com.jetbrains.cidr.execution.debugger.evaluation.CidrPhysicalValue;
import com.sun.jdi.*;
import org.exbin.bined.intellij.debug.DebugViewData;

import java.util.List;

/**
 * C character array data source for debugger view.
 *
 * TODO
 *
 * @author ExBin Project (http://exbin.org)
 * @version 0.2.3 2020/03/23
 */
public class CCharArrayPageProvider implements DebugViewData.PageProvider {

    private final ArrayReference arrayRef;

    public CCharArrayPageProvider(XValueNodeImpl myDataNode, CidrPhysicalValue cidrValue) {
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
//        myDataNode.getChildCount()
//        KeyFMap userMap = var.getUserData(GDBDriver.LLVALUE_DATA);
//        LLValueData preparedVarData = cidrElementValue.getPreparedVarData();

//        cidrElementValue.getContainer().getPresentationVarData(cidrValue.getProcess().getDebuggerContext()); // cidrElementValue.createEvaluationContext()
//        cidrValue.computePresentation();
//        Object userData = cidrValue.getUserData(new Key<>("GDBDriver.LLVALUE_DATA"));
        this.arrayRef = null;
    }

    @Override
    public byte[] getPage(long pageIndex) {
        int startPos = (int) (pageIndex * DebugViewData.PAGE_SIZE);
        int length = DebugViewData.PAGE_SIZE;
        if (arrayRef.length() - startPos < DebugViewData.PAGE_SIZE) {
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
