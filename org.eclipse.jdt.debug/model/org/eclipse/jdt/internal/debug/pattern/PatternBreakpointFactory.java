package org.eclipse.jdt.internal.debug.pattern;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.jdt.debug.core.JDIDebugModel;

/**
 * Creates pattern breakpoints.  Pattern breakpoints
 * are only installed in <code>PatternDebugTarget</code>s.
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
	
	public IMarker createLineBreakpoint(IFile file, int line) throws DebugException {
		BreakpointCreator wr= new BreakpointCreator(file, line);

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
	public BreakpointCreator(IFile file, int line) {
		fFile = file;
		fLine = line;
	}

	public IMarker getBreakpoint() {
		return fBreakpoint;
	}
	
	/**
	 * @see IWorkspaceRunnable
	 */
	public void run(IProgressMonitor monitor) throws CoreException {
		// create the marker
		fBreakpoint = fFile.createMarker(JDIDebugModel.getPluginIdentifier() + ".patternBreakpoint");
		// configure the standard attributes
		DebugPlugin.getDefault().getBreakpointManager().configureLineBreakpoint(
			fBreakpoint, JDIDebugModel.getPluginIdentifier(), true, fLine, -1, -1);
	}
}