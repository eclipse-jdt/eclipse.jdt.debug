package org.eclipse.jdt.internal.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModelStatusConstants;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ClassNotPreparedException;
import com.sun.jdi.InvalidLineNumberException;
import com.sun.jdi.Location;
import com.sun.jdi.NativeMethodException;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.EventRequest;

public class JavaLineBreakpoint extends JavaBreakpoint implements IJavaLineBreakpoint {

	private static final String JAVA_LINE_BREAKPOINT = "org.eclipse.jdt.debug.javaLineBreakpointMarker"; //$NON-NLS-1$
	// Marker label String keys
	protected static final String LINE= "line"; //$NON-NLS-1$
	/**
	 * Sets of attributes used to configure a line breakpoint
	 */
	protected static final String[] fgTypeAndHitCountAttributes= new String[]{TYPE_HANDLE, HIT_COUNT, EXPIRED};	
	/**
	 * Sets of attributes used to configure a line breakpoint
	 */
	protected static final String[] fgLineBreakpointAttributes= new String[]{ENABLED, IMarker.LINE_NUMBER, IMarker.CHAR_START, IMarker.CHAR_END};
		
	public JavaLineBreakpoint() {
	}
	
	public JavaLineBreakpoint(IType type, int lineNumber, int charStart, int charEnd, int hitCount) throws DebugException {
		this(type, lineNumber, charStart, charEnd, hitCount, JAVA_LINE_BREAKPOINT);
	}

	protected JavaLineBreakpoint(final IType type, final int lineNumber, final int charStart, final int charEnd, final int hitCount, final String markerType) throws DebugException {
		IWorkspaceRunnable wr= new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				IResource resource= getResource(type);

	
				// create the marker
				setMarker(resource.createMarker(markerType));
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
	 * Returns the type of marker associated with java line breakpoints
	 */
	public static String getMarkerType() {
		return JAVA_LINE_BREAKPOINT;
	}
	
	/**
	 * Get the resource associated with the given type. This is
	 * used to set the breakpoint's resource during initialization.
	 */
	protected IResource getResource(IType type) throws CoreException {
		IResource resource= null;
		try {
			resource= type.getUnderlyingResource();
		} catch (JavaModelException e) {
			IStatus status = e.getStatus();
			if (status.getCode() != IJavaModelStatusConstants.ELEMENT_DOES_NOT_EXIST) {
				throw e;
			}
			// use the associated project if the type does
			// not actually exist as a resource
		}
		if (resource == null) {
			resource= type.getJavaProject().getProject();
		}
		return resource;
	}
	
	/**
	 * @see JavaBreakpoint#newRequest(JDIDebugTarget, ReferenceType)
	 */
	protected EventRequest newRequest(JDIDebugTarget target, ReferenceType type) throws CoreException {
		Location location= null;
		int lineNumber= getLineNumber();			
		location= determineLocation(lineNumber, type);
		if (location == null) {
			// could be an inner type not yet loaded, or line information not available
			return null;
		}
		
		EventRequest request = createLineBreakpointRequest(location, target);	
		return request;		
	}	

	/**
	 * Creates, installs, and returns a line breakpoint request at
	 * the given location for this breakpoint.
	 */
	protected BreakpointRequest createLineBreakpointRequest(Location location, JDIDebugTarget target) throws CoreException {
		BreakpointRequest request = null;
		try {
			request= target.getEventRequestManager().createBreakpointRequest(location);
			configureRequest(request);
		} catch (VMDisconnectedException e) {
			if (target.isDisconnected() || target.isTerminated()) {			
				return null;
			} 
			JDIDebugPlugin.logError(e);
		} catch (RuntimeException e) {
			JDIDebugPlugin.logError(e);
			return null;
		}
		return request;
	}
		
	/**
	 * Returns a location for the line number in the given type.
	 * Returns <code>null</code> if a location cannot be determined.
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
			//possibly in a nested type, will be handled when that class is loaded
			return null;
		} catch (VMDisconnectedException e) {
			return null;
		} catch (ClassNotPreparedException e) {
			// could be a nested type that is not yet loaded
			return null;
		} catch (RuntimeException e) {
			// not able to retrieve line info
			JDIDebugPlugin.logError(e);
			return null;
		}
		
		if (locations != null && locations.size() > 0) {
			return (Location) locations.get(0);
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
				request = createLineBreakpointRequest(location, target);
			} catch (VMDisconnectedException e) {
				if (target.isDisconnected() || target.isTerminated()) {
					return request;
				}
				JDIDebugPlugin.logError(e);
			} catch (RuntimeException e) {
				JDIDebugPlugin.logError(e);
			}
		}
		return request;
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
	 * </ol>	
	 */	
	public void setLineBreakpointAttributes(String modelIdentifier, boolean enabled, int lineNumber, int charStart, int charEnd) throws CoreException {
		Object[] values= new Object[]{new Boolean(true), new Integer(lineNumber), new Integer(charStart), new Integer(charEnd)};
		ensureMarker().setAttributes(fgLineBreakpointAttributes, values);			
	}		
	
	/**
	 * Sets the type in which this breakpoint is installed.
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
	 * @see IJavaLineBreakpoint#getMember()
	 */
	public IMember getMember() throws CoreException {
		int start = getCharStart();
		int end = getCharEnd();
		IType type = getType();
		IMember member = null;
		if ((type != null) && (end >= start) && (start >= 0)) {
			try {
				member= binSearch(type, start, end);
			} catch (CoreException ce) {
				JDIDebugPlugin.logError(ce);
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
	protected IMember binSearch(IType type, int start, int end) throws JavaModelException {
		IJavaElement je = getElementAt(type, start);
		if (je != null && !je.equals(type)) {
			return (IMember)je;
		}
		if (end > start) {
			je = getElementAt(type, end);
			if (je != null && !je.equals(type)) {
				return (IMember)je;
			}
			int mid = ((end - start) / 2) + start;
			if (mid > start) {
				je = binSearch(type, start + 1, mid);
				if (je == null) {
					je = binSearch(type, mid + 1, end - 1);
				}
				return (IMember)je;
			}
		}
		return null;
	}	
	
	/**
	 * Returns the element at the given position in the given type
	 */
	protected IJavaElement getElementAt(IType type, int pos) throws JavaModelException {
		if (type.isBinary()) {
			return type.getClassFile().getElementAt(pos);
		} else {
			return type.getCompilationUnit().getElementAt(pos);
		}
	}

}



