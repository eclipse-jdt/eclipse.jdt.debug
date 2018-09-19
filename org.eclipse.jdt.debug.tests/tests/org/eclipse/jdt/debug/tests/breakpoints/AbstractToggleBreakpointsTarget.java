/*******************************************************************************
 * Copyright (c) 2014, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
import org.eclipse.debug.core.IBreakpointListener;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.sourcelookup.containers.LocalFileStorage;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTarget;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTargetExtension;
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
public abstract class AbstractToggleBreakpointsTarget extends AbstractDebugTest {

	class Listener implements IBreakpointListener {

		List<IBreakpoint> added = new ArrayList<>();
		List<IBreakpoint> removed = new ArrayList<>();

		@Override
		public void breakpointAdded(IBreakpoint breakpoint) {
			synchronized (added) {
				added.add(breakpoint);
				added.notifyAll();
			}
		}

		@Override
		public void breakpointRemoved(IBreakpoint breakpoint, IMarkerDelta delta) {
			synchronized (removed) {
				removed.add(breakpoint);
				removed.notifyAll();
			}
		}

		@Override
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

		public boolean isEmpty() throws Exception{
			synchronized (added) {
				if (added.isEmpty()) {
					added.wait(DEFAULT_TIMEOUT);
					return true;
				}
			}
			return false;

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

	public AbstractToggleBreakpointsTarget(String name) {
		super(name);
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
			@Override
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
