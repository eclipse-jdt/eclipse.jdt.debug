/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.JavaLaunchDelegate;
import org.eclipse.jdt.launching.JavaRuntime;

/**
 * Tests bootpath settings
 */
public class BootpathTests extends AbstractDebugTest {

	public BootpathTests(String name) {
		super(name);
	}

	public void testDefaultBootpath() throws Exception {
		ILaunchConfiguration config = getLaunchConfiguration("Breakpoints");

		JavaLaunchDelegate delegate = new JavaLaunchDelegate();
		String[] path = delegate.getBootpath(config);
		assertNull("Default bootpath should be null", path);
		String[][] pathInfo= delegate.getBootpathExt(config);
		assertNotNull("Bootpath info shouldn't be null", pathInfo);
		assertEquals("Wrong bootpath info array size",3, pathInfo.length);
		assertNull("Prepend bootpath should be null", pathInfo[0]);
		assertNull("Main bootpath should be null", pathInfo[1]);
		assertNull("Append bootpath should be null", pathInfo[2]);
	}

	public void testEmptyBootpath() throws Exception {
		ILaunchConfiguration config = getLaunchConfiguration("Breakpoints");
		ILaunchConfigurationWorkingCopy wc = config.getWorkingCopy();

		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH, false);

		JavaLaunchDelegate delegate = new JavaLaunchDelegate();
		String[] path = delegate.getBootpath(wc);
		assertNotNull("Empty bootpath should be empty array", path);
		assertEquals("bootpath should be empty", 0, path.length);
		String[][] pathInfo= delegate.getBootpathExt(config);
		assertNotNull("Bootpath info should'nt be null", pathInfo);
		assertEquals("Wrong bootpath info array size",3, pathInfo.length);
		assertNull("Prepend bootpath should be null", pathInfo[0]);
		assertNull("Main bootpath should be empty array", pathInfo[1]);
		assertNull("Append bootpath should be null", pathInfo[2]);
	}

	public void testPrependBootpath() throws Exception {
		ILaunchConfiguration config = getLaunchConfiguration("Breakpoints");
		ILaunchConfigurationWorkingCopy wc = config.getWorkingCopy();
		IRuntimeClasspathEntry[] classpath = JavaRuntime.computeUnresolvedRuntimeClasspath(wc);
		IRuntimeClasspathEntry[] newpath = new IRuntimeClasspathEntry[classpath.length + 1];
		IResource jar = get14Project().getProject().getFile(new Path("src/A.jar"));
		IRuntimeClasspathEntry pre = JavaRuntime.newArchiveRuntimeClasspathEntry(jar);
		pre.setClasspathProperty(IRuntimeClasspathEntry.BOOTSTRAP_CLASSES);
		newpath[0] = pre;
		System.arraycopy(classpath, 0, newpath, 1, classpath.length);
		List<String> mementos = new ArrayList<>(newpath.length);
		for (int i = 0; i < newpath.length; i++) {
			IRuntimeClasspathEntry entry = newpath[i];
			mementos.add(entry.getMemento());
		}

		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH, false);
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH, mementos);

		JavaLaunchDelegate delegate = new JavaLaunchDelegate();
		Map<String, Object> map = delegate.getVMSpecificAttributesMap(wc);
		assertNotNull("Missing VM specific attributes map", map);
		String[] prepath = (String[]) map.get(IJavaLaunchConfigurationConstants.ATTR_BOOTPATH_PREPEND);
		assertNotNull("Missing bootpath prepend", pre);
		assertEquals("Incorrect number of prepends", 1, prepath.length);
		assertEquals("wrong prepended path", jar.getLocation().toOSString(), prepath[0]);
	}

}
