/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
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
package org.eclipse.jdt.internal.debug.core.refactoring;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.debug.core.IJavaClassPrepareBreakpoint;

/**
 * @since 3.2
 *
 */
public abstract class ClassPrepareBreakpointChange extends BreakpointChange {

	private final int fMemberType;

	public ClassPrepareBreakpointChange(IJavaClassPrepareBreakpoint breakpoint) throws CoreException {
		super(breakpoint);
		fMemberType = breakpoint.getMemberType();
	}

	protected int getMemberType() {
		return fMemberType;
	}

}
