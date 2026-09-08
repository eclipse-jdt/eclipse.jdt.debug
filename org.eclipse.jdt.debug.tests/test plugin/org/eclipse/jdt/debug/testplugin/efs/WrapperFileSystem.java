/*******************************************************************************
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jdt.debug.testplugin.efs;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.provider.FileSystem;

/**
 * A simple {@link org.eclipse.core.filesystem.IFileSystem} which delegates all operations to the local file system, but which uses an own (non
 * <code>file</code>) URI scheme.
 * <p>
 * Resources stored in this file system are fully functional from the resources API point of view, but they are <b>not</b> located in the local file
 * system, hence {@link org.eclipse.core.resources.IResource#getLocation()} returns <code>null</code> for them.
 * </p>
 */
public class WrapperFileSystem extends FileSystem {

	/**
	 * The URI scheme of this file system.
	 */
	public static final String SCHEME = "jdtdebugtestfs"; //$NON-NLS-1$

	/**
	 * Converts the given local (<code>file</code>) URI into an URI of this file system.
	 *
	 * @param localUri
	 *            an URI with the <code>file</code> scheme
	 * @return the corresponding URI in this file system
	 */
	public static URI toWrapperURI(URI localUri) {
		return convertScheme(localUri, SCHEME);
	}

	/**
	 * Converts the given URI of this file system into a local (<code>file</code>) URI.
	 *
	 * @param wrapperUri
	 *            an URI with the {@link #SCHEME} scheme
	 * @return the corresponding URI in the local file system
	 */
	public static URI toLocalURI(URI wrapperUri) {
		return convertScheme(wrapperUri, EFS.SCHEME_FILE);
	}

	private static URI convertScheme(URI uri, String scheme) {
		try {
			return new URI(scheme, uri.getUserInfo(), uri.getHost(), uri.getPort(), uri.getPath(), uri.getQuery(), uri.getFragment());
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException("Unable to convert " + uri + " to scheme " + scheme, e); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	@Override
	public IFileStore getStore(URI uri) {
		return new WrapperFileStore(EFS.getLocalFileSystem().getStore(toLocalURI(uri)));
	}

	@Override
	public boolean canDelete() {
		return true;
	}

	@Override
	public boolean canWrite() {
		return true;
	}

	@Override
	public int attributes() {
		return EFS.getLocalFileSystem().attributes();
	}

	@Override
	public boolean isCaseSensitive() {
		return EFS.getLocalFileSystem().isCaseSensitive();
	}
}
