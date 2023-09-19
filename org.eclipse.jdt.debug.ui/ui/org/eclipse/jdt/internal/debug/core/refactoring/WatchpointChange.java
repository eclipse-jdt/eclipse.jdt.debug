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
import org.eclipse.jdt.debug.core.IJavaWatchpoint;

/**
 * @since 3.2
 *
 */
public abstract class WatchpointChange extends LineBreakpointChange {

	private final String fFieldName;
	private final boolean fAccess, fModification;

	public WatchpointChange(IJavaWatchpoint watchpoint) throws CoreException {
		super(watchpoint);
		fFieldName = watchpoint.getFieldName();
		fAccess = watchpoint.isAccess();
		fModification = watchpoint.isModification();
	}

	protected String getFieldName() {
		return fFieldName;
	}

	protected void apply(IJavaWatchpoint breakpoint) throws CoreException {
		super.apply(breakpoint);
		breakpoint.setAccess(fAccess);
		breakpoint.setModification(fModification);
	}
}
