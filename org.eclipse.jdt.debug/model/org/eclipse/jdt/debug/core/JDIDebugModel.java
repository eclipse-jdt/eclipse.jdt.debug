/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.core;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;
import org.eclipse.jdt.internal.debug.core.breakpoints.JavaClassPrepareBreakpoint;
import org.eclipse.jdt.internal.debug.core.breakpoints.JavaExceptionBreakpoint;
import org.eclipse.jdt.internal.debug.core.breakpoints.JavaLineBreakpoint;
import org.eclipse.jdt.internal.debug.core.breakpoints.JavaMethodBreakpoint;
import org.eclipse.jdt.internal.debug.core.breakpoints.JavaMethodEntryBreakpoint;
import org.eclipse.jdt.internal.debug.core.breakpoints.JavaPatternBreakpoint;
import org.eclipse.jdt.internal.debug.core.breakpoints.JavaStratumLineBreakpoint;
import org.eclipse.jdt.internal.debug.core.breakpoints.JavaTargetPatternBreakpoint;
import org.eclipse.jdt.internal.debug.core.breakpoints.JavaWatchpoint;
import org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget;
import org.osgi.service.prefs.BackingStoreException;

import com.sun.jdi.VirtualMachine;

/**
 * Provides utility methods for creating debug targets and breakpoints specific
 * to the JDI debug model.
 * <p>
 * To provide access to behavior and information specific to the JDI debug
 * model, a set of interfaces are defined which extend the base set of debug
 * element interfaces. For example, <code>IJavaStackFrame</code> is declared to
 * extend <code>IStackFrame</code>, and provides methods specific to this debug
 * model. The specialized interfaces are also available as adapters from the
 * debug elements generated from this model.
 * </p>
 * <p>
 * This class provides static utility methods only.
 * </p>
 * 
 * @noinstantiate This class is not intended to be instantiated by clients.
 * @noextend This class is not intended to be subclassed by clients.
 */
public class JDIDebugModel {

	/**
	 * Preference key for default JDI request timeout value.
	 */
	public static final String PREF_REQUEST_TIMEOUT = getPluginIdentifier()
			+ ".PREF_REQUEST_TIMEOUT"; //$NON-NLS-1$

	/**
	 * Preference key for specifying if hot code replace should be performed
	 * when a replacement class file contains compilation errors.
	 */
	public static final String PREF_HCR_WITH_COMPILATION_ERRORS = getPluginIdentifier()
			+ ".PREF_HCR_WITH_COMPILATION_ERRORS"; //$NON-NLS-1$

	/**
	 * The default JDI request timeout when no preference is set.
	 */
	public static final int DEF_REQUEST_TIMEOUT = 3000;

	/**
	 * Boolean preference controlling whether breakpoints are hit during an
	 * evaluation operation. If true, breakpoints will be hit as usual during
	 * evaluations. If false, the breakpoint manager will be automatically
	 * disabled during evaluations.
	 * 
	 * @since 3.0
	 */
	public static final String PREF_SUSPEND_FOR_BREAKPOINTS_DURING_EVALUATION = getPluginIdentifier()
			+ ".suspend_for_breakpoints_during_evaluation"; //$NON-NLS-1$

	/**
	 * Not to be instantiated.
	 */
	private JDIDebugModel() {
		super();
	}

	/**
	 * Creates and returns a debug target for the given VM, with the specified
	 * name, and associates the debug target with the given process for console
	 * I/O. The allow terminate flag specifies whether the debug target will
	 * support termination (<code>ITerminate</code>). The allow disconnect flag
	 * specifies whether the debug target will support disconnection (
	 * <code>IDisconnect</code>). Launching the actual VM is a client
	 * responsibility. By default, the target VM will be resumed on startup. The
	 * debug target is added to the given launch.
	 * 
	 * @param launch
	 *            the launch the new debug target will be contained in
	 * @param vm
	 *            the VM to create a debug target for
	 * @param name
	 *            the name to associate with the VM, which will be returned from
	 *            <code>IDebugTarget.getName</code>. If <code>null</code> the
	 *            name will be retrieved from the underlying VM.
	 * @param process
	 *            the process to associate with the debug target, which will be
	 *            returned from <code>IDebugTarget.getProcess</code>
	 * @param allowTerminate
	 *            whether the target will support termination
	 * @param allowDisconnect
	 *            whether the target will support disconnection
	 * @return a debug target
	 * @see org.eclipse.debug.core.model.ITerminate
	 * @see org.eclipse.debug.core.model.IDisconnect
	 * @since 2.0
	 */
	public static IDebugTarget newDebugTarget(ILaunch launch,
			VirtualMachine vm, String name, IProcess process,
			boolean allowTerminate, boolean allowDisconnect) {
		return newDebugTarget(launch, vm, name, process, allowTerminate,
				allowDisconnect, true);
	}

