package org.eclipse.jdt.internal.debug.core.breakpoints;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.debug.core.IJavaMethodEntryBreakpoint;
import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;
import org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget;
import org.eclipse.jdt.internal.debug.core.model.JDIThread;

import com.sun.jdi.Method;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.MethodEntryEvent;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.MethodEntryRequest;

public class JavaMethodEntryBreakpoint extends JavaLineBreakpoint implements IJavaMethodEntryBreakpoint {
	
	private static final String JAVA_METHOD_ENTRY_BREAKPOINT = "org.eclipse.jdt.debug.javaMethodEntryBreakpointMarker"; //$NON-NLS-1$
	
	/**
	 * Breakpoint attribute storing the name of the method
	 * in which a breakpoint is contained.
	 * (value <code>"org.eclipse.jdt.debug.core.methodName"</code>). This attribute is a <code>String</code>.
	 */
	private static final String METHOD_NAME = "org.eclipse.jdt.debug.core.methodName"; //$NON-NLS-1$	
	
	/**
	 * Breakpoint attribute storing the signature of the method
	 * in which a breakpoint is contained.
	 * (value <code>"org.eclipse.jdt.debug.core.methodSignature"</code>). This attribute is a <code>String</code>.
	 */
	private static final String METHOD_SIGNATURE = "org.eclipse.jdt.debug.core.methodSignature"; //$NON-NLS-1$	
	
	/**
	 * Caches the name and signature of the method in which this breakpoint is installed
	 * Array entries are:
	 * <ol>
	 * <li>String[0] - name</li>
	 * <li>String[1] - signature</li>
	 * </ol>
	 */
	private String[] fMethodNameSignature= null;

	public JavaMethodEntryBreakpoint() {
	}
	
	/**
	 * @see JDIDebugModel#createMethodEntryBreakpoint(IResource, String, String, String, int, int, int, int, boolean, Map)
	 */
	public JavaMethodEntryBreakpoint(final IResource resource, final String typeName, final String methodName, final String methodSignature, final int lineNumber, final int charStart, final int charEnd, final int hitCount, final boolean add, final Map attributes) throws DebugException {
		IWorkspaceRunnable wr= new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				// create the marker
				setMarker(resource.createMarker(JAVA_METHOD_ENTRY_BREAKPOINT));
				
				// add attributes
				addLineBreakpointAttributes(attributes, getModelIdentifier(), true, lineNumber, charStart, charEnd);
				addMethodNameAndSignature(attributes, methodName, methodSignature);
				addTypeNameAndHitCount(attributes, typeName, hitCount);
				
				//set attributes
				ensureMarker().setAttributes(attributes);
				
				if (add) {
					addToBreakpointManager();
				}
			}

		};
		run(wr);
	}	
	
	/**
	 * @see JavaBreakpoint#addToTarget(JDIDebugTarget)
	 */
	public void addToTarget(JDIDebugTarget target) throws CoreException {
		String className = getTypeName();
		
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
	 * Adds the method name and signature attributes to the
	 * given attribute map, and intializes the local cache
	 * of method name and signature.
	 */
	private void addMethodNameAndSignature(Map attributes, String methodName, String methodSignature) {
		attributes.put(METHOD_NAME, methodName);
		attributes.put(METHOD_SIGNATURE, methodSignature);
		fMethodNameSignature= new String[2];
		fMethodNameSignature[0]= methodName;
		fMethodNameSignature[1]= methodSignature;
	}
	
	/**
	 * @see IJDIEventListener#handleEvent(Event, JDIDebugTarget)
	 * 
	 * Method entry events are fired each time any method is invoked in a class
	 * in which a method entry breakpoint has been installed.
	 * When a method entry event is received by this breakpoint, ensure that
	 * the event has been fired by a method invocation that this breakpoint
	 * is interested in. If it is not, do nothing.
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
	private boolean doSuspend(ThreadReference threadRef, JDIDebugTarget target) {
		JDIThread thread= target.findThread(threadRef);	
		if (thread == null) {
			return true;
		} else {						
			thread.handleSuspendForBreakpoint(this);
			return false;
		}		
	}
	
	/**
	 * Method entry breakpoints simulate hit count.
	 * When a method entry event is received, decrement the hit count
	 * property on the request and suspend if the hit count reaches 0.
	 */
	private boolean handleHitCountMethodEntryBreakpoint(MethodEntryEvent event, Integer count, JDIDebugTarget target) {	
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
	 * @see IJavaMethodEntryBreakpoint#getMethodName()		
	 */
	public String getMethodName() throws CoreException {
		return ensureMarker().getAttribute(METHOD_NAME, null);
	}	
	
	/**
	 * @see IJavaMethodEntryBreakpoint#getMethodSignature()		
	 */
	public String getMethodSignature() throws CoreException {
		return ensureMarker().getAttribute(METHOD_SIGNATURE, null);
	}		
	
	/**
	 * Returns the name and signature of the method in which this
	 * breakpoint is installed.
	 * This information is initialized by <code>addMethodNameAndSignature</code>.
	 */
	protected String[] getMethodNameSignature() throws CoreException {
		return fMethodNameSignature;
	}		
}