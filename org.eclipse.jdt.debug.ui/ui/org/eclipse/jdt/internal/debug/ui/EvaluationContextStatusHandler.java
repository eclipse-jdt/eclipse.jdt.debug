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
package org.eclipse.jdt.internal.debug.ui;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.IStatusHandler;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * Supplies a thread that can be used for an evaluation, given an element
 * from a java debug model. Currently use for logical object structure
 * computations.
 * 
 * @since 3.0
 */
public class EvaluationContextStatusHandler implements IStatusHandler {

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IStatusHandler#handleStatus(org.eclipse.core.runtime.IStatus, java.lang.Object)
	 */
	public Object handleStatus(IStatus status, Object source) {
		if (source instanceof IDebugElement) {
			IDebugElement element = (IDebugElement) source;
			IJavaDebugTarget target = (IJavaDebugTarget) element.getDebugTarget().getAdapter(IJavaDebugTarget.class);
			if (target != null) {
				IJavaStackFrame frame = EvaluationContextManager.getEvaluationContext((IWorkbenchWindow)null);
				if (frame != null && frame.getDebugTarget().equals(target)) {
					return frame.getThread();
				}
			}
		}
		return null;
	}

}
