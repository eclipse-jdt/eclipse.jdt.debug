
package org.eclipse.jdt.internal.debug.core;

import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.core.dom.Message;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;

public interface IJavaConditionalBreakpointListener {

	/**
	 * Notifies this listener that the given breakpoint has runtime errors
	 * 
	 * @param breakpoint the breakpoint
	 * @param errors the runtime errors that occurred evaluating the breakpoint's
	 *  condition
	 */
	public void breakpointHasRuntimeException(IJavaLineBreakpoint breakpoint, DebugException exception);
	
	/**
	 * Notifies this listener that the given breakpoint has compilation errors
	 * 
	 * @param breakpoint the breakpoint
	 * @param errors the compilation errors in the breakpoint's condition
	 */
	public void breakpointHasCompilationErrors(IJavaLineBreakpoint breakpoint, Message[] errors);
}
