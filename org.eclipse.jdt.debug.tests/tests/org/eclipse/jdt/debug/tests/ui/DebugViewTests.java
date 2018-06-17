/*******************************************************************************
 *  Copyright (c) 2018 Andrey Loskutov and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *     Andrey Loskutov <loskutov@gmx.de> - initial API and implementation
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
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.internal.ui.views.console.ProcessConsole;
import org.eclipse.debug.internal.ui.views.launch.LaunchView;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.debug.core.IJavaMethodBreakpoint;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.tests.TestAgainException;
import org.eclipse.jdt.debug.tests.TestUtil;
import org.eclipse.jdt.debug.ui.IJavaDebugUIConstants;
import org.eclipse.jdt.internal.debug.core.model.JDIStackFrame;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.test.OrderedTestSuite;

import junit.framework.Test;

/**
 * Tests for debug view.
 */
public class DebugViewTests extends AbstractDebugUiTests {

	public static Test suite() {
		return new OrderedTestSuite(DebugViewTests.class);
	}

	private LaunchView debugView;
	private boolean showMonitorsOriginal;

	public DebugViewTests(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		IPreferenceStore jdiUIPreferences = JDIDebugUIPlugin.getDefault().getPreferenceStore();
		showMonitorsOriginal = jdiUIPreferences.getBoolean(IJavaDebugUIConstants.PREF_SHOW_MONITOR_THREAD_INFO);
		jdiUIPreferences.setValue(IJavaDebugUIConstants.PREF_SHOW_MONITOR_THREAD_INFO, true);
		resetPerspective(DebugViewPerspectiveFactory.ID);
		debugView = sync(() -> (LaunchView) getActivePage().showView(IDebugUIConstants.ID_DEBUG_VIEW));
		processUiEvents(100);
	}

	@Override
	protected void tearDown() throws Exception {
		IPreferenceStore jdiUIPreferences = JDIDebugUIPlugin.getDefault().getPreferenceStore();
		jdiUIPreferences.setValue(IJavaDebugUIConstants.PREF_SHOW_MONITOR_THREAD_INFO, showMonitorsOriginal);
		sync(() -> getActivePage().closeAllEditors(false));
		processUiEvents(100);
		super.tearDown();
	}

	public void testLastStackElementShown() throws Exception {
		final String typeName = "DropTests";
		final int expectedFramesNumber = 5;
		final String expectedMethod = "method" + (expectedFramesNumber - 1);
		createMethodBreakpoint(typeName, expectedMethod);

		// Open editor to avoid UI overhead on suspend
		sync(() -> openEditor(typeName + ".java"));

		IJavaThread thread = null;
		try {
			thread = launchMainToBreakpoint(typeName);

			IJavaStackFrame topFrame = (IJavaStackFrame) thread.getTopStackFrame();
			assertNotNull("There should be a stackframe", topFrame);
			assertEquals(expectedMethod, topFrame.getMethodName());

			IStackFrame[] frames = topFrame.getThread().getStackFrames();
			assertEquals(expectedFramesNumber, frames.length);

			IJavaStackFrame mainFrame = (IJavaStackFrame) frames[expectedFramesNumber - 1];
			assertEquals("First frame must be 'main'", "main", mainFrame.getMethodName());

			waitForNonConsoleJobs();

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
			TreeItem selectedTreeItem = selected[selected.length - 1];
			IJavaStackFrame selectedFrame = selectedFrame(selected);

			// DropTests.method5() should be selected
			assertEquals(selectedFrame.getMethodName(), topFrame.getMethodName());

			// Now we inspect the children of the stopped thread (parent element of selected method)
			TreeItem threadItem = sync(() -> selectedTreeItem.getParentItem());
			TreeItem[] children = sync(() -> threadItem.getItems());
			Object[] childrenText = sync(() -> Arrays.stream(children).map(x -> x.getText()).toArray());

			// we expect to see one monitor + frames
			final int expectedChildrenCount = expectedFramesNumber + 1;
			if (childrenText.length != expectedChildrenCount) {
				throw new TestAgainException("Not all frames shown: " + dumpFrames(childrenText));
			}
			assertEquals("Unexpected stack: " + dumpFrames(childrenText), expectedChildrenCount, childrenText.length);

			// This is too unstable, see bug 516024 comment 10

			// // Now we will check if the very first frame (main) is shown in the tree (on the bottom of the stack)
			// Object firstFrame = childrenText[expectedChildrenCount - 1].toString();
			//
			// String frameLabel = firstFrame.toString();
			// if (frameLabel.trim().isEmpty()) {
			// // Some times (see bug 516024 comment 7) tree items are there but they are "empty", let restart test
			// throw new TestAgainException("Tree children not rendered: " + dumpFrames(childrenText));
			// }
			//
			// assertTrue("Unexpected first frame: " + firstFrame + ", ALL: "
			// + dumpFrames(childrenText), frameLabel.contains("DropTests.main"));
		}
		finally {
			terminateAndCleanUp(thread);
		}
	}

	private void assertSuspendedInJavaMethod(IJavaThread thread) {
		IBreakpoint hit = getBreakpoint(thread);
		assertNotNull("Suspended, but not by breakpoint", hit);
		assertTrue("Breakpoint was not a method breakpoint", hit instanceof IJavaMethodBreakpoint);
	}

