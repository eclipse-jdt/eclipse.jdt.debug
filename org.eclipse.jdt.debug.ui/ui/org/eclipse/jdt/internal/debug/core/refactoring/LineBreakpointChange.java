/*******************************************************************************
 * Copyright (c) 2005, 2011 IBM Corporation and others.
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
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;

/**
 * Specialization of a {@link BreakpointChange}
 * @since 3.2
 */
public abstract class LineBreakpointChange extends BreakpointChange {
	
	private int fCharEnd, fCharStart, fLineNumber;
	private boolean fConditionEnabled, fConditionSuspendOnTrue;
	private String fCondition;

	/**
	 * Constructor
	 * @param breakpoint
	 * @throws CoreException
	 */
	public LineBreakpointChange(IJavaLineBreakpoint breakpoint) throws CoreException {
		super(breakpoint);
		fCharEnd = breakpoint.getCharEnd();
		fCharStart = breakpoint.getCharStart();
		fLineNumber = breakpoint.getLineNumber();
		if (breakpoint.supportsCondition()) {
			fCondition = breakpoint.getCondition();
			fConditionEnabled = breakpoint.isConditionEnabled();
			fConditionSuspendOnTrue = breakpoint.isConditionSuspendOnTrue();
		}
	}

	/**
	 * Applies the original attributes to the new breakpoint
	 * @param breakpoint
	 * @throws CoreException
	 */
	protected void apply(IJavaLineBreakpoint breakpoint) throws CoreException {
		super.apply(breakpoint);
		if (breakpoint.supportsCondition()) {
			breakpoint.setCondition(fCondition);
			breakpoint.setConditionEnabled(fConditionEnabled);
			breakpoint.setConditionSuspendOnTrue(fConditionSuspendOnTrue);
		}
	}

	/**
	 * @see org.eclipse.jdt.internal.debug.core.refactoring.BreakpointChange#getLineNumber()
	 */
	@Override
	protected int getLineNumber() {
		return fLineNumber;
	}
	
	/**
	 * Returns the char end attribute of the underlying line breakpoint
	 * @return the char end attribute of the underlying line breakpoint
	 */
	protected int getCharEnd() {
		return fCharEnd;
	}
	
	/**
	 * Returns the char start attribute of the underlying line breakpoint
	 * @return the char start attribute of the underlying line breakpoint
	 */
	protected int getCharStart() {
		return fCharStart;
	}

}
