package com.sun.jdi.request;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.List;

import com.sun.jdi.Field;
import com.sun.jdi.Location;
import com.sun.jdi.Mirror;
import com.sun.jdi.ThreadReference;

public interface EventRequestManager extends Mirror {
	public List accessWatchpointRequests();
	public List breakpointRequests();
	public List classPrepareRequests();
	public List classUnloadRequests();
	public AccessWatchpointRequest createAccessWatchpointRequest(Field arg1);
	public BreakpointRequest createBreakpointRequest(Location arg1);
	public ClassPrepareRequest createClassPrepareRequest();
	public ClassUnloadRequest createClassUnloadRequest();
	public ExceptionRequest createExceptionRequest(com.sun.jdi.ReferenceType arg1, boolean arg2, boolean arg3);
	public MethodEntryRequest createMethodEntryRequest();
	public MethodExitRequest createMethodExitRequest();
	public ModificationWatchpointRequest createModificationWatchpointRequest(Field arg1);
	public StepRequest createStepRequest(ThreadReference arg1, int arg2, int arg3);
	public ThreadDeathRequest createThreadDeathRequest();
	public ThreadStartRequest createThreadStartRequest();
	public void deleteAllBreakpoints();
	public void deleteEventRequest(EventRequest arg1);
	public void deleteEventRequests(List arg1);
	public List exceptionRequests();
	public List methodEntryRequests();
	public List methodExitRequests();
	public List modificationWatchpointRequests();
	public List stepRequests();
	public List threadDeathRequests();
	public List threadStartRequests();
	public VMDeathRequest createVMDeathRequest();
	public List vmDeathRequests();
}
