/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.core.breakpoints;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.model.Breakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.debug.core.IJavaBreakpointListener;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.internal.debug.core.IJDIEventListener;
import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;
import org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget;
import org.eclipse.jdt.internal.debug.core.model.JDIObjectValue;
import org.eclipse.jdt.internal.debug.core.model.JDIThread;
import org.eclipse.jdt.internal.debug.core.model.JDIType;

import com.ibm.icu.text.MessageFormat;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.event.ClassPrepareEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventSet;
import com.sun.jdi.event.LocatableEvent;
import com.sun.jdi.event.ThreadStartEvent;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;

public abstract class JavaBreakpoint extends Breakpoint implements IJavaBreakpoint, IJDIEventListener, IDebugEventSetListener {

	/**
	 * Breakpoint attribute storing the expired value (value
	 * <code>"org.eclipse.jdt.debug.core.expired"</code>). This attribute is
	 * stored as a <code>boolean</code>. Once a hit count has been reached, a
	 * breakpoint is considered to be "expired".
	 */
	protected static final String EXPIRED = "org.eclipse.jdt.debug.core.expired"; //$NON-NLS-1$
	/**
	 * Breakpoint attribute storing a breakpoint's hit count value (value
	 * <code>"org.eclipse.jdt.debug.core.hitCount"</code>). This attribute is
	 * stored as an <code>int</code>.
	 */
	protected static final String HIT_COUNT = "org.eclipse.jdt.debug.core.hitCount"; //$NON-NLS-1$
	/**
	 * Breakpoint attribute storing the number of debug targets a breakpoint is
	 * installed in (value
	 * <code>"org.eclipse.jdt.debug.core.installCount"</code>). This attribute
	 * is a <code>int</code>.
	 */
	protected static final String INSTALL_COUNT = "org.eclipse.jdt.debug.core.installCount"; //$NON-NLS-1$	

	/**
	 * Breakpoint attribute storing the fully qualified name of the type this
	 * breakpoint is located in. (value
	 * <code>"org.eclipse.jdt.debug.core.typeName"</code>). This attribute is a
	 * <code>String</code>.
	 */
	protected static final String TYPE_NAME = "org.eclipse.jdt.debug.core.typeName"; //$NON-NLS-1$		

	/**
	 * Breakpoint attribute storing suspend policy code for this breakpoint.
	 * (value <code>"org.eclipse.jdt.debug.core.suspendPolicy</code>). This
	 * attribute is an <code>int</code> corresponding to
	 * <code>IJavaBreakpoint.SUSPEND_VM</code> or
	 * <code>IJavaBreakpoint.SUSPEND_THREAD</code>.
	 */
	protected static final String SUSPEND_POLICY = "org.eclipse.jdt.debug.core.suspendPolicy"; //$NON-NLS-1$	

	/**
	 * Breakpoint attribute storing a comma delimited list of extension
	 * identifiers of breakpoint listeners. The listeners will be notified in
	 * the order specified in the list.
	 * 
	 * @since 3.5
	 */
	public static final String BREAKPOINT_LISTENERS = JDIDebugPlugin.EXTENSION_POINT_JAVA_BREAKPOINT_LISTENERS;

	/**
	 * Stores the collection of requests that this breakpoint has installed in
	 * debug targets. key: a debug target value: the requests this breakpoint
	 * has installed in that target
	 */
	protected HashMap<JDIDebugTarget, List<EventRequest>> fRequestsByTarget;

	/**
	 * The list of threads (ThreadReference objects) in which this breakpoint
	 * will suspend, associated with the target in which each thread exists
	 * (JDIDebugTarget). key: targets the debug targets (IJavaDebugTarget)
	 * value: thread the filtered thread (IJavaThread) in the given target
	 */
	protected Map<JDIDebugTarget, IJavaThread> fFilteredThreadsByTarget;

	/**
	 * Stores the type name that this breakpoint was last installed in. When a
	 * breakpoint is created, the TYPE_NAME attribute assigned to it is that of
	 * its top level enclosing type. When installed, the type may actually be an
	 * inner type. We need to keep track of the type type the breakpoint was
	 * installed in, in case we need to re-install the breakpoint for HCR (i.e.
	 * in case an inner type is HCR'd).
	 */
	protected String fInstalledTypeName = null;

	/**
	 * List of targets in which this breakpoint is installed. Used to prevent
	 * firing of more than one install notification when a breakpoint's requests
	 * are re-created.
	 */
	protected Set<IJavaDebugTarget> fInstalledTargets = null;

	/**
	 * List of active instance filters for this breakpoint (list of
	 * <code>IJavaObject</code>).
	 */
	protected List<IJavaObject> fInstanceFilters = null;

	/**
	 * List of breakpoint listener identifiers corresponding to breakpoint
	 * listener extensions. Listeners are cached with the breakpoint object such
	 * that they can be notified when a breakpoint is removed.
	 */
	private List<String> fBreakpointListenerIds = null;

	/**
	 * Empty instance filters array.
	 */
	protected static final IJavaObject[] fgEmptyInstanceFilters = new IJavaObject[0];

	/**
	 * Property identifier for a breakpoint object on an event request
	 */
	public static final String JAVA_BREAKPOINT_PROPERTY = "org.eclipse.jdt.debug.breakpoint"; //$NON-NLS-1$

