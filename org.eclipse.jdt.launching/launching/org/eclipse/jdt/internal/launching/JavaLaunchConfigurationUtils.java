package org.eclipse.jdt.internal.launching;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.sourcelookup.ArchiveSourceLocation;
import org.eclipse.jdt.launching.sourcelookup.DirectorySourceLocation;
import org.eclipse.jdt.launching.sourcelookup.IJavaSourceLocation;
import org.eclipse.jdt.launching.sourcelookup.JavaProjectSourceLocation;

/**
 * This class contains a number of static helper methods useful for the 'local java' delegate.
 */
public class JavaLaunchConfigurationUtils {
																		 	
	/**
	 * Return the <code>IResource</code> that contains the main type referenced by the
	 * specified configuration or throw a <code>CoreException</code> whose message explains
	 * why this couldn't be done.
	 */
	public static IResource getMainTypeResource(ILaunchConfiguration configuration) throws CoreException{
		IType mainType = getMainType(configuration);
		return mainType.getUnderlyingResource();
	}
	
	/**
	 * Return the <code>IType</code> referenced in the specified configuration or throw a 
	 * <code>CoreException</code> whose message explains why this couldn't be done.
	 */
	public static IType getMainType(ILaunchConfiguration configuration) throws CoreException {
		return getMainType(configuration, getJavaProject(configuration));
	}
	
