package org.eclipse.jdt.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.Map;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.*;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.internal.debug.core.*;

import com.sun.jdi.VirtualMachine;

/**
 * The JDI debug model plug-in provides an implementation of a debug
 * model based on JDI. This class provides utility methods for
 * creating debug targets and breakpoints specific to the JDI debug
 * model, as well as accessing attributes of breakpoints created by
 * this debug model.
 * <p>
 * To provide access to behavior and information specific to the JDI
 * debug model, a set of interfaces are defined which extend the base
 * set of debug element interfaces. For example, <code>IJavaStackFrame</code>
 * is declared to extend <code>IStackFrame</code>, and provides methods
 * specific to this debug model. The specialized interfaces are also
 * available as adapters from the debug elements generated from this
 * model.
 * </p>
 * <p>
 * Clients are not intended to instantiate or subclass this class;
 * this class provides static utility methods only.
 * </p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under development and expected to 
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback 
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken
 * (repeatedly) as the API evolves.
 * </p>
 * @see IJavaDebugTarget
 * @see IJavaThread
 * @see IJavaStackFrame
 * @see IJavaVariable
 */
public class JDIDebugModel {

	/**
	 * The last breakpoint that was created, or <code>null</code>
	 */
	private static IMarker fgBreakpoint= null;
	
	/**
	 * The most recently created debug target
	 */
	private static IJavaDebugTarget fgTarget = null;

	/**
	 * Not to be instantiated.
	 */
	private JDIDebugModel() {
		super();
	}
	
	/**
	 * Creates and returns a debug target for the given VM, with
	 * the specified name, and associates the debug target with the
	 * given process for console I/O. The allow terminate flag specifies whether
	 * the debug target will support termination (<code>ITerminate</code>).
	 * The allow disconnect flag specifies whether the debug target will
	 * support disconnection (<code>IDisconnect</code>). Launching the actual
	 * VM is a client responsibility.
	 *
	 * @param vm the VM do create a debug target for
	 * @param name the name to associate with the VM, which will be 
	 *   returned from <code>IDebugTarget.getName</code>. If <code>null</code>
	 *   the name will be retrieved from the underlying VM.
	 * @param process the process to associate with the debug target,
	 *   which will be returned from <code>IDebugTarget.getProcess</code>
	 * @param allowTermiante specifies if the target will support termianation
	 * @param allowDisconnect specifies if the target will support disconnection
	 * @return a debug target
	 * @see org.eclipse.debug.core.model.ITerminate
	 * @see org.eclipse.debug.core.model.IDisconnect
	 */
	public static IDebugTarget newDebugTarget(final VirtualMachine vm, final String name, final IProcess process, final boolean allowTerminate, final boolean allowDisconnect) {
		fgTarget = null;
		IWorkspaceRunnable r = new IWorkspaceRunnable() {
			public void run(IProgressMonitor m) {
				fgTarget= new JDIDebugTarget(vm, name, allowTerminate, allowDisconnect, process);
			}
		};
		try {
			ResourcesPlugin.getWorkspace().run(r, null);
		} catch (CoreException e) {
			DebugJavaUtils.logError(e);
		}
		return fgTarget;
	}

	/**
	 * Returns the identifier for this JDI debug model plug-in
	 *
	 * @return plugin identifier
	 */
	public static String getPluginIdentifier() {
		return JDIDebugPlugin.getDefault().getDescriptor().getUniqueIdentifier();
	}
	
	/**
	 * Creates and returns a line breakpoint in the
	 * given type, at the given line number. If a character range within the
	 * line is known, it may be specified by charStart/charEnd.
	 * If hitCount is > 0, the breakpoint will suspend execution when it is
	 * "hit" the specified number of times. Note: the breakpoint is not
	 * added to the breakpoint manager - it is merely created.
	 *
	 * @param type the type in which to create the breakpoint
	 * @param lineNumber the lineNumber on which the breakpoint is created - line
	 *   numbers are 1 based, associated with the compilation unit in which
	 *   the type is defined
	 * @param charStart the first character index associated with the breakpoint,
	 *   or -1 if unspecified
 	 * @param charEnd the last character index associated with the breakpoint,
	 *   or -1 if unspecified
	 * @param hitCount the number of times the breakpoint will be hit before
	 *   suspending execution - 0 if it should always suspend
	 * @return a line breakpoint
	 * @exception DebugException if unable to create the breakpoint marker due
	 *  to a lower level exception.
	 */
	public static IMarker createLineBreakpoint(IType type, int lineNumber, int charStart, int charEnd, int hitCount) throws DebugException {
		return (new LineBreakpoint(type, lineNumber, charStart, charEnd, hitCount, IJavaDebugConstants.JAVA_LINE_BREAKPOINT)).getMarker();
	}

	public static IBreakpointManager getBreakpointManager() {
		return DebugPlugin.getDefault().getBreakpointManager();
	}

	/**
	 * Returns the hit count of the given breakpoint or -1 if the attribute is not set.
	 * 
	 * @param breakpoint the breakpoint
	 * @return hit count, or -1
	 */
	public static int getHitCount(IMarker marker) {
		JavaBreakpoint breakpoint= getBreakpoint(marker);
		if (breakpoint instanceof LineBreakpoint) {
			return ((LineBreakpoint)breakpoint).getHitCount();
		}
		return -1;
	}
	
	/**
	 * Sets the hit count of the given breakpoint
	 *
	 * @param breakpoint the breakpoint
	 * @param hitCount the number of times the breakpoint is hit before suspending execution
	 * @exception CoreException if an exception occurrs updating the marker
	 */
	public static void setHitCount(IMarker marker, int hitCount) throws CoreException {
		JavaBreakpoint breakpoint= getBreakpoint(marker);
		if (breakpoint instanceof LineBreakpoint) {
			((LineBreakpoint) breakpoint).setHitCount(hitCount);
		}
	}

	/**
	 * Returns the type the given breakpoint is installed in
	 * or <code>null</code> if breakpoint is not installed in a type. If
	 * the breakpoint is an exception breakpoint, the type associated with
	 * the exception is returned. For a method entry breakpoint, the
	 * method's declaring type is retured.
	 *
	 * @param breakpoint the breakpoint
	 * @return a type, or <code>nulll</code>
	 */
	public static IType getType(IMarker marker) {
		JavaBreakpoint breakpoint= getBreakpoint(marker);
		if (breakpoint != null) {
			return breakpoint.getInstalledType();
		}
		return null;
	}

	/**
	 * Returns the JavaBreakpoint in the breakpoint manager associated
	 * with the given marker. If no such breakpoint exists, returns 
	 * <code>null</code>.
	 */
	private static JavaBreakpoint getBreakpoint(IMarker marker) {
		IBreakpoint breakpoint= getBreakpointManager().getBreakpoint(marker);
		if (breakpoint instanceof JavaBreakpoint) {
			return (JavaBreakpoint) breakpoint;
		}
		return null;
	}

}


