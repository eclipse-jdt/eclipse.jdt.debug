/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.jdt.launching;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;

import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.core.model.IStackFrame;

import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaModelStatusConstants;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import org.eclipse.jdt.debug.core.IJavaStackFrame;

import org.eclipse.jdt.internal.launching.JavaLaunchConfigurationHelper;
import org.eclipse.jdt.internal.launching.LaunchingPlugin;
import org.eclipse.jdt.launching.sourcelookup.ArchiveSourceLocation;
import org.eclipse.jdt.launching.sourcelookup.DirectorySourceLocation;
import org.eclipse.jdt.launching.sourcelookup.IJavaSourceLocation;
import org.eclipse.jdt.launching.sourcelookup.JavaProjectSourceLocation;
import org.eclipse.jdt.launching.sourcelookup.JavaSourceLocator;


/**
 * Standard source code locator for Java elements. This class will search the
 * build path of a project for source. If there is a custom search path defined
 * via <code>SourceLookupSettings</code>, it will be used instead of the build
 * classpath.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 * <p>
 * Note: This class/interface is part of an interim API that is still under development and expected to 
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback 
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken
 * (repeatedly) as the API evolves.
 * </p>
 *
 * @see org.eclipse.debug.core.model.ISourceLocator
 * @deprecated use org.eclipse.jdt.launching.sourcelookup.JavaSourceLocator
 */
public class ProjectSourceLocator extends JavaSourceLocator {

	private IJavaProject fCustomSearchPathProject;
	
	private static boolean fgCustomSearchPathChanged;
	
	/**
	 * Preference used in 1.0, replaced with
	 * <code>ADDITIONAL_LOCATIONS_PROPERTY</code>
	 */
	private static final String ADDITIONAL_LOOKUP_PROPERTY= "additional_lookup"; //$NON-NLS-1$

	/**
	 * Persistent property with a set of source locations
	 */	
	private static final String ADDITIONAL_LOCATIONS_PROPERTY= "additional_locations"; //$NON-NLS-1$

	/**
	 * Constructs a new ProjectSourceLocator for a project. Uses the custom search path for sources, if
	 * set.
	 * 
	 * @deprecated use org.eclipse.jdt.launching.sourcelookup.JavaSourceLocator
	 */
	public ProjectSourceLocator(IJavaProject project) {
		super(new IJavaSourceLocation[0]);
		fCustomSearchPathProject= project;
		fgCustomSearchPathChanged= true;
		try {
			updateCustomSearchPath(project);
		} catch (CoreException e) {
			LaunchingPlugin.log(e);
		}
	}
	
	private void updateCustomSearchPath(IJavaProject jproject) throws CoreException {
		if (fgCustomSearchPathChanged) {
			IJavaProject[] projects= getSourceLookupPath(jproject);
			IJavaSourceLocation[] locations = getPersistedSourceLocations(jproject);
			if (projects != null) {
				locations = asSourceLocations(projects);
				// replace the old preference with the new locations
				setPersistedSourceLocations(jproject, locations);
				setSourceLookupPath(jproject, null);
			} else if (locations == null) {
				locations = getDefaultSourceLocations(jproject);
			}
			setSourceLocations(locations);
			fgCustomSearchPathChanged= false;
		}
	}
	
	/**
	 * Returns source locations for the given projects.
	 * 
	 * @param projects list of java projects
	 * @return corresponding java project source locations
	 */
	private IJavaSourceLocation[] asSourceLocations(IJavaProject[] projects) {
		IJavaSourceLocation[] locations = new IJavaSourceLocation[projects.length];
		for (int i = 0; i < projects.length; i++) {
			locations[i] = new JavaProjectSourceLocation(projects[i]);
		}	
		return locations;
	}
	/**
	 * Constructs a new ProjectSourceLocator for a specified set of projects.
	 * Does not use the custom search path settings
	 * 
	 * @deprecated use org.eclipse.jdt.launching.sourcelookup.JavaSourceLocator
	 */
	public ProjectSourceLocator(IJavaProject[] projects, boolean includeRequired) throws JavaModelException {
		super(new IJavaSourceLocation[0]);
		fCustomSearchPathProject= null;
		
		ArrayList requiredProjects = new ArrayList();
		for (int i= 0; i < projects.length; i++) {
			if (includeRequired) {
				collectRequiredProjects(projects[i], requiredProjects);
			} else {
				if (!requiredProjects.contains(projects[i])) {
					requiredProjects.add(projects[i]);
				}
			}
		}
		setSourceLocations(asSourceLocations((IJavaProject[])requiredProjects.toArray(new IJavaProject[requiredProjects.size()])));
		
	}
			
