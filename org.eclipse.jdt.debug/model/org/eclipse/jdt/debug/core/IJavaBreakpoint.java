package org.eclipse.jdt.debug.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.IBreakpoint;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.debug.core.JDIDebugTarget;

import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.Event;

public interface IJavaBreakpoint extends IBreakpoint {
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
	 * Returns whether this breakpoint is installed in at least
	 * one debug target.
	 */
	public boolean isInstalled();
	/**
	 * Returns the type the given breakpoint is associated with
	 * or <code>null</code> a type cannot be resolved.
	 */
	public IType getType();	

}