	/**
	 * JavaBreakpoint attributes
	 */
	protected static final String[] fgExpiredEnabledAttributes = new String[] {
			EXPIRED, ENABLED };

	public JavaBreakpoint() {
		fRequestsByTarget = new HashMap<JDIDebugTarget, List<EventRequest>>(1);
		fFilteredThreadsByTarget = new HashMap<JDIDebugTarget, IJavaThread>(1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.IBreakpoint#getModelIdentifier()
	 */
	public String getModelIdentifier() {
		return JDIDebugModel.getPluginIdentifier();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.debug.core.model.Breakpoint#setMarker(org.eclipse.core.resources
	 * .IMarker)
	 */
	@Override
	public void setMarker(IMarker marker) throws CoreException {
		super.setMarker(marker);
		configureAtStartup();
	}

	/**
	 * Add this breakpoint to the breakpoint manager, or sets it as
	 * unregistered.
	 */
	protected void register(boolean register) throws CoreException {
		DebugPlugin plugin = DebugPlugin.getDefault();
		if (plugin != null && register) {
			plugin.getBreakpointManager().addBreakpoint(this);
		} else {
			setRegistered(false);
		}
	}

	/**
	 * Add the given event request to the given debug target. If the request is
	 * the breakpoint request associated with this breakpoint, increment the
	 * install count.
	 */
	protected void registerRequest(EventRequest request, JDIDebugTarget target)
			throws CoreException {
		if (request == null) {
			return;
		}
		List<EventRequest> reqs = getRequests(target);
		if (reqs.isEmpty()) {
			fRequestsByTarget.put(target, reqs);
		}
		reqs.add(request);
		target.addJDIEventListener(this, request);
		// update the install attribute on the breakpoint
		if (!(request instanceof ClassPrepareRequest)) {
			incrementInstallCount();
			// notification
			fireInstalled(target);
		}
	}

	/**
	 * Returns a String corresponding to the reference type name to the top
	 * enclosing type in which this breakpoint is located or <code>null</code>
	 * if no reference type could be found.
	 */
	protected String getEnclosingReferenceTypeName() throws CoreException {
		String name = getTypeName();
		int index = name.indexOf('$');
		if (index == -1) {
			return name;
		}
		return name.substring(0, index);
	}

	/**
	 * Returns the requests that this breakpoint has installed in the given
	 * target.
	 */
	protected ArrayList<EventRequest> getRequests(JDIDebugTarget target) {
		ArrayList<EventRequest> list = (ArrayList<EventRequest>) fRequestsByTarget.get(target);
		if (list == null) {
			list = new ArrayList<EventRequest>(2);
		}
		return list;
	}

	/**
	 * Remove the given request from the given target. If the request is the
	 * breakpoint request associated with this breakpoint, decrement the install
	 * count.
	 */
	protected void deregisterRequest(EventRequest request, JDIDebugTarget target)
			throws CoreException {
		target.removeJDIEventListener(this, request);
		// A request may be getting de-registered because the breakpoint has
		// been deleted. It may be that this occurred because of a marker
		// deletion.
		// Don't try updating the marker (decrementing the install count) if
		// it no longer exists.
		if (!(request instanceof ClassPrepareRequest) && getMarker().exists()) {
			decrementInstallCount();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jdt.internal.debug.core.IJDIEventListener#handleEvent(com
	 * .sun.jdi.event.Event,
	 * org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget)
	 */
	public boolean handleEvent(Event event, JDIDebugTarget target,
			boolean suspendVote, EventSet eventSet) {
		if (event instanceof ClassPrepareEvent) {
			return handleClassPrepareEvent((ClassPrepareEvent) event, target,
					suspendVote);
		}
		ThreadReference threadRef = ((LocatableEvent) event).thread();
		JDIThread thread = target.findThread(threadRef);
		if (thread == null) {
			// wait for any thread start event sets to complete processing
			// see bug 271700
			try {
				Job.getJobManager().join(ThreadStartEvent.class, null);
			} catch (OperationCanceledException e) {
			} catch (InterruptedException e) {
			}
			thread = target.findThread(threadRef);
		}
		if (thread == null || thread.isIgnoringBreakpoints()) {
			return true;
		}
		return handleBreakpointEvent(event, thread, suspendVote);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jdt.internal.debug.core.IJDIEventListener#eventSetComplete
	 * (com.sun.jdi.event.Event,
	 * org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget, boolean)
	 */
	public void eventSetComplete(Event event, JDIDebugTarget target,
			boolean suspend, EventSet eventSet) {
		ThreadReference threadRef = null;
		if (event instanceof ClassPrepareEvent) {
			threadRef = ((ClassPrepareEvent) event).thread();
		} else if (event instanceof LocatableEvent) {
			threadRef = ((LocatableEvent) event).thread();
		}
		if (threadRef == null) {
			return;
		}
		JDIThread thread = target.findThread(threadRef);
		if (thread == null || thread.isIgnoringBreakpoints()) {
			return;
		}
		if (event instanceof ClassPrepareEvent) {
			classPrepareComplete(event, thread, suspend, eventSet);
		} else {
			thread.completeBreakpointHandling(this, suspend, true, eventSet);
		}
	}

	/**
	 * Call-back that the class prepare event has completed
	 * @param event the event
	 * @param thread the thread that sent the event
	 * @param suspend if the the thread was suspended
	 * @param eventSet the event set context
	 */
	protected void classPrepareComplete(Event event, JDIThread thread, boolean suspend, EventSet eventSet) {
		// resume the thread if this is a class load event to install a deferred
		// breakpoint (and the vote is to resume)
		if (thread != null && !suspend) {
			thread.resumedFromClassPrepare();
		}
	}

	/**
	 * Handle the given class prepare event, which was generated by the class
	 * prepare event installed in the given target by this breakpoint.
	 * 
	 * If the class which has been loaded is a class in which this breakpoint
	 * should install, create a breakpoint request for that class.
	 * @param event the event
	 * @param target the target
	 * @param suspendVote the current suspend vote
	 * @return is the thread should suspend or not
	 */
	public boolean handleClassPrepareEvent(ClassPrepareEvent event, JDIDebugTarget target, boolean suspendVote) {
		try {
			if (!installableReferenceType(event.referenceType(), target)) {
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
	 *      Handle the given event, which was generated by the breakpoint
	 *      request installed in the given target by this breakpoint.
	 */
	public boolean handleBreakpointEvent(Event event, JDIThread thread,
			boolean suspendVote) {
		expireHitCount(event);
		return !suspend(thread, suspendVote); // Resume if suspend fails
	}

	/**
	 * Delegates to the given thread to suspend, and returns whether the thread
	 * suspended It is possible that the thread will not suspend as directed by
	 * a Java breakpoint listener.
	 * 
	 * @see IJavaBreakpointListener#breakpointHit(IJavaThread, IJavaBreakpoint)
	 */
	protected boolean suspend(JDIThread thread, boolean suspendVote) {
		return thread.handleSuspendForBreakpoint(this, suspendVote);
	}

	/**
	 * Returns whether the given reference type is appropriate for this
	 * breakpoint to be installed in the given target. Query registered
	 * breakpoint listeners.
	 */
	protected boolean installableReferenceType(ReferenceType type,
			JDIDebugTarget target) throws CoreException {
		String installableType = getTypeName();
		String queriedType = type.name();
		if (installableType == null || queriedType == null) {
			return false;
		}
		int index = queriedType.indexOf('<');
		if (index != -1) {
			queriedType = queriedType.substring(0, index);
		}
		if (installableType.equals(queriedType)) {
			return queryInstallListeners(target, type);
		}
		index = queriedType.indexOf('$', 0);
		if (index == -1) {
			return false;
		}
		if (installableType.regionMatches(0, queriedType, 0, index)) {
			return queryInstallListeners(target, type);
		}
		return false;
	}

	/**
	 * Called when a breakpoint event is encountered. Expires the hit count in
	 * the event's request and updates the marker.
	 * 
	 * @param event
	 *            the event whose request should have its hit count expired or
	 *            <code>null</code> to only update the breakpoint marker.
	 */
	protected void expireHitCount(Event event) {
		Integer requestCount = null;
		EventRequest request = null;
		if (event != null) {
			request = event.request();
			requestCount = (Integer) request.getProperty(HIT_COUNT);
		}
		if (requestCount != null) {
			if (request != null) {
				request.putProperty(EXPIRED, Boolean.TRUE);
			}
			try {
				setAttributes(fgExpiredEnabledAttributes, new Object[] {
						Boolean.TRUE, Boolean.FALSE });
				// make a note that we auto-disabled this breakpoint.
			} catch (CoreException ce) {
				JDIDebugPlugin.log(ce);
			}
		}
	}

	/**
	 * Returns whether this breakpoint should be "skipped". Breakpoints are
	 * skipped if the breakpoint manager is disabled and the breakpoint is
	 * registered with the manager
	 * 
	 * @return whether this breakpoint should be skipped
	 */
	public boolean shouldSkipBreakpoint() throws CoreException {
		DebugPlugin plugin = DebugPlugin.getDefault();
		return plugin != null && isRegistered()
				&& !plugin.getBreakpointManager().isEnabled();
	}

	/**
	 * Attempts to create a breakpoint request for this breakpoint in the given
	 * reference type in the given target.
	 * 
	 * @return Whether a request was created
	 */
	protected boolean createRequest(JDIDebugTarget target, ReferenceType type)
			throws CoreException {
		if (shouldSkipBreakpoint()) {
			return false;
		}
		EventRequest[] requests = newRequests(target, type);
		if (requests == null) {
			return false;
		}
		fInstalledTypeName = type.name();
		for (EventRequest request : requests) {
			registerRequest(request, target);
		}
		return true;
	}

	/**
	 * Configure a breakpoint request with common properties:
	 * <ul>
	 * <li><code>JAVA_BREAKPOINT_PROPERTY</code></li>
	 * <li><code>HIT_COUNT</code></li>
	 * <li><code>EXPIRED</code></li>
	 * </ul>
	 * and sets the suspend policy of the request to suspend the event thread.
	 */
	protected void configureRequest(EventRequest request, JDIDebugTarget target)
			throws CoreException {
		request.setSuspendPolicy(getJDISuspendPolicy());
		request.putProperty(JAVA_BREAKPOINT_PROPERTY, this);
		configureRequestThreadFilter(request, target);
		configureRequestHitCount(request);
		configureInstanceFilters(request, target);
		// Important: only enable a request after it has been configured
		updateEnabledState(request, target);
	}

	/**
	 * Adds an instance filter to the given request. Since the implementation is
	 * request specific, subclasses must override.
	 * 
	 * @param request
	 * @param object
	 *            instance filter
	 */
	protected abstract void addInstanceFilter(EventRequest request,
			ObjectReference object);

	/**
	 * Configure the thread filter property of the given request.
	 */
	protected void configureRequestThreadFilter(EventRequest request,
			JDIDebugTarget target) {
		IJavaThread thread = fFilteredThreadsByTarget.get(target);
		if (thread == null || (!(thread instanceof JDIThread))) {
			return;
		}
		setRequestThreadFilter(request,
				((JDIThread) thread).getUnderlyingThread());
	}

	/**
	 * Configure the given request's hit count
	 */
	protected void configureRequestHitCount(EventRequest request)
			throws CoreException {
		int hitCount = getHitCount();
		if (hitCount > 0) {
			request.addCountFilter(hitCount);
			request.putProperty(HIT_COUNT, new Integer(hitCount));
		}
	}

	protected void configureInstanceFilters(EventRequest request,
			JDIDebugTarget target) {
		if (fInstanceFilters != null && !fInstanceFilters.isEmpty()) {
			Iterator<IJavaObject> iter = fInstanceFilters.iterator();
			while (iter.hasNext()) {
				IJavaObject object = iter.next();
				if (object.getDebugTarget().equals(target)) {
					addInstanceFilter(request,
							((JDIObjectValue) object).getUnderlyingObject());
				}
			}
		}
	}

	/**
	 * Creates, installs, and returns all event requests for this breakpoint in
	 * the given reference type and and target.
	 * 
	 * @return the event requests created or <code>null</code> if creation
	 *         failed
	 */
	protected abstract EventRequest[] newRequests(JDIDebugTarget target,
			ReferenceType type) throws CoreException;

	/**
	 * Add this breakpoint to the given target. After it has been added to the
	 * given target, this breakpoint will suspend execution of that target as
	 * appropriate.
	 */
	public void addToTarget(JDIDebugTarget target) throws CoreException {
		fireAdding(target);
		createRequests(target);
	}

	/**
	 * Creates event requests for the given target
	 */
	protected void createRequests(JDIDebugTarget target) throws CoreException {
		if (target.isTerminated() || shouldSkipBreakpoint()) {
			return;
		}
		String referenceTypeName = getTypeName();
		String enclosingTypeName = getEnclosingReferenceTypeName();
		if (referenceTypeName == null || enclosingTypeName == null) {
			return;
		}
		// create request to listen to class loads
		if (referenceTypeName.indexOf('$') == -1) {
			registerRequest(
					target.createClassPrepareRequest(enclosingTypeName), target);
			// register to ensure we hear about local and anonymous inner
			// classes
			registerRequest(
					target.createClassPrepareRequest(enclosingTypeName + "$*"), target); //$NON-NLS-1$
		} else {
			registerRequest(
					target.createClassPrepareRequest(referenceTypeName), target);
			// register to ensure we hear about local and anonymous inner
			// classes
			registerRequest(target.createClassPrepareRequest(enclosingTypeName
					+ "$*", referenceTypeName), target); //$NON-NLS-1$
		}

		// create breakpoint requests for each class currently loaded
		List<ReferenceType> classes = target.jdiClassesByName(referenceTypeName);
		if (classes.isEmpty() && enclosingTypeName.equals(referenceTypeName)) {
			return;
		}

		boolean success = false;
		Iterator<ReferenceType> iter = classes.iterator();
		while (iter.hasNext()) {
			ReferenceType type = iter.next();
			if (createRequest(target, type)) {
				success = true;
			}
		}

		if (!success) {
			addToTargetForLocalType(target, enclosingTypeName);
		}
	}

	/**
	 * Local types (types defined in methods) are handled specially due to the
	 * different types that the local type is associated with as well as the
	 * performance problems of using ReferenceType#nestedTypes. From the Java
	 * model perspective a local type is defined within a method of a type.
	 * Therefore the type of a breakpoint placed in a local type is the type
	 * that encloses the method where the local type was defined. The local type
	 * is enclosed within the top level type according to the VM. So if "normal"
	 * attempts to create a request when a breakpoint is being added to a target
	 * fail, we must be dealing with a local type and therefore resort to
	 * looking up all of the nested types of the top level enclosing type.
	 * 
	 * @param target the target
	 * @param enclosingTypeName the type name of the enclosing type 
	 * @throws CoreException if something bad happens
	 */
	protected void addToTargetForLocalType(JDIDebugTarget target, String enclosingTypeName) throws CoreException {
		List<ReferenceType> classes = target.jdiClassesByName(enclosingTypeName);
		for(ReferenceType type : classes) {
			for(ReferenceType nestedType : type.nestedTypes()) {
				if (createRequest(target, nestedType)) {
					break;
				}
			}
		}
	}

	/**
	 * Returns the JDI suspend policy that corresponds to this breakpoint's
	 * suspend policy
	 * 
	 * @return the JDI suspend policy that corresponds to this breakpoint's
	 *         suspend policy
	 * @exception CoreException
	 *                if unable to access this breakpoint's suspend policy
	 *                setting
	 */
	protected int getJDISuspendPolicy() throws CoreException {
		int breakpointPolicy = getSuspendPolicy();
		if (breakpointPolicy == IJavaBreakpoint.SUSPEND_THREAD) {
			return EventRequest.SUSPEND_EVENT_THREAD;
		}
		return EventRequest.SUSPEND_ALL;
	}

	/**
	 * returns the default suspend policy based on the pref setting on the
	 * Java-Debug pref page
	 * 
	 * @return the default suspend policy
	 * @since 3.2
	 */
	protected int getDefaultSuspendPolicy() {
		return Platform.getPreferencesService().getInt(
				JDIDebugPlugin.getUniqueIdentifier(), 
				JDIDebugPlugin.PREF_DEFAULT_BREAKPOINT_SUSPEND_POLICY, 
				IJavaBreakpoint.SUSPEND_THREAD, 
				null);
	}
	
	/**
	 * Returns whether the hitCount of this breakpoint is equal to the hitCount
	 * of the associated request.
	 */
	protected boolean hasHitCountChanged(EventRequest request)
			throws CoreException {
		int hitCount = getHitCount();
		Integer requestCount = (Integer) request.getProperty(HIT_COUNT);
		int oldCount = -1;
		if (requestCount != null) {
			oldCount = requestCount.intValue();
		}
		return hitCount != oldCount;
	}

	/**
	 * Removes this breakpoint from the given target.
	 */
	public void removeFromTarget(final JDIDebugTarget target)
			throws CoreException {
		removeRequests(target);
		Object removed = fFilteredThreadsByTarget.remove(target);
		boolean changed = removed != null;
		boolean markerExists = markerExists();
		if (!markerExists || (markerExists && getInstallCount() == 0)) {
			fInstalledTypeName = null;
		}

		// remove instance filters
		if (fInstanceFilters != null && !fInstanceFilters.isEmpty()) {
			for (int i = 0; i < fInstanceFilters.size(); i++) {
				IJavaObject object = fInstanceFilters.get(i);
				if (object.getDebugTarget().equals(target)) {
					fInstanceFilters.remove(i);
					changed = true;
				}
			}
		}

		// fire change notification if required
		if (changed) {
			fireChanged();
		}

		// notification
		fireRemoved(target);
	}

	/**
	 * Remove all requests that this breakpoint has installed in the given debug
	 * target.
	 */
	protected void removeRequests(final JDIDebugTarget target)
			throws CoreException {
		// removing was previously done is a workspace runnable, but that is
		// not possible since it can be a resource callback (marker deletion)
		// that
		// causes a breakpoint to be removed
		ArrayList<EventRequest> requests = (ArrayList<EventRequest>) getRequests(target).clone();
		// Iterate over a copy of the requests since this list of requests
		// can be changed in other threads which would cause an
		// ConcurrentModificationException
		Iterator<EventRequest> iter = requests.iterator();
		EventRequest req;
		while (iter.hasNext()) {
			req = iter.next();
			try {
				if (target.isAvailable() && !isExpired(req)) { // cannot delete
																// an expired
																// request
					EventRequestManager manager = target
							.getEventRequestManager();
					if (manager != null) {
						manager.deleteEventRequest(req); // disable & remove
					}
				}
			} catch (VMDisconnectedException e) {
				if (target.isAvailable()) {
					JDIDebugPlugin.log(e);
				}
			} catch (RuntimeException e) {
				target.internalError(e);
			} finally {
				deregisterRequest(req, target);
			}
		}
		fRequestsByTarget.remove(target);
	}

	/**
	 * Update the enabled state of the given request in the given target, which
	 * is associated with this breakpoint. Set the enabled state of the request
	 * to the enabled state of this breakpoint.
	 */
	protected void updateEnabledState(EventRequest request,
			JDIDebugTarget target) throws CoreException {
		internalUpdateEnabledState(request, isEnabled(), target);
	}

	/**
	 * Set the enabled state of the given request to the given value, also
	 * taking into account instance filters.
	 */
	protected void internalUpdateEnabledState(EventRequest request,
			boolean enabled, JDIDebugTarget target) {
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
				target.internalError(e);
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
		Boolean requestExpired = (Boolean) request.getProperty(EXPIRED);
		if (requestExpired == null) {
			return false;
		}
		return requestExpired.booleanValue();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.debug.core.IJavaBreakpoint#isInstalled()
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
	 * Returns the <code>INSTALL_COUNT</code> attribute of this breakpoint or 0
	 * if the attribute is not set.
	 */
	public int getInstallCount() throws CoreException {
		return ensureMarker().getAttribute(INSTALL_COUNT, 0);
	}

	/**
	 * Decrements the install count of this breakpoint.
	 */
	protected void decrementInstallCount() throws CoreException {
		int count = getInstallCount();
		if (count > 0) {
			setAttribute(INSTALL_COUNT, count - 1);
		}
		if (count == 1) {
			if (isExpired()) {
				// if breakpoint was auto-disabled, re-enable it
				setAttributes(fgExpiredEnabledAttributes, new Object[] {
						Boolean.FALSE, Boolean.TRUE });
			}
		}
	}

	/**
	 * Sets the type name in which to install this breakpoint.
	 */
	protected void setTypeName(String typeName) throws CoreException {
		setAttribute(TYPE_NAME, typeName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.debug.core.IJavaBreakpoint#getTypeName()
	 */
	public String getTypeName() throws CoreException {
		if (fInstalledTypeName == null) {
			return ensureMarker().getAttribute(TYPE_NAME, null);
		}
		return fInstalledTypeName;
	}

	/**
	 * Resets the install count attribute on this breakpoint's marker to "0".
	 * Resets the expired attribute on all breakpoint markers to
	 * <code>false</code>. Resets the enabled attribute on the breakpoint marker
	 * to <code>true</code>. If a workbench crashes, the attributes could have
	 * been persisted in an incorrect state.
	 */
	private void configureAtStartup() throws CoreException {
		List<String> attributes = null;
		List<Object> values = new ArrayList<Object>(3);
		if (isInstalled()) {
			attributes = new ArrayList<String>(3);
			attributes.add(INSTALL_COUNT);
			values.add(new Integer(0));
		}
		if (isExpired()) {
			if (attributes == null) {
				attributes = new ArrayList<String>(3);
			}
			// if breakpoint was auto-disabled, re-enable it
			attributes.add(EXPIRED);
			values.add(Boolean.FALSE);
			attributes.add(ENABLED);
			values.add(Boolean.TRUE);
		}
		if (attributes != null) {
			String[] strAttributes = new String[attributes.size()];
			setAttributes(attributes.toArray(strAttributes), values.toArray());
		}
		String[] listeners = readBreakpointListeners();
		if (listeners.length > 0) {
			fBreakpointListenerIds = new ArrayList<String>();
			for (String listener : listeners) {
				fBreakpointListenerIds.add(listener);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.debug.core.IJavaBreakpoint#getHitCount()
	 */
	public int getHitCount() throws CoreException {
		return ensureMarker().getAttribute(HIT_COUNT, -1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.debug.core.IJavaBreakpoint#setHitCount(int)
	 */
	public void setHitCount(int count) throws CoreException {
		if (getHitCount() != count) {
			if (!isEnabled() && count > -1) {
				setAttributes(new String[] { ENABLED, HIT_COUNT, EXPIRED },
						new Object[] { Boolean.TRUE, new Integer(count),
								Boolean.FALSE });
			} else {
				setAttributes(new String[] { HIT_COUNT, EXPIRED },
						new Object[] { new Integer(count), Boolean.FALSE });
			}
			recreate();
		}
	}

	protected String getMarkerMessage(int hitCount, int suspendPolicy) {
		StringBuffer buff = new StringBuffer();
		if (hitCount > 0) {
			buff.append(MessageFormat
					.format(JDIDebugBreakpointMessages.JavaBreakpoint___Hit_Count___0___1,
							new Object[] { Integer.toString(hitCount) }));
			buff.append(' ');
		}
		String suspendPolicyString;
		if (suspendPolicy == IJavaBreakpoint.SUSPEND_THREAD) {
			suspendPolicyString = JDIDebugBreakpointMessages.JavaBreakpoint__suspend_policy__thread__1;
		} else {
			suspendPolicyString = JDIDebugBreakpointMessages.JavaBreakpoint__suspend_policy__VM__2;
		}

		buff.append(suspendPolicyString);
		return buff.toString();
	}

	/**
	 * Sets whether this breakpoint's hit count has expired.
	 */
	public void setExpired(boolean expired) throws CoreException {
		setAttribute(EXPIRED, expired);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.debug.core.IJavaBreakpoint#getSuspendPolicy()
	 */
	public int getSuspendPolicy() throws CoreException {
		return ensureMarker().getAttribute(SUSPEND_POLICY,
				IJavaBreakpoint.SUSPEND_THREAD);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.debug.core.IJavaBreakpoint#setSuspendPolicy(int)
	 */
	public void setSuspendPolicy(int suspendPolicy) throws CoreException {
		if (getSuspendPolicy() != suspendPolicy) {
			setAttribute(SUSPEND_POLICY, suspendPolicy);
			recreate();
		}
	}

	/**
	 * Notifies listeners this breakpoint is to be added to the given target.
	 * 
	 * @param target
	 *            debug target
	 */
	protected void fireAdding(IJavaDebugTarget target) {
		JDIDebugPlugin plugin = JDIDebugPlugin.getDefault();
		if (plugin != null)
			plugin.fireBreakpointAdding(target, this);
	}

	/**
	 * Notifies listeners this breakpoint has been removed from the given
	 * target.
	 * 
	 * @param target
	 *            debug target
	 */
	protected void fireRemoved(IJavaDebugTarget target) {
		JDIDebugPlugin plugin = JDIDebugPlugin.getDefault();
		if (plugin != null) {
			plugin.fireBreakpointRemoved(target, this);
			setInstalledIn(target, false);
		}
	}

	/**
	 * Notifies listeners this breakpoint has been installed in the given
	 * target.
	 * 
	 * @param target
	 *            debug target
	 */
	protected void fireInstalled(IJavaDebugTarget target) {
		JDIDebugPlugin plugin = JDIDebugPlugin.getDefault();
		if (plugin != null && !isInstalledIn(target)) {
			plugin.fireBreakpointInstalled(target, this);
			setInstalledIn(target, true);
		}
	}

	/**
	 * Returns whether this breakpoint is installed in the given target.
	 * 
	 * @param target
	 * @return whether this breakpoint is installed in the given target
	 */
	protected boolean isInstalledIn(IJavaDebugTarget target) {
		return fInstalledTargets != null && fInstalledTargets.contains(target);
	}

	/**
	 * Sets this breakpoint as installed in the given target
	 * 
	 * @param target
	 * @param installed
	 *            whether installed
	 */
	protected void setInstalledIn(IJavaDebugTarget target, boolean installed) {
		if (installed) {
			if (fInstalledTargets == null) {
				fInstalledTargets = new HashSet<IJavaDebugTarget>();
			}
			fInstalledTargets.add(target);
		} else {
			if (fInstalledTargets != null) {
				fInstalledTargets.remove(target);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jdt.debug.core.IJavaBreakpoint#setThreadFilter(org.eclipse
	 * .jdt.debug.core.IJavaThread)
	 */
	public void setThreadFilter(IJavaThread thread) throws CoreException {
		if (!(thread.getDebugTarget() instanceof JDIDebugTarget)
				|| !(thread instanceof JDIThread)) {
			return;
		}
		JDIDebugTarget target = (JDIDebugTarget) thread.getDebugTarget();
		if (thread != fFilteredThreadsByTarget.put(target, thread)) {
			// recreate the breakpoint only if it is not the same thread

			// Other breakpoints set attributes on the underlying
			// marker and the marker changes are eventually
			// propagated to the target. The target then asks the
			// breakpoint to update its request. Since thread filters
			// are transient properties, they are not set on
			// the marker. Thus we must update the request
			// here.
			recreate(target);
			fireChanged();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.debug.core.IDebugEventSetListener#handleDebugEvents(org.eclipse
	 * .debug.core.DebugEvent[])
	 */
	public void handleDebugEvents(DebugEvent[] events) {
		for (DebugEvent event : events) {
			if (event.getKind() == DebugEvent.TERMINATE) {
				Object source = event.getSource();
				if (!(source instanceof JDIThread)) {
					return;
				}
				try {
					cleanupForThreadTermination((JDIThread) source);
				} catch (VMDisconnectedException exception) {
					// Thread death often occurs at shutdown.
					// A VMDisconnectedException trying to
					// update the breakpoint request is
					// acceptable.
				}
			}
		}
	}

	/**
	 * Removes cached information relevant to this thread which has terminated.
	 * 
	 * Remove thread filters for terminated threads
	 * 
	 * Subclasses may override but need to call super.
	 */
	protected void cleanupForThreadTermination(JDIThread thread) {
		JDIDebugTarget target = (JDIDebugTarget) thread.getDebugTarget();
		try {
			if (thread == getThreadFilter(target)) {
				removeThreadFilter(target);
			}
		} catch (CoreException exception) {
			JDIDebugPlugin.log(exception);
		}
	}

	/**
	 * EventRequest does not support thread filters, so they can't be set
	 * generically here. However, each of the breakpoint subclasses of
	 * EventRequest do support thread filters. So subclasses can set thread
	 * filters on their specific request type.
	 */
	protected abstract void setRequestThreadFilter(EventRequest request,
			ThreadReference thread);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jdt.debug.core.IJavaBreakpoint#getThreadFilter(org.eclipse
	 * .jdt.debug.core.IJavaDebugTarget)
	 */
	public IJavaThread getThreadFilter(IJavaDebugTarget target) {
		return fFilteredThreadsByTarget.get(target);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.debug.core.IJavaBreakpoint#getThreadFilters()
	 */
	public IJavaThread[] getThreadFilters() {
		IJavaThread[] threads = null;
		Collection<IJavaThread> values = fFilteredThreadsByTarget.values();
		threads = new IJavaThread[values.size()];
		values.toArray(threads);
		return threads;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jdt.debug.core.IJavaBreakpoint#removeThreadFilter(org.eclipse
	 * .jdt.debug.core.IJavaDebugTarget)
	 */
	public void removeThreadFilter(IJavaDebugTarget javaTarget)
			throws CoreException {
		if (!(javaTarget instanceof JDIDebugTarget)) {
			return;
		}
		JDIDebugTarget target = (JDIDebugTarget) javaTarget;
		if (fFilteredThreadsByTarget.remove(target) != null) {
			recreate(target);
			fireChanged();
		}
	}

	/**
	 * Returns whether this breakpoint should be installed in the given
	 * reference type in the given target according to registered breakpoint
	 * listeners.
	 * 
	 * @param target
	 *            debug target
	 * @param type
	 *            reference type or <code>null</code> if this breakpoint is not
	 *            installed in a specific type
	 */
	protected boolean queryInstallListeners(JDIDebugTarget target,
			ReferenceType type) {
		JDIDebugPlugin plugin = JDIDebugPlugin.getDefault();
		if (plugin != null) {
			IJavaType jt = null;
			if (type != null) {
				jt = JDIType.createType(target, type);
			}
			return plugin.fireInstalling(target, this, jt);
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jdt.debug.core.IJavaBreakpoint#addInstanceFilter(org.eclipse
	 * .jdt.debug.core.IJavaObject)
	 */
	public void addInstanceFilter(IJavaObject object) throws CoreException {
		if (fInstanceFilters == null) {
			fInstanceFilters = new ArrayList<IJavaObject>();
		}
		if (!fInstanceFilters.contains(object)) {
			fInstanceFilters.add(object);
			recreate((JDIDebugTarget) object.getDebugTarget());
			fireChanged();
		}
	}

	/**
	 * Change notification when there are no marker changes. If the marker does
	 * not exist, do not fire a change notification (the marker may not exist if
	 * the associated project was closed).
	 */
	protected void fireChanged() {
		DebugPlugin plugin = DebugPlugin.getDefault();
		if (plugin != null && markerExists()) {
			plugin.getBreakpointManager().fireBreakpointChanged(this);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.debug.core.IJavaBreakpoint#getInstanceFilters()
	 */
	public IJavaObject[] getInstanceFilters() {
		if (fInstanceFilters == null || fInstanceFilters.isEmpty()) {
			return fgEmptyInstanceFilters;
		}
		return fInstanceFilters
				.toArray(new IJavaObject[fInstanceFilters.size()]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jdt.debug.core.IJavaBreakpoint#removeInstanceFilter(org.eclipse
	 * .jdt.debug.core.IJavaObject)
	 */
	public void removeInstanceFilter(IJavaObject object) throws CoreException {
		if (fInstanceFilters == null) {
			return;
		}
		if (fInstanceFilters.remove(object)) {
			recreate((JDIDebugTarget) object.getDebugTarget());
			fireChanged();
		}
	}

	/**
	 * An attribute of this breakpoint has changed - recreate event requests in
	 * all targets.
	 */
	protected void recreate() throws CoreException {
		DebugPlugin plugin = DebugPlugin.getDefault();
		if (plugin != null) {
			IDebugTarget[] targets = plugin.getLaunchManager()
					.getDebugTargets();
			for (IDebugTarget target : targets) {
				MultiStatus multiStatus = new MultiStatus(
						JDIDebugPlugin.getUniqueIdentifier(),
						JDIDebugPlugin.ERROR,
						JDIDebugBreakpointMessages.JavaBreakpoint_Exception,
						null);
				IJavaDebugTarget jdiTarget = (IJavaDebugTarget) target
						.getAdapter(IJavaDebugTarget.class);
				if (jdiTarget instanceof JDIDebugTarget) {
					try {
						recreate((JDIDebugTarget) jdiTarget);
					} catch (CoreException e) {
						multiStatus.add(e.getStatus());
					}
				}
				if (!multiStatus.isOK()) {
					throw new CoreException(multiStatus);
				}
			}
		}
	}

	/**
	 * Recreate this breakpoint in the given target, as long as the target
	 * already contains this breakpoint.
	 * 
	 * @param target
	 *            the target in which to re-create the breakpoint
	 */
	protected void recreate(JDIDebugTarget target) throws CoreException {
		if (target.isAvailable() && target.getBreakpoints().contains(this)) {
			removeRequests(target);
			createRequests(target);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.Breakpoint#setEnabled(boolean)
	 */
	@Override
	public void setEnabled(boolean enabled) throws CoreException {
		super.setEnabled(enabled);
		recreate();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.debug.core.IJavaBreakpoint#supportsInstanceFilters()
	 */
	public boolean supportsInstanceFilters() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.debug.core.IJavaBreakpoint#supportsThreadFilters()
	 */
	public boolean supportsThreadFilters() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jdt.debug.core.IJavaBreakpoint#addBreakpointListener(java
	 * .lang.String)
	 */
	public synchronized void addBreakpointListener(String identifier)
			throws CoreException {
		if (fBreakpointListenerIds == null) {
			fBreakpointListenerIds = new ArrayList<String>();
		}
		if (!fBreakpointListenerIds.contains(identifier)) {
			fBreakpointListenerIds.add(identifier);
			writeBreakpointListeners();
		}
	}

	/**
	 * Writes the current breakpoint listener collection to the underlying
	 * marker.
	 * 
	 * @throws CoreException
	 */
	private void writeBreakpointListeners() throws CoreException {
		StringBuffer buf = new StringBuffer();
		Iterator<String> iterator = fBreakpointListenerIds.iterator();
		while (iterator.hasNext()) {
			buf.append(iterator.next());
			if (iterator.hasNext()) {
				buf.append(","); //$NON-NLS-1$
			}
		}
		setAttribute(BREAKPOINT_LISTENERS, buf.toString());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jdt.debug.core.IJavaBreakpoint#removeBreakpointListener(java
	 * .lang.String)
	 */
	public synchronized boolean removeBreakpointListener(String identifier)
			throws CoreException {
		if (fBreakpointListenerIds != null) {
			if (fBreakpointListenerIds.remove(identifier)) {
				writeBreakpointListeners();
				return true;
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.debug.core.IJavaBreakpoint#getBreakpointListeners()
	 */
	public synchronized String[] getBreakpointListeners() throws CoreException {
		// use the cache in case the underlying marker has been deleted
		if (fBreakpointListenerIds == null) {
			return new String[0];
		}
		return fBreakpointListenerIds
				.toArray(new String[fBreakpointListenerIds.size()]);
	}

	/**
	 * Reads breakpoint listeners from the underlying marker.
	 * 
	 * @return breakpoint listener identifiers stored in this breakpoint's
	 *         marker
	 * @throws CoreException
	 *             if no marker
	 */
	private String[] readBreakpointListeners() throws CoreException {
		String value = ensureMarker().getAttribute(BREAKPOINT_LISTENERS,
				(String) null);
		if (value == null) {
			return new String[0];
		}
		return value.split(","); //$NON-NLS-1$
	}
}
