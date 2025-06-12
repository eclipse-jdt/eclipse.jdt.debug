/*******************************************************************************
 * Copyright (c) 2005, 2012 IBM Corporation and others.
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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.NullChange;
import org.eclipse.osgi.util.NLS;

/**
 * A change to delete a breakpoint. Currently used for undo.
 * When undoing a refactoring, the "target/original" resource does
 * not exist in time to create a marker on it, and thus the operation
 * cannot be undone. Instead, we delete breakpoints on undo.
 *
 * @since 3.2
 */
public class DeleteBreakpointChange extends BreakpointChange {

	public DeleteBreakpointChange(IJavaBreakpoint breakpoint) throws CoreException {
		super(breakpoint);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ltk.core.refactoring.Change#getName()
	 */
	@Override
	public String getName() {
		return NLS.bind(RefactoringMessages.DeleteBreakpointChange_0,
				getBreakpointLabel(getOriginalBreakpoint()));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ltk.core.refactoring.Change#perform(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public Change perform(IProgressMonitor pm) throws CoreException {
		getOriginalBreakpoint().delete();
		return new NullChange();
	}

}
