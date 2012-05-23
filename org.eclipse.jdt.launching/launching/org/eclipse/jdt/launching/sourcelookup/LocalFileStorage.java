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
package org.eclipse.jdt.launching.sourcelookup;

import java.io.File;

/**
 * Implementation of storage for a local file
 * (<code>java.io.File</code>).
 * <p>
 * This class may be instantiated.
 * </p>
 * @see DirectorySourceLocation
 * @see org.eclipse.core.resources.IStorage
 * @since 2.0
 * @deprecated In 3.0 this class is now provided by the debug platform. Clients
 *  should use the replacement class
 *  <code>org.eclipse.debug.core.sourcelookup.containers.LocalFileStorage</code>
 * @noextend This class is not intended to be sub-classed by clients.
 */
@Deprecated
public class LocalFileStorage extends org.eclipse.debug.core.sourcelookup.containers.LocalFileStorage {
	
	/**
	 * Constructs and returns storage for the given file.
	 * 
	 * @param file a local file
	 */
	public LocalFileStorage(File file){
		super(file);
	}
	
}
