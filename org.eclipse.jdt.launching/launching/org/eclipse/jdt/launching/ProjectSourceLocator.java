/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.jdt.launching;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.core.model.IStackFrame;

import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import org.eclipse.jdt.debug.core.IJavaStackFrame;

import org.eclipse.jdt.internal.launching.LaunchingPlugin;


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
 */
public class ProjectSourceLocator implements ISourceLocator {

	private ArrayList fProjects;
	private IJavaProject fCustomSearchPathProject;
	
	private static boolean fgCustomSearchPathChanged;
	
	private static final String ADDITIONAL_LOOKUP_PROPERTY= "additional_lookup"; //$NON-NLS-1$

	/**
	 * Constructs a new ProjectSourceLocator for a project. Uses the custom search path for sources, if
	 * set.
	 */
	public ProjectSourceLocator(IJavaProject project) {
		fCustomSearchPathProject= project;
		fgCustomSearchPathChanged= true;
		
		fProjects= new ArrayList();
		try {
			updateCustomSearchPath(project, fProjects);

		} catch (JavaModelException e) {
			LaunchingPlugin.log(e);
		}
	}
	
	private void updateCustomSearchPath(IJavaProject jproject, ArrayList list) throws JavaModelException {
		if (fgCustomSearchPathChanged) {
			list.clear();
			IJavaProject[] projects= getSourceLookupPath(jproject);
			if (projects != null) {
				fProjects.addAll(Arrays.asList(projects));
			} else {
				collectRequiredProjects(jproject, fProjects);
			}
			fgCustomSearchPathChanged= false;
		}
	}
	
	/**
	 * Constructs a new ProjectSourceLocator for a specified set of projects.
	 * Does not use the custom search path settings
	 */
	public ProjectSourceLocator(IJavaProject[] projects, boolean includeRequired) throws JavaModelException {
		fCustomSearchPathProject= null;
		
		fProjects= new ArrayList();
		for (int i= 0; i < projects.length; i++) {
			if (includeRequired) {
				collectRequiredProjects(projects[i], fProjects);
			} else {
				if (!fProjects.contains(projects[i])) {
					fProjects.add(projects[i]);
				}
			}
		}
	}
	
	private void collectRequiredProjects(IJavaProject proj, ArrayList res) throws JavaModelException {
		if (!res.contains(proj)) {
			res.add(proj);
			
			IJavaModel model= proj.getJavaModel();
			
			IClasspathEntry[] entries= proj.getRawClasspath();
			for (int i= 0; i < entries.length; i++) {
				IClasspathEntry curr= entries[i];
				if (curr.getEntryKind() == IClasspathEntry.CPE_PROJECT) {
					IJavaProject ref= model.getJavaProject(curr.getPath().segment(0));
					if (ref.exists()) {
						collectRequiredProjects(ref, res);
					}
				}
			}
		}
	}
	
	/*
	 *@see ISourceLocator#getSourceElement
	 */
	public Object getSourceElement(IStackFrame stackFrame) {
		IJavaStackFrame frame= (IJavaStackFrame)stackFrame.getAdapter(IJavaStackFrame.class);
		if (frame != null) {
			try {
				if (fCustomSearchPathProject != null) {
					updateCustomSearchPath(fCustomSearchPathProject, fProjects);
				}
				String name= frame.getDeclaringTypeName();
				for (int i= 0; i < fProjects.size(); i++) {
					IJavaProject curr= (IJavaProject) fProjects.get(i);
					IJavaElement openable= findOpenable(curr, name);
					if (openable != null) {
						return openable;
					}
				}
			} catch (CoreException e) {
				LaunchingPlugin.log(e);
			}
		}
		return null;
	}
		
	/** 
	 * Finds the classfile / compilation unit for the declaring type name.
	 * The type name is in debug format and can contain '$' for a inner type
	 */
	private static IJavaElement findOpenable(IJavaProject jproject, String typeName) throws JavaModelException {
		String pathStr= typeName.replace('.', '/') + ".java"; //$NON-NLS-1$
		IJavaElement jelement= jproject.findElement(new Path(pathStr));
		if (jelement == null) {
			// maybe an inner type
			int dotIndex= pathStr.lastIndexOf('/');
			int dollarIndex= pathStr.indexOf('$', dotIndex + 1);
			if (dollarIndex != -1) {
				jelement= jproject.findElement(new Path(pathStr.substring(0, dollarIndex) + ".java")); //$NON-NLS-1$
			}
		}

		return jelement;
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
}