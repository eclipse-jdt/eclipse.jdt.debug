package org.eclipse.jdt.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
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
	public static IJavaLineBreakpoint createLineBreakpoint(IType type, int lineNumber, int charStart, int charEnd, int hitCount) throws DebugException {
		return new JavaLineBreakpoint(type, lineNumber, charStart, charEnd, hitCount);
	}
	
	public static ISnippetSupportLineBreakpoint createSnippetSupportLineBreakpoint(IType type, int lineNumber, int charStart, int charEnd, int hitCount) throws DebugException {
		return new SnippetSupportLineBreakpoint(type, lineNumber, charStart, charEnd, hitCount);
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
	public static IJavaRunToLineBreakpoint createRunToLineBreakpoint(IType type, int lineNumber, int charStart, int charEnd) throws DebugException {
		return new JavaRunToLineBreakpoint(type, lineNumber, charStart, charEnd);
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
	public static IJavaExceptionBreakpoint createExceptionBreakpoint(final IType exception, final boolean caught, final boolean uncaught, final boolean checked) throws DebugException {
		return new JavaExceptionBreakpoint(exception, caught, uncaught, checked);
	}

	/**
	 * Creates and returns a watchpoint on the
	 * given field.
	 * If hitCount > 0, the breakpoint will suspend execution when it is
	 * "hit" the specified number of times. Note: the breakpoint is not
	 * added to the breakpoint manager - it is merely created.
	 * 
	 * @param field the field on which to suspend (on access or modification)
	 * @param hitCount the number of times the breakpoint will be hit before
	 * 	suspending execution - 0 if it should always suspend
	 * @return a watchpoint
	 * @exception DebugException if unable to create the breakpoint marker due
	 * 	to a lower level exception
	 */
	public static IJavaWatchpoint createWatchpoint(final IField field, final int hitCount) throws DebugException {
		return new JavaWatchpoint(field, hitCount);
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
	public static IJavaMethodEntryBreakpoint createMethodEntryBreakpoint(final IMethod method, final int hitCount) throws DebugException {
		return new JavaMethodEntryBreakpoint(method, hitCount);
	}
	
	private static IBreakpointManager getBreakpointManager() {
		return DebugPlugin.getDefault().getBreakpointManager();
	}
}


