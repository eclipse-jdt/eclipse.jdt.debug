package com.sun.jdi;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import com.sun.jdi.connect.*;
import com.sun.jdi.event.*;
import com.sun.jdi.request.*;

public interface VirtualMachine extends com.sun.jdi.Mirror {
	public static final int TRACE_NONE = 0;
	public static final int TRACE_SENDS = 1;
	public static final int TRACE_RECEIVES = 2;
	public static final int TRACE_EVENTS = 4;
	public static final int TRACE_REFTYPES = 8;
	public static final int TRACE_OBJREFS = 16;
	public static final int TRACE_ALL = 16777215;
	public java.util.List allClasses();
	public java.util.List allThreads();
	public boolean canGetBytecodes();
	public boolean canGetCurrentContendedMonitor();
	public boolean canGetMonitorInfo();
	public boolean canGetOwnedMonitorInfo();
	public boolean canGetSyntheticAttribute();
	public boolean canWatchFieldAccess();
	public boolean canWatchFieldModification();
	public java.util.List classesByName(String arg1);
	public String description();
	public void dispose();
	public com.sun.jdi.event.EventQueue eventQueue();
	public com.sun.jdi.request.EventRequestManager eventRequestManager();
	public void exit(int arg1);
	public com.sun.jdi.ByteValue mirrorOf(byte arg1);
	public com.sun.jdi.CharValue mirrorOf(char arg1);
	public com.sun.jdi.DoubleValue mirrorOf(double arg1);
	public com.sun.jdi.FloatValue mirrorOf(float arg1);
	public com.sun.jdi.IntegerValue mirrorOf(int arg1);
	public com.sun.jdi.LongValue mirrorOf(long arg1);
	public com.sun.jdi.StringReference mirrorOf(String arg1);
	public com.sun.jdi.ShortValue mirrorOf(short arg1);
	public com.sun.jdi.BooleanValue mirrorOf(boolean arg1);
	public Process process();
	public void resume();
	public void setDebugTraceMode(int arg1);
	public void suspend();
	public java.util.List topLevelThreadGroups();
	public String name();
	public String version();
	public void redefineClasses(java.util.Map arg1);
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
