/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui;


import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.ui.IActionFilter;

public class JavaStackFrameActionFilter implements IActionFilter {

	/**
	 * @see org.eclipse.ui.IActionFilter#testAttribute(Object, String, String)
	 */
	public boolean testAttribute(Object target, String name, String value) {
		IJavaStackFrame frame = null;
		if (target instanceof IStackFrame) {
			frame = (IJavaStackFrame) ((IStackFrame)target).getAdapter(IJavaStackFrame.class);
		}
		if (frame != null) {
			if (name.equals("DropToFrameActionFilter") //$NON-NLS-1$
				&& value.equals("supportsDropToFrame")) { //$NON-NLS-1$
					return frame.canDropToFrame();
			} else if (name.equals("ReceivingStackFrameActionFilter")  //$NON-NLS-1$
				&& value.equals("isReceivingType")) { //$NON-NLS-1$
					try {
						return !frame.getReceivingTypeName().equals(frame.getDeclaringTypeName());
					} catch (DebugException de) {
					}
			}
		}
		return false;
	}
}
