/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.core;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.ui.console.IConsole;

/**
 * Tests console line tracker.
 */
public class ConsoleTests extends AbstractDebugTest {
	
	public ConsoleTests(String name) {
		super(name);
	}
	
	/** 
	 * Test that when a process is removed from a launch, the associated
	 * console is closed.
	 * 
	 * @throws Exception
	 */
	public void testRemoveProcess() throws Exception {
		String typeName = "Breakpoints";
		IJavaDebugTarget target = null;
		try {
			final IJavaDebugTarget otherTarget = launchAndTerminate(typeName);
			target = otherTarget;
			IProcess process = target.getProcess();
			assertNotNull("Missing VM process", process);
			ILaunch launch = target.getLaunch();
			// make sure the console exists
			DebugUIPlugin.getStandardDisplay().syncExec(new Runnable() {
				public void run() {
					IConsole console = DebugUITools.getConsole(otherTarget);
					assertNotNull("Missing console", console);
				}
			});
			launch.removeProcess(process);
			// make sure the console is gone
			DebugUIPlugin.getStandardDisplay().syncExec(new Runnable() {
				public void run() {
					IConsole console = DebugUITools.getConsole(otherTarget);
					assertNull("Console should no longer exist", console);
				}
			});			
		} finally {
			terminateAndRemove(target);
		}				
	} 
	

}
