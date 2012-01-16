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
package org.eclipse.jdi.internal.request;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jdi.internal.FieldImpl;
import org.eclipse.jdi.internal.LocationImpl;
import org.eclipse.jdi.internal.MirrorImpl;
import org.eclipse.jdi.internal.ReferenceTypeImpl;
import org.eclipse.jdi.internal.ThreadReferenceImpl;
import org.eclipse.jdi.internal.VirtualMachineImpl;
import org.eclipse.jdi.internal.event.AccessWatchpointEventImpl;
import org.eclipse.jdi.internal.event.BreakpointEventImpl;
import org.eclipse.jdi.internal.event.ClassPrepareEventImpl;
import org.eclipse.jdi.internal.event.ClassUnloadEventImpl;
import org.eclipse.jdi.internal.event.EventImpl;
import org.eclipse.jdi.internal.event.ExceptionEventImpl;
import org.eclipse.jdi.internal.event.MethodEntryEventImpl;
import org.eclipse.jdi.internal.event.MethodExitEventImpl;
import org.eclipse.jdi.internal.event.ModificationWatchpointEventImpl;
import org.eclipse.jdi.internal.event.MonitorContendedEnterEventImpl;
import org.eclipse.jdi.internal.event.MonitorContendedEnteredEventImpl;
import org.eclipse.jdi.internal.event.MonitorWaitEventImpl;
import org.eclipse.jdi.internal.event.MonitorWaitedEventImpl;
import org.eclipse.jdi.internal.event.StepEventImpl;
import org.eclipse.jdi.internal.event.ThreadDeathEventImpl;
import org.eclipse.jdi.internal.event.ThreadStartEventImpl;
import org.eclipse.jdi.internal.event.VMDeathEventImpl;
import org.eclipse.osgi.util.NLS;

import com.sun.jdi.Field;
import com.sun.jdi.Location;
import com.sun.jdi.ObjectCollectedException;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VMMismatchException;
import com.sun.jdi.request.AccessWatchpointRequest;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.ClassUnloadRequest;
import com.sun.jdi.request.DuplicateRequestException;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.ExceptionRequest;
import com.sun.jdi.request.InvalidRequestStateException;
import com.sun.jdi.request.MethodEntryRequest;
import com.sun.jdi.request.MethodExitRequest;
import com.sun.jdi.request.ModificationWatchpointRequest;
import com.sun.jdi.request.MonitorContendedEnterRequest;
import com.sun.jdi.request.MonitorContendedEnteredRequest;
import com.sun.jdi.request.MonitorWaitRequest;
import com.sun.jdi.request.MonitorWaitedRequest;
import com.sun.jdi.request.StepRequest;
import com.sun.jdi.request.ThreadDeathRequest;
import com.sun.jdi.request.ThreadStartRequest;
import com.sun.jdi.request.VMDeathRequest;

/**
 * this class implements the corresponding interfaces
 * declared by the JDI specification. See the com.sun.jdi package
 * for more information.
 */
public class EventRequestManagerImpl extends MirrorImpl implements EventRequestManager, org.eclipse.jdi.hcr.EventRequestManager {
	/** Indexes used in arrays of request types. */
	private static final int ACCESS_WATCHPOINT_INDEX = 0;
	private static final int BREAKPOINT_INDEX = 1;
	private static final int CLASS_PREPARE_INDEX = 2;
	private static final int CLASS_UNLOAD_INDEX = 3;
	private static final int EXCEPTION_INDEX = 4;
	private static final int METHOD_ENTRY_INDEX = 5;
	private static final int METHOD_EXIT_INDEX = 6;
	private static final int MODIFICATION_WATCHPOINT_INDEX = 7;
	private static final int STEP_INDEX = 8;
	private static final int THREAD_DEATH_INDEX = 9;
	private static final int THREAD_START_INDEX = 10;
	private static final int VM_DEATH_INDEX = 11;
	private static final int MONITOR_CONTENDED_ENTERED_INDEX = 12;
	private static final int MONITOR_CONTENDED_ENTER_INDEX = 13;
	private static final int MONITOR_WAITED_INDEX = 14;
	private static final int MONITOR_WAIT_INDEX = 15;

