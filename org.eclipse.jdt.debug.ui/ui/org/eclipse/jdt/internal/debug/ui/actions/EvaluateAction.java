/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
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



import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.debug.eval.IEvaluationEngine;
import org.eclipse.jdt.debug.eval.IEvaluationListener;
import org.eclipse.jdt.debug.eval.IEvaluationResult;
import org.eclipse.jdt.debug.ui.IJavaDebugUIConstants;
import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;
import org.eclipse.jdt.internal.debug.core.JavaDebugUtils;
import org.eclipse.jdt.internal.debug.ui.EvaluationContextManager;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.JavaWordFinder;
import org.eclipse.jdt.internal.debug.ui.display.IDataDisplay;
import org.eclipse.jdt.internal.debug.ui.display.JavaInspectExpression;
import org.eclipse.jdt.internal.debug.ui.snippeteditor.ISnippetStateChangedListener;
import org.eclipse.jdt.internal.debug.ui.snippeteditor.JavaSnippetEditor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.util.Util;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ITextEditor;

import com.sun.jdi.InvocationException;
import com.sun.jdi.ObjectReference;


/**
 * Action to do simple code evaluation. The evaluation
 * is done in the UI thread and the expression and result are
 * displayed using the IDataDisplay.
 */
public abstract class EvaluateAction implements IEvaluationListener, IWorkbenchWindowActionDelegate, IObjectActionDelegate, IEditorActionDelegate, IPartListener, IViewActionDelegate, ISnippetStateChangedListener {

	private IAction fAction;
	private IWorkbenchPart fTargetPart;
	private IWorkbenchWindow fWindow;
	private Object fSelection;
	private IRegion fRegion;

	/**
	 * Is the action waiting for an evaluation.
	 */
	private boolean fEvaluating;

	/**
	 * The new target part to use with the evaluation completes.
	 */
	private IWorkbenchPart fNewTargetPart= null;

	/**
	 * Used to resolve editor input for selected stack frame
	 */
	private IDebugModelPresentation fPresentation;

	public EvaluateAction() {
		super();
	}

	/**
	 * Returns the 'object' context for this evaluation,
	 * or <code>null</code> if none. If the evaluation is being performed
	 * in the context of the variables view/inspector. Then
	 * perform the evaluation in the context of the
	 * selected value.
	 *
	 * @return Java object or <code>null</code>
	 */
	protected IJavaObject getObjectContext() {
		IWorkbenchPage page= JDIDebugUIPlugin.getActivePage();
		if (page != null) {
			IWorkbenchPart activePart= page.getActivePart();
			if (activePart != null) {
				IDebugView a = activePart.getAdapter(IDebugView.class);
				if (a != null) {
					if (a.getViewer() != null) {
						ISelection s = a.getViewer().getSelection();
						if (s instanceof IStructuredSelection) {
							IStructuredSelection structuredSelection = (IStructuredSelection)s;
							if (structuredSelection.size() == 1) {
								Object selection= structuredSelection.getFirstElement();
								if (selection instanceof IJavaVariable) {
									IJavaVariable var = (IJavaVariable)selection;
									// if 'this' is selected, use stack frame context
									try {
										if (!var.getName().equals("this")) { //$NON-NLS-1$
											IValue value= var.getValue();
											if (value instanceof IJavaObject) {
												return (IJavaObject)value;
											}
										}
									} catch (DebugException e) {
										JDIDebugUIPlugin.log(e);
									}
								} else if (selection instanceof JavaInspectExpression) {
									IValue value= ((JavaInspectExpression)selection).getValue();
									if (value instanceof IJavaObject) {
										return (IJavaObject)value;
									}
								}
							}
						}
					}
				}
			}
		}
		return null;
	}

	/**
	 * Finds the currently selected stack frame in the UI.
	 * Stack frames from a scrapbook launch are ignored.
	 */
	protected IJavaStackFrame getStackFrameContext() {
		IWorkbenchPart part = getTargetPart();
		IJavaStackFrame frame = null;
		if (part == null) {
			frame = EvaluationContextManager.getEvaluationContext(getWindow());
		} else {
			frame = EvaluationContextManager.getEvaluationContext(part);
		}
		return frame;
	}

