package org.eclipse.jdt.internal.debug.pattern;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.jdt.debug.core.JDIDebugModel;

/**
 * Creates pattern breakpoints. Pattern breakpoints are line
 * breakpoints to be installed in java classes that were created
 * by compiling a non-Java source file into a .java file, which is
 * then compiled into a .class file. By specifying the pattern of
 * the name of the Java type associated with the source file that will
 * be loaded at runtime, breakpoints are installed.
 * <p>
 * When a type is loaded at debug time, the pattern debug target
 * checks all pattern breakpoints that match the name of the type
 * just loaded. For each match, the source file name debug attribute of
 * the loaded type is checked for a match against the breakpoint's
 * associated file name. If they match, the breakpoint is installed.
 * This requires that line number and source file name debug
 * attributes in the .class file loaded at debug time, reflect 
 * the original source.
 * <p>
 * Pattern breakpoints are only installed in pattern debug targets.
 */
public class PatternBreakpointFactory {
	/**
	 * Singleton
	 */
	private static PatternBreakpointFactory fgDefault;
	
	/**
	 * Singleton
	 */	
	public static PatternBreakpointFactory getDefault() {
		if (fgDefault == null)
			fgDefault= new PatternBreakpointFactory();
		return fgDefault;
	}
	
	/**
	 * Singleton
	 */
	private PatternBreakpointFactory() {}
	
	public IMarker createPatternBreakpoint(IFile file, int line, String pattern) throws DebugException {
		BreakpointCreator wr= new BreakpointCreator(file, line, pattern);

		try {
			ResourcesPlugin.getWorkspace().run(wr, null);
		} catch (CoreException e) {
			throw new DebugException(e.getStatus());
		}
		
		return wr.getBreakpoint();
	}
}

/**
 * @see org.eclipse.core.resources.IWorkspaceRunnable
 */
class BreakpointCreator implements IWorkspaceRunnable {
	protected IMarker fBreakpoint;
	protected IFile fFile;
	protected int fLine;
	protected String fPattern;
	public BreakpointCreator(IFile file, int line, String pattern) {
		fFile = file;
		fLine = line;
		fPattern = pattern;
	}

	public IMarker getBreakpoint() {
		return fBreakpoint;
	}
	
	/**
	 * @see IWorkspaceRunnable
	 */
	public void run(IProgressMonitor monitor) throws CoreException {
		// create the marker
		fBreakpoint = fFile.createMarker(PatternDebugModel.PATTERN_BREAKPOINT);
		// configure the standard attributes
		DebugPlugin.getDefault().getBreakpointManager().configureLineBreakpoint(
			fBreakpoint, JDIDebugModel.getPluginIdentifier(), true, fLine, -1, -1);
		fBreakpoint.setAttribute(PatternDebugModel.PATTERN, fPattern);
	}
}