
package org.eclipse.jdt.internal.debug.core;

import org.eclipse.jdt.core.dom.Message;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;

public interface IJavaConditionalBreakpointListener {

	public void breakpointHasRuntimeException(IJavaLineBreakpoint breakpoint, Throwable exception);
	
	public void breakpointHasCompilationErrors(IJavaLineBreakpoint breakpoint, Message[] errors);

}
