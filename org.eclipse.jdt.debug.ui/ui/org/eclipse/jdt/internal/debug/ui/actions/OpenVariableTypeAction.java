/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.actions;


import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.jdt.debug.core.IJavaVariable;

public abstract class OpenVariableTypeAction extends OpenTypeAction {

	/**
	 * @see org.eclipse.jdt.internal.debug.ui.actions.OpenTypeAction#getDebugElement(IAdaptable)
	 */
	protected IDebugElement getDebugElement(IAdaptable element) {
		return (IDebugElement)element.getAdapter(IJavaVariable.class);
	}

	/**
	 * @see org.eclipse.jdt.internal.debug.ui.actions.OpenTypeAction#getTypeNameToOpen(IDebugElement)
	 */
	protected String getTypeNameToOpen(IDebugElement element) throws DebugException {
		return null;
	}

	public static String removeArray(String typeName) {
		if (typeName == null) {
			return null;
		}
		int index= typeName.indexOf('[');
		if (index > 0) {
			return typeName.substring(0, index);
		}
		return typeName;
	}
}
