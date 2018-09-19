/*******************************************************************************
 *  Copyright (c) 2000, 2007 IBM Corporation and others.
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
package org.eclipse.jdt.debug.tests.breakpoints;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.testplugin.DebugElementKindEventDetailWaiter;
import org.eclipse.jdt.debug.testplugin.DebugEventWaiter;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Tests launch notification.
 */
public class PreLaunchBreakpointTest extends AbstractDebugTest {

	/**
	 * Constructor
	 * @param name
	 */
	public PreLaunchBreakpointTest(String name) {
		super(name);
	}

	/**
	 * Tests that the perspective will switch if breakpoints are detected and the program was launched in
	 * 'run' mode
	 * @throws Exception
	 */
	public void testRunModeLaunchWithBreakpoints() throws Exception {
		String typeName = "Breakpoints";

		ILaunchConfiguration configuration = getLaunchConfiguration(typeName);

		IPreferenceStore preferenceStore = DebugUIPlugin.getDefault().getPreferenceStore();
		preferenceStore.setValue(IInternalDebugUIConstants.PREF_RELAUNCH_IN_DEBUG_MODE, MessageDialogWithToggle.ALWAYS);
		IJavaThread thread = null;
		try {
			createLineBreakpoint(55, typeName);
			DebugEventWaiter waiter= new DebugElementKindEventDetailWaiter(DebugEvent.SUSPEND, IJavaThread.class, DebugEvent.BREAKPOINT);
			waiter.setTimeout(DEFAULT_TIMEOUT);
			configuration.launch(ILaunchManager.RUN_MODE, null);
			Object suspendee= waiter.waitForEvent();
			assertTrue("Program did not suspend", suspendee instanceof IJavaThread);
			thread = (IJavaThread) suspendee;

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			removeAllBreakpoints();
			//this must get done... other tests might fail.
			preferenceStore.setValue(IInternalDebugUIConstants.PREF_RELAUNCH_IN_DEBUG_MODE, MessageDialogWithToggle.NEVER);
			terminateAndRemove(thread);
		}
	}
}
