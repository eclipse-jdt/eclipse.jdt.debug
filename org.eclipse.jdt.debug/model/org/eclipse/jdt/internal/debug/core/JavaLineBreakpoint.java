package org.eclipse.jdt.internal.debug.core;

import java.util.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.IDebugConstants;
import org.eclipse.debug.core.model.*;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.debug.core.*;

import com.sun.jdi.*;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.LocatableEvent;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.EventRequest;

public class JavaLineBreakpoint extends JavaBreakpoint implements IJavaLineBreakpoint {
	
	// Thread label String keys
	private static final String LINE_BREAKPOINT_SYS= THREAD_LABEL + "line_breakpoint_sys";
	private static final String LINE_BREAKPOINT_USR= THREAD_LABEL + "line_breakpoint_usr";
	// Marker label String keys
	private static final String LINE= "line";
	private static final String HITCOUNT= "hitCount";
		
	static String fMarkerType= IJavaDebugConstants.JAVA_LINE_BREAKPOINT;
	
	/**
	 * Sets of attributes used to configure a line breakpoint
	 */
	protected static final String[] fgTypeAndHitCountAttributes= new String[]{IJavaDebugConstants.TYPE_HANDLE, IJavaDebugConstants.HIT_COUNT, IJavaDebugConstants.EXPIRED};	
	protected static final String[] fgLineBreakpointAttributes= new String[]{IDebugConstants.MODEL_IDENTIFIER, IDebugConstants.ENABLED, IMarker.LINE_NUMBER, IMarker.CHAR_START, IMarker.CHAR_END};
	
	public JavaLineBreakpoint() {
	}
	
	public JavaLineBreakpoint(IType type, int lineNumber, int charStart, int charEnd, int hitCount) throws DebugException {
		this(type, lineNumber, charStart, charEnd, hitCount, fMarkerType);
	}
	
