/*******************************************************************************
 *  Copyright (c) 2017 Andrey Loskutov and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *     Andrey Loskutov <loskutov@gmx.de> - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.ui;

import java.util.Arrays;
import java.util.Objects;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.DebugException;
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
		IJavaBreakpoint bp = createMethodBreakpoint(typeName, expectedMethod, "()V", true, false);
		bp.setSuspendPolicy(IJavaBreakpoint.SUSPEND_THREAD);

		// Open editor to avoid UI overhead on suspend
		sync(() -> openEditor(typeName + ".java"));

		IJavaThread thread = null;
		try {
			thread = launchToBreakpoint(typeName);
			processUiEvents(100);

			// Prepare breakpoint and check everything below UI is OK
			IBreakpoint hit = getBreakpoint(thread);
			assertNotNull("Suspended, but not by breakpoint", hit);
			assertTrue("Breakpoint was not a method breakpoint", hit instanceof IJavaMethodBreakpoint);

			IJavaStackFrame topFrame = (IJavaStackFrame) thread.getTopStackFrame();
			assertNotNull("There should be a stackframe", topFrame);
			assertEquals(expectedMethod, topFrame.getMethodName());

			IStackFrame[] frames = topFrame.getThread().getStackFrames();
			assertEquals(expectedFramesNumber, frames.length);

			IJavaStackFrame mainFrame = (IJavaStackFrame) frames[expectedFramesNumber - 1];
			assertEquals("First frame must be 'main'", "main", mainFrame.getMethodName());

			// Let now all pending jobs proceed, ignore console jobs
			sync(() -> TestUtil.waitForJobs(getName(), 2000, 10000, ProcessConsole.class));
			processUiEvents(100);

			// Get and check the selection form the tree, we expect only one method selected
			TreeItem[] selected = getSelectedItemsFromDebugView(true);
			Object[] selectedText = sync(() -> Arrays.stream(selected).map(x -> x.getText()).toArray());
			if (selected.length != 1) {
				throw new TestAgainException("Unexpected selection: " + Arrays.toString(selectedText));
			}
			assertEquals("Unexpected selection: " + Arrays.toString(selectedText), 1, selected.length);
			TreeItem selectedTreeItem = selected[selected.length - 1];
			IJavaStackFrame selectedFrame = (IJavaStackFrame) sync(() -> {
				Object data = selectedTreeItem.getData();
				assertNotNull("No data for selected frame in the tree?", data);
				assertEquals("Wrong object selected: " + data, JDIStackFrame.class, data.getClass());
				return data;
			});

			// DropTests.method5() should be selected
			assertEquals(selectedFrame.getMethodName(), topFrame.getMethodName());

			// Now we inspect the children of the stopped thread (parent element of selected method)
			TreeItem threadItem = sync(() -> selectedTreeItem.getParentItem());
			TreeItem[] children = sync(() -> threadItem.getItems());
			Object[] childrenData = sync(() -> Arrays.stream(children).map(x -> x.getText()).toArray());

			// we expect to see one monitor + frames
			final int expectedChildrenCount = expectedFramesNumber + 1;
			assertEquals("Unexpected stack: " + dumpFrames(childrenData), expectedChildrenCount, childrenData.length);

			// Now we will check if the very first frame (main) is shown in the tree (on the bottom of the stack)
			Object firstFrame = childrenData[expectedChildrenCount - 1].toString();

			assertTrue("Unexpected first frame: " + firstFrame + ", ALL: "
					+ dumpFrames(childrenData), firstFrame.toString().contains("DropTests.main"));
		}
		finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	private String dumpFrames(Object[] childrenData) {
		childrenData = Arrays.stream(childrenData).map(x -> {
			if (x instanceof JDIStackFrame) {
				try {
					return ((JDIStackFrame) x).getName();
				}
				catch (DebugException e) {
					return e.getMessage();
				}
			}
			return Objects.toString(x);
		}).toArray();
		return Arrays.toString(childrenData);
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
			while (selected.length != 1 && System.currentTimeMillis() - start < 5000) {
				processUiEvents(500);
				TestUtil.log(IStatus.INFO, getName(), "Waiting for selection, current size: " + selected.length);
				selected = tree.getSelection();
			}
			return selected;
		});
	}

}
