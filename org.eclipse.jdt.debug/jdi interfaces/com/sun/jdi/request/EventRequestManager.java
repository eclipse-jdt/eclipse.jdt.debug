package com.sun.jdi.request;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import com.sun.jdi.*;
import com.sun.jdi.connect.*;
import com.sun.jdi.event.*;

public interface EventRequestManager extends com.sun.jdi.Mirror {
	public java.util.List accessWatchpointRequests();
	public java.util.List breakpointRequests();
	public java.util.List classPrepareRequests();
	public java.util.List classUnloadRequests();
	public com.sun.jdi.request.AccessWatchpointRequest createAccessWatchpointRequest(com.sun.jdi.Field arg1);
	public com.sun.jdi.request.BreakpointRequest createBreakpointRequest(com.sun.jdi.Location arg1);
	public com.sun.jdi.request.ClassPrepareRequest createClassPrepareRequest();
	public com.sun.jdi.request.ClassUnloadRequest createClassUnloadRequest();
	public com.sun.jdi.request.ExceptionRequest createExceptionRequest(com.sun.jdi.ReferenceType arg1, boolean arg2, boolean arg3);
	public com.sun.jdi.request.MethodEntryRequest createMethodEntryRequest();
	public com.sun.jdi.request.MethodExitRequest createMethodExitRequest();
	public com.sun.jdi.request.ModificationWatchpointRequest createModificationWatchpointRequest(com.sun.jdi.Field arg1);
	public com.sun.jdi.request.StepRequest createStepRequest(com.sun.jdi.ThreadReference arg1, int arg2, int arg3);
	public com.sun.jdi.request.ThreadDeathRequest createThreadDeathRequest();
	public com.sun.jdi.request.ThreadStartRequest createThreadStartRequest();
	public void deleteAllBreakpoints();
	public void deleteEventRequest(com.sun.jdi.request.EventRequest arg1);
	public void deleteEventRequests(java.util.List arg1);
	public java.util.List exceptionRequests();
	public java.util.List methodEntryRequests();
	public java.util.List methodExitRequests();
	public java.util.List modificationWatchpointRequests();
	public java.util.List stepRequests();
	public java.util.List threadDeathRequests();
	public java.util.List threadStartRequests();
	public VMDeathRequest createVMDeathRequest();
}
