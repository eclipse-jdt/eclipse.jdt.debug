package org.eclipse.jdt.internal.debug.ui.actions;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICodeAssist;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;
import org.eclipse.jdt.internal.debug.ui.EvaluationContextManager;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.texteditor.IEditorStatusLine;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Steps into the selected method.
 */
public class StepIntoSelectionActionDelegate implements IEditorActionDelegate, IWorkbenchWindowActionDelegate {
	
	private IEditorPart fEditorPart = null;
	private IWorkbenchWindow fWindow = null;

	/**
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		if (getActiveEditor() != null) {
			ICodeAssist codeAssist = null;
			ITextSelection textSelection = null;
			IJavaStackFrame frame = getStackFrame();
			if (frame != null && frame.isSuspended()) {
				IEditorPart part = getActiveEditor();
				if (part instanceof ITextEditor) { 
					ITextEditor editor = (ITextEditor)part;
					textSelection = (ITextSelection)editor.getSelectionProvider().getSelection();
					try {
						// ensure top stack frame
						if (frame.getThread().getTopStackFrame().equals(frame)) {
							int lineNumber = frame.getLineNumber();
							// debug line numbers are 1 based, document line numbers are 0 based
							if (textSelection.getStartLine() == (lineNumber - 1)) {
								IEditorInput input = getActiveEditor().getEditorInput();
								Object element = JavaUI.getWorkingCopyManager().getWorkingCopy(input);
								if (element == null) {
									element = input.getAdapter(IClassFile.class);
								}
								if (element instanceof ICodeAssist) {
									codeAssist = ((ICodeAssist)element);
								} else {
									// editor does not support code assist
									showErrorMessage(ActionMessages.getString("StepIntoSelectionActionDelegate.Step_into_selection_only_available_for_types_in_Java_projects._1")); //$NON-NLS-1$
									return;
								}
							} else {
								// not on current line
								showErrorMessage(ActionMessages.getString("StepIntoSelectionActionDelegate.Step_into_selection_only_available_on_current_line_of_execution._2")); //$NON-NLS-1$
								return;
							}
						} else {
							showErrorMessage(ActionMessages.getString("StepIntoSelectionActionDelegate.Step_into_selection_only_available_in_top_stack_frame._3")); //$NON-NLS-1$
							return;
						}
					} catch (CoreException e) {
						showErrorMessage(e.getStatus().getMessage());
						return;
					}
				} else {
					showErrorMessage(ActionMessages.getString("StepIntoSelectionActionDelegate.Step_into_selection_only_available_in_Java_editor._4")); //$NON-NLS-1$
					return;
				}
			} else {
				// no longer suspended - unexpected
				return;
			}
			
			IMethod method = null;
			try {
				IJavaElement[] resolve = codeAssist.codeSelect(textSelection.getOffset(), textSelection.getLength());
				for (int i = 0; i < resolve.length; i++) {
					IJavaElement javaElement = resolve[i];
					if (javaElement instanceof IMethod) {
						method = (IMethod)javaElement;
						break;
					}
				}
			} catch (CoreException e) {
				JDIDebugPlugin.log(e);
			}
		
			if (method == null) {
				// no resolved method
				showErrorMessage(ActionMessages.getString("StepIntoSelectionActionDelegate.No_Method")); //$NON-NLS-1$
			} else {
				StepIntoSelectionHandler handler = new StepIntoSelectionHandler((IJavaThread)getStackFrame().getThread(), getStackFrame(), method);
				handler.step();
			}			
		}
	}
	
	/**
	 * Displays an error message in the status area
	 * 
	 * @param message
	 */
	protected void showErrorMessage(String message) {	
		if (getActiveEditor() != null) {
			IEditorStatusLine statusLine= (IEditorStatusLine) getActiveEditor().getAdapter(IEditorStatusLine.class);
			if (statusLine != null) {
				statusLine.setMessage(true, message, null);
			}
		}		
		JDIDebugUIPlugin.getStandardDisplay().beep();		
	}


	/**
	 * @see org.eclipse.ui.IEditorActionDelegate#setActiveEditor(org.eclipse.jface.action.IAction, org.eclipse.ui.IEditorPart)
	 */
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		fEditorPart = targetEditor;
	}
	
	/**
	 * Returns the active editor or <code>null</code>.
	 * 	 * @return active editor or <code>null</code>	 */
	protected IEditorPart getActiveEditor() {
		if (fWindow != null) {
			// global action
			return fWindow.getActivePage().getActiveEditor();
		} else {
			// pop-up action
			return fEditorPart;
		}
	}

	/**
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}

	/**
	 * Returns the current stack frame context, or <code>null</code> if none.
	 * 	 * @return the current stack frame context, or <code>null</code> if none	 */
	protected IJavaStackFrame getStackFrame() {
		return EvaluationContextManager.getEvaluationContext(getActiveEditor());
	}
		
	/**
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
	 */
	public void dispose() {
	}

	/**
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
	 */
	public void init(IWorkbenchWindow window) {
		fWindow = window;
	}

}
