package org.eclipse.jdt.internal.debug.core;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.IBreakpoint;
import org.eclipse.debug.core.model.IBreakpointFactoryDelegate;
import org.eclipse.jdt.debug.core.IJavaDebugConstants;

public class JavaBreakpointFactory implements IBreakpointFactoryDelegate {

	public JavaBreakpointFactory() {
	}
	
	public IBreakpoint createBreakpointFor(IMarker marker) throws DebugException {
		String markerType= null;
		try {
			markerType= marker.getType();
		} catch (CoreException ce) {
			throw new DebugException(ce.getStatus());
		}
		if (markerType.equals(IJavaDebugConstants.JAVA_LINE_BREAKPOINT)) {
			return new LineBreakpoint(marker);
		} else if (markerType.equals(IJavaDebugConstants.JAVA_WATCHPOINT)) {
			return new Watchpoint(marker);
		} else if (markerType.equals(IJavaDebugConstants.JAVA_METHOD_ENTRY_BREAKPOINT)) {
			return new MethodEntryBreakpoint(marker);
		} else if (markerType.equals(IJavaDebugConstants.JAVA_RUN_TO_LINE_BREAKPOINT)) {
			return new RunToLineBreakpoint(marker);
		} else if (markerType.equals(IJavaDebugConstants.JAVA_EXCEPTION_BREAKPOINT)) {
			return new ExceptionBreakpoint(marker);
		}
		return null;
	}

}

