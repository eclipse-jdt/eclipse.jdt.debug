package org.eclipse.jdt.internal.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.IDebugConstants;
import org.eclipse.debug.core.IDebugStatusConstants;
import org.eclipse.debug.core.model.Breakpoint;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.debug.core.JDIDebugModel;

import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.event.ClassPrepareEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.LocatableEvent;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.MethodEntryRequest;

public abstract class JavaBreakpoint extends Breakpoint implements IJavaBreakpoint, IJDIEventListener {
	
	/**
	 * Breakpoint attribute storing the expired value (value <code>"expired"</code>).
	 * This attribute is stored as a <code>boolean</code>. Once a hit count has
	 * been reached, a breakpoint is considered to be "expired".
	 */
	protected static final String EXPIRED = "expired"; //$NON-NLS-1$
	/**
	 * Breakpoint attribute storing a breakpoint's hit count value
	 * (value <code>"hitCount"</code>). This attribute is stored as an
	 * <code>int</code>.
	 */
	protected static final String HIT_COUNT = "hitCount"; //$NON-NLS-1$
	/**
	 * Breakpoint attribute storing the number of debug targets a
	 * breakpoint is installed in (value <code>"installCount"</code>).
	 * This attribute is a <code>int</code>.
	 */
	protected static final String INSTALL_COUNT = "installCount"; //$NON-NLS-1$	
	/**
	 * Breakpoint attribute storing the handle identifier of the Java element
	 * corresponding to the type in which a breakpoint is contained
	 * (value <code>"typeHandle"</code>). This attribute is a <code>String</code>.
	 */
	protected static final String TYPE_HANDLE = "typeHandle"; //$NON-NLS-1$	
	
	/**
	 * Stores the collection of requests that this breakpoint has installed in
	 * debug targets.
	 * key: a debug target
	 * value: the requests this breakpoint has installed in that target
	 */
	protected HashMap fRequestsByTarget;
	
	/**
	 * Propery identifier for a breakpoint object on an event request
	 */
	public static final String JAVA_BREAKPOINT_PROPERTY = "org.eclipse.jdt.debug.breakpoint"; //$NON-NLS-1$
	
	/**
	 * JavaBreakpoint attributes
	 */	
	protected static final String[] fgExpiredEnabledAttributes= new String[]{EXPIRED, IDebugConstants.ENABLED};
	
	/**
	 * Returns the identifier for this JDI debug model plug-in
	 *
	 * @return plugin identifier
	 */
	public static String getPluginIdentifier() {
		return JDIDebugPlugin.getDefault().getDescriptor().getUniqueIdentifier();
	}		
	
	public JavaBreakpoint() {
		fRequestsByTarget = new HashMap(2);
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

	protected IMarker ensureMarker() throws DebugException {
		IMarker m = getMarker();
		if (m == null || !m.exists()) {
			throw new DebugException(new Status(IStatus.ERROR, JDIDebugModel.getPluginIdentifier(), IDebugStatusConstants.REQUEST_FAILED,
				JDIDebugModelMessages.getString("JavaBreakpoint.no_associated_marker"),null)); //$NON-NLS-1$
		}
		return m;
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
			reqs = new ArrayList(1);
			fRequestsByTarget.put(target, reqs);
		}
		reqs.add(request);
		target.addJDIEventListener(this, request);
		// update the install attibute on the breakpoint
		if (!(request instanceof ClassPrepareRequest)) {
			incrementInstallCount();
		}
	}
	
	/**
	 * Returns a string corresponding to the reference type
	 * name in which this breakpoint is installed or
	 * <code>null</code> if no reference type could be
	 * found.
	 */
	protected String getReferenceTypeName() {
		String name= null;
		try {
			IType type = getType();
			if (type != null) {
				while (type.getDeclaringType() != null) {
					type = type.getDeclaringType();
				}
				name= type.getFullyQualifiedName();
			}
		} catch (CoreException ce) {
			JDIDebugPlugin.logError(ce);
		}
		return name;
	}	
	
