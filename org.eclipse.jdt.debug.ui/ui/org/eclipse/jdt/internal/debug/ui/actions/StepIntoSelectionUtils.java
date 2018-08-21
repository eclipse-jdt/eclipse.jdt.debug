/*******************************************************************************
 * Copyright (c) 2007, 2015 IBM Corporation and others.
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
import org.eclipse.jdt.core.ICodeAssist;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.compiler.IScanner;
import org.eclipse.jdt.core.compiler.ITerminalSymbols;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.internal.debug.ui.EvaluationContextManager;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.IEditorStatusLine;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Utility class for aiding step into selection actions and hyper-linking
 *
 * @see StepIntoSelectionActionDelegate
 * @see StepIntoSelectionHyperlinkDetector
 *
 * @since 3.3
 */
public class StepIntoSelectionUtils {


	/**
     * gets the <code>IJavaElement</code> from the editor input
     * @param input the current editor input
     * @return the corresponding <code>IJavaElement</code>
     */
    public static IJavaElement getJavaElement(IEditorInput input) {
    	IJavaElement je = JavaUI.getEditorInputJavaElement(input);
    	if(je != null) {
    		return je;
    	}
    	return JavaUI.getWorkingCopyManager().getWorkingCopy(input);
    }

    /**
     * Returns the <code>IMethod</code> from the given selection within the given <code>IJavaElement</code>,
     * or <code>null</code> if the selection does not container or is not an <code>IMethod</code>
     * @param selection
     * @param element
     * @return the corresponding <code>IMethod</code> from the selection within the provided <code>IJavaElement</code>
     * @throws JavaModelException
     */
    public static IMethod getMethod(ITextSelection selection, IJavaElement element) throws JavaModelException {
    	if(element != null && element instanceof ICodeAssist) {
    		return resolveMethod(selection.getOffset(), selection.getLength(), (ICodeAssist)element);
    	}
    	return null;
    }

	/**
	 * @param offset selection offset
	 * @param length selection length
	 * @param codeAssist context
	 * @return the method at the given position, or <code>null</code> if no method could be resolved
	 * @throws JavaModelException
	 */
	private static IMethod resolveMethod(int offset, int length, ICodeAssist codeAssist) throws JavaModelException {
		IJavaElement[] elements = codeAssist.codeSelect(offset, length);
		for (int i = 0; i < elements.length; i++) {
			if (elements[i] instanceof IMethod) {
				return (IMethod)elements[i];
			}
		}
		return null;
	}

	/**
	 * @param offset
	 * @param activeEditor
	 * @param element
	 * @return the first method found at or after <code>offset</code> on the same line
	 * @throws JavaModelException
	 */
	public static IMethod getFirstMethodOnLine(int offset, IEditorPart activeEditor, IJavaElement element) throws JavaModelException {
		if (! (activeEditor instanceof ITextEditor) || ! (element instanceof ICodeAssist)) {
			return null;
		}
		ITextEditor editor = (ITextEditor)activeEditor;
		IDocumentProvider documentProvider = editor.getDocumentProvider();
		if (documentProvider == null) {
			return null;
		}
		IDocument document = documentProvider.getDocument(editor.getEditorInput());
		if (document == null) {
			return null;
		}
		try {
			IRegion lineInfo = document.getLineInformationOfOffset(offset);
			String line = document.get(lineInfo.getOffset(), lineInfo.getLength());
			IScanner scanner = ToolFactory.createScanner(false, false, false, null, JavaCore.VERSION_1_5);
			scanner.setSource(line.toCharArray());
			scanner.resetTo(offset - lineInfo.getOffset(), lineInfo.getLength());
			int token = scanner.getNextToken();
			while (token != ITerminalSymbols.TokenNameEOF) {
				if (token == ITerminalSymbols.TokenNameIdentifier) {
					int methodStart = scanner.getCurrentTokenStartPosition();
					token = scanner.getNextToken();
					if (token == ITerminalSymbols.TokenNameLPAREN) {
						return resolveMethod(lineInfo.getOffset() + methodStart, 0, (ICodeAssist)element);
					}
				}
				else {
					token = scanner.getNextToken();
				}
			}
		}
		catch (BadLocationException e) {
			return null;
		}
		catch (InvalidInputException e) {
			return null;
		}
		return null;
	}

	/**
	 * Steps into the selection described by the given {@link IRegion}
	 *
	 * @param region the region of the selection or <code>null</code> if we should use the page's selection service to compute
	 * the selection
	 *
	 * @since 3.6.200
	 */
	public static void stepIntoSelection(ITextSelection selection) {
		IWorkbenchPage page = JDIDebugUIPlugin.getActiveWorkbenchWindow().getActivePage();
		if(page != null) {
			IEditorPart editor = page.getActiveEditor();
			if(editor instanceof ITextEditor) {
				IJavaStackFrame frame = EvaluationContextManager.getEvaluationContext(editor);
				if (frame == null || !frame.isSuspended()) {
					// no longer suspended - unexpected
					return;
				}
				if(selection == null) {
					//grab it from the provider, either the passed region was null or we failed to get it
					selection = (ITextSelection)editor.getEditorSite().getSelectionProvider().getSelection();
				}
				try {
					IJavaElement javaElement= StepIntoSelectionUtils.getJavaElement(editor.getEditorInput());
					IMethod method = StepIntoSelectionUtils.getMethod(selection, javaElement);
					if (method == null) {
						method = StepIntoSelectionUtils.getFirstMethodOnLine(selection.getOffset(), editor, javaElement);
					}
					IType callingType = getType(selection);
					if (method == null || callingType == null) {
						return;
					}
					int lineNumber = frame.getLineNumber();
		            String callingTypeName = stripInnerNamesAndParameterType(callingType.getFullyQualifiedName());
		            String frameName = stripInnerNamesAndParameterType(frame.getDeclaringTypeName());
					// debug line numbers are 1 based, document line numbers are 0 based
					if (selection.getStartLine() == (lineNumber - 1) && callingTypeName.equals(frameName)) {
		                doStepIn(editor, frame, method);
					} else {
						// not on current line
						runToLineBeforeStepIn(editor, callingTypeName, selection, frame.getThread(), method);
						return;
					}
				}
				catch (DebugException e) {
					showErrorMessage(editor, e.getStatus().getMessage());
					return;
				}
				catch(JavaModelException jme) {
					showErrorMessage(editor, jme.getStatus().getMessage());
					return;
				}
			}
		}
	}

