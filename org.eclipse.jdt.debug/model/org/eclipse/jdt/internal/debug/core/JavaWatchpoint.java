package org.eclipse.jdt.internal.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IWorkingCopy;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.debug.core.IJavaWatchpoint;

import com.sun.jdi.Field;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.AccessWatchpointEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.ModificationWatchpointEvent;
import com.sun.jdi.request.AccessWatchpointRequest;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.ModificationWatchpointRequest;
import com.sun.jdi.request.WatchpointRequest;

public class JavaWatchpoint extends JavaLineBreakpoint implements IJavaWatchpoint {
	
	private static final String JAVA_WATCHPOINT= "org.eclipse.jdt.debug.javaWatchpointMarker"; //$NON-NLS-1$
	/**
	 * Watchpoint attribute storing the access value (value <code>"access"</code>).
	 * This attribute is stored as a <code>boolean</code>, indicating whether a
	 * watchpoint is an access watchpoint.
	 */
	protected static final String ACCESS= "access"; //$NON-NLS-1$
	/**
	 * Watchpoint attribute storing the modification value (value <code>"modification"</code>).
	 * This attribute is stored as a <code>boolean</code>, indicating whether a
	 * watchpoint is a modification watchpoint.
	 */
	protected static final String MODIFICATION= "modification"; //$NON-NLS-1$	
	/**
	 * Watchpoint attribute storing the auto_disabled value (value <code>"auto_disabled"</code>).
	 * This attribute is stored as a <code>boolean</code>, indicating whether a
	 * watchpoint has been auto-disabled (as opposed to being disabled explicitly by the user)
	 */
	protected static final String AUTO_DISABLED="auto_disabled"; //$NON-NLS-1$
	/**
	 * Breakpoint attribute storing the handle identifier of the Java element
	 * corresponding to the field on which a breakpoint is set
	 * (value <code>"fieldHandle"</code>). This attribute is a <code>String</code>.
	 */
	protected static final String FIELD_HANDLE= "fieldHandle"; //$NON-NLS-1$		
	/**
	 * Flag indicating that this breakpoint last suspended execution
	 * due to a field access
	 */
	protected static final Integer ACCESS_EVENT= new Integer(0); // $NON-NLS-1$
	/**
	 * Flag indicating that this breakpoint last suspended execution
	 * due to a field modification
	 */	
	protected static final Integer MODIFICATION_EVENT= new Integer(1); // $NON-NLS-1$
	/**
	 * Maps each debug target that is suspended for this breakpiont to reason that 
	 * this breakpoint suspended it. Reasons include:
	 * <ol>
	 * <li>Field access (value <code>ACCESS_EVENT</code>)</li>
	 * <li>Field modification (value <code>MODIFICATION_EVENT</code>)</li>
	 * </ol>
	 */
	private HashMap fLastEventTypes= new HashMap(10); // $NON-NLS-1$
	
	public JavaWatchpoint() {
	}

