package org.eclipse.jdt.internal.debug.core.breakpoints;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */ 
 
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.model.Breakpoint;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.internal.debug.core.IJDIEventListener;
import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;
import org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget;
import org.eclipse.jdt.internal.debug.core.model.JDIThread;

import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.event.ClassPrepareEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.LocatableEvent;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.EventRequest;

public abstract class JavaBreakpoint extends Breakpoint implements IJavaBreakpoint, IJDIEventListener, IDebugEventSetListener {

	/**
	 * Breakpoint attribute storing the expired value (value <code>"org.eclipse.jdt.debug.core.expired"</code>).
	 * This attribute is stored as a <code>boolean</code>. Once a hit count has
	 * been reached, a breakpoint is considered to be "expired".
	 */
	protected static final String EXPIRED = "org.eclipse.jdt.debug.core.expired"; //$NON-NLS-1$
	/**
	 * Breakpoint attribute storing a breakpoint's hit count value
	 * (value <code>"org.eclipse.jdt.debug.core.hitCount"</code>). This attribute is stored as an
	 * <code>int</code>.
	 */
	protected static final String HIT_COUNT = "org.eclipse.jdt.debug.core.hitCount"; //$NON-NLS-1$
	/**
	 * Breakpoint attribute storing the number of debug targets a
	 * breakpoint is installed in (value <code>"org.eclipse.jdt.debug.core.installCount"</code>).
	 * This attribute is a <code>int</code>.
	 */
	protected static final String INSTALL_COUNT = "org.eclipse.jdt.debug.core.installCount"; //$NON-NLS-1$	
	
	/**
	 * Breakpoint attribute storing the fully qualified name of the type
	 * this breakpoint is located in.
	 * (value <code>"org.eclipse.jdt.debug.core.typeName"</code>). This attribute is a <code>String</code>.
	 */
	protected static final String TYPE_NAME = "org.eclipse.jdt.debug.core.typeName"; //$NON-NLS-1$		
	
	/**
	 * Breakpoint attribute storing suspend policy code for 
	 * this breakpoint.
	 * (value <code>"org.eclipse.jdt.debug.core.suspendPolicy</code>).
	 * This attribute is an <code>int</code> correspoinding
	 * to <code>IJavaBreakpoint.SUSPEND_VM</code> or
	 * <code>IJavaBreakpoint.SUSPEND_THREAD</code>.
	 */
	protected static final String SUSPEND_POLICY = "org.eclipse.jdt.debug.core.suspendPolicy"; //$NON-NLS-1$			
	
	/**
	 * Stores the collection of requests that this breakpoint has installed in
	 * debug targets.
	 * key: a debug target
	 * value: the requests this breakpoint has installed in that target
	 */
	protected HashMap fRequestsByTarget;
	
	/**
	 * The list of threads (ThreadReference objects) in which this breakpoint will suspend,
	 * associated with the target in which each thread exists (JDIDebugTarget).
	 * key: targets the debug targets (IJavaDebugTarget)
	 * value: thread the filtered thread (IJavaThread) in the given target
	 */
	protected Map fFilteredThreadsByTarget;
	
	/**
	 * Propery identifier for a breakpoint object on an event request
	 */
	public static final String JAVA_BREAKPOINT_PROPERTY = "org.eclipse.jdt.debug.breakpoint"; //$NON-NLS-1$
	
	/**
	 * JavaBreakpoint attributes
	 */	
	protected static final String[] fgExpiredEnabledAttributes= new String[]{EXPIRED, ENABLED};
	
	public JavaBreakpoint() {
		fRequestsByTarget = new HashMap(1);
		fFilteredThreadsByTarget= new HashMap(1);
	}	
	
	/**
	 * @see IBreakpoint#getModelIdentifier()
	 */
	public String getModelIdentifier() {
		return JDIDebugModel.getPluginIdentifier();
	}

	/**
	 * @see IBreakpoint#setMarker(IMarker)
	 */
	public void setMarker(IMarker marker) throws CoreException {
		super.setMarker(marker);
		configureAtStartup();
	}

