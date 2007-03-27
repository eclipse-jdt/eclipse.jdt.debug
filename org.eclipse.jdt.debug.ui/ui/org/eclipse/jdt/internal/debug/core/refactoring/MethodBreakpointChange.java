/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
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
import org.eclipse.jdt.debug.core.IJavaMethodBreakpoint;

/**
 * Specialization of breakpoint change for method breakpoints
 * @since 3.2
 */
public abstract class MethodBreakpointChange extends LineBreakpointChange {
	
	private String fMethodName, fSignature;
	private boolean fEntry, fExit, fNativeOnly;

	/**
	 * Constructor
	 * @param breakpoint
	 * @throws CoreException
	 */
	public MethodBreakpointChange(IJavaMethodBreakpoint breakpoint) throws CoreException {
		super(breakpoint);
		fMethodName = breakpoint.getMethodName();
		fSignature = breakpoint.getMethodSignature();
		fEntry = breakpoint.isEntry();
		fExit = breakpoint.isExit();
		fNativeOnly = breakpoint.isNativeOnly();
	}
	
	/**
	 * Returns the name of the method
	 * @return the name of the method
	 */
	protected String getMethodName() {
		return fMethodName;
	}
	
	/**
	 * Returns the signature of the method
	 * @return the signature of the method
	 */
	protected String getSignature() {
		return fSignature;
	}
	
	/**
	 * Returns if it is an entry breakpoint
	 * @return if it is an entry breakpoint
	 */
	protected boolean isEntry() {
		return fEntry;
	}
	
	/**
	 * Returns if it is an exit breakpoint
	 * @return if it is an exit breakpoint
	 */
	protected boolean isExit() {
		return fExit;
	}
	
	/**
	 * Returns if it is native only
	 * @return if it is native only
	 */
	protected boolean isNativeOnly() {
		return fNativeOnly;
	}

	/**
	 * Applies the old settings to the new breakpoint
	 * @param breakpoint
	 * @throws CoreException
	 */
	protected void apply(IJavaMethodBreakpoint breakpoint) throws CoreException {
		super.apply(breakpoint);
		breakpoint.setEntry(fEntry);
		breakpoint.setExit(fExit);
		breakpoint.setNativeOnly(fNativeOnly);
	}	
	
}
