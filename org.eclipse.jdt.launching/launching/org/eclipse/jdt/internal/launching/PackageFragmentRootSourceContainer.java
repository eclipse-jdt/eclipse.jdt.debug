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
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.internal.core.sourcelookup.ISourceContainerType;
import org.eclipse.debug.internal.core.sourcelookup.SourceLookupUtils;
import org.eclipse.debug.internal.core.sourcelookup.containers.AbstractSourceContainer;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;

/**
 * Package fragment root source container. Represents an archive
 * or folder in the Java model containing classfiles, with a possible
 * source attachment.
 * 
 * @since 3.0
 */
public class PackageFragmentRootSourceContainer extends AbstractSourceContainer {
	
	private IPackageFragmentRoot fRoot;
	
	/**
	 * Constructs a new package fragment root source container on the
	 * given root. The root must be of kind <code>K_BINARY</code>.
	 * 
	 * @param root package fragment root
	 */
	public PackageFragmentRootSourceContainer(IPackageFragmentRoot root) {
		fRoot = root;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.ISourceContainer#findSourceElements(java.lang.String)
	 */
	public Object[] findSourceElements(String name) throws CoreException {
		// look for a class file
		int index = name.lastIndexOf('.');
		String typeName = name;
		if (index >= 0) {
			// remove file type suffix
			typeName = typeName.substring(0, index);
		}
		typeName = typeName.replace('/', '.');
		typeName = typeName.replace('\\', '.');
		index = typeName.lastIndexOf('.');
		String packageName = ""; //$NON-NLS-1$
		if (index >= 0) {
			packageName = typeName.substring(0, index);
			typeName = typeName.substring(index + 1);
		}
		IPackageFragment fragment = fRoot.getPackageFragment(packageName);
		if (fragment.exists()) {
			IClassFile file = fragment.getClassFile(typeName + ".class"); //$NON-NLS-1$
			if (file.exists()) {
				return new Object[]{file};
			}
		}
		return EMPTY;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.ISourceContainer#getName()
	 */
	public String getName() {
		return fRoot.getElementName();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.ISourceContainer#getType()
	 */
	public ISourceContainerType getType() {
		return SourceLookupUtils.getSourceContainerType(PackageFragmentRootSourceContainerTypeDelegate.TYPE_ID);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		return obj instanceof PackageFragmentRootSourceContainer &&
		 ((PackageFragmentRootSourceContainer)obj).getPackageFragmentRoot().equals(getPackageFragmentRoot());
	}
	
	/**
	 * Returns the package fragment root this container searches for source.
	 * 
	 * @return the package fragment root this container searches for source
	 */
	public IPackageFragmentRoot getPackageFragmentRoot() {
		return fRoot;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return fRoot.hashCode();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		if (IAdaptable.class.equals(adapter)) {
			return getPackageFragmentRoot();
		}
		return super.getAdapter(adapter);
	}
}
