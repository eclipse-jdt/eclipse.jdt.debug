package org.eclipse.jdt.internal.debug.core;

import java.util.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.IDebugConstants;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.internal.debug.core.IJavaDebugConstants;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;

import com.sun.jdi.*;
import com.sun.jdi.event.*;
import com.sun.jdi.request.*;

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
	protected static final String[] fgLineBreakpointAttributes= new String[]{IDebugConstants.ENABLED, IMarker.LINE_NUMBER, IMarker.CHAR_START, IMarker.CHAR_END};
	
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
				IMarker marker = ensureMarker();
				Map attributes= marker.getAttributes();
				JavaCore.addJavaElementMarkerAttributes(attributes, type);
				marker.setAttributes(attributes);
				
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
	public int getLineNumber() throws CoreException {
		return ensureMarker().getAttribute(IMarker.LINE_NUMBER, -1);
	}

	/**
	 * @see ILineBreakpoint
	 */
	public int getCharStart() throws CoreException {
		return ensureMarker().getAttribute(IMarker.CHAR_START, -1);
	}

	/**
	 * @see ILineBreakpoint
	 */
	public int getCharEnd() throws CoreException {
		return ensureMarker().getAttribute(IMarker.CHAR_END, -1);
	}		
	
	/**
	 * Get the resource associated with the given type. This is
	 * used to set the breakpoint's resource during initialization.
	 */
	protected IResource getResource(IType type) throws CoreException {
		IResource resource= null;
		resource= type.getUnderlyingResource();
		if (resource == null) {
			resource= type.getJavaProject().getProject();
		}
		return resource;
	}
	
	/**
	 * Creates the event requests to:<ul>
	 * <li>Listen to class loads related to the breakpoint</li>
	 * <li>Respond to the breakpoint being hti</li>
	 * </ul>
	 */
	protected void  addToTarget(JDIDebugTarget target) throws CoreException {
		String topLevelName= getTopLevelTypeName();
		if (topLevelName == null) {
//			internalError(ERROR_BREAKPOINT_NO_TYPE);
			return;
		}
		
		// create request to listen to class loads
		registerRequest(target, target.createClassPrepareRequest(topLevelName));
		
		// create breakpoint requests for each class currently loaded
		List classes= target.jdiClassesByName(topLevelName);
		if (classes != null) {
			Iterator iter = classes.iterator();
			while (iter.hasNext()) {
				ReferenceType type= (ReferenceType) iter.next();
				createRequest(target, type);
			}
		}
	}
		
	/**
	 * Installs a line breakpoint in the given type, returning whether successful.
	 */
	protected void createRequest(JDIDebugTarget target, ReferenceType type) throws CoreException {
		Location location= null;
		int lineNumber= getLineNumber();			
		location= determineLocation(lineNumber, type);
		if (location == null) {
			// could be an inner type not yet loaded, or line information not available
			return;
		}
		
		EventRequest request = createLineBreakpointRequest(location, target);	
		registerRequest(target, request);		
	}	
	
	/**
	 * Creates, installs, and returns a line breakpoint request at
	 * the given location for the given breakpoint.
	 */
	protected BreakpointRequest createLineBreakpointRequest(Location location, JDIDebugTarget target) throws CoreException {
		BreakpointRequest request = null;
		try {
			request= target.getEventRequestManager().createBreakpointRequest(location);
			configureRequest(request);
		} catch (VMDisconnectedException e) {
			return null;
		} catch (RuntimeException e) {
			logError(e);
			return null;
		}
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

	
	/**
	 * Update the hit count of an <code>EventRequest</code>. Return a new request with
	 * the appropriate settings.
	 */
	protected EventRequest updateHitCount(EventRequest request, JDIDebugTarget target) throws CoreException {		
		
		// if the hit count has changed, or the request has expired and is being re-enabled,
		// create a new request
		if (hasHitCountChanged(request) || (isExpired(request) && isEnabled())) {
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
	protected void configureRequest(EventRequest request) throws CoreException {
		request.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
		request.putProperty(JDIDebugPlugin.JAVA_BREAKPOINT_PROPERTY, this);								
		int hitCount= getHitCount();
		if (hitCount > 0) {
			request.addCountFilter(hitCount);
			request.putProperty(IJavaDebugConstants.HIT_COUNT, new Integer(hitCount));
			request.putProperty(IJavaDebugConstants.EXPIRED, Boolean.FALSE);
		}
		// Important: only enable a request after it has been configured
		updateEnabledState(request);
	}	
		
	/**
	 * @see JavaBreakpoint#isSupportedBy(VirtualMachine)
	 */
	public boolean isSupportedBy(VirtualMachine vm) {
		return true;
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
		Object[] values= new Object[]{new Boolean(true), new Integer(lineNumber), new Integer(charStart), new Integer(charEnd)};
		ensureMarker().setAttributes(fgLineBreakpointAttributes, values);			
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
		ensureMarker().setAttributes(fgTypeAndHitCountAttributes, values);
	}		
	
	/**
	 * Returns the <code>METHOD_HANDLE</code> attribute of the given breakpoint.
	 */
	public String getMethodHandleIdentifier() throws CoreException {
		return (String) ensureMarker().getAttribute(IJavaDebugConstants.METHOD_HANDLE);
	}	
	
	/**
	 * @see IJavaLineBreakpoint#getMember()
	 */
	public IMember getMember() throws CoreException {
		int start = getCharStart();
		int end = getCharEnd();
		IType type = getType();
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

}