	/**
	 * Add this breakpoint to the breakpoint manager,
	 * or sets it as unregistered.
	 */
	protected void register(boolean register) throws CoreException {
		if (register) {
			DebugPlugin.getDefault().getBreakpointManager().addBreakpoint(this);
		} else {
			setRegistered(false);
		}
	}	
	
	/**
	 * Add the given event request to the given debug target. If 
	 * the request is the breakpoint request associated with this 
	 * breakpoint, increment the install count.
	 */
	protected void registerRequest(EventRequest request, JDIDebugTarget target) throws CoreException {
		if (request == null) {
			return;
		}
		List reqs = getRequests(target);
		if (reqs.isEmpty()) {
			fRequestsByTarget.put(target, reqs);
		}
		reqs.add(request);
		target.addJDIEventListener(this, request);
		// update the install attibute on the breakpoint
		if (!(request instanceof ClassPrepareRequest)) {
			incrementInstallCount();
			// notification 
			fireInstalled(target);
		}
	}
	
	/**
	 * Returns a String corresponding to the reference type
	 * name to the top enclosing type in which this breakpoint
	 * is located or <code>null</code> if no reference type could be
	 * found.
	 */
	protected String getEnclosingReferenceTypeName() throws CoreException {
		String name= getTypeName();
		int index = name.indexOf('$');
		if (index == -1) {
			return name;
		} else {
			return name.substring(0, index);
		}
	}	
		
	/**
	 * Returns the requests that this breakpoint has installed
	 * in the given target.
	 */
	protected List getRequests(JDIDebugTarget target) {
		List list= (List)fRequestsByTarget.get(target);
		if (list == null) {
			list= new ArrayList(2);
		}
		return list;
	}
	
	/**
	 * Remove the given request from the given target. If the request
	 * is the breakpoint request associated with this breakpoint,
	 * decrement the install count.
	 */
	protected void deregisterRequest(EventRequest request, JDIDebugTarget target) throws CoreException {
		target.removeJDIEventListener(this, request);
		// A request may be getting deregistered because the breakpoint has
		// been deleted. It may be that this occurred because of a marker deletion.
		// Don't try updating the marker (decrementing the install count) if
		// it no longer exists.
		if (!(request instanceof ClassPrepareRequest) && getMarker().exists()) {
			decrementInstallCount();
		}
	}

	/**
	 * Removes the <code>oldRequest</code> from the given target if present
	 * and replaces it with <code>newRequest</code>
	 */
	protected void replaceRequest(JDIDebugTarget target, EventRequest oldRequest, EventRequest newRequest) {
		target.removeJDIEventListener(this, oldRequest);
		target.addJDIEventListener(this, newRequest);
		// delete old request
		//on JDK you cannot delete (disable) an event request that has hit its count filter
		if (!isExpired(oldRequest)) {
			target.getEventRequestManager().deleteEventRequest(oldRequest); // disable & remove
		}			
	}

	/**
	 * @see IJDIEventListener#handleEvent(Event, JDIDebugTarget)
	 */
	public boolean handleEvent(Event event, JDIDebugTarget target) {
		if (event instanceof ClassPrepareEvent) {
			return handleClassPrepareEvent((ClassPrepareEvent)event, target);
		} else {
			return handleBreakpointEvent(event, target);
		}		
	}

	/**
	 * Handle the given class prepare event, which was generated by the
	 * class prepare event installed in the given target by this breakpoint.
	 * 
	 * If the class which has been loaded is a class in which this breakpoint
	 * should install, create a breakpoint request for that class.
	 */	
	public boolean handleClassPrepareEvent(ClassPrepareEvent event, JDIDebugTarget target) {
		try {
			if (!installableReferenceType(event.referenceType())) {
				// Don't install this breakpoint in an
				// inappropriate type
				return true;
			}			
			createRequest(target, event.referenceType());
		} catch (CoreException e) {
			JDIDebugPlugin.log(e);
		}
		return true;
	}
	
