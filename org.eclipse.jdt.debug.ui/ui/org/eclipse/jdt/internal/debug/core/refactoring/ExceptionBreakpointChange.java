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
import org.eclipse.jdt.debug.core.IJavaExceptionBreakpoint;

/**
 * @since 3.2
 *
 */
public abstract class ExceptionBreakpointChange extends BreakpointChange {
	
	private String[] fExclusionFilters, fInclusionFilters;
	boolean fCaught, fUncaught, fChecked;
	
	public ExceptionBreakpointChange(IJavaExceptionBreakpoint breakpoint) throws CoreException {
		super(breakpoint);
		fExclusionFilters = breakpoint.getExclusionFilters();
		fInclusionFilters = breakpoint.getInclusionFilters();
		fCaught = breakpoint.isCaught();
		fUncaught = breakpoint.isUncaught();
		fChecked  = breakpoint.isChecked();
	}
	
	protected boolean isChecked() {
		return fChecked;
	}
	
	protected boolean isCaught() {
		return fCaught;
	}
	
	protected boolean isUncaught() {
		return fUncaught;
	}
	
	protected void apply(IJavaExceptionBreakpoint breakpoint) throws CoreException {
		super.apply(breakpoint);
		breakpoint.setExclusionFilters(fExclusionFilters);
		breakpoint.setInclusionFilters(fInclusionFilters);
		breakpoint.setCaught(fCaught);
		breakpoint.setUncaught(fUncaught);
	}

}
