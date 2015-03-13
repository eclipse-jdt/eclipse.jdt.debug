/*******************************************************************************
 *  Copyright (c) 2000, 2015 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.debug.internal.core.IInternalDebugCoreConstants;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.test.OrderedTestSuite;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveListener3;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import junit.framework.Test;

/**
 * Tests view management.
 */
public class ViewManagementTests extends AbstractDebugTest implements IPerspectiveListener3 {

	public static Test suite() {
		return new OrderedTestSuite(ViewManagementTests.class);
	}

	// See https://bugs.eclipse.org/420778
	private static final boolean HAS_BUG_420778 = true;

	// view ids
	/**
	 * The id of test view 'two'
	 */
	public static final String VIEW_TWO = "org.eclipse.jdt.debug.tests.context.view.two";
	/**
	 * The id of test view 'one'
	 */
	public static final String VIEW_ONE = "org.eclipse.jdt.debug.tests.context.view.one";

	private Object fEventLock = new Object();
	
	/**
	 * List of view ids expecting to open.
	 */
	private List<String> fExpectingOpenEvents = new ArrayList<String>();
	
	/**
	 * List of view ids expecting to close.
	 */
	private List<String> fExpectingCloseEvents = new ArrayList<String>();	
	
	// prefs to restore
	private String switch_on_launch;
	private String switch_on_suspend;
	private String debug_perspectives;
	private String user_view_bindings;
	private boolean activate_debug_view;
	
	/**
	 * Constructor
	 * @param name
	 */
	public ViewManagementTests(String name) {
		super(name);
	}
		
	/**
	 * Switches to the specified perspective in the given window, and resets the perspective.
	 * 
	 * @param window
	 * @param perspectiveId
	 */
	protected void switchPerspective(IWorkbenchWindow window, String perspectiveId) {
		IPerspectiveDescriptor descriptor = PlatformUI.getWorkbench().getPerspectiveRegistry().findPerspectiveWithId(perspectiveId);
		assertNotNull("missing perspective " + perspectiveId, descriptor);
		IWorkbenchPage page = window.getActivePage();
		page.setPerspective(descriptor);
		page.resetPerspective();
	}
	
	/**
	 * Switches to and resets the specified perspective in the active workbench window.
	 * 
	 * @return the window in which the perspective is ready
	 */
	private IWorkbenchWindow resetPerspective(final String id) {
		final IWorkbenchWindow[] windows = new IWorkbenchWindow[1];
		Runnable r = new Runnable() {
			@Override
			public void run() {
				IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				switchPerspective(window, id);
				windows[0] = window;
			}
		};
		sync(r);
		return windows[0];
	}
	
	/**
	 * Siwtches to and resets the debug perspective in the active workbench window.
	 * 
	 * @return the window in which the perspective is ready
	 */
	protected IWorkbenchWindow resetDebugPerspective() {
		return resetPerspective(IDebugUIConstants.ID_DEBUG_PERSPECTIVE);
	}
	
	/**
	 * Siwtches to and resets the java perspective in the active workbench window.
	 * 
	 * @return the window in which the perspective is ready
	 */
	protected IWorkbenchWindow resetJavaPerspective() {
		return resetPerspective(JavaUI.ID_PERSPECTIVE); 
	}	
	
	/**
	 * Sync exec the given runnable
	 * 
	 * @param r
	 */
	protected void sync(Runnable r) {
		DebugUIPlugin.getStandardDisplay().syncExec(r);
	}
	
