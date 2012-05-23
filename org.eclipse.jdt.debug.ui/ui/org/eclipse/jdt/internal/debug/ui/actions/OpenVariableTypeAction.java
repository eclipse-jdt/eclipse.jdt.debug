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


import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.jdt.debug.core.IJavaVariable;

/**
 * Action to open a type associated with a selected variable.
 */
public abstract class OpenVariableTypeAction extends OpenTypeAction {

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.actions.OpenTypeAction#getDebugElement(org.eclipse.core.runtime.IAdaptable)
	 */
	@Override
	protected IDebugElement getDebugElement(IAdaptable element) {
		return (IDebugElement)element.getAdapter(IJavaVariable.class);
	}
}
