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
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.internal.ui.model.elements.VariableContentProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.internal.debug.core.HeapWalkingManager;
import org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget;
import org.eclipse.jdt.internal.debug.core.model.JDIReferenceListVariable;

import com.ibm.icu.text.MessageFormat;

/**
 * Determines the child content of an IJavaVariable.
 * 
 * @since 3.3
 * @see VariableContentProvider
 * @see IJavaVariable
 * @see JavaVariableAdapterFactory
 * @see JavaExpressionContentProvider
 */
public class JavaVariableContentProvider extends VariableContentProvider {

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.model.elements.VariableContentProvider#getChildren(java.lang.Object, int, int, org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext, org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate)
	 */
	protected Object[] getChildren(Object parent, int index, int length, IPresentationContext context, IViewerUpdate monitor) throws CoreException {
		Object[] variables = getAllChildren(parent, context);
        if (displayReferencesAsChild(parent)){
        	Object[] moreVariables = new Object[variables.length+1];
        	System.arraycopy(variables, 0, moreVariables, 1, variables.length);
        	IValue value = ((IVariable)parent).getValue();
        	if (supportsInstanceRetrieval(parent)){
        		moreVariables[0] = new JDIReferenceListVariable(MessageFormat.format(VariableMessages.JavaVariableContentProvider_0, new String[]{((IVariable)parent).getName()}),(IJavaObject)value);
        	} else {
        		moreVariables[0] = new JDIReferenceListVariable(MessageFormat.format(VariableMessages.JavaVariableContentProvider_0, new String[]{((IVariable)parent).getName()}),VariableMessages.JavaVariableContentProvider_2,(JDIDebugTarget)value.getDebugTarget());
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
		if (displayReferencesAsChild(element)){
			count++;
		}
		return count;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.model.elements.VariableContentProvider#hasChildren(java.lang.Object, org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext, org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate)
	 */
	protected boolean hasChildren(Object element, IPresentationContext context,	IViewerUpdate monitor) throws CoreException {
		if (displayReferencesAsChild(element)){
			return true;
		}
		return super.hasChildren(element, context, monitor);
	}
	
	/**
	 * Determines if an all references variable should be added as a child to the passed object.
	 * 
	 * @param parent element to display references as a child for
	 * @return whether to display references as a child of the given parent
	 * @throws DebugException
	 */
	public static boolean displayReferencesAsChild(Object parent) throws DebugException{
		// Lists of references don't have references
		if (!(parent instanceof JDIReferenceListVariable)){
			IValue value = null;
			if (parent instanceof IVariable){
				value = ((IVariable)parent).getValue();
			} else if (parent instanceof IExpression){
				value = ((IExpression)parent).getValue();
			} else{
				return false;
			}
			// Only java objects have references
			if (value instanceof IJavaObject){
				// Null objects don't have references
				if (!((IJavaDebugTarget)value.getDebugTarget()).nullValue().equals(value)){
					return HeapWalkingManager.getDefault().isShowReferenceInVarView();
				}
			}
		}
		return false;
	}
	
	/**
	 * Returns whether the given parent object is an <ode>IVariable</code> that has a debug
	 * target capable of getting all instance or all reference information.
	 * 
	 * @param parent the object to test
	 * @return whether the given object's debug target supports instance retrieval
	 */
	public static boolean supportsInstanceRetrieval(Object parent){
		return ((IJavaDebugTarget)((IDebugElement)parent).getDebugTarget()).supportsInstanceRetrieval();
	}
}
