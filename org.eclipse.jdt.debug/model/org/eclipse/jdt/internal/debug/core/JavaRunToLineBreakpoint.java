package org.eclipse.jdt.internal.debug.core;

import org.eclipse.core.resources.IMarker;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.debug.core.IJavaDebugConstants;
import org.eclipse.jdt.debug.core.IJavaRunToLineBreakpoint;

public class JavaRunToLineBreakpoint extends JavaLineBreakpoint implements IJavaRunToLineBreakpoint {
	
	// Thread label String keys
	private static final String RUN_TO_LINE_SYS= THREAD_LABEL + "run_to_line_sys";
	private static final String RUN_TO_LINE_USR= THREAD_LABEL + "run_to_line_usr";
		
	static String fMarkerType= IJavaDebugConstants.JAVA_RUN_TO_LINE_BREAKPOINT;		

	/**
	 * Create a run to line breakpoint
	 */
	public JavaRunToLineBreakpoint() {
	}

	/**
	 * Creates a run-to-line breakpoint in the
	 * given type, at the given line number. If a character range within the
	 * line is known, it may be specified by charStart/charEnd. Run-to-line
	 * breakpoints have a hit count of 1.
	 *
	 * @param type the type in which to create the breakpoint
	 * @param lineNumber the lineNumber on which the breakpoint is created - line
	 *   numbers are 1 based, associated with the compilation unit in which
	 *   the type is defined
	 * @param charStart the first character index associated with the breakpoint,
	 *   or -1 if unspecified
 	 * @param charEnd the last character index associated with the breakpoint,
	 *   or -1 if unspecified
	 * @return a run-to-line breakpoint
	 * @exception DebugException if unable to create the breakpoint marker due
	 *  to a lower level exception.
	 */
	public JavaRunToLineBreakpoint(IType type, int lineNumber, int charStart, int charEnd) throws DebugException {
		super(type, lineNumber, charStart, charEnd, 1, fMarkerType);
	}

/*	public String getFormattedThreadText(String threadName, String typeName, boolean systemThread) {
		int lineNumber= getAttribute(IMarker.LINE_NUMBER, -1);
		if (lineNumber > -1) {
			if (systemThread) {
				return getFormattedString(RUN_TO_LINE_SYS, new String[] {threadName, String.valueOf(lineNumber), typeName});
			} else {
				return getFormattedString(RUN_TO_LINE_USR, new String[] {threadName, String.valueOf(lineNumber), typeName});
			}
		}
		return "";
	}
	*/
	
	/**
	 * Run to line breakpoints should not be added to the breakpoint
	 * manager
	 */
	protected void addToBreakpointManager() throws DebugException {
		return;
	}	

}