	/**
	 * @see IJDIEventListener#handleEvent(Event, JDIDebugTarget)
	 * 
	 * Handle the given event, which was generated by the breakpoint request
	 * installed in the given target by this breakpoint.
	 */
	public boolean handleBreakpointEvent(Event event, JDIDebugTarget target) {
		ThreadReference threadRef= ((LocatableEvent)event).thread();
		JDIThread thread= target.findThread(threadRef);		
		if (thread == null) {
			return true;
		} else {
			expireHitCount(event);
			return !suspend(thread); // Resume if suspend fails
		}	
	}
	
	/**
	 * Deletegates to the given thread to suspend, and
	 * returns whether the thread suspended
	 * It is possible that the thread will not suspend
	 * as directed by a Java breakpoint listener.
	 * 
	 * @see IJavaBreakpointListener#breakpointHit(IJavaThread, IJavaBreakpoint)
	 */
	protected boolean suspend(JDIThread thread) {
		return thread.handleSuspendForBreakpoint(this);
	}
	
	/**
	 * Returns whether the given reference type is appropriate for this
	 * breakpoint to be installed in.
	 */
	protected boolean installableReferenceType(ReferenceType type) throws CoreException {
		String installableType= getTypeName();
		String queriedType= type.name();
		if (installableType == null || queriedType == null) {
			return false;
		}
		if (installableType.equals(queriedType)) {
			return true;
		}
		int index= queriedType.indexOf('$', 0);
		if (index == -1) {
			return false;
		}
		return installableType.regionMatches(0, queriedType, 0, index);
		
	}
	
	/**
	 * Called when a breakpoint event is encountered
	 */
	public void expireHitCount(Event event) {
		EventRequest request= event.request();
		Integer requestCount= (Integer) request.getProperty(HIT_COUNT);
		if (requestCount != null) {
			try {
				request.putProperty(EXPIRED, Boolean.TRUE);
				setEnabled(false);
				// make a note that we auto-disabled this breakpoint.
				setExpired(true);
			} catch (CoreException ce) {
				JDIDebugPlugin.log(ce);
			}
		}
	}	

	/**
	 * Attempts to create a breakpoint request for this breakpoint in the given
	 * reference type in the given target.
	 * 
	 * @return Whether a request was created
	 */
	protected boolean createRequest(JDIDebugTarget target, ReferenceType type) throws CoreException {
		EventRequest request= newRequest(target, type);
		if (request == null) {
			return false;
		}
		registerRequest(request, target);
		return true;
	}
	
	/**
	 * Configure a breakpoint request with common properties:
	 * <ul>
	 * <li><code>JAVA_BREAKPOINT_PROPERTY</code></li>
	 * <li><code>HIT_COUNT</code></li>
	 * <li><code>EXPIRED</code></li>
	 * </ul>
	 * and sets the suspend policy of the request to suspend 
	 * the event thread.
	 */
	protected void configureRequest(EventRequest request, JDIDebugTarget target) throws CoreException {
		request.setSuspendPolicy(getJDISuspendPolicy());
		request.putProperty(JAVA_BREAKPOINT_PROPERTY, this);
		configureRequestThreadFilter(request, target);
		configureRequestHitCount(request);
		// Important: only enable a request after it has been configured
		updateEnabledState(request);
	}
	
	/**
	 * Configure the thread filter property of the given request.
	 */
	protected void configureRequestThreadFilter(EventRequest request, JDIDebugTarget target) {
		IJavaThread thread= (IJavaThread)fFilteredThreadsByTarget.get(target);
		if (thread == null || (!(thread instanceof JDIThread))) {
			return;
		}
		setRequestThreadFilter(request, ((JDIThread)thread).getUnderlyingThread());
	}
	
	/**
	 * Configure the given request's hit count
	 */
	protected void configureRequestHitCount(EventRequest request) throws CoreException {
		int hitCount= getHitCount();
		if (hitCount > 0) {
			request.addCountFilter(hitCount);
			request.putProperty(HIT_COUNT, new Integer(hitCount));
			request.putProperty(EXPIRED, Boolean.FALSE);
		}
	}
	
