package org.eclipse.jdt.internal.debug.ui.actions;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

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