/*******************************************************************************
 * Copyright (c) 2010, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.breakpoints;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.IBreakpointListener;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.sourcelookup.containers.LocalFileStorage;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTarget;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTargetExtension;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaMethodBreakpoint;
import org.eclipse.jdt.debug.core.IJavaWatchpoint;
import org.eclipse.jdt.debug.testplugin.JavaTestPlugin;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.internal.debug.ui.LocalFileStorageEditorInput;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Tests the Java debugger's 'toggle breakpoints target'.
 */
public class TestToggleBreakpointsTarget extends AbstractDebugTest {
	
	class Listener implements IBreakpointListener {
		
		List<IBreakpoint> added = new ArrayList<IBreakpoint>();
		List<IBreakpoint> removed = new ArrayList<IBreakpoint>();

		public void breakpointAdded(IBreakpoint breakpoint) {
			synchronized (added) {
				added.add(breakpoint);
				added.notifyAll();
			}
		}

		public void breakpointRemoved(IBreakpoint breakpoint, IMarkerDelta delta) {
			synchronized (removed) {
				removed.add(breakpoint);
				removed.notifyAll();
			}
		}

		public void breakpointChanged(IBreakpoint breakpoint, IMarkerDelta delta) {
		}
		
		public IBreakpoint getAdded() throws Exception {
			synchronized (added) {
				if (added.isEmpty()) {
					added.wait(DEFAULT_TIMEOUT);
				}
			}
			assertFalse("Breakpoint not added", added.isEmpty());
			return added.get(0);
		}
		
		public IBreakpoint getRemoved() throws Exception {
			synchronized (removed) {
				if (removed.isEmpty()) {
					removed.wait(DEFAULT_TIMEOUT);
				}
			}
			assertFalse("Breakpoint not removed", removed.isEmpty());
			return removed.get(0);
		}
		
		
	}

	public TestToggleBreakpointsTarget(String name) {
		super(name);
	}
	
	/**
	 * Tests that qualified names get created for line breakpoints in external
	 * files.
	 * 
	 * @throws Exception
	 */
	public void testExternalLineBreakpoint() throws Exception {
		Listener listener = new Listener();
		IBreakpointManager manager = getBreakpointManager();
		manager.addBreakpointListener(listener);
		try {
			Path path = new Path("testfiles/source/SomeClass.java");
			toggleBreakpoint(path, 22); // 0 based offset in document line numbers
			IBreakpoint added = listener.getAdded();
			assertTrue("Should be a line breakpoint", added instanceof IJavaLineBreakpoint);
			IJavaLineBreakpoint breakpoint = (IJavaLineBreakpoint) added;
			assertEquals("Wrong line number", 23, breakpoint.getLineNumber());
			assertEquals("Wrong type name", "a.b.c.SomeClass", breakpoint.getTypeName());
		} finally {
			manager.removeBreakpointListener(listener);
			removeAllBreakpoints();
		}
	}
	
	/**
	 * Tests that qualified names get created for watchpoints in external
	 * files.
	 * 
	 * @throws Exception
	 */
	public void testExternalWatchpoint() throws Exception {
		Listener listener = new Listener();
		IBreakpointManager manager = getBreakpointManager();
		manager.addBreakpointListener(listener);
		try {
			Path path = new Path("testfiles/source/SomeClass.java");
			toggleBreakpoint(path, 19); // 0 based offset in document line numbers
			IBreakpoint added = listener.getAdded();
			assertTrue("Should be a watchpoint", added instanceof IJavaWatchpoint);
			IJavaWatchpoint breakpoint = (IJavaWatchpoint) added;
			assertEquals("Wrong type name", "a.b.c.SomeClass", breakpoint.getTypeName());
			assertEquals("Wrong field name", "someField", breakpoint.getFieldName());
		} finally {
			manager.removeBreakpointListener(listener);
			removeAllBreakpoints();
		}
	}	
	
	/**
	 * Tests that qualified names get created for method breakpoints in external
	 * files.
	 * 
	 * @throws Exception
	 */
	public void testExternalMethodBreakpoint() throws Exception {
		Listener listener = new Listener();
		IBreakpointManager manager = getBreakpointManager();
		manager.addBreakpointListener(listener);
		try {
			Path path = new Path("testfiles/source/SomeClass.java");
			toggleBreakpoint(path, 21); // 0 based offset in document line numbers
			IBreakpoint added = listener.getAdded();
			assertTrue("Should be a method breakpoint", added instanceof IJavaMethodBreakpoint);
			IJavaMethodBreakpoint breakpoint = (IJavaMethodBreakpoint) added;
			assertEquals("Wrong type name", "a.b.c.SomeClass", breakpoint.getTypeName());
			assertEquals("Wrong method name", "someMethod", breakpoint.getMethodName());
			// this will actually fail to suspend since 'SomeClass' is not qualified, but we can't resolve the type
			// without a build path, etc. (not a regression)
			assertEquals("Wrong signature", "(Ljava/lang/String;LSomeClass;)V", breakpoint.getMethodSignature());
		} finally {
			manager.removeBreakpointListener(listener);
			removeAllBreakpoints();
		}
	}	
	
	/**
	 * Opens an editor on the given external file and toggles a breakpoint.
	 * 
	 * @param externalFile path to external file in the test plug-in
	 * @param line line number (1 based)
	 * @throws Exception on failure
	 */
	protected void toggleBreakpoint(final IPath externalFile, final int line) throws Exception {
		final Exception[] exs = new Exception[1];
		DebugUIPlugin.getStandardDisplay().syncExec(new Runnable() {
			public void run() {
				try {
					File file = JavaTestPlugin.getDefault().getFileInPlugin(externalFile);
					LocalFileStorage storage = new LocalFileStorage(file);
					IEditorPart editor = DebugUIPlugin.getActiveWorkbenchWindow().getActivePage().openEditor(new LocalFileStorageEditorInput(storage), JavaUI.ID_CU_EDITOR);
					ITextEditor textEditor = (ITextEditor) editor;
			        IEditorInput editorInput = textEditor.getEditorInput();
			        IDocumentProvider documentProvider = textEditor.getDocumentProvider();
			        IDocument document = documentProvider.getDocument(editorInput);
			        int offset = document.getLineOffset(line);
			        IToggleBreakpointsTargetExtension toggle = (IToggleBreakpointsTargetExtension) editor.getAdapter(IToggleBreakpointsTarget.class);
			        toggle.toggleBreakpoints(editor, new TextSelection(document, offset, 0));
				} catch (Exception e) {
					exs[0] = e;
				}
			}
		});
		if (exs[0] != null) {
			throw exs[0];
		}
	}

}