	/**
	 * Creates and returns a breakpoint request for this breakpoint which
	 * has been installed in the given reference type and registered
	 * in the given target.
	 * 
	 * @return the event request which was created or <code>null</code> if
	 *  the request creation failed
	 */
	protected abstract EventRequest newRequest(JDIDebugTarget target, ReferenceType type) throws CoreException;
	
	/**
	 * Add this breakpoint to the given target. After it has been
	 * added to the given target, this breakpoint will suspend
	 * execution of that target as appropriate.
	 */
	public void addToTarget(JDIDebugTarget target) throws CoreException {
		
		// pre-notification
		fireAdded(target);
		
		String referenceTypeName= getTypeName();
		String enclosingTypeName= getEnclosingReferenceTypeName();
		if (referenceTypeName == null || enclosingTypeName == null) {
			return;
		}
		
		// create request to listen to class loads
		if (referenceTypeName.indexOf('$') == -1) {
			registerRequest(target.createClassPrepareRequest(enclosingTypeName), target);
			//register to ensure we here about local and anonymous inner classes
			registerRequest(target.createClassPrepareRequest(enclosingTypeName + "$*"), target);  //$NON-NLS-1$
		} else {
			registerRequest(target.createClassPrepareRequest(referenceTypeName), target);
			registerRequest(target.createClassPrepareRequest(enclosingTypeName + "$*", referenceTypeName), target);  //$NON-NLS-1$
		}
		
		// create breakpoint requests for each class currently loaded
		List classes= target.jdiClassesByName(referenceTypeName);
		if (classes.isEmpty() && enclosingTypeName.equals(referenceTypeName)) {
			return;
		} 
		
		boolean success= false;
		Iterator iter = classes.iterator();
		while (iter.hasNext()) {
			ReferenceType type= (ReferenceType) iter.next();
			if (createRequest(target, type)) {
				success= true;
			}
		}
		
		if (!success) {
			addToTargetForLocalType(target, enclosingTypeName);
		}
	}
	
	/**
	 * Local types (types defined in methods) are handled specially due to the
	 * different types that the local type is associated with as well as the 
	 * performance problems of using ReferenceType#nestedTypes.  From the Java 
	 * model perspective a local type is defined within a method of a type.  
	 * Therefore the type of a breakpoint placed in a local type is the type
	 * that encloses the method where the local type was defined.
	 * The local type is enclosed within the top level type according
	 * to the VM.
	 * So if "normal" attempts to create a request when a breakpoint is
	 * being added to a target, we must be dealing with a local type and therefore resort
	 * to looking up all of the nested types of the top level enclosing type.
	 */
	protected void addToTargetForLocalType(JDIDebugTarget target, String enclosingTypeName) throws CoreException {
		List classes= target.jdiClassesByName(enclosingTypeName);
		if (!classes.isEmpty()) {
			Iterator iter = classes.iterator();
			while (iter.hasNext()) {
				ReferenceType type= (ReferenceType) iter.next();
				Iterator nestedTypes= type.nestedTypes().iterator();
				while (nestedTypes.hasNext()) {
					ReferenceType nestedType= (ReferenceType) nestedTypes.next();
					if (createRequest(target, nestedType)) {
						break;
					}				
				}
			}
		}
	}
	
	/**
	 * Update all requests that this breakpoint has installed in the
	 * given target to reflect the current state of this breakpoint.
	 */
	public void changeForTarget(JDIDebugTarget target) throws CoreException {
		List requests = getRequests(target);
		if (!requests.isEmpty()) {
			ListIterator iter = requests.listIterator();
			EventRequest req;
			while (iter.hasNext()) {
				req = (EventRequest)iter.next();
				if (!(req instanceof ClassPrepareRequest)) {
					EventRequest newRequest= updateRequest(req, target);
					if (newRequest != req) {
						iter.set(newRequest);
					}
				}
			}
		}
	}

