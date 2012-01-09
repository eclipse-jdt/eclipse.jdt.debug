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
import java.util.HashMap;
import java.util.HashSet;
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

import com.ibm.icu.text.MessageFormat;
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
 * this class implements the corresponding interfaces declared by the JDI
 * specification. See the com.sun.jdi package for more information.
 * 
 */
public class EventRequestManagerImpl extends MirrorImpl implements EventRequestManager, org.eclipse.jdi.hcr.EventRequestManager {
	private final HashSet<AccessWatchpointRequest> accesswpReqs = new HashSet<AccessWatchpointRequest>(8);
	private final HashSet<BreakpointRequest> bpReqs = new HashSet<BreakpointRequest>(8);
	private final HashSet<ClassPrepareRequest> cpReqs = new HashSet<ClassPrepareRequest>(8);
	private final HashSet<ClassUnloadRequest> cuReqs = new HashSet<ClassUnloadRequest>(8);
	private final HashSet<ExceptionRequest> exReqs = new HashSet<ExceptionRequest>(8);
	private final HashSet<MethodEntryRequest> menReqs = new HashSet<MethodEntryRequest>(8);
	private final HashSet<MethodExitRequest> mexReqs = new HashSet<MethodExitRequest>(8);
	private final HashSet<ModificationWatchpointRequest> mwpReqs = new HashSet<ModificationWatchpointRequest>(8);
	private final HashSet<StepRequest> stepReqs = new HashSet<StepRequest>(8);
	private final HashSet<ThreadDeathRequest> tdReqs = new HashSet<ThreadDeathRequest>(8);
	private final HashSet<ThreadStartRequest> tsReqs = new HashSet<ThreadStartRequest>(8);
	private final HashSet<VMDeathRequest> vmdReqs = new HashSet<VMDeathRequest>(8);
	private final HashSet<MonitorContendedEnteredRequest> mcenteredReqs = new HashSet<MonitorContendedEnteredRequest>(8);
	private final HashSet<MonitorContendedEnterRequest> mcenterReqs = new HashSet<MonitorContendedEnterRequest>(8);
	private final HashSet<MonitorWaitedRequest> mwaitedReqs = new HashSet<MonitorWaitedRequest>(8);
	private final HashSet<MonitorWaitRequest> mwaitReqs = new HashSet<MonitorWaitRequest>(8);

	HashMap<Class<?>, HashSet<? extends EventRequest>> requests = null;
	HashMap<Class<?>, HashMap<RequestID, EventRequest>> enabled = null;
	
	/**
	 * Creates new EventRequestManager.
	 */
	public EventRequestManagerImpl(VirtualMachineImpl vmImpl) {
		super("EventRequestManager", vmImpl); //$NON-NLS-1$
		requests = new HashMap<Class<?>, HashSet<? extends EventRequest>>();
		requests.put(AccessWatchpointRequestImpl.class, accesswpReqs);
		requests.put(BreakpointRequestImpl.class, bpReqs);
		requests.put(ClassPrepareRequestImpl.class, cpReqs);
		requests.put(ClassUnloadRequestImpl.class, cuReqs);
		requests.put(ExceptionRequestImpl.class, exReqs);
		requests.put(MethodEntryRequestImpl.class, menReqs);
		requests.put(MethodExitRequestImpl.class, mexReqs);
		requests.put(ModificationWatchpointRequestImpl.class, mwpReqs);
		requests.put(StepRequestImpl.class, stepReqs);
		requests.put(ThreadDeathRequestImpl.class, tdReqs);
		requests.put(ThreadStartRequestImpl.class, tsReqs);
		requests.put(VMDeathRequestImpl.class, vmdReqs);
		requests.put(MonitorContendedEnteredRequestImpl.class, mcenteredReqs);
		requests.put(MonitorContendedEnterRequestImpl.class, mcenterReqs);
		requests.put(MonitorWaitedRequestImpl.class, mwaitedReqs);
		requests.put(MonitorWaitRequestImpl.class, mwaitReqs);
		enabled = new HashMap<Class<?>, HashMap<RequestID,EventRequest>>();
	}

