package org.eclipse.jdt.internal.debug.core.breakpoints;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jdt.debug.core.IJavaExceptionBreakpoint;
import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;
import org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget;

import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.ExceptionEvent;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.ExceptionRequest;

public class JavaExceptionBreakpoint extends JavaBreakpoint implements IJavaExceptionBreakpoint {

	private static final String JAVA_EXCEPTION_BREAKPOINT= "org.eclipse.jdt.debug.javaExceptionBreakpointMarker"; //$NON-NLS-1$
	
	/**
	 * Exception breakpoint attribute storing the suspend on caught value
	 * (value <code>"org.eclipse.jdt.debug.core.caught"</code>). This attribute is stored as a <code>boolean</code>.
	 * When this attribute is <code>true</code>, a caught exception of the associated
	 * type will cause excecution to suspend .
	 */
	protected static final String CAUGHT = "org.eclipse.jdt.debug.core.caught"; //$NON-NLS-1$
	/**
	 * Exception breakpoint attribute storing the suspend on uncaught value
	 * (value <code>"org.eclipse.jdt.debug.core.uncaught"</code>). This attribute is stored as a
	 * <code>boolean</code>. When this attribute is <code>true</code>, an uncaught
	 * exception of the associated type will cause excecution to suspend.
	 */
	protected static final String UNCAUGHT = "org.eclipse.jdt.debug.core.uncaught"; //$NON-NLS-1$	
	/**
	 * Exception breakpoint attribute storing the checked value (value <code>"org.eclipse.jdt.debug.core.checked"</code>).
	 * This attribute is stored as a <code>boolean</code>, indicating whether an
	 * exception is a checked exception.
	 */
	protected static final String CHECKED = "org.eclipse.jdt.debug.core.checked"; //$NON-NLS-1$	
	
	/**
	 * Name of the exception that was actually hit (could be a
	 * subtype of the type that is being caught).
	 */
	protected String fExceptionName = null;
	
	public JavaExceptionBreakpoint() {
	}
	
	/**
	 * Creates and returns an exception breakpoint for the
	 * given (throwable) type. Caught and uncaught specify where the exception
	 * should cause thread suspensions - that is, in caught and/or uncaught locations.
	 * Checked indicates if the given exception is a checked exception.
	 * @param resource the resource on which to create the associated
	 *  breakpoint marker 
	 * @param exceptionName the fully qualified name of the exception for
	 *  which to create the breakpoint
	 * @param caught whether to suspend in caught locations
	 * @param uncaught whether to suspend in uncaught locations
 	 * @param checked whether the exception is a checked exception
 	 * @param add whether to add this breakpoint to the breakpoint manager
	 * @return a Java exception breakpoint
	 * @exception DebugException if unable to create the associated marker due
	 *  to a lower level exception.
	 */	
	public JavaExceptionBreakpoint(final IResource resource, final String exceptionName, final boolean caught, final boolean uncaught, final boolean checked, final boolean add, final Map attributes) throws DebugException {
		IWorkspaceRunnable wr= new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {				
				// create the marker
				setMarker(resource.createMarker(JAVA_EXCEPTION_BREAKPOINT));
				
				// add attributes
				attributes.put(IBreakpoint.ID, getModelIdentifier());
				attributes.put(TYPE_NAME, exceptionName);
				attributes.put(ENABLED, new Boolean(true));
				attributes.put(CAUGHT, new Boolean(caught));
				attributes.put(UNCAUGHT, new Boolean(uncaught));
				attributes.put(CHECKED, new Boolean(checked));
				
				ensureMarker().setAttributes(attributes);
				
				register(add);
			}

		};
		run(wr);

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
				configureRequest(request, target);
			} catch (VMDisconnectedException e) {
				if (!target.isAvailable()) {
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
			request= (ExceptionRequest)recreateRequest(request, target);
		}
		return request;
	}
	
	/**
	 * @see JavaBreakpoint#recreateRequest(EventRequest, JDIDebugTarget)
	 */
	protected EventRequest recreateRequest(EventRequest request, JDIDebugTarget target) throws CoreException{
		try {
			ReferenceType exClass = ((ExceptionRequest)request).exception();				
			request = newRequest(target, exClass);
		} catch (VMDisconnectedException e) {
			if (!target.isAvailable()) {
				return request;
			}
			JDIDebugPlugin.logError(e);
		} catch (RuntimeException e) {
			JDIDebugPlugin.logError(e);
		}
		return request;
	}
	
	/**
	 * @see JavaBreakpoint#setRequestThreadFilter(EventRequest)
	 */
	protected void setRequestThreadFilter(EventRequest request, ThreadReference thread) {
		((ExceptionRequest)request).addThreadFilter(thread);
	}
	
	/**
	 * @see IJDIEventListener#handleEvent(Event)
	 */
	public boolean handleEvent(Event event, JDIDebugTarget target) {
		if (event instanceof ExceptionEvent) {
			setExceptionName(((ExceptionEvent)event).exception().type().name());
		}	
		return super.handleEvent(event, target);
	}	
	
	/**
	 * Sets the name of the exception that was last hit
	 * 
	 * @param name fully qualified exception name
	 */
	protected void setExceptionName(String name) {
		fExceptionName = name;
	}
	
	/**
	 * @see IJavaExceptionBreakpoint#getExceptionTypeName()
	 */
	public String getExceptionTypeName() {
		return fExceptionName;
	}

}

