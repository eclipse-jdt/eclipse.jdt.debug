/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.core;

import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.core.dom.Message;


/**
 * Provides event and error notification for Java breakpoints.
 * Listeners register with the <code>JDIDebugModel</code>.
 * <p>
 * Clients are intended to implement this interface.
 * </p>
 * @since 2.0
 */
public interface IJavaBreakpointListener {
	
	/**
	 * Return code in response to a "breakpoint hit" notification, indicating
	 * that this listener thinks the breakpoint should suspend the given
	 * thread.
	 * 
	 * @since 3.0
	 */
	public static int SUSPEND= 0x0000;
	/**
	 * Return code in response to a "breakpoint hit" notification, indicating
	 * that this listener thinks the breakpoint should not suspend the given
	 * thread.
	 * 
	 * @since 3.0
	 */
	public static int DONT_SUSPEND= 0x0001;
	/**
	 * Return code in response to an "installing" notification, indicating
	 * that this listener thinks the breakpoint should be installed.
	 * 
	 * @since 3.0
	 */
	public static int INSTALL= 0x0000;
	/**
	 * Return code in response to an "installing" notification, indicating
	 * that this listener does not think the breakpoint should be installed.
	 * 
	 * @since 3.0
	 */
	public static int DONT_INSTALL= 0x0001;
	/**
	 * Return code indicating that this listener does not care about the
	 * notification.
	 * 
	 * @since 3.0
	 */
	public static int DONT_CARE= 0x0002;
	
	/**
	 * Notification that the given breakpoint is about to be added to
	 * the specified target. This message is sent before the breakpoint
	 * is actually added to the debut target (i.e. this is a
	 * pre-notification).
	 * 
	 * @param target Java debug target
	 * @param breakpoint Java breakpoint
	 */
	public void addingBreakpoint(IJavaDebugTarget target, IJavaBreakpoint breakpoint);
	
	/**
	 * Notification that the given breakpoint is about to be installed in
	 * the specified target, in the specified type. Returns whether the
	 * installation should proceed. If any registered listener returns
	 * <code>DONT_INSTALL</code> the breakpoint is not installed in the given
	 * target for the given type.
	 * 
	 * @param target Java debug target
	 * @param breakpoint Java breakpoint
	 * @param type the type (class or interface) the breakpoint is about to be installed in 
	 *  or <code>null</code> if the given breakpoint is not installed in a specific type
	 *  (one of <code>IJavaClassType</code>, <code>IJavaInterfaceType</code>, or 
	 *  <code>IJavaArrayType</code>)
	 * @return whether the the breakpoint should be installed in the given type and target,
	 *  or whether this listener doesn't care.
	 * @since 3.0
	 */
	public int installingBreakpoint(IJavaDebugTarget target, IJavaBreakpoint breakpoint, IJavaType type);
		
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
	 * The breakpoint will cause the thread to suspend if
	 * at least one listener returns <code>SUSPEND</code>
	 * or if all listeners vote <code>DONT_CARE</code>.
	 * 
	 * @param thread Java thread
	 * @param breakpoint Java breakpoint
	 * @return whether the thread should suspend or whether this
	 *  listener doesn't care.
	 * @since 3.0
	 */
	public int breakpointHit(IJavaThread thread, IJavaBreakpoint breakpoint);	
	
	/**
	 * Notification that the given breakpoint has been removed
	 * from the specified target.
	 * 
	 * @param target Java debug target
	 * @param breakpoint Java breakpoint
	 */
	public void breakpointRemoved(IJavaDebugTarget target, IJavaBreakpoint breakpoint);	
	
	/**
	 * Notification that the given breakpoint had runtime errors in its conditional
	 * expression.
	 * 
	 * @param breakpoint the breakpoint
	 * @param errors the runtime errors that occurred evaluating the breakpoint's
	 *  condition
	 */
	public void breakpointHasRuntimeException(IJavaLineBreakpoint breakpoint, DebugException exception);
	
	/**
	 * Notification that the given breakpoint has compilation errors in its conditional
	 * expression.
	 * 
	 * @param breakpoint the breakpoint
	 * @param errors the compilation errors in the breakpoint's condition
	 */
	public void breakpointHasCompilationErrors(IJavaLineBreakpoint breakpoint, Message[] errors);
}