	/**
	 * @see IEvaluationListener#evaluationComplete(IEvaluationResult)
	 */
	@Override
	public void evaluationComplete(final IEvaluationResult result) {
		// if plug-in has shutdown, ignore - see bug# 8693
		if (JDIDebugUIPlugin.getDefault() == null) {
			return;
		}

		final IJavaValue value= result.getValue();
		if (result.hasErrors() || value != null) {
			final Display display= JDIDebugUIPlugin.getStandardDisplay();
			if (display.isDisposed()) {
				return;
			}
			displayResult(result);
		}
	}

	protected void evaluationCleanup() {
		setEvaluating(false);
		setTargetPart(fNewTargetPart);
	}
	/**
	 * Display the given evaluation result.
	 */
	abstract protected void displayResult(IEvaluationResult result);

	protected void run() {
		// eval in context of object or stack frame
		final IJavaObject object = getObjectContext();
		final IJavaStackFrame stackFrame= getStackFrameContext();
		if (stackFrame == null) {
			reportError(ActionMessages.Evaluate_error_message_stack_frame_context);
			return;
		}

		// check for nested evaluation
		IJavaThread thread = (IJavaThread)stackFrame.getThread();
		if (thread.isPerformingEvaluation()) {
			reportError(ActionMessages.EvaluateAction_Cannot_perform_nested_evaluations__1);
			return;
		}

		setNewTargetPart(getTargetPart());

        IRunnableWithProgress runnable = new IRunnableWithProgress() {
            @Override
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                if (stackFrame.isSuspended()) {
					IJavaProject project = getJavaProject(stackFrame);
					if (project != null) {
                        IEvaluationEngine engine = null;
                        try {
                            Object selection= getSelectedObject();
							if (!(selection instanceof String) || ((String) selection).isEmpty()) {
                                return;
                            }
                            String expression= (String)selection;

                            engine = JDIDebugPlugin.getDefault().getEvaluationEngine(project, (IJavaDebugTarget)stackFrame.getDebugTarget());
                            setEvaluating(true);
                            boolean hitBreakpoints= Platform.getPreferencesService().getBoolean(
                            		JDIDebugPlugin.getUniqueIdentifier(),
                            		JDIDebugModel.PREF_SUSPEND_FOR_BREAKPOINTS_DURING_EVALUATION,
                            		true,
                            		null);
                            if (object == null) {
                                engine.evaluate(expression, stackFrame, EvaluateAction.this, DebugEvent.EVALUATION, hitBreakpoints);
                            } else {
                                engine.evaluate(expression, object, (IJavaThread)stackFrame.getThread(), EvaluateAction.this, DebugEvent.EVALUATION, hitBreakpoints);
                            }
                            return;
                        } catch (CoreException e) {
                            throw new InvocationTargetException(e, getExceptionMessage(e));
                        }
                    }
                    throw new InvocationTargetException(null, ActionMessages.Evaluate_error_message_src_context);
                }
                // thread not suspended
                throw new InvocationTargetException(null, ActionMessages.EvaluateAction_Thread_not_suspended___unable_to_perform_evaluation__1);
            }
        };

