package org.eclipse.jdt.debug.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.IBreakpoint;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.debug.core.JDIDebugTarget;

import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.Event;

public interface IJavaBreakpoint extends IBreakpoint {
	/**
	 * Handle the given event which is associated with this breakpoint
	 * and the given target.
	 */
	public void handleEvent(Event event, JDIDebugTarget target);
	/**
	 * Returns the text that should be displayed on a thread when the
	 * breakpoint is hit.
	 * @param threadName  the name of the thread this breakpoint has suspended
	 * @param qualified  whether to display fully qualified names
	 * @param systemThread  whether the thread this breakpoint has suspended
	 * 			is a system thread.
	 * 
	 * @return the text to be displayed on the thread
	 */
	public String getThreadText(String threadName, boolean qualified, boolean systemThread);
	/**
	 * Returns whether this kind of breakpoint is supported by the given
	 * virtual machine.
	 */
	public boolean isSupportedBy(VirtualMachine vm);
	/**
	 * @see IBreakpoint#addToTarget(IDebugTarget)
	 */
	public abstract void addToTarget(JDIDebugTarget target);
	/**
	 * @see IBreakpoint#changeForTarget(IDebugTarget)
	 */
	public abstract void changeForTarget(JDIDebugTarget target);
	/**
	 * @see IBreakpoint#removeFromTarget(IDebugTarget)
	 */
	public void removeFromTarget(JDIDebugTarget target);
	/**
	 * Returns whether this breakpoint is installed in at least
	 * one debug target.
	 */
	public boolean isInstalled();
	/**
	 * Returns the type the given breakpoint is installed in
	 * or <code>null</code> a type cannot be resolved.
	 */
	public IType getInstalledType();	
	/**
	 * Decrements the install count on this breakpoint. If the new
	 * install count is 0, the <code>EXPIRED</code> attribute is reset to
	 * <code>false</code> (since any hit count breakpoints that auto-expired
	 * should be re-enabled when the debug session is over).
	 */
	public void decrementInstallCount() throws CoreException;
	/**
	 * Return whether this breakpoint is enabled.
	 */
	public boolean isEnabled();	

}