	/**
	 * Get's a list of java project from a persistent property.
	 * @param project	The project you get the lookup path for
	 * @return 	Either an array of JavaProjects to be searched (including an empty array)
	 		or null if no lookup path has been set for the <code>project</code>.
	 * @throws JavaModelException	When access to the underlying <code>IProject</code> 
	 					fails or when the persisten property has a wrong format.
	 */
	public static IJavaProject[] getSourceLookupPath(IJavaProject project) throws JavaModelException {
		IProject p= project.getProject();
		try {
			String property= p.getPersistentProperty(new QualifiedName(LaunchingPlugin.PLUGIN_ID, ADDITIONAL_LOOKUP_PROPERTY));
			if (property == null)
				return null;
			return decodeProjects(property);
		} catch (CoreException e) {
			throw new JavaModelException(e);
		} catch (IOException e) {
			throw new JavaModelException(e, IStatus.ERROR);
		}
	}
	
	/**
	 * Returns a set of source locations persisted with the given
	 * project, or <code>null</code> if none.
	 * 
	 * @param project Java project
	 * @return source locations associated with the project,
	 * 	or <code>null</code> if none have been set
	 * @throws JavaModelException	When access to the underlying <code>IProject</code> 
	 *  fails or when the persistent property has an invalid format.
	 */
	public static IJavaSourceLocation[] getPersistedSourceLocations(IJavaProject project) throws JavaModelException {
		IProject p= project.getProject();
		try {
			String property= p.getPersistentProperty(new QualifiedName(LaunchingPlugin.PLUGIN_ID, ADDITIONAL_LOCATIONS_PROPERTY));
			if (property == null)
				return null;
			return JavaLaunchConfigurationHelper.decodeSourceLocations(property);
		} catch (CoreException e) {
			throw new JavaModelException(e);
		} catch (IOException e) {
			throw new JavaModelException(e, IJavaModelStatusConstants.IO_EXCEPTION);
		}
	}	
		
	/**
	 * Sets a array of <code>IJavaProject</code> to be used in debugger source
	 * lookup.
	 * @param project		The project to set the search path for
	 * @param projects	The projects that will be searched for source files.
	 * @throws JavaModelException	When access to the underlying <code>IProject</code> 
	 					fails.
	 */
	public static void setSourceLookupPath(IJavaProject project, IJavaProject[] projects) throws JavaModelException {
		fgCustomSearchPathChanged= true;
		IProject p= project.getProject();
		String property= null;
		if (projects != null)
			property= encodeProjects(projects);
		try {
			p.setPersistentProperty(new QualifiedName(LaunchingPlugin.PLUGIN_ID, ADDITIONAL_LOOKUP_PROPERTY), property);
		} catch (CoreException e) {
			throw new JavaModelException(e);
		}
	}
	
	/**
	 * Sets an array of <code>IJavaSourceLocation</code> to be used in debugger source
	 * lookup for the given project
	 * @param project The project to set the search path for
	 * @param locations The locations that will be searched for source files.
	 * @throws JavaModelException	When access to the underlying <code>IProject</code>
	 *  fails, or an exception occurs persisting the property
	 */
	public static void setPersistedSourceLocations(IJavaProject project, IJavaSourceLocation[] locations) throws JavaModelException {
		fgCustomSearchPathChanged= true;
		IProject p= project.getProject();
		String property= null;
		try {
			if (locations != null)
				property= JavaLaunchConfigurationHelper.encodeSourceLocations(locations);
			p.setPersistentProperty(new QualifiedName(LaunchingPlugin.PLUGIN_ID, ADDITIONAL_LOCATIONS_PROPERTY), property);
		} catch (CoreException e) {
			throw new JavaModelException(e);
		} catch (IOException e) {
			throw new JavaModelException(e, IJavaModelStatusConstants.IO_EXCEPTION);
		}
	}	
	
	private static IJavaProject[] decodeProjects(String property) throws IOException {
		BufferedReader reader= new BufferedReader(new StringReader(property));
		ArrayList jprojects= new ArrayList();
		String line= reader.readLine();
		while (line != null && line.length() > 0) {
			IJavaProject proj= (IJavaProject) JavaCore.create(line);
			if (proj != null)
				jprojects.add(proj);
			line= reader.readLine();
		}
		return (IJavaProject[]) jprojects.toArray(new IJavaProject[jprojects.size()]);
	}
	
	private static String encodeProjects(IJavaProject[] projects) {
		StringBuffer buf= new StringBuffer();
		for (int i= 0; i < projects.length; i++) {
			buf.append(projects[i].getHandleIdentifier());
			buf.append('\n');
		}
		return buf.toString();
	}
	
	/**
	 *@see ISourceLocator#getSourceElement
	 */
	public Object getSourceElement(IStackFrame stackFrame) {	
		if (fCustomSearchPathProject != null) {
			try {
				updateCustomSearchPath(fCustomSearchPathProject);
			} catch (CoreException e) {
				LaunchingPlugin.log(e);
			}
		}
		return super.getSourceElement(stackFrame);
		
	}
}