	/**
	 * Update the given request in the given target to reflect
	 * the current state of this breakpoint.  Returns the updated
	 * request
	 */
	protected EventRequest updateRequest(EventRequest request, JDIDebugTarget target) throws CoreException {
		updateEnabledState(request);
		updateSuspendPolicy(request, target);
		EventRequest newRequest = updateHitCount(request, target);
		if (newRequest != request) {
			replaceRequest(target, request, newRequest);
			return newRequest;
		}
		return request;
	}
	
	/**
	 * Update the given request in the given debug target to
	 * reflect the current hit count of this breakpoint.
	 */
	protected EventRequest updateHitCount(EventRequest request, JDIDebugTarget target) throws CoreException {
		// if the hit count has changed, or the request has expired and is being re-enabled,
		// create a new request
		if (hasHitCountChanged(request) || (isExpired(request) && this.isEnabled())) {
			request= recreateRequest(request, target);
		}
		return request;
	}
	
	/**
	 * Create a request that reflects the current state of this breakpoint.
	 * The returned request will be installed in the same type as the given
	 * request.
	 */
	protected abstract EventRequest recreateRequest(EventRequest request, JDIDebugTarget target) throws CoreException;
	
	/**
	 * Update the given request in the given debug target to
	 * reflect the current suspend policy of this breakpoint.
	 */
	protected void updateSuspendPolicy(EventRequest request, JDIDebugTarget target) throws CoreException {
		int breakpointPolicy = getSuspendPolicy();
		int requestPolicy = request.suspendPolicy();
		if (requestPolicy == EventRequest.SUSPEND_EVENT_THREAD && breakpointPolicy == IJavaBreakpoint.SUSPEND_THREAD) {
			return;
		}
		if (requestPolicy == EventRequest.SUSPEND_ALL && breakpointPolicy == IJavaBreakpoint.SUSPEND_VM) {
			return;
		}
		try {
			switch (breakpointPolicy) {
				case IJavaBreakpoint.SUSPEND_THREAD :
					request.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
					break;
				case IJavaBreakpoint.SUSPEND_VM :
					request.setSuspendPolicy(EventRequest.SUSPEND_ALL);
					break;					
			} 
		} catch (RuntimeException e) {
			JDIDebugPlugin.log(e);
		}
	}
	
	/**
	 * Returns the JDI suspend policy that corresponds to this
	 * breakpoint's suspend policy
	 * 
	 * @return the JDI suspend policy that corresponds to this
	 *  breakpoint's suspend policy
	 * @exception CoreException if unable to access this breakpoint's
	 *  suspend policy setting
	 */
	protected int getJDISuspendPolicy() throws CoreException {
		int breakpointPolicy = getSuspendPolicy();
		if (breakpointPolicy == IJavaBreakpoint.SUSPEND_THREAD) {
			return EventRequest.SUSPEND_EVENT_THREAD;
		}
		return EventRequest.SUSPEND_ALL;
	}

	
	/**
	 * Returns whether the hitCount of this breakpoint is equal to the hitCount of
	 * the associated request.
	 */
	protected boolean hasHitCountChanged(EventRequest request) throws CoreException {
		int hitCount= getHitCount();
		Integer requestCount= (Integer) request.getProperty(HIT_COUNT);
		int oldCount = -1;
		if (requestCount != null)  {
			oldCount = requestCount.intValue();
		} 
		return hitCount != oldCount;
	}
	
