package org.eclipse.jdt.internal.debug.ui.launcher;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.core.ILaunchManager;

/**
 * Convenience action to create/launch a local Java application
 */
public class DebugJavaApplicationAction extends JavaApplicationAction {

	/**
	 * @see JavaApplicationAction#getMode()
	 */
	protected String getMode() {
		return ILaunchManager.DEBUG_MODE;
	}

}
