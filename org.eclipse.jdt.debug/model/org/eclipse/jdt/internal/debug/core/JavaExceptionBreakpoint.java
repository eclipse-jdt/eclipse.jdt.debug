package org.eclipse.jdt.internal.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.debug.core.IJavaExceptionBreakpoint;

import com.sun.jdi.ReferenceType;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.ExceptionRequest;

public class JavaExceptionBreakpoint extends JavaBreakpoint implements IJavaExceptionBreakpoint {
	
	private static final String JAVA_EXCEPTION_BREAKPOINT= "org.eclipse.jdt.debug.javaExceptionBreakpointMarker"; //$NON-NLS-1$
	
	/**
	 * Exception breakpoint attribute storing the suspend on caught value
	 * (value <code>"caught"</code>). This attribute is stored as a <code>boolean</code>.
	 * When this attribute is <code>true</code>, a caught exception of the associated
	 * type will cause excecution to suspend .
	 */
	protected static final String CAUGHT = "caught"; //$NON-NLS-1$
	/**
	 * Exception breakpoint attribute storing the suspend on uncaught value
	 * (value <code>"uncaught"</code>). This attribute is stored as a
	 * <code>boolean</code>. When this attribute is <code>true</code>, an uncaught
	 * exception of the associated type will cause excecution to suspend. .
	 */
	protected static final String UNCAUGHT = "uncaught"; //$NON-NLS-1$	
	/**
	 * Exception breakpoint attribute storing the checked value (value <code>"checked"</code>).
	 * This attribute is stored as a <code>boolean</code>, indicating whether an
	 * exception is a checked exception.
	 */
	protected static final String CHECKED = "checked"; //$NON-NLS-1$	
	
	// Attribute strings
	protected static final String[] fgExceptionBreakpointAttributes= new String[]{CHECKED, TYPE_HANDLE};		
	
	public JavaExceptionBreakpoint() {
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
	 * @return a java exception breakpoint
	 * @exception DebugException if unable to create the associated marker due
	 *  to a lower level exception.
	 */	
	public JavaExceptionBreakpoint(final IType exception, final boolean caught, final boolean uncaught, final boolean checked) throws DebugException {
		IWorkspaceRunnable wr= new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {
				IResource resource= null;
				resource= exception.getUnderlyingResource();

				if (resource == null) {
					resource= exception.getJavaProject().getProject();
				}
				
				// create the marker
				setMarker(resource.createMarker(JAVA_EXCEPTION_BREAKPOINT));
				// configure the standard attributes
				setEnabled(true);
				// configure caught, uncaught, checked, and the type attributes
				setCaughtAndUncaught(caught, uncaught);
				setTypeAndChecked(exception, checked);

				// configure the marker as a Java marker
				IMarker marker = ensureMarker();
				Map attributes= marker.getAttributes();
				JavaCore.addJavaElementMarkerAttributes(attributes, exception);
				marker.setAttributes(attributes);
				
				addToBreakpointManager();				
			}

		};
		run(wr);
	}
	
	/**
	 * Sets the exception type on which this breakpoint is installed and whether
	 * or not that exception is a checked exception.
	 */
	protected void setTypeAndChecked(IType exception, boolean checked) throws CoreException {
		String handle = exception.getHandleIdentifier();
		Object[] values= new Object[]{new Boolean(checked), handle};
		ensureMarker().setAttributes(fgExceptionBreakpointAttributes, values);
	}
	
	/**
	 * Sets the values for whether this breakpoint will
	 * suspend execution when the associated exception is thrown
	 * and caught or not caught..
	 */
	public void setCaughtAndUncaught(boolean caught, boolean uncaught) throws CoreException {
		Object[] values= new Object[]{new Boolean(caught), new Boolean(uncaught)};
		String[] attributes= new String[]{CAUGHT, UNCAUGHT};
		ensureMarker().setAttributes(attributes, values);
	}
	
	/**
	 * Creates a request in the given target to suspend when the given exception
	 * type is thrown. The request is returned installed, configured, and enabled
	 * as appropriate for this breakpoint.
	 */
	protected EventRequest newRequest(JDIDebugTarget target, ReferenceType type) throws CoreException {
		if (!isCaught() && !isUncaught()) {
			return null;
		}
			ExceptionRequest request= null;
			try {
				request= target.getEventRequestManager().createExceptionRequest(type, isCaught(), isUncaught());
				configureRequest(request);
			} catch (VMDisconnectedException e) {
				if (target.isTerminated() || target.isDisconnected()) {
					return null;
				}
				JDIDebugPlugin.logError(e);
				return null;
			} catch (RuntimeException e) {
				JDIDebugPlugin.logError(e);
				return null;
			}	
			return request;
	}