	/**
	 * Remove all requests that this breakpoint has installed in the given
	 * debug target.
	 */
	public void removeFromTarget(final JDIDebugTarget target) throws CoreException {
		IWorkspaceRunnable runnable= new IWorkspaceRunnable() {
			// This operation is wrapped in a workspace runnable so that the
			// marker changes which result from deregistering a request are
			// fired after we're done iterating over the requests.
			// Failing to do so can result in a ConcurrentModificationException
			public void run(IProgressMonitor monitor) throws CoreException {
				List requests = getRequests(target);
				Iterator iter = requests.iterator();
				EventRequest req;
				while (iter.hasNext()) {
					req = (EventRequest)iter.next();
					try {				
						if (target.isAvailable() && !isExpired(req)) { // cannot delete an expired request
							target.getEventRequestManager().deleteEventRequest(req); // disable & remove
						}
					} catch (VMDisconnectedException e) {
						if (target.isAvailable()) {
							JDIDebugPlugin.log(e);
						}
					} catch (RuntimeException e) {
						JDIDebugPlugin.log(e);
					} finally {
						deregisterRequest(req, target);
					}
				}
			}
		};
		runnable.run(null);
		fRequestsByTarget.remove(target);
		fFilteredThreadsByTarget.remove(target);
		
		// notification
		fireRemoved(target);
	}		
	
	/**
	 * Update the enabled state of the given request, which is associated
	 * with this breakpoint. Set the enabled state of the request
	 * to the enabled state of this breakpoint.
	 */
	protected void updateEnabledState(EventRequest request) throws CoreException  {
		boolean enabled = isEnabled();
		if (request.isEnabled() != enabled) {
			// change the enabled state
			try {
				// if the request has expired, do not disable.
				// BreakpointRequests that have expired cannot be deleted.
				if (!isExpired(request)) {
					request.setEnabled(enabled);
				}
			} catch (VMDisconnectedException e) {
			} catch (RuntimeException e) {
				JDIDebugPlugin.log(e);
			}
		}
	}
	
	/**
	 * Returns whether this breakpoint has expired.
	 */
	public boolean isExpired() throws CoreException {
		return ensureMarker().getAttribute(EXPIRED, false);
	}	
	
	/**
	 * Returns whether the given request is expired
	 */
	protected boolean isExpired(EventRequest request) {
		Boolean requestExpired= (Boolean) request.getProperty(EXPIRED);
		if (requestExpired == null) {
				return false;
		}
		return requestExpired.booleanValue();
	}
	
	/**
	 * @see IJavaBreakpoint#isInstalled()
	 */
	public boolean isInstalled() throws CoreException {
		return ensureMarker().getAttribute(INSTALL_COUNT, 0) > 0;
	}	
	
	/**
	 * Increments the install count of this breakpoint
	 */
	protected void incrementInstallCount() throws CoreException {
		int count = getInstallCount();
		setAttribute(INSTALL_COUNT, count + 1);
	}	
	
	/**
	 * Returns the <code>INSTALL_COUNT</code> attribute of this breakpoint
	 * or 0 if the attribute is not set.
	 */
	public int getInstallCount() throws CoreException {
		return ensureMarker().getAttribute(INSTALL_COUNT, 0);
	}	

	/**
	 * Decrements the install count of this breakpoint
	 */
	protected void decrementInstallCount() throws CoreException {
		int count= getInstallCount();
		if (count > 0) {
			setAttribute(INSTALL_COUNT, count - 1);	
		}
		if (count == 1) {
			if (isExpired()) {
				// if breakpoint was auto-disabled, re-enable it
				setAttributes(fgExpiredEnabledAttributes,
						new Object[]{Boolean.FALSE, Boolean.TRUE});
			}
		}
	}
	
	/**
	 * Sets the type name in which to install this breakpoint.
	 */
	protected void setTypeName(String typeName) throws CoreException {
		setAttribute(TYPE_NAME, typeName);
	}	

	/**
	 * @see IJavaBreakpoint#getTypeName()
	 */
	public String getTypeName() throws CoreException {
		return ensureMarker().getAttribute(TYPE_NAME, null);
	}
	
	/**
	 * Execute the given workspace runnable
	 */
	protected void run(IWorkspaceRunnable wr) throws DebugException {
		try {
			ResourcesPlugin.getWorkspace().run(wr, null);
		} catch (CoreException e) {
			throw new DebugException(e.getStatus());
		}			
	}
	
