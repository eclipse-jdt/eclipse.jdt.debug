package com.sun.jdi;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.List;
import java.util.Map;

import com.sun.jdi.event.EventQueue;
import com.sun.jdi.request.EventRequestManager;

public interface VirtualMachine extends Mirror {
	public static final int TRACE_NONE = 0;
	public static final int TRACE_SENDS = 1;
	public static final int TRACE_RECEIVES = 2;
	public static final int TRACE_EVENTS = 4;
	public static final int TRACE_REFTYPES = 8;
	public static final int TRACE_OBJREFS = 16;
	public static final int TRACE_ALL = 16777215;
	public List allClasses();
	public List allThreads();
	public boolean canGetBytecodes();
	public boolean canGetCurrentContendedMonitor();
	public boolean canGetMonitorInfo();
	public boolean canGetOwnedMonitorInfo();
	public boolean canGetSyntheticAttribute();
	public boolean canWatchFieldAccess();
	public boolean canWatchFieldModification();
	public List classesByName(String arg1);
	public String description();
	public void dispose();
	public EventQueue eventQueue();
	public EventRequestManager eventRequestManager();
	public void exit(int arg1);
	public ByteValue mirrorOf(byte arg1);
	public CharValue mirrorOf(char arg1);
	public DoubleValue mirrorOf(double arg1);
	public FloatValue mirrorOf(float arg1);
	public IntegerValue mirrorOf(int arg1);
	public LongValue mirrorOf(long arg1);
	public StringReference mirrorOf(String arg1);
	public ShortValue mirrorOf(short arg1);
	public BooleanValue mirrorOf(boolean arg1);
	public Process process();
	public void resume();
	public void setDebugTraceMode(int arg1);
	public void suspend();
	public List topLevelThreadGroups();
	public String name();
	public String version();
	public void redefineClasses(Map arg1);
	public boolean canRedefineClasses();
	public boolean canUseInstanceFilters();
	public boolean canAddMethod();
	public boolean canUnrestrictedlyRedefineClasses();
	public boolean canPopFrames();
	public boolean canGetSourceDebugExtension();
	public boolean canRequestVMDeathEvent();
	public void setDefaultStratum(String arg1);
	public String getDefaultStratum();
}
