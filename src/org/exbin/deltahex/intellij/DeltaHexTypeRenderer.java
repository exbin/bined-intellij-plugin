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
package org.exbin.deltahex.intellij;

import com.intellij.debugger.DebuggerContext;
import com.intellij.debugger.engine.evaluation.EvaluateException;
import com.intellij.debugger.engine.evaluation.EvaluationContext;
import com.intellij.debugger.ui.tree.DebuggerTreeNode;
import com.intellij.debugger.ui.tree.NodeDescriptor;
import com.intellij.debugger.ui.tree.ValueDescriptor;
import com.intellij.debugger.ui.tree.render.ChildrenBuilder;
import com.intellij.debugger.ui.tree.render.DescriptorLabelListener;
import com.intellij.debugger.ui.tree.render.NodeRendererImpl;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NonNls;

/**
 * Debugger byte[] type data viewer.
 *
 * @author ExBin Project (http://exbin.org)
 * @version 0.1.6 2018/02/22
 */
public class DeltaHexTypeRenderer extends NodeRendererImpl {

    public static final @NonNls
    String UNIQUE_ID = "DeltaHex.TypeRenderer";

    @Override
    public void buildChildren(com.sun.jdi.Value value, ChildrenBuilder builder, EvaluationContext evaluationContext) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public PsiElement getChildValueExpression(DebuggerTreeNode node, DebuggerContext context) throws EvaluateException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isExpandable(com.sun.jdi.Value value, EvaluationContext evaluationContext, NodeDescriptor parentDescriptor) {
        return false;
    }

    @Override
    public String calcLabel(ValueDescriptor descriptor, EvaluationContext evaluationContext, DescriptorLabelListener listener) throws EvaluateException {
        return "TEST";
    }

    @Override
    public String getUniqueId() {
        return UNIQUE_ID;
    }

    @Override
    public boolean isApplicable(com.sun.jdi.Type type) {
        return true;
    }
}
