/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.launching.sourcelookup;


import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.launching.LaunchingMessages;
import org.eclipse.jdt.internal.launching.LaunchingPlugin;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
 
/**
 * Locates source elements in all source folders of the
 * given Java project. Returns instances of <code>ICompilationUnit</code>
 * and </code>IClassFile</code>.
 * <p>
 * This class may be instantiated.
 * </p>
 * @see IJavaSourceLocation
 * @since 2.0
 * @deprecated In 3.0, the debug platform provides source lookup facilities that
 *  should be used in place of the Java source lookup support provided in 2.0.
 *  The new facilities provide a source lookup director that coordinates source
 *  lookup among a set of participants, searching a set of source containers.
 *  See the following packages: <code>org.eclipse.debug.core.sourcelookup</code>
 *  and <code>org.eclipse.debug.core.sourcelookup.containers</code>. This class
 *  has been replaced by
 *  <code>org.eclipse.jdt.launching.sourcelookup.containers.JavaProjectSourceContainer</code>. 
 * @noextend This class is not intended to be sub-classed by clients.
 */
@Deprecated
public class JavaProjectSourceLocation extends PlatformObject implements IJavaSourceLocation {

	/**
	 * The project associated with this source location
	 */
	private IJavaProject fProject;
	
	/**
	 * Corresponding package fragment root locations.
	 */
	private IJavaSourceLocation[] fRootLocations = null;
	
	/**
	 * Constructs a new empty source location to be initialized
	 * by a memento.
	 */
	public JavaProjectSourceLocation() {
	}
	
	/**
	 * Constructs a new source location that will retrieve source
	 * elements from the given Java project.
	 * 
	 * @param project Java project
	 */
	public JavaProjectSourceLocation(IJavaProject project) {
		setJavaProject(project);
	}	
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.sourcelookup.IJavaSourceLocation#findSourceElement(java.lang.String)
	 */
	public Object findSourceElement(String name) throws CoreException {
		if (fRootLocations != null) {
			for (int i = 0; i < fRootLocations.length; i++) {
				Object element = fRootLocations[i].findSourceElement(name);
				if (element != null) {
					return element;
				}
			}
		}
		return null;
	}

	/**
	 * Sets the Java project in which source elements will
	 * be searched for.
	 * 
	 * @param project Java project
	 */
	private void setJavaProject(IJavaProject project) {
		fProject = project;
		fRootLocations = null;
		if (fProject != null) {
			try {
				IPackageFragmentRoot[] roots = project.getPackageFragmentRoots();
				ArrayList<PackageFragmentRootSourceLocation> list = new ArrayList<PackageFragmentRootSourceLocation>(roots.length);
				
				for (int i = 0; i < roots.length; i++) {
					if (roots[i].getKind() == IPackageFragmentRoot.K_SOURCE) {
						list.add(new PackageFragmentRootSourceLocation(roots[i]));
					}
				}
				fRootLocations = list.toArray(new IJavaSourceLocation[list.size()]);
			} catch (JavaModelException e) {
				LaunchingPlugin.log(e);
			}
		}
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
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object object) {		
		return object instanceof JavaProjectSourceLocation &&
			 getJavaProject().equals(((JavaProjectSourceLocation)object).getJavaProject());
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return getJavaProject().hashCode();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.sourcelookup.IJavaSourceLocation#getMemento()
	 */
	public String getMemento() throws CoreException {
		Document doc = DebugPlugin.newDocument();
		Element node = doc.createElement("javaProjectSourceLocation"); //$NON-NLS-1$
		doc.appendChild(node);
		node.setAttribute("name", getJavaProject().getElementName()); //$NON-NLS-1$
		return DebugPlugin.serializeDocument(doc);  
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.sourcelookup.IJavaSourceLocation#initializeFrom(java.lang.String)
	 */
	public void initializeFrom(String memento) throws CoreException {
		Exception ex = null;
		try {
			Element root = null;
			DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			parser.setErrorHandler(new DefaultHandler());
			StringReader reader = new StringReader(memento);
			InputSource source = new InputSource(reader);
			root = parser.parse(source).getDocumentElement();
												
			String name = root.getAttribute("name"); //$NON-NLS-1$
			if (isEmpty(name)) {
				abort(LaunchingMessages.JavaProjectSourceLocation_Unable_to_initialize_source_location___missing_project_name_3, null); 
			} else {
				IProject proj = ResourcesPlugin.getWorkspace().getRoot().getProject(name);
				setJavaProject(JavaCore.create(proj));
			}
			return;
		} catch (ParserConfigurationException e) {
			ex = e;			
		} catch (SAXException e) {
			ex = e;
		} catch (IOException e) {
			ex = e;
		}
		abort(LaunchingMessages.JavaProjectSourceLocation_Exception_occurred_initializing_source_location__4, ex); 
	}

	private boolean isEmpty(String string) {
		return string == null || string.length() == 0;
	}
	
	/*
	 * Throws an internal error exception
	 */
	private void abort(String message, Throwable e)	throws CoreException {
		IStatus s = new Status(IStatus.ERROR, LaunchingPlugin.getUniqueIdentifier(), IJavaLaunchConfigurationConstants.ERR_INTERNAL_ERROR, message, e);
		throw new CoreException(s);		
	}

}
