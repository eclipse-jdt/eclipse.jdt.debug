package org.eclipse.jdt.internal.debug.core;

import java.util.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.*;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.debug.core.IJavaDebugConstants;
import org.eclipse.jdt.debug.core.IJavaMethodEntryBreakpoint;

import com.sun.jdi.*;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.MethodEntryEvent;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.MethodEntryRequest;

public class JavaMethodEntryBreakpoint extends JavaLineBreakpoint implements IJavaMethodEntryBreakpoint {
	
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
	
	public JavaMethodEntryBreakpoint() {
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
	public JavaMethodEntryBreakpoint(final IMethod method, final int hitCount) throws DebugException {
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
				IMarker marker = ensureMarker();
				Map attributes= marker.getAttributes();
				JavaCore.addJavaElementMarkerAttributes(attributes, method);
				marker.setAttributes(attributes);
				
				// Lastly, add the breakpoint manager
				addToBreakpointManager();
			}

		};
		run(wr);
	}	
	
	/**
	 * A method entry breakpoint has been added.
     * Create or update the request.
	 */
	public void addToTarget(JDIDebugTarget target) throws CoreException {
		IType type = getType();
		String className = type.getFullyQualifiedName();
		
		MethodEntryRequest request = target.getMethodEntryRequest(className);
		
		if (request == null) {
			try {
				request= target.getEventRequestManager().createMethodEntryRequest();
				request.addClassFilter(className);
				request.putProperty(CLASS_NAME, className);
				request.putProperty(BREAKPOINT_INFO, new ArrayList(1));
				request.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
				// Store this breakpoint at the "key" breakpoint to be dispatched to when
				// an event comes back for this request
				request.putProperty(JDIDebugPlugin.JAVA_BREAKPOINT_PROPERTY, this);
				// Create the list of method entry breakpoints (including this one) in the request
				request.putProperty(IJavaDebugConstants.JAVA_METHOD_ENTRY_BREAKPOINT, new ArrayList(3));				
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
		
		List breakpoints= (List)request.getProperty(IJavaDebugConstants.JAVA_METHOD_ENTRY_BREAKPOINT);
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
	public void changeForTarget(JDIDebugTarget target) throws CoreException  {
		MethodEntryRequest request = (MethodEntryRequest)target.getRequest(this);
		if (request == null) {
			return;
		}
		// check the enabled state
		updateEnabledState(request);
		updateHitCount(request);
	}

	/**
	 * Update the hit count associated with this method entry breakpoint
	 * in the given request
	 */	
	private void updateHitCount(MethodEntryRequest request) throws CoreException {
		if (!isEnabled()) {
			// Don't reset the request hitCount it this breakpoint is disabled
			return;
		}
		List breakpoints= (List) request.getProperty(IJavaDebugConstants.JAVA_METHOD_ENTRY_BREAKPOINT);
		int index= breakpoints.indexOf(this);
		// update the breakpoint's hit count
		List hitCounts= (List)request.getProperty(IJavaDebugConstants.HIT_COUNT);
		int hitCount= getHitCount();
		if (hitCount > 0) {
			hitCounts.set(index, new Integer(hitCount));
		} else {
			//back to a regular breakpoint
			hitCounts.set(index, null);			
		}		
	}
	
	/**
	 * Enable a request as appropriate and increment the install count of the associated breakpoint.
	 */
	protected void completeConfiguration(EventRequest request) {
		// Important: Enable only after request has been configured		
		if (!(request instanceof MethodEntryRequest)) {
			return;
		}
		try {	
			updateEnabledState((MethodEntryRequest) request);
			incrementInstallCount();
		} catch (CoreException e) {
			logError(e);
		}
	}		
	
	/**
	 * Update the enabled state of the request associated with this
	 * method entry breakpoint. Since a request is potentially associated
	 * with multiple method entry breakpoints, it should be enabled if 
	 * any of them are enabled.
	 */
	protected void updateEnabledState(MethodEntryRequest request) throws CoreException {
		IBreakpointManager manager= DebugPlugin.getDefault().getBreakpointManager();
		Iterator breakpoints= ((List)request.getProperty(IJavaDebugConstants.JAVA_METHOD_ENTRY_BREAKPOINT)).iterator();
		boolean requestEnabled= false;
		while (breakpoints.hasNext()) {
			JavaMethodEntryBreakpoint breakpoint= (JavaMethodEntryBreakpoint)breakpoints.next();
			if (breakpoint.isEnabled()) {
				requestEnabled = true;
				break;
			}
		}
		updateEnabledState(request, requestEnabled);
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
			List breakpoints= (List)request.getProperty(IJavaDebugConstants.JAVA_METHOD_ENTRY_BREAKPOINT);
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
				
				// Fixup the list of breakpoints in the request
				JavaMethodEntryBreakpoint breakpoint= (JavaMethodEntryBreakpoint) request.getProperty(JDIDebugPlugin.JAVA_BREAKPOINT_PROPERTY);
				if (breakpoint == this) {
					// We were the "key" breakpoint in the request. select a
					// new key for the request
					Iterator breakpointIterator= breakpoints.iterator();
					JavaMethodEntryBreakpoint newKey= (JavaMethodEntryBreakpoint) breakpointIterator.next();
					request.putProperty(JDIDebugPlugin.JAVA_BREAKPOINT_PROPERTY, newKey);
				}
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
		ensureMarker().setAttribute(IJavaDebugConstants.METHOD_HANDLE, identifier);
	}	
	
	/**
	 * Handles a method entry event. If this method entry event is
	 * in a method that a method entry breakpoint has been set for,
	 * dispatch the event to the correct breakpoint.
	 */
	public void handleEvent(Event genericEvent, JDIDebugTarget target) throws CoreException {
		if (!(genericEvent instanceof MethodEntryEvent)) {
			return;
		}
		MethodEntryEvent event= (MethodEntryEvent) genericEvent;
		Method enteredMethod = event.method();
		String enteredMethodName= enteredMethod.name();
		MethodEntryRequest request = (MethodEntryRequest)event.request();
		List breakpoints = (List)request.getProperty(IJavaDebugConstants.JAVA_METHOD_ENTRY_BREAKPOINT);
		Iterator requestBreakpoints= breakpoints.iterator();
		JavaMethodEntryBreakpoint breakpoint= null;
		int index= 0;
		while (requestBreakpoints.hasNext()) {
			JavaMethodEntryBreakpoint aBreakpoint= (JavaMethodEntryBreakpoint)requestBreakpoints.next();
			Object[] nameSignature= aBreakpoint.getMethodNameSignature();
			if (nameSignature != null && nameSignature[0].equals(enteredMethodName) &&
				nameSignature[1].equals(enteredMethod.signature())) {
				breakpoint= aBreakpoint;
				break;
			}
			index++;	
		}
		if (breakpoint == null || !breakpoint.isEnabled()) {
			doResume(event.thread(), target);
			return;
		}	
		
		List counts = (List)request.getProperty(IJavaDebugConstants.HIT_COUNT);
		Integer count= (Integer)counts.get(index);
		if (count != null) {
			breakpoint.handleHitCountMethodEntryBreakpoint(event, counts, count, index, target);
		} else {
			// no hit count - suspend
			breakpoint.doSuspend(event.thread(), target);
		}
	}
	
	/**
	 * Resume the given thread in the given target
	 */
	protected void doResume(ThreadReference thread, JDIDebugTarget target) {
		if (!target.hasPendingEvents()) {
			target.resume(thread);
		}
	}
	
	/**
	 * Suspend the given thread in the given target
	 */
	protected void doSuspend(ThreadReference threadRef, JDIDebugTarget target) {
		JDIThread thread= target.findThread(threadRef);	
		if (thread == null) {
			target.resume(threadRef);
			return;
		} else {						
			thread.handleSuspendForBreakpoint(this);
		}		
	}
	
	protected void handleHitCountMethodEntryBreakpoint(MethodEntryEvent event, List counts, Integer count, int index, JDIDebugTarget target) {	
	// decrement count and suspend if 0
		int hitCount = count.intValue();
		if (hitCount > 0) {
			hitCount--;
			count = new Integer(hitCount);
			counts.set(index, count);
			if (hitCount == 0) {
				// the count has reached 0, breakpoint hit
				counts.set(index, null);
				doSuspend(event.thread(), target);
				try {
					// make a note that we auto-disabled the breakpoint
					// order is important here...see methodEntryChanged
					setExpired(true);
					setEnabled(false);
				} catch (CoreException e) {
					logError(e);
				}
			}  else {
				// count still > 0, keep running
				doResume(event.thread(), target);		
			}
		} else {
			// hit count expired, keep running
			doResume(event.thread(), target);
		}
	}
	
	/**
	 * @see IJavaLineBreakpoint#getMethod()		
	 */
	public IMethod getMethod() throws CoreException {
		String handle = getMethodHandleIdentifier();
		if (handle != null) {
			return (IMethod)JavaCore.create(handle);
		}
		return null;
	}	
	
	protected String[] getMethodNameSignature() throws CoreException {
		String[] nameSignature= new String[2];
		IMethod aMethod= getMethod(); 
		if (aMethod.isConstructor()) {
			nameSignature[0]= "<init>";
		} else {
			 nameSignature[0]= aMethod.getElementName();
		}
		nameSignature[1]= aMethod.getSignature();
		return nameSignature;
	}	
	
	protected String[] getMethodEntryBreakpointInfo(MethodEntryRequest request, int index) throws CoreException {
		List nameSignatures = (List)request.getProperty(BREAKPOINT_INFO);
		if (nameSignatures.get(index) != null) {
			return (String[])nameSignatures.get(index);
		}
		String[] nameSignature= new String[2];
		IMethod aMethod= getMethod(); 
		if (aMethod.isConstructor()) {
			nameSignature[0]= "<init>";
		} else {
			 nameSignature[0]= aMethod.getElementName();
		}
		nameSignature[1]= aMethod.getSignature();
		nameSignatures.add(index, nameSignature);
		return nameSignature;
	}
		

}

