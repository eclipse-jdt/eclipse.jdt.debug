/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.core;

import org.eclipse.core.resources.IResource;
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
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Tests deferred breakpoints.
 */
public class RunToLineTests extends AbstractDebugTest {
	
	public RunToLineTests(String name) {
		super(name);
	}

	private Object fLock = new Object();
	private IEditorPart fEditor = null;
	
	class MyPartListener implements IPartListener {

        /* (non-Javadoc)
         * @see org.eclipse.ui.IPartListener#partActivated(org.eclipse.ui.IWorkbenchPart)
         */
        public void partActivated(IWorkbenchPart part) {
        }

        /* (non-Javadoc)
         * @see org.eclipse.ui.IPartListener#partBroughtToTop(org.eclipse.ui.IWorkbenchPart)
         */
        public void partBroughtToTop(IWorkbenchPart part) {
        }

        /* (non-Javadoc)
         * @see org.eclipse.ui.IPartListener#partClosed(org.eclipse.ui.IWorkbenchPart)
         */
        public void partClosed(IWorkbenchPart part) {            
        }

        /* (non-Javadoc)
         * @see org.eclipse.ui.IPartListener#partDeactivated(org.eclipse.ui.IWorkbenchPart)
         */
        public void partDeactivated(IWorkbenchPart part) {
        }

        /* (non-Javadoc)
         * @see org.eclipse.ui.IPartListener#partOpened(org.eclipse.ui.IWorkbenchPart)
         */
        public void partOpened(IWorkbenchPart part) {
            if (part instanceof IEditorPart) {
                IEditorPart editorPart = (IEditorPart) part;
                IEditorInput editorInput = editorPart.getEditorInput();
                IResource resource = (IResource) editorInput.getAdapter(IResource.class);
                if (resource != null) {
                    if (resource.getFullPath().lastSegment().equals("Breakpoints.java")) {
                        synchronized (fLock) {
                            fEditor = editorPart;
                            fLock.notifyAll();
                        }
                    }
                }
            }
        }
	    
	}

	/**
	 * Test a run to line, with no extra breakpoints.
	 * 
	 * @throws Exception
	 */
	public void testRunToLine() throws Exception {
	    runToLine(55, 55, true);
	}
	
	/**
	 * Test a run to line, with an extra breakpoint, and preference to skip
	 * 
	 * @throws Exception
	 */
	public void testRunToLineSkipBreakpoint() throws Exception {
	    createLineBreakpoint(53, "Breakpoints");
	    runToLine(55, 55, true);
	}	
	
	/**
	 * Test a run to line, with an extra breakpoint, and preference to *not* skip
	 * 
	 * @throws Exception
	 */
	public void testRunToLineHitBreakpoint() throws Exception {
	    createLineBreakpoint(53, "Breakpoints");
	    runToLine(55, 53, false);
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
		IJavaLineBreakpoint breakpoint = createLineBreakpoint(52, typeName);
		
		boolean restore = DebugUITools.getPreferenceStore().getBoolean(IDebugUIConstants.PREF_SKIP_BREAKPOINTS_DURING_RUN_TO_LINE);
		DebugUITools.getPreferenceStore().setValue(IDebugUIConstants.PREF_SKIP_BREAKPOINTS_DURING_RUN_TO_LINE, skipBreakpoints);
		IJavaThread thread= null;
		final IPartListener listener = new MyPartListener();
		try {
		    // close all editors
		    Runnable closeAll = new Runnable() {
                public void run() {
                    IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                    activeWorkbenchWindow.getActivePage().closeAllEditors(false);
                    activeWorkbenchWindow.getActivePage().addPartListener(listener);
                }
            };
            Display display = DebugUIPlugin.getStandardDisplay();
            display.asyncExec(closeAll);
            
			thread= launchToLineBreakpoint(typeName, breakpoint);
			// wait for editor to open
			synchronized (fLock) {
			    if (fEditor == null) {
			        fLock.wait(30000);
			    }
            }
			assertNotNull("Editor did not open", fEditor);
			
			final Exception[] exs = new Exception[1];
			final IJavaThread suspendee = thread;
			Runnable r = new Runnable() {
                public void run() {
                    ITextEditor editor = (ITextEditor) fEditor;
                    IRunToLineTarget adapter = (IRunToLineTarget) editor.getAdapter(IRunToLineTarget.class);
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
                public void run() {
                    IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                    activeWorkbenchWindow.getActivePage().removePartListener(listener);
                }
            };
            Display display = DebugUIPlugin.getStandardDisplay();
            display.asyncExec(cleanup);			
		}		
	}
}
