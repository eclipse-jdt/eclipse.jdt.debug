package org.eclipse.jdt.internal.debug.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.jdt.launching.sourcelookup.LocalFileStorage;

public class LocalFileStorageEditorInput extends StorageEditorInput {

	/**
	 * Constructs an editor input for the given storage
	 */	
	public LocalFileStorageEditorInput(LocalFileStorage storage) {
		super(storage);
	}
	

	/**
	 * @see IEditorInput#exists()
	 */
	public boolean exists() {
		return ((LocalFileStorage)getStorage()).getFile().exists();
	}

}
