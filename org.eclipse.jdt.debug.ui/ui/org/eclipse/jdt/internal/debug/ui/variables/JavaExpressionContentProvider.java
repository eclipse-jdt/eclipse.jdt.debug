/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.variables;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.internal.ui.model.elements.ExpressionContentProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.internal.debug.core.logicalstructures.JDIPlaceholderVariable;
import org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget;
import org.eclipse.jdt.internal.debug.core.model.JDIPlaceholderValue;
import org.eclipse.jdt.internal.debug.core.model.JDIReferenceListVariable;

/**
 * Provides content for the result of an inspect operation that is displayed in the expressions view.
 * 
 * @since 3.3
 * @see JavaVariableContentProvider
 * @see JavaInspectExpression 
 */
public class JavaExpressionContentProvider extends ExpressionContentProvider{

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.model.elements.VariableContentProvider#getChildren(java.lang.Object, int, int, org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext, org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate)
	 */
	protected Object[] getChildren(Object parent, int index, int length, IPresentationContext context, IViewerUpdate monitor) throws CoreException {
		Object[] variables = getAllChildren(parent, context);
        if (JavaVariableContentProvider.displayReferencesAsChild(parent)){
        	Object[] moreVariables = new Object[variables.length+1];
        	System.arraycopy(variables, 0, moreVariables, 1, variables.length);
        	IValue value = ((IExpression)parent).getValue();
        	if (JavaVariableContentProvider.supportsInstanceRetrieval(parent)){
        		moreVariables[0] = new JDIReferenceListVariable(VariableMessages.JavaExpressionContentProvider_0,(IJavaObject)value);
        	} else {
        		moreVariables[0] = new JDIPlaceholderVariable(VariableMessages.JavaExpressionContentProvider_1,new JDIPlaceholderValue((JDIDebugTarget)value.getDebugTarget(),VariableMessages.JavaExpressionContentProvider_2));
        	}
        	
        	return getElements(moreVariables, index, length);
        }
        return getElements(variables, index, length);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.model.elements.VariableContentProvider#getChildCount(java.lang.Object, org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext, org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate)
	 */
	protected int getChildCount(Object element, IPresentationContext context, IViewerUpdate monitor) throws CoreException {
		int count = super.getChildCount(element, context, monitor);
		if (JavaVariableContentProvider.displayReferencesAsChild(element)){
			count++;
		}
		return count;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.model.elements.ExpressionContentProvider#hasChildren(java.lang.Object, org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext, org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate)
	 */
	protected boolean hasChildren(Object element, IPresentationContext context,	IViewerUpdate monitor) throws CoreException {
		if (JavaVariableContentProvider.displayReferencesAsChild(element)){
			return true;
		}
		return super.hasChildren(element, context, monitor);
	}
	
}
