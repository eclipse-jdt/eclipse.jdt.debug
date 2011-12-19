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
package com.sun.jdi.request;

import java.util.List;

import com.sun.jdi.Field;
import com.sun.jdi.Location;
import com.sun.jdi.Mirror;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadReference;
/**
 * See http://docs.oracle.com/javase/6/docs/jdk/api/jpda/jdi/com/sun/jdi/request/EventRequestManager.html
 */
public interface EventRequestManager extends Mirror {
	public List<AccessWatchpointRequest> accessWatchpointRequests();
	public List<BreakpointRequest> breakpointRequests();
	public List<ClassPrepareRequest> classPrepareRequests();
	public List<ClassUnloadRequest> classUnloadRequests();
	public AccessWatchpointRequest createAccessWatchpointRequest(Field arg1);
	public BreakpointRequest createBreakpointRequest(Location arg1);
	public ClassPrepareRequest createClassPrepareRequest();
	public ClassUnloadRequest createClassUnloadRequest();
	public ExceptionRequest createExceptionRequest(ReferenceType arg1, boolean arg2, boolean arg3);
	public MethodEntryRequest createMethodEntryRequest();
	public MethodExitRequest createMethodExitRequest();
	public MonitorContendedEnteredRequest createMonitorContendedEnteredRequest();
	public MonitorContendedEnterRequest createMonitorContendedEnterRequest();
	public MonitorWaitedRequest createMonitorWaitedRequest();
	public MonitorWaitRequest createMonitorWaitRequest();
	public ModificationWatchpointRequest createModificationWatchpointRequest(Field arg1);
	public StepRequest createStepRequest(ThreadReference arg1, int arg2, int arg3);
	public ThreadDeathRequest createThreadDeathRequest();
	public ThreadStartRequest createThreadStartRequest();
	public VMDeathRequest createVMDeathRequest();
	public void deleteAllBreakpoints();
	public void deleteEventRequest(EventRequest arg1);
	public void deleteEventRequests(List<? extends EventRequest> arg1);
	public List<ExceptionRequest> exceptionRequests();
	public List<MethodEntryRequest> methodEntryRequests();
	public List<MethodExitRequest> methodExitRequests();
	public List<ModificationWatchpointRequest> modificationWatchpointRequests();
	public List<StepRequest> stepRequests();
	public List<ThreadDeathRequest> threadDeathRequests();
	public List<ThreadStartRequest> threadStartRequests();
	public List<VMDeathRequest> vmDeathRequests();
	public List<MonitorContendedEnterRequest> monitorContendedEnterRequests();
	public List<MonitorContendedEnteredRequest> monitorContendedEnteredRequests();
	public List<MonitorWaitRequest> monitorWaitRequests();
	public List<MonitorWaitedRequest> monitorWaitedRequests();
}