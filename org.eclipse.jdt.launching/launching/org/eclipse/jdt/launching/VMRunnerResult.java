/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.jdt.launching;

import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.jdt.internal.launching.LaunchingMessages;


/**
 * This class holds the results of a call to <code>IVMRunner.run()</code>.
 */

public class VMRunnerResult {
	private IDebugTarget fDebugTarget;
	private IProcess[] fProcesses;
	
	/**
	 * Constructs a new VMRunnerResult.
	 * @param target	  An IDebugTarget if a debugger connection was established.
	 			  May be null.
	 * @param processes An array of the processes that were created. Must not be
	 			  null. The first entry in the processes list (if it exists)
	 			  will be interpreted as the process representing the 
	  			  debug target.	
	 */
	public VMRunnerResult(IDebugTarget target, IProcess[] processes) {
		fDebugTarget= target;
		fProcesses= processes;
		if (processes == null) {
			throw new IllegalArgumentException(LaunchingMessages.getString("vmRunnerResult.assert.processesNotNull")); //$NON-NLS-1$
		}
	}
	
	/**
	 * @return 	The debug target passed into the constructor.
	 */
	public IDebugTarget getDebugTarget() {
		return fDebugTarget;
	}
	
	/**
	 *  @return The processes passed into the constructor
	 */
	public IProcess[] getProcesses() {
		return fProcesses;
	}

}