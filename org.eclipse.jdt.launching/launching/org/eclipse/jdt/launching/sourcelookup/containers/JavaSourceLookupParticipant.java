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

package org.eclipse.jdt.launching.sourcelookup.containers;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.sourcelookup.AbstractSourceLookupParticipant;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;
import org.eclipse.debug.core.sourcelookup.containers.ArchiveSourceContainer;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.debug.core.JavaDebugUtils;

/**
 * A source lookup participant that searches for Java source code.
 * <p>
 * This class may be instantiated.
 * </p>
 * @since 3.0
 * @noextend This class is not intended to be sub-classed by clients.
 */
public class JavaSourceLookupParticipant extends AbstractSourceLookupParticipant {
	
	/**
	 * Map of delegate source containers for internal jars.
	 * Internal jars are translated to package fragment roots
	 * if possible.
	 */
	private Map<ISourceContainer, PackageFragmentRootSourceContainer> fDelegateContainers;
	
	/**
	 * Returns the source name associated with the given object, or <code>null</code>
	 * if none.
	 * 
	 * @param object an object with an <code>IJavaStackFrame</code> adapter, an IJavaValue
	 *  or an IJavaType 
	 * @return the source name associated with the given object, or <code>null</code>
	 * if none
	 * @exception CoreException if unable to retrieve the source name
	 */
	public String getSourceName(Object object) throws CoreException {
		return JavaDebugUtils.getSourceName(object); 
	}	

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.ISourceLookupParticipant#dispose()
	 */
	@Override
	public void dispose() {
		Iterator<PackageFragmentRootSourceContainer> iterator = fDelegateContainers.values().iterator();
		while (iterator.hasNext()) {
			ISourceContainer container = iterator.next();
			container.dispose();
		}
		fDelegateContainers = null;
		super.dispose();
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.AbstractSourceLookupParticipant#getDelegateContainer(org.eclipse.debug.internal.core.sourcelookup.ISourceContainer)
	 */
	@Override
	protected ISourceContainer getDelegateContainer(ISourceContainer container) {
		ISourceContainer delegate = fDelegateContainers.get(container);
		if (delegate == null) {
			return container;
		} 
		return delegate; 
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.ISourceLookupParticipant#init(org.eclipse.debug.internal.core.sourcelookup.ISourceLookupDirector)
	 */
	@Override
	public void init(ISourceLookupDirector director) {
		super.init(director);
		fDelegateContainers = new HashMap<ISourceContainer, PackageFragmentRootSourceContainer>();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.ISourceLookupParticipant#sourceContainersChanged(org.eclipse.debug.internal.core.sourcelookup.ISourceLookupDirector)
	 */
	@Override
	public void sourceContainersChanged(ISourceLookupDirector director) {
		// use package fragment roots in place of local archives, where they exist
		fDelegateContainers.clear();
		ISourceContainer[] containers = director.getSourceContainers();
		for (int i = 0; i < containers.length; i++) {
			ISourceContainer container = containers[i];
			if (container.getType().getId().equals(ArchiveSourceContainer.TYPE_ID)) {
				IFile file = ((ArchiveSourceContainer)container).getFile();
				IProject project = file.getProject();
				IJavaProject javaProject = JavaCore.create(project);
				if (javaProject.exists()) {
					try {
						IPackageFragmentRoot[] roots = javaProject.getPackageFragmentRoots();
						for (int j = 0; j < roots.length; j++) {
							IPackageFragmentRoot root = roots[j];
							if (file.equals(root.getUnderlyingResource())) {
								// the root was specified
								fDelegateContainers.put(container, new PackageFragmentRootSourceContainer(root));
							} else {
								IPath path = root.getSourceAttachmentPath();
								if (path != null) {
									if (file.getFullPath().equals(path)) {
										// a source attachment to a root was specified
										fDelegateContainers.put(container, new PackageFragmentRootSourceContainer(root));
									}
								}
							}
						}
					} catch (JavaModelException e) {
					}
				}
			}
		}
	}
}
