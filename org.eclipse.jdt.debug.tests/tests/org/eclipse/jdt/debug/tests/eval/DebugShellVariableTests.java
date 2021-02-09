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

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.internal.ui.stringsubstitution.SelectedResourceManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.contexts.IDebugContextListener;
import org.eclipse.debug.ui.contexts.IDebugContextProvider;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.debug.core.IJavaThread;
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

public class DebugShellVariableTests extends AbstractDebugUiTests {

	private IJavaThread javaThread;
	private IDebugContextProvider debugContextProvider;

	public DebugShellVariableTests(String name) {
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
					if (javaThread == null) {
						return StructuredSelection.EMPTY;
					}

					return new StructuredSelection(javaThread.getTopStackFrame());
				} catch (DebugException e) {
					return StructuredSelection.EMPTY;
				}
			}

			@Override
			public void addDebugContextListener(IDebugContextListener listener) {
			}
		};
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		registerContextProvider();
	}

	@Override
	protected void tearDown() throws Exception {
		unregisterContextProvider();
		super.tearDown();
	}

	public void testComplete_OnInnerClassTypeVariable_ExpectTypeMemberCompletions() throws Exception {
		try {
			debugWithBreakpoint("a.b.c.Bug570988", 27);

			List<ICompletionProposal> proposals = computeCompletionProposals("entry.", 6);

			assertFalse("proposals are empty : ", proposals.isEmpty());
			assertTrue("expected method[getKey() : String - Entry] is not in proposals :", proposals.stream().anyMatch(p -> p.getDisplayString().equals("getKey() : String - Entry")));
		} finally {
			terminateAndRemove(javaThread);
			removeAllBreakpoints();
		}
	}

	public void testComplete_OnGenericClassTypeVariable_ExpectTypeMemberWithCompletionsTypeArguments() throws Exception {
		try {
			debugWithBreakpoint("a.b.c.Bug570988", 27);

			List<ICompletionProposal> proposals = computeCompletionProposals("entry.", 6);

			assertFalse("proposals are empty : ", proposals.isEmpty());
			assertTrue("expected method[getKey() : String - Entry] is not in proposals :", proposals.stream().anyMatch(p -> p.getDisplayString().equals("getKey() : String - Entry")));
			assertTrue("expected method [getValue() : Long - Entry] is not in proposals :", proposals.stream().anyMatch(p -> p.getDisplayString().equals("getKey() : String - Entry")));
		} finally {
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

	private void debugWithBreakpoint(String testClass, int lineNumber) throws Exception {
		createLineBreakpoint(lineNumber, testClass);
		javaThread = launchToBreakpoint(testClass);
		assertNotNull("The program did not suspend", javaThread);
	}

	@Override
	protected IJavaProject getProjectContext() {
		return get15Project();
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
}