	/**
	 * Creates and returns a watchpoint on the
	 * given field.
	 * If hitCount > 0, the breakpoint will suspend execution when it is
	 * "hit" the specified number of times.
	 * 
	 * @param field the field on which to suspend (on access or modification)
	 * @param hitCount the number of times the breakpoint will be hit before
	 * 	suspending execution - 0 if it should always suspend
	 * @return a watchpoint
	 * @exception DebugException if unable to create the breakpoint marker due
	 * 	to a lower level exception
	 */
	public JavaWatchpoint(final IField field, final int hitCount) throws DebugException {
		IWorkspaceRunnable wr= new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {		
				IResource resource = null;
				ICompilationUnit compilationUnit = getCompilationUnit(field);
				if (compilationUnit != null) {
					resource = compilationUnit.getUnderlyingResource();
				}
				if (resource == null) {
					resource = field.getJavaProject().getProject();
				}
				
				setMarker(resource.createMarker(JAVA_WATCHPOINT));
				
				// configure the standard attributes
				setStandardAttributes(field);
				// configure the type handle and hit count
				setTypeAndHitCount(field.getDeclaringType(), hitCount);
				// configure the field handle
				setField(field);
				// configure the access and modification flags to defaults
				setDefaultAccessAndModification();
				setAutoDisabled(false);				
				
				
				// configure the marker as a Java marker
				IMarker marker = ensureMarker();
				Map attributes= marker.getAttributes();
				JavaCore.addJavaElementMarkerAttributes(attributes, field);
				marker.setAttributes(attributes);
				
				// Lastly, add the breakpoint manager
				addToBreakpointManager();
			}
		};
		run(wr);
	}

	/**
	 * @see JavaBreakpoint#createRequest(JDIDebugTarget, ReferenceType)
	 * 
	 * Creates and installs an access and modification watchpoint request
	 * in the given reference type, configuring the requests as appropriate
	 * for this watchpoint. The requests are then enabled based on whether
	 * this watchpoint is an access watchpoint, modification watchpoint, or
	 * both. Finally, the requests are registered with the given target.
	 */	
	protected boolean createRequest(JDIDebugTarget target, ReferenceType type) throws CoreException {
		IField javaField= getField();
		Field field= null;
		
		field= type.fieldByName(javaField.getElementName());
		if (field == null) {
			// error
			return false;
		}
		AccessWatchpointRequest accessRequest= null;
		ModificationWatchpointRequest modificationRequest= null;			
		if (accessSupportedBy(target.getVM())) {
			accessRequest= createAccessWatchpoint(target, field);
			registerRequest(accessRequest, target);
		} else {
			notSupported(JDIDebugModelMessages.getString("JavaWatchpoint.no_access_watchpoints"));				 //$NON-NLS-1$
		}
		if (modificationSupportedBy(target.getVM())) {
			modificationRequest= createModificationWatchpoint(target, field);
			if (modificationRequest == null) {
				return false;
			} 
			registerRequest(modificationRequest, target);
			return true;
		} else {
			notSupported(JDIDebugModelMessages.getString("JavaWatchpoint.no_modification_watchpoints")); //$NON-NLS-1$
		}
		return false;
	}
	
	/**
	 * Returns whether the given virtual machine supports modification watchpoints
	 */
	protected boolean modificationSupportedBy(VirtualMachine vm) {
		return vm.canWatchFieldModification();
	}
	
	/**
	 * Returns whether the given virtual machine supports access watchpoints
	 */
	protected boolean accessSupportedBy(VirtualMachine vm) {
		return vm.canWatchFieldAccess();
	}
	
	/**
	 * Either access or modification watchpoints are not supported. Throw an appropriate exception.
	 * 
	 * @param message the message that states that access or modification watchpoints
	 *  are not supported
	 */
	protected void notSupported(String message) throws DebugException {
		throw new DebugException(new Status(IStatus.ERROR, DebugPlugin.getDefault().getDescriptor().getUniqueIdentifier(), 
			DebugException.NOT_SUPPORTED, message, null)); //$NON-NLS-1$		
	}
	
	/**
	 * Create an access watchpoint for the given breakpoint and associated field
	 */
	protected AccessWatchpointRequest createAccessWatchpoint(JDIDebugTarget target, Field field) throws CoreException {
		return (AccessWatchpointRequest) createWatchpoint(target, field, true);
	}
	
	/**
	 * Create a modification watchpoint for the given breakpoint and associated field
	 */
	protected ModificationWatchpointRequest createModificationWatchpoint(JDIDebugTarget target, Field field) throws CoreException {
		return (ModificationWatchpointRequest) createWatchpoint(target, field, false);		
	}	
	
	/**
	 * Create a watchpoint for the given breakpoint and associated field.
	 * 
	 * @param target the target in which the request will be installed
	 * @param field the field on which the request will be set
	 * @param access <code>true</code> if an access watchpoint will be 
	 *  created. <code>false</code> if a modification watchpoint will
	 *  be created.
	 * 
	 * @return an WatchpointRequest (AccessWatchpointRequest if access is
	 *  <code>true</code>; ModificationWatchpointRequest if access is <code>false</code>).
	 */
	protected WatchpointRequest createWatchpoint(JDIDebugTarget target, Field field, boolean access) throws CoreException {
		WatchpointRequest request= null;
			try {
				if (access) {
					request= target.getEventRequestManager().createAccessWatchpointRequest(field);
				} else {
					request= target.getEventRequestManager().createModificationWatchpointRequest(field);
				}
				configureRequest(request);
			} catch (VMDisconnectedException e) {
				if (target.isTerminated() || target.isDisconnected()) {
					return null;
				}
				target.internalError(e);
				return null;
			} catch (RuntimeException e) {
				target.internalError(e);
				return null;
			}
		return request;
	}

	/**
	 * @see JavaBreakpoint#updateHitCount(EventRequest, JDIDebugTarget)
	 */
	protected EventRequest updateHitCount(EventRequest request, JDIDebugTarget target) throws CoreException {		
		
		// if the hit count has changed, or the request has expired and is being re-enabled,
		// create a new request
		if (hasHitCountChanged(request) || (isExpired(request) && this.isEnabled())) {
			try {	
				Field field= ((WatchpointRequest) request).field();
				if (request instanceof AccessWatchpointRequest) {
					request= createAccessWatchpoint(target, field);
				} else if (request instanceof ModificationWatchpointRequest) {
					request= createModificationWatchpoint(target, field);
				}
			} catch (VMDisconnectedException e) {
				if (target.isTerminated() || target.isDisconnected()) {
					return request;
				}
				target.internalError(e);
				return request;
			} catch (RuntimeException e) {
				JDIDebugPlugin.logError(e);
			}
		}
		return request;
	}

	/**
	 * @see IBreakpoint#setEnabled(boolean)
	 * 
	 * If the watchpoint is not watching access or modification,
	 * set the default values. If this isn't done, the resulting
	 * state (enabled with access and modification both disabled)
	 * is ambiguous.
	 */
	public void setEnabled(boolean enabled) throws CoreException {
		super.setEnabled(enabled);
		if (isEnabled()) {
			if (!(isAccess() || isModification())) {
				setDefaultAccessAndModification();
			}
		}
	}
	
	/**
	 * @see IJavaWatchpoint#isAccess()
	 */
	public boolean isAccess() throws CoreException {
		return ensureMarker().getAttribute(ACCESS, false);
	}
	
	/**
	 * @see IJavaWatchpoint#setAccess(boolean)
	 */
	public void setAccess(boolean access) throws CoreException {
		if (access == isAccess()) {
			return;
		}		
		ensureMarker().setAttribute(ACCESS, access);
		if (access && !isEnabled()) {
			setEnabled(true);
		} else if (!(access || isModification())) {
			setEnabled(false);
		}
	}
	
	/**
	 * @see IJavaWatchpoint#isModification()
	 */	
	public boolean isModification() throws CoreException {
		return ensureMarker().getAttribute(MODIFICATION, false);
	}
	
	/**
	 * @see IJavaWatchpoint#setModification(boolean)
	 */
	public void setModification(boolean modification) throws CoreException {
		if (modification == isModification()) {
			return;
		}
		ensureMarker().setAttribute(MODIFICATION, modification);
		if (modification && !isEnabled()) {
			setEnabled(true);
		} else if (!(modification || isAccess())) {
			setEnabled(false);
		}
	}
		
	/**
	 * Sets the default access and modification attributes of the watchpoint.
	 * The default values are:
	 * <ul>
	 * <li>access = <code>false</code>
	 * <li>modification = <code>true</code>
	 * <ul>
	 */
	protected void setDefaultAccessAndModification() throws CoreException {
		Object[] values= new Object[]{Boolean.FALSE, Boolean.TRUE};
		String[] attributes= new String[]{ACCESS, MODIFICATION};
		ensureMarker().setAttributes(attributes, values);
	}

	/**
	 * Sets the field on which this watchpoint is installed
	 */
	protected void setField(IField field) throws CoreException {
		String handle = field.getHandleIdentifier();
		ensureMarker().setAttribute(FIELD_HANDLE, handle);
	}
	
	/**
	 * Set standard attributes of a watchpoint based on the
	 * given field.
	 */
	protected void setStandardAttributes(IField field) throws CoreException {
		// find the name range if available
		int start = -1;
		int stop = -1;
		
		ISourceRange range = field.getNameRange();
		if (range != null) {
			start = range.getOffset();
			stop = start + range.getLength() - 1;
		}
		super.setLineBreakpointAttributes(getModelIdentifier(), true, -1, start, stop);
	}		

	/**
	 * Sets the whether this watchpoint has been automatically disabled
	 * (because modification and access were both set <code>false</code>).
	 */
	protected void setAutoDisabled(boolean autoDisabled) throws CoreException {
		ensureMarker().setAttribute(AUTO_DISABLED, autoDisabled);
	}

	/**
	 * Returns the underlying compilation unit of an element.
	 */
	public static ICompilationUnit getCompilationUnit(IJavaElement element) {
		if (element instanceof IWorkingCopy) {
			IWorkingCopy copy= (IWorkingCopy) element;
			if (copy.isWorkingCopy()) {
				return (ICompilationUnit)copy.getOriginalElement();
			}
		}
		if (element instanceof ICompilationUnit) {
			return (ICompilationUnit) element;
		}		
		IJavaElement parent = element.getParent();
		if (parent != null) {
			return getCompilationUnit(parent);
		}
		return null;
	}
	
	/**
	 * @see IJavaWatchpoint#getField()
	 */
	public IField getField() throws CoreException {
		String handle= (String) ensureMarker().getAttribute(FIELD_HANDLE);;
		if (handle != null && handle != "") { //$NON-NLS-1$
			return (IField)JavaCore.create(handle);
		}
		return null;
	}
	
	/**
	 * Store the type of the event, then handle it as specified in
	 * the superclass. This is useful for correctly generating the
	 * thread text when asked (assumes thread text is requested after
	 * the event is passed to this breakpoint.
	 * 
	 * Also, @see JavaBreakpoint#handleEvent(Event)
	 */
	public boolean handleEvent(Event event, JDIDebugTarget target)  {
		if (event instanceof AccessWatchpointEvent) {
			fLastEventTypes.put(target, ACCESS_EVENT);
		} else if (event instanceof ModificationWatchpointEvent) {
			fLastEventTypes.put(target, MODIFICATION_EVENT);
		}
		return super.handleEvent(event, target);
	}	
	
	
	/**
	 * @see JavaBreakpoint#updateEnabledState(EventRequest)
	 */
	protected void updateEnabledState(EventRequest request) throws CoreException  {
		boolean enabled = isEnabled();
		if (request instanceof AccessWatchpointRequest) {
			if (isAccess()) {
				if (enabled != request.isEnabled()) {
					internalUpdateEnabeldState(request, enabled);
				}
			} else {
				if (request.isEnabled()) {
					internalUpdateEnabeldState(request, false);
				}
			}
		}
		if (request instanceof ModificationWatchpointRequest) {
			if (isModification()) {
				if (enabled != request.isEnabled()) {
					internalUpdateEnabeldState(request, enabled);
				}
			} else {
				if (request.isEnabled()) {
					internalUpdateEnabeldState(request, false);
				}
			}
		}
	}
	
	/**
	 * Set the enabled state of the given request to the given
	 * value
	 */
	protected void internalUpdateEnabeldState(EventRequest request, boolean enabled) {
		// change the enabled state
		try {
			// if the request has expired, do not enable/disable.
			// Requests that have expired cannot be deleted.
			if (!isExpired(request)) {
				request.setEnabled(enabled);
			}
		} catch (VMDisconnectedException e) {
		} catch (RuntimeException e) {
			JDIDebugPlugin.logError(e);
		}
	}
	
	/**
	 * @see IJavaWatchpoint#isAccessSuspend(IDebugTarget)
	 */
	public boolean isAccessSuspend(IDebugTarget target) {
		Integer lastEventType= (Integer) fLastEventTypes.get(target);
		if (lastEventType == null) {
			return false;
		}
		return lastEventType.equals(ACCESS_EVENT);
	}
}