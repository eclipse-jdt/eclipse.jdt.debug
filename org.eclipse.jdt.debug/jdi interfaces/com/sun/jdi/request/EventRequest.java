package com.sun.jdi.request;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import com.sun.jdi.*;
import com.sun.jdi.connect.*;
import com.sun.jdi.event.*;

public interface EventRequest extends com.sun.jdi.Mirror {
	public static final int SUSPEND_NONE = 0;
	public static final int SUSPEND_EVENT_THREAD = 1;
	public static final int SUSPEND_ALL = 2;
	public void addCountFilter(int arg1) throws InvalidRequestStateException;
	public void disable();
	public void enable();
	public boolean isEnabled();
	public void setEnabled(boolean arg1);
	public void setSuspendPolicy(int arg1);
	public int suspendPolicy();
	public Object getProperty(Object key);
	public void putProperty(Object key, Object value);
}
