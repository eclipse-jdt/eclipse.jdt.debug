package org.eclipse.jdt.internal.debug.core;

import java.util.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.*;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.debug.core.IJavaDebugConstants;

import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.MethodEntryRequest;

public class MethodEntryBreakpoint extends LineBreakpoint {
	
	static String fMarkerType= IJavaDebugConstants.JAVA_METHOD_ENTRY_BREAKPOINT;

	/**
	 * Key used to store the class name attribute pertinent to a
	 * specific method entry request. Used for method entry breakpoints.
	 */
	public final static String CLASS_NAME= "className";
	/**
	 * Key used to store the name and signature
	 * attribute pertinent to a specific method 
	 * entry request breakpoint. Used for method entry breakpoints.
	 */
	public final static String BREAKPOINT_INFO= "breakpointInfo";
	
	/**
	 * Create a method entry breakpoint on the given marker
	 */
	public MethodEntryBreakpoint(IMarker marker) {
		fMarker= marker;
	}
	
	/**
	 * Creates and returns a method entry breakpoint in the
	 * given method.
	 * If hitCount is > 0, the breakpoint will suspend execution when it is
	 * "hit" the specified number of times. Note: the breakpoint is not
	 * added to the breakpoint manager - it is merely created.
	 *
	 * @param method the method in which to suspend on entry
	 * @param hitCount the number of times the breakpoint will be hit before
	 *   suspending execution - 0 if it should always suspend
	 * @return a method entry breakpoint
	 * @exception DebugException if unable to create the breakpoint marker due
	 *  to a lower level exception.
	 */
	public MethodEntryBreakpoint(final IMethod method, final int hitCount) throws DebugException {
		IWorkspaceRunnable wr= new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				// determine the resource to associate the marker with				
				IResource resource= null;
				resource= method.getUnderlyingResource();
				if (resource == null) {
					resource= method.getJavaProject().getProject();
				}

				// create the marker
				fMarker= resource.createMarker(fMarkerType);
				
				// find the source range if available
				int start = -1;
				int end = -1;
				ISourceRange range = method.getSourceRange();
				if (range != null) {
					start = range.getOffset();
					end = start + range.getLength() - 1;
				}
				// configure the standard attributes
				setLineBreakpointAttributes(getPluginIdentifier(), true, -1, start, end);
				// configure the type handle and hit count
				setTypeAndHitCount(method.getDeclaringType(), hitCount);

				// configure the method handle
				setMethod(method);
				
				// configure the marker as a Java marker
				Map attributes= getAttributes();
				JavaCore.addJavaElementMarkerAttributes(attributes, method);
				setAttributes(attributes);
			}

		};
		run(wr);
	}	
	
	/**
	 * A method entry breakpoint has been added.
     * Create or update the request.
	 */
	public void addToTarget(JDIDebugTarget target) {
		IType type = getBreakpointType();
		if (type == null) {
//			internalError(ERROR_BREAKPOINT_NO_TYPE);
			return;
		}
		String className = type.getFullyQualifiedName();
		
		MethodEntryRequest request = target.getMethodEntryRequest(className);
		
		if (request == null) {
			try {
				request= target.getEventRequestManager().createMethodEntryRequest();
				request.addClassFilter(className);
				request.putProperty(CLASS_NAME, className);
				request.putProperty(BREAKPOINT_INFO, new ArrayList(1));
				request.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
				request.putProperty(IDebugConstants.BREAKPOINT_MARKER, new ArrayList(3));
				request.putProperty(IJavaDebugConstants.HIT_COUNT, new ArrayList(3));
			} catch (VMDisconnectedException e) {
				return;
			} catch (RuntimeException e) {
				logError(e);
				return;
			}
		}
		List breakpointInfo= (List)request.getProperty(BREAKPOINT_INFO);
		breakpointInfo.add(null);
		
		List breakpoints= (List)request.getProperty(IDebugConstants.BREAKPOINT);
		breakpoints.add(this);
		
		
		List hitCounts = (List)request.getProperty(IJavaDebugConstants.HIT_COUNT);
		int hitCount = getHitCount();
		if (hitCount > 0) {
			hitCounts.add(new Integer(hitCount));
		} else {
			hitCounts.add(null);
		}
		completeConfiguration(request);
		target.installBreakpoint(this, request);
	}
	
	/**
	 * A method entry breakpoint has been changed.
	 * Update the request.
	 */
	public void changeForTarget(JDIDebugTarget target) {
		MethodEntryRequest request = (MethodEntryRequest)target.getRequest(this);
		if (request == null) {
			return;
		}
		// check the enabled state
		updateMethodEntryEnabledState(request);
		
		List breakpoints= (List)request.getProperty(IDebugConstants.BREAKPOINT);
		int index= breakpoints.indexOf(this);
		// update the breakpoints hit count
		int newCount = getHitCount();
		List hitCounts= (List)request.getProperty(IJavaDebugConstants.HIT_COUNT);
		if (newCount > 0) {
			hitCounts.set(index, new Integer(newCount));
		} else {
			//back to a regular breakpoint
			hitCounts.set(index, null);			
		}
	}
	
	/**
	 * Update the enabled state of the request associated with this
	 * method entry breakpoint. Since a request is potentially associated
	 * with multiple method entry breakpoints, it should be enabled if 
	 * any of them are enabled.
	 */
	protected void updateMethodEntryEnabledState(MethodEntryRequest request)  {
		IBreakpointManager manager= DebugPlugin.getDefault().getBreakpointManager();
		Iterator breakpoints= ((List)request.getProperty(IDebugConstants.BREAKPOINT)).iterator();
		boolean requestEnabled= false;
		while (breakpoints.hasNext()) {
			JavaBreakpoint breakpoint= (JavaBreakpoint)breakpoints.next();
			if (breakpoint.isEnabled()) {
				requestEnabled = true;
				break;
			}
		}
		updateEnabledState0(request, requestEnabled);
	}
	
	/**
	 * @see JavaBreakpoint#removeFromTarget(JDIDebugTarget)
	 */
	public void removeFromTarget(JDIDebugTarget target) {
		MethodEntryRequest request = (MethodEntryRequest)target.getRequest(this);
		if (request != null) {
			try {
				decrementInstallCount();
			} catch (CoreException e) {
				logError(e);
			}
			List breakpoints= (List)request.getProperty(IDebugConstants.BREAKPOINT);
			int index = breakpoints.indexOf(this);
			breakpoints.remove(index);
			if (breakpoints.isEmpty()) {
				try {
					target.getEventRequestManager().deleteEventRequest(request); // disable & remove
				} catch (VMDisconnectedException e) {
				} catch (RuntimeException e) {
					logError(e);
				}
			} else {
				List hitCounts= (List)request.getProperty(IJavaDebugConstants.HIT_COUNT);
				hitCounts.remove(index);
			}
		}
	}	
	
	/**
	 * Sets the <code>METHOD_HANDLE</code> attribute of this breakpoint, associated
	 * with the given IMethod.
	 */
	public void setMethod(IMethod method) throws CoreException {
		String handle = method.getHandleIdentifier();
		setMethodHandleIdentifier(handle);
	}
	
	
	/**
	 * Sets the <code>METHOD_HANDLE</code> attribute of this breakpoint.
	 */
	public void setMethodHandleIdentifier(String identifier) throws CoreException {
		setAttribute(IJavaDebugConstants.METHOD_HANDLE, identifier);
	}	

}

