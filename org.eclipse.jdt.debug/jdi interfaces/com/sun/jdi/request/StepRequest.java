package com.sun.jdi.request;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import com.sun.jdi.*;
import com.sun.jdi.connect.*;
import com.sun.jdi.event.*;

public interface StepRequest extends com.sun.jdi.request.EventRequest {
	public static final int STEP_INTO = 1;
	public static final int STEP_OVER = 2;
	public static final int STEP_OUT = 3;
	public static final int STEP_MIN = -1;
	public static final int STEP_LINE = -2;
	public void addClassExclusionFilter(String arg1);
	public void addClassFilter(com.sun.jdi.ReferenceType arg1);
	public void addClassFilter(String arg1);
	public int depth();
	public int size();
	public com.sun.jdi.ThreadReference thread();
	public void addInstanceFilter(ObjectReference instance);
}