	/**
	 * Creates AccessWatchpointRequest.
	 */
	public AccessWatchpointRequest createAccessWatchpointRequest(Field field) {
		FieldImpl fieldImpl = (FieldImpl) field;
		AccessWatchpointRequestImpl req = new AccessWatchpointRequestImpl(virtualMachineImpl());
		req.addFieldFilter(fieldImpl);
		accesswpReqs.add(req);
		return req;
	}

	/**
	 * Creates BreakpointRequest.
	 */
	public BreakpointRequest createBreakpointRequest(Location location) throws VMMismatchException {
		LocationImpl locImpl = (LocationImpl) location;
		BreakpointRequestImpl req = new BreakpointRequestImpl(virtualMachineImpl());
		req.addLocationFilter(locImpl);
		bpReqs.add(req);
		return req;
	}

	/**
	 * Creates ClassPrepareRequest.
	 */
	public ClassPrepareRequest createClassPrepareRequest() {
		ClassPrepareRequestImpl req = new ClassPrepareRequestImpl(virtualMachineImpl());
		cpReqs.add(req);
		return req;
	}

	/**
	 * Creates ClassUnloadRequest.
	 */
	public ClassUnloadRequest createClassUnloadRequest() {
		ClassUnloadRequestImpl req = new ClassUnloadRequestImpl(virtualMachineImpl());
		cuReqs.add(req);
		return req;
	}

	/**
	 * Creates ExceptionRequest.
	 */
	public ExceptionRequest createExceptionRequest(ReferenceType refType, boolean notifyCaught, boolean notifyUncaught) {
		ReferenceTypeImpl refTypeImpl = (ReferenceTypeImpl) refType;
		ExceptionRequestImpl req = new ExceptionRequestImpl(virtualMachineImpl());
		req.addExceptionFilter(refTypeImpl, notifyCaught, notifyUncaught);
		exReqs.add(req);
		return req;
	}

	/**
	 * Creates MethodEntryRequest.
	 */
	public MethodEntryRequest createMethodEntryRequest() {
		MethodEntryRequestImpl req = new MethodEntryRequestImpl(virtualMachineImpl());
		menReqs.add(req);
		return req;
	}

	/**
	 * Creates MethodExitRequest.
	 */
	public MethodExitRequest createMethodExitRequest() {
		MethodExitRequestImpl req = new MethodExitRequestImpl(virtualMachineImpl());
		mexReqs.add(req);
		return req;
	}

	/**
	 * Creates a MonitorContendedEnteredRequest
	 * 
	 * @since 3.3
	 */
	public MonitorContendedEnteredRequest createMonitorContendedEnteredRequest() {
		MonitorContendedEnteredRequestImpl req = new MonitorContendedEnteredRequestImpl(virtualMachineImpl());
		mcenteredReqs.add(req);
		return req;
	}

	/**
	 * Creates a MonitorContendedEnterRequest
	 * 
	 * @since 3.3
	 */
	public MonitorContendedEnterRequest createMonitorContendedEnterRequest() {
		MonitorContendedEnterRequestImpl req = new MonitorContendedEnterRequestImpl(virtualMachineImpl());
		mcenterReqs.add(req);
		return req;
	}

	/**
	 * Creates a MonitorWaitedRequest
	 * 
	 * @since 3.3
	 */
	public MonitorWaitedRequest createMonitorWaitedRequest() {
		MonitorWaitedRequestImpl req = new MonitorWaitedRequestImpl(virtualMachineImpl());
		mwaitedReqs.add(req);
		return req;
	}

	/**
	 * Creates a MonitorWaitRequest
	 * 
	 * @since 3.3
	 */
	public MonitorWaitRequest createMonitorWaitRequest() {
		MonitorWaitRequestImpl req = new MonitorWaitRequestImpl(virtualMachineImpl());
		mwaitReqs.add(req);
		return req;
	}