	/**
	 * Resets the install count attribute on this breakpoint's marker
	 * to "0".  Resets the expired attribute on all breakpoint markers to <code>false</code>.
	 * Resets the enabled attribute on the breakpoint marker to <code>true</code>.
	 * If a workbench crashes, the attributes could have been persisted
	 * in an incorrect state.
	 */
	private void configureAtStartup() throws CoreException {
		List attributes= null;
		List values= null;
		if (isInstalled()) {
			attributes= new ArrayList(3);
			values= new ArrayList(3);
			attributes.add(INSTALL_COUNT);
			values.add(new Integer(0));
		}
		if (isExpired()) {
			if (attributes == null) {
				attributes= new ArrayList(3);
				values= new ArrayList(3);
			}
			// if breakpoint was auto-disabled, re-enable it
			attributes.add(EXPIRED);
			values.add(Boolean.FALSE);
			attributes.add(ENABLED);
			values.add(Boolean.TRUE);
		}
		if (attributes != null) {
			String[] strAttributes= new String[attributes.size()];
			setAttributes((String[])attributes.toArray(strAttributes), values.toArray());
		}
	}	

	/**
	 * @see IJavaBreakpoint#getHitCount()
	 */
	public int getHitCount() throws CoreException {
		return ensureMarker().getAttribute(HIT_COUNT, -1);
	}
	
	/**
	 * @see IJavaBreakpoint#setHitCount(int)
	 */
	public void setHitCount(int count) throws CoreException {	
		if (!isEnabled() && count > -1) {
			setAttributes(new String []{HIT_COUNT, EXPIRED, ENABLED},
				new Object[]{new Integer(count), Boolean.FALSE, Boolean.TRUE});
		} else {
			setAttributes(new String[]{HIT_COUNT, EXPIRED},
				new Object[]{new Integer(count), Boolean.FALSE});
		}
	}	
	
	/**
	 * Sets whether or not this breakpoint's hit count has expired.
	 */
	public void setExpired(boolean expired) throws CoreException {
		setAttribute(EXPIRED, expired);	
	}	

	/**
	 * @see IJavaBreakpoint#getSuspendPolicy()
	 */
	public int getSuspendPolicy() throws CoreException {
		return ensureMarker().getAttribute(SUSPEND_POLICY, IJavaBreakpoint.SUSPEND_THREAD);
	}

	/**
	 * @see IJavaBreakpoint#setSuspendPolicy(int)
	 */
	public void setSuspendPolicy(int suspendPolicy) throws CoreException {
		if (getSuspendPolicy() != suspendPolicy) {
			setAttribute(SUSPEND_POLICY, suspendPolicy);
		}
	}
	
	/**
	 * Notifies listeners this breakpoint is to be added to the
	 * given target.
	 * 
	 * @param target debug target
	 */
	protected void fireAdded(IJavaDebugTarget target) {
		JDIDebugPlugin.getDefault().fireBreakpointAdded(target, this);
	}
	
	/**
	 * Notifies listeners this breakpoint has been remvoed from the
	 * given target.
	 * 
	 * @param target debug target
	 */
	protected void fireRemoved(IJavaDebugTarget target) {
		JDIDebugPlugin.getDefault().fireBreakpointRemoved(target, this);
	}	
	
	/**
	 * Notifies listeners this breakpoint has been installed in the
	 * given target.
	 * 
	 * @param target debug target
	 */
	protected void fireInstalled(IJavaDebugTarget target) {
		JDIDebugPlugin.getDefault().fireBreakpointInstalled(target, this);
	}	
	
