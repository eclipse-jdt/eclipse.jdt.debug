package org.eclipse.jdt.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.ILineBreakpoint;


/**
 * Java line breakpoints are java breakpoints that suspend execution
 * when a particular line of code is reached.
 * 
 * Clients are not intended to implement this interface
 */
public interface IJavaLineBreakpoint extends IJavaBreakpoint, ILineBreakpoint {	
	
	/**
	 * Returns whether this breakpoint can be a conditional
	 * breakpoint. Conditional breakpoints only suspend when
	 * the assigned condition evaluates true.
	 * 
	 * @return whether this breakpoint supports a condition
	 */
	public boolean supportsCondition();
	/**
	 * Returns the conditional expression associated with this breakpoint,
	 * the empty String ("") if no condition is defined, or <code>null</code>
	 * if this breakpoint does not support a condition.
	 * 
	 * @return this breakpoint's conditional expression
	 * @exception CoreException if a <code>CoreException</code> is
	 * 	thrown accessing this breakpoint's underlying marker
	 */
	public String getCondition() throws CoreException;
	/**
	 * Sets the condition associated with this breakpoint.
	 * When the condition is enabled, this breakpoint will only suspend execution
	 * when the given condition evaluates to <code>true</code>.
	 * Setting the condition to the empty String ("") removes
	 * the condition.
	 * 
	 * If this method does not support conditions, setting the condition has
	 * no effect.
	 * 
	 * @exception CoreException if a <code>CoreException</code> is
	 * 	thrown accessing this breakpoint's underlying marker
	 */
	public void setCondition(String condition) throws CoreException;
	/**
	 * Returns whether the condition on this breakpoint is enabled.
	 * 
	 * @return whether this breakpoint's condition is enabled
	 * @exception CoreException if a <code>CoreException</code> is
	 * 	thrown accessing this breakpoint's underlying marker
	 */
	public boolean isConditionEnabled() throws CoreException;
	/**
	 * Sets the enabled state of this breakpoint's condition to the given
	 * state. When enabled, this breakpoint will only suspend when its
	 * condition evaluates to true. When disabled, this breakpoint will suspend
	 * as it would with no condition defined.
	 * 
	 * @exception CoreException if a <code>CoreException</code> is
	 * 	thrown accessing this breakpoint's underlying marker
	 */
	public void setConditionEnabled(boolean enabled) throws CoreException;

}

