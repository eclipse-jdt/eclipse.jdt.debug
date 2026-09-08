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

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.provider.FileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * A file store which delegates all operations to a store of the local file system.
 * <p>
 * Note that {@link #toLocalFile(int, IProgressMonitor)} is intentionally <b>not</b> overridden: the default implementation returns <code>null</code>
 * if no caching is requested, which is what makes resources stored in the {@link WrapperFileSystem} "non local".
 * </p>
 */
public class WrapperFileStore extends FileStore {

	private final IFileStore baseStore;

	public WrapperFileStore(IFileStore baseStore) {
		this.baseStore = baseStore;
	}

	@Override
	public String[] childNames(int options, IProgressMonitor monitor) throws CoreException {
		return baseStore.childNames(options, monitor);
	}

	@Override
	public IFileInfo fetchInfo(int options, IProgressMonitor monitor) throws CoreException {
		return baseStore.fetchInfo(options, monitor);
	}

	@Override
	public IFileStore getChild(String name) {
		return new WrapperFileStore(baseStore.getChild(name));
	}

	@Override
	public String getName() {
		return baseStore.getName();
	}

	@Override
	public IFileStore getParent() {
		IFileStore parent = baseStore.getParent();
		return parent == null ? null : new WrapperFileStore(parent);
	}

	@Override
	public InputStream openInputStream(int options, IProgressMonitor monitor) throws CoreException {
		return baseStore.openInputStream(options, monitor);
	}

	@Override
	public OutputStream openOutputStream(int options, IProgressMonitor monitor) throws CoreException {
		return baseStore.openOutputStream(options, monitor);
	}

	@Override
	public IFileStore mkdir(int options, IProgressMonitor monitor) throws CoreException {
		return new WrapperFileStore(baseStore.mkdir(options, monitor));
	}

	@Override
	public void delete(int options, IProgressMonitor monitor) throws CoreException {
		baseStore.delete(options, monitor);
	}

	@Override
	public void putInfo(IFileInfo info, int options, IProgressMonitor monitor) throws CoreException {
		baseStore.putInfo(info, options, monitor);
	}

	@Override
	public URI toURI() {
		return WrapperFileSystem.toWrapperURI(baseStore.toURI());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof WrapperFileStore)) {
			return false;
		}
		return baseStore.equals(((WrapperFileStore) obj).baseStore);
	}

	@Override
	public int hashCode() {
		return baseStore.hashCode();
	}

	@Override
	public String toString() {
		return toURI().toString();
	}
}
