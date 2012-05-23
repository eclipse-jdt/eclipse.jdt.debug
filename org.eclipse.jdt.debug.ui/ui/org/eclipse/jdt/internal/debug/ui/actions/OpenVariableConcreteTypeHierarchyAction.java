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

/**
 * Opens the concrete type hierarhcy of variable - i.e. it's value's actual type.
 */
public class OpenVariableConcreteTypeHierarchyAction extends OpenVariableConcreteTypeAction {
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.actions.OpenTypeAction#isHierarchy()
	 */
	@Override
	protected boolean isHierarchy() {
		return true;
	}	
}
