/*******************************************************************************
 *  Copyright (c) 2018 Simeon Andreev and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *     Simeon Andreev - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.internal.ui.views.console.ProcessConsole;
import org.eclipse.debug.internal.ui.views.launch.LaunchView;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.debug.core.IJavaMethodBreakpoint;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.testplugin.DebugElementKindEventDetailWaiter;
import org.eclipse.jdt.debug.testplugin.DebugEventWaiter;
import org.eclipse.jdt.debug.tests.TestAgainException;
import org.eclipse.jdt.debug.tests.TestUtil;
import org.eclipse.jdt.debug.ui.IJavaDebugUIConstants;
import org.eclipse.jdt.internal.debug.core.model.JDIStackFrame;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

public abstract class AbstractDebugViewTests extends AbstractDebugUiTests {

	public AbstractDebugViewTests(String name) {
		super(name);
	}

	private LaunchView debugView;
	private Boolean showMonitorsOriginal;
	private Boolean showSystemThreadsOriginal;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		IPreferenceStore jdiUIPreferences = JDIDebugUIPlugin.getDefault().getPreferenceStore();
		showMonitorsOriginal = jdiUIPreferences.getBoolean(IJavaDebugUIConstants.PREF_SHOW_MONITOR_THREAD_INFO);
		showSystemThreadsOriginal = jdiUIPreferences.getBoolean(IJavaDebugUIConstants.PREF_SHOW_SYSTEM_THREADS);
		jdiUIPreferences.setValue(IJavaDebugUIConstants.PREF_SHOW_MONITOR_THREAD_INFO, true);
		resetPerspective(DebugViewPerspectiveFactory.ID);
		debugView = sync(() -> (LaunchView) getActivePage().showView(IDebugUIConstants.ID_DEBUG_VIEW));
		processUiEvents(100);
	}

	@Override
	protected void tearDown() throws Exception {
		IPreferenceStore jdiUIPreferences = JDIDebugUIPlugin.getDefault().getPreferenceStore();
		jdiUIPreferences.setValue(IJavaDebugUIConstants.PREF_SHOW_MONITOR_THREAD_INFO, showMonitorsOriginal);
		jdiUIPreferences.setValue(IJavaDebugUIConstants.PREF_SHOW_SYSTEM_THREADS, showSystemThreadsOriginal);
		sync(() -> getActivePage().closeAllEditors(false));
		processUiEvents(100);
		super.tearDown();
	}

	/**
	 * Debugs the specified snippet a number of times, with a breakpoint at the specified method. Fails if the {@code Debug View} does not have a
	 * stack frame selected upon reaching the breakpoint.
	 *
	 * @param iterations
	 *            the number of iterations to run, high enough to detect sporadic issues
	 * @param typeName
	 *            the type name of the snippet, found under {@code testprogram/} in this test plug-in
	 * @param breakpointMethodName
	 *            the name of the method in which to set a breakpoint
	 */
	protected void doTestWrongSelection(int iterations, String typeName, String breakpointMethodName) throws Exception {
		doTestWrongSelection(iterations, typeName, breakpointMethodName, 1);
	}

	/**
	 * Debugs the specified snippet a number of times, with a breakpoint at the specified method. Fails if the {@code Debug View} does not have a
	 * stack frame selected upon reaching the breakpoint.
	 *
	 * @param iterations
	 *            the number of iterations to run, high enough to detect sporadic issues
	 * @param typeName
	 *            the type name of the snippet, found under {@code testprogram/} in this test plug-in
	 * @param breakpointMethodName
	 *            the name of the method in which to set a breakpoint
	 * @param expectedBreakpointHitsCount
	 *            the number of times that the breakpoint is expected to be hit
	 */
	protected void doTestWrongSelection(int iterations, String typeName, String breakpointMethodName, int expectedBreakpointHitsCount) throws Exception {
		waitForNonConsoleJobs();
		assertNoErrorMarkersExist();
		setPreferenceToShowSystemThreads();

		// Collect failures to have a better idea of what broke and how many times it broke
		List<AssertionError> failedAssertions = new ArrayList<>();
		for (int i = 0; i < iterations; ++i) {

			IJavaThread thread = null;
			try {
				thread = launchToBreakpoint(typeName, breakpointMethodName, expectedBreakpointHitsCount);

				assertDebugViewIsOpen();
				assertStackFrameIsSelected(breakpointMethodName);
			} catch (AssertionError assertionError) {
				failedAssertions.add(assertionError);
			} finally {
				terminateAndCleanUp(thread);
			}
		}

		assertEquals("expected no assertions to fail during test", Collections.emptyList(), failedAssertions);
	}

	protected void setPreferenceToShowSystemThreads() {
		IPreferenceStore preferenceStore = JDIDebugUIPlugin.getDefault().getPreferenceStore();
		preferenceStore.setValue(IJavaDebugUIConstants.PREF_SHOW_SYSTEM_THREADS, true);
	}

	protected IJavaThread launchToBreakpoint(String typeName, String breakpointMethodName, int expectedBreakpointHitsCount) throws Exception, CoreException {
		createMethodBreakpoint(typeName, breakpointMethodName);
		IJavaThread thread = launchToBreakpoint(typeName, expectedBreakpointHitsCount);

		// Let now all pending jobs proceed, ignore console jobs
		waitForNonConsoleJobs();

		return thread;
	}

	protected void assertStackFrameIsSelected(String breakpointMethodName) throws Exception {
		// Get and check the selection form the tree, we expect only one method selected
		TreeItem[] selected = getSelectedItemsFromDebugView(true);
		Object[] selectedText = selectedText(selected);
		if (selected.length != 1) {
			if (Platform.getOS().equals(Platform.OS_MACOSX)) {
				// skip this test on Mac - see bug 516024
				return;
			}
			throw new TestAgainException("Unexpected selection: " + Arrays.toString(selectedText));
		}
		assertEquals("Unexpected selection: " + Arrays.toString(selectedText), 1, selected.length);
		IJavaStackFrame selectedFrame = selectedFrame(selected);

		assertEquals("\"breakpointMethod\" should be selected after reaching breakpoint", selectedFrame.getMethodName(), breakpointMethodName);

	}

	@Override
	protected boolean enableUIEventLoopProcessingInWaiter() {
		// After fixes for bug 535686 and 534319 we do not depend on proper event processing in the UI
		// so we can allow UI thread to be blocked while waiting.
		// Note: if this test start to fail, we still have some issues with event dispatching in debug UI.
		return false;
	}

	protected void createMethodBreakpoint(final String typeName, final String breakpointMethodName) throws Exception, CoreException {
		IJavaBreakpoint bp = createMethodBreakpoint(typeName, breakpointMethodName, "()V", true, false);
		bp.setSuspendPolicy(IJavaBreakpoint.SUSPEND_THREAD);
	}

	protected IJavaThread launchToBreakpoint(String typeName, int expectedBreakpointHitsCount) throws Exception {
		ILaunchConfiguration config = getLaunchConfiguration(getProjectContext(), typeName);
		assertNotNull("Could not locate launch configuration for " + typeName, config); //$NON-NLS-1$

		DebugEventWaiter waiter = new BreakpointWaiter(expectedBreakpointHitsCount);
		waiter.setTimeout(DEFAULT_TIMEOUT);
		waiter.setEnableUIEventLoopProcessing(enableUIEventLoopProcessingInWaiter());

		boolean registerLaunch = true;
		Object suspendee = launchAndWait(config, waiter, registerLaunch);
		IJavaThread thread = (IJavaThread) suspendee;
		processUiEvents(100);

		// Prepare breakpoint and check everything below UI is OK
		assertSuspendedInJavaMethod(thread);
		return thread;
	}

	protected void assertSuspendedInJavaMethod(IJavaThread thread) {
		IBreakpoint hit = getBreakpoint(thread);
		assertNotNull("Suspended, but not by breakpoint", hit);
		assertTrue("Breakpoint was not a method breakpoint", hit instanceof IJavaMethodBreakpoint);
	}

	protected void waitForNonConsoleJobs() throws Exception {
		sync(() -> TestUtil.waitForJobs(getName(), 1_000, 30_000, ProcessConsole.class));
		processUiEvents(100);
	}

	protected Object[] selectedText(TreeItem[] selected) throws Exception {
		Object[] selectedText = sync(() -> Arrays.stream(selected).map(x -> x.getText()).toArray());
		return selectedText;
	}

	protected IJavaStackFrame selectedFrame(TreeItem[] selected) throws Exception {
		TreeItem selectedTreeItem = selected[selected.length - 1];
		IJavaStackFrame selectedFrame = (IJavaStackFrame) sync(() -> {
			Object data = selectedTreeItem.getData();
			assertNotNull("No data for selected frame in the tree?", data);
			assertEquals("Wrong object selected: " + data, JDIStackFrame.class, data.getClass());
			return data;
		});
		return selectedFrame;
	}

	protected void terminateAndCleanUp(IJavaThread thread) throws Exception {
		terminateAndRemove(thread);
		removeAllBreakpoints();
		waitForNonConsoleJobs();
	}

	protected String dumpFrames(Object[] childrenData) {
		return Arrays.toString(Arrays.stream(childrenData).map(x -> Objects.toString(x)).toArray());
	}

	protected TreeItem[] getSelectedItemsFromDebugView(boolean wait) throws Exception {
		return sync(() -> {
			Tree tree = (Tree) debugView.getViewer().getControl();
			TreeItem[] selected = tree.getSelection();
			if (!wait) {
				return selected;
			}
			long start = System.currentTimeMillis();

			// At least on GTK3 it takes some time until we see the viewer selection propagated to the SWT tree
			while (selected.length != 1 && System.currentTimeMillis() - start < 10000) {
				TreeViewer treeViewer = (TreeViewer) debugView.getViewer();
				treeViewer.refresh(true);
				processUiEvents(500);
				TestUtil.log(IStatus.INFO, getName(), "Waiting for selection, current size: " + selected.length);
				selected = tree.getSelection();
			}
			return selected;
		});
	}

	protected ISelection getDebugViewSelection() throws Exception {
		return debugView.getViewer().getSelection();
	}

	protected void setDebugViewSelection(IThread thread) throws Exception {
		IStackFrame frame = thread.getTopStackFrame();
		IDebugTarget debugTarget = thread.getDebugTarget();
		ILaunch launch = debugTarget.getLaunch();

		Object[] segments = new Object[] { launch, debugTarget, thread, frame };
		TreePath newPath = new TreePath(segments);
		TreeSelection newSelection = new TreeSelection(newPath);
		debugView.getViewer().setSelection(newSelection);

		waitForNonConsoleJobs();
	}

	protected void activateDebugView() throws Exception {
		sync(() -> getActivePage().activate(debugView));
	}

	protected void assertDebugViewIsOpen() throws Exception {
		debugView = sync(() -> (LaunchView) getActivePage().findView(IDebugUIConstants.ID_DEBUG_VIEW));
		assertNotNull("expected Debug View to be open", debugView);
	}

	protected void assertDebugViewIsActive() throws Exception {
		assertEquals("expected Debug View to be activate", debugView, sync(() -> getActivePage().getActivePart()));
	}

	protected static class BreakpointWaiter extends DebugElementKindEventDetailWaiter {

		private final int expectedHitCount;
		private int hitCount;

		public BreakpointWaiter(int expectedHitCount) {
			super(DebugEvent.SUSPEND, IJavaThread.class, DebugEvent.BREAKPOINT);
			this.expectedHitCount = expectedHitCount;
			assertTrue("expected hit count should be positive, instead is: " + expectedHitCount, expectedHitCount > 0);
			hitCount = 0;
		}

		@Override
		public boolean accept(DebugEvent event) {
			if (super.accept(event)) {
				hitCount++;
			}
			return hitCount == expectedHitCount;
		}
	}
}