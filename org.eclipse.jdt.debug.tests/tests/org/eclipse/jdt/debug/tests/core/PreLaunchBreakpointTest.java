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

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.ILaunchesListener;
import org.eclipse.debug.internal.ui.AlwaysNeverDialog;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Tests launch notification.
 */
public class PreLaunchBreakpointTest extends AbstractDebugTest {
	
	private boolean debugAdded = false;
	private boolean runRemoved = false;
	
	ILaunchConfiguration configuration;
	String sourceName;
	
	public PreLaunchBreakpointTest(String name) {
		super(name);
		sourceName = name;
	}
	
	public void testRunModeLaunchWithBreakpoints() {
		String typeName = "Breakpoints";		
		
		configuration = getLaunchConfiguration(typeName);
		getLaunchManager().addLaunchListener(new MyListener());
		
		IPreferenceStore preferenceStore = DebugUIPlugin.getDefault().getPreferenceStore();
		preferenceStore.setValue(IDebugUIConstants.PREF_RELAUNCH_IN_DEBUG_MODE, AlwaysNeverDialog.ALWAYS);
		
		try {
			createTargetPatternBreakpoint(77, sourceName);
			
			DebugUITools.buildAndLaunch(configuration, ILaunchManager.RUN_MODE, new NullProgressMonitor());
			
			synchronized (this) {
				if (!debugAdded) {
					try {
						wait();
					} catch (InterruptedException e) {
					}
				}
			}
			assertFalse("RUN_MODE Launch should never have been added", runRemoved);
			assertTrue("DEBUG_MODE Launch should have been added", debugAdded);
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			removeAllBreakpoints();
			//this must get done... other tests might fail.
			preferenceStore.setValue(IDebugUIConstants.PREF_RELAUNCH_IN_DEBUG_MODE, AlwaysNeverDialog.NEVER);
		}
	}
	
	private class MyListener implements ILaunchesListener {
		/* (non-Javadoc)
		 * @see org.eclipse.debug.core.ILaunchesListener#launchesRemoved(org.eclipse.debug.core.ILaunch[])
		 */
		public void launchesRemoved(ILaunch[] launches) {
			synchronized (PreLaunchBreakpointTest.this) {
				for (int i = 0; i < launches.length; i++) {
					ILaunchConfiguration goneAway = launches[i].getLaunchConfiguration();
					if (goneAway.equals(configuration) && launches[i].getLaunchMode().equals(ILaunchManager.RUN_MODE)) {
						runRemoved = true;
						PreLaunchBreakpointTest.this.notify();
					}
				}
			}
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.debug.core.ILaunchesListener#launchesAdded(org.eclipse.debug.core.ILaunch[])
		 */
		public void launchesAdded(ILaunch[] launches) {
			synchronized (PreLaunchBreakpointTest.this) {
				for (int i = 0; i < launches.length; i++) {
					ILaunchConfiguration newLC = launches[i].getLaunchConfiguration();
					if (newLC.equals(configuration) && launches[i].getLaunchMode().equals(ILaunchManager.DEBUG_MODE)) {
						debugAdded = true;
						PreLaunchBreakpointTest.this.notify();
					}
				}
			}
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.debug.core.ILaunchesListener#launchesChanged(org.eclipse.debug.core.ILaunch[])
		 */
		public void launchesChanged(ILaunch[] launches) {
		}
	}
}