	/**
	 * Enable this exception breakpoint.
	 * 
	 * If the exception breakpoint is not catching caught or uncaught,
	 * turn both modes on. If this isn't done, the resulting
	 * state (enabled with caught and uncaught both disabled)
	 * is ambiguous.
	 */
	public void setEnabled(boolean enabled) throws CoreException {
		super.setEnabled(enabled);
		if (isEnabled()) {
			if (!(isCaught() || isUncaught())) {
				setCaughtAndUncaught(true, true);
			}
		}
	}
	
	/**
	 * @see IJavaExceptionBreakpoint#isCaught()
	 */
	public boolean isCaught() throws CoreException {
		return ensureMarker().getAttribute(CAUGHT, false);
	}
	
	/**
	 * @see IJavaExceptionBreakpoint#setCaught(boolean)
	 */
	public void setCaught(boolean caught) throws CoreException {
		if (caught == isCaught()) {
			return;
		}
		ensureMarker().setAttribute(CAUGHT, caught);
		if (caught && !isEnabled()) {
			setEnabled(true);
		} else if (!(caught || isUncaught())) {
			setEnabled(false);
		}
	}
	
	/**
	 * @see IJavaExceptionBreakpoint#isUncaught()
	 */
	public boolean isUncaught() throws CoreException {
		return ensureMarker().getAttribute(UNCAUGHT, false);
	}	
	
	/**
	 * @see IJavaExceptionBreakpoint#setUncaught(boolean)
	 */
	public void setUncaught(boolean uncaught) throws CoreException {
	
		if (uncaught == isUncaught()) {
			return;
		}
		ensureMarker().setAttribute(UNCAUGHT, uncaught);
		if (uncaught && !isEnabled()) {
			setEnabled(true);
		} else if (!(uncaught || isCaught())) {
			setEnabled(false);
		}
	}
	
	/**
	 * @see IJavaExceptionBreakpoint#isChecked()
	 */
	public boolean isChecked() throws CoreException {
		return ensureMarker().getAttribute(CHECKED, false);
	}
	
	/**
	 * Update the hit count of an <code>EventRequest</code>. Return a new request with
	 * the appropriate settings.
	 */
	protected EventRequest updateHitCount(EventRequest request, JDIDebugTarget target) throws CoreException {		
		// if the hit count has changed, or the request has expired and is being re-enabled,
		// create a new request
		if (hasHitCountChanged(request) || (isExpired(request) && isEnabled())) {
			request= createUpdatedExceptionRequest(target, (ExceptionRequest)request);
		}
		return request;
	}	
	
	/**
	 * @see JavaBreakpoint#updateRequest(EventRequest, JDIDebugTarget)
	 */
	protected void updateRequest(EventRequest request, JDIDebugTarget target) throws CoreException {
		updateEnabledState(request);
		EventRequest newRequest = updateHitCount(request, target);
		newRequest= updateCaughtState(newRequest,target);
		if (newRequest != null && newRequest != request) {
			replaceRequest(target, request, newRequest);
			request = newRequest;
		}
	}
	
	/**
	 * Return a request that will suspend execution when a caught and/or uncaught
	 * exception is thrown as is appropriate for the current state of this breakpoint.
	 */
	protected EventRequest updateCaughtState(EventRequest req, JDIDebugTarget target) throws CoreException  {
		if(!(req instanceof ExceptionRequest)) {
			return req;
		}
		ExceptionRequest request= (ExceptionRequest) req;
		if (request.notifyCaught() != isCaught() || request.notifyUncaught() != isUncaught()) {
			request= createUpdatedExceptionRequest(target, (ExceptionRequest)request);
		}
		return request;
	}
	
	/**
	 * Create a request that reflects the current state of this breakpoint.
	 * The new request will be installed in the same type as the given
	 * request.
	 */
	protected ExceptionRequest createUpdatedExceptionRequest(JDIDebugTarget target, ExceptionRequest request) throws CoreException{
		try {
			ReferenceType exClass = ((ExceptionRequest)request).exception();				
			request = (ExceptionRequest) newRequest(target, exClass);
		} catch (VMDisconnectedException e) {
			if (target.isTerminated() || target.isDisconnected()) {
				return request;
			}
			JDIDebugPlugin.logError(e);
		} catch (RuntimeException e) {
			JDIDebugPlugin.logError(e);
		}
		return request;
	}	
}

