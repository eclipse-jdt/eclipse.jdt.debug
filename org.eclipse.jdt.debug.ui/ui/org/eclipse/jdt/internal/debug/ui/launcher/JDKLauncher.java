package org.eclipse.jdt.internal.debug.ui.launcher;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.VMRunnerConfiguration;
import org.eclipse.jdt.launching.VMRunnerResult;
import org.eclipse.jface.dialogs.MessageDialog;

public class JDKLauncher extends JavaLauncher {
	
	public interface IRetryQuery {
		/**
		 * Query the user to retry connecting to the VM.
		 */
		boolean queryRetry();
	}
	
	public JDKLauncher(IVMInstall vmInstance) {
		super(vmInstance);
	}
	
	/**
	 * @see IVMRunner#run(VMRunnerConfiguration)
	 */
	public VMRunnerResult run(VMRunnerConfiguration config) throws CoreException {
		verifyVMInstall();
		
		String program= constructProgramString();
		File javawexe= new File(program + "w.exe"); //$NON-NLS-1$
		File javaw= new File(program + "w"); //$NON-NLS-1$
		
		if (javawexe.isFile()) {
			program= javaw.getAbsolutePath();
		} else if (javaw.isFile()) {
			program= javawexe.getAbsolutePath();
		}
		
		List arguments= new ArrayList();

		arguments.add(program);
				
		String[] bootCP= config.getBootClassPath();
		if (bootCP.length > 0) {
			arguments.add("-Xbootclasspath:" + convertClassPath(bootCP)); //$NON-NLS-1$
		} 
		
		String[] cp= config.getClassPath();
		if (cp.length > 0) {
			arguments.add("-classpath"); //$NON-NLS-1$
			arguments.add(convertClassPath(cp));
		}
		String[] vmArgs= config.getVMArguments();
		addArguments(vmArgs, arguments);
		
		arguments.add(config.getClassToLaunch());
		
		String[] programArgs= config.getProgramArguments();
		addArguments(programArgs, arguments);
				
		String[] cmdLine= new String[arguments.size()];
		arguments.toArray(cmdLine);

		Process p= null;
		try {
			File workingDir = getWorkingDir(config);
			p= createProcess(workingDir, cmdLine);
		} catch (NoSuchMethodError e) {
			//attempting launches on 1.2.* - no ability to set working directory
			boolean retry= createRetryQueryForNoWorkingDirectory().queryRetry();
			if (retry) {
				p= createProcess(null, cmdLine);
			}
		}
		if (p == null) {
			return null;
		}
		IProcess process= DebugPlugin.getDefault().newProcess(p, renderProcessLabel(cmdLine));
		process.setAttribute(JavaRuntime.ATTR_CMDLINE, renderCommandLine(cmdLine));
		return new VMRunnerResult(null, new IProcess[] { process });
	}

	protected String convertClassPath(String[] cp) {
		int pathCount= 0;
		StringBuffer buf= new StringBuffer();
		if (cp.length == 0)
			return ""; //$NON-NLS-1$
		for (int i= 0; i < cp.length; i++) {
			if (cp[i].endsWith("rt.jar")) { //$NON-NLS-1$
				File f= new File(cp[i]);
				if ("rt.jar".equals(f.getName())) //$NON-NLS-1$
					continue;
			}
			if (pathCount > 0) {
				buf.append(File.pathSeparator);
			}
			buf.append(cp[i]);
			pathCount++;
		}
		return buf.toString();
	}
	
	protected Process createProcess(File workingDir, String[] cmdLine) throws CoreException {
		Process p= null;
		try {
			if (workingDir == null) {
				p= Runtime.getRuntime().exec(cmdLine, null);
			} else {
				p= Runtime.getRuntime().exec(cmdLine, null, workingDir);
			}
		} catch (IOException e) {
				if (p != null) {
					p.destroy();
				}
				throw new CoreException(createStatus(LauncherMessages.getString("jdkLauncher.error.startVM"), e)); //$NON-NLS-1$
		} 
		return p;
	}
	
	protected IRetryQuery createRetryQueryForNoWorkingDirectory() {
		return new IRetryQuery() {
			public boolean queryRetry() {
				final boolean[] result= new boolean[1];
				JDIDebugUIPlugin.getStandardDisplay().syncExec(new Runnable() {
					public void run() {
						String title= LauncherMessages.getString("jdkLauncher.error.title"); //$NON-NLS-1$
						String message= LauncherMessages.getString("JDKDebugLauncher.Setting_a_working_directory"); //$NON-NLS-1$
						result[0]= (MessageDialog.openQuestion(JDIDebugUIPlugin.getActiveWorkbenchShell(), title, message));
					}
				});
				return result[0];
			}
		};
	}
	
	protected String constructProgramString() {
		StringBuffer buff= new StringBuffer(getJDKLocation());
		buff.append(File.separator);
		buff.append("bin"); //$NON-NLS-1$
		buff.append(File.separator);
		buff.append("java"); //$NON-NLS-1$
		return buff.toString();
	}	
	
	/**
	 * @see IVMRunner#run(ILaunchConfiguration)
	 */
	public VMRunnerResult run(ILaunchConfiguration config) throws CoreException {
		verifyVMInstall();
		
		String program= constructProgramString();
		File javawexe= new File(program + "w.exe"); //$NON-NLS-1$
		File javaw= new File(program + "w"); //$NON-NLS-1$
		
		if (javawexe.isFile()) {
			program= javaw.getAbsolutePath();
		} else if (javaw.isFile()) {
			program= javawexe.getAbsolutePath();
		}
		
		List arguments= new ArrayList();

		arguments.add(program);
				
		String[] bootCP= getBootpath(config);
		if (bootCP.length > 0) {
			arguments.add("-Xbootclasspath:" + convertClassPath(bootCP)); //$NON-NLS-1$
		} 
		
		String[] cp= getClasspath(config);
		if (cp.length > 0) {
			arguments.add("-classpath"); //$NON-NLS-1$
			arguments.add(convertClassPath(cp));
		}
		String[] vmArgs= getVMArgumentsArray(config);
		addArguments(vmArgs, arguments);
		
		arguments.add(verifyMainTypeName(config));
		
		String[] programArgs= getProgramArgumentsArray(config);
		addArguments(programArgs, arguments);
				
		String[] cmdLine= new String[arguments.size()];
		arguments.toArray(cmdLine);

		Process p= null;
		try {
			File workingDir = verifyWorkingDirectory(config);
			p= createProcess(workingDir, cmdLine);
		} catch (NoSuchMethodError e) {
			//attempting launches on 1.2.* - no ability to set working directory
			boolean retry= createRetryQueryForNoWorkingDirectory().queryRetry();
			if (retry) {
				p= createProcess(null, cmdLine);
			}
		}
		if (p == null) {
			return null;
		}
		IProcess process= DebugPlugin.getDefault().newProcess(p, renderProcessLabel(cmdLine));
		process.setAttribute(JavaRuntime.ATTR_CMDLINE, renderCommandLine(cmdLine));
		return new VMRunnerResult(null, new IProcess[] { process });
	}	
}