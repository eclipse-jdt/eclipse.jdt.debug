/*******************************************************************************
 *  Copyright (c) 2000, 2012 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.launching;


import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.Launch;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.jdt.launching.AbstractVMRunner;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstall2;
import org.eclipse.jdt.launching.VMRunnerConfiguration;
import org.eclipse.osgi.util.NLS;

import com.ibm.icu.text.DateFormat;

/**
 * A launcher for running Java main classes.
 */
public class StandardVMRunner extends AbstractVMRunner {
	
	/**
	 * The VM install instance
	 */
	protected IVMInstall fVMInstance;

	/**
	 * Constructor
	 * @param vmInstance the VM
	 */
	public StandardVMRunner(IVMInstall vmInstance) {
		fVMInstance= vmInstance;
	}
	
	/**
	 * Returns the 'rendered' name for the current target
	 * @param classToRun the class
	 * @param host the host name
	 * @return the name for the current target
	 */
	protected String renderDebugTarget(String classToRun, int host) {
		String format= LaunchingMessages.StandardVMRunner__0__at_localhost__1__1; 
		return NLS.bind(format, new String[] { classToRun, String.valueOf(host) });
	}

	/**
	 * Returns the 'rendered' name for the specified command line
	 * @param commandLine the command line
	 * @return the name for the process
	 */
	public static String renderProcessLabel(String[] commandLine) {
		String format= LaunchingMessages.StandardVMRunner__0____1___2; 
		String timestamp= DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM).format(new Date(System.currentTimeMillis()));
		return NLS.bind(format, new String[] { commandLine[0], timestamp });
	}
	
	/**
	 * Prepares the command line from the specified array of strings
	 * @param commandLine the command line
	 * @return the command line label
	 */
	protected String renderCommandLine(String[] commandLine) {
		if (commandLine.length < 1)
			return ""; //$NON-NLS-1$
		StringBuffer buf= new StringBuffer();
		for (int i= 0; i < commandLine.length; i++) {
			buf.append(' ');
			char[] characters= commandLine[i].toCharArray();
			StringBuffer command= new StringBuffer();
			boolean containsSpace= false;
			for (int j = 0; j < characters.length; j++) {
				char character= characters[j];
				if (character == '\"') {
					command.append('\\');
				} else if (character == ' ') {
					containsSpace = true;
				}
				command.append(character);
			}
			if (containsSpace) {
				buf.append('\"');
				buf.append(command.toString());
				buf.append('\"');
			} else {
				buf.append(command.toString());
			}
		}	
		return buf.toString();
	}	
	
	/**
	 * Adds the array of {@link String}s to the given {@link List}
	 * @param args the strings
	 * @param v the list
	 */
	protected void addArguments(String[] args, List<String> v) {
		if (args == null) {
			return;
		}
		for (int i= 0; i < args.length; i++) {
			v.add(args[i]);
		}
	}
	
	/**
	 * Returns the working directory to use for the launched VM,
	 * or <code>null</code> if the working directory is to be inherited
	 * from the current process.
	 * 
	 * @param config the VM configuration
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
			abort(NLS.bind(LaunchingMessages.StandardVMRunner_Specified_working_directory_does_not_exist_or_is_not_a_directory___0__3, new String[] {path}), null, IJavaLaunchConfigurationConstants.ERR_WORKING_DIRECTORY_DOES_NOT_EXIST); 
		}
		return dir;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.AbstractVMRunner#getPluginIdentifier()
	 */
	@Override
	protected String getPluginIdentifier() {
		return LaunchingPlugin.getUniqueIdentifier();
	}
	
	/**
	 * Construct and return a String containing the full path of a java executable
	 * command such as 'java' or 'javaw.exe'.  If the configuration specifies an
	 * explicit executable, that is used.
	 * 
	 * @param config the runner configuration
	 * @return full path to java executable
	 * @exception CoreException if unable to locate an executable
	 */
	protected String constructProgramString(VMRunnerConfiguration config) throws CoreException {

		// Look for the user-specified java executable command
		String command= null;
		Map<String, Object> map= config.getVMSpecificAttributesMap();
		if (map != null) {
			command = (String) map.get(IJavaLaunchConfigurationConstants.ATTR_JAVA_COMMAND);
		}
		
		// If no java command was specified, use default executable
		if (command == null) {
			File exe = null;
			if (fVMInstance instanceof StandardVM) {
				exe = ((StandardVM)fVMInstance).getJavaExecutable();
			} else {
				exe = StandardVMType.findJavaExecutable(fVMInstance.getInstallLocation());
			}
			if (exe == null) {
				abort(NLS.bind(LaunchingMessages.StandardVMRunner_Unable_to_locate_executable_for__0__1, new String[]{fVMInstance.getName()}), null, IJavaLaunchConfigurationConstants.ERR_INTERNAL_ERROR); 
			} else {
				return exe.getAbsolutePath();
			}
		}
				
		// Build the path to the java executable.  First try 'bin', and if that
		// doesn't exist, try 'jre/bin'
		String installLocation = fVMInstance.getInstallLocation().getAbsolutePath() + File.separatorChar;
		File exe = new File(installLocation + "bin" + File.separatorChar + command); //$NON-NLS-1$ 		
		if (fileExists(exe)){
			return exe.getAbsolutePath();
		}
		exe = new File(exe.getAbsolutePath() + ".exe"); //$NON-NLS-1$
		if (fileExists(exe)){
			return exe.getAbsolutePath();
		}
		exe = new File(installLocation + "jre" + File.separatorChar + "bin" + File.separatorChar + command); //$NON-NLS-1$ //$NON-NLS-2$
		if (fileExists(exe)) {
			return exe.getAbsolutePath(); 
		}
		exe = new File(exe.getAbsolutePath() + ".exe"); //$NON-NLS-1$
		if (fileExists(exe)) {
			return exe.getAbsolutePath(); 
		}		

		
		// not found
		abort(NLS.bind(LaunchingMessages.StandardVMRunner_Specified_executable__0__does_not_exist_for__1__4, new String[]{command, fVMInstance.getName()}), null, IJavaLaunchConfigurationConstants.ERR_INTERNAL_ERROR); 
		// NOTE: an exception will be thrown - null cannot be returned
		return null;		
	}	
	
	/**
	 * Convenience method to determine if the specified file exists or not
	 * @param file the file to check
	 * @return true if the file indeed exists, false otherwise
	 */
	protected boolean fileExists(File file) {
		return file.exists() && file.isFile();
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
	 * This method is used to ensure that the JVM file encoding matches that of the console preference for file encoding.
	 * If the user explicitly declares a file encoding in the launch configuration, then that file encoding is used.
	 * 
	 * @param launch the {@link Launch}
	 * @param vmargs the original listing of JVM arguments
	 * @return the listing of JVM arguments including file encoding if one was not specified
	 * 
	 * @since 3.4
	 */
	protected String[] ensureEncoding(ILaunch launch, String[] vmargs) {
		boolean foundencoding = false;
		for(int i = 0; i < vmargs.length; i++) {
			if(vmargs[i].startsWith("-Dfile.encoding=")) { //$NON-NLS-1$
				foundencoding = true; 
			}
		}
		if(!foundencoding) {
			String encoding = launch.getAttribute(DebugPlugin.ATTR_CONSOLE_ENCODING);
			if(encoding == null) {
				return vmargs;
			}
			String[] newargs = new String[vmargs.length+1];
			System.arraycopy(vmargs, 0, newargs, 0, vmargs.length);
			newargs[newargs.length-1] = "-Dfile.encoding="+encoding; //$NON-NLS-1$
			return newargs;
		}
		return vmargs;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IVMRunner#run(org.eclipse.jdt.launching.VMRunnerConfiguration, org.eclipse.debug.core.ILaunch, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void run(VMRunnerConfiguration config, ILaunch launch, IProgressMonitor monitor) throws CoreException {

		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		
		IProgressMonitor subMonitor = new SubProgressMonitor(monitor, 1);
		subMonitor.beginTask(LaunchingMessages.StandardVMRunner_Launching_VM____1, 2); 
		subMonitor.subTask(LaunchingMessages.StandardVMRunner_Constructing_command_line____2); 
		
		String program= constructProgramString(config);
		
		List<String> arguments= new ArrayList<String>();
		arguments.add(program);
				
		// VM args are the first thing after the java program so that users can specify
		// options like '-client' & '-server' which are required to be the first option
		String[] allVMArgs = combineVmArgs(config, fVMInstance);
		addArguments(ensureEncoding(launch, allVMArgs), arguments);
		
		addBootClassPathArguments(arguments, config);
		
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
		
		String[] envp = prependJREPath(config.getEnvironment());
		
		subMonitor.worked(1);

		// check for cancellation
		if (monitor.isCanceled()) {
			return;
		}
		
		subMonitor.subTask(LaunchingMessages.StandardVMRunner_Starting_virtual_machine____3); 
		Process p= null;
		File workingDir = getWorkingDir(config);
		p= exec(cmdLine, workingDir, envp);
		if (p == null) {
			return;
		}
		
		// check for cancellation
		if (monitor.isCanceled()) {
			p.destroy();
			return;
		}		
		
		IProcess process= newProcess(launch, p, renderProcessLabel(cmdLine), getDefaultProcessMap());
		process.setAttribute(IProcess.ATTR_CMDLINE, renderCommandLine(cmdLine));
		subMonitor.worked(1);
		subMonitor.done();
	}
	
	/**
	 * Prepends the correct java version variable state to the environment path for Mac VMs
	 * 
	 * @param env the current array of environment variables to run with
	 * @return the new path segments
	 * @since 3.3
	 */
	protected String[] prependJREPath(String[] env) {
		if (Platform.OS_MACOSX.equals(Platform.getOS())) {
			if (fVMInstance instanceof IVMInstall2) {
				IVMInstall2 vm = (IVMInstall2) fVMInstance;
				String javaVersion = vm.getJavaVersion();
				if (javaVersion != null) {
					if (env == null) {
						Map<String, String> map = DebugPlugin.getDefault().getLaunchManager().getNativeEnvironmentCasePreserved();
						if (map.containsKey(StandardVMDebugger.JAVA_JVM_VERSION)) {
							String[] env2 = new String[map.size()];
							Iterator<Entry<String, String>> iterator = map.entrySet().iterator();
							int i = 0;
							while (iterator.hasNext()) {
								Entry<String, String> entry = iterator.next();
								String key = entry.getKey();
								if (StandardVMDebugger.JAVA_JVM_VERSION.equals(key)) {
									env2[i] = key + "=" + javaVersion; //$NON-NLS-1$
								} else {
									env2[i] = key + "=" + entry.getValue(); //$NON-NLS-1$
								}
								i++;
							}
							env = env2;
						}
					} else {
						for (int i = 0; i < env.length; i++) {
							String string = env[i];
							if (string.startsWith(StandardVMDebugger.JAVA_JVM_VERSION)) {
								env[i]=StandardVMDebugger.JAVA_JVM_VERSION+"="+javaVersion; //$NON-NLS-1$
								break;
							}
						}
					}
				}
			}
		}
		return env;
	}

	/**
	 * Adds arguments to the bootpath
	 * @param arguments the arguments
	 * @param config the VM config
	 */
	protected void addBootClassPathArguments(List<String> arguments, VMRunnerConfiguration config) {
		String[] prependBootCP= null;
		String[] bootCP= null;
		String[] appendBootCP= null;
		Map<String, Object> map = config.getVMSpecificAttributesMap();
		if (map != null) {
			prependBootCP= (String[]) map.get(IJavaLaunchConfigurationConstants.ATTR_BOOTPATH_PREPEND);
			bootCP= (String[]) map.get(IJavaLaunchConfigurationConstants.ATTR_BOOTPATH);
			appendBootCP= (String[]) map.get(IJavaLaunchConfigurationConstants.ATTR_BOOTPATH_APPEND);
		}
		if (prependBootCP == null && bootCP == null && appendBootCP == null) {
			// use old single attribute instead of new attributes if not specified
			bootCP = config.getBootClassPath();
		}
		if (prependBootCP != null) {
			arguments.add("-Xbootclasspath/p:" + convertClassPath(prependBootCP)); //$NON-NLS-1$
		}
		if (bootCP != null) {
			if (bootCP.length > 0) {
				arguments.add("-Xbootclasspath:" + convertClassPath(bootCP)); //$NON-NLS-1$
			}
		}
		if (appendBootCP != null) {
			arguments.add("-Xbootclasspath/a:" + convertClassPath(appendBootCP)); //$NON-NLS-1$
		}
	}

}
