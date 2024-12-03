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

import java.util.Arrays;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointListener;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.internal.core.IInternalDebugCoreConstants;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.debug.ui.actions.ToggleBreakpointAction;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaMethodBreakpoint;
import org.eclipse.jdt.debug.core.IJavaMethodEntryBreakpoint;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.debug.tests.TestUtil;
import org.eclipse.jdt.debug.tests.VerticalRulerInfoStub;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IViewPart;
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
		sync(() -> TestUtil.waitForJobs(getName(), 10, 10000));
	}

	@Override
	protected void tearDown() throws Exception {
		IPreferenceStore preferenceStore = DebugUITools.getPreferenceStore();
		preferenceStore.setValue(IInternalDebugUIConstants.PREF_SWITCH_PERSPECTIVE_ON_SUSPEND, switch_on_suspend);
		preferenceStore.setValue(IInternalDebugUIConstants.PREF_SWITCH_TO_PERSPECTIVE, switch_on_launch);
		preferenceStore.setValue(IDebugUIConstants.PREF_MANAGE_VIEW_PERSPECTIVES, debug_perspectives);
		preferenceStore.setValue(IInternalDebugUIConstants.PREF_USER_VIEW_BINDINGS, user_view_bindings);
		preferenceStore.setValue(IInternalDebugUIConstants.PREF_ACTIVATE_DEBUG_VIEW, activate_debug_view);
		sync(() -> TestUtil.waitForJobs(getName(), 10, 10000));
		super.tearDown();
	}

	/**
	 * Switches to the specified perspective in the given window, and resets the perspective.
	 */
	protected static void switchPerspective(IWorkbenchWindow window, String perspectiveId) {
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
	protected static IWorkbenchWindow resetPerspective(final String id) throws RuntimeException {
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
	protected static IWorkbenchWindow resetDebugPerspective() throws RuntimeException {
		return resetPerspective(IDebugUIConstants.ID_DEBUG_PERSPECTIVE);
	}

	/**
	 * Swtches to and resets the java perspective in the active workbench window.
	 *
	 * @return the window in which the perspective is ready
	 */
	protected static IWorkbenchWindow resetJavaPerspective() throws RuntimeException {
		return resetPerspective(JavaUI.ID_PERSPECTIVE);
	}

	/**
	 * Process all queued UI events.
	 */
	public static void processUiEvents() throws RuntimeException {
		sync(() -> {
			Display display = Display.getCurrent();
			if (!display.isDisposed()) {
				while (display.readAndDispatch()) {
					// Keep pumping events until the queue is empty
				}
			}
		});
	}

	/**
	 * Process all queued UI events. If called from background thread, just waits
	 *
	 * @param timeoutMs
	 *            max wait time in milliseconds to process events
	 */
	public static void processUiEvents(final long timeoutMs) throws RuntimeException {
		sync(() -> {
			long timeoutNanos = System.nanoTime() + timeoutMs * 1_000_000L;
			while (System.nanoTime() < timeoutNanos) {
				Display display = Display.getCurrent();
				if (display != null && !display.isDisposed()) {
					while (display.readAndDispatch()) {
						// loop until the queue is empty
					}
				} else {
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						break;
					}
				}
			}
		});
	}

	/**
	 * Sync exec the given runnable, re-throwing exceptions in the current thread
	 */
	protected static void sync(Runnable r) throws RuntimeException {
		AtomicReference<Exception> error = new AtomicReference<>();
		DebugUIPlugin.getStandardDisplay().syncExec(() -> {
			try {
				r.run();
			}
			catch (Exception t) {
				error.set(t);
			}
			catch (Throwable t) {
				error.set(new RuntimeException(t));
			}
		});
		if (error.get() != null) {
			throwException(error.get());
		}
	}

	/**
	 * Sync exec the given runnable, re-throwing exceptions in the current thread
	 */
	protected static <V> V sync(Callable<V> c) throws RuntimeException {
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
	 * This method is to allow to catch AssertionFailedError's and other non-Exceptions happened in the UI thread
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Throwable> void throwException(Throwable exception) throws T {
		throw (T) exception;
	}

	protected static IWorkbenchPage getActivePage() throws RuntimeException {
		Callable<IWorkbenchPage> callable = () -> {
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
		};
		return callInUi(callable);
	}

	protected IEditorPart openEditor(String type) throws RuntimeException {
		Callable<IEditorPart> callable = () -> {
			IFile resource = (IFile) getResource(type);
			IEditorPart editor = IDE.openEditor(getActivePage(), resource);
			assertNotNull(editor);
			processUiEvents(100);
			return editor;
		};
		return callInUi(callable);
	}

	private static <T> T callInUi(Callable<T> callable) throws RuntimeException {
		if (Display.getCurrent() != null) {
			try {
				return callable.call();
			} catch (Throwable e) {
				throwException(e);
			}
		}
		return sync(callable);
	}

	/**
	 * Opens and returns an editor on the given file or <code>null</code> if none. The editor will be activated.
	 *
	 * @return editor or <code>null</code>
	 */
	protected static IEditorPart openEditor(final IFile file) throws RuntimeException {
		Callable<IEditorPart> callable = () -> {
			IWorkbenchPage page = getActivePage();
			return IDE.openEditor(page, file, true);
		};
		return callInUi(callable);
	}

	protected IJavaLineBreakpoint createLineBreakpoint(int lineNumber, IEditorPart editor, BreakpointsMap bpMap) throws Exception {
		IJavaLineBreakpoint breakpoint = (IJavaLineBreakpoint) toggleBreakpoint(editor, lineNumber);
		bpMap.put(breakpoint, lineNumber);
		return breakpoint;
	}

	/**
	 * Toggles a breakpoint in the editor at the given line number returning the breakpoint or <code>null</code> if none.
	 *
	 * @return returns the created breakpoint or <code>null</code> if none.
	 */
	protected IBreakpoint toggleBreakpoint(final IEditorPart editor, int lineNumber) throws Exception {
		final Object lock = new Object();
		final AtomicReference<IBreakpoint> breakpoint = new AtomicReference<>();
		IBreakpointListener listener = new IBreakpointListener() {
			@Override
			public void breakpointRemoved(IBreakpoint breakpoint, IMarkerDelta delta) {
			}

			@Override
			public void breakpointChanged(IBreakpoint breakpoint, IMarkerDelta delta) {
			}

			@Override
			public void breakpointAdded(IBreakpoint b) {
				synchronized (lock) {
					breakpoint.set(b);
					lock.notifyAll();
				}
			}
		};
		IBreakpointManager manager = DebugPlugin.getDefault().getBreakpointManager();
		manager.addBreakpointListener(listener);
		sync(() -> {
			final IVerticalRulerInfo info = new VerticalRulerInfoStub(lineNumber - 1); // sub 1, as the doc lines start at 0
			new ToggleBreakpointAction(editor, null, info).run();
		});
		synchronized (lock) {
			if (breakpoint.get() == null) {
				lock.wait(DEFAULT_TIMEOUT);
			}
		}
		manager.removeBreakpointListener(listener);
		IBreakpoint bp = breakpoint.get();
		assertNotNull("Breakpoint not created", bp);
		if (isLineBreakpoint(bp)) {
			int line = ((IJavaLineBreakpoint) bp).getLineNumber();
			assertEquals("Breakpoint line differs from expected", lineNumber, line);
		}
		return bp;
	}

	private boolean isLineBreakpoint(IBreakpoint bp) {
		return bp instanceof IJavaLineBreakpoint
				&& !(bp instanceof IJavaMethodBreakpoint)
				&& !(bp instanceof IJavaMethodEntryBreakpoint);
	}

	/**
	 * Closes all editors in the active workbench page.
	 */
	protected static void closeAllEditors() throws RuntimeException {
		sync(() -> {
			for (IWorkbenchWindow window : PlatformUI.getWorkbench().getWorkbenchWindows()) {
				window.getActivePage().closeAllEditors(false);
			}
		});
	}

	/**
	 * Opens the view with the given id, does nothing if no such view exists. This method can return <code>null</code>
	 *
	 * @return the handle to the {@link IDebugView} with the given id
	 */
	protected static IViewPart openView(final String viewId) throws RuntimeException {
		return sync(() -> {
			return getActivePage().showView(viewId);
		});
	}

	private void assertBreakpointExists(IJavaLineBreakpoint bp, IBreakpoint[] bps) throws Exception {
		boolean contains = Arrays.asList(bps).contains(bp);
		assertTrue("Breakpoint not found: " + bp + ", all: " + Arrays.toString(bps), contains);
	}

	public class BreakpointsMap {
		IdentityHashMap<IJavaLineBreakpoint, Integer> breakpoints = new IdentityHashMap<>();
		HashMap<Integer, IJavaLineBreakpoint> lines = new HashMap<>();

		public void put(IJavaLineBreakpoint breakpoint, int line) throws Exception {
			int lineNumber = breakpoint.getLineNumber();
			assertEquals("Breakpoint line differs from original", line, lineNumber);
			Integer old = breakpoints.put(breakpoint, lineNumber);
			assertNull("Breakpoint already exists for line: " + old, old);
			IJavaLineBreakpoint oldB = lines.put(lineNumber, breakpoint);
			assertNull("Breakpoint already exists for line: " + lineNumber, oldB);
		}

		public void assertBreakpointsConsistency() throws Exception {
			IBreakpoint[] bps = getBreakpointManager().getBreakpoints();
			Set<Entry<IJavaLineBreakpoint, Integer>> set = breakpoints.entrySet();
			for (Entry<IJavaLineBreakpoint, Integer> entry : set) {
				IJavaLineBreakpoint bp = entry.getKey();
				assertBreakpointExists(bp, bps);
				Integer line = entry.getValue();
				assertEquals("Breakpoint moved", line.intValue(), bp.getLineNumber());
			}
		}
	}

}
