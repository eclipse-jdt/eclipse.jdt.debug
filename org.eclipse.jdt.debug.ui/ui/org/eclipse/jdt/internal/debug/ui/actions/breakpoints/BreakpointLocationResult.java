package org.eclipse.jdt.internal.debug.ui.actions.breakpoints;

/**********************************************************************
Copyright (c) 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;

/**
 * NOTE: This class is yet experimental. Investigating breakpoint creation
 * and location verification via the use of an AST. This could be used to
 * support breakpoints in external source (i.e. without the knowlegde of
 * Java model elements).
 */
public class BreakpointLocationResult {
	
	/**
	 * Valid offset for a breakpoint, or -1	 */
	private int fOffset = -1;
	
	/**
	 * Status object providing information why location is invalid,
	 * or OK.	 */
	private IStatus fStatus = fgOK;
	
	/**
	 * Singleton "ok" status	 */
	private static final IStatus fgOK = new Status(IStatus.OK, JDIDebugUIPlugin.getUniqueIdentifier(),0, "", null);

	/**
	 * Constructs a new breakpoint location result at the given offset.
	 */
	public BreakpointLocationResult(int offset) {
		fOffset = offset;
	}
	
	/**
	 * Constructs a new breakpoint location result descibing why the location
	 * is invalid.
	 * 	 * @param status	 */
	public BreakpointLocationResult(IStatus status) {
		fStatus = status;
	}
	
	/**
	 * Returns the offset at which a breakpoint can be set, or -1
	 * if no valid offset was found.
	 *  	 * @return breakpoint offset of -1	 */
	public int getOffset() {
		return fOffset;
	}
	
	/**
	 * Returns a status indicating why a valid breakpoint offset could not
	 * be located, or an OK status.
	 * 	 * @return status	 */
	public IStatus getStatus() {
		return fStatus;
	}

}
