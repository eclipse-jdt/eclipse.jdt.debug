package org.eclipse.jdt.internal.debug.ui.actions;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICodeAssist;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

/**
 * Steps into the selected method.
 */
public class StepIntoSelectionActionDelegate implements IEditorActionDelegate, IWorkbenchWindowActionDelegate, IPartListener {
	
	private IEditorPart fEditorPart = null;
	private ITextSelection fTextSelection = null;
	private ICodeAssist fCodeAssist = null;
	private IWorkbenchWindow fWindow = null;

	/**
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		ICodeAssist codeAssist = getCodeAssist();
		ITextSelection textSelection = getTextSelection();
		
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
			JDIDebugUIPlugin.getStandardDisplay().beep();
		} else {
			StepIntoSelectionHandler handler = new StepIntoSelectionHandler((IJavaThread)getStackFrame().getThread(), getStackFrame(), method);
			handler.step();
		}
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
		return fEditorPart;
	}

	/**
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		setCodeAssist(null);
		setTextSelection(null);
		IJavaStackFrame frame = getStackFrame();
		boolean enabled = false;
		if (getActiveEditor() != null && frame != null && frame.isSuspended() && selection instanceof ITextSelection && !selection.isEmpty()) {
			ITextSelection textSelection = (ITextSelection)selection;
			try {
				int lineNumber = frame.getLineNumber();
				// debug line numbers are 1 based, document line numbers are 0 based
				if (textSelection.getStartLine() == (lineNumber - 1)) {
					IEditorInput input = getActiveEditor().getEditorInput();
					Object element = JavaUI.getWorkingCopyManager().getWorkingCopy(input);
					if (element == null) {
						element = input.getAdapter(IClassFile.class);
					}
					if (element instanceof ICodeAssist) {
						setCodeAssist((ICodeAssist)element);
						setTextSelection(textSelection);
						enabled = true;
					}
				}
			} catch (CoreException e) {
				if (e.getStatus().getCode() != IJavaThread.ERR_THREAD_NOT_SUSPENDED) {
					// do not log "thread not suspended" errors
					JDIDebugUIPlugin.log(e);
				}
			}
		}
		action.setEnabled(enabled);
	}

	/**
	 * Returns the current stack frame context, or <code>null</code> if none.
	 * 	 * @return the current stack frame context, or <code>null</code> if none	 */
	protected IJavaStackFrame getStackFrame() {
		IAdaptable context = DebugUITools.getDebugContext();
		if (context instanceof IJavaStackFrame) {
			return (IJavaStackFrame)context;
		}
		return null;
	}
	
	/**
	 * Sets the resolved code assist element
	 * 	 * @param codeAssist	 */
	protected void setCodeAssist(ICodeAssist codeAssist) {
		fCodeAssist = codeAssist;
	}
	
	/**
	 * Returns the resolved code assist element, or <code>null</code>
	 * 
	 * @return the resolved code assist element, or <code>null</code>
	 */
	protected ICodeAssist getCodeAssist() {
		return fCodeAssist;
	}
	
	/**
	 * Sets the resolved text selection
	 * 
	 * @param textSelection
	 */
	protected void setTextSelection(ITextSelection textSelection) {
		fTextSelection = textSelection;
	}
	
	/**
	 * Returns the resolved text selection, or <code>null</code>
	 * 
	 * @return the resolved text selection, or <code>null</code>
	 */
	protected ITextSelection getTextSelection() {
		return fTextSelection;
	}	
	/**
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
	 */
	public void dispose() {
		fWindow.getPartService().removePartListener(this);
	}

	/**
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
	 */
	public void init(IWorkbenchWindow window) {
		fWindow = window;
		window.getPartService().addPartListener(this);
	}

	/**
	 * @see org.eclipse.ui.IPartListener#partActivated(org.eclipse.ui.IWorkbenchPart)
	 */
	public void partActivated(IWorkbenchPart part) {
		if (part instanceof IEditorPart) {
			fEditorPart = (IEditorPart)part;
		}
	}

	/**
	 * @see org.eclipse.ui.IPartListener#partBroughtToTop(org.eclipse.ui.IWorkbenchPart)
	 */
	public void partBroughtToTop(IWorkbenchPart part) {
	}

	/**
	 * @see org.eclipse.ui.IPartListener#partClosed(org.eclipse.ui.IWorkbenchPart)
	 */
	public void partClosed(IWorkbenchPart part) {
		if (part == fEditorPart) {
			fEditorPart = null;
		}
	}

	/**
	 * @see org.eclipse.ui.IPartListener#partDeactivated(org.eclipse.ui.IWorkbenchPart)
	 */
	public void partDeactivated(IWorkbenchPart part) {
	}

	/**
	 * @see org.eclipse.ui.IPartListener#partOpened(org.eclipse.ui.IWorkbenchPart)
	 */
	public void partOpened(IWorkbenchPart part) {
		if (part instanceof IEditorPart) {
			fEditorPart = (IEditorPart)part;
		}
	}

}
