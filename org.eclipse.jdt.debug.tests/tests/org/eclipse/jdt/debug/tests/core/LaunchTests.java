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
package org.eclipse.jdt.debug.tests.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchListener2;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;

/**
 * Tests launch notification.
 */
public class LaunchTests extends AbstractDebugTest implements ILaunchListener2 {
	
	private boolean added = false;
	private boolean removed = false;
	private boolean terminated = false; 
	
	public LaunchTests(String name) {
		super(name);
	}

	public void testDeferredBreakpoints() throws CoreException {
		String typeName = "Breakpoints";		

		ILaunchConfiguration configuration = getLaunchConfiguration(typeName);
		getLaunchManager().addLaunchListener(this);
		ILaunch launch = configuration.launch(ILaunchManager.DEBUG_MODE, null);
		synchronized (this) {
			if (!added) {
				try {
					wait(30000);
				} catch (InterruptedException e) {
				}
			}
		}
		assertTrue("Launch should have been added", added);

		synchronized (this) {
			if (!terminated) {
				try {
					wait(30000);
				} catch (InterruptedException e) {
				}
			}
		}
		assertTrue("Launch should have been terminated", terminated);
		
		getLaunchManager().removeLaunch(launch);
		
		synchronized (this) {
			if (!removed) {
				try {
					wait(30000);
				} catch (InterruptedException e) {
				}
			}
		}
		assertTrue("Launch should have been removed", removed);		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchListener2#launchTerminated(org.eclipse.debug.core.ILaunch)
	 */
	public synchronized void launchTerminated(ILaunch launch) {
		terminated = true;
		notifyAll();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchListener#launchRemoved(org.eclipse.debug.core.ILaunch)
	 */
	public synchronized void launchRemoved(ILaunch launch) {
		removed = true;
		notifyAll();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchListener#launchAdded(org.eclipse.debug.core.ILaunch)
	 */
	public synchronized void launchAdded(ILaunch launch) {
		added = true;
		notifyAll();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchListener#launchChanged(org.eclipse.debug.core.ILaunch)
	 */
	public synchronized void launchChanged(ILaunch launch) {

	}

}
