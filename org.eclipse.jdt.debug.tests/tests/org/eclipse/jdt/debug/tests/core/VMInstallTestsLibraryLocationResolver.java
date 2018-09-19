/*******************************************************************************
 * Copyright (c) 2013, 2015 IBM Corporation and others.
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
package org.eclipse.jdt.debug.tests.core;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.URIUtil;
import org.eclipse.jdt.debug.testplugin.JavaTestPlugin;
import org.eclipse.jdt.internal.launching.JavaFxLibraryResolver;
import org.eclipse.jdt.launching.ILibraryLocationResolver;


public class VMInstallTestsLibraryLocationResolver implements ILibraryLocationResolver {

	// This used to be in VMInstallTests. Moved here to support headless test runs,
	// which previously failed because VMInstallTests extends a UI class.
	static boolean isTesting = false;

	static boolean applies(IPath path) {
		if (!isTesting) {
			return false;
		}

		for (int i = 0; i < path.segmentCount(); i++) {
			if ("ext".equals(path.segment(i))) {
				return !JavaFxLibraryResolver.JFXRT_JAR.equals(path.lastSegment());
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jdt.launching.ILibraryLocationResolver#getPackageRoot(org.eclipse.core.runtime.IPath)
	 */
	@Override
	public IPath getPackageRoot(IPath libraryPath) {
		if (applies(libraryPath)) {
			return new Path("src");
		}
		return Path.EMPTY;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jdt.launching.ILibraryLocationResolver#getSourcePath(org.eclipse.core.runtime.IPath)
	 */
	@Override
	public IPath getSourcePath(IPath libraryPath) {
		if (applies(libraryPath)) {
			File file = JavaTestPlugin.getDefault().getFileInPlugin(new Path("testresources/test_resolver_src.zip"));
			if (file.isFile()) {
				return new Path(file.getAbsolutePath());
			}
		}
		return Path.EMPTY;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jdt.launching.ILibraryLocationResolver#getJavadocLocation(org.eclipse.core.runtime.IPath)
	 */
	@Override
	public URL getJavadocLocation(IPath libraryPath) {
		if (applies(libraryPath)) {
			File file = JavaTestPlugin.getDefault().getFileInPlugin(new Path("testresources/test_resolver_javadoc.zip"));
			if (file.isFile()) {
				try {
					return URIUtil.toURL(file.toURI());
				}
				catch (MalformedURLException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jdt.launching.ILibraryLocationResolver#getIndexLocation(org.eclipse.core.runtime.IPath)
	 */
	@Override
	public URL getIndexLocation(IPath libraryPath) {
		if (applies(libraryPath)) {
			File file = JavaTestPlugin.getDefault().getFileInPlugin(new Path("testresources/test_resolver_index.index"));
			if (file.isFile()) {
				try {
					return URIUtil.toURL(file.toURI());
				}
				catch (MalformedURLException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

}