	/**
	 * Creates and returns a debug target for the given VM, with the specified
	 * name, and associates the debug target with the given process for console
	 * I/O. The allow terminate flag specifies whether the debug target will
	 * support termination (<code>ITerminate</code>). The allow disconnect flag
	 * specifies whether the debug target will support disconnection (
	 * <code>IDisconnect</code>). The resume flag specifies if the target VM
	 * should be resumed on startup (has no effect if the VM was already running
	 * when the connection to the VM was established). Launching the actual VM
	 * is a client responsibility. The debug target is added to the given
	 * launch.
	 * 
	 * @param launch
	 *            the launch the new debug target will be contained in
	 * @param vm
	 *            the VM to create a debug target for
	 * @param name
	 *            the name to associate with the VM, which will be returned from
	 *            <code>IDebugTarget.getName</code>. If <code>null</code> the
	 *            name will be retrieved from the underlying VM.
	 * @param process
	 *            the process to associate with the debug target, which will be
	 *            returned from <code>IDebugTarget.getProcess</code>
	 * @param allowTerminate
	 *            whether the target will support termination
	 * @param allowDisconnect
	 *            whether the target will support disconnection
	 * @param resume
	 *            whether the target is to be resumed on startup. Has no effect
	 *            if the target was already running when the connection to the
	 *            VM was established.
	 * @return a debug target
	 * @see org.eclipse.debug.core.model.ITerminate
	 * @see org.eclipse.debug.core.model.IDisconnect
	 * @since 2.0
	 */
	public static IDebugTarget newDebugTarget(final ILaunch launch,
			final VirtualMachine vm, final String name, final IProcess process,
			final boolean allowTerminate, final boolean allowDisconnect,
			final boolean resume) {
		final IJavaDebugTarget[] target = new IJavaDebugTarget[1];
		IWorkspaceRunnable r = new IWorkspaceRunnable() {
			public void run(IProgressMonitor m) {
				target[0] = new JDIDebugTarget(launch, vm, name,
						allowTerminate, allowDisconnect, process, resume);
			}
		};
		try {
			ResourcesPlugin.getWorkspace().run(r, null, 0, null);
		} catch (CoreException e) {
			JDIDebugPlugin.log(e);
		}
		return target[0];
	}

	/**
	 * Returns the identifier for the JDI debug model plug-in
	 * 
	 * @return plug-in identifier
	 */
	public static String getPluginIdentifier() {
		return JDIDebugPlugin.getUniqueIdentifier();
	}

	/**
	 * Registers the given listener for hot code replace notifications. Has no
	 * effect if an identical listener is already registered.
	 * <p>
	 * Note that since 3.6, if an {@link IJavaDebugTarget} has hot code replace
	 * listeners registered with it directly, listeners registered with this
	 * class are not notified of hot code replace events for that target. This
	 * allows a target to have custom hot code replace notification behavior.
	 * </p>
	 * 
	 * @param listener
	 *            hot code replace listener
	 * @see IJavaHotCodeReplaceListener
	 * @see IJavaDebugTarget#addHotCodeReplaceListener(IJavaHotCodeReplaceListener)
	 * @since 2.0
	 */
	public static void addHotCodeReplaceListener(
			IJavaHotCodeReplaceListener listener) {
		JDIDebugPlugin.getDefault().addHotCodeReplaceListener(listener);
	}

	/**
	 * Unregisters the given listener for hot code replace notifications. Has no
	 * effect if an identical listener is not already registered.
	 * 
	 * @param listener
	 *            hot code replace listener
	 * @see IJavaHotCodeReplaceListener
	 * @since 2.0
	 */
	public static void removeHotCodeReplaceListener(
			IJavaHotCodeReplaceListener listener) {
		JDIDebugPlugin.getDefault().removeHotCodeReplaceListener(listener);
	}

	/**
	 * Registers the given listener for breakpoint notifications. Has no effect
	 * if an identical listener is already registered.
	 * 
	 * @param listener
	 *            breakpoint listener
	 * @see IJavaBreakpointListener
	 * @since 2.0
	 */
	public static void addJavaBreakpointListener(
			IJavaBreakpointListener listener) {
		JDIDebugPlugin.getDefault().addJavaBreakpointListener(listener);
	}

	/**
	 * Unregisters the given listener for breakpoint notifications. Has no
	 * effect if an identical listener is not already registered.
	 * 
	 * @param listener
	 *            breakpoint listener
	 * @see IJavaBreakpointListener
	 * @since 2.0
	 */
	public static void removeJavaBreakpointListener(
			IJavaBreakpointListener listener) {
		JDIDebugPlugin.getDefault().removeJavaBreakpointListener(listener);
	}

