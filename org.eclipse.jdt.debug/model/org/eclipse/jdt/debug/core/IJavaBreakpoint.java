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
	 * Suspend policy constant indicating a breakpoint will
	 * suspend the target VM when hit.
	 */
	public static final int SUSPEND_VM = 1;
	
	/**
	 * Default suspend policy constant indicating a breakpoint will
	 * suspend only the thread in which it occurred.
	 */
	public static final int SUSPEND_THREAD = 2;
	
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
	
	/**
	 * Sets whether all threads in the target VM will be suspended
	 * when this breakpoint is hit. When <code>SUSPEND_VM</code> the target
	 * VM is suspended, and when <code>SUSPEND_THREAD</code> only the thread
	 * in which the breakpoint occurred is suspended.
	 * 
	 * @param suspendPolicy one of <code>SUSPEND_VM</code> or
	 *  <code>SUSPEND_THREAD</code>
	 * @exception CoreException if a <code>CoreException</code> is
	 * 	thrown accessing this breakpoint's underlying marker
	 */
	public void setSuspendPolicy(int suspendPolicy) throws CoreException;
	
	/**
	 * Returns the suspend policy used by this breakpoint, one of
	 * <code>SUSPEND_VM</code> or <code>SUSPEND_THREAD</code>.
	 * 
	 * @return one of <code>SUSPEND_VM</code> or <code>SUSPEND_THREAD</code>
	 * @exception CoreException if a <code>CoreException</code> is
	 * 	thrown accessing this breakpoint's underlying marker
	 */
	public int getSuspendPolicy() throws CoreException;
	
	/**
	 * Set the given threads as the thread in which this
	 * breakpoint is enabled. The previous thread filter, if
	 * any, is lost.
	 * 
	 * A thread filter applies to a single debug target and is not persisted
	 * across invokations.
	 * 
	 * While this breakpoint has a thread filter, 
	 * it will only suspend in the filtered thread.
	 * 
	 * @exception CoreException if a <code>CoreException</code> is
	 * 	thrown accessing this breakpoint's underlying marker
	 */
	public void setThreadFilter(IJavaThread thread) throws CoreException;
	
	/**
	 * Removes this breakpoint's thread filter in the given target, if any. 
	 * Has no effect if this breakpoint does not have a filter in the given target.
	 * 
	 * While this breakpoint has a thread filter in the given target,
	 * it will only suspend threads which are filtered.
	 * 
	 * @param target the target whose thread filter will be removed
	 * @exception CoreException if a <code>CoreException</code> is
	 * 	thrown accessing this breakpoint's underlying marker
	 */
	public void removeThreadFilter(IJavaDebugTarget target) throws CoreException;
	
	/**
	 * Returns the thread in the given target in which this breakpoint
	 * is enabled or <code>null</code> if this breakpoint is enabled in
	 * all threads in the given target.
	 * 
	 * @return the thread in the given target that this breakpoint is enabled for
	 * @exception CoreException if a <code>CoreException</code> is
	 * 	thrown accessing this breakpoint's underlying marker
	 */
	public IJavaThread getThreadFilter(IJavaDebugTarget target) throws CoreException;
	/**
	 * Returns the threads in which this breakpoint is enabled or an empty array 
	 * if this breakpoint is enabled in all threads.
	 * 
	 * @return the threads that this breakpoint is enabled for
	 * @exception CoreException if a <code>CoreException</code> is
	 * 	thrown accessing this breakpoint's underlying marker
	 */
	public IJavaThread[] getThreadFilters() throws CoreException; 
}

