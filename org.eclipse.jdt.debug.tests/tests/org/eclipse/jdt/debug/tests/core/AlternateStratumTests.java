/*******************************************************************************
 * Copyright (c) 2000, 2018 IBM Corporation and others.
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

import java.io.File;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.debug.core.IJavaReferenceType;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.debug.testplugin.JavaProjectHelper;
import org.eclipse.jdt.debug.testplugin.JavaTestPlugin;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.internal.debug.core.JavaDebugUtils;

/**
 * Tests strata.
 */
public class AlternateStratumTests extends AbstractDebugTest {

	public AlternateStratumTests(String name) {
		super(name);
	}

	/**
	 * Test available strata on a type with alternate strata
	 *
	 * @throws Exception
	 */
	public void testAvailableStrata() throws Exception {
		String typeName = "HelloWorld";
		prepareXtendBreakpoint(typeName, 3);

		IJavaThread thread = null;
		try {
			thread = launchToBreakpoint(typeName);
			assertNotNull("Breakpoint not hit within timeout period", thread);
			IJavaReferenceType type = ((IJavaStackFrame) thread.getTopStackFrame()).getReferenceType();
			String[] strata = type.getAvailableStrata();
			assertEquals("Wrong number of available strata", 2, strata.length);
			assertEquals("Wrong strata", "Xtend", strata[0]);
			assertEquals("Wrong strata", "Java", strata[1]);
		}
		finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * Test default stratum on a type with alternate strata.
	 *
	 * @throws Exception
	 */
	public void testDefaultStratum() throws Exception {
		String typeName = "HelloWorld";
		prepareXtendBreakpoint(typeName, 3);

		IJavaThread thread = null;
		try {
			thread = launchToBreakpoint(typeName);
			assertNotNull("Breakpoint not hit within timeout period", thread);
			IJavaReferenceType type = ((IJavaStackFrame) thread.getTopStackFrame()).getReferenceType();
			String stratum = type.getDefaultStratum();
			assertEquals("Wrong strata", "Xtend", stratum);
		}
		finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	public void testGetSourceNameInStratum2() throws Exception {
		String typeName = "HelloWorld";
		prepareXtendBreakpoint(typeName, 3);

		IJavaThread thread = null;
		try {
			thread = launchToBreakpoint(typeName);
			assertNotNull("Breakpoint not hit within timeout period", thread);
			IJavaStackFrame stackFrame = (IJavaStackFrame) thread.getTopStackFrame();
			String sourceName = stackFrame.getSourceName("Xtend");
			assertEquals("Wrong source name", "HelloWorld.xtend", sourceName);
		}
		finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	public void testResolveJavaElement() throws Exception {
		String typeName = "HelloWorld";
		prepareXtendBreakpoint(typeName, 3);

		IJavaThread thread = null;
		try {
			thread = launchToBreakpoint(typeName);
			assertNotNull("Breakpoint not hit within timeout period", thread);
			IJavaStackFrame stackFrame = (IJavaStackFrame) thread.getTopStackFrame();
			IJavaElement javaElement = JavaDebugUtils.resolveJavaElement(stackFrame, stackFrame.getLaunch());
			assertEquals("Wrong java element", "HelloWorld.java", javaElement.getElementName());
		}
		finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	public void testResolveJavaProject() throws Exception {
		String typeName = "HelloWorld";
		prepareXtendBreakpoint(typeName, 3);

		IJavaThread thread = null;
		try {
			thread = launchToBreakpoint(typeName);
			assertNotNull("Breakpoint not hit within timeout period", thread);
			IJavaStackFrame stackFrame = (IJavaStackFrame) thread.getTopStackFrame();
			IJavaProject project = JavaDebugUtils.resolveJavaProject(stackFrame);
			assertEquals("Wrong java project", getProjectContext(), project);
		}
		finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	public void testResolveType() throws Exception {
		String typeName = "HelloWorld";
		prepareXtendBreakpoint(typeName, 3);

		IJavaThread thread = null;
		try {
			thread = launchToBreakpoint(typeName);
			assertNotNull("Breakpoint not hit within timeout period", thread);
			IJavaStackFrame stackFrame = (IJavaStackFrame) thread.getTopStackFrame();
			IJavaReferenceType refType = stackFrame.getReferenceType();
			IType type = JavaDebugUtils.resolveType(refType);
			assertEquals("Wrong type", "HelloWorld", type.getElementName());
		}
		finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	@Override
	protected IJavaProject getProjectContext() {
		return get15Project();
	}

	private void prepareXtendBreakpoint(String typeName, int lineNumber) throws JavaModelException, Exception, CoreException {
		addXtendClass(typeName);
		JDIDebugModel.createStratumBreakpoint(getProjectContext().getProject(), "Xtend", null, null, typeName, lineNumber, -1, -1, 0, true, null);
	}

	private void addXtendClass(String typeName) throws Exception {
		IPath src = getProjectContext().getPath().append(JavaProjectHelper.SRC_DIR).makeAbsolute();
		File xtendSources = JavaTestPlugin.getDefault().getFileInPlugin(new Path("testresources/xtend-source"));
		JavaProjectHelper.importFile(new File(xtendSources, typeName + ".java"), src, null);
		JavaProjectHelper.importFile(new File(xtendSources, typeName + ".xtend"), src, null);
		createLaunchConfiguration(typeName);
		waitForBuild();
		IPath bin = getProjectContext().getPath().append(JavaProjectHelper.BIN_DIR).makeAbsolute();
		File xtendClasses = JavaTestPlugin.getDefault().getFileInPlugin(new Path("testresources/xtend"));
		JavaProjectHelper.importFile(new File(xtendClasses, typeName + ".class"), bin, null);
	}
}
