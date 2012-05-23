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
package org.eclipse.jdt.internal.launching;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry2;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Common function for runtime classpath entries.
 * <p>
 * Clients implementing runtime classpath entries must subclass this
 * class.
 * </p>
 * @since 3.0
 */
public abstract class AbstractRuntimeClasspathEntry extends PlatformObject implements IRuntimeClasspathEntry2 {
	
	private IPath sourceAttachmentPath = null;
	private IPath rootSourcePath = null;
	private int classpathProperty = IRuntimeClasspathEntry.USER_CLASSES;
	/**
	 * Associated Java project, or <code>null</code>
	 */
	private IJavaProject fJavaProject;
	
	/* (non-Javadoc)
	 * 
	 * Default implementation returns <code>false</code>.
	 * Subclasses should override if required.
	 * 
	 * @see org.eclipse.jdt.internal.launching.IRuntimeClasspathEntry2#isComposite()
	 */
	public boolean isComposite() {
		return false;
	}
	
	/** 
	 * Default implementation returns an empty collection.
	 * Subclasses should override if required.
	 * @return the array of entries
	 * @throws CoreException if computing the entries fails
	 * @see IRuntimeClasspathEntry2
	 */
	public IRuntimeClasspathEntry[] getRuntimeClasspathEntries() throws CoreException {
		return new IRuntimeClasspathEntry[0];
	}
	
	/**
	 * Throws an exception with the given message and underlying exception.
	 * 
	 * @param message error message
	 * @param exception underlying exception or <code>null</code> if none
	 * @throws CoreException the new {@link CoreException}
	 */
	protected void abort(String message, Throwable exception) throws CoreException {
		IStatus status = new Status(IStatus.ERROR, LaunchingPlugin.getUniqueIdentifier(), IJavaLaunchConfigurationConstants.ERR_INTERNAL_ERROR, message, exception);
		throw new CoreException(status);
	}

	/* (non-Javadoc)
	 * 
	 * Default implementation generates a string containing an XML
	 * document. Subclasses should override <code>buildMemento</code>
	 * to specify the contents of the required <code>memento</code>
	 * node.
	 * 
	 * @see org.eclipse.jdt.launching.IRuntimeClasspathEntry#getMemento()
	 */
	public String getMemento() throws CoreException {
		Document doc= DebugPlugin.newDocument();
		Element root = doc.createElement("runtimeClasspathEntry"); //$NON-NLS-1$
		doc.appendChild(root);
		root.setAttribute("id", getTypeId()); //$NON-NLS-1$
		Element memento = doc.createElement("memento"); //$NON-NLS-1$
		root.appendChild(memento);
		buildMemento(doc, memento);
		return DebugPlugin.serializeDocument(doc);
	}
	
	/**
	 * Constructs a memento for this classpath entry in the given 
	 * document and element. The memento element has already been
	 * appended to the document.
	 * 
	 * @param document XML document
	 * @param memento element node for client specific attributes
	 * @throws CoreException if unable to create a memento 
	 */
	protected abstract void buildMemento(Document document, Element memento) throws CoreException;
	
	/* (non-Javadoc)
	 * 
	 * Default implementation returns <code>null</code>.
	 * Subclasses should override if required.
	 * 
	 * @see org.eclipse.jdt.launching.IRuntimeClasspathEntry#getPath()
	 */
	public IPath getPath() {
		return null;
	}
	
	/* (non-Javadoc)
	 * 
	 * Default implementation returns <code>null</code>.
	 * Subclasses should override if required.
	 * 
	 * @see org.eclipse.jdt.launching.IRuntimeClasspathEntry#getResource()
	 */
	public IResource getResource() {
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IRuntimeClasspathEntry#getSourceAttachmentPath()
	 */
	public IPath getSourceAttachmentPath() {
		return sourceAttachmentPath;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IRuntimeClasspathEntry#setSourceAttachmentPath(org.eclipse.core.runtime.IPath)
	 */
	public void setSourceAttachmentPath(IPath path) {
		sourceAttachmentPath = path;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IRuntimeClasspathEntry#getSourceAttachmentRootPath()
	 */
	public IPath getSourceAttachmentRootPath() {
		return rootSourcePath;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IRuntimeClasspathEntry#setSourceAttachmentRootPath(org.eclipse.core.runtime.IPath)
	 */
	public void setSourceAttachmentRootPath(IPath path) {
		rootSourcePath = path;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IRuntimeClasspathEntry#getClasspathProperty()
	 */
	public int getClasspathProperty() {
		return classpathProperty;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IRuntimeClasspathEntry#setClasspathProperty(int)
	 */
	public void setClasspathProperty(int property) {
		classpathProperty = property;
	}
	/* (non-Javadoc)
	 * 
	 * Default implementation returns <code>null</code>.
	 * Subclasses should override if required.
	 * 
	 * @see org.eclipse.jdt.launching.IRuntimeClasspathEntry#getLocation()
	 */
	public String getLocation() {
		return null;
	}
	
	/* (non-Javadoc)
	 * 
	 * Default implementation returns <code>null</code>.
	 * Subclasses should override if required.
	 * @see org.eclipse.jdt.launching.IRuntimeClasspathEntry#getSourceAttachmentLocation()
	 */
	public String getSourceAttachmentLocation() {
		return null;
	}
	/* (non-Javadoc)
	 * 
	 * Default implementation returns <code>null</code>.
	 * Subclasses should override if required.
	 * @see org.eclipse.jdt.launching.IRuntimeClasspathEntry#getSourceAttachmentRootLocation()
	 */
	public String getSourceAttachmentRootLocation() {
		return null;
	}
	/* (non-Javadoc)
	 * 
	 * Default implementation returns <code>null</code>.
	 * Subclasses should override if required.
	 * 
	 * @see org.eclipse.jdt.launching.IRuntimeClasspathEntry#getVariableName()
	 */
	public String getVariableName() {
		return null;
	}
	/* (non-Javadoc)
	 * 
	 * Default implementation returns <code>null</code>.
	 * Subclasses should override if required.
	 * 
	 * @see org.eclipse.jdt.launching.IRuntimeClasspathEntry#getClasspathEntry()
	 */
	public IClasspathEntry getClasspathEntry() {
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IRuntimeClasspathEntry#getJavaProject()
	 */
	public IJavaProject getJavaProject() {
		return fJavaProject;
	}
	
	/**
	 * Sets the Java project associated with this entry.
	 * 
	 * @param javaProject the Java project context
	 */
	protected void setJavaProject(IJavaProject javaProject) {
		fJavaProject = javaProject;
	}
}
