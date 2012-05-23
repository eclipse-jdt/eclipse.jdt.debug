/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.actions;


import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.IJavaVariable;

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
	
}
