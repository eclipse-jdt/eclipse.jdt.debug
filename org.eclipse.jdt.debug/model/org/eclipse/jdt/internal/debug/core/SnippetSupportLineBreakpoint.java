package org.eclipse.jdt.internal.debug.core;

import org.eclipse.core.resources.IResource;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.debug.core.IJavaDebugConstants;
import org.eclipse.jdt.debug.core.ISnippetSupportLineBreakpoint;

public class SnippetSupportLineBreakpoint extends JavaLineBreakpoint implements ISnippetSupportLineBreakpoint {
	
	static String fMarkerType= IJavaDebugConstants.SNIPPET_SUPPORT_LINE_BREAKPOINT;	

	/**
	 * Constructor for SnippetSupportLineBreakpoint
	 */
	public SnippetSupportLineBreakpoint() {
		super();
	}

	/**
	 * Constructor for SnippetSupportLineBreakpoint
	 */
	public SnippetSupportLineBreakpoint(IType type, int lineNumber, int charStart, int charEnd, int hitCount) throws DebugException {
		super(type, lineNumber, charStart, charEnd, hitCount, fMarkerType);
	}

	/**
	 * Constructor for SnippetSupportLineBreakpoint
	 */
	public SnippetSupportLineBreakpoint(IType type, int lineNumber, int charStart, int charEnd, int hitCount, String markerType) throws DebugException {
		super(type, lineNumber, charStart, charEnd, hitCount, markerType);
	}

	/**
	 * Return the resource for this breakpoint.
	 * The resource for a SnippetSupportLineBreakpoint is the java project
	 * it belongs to.
	 */	
	protected IResource getResource(IType type) {
		return type.getJavaProject().getProject();
	}
	
	/**
	 * Run to line breakpoints should not be added to the breakpoint
	 * manager
	 */
	protected void addToBreakpointManager() throws DebugException {
		return;
	}	
	

}

