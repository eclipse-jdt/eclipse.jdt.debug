/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.actions;


import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.jdt.debug.core.IJavaVariable;


public class OpenVariableConcreteTypeAction extends OpenVariableTypeAction {

	protected String getTypeNameToOpen(IDebugElement element) throws DebugException {
		String refType= ((IJavaVariable)element).getValue().getReferenceTypeName();
		refType= removeArray(refType);
		return refType;
	}
	
}
