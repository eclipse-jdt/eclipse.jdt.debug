package org.eclipse.jdt.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jdt.core.IType;

/**
 * IJavaBreakpoints extend IBreakpoint by adding
 * the following notions:
 * <ul>
 * <li>hit count - a number of times that this breakpoint
 * will be "hit" before it suspends execution
 * <li>type - the type the given breakpoint is installed in
 * <li>installed - whether the given breakpoint is installed
 * in a debug target. when a breakpoint is installed in a debug
 * target, it may cause the suspension of that target
 * 
 * Clients are not intended to implement this interface
 */
public interface IJavaBreakpoint extends IBreakpoint {
	/**
	 * Returns whether this breakpoint is installed in at least
	 * one debug target.
	 * 
	 * @return whether this breakpoint is installed
	 * @exception CoreException if a <code>CoreException</code> is
	 * 	thrown accessing this breakpoint's underlying marker
	 */
	public boolean isInstalled() throws CoreException;
	/**
	 * Returns the fully qualified name of type this breakpoint
	 * is located in, or <code>null</code> is this breakpoint
	 * is not located in a type - for example, a pattern breakpoint
	 * 
	 * @return the fully qualified name of the type this breakpoint
	 *  is located in, or <code>null</code>
	 * @exception CoreException if a <code>CoreException</code> is
	 * 	thrown accessing this breakpoint's underlying marker
	 */
	public String getTypeName() throws CoreException;
	/**
	 * Returns this breakpoint's hit count or, -1 if this
	 * breakpoint does not have a hit count.
	 * 
	 * @return this breakpoint's hit count, or -1
	 * @exception CoreException if a <code>CoreException</code> is
	 * 	thrown accessing this breakpoint's underlying marker
	 */
	public int getHitCount() throws CoreException;
	/**
	 * Sets the hit count attribute of this breakpoint.
	 * If this breakpoint is currently disabled and the hit count
	 * is set greater than -1, the breakpoint is enabled.
	 * 
	 * @param count the new hit count
	 * @exception CoreException if a <code>CoreException</code> is
	 * 	thrown accessing this breakpoint's underlying marker
	 */
	public void setHitCount(int count) throws CoreException;	

}

