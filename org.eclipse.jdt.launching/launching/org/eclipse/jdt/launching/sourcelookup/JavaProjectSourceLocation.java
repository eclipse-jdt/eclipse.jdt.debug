package org.eclipse.jdt.launching.sourcelookup;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
 
/**
 * Locates source elements in a Java project. Returns
 * instances of <code>ICompilationUnit</code> and
 * </code>IClassFile</code>.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 * <p>
 * Note: This class/interface is part of an interim API that is still under development and expected to 
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback 
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken
 * (repeatedly) as the API evolves.
 * </p>
 * @see IJavaSourceLocation
 */
public class JavaProjectSourceLocation implements IJavaSourceLocation {

	/**
	 * The project associated with this source location
	 */
	private IJavaProject fProject;
	
	/**
	 * Constructs a new source location that will retrieve source
	 * elements from the given Java project.
	 * 
	 * @param project Java project
	 */
	public JavaProjectSourceLocation(IJavaProject project) {
		setJavaProject(project);
	}
	
	/**
	 * @see IJavaSourceLocation#findSourceElement(String)
	 */
	public Object findSourceElement(String name) throws CoreException {
		String pathStr= name.replace('.', '/') + ".java"; //$NON-NLS-1$
		IJavaElement jelement= getJavaProject().findElement(new Path(pathStr));
		if (jelement == null) {
			// maybe an inner type
			int dotIndex= pathStr.lastIndexOf('/');
			int dollarIndex= pathStr.indexOf('$', dotIndex + 1);
			if (dollarIndex != -1) {
				jelement= getJavaProject().findElement(new Path(pathStr.substring(0, dollarIndex) + ".java")); //$NON-NLS-1$
			}
		}
		return jelement;
	}

	/**
	 * Sets the Java project in which source elements will
	 * be searched for.
	 * 
	 * @param project Java project
	 */
	private void setJavaProject(IJavaProject project) {
		fProject = project;
	}
	
	/**
	 * Returns the Java project associated with this source
	 * location.
	 * 
	 * @return Java project
	 */
	public IJavaProject getJavaProject() {
		return fProject;
	}
}
