
package org.eclipse.jdt.internal.debug.core;

import org.eclipse.jdt.core.dom.Message;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaThread;

public interface IJavaConditionalBreakpointListener {

	/**
	 * Notifies this listener that the given breakpoint has runtime errors
	 * 
	 * @param breakpoint the breakpoint
	 * @param errors the runtime errors that occurred evaluating the breakpoint's
	 *  condition
	 */
	public void breakpointHasRuntimeException(IJavaLineBreakpoint breakpoint, Throwable exception);
	
	/**
	 * Notifies this listener that the given breakpoint has compilation errors
	 * 
	 * @param breakpoint the breakpoint
	 * @param errors the compilation errors in the breakpoint's condition
	 */
	public void breakpointHasCompilationErrors(IJavaLineBreakpoint breakpoint, Message[] errors);

	/**
	 * Notifies this listener that the evaluation of the given breakpoint's condition
	 * has exceeded some timeout threshold.
	 * 
	 * @param breakpoint the breakpoint
	 * @param thread the thread in which the evaluation is occurring
	 */
	public void breakpointHasTimedOut(IJavaLineBreakpoint breakpoint, IJavaThread thread);
}
