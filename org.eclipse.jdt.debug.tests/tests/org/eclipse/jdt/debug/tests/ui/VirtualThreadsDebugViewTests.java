/*******************************************************************************
 * Copyright (c) 2024, 2025 IBM Corporation.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation -- initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.ui;

import org.eclipse.core.resources.IFile;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.internal.ui.views.console.ProcessConsole;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.tests.TestUtil;
import org.eclipse.jdt.debug.ui.IJavaDebugUIConstants;
import org.eclipse.jdt.internal.debug.core.model.JDIThread;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.test.OrderedTestSuite;

import junit.framework.Test;

/**
 * Tests for debug view.
 */
public class VirtualThreadsDebugViewTests extends AbstractDebugUiTests {


	public static Test suite() {
		return new OrderedTestSuite(VirtualThreadsDebugViewTests.class);
	}

	private boolean showMonitorsOriginal;

	public VirtualThreadsDebugViewTests(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		IPreferenceStore jdiUIPreferences = JDIDebugUIPlugin.getDefault().getPreferenceStore();
		showMonitorsOriginal = jdiUIPreferences.getBoolean(IJavaDebugUIConstants.PREF_SHOW_MONITOR_THREAD_INFO);
		jdiUIPreferences.setValue(IJavaDebugUIConstants.PREF_SHOW_MONITOR_THREAD_INFO, true);
		resetPerspective(DebugViewPerspectiveFactory.ID);
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

	@Override
	protected IJavaProject getProjectContext() {
		return get23Project();
	}

	public void testVirtualThreadDebugView() throws Exception {
		sync(() -> TestUtil.waitForJobs(getName(), 1000, 10000, ProcessConsole.class));
		final String typeName = "Main21";
		final int bpLine = 19;

		IJavaBreakpoint bp = createLineBreakpoint(bpLine, "", typeName + ".java", typeName);
		bp.setSuspendPolicy(IJavaBreakpoint.SUSPEND_THREAD);
		IFile file = (IFile) bp.getMarker().getResource();
		assertEquals(typeName + ".java", file.getName());

		IJavaThread mainThread = null;
		try {
			mainThread = launchToBreakpoint(typeName);
			assertNotNull("Launch unsuccessful", mainThread);
			openEditorInDebug(file);
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					IDebugView debugViewer = (IDebugView) getActivePage().findView(IDebugUIConstants.ID_DEBUG_VIEW);
					ISelection currentSelection = debugViewer.getViewer().getSelection();
					assertNotNull("Debug View is not available", debugViewer);
					if (currentSelection instanceof IStructuredSelection) {
						Object sel = ((IStructuredSelection) currentSelection).getFirstElement();
						if (sel instanceof IStackFrame stackFrame) {
							IThread thread = stackFrame.getThread();
							JDIThread vThread = (JDIThread) stackFrame.getThread();
							assertTrue("Not a Virtual thread", vThread.isVirtualThread());
							StructuredSelection select = new StructuredSelection(thread);
							debugViewer.getViewer().setSelection(select, true);
							IDebugModelPresentation md = DebugUITools.newDebugModelPresentation();
							String groupName = md.getText(thread);
							assertTrue("Not a Virtual thread grouping", groupName.contains("Virtual"));
						}
					}
				}
			});
			mainThread.resume();
		} catch (Exception e) {
			DebugPlugin.log(e);
		} finally {
			terminateAndRemove(mainThread);
			removeAllBreakpoints();
		}
	}

	private void openEditorInDebug(IFile file) throws Exception {
		// Let now all pending jobs proceed, ignore console jobs
		sync(() -> TestUtil.waitForJobs(getName(), 1000, 10000, ProcessConsole.class));
		@SuppressWarnings("unused")
		CompilationUnitEditor part = (CompilationUnitEditor) sync(() -> openEditor(file));
		processUiEvents(100);
	}

	@Override
	protected boolean enableUIEventLoopProcessingInWaiter() {
		return true;
	}

}
