package org.eclipse.jdt.internal.debug.core;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.debug.core.IJavaDebugConstants;
import org.eclipse.jdt.debug.core.IJavaWatchpoint;

import com.sun.jdi.*;
import com.sun.jdi.event.*;
import com.sun.jdi.request.*;

public class JavaWatchpoint extends JavaLineBreakpoint implements IJavaWatchpoint {
	
	// Thread label String keys
	private static final String ACCESS_SYS= THREAD_LABEL + "access_sys";
	private static final String ACCESS_USR= THREAD_LABEL + "access_usr";
	private static final String MODIFICATION_SYS= THREAD_LABEL + "modification_sys";
	private static final String MODIFICATION_USR= THREAD_LABEL + "modification_usr";
	// Error String keys
	private final static String PREFIX= "jdi_breakpoint.";
	private final static String ERROR = PREFIX + "error.";	
	private final static String ERROR_ACCESS_WATCHPOINT_NOT_SUPPORTED = ERROR + "access.not_supported";
	private final static String ERROR_MODIFICATION_WATCHPOINT_NOT_SUPPORTED = ERROR + "modification.net_supported";
	// Marker label String keys
	protected final static String WATCHPOINT= MARKER_LABEL + "watchpoint.";
	protected final static String FORMAT= WATCHPOINT + "format";	
	protected final static String ACCESS= WATCHPOINT + "access";
	protected final static String MODIFICATION= WATCHPOINT + "modification";
	protected final static String BOTH= WATCHPOINT + "both";		

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
				Map attributes= getAttributes();
				JavaCore.addJavaElementMarkerAttributes(attributes, field);
				setAttributes(attributes);
				