	/**
	 * Creates ModificationWatchpointRequest.
	 */
	public ModificationWatchpointRequest createModificationWatchpointRequest(Field field) {
		FieldImpl fieldImpl = (FieldImpl) field;
		ModificationWatchpointRequestImpl req = new ModificationWatchpointRequestImpl(virtualMachineImpl());
		req.addFieldFilter(fieldImpl);
		mwpReqs.add(req);
		return req;
	}

	/**
	 * Creates StepRequest.
	 */
	public StepRequest createStepRequest(ThreadReference thread, int size, int depth) throws DuplicateRequestException,	ObjectCollectedException {
		ThreadReferenceImpl threadImpl = (ThreadReferenceImpl) thread;
		StepRequestImpl req = new StepRequestImpl(virtualMachineImpl());
		req.addStepFilter(threadImpl, size, depth);
		stepReqs.add(req);
		return req;
	}

	/**
	 * Creates ThreadDeathRequest.
	 */
	public ThreadDeathRequest createThreadDeathRequest() {
		ThreadDeathRequestImpl req = new ThreadDeathRequestImpl(virtualMachineImpl());
		tdReqs.add(req);
		return req;
	}

	/**
	 * Creates ThreadStartRequest.
	 */
	public ThreadStartRequest createThreadStartRequest() {
		ThreadStartRequestImpl req = new ThreadStartRequestImpl(virtualMachineImpl());
		tsReqs.add(req);
		return req;
	}

	/*
	 * @see EventRequestManager#createVMDeathRequest()
	 */
	public VMDeathRequest createVMDeathRequest() {
		VMDeathRequestImpl req = new VMDeathRequestImpl(virtualMachineImpl());
		vmdReqs.add(req);
		return req;
	}

	/**
	 * Creates ReenterStepRequest (for OTI specific Hot Code Replacement).
	 */
	public org.eclipse.jdi.hcr.ReenterStepRequest createReenterStepRequest(ThreadReference thread) {
		virtualMachineImpl().checkHCRSupported();
		ThreadReferenceImpl threadImpl = (ThreadReferenceImpl) thread;
		ReenterStepRequestImpl req = new ReenterStepRequestImpl(virtualMachineImpl());
		// Note that the StepFilter is only used to specify the thread.
		// The size is ignored and the depth will always be writter as
		// HCR_STEP_DEPTH_REENTER_JDWP.
		req.addStepFilter(threadImpl, StepRequest.STEP_MIN, 0);
		// Since this is a special case of a step request, we use the same
		// request list.
		stepReqs.add(req);
		return req;
	}

	/**
	 * Enables class prepare requests for all loaded classes. This is necessary
	 * for current versions of the KVM to function correctly. This method is
	 * only called when the remote VM is determined to be the KVM.
	 */
	public void enableInternalClassPrepareEvent() {
		// Note that these requests are not stored in the set of outstanding
		// requests because
		// they must be invisible from outside.
		ClassPrepareRequestImpl requestPrepare = new ClassPrepareRequestImpl(virtualMachineImpl());
		requestPrepare.setGeneratedInside();
		requestPrepare.setSuspendPolicy(EventRequest.SUSPEND_NONE);

		requestPrepare.enable();
	}

