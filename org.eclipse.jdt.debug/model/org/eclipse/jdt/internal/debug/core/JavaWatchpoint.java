package org.eclipse.jdt.internal.debug.core;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.internal.debug.core.IJavaDebugConstants;
import org.eclipse.jdt.debug.core.IJavaWatchpoint;

import com.sun.jdi.*;
import com.sun.jdi.event.*;
import com.sun.jdi.request.*;

public class JavaWatchpoint extends JavaLineBreakpoint implements IJavaWatchpoint {
	
	static String fMarkerType= IJavaDebugConstants.JAVA_WATCHPOINT;
	
	private final static int ACCESS_EVENT= 0;
	private final static int MODIFICATION_EVENT= 1;
	private int fLastEventType= -1;
	
	public JavaWatchpoint() {
	}

	/**
	 * Creates and returns a watchpoint on the
	 * given field.
	 * If hitCount > 0, the breakpoint will suspend execution when it is
	 * "hit" the specified number of times. Note: the breakpoint is not
	 * added to the breakpoint manager - it is merely created.
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
				
				if (fMarker == null) {
					// Only create a marker if one is not already assigned
					fMarker= resource.createMarker(fMarkerType);
				}
				
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
	 * A single watchpoint can create multiple requests. This method provides control over this
	 * property for explicitly choosing which requests (access, modification, or both) to 
	 * potentially add.
	 */	
	protected void createRequest(JDIDebugTarget target, ReferenceType type) throws CoreException {
				
		IField javaField= getField();
		Field field= null;
		
		field= type.fieldByName(javaField.getElementName());
		if (field == null) {
			// error
			return;
		}
		AccessWatchpointRequest accessRequest= null;
		ModificationWatchpointRequest modificationRequest= null;			
		if (accessSupportedBy(target.getVM())) {
			accessRequest= createAccessWatchpoint(target, field);
			registerRequest(target, accessRequest);
		} else {
			notSupported(JDIDebugModelMessages.getString("JavaWatchpoint.no_access_watchpoints"));				 //$NON-NLS-1$
		}
		if (modificationSupportedBy(target.getVM())) {
			modificationRequest= createModificationWatchpoint(target, field);
			registerRequest(target, modificationRequest);
		} else {
			notSupported(JDIDebugModelMessages.getString("JavaWatchpoint.no_modification_watchpoints")); //$NON-NLS-1$
		}

	}
	
	/**
	 * Returns whether this kind of breakpoint is supported by the given
	 * virtual machine. A watchpoint is supported if both access and
	 * modification watchpoints are supported.
	 */
	public boolean isSupportedBy(VirtualMachine vm) {
		return (modificationSupportedBy(vm) && accessSupportedBy(vm));
	}
	
	/**
	 * Returns whether the given virtual machine supports modification watchpoints
	 */
	public boolean modificationSupportedBy(VirtualMachine vm) {
		return vm.canWatchFieldModification();
	}
	
	/**
	 * Returns whether the given virtual machine supports access watchpoints
	 */
	public boolean accessSupportedBy(VirtualMachine vm) {
		return vm.canWatchFieldAccess();
	}
	
	/**
	 * This watchpoint is not supported for some reason. Alert the user.
	 */
	protected void notSupported(String message) {
	}
	
	/**
	 * Create an access watchpoint for the given breakpoint and associated field
	 */
	protected AccessWatchpointRequest createAccessWatchpoint(JDIDebugTarget target, Field field) throws CoreException {
		AccessWatchpointRequest request= null;
			try {
				request= target.getEventRequestManager().createAccessWatchpointRequest(field);
				configureRequest(request);
			} catch (VMDisconnectedException e) {
				return null;
			} catch (RuntimeException e) {
				target.internalError(e);
				return null;
			}
		return request;
	}	
		
	/**
	 * Create a modification watchpoint for the given breakpoint and associated field
	 */
	protected ModificationWatchpointRequest createModificationWatchpoint(JDIDebugTarget target, Field field) throws CoreException {
		ModificationWatchpointRequest request= null;
		try {
			request= target.getEventRequestManager().createModificationWatchpointRequest(field);
			configureRequest(request);
		} catch (VMDisconnectedException e) {
			return null;
		} catch (RuntimeException e) {
			target.internalError(e);
			return null;
		}
		return request;			
	}

	/**
	 * Update the hit count of an <code>EventRequest</code>. Return a new request with
	 * the appropriate settings.
	 */
	protected EventRequest updateHitCount(EventRequest request, JDIDebugTarget target) throws CoreException {		
		
		// if the hit count has changed, or the request has expired and is being re-enabled,
		// create a new request
		if (hasHitCountChanged(request) || (isExpired(request) && this.isEnabled())) {
			try {
				// delete old request
				//on JDK you cannot delete (disable) an event request that has hit its count filter
				if (!isExpired(request)) {
					target.getEventRequestManager().deleteEventRequest(request); // disable & remove
				}		
				Field field= ((WatchpointRequest) request).field();
				if (request instanceof AccessWatchpointRequest) {
					request= createAccessWatchpoint(target, field);
				} else if (request instanceof ModificationWatchpointRequest) {
					request= createModificationWatchpoint(target, field);
				}
			} catch (VMDisconnectedException e) {
			} catch (RuntimeException e) {
				JDIDebugPlugin.logError(e);
			}
		}
		return request;
	}

