package org.eclipse.jdt.internal.debug.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
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
