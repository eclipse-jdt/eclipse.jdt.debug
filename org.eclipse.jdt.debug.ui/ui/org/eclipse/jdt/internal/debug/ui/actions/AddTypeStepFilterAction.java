package org.eclipse.jdt.internal.debug.ui.actions;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.debug.core.IJavaStackFrame;

/**
 * Adds a Java type to the set of active step filters.
 */
public class AddTypeStepFilterAction extends AbstractAddStepFilterAction {

	/**
	 * @see org.eclipse.jdt.internal.debug.ui.actions.AbstractAddStepFilterAction#generateStepFilterPattern(org.eclipse.jdt.debug.core.IJavaStackFrame)
	 */
	protected String generateStepFilterPattern(IJavaStackFrame frame) {
		try {
			return frame.getReceivingTypeName();
		} catch (DebugException de) {
			return null;
		}
	}

}
