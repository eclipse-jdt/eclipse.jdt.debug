/*******************************************************************************
 * Copyright (c) 2025 Christoph Läubrich and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Christoph Läubrich - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.core;

import java.io.StringReader;
import java.util.Properties;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.tests.ui.AbstractDebugUiTests;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.TextConsole;

public class MultiReleaseLaunchTests extends AbstractDebugUiTests {

	public MultiReleaseLaunchTests(String name) {
		super(name);
	}

	@Override
	protected IJavaProject getProjectContext() {
		return getMultireleaseProject();
	}

	public void testMultiReleaseLaunch() throws Exception {
		ILaunchConfiguration config = getLaunchConfiguration("p.Main");
		Properties result = launchAndReadResult(config, 11);
		assertEquals("X should be executed from Java 11 version: " + result, "11", result.get("X"));
		assertNull("Y should not be executed from Java 11 version: " + result, result.get("Y"));
		assertNull("Z should not be executed from Java 11 version: " + result, result.get("Z"));
		Properties result17 = launchAndReadResult(config, 17);
		assertEquals("X should be executed from Java 17 version: " + result17, "17", result17.get("X"));
		assertEquals("Y should be executed from Java 11 version: " + result17, "11", result17.get("Y"));
		assertNull("Z should not be executed from Java 17 version: " + result17, result17.get("Z"));
		Properties result21 = launchAndReadResult(config, 21);
		assertEquals("X should be executed from Java 17 version: " + result21, "17", result21.get("X"));
		assertEquals("Y should be executed from Java 21 version: " + result21, "21", result21.get("Y"));
		assertEquals("Z should be executed from Java 17 version: " + result21, "17", result21.get("Z"));
	}

	private Properties launchAndReadResult(ILaunchConfiguration config, int javaVersion) throws Exception {
		ILaunchConfigurationWorkingCopy workingCopy = config.getWorkingCopy();
		workingCopy.setAttribute("org.eclipse.jdt.launching.JRE_CONTAINER", "org.eclipse.jdt.launching.JRE_CONTAINER/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType/JavaSE-"
				+ javaVersion + "/");
		Properties properties = new Properties();
		IJavaDebugTarget target = launchAndTerminate(workingCopy.doSave(), DEFAULT_TIMEOUT);
		processUiEvents();
		final IConsole console = DebugUITools.getConsole(target.getProcess());
		final TextConsole textConsole = (TextConsole) console;
		final IDocument consoleDocument = textConsole.getDocument();
		String content = consoleDocument.get();
		properties.load(new StringReader(content));
		DebugPlugin.getDefault().getLaunchManager().removeLaunch(target.getLaunch());
		return properties;
	}

}