	/**
	 * Returns the requests that this breakpoint has installed
	 * in the given target.
	 */
	protected List getRequests(JDIDebugTarget target) {
		List list= (List)fRequestsByTarget.get(target);
		if (list == null) {
			list= new ArrayList(0);
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
		if (!(request instanceof ClassPrepareRequest)) {
			decrementInstallCount();
		}
	}

	/**
	 * Removes the <code>oldRequest</code> from the given target if present
	 * and replaces it with <code>newRequest</code>
	 */
	protected void replaceRequest(JDIDebugTarget target, EventRequest oldRequest, EventRequest newRequest) {
		List list = getRequests(target);
		list.remove(oldRequest);
		list.add(newRequest);
		target.removeJDIEventListener(this, oldRequest);
		target.addJDIEventListener(this, newRequest);
		// delete old request
		//on JDK you cannot delete (disable) an event request that has hit its count filter
		if (!isExpired(oldRequest)) {
			target.getEventRequestManager().deleteEventRequest(oldRequest); // disable & remove
		}			
	}

	/**
	 * @see IJDIEventListener#handleEvent(Event)
	 */
	public boolean handleEvent(Event event, JDIDebugTarget target) {
		if (event instanceof ClassPrepareEvent) {
			ClassPrepareEvent cpe = (ClassPrepareEvent)event;
			if (!installableReferenceType(cpe.referenceType())) {
				// Don't install this breakpoint in an
				// inappropriate type
				return true;
			}			
			try {
				createRequest(target, cpe.referenceType());
			} catch (CoreException e) {
				JDIDebugPlugin.logError(e);
			}
			return true;
		} else {
			ThreadReference threadRef= ((LocatableEvent)event).thread();
			JDIThread thread= target.findThread(threadRef);		
			if (thread == null) {
				return true;
			} else {
				thread.handleSuspendForBreakpoint(this);
				expireHitCount(event);	
				return false;
			}						
		}		
	}
	
	/**
	 * Returns whether the given reference type is appropriate for this
	 * breakpoint to be installed in.
	 */
	protected boolean installableReferenceType(ReferenceType type) {
		String installableType= getReferenceTypeName();
		String queriedType= type.name();
		if (installableType == null || queriedType == null) {
			return false;
		}
		int installableLength= installableType.length();
		int queriedLength= queriedType.length();
		if (queriedType.regionMatches(0, installableType, 0, installableLength)) {
			if (queriedLength > installableLength) {
				if (queriedType.charAt(installableLength) == '$') {
					return true;
				}
			} else {
				return true;
			}
		}
		return false;
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
				JDIDebugPlugin.logError(ce);
			}
		}
	}	

	/**
	 * Create a breakpoint request for this breakpoint in the given
	 * reference type in the given target.
	 */
	protected void createRequest(JDIDebugTarget target, ReferenceType type) throws CoreException {
		EventRequest request= newRequest(target, type);
		registerRequest(request, target);
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
	protected void addToTarget(JDIDebugTarget target) throws CoreException {
		String referenceTypeName= getReferenceTypeName();
		if (referenceTypeName == null) {
			return;
		}
		
		// create request to listen to class loads
		registerRequest(target.createClassPrepareRequest(referenceTypeName), target);
		
		// create breakpoint requests for each class currently loaded
		List classes= target.jdiClassesByName(referenceTypeName);
		if (!classes.isEmpty()) {
			Iterator iter = classes.iterator();
			while (iter.hasNext()) {
				ReferenceType type= (ReferenceType) iter.next();
				createRequest(target, type);
			}
		}
	}
	
	/**
	 * Update all requests that this breakpoint has installed in the
	 * given target to reflect the current state of this breakpoint.
	 */
	protected void changeForTarget(JDIDebugTarget target) throws CoreException {
		List requests = getRequests(target);
		if (!requests.isEmpty()) {
			Iterator iter = requests.iterator();
			while (iter.hasNext()) {
				EventRequest req = (EventRequest)iter.next();
				if (!(req instanceof ClassPrepareRequest)) {
					updateRequest(req, target);
				}
			}
		}
	}

	/**
	 * Update the given request in the given target to reflect
	 * the current state of this breakpoint.
	 */
	protected void updateRequest(EventRequest request, JDIDebugTarget target) throws CoreException {
		updateEnabledState(request);
		EventRequest newRequest = updateHitCount(request, target);
		if (newRequest != request) {
			replaceRequest(target, request, newRequest);
			request = newRequest;
		}
	}
	
	/**
	 * Update the given request in the given debug target to
	 * reflect the current hit count of this breakpoint.
	 */
	protected abstract EventRequest updateHitCount(EventRequest request, JDIDebugTarget target) throws CoreException;
	
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
	protected void removeFromTarget(JDIDebugTarget target) throws CoreException {
		List requests = getRequests(target);
		Iterator iter = requests.iterator();
		while (iter.hasNext()) {
			EventRequest req = (EventRequest)iter.next();
			try {				
				if (!isExpired(req)) { // cannot delete an expired request
					target.getEventRequestManager().deleteEventRequest(req); // disable & remove
				}
			} catch (VMDisconnectedException e) {
				if (!(target.isDisconnected() || target.isTerminated())) {
					JDIDebugPlugin.logError(e);
				}
			} catch (RuntimeException e) {
				JDIDebugPlugin.logError(e);
			} finally {
				deregisterRequest(req, target);
			}
		}
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
				// if the request has expired, and is not a method entry request, do not disable.
				// BreakpointRequests that have expired cannot be deleted. However method entry 
				// requests that are expired can be deleted (since we simulate the hit count)
				if (request instanceof MethodEntryRequest || !isExpired(request)) {
					request.setEnabled(enabled);
				}
			} catch (VMDisconnectedException e) {
			} catch (RuntimeException e) {
				JDIDebugPlugin.logError(e);
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
		ensureMarker().setAttribute(INSTALL_COUNT, count + 1);
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
			ensureMarker().setAttribute(INSTALL_COUNT, count - 1);	
		}
		if (count == 1) {
			if (isExpired()) {
				// if breakpoint was auto-disabled, re-enable it
				ensureMarker().setAttributes(fgExpiredEnabledAttributes,
						new Object[]{Boolean.FALSE, Boolean.TRUE});
			}
		}
	}

	/**
	 * Sets the type in which this breakpoint is set.
	 */
	protected void setType(IType type) throws CoreException {
		String handle = type.getHandleIdentifier();
		ensureMarker().setAttribute(TYPE_HANDLE, handle);
	}
	
	/**
	 * @see IJavaBreakpoint#getType()
	 */
	public IType getType() throws CoreException {
		String handle = (String) ensureMarker().getAttribute(TYPE_HANDLE);
		if (handle != null) {
			return (IType)JavaCore.create(handle);
		}
		return null;
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
		List attributes= new ArrayList(3);
		List values= new ArrayList(3);
		if (isInstalled()) {
			attributes.add(INSTALL_COUNT);
			values.add(new Integer(0));
		}
		if (isExpired()) {
			// if breakpoint was auto-disabled, re-enable it
			attributes.add(EXPIRED);
			values.add(Boolean.FALSE);
			attributes.add(IDebugConstants.ENABLED);
			values.add(Boolean.TRUE);
		}
		if (!attributes.isEmpty()) {
			String[] strAttributes= new String[attributes.size()];
			ensureMarker().setAttributes((String[])attributes.toArray(strAttributes), values.toArray());
		}
	}	

	/**
	 * Add this breakpoint to the breakpoint manager
	 */
	protected void addToBreakpointManager() throws DebugException {
		DebugPlugin.getDefault().getBreakpointManager().addBreakpoint(this);
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
			ensureMarker().setAttributes(new String []{HIT_COUNT, EXPIRED, IDebugConstants.ENABLED},
				new Object[]{new Integer(count), Boolean.FALSE, Boolean.TRUE});
		} else {
			ensureMarker().setAttributes(new String[]{HIT_COUNT, EXPIRED},
				new Object[]{new Integer(count), Boolean.FALSE});
		}
	}	
	
	/**
	 * Sets whether or not this breakpoint's hit count has expired.
	 */
	public void setExpired(boolean expired) throws CoreException {
		ensureMarker().setAttribute(EXPIRED, expired);	
	}	

}

