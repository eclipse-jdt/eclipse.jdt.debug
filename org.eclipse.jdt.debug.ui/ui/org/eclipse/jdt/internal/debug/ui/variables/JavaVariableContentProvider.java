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
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.internal.ui.model.elements.VariableContentProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.internal.debug.core.HeapWalkingManager;
import org.eclipse.jdt.internal.debug.core.model.JDIDebugModelMessages;
import org.eclipse.jdt.internal.debug.core.model.JDIReferenceListVariable;
import org.eclipse.osgi.util.NLS;

/**
 * Determines the child content of an IJavaVariable.
 *
 * @since 3.3
 * @see VariableContentProvider
 * @see IJavaVariable
 * @see JavaExpressionContentProvider
 */
public class JavaVariableContentProvider extends VariableContentProvider {

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.model.elements.VariableContentProvider#getChildren(java.lang.Object, int, int, org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext, org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate)
	 */
	@Override
	protected Object[] getChildren(Object parent, int index, int length, IPresentationContext context, IViewerUpdate monitor) throws CoreException {
		try {
			Object[] variables = getAllChildren(parent, context);
	        if (displayReferencesAsChild(parent)){
	        	Object[] moreVariables = new Object[variables.length+1];
	        	System.arraycopy(variables, 0, moreVariables, 1, variables.length);
	        	IValue value = ((IVariable)parent).getValue();
				moreVariables[0] = new JDIReferenceListVariable(NLS.bind(JDIDebugModelMessages.JDIReferenceListValue_6, ((IVariable) parent).getName()), (IJavaObject) value);
	        	return getElements(moreVariables, index, length);
	        }
	        return getElements(variables, index, length);
		} catch (CoreException e) {
			if (e.getStatus().getCode() == IJavaThread.ERR_THREAD_NOT_SUSPENDED) {
				monitor.cancel();
				return EMPTY;
			}
			throw e;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.model.elements.VariableContentProvider#getChildCount(java.lang.Object, org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext, org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate)
	 */
	@Override
	protected int getChildCount(Object element, IPresentationContext context, IViewerUpdate monitor) throws CoreException {
		try {
			int count = super.getChildCount(element, context, monitor);
			if (displayReferencesAsChild(element)){
				count++;
			}
			return count;
		} catch (CoreException e) {
			if (e.getStatus().getCode() == IJavaThread.ERR_THREAD_NOT_SUSPENDED) {
				monitor.cancel();
				return 0;
			}
			throw e;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.model.elements.VariableContentProvider#hasChildren(java.lang.Object, org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext, org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate)
	 */
	@Override
	protected boolean hasChildren(Object element, IPresentationContext context,	IViewerUpdate monitor) throws CoreException {
		try {
			if (displayReferencesAsChild(element)){
				return true;
			}
			return super.hasChildren(element, context, monitor);
		} catch (CoreException e) {
			if (e.getStatus().getCode() == IJavaThread.ERR_THREAD_NOT_SUSPENDED) {
				monitor.cancel();
				return false;
			}
			throw e;
		}
	}

	/**
	 * Determines if an all references variable should be added as a child to the passed object.
	 *
	 * @param parent element to display references as a child for
	 * @return whether to display references as a child of the given parent
	 */
	public static boolean displayReferencesAsChild(Object parent) throws DebugException{
		// Note, this method is used by the JavaExpressionContentProvider as well as the JavaVariableContentProvider
		// Only display references if the target can support it
		if (HeapWalkingManager.supportsHeapWalking(parent)){
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
					if (!((IJavaObject)value).isNull()){
						return HeapWalkingManager.getDefault().isShowReferenceInVarView();
					}
				}
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.model.elements.VariableContentProvider#getAllChildren(java.lang.Object, org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext)
	 */
	@Override
	protected Object[] getAllChildren(Object parent, IPresentationContext context) throws CoreException {
		Object[] children = super.getAllChildren(parent, context);
		return JavaContentProviderFilter.filterVariables(children, context);
	}
}
