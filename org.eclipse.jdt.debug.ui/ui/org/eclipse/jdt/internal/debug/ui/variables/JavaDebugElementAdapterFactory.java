/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.variables;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.debug.internal.ui.model.elements.ExpressionLabelProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementContentProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementLabelProvider;
import org.eclipse.debug.internal.ui.views.variables.IndexedVariablePartition;
import org.eclipse.debug.ui.actions.IWatchExpressionFactoryAdapter;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.internal.debug.ui.display.JavaInspectExpression;
import org.eclipse.jdt.internal.debug.ui.heapwalking.JavaWatchExpressionFilter;

/**
 * Provides provider adapters for IJavaVariables.
 * 
 * @see IJavaVariable
 * @see JavaVariableLabelProvider
 * @see JavaVariableContentProvider
 * @see ExpressionLabelProvider
 * @see JavaExpressionContentProvider
 * @see JavaWatchExpressionFilter
 * @since 3.3
 */
public class JavaDebugElementAdapterFactory implements IAdapterFactory {
	
	private static final IElementLabelProvider fgLPVariable = new JavaVariableLabelProvider();
	private static final IElementContentProvider fgCPVariable = new JavaVariableContentProvider();
	private static final IElementLabelProvider fgLPExpression = new ExpressionLabelProvider();
	private static final IElementContentProvider fgCPExpression = new JavaExpressionContentProvider();
	private static final IWatchExpressionFactoryAdapter fgWEVariable = new JavaWatchExpressionFilter();

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object, java.lang.Class)
	 */
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (IElementLabelProvider.class.equals(adapterType)) {
			if (adaptableObject instanceof IJavaVariable) {
				return fgLPVariable; 
			}
			if (adaptableObject instanceof JavaInspectExpression) {
				return fgLPExpression;
			}
			if(adaptableObject instanceof IndexedVariablePartition) {
				return fgLPVariable;
			}
		}
		if (IElementContentProvider.class.equals(adapterType)) {
			if (adaptableObject instanceof IJavaVariable) {
				return fgCPVariable;
			}
			if (adaptableObject instanceof JavaInspectExpression) {
				return fgCPExpression;
			}
			if(adaptableObject instanceof IndexedVariablePartition) {
				return fgCPVariable;
			}
		}
		if (IWatchExpressionFactoryAdapter.class.equals(adapterType)) {
			if (adaptableObject instanceof IJavaVariable) {
				return fgWEVariable;
			}
			if (adaptableObject instanceof JavaInspectExpression) {
				return fgCPExpression;
			}
			if(adaptableObject instanceof IndexedVariablePartition) {
				return fgWEVariable;
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
	 */
	public Class[] getAdapterList() {
		return new Class[]{IElementLabelProvider.class,IElementContentProvider.class,IWatchExpressionFactoryAdapter.class};
	}

}
