/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui;

import org.eclipse.debug.core.sourcelookup.containers.LocalFileStorage;

public class LocalFileStorageEditorInput extends StorageEditorInput {

	/**
	 * Constructs an editor input for the given storage
	 */	
	public LocalFileStorageEditorInput(LocalFileStorage storage) {
		super(storage);
	}
	

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IEditorInput#exists()
	 */
	public boolean exists() {
		return ((LocalFileStorage)getStorage()).getFile().exists();
	}

}
