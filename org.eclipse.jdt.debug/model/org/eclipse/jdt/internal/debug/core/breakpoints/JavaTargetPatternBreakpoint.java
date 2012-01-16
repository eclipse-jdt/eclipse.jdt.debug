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
package org.eclipse.jdt.internal.debug.core.breakpoints;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaTargetPatternBreakpoint;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;
import org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget;

import com.ibm.icu.text.MessageFormat;
import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.VirtualMachine;

public class JavaTargetPatternBreakpoint extends JavaLineBreakpoint implements
		IJavaTargetPatternBreakpoint {

	private static final String TARGET_PATTERN_BREAKPOINT = "org.eclipse.jdt.debug.javaTargetPatternBreakpointMarker"; //$NON-NLS-1$	

	/**
	 * Table of targets to patterns
	 */
	private HashMap<IJavaDebugTarget, String> fPatterns;

	public JavaTargetPatternBreakpoint() {
	}

	/**
	 * @see JDIDebugModel#createTargetPatternBreakpoint(IResource, String, int,
	 *      int, int, int, boolean, Map)
	 */
	public JavaTargetPatternBreakpoint(IResource resource, String sourceName,
			int lineNumber, int charStart, int charEnd, int hitCount,
			boolean add, Map<String, Object> attributes) throws DebugException {
		this(resource, sourceName, lineNumber, charStart, charEnd, hitCount,
				add, attributes, TARGET_PATTERN_BREAKPOINT);
	}

	public JavaTargetPatternBreakpoint(final IResource resource,
			final String sourceName, final int lineNumber, final int charStart,
			final int charEnd, final int hitCount, final boolean add,
			final Map<String, Object> attributes, final String markerType)
			throws DebugException {
		IWorkspaceRunnable wr = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {

				// create the marker
				setMarker(resource.createMarker(markerType));

				// add attributes
				addLineBreakpointAttributes(attributes, getModelIdentifier(),
						true, lineNumber, charStart, charEnd);
				addSourceNameAndHitCount(attributes, sourceName, hitCount);
				attributes.put(SUSPEND_POLICY, new Integer(
						getDefaultSuspendPolicy()));
				// set attributes
				ensureMarker().setAttributes(attributes);

				register(add);
			}
		};
		run(getMarkerRule(resource), wr);
	}

	/**
	 * Creates the event requests to:
	 * <ul>
	 * <li>Listen to class loads related to the breakpoint</li>
	 * <li>Respond to the breakpoint being hit</li>
	 * </ul>
	 */
	@Override
	public void addToTarget(JDIDebugTarget target) throws CoreException {

		// pre-notification
		fireAdding(target);

		String referenceTypeName = getPattern(target);
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
		if (vm != null) {
			List<ReferenceType> classes = vm.allClasses();
			if (classes != null) {
				String typeName = null;
				for(ReferenceType type : classes) {
					typeName = type.name();
					if (typeName != null && typeName.startsWith(referenceTypeName)) {
						createRequest(target, type);
					}
				}
			}
		} else {
			target.requestFailed(
					JDIDebugBreakpointMessages.JavaTargetPatternBreakpoint_Unable_to_add_breakpoint___VM_disconnected__1,
					null);
		}
	}

	/**
	 * @see JavaBreakpoint#getReferenceTypeName()
	 */
	protected String getReferenceTypeName() {
		String name = "*"; //$NON-NLS-1$
		try {
			name = getSourceName();
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
						MessageFormat.format(
								JDIDebugBreakpointMessages.JavaPatternBreakpoint_exception_source_name, 
								e.toString(), 
								type.name()), 
								e);
				// execution will not reach this line, as
				// #targetRequestFailed will throw an exception
				return false;
			} catch (RuntimeException e) {
				target.targetRequestFailed(
						MessageFormat.format(
								JDIDebugBreakpointMessages.JavaPatternBreakpoint_exception_source_name,
								e.toString(),
								type.name()), e);
				// execution will not reach this line, as
				// #targetRequestFailed will throw an exception
				return false;
			}

			// if the debug attribute matches the source name, attempt
			// installion
			if (sourceName != null) {
				if (!getSourceName().equalsIgnoreCase(sourceName)) {
					return false;
				}
			}
		}

		String pattern = getPattern(target);
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
	 * Adds the source name and hit count attributes to the given map.
	 */
	protected void addSourceNameAndHitCount(Map<String, Object> attributes, String sourceName,
			int hitCount) {
		if (sourceName != null) {
			attributes.put(SOURCE_NAME, sourceName);
		}
		if (hitCount > 0) {
			attributes.put(HIT_COUNT, new Integer(hitCount));
			attributes.put(EXPIRED, Boolean.FALSE);
		}
	}

	/**
	 * @see IJavaTargetPatternBreakpoint#getPattern(IJavaDebugTarget)
	 */
	public String getPattern(IJavaDebugTarget target) {
		if (fPatterns != null) {
			return fPatterns.get(target);
		}
		return null;
	}

	/**
	 * @see IJavaTargetPatternBreakpoint#setPattern(IJavaDebugTarget, String)
	 */
	public void setPattern(IJavaDebugTarget target, String pattern)
			throws CoreException {
		if (fPatterns == null) {
			fPatterns = new HashMap<IJavaDebugTarget, String>(2);
		}
		// if pattern is changing then remove and re-add
		String oldPattern = getPattern(target);
		fPatterns.put(target, pattern);
		if (oldPattern != null && !oldPattern.equals(pattern)) {
			recreate((JDIDebugTarget) target);
			fireChanged();
		}
	}

	/**
	 * @see IJavaTargetPatternBreakpoint#getSourceName()
	 */
	public String getSourceName() throws CoreException {
		return (String) ensureMarker().getAttribute(SOURCE_NAME);
	}

	/**
	 * @see org.eclipse.jdt.internal.debug.core.breakpoints.JavaBreakpoint#removeFromTarget(JDIDebugTarget)
	 */
	@Override
	public void removeFromTarget(JDIDebugTarget target) throws CoreException {
		fPatterns.remove(target);
		super.removeFromTarget(target);
	}
}
