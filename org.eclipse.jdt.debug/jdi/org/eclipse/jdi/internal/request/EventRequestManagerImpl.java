/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
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
 * this class implements the corresponding interfaces
 * declared by the JDI specification. See the com.sun.jdi package
 * for more information.
 *
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
	private HashSet[] fRequests;

	/** Maps per request type of requestIDs to enabled requests. */
	private Hashtable[] fEnabledRequests;
	
	/**
	 * Creates new EventRequestManager.
	 */
	public EventRequestManagerImpl(VirtualMachineImpl vmImpl) {
		super("EventRequestManager", vmImpl); //$NON-NLS-1$
		
		// Initialize list of requests.
		fRequests = new HashSet[MONITOR_WAIT_INDEX + 1];
		for (int i = 0; i < fRequests.length; i++)
			fRequests[i] = new HashSet();
			
		// Initialize map of request IDs to enabled requests.
		fEnabledRequests = new Hashtable[MONITOR_WAIT_INDEX + 1];
		for (int i = 0; i < fEnabledRequests.length; i++)
			fEnabledRequests[i] = new Hashtable();
	}

	/**
	 * Creates AccessWatchpointRequest.
	 */ 
	public AccessWatchpointRequest createAccessWatchpointRequest(Field field) {
		FieldImpl fieldImpl = (FieldImpl)field;
		AccessWatchpointRequestImpl req = new AccessWatchpointRequestImpl(virtualMachineImpl());
		req.addFieldFilter(fieldImpl);
		addEventRequest(ACCESS_WATCHPOINT_INDEX, req);
		return req;
	}

	/**
	 * Creates BreakpointRequest.
	 */ 
	public BreakpointRequest createBreakpointRequest(Location location) throws VMMismatchException {
		LocationImpl locImpl = (LocationImpl)location;
		BreakpointRequestImpl req = new BreakpointRequestImpl(virtualMachineImpl());
		req.addLocationFilter(locImpl);
		addEventRequest(BREAKPOINT_INDEX, req);
		return req;
	}

	/**
	 * Creates ClassPrepareRequest.
	 */ 
	public ClassPrepareRequest createClassPrepareRequest() {
		ClassPrepareRequestImpl req = new ClassPrepareRequestImpl(virtualMachineImpl());
		addEventRequest(CLASS_PREPARE_INDEX, req);
		return req;
	} 
	
	/**
	 * Creates ClassUnloadRequest.
	 */ 
	public ClassUnloadRequest createClassUnloadRequest() {
		ClassUnloadRequestImpl req = new ClassUnloadRequestImpl(virtualMachineImpl());
		addEventRequest(CLASS_UNLOAD_INDEX, req);
		return req;
	} 

	/**
	 * Creates ExceptionRequest.
	 */ 	 
	public ExceptionRequest createExceptionRequest(ReferenceType refType, boolean notifyCaught, boolean notifyUncaught) {
		ReferenceTypeImpl refTypeImpl = (ReferenceTypeImpl)refType;
		ExceptionRequestImpl req = new ExceptionRequestImpl(virtualMachineImpl());
		req.addExceptionFilter(refTypeImpl, notifyCaught, notifyUncaught);
		addEventRequest(EXCEPTION_INDEX, req);
		return req;
	} 

	/**
	 * Creates MethodEntryRequest.
	 */ 
	public MethodEntryRequest createMethodEntryRequest() {
		MethodEntryRequestImpl req = new MethodEntryRequestImpl(virtualMachineImpl());
		addEventRequest(METHOD_ENTRY_INDEX, req);
		return req;
	} 

	/**
	 * Creates MethodExitRequest.
	 */
	public MethodExitRequest createMethodExitRequest() {
		MethodExitRequestImpl req = new MethodExitRequestImpl(virtualMachineImpl());
		addEventRequest(METHOD_EXIT_INDEX, req);
		return req;
	} 
	
	/**
	 * Creates a MonitorContendedEnteredRequest
	 * @since 3.3
	 */
	public MonitorContendedEnteredRequest createMonitorContendedEnteredRequest() {
		MonitorContendedEnteredRequestImpl req = new MonitorContendedEnteredRequestImpl(virtualMachineImpl());
		addEventRequest(MONITOR_CONTENDED_ENTERED_INDEX, req);
		return req;
	}
	
	/**
	 * Creates a MonitorContendedEnterRequest
	 * @since 3.3
	 */
	public MonitorContendedEnterRequest createMonitorContendedEnterRequest() {
		MonitorContendedEnterRequestImpl req = new MonitorContendedEnterRequestImpl(virtualMachineImpl());
		addEventRequest(MONITOR_CONTENDED_ENTER_INDEX, req);
		return req;
	}
	
	/**
	 * Creates a MonitorWaitedRequest
	 * @since 3.3
	 */
	public MonitorWaitedRequest createMonitorWaitedRequest() {
		MonitorWaitedRequestImpl req = new MonitorWaitedRequestImpl(virtualMachineImpl());
		addEventRequest(MONITOR_WAITED_INDEX, req);
		return req;
	}
	
	/**
	 * Creates a MonitorWaitRequest
	 * @since 3.3
	 */
	public MonitorWaitRequest createMonitorWaitRequest() {
		MonitorWaitRequestImpl req = new MonitorWaitRequestImpl(virtualMachineImpl());
		addEventRequest(MONITOR_WAIT_INDEX, req);
		return req;
	}
	
	/**
	 * Creates ModificationWatchpointRequest.
	 */ 
	public ModificationWatchpointRequest createModificationWatchpointRequest(Field field) {
		FieldImpl fieldImpl = (FieldImpl)field;
		ModificationWatchpointRequestImpl req = new ModificationWatchpointRequestImpl(virtualMachineImpl());
		req.addFieldFilter(fieldImpl);
		addEventRequest(MODIFICATION_WATCHPOINT_INDEX, req);
		return req;
	} 
	
	/**
	 * Creates StepRequest.
	 */ 
	public StepRequest createStepRequest(ThreadReference thread, int size, int depth) throws DuplicateRequestException, ObjectCollectedException {
	   	ThreadReferenceImpl threadImpl = (ThreadReferenceImpl)thread;
		StepRequestImpl req = new StepRequestImpl(virtualMachineImpl());		
		req.addStepFilter(threadImpl, size, depth);
		addEventRequest(STEP_INDEX, req);
		return req;
	} 

	/**
	 * Creates ThreadDeathRequest.
	 */ 
	public ThreadDeathRequest createThreadDeathRequest() {
		ThreadDeathRequestImpl req = new ThreadDeathRequestImpl(virtualMachineImpl());
		addEventRequest(THREAD_DEATH_INDEX, req);
		return req;
	} 

	/**
	 * Creates ThreadStartRequest.
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

	/**
	 * Creates ReenterStepRequest (for OTI specific Hot Code Replacement).
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
	public void enableInternalClasUnloadEvent(/* tbd: ReferenceTypeImpl refType*/) {
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
		Enumeration enumeration = fEnabledRequests[STEP_INDEX].elements();
		StepRequestImpl step;
		while (enumeration.hasMoreElements()) {
			step = (StepRequestImpl)enumeration.nextElement();
			if (step.thread() == threadImpl)
				return true;
		}
		return false;
	}

	/**
	 * Deletes all Breakpoints.
	 */ 
	public void deleteAllBreakpoints() {
		EventRequestImpl.clearAllBreakpoints(this);
		fRequests[BREAKPOINT_INDEX].clear();
		fEnabledRequests[BREAKPOINT_INDEX].clear();
	}

	/**
	 * Adds an EventRequests to the given list.
	 */ 
	public void addEventRequest(int index, EventRequest req) {
		fRequests[index].add(req);
	}

	/**
	 * Deletes an EventRequest.
	 */ 
	private void deleteEventRequest(int index, EventRequest req) throws VMMismatchException {
		// Remove request from list of requests and from the mapping of requestIDs to requests.
		checkVM(req);
		EventRequestImpl requestImpl = (EventRequestImpl)req;
		fRequests[index].remove(requestImpl);
		if (requestImpl.requestID() != null)
			fEnabledRequests[index].remove(requestImpl.requestID());
	}

	/**
	 * Deletes an EventRequest.
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
		
		throw new InternalError(MessageFormat.format(RequestMessages.EventRequestManagerImpl_EventRequest_type_of__0__is_unknown_1, new String[]{req.toString()})); 
	}

	/**
	 * Deletes all EventRequests from the given list.
	 */ 
	public void deleteEventRequests(List requests) throws VMMismatchException {
		Iterator iter = requests.iterator();
		while(iter.hasNext()) {
			Object obj = iter.next();
			deleteEventRequest((EventRequest)obj);
		}
	}

	/**
	 * @return Returns list of AccessWatchpointRequests.
	 * For changes, the appropriate EventRequestManager methods should be used.
	 */
	public List accessWatchpointRequests() {
		return new ArrayList(fRequests[ACCESS_WATCHPOINT_INDEX]);
	}

	/**
	 * @return Returns list of BreakpointRequests.
	 * For changes, the appropriate EventRequestManager methods should be used.
	 */
	public List breakpointRequests() {
		return new ArrayList(fRequests[BREAKPOINT_INDEX]);
	}

	/**
	 * @return Returns list of ClassPrepareRequests.
	 * For changes, the appropriate EventRequestManager methods should be used.
	 */
	public List classPrepareRequests() {
		return new ArrayList(fRequests[CLASS_PREPARE_INDEX]);
	}

	/**
	 * @return Returns list of ClassUnloadRequests.
	 * For changes, the appropriate EventRequestManager methods should be used.
	 */
	public List classUnloadRequests() {
		return new ArrayList(fRequests[CLASS_UNLOAD_INDEX]);
	}

	/**
	 * @return Returns list of ExceptionRequests.
	 * For changes, the appropriate EventRequestManager methods should be used.
	 */
	public List exceptionRequests() {
		return new ArrayList(fRequests[EXCEPTION_INDEX]);
	}

	/**
	 * @return Returns list of MethodEntryRequests.
	 * For changes, the appropriate EventRequestManager methods should be used.
	 */
	public List methodEntryRequests() {
		return new ArrayList(fRequests[METHOD_ENTRY_INDEX]);
	}

	/**
	 * @return Returns list of MethodExitRequests.
	 * For changes, the appropriate EventRequestManager methods should be used.
	 */
	public List methodExitRequests() {
		return new ArrayList(fRequests[METHOD_EXIT_INDEX]);
	}
	
	/**
	 * @return Returns list of ModificationWatchpointRequests.
	 * For changes, the appropriate EventRequestManager methods should be used.
	 */
	public List modificationWatchpointRequests() {
		return new ArrayList(fRequests[MODIFICATION_WATCHPOINT_INDEX]);
	}

	/**
	 * @return Returns list of StepRequests.
	 * For changes, the appropriate EventRequestManager methods should be used.
	 */
	public List stepRequests() {
		return new ArrayList(fRequests[STEP_INDEX]);
	}

	/**
	 * @return Returns list of ThreadDeathRequests.
	 * For changes, the appropriate EventRequestManager methods should be used.
	 */
	public List threadDeathRequests() {
		return new ArrayList(fRequests[THREAD_DEATH_INDEX]);
	}

	/**
	 * @return Returns list of ThreadStartRequests.
	 * For changes, the appropriate EventRequestManager methods should be used.
	 */
	public List threadStartRequests() {
		return new ArrayList(fRequests[THREAD_START_INDEX]);
	}
	
	/**
	 * @return Returns list of VMDeathRequests.
	 * For changes, the appropriate EventRequestManager methods should be used.
	 */
	public List vmDeathRequests() {
		return new ArrayList(fRequests[VM_DEATH_INDEX]);
	}

	public void removeRequestIDMapping(EventRequestImpl req) {
		if (req instanceof AccessWatchpointRequestImpl)
			fEnabledRequests[ACCESS_WATCHPOINT_INDEX].remove(req.requestID());
		else if (req instanceof BreakpointRequestImpl)
			fEnabledRequests[BREAKPOINT_INDEX].remove(req.requestID());
		else if (req instanceof ClassPrepareRequestImpl)
			fEnabledRequests[CLASS_PREPARE_INDEX].remove(req.requestID());
		else if (req instanceof ClassUnloadRequestImpl)
			fEnabledRequests[CLASS_UNLOAD_INDEX].remove(req.requestID());
		else if (req instanceof ExceptionRequestImpl)
			fEnabledRequests[EXCEPTION_INDEX].remove(req.requestID());
		else if (req instanceof MethodEntryRequestImpl)
			fEnabledRequests[METHOD_ENTRY_INDEX].remove(req.requestID());
		else if (req instanceof MethodExitRequestImpl)
			fEnabledRequests[METHOD_EXIT_INDEX].remove(req.requestID());
		else if (req instanceof ModificationWatchpointRequestImpl)
			fEnabledRequests[MODIFICATION_WATCHPOINT_INDEX].remove(req.requestID());
		else if (req instanceof StepRequestImpl)
			fEnabledRequests[STEP_INDEX].remove(req.requestID());
		else if (req instanceof ThreadDeathRequestImpl)
			fEnabledRequests[THREAD_DEATH_INDEX].remove(req.requestID());
		else if (req instanceof ThreadStartRequestImpl)
			fEnabledRequests[THREAD_START_INDEX].remove(req.requestID());
		else if(req instanceof MonitorContendedEnterRequestImpl) {
			fEnabledRequests[MONITOR_CONTENDED_ENTER_INDEX].remove(req.requestID());
		}
		else if(req instanceof MonitorContendedEnteredRequestImpl) {
			fEnabledRequests[MONITOR_CONTENDED_ENTERED_INDEX].remove(req.requestID());
		}
		else if(req instanceof MonitorWaitRequestImpl) {
			fEnabledRequests[MONITOR_WAIT_INDEX].remove(req.requestID());
		}
		else if(req instanceof MonitorWaitedRequestImpl) {
			fEnabledRequests[MONITOR_WAITED_INDEX].remove(req.requestID());
		}
	}
	
	/**
	 * Maps a request ID to requests.
	 */ 
	public void addRequestIDMapping(EventRequestImpl req) {
		if (req instanceof AccessWatchpointRequestImpl)
			fEnabledRequests[ACCESS_WATCHPOINT_INDEX].put(req.requestID(), req);
		else if (req instanceof BreakpointRequestImpl)
			fEnabledRequests[BREAKPOINT_INDEX].put(req.requestID(), req);
		else if (req instanceof ClassPrepareRequestImpl)
			fEnabledRequests[CLASS_PREPARE_INDEX].put(req.requestID(), req);
		else if (req instanceof ClassUnloadRequestImpl)
			fEnabledRequests[CLASS_UNLOAD_INDEX].put(req.requestID(), req);
		else if (req instanceof ExceptionRequestImpl)
			fEnabledRequests[EXCEPTION_INDEX].put(req.requestID(), req);
		else if (req instanceof MethodEntryRequestImpl)
			fEnabledRequests[METHOD_ENTRY_INDEX].put(req.requestID(), req);
		else if (req instanceof MethodExitRequestImpl)
			fEnabledRequests[METHOD_EXIT_INDEX].put(req.requestID(), req);
		else if (req instanceof ModificationWatchpointRequestImpl)
			fEnabledRequests[MODIFICATION_WATCHPOINT_INDEX].put(req.requestID(), req);
		else if (req instanceof StepRequestImpl)
			fEnabledRequests[STEP_INDEX].put(req.requestID(), req);
		else if (req instanceof ThreadDeathRequestImpl)
			fEnabledRequests[THREAD_DEATH_INDEX].put(req.requestID(), req);
		else if (req instanceof ThreadStartRequestImpl)
			fEnabledRequests[THREAD_START_INDEX].put(req.requestID(), req);
		else if(req instanceof MonitorWaitRequestImpl) {
			fEnabledRequests[MONITOR_WAIT_INDEX].put(req.requestID(), req);
		}
		else if(req instanceof MonitorWaitedRequestImpl) {
			fEnabledRequests[MONITOR_WAITED_INDEX].put(req.requestID(), req);
		}
		else if(req instanceof MonitorContendedEnterRequestImpl) {
			fEnabledRequests[MONITOR_CONTENDED_ENTER_INDEX].put(req.requestID(), req);
		}
		else if(req instanceof MonitorContendedEnteredRequestImpl) {
			fEnabledRequests[MONITOR_CONTENDED_ENTERED_INDEX].put(req.requestID(), req);
		}
	}

	/**
	 * Find Request that matches event.
	 */ 
	public EventRequestImpl findRequest(EventImpl event) {
		if (event instanceof AccessWatchpointEventImpl)
			return (EventRequestImpl)fEnabledRequests[ACCESS_WATCHPOINT_INDEX].get(event.requestID());
		else if (event instanceof BreakpointEventImpl)
			return (EventRequestImpl)fEnabledRequests[BREAKPOINT_INDEX].get(event.requestID());
		else if (event instanceof ClassPrepareEventImpl)
			return (ClassPrepareRequestImpl)fEnabledRequests[CLASS_PREPARE_INDEX].get(event.requestID());
		else if (event instanceof ClassUnloadEventImpl)
			return (EventRequestImpl)fEnabledRequests[CLASS_UNLOAD_INDEX].get(event.requestID());
		else if (event instanceof ExceptionEventImpl)
			return (EventRequestImpl)fEnabledRequests[EXCEPTION_INDEX].get(event.requestID());
		else if (event instanceof MethodEntryEventImpl)
			return (EventRequestImpl)fEnabledRequests[METHOD_ENTRY_INDEX].get(event.requestID());
		else if (event instanceof MethodExitEventImpl)
			return (EventRequestImpl)fEnabledRequests[METHOD_EXIT_INDEX].get(event.requestID());
		else if (event instanceof ModificationWatchpointEventImpl)
			return (EventRequestImpl)fEnabledRequests[MODIFICATION_WATCHPOINT_INDEX].get(event.requestID());
		else if (event instanceof StepEventImpl)
			return (EventRequestImpl)fEnabledRequests[STEP_INDEX].get(event.requestID());
		else if (event instanceof ThreadDeathEventImpl)
			return (EventRequestImpl)fEnabledRequests[THREAD_DEATH_INDEX].get(event.requestID());
		else if (event instanceof ThreadStartEventImpl)
			return (EventRequestImpl)fEnabledRequests[THREAD_START_INDEX].get(event.requestID());
		else if (event instanceof VMDeathEventImpl)
			return (EventRequestImpl)fEnabledRequests[VM_DEATH_INDEX].get(event.requestID());
		else if(event instanceof MonitorWaitEventImpl) {
			return (EventRequestImpl)fEnabledRequests[MONITOR_WAIT_INDEX].get(event.requestID());
		}
		else if(event instanceof MonitorWaitedEventImpl) {
			return (EventRequestImpl)fEnabledRequests[MONITOR_WAITED_INDEX].get(event.requestID());
		}
		else if(event instanceof MonitorContendedEnterEventImpl) {
			return (EventRequestImpl)fEnabledRequests[MONITOR_CONTENDED_ENTER_INDEX].get(event.requestID());
		}
		else if(event instanceof MonitorContendedEnteredEventImpl) {
			return (EventRequestImpl)fEnabledRequests[MONITOR_CONTENDED_ENTERED_INDEX].get(event.requestID());
		}
		else
			throw new InternalError(RequestMessages.EventRequestManagerImpl_Got_event_of_unknown_type_2); 
	}

	/**
	 * @see com.sun.jdi.request.EventRequestManager#monitorContendedEnterRequests()
	 * @since 3.3
	 */
	public List monitorContendedEnterRequests() {
		return new ArrayList(fRequests[MONITOR_CONTENDED_ENTER_INDEX]);
	}
	
    /**
     * @see com.sun.jdi.request.EventRequestManager#monitorContendedEnteredRequests()
     * @since 3.3
     */
    public List monitorContendedEnteredRequests() {
    	return new ArrayList(fRequests[MONITOR_CONTENDED_ENTERED_INDEX]);
    }
    
    /**
     * @see com.sun.jdi.request.EventRequestManager#monitorWaitRequests()
     * @since 3.3
     */
    public List monitorWaitRequests() {
    	return new ArrayList(fRequests[MONITOR_WAIT_INDEX]);
    }
    
    /**
     * @see com.sun.jdi.request.EventRequestManager#monitorWaitedRequests()
     * @since 3.3
     */
    public List monitorWaitedRequests() {
    	return new ArrayList(fRequests[MONITOR_WAITED_INDEX]);
    }
}