	/**
	 * Creates and returns a line breakpoint in the type with the given name, at
	 * the given line number. The marker associated with the breakpoint will be
	 * created on the specified resource. If a character range within the line
	 * is known, it may be specified by charStart/charEnd. If hitCount is > 0,
	 * the breakpoint will suspend execution when it is "hit" the specified
	 * number of times.
	 * 
	 * @param resource
	 *            the resource on which to create the associated breakpoint
	 *            marker
	 * @param typeName
	 *            the fully qualified name of the type the breakpoint is to be
	 *            installed in. If the breakpoint is to be installed in an inner
	 *            type, it is sufficient to provide the name of the top level
	 *            enclosing type. If an inner class name is specified, it should
	 *            be formatted as the associated class file name (i.e. with
	 *            <code>$</code>). For example,
	 *            <code>example.SomeClass$InnerType</code>, could be specified,
	 *            but <code>example.SomeClass</code> is sufficient.
	 * @param lineNumber
	 *            the lineNumber on which the breakpoint is set - line numbers
	 *            are 1 based, associated with the source file in which the
	 *            breakpoint is set
	 * @param charStart
	 *            the first character index associated with the breakpoint, or
	 *            -1 if unspecified, in the source file in which the breakpoint
	 *            is set
	 * @param charEnd
	 *            the last character index associated with the breakpoint, or -1
	 *            if unspecified, in the source file in which the breakpoint is
	 *            set
	 * @param hitCount
	 *            the number of times the breakpoint will be hit before
	 *            suspending execution - 0 if it should always suspend
	 * @param register
	 *            whether to add this breakpoint to the breakpoint manager
	 * @param attributes
	 *            a map of client defined attributes that should be assigned to
	 *            the underlying breakpoint marker on creation, or
	 *            <code>null</code> if none.
	 * @return a line breakpoint
	 * @exception CoreException
	 *                If this method fails. Reasons include:
	 *                <ul>
	 *                <li>Failure creating underlying marker. The exception's
	 *                status contains the underlying exception responsible for
	 *                the failure.</li>
	 *                </ul>
	 * @since 2.0
	 */
	public static IJavaLineBreakpoint createLineBreakpoint(IResource resource,
			String typeName, int lineNumber, int charStart, int charEnd,
			int hitCount, boolean register, Map<String, Object> attributes)
			throws CoreException {
		if (attributes == null) {
			attributes = new HashMap<String, Object>(10);
		}
		return new JavaLineBreakpoint(resource, typeName, lineNumber,
				charStart, charEnd, hitCount, register, attributes);
	}

	/**
	 * Creates and returns a pattern breakpoint for the given resource at the
	 * given line number, which is installed in all classes whose fully
	 * qualified name matches the given pattern. If hitCount > 0, the breakpoint
	 * will suspend execution when it is "hit" the specified number of times.
	 * 
	 * @param resource
	 *            the resource on which to create the associated breakpoint
	 *            marker
	 * @param sourceName
	 *            the name of the source file in which the breakpoint is set, or
	 *            <code>null</code>. When specified, the pattern breakpoint will
	 *            install itself in classes that have a source file name debug
	 *            attribute that matches this value, and satisfies the class
	 *            name pattern.
	 * @param pattern
	 *            the class name pattern in which the pattern breakpoint should
	 *            be installed. The pattern breakpoint will install itself in
	 *            every class which matches the pattern.
	 * @param lineNumber
	 *            the lineNumber on which the breakpoint is set - line numbers
	 *            are 1 based, associated with the source file in which the
	 *            breakpoint is set
	 * @param charStart
	 *            the first character index associated with the breakpoint, or
	 *            -1 if unspecified, in the source file in which the breakpoint
	 *            is set
	 * @param charEnd
	 *            the last character index associated with the breakpoint, or -1
	 *            if unspecified, in the source file in which the breakpoint is
	 *            set
	 * @param hitCount
	 *            the number of times the breakpoint will be hit before
	 *            suspending execution - 0 if it should always suspend
	 * @param register
	 *            whether to add this breakpoint to the breakpoint manager
	 * @param attributes
	 *            a map of client defined attributes that should be assigned to
	 *            the underlying breakpoint marker on creation, or
	 *            <code>null</code> if none.
	 * @return a pattern breakpoint
	 * @exception CoreException
	 *                If this method fails. Reasons include:
	 *                <ul>
	 *                <li>Failure creating underlying marker. The exception's
	 *                status contains the underlying exception responsible for
	 *                the failure.</li>
	 *                </ul>
	 * @deprecated use <code>createStratumBreakpoint</code> instead
	 */
	@Deprecated
	public static IJavaPatternBreakpoint createPatternBreakpoint(
			IResource resource, String sourceName, String pattern,
			int lineNumber, int charStart, int charEnd, int hitCount,
			boolean register, Map<String, Object> attributes) throws CoreException {
		if (attributes == null) {
			attributes = new HashMap<String, Object>(10);
		}
		return new JavaPatternBreakpoint(resource, sourceName, pattern,
				lineNumber, charStart, charEnd, hitCount, register, attributes);
	}

