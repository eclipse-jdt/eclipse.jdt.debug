/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

package org.eclipse.jdt.internal.launching.macosx;

import java.io.File;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.launching.AbstractVMRunner;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.VMRunnerConfiguration;

public abstract class JavaVMRunner extends AbstractVMRunner {

	protected IVMInstall fVMInstance;

	public JavaVMRunner(IVMInstall vmInstance) {
		fVMInstance= vmInstance;
	}
	
	protected String renderDebugTarget(String classToRun, int host) {
		String format= MacOSXLauncherMessages.getString("javaVMRunner.format.dbgTarget"); //$NON-NLS-1$
		return MessageFormat.format(format, new String[] { classToRun, String.valueOf(host) });
	}

	public static String renderProcessLabel(String[] commandLine) {
		String format= MacOSXLauncherMessages.getString("javaVMRunner.format.processLabel"); //$NON-NLS-1$
		String timestamp= DateFormat.getInstance().format(new Date(System.currentTimeMillis()));
		return MessageFormat.format(format, new String[] { commandLine[0], timestamp });
	}
	
	protected static String renderCommandLine(String[] commandLine) {
		if (commandLine.length < 1)
			return ""; //$NON-NLS-1$
		StringBuffer buf= new StringBuffer(commandLine[0]);
		for (int i= 1; i < commandLine.length; i++) {
			buf.append(' ');
			buf.append(commandLine[i]);
		}	
		return buf.toString();
	}	
	
	protected void addArguments(String[] args, List v) {
		if (args == null)
			return;
		for (int i= 0; i < args.length; i++)
			v.add(args[i]);
	}
	
	protected String getJDKLocation() {
		File location= fVMInstance.getInstallLocation();
		return location.getAbsolutePath();
	}
	
	/**
	 * Returns the working directory to use for the launched VM,
	 * or <code>null</code> if the working directory is to be inherited
	 * from the current process.
	 * 
	 * @return the working directory to use
	 * @exception CoreException if the working directory specified by
	 *  the configuration does not exist or is not a directory
	 */	
	protected File getWorkingDir(VMRunnerConfiguration config) throws CoreException {
		String path = config.getWorkingDirectory();
		if (path == null) {
			return null;
		}
		File dir = new File(path);
		if (!dir.isDirectory()) {
			abort(MessageFormat.format(MacOSXLauncherMessages.getString("JavaVMRunner.Specified_working_directory_does_not_exist_or_is_not_a_directory__{0}_1"), new String[] {path}), null, IJavaLaunchConfigurationConstants.ERR_WORKING_DIRECTORY_DOES_NOT_EXIST); //$NON-NLS-1$
		}
		return dir;
	}	
	
	/**
	 * @see VMRunner#getPluginIdentifier()
	 */
	protected String getPluginIdentifier() {
		return MacOSXLaunchingPlugin.getUniqueIdentifier();
	}
}