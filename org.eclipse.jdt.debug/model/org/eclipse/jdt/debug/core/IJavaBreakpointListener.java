package org.eclipse.jdt.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * Provides event notification for Java breakpoints.
 * Listeners register with the <code>JDIDebugModel</code>.
 * <p>
 * Clients are intended to implement this interface.
 * </p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under development and expected to 
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback 
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken
 * (repeatedly) as the API evolves.
 * </p>
 */
public interface IJavaBreakpointListener {
	
	/**
	 * Notification that the given breakpoint is about to be added to
	 * the specified target.
	 * 
	 * @param target Java debug target
	 * @param breakpoint Java breakpoint
	 */
	public void breakpointAdded(IJavaDebugTarget target, IJavaBreakpoint breakpoint);
	
	/**
	 * Notification that the given breakpoint has been installed in
	 * the specified target.
	 * 
	 * @param target Java debug target
	 * @param breakpoint Java breakpoint
	 */
	public void breakpointInstalled(IJavaDebugTarget target, IJavaBreakpoint breakpoint);
	
	/**
	 * Notification that the given breakpoint has been hit
	 * in the specified thread - returns whether the thread
	 * should suspend. This allows the listener to override
	 * default thread suspension when a breakpoint is hit.
	 * If at least one listener returns <code>true</code>,
	 * the breakpoint will causes the thread to suspend.
	 * 
	 * @param thread Java thread
	 * @param breakpoint Java breakpoint
	 * @return whether the thread should suspend
	 */
	public boolean breakpointHit(IJavaThread thread, IJavaBreakpoint breakpoint);	
	
	/**
	 * Notification that the given breakpoint has been removed
	 * from the specified target.
	 * 
	 * @param target Java debug target
	 * @param breakpoint Java breakpoint
	 */
	public void breakpointRemoved(IJavaDebugTarget target, IJavaBreakpoint breakpoint);	
	

}
