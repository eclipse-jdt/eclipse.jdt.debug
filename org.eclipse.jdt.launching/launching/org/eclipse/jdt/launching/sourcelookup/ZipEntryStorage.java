/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
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
package org.eclipse.jdt.launching.sourcelookup;


import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Storage implementation for zip entries.
 * <p>
 * This class may be instantiated.
 * </p>
 * @see ArchiveSourceLocation
 * @see org.eclipse.core.resources.IStorage
 * @since 2.0
 * @deprecated In 3.0 this class is provided by the debug platform and clients
 *  should use the replacement class
 *  <code>org.eclipse.debug.core.sourcelookup.containers.ZipEntryStorage</code>.
 * @noextend This class is not intended to be sub-classed by clients.
 */
@Deprecated
public class ZipEntryStorage extends org.eclipse.debug.core.sourcelookup.containers.ZipEntryStorage {

	/**
	 * Constructs a new storage implementation for the
	 * given zip entry in the specified zip file
	 *
	 * @param archive zip file
	 * @param entry zip entry
	 */
	@Deprecated
	public ZipEntryStorage(ZipFile archive, ZipEntry entry) {
		super(archive, entry);
	}

}