        IWorkbench workbench = PlatformUI.getWorkbench();
        try {
            workbench.getProgressService().busyCursorWhile(runnable);
        } catch (InvocationTargetException e) {
        	evaluationCleanup();
        	String message = e.getMessage();
        	if (message == null) {
        		message = e.getClass().getName();
        		if (e.getCause() != null) {
        			message = e.getCause().getClass().getName();
        			if (e.getCause().getMessage() != null) {
        				message = e.getCause().getMessage();
        			}
        		}
        	}
			String error = NLS.bind(ActionMessages.EvaluateAction__evaluation_failed__Reason, message);
			Status status = new Status(IStatus.WARNING, JDIDebugUIPlugin.getUniqueIdentifier(), error, e);
			JDIDebugUIPlugin.log(status);
            reportError(message);
        } catch (InterruptedException e) {
        }
	}

	protected IJavaProject getJavaProject(IStackFrame stackFrame) {

		// Get the corresponding element.
		ILaunch launch = stackFrame.getLaunch();
		if (launch == null) {
			return null;
		}
		IJavaProject javaProject = null;
		if (stackFrame instanceof IJavaStackFrame) {
			javaProject = JavaDebugUtils.resolveJavaProject((IJavaStackFrame) stackFrame);
		}
		return javaProject;
	}

	/**
	 * Updates the enabled state of the action that this is a
	 * delegate for.
	 */
	protected void update() {
		IAction action= getAction();
		if (action != null) {
			resolveSelectedObject();
		}
	}

	/**
	 * Resolves the selected object in the target part, or <code>null</code>
	 * if there is no selection.
	 */
	protected void resolveSelectedObject() {
		Object selectedObject= null;
		fRegion = null;
		ISelection selection= getTargetSelection();
		if (selection instanceof ITextSelection) {
			ITextSelection ts = (ITextSelection)selection;
			String text= ts.getText();
			if (textHasContent(text)) {
				selectedObject= text;
				fRegion = new Region(ts.getOffset(), ts.getLength());
			} else if (getTargetPart() instanceof IEditorPart) {
				IEditorPart editor= (IEditorPart)getTargetPart();
				if (editor instanceof ITextEditor) {
					selectedObject = resolveSelectedObjectUsingToken(selectedObject, ts, editor);
				}
			}
		} else if (selection instanceof IStructuredSelection) {
			if (!selection.isEmpty()) {
				if (getTargetPart().getSite().getId().equals(IDebugUIConstants.ID_DEBUG_VIEW)) {
					//work on the editor selection
					IEditorPart editor= getTargetPart().getSite().getPage().getActiveEditor();
					setTargetPart(editor);
					selection= getTargetSelection();
					if (selection instanceof ITextSelection) {
						ITextSelection ts = (ITextSelection)selection;
						String text= ts.getText();
						if (textHasContent(text)) {
							selectedObject= text;
						} else if (editor instanceof ITextEditor) {
							selectedObject= resolveSelectedObjectUsingToken(selectedObject, ts, editor);
						}
					}
				} else {
					IStructuredSelection ss= (IStructuredSelection)selection;
					Iterator<?> elements = ss.iterator();
					while (elements.hasNext()) {
						if (!(elements.next() instanceof IJavaVariable)) {
							setSelectedObject(null);
							return;
						}
					}
					selectedObject= ss;
				}
			}
		}
		setSelectedObject(selectedObject);
	}

	private Object resolveSelectedObjectUsingToken(Object selectedObject, ITextSelection ts, IEditorPart editor) {
		ITextEditor textEditor= (ITextEditor) editor;
		IDocument doc= textEditor.getDocumentProvider().getDocument(editor.getEditorInput());
		fRegion= JavaWordFinder.findWord(doc, ts.getOffset());
		if (fRegion != null) {
			try {
				selectedObject= doc.get(fRegion.getOffset(), fRegion.getLength());
			} catch (BadLocationException e) {
			}
		}
		return selectedObject;
	}

	protected ISelection getTargetSelection() {
		IWorkbenchPart part = getTargetPart();
		if (part != null) {
			ISelectionProvider provider = part.getSite().getSelectionProvider();
			if (provider != null) {
				return provider.getSelection();
			}
		}
		return null;
	}

	/**
	 * Resolve an editor input from the source element of the stack frame
	 * argument, and return whether it's equal to the editor input for the
	 * editor that owns this action.
	 */
	protected boolean compareToEditorInput(IStackFrame stackFrame) {
		ILaunch launch = stackFrame.getLaunch();
		if (launch == null) {
			return false;
		}
		Object sourceElement;
		try {
			sourceElement = JavaDebugUtils.resolveSourceElement(stackFrame, launch);
		}
		catch (CoreException e) {
			return false;
		}
		if (sourceElement == null) {
			return false;
		}
		IEditorInput sfEditorInput= getDebugModelPresentation().getEditorInput(sourceElement);
		if (getTargetPart() instanceof IEditorPart) {
			return ((IEditorPart)getTargetPart()).getEditorInput().equals(sfEditorInput);
		}
		return false;
	}

	protected Shell getShell() {
		if (getTargetPart() != null) {
			return getTargetPart().getSite().getShell();
		}
		return JDIDebugUIPlugin.getActiveWorkbenchShell();
	}

	protected IDataDisplay getDataDisplay() {
		IDataDisplay display= getDirectDataDisplay();
		if (display != null) {
			return display;
		}
		IWorkbenchPage page= JDIDebugUIPlugin.getActivePage();
		if (page != null) {
			IWorkbenchPart activePart= page.getActivePart();
			if (activePart != null) {
				IViewPart view = page.findView(IJavaDebugUIConstants.ID_DISPLAY_VIEW);
				if (view == null) {
					try {
						view= page.showView(IJavaDebugUIConstants.ID_DISPLAY_VIEW);
					} catch (PartInitException e) {
						JDIDebugUIPlugin.statusDialog(ActionMessages.EvaluateAction_Cannot_open_Display_view, e.getStatus());
					} finally {
						page.activate(activePart);
					}
				}
				if (view != null) {
					page.bringToTop(view);
					return view.getAdapter(IDataDisplay.class);
				}
			}
		}

		return null;
	}

	protected IDataDisplay getDirectDataDisplay() {
		IWorkbenchPart part= getTargetPart();
		if (part != null) {
			IDataDisplay display= part.getAdapter(IDataDisplay.class);
			if (display != null) {
				IWorkbenchPage page= JDIDebugUIPlugin.getActivePage();
				if (page != null) {
					IWorkbenchPart activePart= page.getActivePart();
					if (activePart != null) {
						if (activePart != part) {
							page.activate(part);
						}
					}
				}
				return display;
			}
		}
		IWorkbenchPage page= JDIDebugUIPlugin.getActivePage();
		if (page != null) {
			IWorkbenchPart activePart= page.getActivePart();
			if (activePart != null) {
				IDataDisplay display= activePart.getAdapter(IDataDisplay.class);
				if (display != null) {
					return display;
				}
			}
		}
		return null;
	}

	protected boolean textHasContent(String text) {
		if (text != null) {
			int length= text.length();
			if (length > 0) {
				for (int i= 0; i < length; i++) {
					if (Character.isLetterOrDigit(text.charAt(i))) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Displays a failed evaluation message in the data display.
	 */
	protected void reportErrors(IEvaluationResult result) {
		String message= getErrorMessage(result);
		reportError(message);
	}

	protected void reportError(String message) {
		IDataDisplay dataDisplay= getDirectDataDisplay();
		if (dataDisplay != null) {
			if (message.length() != 0) {
				dataDisplay.displayExpressionValue(NLS.bind(ActionMessages.EvaluateAction__evaluation_failed__Reason, format(message)));
			} else {
				dataDisplay.displayExpressionValue(ActionMessages.EvaluateAction__evaluation_failed__1);
			}
		} else {
			Status status= new Status(IStatus.ERROR, JDIDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, message, null);
			ErrorDialog.openError(getShell(), ActionMessages.Evaluate_error_title_eval_problems, null, status);
		}
	}

	private String format(String message) {
		StringBuilder result= new StringBuilder();
		int index= 0, pos;
		while ((pos= message.indexOf('\n', index)) != -1) {
			result.append("\t\t").append(message.substring(index, index= pos + 1)); //$NON-NLS-1$
		}
		if (index < message.length()) {
			result.append("\t\t").append(message.substring(index)); //$NON-NLS-1$
		}
		return result.toString();
	}

	public static String getExceptionMessage(Throwable exception) {
		if (exception instanceof CoreException) {
			CoreException ce = (CoreException)exception;
			Throwable throwable= ce.getStatus().getException();
			if (throwable instanceof com.sun.jdi.InvocationException) {
				return getInvocationExceptionMessage((com.sun.jdi.InvocationException)throwable);
			} else if (throwable instanceof CoreException) {
				// Traverse nested CoreExceptions
				return getExceptionMessage(throwable);
			}
			return ce.getStatus().getMessage();
		}
		String message= NLS.bind(ActionMessages.Evaluate_error_message_direct_exception, new Object[] { exception.getClass() });
		if (exception.getMessage() != null) {
			message= NLS.bind(ActionMessages.Evaluate_error_message_exception_pattern, new Object[] { message, exception.getMessage() });
		}
		return message;
	}

	/**
	 * Returns a message for the exception wrapped in an invocation exception
	 */
	protected static String getInvocationExceptionMessage(com.sun.jdi.InvocationException exception) {
			InvocationException ie= exception;
			ObjectReference ref= ie.exception();
			return NLS.bind(ActionMessages.Evaluate_error_message_wrapped_exception, new Object[] { ref.referenceType().name() });
	}

	protected String getErrorMessage(IEvaluationResult result) {
		String[] errors= result.getErrorMessages();
		if (errors.length == 0) {
			return getExceptionMessage(result.getException());
		}
		return getErrorMessage(errors);
	}

	protected String getErrorMessage(String[] errors) {
		String message = Util.ZERO_LENGTH_STRING;
		for (int i= 0; i < errors.length; i++) {
			String msg= errors[i];
			if (i == 0) {
				message= msg;
			} else {
				message= NLS.bind(ActionMessages.Evaluate_error_problem_append_pattern, new Object[] { message, msg });
			}
		}
		return message;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(IAction)
	 */
	@Override
	public void run(IAction action) {
		update();
		run();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		setAction(action);
	}

	/**
	 * @see IWorkbenchWindowActionDelegate#dispose()
	 */
	@Override
	public void dispose() {
		disposeDebugModelPresentation();
		IWorkbenchWindow win = getWindow();
		if (win != null) {
			win.getPartService().removePartListener(this);
		}
	}

	/**
	 * @see IWorkbenchWindowActionDelegate#init(IWorkbenchWindow)
	 */
	@Override
	public void init(IWorkbenchWindow window) {
		setWindow(window);
		IWorkbenchPage page= window.getActivePage();
		if (page != null) {
			setTargetPart(page.getActivePart());
		}
		window.getPartService().addPartListener(this);
		update();
	}

	protected IAction getAction() {
		return fAction;
	}

	protected void setAction(IAction action) {
		fAction = action;
	}

	/**
	 * Returns a debug model presentation (creating one
	 * if necessary).
	 *
	 * @return debug model presentation
	 */
	protected IDebugModelPresentation getDebugModelPresentation() {
		if (fPresentation == null) {
			fPresentation = DebugUITools.newDebugModelPresentation(JDIDebugModel.getPluginIdentifier());
		}
		return fPresentation;
	}

	/**
	 * Disposes this action's debug model presentation, if
	 * one was created.
	 */
	protected void disposeDebugModelPresentation() {
		if (fPresentation != null) {
			fPresentation.dispose();
		}
	}

	/**
	 * @see IEditorActionDelegate#setActiveEditor(IAction, IEditorPart)
	 */
	@Override
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		setAction(action);
		setTargetPart(targetEditor);
	}

	/**
	 * @see IPartListener#partActivated(IWorkbenchPart)
	 */
	@Override
	public void partActivated(IWorkbenchPart part) {
		setTargetPart(part);
	}

	/**
	 * @see IPartListener#partBroughtToTop(IWorkbenchPart)
	 */
	@Override
	public void partBroughtToTop(IWorkbenchPart part) {
	}

	/**
	 * @see IPartListener#partClosed(IWorkbenchPart)
	 */
	@Override
	public void partClosed(IWorkbenchPart part) {
		if (part == getTargetPart()) {
			setTargetPart(null);
		}
		if (part == getNewTargetPart()) {
			setNewTargetPart(null);
		}
	}

	/**
	 * @see IPartListener#partDeactivated(IWorkbenchPart)
	 */
	@Override
	public void partDeactivated(IWorkbenchPart part) {
	}

	/**
	 * @see IPartListener#partOpened(IWorkbenchPart)
	 */
	@Override
	public void partOpened(IWorkbenchPart part) {
	}

	/**
	 * @see IViewActionDelegate#init(IViewPart)
	 */
	@Override
	public void init(IViewPart view) {
		setTargetPart(view);
	}

	protected IWorkbenchPart getTargetPart() {
		return fTargetPart;
	}

	protected void setTargetPart(IWorkbenchPart part) {
		if (isEvaluating()) {
			//do not want to change the target part while evaluating
			//see bug 8334
			setNewTargetPart(part);
		} else {
			if (getTargetPart() instanceof JavaSnippetEditor) {
				((JavaSnippetEditor)getTargetPart()).removeSnippetStateChangedListener(this);
			}
			fTargetPart= part;
			if (part instanceof JavaSnippetEditor) {
				((JavaSnippetEditor)part).addSnippetStateChangedListener(this);
			}
		}
	}

	protected IWorkbenchWindow getWindow() {
		return fWindow;
	}

	protected void setWindow(IWorkbenchWindow window) {
		fWindow = window;
	}

	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		setAction(action);
		setTargetPart(targetPart);
		update();
	}

	protected Object getSelectedObject() {
		return fSelection;
	}

	protected void setSelectedObject(Object selection) {
		fSelection = selection;
	}

	/**
	 * @see ISnippetStateChangedListener#snippetStateChanged(JavaSnippetEditor)
	 */
	@Override
	public void snippetStateChanged(JavaSnippetEditor editor) {
		if (editor != null && !editor.isEvaluating() && editor.getFile() != null) {
			update();
			getAction().setEnabled(getSelectedObject() != null);
		} else {
			getAction().setEnabled(false);
		}
	}

	protected IWorkbenchPart getNewTargetPart() {
		return fNewTargetPart;
	}

	protected void setNewTargetPart(IWorkbenchPart newTargetPart) {
		fNewTargetPart = newTargetPart;
	}

	protected boolean isEvaluating() {
		return fEvaluating;
	}

	protected void setEvaluating(boolean evaluating) {
		fEvaluating = evaluating;
	}

	/**
	 * Returns the selected text region, or <code>null</code> if none.
	 */
	protected IRegion getRegion() {
		return fRegion;
	}

	/**
	 * Returns the styled text widget associated with the given part
	 * or <code>null</code> if none.
	 *
	 * @param part workbench part
	 * @return associated style text widget or <code>null</code>
	 */
	public static StyledText getStyledText(IWorkbenchPart part) {
		ITextViewer viewer = part.getAdapter(ITextViewer.class);
		StyledText textWidget = null;
		if (viewer == null) {
			Control control = part.getAdapter(Control.class);
			if (control instanceof StyledText) {
				textWidget = (StyledText) control;
			}
		} else {
			textWidget = viewer.getTextWidget();
		}
		return textWidget;
	}

	/**
	 * Returns an anchor point for a popup dialog on top of a styled text or <code>null</code> if none.
	 *
	 * @param textWidget
	 *            StyledText or <code>null</code>
	 * @return anchor point or <code>null</code>
	 */
	public static Point getPopupAnchor(StyledText textWidget) {
		if (textWidget != null) {
	        Point docRange = textWidget.getSelectionRange();
	        int midOffset = docRange.x + (docRange.y / 2);
	        Point point = textWidget.getLocationAtOffset(midOffset);
	        point = textWidget.toDisplay(point);

	        GC gc = new GC(textWidget);
	        gc.setFont(textWidget.getFont());
	        int height = gc.getFontMetrics().getHeight();
	        gc.dispose();
	        point.y += height;
	        return point;
		}
		return null;
	}
}
