package org.eclipse.jdt.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;
import org.eclipse.jdt.internal.debug.core.breakpoints.JavaExceptionBreakpoint;
import org.eclipse.jdt.internal.debug.core.breakpoints.JavaLineBreakpoint;
import org.eclipse.jdt.internal.debug.core.breakpoints.JavaMethodBreakpoint;
import org.eclipse.jdt.internal.debug.core.breakpoints.JavaPatternBreakpoint;
import org.eclipse.jdt.internal.debug.core.breakpoints.JavaTargetPatternBreakpoint;
import org.eclipse.jdt.internal.debug.core.breakpoints.JavaWatchpoint;
import org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget;

import com.sun.jdi.VirtualMachine;

/**
 * The JDI debug model plug-in provides an implementation of a debug
 * model based on the standard "Java Debug Interface" (JDI). This class provides utility
 * methods for creating debug targets and breakpoints specific to the JDI debug
 * model.
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
	 * Not to be instantiated.
	 */
	private JDIDebugModel() {
		super();
	}
	
	//XXX Fix all hit count comments after Bug#1740 is addressed
	/**
	 * Creates and returns a debug target for the given VM, with
	 * the specified name, and associates the debug target with the
	 * given process for console I/O. The allow terminate flag specifies whether
	 * the debug target will support termination (<code>ITerminate</code>).
	 * The allow disconnect flag specifies whether the debug target will
	 * support disconnection (<code>IDisconnect</code>). Launching the actual
	 * VM is a client responsibility. By default, the target VM will be
	 * resumed on startup.
	 * The debug target is added to the given launch.
	 *
	 * @param lanuch the launch the new debug target will be contained in
	 * @param vm the VM to create a debug target for
	 * @param name the name to associate with the VM, which will be 
	 *   returned from <code>IDebugTarget.getName</code>. If <code>null</code>
	 *   the name will be retrieved from the underlying VM.
	 * @param process the process to associate with the debug target,
	 *   which will be returned from <code>IDebugTarget.getProcess</code>
	 * @param allowTerminate whether the target will support termianation
	 * @param allowDisconnect whether the target will support disconnection
	 * @return a debug target
	 * @see org.eclipse.debug.core.model.ITerminate
	 * @see org.eclipse.debug.core.model.IDisconnect
	 */
	public static IDebugTarget newDebugTarget(ILaunch launch, VirtualMachine vm, String name, IProcess process, boolean allowTerminate, boolean allowDisconnect) {
		return newDebugTarget(launch, vm, name, process, allowTerminate, allowDisconnect, true);
	}

	/**
	 * Creates and returns a debug target for the given VM, with
	 * the specified name, and associates the debug target with the
	 * given process for console I/O. The allow terminate flag specifies whether
	 * the debug target will support termination (<code>ITerminate</code>).
	 * The allow disconnect flag specifies whether the debug target will
	 * support disconnection (<code>IDisconnect</code>). The resume
	 * flag specifies if the target VM should be resumed on startup (has
	 * no effect if the VM was already running when the connection to the
	 * VM was esatbished). Launching the actual VM is a client responsibility.
	 * The debug target is added to the given launch.
	 *
	 * @param launch the launch the new debug target will be contained in
	 * @param vm the VM to create a debug target for
	 * @param name the name to associate with the VM, which will be 
	 *   returned from <code>IDebugTarget.getName</code>. If <code>null</code>
	 *   the name will be retrieved from the underlying VM.
	 * @param process the process to associate with the debug target,
	 *   which will be returned from <code>IDebugTarget.getProcess</code>
	 * @param allowTerminate whether the target will support termianation
	 * @param allowDisconnect whether the target will support disconnection
	 * @param resume whether the target is to be resumed on startup. Has
	 *   no effect if the target was already running when the connection
	 *   to the VM was established.
	 * @return a debug target
	 * @see org.eclipse.debug.core.model.ITerminate
	 * @see org.eclipse.debug.core.model.IDisconnect
	 * @since 2.0
	 */
	public static IDebugTarget newDebugTarget(final ILaunch launch, final VirtualMachine vm, final String name, final IProcess process, final boolean allowTerminate, final boolean allowDisconnect, final boolean resume) {
		final IJavaDebugTarget[] target = new IJavaDebugTarget[1];
		IWorkspaceRunnable r = new IWorkspaceRunnable() {
			public void run(IProgressMonitor m) {
				target[0]= new JDIDebugTarget(launch, vm, name, allowTerminate, allowDisconnect, process, resume);
			}
		};
		try {
			ResourcesPlugin.getWorkspace().run(r, null);
		} catch (CoreException e) {
			JDIDebugPlugin.log(e);
		}
		return target[0];
	}
	
	/**
	 * Returns the identifier for the JDI debug model plugin
	 *
	 * @return plugin identifier
	 */
	public static String getPluginIdentifier() {
		return JDIDebugPlugin.getDefault().getDescriptor().getUniqueIdentifier();
	}
	
	/**
	 * Adds the given hot code replace listener to the JDI debug model.
	 * Added listeners will receive hot code replace notifications.
	 */
	public static void addHotCodeReplaceListener(IJavaHotCodeReplaceListener listener) {
		JDIDebugPlugin.getDefault().addHotCodeReplaceListener(listener);
	}
		
	/**
	 * Removes the given hot code replace listener to the JDI debug model.
	 * Removed listeners will not receive hot code replace notifications.
	 */
	public static void removeHotCodeReplaceListener(IJavaHotCodeReplaceListener listener) {
		JDIDebugPlugin.getDefault().removeHotCodeReplaceListener(listener);
	}
	
	/**
	 * Adds the given breakpoint listener to the JDI debug model.
	 * 
	 * @param listener breakpoint listener
	 */
	public static void addJavaBreakpointListener(IJavaBreakpointListener listener) {
		JDIDebugPlugin.getDefault().addJavaBreakpointListener(listener);
	}	

	/**
	 * Removes the given breakpoint listener from the JDI debug model.
	 * 
	 * @param listener breakpoint listener
	 */
	public static void removeJavaBreakpointListener(IJavaBreakpointListener listener) {
		JDIDebugPlugin.getDefault().removeJavaBreakpointListener(listener);
	}	
	
	
	/**
	 * Creates and returns a line breakpoint in the type with the
	 * given name, at the given line number. The marker associated with the
	 * breakpoint will be created on the specified resource. If a character
	 * range within the line is known, it may be specified by charStart/charEnd.
	 * If hitCount is > 0, the breakpoint will suspend execution when it is
	 * "hit" the specified number of times.
	 * 
	 * @param resource the resource on which to create the associated breakpoint
	 *  marker
	 * @param typeName the fully qualified name of the type the breakpoint is
	 *  to be installed in. If the breakpoint is to be installed in an inner type,
	 *  it is sufficient to provide the name of the top level enclosing type.
	 * 	If an inner class name is specified, it should be formatted as the 
	 *  associated class file name (i.e. with <code>$</code>). For example,
	 * 	<code>example.SomeClass$InnerType</code>, could be specified, but
	 * 	<code>example.SomeClass</code> is sufficient.
	 * @param lineNumber the lineNumber on which the breakpoint is created - line
	 *   numbers are 1 based, associated with the compilation unit in which
	 *   the type is defined
	 * @param charStart the first character index associated with the breakpoint,
	 *   or -1 if unspecified
 	 * @param charEnd the last character index associated with the breakpoint,
	 *   or -1 if unspecified
	 * @param hitCount the number of times the breakpoint will be hit before
	 *   suspending execution - 0 if it should always suspend
	 * @param register whether to add this breakpoint to the breakpoint manager
	 * @param attributes a map of client defined attributes that should be assigned
 	 *  to the underlying breakpoint marker on creation, or <code>null</code> if none.
	 * @return a line breakpoint
	 * @exception CoreException If this method fails. Reasons include:<ul> 
	 *<li>Failure creating underlying marker.  The exception's status contains
	 * the underlying exception responsible for the failure.</li></ul>
	 * @since 2.0
	 */
	public static IJavaLineBreakpoint createLineBreakpoint(IResource resource, String typeName, int lineNumber, int charStart, int charEnd, int hitCount, boolean register, Map attributes) throws CoreException {
		if (attributes == null) {
			attributes = new HashMap(10);
		}		
		return new JavaLineBreakpoint(resource, typeName, lineNumber, charStart, charEnd, hitCount, register, attributes);
	}
	
	/**
	 * Creates and returns a pattern breakpoint for the given resource at the
	 * given line number, which is installed in all classes whose fully 
	 * qualified name matches the given pattern.
	 * If hitCount > 0, the breakpoint will suspend execution when it is
	 * "hit" the specified number of times. 
	 * @param resource the original source file
	 * @param sourceName the name of the source file in which the breakpoint is
	 *  set, or <code>null</code>. When specified, the pattern breakpoint will
	 *  install itself in classes that have a source file name debug attribute
	 *  that matches this value, and satisfies the class name pattern.
	 * @param pattern the class name pattern in which the pattern breakpoint should
	 *   be installed. The pattern breakpoint will install itself in every class which
	 *   matches the pattern.
	 * @param lineNumber the line number on which this breakpoint should be placed.
	 *   Note that the line number refers to the debug attributes in the generated
	 * 	 class file. Generally, this refers to a line number in the original
	 *   source, but the attribute is client defined.
	 * @param charStart the first character index associated with the breakpoint,
	 *   or -1 if unspecified
 	 * @param charEnd the last character index associated with the breakpoint,
	 *   or -1 if unspecified
	 * @param hitCount the number of times the breakpoint will be hit before
	 *   suspending execution - 0 if it should always suspend
	 * @param register whether to add this breakpoint to the breakpoint manager
	 * @param attributes a map of client defined attributes that should be assigned
 	 *  to the underlying breakpoint marker on creation, or <code>null</code> if none.
	 * @return a pattern breakpoint
	 * @exception CoreException If this method fails. Reasons include:<ul> 
	 *<li>Failure creating underlying marker.  The exception's status contains
	 * the underlying exception responsible for the failure.</li></ul>
	 */
	public static IJavaPatternBreakpoint createPatternBreakpoint(IResource resource, String sourceName, String pattern, int lineNumber, int charStart, int charEnd, int hitCount, boolean register, Map attributes) throws CoreException {
		if (attributes == null) {
			attributes = new HashMap(10);
		}		
		return new JavaPatternBreakpoint(resource, sourceName, pattern, lineNumber, charStart, charEnd, hitCount, register, attributes);
	}	
	
	/**
	 * Creates and returns a target pattern breakpoint for the given resource at the
	 * given line number. Clients must set the class name pattern per target for
	 * this type of breakpoint.
	 * If hitCount > 0, the breakpoint will suspend execution when it is
	 * "hit" the specified number of times. 
	 * @param resource the original source file
	 * @param sourceName the name of the source file in which the breakpoint is
	 *  set, or <code>null</code>. When specified, the pattern breakpoint will
	 *  install itself in classes that have a source file name debug attribute
	 *  that matches this value, and satisfies the class name pattern.
	 * @param lineNumber the line number on which this breakpoint should be placed.
	 *   Note that the line number refers to the debug attributes in the generated
	 * 	 class file. Generally, this refers to a line number in the original
	 *   source, but the attribute is client defined.
	 * @param charStart the first character index associated with the breakpoint,
	 *   or -1 if unspecified
 	 * @param charEnd the last character index associated with the breakpoint,
	 *   or -1 if unspecified
	 * @param hitCount the number of times the breakpoint will be hit before
	 *   suspending execution - 0 if it should always suspend
	 * @param register whether to add this breakpoint to the breakpoint manager
	 * @param attributes a map of client defined attributes that should be assigned
 	 *  to the underlying breakpoint marker on creation, or <code>null</code> if none.
	 * @return a target pattern breakpoint
	 * @exception CoreException If this method fails. Reasons include:<ul> 
	 *<li>Failure creating underlying marker.  The exception's status contains
	 * the underlying exception responsible for the failure.</li></ul>
	 */
	public static IJavaTargetPatternBreakpoint createTargetPatternBreakpoint(IResource resource, String sourceName, int lineNumber, int charStart, int charEnd, int hitCount, boolean register, Map attributes) throws CoreException {
		if (attributes == null) {
			attributes = new HashMap(10);
		}		
		return new JavaTargetPatternBreakpoint(resource, sourceName, lineNumber, charStart, charEnd, hitCount, register, attributes);
	}	
		
	/**
	 * Creates and returns an exception breakpoint in a type with the given name.
	 * The marker associated with the breakpoint will be created on the specified resource.
	 * Caught and uncaught specify where the exception
	 * should cause thread suspensions - that is, in caught and/or uncaught locations.
	 * Checked indicates if the given exception is a checked exception.
	 * 
	 * @param resource the resource on which to create the associated
	 *  breakpoint marker
	 * @param exceptionName the fully qualified name of the exception for
	 *  which to create the breakpoint
	 * @param caught whether to suspend in caught locations
	 * @param uncaught whether to suspend in uncaught locations
 	 * @param checked whether the exception is a checked exception
 	 * @param register whether to add this breakpoint to the breakpoint manager
 	 * @param attributes a map of client defined attributes that should be assigned
 	 *  to the underlying breakpoint marker on creation or <code>null</code> if none.
	 * @return an exception breakpoint
	 * @exception CoreException If this method fails. Reasons include:<ul> 
	 *<li>Failure creating underlying marker.  The exception's status contains
	 * the underlying exception responsible for the failure.</li></ul>
	 * @since 2.0
	 */
	public static IJavaExceptionBreakpoint createExceptionBreakpoint(IResource resource, String exceptionName, boolean caught, boolean uncaught, boolean checked, boolean register, Map attributes) throws CoreException {
		if (attributes == null) {
			attributes = new HashMap(10);
		}
		return new JavaExceptionBreakpoint(resource, exceptionName, caught, uncaught, checked, register, attributes);
	}

	/**
	 * Creates and returns a watchpoint on a field with the given name
	 * in a type with the given name.
	 * The marker associated with the breakpoint will be created on the specified resource.
	 * If hitCount > 0, the breakpoint will suspend execution when it is
	 * "hit" the specified number of times.
	 * 
	 * @param resource the resource on which to create the associated breakpoint
	 *  marker
	 * @param typeName the fully qualified name of the type the breakpoint is
	 *  to be installed in. If the breakpoint is to be installed in an inner type,
	 *  it is sufficient to provide the name of the top level enclosing type.
	 * 	If an inner class name is specified, it should be formatted as the 
	 *  associated class file name (i.e. with <code>$</code>). For example,
	 * 	<code>example.SomeClass$InnerType</code>, could be specified, but
	 * 	<code>example.SomeClass</code> is sufficient.
	 * @param fieldName the name of the field on which to suspend (on access or modification)
	 * @param lineNumber the lineNumber with which the breakpoint is asscoiated,
	 *   or -1 is unspecfied. Line numbers are 1 based, associated with the compilation
	 *   unit in which the type is defined
	 * @param charStart the first character index associated with the breakpoint,
	 *   or -1 if unspecified
 	 * @param charEnd the last character index associated with the breakpoint,
	 *   or -1 if unspecified
	 * @param hitCount the number of times the breakpoint will be hit before
	 * 	suspending execution - 0 if it should always suspend
	 * @param register whether to add this breakpoint to the breakpoint manager
	 * @param attributes a map of client defined attributes that should be assigned
 	 *  to the underlying breakpoint marker on creation, or <code>null</code> if none.
	 * @return a watchpoint
	 * @exception CoreException If this method fails. Reasons include:<ul> 
	 *<li>Failure creating underlying marker.  The CoreException's status contains
	 * the underlying exception responsible for the failure.</li></ul>
	 * @since 2.0
	 */
	public static IJavaWatchpoint createWatchpoint(IResource resource, String typeName, String fieldName, int lineNumber, int charStart, int charEnd, int hitCount, boolean register, Map attributes) throws CoreException {
		if (attributes == null) {
			attributes = new HashMap(10);
		}		
		return new JavaWatchpoint(resource, typeName, fieldName, lineNumber, charStart, charEnd, hitCount, register, attributes);
	}
	
	/**
	 * Creates and returns a method breakpoint with the specified
	 * criteria.
	 *
	 * @param resource the resource on which to create the associated
	 *  breakpoint marker
	 * @param typePattern the pattern specifying the fully qualified name of type(s)
	 *  this breakpoint suspends execution in. Patterns are limited to exact
	 *  matches and patterns that begin or end with '*'.
	 * @param methodName the name of the method(s) this breakpoint suspends
	 *  execution in, or <code>null</code> if this breakpoint does
	 *  not suspend execution based on method name
	 * @param methodSignature the signature of the method(s) this breakpoint suspends
	 *  execution in, or <code>null</code> if this breakpoint does not
	 *  suspend exectution based on method signature
	 * @param entry whether this breakpoint causes execution to suspend
	 *  on entry of methods
	 * @param exit whether this breakpoint causes execution to suspend
	 *  on exit of methods
	 * @param nativeOnly whether this breakpoint causes execution to suspend
	 *  on entry/exit of native methods only
	 * @param lineNumber the lineNumber on which the breakpoint is created - line
	 *   numbers are 1 based, associated with the compilation unit in which
	 *   the type is defined
	 * @param charStart the first character index associated with the breakpoint,
	 *   or -1 if unspecified
 	 * @param charEnd the last character index associated with the breakpoint,
	 *   or -1 if unspecified
	 * @param hitCount the number of times the breakpoint will be hit before
	 *   suspending execution - 0 if it should always suspend
	 * @param register whether to add this breakpoint to the breakpoint manager
	 * @param attributes a map of client defined attributes that should be assigned
 	 *  to the underlying breakpoint marker on creation, or <code>null</code> if none.
	 * @return a method breakpoint
	 * @exception CoreException If this method fails. Reasons include:<ul> 
	 *<li>Failure creating underlying marker.  The exception's status contains
	 * the underlying exception responsible for the failure.</li></ul>
	 * @since 2.0
	 */
	public static IJavaMethodBreakpoint createMethodBreakpoint(IResource resource, String typePattern, String methodName, String methodSignature, boolean entry, boolean exit, boolean nativeOnly, int lineNumber, int charStart, int charEnd, int hitCount, boolean register, Map attributes) throws CoreException {
		if (attributes == null) {
			attributes = new HashMap(10);
		}
		return new JavaMethodBreakpoint(resource, typePattern, methodName, methodSignature, entry, exit, nativeOnly, lineNumber, charStart, charEnd, hitCount, register, attributes);
	}
		
	/**
	 * Returns whether a line breakpoint already exists on the given line number in a
	 * type with the specified fully qualified name.
	 * 
	 * @param typeName the fully qualified name of the type in which to check for a line breakpoint
	 * @param lineNumber the line number on which to check for a line breakpoint
	 * @return whether a line breakpoint already exists on the given line number in
	 *   the given type.
	 * @exception CoreException if unable to retrieve the associated marker
	 * 	attributes (line number).
	 */
	public static boolean lineBreakpointExists(String typeName, int lineNumber) throws CoreException {
		String modelId= getPluginIdentifier();
		String markerType= JavaLineBreakpoint.getMarkerType();
		IBreakpointManager manager= DebugPlugin.getDefault().getBreakpointManager();
		IBreakpoint[] breakpoints= manager.getBreakpoints(modelId);
		for (int i = 0; i < breakpoints.length; i++) {
			if (!(breakpoints[i] instanceof IJavaLineBreakpoint)) {
				continue;
			}
			IJavaLineBreakpoint breakpoint = (IJavaLineBreakpoint) breakpoints[i];
			if (breakpoint.getMarker().getType().equals(markerType)) {
				if (breakpoint.getTypeName().equals(typeName)) {
					if (breakpoint.getLineNumber() == lineNumber) {
						return true;
					}
				}
			}
		}
		return false;
	}	
}
