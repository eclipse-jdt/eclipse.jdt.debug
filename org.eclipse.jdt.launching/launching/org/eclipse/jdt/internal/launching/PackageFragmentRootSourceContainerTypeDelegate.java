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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.internal.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.internal.core.sourcelookup.SourceLookupUtils;
import org.eclipse.debug.internal.core.sourcelookup.containers.AbstractSourceContainerTypeDelegate;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Package fragment root source container type.
 * 
 * @since 3.0
 */
public class PackageFragmentRootSourceContainerTypeDelegate extends AbstractSourceContainerTypeDelegate {

	/**
	 * Unique identifier for Java project source container type
	 * (value <code>org.eclipse.jdt.launching.sourceContainer.packageFragmentRoot</code>).
	 */
	public static final String TYPE_ID = LaunchingPlugin.getUniqueIdentifier() + ".sourceContainer.packageFragmentRoot";   //$NON-NLS-1$

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.ISourceContainerTypeDelegate#createSourceContainer(java.lang.String)
	 */
	public ISourceContainer createSourceContainer(String memento) throws CoreException {
		Node node = SourceLookupUtils.parseDocument(memento);
		if (node.getNodeType() == Node.ELEMENT_NODE) {
			Element element = (Element)node;
			if ("packageFragmentRoot".equals(element.getNodeName())) { //$NON-NLS-1$
				String string = element.getAttribute("handle"); //$NON-NLS-1$
				if (string == null || string.length() == 0) {
					abort(LaunchingMessages.getString("PackageFragmentRootSourceContainerTypeDelegate.6"), null); //$NON-NLS-1$
				}
				IJavaElement root = JavaCore.create(string);
				if (root != null && root instanceof IPackageFragmentRoot) {
					return new PackageFragmentRootSourceContainer((IPackageFragmentRoot)root);
				}
				abort(LaunchingMessages.getString("PackageFragmentRootSourceContainerTypeDelegate.7"), null); //$NON-NLS-1$
			} else {
				abort(LaunchingMessages.getString("PackageFragmentRootSourceContainerTypeDelegate.8"), null); //$NON-NLS-1$
			}
		}
		abort(LaunchingMessages.getString("JavaProjectSourceContainerTypeDelegate.7"), null); //$NON-NLS-1$
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.ISourceContainerTypeDelegate#getMemento(org.eclipse.debug.internal.core.sourcelookup.ISourceContainer)
	 */
	public String getMemento(ISourceContainer container) throws CoreException {
		PackageFragmentRootSourceContainer root = (PackageFragmentRootSourceContainer) container;
		Document document = SourceLookupUtils.newDocument();
		Element element = document.createElement("packageFragmentRoot"); //$NON-NLS-1$
		element.setAttribute("handle", root.getPackageFragmentRoot().getHandleIdentifier()); //$NON-NLS-1$
		document.appendChild(element);
		return SourceLookupUtils.serializeDocument(document);
	}
}
