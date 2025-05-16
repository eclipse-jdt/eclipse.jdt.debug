/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdi.internal.request;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
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

	private static class EventRequestType<RT extends EventRequest> {

		private final ArrayList<RT> requests;
		private final Hashtable<RequestID, RT> enabledrequests;

		private EventRequestType() {
			requests= new ArrayList<>();
			enabledrequests= new Hashtable<>();
		}

		public List<RT> getUnmodifiableList() {
			return Collections.unmodifiableList(requests);
		}

		public void clear() {
			requests.clear();
			enabledrequests.clear();
		}
	}

	private final EventRequestType<AccessWatchpointRequest> ACCESS_WATCHPOINT_TYPE= new EventRequestType<>();
	private final EventRequestType<BreakpointRequest> BREAKPOINT_TYPE= new EventRequestType<>();
	private final EventRequestType<ClassPrepareRequest> CLASS_PREPARE_TYPE= new EventRequestType<>();
	private final EventRequestType<ClassUnloadRequest> CLASS_UNLOAD_TYPE= new EventRequestType<>();
	private final EventRequestType<MethodEntryRequest> METHOD_ENTRY_TYPE= new EventRequestType<>();
	private final EventRequestType<MethodExitRequest> METHOD_EXIT_TYPE= new EventRequestType<>();
	private final EventRequestType<ExceptionRequest> EXCEPTION_TYPE= new EventRequestType<>();
	private final EventRequestType<ModificationWatchpointRequest> MODIFICATION_WATCHPOINT_TYPE= new EventRequestType<>();
	private final EventRequestType<StepRequest> STEP_TYPE= new EventRequestType<>();
	private final EventRequestType<ThreadDeathRequest> THREAD_DEATH_TYPE= new EventRequestType<>();
	private final EventRequestType<ThreadStartRequest> THREAD_START_TYPE= new EventRequestType<>();
	private final EventRequestType<VMDeathRequest> VM_DEATH_TYPE= new EventRequestType<>();
	private final EventRequestType<MonitorContendedEnteredRequest> MONITOR_CONTENDED_ENTERED_TYPE= new EventRequestType<>();
	private final EventRequestType<MonitorContendedEnterRequest> MONITOR_CONTENDED_ENTER_TYPE= new EventRequestType<>();
	private final EventRequestType<MonitorWaitedRequest> MONITOR_WAITED_TYPE= new EventRequestType<>();
	private final EventRequestType<MonitorWaitRequest> MONITOR_WAIT_TYPE= new EventRequestType<>();

	/**
	 * Creates new EventRequestManager.
	 */
	public EventRequestManagerImpl(VirtualMachineImpl vmImpl) {
		super("EventRequestManager", vmImpl); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.request.EventRequestManager#createAccessWatchpointRequest(com.sun.jdi.Field)
	 */
	@Override
	public AccessWatchpointRequest createAccessWatchpointRequest(Field field) {
		FieldImpl fieldImpl = (FieldImpl)field;
		AccessWatchpointRequestImpl req = new AccessWatchpointRequestImpl(virtualMachineImpl());
		req.addFieldFilter(fieldImpl);
		ACCESS_WATCHPOINT_TYPE.requests.add(req);
		return req;
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.request.EventRequestManager#createBreakpointRequest(com.sun.jdi.Location)
	 */
	@Override
	public BreakpointRequest createBreakpointRequest(Location location) throws VMMismatchException {
		LocationImpl locImpl = (LocationImpl)location;
		BreakpointRequestImpl req = new BreakpointRequestImpl(virtualMachineImpl());
		req.addLocationFilter(locImpl);
		BREAKPOINT_TYPE.requests.add(req);
		return req;
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.request.EventRequestManager#createClassPrepareRequest()
	 */
	@Override
	public ClassPrepareRequest createClassPrepareRequest() {
		ClassPrepareRequestImpl req = new ClassPrepareRequestImpl(virtualMachineImpl());
		CLASS_PREPARE_TYPE.requests.add(req);
		return req;
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.request.EventRequestManager#createClassUnloadRequest()
	 */
	@Override
	public ClassUnloadRequest createClassUnloadRequest() {
		ClassUnloadRequestImpl req = new ClassUnloadRequestImpl(virtualMachineImpl());
		CLASS_UNLOAD_TYPE.requests.add(req);
		return req;
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.request.EventRequestManager#createExceptionRequest(com.sun.jdi.ReferenceType, boolean, boolean)
	 */
	@Override
	public ExceptionRequest createExceptionRequest(ReferenceType refType, boolean notifyCaught, boolean notifyUncaught) {
		ReferenceTypeImpl refTypeImpl = (ReferenceTypeImpl)refType;
		ExceptionRequestImpl req = new ExceptionRequestImpl(virtualMachineImpl());
		req.addExceptionFilter(refTypeImpl, notifyCaught, notifyUncaught);
		EXCEPTION_TYPE.requests.add(req);
		return req;
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.request.EventRequestManager#createMethodEntryRequest()
	 */
	@Override
	public MethodEntryRequest createMethodEntryRequest() {
		MethodEntryRequestImpl req = new MethodEntryRequestImpl(virtualMachineImpl());
		METHOD_ENTRY_TYPE.requests.add(req);
		return req;
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.request.EventRequestManager#createMethodExitRequest()
	 */
	@Override
	public MethodExitRequest createMethodExitRequest() {
		MethodExitRequestImpl req = new MethodExitRequestImpl(virtualMachineImpl());
		METHOD_EXIT_TYPE.requests.add(req);
		return req;
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.request.EventRequestManager#createMonitorContendedEnteredRequest()
	 */
	@Override
	public MonitorContendedEnteredRequest createMonitorContendedEnteredRequest() {
		MonitorContendedEnteredRequestImpl req = new MonitorContendedEnteredRequestImpl(virtualMachineImpl());
		MONITOR_CONTENDED_ENTERED_TYPE.requests.add(req);
		return req;
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.request.EventRequestManager#createMonitorContendedEnterRequest()
	 */
	@Override
	public MonitorContendedEnterRequest createMonitorContendedEnterRequest() {
		MonitorContendedEnterRequestImpl req = new MonitorContendedEnterRequestImpl(virtualMachineImpl());
		MONITOR_CONTENDED_ENTER_TYPE.requests.add(req);
		return req;
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.request.EventRequestManager#createMonitorWaitedRequest()
	 */
	@Override
	public MonitorWaitedRequest createMonitorWaitedRequest() {
		MonitorWaitedRequestImpl req = new MonitorWaitedRequestImpl(virtualMachineImpl());
		MONITOR_WAITED_TYPE.requests.add(req);
		return req;
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.request.EventRequestManager#createMonitorWaitRequest()
	 */
	@Override
	public MonitorWaitRequest createMonitorWaitRequest() {
		MonitorWaitRequestImpl req = new MonitorWaitRequestImpl(virtualMachineImpl());
		MONITOR_WAIT_TYPE.requests.add(req);
		return req;
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.request.EventRequestManager#createModificationWatchpointRequest(com.sun.jdi.Field)
	 */
	@Override
	public ModificationWatchpointRequest createModificationWatchpointRequest(Field field) {
		FieldImpl fieldImpl = (FieldImpl)field;
		ModificationWatchpointRequestImpl req = new ModificationWatchpointRequestImpl(virtualMachineImpl());
		req.addFieldFilter(fieldImpl);
		MODIFICATION_WATCHPOINT_TYPE.requests.add(req);
		return req;
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.request.EventRequestManager#createStepRequest(com.sun.jdi.ThreadReference, int, int)
	 */
	@Override
	public StepRequest createStepRequest(ThreadReference thread, int size, int depth) throws DuplicateRequestException, ObjectCollectedException {
	   	ThreadReferenceImpl threadImpl = (ThreadReferenceImpl)thread;
		StepRequestImpl req = new StepRequestImpl(virtualMachineImpl());
		req.addStepFilter(threadImpl, size, depth);
		STEP_TYPE.requests.add(req);
		return req;
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.request.EventRequestManager#createThreadDeathRequest()
	 */
	@Override
	public ThreadDeathRequest createThreadDeathRequest() {
		ThreadDeathRequestImpl req = new ThreadDeathRequestImpl(virtualMachineImpl());
		THREAD_DEATH_TYPE.requests.add(req);
		return req;
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.request.EventRequestManager#createThreadStartRequest()
	 */
	@Override
	public ThreadStartRequest createThreadStartRequest() {
		ThreadStartRequestImpl req = new ThreadStartRequestImpl(virtualMachineImpl());
		THREAD_START_TYPE.requests.add(req);
		return req;
	}

	/*
	 * @see EventRequestManager#createVMDeathRequest()
	 */
	@Override
	public VMDeathRequest createVMDeathRequest() {
		VMDeathRequestImpl req = new VMDeathRequestImpl(virtualMachineImpl());
		VM_DEATH_TYPE.requests.add(req);
		return req;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdi.hcr.EventRequestManager#createReenterStepRequest(com.sun.jdi.ThreadReference)
	 */
	@Override
	public org.eclipse.jdi.hcr.ReenterStepRequest createReenterStepRequest(ThreadReference thread) {
		virtualMachineImpl().checkHCRSupported();
	   	ThreadReferenceImpl threadImpl = (ThreadReferenceImpl)thread;
		ReenterStepRequestImpl req = new ReenterStepRequestImpl(virtualMachineImpl());
		// Note that the StepFilter is only used to specify the thread.
		// The size is ignored and the depth will always be written as HCR_STEP_DEPTH_REENTER_JDWP.
		req.addStepFilter(threadImpl, StepRequest.STEP_MIN, 0);
		// Since this is a special case of a step request, we use the same request list.
		STEP_TYPE.requests.add(req);
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
		// TBD: It is now yet possible to only ask for unload events for
		// classes that we know of due to a limitation in the J9 VM.
		// reqUnload.addClassFilter(refType);
		reqUnload.setSuspendPolicy(EventRequest.SUSPEND_NONE);
		reqUnload.enable();
	}

	/**
	 * Checks if a steprequest is for the given thread is already enabled.
	 */
	boolean existsEnabledStepRequest(ThreadReferenceImpl threadImpl) {
		Enumeration<StepRequest> enumeration = STEP_TYPE.enabledrequests.elements();
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
	@Override
	public void deleteAllBreakpoints() {
		EventRequestImpl.clearAllBreakpoints(this);
		BREAKPOINT_TYPE.clear();
	}

	/**
	 * Deletes an EventRequest.
	 */
	private void deleteEventRequest(EventRequestType<? extends EventRequest> type, EventRequestImpl req) throws VMMismatchException {
		// Remove request from list of requests and from the mapping of requestIDs to requests.
		checkVM(req);
		type.requests.remove(req);
		RequestID id = req.requestID();
		if(id != null) {
			type.enabledrequests.remove(id);
		}
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.request.EventRequestManager#deleteEventRequest(com.sun.jdi.request.EventRequest)
	 */
	@Override
	public void deleteEventRequest(EventRequest req) {
		// Disable request, note that this also causes the event request to be removed from fEnabledRequests.
		try {
			req.disable();
		} catch (InvalidRequestStateException exception) {
			// The event has already been removed from the VM.
		}
		if (req instanceof AccessWatchpointRequestImpl) {
			deleteEventRequest(ACCESS_WATCHPOINT_TYPE, (AccessWatchpointRequestImpl) req);
		} else if (req instanceof BreakpointRequestImpl) {
			deleteEventRequest(BREAKPOINT_TYPE, (BreakpointRequestImpl)req);
		} else if (req instanceof ClassPrepareRequestImpl) {
			deleteEventRequest(CLASS_PREPARE_TYPE, (ClassPrepareRequestImpl)req);
		} else if (req instanceof ClassUnloadRequestImpl) {
			deleteEventRequest(CLASS_UNLOAD_TYPE, (ClassUnloadRequestImpl)req);
		} else if (req instanceof ExceptionRequestImpl) {
			deleteEventRequest(EXCEPTION_TYPE, (ExceptionRequestImpl)req);
		} else if (req instanceof MethodEntryRequestImpl) {
			deleteEventRequest(METHOD_ENTRY_TYPE, (MethodEntryRequestImpl)req);
		} else if (req instanceof MethodExitRequestImpl) {
			deleteEventRequest(METHOD_EXIT_TYPE, (MethodExitRequestImpl)req);
		} else if (req instanceof ModificationWatchpointRequestImpl) {
			deleteEventRequest(MODIFICATION_WATCHPOINT_TYPE, (ModificationWatchpointRequestImpl)req);
		} else if (req instanceof StepRequestImpl) {
			deleteEventRequest(STEP_TYPE, (StepRequestImpl)req);
		} else if (req instanceof ThreadDeathRequestImpl) {
			deleteEventRequest(THREAD_DEATH_TYPE, (ThreadDeathRequestImpl)req);
		} else if (req instanceof ThreadStartRequestImpl) {
			deleteEventRequest(THREAD_START_TYPE, (ThreadStartRequestImpl)req);
		} else if(req instanceof MonitorContendedEnterRequestImpl) {
			deleteEventRequest(MONITOR_CONTENDED_ENTER_TYPE, (MonitorContendedEnterRequestImpl)req);
		} else if(req instanceof MonitorContendedEnteredRequestImpl) {
			deleteEventRequest(MONITOR_CONTENDED_ENTERED_TYPE, (MonitorContendedEnteredRequestImpl)req);
		} else if(req instanceof MonitorWaitRequestImpl) {
			deleteEventRequest(MONITOR_WAIT_TYPE, (MonitorWaitRequestImpl)req);
		} else if(req instanceof MonitorWaitedRequestImpl) {
			deleteEventRequest(MONITOR_WAITED_TYPE, (MonitorWaitedRequestImpl)req);
		} else {
			throw new InternalError(NLS.bind(RequestMessages.EventRequestManagerImpl_EventRequest_type_of__0__is_unknown_1, req.toString()));
		}
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.request.EventRequestManager#deleteEventRequests(java.util.List)
	 */
	@Override
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
	@Override
	public List<AccessWatchpointRequest> accessWatchpointRequests() {
		return ACCESS_WATCHPOINT_TYPE.getUnmodifiableList();
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.request.EventRequestManager#breakpointRequests()
	 */
	@Override
	public List<BreakpointRequest> breakpointRequests() {
		return BREAKPOINT_TYPE.getUnmodifiableList();
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.request.EventRequestManager#classPrepareRequests()
	 */
	@Override
	public List<ClassPrepareRequest> classPrepareRequests() {
		return CLASS_PREPARE_TYPE.getUnmodifiableList();
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.request.EventRequestManager#classUnloadRequests()
	 */
	@Override
	public List<ClassUnloadRequest> classUnloadRequests() {
		return CLASS_UNLOAD_TYPE.getUnmodifiableList();
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.request.EventRequestManager#exceptionRequests()
	 */
	@Override
	public List<ExceptionRequest> exceptionRequests() {
		return EXCEPTION_TYPE.getUnmodifiableList();
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.request.EventRequestManager#methodEntryRequests()
	 */
	@Override
	public List<MethodEntryRequest> methodEntryRequests() {
		return METHOD_ENTRY_TYPE.getUnmodifiableList();
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.request.EventRequestManager#methodExitRequests()
	 */
	@Override
	public List<MethodExitRequest> methodExitRequests() {
		return METHOD_EXIT_TYPE.getUnmodifiableList();
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.request.EventRequestManager#modificationWatchpointRequests()
	 */
	@Override
	public List<ModificationWatchpointRequest> modificationWatchpointRequests() {
		return MODIFICATION_WATCHPOINT_TYPE.getUnmodifiableList();
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.request.EventRequestManager#stepRequests()
	 */
	@Override
	public List<StepRequest> stepRequests() {
		return STEP_TYPE.getUnmodifiableList();
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.request.EventRequestManager#threadDeathRequests()
	 */
	@Override
	public List<ThreadDeathRequest> threadDeathRequests() {
		return THREAD_DEATH_TYPE.getUnmodifiableList();
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.request.EventRequestManager#threadStartRequests()
	 */
	@Override
	public List<ThreadStartRequest> threadStartRequests() {
		return THREAD_START_TYPE.getUnmodifiableList();
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.request.EventRequestManager#vmDeathRequests()
	 */
	@Override
	public List<VMDeathRequest> vmDeathRequests() {
		return VM_DEATH_TYPE.getUnmodifiableList();
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.request.EventRequestManager#monitorContendedEnterRequests()
	 */
	@Override
	public List<MonitorContendedEnterRequest> monitorContendedEnterRequests() {
		return MONITOR_CONTENDED_ENTER_TYPE.getUnmodifiableList();
	}

    /* (non-Javadoc)
     * @see com.sun.jdi.request.EventRequestManager#monitorContendedEnteredRequests()
     */
    @Override
	public List<MonitorContendedEnteredRequest> monitorContendedEnteredRequests() {
    	return MONITOR_CONTENDED_ENTERED_TYPE.getUnmodifiableList();
    }

    /* (non-Javadoc)
     * @see com.sun.jdi.request.EventRequestManager#monitorWaitRequests()
     */
    @Override
	public List<MonitorWaitRequest> monitorWaitRequests() {
    	return MONITOR_WAIT_TYPE.getUnmodifiableList();
    }

    /* (non-Javadoc)
     * @see com.sun.jdi.request.EventRequestManager#monitorWaitedRequests()
     */
    @Override
	public List<MonitorWaitedRequest> monitorWaitedRequests() {
    	return MONITOR_WAITED_TYPE.getUnmodifiableList();
    }

	public void removeRequestIDMapping(EventRequestImpl req) {
		if (req instanceof AccessWatchpointRequestImpl) {
			ACCESS_WATCHPOINT_TYPE.enabledrequests.remove(req.requestID());
		} else if (req instanceof BreakpointRequestImpl) {
			BREAKPOINT_TYPE.enabledrequests.remove(req.requestID());
		} else if (req instanceof ClassPrepareRequestImpl) {
			CLASS_PREPARE_TYPE.enabledrequests.remove(req.requestID());
		} else if (req instanceof ClassUnloadRequestImpl) {
			CLASS_UNLOAD_TYPE.enabledrequests.remove(req.requestID());
		} else if (req instanceof ExceptionRequestImpl) {
			EXCEPTION_TYPE.enabledrequests.remove(req.requestID());
		} else if (req instanceof MethodEntryRequestImpl) {
			METHOD_ENTRY_TYPE.enabledrequests.remove(req.requestID());
		} else if (req instanceof MethodExitRequestImpl) {
			METHOD_EXIT_TYPE.enabledrequests.remove(req.requestID());
		} else if (req instanceof ModificationWatchpointRequestImpl) {
			MODIFICATION_WATCHPOINT_TYPE.enabledrequests.remove(req.requestID());
		} else if (req instanceof StepRequestImpl) {
			STEP_TYPE.enabledrequests.remove(req.requestID());
		} else if (req instanceof ThreadDeathRequestImpl) {
			THREAD_DEATH_TYPE.enabledrequests.remove(req.requestID());
		} else if (req instanceof ThreadStartRequestImpl) {
			THREAD_START_TYPE.enabledrequests.remove(req.requestID());
		} else if(req instanceof MonitorContendedEnterRequestImpl) {
			MONITOR_CONTENDED_ENTER_TYPE.enabledrequests.remove(req.requestID());
		} else if(req instanceof MonitorContendedEnteredRequestImpl) {
			MONITOR_CONTENDED_ENTERED_TYPE.enabledrequests.remove(req.requestID());
		} else if(req instanceof MonitorWaitRequestImpl) {
			MONITOR_WAIT_TYPE.enabledrequests.remove(req.requestID());
		} else if(req instanceof MonitorWaitedRequestImpl) {
			MONITOR_WAITED_TYPE.enabledrequests.remove(req.requestID());
		} else if(req instanceof VMDeathRequestImpl) {
			VM_DEATH_TYPE.enabledrequests.remove(req.requestID());
		}
	}

	/**
	 * Maps a request ID to requests.
	 */
	public void addRequestIDMapping(EventRequestImpl req) {
		if (req instanceof AccessWatchpointRequestImpl) {
			ACCESS_WATCHPOINT_TYPE.enabledrequests.put(req.requestID(), (AccessWatchpointRequestImpl) req);
		} else if (req instanceof BreakpointRequestImpl) {
			BREAKPOINT_TYPE.enabledrequests.put(req.requestID(), (BreakpointRequestImpl)req);
		} else if (req instanceof ClassPrepareRequestImpl) {
			CLASS_PREPARE_TYPE.enabledrequests.put(req.requestID(), (ClassPrepareRequestImpl)req);
		} else if (req instanceof ClassUnloadRequestImpl) {
			CLASS_UNLOAD_TYPE.enabledrequests.put(req.requestID(), (ClassUnloadRequestImpl)req);
		} else if (req instanceof ExceptionRequestImpl) {
			EXCEPTION_TYPE.enabledrequests.put(req.requestID(), (ExceptionRequestImpl)req);
		} else if (req instanceof MethodEntryRequestImpl) {
			METHOD_ENTRY_TYPE.enabledrequests.put(req.requestID(), (MethodEntryRequestImpl)req);
		} else if (req instanceof MethodExitRequestImpl) {
			METHOD_EXIT_TYPE.enabledrequests.put(req.requestID(), (MethodExitRequestImpl)req);
		} else if (req instanceof ModificationWatchpointRequestImpl) {
			MODIFICATION_WATCHPOINT_TYPE.enabledrequests.put(req.requestID(), (ModificationWatchpointRequestImpl)req);
		} else if (req instanceof StepRequestImpl) {
			STEP_TYPE.enabledrequests.put(req.requestID(), (StepRequestImpl)req);
		} else if (req instanceof ThreadDeathRequestImpl) {
			THREAD_DEATH_TYPE.enabledrequests.put(req.requestID(), (ThreadDeathRequestImpl)req);
		} else if (req instanceof ThreadStartRequestImpl) {
			THREAD_START_TYPE.enabledrequests.put(req.requestID(), (ThreadStartRequestImpl)req);
		} else if(req instanceof MonitorWaitRequestImpl) {
			MONITOR_WAIT_TYPE.enabledrequests.put(req.requestID(), (MonitorWaitRequestImpl)req);
		} else if(req instanceof MonitorWaitedRequestImpl) {
			MONITOR_WAITED_TYPE.enabledrequests.put(req.requestID(), (MonitorWaitedRequestImpl)req);
		} else if(req instanceof MonitorContendedEnterRequestImpl) {
			MONITOR_CONTENDED_ENTER_TYPE.enabledrequests.put(req.requestID(), (MonitorContendedEnterRequestImpl)req);
		} else if(req instanceof MonitorContendedEnteredRequestImpl) {
			MONITOR_CONTENDED_ENTERED_TYPE.enabledrequests.put(req.requestID(), (MonitorContendedEnteredRequestImpl)req);
		} else if(req instanceof VMDeathRequestImpl) {
			VM_DEATH_TYPE.enabledrequests.put(req.requestID(), (VMDeathRequest) req);
		}
	}

	/**
	 * Find Request that matches event.
	 */
	public EventRequest findRequest(EventImpl event) {
		if (event instanceof AccessWatchpointEventImpl) {
			return ACCESS_WATCHPOINT_TYPE.enabledrequests.get(event.requestID());
		} else if (event instanceof BreakpointEventImpl) {
			return BREAKPOINT_TYPE.enabledrequests.get(event.requestID());
		} else if (event instanceof ClassPrepareEventImpl) {
			return CLASS_PREPARE_TYPE.enabledrequests.get(event.requestID());
		} else if (event instanceof ClassUnloadEventImpl) {
			return CLASS_UNLOAD_TYPE.enabledrequests.get(event.requestID());
		} else if (event instanceof ExceptionEventImpl) {
			return EXCEPTION_TYPE.enabledrequests.get(event.requestID());
		} else if (event instanceof MethodEntryEventImpl) {
			return METHOD_ENTRY_TYPE.enabledrequests.get(event.requestID());
		} else if (event instanceof MethodExitEventImpl) {
			return METHOD_EXIT_TYPE.enabledrequests.get(event.requestID());
		} else if (event instanceof ModificationWatchpointEventImpl) {
			return MODIFICATION_WATCHPOINT_TYPE.enabledrequests.get(event.requestID());
		} else if (event instanceof StepEventImpl) {
			return STEP_TYPE.enabledrequests.get(event.requestID());
		} else if (event instanceof ThreadDeathEventImpl) {
			return THREAD_DEATH_TYPE.enabledrequests.get(event.requestID());
		} else if (event instanceof ThreadStartEventImpl) {
			return THREAD_START_TYPE.enabledrequests.get(event.requestID());
		} else if (event instanceof VMDeathEventImpl) {
			return VM_DEATH_TYPE.enabledrequests.get(event.requestID());
		} else if(event instanceof MonitorWaitEventImpl) {
			return MONITOR_WAIT_TYPE.enabledrequests.get(event.requestID());
		} else if(event instanceof MonitorWaitedEventImpl) {
			return MONITOR_WAITED_TYPE.enabledrequests.get(event.requestID());
		} else if(event instanceof MonitorContendedEnterEventImpl) {
			return MONITOR_CONTENDED_ENTER_TYPE.enabledrequests.get(event.requestID());
		} else if(event instanceof MonitorContendedEnteredEventImpl) {
			return MONITOR_CONTENDED_ENTERED_TYPE.enabledrequests.get(event.requestID());
		}
		throw new InternalError(RequestMessages.EventRequestManagerImpl_Got_event_of_unknown_type_2);
	}
}
