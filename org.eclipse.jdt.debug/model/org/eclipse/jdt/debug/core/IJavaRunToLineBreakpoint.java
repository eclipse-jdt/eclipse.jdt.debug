package org.eclipse.jdt.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
/**
 * Java run to line breakpoints are transient java line breakpoints.
 * Run to line breakpoints are created with a hit count of 1 and
 * they are not added to the breakpoint manager or persisted.
 * 
 * Clients are not intended to implement this interface.
 */
public interface IJavaRunToLineBreakpoint extends IJavaLineBreakpoint {

}