	/**
	 * Creates and returns a line breakpoint identified by its source file name
	 * and/or path, and stratum that it is relative to.
	 * 
	 * @param resource
	 *            the resource on which to create the associated breakpoint
	 *            marker
	 * @param stratum
	 *            the stratum in which the source name, source path and line
	 *            number are relative, or <code>null</code>. If
	 *            <code>null</code> or if the specified stratum is not defined
	 *            for a type, the source name, source path and line number are
	 *            relative to the type's default stratum.
	 * @param sourceName
	 *            the simple name of the source file in which the breakpoint is
	 *            set, or <code>null</code>. The breakpoint will install itself
	 *            in classes that have a source file name debug attribute that
	 *            matches this value in the specified stratum, and satisfies the
	 *            class name pattern and source path attribute. When
	 *            <code>null</code>, the source file name debug attribute is not
	 *            considered.
	 * @param sourcePath
	 *            the qualified source file name in which the breakpoint is set,
	 *            or <code>null</code>. The breakpoint will install itself in
	 *            classes that have a source file path in the specified stratum
	 *            that matches this value, and satisfies the class name pattern
	 *            and source name attribute. When <code>null</code>, the source
	 *            path attribute is not considered.
	 * @param classNamePattern
	 *            the class name pattern to which the breakpoint should be
	 *            restricted, or <code>null</code>. The breakpoint will install
	 *            itself in each type that matches this class name pattern, with
	 *            a satisfying source name and source path. Patterns may begin
	 *            or end with '*', which matches 0 or more characters. A pattern
	 *            that does not contain a '*' is equivalent to a pattern ending
	 *            in '*'. Specifying <code>null</code>, or an empty string is
	 *            the equivalent to "*". Multiple patterns can be specified by
	 *            delimiting the patterns with a comma - e.g. "x.y.z,a.b.c".
	 *            When multiple patterns are specified, The breakpoint will
	 *            install itself in each of the types that match any of the
	 *            specified class pattern, with a satisfying source name and
	 *            source path.
	 * @param lineNumber
	 *            the lineNumber on which the breakpoint is set - line numbers
	 *            are 1 based, associated with the source file (stratum) in
	 *            which the breakpoint is set
	 * @param charStart
	 *            the first character index associated with the breakpoint, or
	 *            -1 if unspecified, in the source file in which the breakpoint
	 *            is set
	 * @param charEnd
	 *            the last character index associated with the breakpoint, or -1
	 *            if unspecified, in the source file in which the breakpoint is
	 *            set
	 * @param hitCount
	 *            the number of times the breakpoint will be hit before
	 *            suspending execution - 0 if it should always suspend
	 * @param register
	 *            whether to add this breakpoint to the breakpoint manager
	 * @param attributes
	 *            a map of client defined attributes that should be assigned to
	 *            the underlying breakpoint marker on creation, or
	 *            <code>null</code> if none.
	 * @return a stratum breakpoint
	 * @exception CoreException
	 *                If this method fails. Reasons include:
	 *                <ul>
	 *                <li>Failure creating underlying marker. The exception's
	 *                status contains the underlying exception responsible for
	 *                the failure.</li>
	 *                </ul>
	 * @since 3.0
	 */
	public static IJavaStratumLineBreakpoint createStratumBreakpoint(
			IResource resource, String stratum, String sourceName,
			String sourcePath, String classNamePattern, int lineNumber,
			int charStart, int charEnd, int hitCount, boolean register,
			Map<String, Object> attributes) throws CoreException {
		if (attributes == null) {
			attributes = new HashMap<String, Object>(10);
		}
		return new JavaStratumLineBreakpoint(resource, stratum, sourceName,
				sourcePath, classNamePattern, lineNumber, charStart, charEnd,
				hitCount, register, attributes);
	}

