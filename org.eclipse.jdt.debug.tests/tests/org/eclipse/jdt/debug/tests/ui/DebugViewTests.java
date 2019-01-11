/*******************************************************************************
 *  Copyright (c) 2018 Andrey Loskutov and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     Andrey Loskutov <loskutov@gmx.de> - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.ui;

import java.util.Arrays;

import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.tests.TestAgainException;
import org.eclipse.jdt.debug.ui.IJavaDebugUIConstants;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.test.OrderedTestSuite;
import org.eclipse.ui.IViewPart;

import junit.framework.Test;

/**
 * Tests for debug view.
 */
public class DebugViewTests extends AbstractDebugViewTests {

	public static Test suite() {
		return new OrderedTestSuite(DebugViewTests.class);
	}

	public DebugViewTests(String name) {
		super(name);
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
			thread = launchToBreakpoint(typeName);

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
		} finally {
			terminateAndCleanUp(thread);
		}
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
	 * Test for Bug 538303 - Debug View shows wrong selection if switched to by breakpoint hit
	 *
	 * When hitting a breakpoint, if the Debug View is not active in its part stack, its possible to see a wrong selection in the Debug View. To
	 * ensure this doesn't occur, this test does the following:
	 *
	 * <ol>
	 * <li>create a Java snippet which starts a threads</li>
	 * <li>set a break point in a loop which is run by the thread</li>
	 * <li>debug the snippet until the break point is reached</li>
	 * <li>validate that the selection in the Debug View contains is exactly the method with a break point</li>
	 * <li>switch to another view in the same part stack as the Debug View</li>
	 * <li>resume the suspended thread</li>
	 * <li>validate that the selection in the Debug View contains is exactly the method with a break point</li>
	 * <li>terminate and remove break point</li>
	 * </ol>
	 */
	public void testWrongSelectionBug538303() throws Exception {
		String typeName = "Bug538303";
		String breakpointMethodName = "breakpointMethod";
		int expectedBreakpointHitsCount = 1;

		IViewPart anotherView = sync(() -> getActivePage().showView(ViewManagementTests.VIEW_THREE));
		activateDebugView();

		waitForNonConsoleJobs();
		assertNoErrorMarkersExist();
		setPreferenceToShowSystemThreads();

		IJavaThread thread = null;
		try {
			thread = launchToBreakpoint(typeName, breakpointMethodName, expectedBreakpointHitsCount);

			assertStackFrameIsSelected(breakpointMethodName);

			sync(() -> getActivePage().activate(anotherView));
			waitForNonConsoleJobs();

			thread.resume();
			waitForNonConsoleJobs();
			assertDebugViewIsActive();

			assertStackFrameIsSelected(breakpointMethodName);
		} finally {
			terminateAndCleanUp(thread);
			sync(() -> getActivePage().hideView(anotherView));
			activateDebugView();
		}
	}

	/**
	 * Test for Bug 540243 - Wrong selection when first opening view due to breakpoint
	 *
	 * When hitting a breakpoint, if the Debug View is open at all, and we show Java thread owned monitors, its possible to see a wrong selection in
	 * the Debug View. To ensure this doesn't occur, this test does the following:
	 *
	 * <ol>
	 * <li>ensures the Debug View is showing owned monitors for threads</li>
	 * <li>close the Debug View</li>
	 * <li>create a Java snippet which starts a thread</li>
	 * <li>set a break point in the code executed by the thread</li>
	 * <li>debug the snippet until the break point is reached</li>
	 * <li>validate that the selection in the Debug View contains is exactly the method with a break point</li>
	 * </ol>
	 */
	public void testWrongSelectionBug540243() throws Exception {
		IPreferenceStore jdiUIPreferences = JDIDebugUIPlugin.getDefault().getPreferenceStore();
		Boolean isShowingMonitorThreadInfo = jdiUIPreferences.getBoolean(IJavaDebugUIConstants.PREF_SHOW_MONITOR_THREAD_INFO);
		assertNotNull("Preference to show thread owned monitors must be set but is not", isShowingMonitorThreadInfo);
		assertTrue("Preference to show thread owned monitors must be enabled but is not", isShowingMonitorThreadInfo);

		sync(() -> getActivePage().hideView(getActivePage().findView(IDebugUIConstants.ID_DEBUG_VIEW)));

		int iterations = 1;
		String typeName = "Bug540243";
		String breakpointMethodName = "breakpointMethod";
		int expectedBreakpointHitsCount = 1;
		doTestWrongSelection(iterations, typeName, breakpointMethodName, expectedBreakpointHitsCount);
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
		doTestWrongSelection(iterations, typeName, breakpointMethodName);
	}
}
