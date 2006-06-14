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
package com.sun.jdi.request;


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
	public void deleteEventRequests(List arg1);
	public List exceptionRequests();
	public List methodEntryRequests();
	public List methodExitRequests();
	public List modificationWatchpointRequests();
	public List stepRequests();
	public List threadDeathRequests();
	public List threadStartRequests();
	public List vmDeathRequests();
	public List monitorContendedEnterRequests();
    public List monitorContendedEnteredRequests();
    public List monitorWaitRequests();
    public List monitorWaitedRequests();
}
