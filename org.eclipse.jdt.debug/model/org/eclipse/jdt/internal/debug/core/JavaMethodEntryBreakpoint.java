package org.eclipse.jdt.internal.debug.core;

import java.util.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.*;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.debug.core.IJavaMethodEntryBreakpoint;

import com.sun.jdi.*;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.MethodEntryEvent;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.MethodEntryRequest;

public class JavaMethodEntryBreakpoint extends JavaLineBreakpoint implements IJavaMethodEntryBreakpoint {
	
	private static final String JAVA_METHOD_ENTRY_BREAKPOINT = "org.eclipse.jdt.debug.javaMethodEntryBreakpointMarker"; //$NON-NLS-1$
	/**
	 * Breakpoint attribute storing the handle identifier of the Java element
	 * corresponding to the method in which a breakpoint is contained
	 * (value <code>"methodHandle"</code>). This attribute is a <code>String</code>.
	 */
	private static final String METHOD_HANDLE = "methodHandle"; //$NON-NLS-1$	
	
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
				fMarker= resource.createMarker(JAVA_METHOD_ENTRY_BREAKPOINT);
				
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
		
		MethodEntryRequest request;
		try {
			request= target.getEventRequestManager().createMethodEntryRequest();
			request.addClassFilter(className);
			request.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
			int hitCount = getHitCount();
			if (hitCount > 0) {
				request.putProperty(HIT_COUNT, new Integer(hitCount));
			}		
			request.setEnabled(isEnabled());
		} catch (VMDisconnectedException e) {
			if (target.isTerminated() || target.isDisconnected()) {
				return;
			}
			JDIDebugPlugin.logError(e);
			return;
		} catch (RuntimeException e) {
			JDIDebugPlugin.logError(e);
			return;
		}
		registerRequest(request, target);

	}

	/**
	 * Update the hit count associated with this method entry breakpoint
	 * in the given request
	 */	
	protected EventRequest updateHitCount(EventRequest request, JDIDebugTarget target) throws CoreException {
		if (hasHitCountChanged(request) || (isExpired(request) && isEnabled())) {
			try {
				int hitCount = getHitCount();
				Integer hc = null;
				if (hitCount > 0) {
					hc = new Integer(hitCount);
				}
				request.putProperty(HIT_COUNT, hc);
			} catch (VMDisconnectedException e) {
				if (target.isTerminated() || target.isDisconnected()) {
					return request;
				}
				JDIDebugPlugin.logError(e);
				return request;
			} catch (RuntimeException e) {
				JDIDebugPlugin.logError(e);
			}
		}
		return request;
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
		ensureMarker().setAttribute(METHOD_HANDLE, identifier);
	}	
	
	/**
	 * Returns the <code>METHOD_HANDLE</code> attribute of the given breakpoint.
	 */
	public String getMethodHandleIdentifier() throws CoreException {
		return (String) ensureMarker().getAttribute(METHOD_HANDLE);
	}	
		
	
	/**
	 * Handles a method entry event. If this method entry event is
	 * in a method that a method entry breakpoint has been set for,
	 * dispatch the event to the correct breakpoint.
	 */
	public boolean handleEvent(Event genericEvent, JDIDebugTarget target) {
		if (!(genericEvent instanceof MethodEntryEvent)) {
			return true;
		}
		MethodEntryEvent event= (MethodEntryEvent) genericEvent;
		Method enteredMethod = event.method();
		String enteredMethodName= enteredMethod.name();
		MethodEntryRequest request = (MethodEntryRequest)event.request();
		try {
			Object[] nameSignature= getMethodNameSignature();
			if (nameSignature != null && nameSignature[0].equals(enteredMethodName) &&
				nameSignature[1].equals(enteredMethod.signature())) {
					// simulate hit count
					Integer count = (Integer)request.getProperty(HIT_COUNT);
					if (count != null) {
						return handleHitCountMethodEntryBreakpoint(event, count, target);
					} else {
						// no hit count - suspend
						return doSuspend(event.thread(), target);
					}
			} else {
				return true;
				}
		} catch (CoreException e) {
			JDIDebugPlugin.logError(e);
		}
		return true;
		
	}
	
	/**
	 * Suspend the given thread in the given target, and returns
	 * whether the thread should be suspended.
	 */
	protected boolean doSuspend(ThreadReference threadRef, JDIDebugTarget target) {
		JDIThread thread= target.findThread(threadRef);	
		if (thread == null) {
			return true;
		} else {						
			thread.handleSuspendForBreakpoint(this);
			return false;
		}		
	}
	
	protected boolean handleHitCountMethodEntryBreakpoint(MethodEntryEvent event, Integer count, JDIDebugTarget target) {	
	// decrement count and suspend if 0
		int hitCount = count.intValue();
		if (hitCount > 0) {
			hitCount--;
			count = new Integer(hitCount);
			event.request().putProperty(HIT_COUNT, count);
			if (hitCount == 0) {
				// the count has reached 0, breakpoint hit
				boolean resume = doSuspend(event.thread(), target);
				try {
					// make a note that we auto-disabled the breakpoint
					// order is important here...see methodEntryChanged
					setExpired(true);
					setEnabled(false);
				} catch (CoreException e) {
					JDIDebugPlugin.logError(e);
				}
				return resume;
			}  else {
				// count still > 0, keep running
				return true;		
			}
		} else {
			// hit count expired, keep running
			return true;
		}
	}
	
	/**
	 * @see IJavaLineBreakpoint#getMember()
	 */	
	public IMember getMember() throws CoreException {
		return getMethod();
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
			nameSignature[0]= "<init>"; //$NON-NLS-1$
		} else {
			 nameSignature[0]= aMethod.getElementName();
		}
		nameSignature[1]= aMethod.getSignature();
		return nameSignature;
	}		
}

