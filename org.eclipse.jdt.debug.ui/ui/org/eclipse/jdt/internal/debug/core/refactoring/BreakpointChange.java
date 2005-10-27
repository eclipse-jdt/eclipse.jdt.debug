/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.core.refactoring;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

/**
 * @since 3.2
 *
 */
public abstract class BreakpointChange extends Change {
	
	private IJavaBreakpoint fBreakpoint;
	private String fTypeName;
	private int fHitCount;
	private IJavaObject[] fInstanceFilters;
	private int fSuspendPolicy;
	private IJavaThread[] fThreadFilters;
	private boolean fEnabled;

	public BreakpointChange(IJavaBreakpoint breakpoint) throws CoreException {
		fBreakpoint = breakpoint;
		fTypeName = breakpoint.getTypeName();
		fHitCount = breakpoint.getHitCount();
		fInstanceFilters = breakpoint.getInstanceFilters();
		fSuspendPolicy = breakpoint.getSuspendPolicy();
		fThreadFilters = breakpoint.getThreadFilters();
		fEnabled = breakpoint.isEnabled();
	}
	
	/**
	 * Applies the original attributes to the new breakpoint
	 * 
	 * @param breakpoint the new breakpoint
	 * @throws CoreException
	 */
	protected void apply(IJavaBreakpoint breakpoint) throws CoreException {
		breakpoint.setHitCount(fHitCount);
		for (int i = 0; i < fInstanceFilters.length; i++) {
			breakpoint.addInstanceFilter(fInstanceFilters[i]);
		}
		breakpoint.setSuspendPolicy(fSuspendPolicy);
		for (int i = 0; i < fThreadFilters.length; i++) {
			breakpoint.setThreadFilter(fThreadFilters[i]);
		}
		breakpoint.setEnabled(fEnabled);
	}
	
	protected IJavaBreakpoint getOriginalBreakpoint() {
		return fBreakpoint;
	}
	
	/**
	 * Returns the original name of the type the associated breakpoint was set on.
	 * This can be different than the type being changed.
	 * 
	 * @return
	 */
	protected String getOriginalBreakpointTypeName() {
		return fTypeName;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ltk.core.refactoring.Change#initializeValidationData(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void initializeValidationData(IProgressMonitor pm) {
		// do nothing
	}	
	
	/* (non-Javadoc)
	 * @see org.eclipse.ltk.core.refactoring.Change#isValid(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public RefactoringStatus isValid(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		return new RefactoringStatus();
	}	

	/* (non-Javadoc)
	 * @see org.eclipse.ltk.core.refactoring.Change#getModifiedElement()
	 */
	public Object getModifiedElement() {
		return fBreakpoint;
	}	
}
