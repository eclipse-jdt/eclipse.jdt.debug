package org.eclipse.jdt.internal.launching;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.File;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.jdt.launching.AbstractVMRunner;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.VMRunnerConfiguration;
import org.eclipse.jdt.launching.VMRunnerResult;

public class StandardVMRunner extends AbstractVMRunner {
	protected IVMInstall fVMInstance;

	public StandardVMRunner(IVMInstall vmInstance) {
		fVMInstance= vmInstance;
	}
	
	protected String renderDebugTarget(String classToRun, int host) {
		String format= "{0} at localhost:{1}";
		return MessageFormat.format(format, new String[] { classToRun, String.valueOf(host) });
	}

	public static String renderProcessLabel(String[] commandLine) {
		String format= "{0} ({1})";
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
		if (args == null) {
			return;
		}
		for (int i= 0; i < args.length; i++) {
			v.add(args[i]);
		}
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
			abort(MessageFormat.format("Specified working directory does not exist or is not a directory: {0}", new String[] {path}), null, IJavaLaunchConfigurationConstants.ERR_WORKING_DIRECTORY_DOES_NOT_EXIST);
		}
		return dir;
	}
	
	/**
	 * @see VMRunner#getPluginIdentifier()
	 */
	protected String getPluginIdentifier() {
		return LaunchingPlugin.PLUGIN_ID;
	}
	
	protected String constructProgramString() {
		StringBuffer buff= new StringBuffer(getJDKLocation());
		buff.append(File.separator);
		buff.append("bin"); //$NON-NLS-1$
		buff.append(File.separator);
		buff.append("java"); //$NON-NLS-1$
		return buff.toString();
	}	

	protected String convertClassPath(String[] cp) {
		int pathCount= 0;
		StringBuffer buf= new StringBuffer();
		if (cp.length == 0) {
			return "";    //$NON-NLS-1$
		}
		for (int i= 0; i < cp.length; i++) {
			if (pathCount > 0) {
				buf.append(File.pathSeparator);
			}
			buf.append(cp[i]);
			pathCount++;
		}
		return buf.toString();
	}


	/**
	 * @see IVMRunner#run(VMRunnerConfiguration)
	 */
	public VMRunnerResult run(VMRunnerConfiguration config, IProgressMonitor monitor) throws CoreException {
		
		String program= constructProgramString();
		File javawexe= new File(program + "w.exe"); //$NON-NLS-1$
		File javaw= new File(program + "w"); //$NON-NLS-1$
		
		if (javawexe.isFile()) {
			program= javawexe.getAbsolutePath();
		} else if (javaw.isFile()) {
			program= javaw.getAbsolutePath();
		}
		
		List arguments= new ArrayList();

		arguments.add(program);
				
		// VM args are the first thing after the java program so that users can specify
		// options like '-client' & '-server' which are required to be the first options
		String[] vmArgs= config.getVMArguments();
		addArguments(vmArgs, arguments);
		
		String[] bootCP= config.getBootClassPath();
		if (bootCP.length > 0) {
			arguments.add("-Xbootclasspath:" + convertClassPath(bootCP)); //$NON-NLS-1$
		} 
		
		String[] cp= config.getClassPath();
		if (cp.length > 0) {
			arguments.add("-classpath"); //$NON-NLS-1$
			arguments.add(convertClassPath(cp));
		}
		arguments.add(config.getClassToLaunch());
		
		String[] programArgs= config.getProgramArguments();
		addArguments(programArgs, arguments);
				
		String[] cmdLine= new String[arguments.size()];
		arguments.toArray(cmdLine);

		Process p= null;
		File workingDir = getWorkingDir(config);
		p= exec(cmdLine, workingDir);
		if (p == null) {
			return null;
		}
		
		IProcess process= DebugPlugin.getDefault().newProcess(p, renderProcessLabel(cmdLine));
		process.setAttribute(JavaRuntime.ATTR_CMDLINE, renderCommandLine(cmdLine));
		return new VMRunnerResult(null, new IProcess[] { process });
	}

	
}