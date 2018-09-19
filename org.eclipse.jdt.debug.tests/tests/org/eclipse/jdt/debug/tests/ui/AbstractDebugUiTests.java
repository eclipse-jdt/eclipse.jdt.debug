/*******************************************************************************
 *  Copyright (c) 2017 Andrey Loskutov and others.
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

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.resources.IFile;
import org.eclipse.debug.internal.core.IInternalDebugCoreConstants;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.debug.tests.TestUtil;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

/**
 * Base class for UI related debug tests.
 */
public abstract class AbstractDebugUiTests extends AbstractDebugTest {

	// prefs to restore
	private String switch_on_launch;
	private String switch_on_suspend;
	private String debug_perspectives;
	private String user_view_bindings;
	private boolean activate_debug_view;

	public AbstractDebugUiTests(String name) {
		super(name);
	}

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
		sync(() -> TestUtil.waitForJobs(getName(), 10, 1000));
	}

	@Override
	protected void tearDown() throws Exception {
		IPreferenceStore preferenceStore = DebugUITools.getPreferenceStore();
		preferenceStore.setValue(IInternalDebugUIConstants.PREF_SWITCH_PERSPECTIVE_ON_SUSPEND, switch_on_suspend);
		preferenceStore.setValue(IInternalDebugUIConstants.PREF_SWITCH_TO_PERSPECTIVE, switch_on_launch);
		preferenceStore.setValue(IDebugUIConstants.PREF_MANAGE_VIEW_PERSPECTIVES, debug_perspectives);
		preferenceStore.setValue(IInternalDebugUIConstants.PREF_USER_VIEW_BINDINGS, user_view_bindings);
		preferenceStore.setValue(IInternalDebugUIConstants.PREF_ACTIVATE_DEBUG_VIEW, activate_debug_view);
		sync(() -> TestUtil.waitForJobs(getName(), 10, 1000));
		super.tearDown();
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
		TestUtil.runEventLoop();
	}

	/**
	 * Switches to and resets the specified perspective in the active workbench window.
	 *
	 * @return the window in which the perspective is ready
	 */
	protected IWorkbenchWindow resetPerspective(final String id) throws Exception {
		final IWorkbenchWindow[] windows = new IWorkbenchWindow[1];
		sync(() -> {
			IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			switchPerspective(window, id);
			windows[0] = window;
		});
		return windows[0];
	}

	/**
	 * Siwtches to and resets the debug perspective in the active workbench window.
	 *
	 * @return the window in which the perspective is ready
	 */
	protected IWorkbenchWindow resetDebugPerspective() throws Exception {
		return resetPerspective(IDebugUIConstants.ID_DEBUG_PERSPECTIVE);
	}

	/**
	 * Swtches to and resets the java perspective in the active workbench window.
	 *
	 * @return the window in which the perspective is ready
	 */
	protected IWorkbenchWindow resetJavaPerspective() throws Exception {
		return resetPerspective(JavaUI.ID_PERSPECTIVE);
	}

	/**
	 * Sync exec the given runnable, re-throwing exceptions in the current thread
	 *
	 * @param r
	 * @throws Exception
	 */
	protected void sync(Runnable r) throws Exception {
		AtomicReference<Exception> error = new AtomicReference<>();
		DebugUIPlugin.getStandardDisplay().syncExec(() -> {
			try {
				r.run();
			}
			catch (Exception t) {
				error.set(t);
			}
		});
		if (error.get() != null) {
			throw error.get();
		}
	}

	/**
	 * Sync exec the given runnable, re-throwing exceptions in the current thread
	 *
	 * @param c
	 * @throws Exception
	 */
	protected <V> V sync(Callable<V> c) throws Exception {
		AtomicReference<Throwable> error = new AtomicReference<>();
		AtomicReference<V> result = new AtomicReference<>();
		DebugUIPlugin.getStandardDisplay().syncExec(() -> {
			try {
				result.set(c.call());
			}
			catch (Throwable t) {
				error.set(t);
			}
		});
		if (error.get() != null) {
			throwException(error.get());
		}
		return result.get();
	}

	/**
	 * This and the another {@link #throwException1(Throwable)} method below are here to allow to catch AssertionFailedError's and other
	 * non-Exceptions happened in the UI thread
	 *
	 * @param exception
	 */
	private static void throwException(Throwable exception) {
		AbstractDebugUiTests.<RuntimeException> throwException1(exception);
	}

	/**
	 * @param dummy to make compiler happy
	 */
	@SuppressWarnings("unchecked")
	private static <T extends Throwable> void throwException1(Throwable exception) throws T {
		throw (T) exception;
	}

	protected void processUiEvents(long millis) throws Exception {
		Thread.sleep(millis);
		if (Display.getCurrent() == null) {
			sync(() -> TestUtil.runEventLoop());
		} else {
			TestUtil.runEventLoop();
		}
	}

	protected IWorkbenchPage getActivePage() {
		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
		if (window == null) {
			window = workbench.getWorkbenchWindows()[0];
			Shell shell = window.getShell();
			shell.moveAbove(null);
			shell.setActive();
			shell.forceActive();
		}
		return window.getActivePage();
	}

	protected IEditorPart openEditor(String type) throws Exception {
		IFile resource = (IFile) getResource(type);
		IEditorPart editor = IDE.openEditor(getActivePage(), resource);
		assertNotNull(editor);
		processUiEvents(100);
		return editor;
	}
}
