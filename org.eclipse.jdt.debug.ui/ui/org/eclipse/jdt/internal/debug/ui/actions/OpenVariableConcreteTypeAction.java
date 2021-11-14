/*******************************************************************************
 * Copyright (c) 2000, 2022 IBM Corporation and others.
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
package org.eclipse.jdt.internal.debug.ui.actions;


import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.debug.ui.IJavaDebugUIConstants;
import org.eclipse.jdt.internal.debug.core.model.JDIInterfaceType;
import org.eclipse.jdt.internal.debug.core.model.JDIObjectValue;
import org.eclipse.jdt.internal.debug.core.model.JDIVariable;
import org.eclipse.jface.action.IAction;

/**
 * Opens the concrete type of variable - i.e. it's value's actual type.
 */
public class OpenVariableConcreteTypeAction extends OpenVariableTypeAction {

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.actions.OpenTypeAction#getTypeToOpen(org.eclipse.debug.core.model.IDebugElement)
	 */
	@Override
	protected IJavaType getTypeToOpen(IDebugElement element) throws CoreException {
		if (element instanceof IJavaVariable) {
			IJavaVariable variable = (IJavaVariable) element;
			return ((IJavaValue)variable.getValue()).getJavaType();
		}
		return null;
	}

	@Override
	public boolean openElement(IAction action, Object element) throws DebugException, CoreException {
		if (element instanceof JDIVariable) {
			final var jdiVariable = (JDIVariable) element;
			if (isInterfaceType(jdiVariable)) {
				final var val = (JDIObjectValue) jdiVariable.getValue();
				if (val.getJavaType().toString().contains("$$Lambda$")) { //$NON-NLS-1$
					OpenVariableDeclaredTypeAction declaredAction = new OpenVariableDeclaredTypeAction();
					declaredAction.setActivePart(action, getPart());
					declaredAction.run(action);
					return true;
				}
			}
		}
		IType sourceElement = resolveSourceElement(element);
		if (sourceElement != null) {
			openInEditor(element, sourceElement);
			return false;
		}
		IStatus status = new Status(IStatus.INFO, IJavaDebugUIConstants.PLUGIN_ID, IJavaDebugUIConstants.INTERNAL_ERROR, "Source not found", null); //$NON-NLS-1$
		throw new CoreException(status);
	}

	private boolean isInterfaceType(JDIVariable jdiVariable) {
		try {
			return jdiVariable.getJavaType() instanceof JDIInterfaceType;
		} catch (DebugException e) {
			return false;
		}
	}
}
