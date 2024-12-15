/*******************************************************************************
 * Copyright (c) 2020 Gayan Perera and others.
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
 *     IBM Corporation - bugfixes
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.eval;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.internal.ui.stringsubstitution.SelectedResourceManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.contexts.IDebugContextListener;
import org.eclipse.debug.ui.contexts.IDebugContextProvider;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.debug.testplugin.JavaProjectHelper;
import org.eclipse.jdt.debug.testplugin.JavaTestPlugin;
import org.eclipse.jdt.debug.tests.ui.AbstractDebugUiTests;
import org.eclipse.jdt.internal.debug.ui.contentassist.CurrentFrameContext;
import org.eclipse.jdt.internal.debug.ui.contentassist.JavaDebugContentAssistProcessor;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;

public class SyntheticVariableTests extends AbstractDebugUiTests {

	private IJavaThread javaThread;
	private IDebugContextProvider debugContextProvider;
	private IJavaProject project;

	public SyntheticVariableTests(String name) {
		super(name);
		debugContextProvider = new IDebugContextProvider() {
			@Override
			public void removeDebugContextListener(IDebugContextListener listener) {
			}

			@Override
			public IWorkbenchPart getPart() {
				return null;
			}

			@Override
			public ISelection getActiveContext() {
				try {
					return new StructuredSelection(javaThread.getTopStackFrame());
				} catch (DebugException e) {
					return null;
				}
			}

			@Override
			public void addDebugContextListener(IDebugContextListener listener) {
			}
		};
	}

	public void testEvaluateMethodParameter_DeepInTwoNestedClasses() throws Exception {
		addClasses();
		createBreakPoint(31);
		try {
			javaThread = launchToBreakpoint("SyntheticTest");

			IValue value = doEval(javaThread, "predicate");

			assertNotNull("type is null : ", value.getReferenceTypeName());
			assertTrue("Not expected lambda type : ", value.getReferenceTypeName().startsWith("SyntheticTest$$Lambda"));
			assertNotNull("value is null :", value.getValueString());
		} finally {
			terminateAndRemove(javaThread);
			removeAllBreakpoints();
		}
	}

	public void testCompleteMethodParameter_DeepInTwoNestedClasses() throws Exception {
		if (Platform.OS.isMac()) {
			return;
		}
		addClasses();
		createBreakPoint(31);
		try {
			javaThread = launchToBreakpoint("SyntheticTest");
			registerContextProvider();
			waitForBuild();
			List<ICompletionProposal> proposals = computeCompletionProposals(" ", 0);

			assertNotNull("proposals are null : ", proposals);
			assertFalse("proposals are empty : ", proposals.isEmpty());
			System.out.println(proposals);
			assertTrue("expected variable is not in proposals :", proposals.stream().anyMatch(p -> p.getDisplayString().equals("predicate : Predicate")));
		} finally {
			unregisterContextProvider();
			terminateAndRemove(javaThread);
			removeAllBreakpoints();
		}
	}

	private List<ICompletionProposal> computeCompletionProposals(String source, int completionIndex) throws Exception {
		JavaDebugContentAssistProcessor comp = new JavaDebugContentAssistProcessor(new CurrentFrameContext());
		ICompletionProposal[] proposals = sync(new Callable<ICompletionProposal[]>() {

			@Override
			public ICompletionProposal[] call() throws Exception {
				ITextViewer viewer = new TextViewer(Display.getDefault().getActiveShell(), SWT.NONE);
				viewer.setDocument(new Document(source));
				return comp.computeCompletionProposals(viewer, completionIndex);
			}
		});
		assertNull(String.format("Has errors : %s", comp.getErrorMessage()), comp.getErrorMessage());
		assertNotNull("proposals are null", proposals);

		return Arrays.asList(proposals);
	}

	private void createBreakPoint(int lineNumber) throws CoreException {
		JDIDebugModel.createLineBreakpoint(getProjectContext().getProject(), "SyntheticTest", lineNumber, -1, -1, 0, true, null);
	}

	@Override
	protected IJavaProject getProjectContext() {
		if (project == null) {
			try {
				project = createProject("Synthetic", "testresources/synthetic/src", JavaProjectHelper.JAVA_SE_1_8_EE_NAME, true);
			} catch (Exception e) {
				fail(e.getMessage());
			}
		}
		return project;
	}

	private void addClasses() throws Exception {
		IPath bin = getProjectContext().getPath().append(JavaProjectHelper.BIN_DIR).makeAbsolute();
		File classDir = JavaTestPlugin.getDefault().getFileInPlugin(new Path("testresources/synthetic/bin"));
		JavaProjectHelper.importFilesFromDirectory(classDir, bin, null);
		createLaunchConfiguration("SyntheticTest");
	}

	private void registerContextProvider() {
		IWorkbenchWindow activeWindow = SelectedResourceManager.getDefault().getActiveWindow();
		assertNotNull("activeWindow is null", activeWindow);
		DebugUITools.getDebugContextManager().getContextService(activeWindow).addDebugContextProvider(debugContextProvider);
	}

	private void unregisterContextProvider() {
		IWorkbenchWindow activeWindow = SelectedResourceManager.getDefault().getActiveWindow();
		assertNotNull("activeWindow is null", activeWindow);
		DebugUITools.getDebugContextManager().getContextService(activeWindow).removeDebugContextProvider(debugContextProvider);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		if (project != null && project.getProject() != null) {
			project.getProject().delete(true, null);
		}
		project = null;
	}
}
