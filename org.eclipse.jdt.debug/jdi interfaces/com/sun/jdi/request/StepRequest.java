package com.sun.jdi.request;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadReference;

public interface StepRequest extends EventRequest {
	public static final int STEP_INTO = 1;
	public static final int STEP_OVER = 2;
	public static final int STEP_OUT = 3;
	public static final int STEP_MIN = -1;
	public static final int STEP_LINE = -2;
	public void addClassExclusionFilter(String arg1);
	public void addClassFilter(ReferenceType arg1);
	public void addClassFilter(String arg1);
	public int depth();
	public int size();
	public ThreadReference thread();
	public void addInstanceFilter(ObjectReference instance);
}
