package org.eclipse.jdt.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.internal.debug.core.*;
import com.sun.jdi.VirtualMachine;
import java.util.Map;

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
		return createLineBreakpointCommon(type, lineNumber, charStart, charEnd, hitCount, IJavaDebugConstants.JAVA_LINE_BREAKPOINT);
	}
	
	/**
	 * Creates and returns a run-to-line breakpoint in the
	 * given type, at the given line number. If a character range within the
	 * line is known, it may be specified by charStart/charEnd. Run-to-line
	 * breakpoints have a hit count of 1.
	 * Note: the breakpoint is not added to the breakpoint manager
	 * - it is merely created.
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
	public static IMarker createRunToLineBreakpoint(IType type, int lineNumber, int charStart, int charEnd) throws DebugException {
		return createLineBreakpointCommon(type, lineNumber, charStart, charEnd, 1, IJavaDebugConstants.JAVA_RUN_TO_LINE_BREAKPOINT);
	}
	
	/**
	 * Common method for creating line breakpoints, either 'regular' or 'run to line'
	 */
	private static IMarker createLineBreakpointCommon(final IType type, final int lineNumber, final int charStart, final int charEnd, final int hitCount, final String exceptionType) throws DebugException {

		fgBreakpoint= null;

		IWorkspaceRunnable wr= new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {
				IResource resource= null;
				resource= type.getUnderlyingResource();
				if (resource == null) {
					resource= type.getJavaProject().getProject();
				}

				// create the marker
				fgBreakpoint= resource.createMarker(exceptionType);
				DebugPlugin.getDefault().getBreakpointManager().configureLineBreakpoint(fgBreakpoint, getPluginIdentifier(), true, lineNumber, charStart, charEnd);

				// configure the hit count and type handle
				DebugJavaUtils.setTypeAndHitCount(fgBreakpoint, type, hitCount);

				// configure the marker as a Java marker
				Map attributes= fgBreakpoint.getAttributes();
				JavaCore.addJavaElementMarkerAttributes(attributes, type);
				fgBreakpoint.setAttributes(attributes);
			}
		};
		
		try {
			ResourcesPlugin.getWorkspace().run(wr, null);
		} catch (CoreException e) {
			throw new DebugException(e.getStatus());
		}

		return fgBreakpoint;
	}
	
	/**
	 * Creates and returns an exception breakpoint for the
	 * given (throwable) type. Caught and uncaught specify where the exception
	 * should cause thread suspensions - that is, in caught and/or uncaught locations.
	 * Checked indicates if the given exception is a checked exception.
	 * Note: the breakpoint is not added to the breakpoint manager
	 * - it is merely created.
	 *
	 * @param type the exception for which to create the breakpoint
	 * @param caught whether to suspend in caught locations
	 * @param uncaught whether to suspend in uncaught locations
 	 * @param checked whether the exception is a checked exception
	 * @return an exception breakpoint
	 * @exception DebugException if unable to create the breakpoint marker due
	 *  to a lower level exception.
	 */
	public static IMarker createExceptionBreakpoint(final IType exception, final boolean caught, final boolean uncaught, final boolean checked) throws DebugException {
		// determine the resource to associate the marker with

		fgBreakpoint= null;

		IWorkspaceRunnable wr= new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {
				IResource resource= null;
				resource= exception.getUnderlyingResource();

				if (resource == null) {
					resource= exception.getJavaProject().getProject();
				}
				
				// if the exception breakpoint already exists in the breakpoint mgr.,
				// just use it
				IMarker existing= findExistingExceptionBreakpoint(exception);
				if (existing != null) {
					fgBreakpoint= existing;
					return;
				}
				
				// create the marker
				fgBreakpoint= resource.createMarker(IJavaDebugConstants.JAVA_EXCEPTION_BREAKPOINT);
				// configure the standard attributes
				DebugPlugin.getDefault().getBreakpointManager().configureBreakpoint(fgBreakpoint, getPluginIdentifier(), true);
				// configure caught, uncaught, checked, and the type attributes
				DebugJavaUtils.configureExceptionBreakpoint(fgBreakpoint, caught, uncaught, checked, exception);

				// configure the marker as a Java marker
				Map attributes= fgBreakpoint.getAttributes();
				JavaCore.addJavaElementMarkerAttributes(attributes, exception);
				fgBreakpoint.setAttributes(attributes);
			}

		};
		
		try {
			ResourcesPlugin.getWorkspace().run(wr, null);
		} catch (CoreException e) {
			throw new DebugException(e.getStatus());
		}

		return fgBreakpoint;
	}

	private static IMarker findExistingExceptionBreakpoint(IType type) {
		IBreakpointManager manager= DebugPlugin.getDefault().getBreakpointManager();
		IMarker[] allBreakpoints= manager.getBreakpoints(IJavaDebugConstants.JAVA_EXCEPTION_BREAKPOINT);
		for (int i = 0; i < allBreakpoints.length; i++) {
			IMarker bp= allBreakpoints[i];
			if (DebugJavaUtils.getType(bp).equals(type)) {
				return bp;
			}
		}
		return null;
	}

	/**
	 * Creates and returns a method entry breakpoint in the
	 * given method.
	 * If hitCount is > 0, the breakpoint will suspend execution when it is
	 * "hit" the specified number of times. Note: the breakpoint is not
	 * added to the breakpoint manager - it is merely created.
	 *
	 * @param method the method in which to suspend on entry
	 * @param hitCount the number of times the breakpoint will be hit before
	 *   suspending execution - 0 if it should always suspend
	 * @return a method entry breakpoint
	 * @exception DebugException if unable to create the breakpoint marker due
	 *  to a lower level exception.
	 */
	public static IMarker createMethodEntryBreakpoint(final IMethod method, final int hitCount) throws DebugException {
		// determine the resource to associate the marker with

		fgBreakpoint= null;

		IWorkspaceRunnable wr= new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {
				IResource resource= null;
				resource= method.getUnderlyingResource();
				if (resource == null) {
					resource= method.getJavaProject().getProject();
				}

				// create the marker
				fgBreakpoint= resource.createMarker(IJavaDebugConstants.JAVA_METHOD_ENTRY_BREAKPOINT);
				
				// find the source range if available
				int start = -1;
				int end = -1;
				ISourceRange range = method.getSourceRange();
				if (range != null) {
					start = range.getOffset();
					end = start + range.getLength() - 1;
				}
				// configure the standard attributes
				DebugPlugin.getDefault().getBreakpointManager().configureLineBreakpoint(fgBreakpoint, getPluginIdentifier(), true, -1, start, end);
				// configure the type handle and hit count
				DebugJavaUtils.setTypeAndHitCount(fgBreakpoint, method.getDeclaringType(), hitCount);

				// configure the method handle
				DebugJavaUtils.setMethod(fgBreakpoint, method);
				
				// configure the marker as a Java marker
				Map attributes= fgBreakpoint.getAttributes();
				JavaCore.addJavaElementMarkerAttributes(attributes, method);
				fgBreakpoint.setAttributes(attributes);
			}

		};
		
		try {
			ResourcesPlugin.getWorkspace().run(wr, null);
		} catch (CoreException e) {
			throw new DebugException(e.getStatus());
		}
		
		return fgBreakpoint;
	}
	/**
	 * Returns the hit count of the given breakpoint or -1 if the attribute is not set.
	 * 
	 * @param breakpoint the breakpoint
	 * @return hit count, or -1
	 */
	public static int getHitCount(IMarker breakpoint) {
		return DebugJavaUtils.getHitCount(breakpoint);
	}
	
	/**
	 * Sets the hit count of the given breakpoint
	 *
	 * @param breakpoint the breakpoint
	 * @param hitCount the number of times the breakpoint is hit before suspending execution
	 * @exception CoreException if an exception occurrs updating the marker
	 */
	public static void setHitCount(IMarker breakpoint, int hitCount) throws CoreException {
		DebugJavaUtils.setHitCount(breakpoint, hitCount);
	}

	/**
	 * Returns the member the given breakpoint is installed in,
	 * or <code>null</code> if a member is not determinable.
	 *
	 * @param breakpoint the breakpoint
	 * @return a member, or <code>null</code>
	 */
	public static IMember getMember(IMarker breakpoint) {
		try {
			return DebugJavaUtils.getMember(breakpoint);
		} catch (CoreException e) {
			return null;
		}
	}
	/**
	 * Returns the method the given breakpoint is installed in
	 * or <code>null</code> if breakpoint is not installed in a method.
	 *
	 * @param breakpoint the breakpoint
	 * @return a method, or <code>null</code>
	 */
	public static IMethod getMethod(IMarker breakpoint) {
		return DebugJavaUtils.getMethod(breakpoint);
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
	public static IType getType(IMarker breakpoint) {
		return DebugJavaUtils.getType(breakpoint);
	}
	/**
	 * Returns whether the given breakpoint is an exception
	 * breakpoint that will suspend in caught locations.
	 *
	 * @param breakpoint the breakpoint
	 * @return whether the given breakpoint is an exception
	 * breakpoint that will suspend in caught locations
	 */
	public static boolean isCaught(IMarker breakpoint) {
		return DebugJavaUtils.isCaught(breakpoint);
	}
	/**
	 * Returns whether the given breakpoint is an exception
	 * breakpoint for a checked exception.
	 *
	 * @param breakpoint the breakpoint
	 * @return whether the given breakpoint is an exception
	 * breakpoint for a checked exception
	 */
	public static boolean isChecked(IMarker breakpoint) {
		return DebugJavaUtils.isChecked(breakpoint);
	}
	/**
	 * Returns whether the given breakpoint
	 * is an exception breakpoint.
	 *
	 * @param breakpoint the breakpoint
	 * @return whether the given breakpoint
	 * is an exception breakpoint
	 */
	public static boolean isExceptionBreakpoint(IMarker breakpoint) {
		return DebugJavaUtils.isExceptionBreakpoint(breakpoint);
	}
	/**
	 * Returns whether the given breakpoint is installed in at least one debug target.
	 *
	 * @param breakpoint the breakpoint
	 * @return whether the given breakpoint is installed in at least one debug target
	 */
	public static boolean isInstalled(IMarker breakpoint) {
		return DebugJavaUtils.isInstalled(breakpoint);
	}
	/**
	 * Returns whether the given breakpoint is a method entry breakpoint.
	 *
	 * @param breakpoint the breakpoint
	 * @return whether the given breakpoint is a method entry breakpoint
	 */
	public static boolean isMethodEntryBreakpoint(IMarker breakpoint) {
		return DebugJavaUtils.isMethodEntryBreakpoint(breakpoint);
	}
	/**
	 * Returns whether the given breakpoint is a run to line breakpoint.
	 *
	 * @param breakpoint the breakpoint
	 * @retrun whether the given breakpoint is a run to line breakpoint
	 */
	public static boolean isRunToLineBreakpoint(IMarker breakpoint) {
		return DebugJavaUtils.isRunToLineBreakpoint(breakpoint);
	}
	/**
	 * Returns whether the given breakpoint is an exception
	 * breakpoint that will suspend in uncaught locations.
	 *
	 * @param breakpoint the breakpoint
	 * @return whether the given breakpoint is an exception
	 * breakpoint that will suspend in uncaught locations
	 */
	public static boolean isUncaught(IMarker breakpoint) {
		return DebugJavaUtils.isUncaught(breakpoint);
	}
}


