package org.eclipse.jdt.internal.debug.ui.launcher;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.File;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.debug.ui.JavaDebugUI;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMRunner;

public abstract class JavaLauncher implements IVMRunner {
	protected IVMInstall fVMInstance;

	public JavaLauncher(IVMInstall vmInstance) {
		fVMInstance= vmInstance;
	}
	
	protected String renderDebugTarget(String classToRun, int host) {
		String format= LauncherMessages.getString("javaLauncher.format.dbgTarget"); //$NON-NLS-1$
		return MessageFormat.format(format, new String[] { classToRun, String.valueOf(host) });
	}

	public static String renderProcessLabel(String[] commandLine) {
		String format= LauncherMessages.getString("javaLauncher.format.processLabel"); //$NON-NLS-1$
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
		
	/**
	 * Verifies this launcher's VM install is valid.
	 * 
	 * @exception CoreException if this launcher's VM install
	 *  is invalid. Reasons include:<ul>
	 * <li>The install location does not exist</li>
	 * </ul>
	 */
	protected void verifyVMInstall() throws CoreException {
		File location = fVMInstance.getInstallLocation();
		if (location == null) {
			throw new CoreException(createStatus(
				MessageFormat.format(LauncherMessages.getString("JavaLauncher.JRE_home_not_specified"), new String[]{fVMInstance.getName()}), null)); //$NON-NLS-1$
		}
		if (!location.exists()) {
			throw new CoreException(createStatus(
				MessageFormat.format(LauncherMessages.getString("JavaLauncher.JRE_home_does_not_exist"), new String[]{fVMInstance.getName(), location.getAbsolutePath()}), null)); //$NON-NLS-1$
		}
	}
	
	protected String getJDKLocation() {
		File location= fVMInstance.getInstallLocation();
		return location.getAbsolutePath();
	}
	
	protected static IStatus createStatus(String message, Throwable th) {
		return new Status(IStatus.ERROR, JavaDebugUI.PLUGIN_ID, IStatus.ERROR, message, th);
	}
		
		
	
	
}