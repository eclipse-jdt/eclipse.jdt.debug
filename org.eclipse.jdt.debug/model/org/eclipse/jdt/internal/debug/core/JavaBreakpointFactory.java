package org.eclipse.jdt.internal.debug.core;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.IBreakpoint;
import org.eclipse.debug.core.model.IBreakpointFactoryDelegate;
import org.eclipse.jdt.debug.core.IJavaDebugConstants;
import org.eclipse.jdt.debug.core.JDIDebugModel;

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
		IBreakpoint breakpoint= null;

		if (markerType.equals(IJavaDebugConstants.JAVA_LINE_BREAKPOINT)) {
			breakpoint= new JavaLineBreakpoint();
		} else if (markerType.equals(IJavaDebugConstants.JAVA_WATCHPOINT)) {
			breakpoint= new JavaWatchpoint();
		} else if (markerType.equals(IJavaDebugConstants.JAVA_METHOD_ENTRY_BREAKPOINT)) {
			breakpoint= new JavaMethodEntryBreakpoint();
		} else if (markerType.equals(IJavaDebugConstants.JAVA_EXCEPTION_BREAKPOINT)) {
			breakpoint= new JavaExceptionBreakpoint();
		}
		if (breakpoint != null) {
			breakpoint.setMarker(marker);
		}
		return breakpoint;
	}

}