	public void testWrongSelectionBug534319singleThread() throws Exception {
		// Run a few times since the problem doesn't occur always
		int iterations = 5;
		final String typeName = "Bug534319singleThread";
		final String breakpointMethodName = "breakpointMethod";
		doTestWrongSelectionBug534319(iterations, typeName, breakpointMethodName);
	}

	public void testWrongSelectionBug534319earlyStart() throws Exception {
		// Run a few times since the problem doesn't occur always
		int iterations = 5;
		final String typeName = "Bug534319earlyStart";
		final String breakpointMethodName = "breakpointMethod";
		doTestWrongSelectionBug534319(iterations, typeName, breakpointMethodName);
	}

	public void testWrongSelectionBug534319lateStart() throws Exception {
		// Run a few times since the problem doesn't occur always
		int iterations = 5;
		final String typeName = "Bug534319lateStart";
		final String breakpointMethodName = "breakpointMethod";
		doTestWrongSelectionBug534319(iterations, typeName, breakpointMethodName);
	}

	public void testWrongSelectionBug534319startBetwen() throws Exception {
		// Run a few times since the problem doesn't occur always
		int iterations = 5;
		final String typeName = "Bug534319startBetwen";
		final String breakpointMethodName = "breakpointMethod";
		doTestWrongSelectionBug534319(iterations, typeName, breakpointMethodName);
	}

	/**
	 * Test for Bug 534319 - Debug View shows wrong information due to threads with short lifetime
	 *
	 * We observe that e.g. starting new threads from the debugged JVM can cause incorrect selections in the Debug View. To assure this doesn't occur,
	 * the test does the following multiple times:
	 *
	 * <ol>
	 * <li>create a Java snippet which starts some threads</li>
	 * <li>set a break point in a method which is run by the first snippet thread</li>
	 * <li>debug the snippet until the break point is reached</li>
	 * <li>validate that the selection in the Debug View contains is exactly the method with a break point</li>
	 * <li>terminate and remove break point</li>
	 * </ol>
	 */
	private void doTestWrongSelectionBug534319(int iterations, String typeName, String breakpointMethodName) throws Exception {
		waitForNonConsoleJobs();
		IPreferenceStore preferenceStore = JDIDebugUIPlugin.getDefault().getPreferenceStore();
		preferenceStore.setValue(IJavaDebugUIConstants.PREF_SHOW_SYSTEM_THREADS, true);

		// Collect failures to have a better idea of what broke
		List<AssertionError> failedAssertions = new ArrayList<>();
		for (int i = 0; i < iterations; ++i) {
			createMethodBreakpoint(typeName, breakpointMethodName);

			IJavaThread thread = null;
			try {
				thread = launchMainToBreakpoint(typeName);

				// Let now all pending jobs proceed, ignore console jobs
				waitForNonConsoleJobs();

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
			} catch (AssertionError assertionError) {
				failedAssertions.add(assertionError);
			} finally {
				terminateAndCleanUp(thread);
				waitForNonConsoleJobs();
			}
		}

		assertEquals("expected no assertions to fail during test", Collections.emptyList(), failedAssertions);
	}

	@Override
	protected boolean enableUIEventLoopProcessingInWaiter() {
		// After fixes for bug 535686 and 534319 we do not depend on proper event processing in the UI
		// so we can allow UI thread to be blocked while waiting.
		// Note: if this test start to fail, we still have some issues with event dispatching in debug UI.
		return false;
	}

	private void createMethodBreakpoint(final String typeName, final String breakpointMethodName) throws Exception, CoreException {
		IJavaBreakpoint bp = createMethodBreakpoint(typeName, breakpointMethodName, "()V", true, false);
		bp.setSuspendPolicy(IJavaBreakpoint.SUSPEND_THREAD);
	}

	private IJavaThread launchMainToBreakpoint(final String typeName) throws Exception {
		IJavaThread thread;
		thread = launchToBreakpoint(typeName);
		processUiEvents(100);

		// Prepare breakpoint and check everything below UI is OK
		assertSuspendedInJavaMethod(thread);
		return thread;
	}

	private void waitForNonConsoleJobs() throws Exception {
		sync(() -> TestUtil.waitForJobs(getName(), 1000, 10000, ProcessConsole.class));
		processUiEvents(100);
	}

	private Object[] selectedText(TreeItem[] selected) throws Exception {
		Object[] selectedText = sync(() -> Arrays.stream(selected).map(x -> x.getText()).toArray());
		return selectedText;
	}

	private IJavaStackFrame selectedFrame(TreeItem[] selected) throws Exception {
		TreeItem selectedTreeItem = selected[selected.length - 1];
		IJavaStackFrame selectedFrame = (IJavaStackFrame) sync(() -> {
			Object data = selectedTreeItem.getData();
			assertNotNull("No data for selected frame in the tree?", data);
			assertEquals("Wrong object selected: " + data, JDIStackFrame.class, data.getClass());
			return data;
		});
		return selectedFrame;
	}

	private void terminateAndCleanUp(IJavaThread thread) {
		terminateAndRemove(thread);
		removeAllBreakpoints();
	}

	private String dumpFrames(Object[] childrenData) {
		return Arrays.toString(Arrays.stream(childrenData).map(x -> Objects.toString(x)).toArray());
	}

	private TreeItem[] getSelectedItemsFromDebugView(boolean wait) throws Exception {
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

}