	public JavaLineBreakpoint(final IType type, final int lineNumber, final int charStart, final int charEnd, final int hitCount, final String markerType) throws DebugException {
		IWorkspaceRunnable wr= new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				IResource resource= getResource(type);

	
				// create the marker
				fMarker= resource.createMarker(markerType);
				setLineBreakpointAttributes(getPluginIdentifier(), true, lineNumber, charStart, charEnd);
	
				// configure the hit count and type handle
				setTypeAndHitCount(type, hitCount);
	
				// configure the marker as a Java marker
				Map attributes= getAttributes();
				JavaCore.addJavaElementMarkerAttributes(attributes, type);
				setAttributes(attributes);
				
				// Lastly, add the breakpoint manager
				addToBreakpointManager();
			}
		};
		run(wr);
	}
	
	public static String getMarkerType() {
		return fMarkerType;
	}	
	
	/**
	 * @see ILineBreakpoint
	 */
	public int getLineNumber() {
		return getAttribute(IMarker.LINE_NUMBER, -1);
	}

	/**
	 * @see ILineBreakpoint
	 */
	public int getCharStart() {
		return getAttribute(IMarker.CHAR_START, -1);
	}

	/**
	 * @see ILineBreakpoint
	 */
	public int getCharEnd() {
		return getAttribute(IMarker.CHAR_END, -1);
	}		
	
	/**
	 * Get the resource associated with the given type. This is
	 * used to set the breakpoint's resource during initialization.
	 */
	protected IResource getResource(IType type) {
		IResource resource= null;
		try {
			resource= type.getUnderlyingResource();
			if (resource == null) {
				resource= type.getJavaProject().getProject();
			}
		} catch (JavaModelException jme) {
			logError(jme);
		}
		return resource;
	}
	
	/**
	 * @see JavaBreakpoint#installIn(JDIDebugTarget)
	 */
	public void addToTarget(JDIDebugTarget target) {
		String topLevelName= getTopLevelTypeName();
		if (topLevelName == null) {
//			internalError(ERROR_BREAKPOINT_NO_TYPE);
			return;
		}
		
		// look for the top-level class - if it is loaded, inner classes may also be loaded
		List classes= target.jdiClassesByName(topLevelName);
		if (classes == null || classes.isEmpty()) {
			// defer
			target.defer(this, topLevelName);
		} else {
			// try to install
			ReferenceType type= (ReferenceType) classes.get(0);
			if (!installLineBreakpoint(target, type)) {
				// install did not succeed - could be an inner type not yet loaded
				target.defer(this, topLevelName);
			}
		}
	}	
	
	/**
	 * Installs a line breakpoint in the given type, returning whether successful.
	 */
	protected boolean installLineBreakpoint(JDIDebugTarget target, ReferenceType type) {
		Location location= null;
		int lineNumber= getLineNumber();			
		location= determineLocation(lineNumber, type);
		if (location == null) {
			// could be an inner type not yet loaded, or line information not available
			return false;
		}
		
		if (createLineBreakpointRequest(location, target) != null) {
			// update the install attibute on the breakpoint
			if (!target.inHCR()) {
				try {
					incrementInstallCount();
				} catch (CoreException e) {
					logError(e);
				}
			}
			return true;
		} else {
			return false;
		}
		
	}	
	
	/**
	 * Creates, installs, and returns a line breakpoint request at
	 * the given location for the given breakpoint.
	 */
	protected BreakpointRequest createLineBreakpointRequest(Location location, JDIDebugTarget target) {
		BreakpointRequest request = null;
		try {
			request= target.getEventRequestManager().createBreakpointRequest(location);
			configureRequest(request);
		} catch (VMDisconnectedException e) {
			target.uninstallBreakpoint(this);
			return null;
		} catch (RuntimeException e) {
			target.uninstallBreakpoint(this);
			logError(e);
			return null;
		}
		target.installBreakpoint(this, request);	
		return request;
	}
	
	
	/**
	 * Returns a location for the line number in the given type, or any of its
	 * nested types. Returns <code>null</code> if a location cannot be determined.
	 */
	protected Location determineLocation(int lineNumber, ReferenceType type) {
		List locations= null;
		try {
			locations= type.locationsOfLine(lineNumber);
		} catch (AbsentInformationException e) {
			return null;
		} catch (NativeMethodException e) {
			return null;
		} catch (InvalidLineNumberException e) {
			//possible in a nested type, fall through and traverse nested types
		} catch (VMDisconnectedException e) {
			return null;
		} catch (ClassNotPreparedException e) {
			// could be a nested type that is not yet loaded
			return null;
		} catch (RuntimeException e) {
			// not able to retrieve line info
			logError(e);
			return null;
		}
		
		if (locations != null && locations.size() > 0) {
			return (Location) locations.get(0);
		} else {
			Iterator nestedTypes= null;
			try {
				nestedTypes= type.nestedTypes().iterator();
			} catch (RuntimeException e) {
				// not able to retrieve line info
				logError(e);
				return null;
			}
			while (nestedTypes.hasNext()) {
				ReferenceType nestedType= (ReferenceType) nestedTypes.next();
				Location innerLocation= determineLocation(lineNumber, nestedType);
				if (innerLocation != null) {
					return innerLocation;
				}
			}
		}

		return null;
	}	
	
	public void changeForTarget(JDIDebugTarget target) {
		BreakpointRequest request= (BreakpointRequest) target.getRequest(this);
		if (request != null) {
			// already installed - could be a change in the enabled state or hit count
			//may result in a new request being generated
			request= updateHitCount(request, target);
			if (request != null) {
				updateEnabledState(request);
				target.installBreakpoint(this, request);				
			}
		}	
	}
	
	/**
	 * Update the hit count of an <code>EventRequest</code>. Return a new request with
	 * the appropriate settings.
	 */
	protected BreakpointRequest updateHitCount(BreakpointRequest request, JDIDebugTarget target) {		
		
		// if the hit count has changed, or the request has expired and is being re-enabled,
		// create a new request
		if (hasHitCountChanged(request) || (isExpired(request) && this.isEnabled())) {
			try {
				Location location = ((BreakpointRequest) request).location();				
				// delete old request
				//on JDK you cannot delete (disable) an event request that has hit its count filter
				if (!isExpired(request)) {
					target.getEventRequestManager().deleteEventRequest(request); // disable & remove
				}				
				request = createLineBreakpointRequest(location, target);
			} catch (VMDisconnectedException e) {
			} catch (RuntimeException e) {
				logError(e);
			}
		}
		return request;
	}
	
	/**
	 * Returns whether the hitCount of this breakpoint is equal to the hitCount of
	 * the associated request.
	 */
	protected boolean hasHitCountChanged(EventRequest request) {
		int hitCount= getHitCount();
		Integer requestCount= (Integer) request.getProperty(IJavaDebugConstants.HIT_COUNT);
		int oldCount = -1;
		if (requestCount != null)  {
			oldCount = requestCount.intValue();
		} 
		return hitCount != oldCount;
	}
		
	
	/**
	 * Configure a breakpoint request with common properties:
	 * <ul>
	 * <li><code>IDebugConstants.BREAKPOINT_MARKER</code></li>
	 * <li><code>IJavaDebugConstants.HIT_COUNT</code></li>
	 * <li><code>IJavaDebugConstants.EXPIRED</code></li>
	 * <li><code>IDebugConstants.ENABLED</code></li>
	 * </ul>
	 * and sets the suspend policy of the request to suspend 
	 * the event thread.
	 */
	protected void configureRequest(EventRequest request) {
		request.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
		request.putProperty(IDebugConstants.BREAKPOINT, this);								
		int hitCount= getHitCount();
		if (hitCount > 0) {
			request.addCountFilter(hitCount);
			request.putProperty(IJavaDebugConstants.HIT_COUNT, new Integer(hitCount));
			request.putProperty(IJavaDebugConstants.EXPIRED, Boolean.FALSE);
		}
		// Important: only enable a request after it has been configured
		request.setEnabled(isEnabled());		
	}	
	
	/**
	 * Enable a request and increment the install count of the associated breakpoint.
	 */
	protected void completeConfiguration(EventRequest request) {
		// Important: Enable only after request has been configured		
		try {	
			request.setEnabled(isEnabled());				
			incrementInstallCount();
		} catch (CoreException e) {
			logError(e);
		}
	}		
	
	/**
	 * @see IJavaBreakpoint#handleEvent(Event)
	 */
	public void handleEvent(Event event, JDIDebugTarget target) {
		if (!(event instanceof LocatableEvent)) {
			return;
		}
		ThreadReference threadRef= ((LocatableEvent)event).thread();
		JDIThread thread= target.findThread(threadRef);		
		if (thread == null) {
			target.resume(threadRef);
			return;
		} else {
			thread.handleSuspendForBreakpoint(this);
			expireHitCount((LocatableEvent)event);	
		}			
	}
	
	/**
	 * Called when a breakpoint event is encountered
	 */
	public void expireHitCount(LocatableEvent event) {
		EventRequest request= (EventRequest)event.request();
		Integer requestCount= (Integer) request.getProperty(IJavaDebugConstants.HIT_COUNT);
		if (requestCount != null) {
			try {
				request.putProperty(IJavaDebugConstants.EXPIRED, Boolean.TRUE);
				setEnabled(false);
				// make a note that we auto-disabled this breakpoint.
				setExpired(true);
			} catch (CoreException ce) {
				logError(ce);
			}
		}
	}	
	
	/**
	 * @see JavaBreakpoint#removeFromTarget(JDIDebugTarget)
	 */
	public void removeFromTarget(JDIDebugTarget target) {		
		BreakpointRequest request= (BreakpointRequest) target.getRequest(this);
		if (request == null) {
			//deferred breakpoint
			if (!this.exists()) {
				//resource no longer exists
				return;
			}
			String name= getTopLevelTypeName();
			if (name == null) {
//				internalError(ERROR_BREAKPOINT_NO_TYPE);
				return;
			}
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
				// cannot delete an expired request
				if (!isExpired(request)) {
					target.getEventRequestManager().deleteEventRequest(request); // disable & remove
				}
			} catch (VMDisconnectedException e) {
				return;
			} catch (RuntimeException e) {
				logError(e);
			}
		}
	}	

	/**
	 * @see IJavaLineBreakpoint#getHitCount()
	 */
	public int getHitCount() {
		return getAttribute(IJavaDebugConstants.HIT_COUNT, -1);
	}
	
	/**
	 * @see IJavaLineBreakpoint#setHitCount(int)
	 */
	public void setHitCount(int count) throws CoreException {
		setAttributes(new String[]{IJavaDebugConstants.HIT_COUNT, IJavaDebugConstants.EXPIRED},
						new Object[]{new Integer(count), Boolean.FALSE});
	}		
	
	/**
	 * @see JavaBreakpoint#isSupportedBy(VirtualMachine)
	 */
	public boolean isSupportedBy(VirtualMachine vm) {
		return true;
	}
	
	/**
	 * Returns whether the given breakpoint has expired.
	 */
	public boolean isExpired() {
		return getBooleanAttribute( IJavaDebugConstants.EXPIRED);
	}	
	
	/**
	 * Sets the <code>EXPIRED</code> attribute of the given breakpoint.
	 */
	public void setExpired(boolean expired) throws CoreException {
		setBooleanAttribute(IJavaDebugConstants.EXPIRED, expired);	
	}	
	
	/**
	 * @see IJavaLineBreakpoint#getMethod()		
	 */
	public IMethod getMethod() {
		try {
			String handle = getMethodHandleIdentifier();
			if (handle != null) {
				return (IMethod)JavaCore.create(handle);
			}
		} catch (CoreException e) {
			logError(e);
		}
		return null;
	}	
	
	/**
	 * Set standard attributes of a line breakpoint.
	 * The standard attributes are:
	 * <ol>
	 * <li>IDebugConstants.MODEL_IDENTIFIER</li>
	 * <li>IDebugConstants.ENABLED</li>
	 * <li>IMarker.LINE_NUMBER</li>
	 * <li>IMarker.CHAR_START</li>
	 * <li>IMarker.CHAR_END</li>
	 * <li>IJavaDebugConstants.CONDITION</li>		
	 */	
	public void setLineBreakpointAttributes(String modelIdentifier, boolean enabled, int lineNumber, int charStart, int charEnd) throws CoreException {
		Object[] values= new Object[]{getPluginIdentifier(), new Boolean(true), new Integer(lineNumber), new Integer(charStart), new Integer(charEnd)};
		setAttributes(fgLineBreakpointAttributes, values);			
	}
	
	/**
	 * Sets the <code>TYPE_HANDLE</code> attribute of the given breakpoint, associated
	 * with the given IType.
	 *
	 * If <code>hitCount > 0</code>, sets the <code>HIT_COUNT</code> attribute of the given breakpoint,
	 * and resets the <code>EXPIRED</code> attribute to false (since, if
	 * the hit count is changed, the breakpoint should no longer be expired).
	 */
	public void setTypeAndHitCount(IType type, int hitCount) throws CoreException {
		if (hitCount == 0) {
			setType(type);
			return;
		}
		String handle = type.getHandleIdentifier();
		Object[] values= new Object[]{handle, new Integer(hitCount), Boolean.FALSE};
		setAttributes(fgTypeAndHitCountAttributes, values);
	}		
	
	/**
	 * Returns the <code>METHOD_HANDLE</code> attribute of the given breakpoint.
	 */
	public String getMethodHandleIdentifier() throws CoreException {
		return (String)getAttribute(IJavaDebugConstants.METHOD_HANDLE);
	}	
	
	/**
	 * @see IJavaLineBreakpoint#getMember()
	 */
	public IMember getMember() {
		int start = getCharStart();
		int end = getCharEnd();
		IType type = getInstalledType();
		IMember member = null;
		if (type != null && end >= start && start >= 0) {
			try {
				if (type.isBinary()) {
					member= binSearch(type.getClassFile(), type, start, end);
				} else {
					member= binSearch(type.getCompilationUnit(), type, start, end);
				}
			} catch (CoreException ce) {
				logError(ce);
			}
		}
		if (member == null) {
			member= type;
		}
		return member;
	}
	
	/**
	 * Searches the given source range of the container for a member that is
	 * not the same as the given type.
	 */
	protected IMember binSearch(IClassFile container, IType type, int start, int end) throws JavaModelException {
		IJavaElement je = container.getElementAt(start);
		if (je != null && !je.equals(type)) {
			return (IMember)je;
		}
		if (end > start) {
			je = container.getElementAt(end);
			if (je != null && !je.equals(type)) {
				return (IMember)je;
			}
			int mid = ((end - start) / 2) + start;
			if (mid > start) {
				je = binSearch(container, type, start + 1, mid);
				if (je == null) {
					je = binSearch(container, type, mid + 1, end - 1);
				}
				return (IMember)je;
			}
		}
		return null;
	}	
	
	/**
	 * Searches the given source range of the container for a member that is
	 * not the same as the given type.
	 */
	protected IMember binSearch(ICompilationUnit container, IType type, int start, int end) throws JavaModelException {
		IJavaElement je = container.getElementAt(start);
		if (je != null && !je.equals(type)) {
			return (IMember)je;
		}
		if (end > start) {
			je = container.getElementAt(end);
			if (je != null && !je.equals(type)) {
				return (IMember)je;
			}
			int mid = ((end - start) / 2) + start;
			if (mid > start) {
				je = binSearch(container, type, start + 1, mid);
				if (je == null) {
					je = binSearch(container, type, mid + 1, end - 1);
				}
				return (IMember)je;
			}
		}
		return null;
	}
	
	/**
	 * @see JavaBreakpoint
	 */	
	public String getFormattedThreadText(String threadName, String typeName, boolean systemThread) {
		int lineNumber= getAttribute(IMarker.LINE_NUMBER, -1);
		if (lineNumber > -1) {
			if (systemThread) {
				return getFormattedString(LINE_BREAKPOINT_SYS, new String[] {threadName, String.valueOf(lineNumber), typeName});
			} else {
				return getFormattedString(LINE_BREAKPOINT_USR, new String[] {threadName, String.valueOf(lineNumber), typeName});
			}
		}
		return "";
	}
	
	public String getMarkerText(boolean showQualified, String memberString) {
		IType type= getInstalledType();
		if (type != null) {
			StringBuffer label= new StringBuffer();
			if (showQualified) {
				label.append(type.getFullyQualifiedName());
			} else {
				label.append(type.getElementName());
			}
			int lineNumber= getLineNumber();
			if (lineNumber > 0) {
				label.append(" [");
				label.append(DebugJavaUtils.getResourceString(MARKER_LABEL + LINE));
				label.append(' ');
				label.append(lineNumber);
				label.append(']');

			}
			int hitCount= getHitCount();
			if (hitCount > 0) {
				label.append(" [");
				label.append(DebugJavaUtils.getResourceString(MARKER_LABEL + HITCOUNT));
				label.append(' ');
				label.append(hitCount);
				label.append(']');
			}
			if (memberString != null) {
				label.append(" - ");
				label.append(memberString);
			}
			return label.toString();
		}
		return "";		
	}

}



