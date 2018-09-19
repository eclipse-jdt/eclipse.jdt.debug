/*******************************************************************************
 * Copyright (c) 2014, 2015 IBM Corporation and others.
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
package org.eclipse.jdt.internal.launching;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.launching.ILibraryLocationResolver;

public class JavaFxLibraryResolver implements ILibraryLocationResolver {

	public static final String JFXRT_JAR = "jfxrt.jar"; //$NON-NLS-1$
	private static final String JAVAFX_SRC_ZIP = "javafx-src.zip"; //$NON-NLS-1$
	private static final String JAVAFX_8_JAVADOC = "http://docs.oracle.com/javase/8/javafx/api/"; //$NON-NLS-1$

	private static boolean isJavaFx(IPath libraryPath) {
		return JFXRT_JAR.equals(libraryPath.lastSegment());
	}

	@Override
	public IPath getPackageRoot(IPath libraryPath) {
		return Path.EMPTY;
	}

	@Override
	public IPath getSourcePath(IPath libraryPath) {
		if (isJavaFx(libraryPath)) {
			File parent = libraryPath.toFile().getParentFile();
			while (parent != null) {
				File parentsrc = new File(parent, JAVAFX_SRC_ZIP);
				if (parentsrc.isFile()) {
					return new Path(parentsrc.getPath());
				}
				parent = parent.getParentFile();
			}
		}
		return Path.EMPTY;
	}

	@Override
	public URL getJavadocLocation(IPath libraryPath) {
		if (isJavaFx(libraryPath)) {
			/*
			 * TODO: We don't know if JavaSE-9 will ship JavaFX in the ext folder as well. If yes, then we have to use something like
			 * JavaRuntime#getVMInstall(IPath) and IVMInstall2#getJavaVersion() to determine the right Javadoc URL.
			 */
			try {
				return new URL(JAVAFX_8_JAVADOC);
			}
			catch (MalformedURLException e) {
				LaunchingPlugin.log(e);
			}
		}
		return null;
	}

	@Override
	public URL getIndexLocation(IPath libraryPath) {
		return null;
	}
}
