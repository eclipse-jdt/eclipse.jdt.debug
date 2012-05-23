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
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaType;

/**
 * Opens the receiving type of a stack frame.
 */
public class OpenReceivingTypeAction extends OpenStackFrameAction {

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.actions.OpenTypeAction#getTypeToOpen(org.eclipse.debug.core.model.IDebugElement)
	 */
	@Override
	protected IJavaType getTypeToOpen(IDebugElement element) throws CoreException {
		if (element instanceof IJavaStackFrame) {
			IJavaStackFrame frame = (IJavaStackFrame) element;
			if (frame.isStatic()) {
				return frame.getReferenceType();
			}
			return frame.getThis().getJavaType();
		}
		return null;
	}
}
