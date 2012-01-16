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
package com.sun.jdi;

import java.util.List;
/**
 * See http://docs.oracle.com/javase/6/docs/jdk/api/jpda/jdi/com/sun/jdi/ThreadReference.html
 */
public interface ThreadReference extends ObjectReference {
	public static final int THREAD_STATUS_UNKNOWN = -1;
	public static final int THREAD_STATUS_ZOMBIE = 0;
	public static final int THREAD_STATUS_RUNNING = 1;
	public static final int THREAD_STATUS_SLEEPING = 2;
	public static final int THREAD_STATUS_MONITOR = 3;
	public static final int THREAD_STATUS_WAIT = 4;
	public static final int THREAD_STATUS_NOT_STARTED = 5;
	public ObjectReference currentContendedMonitor() throws IncompatibleThreadStateException;
	public StackFrame frame(int arg1) throws IncompatibleThreadStateException;
	public int frameCount() throws IncompatibleThreadStateException;
	public List<StackFrame> frames() throws IncompatibleThreadStateException;
	public List<StackFrame> frames(int arg1, int arg2) throws IncompatibleThreadStateException;
	public void interrupt();
	public boolean isAtBreakpoint();
	public boolean isSuspended();
	public String name();
	public List<ObjectReference> ownedMonitors() throws IncompatibleThreadStateException;
	public void popFrames(StackFrame frame) throws IncompatibleThreadStateException;
	public void resume();
	public int status();
	public void stop(ObjectReference arg1) throws InvalidTypeException;
	public void suspend();
	public int suspendCount();
	public ThreadGroupReference threadGroup();
	public void forceEarlyReturn(Value arg1) throws InvalidTypeException, ClassNotLoadedException, IncompatibleThreadStateException;
	public List<MonitorInfo> ownedMonitorsAndFrames() throws IncompatibleThreadStateException;
}
