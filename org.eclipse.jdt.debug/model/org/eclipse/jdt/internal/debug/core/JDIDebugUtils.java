package org.eclipse.jdt.internal.debug.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

public class JDIDebugUtils {

	/**
	 * Returns a collection of <code>String</code>s representing
	 * the qualified type names of the given resources. The qualified
	 * names are returned dot separated.
	 * <p>
	 * This method takes into account the output directory of 
	 * Java projects.
	 */
	protected static List getQualifiedNames(List resources) {
		List qualifiedNames= new ArrayList(resources.size());
		Iterator itr= resources.iterator();
		IProject project = null;
		IPath outputPath = null;
		IJavaProject javaProject = null;
		while (itr.hasNext()) {
			IResource resource= (IResource) itr.next();
			if (project == null || !resource.getProject().equals(project)) {
				project= resource.getProject();
				javaProject= JavaCore.create(project);
				try {
					outputPath= javaProject.getOutputLocation();
				} catch (JavaModelException e) {
					JDIDebugPlugin.logError(e);
					project = null;
					continue;
				}
			}
			IPath resourcePath= resource.getFullPath();
			int count= resourcePath.matchingFirstSegments(outputPath);
			resourcePath= resourcePath.removeFirstSegments(count);
			String pathString= resourcePath.toString();
			pathString= translateResourceName(pathString);
			qualifiedNames.add(pathString);
		}
		return qualifiedNames;
	}
	
	/**
	 * Translates the given resourceName, which is of the form:
	 * 	foo/bar/baz.class
	 * the form:
	 * 	foo.bar.baz
	 */
	private static String translateResourceName(String resourceName) {
		// get rid of ".class"
		resourceName= resourceName.substring(0, resourceName.length() - 6);
		// switch to dot separated
		return resourceName.replace(IPath.SEPARATOR, '.');
	}

}

