package org.eclipse.jdt.launching;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.File;
import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IStatusHandler;
import org.eclipse.jdt.internal.launching.LaunchingMessages;
import org.eclipse.jdt.internal.launching.LaunchingPlugin;

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
	 * Performs a runtime exec on the given command line in the context
	 * of the specified working directory, and returns
	 * the resulting process. If the current runtime does not support the
	 * specification of a working directory, the status handler for error code
	 * <code>ERR_WORKING_DIRECTORY_NOT_SUPPORTED</code> is queried to see if the
	 * exec should be re-executed without specifying a working directory.
	 * 
	 * @param cmdLine the command line
	 * @param workingDirectory the working directory, or <code>null</code>
	 * @return the resulting process or <code>null</code> if the exec is
	 *  cancelled
	 * @see Runtime
	 */
	protected Process exec(String[] cmdLine, File workingDirectory) throws CoreException {
		Process p= null;
		try {
			if (workingDirectory == null) {
				p= Runtime.getRuntime().exec(cmdLine, null);
			} else {
				p= Runtime.getRuntime().exec(cmdLine, null, workingDirectory);
			}
		} catch (IOException e) {
				if (p != null) {
					p.destroy();
				}
				abort(LaunchingMessages.getString("AbstractVMRunner.Exception_starting_process_1"), e, IJavaLaunchConfigurationConstants.ERR_INTERNAL_ERROR); //$NON-NLS-1$
		} catch (NoSuchMethodError e) {
			//attempting launches on 1.2.* - no ability to set working directory
			
			IStatus status = new Status(IStatus.ERROR, LaunchingPlugin.getUniqueIdentifier(), IJavaLaunchConfigurationConstants.ERR_WORKING_DIRECTORY_NOT_SUPPORTED, LaunchingMessages.getString("AbstractVMRunner.Eclipse_runtime_does_not_support_working_directory_2"), e); //$NON-NLS-1$
			IStatusHandler handler = DebugPlugin.getDefault().getStatusHandler(status);
			
			if (handler != null) {
				Object result = handler.handleStatus(status, this);
				if (result instanceof Boolean && ((Boolean)result).booleanValue()) {
					p= exec(cmdLine, null);
				}
			}
		}
		return p;
	}			
			
}