	/** Set of all existing requests per request type. */
	private ArrayList<HashSet<EventRequest>> fRequests;

	/** Maps per request type of requestIDs to enabled requests. */
	private ArrayList<Hashtable<RequestID, EventRequest>> fEnabledRequests;
	
	/**
	 * Creates new EventRequestManager.
	 */
	public EventRequestManagerImpl(VirtualMachineImpl vmImpl) {
		super("EventRequestManager", vmImpl); //$NON-NLS-1$
		int size = MONITOR_WAIT_INDEX + 1;
		// Initialize list of requests.
		fRequests = new ArrayList<HashSet<EventRequest>>(size);
		for (int i = 0; i < size; i++) {
			fRequests.add(new HashSet<EventRequest>());
		}
		// Initialize map of request IDs to enabled requests.
		fEnabledRequests = new ArrayList<Hashtable<RequestID, EventRequest>>(size);
		for (int i = 0; i < size; i++) {
			fEnabledRequests.add(new Hashtable<RequestID, EventRequest>());
		}
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.request.EventRequestManager#createAccessWatchpointRequest(com.sun.jdi.Field)
	 */
	public AccessWatchpointRequest createAccessWatchpointRequest(Field field) {
		FieldImpl fieldImpl = (FieldImpl)field;
		AccessWatchpointRequestImpl req = new AccessWatchpointRequestImpl(virtualMachineImpl());
		req.addFieldFilter(fieldImpl);
		addEventRequest(ACCESS_WATCHPOINT_INDEX, req);
		return req;
	}
 
	/* (non-Javadoc)
	 * @see com.sun.jdi.request.EventRequestManager#createBreakpointRequest(com.sun.jdi.Location)
	 */
	public BreakpointRequest createBreakpointRequest(Location location) throws VMMismatchException {
		LocationImpl locImpl = (LocationImpl)location;
		BreakpointRequestImpl req = new BreakpointRequestImpl(virtualMachineImpl());
		req.addLocationFilter(locImpl);
		addEventRequest(BREAKPOINT_INDEX, req);
		return req;
	}
 
	/* (non-Javadoc)
	 * @see com.sun.jdi.request.EventRequestManager#createClassPrepareRequest()
	 */
	public ClassPrepareRequest createClassPrepareRequest() {
		ClassPrepareRequestImpl req = new ClassPrepareRequestImpl(virtualMachineImpl());
		addEventRequest(CLASS_PREPARE_INDEX, req);
		return req;
	} 
	
	/* (non-Javadoc)
	 * @see com.sun.jdi.request.EventRequestManager#createClassUnloadRequest()
	 */
	public ClassUnloadRequest createClassUnloadRequest() {
		ClassUnloadRequestImpl req = new ClassUnloadRequestImpl(virtualMachineImpl());
		addEventRequest(CLASS_UNLOAD_INDEX, req);
		return req;
	} 
 	 
	/* (non-Javadoc)
	 * @see com.sun.jdi.request.EventRequestManager#createExceptionRequest(com.sun.jdi.ReferenceType, boolean, boolean)
	 */
	public ExceptionRequest createExceptionRequest(ReferenceType refType, boolean notifyCaught, boolean notifyUncaught) {
		ReferenceTypeImpl refTypeImpl = (ReferenceTypeImpl)refType;
		ExceptionRequestImpl req = new ExceptionRequestImpl(virtualMachineImpl());
		req.addExceptionFilter(refTypeImpl, notifyCaught, notifyUncaught);
		addEventRequest(EXCEPTION_INDEX, req);
		return req;
	} 

	/* (non-Javadoc)
	 * @see com.sun.jdi.request.EventRequestManager#createMethodEntryRequest()
	 */
	public MethodEntryRequest createMethodEntryRequest() {
		MethodEntryRequestImpl req = new MethodEntryRequestImpl(virtualMachineImpl());
		addEventRequest(METHOD_ENTRY_INDEX, req);
		return req;
	} 

	/* (non-Javadoc)
	 * @see com.sun.jdi.request.EventRequestManager#createMethodExitRequest()
	 */
	public MethodExitRequest createMethodExitRequest() {
		MethodExitRequestImpl req = new MethodExitRequestImpl(virtualMachineImpl());
		addEventRequest(METHOD_EXIT_INDEX, req);
		return req;
	} 
	
	/* (non-Javadoc)
	 * @see com.sun.jdi.request.EventRequestManager#createMonitorContendedEnteredRequest()
	 */
	public MonitorContendedEnteredRequest createMonitorContendedEnteredRequest() {
		MonitorContendedEnteredRequestImpl req = new MonitorContendedEnteredRequestImpl(virtualMachineImpl());
		addEventRequest(MONITOR_CONTENDED_ENTERED_INDEX, req);
		return req;
	}
	
	/* (non-Javadoc)
	 * @see com.sun.jdi.request.EventRequestManager#createMonitorContendedEnterRequest()
	 */
	public MonitorContendedEnterRequest createMonitorContendedEnterRequest() {
		MonitorContendedEnterRequestImpl req = new MonitorContendedEnterRequestImpl(virtualMachineImpl());
		addEventRequest(MONITOR_CONTENDED_ENTER_INDEX, req);
		return req;
	}
	
	/* (non-Javadoc)
	 * @see com.sun.jdi.request.EventRequestManager#createMonitorWaitedRequest()
	 */
	public MonitorWaitedRequest createMonitorWaitedRequest() {
		MonitorWaitedRequestImpl req = new MonitorWaitedRequestImpl(virtualMachineImpl());
		addEventRequest(MONITOR_WAITED_INDEX, req);
		return req;
	}
	
	/* (non-Javadoc)
	 * @see com.sun.jdi.request.EventRequestManager#createMonitorWaitRequest()
	 */
	public MonitorWaitRequest createMonitorWaitRequest() {
		MonitorWaitRequestImpl req = new MonitorWaitRequestImpl(virtualMachineImpl());
		addEventRequest(MONITOR_WAIT_INDEX, req);
		return req;
	}
	
	/* (non-Javadoc)
	 * @see com.sun.jdi.request.EventRequestManager#createModificationWatchpointRequest(com.sun.jdi.Field)
	 */
	public ModificationWatchpointRequest createModificationWatchpointRequest(Field field) {
		FieldImpl fieldImpl = (FieldImpl)field;
		ModificationWatchpointRequestImpl req = new ModificationWatchpointRequestImpl(virtualMachineImpl());
		req.addFieldFilter(fieldImpl);
		addEventRequest(MODIFICATION_WATCHPOINT_INDEX, req);
		return req;
	} 
	 
	/* (non-Javadoc)
	 * @see com.sun.jdi.request.EventRequestManager#createStepRequest(com.sun.jdi.ThreadReference, int, int)
	 */
	public StepRequest createStepRequest(ThreadReference thread, int size, int depth) throws DuplicateRequestException, ObjectCollectedException {
	   	ThreadReferenceImpl threadImpl = (ThreadReferenceImpl)thread;
		StepRequestImpl req = new StepRequestImpl(virtualMachineImpl());		
		req.addStepFilter(threadImpl, size, depth);
		addEventRequest(STEP_INDEX, req);
		return req;
	} 

	/* (non-Javadoc)
	 * @see com.sun.jdi.request.EventRequestManager#createThreadDeathRequest()
	 */
	public ThreadDeathRequest createThreadDeathRequest() {
		ThreadDeathRequestImpl req = new ThreadDeathRequestImpl(virtualMachineImpl());
		addEventRequest(THREAD_DEATH_INDEX, req);
		return req;
	} 

	/* (non-Javadoc)
	 * @see com.sun.jdi.request.EventRequestManager#createThreadStartRequest()
	 */
	public ThreadStartRequest createThreadStartRequest() {
		ThreadStartRequestImpl req = new ThreadStartRequestImpl(virtualMachineImpl());
		addEventRequest(THREAD_START_INDEX, req);
		return req;
	}
	
	/*
	 * @see EventRequestManager#createVMDeathRequest()
	 */
	public VMDeathRequest createVMDeathRequest() {
		VMDeathRequestImpl req = new VMDeathRequestImpl(virtualMachineImpl());
		addEventRequest(VM_DEATH_INDEX, req);
		return req;
	}	

	/* (non-Javadoc)
	 * @see org.eclipse.jdi.hcr.EventRequestManager#createReenterStepRequest(com.sun.jdi.ThreadReference)
	 */
	public org.eclipse.jdi.hcr.ReenterStepRequest createReenterStepRequest(ThreadReference thread) {
		virtualMachineImpl().checkHCRSupported();
	   	ThreadReferenceImpl threadImpl = (ThreadReferenceImpl)thread;
		ReenterStepRequestImpl req = new ReenterStepRequestImpl(virtualMachineImpl());
		// Note that the StepFilter is only used to specify the thread.
		// The size is ignored and the depth will always be writter as HCR_STEP_DEPTH_REENTER_JDWP.
		req.addStepFilter(threadImpl, StepRequest.STEP_MIN, 0);
		// Since this is a special case of a step request, we use the same request list.
		addEventRequest(STEP_INDEX, req);
		return req;
	}
	
	/**
	 * Enables class prepare requests for all loaded classes.  This is
	 * necessary for current versions of the KVM to function correctly.
	 * This method is only called when the remote VM is determined to be
	 * the KVM.
	 */
	public void enableInternalClassPrepareEvent() {
		// Note that these requests are not stored in the set of outstanding requests because
		// they must be invisible from outside.
		ClassPrepareRequestImpl requestPrepare =
			new ClassPrepareRequestImpl(virtualMachineImpl());
		requestPrepare.setGeneratedInside();
		requestPrepare.setSuspendPolicy(EventRequest.SUSPEND_NONE);

		requestPrepare.enable();
	}
	
	/**
	 * Creates ClassUnloadRequest for maintaining class information for within JDI.
	 * Needed to known when to flush the cache.
	 */ 
	public void enableInternalClasUnloadEvent(/* TBD: ReferenceTypeImpl refType*/) {
		// Note that these requests are not stored in the set of outstanding requests because
		// they must be invisible from outside.
		ClassUnloadRequestImpl reqUnload = new ClassUnloadRequestImpl(virtualMachineImpl());
		reqUnload.setGeneratedInside();
		// tbd: It is now yet possible to only ask for unload events for
		// classes that we know of due to a limitation in the J9 VM.
		// reqUnload.addClassFilter(refType);
		reqUnload.setSuspendPolicy(EventRequest.SUSPEND_NONE);
		reqUnload.enable();
	}

	/**
	 * Checks if a steprequest is for the given thread is already enabled.
	 */ 
	boolean existsEnabledStepRequest(ThreadReferenceImpl threadImpl) {
		Enumeration<? extends EventRequest> enumeration = fEnabledRequests.get(STEP_INDEX).elements();
		StepRequestImpl step;
		while (enumeration.hasMoreElements()) {
			step = (StepRequestImpl)enumeration.nextElement();
			if (step.thread() == threadImpl)
				return true;
		}
		return false;
	}
 
	/* (non-Javadoc)
	 * @see com.sun.jdi.request.EventRequestManager#deleteAllBreakpoints()
	 */
	public void deleteAllBreakpoints() {
		EventRequestImpl.clearAllBreakpoints(this);
		fRequests.get(BREAKPOINT_INDEX).clear();
		fEnabledRequests.get(BREAKPOINT_INDEX).clear();
	}

	/**
	 * Adds an EventRequests to the given list.
	 */ 
	public void addEventRequest(int index, EventRequest req) {
		fRequests.get(index).add(req);
	}

	/**
	 * Deletes an EventRequest.
	 */ 
	private void deleteEventRequest(int index, EventRequest req) throws VMMismatchException {
		// Remove request from list of requests and from the mapping of requestIDs to requests.
		checkVM(req);
		EventRequestImpl requestImpl = (EventRequestImpl)req;
		fRequests.get(index).remove(requestImpl);
		if (requestImpl.requestID() != null)
			fEnabledRequests.get(index).remove(requestImpl.requestID());
	}
 
	/* (non-Javadoc)
	 * @see com.sun.jdi.request.EventRequestManager#deleteEventRequest(com.sun.jdi.request.EventRequest)
	 */
	public void deleteEventRequest(EventRequest req) {
		// Disable request, note that this also causes the event request to be removed from fEnabledRequests.
		try {
			req.disable();
		} catch (InvalidRequestStateException exception) {
			// The event has already been removed from the VM.
		}
		
		// Remove request from list.
		if (req instanceof AccessWatchpointRequestImpl)
			deleteEventRequest(ACCESS_WATCHPOINT_INDEX, req);
		else if (req instanceof BreakpointRequestImpl)
			deleteEventRequest(BREAKPOINT_INDEX, req);
		else if (req instanceof ClassPrepareRequestImpl)
			deleteEventRequest(CLASS_PREPARE_INDEX, req);
		else if (req instanceof ClassUnloadRequestImpl)
			deleteEventRequest(CLASS_UNLOAD_INDEX, req);
		else if (req instanceof ExceptionRequestImpl)
			deleteEventRequest(EXCEPTION_INDEX, req);
		else if (req instanceof MethodEntryRequestImpl)
			deleteEventRequest(METHOD_ENTRY_INDEX, req);
		else if (req instanceof MethodExitRequestImpl)
			deleteEventRequest(METHOD_EXIT_INDEX, req);
		else if (req instanceof ModificationWatchpointRequestImpl)
			deleteEventRequest(MODIFICATION_WATCHPOINT_INDEX, req);
		else if (req instanceof StepRequestImpl)
			deleteEventRequest(STEP_INDEX, req);
		else if (req instanceof ThreadDeathRequestImpl)
			deleteEventRequest(THREAD_DEATH_INDEX, req);
		else if (req instanceof ThreadStartRequestImpl)
			deleteEventRequest(THREAD_START_INDEX, req);
		else if(req instanceof MonitorContendedEnterRequestImpl) {
			deleteEventRequest(MONITOR_CONTENDED_ENTER_INDEX, req);
		}
		else if(req instanceof MonitorContendedEnteredRequestImpl) {
			deleteEventRequest(MONITOR_CONTENDED_ENTERED_INDEX, req);
		}
		else if(req instanceof MonitorWaitRequestImpl) {
			deleteEventRequest(MONITOR_WAIT_INDEX, req);
		}
		else if(req instanceof MonitorWaitedRequestImpl) {
			deleteEventRequest(MONITOR_WAITED_INDEX, req);
		}
		else
		
		throw new InternalError(NLS.bind(RequestMessages.EventRequestManagerImpl_EventRequest_type_of__0__is_unknown_1, new String[]{req.toString()})); 
	}
 
	/* (non-Javadoc)
	 * @see com.sun.jdi.request.EventRequestManager#deleteEventRequests(java.util.List)
	 */
	public void deleteEventRequests(List<? extends EventRequest> requests) throws VMMismatchException {
		Iterator<? extends EventRequest> iter = requests.iterator();
		while(iter.hasNext()) {
			Object obj = iter.next();
			deleteEventRequest((EventRequest)obj);
		}
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.request.EventRequestManager#accessWatchpointRequests()
	 */
	public List<AccessWatchpointRequest> accessWatchpointRequests() {
		return new ArrayList<AccessWatchpointRequest>((Collection<? extends AccessWatchpointRequest>) fRequests.get(ACCESS_WATCHPOINT_INDEX));
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.request.EventRequestManager#breakpointRequests()
	 */
	public List<BreakpointRequest> breakpointRequests() {
		return new ArrayList<BreakpointRequest>((Collection<? extends BreakpointRequest>) fRequests.get(BREAKPOINT_INDEX));
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.request.EventRequestManager#classPrepareRequests()
	 */
	public List<ClassPrepareRequest> classPrepareRequests() {
		return new ArrayList<ClassPrepareRequest>((Collection<? extends ClassPrepareRequest>) fRequests.get(CLASS_PREPARE_INDEX));
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.request.EventRequestManager#classUnloadRequests()
	 */
	public List<ClassUnloadRequest> classUnloadRequests() {
		return new ArrayList<ClassUnloadRequest>((Collection<? extends ClassUnloadRequest>) fRequests.get(CLASS_UNLOAD_INDEX));
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.request.EventRequestManager#exceptionRequests()
	 */
	public List<ExceptionRequest> exceptionRequests() {
		return new ArrayList<ExceptionRequest>((Collection<? extends ExceptionRequest>) fRequests.get(EXCEPTION_INDEX));
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.request.EventRequestManager#methodEntryRequests()
	 */
	public List<MethodEntryRequest> methodEntryRequests() {
		return new ArrayList<MethodEntryRequest>((Collection<? extends MethodEntryRequest>) fRequests.get(METHOD_ENTRY_INDEX));
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.request.EventRequestManager#methodExitRequests()
	 */
	public List<MethodExitRequest> methodExitRequests() {
		return new ArrayList<MethodExitRequest>((Collection<? extends MethodExitRequest>) fRequests.get(METHOD_EXIT_INDEX));
	}
	
	/* (non-Javadoc)
	 * @see com.sun.jdi.request.EventRequestManager#modificationWatchpointRequests()
	 */
	public List<ModificationWatchpointRequest> modificationWatchpointRequests() {
		return new ArrayList<ModificationWatchpointRequest>((Collection<? extends ModificationWatchpointRequest>) fRequests.get(MODIFICATION_WATCHPOINT_INDEX));
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.request.EventRequestManager#stepRequests()
	 */
	public List<StepRequest> stepRequests() {
		return new ArrayList<StepRequest>((Collection<? extends StepRequest>) fRequests.get(STEP_INDEX));
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.request.EventRequestManager#threadDeathRequests()
	 */
	public List<ThreadDeathRequest> threadDeathRequests() {
		return new ArrayList<ThreadDeathRequest>((Collection<? extends ThreadDeathRequest>) fRequests.get(THREAD_DEATH_INDEX));
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.request.EventRequestManager#threadStartRequests()
	 */
	public List<ThreadStartRequest> threadStartRequests() {
		return new ArrayList<ThreadStartRequest>((Collection<? extends ThreadStartRequest>) fRequests.get(THREAD_START_INDEX));
	}
	
	/* (non-Javadoc)
	 * @see com.sun.jdi.request.EventRequestManager#vmDeathRequests()
	 */
	public List<VMDeathRequest> vmDeathRequests() {
		return new ArrayList<VMDeathRequest>((Collection<? extends VMDeathRequest>) fRequests.get(VM_DEATH_INDEX));
	}

	public void removeRequestIDMapping(EventRequestImpl req) {
		if (req instanceof AccessWatchpointRequestImpl)
			fEnabledRequests.get(ACCESS_WATCHPOINT_INDEX).remove(req.requestID());
		else if (req instanceof BreakpointRequestImpl)
			fEnabledRequests.get(BREAKPOINT_INDEX).remove(req.requestID());
		else if (req instanceof ClassPrepareRequestImpl)
			fEnabledRequests.get(CLASS_PREPARE_INDEX).remove(req.requestID());
		else if (req instanceof ClassUnloadRequestImpl)
			fEnabledRequests.get(CLASS_UNLOAD_INDEX).remove(req.requestID());
		else if (req instanceof ExceptionRequestImpl)
			fEnabledRequests.get(EXCEPTION_INDEX).remove(req.requestID());
		else if (req instanceof MethodEntryRequestImpl)
			fEnabledRequests.get(METHOD_ENTRY_INDEX).remove(req.requestID());
		else if (req instanceof MethodExitRequestImpl)
			fEnabledRequests.get(METHOD_EXIT_INDEX).remove(req.requestID());
		else if (req instanceof ModificationWatchpointRequestImpl)
			fEnabledRequests.get(MODIFICATION_WATCHPOINT_INDEX).remove(req.requestID());
		else if (req instanceof StepRequestImpl)
			fEnabledRequests.get(STEP_INDEX).remove(req.requestID());
		else if (req instanceof ThreadDeathRequestImpl)
			fEnabledRequests.get(THREAD_DEATH_INDEX).remove(req.requestID());
		else if (req instanceof ThreadStartRequestImpl)
			fEnabledRequests.get(THREAD_START_INDEX).remove(req.requestID());
		else if(req instanceof MonitorContendedEnterRequestImpl) {
			fEnabledRequests.get(MONITOR_CONTENDED_ENTER_INDEX).remove(req.requestID());
		}
		else if(req instanceof MonitorContendedEnteredRequestImpl) {
			fEnabledRequests.get(MONITOR_CONTENDED_ENTERED_INDEX).remove(req.requestID());
		}
		else if(req instanceof MonitorWaitRequestImpl) {
			fEnabledRequests.get(MONITOR_WAIT_INDEX).remove(req.requestID());
		}
		else if(req instanceof MonitorWaitedRequestImpl) {
			fEnabledRequests.get(MONITOR_WAITED_INDEX).remove(req.requestID());
		}
	}
	
	/**
	 * Maps a request ID to requests.
	 */ 
	public void addRequestIDMapping(EventRequestImpl req) {
		if (req instanceof AccessWatchpointRequestImpl)
			fEnabledRequests.get(ACCESS_WATCHPOINT_INDEX).put(req.requestID(), req);
		else if (req instanceof BreakpointRequestImpl)
			fEnabledRequests.get(BREAKPOINT_INDEX).put(req.requestID(), req);
		else if (req instanceof ClassPrepareRequestImpl)
			fEnabledRequests.get(CLASS_PREPARE_INDEX).put(req.requestID(), req);
		else if (req instanceof ClassUnloadRequestImpl)
			fEnabledRequests.get(CLASS_UNLOAD_INDEX).put(req.requestID(), req);
		else if (req instanceof ExceptionRequestImpl)
			fEnabledRequests.get(EXCEPTION_INDEX).put(req.requestID(), req);
		else if (req instanceof MethodEntryRequestImpl)
			fEnabledRequests.get(METHOD_ENTRY_INDEX).put(req.requestID(), req);
		else if (req instanceof MethodExitRequestImpl)
			fEnabledRequests.get(METHOD_EXIT_INDEX).put(req.requestID(), req);
		else if (req instanceof ModificationWatchpointRequestImpl)
			fEnabledRequests.get(MODIFICATION_WATCHPOINT_INDEX).put(req.requestID(), req);
		else if (req instanceof StepRequestImpl)
			fEnabledRequests.get(STEP_INDEX).put(req.requestID(), req);
		else if (req instanceof ThreadDeathRequestImpl)
			fEnabledRequests.get(THREAD_DEATH_INDEX).put(req.requestID(), req);
		else if (req instanceof ThreadStartRequestImpl)
			fEnabledRequests.get(THREAD_START_INDEX).put(req.requestID(), req);
		else if(req instanceof MonitorWaitRequestImpl) {
			fEnabledRequests.get(MONITOR_WAIT_INDEX).put(req.requestID(), req);
		}
		else if(req instanceof MonitorWaitedRequestImpl) {
			fEnabledRequests.get(MONITOR_WAITED_INDEX).put(req.requestID(), req);
		}
		else if(req instanceof MonitorContendedEnterRequestImpl) {
			fEnabledRequests.get(MONITOR_CONTENDED_ENTER_INDEX).put(req.requestID(), req);
		}
		else if(req instanceof MonitorContendedEnteredRequestImpl) {
			fEnabledRequests.get(MONITOR_CONTENDED_ENTERED_INDEX).put(req.requestID(), req);
		}
	}

	/**
	 * Find Request that matches event.
	 */ 
	public EventRequest findRequest(EventImpl event) {
		if (event instanceof AccessWatchpointEventImpl)
			return fEnabledRequests.get(ACCESS_WATCHPOINT_INDEX).get(event.requestID());
		else if (event instanceof BreakpointEventImpl)
			return fEnabledRequests.get(BREAKPOINT_INDEX).get(event.requestID());
		else if (event instanceof ClassPrepareEventImpl)
			return fEnabledRequests.get(CLASS_PREPARE_INDEX).get(event.requestID());
		else if (event instanceof ClassUnloadEventImpl)
			return fEnabledRequests.get(CLASS_UNLOAD_INDEX).get(event.requestID());
		else if (event instanceof ExceptionEventImpl)
			return fEnabledRequests.get(EXCEPTION_INDEX).get(event.requestID());
		else if (event instanceof MethodEntryEventImpl)
			return fEnabledRequests.get(METHOD_ENTRY_INDEX).get(event.requestID());
		else if (event instanceof MethodExitEventImpl)
			return fEnabledRequests.get(METHOD_EXIT_INDEX).get(event.requestID());
		else if (event instanceof ModificationWatchpointEventImpl)
			return fEnabledRequests.get(MODIFICATION_WATCHPOINT_INDEX).get(event.requestID());
		else if (event instanceof StepEventImpl)
			return fEnabledRequests.get(STEP_INDEX).get(event.requestID());
		else if (event instanceof ThreadDeathEventImpl)
			return fEnabledRequests.get(THREAD_DEATH_INDEX).get(event.requestID());
		else if (event instanceof ThreadStartEventImpl)
			return fEnabledRequests.get(THREAD_START_INDEX).get(event.requestID());
		else if (event instanceof VMDeathEventImpl)
			return fEnabledRequests.get(VM_DEATH_INDEX).get(event.requestID());
		else if(event instanceof MonitorWaitEventImpl) {
			return fEnabledRequests.get(MONITOR_WAIT_INDEX).get(event.requestID());
		}
		else if(event instanceof MonitorWaitedEventImpl) {
			return fEnabledRequests.get(MONITOR_WAITED_INDEX).get(event.requestID());
		}
		else if(event instanceof MonitorContendedEnterEventImpl) {
			return fEnabledRequests.get(MONITOR_CONTENDED_ENTER_INDEX).get(event.requestID());
		}
		else if(event instanceof MonitorContendedEnteredEventImpl) {
			return fEnabledRequests.get(MONITOR_CONTENDED_ENTERED_INDEX).get(event.requestID());
		}
		else
			throw new InternalError(RequestMessages.EventRequestManagerImpl_Got_event_of_unknown_type_2); 
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.request.EventRequestManager#monitorContendedEnterRequests()
	 */
	public List<MonitorContendedEnterRequest> monitorContendedEnterRequests() {
		return new ArrayList<MonitorContendedEnterRequest>((Collection<? extends MonitorContendedEnterRequest>) fRequests.get(MONITOR_CONTENDED_ENTER_INDEX));
	}

    /* (non-Javadoc)
     * @see com.sun.jdi.request.EventRequestManager#monitorContendedEnteredRequests()
     */
    public List<MonitorContendedEnteredRequest> monitorContendedEnteredRequests() {
    	return new ArrayList<MonitorContendedEnteredRequest>((Collection<? extends MonitorContendedEnteredRequest>) fRequests.get(MONITOR_CONTENDED_ENTERED_INDEX));
    }
    
    /* (non-Javadoc)
     * @see com.sun.jdi.request.EventRequestManager#monitorWaitRequests()
     */
    public List<MonitorWaitRequest> monitorWaitRequests() {
    	return new ArrayList<MonitorWaitRequest>((Collection<? extends MonitorWaitRequest>) fRequests.get(MONITOR_WAIT_INDEX));
    }

    /* (non-Javadoc)
     * @see com.sun.jdi.request.EventRequestManager#monitorWaitedRequests()
     */
    public List<MonitorWaitedRequest> monitorWaitedRequests() {
    	return new ArrayList<MonitorWaitedRequest>((Collection<? extends MonitorWaitedRequest>) fRequests.get(MONITOR_WAITED_INDEX));
    }
}
