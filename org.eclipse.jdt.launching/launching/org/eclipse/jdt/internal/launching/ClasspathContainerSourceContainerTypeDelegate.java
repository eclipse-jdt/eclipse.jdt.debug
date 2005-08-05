/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.launching;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.AbstractSourceContainerTypeDelegate;
import org.eclipse.jdt.launching.sourcelookup.containers.ClasspathContainerSourceContainer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Classpath container source container type.
 * 
 * @since 3.0
 */
public class ClasspathContainerSourceContainerTypeDelegate extends AbstractSourceContainerTypeDelegate {
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.ISourceContainerTypeDelegate#createSourceContainer(java.lang.String)
	 */
	public ISourceContainer createSourceContainer(String memento) throws CoreException {
		Node node = parseDocument(memento);
		if (node.getNodeType() == Node.ELEMENT_NODE) {
			Element element = (Element)node;
			if ("classpathContainer".equals(element.getNodeName())) { //$NON-NLS-1$
				String string = element.getAttribute("path"); //$NON-NLS-1$
				if (string == null || string.length() == 0) {
					abort(LaunchingMessages.ClasspathContainerSourceContainerTypeDelegate_5, null); 
				}
				return new ClasspathContainerSourceContainer(new Path(string));
			} 
			abort(LaunchingMessages.ClasspathContainerSourceContainerTypeDelegate_6, null); 
		}
		abort(LaunchingMessages.ClasspathContainerSourceContainerTypeDelegate_7, null); 
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.ISourceContainerTypeDelegate#getMemento(org.eclipse.debug.internal.core.sourcelookup.ISourceContainer)
	 */
	public String getMemento(ISourceContainer container) throws CoreException {
		ClasspathContainerSourceContainer var =  (ClasspathContainerSourceContainer) container;
		Document document = newDocument();
		Element element = document.createElement("classpathContainer"); //$NON-NLS-1$
		element.setAttribute("path", var.getPath().toString()); //$NON-NLS-1$
		document.appendChild(element);
		return serializeDocument(document);
	}
}