	/**
	 * Creates and returns a target pattern breakpoint for the given resource at
	 * the given line number. Clients must set the class name pattern per target
	 * for this type of breakpoint. If hitCount > 0, the breakpoint will suspend
	 * execution when it is "hit" the specified number of times.
	 * 
	 * @param resource
	 *            the resource on which to create the associated breakpoint
	 *            marker
	 * @param sourceName
	 *            the name of the source file in which the breakpoint is set, or
	 *            <code>null</code>. When specified, the pattern breakpoint will
	 *            install itself in classes that have a source file name debug
	 *            attribute that matches this value, and satisfies the class
	 *            name pattern.
	 * @param lineNumber
	 *            the lineNumber on which the breakpoint is set - line numbers
	 *            are 1 based, associated with the source file in which the
	 *            breakpoint is set
	 * @param charStart
	 *            the first character index associated with the breakpoint, or
	 *            -1 if unspecified, in the source file in which the breakpoint
	 *            is set
	 * @param charEnd
	 *            the last character index associated with the breakpoint, or -1
	 *            if unspecified, in the source file in which the breakpoint is
	 *            set
	 * @param hitCount
	 *            the number of times the breakpoint will be hit before
	 *            suspending execution - 0 if it should always suspend
	 * @param register
	 *            whether to add this breakpoint to the breakpoint manager
	 * @param attributes
	 *            a map of client defined attributes that should be assigned to
	 *            the underlying breakpoint marker on creation, or
	 *            <code>null</code> if none.
	 * @return a target pattern breakpoint
	 * @exception CoreException
	 *                If this method fails. Reasons include:
	 *                <ul>
	 *                <li>Failure creating underlying marker. The exception's
	 *                status contains the underlying exception responsible for
	 *                the failure.</li>
	 *                </ul>
	 */
	public static IJavaTargetPatternBreakpoint createTargetPatternBreakpoint(
			IResource resource, String sourceName, int lineNumber,
			int charStart, int charEnd, int hitCount, boolean register,
			Map<String, Object> attributes) throws CoreException {
		if (attributes == null) {
			attributes = new HashMap<String, Object>(10);
		}
		return new JavaTargetPatternBreakpoint(resource, sourceName,
				lineNumber, charStart, charEnd, hitCount, register, attributes);
	}

	/**
	 * Creates and returns an exception breakpoint for an exception with the
	 * given name. The marker associated with the breakpoint will be created on
	 * the specified resource. Caught and uncaught specify where the exception
	 * should cause thread suspensions - that is, in caught and/or uncaught
	 * locations. Checked indicates if the given exception is a checked
	 * exception.
	 * 
	 * @param resource
	 *            the resource on which to create the associated breakpoint
	 *            marker
	 * @param exceptionName
	 *            the fully qualified name of the exception for which to create
	 *            the breakpoint
	 * @param caught
	 *            whether to suspend in caught locations
	 * @param uncaught
	 *            whether to suspend in uncaught locations
	 * @param checked
	 *            whether the exception is a checked exception (i.e. compiler
	 *            detected)
	 * @param register
	 *            whether to add this breakpoint to the breakpoint manager
	 * @param attributes
	 *            a map of client defined attributes that should be assigned to
	 *            the underlying breakpoint marker on creation or
	 *            <code>null</code> if none.
	 * @return an exception breakpoint
	 * @exception CoreException
	 *                If this method fails. Reasons include:
	 *                <ul>
	 *                <li>Failure creating underlying marker. The exception's
	 *                status contains the underlying exception responsible for
	 *                the failure.</li>
	 *                </ul>
	 * @since 2.0
	 */
	public static IJavaExceptionBreakpoint createExceptionBreakpoint(
			IResource resource, String exceptionName, boolean caught,
			boolean uncaught, boolean checked, boolean register, Map<String, Object> attributes)
			throws CoreException {
		if (attributes == null) {
			attributes = new HashMap<String, Object>(10);
		}
		return new JavaExceptionBreakpoint(resource, exceptionName, caught,
				uncaught, checked, register, attributes);
	}

