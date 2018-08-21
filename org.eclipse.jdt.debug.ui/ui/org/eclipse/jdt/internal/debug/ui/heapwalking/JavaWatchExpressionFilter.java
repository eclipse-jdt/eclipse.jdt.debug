/*******************************************************************************
 * Copyright (c) 2007, 2015 IBM Corporation and others.
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
package org.eclipse.jdt.internal.debug.ui.heapwalking;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.internal.ui.views.variables.IndexedVariablePartition;
import org.eclipse.debug.ui.actions.IWatchExpressionFactoryAdapterExtension;
import org.eclipse.jdt.internal.debug.core.model.JDIArrayEntryVariable;
import org.eclipse.jdt.internal.debug.core.model.JDIPlaceholderValue;
import org.eclipse.jdt.internal.debug.core.model.JDIReferenceListEntryVariable;
import org.eclipse.jdt.internal.debug.core.model.JDIReferenceListVariable;

/**
 * Uses the <code>IWatchExpressionFactoryAdapterExtension</code> to filter when the watch expression
 * action is available based on the variable selected.
 *
 * Currently removes the action from <code>JDIPlaceholderVariable</code>s and <code>JDIReferenceListVariable</code>s.
 *
 * @since 3.3
 */
public class JavaWatchExpressionFilter implements IWatchExpressionFactoryAdapterExtension {

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.IWatchExpressionFactoryAdapterExtension#canCreateWatchExpression(org.eclipse.debug.core.model.IVariable)
	 */
	@Override
	public boolean canCreateWatchExpression(IVariable variable) {
		if (variable instanceof JDIReferenceListVariable || variable instanceof JDIReferenceListEntryVariable ||
				variable instanceof JDIArrayEntryVariable || variable instanceof IndexedVariablePartition){
			return false;
		}
		try{
			if (variable.getValue() instanceof JDIPlaceholderValue){
				return false;
			}
		} catch (DebugException e) {}
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.IWatchExpressionFactoryAdapter#createWatchExpression(org.eclipse.debug.core.model.IVariable)
	 */
	@Override
	public String createWatchExpression(IVariable variable) throws CoreException {
		return variable.getName();
	}

}