	/**
	 * When the user chooses to "step into selection" on a line other than
	 * the currently executing one, first perform a "run to line" to get to
	 * the desired location, then perform a "step into selection."
	 */
	static void runToLineBeforeStepIn(final IEditorPart editor, final String typeName, ITextSelection textSelection, final IThread thread, final IMethod method) {
		final int line = textSelection.getStartLine() + 1;
		if (typeName == null || line == -1) {
			return;
		}
		// see bug 65489 - get the run-to-line adapter from the editor
		IRunToLineTarget runToLineAction = null;
		if (editor != null) {
			runToLineAction  = editor.getAdapter(IRunToLineTarget.class);
			if (runToLineAction == null) {
				IAdapterManager adapterManager = Platform.getAdapterManager();
				if (adapterManager.hasAdapter(editor,   IRunToLineTarget.class.getName())) {
					runToLineAction = (IRunToLineTarget) adapterManager.loadAdapter(editor,IRunToLineTarget.class.getName());
				}
			}
		}
		// if no adapter exists, use the Java adapter
		if (runToLineAction == null) {
		  runToLineAction = new RunToLineAdapter();
		}
		final IDebugEventSetListener listener = new IDebugEventSetListener() {
			/**
			 * @see IDebugEventSetListener#handleDebugEvents(DebugEvent[])
			 */
			@Override
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
							plugin.removeDebugEventListener(this);
							plugin.asyncExec(new Runnable() {
								@Override
								public void run() {
									try {
										doStepIn(editor, frame, method);
									} catch (DebugException e) {
										showErrorMessage(editor, e.getStatus().getMessage());
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
					line == frame.getLineNumber() &&
					frame.getReceivingTypeName().equals(typeName);
			}
			/**
			 * When the debug target we're listening for terminates, stop listening
			 * to debug events.
			 * @param event the debug event
			 */
			private void handleTerminateEvent(DebugEvent event) {
				Object source = event.getSource();
				if (thread.getDebugTarget() == source) {
					DebugPlugin.getDefault().removeDebugEventListener(this);
				}
			}
		};
		DebugPlugin.getDefault().addDebugEventListener(listener);
		try {
			runToLineAction.runToLine(editor, textSelection, thread);
		} catch (CoreException e) {
			DebugPlugin.getDefault().removeDebugEventListener(listener);
			showErrorMessage(editor, ActionMessages.StepIntoSelectionActionDelegate_4);
			JDIDebugUIPlugin.log(e.getStatus());
		}
	}

	/**
	 * Steps into the given method in the given stack frame
	 *
	 * @param editor
	 * @param frame the frame in which the step should begin
	 * @param method the method to step into
	 * @throws DebugException
	 */
	static void doStepIn(IEditorPart editor, IJavaStackFrame frame, IMethod method) throws DebugException {
		// ensure top stack frame
		IStackFrame tos = frame.getThread().getTopStackFrame();
		if (tos == null) {
			return;
		}
		if (!tos.equals(frame)) {
			showErrorMessage(editor, ActionMessages.StepIntoSelectionActionDelegate_Step_into_selection_only_available_in_top_stack_frame__3);
			return;
		}
		StepIntoSelectionHandler handler = new StepIntoSelectionHandler((IJavaThread)frame.getThread(), frame, method);
		handler.step();
	}

	/**
	 * Displays an error message in the status area
	 *
	 * @param editor
	 * @param message
	 */
	static void showErrorMessage(IEditorPart editor, String message) {
		if (editor != null) {
			IEditorStatusLine statusLine = editor.getAdapter(IEditorStatusLine.class);
			if (statusLine != null) {
				statusLine.setMessage(true, message, null);
			}
		}
	}

	/**
	 * Return the type containing the selected text, or <code>null</code> if the
	 * selection is not in a type.
	 */
	static IType getType(ITextSelection textSelection) {
		IMember member= ActionDelegateHelper.getDefault().getCurrentMember(textSelection);
		IType type= null;
		if (member instanceof IType) {
			type = (IType)member;
		} else if (member != null) {
			type= member.getDeclaringType();
		}
		return type;
	}

	/**
     * Strips inner class names and parameterized type information from the given type name.
     *
     * @param fullyQualifiedName
     */
    static String stripInnerNamesAndParameterType(String fullyQualifiedName) {
        // ignore inner class qualification, as the compiler generated names and java model names can be different
    	String sig = fullyQualifiedName;
        int index = sig.indexOf('$');
        if (index > 0) {
           sig = sig.substring(0, index);
        }
        //also ignore erasure
        index = sig.indexOf('<');
        if (index > 0){
        	sig = sig.substring(0, index);
        }
        return sig;
    }
}
