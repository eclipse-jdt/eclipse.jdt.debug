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

import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jdt.debug.core.IJavaClassPrepareBreakpoint;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget;
import org.eclipse.jdt.internal.debug.core.model.JDIThread;

import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.event.ClassPrepareEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventSet;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.EventRequest;

/**
 * Class prepare breakpoint.
 * 
 * @since 3.0
 */
public class JavaClassPrepareBreakpoint extends JavaBreakpoint implements
		IJavaClassPrepareBreakpoint {

	public static final String JAVA_CLASS_PREPARE_BREAKPOINT = "org.eclipse.jdt.debug.javaClassPrepareBreakpointMarker"; //$NON-NLS-1$

	/**
	 * Class prepare breakpoint attribute storing the type of member this
	 * breakpoint is associated with (value
	 * <code>"org.eclipse.jdt.debug.core.memberType"</code>), encoded as an
	 * integer.
	 */
	protected static final String MEMBER_TYPE = "org.eclipse.jdt.debug.core.memberType"; //$NON-NLS-1$

	/**
	 * Creates and returns a Java class prepare breakpoint for the given type.
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
	 * @param add
	 *            whether to add this breakpoint to the breakpoint manager
	 * @return a Java class prepare breakpoint
	 * @exception DebugException
	 *                if unable to create the associated marker due to a lower
	 *                level exception.
	 */
	public JavaClassPrepareBreakpoint(final IResource resource,
			final String typeName, final int memberType, final int charStart,
			final int charEnd, final boolean add, final Map<String, Object> attributes)
			throws DebugException {
		IWorkspaceRunnable wr = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {
				// create the marker
				setMarker(resource.createMarker(JAVA_CLASS_PREPARE_BREAKPOINT));

				// add attributes
				attributes.put(IBreakpoint.ID, getModelIdentifier());
				attributes.put(IMarker.CHAR_START, new Integer(charStart));
				attributes.put(IMarker.CHAR_END, new Integer(charEnd));
				attributes.put(TYPE_NAME, typeName);
				attributes.put(MEMBER_TYPE, new Integer(memberType));
				attributes.put(ENABLED, Boolean.TRUE);
				attributes.put(SUSPEND_POLICY, new Integer(
						getDefaultSuspendPolicy()));

				ensureMarker().setAttributes(attributes);

				register(add);
			}

		};
		run(getMarkerRule(resource), wr);
	}

	public JavaClassPrepareBreakpoint() {
	}

	/**
	 * Creates event requests for the given target
	 */
	@Override
	protected void createRequests(JDIDebugTarget target) throws CoreException {
		if (target.isTerminated() || shouldSkipBreakpoint()) {
			return;
		}
		String referenceTypeName = getTypeName();
		if (referenceTypeName == null) {
			return;
		}

		ClassPrepareRequest request = target.createClassPrepareRequest(
				referenceTypeName, null, false);
		configureRequestHitCount(request);
		updateEnabledState(request, target);
		registerRequest(request, target);
		// TODO: do we show anything for types already loaded?
		incrementInstallCount();
	}

	/**
	 * Remove the given request from the given target. If the request is the
	 * breakpoint request associated with this breakpoint, decrement the install
	 * count.
	 */
	@Override
	protected void deregisterRequest(EventRequest request, JDIDebugTarget target)
			throws CoreException {
		target.removeJDIEventListener(this, request);
		// A request may be getting deregistered because the breakpoint has
		// been deleted. It may be that this occurred because of a marker
		// deletion.
		// Don't try updating the marker (decrementing the install count) if
		// it no longer exists.
		if (getMarker().exists()) {
			decrementInstallCount();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * Not supported for class prepare breakpoints.
	 * 
	 * @see org.eclipse.jdt.internal.debug.core.breakpoints.JavaBreakpoint#
	 * addInstanceFilter(com.sun.jdi.request.EventRequest,
	 * com.sun.jdi.ObjectReference)
	 */
	@Override
	protected void addInstanceFilter(EventRequest request,
			ObjectReference object) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * This method not used for class prepare breakpoints.
	 * 
	 * @see
	 * org.eclipse.jdt.internal.debug.core.breakpoints.JavaBreakpoint#newRequest
	 * (org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget,
	 * com.sun.jdi.ReferenceType)
	 */
	@Override
	protected EventRequest[] newRequests(JDIDebugTarget target,
			ReferenceType type) throws CoreException {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * Not supported for class prepare breakpoints.
	 * 
	 * @see org.eclipse.jdt.internal.debug.core.breakpoints.JavaBreakpoint#
	 * setRequestThreadFilter(com.sun.jdi.request.EventRequest,
	 * com.sun.jdi.ThreadReference)
	 */
	@Override
	protected void setRequestThreadFilter(EventRequest request,
			ThreadReference thread) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.internal.debug.core.breakpoints.JavaBreakpoint#
	 * handleClassPrepareEvent(com.sun.jdi.event.ClassPrepareEvent,
	 * org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget)
	 */
	@Override
	public boolean handleClassPrepareEvent(ClassPrepareEvent event,
			JDIDebugTarget target, boolean suspendVote) {
		try {
			if (isEnabled()
					&& event.referenceType().name().equals(getTypeName())) {
				ThreadReference threadRef = event.thread();
				JDIThread thread = target.findThread(threadRef);
				if (thread == null || thread.isIgnoringBreakpoints()) {
					return true;
				}
				return handleBreakpointEvent(event, thread, suspendVote);
			}
		} catch (CoreException e) {
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.internal.debug.core.breakpoints.JavaBreakpoint#
	 * classPrepareComplete(com.sun.jdi.event.Event,
	 * org.eclipse.jdt.internal.debug.core.model.JDIThread, boolean)
	 */
	@Override
	protected void classPrepareComplete(Event event, JDIThread thread,
			boolean suspend, EventSet eventSet) {
		thread.completeBreakpointHandling(this, suspend, true, eventSet);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jdt.debug.core.IJavaClassPrepareBreakpoint#getMemberType()
	 */
	public int getMemberType() throws CoreException {
		return ensureMarker().getAttribute(MEMBER_TYPE,
				IJavaClassPrepareBreakpoint.TYPE_CLASS);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.debug.core.IJavaBreakpoint#supportsInstanceFilters()
	 */
	@Override
	public boolean supportsInstanceFilters() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jdt.debug.core.IJavaBreakpoint#addInstanceFilter(org.eclipse
	 * .jdt.debug.core.IJavaObject)
	 */
	@Override
	public void addInstanceFilter(IJavaObject object) throws CoreException {
		throw new CoreException(new Status(IStatus.ERROR,
				JDIDebugModel.getPluginIdentifier(),
				DebugException.REQUEST_FAILED,
				JDIDebugBreakpointMessages.JavaClassPrepareBreakpoint_2, null));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jdt.debug.core.IJavaBreakpoint#setThreadFilter(org.eclipse
	 * .jdt.debug.core.IJavaThread)
	 */
	@Override
	public void setThreadFilter(IJavaThread thread) throws CoreException {
		throw new CoreException(new Status(IStatus.ERROR,
				JDIDebugModel.getPluginIdentifier(),
				DebugException.REQUEST_FAILED,
				JDIDebugBreakpointMessages.JavaClassPrepareBreakpoint_3, null));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.debug.core.IJavaBreakpoint#supportsThreadFilters()
	 */
	@Override
	public boolean supportsThreadFilters() {
		return false;
	}
}
