package org.eclipse.jdt.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.debug.core.model.ILineBreakpoint;


/**
 * Java line breakpoints are java breakpoints that suspend execution
 * when a particular line of code is reached.
 * 
 * Clients are not intended to implement this interface
 */
public interface IJavaLineBreakpoint extends IJavaBreakpoint, ILineBreakpoint {	

}

