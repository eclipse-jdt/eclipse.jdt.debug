/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.launching;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.internal.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.internal.core.sourcelookup.SourceLookupUtils;
import org.eclipse.debug.internal.core.sourcelookup.containers.AbstractSourceContainerTypeDelegate;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Java project source container type.
 */
public class JavaProjectSourceContainerTypeDelegate extends AbstractSourceContainerTypeDelegate {

	/**
	 * Unique identifier for Java project source container type
	 * (value <code>org.eclipse.jdt.launching.sourceContainer.javaProject</code>).
	 */
	public static final String TYPE_ID = LaunchingPlugin.getUniqueIdentifier() + ".sourceContainer.javaProject";   //$NON-NLS-1$
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.ISourceContainerTypeDelegate#createSourceContainer(java.lang.String)
	 */
	public ISourceContainer createSourceContainer(String memento) throws CoreException {
		Node node = SourceLookupUtils.parseDocument(memento);
		if (node.getNodeType() == Node.ELEMENT_NODE) {
			Element element = (Element)node;
			if ("javaProject".equals(element.getNodeName())) { //$NON-NLS-1$
				String string = element.getAttribute("name"); //$NON-NLS-1$
				if (string == null || string.length() == 0) {
					abort("Missing required <name> attribute in Java project source container memento.", null);
				}
				IWorkspace workspace = ResourcesPlugin.getWorkspace();
				IProject project = workspace.getRoot().getProject(string);
				IJavaProject javaProject = JavaCore.create(project);
				return new JavaProjectSourceContainer(javaProject);
			} else {
				abort("Missing required <javaProject> attribute in Java project source container memento.", null);
			}
		}
		abort("Invalid format for Java project source container memento.", null);
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.ISourceContainerTypeDelegate#getMemento(org.eclipse.debug.internal.core.sourcelookup.ISourceContainer)
	 */
	public String getMemento(ISourceContainer container) throws CoreException {
		JavaProjectSourceContainer project = (JavaProjectSourceContainer) container;
		Document document = SourceLookupUtils.newDocument();
		Element element = document.createElement("javaProject"); //$NON-NLS-1$
		element.setAttribute("name", project.getName()); //$NON-NLS-1$
		document.appendChild(element);
		return SourceLookupUtils.serializeDocument(document);
	}
}
