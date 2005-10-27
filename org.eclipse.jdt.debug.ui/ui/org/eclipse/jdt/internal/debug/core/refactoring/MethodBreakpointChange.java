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
import org.eclipse.jdt.debug.core.IJavaMethodBreakpoint;

/**
 * @since 3.2
 *
 */
public abstract class MethodBreakpointChange extends LineBreakpointChange {
	
	private String fMethodName, fSignature;
	private boolean fEntry, fExit, fNativeOnly;

	public MethodBreakpointChange(IJavaMethodBreakpoint breakpoint) throws CoreException {
		super(breakpoint);
		fMethodName = breakpoint.getMethodName();
		fSignature = breakpoint.getMethodSignature();
		fEntry = breakpoint.isEntry();
		fExit = breakpoint.isExit();
		fNativeOnly = breakpoint.isNativeOnly();
	}
	
	protected String getMethodName() {
		return fMethodName;
	}
	
	protected String getSignature() {
		return fSignature;
	}
	
	protected boolean isEntry() {
		return fEntry;
	}
	
	protected boolean isExit() {
		return fExit;
	}
	
	protected boolean isNativeOnly() {
		return fNativeOnly;
	}

	protected void apply(IJavaMethodBreakpoint breakpoint) throws CoreException {
		super.apply(breakpoint);
		breakpoint.setEntry(fEntry);
		breakpoint.setExit(fExit);
		breakpoint.setNativeOnly(fNativeOnly);
	}	
	
}