	/**
	 * Enable this watchpoint.
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
	 * @see IJavaWatchpoint#isAccess
	 */
	public boolean isAccess() throws CoreException {
		return ensureMarker().getAttribute(IJavaDebugConstants.ACCESS, false);
	}
	
	/**
	 * @see IJavaWatchpoint#setAccess
	 */
	public void setAccess(boolean access) throws CoreException {
		if (access == isAccess()) {
			return;
		}		
		ensureMarker().setAttribute(IJavaDebugConstants.ACCESS, access);
		if (access && !isEnabled()) {
			setEnabled(true);
		} else if (!(access || isModification())) {
			setEnabled(false);
		}
	}
	
	/**
	 * @see IJavaWatchpoint#isModification
	 */	
	public boolean isModification() throws CoreException {
		return ensureMarker().getAttribute(IJavaDebugConstants.MODIFICATION, false);
	}
	
	/**
	 * @see IJavaWatchpoint#setModification(boolean)
	 */
	public void setModification(boolean modification) throws CoreException {
		if (modification == isModification()) {
			return;
		}
		ensureMarker().setAttribute(IJavaDebugConstants.MODIFICATION, modification);
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
	private void setDefaultAccessAndModification() throws CoreException {
		Object[] values= new Object[]{Boolean.FALSE, Boolean.TRUE};
		String[] attributes= new String[]{IJavaDebugConstants.ACCESS, IJavaDebugConstants.MODIFICATION};
		ensureMarker().setAttributes(attributes, values);
	}

	/**
	 * Sets the <code>FIELD_HANDLE</code> attribute of the given breakpoint, associated
	 * with the given IField.
	 */
	public void setField(IField field) throws CoreException {
		String handle = field.getHandleIdentifier();
		setFieldHandleIdentifier(handle);
	}
	
	/**
	 * Sets the <code>FIELD_HANDLE</code> attribute of the given breakpoint.
	 */
	public void setFieldHandleIdentifier(String handle) throws CoreException {
		ensureMarker().setAttribute(IJavaDebugConstants.FIELD_HANDLE, handle);
	}
	
	/**
	 * Set standard attributes of a watchpoint
	 */
	public void setStandardAttributes(IField field) throws CoreException {
		// find the source range if available
		int start = -1;
		int stop = -1;
		ISourceRange range = field.getSourceRange();
		if (range != null) {
			start = range.getOffset();
			stop = start + range.getLength() - 1;
		}
		super.setLineBreakpointAttributes(getPluginIdentifier(), true, -1, start, stop);
	}		

	/**
	 * Sets the <code>AUTO_DISABLED</code> attribute of this watchpoint.
	 */
	private void setAutoDisabled(boolean autoDisabled) throws CoreException {
		ensureMarker().setAttribute(IJavaDebugConstants.AUTO_DISABLED, autoDisabled);
	}

	/**
	 * Returns the underlying compilation unit of an element.
	 */
	public static ICompilationUnit getCompilationUnit(IJavaElement element) {
		if (element instanceof IWorkingCopy) {
			return (ICompilationUnit) ((IWorkingCopy) element).getOriginalElement();
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
		String handle= getFieldHandleIdentifier();
		if (handle != null && handle != "") { //$NON-NLS-1$
			return (IField)JavaCore.create(handle);
		}
		return null;
	}		
		
	/**
	 * Returns the <code>FIELD_HANDLE</code> attribute of this watchpoint.
	 */
	public String getFieldHandleIdentifier() throws CoreException {
		return (String) ensureMarker().getAttribute(IJavaDebugConstants.FIELD_HANDLE);
	}
	
	/*
	public String getFormattedThreadText(String threadName, String typeName, boolean systemThread) {
		String fieldName= getField().getElementName();
		if (fLastEventType == ACCESS_EVENT) {
			if (systemThread) {
				return getFormattedString(ACCESS_SYS, new String[] {threadName, fieldName, typeName});
			} else {
				return getFormattedString(ACCESS_USR, new String[] {threadName, fieldName, typeName});
			}
		} else if (fLastEventType == MODIFICATION_EVENT) {
			// modification
			if (systemThread) {
				return getFormattedString(MODIFICATION_SYS, new String[] {threadName, fieldName, typeName});
			} else {
				return getFormattedString(MODIFICATION_USR, new String[] {threadName, fieldName, typeName});
			}
		}
		return "";	
	}
	
	public String getMarkerText(boolean qualified, String memberString) {
		String lineInfo= super.getMarkerText(qualified, memberString);

		String state= null;
		boolean access= isAccess();
		boolean modification= isModification();
		if (access && modification) {
			state= BOTH;
		} else if (access) {
			state= ACCESS;
		} else if (modification) {
			state= MODIFICATION;
		}		
		String label= null;
		if (state == null) {
			label= lineInfo;
		} else {
			String format= DebugJavaUtils.getResourceString(FORMAT);
			state= DebugJavaUtils.getResourceString(state);
			label= MessageFormat.format(format, new Object[] {state, lineInfo});
		}
		return label;	
	}
	*/
	
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
			fLastEventType= ACCESS_EVENT;
		} else if (event instanceof ModificationWatchpointEvent) {
			fLastEventType= MODIFICATION_EVENT;
		}
		return super.handleEvent(event, target);
	}	
	
	
	/**
	 * Update the enabled state of the given request, which is associated
	 * with this breakpoint. Set the enabled state of the request
	 * to the enabled state of this breakpoint.
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
		
	public boolean isAccessSuspend() {
		return fLastEventType == ACCESS_EVENT;
	}
}

