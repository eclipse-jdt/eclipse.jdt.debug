package org.eclipse.jdt.internal.debug.core.breakpoints;

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
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;
import org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget;

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
		
	public JavaLineBreakpoint() {
	}

	/**
	 * @see JDIDebugModel#createLineBreakpoint(IResource, String, int, int, int, int, boolean, Map)
	 */
	public JavaLineBreakpoint(final IResource resource, final String typeName, final int lineNumber, final int charStart, final int charEnd, final int hitCount, boolean add, Map attributes) throws DebugException {
		this(resource, typeName,lineNumber, charStart, charEnd, hitCount, add, attributes, JAVA_LINE_BREAKPOINT);
	}
	
	protected JavaLineBreakpoint(final IResource resource, final String typeName, final int lineNumber, final int charStart, final int charEnd, final int hitCount, final boolean add, final Map attributes, final String markerType) throws DebugException {
		IWorkspaceRunnable wr= new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
	
				// create the marker
				setMarker(resource.createMarker(markerType));
				
				// add attributes
				addLineBreakpointAttributes(attributes, getModelIdentifier(), true, lineNumber, charStart, charEnd);
				addTypeNameAndHitCount(attributes, typeName, hitCount);
				
				// set attributes
				ensureMarker().setAttributes(attributes);
				
				// add to breakpoint manager
				if (add) {
					addToBreakpointManager();
				}
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
	 * Returns the type of marker associated with Java line breakpoints
	 */
	public static String getMarkerType() {
		return JAVA_LINE_BREAKPOINT;
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
			if (!target.isAvailable()) {			
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
				if (!target.isAvailable()) {
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
	 * Adds the standard attributes of a line breakpoint to
	 * the given attribute map.
	 * The standard attributes are:
	 * <ol>
	 * <li>IBreakpoint.ID</li>
	 * <li>IBreakpoint.ENABLED</li>
	 * <li>IMarker.LINE_NUMBER</li>
	 * <li>IMarker.CHAR_START</li>
	 * <li>IMarker.CHAR_END</li>
	 * </ol>	
	 * 
	 */	
	public void addLineBreakpointAttributes(Map attributes, String modelIdentifier, boolean enabled, int lineNumber, int charStart, int charEnd) {
		attributes.put(IBreakpoint.ID, modelIdentifier);
		attributes.put(IBreakpoint.ENABLED, new Boolean(enabled));
		attributes.put(IMarker.LINE_NUMBER, new Integer(lineNumber));
		attributes.put(IMarker.CHAR_START, new Integer(charStart));
		attributes.put(IMarker.CHAR_END, new Integer(charEnd)); 
	}		
	
	/**
	 * Adds type name and hit count attributes to the given
	 * map.
	 *
	 * If <code>hitCount > 0</code>, adds the <code>HIT_COUNT</code> attribute
	 * to the given breakpoint, and resets the <code>EXPIRED</code> attribute
	 * to false (since, if the hit count is changed, the breakpoint should no
	 * longer be expired).
	 */
	public void addTypeNameAndHitCount(Map attributes, String typeName, int hitCount) {
		attributes.put(TYPE_NAME, typeName);
		if (hitCount > 0) {
			attributes.put(HIT_COUNT, new Integer(hitCount));
			attributes.put(EXPIRED, new Boolean(false));
		}
	}
	
}



