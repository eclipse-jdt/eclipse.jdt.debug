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
 * @since 2.0
 */
public interface IJavaBreakpointListener {
	
	/**
	 * Notification that the given breakpoint is about to be added to
	 * the specified target.
	 * 
	 * @param target Java debug target
	 * @param breakpoint Java breakpoint
	 */
	public void addingBreakpoint(IJavaDebugTarget target, IJavaBreakpoint breakpoint);
	
	/**
	 * Notification that the given pattern breakpoint is about to be installed in
	 * the specified target, in the specified type. Returns whether the
	 * installation should proceed. If any registered listener returns
	 * <code>false</code> the breakpoint is not installed in the given
	 * target for the given type.
	 * 
	 * @param target Java debug target
	 * @param breakpoint Java breakpoint
	 * @param type the type (class or interface) the breakpoint is about to be installed in 
	 *  (one of <code>IJavaClassType</code>, <code>IJavaInterfaceType</code>, or 
	 *  <code>IJavaArrayType</code>)
	 * @return whether the the breakpoint should be installed in the given type and target
	 */
	public boolean installingBreakpoint(IJavaDebugTarget target, IJavaPatternBreakpoint breakpoint, IJavaType type);
		
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