	/**
	 * Creates and returns a watchpoint on a field with the given name in a type
	 * with the given name. The marker associated with the breakpoint will be
	 * created on the specified resource. If hitCount > 0, the breakpoint will
	 * suspend execution when it is "hit" the specified number of times.
	 * 
	 * @param resource
	 *            the resource on which to create the associated breakpoint
	 *            marker
	 * @param typeName
	 *            the fully qualified name of the type the breakpoint is to be
	 *            installed in. If the breakpoint is to be installed in an inner
	 *            type, it is sufficient to provide the name of the top level
	 *            enclosing type. If an inner class name is specified, it should
	 *            be formatted as the associated class file name (i.e. with
	 *            <code>$</code>). For example,
	 *            <code>example.SomeClass$InnerType</code>, could be specified,
	 *            but <code>example.SomeClass</code> is sufficient.
	 * @param fieldName
	 *            the name of the field on which to suspend (on access or
	 *            modification)
	 * @param lineNumber
	 *            the lineNumber on which the breakpoint is set - line numbers
	 *            are 1 based, associated with the source file in which the
	 *            breakpoint is set
	 * @param charStart
	 *            the first character index associated with the breakpoint, or
	 *            -1 if unspecified, in the source file in which the breakpoint
	 *            is set
	 * @param charEnd
	 *            the last character index associated with the breakpoint, or -1
	 *            if unspecified, in the source file in which the breakpoint is
	 *            set
	 * @param hitCount
	 *            the number of times the breakpoint will be hit before
	 *            suspending execution - 0 if it should always suspend
	 * @param register
	 *            whether to add this breakpoint to the breakpoint manager
	 * @param attributes
	 *            a map of client defined attributes that should be assigned to
	 *            the underlying breakpoint marker on creation, or
	 *            <code>null</code> if none.
	 * @return a watchpoint
	 * @exception CoreException
	 *                If this method fails. Reasons include:
	 *                <ul>
	 *                <li>Failure creating underlying marker. The
	 *                CoreException's status contains the underlying exception
	 *                responsible for the failure.</li>
	 *                </ul>
	 * @since 2.0
	 */
	public static IJavaWatchpoint createWatchpoint(IResource resource,
			String typeName, String fieldName, int lineNumber, int charStart,
			int charEnd, int hitCount, boolean register, Map<String, Object> attributes)
			throws CoreException {
		if (attributes == null) {
			attributes = new HashMap<String, Object>(10);
		}
		return new JavaWatchpoint(resource, typeName, fieldName, lineNumber,
				charStart, charEnd, hitCount, register, attributes);
	}

	/**
	 * Creates and returns a method breakpoint with the specified criteria.
	 * 
	 * @param resource
	 *            the resource on which to create the associated breakpoint
	 *            marker
	 * @param typePattern
	 *            the pattern specifying the fully qualified name of type(s)
	 *            this breakpoint suspends execution in. Patterns are limited to
	 *            exact matches and patterns that begin or end with '*'.
	 * @param methodName
	 *            the name of the method(s) this breakpoint suspends execution
	 *            in, or <code>null</code> if this breakpoint does not suspend
	 *            execution based on method name
	 * @param methodSignature
	 *            the signature of the method(s) this breakpoint suspends
	 *            execution in, or <code>null</code> if this breakpoint does not
	 *            suspend execution based on method signature
	 * @param entry
	 *            whether this breakpoint causes execution to suspend on entry
	 *            of methods
	 * @param exit
	 *            whether this breakpoint causes execution to suspend on exit of
	 *            methods
	 * @param nativeOnly
	 *            whether this breakpoint causes execution to suspend on
	 *            entry/exit of native methods only
	 * @param lineNumber
	 *            the lineNumber on which the breakpoint is set - line numbers
	 *            are 1 based, associated with the source file in which the
	 *            breakpoint is set
	 * @param charStart
	 *            the first character index associated with the breakpoint, or
	 *            -1 if unspecified, in the source file in which the breakpoint
	 *            is set
	 * @param charEnd
	 *            the last character index associated with the breakpoint, or -1
	 *            if unspecified, in the source file in which the breakpoint is
	 *            set
	 * @param hitCount
	 *            the number of times the breakpoint will be hit before
	 *            suspending execution - 0 if it should always suspend
	 * @param register
	 *            whether to add this breakpoint to the breakpoint manager
	 * @param attributes
	 *            a map of client defined attributes that should be assigned to
	 *            the underlying breakpoint marker on creation, or
	 *            <code>null</code> if none.
	 * @return a method breakpoint
	 * @exception CoreException
	 *                If this method fails. Reasons include:
	 *                <ul>
	 *                <li>Failure creating underlying marker. The exception's
	 *                status contains the underlying exception responsible for
	 *                the failure.</li>
	 *                </ul>
	 * @since 2.0
	 */
	public static IJavaMethodBreakpoint createMethodBreakpoint(
			IResource resource, String typePattern, String methodName,
			String methodSignature, boolean entry, boolean exit,
			boolean nativeOnly, int lineNumber, int charStart, int charEnd,
			int hitCount, boolean register, Map<String, Object> attributes)
			throws CoreException {
		if (attributes == null) {
			attributes = new HashMap<String, Object>(10);
		}
		return new JavaMethodBreakpoint(resource, typePattern, methodName,
				methodSignature, entry, exit, nativeOnly, lineNumber,
				charStart, charEnd, hitCount, register, attributes);
	}