	/**
	 * @see IJavaBreakpoint#setThreadFilter(IJavaThread)
	 */
	public void setThreadFilter(IJavaThread thread) throws CoreException {
		if (!(thread.getDebugTarget() instanceof JDIDebugTarget) || !(thread instanceof JDIThread)) {
			return;
		}
		JDIDebugTarget target= (JDIDebugTarget)thread.getDebugTarget();
		fFilteredThreadsByTarget.put(target, thread);
		// Other breakpoints set attributes on the underlying
		// marker and the marker changes are eventually
		// propagated to the target. The target then asks the
		// breakpoint to update its request. Since thread filters
		// are transient properties, they are not set on
		// the marker. Thus we must update the request
		// here.
		List requests= getRequests(target);
		ListIterator iter= requests.listIterator();
		EventRequest request= null;
		while (iter.hasNext()) {
			request= (EventRequest)iter.next();
			if (request instanceof ClassPrepareRequest) {
				continue;
			}
			EventRequest newRequest = recreateRequest(request, target);
			if (newRequest != request) {
				replaceRequest(target, request, newRequest);
				iter.set(newRequest);
				DebugPlugin.getDefault().addDebugEventListener(this);
				// Since thread filters don't affect the underlying marker, fire
				// a changed notification manually
				DebugPlugin.getDefault().getBreakpointManager().fireBreakpointChanged(this);
			}
		}
	}
	
	/**
	 * @see IDebugEventSetListener#handleDebugEvents(DebugEvent[])
	 * 
	 * Remove thread filters for terminated threads
	 */
	public void handleDebugEvents(DebugEvent[] events) {
		for (int i = 0; i < events.length; i++) {
			DebugEvent event = events[i];
			if (event.getKind() == DebugEvent.TERMINATE) {
				Object source= event.getSource();
				if (!(source instanceof JDIThread)) {
					return;
				}
				JDIThread thread= (JDIThread)source;
				JDIDebugTarget target= (JDIDebugTarget)thread.getDebugTarget();
				try {
					if (thread == getThreadFilter(target)) {
						removeThreadFilter(target);
					}
				} catch (VMDisconnectedException exception) {
					// Thread death often occurs at shutdown.
					// A VMDisconnectedException trying to 
					// update the breakpoint request is
					// acceptable.
				} catch (CoreException exception) {
					JDIDebugPlugin.log(exception);
				}
			}
		}
	}
	
	/** 
	 * EventRequest does not support thread filters, so they
	 * can't be set generically here. However, each of the breakpoint
	 * subclasses of EventRequest do support thread filters. So
	 * subclasses can set thread filters on their specific
	 * request type.
	 */
	protected abstract void setRequestThreadFilter(EventRequest request, ThreadReference thread);
	
	/**
	 * @see IJavaBreakpoint#getThreadFilter(IJavaDebugTarget)
	 */
	public IJavaThread getThreadFilter(IJavaDebugTarget target) throws CoreException {
		return (IJavaThread)fFilteredThreadsByTarget.get(target);
	}
	
	/**
	 * @see IJavaBreakpoint#getThreadFilters()
	 */
	public IJavaThread[] getThreadFilters() throws CoreException {
		IJavaThread[] threads= null;
		Collection values= fFilteredThreadsByTarget.values();
		threads= new IJavaThread[values.size()];
		values.toArray(threads);
		return threads;
	}

	/**
	 * @see IJavaBreakpoint#removeThreadFilter(IJavaThread)
	 */
	public void removeThreadFilter(IJavaDebugTarget javaTarget) throws CoreException {
		if (!(javaTarget instanceof JDIDebugTarget)) {
			return;
		}
		JDIDebugTarget target= (JDIDebugTarget)javaTarget;
		fFilteredThreadsByTarget.remove(target);
		List requests = getRequests(target);
		ListIterator iter= requests.listIterator();
		EventRequest request= null;
		while (iter.hasNext()) {
			request= (EventRequest)iter.next();
			if (request instanceof ClassPrepareRequest) {
				continue;
			}
			// Since there is no API for removing thread filters from requests,
			// we create a new request with the current thread filters
			// and replace the old.
			EventRequest newRequest = recreateRequest(request, target);
			if (newRequest != request) {
				iter.set(newRequest);
				replaceRequest(target, request, newRequest);
				DebugPlugin.getDefault().removeDebugEventListener(this);
				// Since thread filters don't affect the underlying marker, fire
				// a changed notification manually
				DebugPlugin.getDefault().getBreakpointManager().fireBreakpointChanged(this);
			}
		}
	}
}

