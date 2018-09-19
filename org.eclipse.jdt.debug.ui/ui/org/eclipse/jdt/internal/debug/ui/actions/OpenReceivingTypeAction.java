/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.jdt.debug.core.IJavaObject;
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
			IJavaObject ths = frame.getThis();
			if (ths != null) {
				return ths.getJavaType();
			}
		}
		return null;
	}
}
