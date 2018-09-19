/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
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


import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.debug.core.IJavaStackFrame;

/**
 * Adds a Java type to the set of active step filters.
 */
public class AddTypeStepFilterAction extends AbstractAddStepFilterAction {

	/**
	 * @see org.eclipse.jdt.internal.debug.ui.actions.AbstractAddStepFilterAction#generateStepFilterPattern(org.eclipse.jdt.debug.core.IJavaStackFrame)
	 */
	@Override
	protected String generateStepFilterPattern(IJavaStackFrame frame) {
		try {
			return frame.getDeclaringTypeName();
		} catch (DebugException de) {
			return null;
		}
	}

}
