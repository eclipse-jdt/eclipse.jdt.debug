/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.launching.sourcelookup;


import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.eclipse.core.resources.IStorage;
 
/**
 * Storage implementation for zip entries.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 * @see ArchiveSourceLocation
 * @see IStorage
 * @since 2.0
 * @deprecated In 3.0 this class is provided by the debug platform and clients
 *  should use the replacement class
 *  <code>org.eclipse.debug.core.sourcelookup.containers.ZipEntryStorage</code>.
 */
public class ZipEntryStorage extends org.eclipse.debug.core.sourcelookup.containers.ZipEntryStorage {
		
	/**
	 * Constructs a new storage implementation for the
	 * given zip entry in the specified zip file
	 * 
	 * @param archive zip file
	 * @param entry zip entry
	 */
	public ZipEntryStorage(ZipFile archive, ZipEntry entry) {
		super(archive, entry);
	}

}
