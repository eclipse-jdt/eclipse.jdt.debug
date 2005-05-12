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


import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaVariable;


public class OpenVariableConcreteTypeAction extends OpenVariableTypeAction {

	protected String getTypeNameToOpen(IDebugElement element) throws DebugException {
		String refType= Signature.toString(((IJavaObject)((IJavaVariable)element).getValue()).getSignature()).replace('/', '.');
		refType= removeArray(refType);
		return refType;
	}
	
}
