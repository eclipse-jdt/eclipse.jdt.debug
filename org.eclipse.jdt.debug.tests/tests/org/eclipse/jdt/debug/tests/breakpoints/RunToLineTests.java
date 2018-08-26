/*******************************************************************************
 *  Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.breakpoints;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.actions.IRunToLineTarget;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.testplugin.DebugElementEventWaiter;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveListener2;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Tests run to line debug functionality
 */
public class RunToLineTests extends AbstractDebugTest {

	/**
	 * Constructor
	 * @param name
	 */
	public RunToLineTests(String name) {
		super(name);
	}

	private Object fLock = new Object();
	private IEditorPart fEditor = null;

	class MyListener implements IPerspectiveListener2 {

		/**
		 * @see org.eclipse.ui.IPerspectiveListener2#perspectiveChanged(org.eclipse.ui.IWorkbenchPage, org.eclipse.ui.IPerspectiveDescriptor, org.eclipse.ui.IWorkbenchPartReference, java.lang.String)
		 */
		@Override
		public void perspectiveChanged(IWorkbenchPage page, IPerspectiveDescriptor perspective, IWorkbenchPartReference partRef, String changeId) {
            if (partRef.getTitle().equals("Breakpoints.java") && changeId == IWorkbenchPage.CHANGE_EDITOR_OPEN) {
                synchronized (fLock) {
                    fEditor = (IEditorPart) partRef.getPart(true);
                    fLock.notifyAll();
                }
            }
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
	}

	/**
	 * Test a run to line, with no extra breakpoints.
	 *
	 * @throws Exception
	 */
	public void testRunToLine() throws Exception {
		runToLine(58, 58, true);
	}

	/**
	 * Test a run to line, with an extra breakpoint, and preference to skip
	 *
	 * @throws Exception
	 */
	public void testRunToLineSkipBreakpoint() throws Exception {
		createLineBreakpoint(56, "Breakpoints");
		runToLine(58, 58, true);
	}

	/**
	 * Test a run to line, with an extra breakpoint, and preference to *not* skip
	 *
	 * @throws Exception
	 */
	public void testRunToLineHitBreakpoint() throws Exception {
		createLineBreakpoint(56, "Breakpoints");
		runToLine(58, 56, false);
	}

	/**
	 * Runs to the given line number in the 'Breakpoints' source file, after stopping at the
	 * first line in the main method.
	 *
	 * @param lineNumber line number to run to, ONE BASED
	 * @param expectedLineNumber the line number to be on after run-to-line (may differ from
	 *  the target line number if the option to skip breakpoints is off).
	 * @param skipBreakpoints preference value for "skip breakpoints during run to line"
	 * @throws Exception
	 */
	public void runToLine(final int lineNumber, int expectedLineNumber, boolean skipBreakpoints) throws Exception {
		String typeName = "Breakpoints";
		IJavaLineBreakpoint breakpoint = createLineBreakpoint(55, typeName);

		boolean restore = DebugUITools.getPreferenceStore().getBoolean(IDebugUIConstants.PREF_SKIP_BREAKPOINTS_DURING_RUN_TO_LINE);
		DebugUITools.getPreferenceStore().setValue(IDebugUIConstants.PREF_SKIP_BREAKPOINTS_DURING_RUN_TO_LINE, skipBreakpoints);
		IJavaThread thread= null;
		final IPerspectiveListener2 listener = new MyListener();
		try {
		    // close all editors
		    Runnable closeAll = new Runnable() {
                @Override
				public void run() {
                    IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                    activeWorkbenchWindow.getActivePage().closeAllEditors(false);
                    activeWorkbenchWindow.addPerspectiveListener(listener);
                }
            };
            Display display = DebugUIPlugin.getStandardDisplay();
            display.syncExec(closeAll);

			thread= launchToLineBreakpoint(typeName, breakpoint);
			// wait for editor to open
			synchronized (fLock) {
			    if (fEditor == null) {
			        fLock.wait(30000);
			    }
            }
			if (fEditor == null) {
				Runnable r = new Runnable() {
					@Override
					public void run() {
						IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
	                    IEditorPart activeEditor = activeWorkbenchWindow.getActivePage().getActiveEditor();
                        if (activeEditor != null) {
                            System.out.println("ACTIVE: " + activeEditor.getTitle());
                        }
					}
				};
				display.syncExec(r);
			}
			assertNotNull("Editor did not open", fEditor);

			final Exception[] exs = new Exception[1];
			final IJavaThread suspendee = thread;
			Runnable r = new Runnable() {
                @Override
				public void run() {
                    ITextEditor editor = (ITextEditor) fEditor;
					IRunToLineTarget adapter = editor.getAdapter(IRunToLineTarget.class);
                    assertNotNull("no run to line adapter", adapter);
                    IDocumentProvider documentProvider = editor.getDocumentProvider();
                    try {
                        // position cursor to line
                        documentProvider.connect(this);
                        IDocument document = documentProvider.getDocument(editor.getEditorInput());
                        int lineOffset = document.getLineOffset(lineNumber - 1); // document is 0 based!
                        documentProvider.disconnect(this);
                        editor.selectAndReveal(lineOffset, 0);
                        // run to line
                        adapter.runToLine(editor, editor.getSelectionProvider().getSelection(), suspendee);
                    } catch (CoreException e) {
                        exs[0] = e;
                    } catch (BadLocationException e) {
                        exs[0] = e;
                    }
                }
            };
            DebugElementEventWaiter waiter = new DebugElementEventWaiter(DebugEvent.SUSPEND, thread);
            DebugUIPlugin.getStandardDisplay().syncExec(r);
            waiter.waitForEvent();
            IStackFrame topStackFrame = thread.getTopStackFrame();
            assertEquals("wrong line", expectedLineNumber, topStackFrame.getLineNumber());
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
			DebugUITools.getPreferenceStore().setValue(IDebugUIConstants.PREF_SKIP_BREAKPOINTS_DURING_RUN_TO_LINE, restore);
		    Runnable cleanup = new Runnable() {
                @Override
				public void run() {
                    IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                    activeWorkbenchWindow.removePerspectiveListener(listener);
                }
            };
            Display display = DebugUIPlugin.getStandardDisplay();
            display.syncExec(cleanup);
		}
	}
}
