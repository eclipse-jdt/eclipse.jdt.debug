/*******************************************************************************
 * Copyright (c) 2013 BestSolution.at and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 *     IBM Corporation - Bug 399798, StandardVMType should allow to contribute default source and Javadoc locations for ext libraries
 *******************************************************************************/
package org.eclipse.jdt.launching;

import java.net.URL;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * This resolver allows contributors to provide {@link LibraryLocation} information for
 * non-standard JRE / JDK libraries.
 * <br><br>
 * For example this resolver could be used to provide
 * Javadoc and source locations for jars in the <code>/ext</code> location of a JRE / JDK
 *
 * @see JavaRuntime#EXTENSION_POINT_LIBRARY_LOCATION_RESOLVERS
 *
 * @since 3.7
 */
public interface ILibraryLocationResolver {

	/**
	 * Returns the path inside the <code>source</code> zip file where packages names begin, must not be
	 * <code>null</code> - use {@link Path#EMPTY}
	 * <br><br>
	 * For example, if the source for <code>java.lang.Object</code> source is found at <code>src/java/lang/Object.java</code> in the zip file, the package root
	 * would be <code>src</code>.
	 *
	 * @param libraryPath the path to the library
	 * @return the {@link IPath} to the root of the source or the empty path, never <code>null</code>
	 */
	public IPath getPackageRoot(IPath libraryPath);

	/**
	 * Returns the {@link IPath} of the <code>zip</code> or <code>jar</code> file containing the sources for <code>library</code>.
	 * <br><br>
	 * Must not be <code>null</code> - use {@link Path#EMPTY}
	 *
	 * @param libraryPath the path to the library, must not be <code>null</code>
	 * @return the {@link IPath} to the source or the empty path, never <code>null</code>
	 */
	public IPath getSourcePath(IPath libraryPath);

	/**
	 * Returns the {@link URL} of the Javadoc for this library or <code>null</code>
	 *
	 * @param libraryPath the path to the library, must not be <code>null</code>
	 * @return the Javadoc {@link URL} or <code>null</code>
	 */
	public URL getJavadocLocation(IPath libraryPath);

	/**
	 * Returns the {@link URL} of the index for the given library or <code>null</code>.
	 *
	 * @param libraryPath the path to the library, must not be <code>null</code>
	 * @return the index {@link URL} or <code>null</code>
	 */
	public URL getIndexLocation(IPath libraryPath);
}