	/**
	 * Return the <code>IJavaProject</code> referenced in the specified configuration or throw a
	 * <code>CoreException</code> whose message explains why this couldn't be done.
	 */
	public static IJavaProject getJavaProject(ILaunchConfiguration configuration) throws CoreException {
		String projectName = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, (String)null);
		if ((projectName == null) || (projectName.trim().length() < 1)) {
			abort("No project specified", null, IJavaLaunchConfigurationConstants.ERR_UNSPECIFIED_PROJECT);
		}			
		IJavaProject javaProject = getJavaModel().getJavaProject(projectName);		
		if ((javaProject == null) || !javaProject.exists()) {
			abort("Invalid project specified", null, IJavaLaunchConfigurationConstants.ERR_NOT_A_JAVA_PROJECT);
		}
		return javaProject;
	}
	
	/**
	 * Return the <code>IType</code> referenced in the specified configuration and contained in 
	 * the specified project or throw a <code>CoreException</code> whose message explains why 
	 * this couldn't be done.
	 */
	public static IType getMainType(ILaunchConfiguration configuration, IJavaProject javaProject) throws CoreException {
		String mainTypeName = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, (String)null);
		return getMainType(mainTypeName, javaProject);
	}
	
	/**
	 * Return the <code>IType</code> referenced by the specified name and contained in 
	 * the specified project or throw a <code>CoreException</code> whose message explains why 
	 * this couldn't be done.
	 */
	public static IType getMainType(String mainTypeName, IJavaProject javaProject) throws CoreException {
		if ((mainTypeName == null) || (mainTypeName.trim().length() < 1)) {
			abort("Main type not specified.", null, IJavaLaunchConfigurationConstants.ERR_UNSPECIFIED_MAIN_TYPE);
		}
		IType mainType = null;
		try {
			mainType = findType(javaProject, mainTypeName);
		} catch (JavaModelException jme) {
		}
		if (mainType == null) {
			abort("Main type does not exist.", null, IJavaLaunchConfigurationConstants.ERR_UNSPECIFIED_MAIN_TYPE);
		}
		return mainType;
	}	
	
	/**
	 * Find the specified (fully-qualified) type name in the specified java project.
	 */
	public static IType findType(IJavaProject javaProject, String mainTypeName) throws JavaModelException {
		String pathStr= mainTypeName.replace('.', '/') + ".java"; //$NON-NLS-1$
		IJavaElement javaElement= javaProject.findElement(new Path(pathStr));
		if (javaElement == null) {
			return null;
		} else if (javaElement instanceof IType) {
			return (IType)javaElement;
		} else if (javaElement.getElementType() == IJavaElement.COMPILATION_UNIT) {
			String simpleName= Signature.getSimpleName(mainTypeName);
			return ((ICompilationUnit) javaElement).getType(simpleName);
		} else if (javaElement.getElementType() == IJavaElement.CLASS_FILE) {
			return ((IClassFile) javaElement).getType();
		}
		return null; 
	}
		
	/**
	 * Throws a core exception with the given message and optional
	 * exception. The exception's status code will indicate an error.
	 * 
	 * @param message error message
	 * @param exception cause of the error, or <code>null</code>
	 * @exception CoreException with the given message and underlying
	 *  exception
	 */
	protected static void abort(String message, Throwable exception, int code) throws CoreException {
		throw new CoreException(new Status(IStatus.ERROR, LaunchingPlugin.PLUGIN_ID,
		  code, message, exception));
	}
	
	/**
	 * Convenience method to get the <code>ILaunchConfigurationType</code> this helper is concerned with.
	 */
	private static ILaunchConfigurationType getConfigurationType() {
		return getLaunchManager().getLaunchConfigurationType("org.eclipse.jdt.debug.ui.localJavaApplication");	//$NON-NLS-1$	
	}
	
	/**
	 * Convenience method to get the launch mgr.
	 */
	private static ILaunchManager getLaunchManager() {
		return DebugPlugin.getDefault().getLaunchManager();
	}
	
	/**
	 * Convenience method to get the java model.
	 */
	private static IJavaModel getJavaModel() {
		return JavaCore.create(getWorkspaceRoot());
	}

	/**
	 * Convenience method to get the workspace root.
	 */
	private static IWorkspaceRoot getWorkspaceRoot() {
		return ResourcesPlugin.getWorkspace().getRoot();
	}
	
	public static IJavaSourceLocation[] decodeSourceLocations(String property) throws IOException {
		BufferedReader reader= new BufferedReader(new StringReader(property));
		ArrayList locations= new ArrayList();
		String line= reader.readLine();
		while (line != null && line.length() > 0) {
			IJavaSourceLocation location = null;
			if (line.equals(JavaProjectSourceLocation.class.getName())) {
				// next line is a project name
				line = reader.readLine();
				IJavaProject proj = (IJavaProject)JavaCore.create(line);
				if (proj != null) {
					location = new JavaProjectSourceLocation(proj);
				}
			} else if (line.equals(DirectorySourceLocation.class.getName())) {
				// next line is directory name
				line = reader.readLine();
				File file = new File(line);
				if (file.exists() && file.isDirectory()) {
					location = new DirectorySourceLocation(file);
				}
			} else if (line.equals(ArchiveSourceLocation.class.getName())) {
				// next two lines are zip file and source root
				String zipName = reader.readLine();
				String root = reader.readLine();
				location = new ArchiveSourceLocation(zipName, root);
			}
			if (location != null)
				locations.add(location);
			line= reader.readLine();
		}
		return (IJavaSourceLocation[]) locations.toArray(new IJavaSourceLocation[locations.size()]);
	}	
	
	public static String encodeSourceLocations(IJavaSourceLocation[] locations) throws IOException {
		StringBuffer buf= new StringBuffer();
		for (int i= 0; i < locations.length; i++) {
			buf.append(locations[i].getClass().getName());
			buf.append('\n');
			if (locations[i] instanceof JavaProjectSourceLocation) {
				JavaProjectSourceLocation location = (JavaProjectSourceLocation)locations[i];
				buf.append(location.getJavaProject().getHandleIdentifier());
			} else if (locations[i] instanceof DirectorySourceLocation) {
				DirectorySourceLocation location = (DirectorySourceLocation)locations[i];
				buf.append(location.getDirectory().getCanonicalPath());
			} else if (locations[i] instanceof ArchiveSourceLocation) {
				ArchiveSourceLocation location = (ArchiveSourceLocation)locations[i];
				buf.append(location.getArchive().getName());
				buf.append('\n');
				IPath root = location.getRootPath();
				if (root == null) {
					buf.append(" ");
				} else {
					buf.append(root.toString());
				}
			}
			buf.append('\n');
		}
		return buf.toString();		
	}	
}
