/*******************************************************************************
 * Copyright (c) 2026 IBM Corporation.
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

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.internal.ui.views.variables.IndexedVariablePartition;
import org.eclipse.debug.ui.actions.IWatchExpressionFactoryAdapter2;
import org.eclipse.debug.ui.actions.IWatchExpressionFactoryAdapterExtension;
import org.eclipse.jdt.internal.debug.core.logicalstructures.JDIPlaceholderVariable;
import org.eclipse.jdt.internal.debug.core.model.JDIArrayEntryVariable;
import org.eclipse.jdt.internal.debug.core.model.JDIPlaceholderValue;
import org.eclipse.jdt.internal.debug.core.model.JDIReferenceListEntryVariable;
import org.eclipse.jdt.internal.debug.core.model.JDIReferenceListVariable;

/**
 * Uses the {@link IWatchExpressionFactoryAdapterExtension} to filter when the watch expression action is available based on the variable selected.
 *
 * Currently removes the action from {@link JDIPlaceholderVariable}s and {@link JDIReferenceListVariable}s.
 */
public class JavaNestedWatchExpressionFilter implements IWatchExpressionFactoryAdapter2 {

	@Override
	public String createWatchExpression(Object element) throws CoreException {
		if (element instanceof List<?> expVariablesList) {
			StringBuilder expresion = new StringBuilder();
			for (Object ob : expVariablesList) {
				if (ob instanceof IVariable variable) {
					String current = variable.getName();
					expresion.append(current);
					expresion.append('.');
				}
			}
			expresion.deleteCharAt(expresion.length() - 1);
			return expresion.toString();
		}
		return null;
	}

	@Override
	public boolean canCreateWatchExpression(Object variable) {
		if (variable instanceof List<?> expVariablesList) {
			for (Object ob : expVariablesList) {
				if (ob instanceof IVariable variable2) {
					return checkForValidVariable(variable2);
				}
			}
		}
		return false;
	}

	/**
	 * Checks whether the given {@link IVariable} is suitable for creating a watch expression.
	 * <p>
	 * Filters out variables that represent internal or synthetic structures (such as reference lists, array entries, indexed partitions) or
	 * placeholder values that do not have a concrete value.
	 * </p>
	 *
	 * @param variable
	 *            the variable to check
	 * @return {@code true} if the variable can be used to create a watch expression, {@code false} otherwise
	 */
	private boolean checkForValidVariable(IVariable variable) {
		if (variable instanceof JDIReferenceListVariable || variable instanceof JDIReferenceListEntryVariable
				|| variable instanceof JDIArrayEntryVariable || variable instanceof IndexedVariablePartition) {
			return false;
		}
		try {
			if (variable.getValue() instanceof JDIPlaceholderValue) {
				return false;
			}
		} catch (DebugException e) {
		}
		return true;
	}

}
