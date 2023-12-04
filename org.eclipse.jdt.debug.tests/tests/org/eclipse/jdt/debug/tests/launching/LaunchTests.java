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

import java.io.File;
import java.util.HashSet;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform.OS;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchListener;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.debug.core.IJavaReferenceType;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.internal.debug.core.model.JDIReferenceType;
import org.eclipse.jdt.launching.sourcelookup.advanced.AdvancedSourceLookup;

/**
 * Tests launch notification.
 */
public class LaunchTests extends AbstractDebugTest implements ILaunchListener {

	private boolean added = false;
	private boolean removed = false;
	private boolean terminated = false;

	/**
	 * Constructor
	 * @param name the name of the test
	 */
	public LaunchTests(String name) {
		super(name);
	}

	public void testUri() throws DebugException {
		// https://github.com/eclipse-jdt/eclipse.jdt.debug/issues/330
		if (OS.isWindows()) {
			IJavaReferenceType ref1 = new JDIReferenceType(null, null) {
				@Override
				public String[] getSourceNames(String stratum) {
					// normal URL resource on windows
					return new String[] { "Main.java", "file:/C:/workspace/prj/bi%20n/main/" };
				}
			};
			File classesLocation1 = AdvancedSourceLookup.getClassesLocation(ref1);
			IJavaReferenceType ref2 = new JDIReferenceType(null, null) {
				@Override
				public String[] getSourceNames(String stratum) {
					// opaque URL on windows
					return new String[] { "Main.java", "file:C:/workspace/prj/bi%20n/main/" };
				}
			};
			File classesLocation2 = AdvancedSourceLookup.getClassesLocation(ref2);
			assertEquals(new File("C:/workspace/prj/bi n/main/"), classesLocation1);
			assertEquals(new File("C:/workspace/prj/bi n/main/"), classesLocation2);
		}

		IJavaReferenceType ref3 = new JDIReferenceType(null, null) {
			@Override
			public String[] getSourceNames(String stratum) {
				// opaque URL on linux
				return new String[] { "Main.java", "file:workspace/prj/bi%20n/main/" };
			}
		};
		File classesLocation3 = AdvancedSourceLookup.getClassesLocation(ref3);
		IJavaReferenceType ref4 = new JDIReferenceType(null, null) {
			@Override
			public String[] getSourceNames(String stratum) {
				// normal URL resource on linux
				return new String[] { "Main.java", "file:/workspace/prj/bi%20n/main/" };
			}
		};
		File classesLocation4 = AdvancedSourceLookup.getClassesLocation(ref4);
		assertEquals(new File("/workspace/prj/bi n/main/"), classesLocation3);
		assertEquals(new File("/workspace/prj/bi n/main/"), classesLocation4);
	}
	/**
	 * test launch notification
	 */
	public void testLaunchNotification() throws CoreException {
		String typeName = "Breakpoints";		 //$NON-NLS-1$
		ILaunchConfiguration configuration = getLaunchConfiguration(typeName);
		getLaunchManager().addLaunchListener(this);
		HashSet<String> set = new HashSet<>();
		set.add(ILaunchManager.DEBUG_MODE);
		ensurePreferredDelegate(configuration, set);
		ILaunch launch = configuration.launch(ILaunchManager.DEBUG_MODE, null);
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
			for (int i= 0; i < 300; i++) {
				if (launch.isTerminated()) {
					terminated= true;
					break;
				}
				try {
					Thread.sleep(100);
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
	 * Tests launching an unregistered launch.
	 */
	public void testUnregisteredLaunch() throws Exception {
	   String typeName = "Breakpoints"; //$NON-NLS-1$
		createLineBreakpoint(55, typeName);
	   IJavaThread thread = null;
       try {
           thread = launchToBreakpoint(typeName, false);
           assertNotNull("Breakpoint not hit within timeout period", thread); //$NON-NLS-1$
           ILaunch launch = thread.getLaunch();
           assertFalse("Launch should not be registered", DebugPlugin.getDefault().getLaunchManager().isRegistered(launch)); //$NON-NLS-1$
       } finally {
           terminateAndRemove(thread);
           removeAllBreakpoints();
       }
	}

	/**
	 * @see org.eclipse.debug.core.ILaunchListener#launchRemoved(org.eclipse.debug.core.ILaunch)
	 */
	@Override
	public synchronized void launchRemoved(ILaunch launch) {
		removed = true;
		notifyAll();
	}

	/**
	 * @see org.eclipse.debug.core.ILaunchListener#launchAdded(org.eclipse.debug.core.ILaunch)
	 */
	@Override
	public synchronized void launchAdded(ILaunch launch) {
		added = true;
		notifyAll();
	}

	/**
	 * @see org.eclipse.debug.core.ILaunchListener#launchChanged(org.eclipse.debug.core.ILaunch)
	 */
	@Override
	public synchronized void launchChanged(ILaunch launch) {}

}
