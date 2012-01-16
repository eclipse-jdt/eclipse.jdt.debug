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
import java.util.Map;

import com.sun.jdi.event.EventQueue;
import com.sun.jdi.request.EventRequestManager;
/**
 * See http://docs.oracle.com/javase/6/docs/jdk/api/jpda/jdi/com/sun/jdi/VirtualMachine.html
 */
public interface VirtualMachine extends Mirror {
	public static final int TRACE_NONE = 0;
	public static final int TRACE_SENDS = 1;
	public static final int TRACE_RECEIVES = 2;
	public static final int TRACE_EVENTS = 4;
	public static final int TRACE_REFTYPES = 8;
	public static final int TRACE_OBJREFS = 16;
	public static final int TRACE_ALL = 16777215;
	public List<ReferenceType> allClasses();
	public List<ThreadReference> allThreads();
	public boolean canAddMethod();
	public boolean canBeModified();
	public boolean canGetBytecodes();
	public boolean canGetCurrentContendedMonitor();
	public boolean canGetMonitorInfo();
	public boolean canGetOwnedMonitorInfo();
	public boolean canGetSourceDebugExtension();
	public boolean canGetSyntheticAttribute();
	public boolean canPopFrames();
	public boolean canRedefineClasses();
	public boolean canRequestVMDeathEvent();
	public boolean canUnrestrictedlyRedefineClasses();
	public boolean canUseInstanceFilters();
	public boolean canWatchFieldAccess();
	public boolean canWatchFieldModification();
	public List<ReferenceType> classesByName(String arg1);
	public String description();
	public void dispose();
	public EventQueue eventQueue();
	public EventRequestManager eventRequestManager();
	public void exit(int arg1);
	public String getDefaultStratum();
	public BooleanValue mirrorOf(boolean arg1);
	public ByteValue mirrorOf(byte arg1);
	public CharValue mirrorOf(char arg1);
	public DoubleValue mirrorOf(double arg1);
	public FloatValue mirrorOf(float arg1);
	public IntegerValue mirrorOf(int arg1);
	public LongValue mirrorOf(long arg1);
	public StringReference mirrorOf(String arg1);
	public ShortValue mirrorOf(short arg1);
	public String name();
	public Process process();
	public void redefineClasses(Map<? extends ReferenceType,byte[]> arg1);
	public void resume();
	public void setDebugTraceMode(int arg1);
	public void setDefaultStratum(String arg1);
	public void suspend();
	public List<ThreadGroupReference> topLevelThreadGroups();
	public String version();
	public boolean canGetInstanceInfo();
	public long[] instanceCounts(List<? extends ReferenceType> arg1);
	public boolean canGetClassFileVersion();
	public boolean canGetConstantPool();
	public boolean canUseSourceNameFilters();
	public boolean canGetMethodReturnValues();
	public boolean canForceEarlyReturn();
	public boolean canRequestMonitorEvents();
	public boolean canGetMonitorFrameInfo();
	public VoidValue mirrorOfVoid();
}
