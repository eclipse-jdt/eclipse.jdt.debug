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
	
	/**
	 * Exception breakpoint attribute storing the suspend on caught value
	 * (value <code>"caught"</code>). This attribute is stored as a <code>boolean</code>.
	 * When this attribute is <code>true</code>, a caught exception of the associated
	 * type will cause excecution to suspend .
	 */
	public static final String CAUGHT = "caught"; //$NON-NLS-1$
	
	// Attribute strings
	protected static final String[] fgExceptionBreakpointAttributes= new String[]{IJavaDebugConstants.CHECKED, IJavaDebugConstants.TYPE_HANDLE};	
	
	static String fMarkerType= IJavaDebugConstants.JAVA_EXCEPTION_BREAKPOINT;	
	
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
	 * @return an exception breakpoint
	 * @exception DebugException if unable to create the breakpoint marker due
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
				fMarker= resource.createMarker(fMarkerType);
				// configure the standard attributes
				setEnabled(true);
				// configure caught, uncaught, checked, and the type attributes
				setDefaultCaughtAndUncaught();
				configureExceptionBreakpoint(checked, exception);

				// configure the marker as a Java marker
				IMarker marker = ensureMarker();
				Map attributes= marker.getAttributes();
				JavaCore.addJavaElementMarkerAttributes(attributes, exception);
				marker.setAttributes(attributes);
				
				// Lastly, add the breakpoint manager
				addToBreakpointManager();				
			}

		};
		run(wr);
	}
	
	/**
	 * Sets the <code>CAUGHT</code>, <code>UNCAUGHT</code>, <code>CHECKED</code> and 
	 * <code>TYPE_HANDLE</code> attributes of the given exception breakpoint.
	 */
	public void configureExceptionBreakpoint(boolean checked, IType exception) throws CoreException {
		String handle = exception.getHandleIdentifier();
		Object[] values= new Object[]{new Boolean(checked), handle};
		ensureMarker().setAttributes(fgExceptionBreakpointAttributes, values);
	}
			
	public void setDefaultCaughtAndUncaught() throws CoreException {
		Object[] values= new Object[]{Boolean.TRUE, Boolean.TRUE};
		String[] attributes= new String[]{CAUGHT, IJavaDebugConstants.UNCAUGHT};
		ensureMarker().setAttributes(attributes, values);
	}
	
	/**
	 * @see JavaBreakpoint#installIn(JDIDebugTarget)
	 */
	public void addToTarget(JDIDebugTarget target) throws CoreException {
		
		IType exceptionType = getType();
		if (exceptionType == null) {
//			internalError(ERROR_BREAKPOINT_NO_TYPE);
			return;
		}
		String exceptionName = exceptionType.getFullyQualifiedName();
		String topLevelName = getTopLevelTypeName();
		if (topLevelName == null) {
//			internalError(ERROR_BREAKPOINT_NO_TYPE);
			return;
		}

		// listen to class loads
		registerRequest(target, target.createClassPrepareRequest(topLevelName));
		
		if (isCaught() || isUncaught()) {			
			List classes= target.jdiClassesByName(exceptionName);
			if (!classes.isEmpty()) {
				Iterator iter = classes.iterator();
				while (iter.hasNext()) {
					ReferenceType exClass = (ReferenceType)iter.next();				
					createRequest(target, exClass);
				}
			}
		}	
	}	
	
	protected ExceptionRequest newRequest(JDIDebugTarget target, ReferenceType type) throws CoreException {
		if (!isCaught() && !isUncaught()) {
			return null;
		}
			ExceptionRequest request= null;
			try {
				request= target.getEventRequestManager().createExceptionRequest(type, isCaught(), isUncaught());
				request.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
				request.putProperty(JAVA_BREAKPOINT_PROPERTY, this);
				int hitCount= getHitCount();
				if (hitCount > 0) {
					request.addCountFilter(hitCount);
					request.putProperty(IJavaDebugConstants.HIT_COUNT, new Integer(hitCount));
					request.putProperty(IJavaDebugConstants.EXPIRED, Boolean.FALSE);
				}
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
			request.setEnabled(isEnabled());	
			return request;
	}
	
	protected void createRequest(JDIDebugTarget target, ReferenceType type)  throws CoreException {
			ExceptionRequest request= newRequest(target, type);
			registerRequest(target, request);
	}
			
	/**
	 * @see JavaBreakpoint#isSupportedBy(VirtualMachine)
	 */
	public boolean isSupportedBy(VirtualMachine vm) {
		return true;
	}
	
	/**
	 * Enable this exception breakpoint.
	 * 
	 * If the exception breakpoint is not catching caught or uncaught,
	 * set the default values. If this isn't done, the resulting
	 * state (enabled with caught and uncaught both disabled)
	 * is ambiguous.
	 */
	public void setEnabled(boolean enabled) throws CoreException {
		super.setEnabled(enabled);
		if (isEnabled()) {
			if (!(isCaught() || isUncaught())) {
				setDefaultCaughtAndUncaught();
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
		return ensureMarker().getAttribute(IJavaDebugConstants.UNCAUGHT, false);
	}	
	
	/**
	 * @see IJavaExceptionBreakpoint#setUncaught(boolean)
	 */
	public void setUncaught(boolean uncaught) throws CoreException {
	
		if (uncaught == isUncaught()) {
			return;
		}
		ensureMarker().setAttribute(IJavaDebugConstants.UNCAUGHT, uncaught);
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
		return ensureMarker().getAttribute(IJavaDebugConstants.CHECKED, false);
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
	
	protected EventRequest updateCaughtState(EventRequest req, JDIDebugTarget target) throws CoreException  {
		if(!(req instanceof ExceptionRequest)) {
			return req;
		}
		ExceptionRequest request= (ExceptionRequest)req;
		
		if (request.notifyCaught() != isCaught() || request.notifyUncaught() != isUncaught()) {
			request= createUpdatedExceptionRequest(target, (ExceptionRequest)request);
		}
		return request;
	}
	
	protected ExceptionRequest createUpdatedExceptionRequest(JDIDebugTarget target, ExceptionRequest request) throws CoreException{
		try {
			// delete old request
			//on JDK you cannot delete (disable) an event request that has hit its count filter
			if (!isExpired(request)) {
				target.getEventRequestManager().deleteEventRequest(request); // disable & remove
			}
			ReferenceType exClass = ((ExceptionRequest)request).exception();				
			request = newRequest(target, exClass);
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
}

