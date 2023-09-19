/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.launching.sourcelookup.containers;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.sourcelookup.ISourceContainerType;
import org.eclipse.debug.core.sourcelookup.containers.AbstractSourceContainer;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.launching.LaunchingPlugin;

/**
 * Package fragment root source container. Represents an archive
 * or folder in the Java model containing class files, with a possible
 * source attachment.
 * <p>
 * This class may be instantiated.
 * </p>
 *
 * @since 3.0
 * @noextend This class is not intended to be subclassed by clients.
 */
public class PackageFragmentRootSourceContainer extends AbstractSourceContainer {

	private final IPackageFragmentRoot fRoot;
	/**
	 * Unique identifier for Java project source container type
	 * (value <code>org.eclipse.jdt.launching.sourceContainer.packageFragmentRoot</code>).
	 */
	public static final String TYPE_ID = LaunchingPlugin.getUniqueIdentifier() + ".sourceContainer.packageFragmentRoot";   //$NON-NLS-1$

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
	@Override
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
			switch (fragment.getKind()) {
				case IPackageFragmentRoot.K_BINARY:
					IClassFile file = fragment.getClassFile(typeName + ".class"); //$NON-NLS-1$
					if (file.exists()) {
						return new Object[]{file};
					}
					break;
				case IPackageFragmentRoot.K_SOURCE:
					String[] extensions = JavaCore.getJavaLikeExtensions();
					for (int i = 0; i < extensions.length; i++) {
						String ext = extensions[i];
						ICompilationUnit unit = fragment.getCompilationUnit(typeName + '.' + ext);
						if (unit.exists()) {
							return new Object[]{unit};
						}
					}
					break;
			}

		}
		return EMPTY;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.ISourceContainer#getName()
	 */
	@Override
	public String getName() {
		return fRoot.getElementName();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.ISourceContainer#getType()
	 */
	@Override
	public ISourceContainerType getType() {
		return getSourceContainerType(TYPE_ID);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
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
	@Override
	public int hashCode() {
		return fRoot.hashCode();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.ISourceContainer#getPath()
	 */
	public IPath getPath() {
		return getPackageFragmentRoot().getPath();
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("["); //$NON-NLS-1$
		if (fRoot != null) {
			builder.append(fRoot.getElementName());
			builder.append(", parent="); //$NON-NLS-1$
			builder.append(fRoot.getParent().getElementName());
			builder.append(", path="); //$NON-NLS-1$
			builder.append(getPath());
		}
		builder.append("]"); //$NON-NLS-1$
		return builder.toString();
	}

}