				// Lastly, add the breakpoint manager
				addToBreakpointManager();
			}
		};
		run(wr);
	}
	
	/**
	 * @see JavaBreakpoint#installIn(JDIDebugTarget)
	 */
	public void addToTarget(JDIDebugTarget target) {
		selectiveAdd(target, true, true);
	}

	/**
	 * A single watchpoint can create multiple requests. This method provides control over this
	 * property for explicitly choosing which requests (access, modification, or both) to 
	 * potentially add.
	 */	
	protected void selectiveAdd(JDIDebugTarget target, boolean accessCheck, boolean modificationCheck) {
		String topLevelName= getTopLevelTypeName();
		
		List classes= target.jdiClassesByName(topLevelName);
		if (classes == null || classes.isEmpty()) {
			// defer
			target.defer(this, topLevelName);
			return;
		}
				
		IField javaField= getField();
		Field field= null;
		ReferenceType reference= null;
		for (int i=0; i<classes.size(); i++) {
			reference= (ReferenceType) classes.get(i);
			field= reference.fieldByName(javaField.getElementName());
			if (field == null) {
				return;
			}
			AccessWatchpointRequest accessRequest= null;
			ModificationWatchpointRequest modificationRequest= null;			
			// If we're not supposed to check access or modification, just retrieve the
			// existing request
			if (!accessCheck) {
				accessRequest= target.getAccessWatchpointRequest(field);
			}
			if (!modificationCheck) {
				modificationRequest= target.getModificationWatchpointRequest(field);
			}
			if (isAccess() && accessCheck) {
				if (accessSupportedBy(target.getVM())) {
					accessRequest= accessWatchpointAdded(target, field);
				} else {
					notSupported(ERROR_ACCESS_WATCHPOINT_NOT_SUPPORTED);				
				}
			}
			if (isModification() && modificationCheck) {
				if (modificationSupportedBy(target.getVM())) {
					modificationRequest= modificationWatchpointAdded(target, field);
				} else {
					notSupported(ERROR_MODIFICATION_WATCHPOINT_NOT_SUPPORTED);
				}
			}
			if (!(accessRequest == null && modificationRequest == null)) {
				Object[] requests= {accessRequest, modificationRequest};
				target.installBreakpoint(this, requests);
				try {		
					incrementInstallCount();
				} catch (CoreException e) {
					target.internalError(e);
				}				
			}
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
	protected void notSupported(String error_key) {
	}
	
	/**
	 * An access watchpoint has been added.
	 * Create or update the request.
	 */
	protected AccessWatchpointRequest accessWatchpointAdded(JDIDebugTarget target, Field field) {
		AccessWatchpointRequest request= target.getAccessWatchpointRequest(field);
		if (request == null) {
			request= createAccessWatchpoint(target, field);
		}
		return request;
	}
	
	/**
	 * Create an access watchpoint for the given breakpoint and associated field
	 */
	protected AccessWatchpointRequest createAccessWatchpoint(JDIDebugTarget target, Field field) {
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
	 * A modification watchpoint has been added.
	 * Create or update the request.
	 */
	protected ModificationWatchpointRequest modificationWatchpointAdded(JDIDebugTarget target, Field field) {
		ModificationWatchpointRequest request= target.getModificationWatchpointRequest(field);
		if (request == null) {
			request= createModificationWatchpoint(target, field);
		}
		return request;
	}
	
	/**
	 * Create a modification watchpoint for the given breakpoint and associated field
	 */
	protected ModificationWatchpointRequest createModificationWatchpoint(JDIDebugTarget target, Field field) {
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
	 * A watchpoint has been changed.
	 * Update the request.
	 */
	public void changeForTarget(JDIDebugTarget target) {
		Object[] requests= (Object[])target.getRequest(this);		
		for (int i=0; i < requests.length; i++) {
			WatchpointRequest request= (WatchpointRequest)requests[i];
			if (request == null) {
				if ((i == 0) && isAccess()) {
					selectiveAdd(target, true, false);
				}
				if ((i == 1) && isModification()) {
					selectiveAdd(target, false, true);
				}
				continue;
			}
			if ((!isAccess() && (request instanceof AccessWatchpointRequest)) ||
			(!isModification() && (request instanceof ModificationWatchpointRequest))) {
				target.getEventRequestManager().deleteEventRequest(request); // disable & remove
				continue;
			}
			request= (WatchpointRequest)updateHitCount(request, target);

			if (request != null) {
				updateEnabledState(request);					
				requests[i]= request;
			}				
		}
	}

	/**
	 * Update the hit count of an <code>EventRequest</code>. Return a new request with
	 * the appropriate settings.
	 */
	protected EventRequest updateHitCount(EventRequest request, JDIDebugTarget target) {		
		
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
				logError(e);
			}
		}
		return request;
	}

	/**
	 * @see JavaBreakpoint#removeFromTarget(JDIDebugTarget)
	 */
	public void removeFromTarget(JDIDebugTarget target) {
		Object[] requests= (Object[]) target.getRequest(this);
		if (requests == null) {
			//deferred breakpoint
			if (!this.exists()) {
				//resource no longer exists
				return;
			}
			String name= getTopLevelTypeName();
			List breakpoints= (List) target.getDeferredBreakpointsByClass(name);
			if (breakpoints == null) {
				return;
			}

			breakpoints.remove(this);
			if (breakpoints.isEmpty()) {
				target.removeDeferredBreakpointByClass(name);
			}
		} else {
			//installed breakpoint
			try {
				for (int i=0; i<requests.length; i++) {
					WatchpointRequest request= (WatchpointRequest)requests[i];
					if (request == null) {
						continue;
					}
					target.getEventRequestManager().deleteEventRequest(request); // disable & remove					
				}
				try {
					decrementInstallCount();
				} catch (CoreException e) {
					logError(e);
				}			
			} catch (VMDisconnectedException e) {
				return;
			} catch (RuntimeException e) {
				logError(e);
			}
		}
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
	public boolean isAccess() {
		return getAttribute(IJavaDebugConstants.ACCESS, false);
	}
	
	/**
	 * @see IJavaWatchpoint#setAccess
	 */
	public void setAccess(boolean access) {
		if (access == isAccess()) {
			return;
		}		
		try {
			setAttribute(IJavaDebugConstants.ACCESS, access);
			if (access && !isEnabled()) {
				setEnabled(true);
			} else if (!(access || isModification())) {
				setEnabled(false);
			}
		} catch (CoreException ce) {
			logError(ce);
		}
	}
	
	/**
	 * @see IJavaWatchpoint#isModification
	 */	
	public boolean isModification() {
		return getAttribute(IJavaDebugConstants.MODIFICATION, false);
	}
	
	/**
	 * @see IJavaWatchpoint#setModification(boolean)
	 */
	public void setModification(boolean modification) {
		if (modification == isModification()) {
			return;
		}
		try {
			setAttribute(IJavaDebugConstants.MODIFICATION, modification);
			if (modification && !isEnabled()) {
				setEnabled(true);
			} else if (!(modification || isAccess())) {
				setEnabled(false);
			}
		} catch (CoreException ce) {
			logError(ce);
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
	private void setDefaultAccessAndModification() {
		Object[] values= new Object[]{Boolean.FALSE, Boolean.TRUE};
		String[] attributes= new String[]{IJavaDebugConstants.ACCESS, IJavaDebugConstants.MODIFICATION};
		try {
			setAttributes(attributes, values);
		} catch (CoreException ce) {
			logError(ce);
		}
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
		setAttribute(IJavaDebugConstants.FIELD_HANDLE, handle);
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
		setAttribute(IJavaDebugConstants.AUTO_DISABLED, autoDisabled);
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
	 * Generate the field associated with the given marker
	 */
	public IField getField(IMarker marker) {
		String handle= getFieldHandleIdentifier(marker);
		if (handle != null && handle != "") {
			return (IField)JavaCore.create(handle);
		}
		return null;
	}	
	
	/**
	 * @see IJavaWatchpoint#getField()
	 */
	public IField getField() {
		String handle= getFieldHandleIdentifier();
		if (handle != null && handle != "") {
			return (IField)JavaCore.create(handle);
		}
		return null;
	}		
	
	/**
	 * Returns the <code>FIELD_HANDLE</code> attribute of the given marker.
	 */
	public String getFieldHandleIdentifier(IMarker marker) {
		String handle;
		try {
			handle= (String)marker.getAttribute(IJavaDebugConstants.FIELD_HANDLE);
		} catch (CoreException ce) {
			handle= "";
			logError(ce);
		}
		return handle;
	}
	
	/**
	 * Returns the <code>FIELD_HANDLE</code> attribute of this watchpoint.
	 */
	public String getFieldHandleIdentifier() {
		String handle;
		try {
			handle= (String)getAttribute(IJavaDebugConstants.FIELD_HANDLE);
		} catch (CoreException ce) {
			handle= "";
			logError(ce);
		}
		return handle;
	}
	
	/**
	 * @see JavaBreakpoint
	 */
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
	
	/**
	 * Store the type of the event, then handle it as specified in
	 * the superclass. This is useful for correctly generating the
	 * thread text when asked (assumes thread text is requested after
	 * the event is passed to this breakpoint.
	 * 
	 * Also, @see JavaBreakpoint#handleEvent(Event)
	 */
	public void handleEvent(Event event, JDIDebugTarget target) {
		if (event instanceof AccessWatchpointEvent) {
			fLastEventType= ACCESS_EVENT;
		} else if (event instanceof ModificationWatchpointEvent) {
			fLastEventType= MODIFICATION_EVENT;
		}
		super.handleEvent(event, target);
	}	
	
	/**
	 * Returns the <code>HIT_COUNT</code> attribute of the given breakpoint
	 * or -1 if the attribute is not set.
	 */
	public int getHitCount() {
		return getAttribute(IJavaDebugConstants.HIT_COUNT, -1);
	}
	
	/**
	 * Sets the <code>HIT_COUNT</code> attribute of the given breakpoint,
	 * and resets the <code>EXPIRED</code> attribute to false (since, if
	 * the hit count is changed, the breakpoint should no longer be expired).
	 */
	public void setHitCount(int count) throws CoreException {
		setAttributes(new String[]{IJavaDebugConstants.HIT_COUNT, IJavaDebugConstants.EXPIRED},
						new Object[]{new Integer(count), Boolean.FALSE});
	}	

}

