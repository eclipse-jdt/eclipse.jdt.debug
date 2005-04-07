/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.actions;


import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.ui.actions.IRunToLineTarget;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICodeAssist;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
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
	 * The name of the type being "run to".
	 * @see StepIntoSelectionActionDelegate#runToLineBeforeStepIn(ITextSelection, IJavaStackFrame, IMethod)
	 */
	private String runToLineType= null;
	/**
	 * The line number being "run to."
	 * @see StepIntoSelectionActionDelegate#runToLineBeforeStepIn(ITextSelection, IJavaStackFrame, IMethod)
	 */
	private int runToLineLine= -1;
	/**
	 * The debug event list listener used to know when a run to line has finished.
	 * @see StepIntoSelectionActionDelegate#runToLineBeforeStepIn(ITextSelection, IJavaStackFrame, IMethod)
	 */
	private IDebugEventSetListener listener= null;

	/**
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		IJavaStackFrame frame = getStackFrame();
		if (frame == null || !frame.isSuspended()) {
			// no longer suspended - unexpected
			return;
		}
		ITextSelection textSelection= getTextSelection();
		IMethod method= getMethod();
		IType callingType= getType();
		if (method == null || callingType == null) {
			return;
		}
		try {
			int lineNumber = frame.getLineNumber();
            String callingTypeName = stripInnerNames(callingType.getFullyQualifiedName());
            String frameName = stripInnerNames(frame.getDeclaringTypeName());
			// debug line numbers are 1 based, document line numbers are 0 based
			if (textSelection.getStartLine() == (lineNumber - 1) && callingTypeName.equals(frameName)) {
                doStepIn(frame, method);
			} else {
				// not on current line
				runToLineBeforeStepIn(textSelection, frame.getThread(), method);
				return;
			}
		} catch (DebugException e) {
			showErrorMessage(e.getStatus().getMessage());
			return;
		}
	}

    /**
     * Strips inner class names from the given type name.
     * 
     * @param fullyQualifiedName
     */
    private String stripInnerNames(String fullyQualifiedName) {
        // ignore inner class qualification, as the compiler generated names and java model names can be different
        int index = fullyQualifiedName.indexOf('$');
        if (index > 0) {
            return fullyQualifiedName.substring(0, index);
        }
        return fullyQualifiedName;
    }
	
	/**
	 * Steps into the given method in the given stack frame
	 * @param frame the frame in which the step should begin
	 * @param method the method to step into
	 * @throws DebugException
	 */
	private void doStepIn(IJavaStackFrame frame, IMethod method) throws DebugException {
		// ensure top stack frame
		IStackFrame tos = frame.getThread().getTopStackFrame();
		if (tos == null) {
			return; 
		}		
		if (!tos.equals(frame)) {
			showErrorMessage(ActionMessages.StepIntoSelectionActionDelegate_Step_into_selection_only_available_in_top_stack_frame__3); //$NON-NLS-1$
			return;
		}
		StepIntoSelectionHandler handler = new StepIntoSelectionHandler((IJavaThread)frame.getThread(), frame, method);
		handler.step();
	}
	
	/**
	 * When the user chooses to "step into selection" on a line other than
	 * the currently executing one, first perform a "run to line" to get to
	 * the desired location, then perform a "step into selection."
	 */
	private void runToLineBeforeStepIn(ITextSelection textSelection, final IThread thread, final IMethod method) {
		runToLineType= getType().getFullyQualifiedName();
		runToLineLine= textSelection.getStartLine() + 1;
		if (runToLineType == null || runToLineLine == -1) {
			return;
		}
		// see bug 65489 - get the run-to-line adapater from the editor
		IRunToLineTarget runToLineAction = null;
		IEditorPart ed = getActiveEditor();
		if (ed != null) {
			runToLineAction  = (IRunToLineTarget) ed.getAdapter(IRunToLineTarget.class);
			if (runToLineAction == null) {
				IAdapterManager adapterManager = Platform.getAdapterManager();
				if (adapterManager.hasAdapter(ed,   IRunToLineTarget.class.getName())) { 
					runToLineAction = (IRunToLineTarget) adapterManager.loadAdapter(ed,IRunToLineTarget.class.getName()); 
				}
			}
		}	
		// if no adapter exists, use the Java adapter
		if (runToLineAction == null) {
		  runToLineAction = new RunToLineAdapter();
		}
		listener= new IDebugEventSetListener() {
			/**
			 * @see IDebugEventSetListener#handleDebugEvents(DebugEvent[])
			 */
			public void handleDebugEvents(DebugEvent[] events) {
				for (int i = 0; i < events.length; i++) {
					DebugEvent event = events[i];
					switch (event.getKind()) {
						case DebugEvent.SUSPEND :
							handleSuspendEvent(event);
							break;
						case DebugEvent.TERMINATE :
							handleTerminateEvent(event);
							break;
						default :
							break;
					}
				}
			}
			/**
			 * Listen for the completion of the "run to line." When the thread
			 * suspends at the correct location, perform a "step into selection"
			 * @param event the debug event
			 */
			private void handleSuspendEvent(DebugEvent event) {
				Object source = event.getSource();
				if (source instanceof IJavaThread) {
					try {
						final IJavaStackFrame frame= (IJavaStackFrame) ((IJavaThread) source).getTopStackFrame();
						if (isExpectedFrame(frame)) {
							DebugPlugin plugin = DebugPlugin.getDefault();
							plugin.removeDebugEventListener(listener);
							plugin.asyncExec(new Runnable() {
								public void run() {
									try {
										doStepIn(frame, method);
									} catch (DebugException e) {
										showErrorMessage(e.getStatus().getMessage());
									}
								}
							});
						}
					} catch (DebugException e) {
						return;
					}
				}
			}
			/**
			 * Returns whether the given frame is the frame that this action is expecting.
			 * This frame is expecting a stack frame for the suspension of the "run to line".
			 * @param frame the given stack frame or <code>null</code>
			 * @return whether the given stack frame is the expected frame
			 * @throws DebugException
			 */
			private boolean isExpectedFrame(IJavaStackFrame frame) throws DebugException {
				return frame != null &&
					runToLineLine == frame.getLineNumber() &&
					frame.getReceivingTypeName().equals(runToLineType);
			}
			/**
			 * When the debug target we're listening for terminates, stop listening
			 * to debug events.
			 * @param event the debug event
			 */
			private void handleTerminateEvent(DebugEvent event) {
				Object source = event.getSource();
				if (thread.getDebugTarget() == source) {
					DebugPlugin.getDefault().removeDebugEventListener(listener);
				}
			}
		};
		DebugPlugin.getDefault().addDebugEventListener(listener);
		try {
			runToLineAction.runToLine(getActiveEditor(), textSelection, thread);
		} catch (CoreException e) {
			DebugPlugin.getDefault().removeDebugEventListener(listener);
			showErrorMessage(ActionMessages.StepIntoSelectionActionDelegate_4); //$NON-NLS-1$
			JDIDebugUIPlugin.log(e.getStatus());
		}
	}
	
	private ITextSelection getTextSelection() {
		IEditorPart part = getActiveEditor();
		if (part instanceof ITextEditor) { 
			ITextEditor editor = (ITextEditor)part;
			return (ITextSelection)editor.getSelectionProvider().getSelection();
		}
		showErrorMessage(ActionMessages.StepIntoSelectionActionDelegate_Step_into_selection_only_available_in_Java_editor__4); //$NON-NLS-1$
		return null;
	}
	
	private IMethod getMethod() {
		ITextSelection textSelection= getTextSelection();
		IEditorInput input = getActiveEditor().getEditorInput();
		ICodeAssist codeAssist = null;
		Object element = JavaUI.getWorkingCopyManager().getWorkingCopy(input);
		if (element == null) {
			element = input.getAdapter(IClassFile.class);
		}
		if (element instanceof ICodeAssist) {
			codeAssist = ((ICodeAssist)element);
		} else {
			// editor does not support code assist
			showErrorMessage(ActionMessages.StepIntoSelectionActionDelegate_Step_into_selection_only_available_for_types_in_Java_projects__1); //$NON-NLS-1$
			return null;
		}
		
		IMethod method = null;
		try {
			IJavaElement[] resolve = codeAssist.codeSelect(textSelection.getOffset(), 0);
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
			showErrorMessage(ActionMessages.StepIntoSelectionActionDelegate_No_Method); //$NON-NLS-1$
		}
		return method;
	}
	
	/**
	 * Return the type containing the selected text, or <code>null</code> if the
	 * selection is not in a type.
	 */
	protected IType getType() {
		IMember member= ActionDelegateHelper.getDefault().getCurrentMember(getTextSelection());
		IType type= null;
		if (member instanceof IType) {
			type = (IType)member;
		} else if (member != null) {
			type= member.getDeclaringType();
		}
		return type;
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
	 * 
	 * @return active editor or <code>null</code>
	 */
	protected IEditorPart getActiveEditor() {
		if (fWindow != null) {
			// global action
			return fWindow.getActivePage().getActiveEditor();
		}
		// pop-up action
		return fEditorPart;
	}

	/**
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}

	/**
	 * Returns the current stack frame context, or <code>null</code> if none.
	 * 
	 * @return the current stack frame context, or <code>null</code> if none
	 */
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
