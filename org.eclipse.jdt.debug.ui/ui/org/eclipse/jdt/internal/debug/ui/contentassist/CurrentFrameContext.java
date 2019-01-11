/*******************************************************************************
 * Copyright (c) 2005, 2018 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.contentassist;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.internal.debug.core.JavaDebugUtils;
import org.eclipse.jdt.internal.debug.core.logicalstructures.JDIPlaceholderVariable;
import org.eclipse.jdt.internal.debug.core.model.JDIThisVariable;


/**
 * Provides a completion context for the active stack frame.
 *
 * @since 3.2
 */
public class CurrentFrameContext extends TypeContext {

    /**
     * Constructs a new completion context for the currently selected
     * stack frame.
     */
    public CurrentFrameContext() {
        super(null, -1);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.internal.debug.ui.contentassist.IJavaDebugContentAssistContext#getType()
     */
    @Override
	public IType getType() throws CoreException {
        IJavaStackFrame frame = getStackFrame();
        if (frame != null) {
            IType type = JavaDebugUtils.resolveDeclaringType(frame);
            if (type != null) {
                return type;
            }
        }
        return super.getType();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.internal.debug.ui.contentassist.IJavaDebugContentAssistContext#getLocalVariables()
     */
    @Override
	public String[][] getLocalVariables() throws CoreException {
        IJavaStackFrame frame = getStackFrame();
        if (frame != null) {
            IVariable[] variables = frame.getVariables();
            int index = 0;
			while (index < variables.length
					&& (variables[index] instanceof JDIThisVariable || JDIPlaceholderVariable.class.isAssignableFrom(variables[index].getClass()))) {
				index++;
			}
            String[][] locals = new String[2][variables.length - index];
            for (int i = 0; i < locals[0].length; i++) {
                IJavaVariable var = (IJavaVariable) variables[index];
                locals[0][i] = var.getName();
                try {
                	locals[1][i] = var.getJavaType().getName();
                }
                catch(DebugException de) {
                	locals[1][i] = var.getReferenceTypeName();
                }
                index++;
            }
            return locals;
        }
        return super.getLocalVariables();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.internal.debug.ui.contentassist.IJavaDebugContentAssistContext#isStatic()
     */
    @Override
	public boolean isStatic() throws CoreException {
        IJavaStackFrame frame = getStackFrame();
        if (frame != null) {
            return frame.isStatic();
        }
        return false;
    }

    /**
     * Returns the currently active stack frame, or <code>null</code>
     * if none.
     *
     * @return the currently active stack frame, or <code>null</code>
     */
    protected IJavaStackFrame getStackFrame() {
        IAdaptable debugContext = DebugUITools.getDebugContext();
        IJavaStackFrame frame = null;
        if (debugContext != null) {
			frame = debugContext.getAdapter(IJavaStackFrame.class);
        }
        return frame;
    }


}
