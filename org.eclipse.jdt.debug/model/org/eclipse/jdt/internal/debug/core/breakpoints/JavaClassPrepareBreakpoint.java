/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.core.breakpoints;

import java.util.Map;

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
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.EventRequest;

/**
 * Class prepare breakpoint.
 * 
 * @since 3.0
 */
public class JavaClassPrepareBreakpoint extends JavaBreakpoint implements IJavaClassPrepareBreakpoint {
	
	private static final String JAVA_CLASS_PREPARE_BREAKPOINT= "org.eclipse.jdt.debug.javaClassPrepareBreakpointMarker"; //$NON-NLS-1$
	
	/**
	 * Class prepare breakpoint attribute storing the type of member this
	 * breakpoint is associated with
	 * (value <code>"org.eclipse.jdt.debug.core.memberType"</code>), encoded
	 * as an integer.
	 */
	protected static final String MEMBER_TYPE = "org.eclipse.jdt.debug.core.memberType"; //$NON-NLS-1$
	
	/**
	 * Creates and returns an exception breakpoint for the
	 * given (throwable) type. Caught and uncaught specify where the exception
	 * should cause thread suspensions - that is, in caught and/or uncaught locations.
	 * Checked indicates if the given exception is a checked exception.
	 * @param resource the resource on which to create the associated
	 *  breakpoint marker 
	 * @param typeName the fully qualified name of the type for
	 *  which to create the breakpoint
	 * @param memberType one of <code>TYPE_CLASS</code> or <code>TYPE_INTERFACE</code>
 	 * @param add whether to add this breakpoint to the breakpoint manager
	 * @return a Java class prepare breakpoint
	 * @exception DebugException if unable to create the associated marker due
	 *  to a lower level exception.
	 */	
	public JavaClassPrepareBreakpoint(final IResource resource, final String typeName, final int memberType, final boolean add, final Map attributes) throws DebugException {
		IWorkspaceRunnable wr= new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {				
				// create the marker
				setMarker(resource.createMarker(JAVA_CLASS_PREPARE_BREAKPOINT));
				
				// add attributes
				attributes.put(IBreakpoint.ID, getModelIdentifier());
				attributes.put(TYPE_NAME, typeName);
				attributes.put(MEMBER_TYPE, new Integer(memberType));
				attributes.put(ENABLED, Boolean.TRUE);
				
				ensureMarker().setAttributes(attributes);
				
				register(add);
			}

		};
		run(null, wr);
	}	
	
	public JavaClassPrepareBreakpoint() {
	}
	
	/**
	 * Creates event requests for the given target
	 */
	protected void createRequests(JDIDebugTarget target) throws CoreException {
		if (target.isTerminated() || shouldSkipBreakpoint()) {
			return;
		}
		String referenceTypeName= getTypeName();
		if (referenceTypeName == null) {
			return;
		}

		ClassPrepareRequest request = target.createClassPrepareRequest(referenceTypeName, null, false);
		configureRequestHitCount(request);
		updateEnabledState(request);
		registerRequest(request, target);
		// TODO: do we show anything for types already loaded?
		incrementInstallCount();
	}
	
	/**
	 * Remove the given request from the given target. If the request
	 * is the breakpoint request associated with this breakpoint,
	 * decrement the install count.
	 */
	protected void deregisterRequest(EventRequest request, JDIDebugTarget target) throws CoreException {
		target.removeJDIEventListener(this, request);
		// A request may be getting deregistered because the breakpoint has
		// been deleted. It may be that this occurred because of a marker deletion.
		// Don't try updating the marker (decrementing the install count) if
		// it no longer exists.
		if (getMarker().exists()) {
			decrementInstallCount();
		}
	}	
	
	/* (non-Javadoc)
	 * 
	 * Not supported for class prepare breakpoints.
	 * 
	 * @see org.eclipse.jdt.internal.debug.core.breakpoints.JavaBreakpoint#addInstanceFilter(com.sun.jdi.request.EventRequest, com.sun.jdi.ObjectReference)
	 */
	protected void addInstanceFilter(EventRequest request, ObjectReference object) {
	}
	/* (non-Javadoc)
	 * 
	 * This method not used for class prepare breakpoints.
	 * 
	 * @see org.eclipse.jdt.internal.debug.core.breakpoints.JavaBreakpoint#newRequest(org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget, com.sun.jdi.ReferenceType)
	 */
	protected EventRequest newRequest(JDIDebugTarget target, ReferenceType type) throws CoreException {
		return null;
	}
	/* (non-Javadoc)
	 * 
	 * Not supported for class prepare breakpoints.
	 * 
	 * @see org.eclipse.jdt.internal.debug.core.breakpoints.JavaBreakpoint#setRequestThreadFilter(com.sun.jdi.request.EventRequest, com.sun.jdi.ThreadReference)
	 */
	protected void setRequestThreadFilter(EventRequest request, ThreadReference thread) {
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.core.breakpoints.JavaBreakpoint#handleClassPrepareEvent(com.sun.jdi.event.ClassPrepareEvent, org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget)
	 */
	public boolean handleClassPrepareEvent(ClassPrepareEvent event, JDIDebugTarget target) {
		try {
			if (isEnabled() && event.referenceType().name().equals(getTypeName())) {
				ThreadReference threadRef= event.thread();
				JDIThread thread= target.findThread(threadRef);	
				if (thread == null || thread.isIgnoringBreakpoints()) {
					return true;
				}
				return handleBreakpointEvent(event, target, thread);
			}
		} catch (CoreException e) {
		}
		return true;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.core.IJavaClassPrepareBreakpoint#getMemberType()
	 */
	public int getMemberType() throws CoreException {
		return ensureMarker().getAttribute(MEMBER_TYPE, IJavaClassPrepareBreakpoint.TYPE_CLASS);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.core.IJavaBreakpoint#supportsInstanceFilters()
	 */
	public boolean supportsInstanceFilters() {
		return false;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.core.IJavaBreakpoint#addInstanceFilter(org.eclipse.jdt.debug.core.IJavaObject)
	 */
	public void addInstanceFilter(IJavaObject object) throws CoreException {
		throw new CoreException(new Status(IStatus.ERROR, JDIDebugModel.getPluginIdentifier(), DebugException.REQUEST_FAILED, JDIDebugBreakpointMessages.getString("JavaClassPrepareBreakpoint.2"), null)); //$NON-NLS-1$
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.core.IJavaBreakpoint#setThreadFilter(org.eclipse.jdt.debug.core.IJavaThread)
	 */
	public void setThreadFilter(IJavaThread thread) throws CoreException {
		throw new CoreException(new Status(IStatus.ERROR, JDIDebugModel.getPluginIdentifier(), DebugException.REQUEST_FAILED, JDIDebugBreakpointMessages.getString("JavaClassPrepareBreakpoint.3"), null)); //$NON-NLS-1$
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.core.IJavaBreakpoint#supportsThreadFilters()
	 */
	public boolean supportsThreadFilters() {
		return false;
	}	
}
