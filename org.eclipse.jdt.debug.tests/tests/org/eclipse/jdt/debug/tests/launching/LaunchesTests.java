/*******************************************************************************
 *  Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.launching;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.ILaunchesListener2;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;

/**
 * Tests launch notification plural
 */
public class LaunchesTests extends AbstractDebugTest implements ILaunchesListener2 {

	private boolean added = false;
	private boolean removed = false;
	private boolean terminated = false;

	/**
	 * Constructor
	 * @param name the name of the test
	 */
	public LaunchesTests(String name) {
		super(name);
	}



	@Override
	protected void setUp() throws Exception {
		added = false;
		removed = false;
		terminated = false;
		super.setUp();
	}



	/**
	 * Tests launch notification in debug mode.
	 * @throws CoreException
	 */
	public void testDebugMode() throws CoreException {
		doMode(ILaunchManager.DEBUG_MODE);
	}

	/**
	 * Tests launch notification in run mode.
	 * @throws CoreException
	 */
	public void testRunMode() throws CoreException {
		doMode(ILaunchManager.RUN_MODE);
	}

	protected void doMode(String mode) throws CoreException {
		String typeName = "Breakpoints";		 //$NON-NLS-1$
		ILaunchConfiguration configuration = getLaunchConfiguration(typeName);
		getLaunchManager().addLaunchListener(this);
		ILaunch launch = configuration.launch(mode, null);
		synchronized (this) {
			if (!added) {
				try {
					wait(30000);
				} catch (InterruptedException e) {
				}
			}
		}
		assertTrue("Launch should have been added", added); //$NON-NLS-1$

		synchronized (this) {
			if (!terminated) {
				try {
					wait(30000);
				} catch (InterruptedException e) {
				}
			}
		}
		assertTrue("Launch should have been terminated", terminated); //$NON-NLS-1$

		getLaunchManager().removeLaunch(launch);

		synchronized (this) {
			if (!removed) {
				try {
					wait(30000);
				} catch (InterruptedException e) {
				}
			}
		}
		assertTrue("Launch should have been removed", removed);		 //$NON-NLS-1$
	}

	/**
	 * @see org.eclipse.debug.core.ILaunchesListener2#launchesTerminated(org.eclipse.debug.core.ILaunch[])
	 */
	@Override
	public synchronized void launchesTerminated(ILaunch[] launches) {
		terminated = true;
		notifyAll();
	}

	/**
	 * @see org.eclipse.debug.core.ILaunchesListener#launchesRemoved(org.eclipse.debug.core.ILaunch[])
	 */
	@Override
	public synchronized void launchesRemoved(ILaunch[] launches) {
		removed = true;
		notifyAll();
	}

	/**
	 * @see org.eclipse.debug.core.ILaunchesListener#launchesAdded(org.eclipse.debug.core.ILaunch[])
	 */
	@Override
	public synchronized void launchesAdded(ILaunch[] launches) {
		added = true;
		notifyAll();
	}

	/**
	 * @see org.eclipse.debug.core.ILaunchesListener#launchesChanged(org.eclipse.debug.core.ILaunch[])
	 */
	@Override
	public synchronized void launchesChanged(ILaunch[] launches) {}

}