	/**
	 * Creates and returns a method entry breakpoint with the specified
	 * criteria. A method entry breakpoint will only be installed for methods
	 * that have executable code (i.e. will not work for native methods).
	 * 
	 * @param resource
	 *            the resource on which to create the associated breakpoint
	 *            marker
	 * @param typeName
	 *            the fully qualified name of type this breakpoint suspends
	 *            execution in.
	 * @param methodName
	 *            the name of the method this breakpoint suspends execution in
	 * @param methodSignature
	 *            the signature of the method this breakpoint suspends execution
	 *            in
	 * @param lineNumber
	 *            the lineNumber on which the breakpoint is set - line numbers
	 *            are 1 based, associated with the source file in which the
	 *            breakpoint is set
	 * @param charStart
	 *            the first character index associated with the breakpoint, or
	 *            -1 if unspecified, in the source file in which the breakpoint
	 *            is set
	 * @param charEnd
	 *            the last character index associated with the breakpoint, or -1
	 *            if unspecified, in the source file in which the breakpoint is
	 *            set
	 * @param hitCount
	 *            the number of times the breakpoint will be hit before
	 *            suspending execution - 0 if it should always suspend
	 * @param register
	 *            whether to add this breakpoint to the breakpoint manager
	 * @param attributes
	 *            a map of client defined attributes that should be assigned to
	 *            the underlying breakpoint marker on creation, or
	 *            <code>null</code> if none.
	 * @return a method entry breakpoint
	 * @exception CoreException
	 *                If this method fails. Reasons include:
	 *                <ul>
	 *                <li>Failure creating underlying marker. The exception's
	 *                status contains the underlying exception responsible for
	 *                the failure.</li>
	 *                </ul>
	 * @since 2.0
	 */
	public static IJavaMethodEntryBreakpoint createMethodEntryBreakpoint(
			IResource resource, String typeName, String methodName,
			String methodSignature, int lineNumber, int charStart, int charEnd,
			int hitCount, boolean register, Map<String, Object> attributes)
			throws CoreException {
		if (attributes == null) {
			attributes = new HashMap<String, Object>(10);
		}
		return new JavaMethodEntryBreakpoint(resource, typeName, methodName,
				methodSignature, lineNumber, charStart, charEnd, hitCount,
				register, attributes);
	}

	/**
	 * Returns a Java line breakpoint that is already registered with the
	 * breakpoint manager for a type with the given name at the given line
	 * number.
	 * 
	 * @param typeName
	 *            fully qualified type name
	 * @param lineNumber
	 *            line number
	 * @return a Java line breakpoint that is already registered with the
	 *         breakpoint manager for a type with the given name at the given
	 *         line number or <code>null</code> if no such breakpoint is
	 *         registered
	 * @exception CoreException
	 *                if unable to retrieve the associated marker attributes
	 *                (line number).
	 */
	public static IJavaLineBreakpoint lineBreakpointExists(String typeName,
			int lineNumber) throws CoreException {
		String modelId = getPluginIdentifier();
		String markerType = JavaLineBreakpoint.getMarkerType();
		IBreakpointManager manager = DebugPlugin.getDefault()
				.getBreakpointManager();
		IBreakpoint[] breakpoints = manager.getBreakpoints(modelId);
		for (int i = 0; i < breakpoints.length; i++) {
			if (!(breakpoints[i] instanceof IJavaLineBreakpoint)) {
				continue;
			}
			IJavaLineBreakpoint breakpoint = (IJavaLineBreakpoint) breakpoints[i];
			IMarker marker = breakpoint.getMarker();
			if (marker != null && marker.exists()
					&& marker.getType().equals(markerType)) {
				String breakpointTypeName = breakpoint.getTypeName();
				if (breakpointTypeName.equals(typeName)
						|| breakpointTypeName.startsWith(typeName + '$')) {
					if (breakpoint.getLineNumber() == lineNumber) {
						return breakpoint;
					}
				}
			}
		}
		return null;
	}

	/**
	 * Returns a Java line breakpoint that is already registered with the
	 * breakpoint manager for a type with the given name at the given line
	 * number in the given resource.
	 * 
	 * @param resource
	 *            the resource
	 * @param typeName
	 *            fully qualified type name
	 * @param lineNumber
	 *            line number
	 * @return a Java line breakpoint that is already registered with the
	 *         breakpoint manager for a type with the given name at the given
	 *         line number or <code>null</code> if no such breakpoint is
	 *         registered
	 * @exception CoreException
	 *                if unable to retrieve the associated marker attributes
	 *                (line number).
	 * @since 3.1
	 */
	public static IJavaLineBreakpoint lineBreakpointExists(IResource resource,
			String typeName, int lineNumber) throws CoreException {
		String modelId = getPluginIdentifier();
		String markerType = JavaLineBreakpoint.getMarkerType();
		IBreakpointManager manager = DebugPlugin.getDefault()
				.getBreakpointManager();
		IBreakpoint[] breakpoints = manager.getBreakpoints(modelId);
		for (int i = 0; i < breakpoints.length; i++) {
			if (!(breakpoints[i] instanceof IJavaLineBreakpoint)) {
				continue;
			}
			IJavaLineBreakpoint breakpoint = (IJavaLineBreakpoint) breakpoints[i];
			IMarker marker = breakpoint.getMarker();
			if (marker != null && marker.exists()
					&& marker.getType().equals(markerType)) {
				String breakpointTypeName = breakpoint.getTypeName();
				if ((breakpointTypeName.equals(typeName) || breakpointTypeName
						.startsWith(typeName + '$'))
						&& breakpoint.getLineNumber() == lineNumber
						&& resource.equals(marker.getResource())) {
					return breakpoint;
				}
			}
		}
		return null;
	}

