package org.eclipse.jdt.internal.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.core.resources.IResource;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.debug.core.ISnippetSupportLineBreakpoint;

/**
 * @see ISnippetSupportLineBreakpoint
 */
public class SnippetSupportLineBreakpoint extends JavaLineBreakpoint implements ISnippetSupportLineBreakpoint {
	
	private static final String SNIPPET_SUPPORT_LINE_BREAKPOINT= "org.eclipse.jdt.debug.snippetSupportLineBreakpointMarker"; //$NON-NLS-1$

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
		super(type, lineNumber, charStart, charEnd, hitCount, SNIPPET_SUPPORT_LINE_BREAKPOINT);
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
	 * Snippet support line breakpoints should not be added to the breakpoint
	 * manager
	 */
	protected void addToBreakpointManager() throws DebugException {
		return;
	}	
	

}

