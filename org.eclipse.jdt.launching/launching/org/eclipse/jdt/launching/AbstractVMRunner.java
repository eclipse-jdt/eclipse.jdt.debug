package org.eclipse.jdt.launching;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.IStatusHandler;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.launching.LaunchingPlugin;

/**
 * Common function for VM runners.
 * <p>
 * This class is intended to be subclassed.
 * </p>
 * <p>
 * Note: This class/interface is part of an interim API that is still under development and expected to 
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback 
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken
 * (repeatedly) as the API evolves.
 * </p>
 * @see IVMRunner
 * @see IJavaLaunchConfigurationConstants
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
	 * Returns the main type name specified by the given 
	 * launch configuration, or <code>null</code> if none.
	 * 
	 * @param configuration launch configuration
	 * @return the main type name specified by the given 
	 *  launch configuration, or <code>null</code> if none
	 * @exception CoreException if unable to retrieve the attribute
	 */
	protected String getMainTypeName(ILaunchConfiguration configuration) throws CoreException {
		return configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, (String)null);
	}
	
	/**
	 * Verifies a main type name is specified by the given 
	 * launch configuration, and returns the main type name.
	 * 
	 * @param configuration launch configuration
	 * @return the main type name specified by the given 
	 *  launch configuration
	 * @exception CoreException if unable to retrieve the attribute
	 * 	or the attribute is unspecified
	 */
	protected String verifyMainTypeName(ILaunchConfiguration configuration) throws CoreException {
		String name = getMainTypeName(configuration);
		if (name == null) {
			abort("Main type not specified.", null, IJavaLaunchConfigurationConstants.ERR_UNSPECIFIED_MAIN_TYPE);
		}
		return name;
	}
	
	/**
	 * Returns the Java project name specified by the given 
	 * launch configuration, or <code>null</code> if none.
	 * 
	 * @param configuration launch configuration
	 * @return the Java project name specified by the given 
	 *  launch configuration, or <code>null</code> if none
	 * @exception CoreException if unable to retrieve the attribute
	 */
	protected String getJavaProjectName(ILaunchConfiguration configuration) throws CoreException {
		return configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, (String)null);
	}
		
	/**
	 * Returns the Java project specified by the given 
	 * launch configuration, or <code>null</code> if none.
	 * 
	 * @param configuration launch configuration
	 * @return the Java project specified by the given 
	 *  launch configuration, or <code>null</code> if none
	 * @exception CoreException if unable to retrieve the attribute
	 */
	protected IJavaProject getJavaProject(ILaunchConfiguration configuration) throws CoreException {
		String projectName = getJavaProjectName(configuration);
		if (projectName != null) {
			projectName = projectName.trim();
			if (projectName.length() > 0) {
				IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
				IJavaProject javaProject = JavaCore.create(project);
				if (javaProject != null && javaProject.exists()) {
					return javaProject;
				}
			}
		}
		return null;
	}
	
	/**
	 * Verifies a Java project is specified by the given 
	 * launch configuration, and returns the Java project.
	 * 
	 * @param configuration launch configuration
	 * @return the Java project specified by the given 
	 *  launch configuration
	 * @exception CoreException if unable to retrieve the attribute
	 * 	or the attribute is unspecified
	 */
	protected IJavaProject verifyJavaProject(ILaunchConfiguration configuration) throws CoreException {
		String name = getJavaProjectName(configuration);
		if (name == null) {
			abort("Java project not specified.", null, IJavaLaunchConfigurationConstants.ERR_UNSPECIFIED_PROJECT);
		}
		IJavaProject project = getJavaProject(configuration);
		if (project == null) {
			abort("Project does not exist, or is not a Java project.", null, IJavaLaunchConfigurationConstants.ERR_NOT_A_JAVA_PROJECT);
		}
		return project;
	}
	
		
		
		
		
		
		
	/**
	 * Returns the working directory path specified by
	 * the given launch configuration, or <code>null</code> if none.
	 * 
	 * @param configuration launch configuration
	 * @return the working directory path specified by the given 
	 *  launch configuration, or <code>null</code> if none
	 * @exception CoreException if unable to retrieve the attribute
	 */
	protected String getWorkingDirectoryPath(ILaunchConfiguration configuration) throws CoreException {
		return configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, (String)null);
	}
	
	/**
	 * Returns the working directory specified by
	 * the given launch configuration, or <code>null</code> if none.
	 * 
	 * @param configuration launch configuration
	 * @return the working directory specified by the given 
	 *  launch configuration, or <code>null</code> if none
	 * @exception CoreException if unable to retrieve the attribute
	 */
	protected File getWorkingDirectory(ILaunchConfiguration configuration) throws CoreException {
		String path = getWorkingDirectoryPath(configuration);
		if (path != null) {
			File dir = new File(path);
			if (dir.isDirectory()) {
				return dir;
			}
		}
		return null;
	}
	
	/**
	 * Verifies the working directory specified by the given 
	 * launch configuration exists, and returns the working
	 * directory, or <code>null</code> if none is specified.
	 * 
	 * @param configuration launch configuration
	 * @return the working directory specified by the given 
	 *  launch configuration, or <code>null</code> if none
	 * @exception CoreException if unable to retrieve the attribute
	 */	
	protected File verifyWorkingDirectory(ILaunchConfiguration configuration) throws CoreException {
		String path = getWorkingDirectoryPath(configuration);
		if (path != null) {
			File dir = new File(path);
			if (!dir.isDirectory()) {
				abort(MessageFormat.format("Working directory does not exist: {0}", new String[] {path}), null, IJavaLaunchConfigurationConstants.ERR_WORKING_DIRECTORY_DOES_NOT_EXIST);
			}
			return dir;
		}
		return null;
	}	
	
	/**
	 * Returns the program arguments specified by the given launch
	 * configuration, as a string. The returned string is empty if
	 * no program arguments are specified.
	 * 
	 * @param configuration launch configuration
	 * @return the program arguments specified by the given 
	 *  launch configuration, possibly an empty string
	 * @exception CoreException if unable to retrieve the attribute
	 */
	protected String getProgramArguments(ILaunchConfiguration configuration) throws CoreException {
		return configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, "");
	}
	
	/**
	 * Returns the program arguments specified by the given launch
	 * configuration, as an array of Strings. The returned array
	 * is empty if no program arguments are specified.
	 * 
	 * @param configuration launch configuration
	 * @return the program arguments specified by the given 
	 *  launch configuration, possibly an empty array
	 * @exception CoreException if unable to retrieve the attribute
	 */
	protected String[] getProgramArgumentsArray(ILaunchConfiguration configuration) throws CoreException {
		return parseArguments(getProgramArguments(configuration));
	}	
	
	/**
	 * Returns the VM arguments specified by the given launch
	 * configuration, as a string. The returned string is empty if
	 * no VM arguments are specified.
	 * 
	 * @param configuration launch configuration
	 * @return the VM arguments specified by the given 
	 *  launch configuration, possibly an empty string
	 * @exception CoreException if unable to retrieve the attribute
	 */
	protected String getVMArguments(ILaunchConfiguration configuration) throws CoreException {
		return configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, "");
	}
	
	/**
	 * Returns the VM arguments specified by the given launch
	 * configuration, as an array of Strings. The returned array
	 * is empty if no VM arguments are specified.
	 * 
	 * @param configuration launch configuration
	 * @return the VM arguments specified by the given 
	 *  launch configuration, possibly an empty array
	 * @exception CoreException if unable to retrieve the attribute
	 */
	protected String[] getVMArgumentsArray(ILaunchConfiguration configuration) throws CoreException {
		return parseArguments(getVMArguments(configuration));
	}	
	
	/**
	 * Returns the classpath specified by the given launch
	 * configuration, as an array of Strings. The returned array
	 * is empty if no classpath is specified.
	 * 
	 * @param configuration launch configuration
	 * @return the classpath specified by the given 
	 *  launch configuration, possibly an empty array
	 * @exception CoreException if unable to retrieve the attribute
	 */	
	protected String[] getClasspath(ILaunchConfiguration configuration) throws CoreException {
		List classpathList = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH, (List)null);
		String[] classpath;
		if (classpathList == null) {
			IJavaProject project = getJavaProject(configuration);
			if (project == null) {
				classpath = new String[0];
			} else {
				classpath = JavaRuntime.computeDefaultRuntimeClassPath(project);
			}
		} else {
			classpath = new String[classpathList.size()];
			classpathList.toArray(classpath);
		}
		return classpath;
	}
	
	/**
	 * Returns the bootpath specified by the given launch
	 * configuration, as an array of Strings. The returned array
	 * is empty if no bootpath is specified.
	 * 
	 * @param configuration launch configuration
	 * @return the bootpath specified by the given 
	 *  launch configuration, possibly an empty array
	 * @exception CoreException if unable to retrieve the attribute
	 */	
	protected String[] getBootpath(ILaunchConfiguration configuration) throws CoreException {
		List bootpathList = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_BOOTPATH, (List)null);
		String[] bootpath = null;
		if (bootpathList != null) {
			bootpath = new String[bootpathList.size()];
			bootpathList.toArray(bootpath);
		} else {
			bootpath = new String[0];
		}
		return bootpath;
	}
	
	/**
	 * Performs a runtime exec on the given command line in the context
	 * of the specified working directory and returns the resulting process.
	 * If the current runtime does not support the specification of a working
	 * directory, the status handler for error code
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
				abort("Exception starting process.", e, IJavaLaunchConfigurationConstants.ERR_INTERNAL_ERROR);
		} catch (NoSuchMethodError e) {
			//attempting launches on 1.2.* - no ability to set working directory
			
			IStatus status = new Status(IStatus.ERROR, LaunchingPlugin.PLUGIN_ID, IJavaLaunchConfigurationConstants.ERR_WORKING_DIRECTORY_NOT_SUPPORTED, "Eclipse runtime does not support working directory.", e);
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
			
	private static class ArgumentParser {
		private String fArgs;
		private int fIndex= 0;
		private int ch= -1;
		
		public ArgumentParser(String args) {
			fArgs= args;
		}
		
		private int getNext() {
			if (fIndex < fArgs.length())
				return fArgs.charAt(fIndex++);
			return -1;
		}
		
		public String[] parseArguments() {
			ArrayList v= new ArrayList();
			
			ch= getNext();
			while (ch > 0) {
				while (Character.isWhitespace((char)ch))
					ch= getNext();	
				
				if (ch == '"') {
					v.add(parseString());
				} else {
					v.add(parseToken());
				}
			}
	
			String[] result= new String[v.size()];
			v.toArray(result);
			return result;
		}
		
		public String parseString() {
			StringBuffer buf= new StringBuffer();
			buf.append((char)ch);
			ch= getNext();
			while (ch > 0 && ch != '"') {
				buf.append((char)ch);
				ch= getNext();
			}
			if (ch > 0)
				buf.append((char)ch);
			ch= getNext();
				
			return buf.toString();
		}
		
		public String parseToken() {
			StringBuffer buf= new StringBuffer();
			
			while (ch > 0 && !Character.isWhitespace((char)ch)) {
				if (ch == '"')
					buf.append(parseString());
				else {
					buf.append((char)ch);
					ch= getNext();
				}
			}
			return buf.toString();
		}
	}
	
	protected static String[] parseArguments(String args) {
		if (args == null)
			return new String[0];
		ArgumentParser parser= new ArgumentParser(args);
		String[] res= parser.parseArguments();
		
		return res;
	}
}
