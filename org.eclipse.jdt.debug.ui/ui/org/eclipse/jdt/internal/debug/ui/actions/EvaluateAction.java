package org.eclipse.jdt.internal.debug.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.text.MessageFormat;
import java.util.Iterator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.Message;
import org.eclipse.jdt.debug.core.IJavaArray;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaExceptionBreakpoint;
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
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.display.DataDisplay;
import org.eclipse.jdt.internal.debug.ui.display.IDataDisplay;
import org.eclipse.jdt.internal.debug.ui.display.JavaInspectExpression;
import org.eclipse.jdt.internal.debug.ui.snippeteditor.ISnippetStateChangedListener;
import org.eclipse.jdt.internal.debug.ui.snippeteditor.JavaSnippetEditor;
import org.eclipse.jdt.internal.debug.ui.snippeteditor.ScrapbookLauncher;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;

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
	private IEditorPart fTargetEditor;
	private IWorkbenchWindow fWindow;
	private Object fSelection;
	/**
	 * Used in evaluationTimedOut
	 */
	private boolean fKeepWaiting;
	
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
		IWorkbenchPage page= JDIDebugUIPlugin.getDefault().getActivePage();
		if (page != null) {
			IWorkbenchPart activePart= page.getActivePart();
			if (activePart != null) {
				IDebugView a = (IDebugView)activePart.getAdapter(IDebugView.class);
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
											if (value instanceof IJavaObject && !(value instanceof IJavaArray)) {
												return (IJavaObject)value;
											}
										} 
									} catch (DebugException e) {
										JDIDebugUIPlugin.log(e);
									}
								} else if (selection instanceof JavaInspectExpression) {
									IValue value= ((JavaInspectExpression)selection).getValue();
									if (value instanceof IJavaObject && !(value instanceof IJavaArray)) {
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
	protected IStackFrame getStackFrameContext() {
		IAdaptable context = DebugUITools.getDebugContext();
		if (context != null) {
			if (context instanceof IStackFrame) {
				IStackFrame frame= (IStackFrame)context;
				if (frame.getLaunch().getAttribute(ScrapbookLauncher.SCRAPBOOK_LAUNCH) == null) {
					return frame;
				}
			}
			if (context instanceof IThread) {
				try {
					IThread thread= (IThread)context;
					if (thread.getLaunch().getAttribute(ScrapbookLauncher.SCRAPBOOK_LAUNCH) == null) {
						return thread.getTopStackFrame();
					}
				} catch (DebugException e) {
					JDIDebugUIPlugin.log(e);
				}
			}
		}
		return null;
	}
	
	/**
	 * @see IEvaluationListener#evaluationComplete(IEvaluationResult)
	 */
	public void evaluationComplete(final IEvaluationResult result) {
		final IJavaValue value= result.getValue();
		if (result.hasErrors() || value != null) {
			final Display display= JDIDebugUIPlugin.getStandardDisplay();
			if (display.isDisposed()) {
				return;
			}
			display.asyncExec(new Runnable() {
				public void run() {
					if (display.isDisposed()) {
						return;
					}
					if (result.hasErrors()) {
						reportErrors(result);
					} else if (value != null) {
						displayResult(result);
					}
				}
			});
		}
	}
	
	/**
	 * Display the given evaluation result.
	 */
	abstract protected void displayResult(IEvaluationResult result);	
	
	protected void run() {		
		// eval in context of object or stack frame
		IJavaObject object = getObjectContext();		
		IStackFrame stackFrame= getStackFrameContext();
		if (stackFrame == null) {
			reportError(ActionMessages.getString("Evaluate.error.message.stack_frame_context")); //$NON-NLS-1$
			return;
		}
		
		IJavaStackFrame jFrame = null;
		if (stackFrame != null) {
			jFrame = (IJavaStackFrame) stackFrame.getAdapter(IJavaStackFrame.class);
		}

		if (jFrame != null) {
			IJavaElement javaElement= getJavaElement(stackFrame);
			if (javaElement != null) {
				IJavaProject project = javaElement.getJavaProject();
				IEvaluationEngine engine = null;
				try {
					Object selection= getSelectedObject();
					if (!(selection instanceof String)) {
						return;
					}
					String expression= (String)selection;
					
					IDataDisplay dataDisplay= getDataDisplay();
					if (dataDisplay != null) {
						dataDisplay.displayExpression(expression);
					}
					
					engine = JDIDebugUIPlugin.getDefault().getEvaluationEngine(project, (IJavaDebugTarget)jFrame.getDebugTarget());
					if (object == null) {
						engine.evaluate(expression, jFrame, this, DebugEvent.EVALUATION, true);
					} else {
						engine.evaluate(expression, object, (IJavaThread)jFrame.getThread(), this, DebugEvent.EVALUATION, true);
					}
					
				} catch (CoreException e) {
					reportError(getExceptionMessage(e));
				}
			} else {
				reportError(ActionMessages.getString("Evaluate.error.message.src_context")); //$NON-NLS-1$
			}
		} else {
			reportError(ActionMessages.getString("Evaluate.error.message.eval_adapter")); //$NON-NLS-1$
		}
	}
		
	protected IJavaElement getJavaElement(IStackFrame stackFrame) {
		
		// Get the corresponding element.
		ILaunch launch = stackFrame.getLaunch();
		if (launch == null) {
			return null;
		}
		ISourceLocator locator= launch.getSourceLocator();
		if (locator == null)
			return null;
		
		Object sourceElement = locator.getSourceElement(stackFrame);
		if (sourceElement instanceof IJavaElement) {
			return (IJavaElement) sourceElement;
		}			
		return null;
	}
	
	/**
	 * Updates the enabled state of the action that this is a
	 * delegate for.
	 */
	protected void update() {
		IAction action= getAction();
		if (action != null) {
			boolean enabled = false;
			resolveSelectedObject();
			Object selection = getSelectedObject();
			if (selection != null) {
				if (selection instanceof IStructuredSelection) {
					//valid selection from the tree viewer in the variables view
					//for inspect
					enabled= true;
				} else {
					if (getTargetPart() instanceof JavaSnippetEditor) {
						enabled= true;
					} else {
						enabled = getStackFrameContext() != null;
					}
				}
			}
			action.setEnabled(enabled);
		}
	}
	
	/**
	 * Resolves the selected object in the target part, or <code>null</code>
	 * if there is no selection.
	 */
	protected void resolveSelectedObject() {
		Object selectedObject= null;
		ISelection selection= getTargetSelection();
		if (selection instanceof ITextSelection) {
			String text= ((ITextSelection)selection).getText();
			if (textHasContent(text)) {
				selectedObject= text;
			}
		} else if (selection instanceof IStructuredSelection) {
			if (!selection.isEmpty()) {
				if (getTargetPart().getSite().getId().equals(IDebugUIConstants.ID_DEBUG_VIEW)) {
					//work on the editor selection
					setTargetPart(getTargetPart().getSite().getPage().getActiveEditor());
					selection= getTargetSelection();
					if (selection instanceof ITextSelection) {
						String text= ((ITextSelection)selection).getText();
						if (textHasContent(text)) {
							selectedObject= text;
						}
					}
				} else {
					IStructuredSelection ss= (IStructuredSelection)selection;
					Iterator elements = ss.iterator();
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
		ISourceLocator locator= launch.getSourceLocator();
		if (locator == null) {
			return false;
		}
		Object sourceElement = locator.getSourceElement(stackFrame);
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
		} else {
			return JDIDebugUIPlugin.getActiveWorkbenchWindow().getShell();
		}
	}
	
	protected IDataDisplay getDataDisplay() {
		IDataDisplay display;
		IWorkbenchPart part= getTargetPart();
		if (part != null) {
			display= (IDataDisplay)part.getAdapter(IDataDisplay.class);
			if (display != null) {
				return display;
			}
		}
		IWorkbenchPage page= JDIDebugUIPlugin.getDefault().getActivePage();
		if (page != null) {
			IWorkbenchPart activePart= page.getActivePart();
			if (activePart != null) {
				display= (IDataDisplay)activePart.getAdapter(IDataDisplay.class);
				if (display != null) {
					return display;
				}	
				ITextViewer viewer = (ITextViewer)activePart.getAdapter(ITextViewer.class);
				if (viewer != null) {
					return new DataDisplay(viewer);
				}
			}
			IViewPart view = page.findView(IJavaDebugUIConstants.ID_DISPLAY_VIEW);;
			if (view == null) {
				try {
					view= page.showView(IJavaDebugUIConstants.ID_DISPLAY_VIEW);
				} catch (PartInitException e) {
					JDIDebugUIPlugin.errorDialog(ActionMessages.getString("EvaluateAction.Cannot_open_Display_view"), e); //$NON-NLS-1$
				} finally {
					page.activate(activePart);
				}
			}
			if (view != null) {
				page.bringToTop(view);
				return (IDataDisplay)view.getAdapter(IDataDisplay.class);
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
	
	protected void reportError(String message) {
		Status status= new Status(IStatus.ERROR, JDIDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, message, null);
		ErrorDialog.openError(getShell(), ActionMessages.getString("Evaluate.error.title.eval_problems"), null, status); //$NON-NLS-1$
	}
	
	protected String getExceptionMessage(Throwable exception) {
		if (exception instanceof DebugException) {
			DebugException de = (DebugException)exception;
			Throwable t= de.getStatus().getException();
			if (t != null) {
				return getWrappedExceptionMessage(t);
			}
		}
		
		if (exception instanceof CoreException) {
			CoreException ce= (CoreException) exception;
			return ce.getStatus().getMessage();
		}
		String message= MessageFormat.format(ActionMessages.getString("Evaluate.error.message.direct_exception"), new Object[] { exception.getClass() }); //$NON-NLS-1$
		if (exception.getMessage() != null) {
			message= MessageFormat.format(ActionMessages.getString("Evaluate.error.message.exception.pattern"), new Object[] { message, exception.getMessage() }); //$NON-NLS-1$
		}
		return message;
	}
	
	protected void reportErrors(IEvaluationResult result) {
		String message= getErrorMessage(result);
		if (message.length() != 0) {
			reportError(message);
		}
	}
	
	protected String getErrorMessage(IEvaluationResult result) {
		Message[] errors= result.getErrors();
		if (errors.length == 0) {
			return getExceptionMessage(result.getException());
		} else {
			return getErrorMessage(errors);
		}
	}
	
	protected String getErrorMessage(Message[] errors) {
		String message= ""; //$NON-NLS-1$
		for (int i= 0; i < errors.length; i++) {
			Message error= errors[i];
			//more than a warning
			String msg= error.getMessage();
			if (i == 0) {
				message= msg;
			} else {
				message= MessageFormat.format(ActionMessages.getString("Evaluate.error.problem_append_pattern"), new Object[] { message, msg }); //$NON-NLS-1$
			}
		}
		return message;
	}
	
	protected String getWrappedExceptionMessage(Throwable exception) {
		if (exception instanceof com.sun.jdi.InvocationException) {
			InvocationException ie= (InvocationException) exception;
			ObjectReference ref= ie.exception();
			return MessageFormat.format(ActionMessages.getString("Evaluate.error.message.wrapped_exception"), new Object[] { ref.referenceType().name() }); //$NON-NLS-1$
		} else
			return getExceptionMessage(exception);
	}
	
	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		run();
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		setAction(action);
		update();
	}	

	/**
	 * @see IWorkbenchWindowActionDelegate#dispose()
	 */
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
	 * if neccesary).
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
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		setAction(action);
		setTargetPart(targetEditor);
		update();
	}

	/**
	 * @see IPartListener#partActivated(IWorkbenchPart)
	 */
	public void partActivated(IWorkbenchPart part) {
		setTargetPart(part);
	}

	/**
	 * @see IPartListener#partBroughtToTop(IWorkbenchPart)
	 */
	public void partBroughtToTop(IWorkbenchPart part) {
	}

	/**
	 * @see IPartListener#partClosed(IWorkbenchPart)
	 */
	public void partClosed(IWorkbenchPart part) {
		if (part == getTargetPart()) {
			setTargetPart(null);
		}
	}

	/**
	 * @see IPartListener#partDeactivated(IWorkbenchPart)
	 */
	public void partDeactivated(IWorkbenchPart part) {
	}

	/**
	 * @see IPartListener#partOpened(IWorkbenchPart)
	 */
	public void partOpened(IWorkbenchPart part) {
	}
	
	/**
	 * @see IViewActionDelegate#init(IViewPart)
	 */
	public void init(IViewPart view) {
		setTargetPart(view);
	}

	protected IWorkbenchPart getTargetPart() {
		return fTargetPart;
	}

	protected void setTargetPart(IWorkbenchPart part) {
		if (fTargetPart instanceof JavaSnippetEditor) {
			((JavaSnippetEditor)fTargetPart).removeSnippetStateChangedListener(this);
		}
		fTargetPart = part;
		if (part instanceof JavaSnippetEditor) {
			((JavaSnippetEditor)part).addSnippetStateChangedListener(this);
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
	public void snippetStateChanged(JavaSnippetEditor editor) {
		if (editor != null && !editor.isEvaluating()) {
			update();
		} else {
			getAction().setEnabled(false);
		}
	}
}
