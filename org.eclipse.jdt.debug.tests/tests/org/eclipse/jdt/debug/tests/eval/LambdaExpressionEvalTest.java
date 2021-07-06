/*******************************************************************************
 * Copyright (c) 2021 Gayan Perera and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Gayan Perera - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.eval;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.testplugin.JavaProjectHelper;
import org.eclipse.jdt.debug.testplugin.JavaTestPlugin;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;

public class LambdaExpressionEvalTest extends AbstractDebugTest {
	private IJavaThread javaThread;

	public LambdaExpressionEvalTest(String name) {
		super(name);
	}

	@Override
	protected IJavaProject getProjectContext() {
		return get18Project();
	}

	public void testBug573589_EvalLambdaExpressionInSourceType_WithStaticImportInImports_ExpectSuccessfulEval() throws Exception {
		debugWithBreakpoint("Bug573589", 9);
		IValue value = doEval(javaThread, "Stream.of(1,2,3).filter(i -> i > 2).map(i -> i * 2).count();");

		assertNotNull("value is null", value);
		assertEquals("value is not 1", "1", value.getValueString());
	}

	public void testBug573589_EvalLambdaExpressionInBinaryType_WithStaticImportInImports_ExpectSuccessfulEval() throws Exception {
		IPath container = new Path(JavaTestPlugin.getDefault().getFileInPlugin(new Path("./testresources/bug573589/classes")).getAbsolutePath());
		JavaProjectHelper.addLibrary(getProjectContext(), container);
		waitForBuild();

		ILaunchConfiguration launchConfiguration = null;
		try {
			launchConfiguration = createLaunchConfiguration(getProjectContext(), "Bug573589Bin");
			debugWithBreakpoint("Bug573589Bin", 9);
			IValue value = doEval(javaThread, "java.util.stream.Stream.of(1,2,3).filter(i -> i > 2).map(i -> i * 2).count();");
			assertNotNull("value is null", value);
			assertEquals("value is not 1", "1", value.getValueString());
		} finally {
			if (launchConfiguration != null) {
				launchConfiguration.delete();
			}
			JavaProjectHelper.removeFromClasspath(getProjectContext(), container);
		}
	}

	public void testBug573589_EvalLambdaExpressionInBinaryTypeAttachedSource_WithStaticImportInImports_ExpectSuccessfulEval() throws Exception {
		IPath container = new Path(JavaTestPlugin.getDefault().getFileInPlugin(new Path("./testresources/bug573589/classes")).getAbsolutePath());
		IPath src = new Path(JavaTestPlugin.getDefault().getFileInPlugin(new Path("./testresources/bug573589/src")).getAbsolutePath());
		JavaProjectHelper.addLibrary(getProjectContext(), container, src, container);
		waitForBuild();

		ILaunchConfiguration launchConfiguration = null;
		try {
			launchConfiguration = createLaunchConfiguration(getProjectContext(), "Bug573589Bin");
			debugWithBreakpoint("Bug573589Bin", 9);
			IValue value = doEval(javaThread, "Stream.of(1,2,3).filter(i -> i > 2).map(i -> i * 2).count();");
			assertNotNull("value is null", value);
			assertEquals("value is not 1", "1", value.getValueString());
		} finally {
			if (launchConfiguration != null) {
				launchConfiguration.delete();
			}
			JavaProjectHelper.removeFromClasspath(getProjectContext(), container);
		}
	}

	public void testBug571310_EvalLambdaWithPublicMethodInvocation_ExpectSuccessfulEval() throws Exception {
		debugWithBreakpoint("Bug571310", 12);
		resume(javaThread);
		IValue value = doEval(javaThread, "this.selfAppend(f) + \".00\"");

		assertNotNull("value is null", value);
		assertEquals("value is not 22.00", "22.00", value.getValueString());
	}

	public void testBug571310_EvalLambdaWithPrivateMethodInvocation_ExpectSuccessfulEval() throws Exception {
		debugWithBreakpoint("Bug571310", 13);
		resume(javaThread);
		IValue value = doEval(javaThread, "this.appendDollar(f) + \"0\"");

		assertNotNull("value is null", value);
		assertEquals("value is not $22.00", "$22.00", value.getValueString());
	}

	private void debugWithBreakpoint(String testClass, int lineNumber) throws Exception {
		createLineBreakpoint(lineNumber, testClass);
		javaThread = launchToBreakpoint(testClass);
		assertNotNull("The program did not suspend", javaThread);
	}

	@Override
	protected void tearDown() throws Exception {
		try {
			terminateAndRemove(javaThread);
		} finally {
			super.tearDown();
			removeAllBreakpoints();
		}
	}
}
