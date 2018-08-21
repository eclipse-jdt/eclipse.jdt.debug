/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
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
package org.eclipse.jdt.internal.debug.ui;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.IStatusHandler;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * Supplies the current stack frame context. Currently use for logical object structure
 * computations.
 *
 * @since 3.1
 */
public class EvaluationStackFrameContextStatusHandler implements IStatusHandler {

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IStatusHandler#handleStatus(org.eclipse.core.runtime.IStatus, java.lang.Object)
	 */
	@Override
	public Object handleStatus(IStatus status, Object source) {
		if (source instanceof IDebugElement) {
			IDebugElement element = (IDebugElement) source;
			IJavaDebugTarget target = element.getDebugTarget().getAdapter(IJavaDebugTarget.class);
			if (target != null) {
				IJavaStackFrame frame = EvaluationContextManager.getEvaluationContext((IWorkbenchWindow)null);
				if (frame != null && frame.getDebugTarget().equals(target)) {
					return frame;
				}
			}
		}
		return null;
	}

}