	/**
	 * Returns whether the specified view is open
	 * 
	 * @param window
	 * @param id
	 * @return
	 */
	protected boolean isViewOpen(final IWorkbenchWindow window, final String id) {
		final IViewReference[] refs = new IViewReference[1];
		Runnable r = new Runnable() {
			@Override
			public void run() {
				refs[0] = window.getActivePage().findViewReference(id);
			}
		};
		sync(r);
		return refs[0] != null;
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		IPreferenceStore preferenceStore = DebugUITools.getPreferenceStore();
		switch_on_launch = preferenceStore.getString(IInternalDebugUIConstants.PREF_SWITCH_TO_PERSPECTIVE);
		switch_on_suspend = preferenceStore.getString(IInternalDebugUIConstants.PREF_SWITCH_PERSPECTIVE_ON_SUSPEND);
		debug_perspectives = preferenceStore.getString(IDebugUIConstants.PREF_MANAGE_VIEW_PERSPECTIVES);
		user_view_bindings = preferenceStore.getString(IInternalDebugUIConstants.PREF_USER_VIEW_BINDINGS);
		activate_debug_view = preferenceStore.getBoolean(IInternalDebugUIConstants.PREF_ACTIVATE_DEBUG_VIEW);
		preferenceStore.setValue(IInternalDebugUIConstants.PREF_SWITCH_PERSPECTIVE_ON_SUSPEND, MessageDialogWithToggle.NEVER);
		preferenceStore.setValue(IInternalDebugUIConstants.PREF_SWITCH_TO_PERSPECTIVE, MessageDialogWithToggle.NEVER);
		preferenceStore.setValue(IDebugUIConstants.PREF_MANAGE_VIEW_PERSPECTIVES, IDebugUIConstants.ID_DEBUG_PERSPECTIVE + "," + 
				JavaUI.ID_PERSPECTIVE + ",");
		preferenceStore.setValue(IInternalDebugUIConstants.PREF_USER_VIEW_BINDINGS, IInternalDebugCoreConstants.EMPTY_STRING);
		preferenceStore.setValue(IInternalDebugUIConstants.PREF_ACTIVATE_DEBUG_VIEW, true);
		fExpectingOpenEvents.clear();
		fExpectingCloseEvents.clear();
	}
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		IPreferenceStore preferenceStore = DebugUITools.getPreferenceStore();
		preferenceStore.setValue(IInternalDebugUIConstants.PREF_SWITCH_PERSPECTIVE_ON_SUSPEND, switch_on_suspend);
		preferenceStore.setValue(IInternalDebugUIConstants.PREF_SWITCH_TO_PERSPECTIVE, switch_on_launch);
		preferenceStore.setValue(IDebugUIConstants.PREF_MANAGE_VIEW_PERSPECTIVES, debug_perspectives);
		preferenceStore.setValue(IInternalDebugUIConstants.PREF_USER_VIEW_BINDINGS, user_view_bindings); 	
		preferenceStore.setValue(IInternalDebugUIConstants.PREF_ACTIVATE_DEBUG_VIEW, activate_debug_view);
	}

	/**
	 * Tests that context views auto-open in debug perspective.
	 * Both context views should auto-open.
	 * 
	 * @throws Exception
	 */
	public void testAutoOpenDebugPerspective() throws Exception {
		if (HAS_BUG_420778) {
			return;
		}

		String typeName = "Breakpoints";
		// first line in main
		createLineBreakpoint(52, typeName);
		
		IJavaThread thread= null;
		IWorkbenchWindow window = null;
		try {
			window = resetDebugPerspective();
			synchronized (fEventLock) {
				expectingViewOpenEvents(window, new String[]{VIEW_ONE, VIEW_TWO});
				thread= launchToBreakpoint(typeName);
				fEventLock.wait(DEFAULT_TIMEOUT);
			}
			assertNotNull("Breakpoint not hit within timeout period", thread);
			assertTrue(buildRemainingEventsMessage(), checkComplete());
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
			window.removePerspectiveListener(this);
		}		
	}
	
	/**
	 * Tests that context views auto-open in debug perspective, and auto close on termination.
	 * View "two" should auto-close.
	 * 
	 * @throws Exception
	 */
	public void testAutoCloseDebugPerspective() throws Exception {
		if (HAS_BUG_420778) {
			return;
		}

		String typeName = "Breakpoints";
		// first line in main
		createLineBreakpoint(52, typeName);
		
		IJavaThread thread= null;
		IWorkbenchWindow window = null;
		try {
			window = resetDebugPerspective();
			synchronized (fEventLock) {
				expectingViewOpenEvents(window, new String[]{VIEW_ONE, VIEW_TWO});
				thread= launchToBreakpoint(typeName);
				fEventLock.wait(DEFAULT_TIMEOUT);
			}
			assertNotNull("Breakpoint not hit within timeout period", thread);
			assertTrue(buildRemainingEventsMessage(), checkComplete());
			// terminate to auto close
			synchronized (fEventLock) {
				expectingViewCloseEvents(window, new String[]{VIEW_TWO});
				thread.terminate();
				fEventLock.wait(DEFAULT_TIMEOUT);
			}
			assertTrue(buildRemainingEventsMessage(), checkComplete());
			assertTrue("View " + VIEW_ONE + " should be open", isViewOpen(window, VIEW_ONE));
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
			window.removePerspectiveListener(this);
		}		
	}
	
	/**
	 * Tests that context views auto-open in java perspective.
	 * Both context views should auto-open as well as standard debug views.
	 * 
	 * @throws Exception
	 */
	public void testAutoOpenJavaPerspective() throws Exception {
		if (HAS_BUG_420778) {
			return;
		}

		String typeName = "Breakpoints";
		// first line in main
		createLineBreakpoint(52, typeName);
		
		IJavaThread thread= null;
		IWorkbenchWindow window = null;
		try {
			window = resetJavaPerspective();
			synchronized (fEventLock) {
				expectingViewOpenEvents(window, new String[]{
						VIEW_ONE,
						VIEW_TWO,
						IDebugUIConstants.ID_DEBUG_VIEW,
						IDebugUIConstants.ID_VARIABLE_VIEW,
						IDebugUIConstants.ID_BREAKPOINT_VIEW});
				thread= launchToBreakpoint(typeName);
				fEventLock.wait(DEFAULT_TIMEOUT);
			}
			assertNotNull("Breakpoint not hit within timeout period", thread);
			assertTrue(buildRemainingEventsMessage(), checkComplete());
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
			window.removePerspectiveListener(this);
		}		
	}
	
	/**
	 * Tests that context views auto-open and close in java perspective.
	 * All views should auto-close in non-standard debug perspective.
	 * 
	 * @throws Exception
	 */
	public void testAutoCloseJavaPerspective() throws Exception {
		if (HAS_BUG_420778) {
			return;
		}

		String typeName = "Breakpoints";
		// first line in main
		createLineBreakpoint(52, typeName);
		
		IJavaThread thread= null;
		IWorkbenchWindow window = null;
		try {
			window = resetJavaPerspective();
			synchronized (fEventLock) {
				expectingViewOpenEvents(window, new String[]{
						VIEW_ONE,
						VIEW_TWO,
						IDebugUIConstants.ID_DEBUG_VIEW,
						IDebugUIConstants.ID_VARIABLE_VIEW,
						IDebugUIConstants.ID_BREAKPOINT_VIEW});
				thread= launchToBreakpoint(typeName);
				fEventLock.wait(DEFAULT_TIMEOUT);
			}
			assertNotNull("Breakpoint not hit within timeout period", thread);
			assertTrue(buildRemainingEventsMessage(), checkComplete());
			// terminate to auto close
			synchronized (fEventLock) {
				expectingViewCloseEvents(window, new String[]{
						VIEW_TWO,
						IDebugUIConstants.ID_VARIABLE_VIEW,
						IDebugUIConstants.ID_BREAKPOINT_VIEW});
				thread.terminate();
				fEventLock.wait(DEFAULT_TIMEOUT);
			}
			assertTrue(buildRemainingEventsMessage(), checkComplete());
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
			window.removePerspectiveListener(this);
		}		
	}
	
	protected String buildRemainingEventsMessage() {
		StringBuffer buffer = new StringBuffer();
		partsMessage("Parts did not open: ", fExpectingOpenEvents, buffer);
		partsMessage("Parts did not close: ", fExpectingCloseEvents, buffer);
		return buffer.toString();
	}
	
	private void partsMessage(String header, List<String> partIds, StringBuffer buffer) {
		String[] ids = partIds.toArray(new String[partIds.size()]);
		if (ids.length > 0) {
			buffer.append(header);
			for (int i = 0; i < ids.length; i++) {
				buffer.append(ids[i]);
				if (i < (ids.length - 1)) {
					buffer.append(", ");
				}
			}
		}		
	}
	
	/**
	 * Adds ids of views to 'expecting open' queue.
	 * 
	 * @param window
	 * @param viewIds
	 */
	protected void expectingViewOpenEvents(IWorkbenchWindow window, String[] viewIds) {
		for (int i = 0; i < viewIds.length; i++) {
			fExpectingOpenEvents.add(viewIds[i]);
		}
		window.addPerspectiveListener(this);
	}
	
	/**
	 * Adds ids of views to 'expecting open' queue.
	 * 
	 * @param window
	 * @param viewIds
	 */
	protected void expectingViewCloseEvents(IWorkbenchWindow window, String[] viewIds) {
		for (int i = 0; i < viewIds.length; i++) {
			fExpectingCloseEvents.add(viewIds[i]);
		}
		window.addPerspectiveListener(this);
	}	

	/**
	 * @see org.eclipse.ui.IPerspectiveListener3#perspectiveOpened(org.eclipse.ui.IWorkbenchPage, org.eclipse.ui.IPerspectiveDescriptor)
	 */
	@Override
	public void perspectiveOpened(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
	}

	/**
	 * @see org.eclipse.ui.IPerspectiveListener3#perspectiveClosed(org.eclipse.ui.IWorkbenchPage, org.eclipse.ui.IPerspectiveDescriptor)
	 */
	@Override
	public void perspectiveClosed(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
	}

	/**
	 * @see org.eclipse.ui.IPerspectiveListener3#perspectiveDeactivated(org.eclipse.ui.IWorkbenchPage, org.eclipse.ui.IPerspectiveDescriptor)
	 */
	@Override
	public void perspectiveDeactivated(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
	}

	/**
	 * @see org.eclipse.ui.IPerspectiveListener3#perspectiveSavedAs(org.eclipse.ui.IWorkbenchPage, org.eclipse.ui.IPerspectiveDescriptor, org.eclipse.ui.IPerspectiveDescriptor)
	 */
	@Override
	public void perspectiveSavedAs(IWorkbenchPage page, IPerspectiveDescriptor oldPerspective, IPerspectiveDescriptor newPerspective) {
	}

	/**
	 * @see org.eclipse.ui.IPerspectiveListener2#perspectiveChanged(org.eclipse.ui.IWorkbenchPage, org.eclipse.ui.IPerspectiveDescriptor, org.eclipse.ui.IWorkbenchPartReference, java.lang.String)
	 */
	@Override
	public void perspectiveChanged(IWorkbenchPage page, IPerspectiveDescriptor perspective, IWorkbenchPartReference partRef, String changeId) {
		if (changeId == IWorkbenchPage.CHANGE_VIEW_SHOW) {
			fExpectingOpenEvents.remove(partRef.getId());
		}
		if (changeId == IWorkbenchPage.CHANGE_VIEW_HIDE) {
			fExpectingCloseEvents.remove(partRef.getId());
		}
		checkComplete();
	}

	/**
	 * @see org.eclipse.ui.IPerspectiveListener#perspectiveActivated(org.eclipse.ui.IWorkbenchPage, org.eclipse.ui.IPerspectiveDescriptor)
	 */
	@Override
	public void perspectiveActivated(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
	}

	/**
	 * @see org.eclipse.ui.IPerspectiveListener#perspectiveChanged(org.eclipse.ui.IWorkbenchPage, org.eclipse.ui.IPerspectiveDescriptor, java.lang.String)
	 */
	@Override
	public void perspectiveChanged(IWorkbenchPage page, IPerspectiveDescriptor perspective, String changeId) {
	}
	
	/**
	 * Check if all expected events have occurred.
	 */
	protected boolean checkComplete() {
		if (!fExpectingOpenEvents.isEmpty()) {
			return false;
		}
		if (!fExpectingCloseEvents.isEmpty()) {
			return false;
		}
		// all expected events have occurred, notify
		synchronized (fEventLock) {
			fEventLock.notifyAll();
		}
		return true;
	}
}
