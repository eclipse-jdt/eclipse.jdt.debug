package org.eclipse.jdt.internal.debug.ui.launcher;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * Listener interface to receive notification when entries in a runtime
 * classpath entry viewer change in some way.
 */
public interface IEntriesChangedListener {

	/**
	 * Notification entries have changed in the viewer
	 */
	public void entriesChanged(RuntimeClasspathViewer viewer);
}
