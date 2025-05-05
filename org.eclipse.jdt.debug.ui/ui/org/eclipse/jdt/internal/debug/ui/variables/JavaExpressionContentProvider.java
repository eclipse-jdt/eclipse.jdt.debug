/*******************************************************************************
 * Copyright (c) 2007, 2012 IBM Corporation and others.
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
package org.eclipse.jdt.internal.debug.ui.variables;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.internal.ui.model.elements.ExpressionContentProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.internal.debug.core.logicalstructures.JDIAllInstancesValue;
import org.eclipse.jdt.internal.debug.core.model.JDIDebugModelMessages;
import org.eclipse.jdt.internal.debug.core.model.JDIReferenceListValue;
import org.eclipse.jdt.internal.debug.core.model.JDIReferenceListVariable;
import org.eclipse.jdt.internal.debug.ui.display.JavaInspectExpression;
import org.eclipse.osgi.util.NLS;

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
	@Override
	protected Object[] getChildren(Object parent, int index, int length, IPresentationContext context, IViewerUpdate monitor) throws CoreException {
		Object[] variables = getAllChildren(parent, context);
        if (JavaVariableContentProvider.displayReferencesAsChild(parent)){
        	IValue value = ((IExpression)parent).getValue();
        	if (!(value instanceof JDIAllInstancesValue) && !(value instanceof JDIReferenceListValue)) {
	        	Object[] moreVariables = new Object[variables.length+1];
	        	System.arraycopy(variables, 0, moreVariables, 1, variables.length);
	        	moreVariables[0] = new JDIReferenceListVariable(NLS.bind(JDIDebugModelMessages.JDIReferenceListValue_6, value.getReferenceTypeName() + " " + value.getValueString()),(IJavaObject)value); //$NON-NLS-1$
	        	return getElements(moreVariables, index, length);
        	}
        }
        return getElements(variables, index, length);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.model.elements.VariableContentProvider#getChildCount(java.lang.Object, org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext, org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate)
	 */
	@Override
	protected int getChildCount(Object element, IPresentationContext context, IViewerUpdate monitor) throws CoreException {
		int count = super.getChildCount(element, context, monitor);
		if (JavaVariableContentProvider.displayReferencesAsChild(element)){
			IValue value = ((IExpression)element).getValue();
			if (!(value instanceof JDIAllInstancesValue) && !(value instanceof JDIReferenceListValue)) {
				count++;
			}
		}
		return count;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.model.elements.ExpressionContentProvider#hasChildren(java.lang.Object, org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext, org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate)
	 */
	@Override
	protected boolean hasChildren(Object element, IPresentationContext context,	IViewerUpdate monitor) throws CoreException {
		if (JavaVariableContentProvider.displayReferencesAsChild(element)){
			return true;
		}
		return super.hasChildren(element, context, monitor);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.model.elements.ExpressionContentProvider#getAllChildren(java.lang.Object, org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext)
	 */
	@Override
	protected Object[] getAllChildren(Object parent, IPresentationContext context) throws CoreException {
		Object[] children = super.getAllChildren(parent, context);
		return JavaContentProviderFilter.filterVariables(children, context);
	}
}
