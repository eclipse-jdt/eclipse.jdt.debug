package org.eclipse.jdt.internal.debug.core;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.IDebugConstants;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.debug.core.IJavaDebugConstants;
import org.eclipse.jdt.debug.core.IJavaExceptionBreakpoint;

import com.sun.jdi.*;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.ExceptionEvent;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.ExceptionRequest;

public class JavaExceptionBreakpoint extends JavaBreakpoint implements IJavaExceptionBreakpoint {
	
	// Thread label String keys
	private static final String EXCEPTION_SYS= THREAD_LABEL + "exception_sys";
	private static final String EXCEPTION_USR= THREAD_LABEL + "exception_usr";
	// Marker label String keys
	protected final static String EXCEPTION= MARKER_LABEL + "exception.";
	protected final static String FORMAT= EXCEPTION + "format";
	protected final static String CAUGHT= EXCEPTION + "caught";
	protected final static String UNCAUGHT= EXCEPTION + "uncaught";
	protected final static String BOTH= EXCEPTION + "both";	
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
				configure(getPluginIdentifier(), true);
				// configure caught, uncaught, checked, and the type attributes
				setDefaultCaughtAndUncaught();
				configureExceptionBreakpoint(checked, exception);

				// configure the marker as a Java marker
				Map attributes= getAttributes();
				JavaCore.addJavaElementMarkerAttributes(attributes, exception);
				setAttributes(attributes);
				
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
		setAttributes(fgExceptionBreakpointAttributes, values);
	}	
	
	public void setDefaultCaughtAndUncaught() {
		Object[] values= new Object[]{Boolean.TRUE, Boolean.TRUE};
		String[] attributes= new String[]{IJavaDebugConstants.CAUGHT, IJavaDebugConstants.UNCAUGHT};
		try {
			setAttributes(attributes, values);
		} catch (CoreException ce) {
			logError(ce);
		}		
	}
	
	/**
	 * @see JavaBreakpoint#installIn(JDIDebugTarget)
	 */
	public void addToTarget(JDIDebugTarget target) {
		changeForTarget(target);
	}	
	
	/**
	 * An exception breakpoint has changed
	 */
	public void changeForTarget(JDIDebugTarget target) {

		boolean caught= isCaught();
		boolean uncaught= isUncaught();

		if (caught || uncaught) {
			IType exceptionType = getType();
			if (exceptionType == null) {
//				internalError(ERROR_BREAKPOINT_NO_TYPE);
				return;
			}
			String exceptionName = exceptionType.getFullyQualifiedName();
			String topLevelName = getTopLevelTypeName();
			if (topLevelName == null) {
//				internalError(ERROR_BREAKPOINT_NO_TYPE);
				return;
			}
			List classes= target.jdiClassesByName(exceptionName);
			ReferenceType exClass= null;
			if (classes != null && !classes.isEmpty()) {
				exClass= (ReferenceType) classes.get(0);
			}
			if (exClass == null) {
				// defer the exception
				target.defer(this, topLevelName);
			} else {
				// new or changed - first delete the old request
				if (null != target.getRequest(this))
					removeFromTarget(target);
				ExceptionRequest request= null;
				try {
					request= target.getEventRequestManager().createExceptionRequest(exClass, caught, uncaught);
					request.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
					request.putProperty(IDebugConstants.BREAKPOINT, this);
				} catch (VMDisconnectedException e) {
					return;
				} catch (RuntimeException e) {
					logError(e);
					return;
				}
				request.setEnabled(isEnabled());
				target.installBreakpoint(this, request);
			}
		} else {
			removeFromTarget(target);
		}
	}

	/**
	 * An exception breakpoint has been removed
	 */
	public void removeFromTarget(JDIDebugTarget target) {
		IType type = getType();
		if (type == null) {
//			internalError(ERROR_BREAKPOINT_NO_TYPE);
			return;
		}
		String name = type.getFullyQualifiedName();
		ExceptionRequest request= (ExceptionRequest) target.uninstallBreakpoint(this);
		if (request != null) {
			try {
				target.getEventRequestManager().deleteEventRequest(request);
			} catch (VMDisconnectedException e) {
				return;
			} catch (RuntimeException e) {
				logError(e);
				return;
			}
		}
		List deferred = target.getDeferredBreakpointsByClass(name);
		if (deferred != null)  {
			deferred.remove(this);
			if (deferred.isEmpty()) {
				target.removeDeferredBreakpointByClass(name);
			}
		}
	}		
	
	/**
	 * @see JavaBreakpoint#handleEvent(Event)
	 */
	public void handleEvent(Event event, JDIDebugTarget target){
		if (!(event instanceof ExceptionEvent)) {
			return;
		}
		ThreadReference threadRef= ((ExceptionEvent)event).thread();
		JDIThread thread= target.findThread(threadRef);
		if (thread == null) {
			target.resume(threadRef);
			return;
		} else {
			thread.handleSuspendForBreakpoint(this);
		}
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
	public boolean isCaught() {
		return getBooleanAttribute(IJavaDebugConstants.CAUGHT);
	}
	
	/**
	 * @see IJavaExceptionBreakpoint#setCaught(boolean)
	 */
	public void setCaught(boolean caught) throws CoreException {
		if (caught == isCaught()) {
			return;
		}
		try {
			setBooleanAttribute(IJavaDebugConstants.CAUGHT, caught);
			if (caught && !isEnabled()) {
				setEnabled(true);
			} else if (!(caught || isUncaught())) {
				setEnabled(false);
			}
		} catch (CoreException ce) {
			logError(ce);
		}			
	}
	
	/**
	 * @see IJavaExceptionBreakpoint#isUncaught()
	 */
	public boolean isUncaught() {
		return getBooleanAttribute(IJavaDebugConstants.UNCAUGHT);
	}	
	
	/**
	 * @see IJavaExceptionBreakpoint#setUncaught(boolean)
	 */
	public void setUncaught(boolean uncaught) throws CoreException {
	
		if (uncaught == isUncaught()) {
			return;
		}
		try {
			setBooleanAttribute(IJavaDebugConstants.UNCAUGHT, uncaught);
			if (uncaught && !isEnabled()) {
				setEnabled(true);
			} else if (!(uncaught || isCaught())) {
				setEnabled(false);
			}
		} catch (CoreException ce) {
			logError(ce);
		}			
	}
	
	/**
	 * @see IJavaExceptionBreakpoint#isChecked()
	 */
	public boolean isChecked() {
		return getBooleanAttribute(IJavaDebugConstants.CHECKED);
	}
	
	/**
	 * @see IJavaExceptionBreakpoint#setChecked(boolean)
	 */
	public void setChecked(boolean checked) throws CoreException {
		if (checked != isChecked()) {
			setBooleanAttribute(IJavaDebugConstants.CHECKED, checked);	
		}
	}	
	
	/**
	 * @see JavaBreakpoint
	 */	
	public String getFormattedThreadText(String threadName, String typeName, boolean systemThread) {
		if (systemThread) {
			return getFormattedString(EXCEPTION_SYS, new String[] {threadName, typeName});
		}
		return getFormattedString(EXCEPTION_USR, new String[] {threadName, typeName});		
	}
	
	/**
	 */
	public String getMarkerText(boolean showQualified) {
		String name;
		if (showQualified) {
			name= getType().getFullyQualifiedName();
		} else {
			name= getType().getElementName();
		}

		String state= null;
		boolean c= isCaught();
		boolean u= isUncaught();
		if (c && u) {
			state= BOTH;
		} else if (c) {
			state= CAUGHT;
		} else if (u) {
			state= UNCAUGHT;
		}
		String label= null;
		if (state == null) {
			label= name;
		} else {
			String format= DebugJavaUtils.getResourceString(FORMAT);
			state= DebugJavaUtils.getResourceString(state);
			label= MessageFormat.format(format, new Object[] {state, name});
		}
		return label;	
	}

}

