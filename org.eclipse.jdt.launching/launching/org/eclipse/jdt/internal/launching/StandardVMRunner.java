package org.eclipse.jdt.internal.launching;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.text.DateFormat;
import java.text.MessageFormat;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.jdt.launching.AbstractVMRunner;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.VMRunnerConfiguration;

public class StandardVMRunner extends AbstractVMRunner {
	protected IVMInstall fVMInstance;

	public StandardVMRunner(IVMInstall vmInstance) {
		fVMInstance= vmInstance;
	}
	
	protected String renderDebugTarget(String classToRun, int host) {
		String format= LaunchingMessages.getString("StandardVMRunner.{0}_at_localhost_{1}_1"); //$NON-NLS-1$
		return MessageFormat.format(format, new String[] { classToRun, String.valueOf(host) });
	}

	public static String renderProcessLabel(String[] commandLine) {
		String format= LaunchingMessages.getString("StandardVMRunner.{0}_({1})_2"); //$NON-NLS-1$
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
			abort(MessageFormat.format(LaunchingMessages.getString("StandardVMRunner.Specified_working_directory_does_not_exist_or_is_not_a_directory__{0}_3"), new String[] {path}), null, IJavaLaunchConfigurationConstants.ERR_WORKING_DIRECTORY_DOES_NOT_EXIST); //$NON-NLS-1$
		}
		return dir;
	}
	
	/**
	 * @see VMRunner#getPluginIdentifier()
	 */
	protected String getPluginIdentifier() {
		return LaunchingPlugin.getUniqueIdentifier();
	}
	
	protected String constructProgramString(VMRunnerConfiguration config) {
		String command= null;
		Map map= config.getVMSpecificAttributesMap();
		if (map != null) {
			command = (String)map.get(IJavaLaunchConfigurationConstants.ATTR_JAVA_COMMAND);
		}
		StringBuffer buff= new StringBuffer(getJDKLocation());
		buff.append(File.separator);
		buff.append("bin"); //$NON-NLS-1$
		buff.append(File.separator);
		String jdkLocation= buff.toString();
		if (command == null) {
			buff.append("java"); //$NON-NLS-1$
			return adjustProgramString(buff.toString());
		} 
		
		buff.append(command);
		String program= buff.toString();
		File exe= new File(program + ".exe"); //$NON-NLS-1$
		File javaCommand= new File(program); 
		
		if (!exe.isFile() && !javaCommand.isFile()) {
			File java= new File(jdkLocation + "java.exe"); //$NON-NLS-1$
			if (java.isFile()) {
				program= java.getAbsolutePath();
			} else {
				java= new File(jdkLocation + "java"); //$NON-NLS-1$
				if (java.isFile()) {
					program= java.getAbsolutePath();
				}
			}
		}
		return program;
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
	 * @see IVMRunner#run(VMRunnerConfiguration, ILaunch, IProgressMonitor)
	 */
	public void run(VMRunnerConfiguration config, ILaunch launch, IProgressMonitor monitor) throws CoreException {

		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		
		String program= constructProgramString(config);
		
		List arguments= new ArrayList();
		arguments.add(program);
				
		// VM args are the first thing after the java program so that users can specify
		// options like '-client' & '-server' which are required to be the first option
		String[] vmArgs= config.getVMArguments();
		addArguments(vmArgs, arguments);
				
		String[] bootCP= config.getBootClassPath();
		if (bootCP != null) {
			if (bootCP.length > 0) {
				arguments.add("-Xbootclasspath:" + convertClassPath(bootCP)); //$NON-NLS-1$
			} else {
				// empty
				arguments.add("-Xbootclasspath:"); //$NON-NLS-1$	
			}
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

		// check for cancellation
		if (monitor.isCanceled()) {
			return;
		}
		
		Process p= null;
		File workingDir = getWorkingDir(config);
		p= exec(cmdLine, workingDir);
		if (p == null) {
			return;
		}
		
		// check for cancellation
		if (monitor.isCanceled()) {
			p.destroy();
			return;
		}		
		
		IProcess process= DebugPlugin.getDefault().newProcess(launch, p, renderProcessLabel(cmdLine));
		process.setAttribute(JavaRuntime.ATTR_CMDLINE, renderCommandLine(cmdLine));
	}

	protected String adjustProgramString(String program) {
		
		File javawexe= new File(program + "w.exe"); //$NON-NLS-1$
		File javaw= new File(program + "w"); //$NON-NLS-1$
		if (javawexe.isFile()) {
			program= javawexe.getAbsolutePath();
		} else if (javaw.isFile()) {
			program= javaw.getAbsolutePath();
		}
		
		return program;
	}	

}