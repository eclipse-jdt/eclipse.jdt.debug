/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.actions;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.ISuspendResume;
import org.eclipse.debug.internal.ui.actions.IRunToLineTarget;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.debug.ui.IJavaDebugUIConstants;
import org.eclipse.jdt.internal.debug.ui.BreakpointUtils;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Run to line target for the Java debugger
 */
public class RunToLineAdapter implements IRunToLineTarget {
	
	private IEditorPart editorPart;
	
	/**
	 * Constructs a new run to line adapter for the given editor
	 * 
	 * @param editor
	 */
	public RunToLineAdapter(IEditorPart editor) {
		editorPart = editor;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.actions.IRunToLineTarget#canPerformRunToLine()
	 */
	public boolean canPerformRunToLine() {
		return true;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.actions.IRunToLineTarget#runToLine()
	 */
	public void runToLine(ISuspendResume target) throws CoreException {
		IEditorInput input = editorPart.getEditorInput();
		String errorMessage = null;
		if (input == null) {
			errorMessage = ActionMessages.getString("RunToLineAdapter.0"); //$NON-NLS-1$
		} else {
			final ITextEditor textEditor = (ITextEditor)editorPart;
			final IDocument document= textEditor.getDocumentProvider().getDocument(input);
			if (document == null) {
				errorMessage = ActionMessages.getString("RunToLineAdapter.1"); //$NON-NLS-1$
			} else {
				final int[] validLine = new int[1];
				final String[] typeName = new String[1];
				final int[] lineNumber = new int[1];
				final ITextSelection selection = (ITextSelection) textEditor.getSelectionProvider().getSelection();
				Runnable r = new Runnable() {
					public void run() {
						lineNumber[0] = selection.getStartLine() + 1;
						ASTParser parser = ASTParser.newParser(AST.LEVEL_2_0);
						parser.setSource(document.get().toCharArray());
						CompilationUnit compilationUnit= (CompilationUnit)parser.createAST(null);
						ValidBreakpointLocationLocator locator= new ValidBreakpointLocationLocator(compilationUnit, lineNumber[0]);
						compilationUnit.accept(locator);
						validLine[0]= locator.getValidLocation();		
						typeName[0]= locator.getFullyQualifiedTypeName();
					}
				};
				BusyIndicator.showWhile(JDIDebugUIPlugin.getStandardDisplay(), r);
				if (validLine[0] == lineNumber[0]) {
					IBreakpoint breakpoint= null;
					Map attributes = new HashMap(4);
					BreakpointUtils.addRunToLineAttributes(attributes);
					breakpoint= JDIDebugModel.createLineBreakpoint(ResourcesPlugin.getWorkspace().getRoot(), typeName[0], lineNumber[0], -1, -1, 1, false, attributes);
					errorMessage = ActionMessages.getString("RunToLineAdapter.2"); //$NON-NLS-1$
					if (target instanceof IAdaptable) {
						IDebugTarget debugTarget = (IDebugTarget) ((IAdaptable)target).getAdapter(IDebugTarget.class);
						if (debugTarget != null) {
							debugTarget.getDebugTarget().breakpointAdded(breakpoint);
							target.resume();
							return;
						}
					}
				} else {
					// invalid line
					if (selection.getLength() > 0) {
						errorMessage = ActionMessages.getString("RunToLineAdapter.3"); //$NON-NLS-1$
					} else {
						errorMessage = ActionMessages.getString("RunToLineAdapter.4"); //$NON-NLS-1$
					}

				}
			}
		}
		throw new CoreException(new Status(IStatus.ERROR, JDIDebugUIPlugin.getUniqueIdentifier(), IJavaDebugUIConstants.INTERNAL_ERROR,
				errorMessage, null));
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.actions.IRunToLineTarget#dispose()
	 */
	public void dispose() {
		editorPart = null;
	}
}
