/*******************************************************************************
 *  Copyright (c) 2000, 2011 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.core.breakpoints;

import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaMethodEntryBreakpoint;
import org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget;

import com.sun.jdi.ClassType;
import com.sun.jdi.Location;
import com.sun.jdi.Method;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.EventRequest;

/**
 * A line breakpoint at the first executable location in a specific method.
 */

public class JavaMethodEntryBreakpoint extends JavaLineBreakpoint implements
		IJavaMethodEntryBreakpoint {

	protected static final String JAVA_METHOD_ENTRY_BREAKPOINT = "org.eclipse.jdt.debug.javaMethodEntryBreakpointMarker"; //$NON-NLS-1$

	/**
	 * Breakpoint attribute storing the name of the method in which a breakpoint
	 * is contained. (value <code>"org.eclipse.jdt.debug.core.methodName"</code>
	 * ). This attribute is a <code>String</code>.
	 */
	private static final String METHOD_NAME = "org.eclipse.jdt.debug.core.methodName"; //$NON-NLS-1$	

	/**
	 * Breakpoint attribute storing the signature of the method in which a
	 * breakpoint is contained. (value
	 * <code>"org.eclipse.jdt.debug.core.methodSignature"</code>). This
	 * attribute is a <code>String</code>.
	 */
	private static final String METHOD_SIGNATURE = "org.eclipse.jdt.debug.core.methodSignature"; //$NON-NLS-1$	

	/**
	 * Cache of method name attribute
	 */
	private String fMethodName = null;

	/**
	 * Cache of method signature attribute
	 */
	private String fMethodSignature = null;

	/**
	 * Constructs a new method breakpoint
	 */
	public JavaMethodEntryBreakpoint() {
	}

	public JavaMethodEntryBreakpoint(final IResource resource,
			final String typeName, final String methodName,
			final String methodSignature, final int lineNumber,
			final int charStart, final int charEnd, final int hitCount,
			final boolean register, final Map<String, Object> attributes) throws CoreException {
		IWorkspaceRunnable wr = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				// create the marker
				setMarker(resource.createMarker(JAVA_METHOD_ENTRY_BREAKPOINT));

				// add attributes
				addLineBreakpointAttributes(attributes, getModelIdentifier(),
						true, lineNumber, charStart, charEnd);
				addMethodNameAndSignature(attributes, methodName,
						methodSignature);
				addTypeNameAndHitCount(attributes, typeName, hitCount);
				// set attributes
				attributes.put(SUSPEND_POLICY, new Integer(
						getDefaultSuspendPolicy()));
				ensureMarker().setAttributes(attributes);

				register(register);
			}

		};
		run(getMarkerRule(resource), wr);
	}

	/**
	 * Adds the method name and signature attributes to the given attribute map,
	 * and initializes the local cache of method name and signature.
	 */
	private void addMethodNameAndSignature(Map<String, Object> attributes, String methodName,
			String methodSignature) {
		if (methodName != null) {
			attributes.put(METHOD_NAME, methodName);
		}
		if (methodSignature != null) {
			attributes.put(METHOD_SIGNATURE, methodSignature);
		}
		fMethodName = methodName;
		fMethodSignature = methodSignature;
	}

	/**
	 * @see IJavaMethodEntryBreakpoint#getMethodName()
	 */
	public String getMethodName() {
		return fMethodName;
	}

	/**
	 * @see IJavaMethodEntryBreakpoint#getMethodSignature()
	 */
	public String getMethodSignature() {
		return fMethodSignature;
	}

	/**
	 * Initialize cache of attributes
	 * 
	 * @see IBreakpoint#setMarker(IMarker)
	 */
	@Override
	public void setMarker(IMarker marker) throws CoreException {
		super.setMarker(marker);
		fMethodName = marker.getAttribute(METHOD_NAME, null);
		fMethodSignature = marker.getAttribute(METHOD_SIGNATURE, null);
	}

	/**
	 * @see IJavaLineBreakpoint#supportsCondition()
	 */
	@Override
	public boolean supportsCondition() {
		return false;
	}

	/**
	 * @see JavaBreakpoint#newRequests(JDIDebugTarget, ReferenceType)
	 */
	@Override
	protected EventRequest[] newRequests(JDIDebugTarget target,
			ReferenceType type) throws CoreException {
		try {
			if (type instanceof ClassType) {
				ClassType clazz = (ClassType) type;
				Method method = clazz.concreteMethodByName(getMethodName(),
						getMethodSignature());
				if (method == null) {
					return null;
				}
				Location location = method.location();
				if (location == null || location.codeIndex() == -1) {
					return null;
				}
				BreakpointRequest req = type.virtualMachine()
						.eventRequestManager()
						.createBreakpointRequest(location);
				configureRequest(req, target);
				return new EventRequest[] { req };
			}
			return null;
		} catch (RuntimeException e) {
			target.internalError(e);
			return null;
		}
	}
}