	/**
	 * Creates ClassUnloadRequest for maintaining class information for within
	 * JDI. Needed to known when to flush the cache.
	 */
	public void enableInternalClasUnloadEvent(/* tbd: ReferenceTypeImpl refType */) {
		// Note that these requests are not stored in the set of outstanding
		// requests because
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
		Collection<EventRequest> reqs = enabled.get(StepRequestImpl.class).values();
		StepRequest step;
		for (EventRequest req : reqs) {
			step = (StepRequest) req;
			if (step.thread() == threadImpl) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Deletes all Breakpoints.
	 */
	public void deleteAllBreakpoints() {
		EventRequestImpl.clearAllBreakpoints(this);
		bpReqs.clear();
		enabled.get(BreakpointRequestImpl.class).clear();
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.request.EventRequestManager#deleteEventRequest(com.sun.jdi.request.EventRequest)
	 */
	public void deleteEventRequest(EventRequest req) {
		// Disable request, note that this also causes the event request to be
		// removed from fEnabledRequests.
		try {
			req.disable();
		} catch (InvalidRequestStateException exception) {
			// The event has already been removed from the VM.
		}
		EventRequestImpl reqimpl = (EventRequestImpl) req;
		boolean removed = requests.get(req.getClass()).remove(req);
		removed |= (enabled.get(req.getClass()).remove(reqimpl.requestID()) != null);
		if(!removed) {
			throw new InternalError(
					MessageFormat
							.format(RequestMessages.EventRequestManagerImpl_EventRequest_type_of__0__is_unknown_1,
									new Object[] { req.toString() }));
		}
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.request.EventRequestManager#deleteEventRequests(java.util.List)
	 */
	public void deleteEventRequests(List<? extends EventRequest> reqs) throws VMMismatchException {
		Iterator<? extends EventRequest> iter = reqs.iterator();
		while (iter.hasNext()) {
			Object obj = iter.next();
			deleteEventRequest((EventRequest) obj);
		}
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.request.EventRequestManager#accessWatchpointRequests()
	 */
	public List<AccessWatchpointRequest> accessWatchpointRequests() {
		return new ArrayList<AccessWatchpointRequest>(accesswpReqs);
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.request.EventRequestManager#breakpointRequests()
	 */
	public List<BreakpointRequest> breakpointRequests() {
		return new ArrayList<BreakpointRequest>(bpReqs);
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.request.EventRequestManager#classPrepareRequests()
	 */
	public List<ClassPrepareRequest> classPrepareRequests() {
		return new ArrayList<ClassPrepareRequest>(cpReqs);
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.request.EventRequestManager#classUnloadRequests()
	 */
	public List<ClassUnloadRequest> classUnloadRequests() {
		return new ArrayList<ClassUnloadRequest>(cuReqs);
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.request.EventRequestManager#exceptionRequests()
	 */
	public List<ExceptionRequest> exceptionRequests() {
		return new ArrayList<ExceptionRequest>(exReqs);
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.request.EventRequestManager#methodEntryRequests()
	 */
	public List<MethodEntryRequest> methodEntryRequests() {
		return new ArrayList<MethodEntryRequest>(menReqs);
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.request.EventRequestManager#methodExitRequests()
	 */
	public List<MethodExitRequest> methodExitRequests() {
		return new ArrayList<MethodExitRequest>(mexReqs);
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.request.EventRequestManager#modificationWatchpointRequests()
	 */
	public List<ModificationWatchpointRequest> modificationWatchpointRequests() {
		return new ArrayList<ModificationWatchpointRequest>(mwpReqs);
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.request.EventRequestManager#stepRequests()
	 */
	public List<StepRequest> stepRequests() {
		return new ArrayList<StepRequest>(stepReqs);
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.request.EventRequestManager#threadDeathRequests()
	 */
	public List<ThreadDeathRequest> threadDeathRequests() {
		return new ArrayList<ThreadDeathRequest>(tdReqs);
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.request.EventRequestManager#threadStartRequests()
	 */
	public List<ThreadStartRequest> threadStartRequests() {
		return new ArrayList<ThreadStartRequest>(tsReqs);
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.request.EventRequestManager#vmDeathRequests()
	 */
	public List<VMDeathRequest> vmDeathRequests() {
		return new ArrayList<VMDeathRequest>(vmdReqs);
	}

	/**
	 * @param req
	 */
	public void removeRequestIDMapping(EventRequestImpl req) {
		enabled.get(req.getClass()).remove(req.requestID());
	}

	/**
	 * Maps a request ID to requests.
	 */
	public void addRequestIDMapping(EventRequestImpl req) {
		enabled.get(req.getClass()).put(req.requestID(), req);
	}

	/**
	 * Find Request that matches event.
	 */
	public EventRequestImpl findRequest(EventImpl event) {
		if (event instanceof AccessWatchpointEventImpl) {
			return (EventRequestImpl) enabled.get(AccessWatchpointRequestImpl.class).get(event.requestID());
		}
		else if (event instanceof BreakpointEventImpl) {
			return (EventRequestImpl) enabled.get(BreakpointRequestImpl.class).get(event.requestID());
		}
		else if (event instanceof ClassPrepareEventImpl) {
			return (EventRequestImpl) enabled.get(ClassPrepareRequestImpl.class).get(event.requestID());
		}
		else if (event instanceof ClassUnloadEventImpl) {
			return (EventRequestImpl) enabled.get(ClassUnloadRequestImpl.class).get(event.requestID());
		}
		else if (event instanceof ExceptionEventImpl) {
			return (EventRequestImpl) enabled.get(ExceptionRequestImpl.class).get(event.requestID());
		}
		else if (event instanceof MethodEntryEventImpl) {
			return (EventRequestImpl) enabled.get(MethodEntryRequestImpl.class).get(event.requestID());
		}
		else if (event instanceof MethodExitEventImpl) {
			return (EventRequestImpl) enabled.get(MethodExitRequestImpl.class).get(event.requestID());
		}
		else if (event instanceof ModificationWatchpointEventImpl) {
			return (EventRequestImpl) enabled.get(ModificationWatchpointRequestImpl.class).get(event.requestID());
		}
		else if (event instanceof StepEventImpl) {
			return (EventRequestImpl) enabled.get(StepRequestImpl.class).get(event.requestID());
		}
		else if (event instanceof ThreadDeathEventImpl) {
			return (EventRequestImpl) enabled.get(ThreadDeathRequestImpl.class).get(event.requestID());
		}
		else if (event instanceof ThreadStartEventImpl) {
			return (EventRequestImpl) enabled.get(ThreadStartRequestImpl.class).get(event.requestID());
		}
		else if (event instanceof VMDeathEventImpl) {
			return (EventRequestImpl) enabled.get(VMDeathRequestImpl.class).get(event.requestID());
		}
		else if (event instanceof MonitorWaitEventImpl) {
			return (EventRequestImpl) enabled.get(MonitorWaitRequestImpl.class).get(event.requestID());
		} else if (event instanceof MonitorWaitedEventImpl) {
			return (EventRequestImpl) enabled.get(MonitorWaitedRequestImpl.class).get(event.requestID());
		} else if (event instanceof MonitorContendedEnterEventImpl) {
			return (EventRequestImpl) enabled.get(MonitorContendedEnterRequestImpl.class).get(event.requestID());
		} else if (event instanceof MonitorContendedEnteredEventImpl) {
			return (EventRequestImpl) enabled.get(MonitorContendedEnteredRequestImpl.class).get(event.requestID());
		} else
			throw new InternalError(
					RequestMessages.EventRequestManagerImpl_Got_event_of_unknown_type_2);
	}

	/**
	 * @see com.sun.jdi.request.EventRequestManager#monitorContendedEnterRequests()
	 * @since 3.3
	 */
	public List<MonitorContendedEnterRequest> monitorContendedEnterRequests() {
		return new ArrayList<MonitorContendedEnterRequest>(mcenterReqs);
	}

	/**
	 * @see com.sun.jdi.request.EventRequestManager#monitorContendedEnteredRequests()
	 * @since 3.3
	 */
	public List<MonitorContendedEnteredRequest> monitorContendedEnteredRequests() {
		return new ArrayList<MonitorContendedEnteredRequest>(mcenteredReqs);
	}

	/**
	 * @see com.sun.jdi.request.EventRequestManager#monitorWaitRequests()
	 * @since 3.3
	 */
	public List<MonitorWaitRequest> monitorWaitRequests() {
		return new ArrayList<MonitorWaitRequest>(mwaitReqs);
	}

	/**
	 * @see com.sun.jdi.request.EventRequestManager#monitorWaitedRequests()
	 * @since 3.3
	 */
	public List<MonitorWaitedRequest> monitorWaitedRequests() {
		return new ArrayList<MonitorWaitedRequest>(mwaitedReqs);
	}
}
