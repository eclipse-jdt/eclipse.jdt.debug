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
package org.eclipse.jdt.internal.debug.ui;

 
import org.eclipse.jdt.launching.sourcelookup.ZipEntryStorage;

public class ZipEntryStorageEditorInput extends StorageEditorInput {
	
	public ZipEntryStorageEditorInput(ZipEntryStorage storage) {
		super(storage);
	}
	
	/**
	 * @see IEditorInput#exists()
	 */
	public boolean exists() {
		return true;
	}

}
