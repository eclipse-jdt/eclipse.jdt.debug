package org.eclipse.jdt.internal.debug.core;

import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.IDebugConstants;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ClassNotPreparedException;
import com.sun.jdi.InvalidLineNumberException;
import com.sun.jdi.Location;
import com.sun.jdi.NativeMethodException;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.EventRequest;

/**
 * @see IJavaLineBreakpoint
 */
public abstract class AbstractJavaLineBreakpoint extends JavaBreakpoint implements IJavaLineBreakpoint {
	
	// Marker label String keys
	private static final String LINE= "line"; //$NON-NLS-1$
	private static final String HITCOUNT= "hitCount"; //$NON-NLS-1$
	
	/**
	 * Sets of attributes used to configure a line breakpoint
	 */
	protected static final String[] fgLineBreakpointAttributes= new String[]{IDebugConstants.ENABLED, IMarker.LINE_NUMBER, IMarker.CHAR_START, IMarker.CHAR_END};
	
	public AbstractJavaLineBreakpoint() {
		super();
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
	 * @see JavaBreakpoint#createRequest(JDIDebutTarget, ReferenceType)
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
		registerRequest(request, target);		
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
			JDIDebugPlugin.logError(e);
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
				JDIDebugPlugin.logError(e);
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
		request.putProperty(JAVA_BREAKPOINT_PROPERTY, this);								
		int hitCount= getHitCount();
		if (hitCount > 0) {
			request.addCountFilter(hitCount);
			request.putProperty(HIT_COUNT, new Integer(hitCount));
			request.putProperty(EXPIRED, Boolean.FALSE);
		}
		// Important: only enable a request after it has been configured
		updateEnabledState(request);
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



