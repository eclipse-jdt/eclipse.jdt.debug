package org.eclipse.jdt.launching;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IProcess;

/**
 * Abstract implementation of a VM runner.
 * <p>
 * Clients implementing VM runners should subclass this class.
 * </p>
 * @see IVMRunner
 * @since 2.0
 */
public abstract class AbstractVMRunner implements IVMRunner {

	/**
	 * Throws a core exception with an error status object built from
	 * the given message, lower level exception, and error code.
	 * 
	 * @param message the status message
	 * @param exception lower level exception associated with the
	 *  error, or <code>null</code> if none
	 * @param code error code
	 */
	protected void abort(String message, Throwable exception, int code) throws CoreException {
		throw new CoreException(new Status(IStatus.ERROR, getPluginIdentifier(), code, message, exception));
	}
	
	/**
	 * Returns the identifier of the plug-in this VM runner 
	 * originated from.
	 * 
	 * @return plug-in identifier
	 */
	protected abstract String getPluginIdentifier();
	
	/**
	 * @see DebugPlugin#exec(String[], File)
	 */
	protected Process exec(String[] cmdLine, File workingDirectory) throws CoreException {
		return DebugPlugin.exec(cmdLine, workingDirectory);
	}	
	
	protected String getCmdLineAsString(String[] cmdLine) {
		StringBuffer buff= new StringBuffer();
		for (int i = 0, numStrings= cmdLine.length; i < numStrings; i++) {
			buff.append(cmdLine[i]);
			buff.append(' ');	
		} 
		return buff.toString().trim();
	}
	
	/**
	 * Returns the default process attribute map for java processes.
	 * 
	 * @return defatult process attribute map for java processes
	 */
	protected Map getDefaultProcessMap() {
		Map map = new HashMap();
		map.put(IProcess.ATTR_PROCESS_TYPE, IJavaLaunchConfigurationConstants.ID_JAVA_PROCESS_TYPE);
		return map;
	}
			
}
