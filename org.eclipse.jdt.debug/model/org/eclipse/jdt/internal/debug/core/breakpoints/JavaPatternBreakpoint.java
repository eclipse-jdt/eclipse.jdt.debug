/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.core.breakpoints;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.debug.core.IJavaPatternBreakpoint;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;
import org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget;

import com.ibm.icu.text.MessageFormat;
import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.VirtualMachine;

@SuppressWarnings("deprecation")
public class JavaPatternBreakpoint extends JavaLineBreakpoint implements
		IJavaPatternBreakpoint {

	private static final String PATTERN_BREAKPOINT = "org.eclipse.jdt.debug.javaPatternBreakpointMarker"; //$NON-NLS-1$

	/**
	 * Breakpoint attribute storing the pattern identifier of the source file in
	 * which a breakpoint is created (value
	 * <code>"org.eclipse.jdt.debug.core.pattern"</code>). This attribute is a
	 * <code>String</code>.
	 */
	protected static final String PATTERN = "org.eclipse.jdt.debug.core.pattern"; //$NON-NLS-1$	

	public JavaPatternBreakpoint() {
	}

	/**
	 * @see JDIDebugModel#createPatternBreakpoint(IResource, String, int, int,
	 *      int, int, boolean, Map)
	 */
	public JavaPatternBreakpoint(IResource resource, String sourceName,
			String pattern, int lineNumber, int charStart, int charEnd,
			int hitCount, boolean add, Map<String, Object> attributes) throws DebugException {
		this(resource, sourceName, pattern, lineNumber, charStart, charEnd,
				hitCount, add, attributes, PATTERN_BREAKPOINT);
	}

	public JavaPatternBreakpoint(final IResource resource,
			final String sourceName, final String pattern,
			final int lineNumber, final int charStart, final int charEnd,
			final int hitCount, final boolean add, final Map<String, Object> attributes,
			final String markerType) throws DebugException {
		IWorkspaceRunnable wr = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {

				// create the marker
				setMarker(resource.createMarker(markerType));

				// add attributes
				addLineBreakpointAttributes(attributes, getModelIdentifier(),
						true, lineNumber, charStart, charEnd);
				addPatternAndHitCount(attributes, sourceName, pattern, hitCount);
				// set attributes
				attributes.put(SUSPEND_POLICY, new Integer(
						getDefaultSuspendPolicy()));
				ensureMarker().setAttributes(attributes);

				register(add);
			}
		};
		run(getMarkerRule(resource), wr);
	}

	/**
	 * @see JavaBreakpoint#getReferenceTypeName()
	 */
	protected String getReferenceTypeName() {
		String name = ""; //$NON-NLS-1$
		try {
			name = getPattern();
		} catch (CoreException ce) {
			JDIDebugPlugin.log(ce);
		}
		return name;
	}

	/**
	 * @see JavaBreakpoint#installableReferenceType(ReferenceType)
	 */
	@Override
	protected boolean installableReferenceType(ReferenceType type,
			JDIDebugTarget target) throws CoreException {
		// if the source name attribute is specified, check for a match with the
		// debug attribute (if available)
		if (getSourceName() != null) {
			String sourceName = null;
			try {
				sourceName = type.sourceName();
			} catch (AbsentInformationException e) {
				// unable to compare
			} catch (VMDisconnectedException e) {
				if (!target.isAvailable()) {
					return false;
				}
				target.targetRequestFailed(
						MessageFormat.format(JDIDebugBreakpointMessages.JavaPatternBreakpoint_exception_source_name, e.toString(), type.name()), e);
				// execution will not reach this line, as
				// #targetRequestFailed will throw an exception
				return false;
			} catch (RuntimeException e) {
				target.targetRequestFailed(
						MessageFormat.format(JDIDebugBreakpointMessages.JavaPatternBreakpoint_exception_source_name, e.toString(), type.name()), e);
				// execution will not reach this line, as
				// #targetRequestFailed will throw an exception
				return false;
			}

			// if the debug attribute matches the source name, attempt
			// installation
			if (sourceName != null) {
				if (!getSourceName().equalsIgnoreCase(sourceName)) {
					return false;
				}
			}
		}

		String pattern = getPattern();
		String queriedType = type.name();
		if (pattern == null || queriedType == null) {
			return false;
		}
		if (queriedType.startsWith(pattern)) {
			// query registered listeners to see if this pattern breakpoint
			// should
			// be installed in the given target
			return queryInstallListeners(target, type);
		}
		return false;
	}

	/**
	 * Adds the class name pattern and hit count attributes to the given map.
	 */
	protected void addPatternAndHitCount(Map<String, Object> attributes, String sourceName,
			String pattern, int hitCount) {
		attributes.put(PATTERN, pattern);
		if (sourceName != null) {
			attributes.put(SOURCE_NAME, sourceName);
		}
		if (hitCount > 0) {
			attributes.put(HIT_COUNT, new Integer(hitCount));
			attributes.put(EXPIRED, Boolean.FALSE);
		}
	}

	/**
	 * @see IJavaPatternBreakpoint#getPattern()
	 */
	public String getPattern() throws CoreException {
		return (String) ensureMarker().getAttribute(PATTERN);
	}

	/**
	 * @see IJavaPatternBreakpoint#getSourceName()
	 */
	public String getSourceName() throws CoreException {
		return (String) ensureMarker().getAttribute(SOURCE_NAME);
	}

	@Override
	protected void createRequests(JDIDebugTarget target) throws CoreException {
		if (target.isTerminated() || shouldSkipBreakpoint()) {
			return;
		}
		String referenceTypeName = getReferenceTypeName();
		if (referenceTypeName == null) {
			return;
		}

		String classPrepareTypeName = referenceTypeName;
		// create request to listen to class loads
		// name may only be partially resolved
		if (!referenceTypeName.endsWith("*")) { //$NON-NLS-1$
			classPrepareTypeName = classPrepareTypeName + '*';
		}
		registerRequest(target.createClassPrepareRequest(classPrepareTypeName),
				target);

		// create breakpoint requests for each class currently loaded
		VirtualMachine vm = target.getVM();
		if (vm == null) {
			target.requestFailed(
					JDIDebugBreakpointMessages.JavaPatternBreakpoint_Unable_to_add_breakpoint___VM_disconnected__1,
					null);
		}
		List<ReferenceType> classes = null;
		try {
			classes = vm.allClasses();
		} catch (RuntimeException e) {
			target.targetRequestFailed(
					JDIDebugBreakpointMessages.JavaPatternBreakpoint_0, e);
		}
		if (classes != null) {
			Iterator<ReferenceType> iter = classes.iterator();
			String typeName = null;
			ReferenceType type = null;
			while (iter.hasNext()) {
				type = iter.next();
				typeName = type.name();
				if (typeName != null && typeName.startsWith(referenceTypeName)) {
					createRequest(target, type);
				}
			}
		}
	}
}
