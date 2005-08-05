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
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.AbstractSourceContainerTypeDelegate;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.sourcelookup.containers.PackageFragmentRootSourceContainer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Package fragment root source container type.
 * 
 * @since 3.0
 */
public class PackageFragmentRootSourceContainerTypeDelegate extends AbstractSourceContainerTypeDelegate {

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.ISourceContainerTypeDelegate#createSourceContainer(java.lang.String)
	 */
	public ISourceContainer createSourceContainer(String memento) throws CoreException {
		Node node = parseDocument(memento);
		if (node.getNodeType() == Node.ELEMENT_NODE) {
			Element element = (Element)node;
			if ("packageFragmentRoot".equals(element.getNodeName())) { //$NON-NLS-1$
				String string = element.getAttribute("handle"); //$NON-NLS-1$
				if (string == null || string.length() == 0) {
					abort(LaunchingMessages.PackageFragmentRootSourceContainerTypeDelegate_6, null); 
				}
				IJavaElement root = JavaCore.create(string);
				if (root != null && root instanceof IPackageFragmentRoot) {
					return new PackageFragmentRootSourceContainer((IPackageFragmentRoot)root);
				}
				abort(LaunchingMessages.PackageFragmentRootSourceContainerTypeDelegate_7, null); 
			} else {
				abort(LaunchingMessages.PackageFragmentRootSourceContainerTypeDelegate_8, null); 
			}
		}
		abort(LaunchingMessages.JavaProjectSourceContainerTypeDelegate_7, null); 
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.ISourceContainerTypeDelegate#getMemento(org.eclipse.debug.internal.core.sourcelookup.ISourceContainer)
	 */
	public String getMemento(ISourceContainer container) throws CoreException {
		PackageFragmentRootSourceContainer root = (PackageFragmentRootSourceContainer) container;
		Document document = newDocument();
		Element element = document.createElement("packageFragmentRoot"); //$NON-NLS-1$
		element.setAttribute("handle", root.getPackageFragmentRoot().getHandleIdentifier()); //$NON-NLS-1$
		document.appendChild(element);
		return serializeDocument(document);
	}
}
