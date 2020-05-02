package org.exbin.bined.intellij.debug.c;

import com.intellij.openapi.util.Key;
import com.intellij.xdebugger.frame.XValuePlace;
import com.intellij.xdebugger.impl.ui.tree.nodes.XValueNodeImpl;
import com.jetbrains.cidr.execution.debugger.backend.LLValue;
import com.jetbrains.cidr.execution.debugger.backend.LLValueData;
import com.jetbrains.cidr.execution.debugger.evaluation.CidrElementValue;
import com.jetbrains.cidr.execution.debugger.evaluation.CidrPhysicalValue;
import com.jetbrains.cidr.execution.debugger.evaluation.CidrValue;
import com.sun.jdi.*;
import org.exbin.bined.intellij.debug.DebugViewData;

import java.util.List;

/**
 * C character array data source for debugger view.
 *
 * @author ExBin Project (http://exbin.org)
 * @version 0.2.3 2020/03/23
 */
public class CCharArrayPageProvider implements DebugViewData.PageProvider {

    private final ArrayReference arrayRef;

    public CCharArrayPageProvider(XValueNodeImpl myDataNode, CidrPhysicalValue cidrValue) {
        LLValue value = cidrValue.getPresentationVar();
        CidrElementValue cidrElementValue = new CidrElementValue(value, "", cidrValue, 0, false);
        cidrElementValue.computePresentation(myDataNode, XValuePlace.TREE);
        LLValueData preparedVarData = cidrElementValue.getPreparedVarData();

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
        return arrayRef.length();
    }
}