	/**
	 * Returns the preference store for this plug-in or <code>null</code> if the
	 * store is not available.
	 * <br><br>
	 * The Preferences class has been deprecated and clients should directly be using the 
	 * InstanceScope node for JDIDebugPlugin rather than this convenience method.
	 * <br><br>
	 * For example:
	 * <pre>
	 * IEclipsePreferences node = InstanceScope.INSTANCE.getNode(JDIDebugPlugin.getUniqueIdentifier());
	 * if(node != null) {
	 * 	...
	 * }
	 * </pre>
	 * 
	 * @return the preference store for this plug-in
	 * @since 2.0
	 * @deprecated the {@link Preferences} class has been deprecated, use the {@link IEclipsePreferences} accessors instead
	 */
	public static Preferences getPreferences() {
		JDIDebugPlugin deflt = JDIDebugPlugin.getDefault();
		if (deflt != null) {
			return deflt.getPluginPreferences();
		}
		return null;
	}

	/**
	 * Saves the preference store for this plug-in.
	 * <br><br>
	 * The Preferences class has been deprecated and clients should directly be using the 
	 * InstanceScope node for JDIDebugPlugin rather than this convenience method.
	 * <br><br>
	 * For example:
	 * <pre>
	 * IEclipsePreferences node = InstanceScope.INSTANCE.getNode(JDIDebugPlugin.getUniqueIdentifier());
	 * if(node != null) {
	 * 	try {
	 * 		node.flush();
	 * 	} catch (BackingStoreException e) {
	 * 		log(e);
	 * 	}
	 * }
	 * </pre>
	 * @since 2.0
	 * @deprecated the {@link Preferences} class has been deprecated, use the {@link IEclipsePreferences} accessors instead
	 */
	public static void savePreferences() {
		IEclipsePreferences node = InstanceScope.INSTANCE.getNode(JDIDebugPlugin.getUniqueIdentifier());
		if(node != null) {
			try {
				node.flush();
			} catch (BackingStoreException bse) {
				JDIDebugPlugin.log(bse);
			}
		}
	}

	/**
	 * Creates and returns a class prepare breakpoint for a type with the given
	 * name. The marker associated with the breakpoint will be created on the
	 * specified resource.
	 * 
	 * @param resource
	 *            the resource on which to create the associated breakpoint
	 *            marker
	 * @param typeName
	 *            the fully qualified name of the type for which to create the
	 *            breakpoint
	 * @param memberType
	 *            one of <code>TYPE_CLASS</code> or <code>TYPE_INTERFACE</code>
	 * @param charStart
	 *            the first character index associated with the breakpoint, or
	 *            -1 if unspecified, in the source file in which the breakpoint
	 *            is set
	 * @param charEnd
	 *            the last character index associated with the breakpoint, or -1
	 *            if unspecified, in the source file in which the breakpoint is
	 *            set
	 * @param register
	 *            whether to add this breakpoint to the breakpoint manager
	 * @param attributes
	 *            a map of client defined attributes that should be assigned to
	 *            the underlying breakpoint marker on creation or
	 *            <code>null</code> if none.
	 * @return a class prepare breakpoint
	 * @exception CoreException
	 *                If this method fails. Reasons include:
	 *                <ul>
	 *                <li>Failure creating underlying marker. The exception's
	 *                status contains the underlying exception responsible for
	 *                the failure.</li>
	 *                </ul>
	 * @since 3.0
	 */
	public static IJavaClassPrepareBreakpoint createClassPrepareBreakpoint(
			IResource resource, String typeName, int memberType, int charStart,
			int charEnd, boolean register, Map<String, Object> attributes) throws CoreException {
		if (attributes == null) {
			attributes = new HashMap<String, Object>(10);
		}
		return new JavaClassPrepareBreakpoint(resource, typeName, memberType,
				charStart, charEnd, register, attributes);
	}
}
