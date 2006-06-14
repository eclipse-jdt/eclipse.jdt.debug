package com.sun.jdi;

public interface MonitorInfo extends Mirror {
	 public ObjectReference monitor() throws InvalidStackFrameException;
	 public int stackDepth() throws InvalidStackFrameException;
	 public ThreadReference thread() throws InvalidStackFrameException;
}
