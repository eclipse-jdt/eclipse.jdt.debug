package org.eclipse.jdt.internal.debug.ui.display;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.File;
import java.text.MessageFormat;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IDebugViewAdapter;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.debug.eval.EvaluationManager;
import org.eclipse.jdt.debug.eval.IEvaluationEngine;
import org.eclipse.jdt.debug.eval.IEvaluationListener;
import org.eclipse.jdt.debug.eval.IEvaluationResult;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.snippeteditor.JavaSnippetEditor;
import org.eclipse.jdt.internal.debug.ui.snippeteditor.SnippetAction;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.texteditor.IUpdate;

import com.sun.jdi.InvocationException;
import com.sun.jdi.ObjectReference;


/**
 * Action to do simple code evaluation. The evaluation
 * is done in the UI thread and the expression and result are
 * displayed using the IDataDisplay.
 * 
 * [Issue: this class is a part listener because the workbench
 * does not fire selection change events to action delegates when
 * the selection is a text selection. Thus we have to listen to parts
 * and do manual updating].
 */
public abstract class EvaluateAction extends Action implements IUpdate, IEvaluationListener, IWorkbenchWindowActionDelegate, IEditorActionDelegate, IPartListener, ISelectionChangedListener {
		
	private String fExpression;
	private IAction fAction;
	
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
	 * @return java object or <code>null</code>
	 */
	protected IJavaObject getObjectContext() {
		IWorkbenchPage page= JDIDebugUIPlugin.getDefault().getActivePage();
		if (page != null) {
			IWorkbenchPart activePart= page.getActivePart();
			if (activePart != null) {
				IDebugViewAdapter a = (IDebugViewAdapter)activePart.getAdapter(IDebugViewAdapter.class);
				if (a != null) {
					if (a.getViewer() != null) {
						ISelection s = a.getViewer().getSelection();
						if (s instanceof IStructuredSelection) {
							IStructuredSelection ss = (IStructuredSelection)s;
							if (ss.size() == 1) {
								if (ss.getFirstElement() instanceof IJavaVariable) {
									IJavaVariable var = (IJavaVariable)ss.getFirstElement();
									// if 'this' is selected, use stack frame context
									try {
										if (!var.getName().equals("this")) { //$NON-NLS-1$
											if (var.getValue() instanceof IJavaObject) {
												return (IJavaObject)var.getValue();
											}
										} 
									} catch (DebugException e) {
										JDIDebugUIPlugin.log(e);
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
	 * Finds the currently selected stack frame in the UI
	 */
	protected IStackFrame getContext() {
		IAdaptable context = DebugUITools.getDebugContext();
		if (context != null) {
			if (context instanceof IStackFrame) {
				return (IStackFrame)context;
			}
			if (context instanceof IThread) {
				try {
					return ((IThread)context).getTopStackFrame();
				} catch (DebugException e) {
					JDIDebugUIPlugin.log(e);
				}
			}
		}
		return null;
	}
	
	/**
	 * Hook to allow snippet editor to use same global
	 * actions
	 */
	protected Class getAdapterClass() {
		return null;
	}
	
	protected IAction getSnippetDelegate() {
		Class delegate = getAdapterClass();
		if (delegate != null) {
			IWorkbenchPart part = getWorkbenchPart();
			if (part != null) {
				return (IAction)part.getAdapter(delegate);
			}
		}	
		return null;	
	}
	
	/**
	 * @see Action#run()
	 */
	public void run() {
		
		IAction delegate = getSnippetDelegate();
		if (delegate != null) {
			delegate.run();
			return;
		}
		
		fExpression= null;
		
		// eval in context of object or stack frame
		IJavaObject object = getObjectContext();		
		IStackFrame stackFrame= getContext();
		if (stackFrame == null) {
			reportError(DisplayMessages.getString("Evaluate.error.message.stack_frame_context")); //$NON-NLS-1$
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
				try {
					fExpression= getExpressionText();
					
					IDataDisplay dataDisplay= getDataDisplay();
					if (dataDisplay != null) {
						dataDisplay.displayExpression(fExpression);
					}
					
					IEvaluationEngine engine = getEvaluationEngine((IJavaDebugTarget)jFrame.getDebugTarget(), project);
					if (object == null) {
						engine.evaluate(fExpression, jFrame, this);
					} else {
						engine.evaluate(fExpression, object, (IJavaThread)jFrame.getThread(), this);
					}
					
				} catch (CoreException e) {
					reportError(e);
				}
			} else {
				reportError(DisplayMessages.getString("Evaluate.error.message.src_context")); //$NON-NLS-1$
			}
		} else {
			reportError(DisplayMessages.getString("Evaluate.error.message.eval_adapter")); //$NON-NLS-1$
		}
	}
	
	/**
	 * Returns an evaluation engine for the given debug target
	 * and Java project.
	 * 
	 * @param vm debug target on which the evaluation will be
	 *  performed
	 * @param project the context in which the evaluation will be
	 *  compiled
	 * @exception CoreException if creation of a new evaluation
	 *  engine is required and fails 
	 */
	protected IEvaluationEngine getEvaluationEngine(IJavaDebugTarget vm, IJavaProject project) throws CoreException {
		IEvaluationEngine engine = EvaluationManager.getEvaluationEngine(vm);
		if (engine == null) {
			IPath outputLocation = project.getOutputLocation();
			IWorkspace workspace = project.getProject().getWorkspace();
			IResource res = workspace.getRoot().findMember(outputLocation);
			File dir = new File(res.getLocation().toOSString());
			engine= EvaluationManager.newClassFileEvaluationEngine(project, vm, dir);
		}	
		return engine;
			
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
	 * Updates the enabled state of this action and the action that this is a
	 * delegate for.
	 */
	public void update() {
		
		IAction delegate = getSnippetDelegate();
		boolean enabled = false;
		if (delegate != null) {
			((SnippetAction)delegate).update();
			enabled = delegate.isEnabled();
		} else {			
			String expression = getExpressionText();
			if (expression != null && textHasContent(expression)) {
				enabled = getContext() != null;
			}
		}
		setEnabled(enabled);
		updateAction();
	}
	
	/**
	 * Returns the selected text in the active view, or <code>null</code>
	 * if there is no text selection.
	 * 
	 * @return the selected text in the active view, or <code>null</code>
	 *  if there is no text selection
	 */
	protected String getExpressionText() {
		IWorkbenchPart part = getWorkbenchPart();
		if (part != null) {
			ISelectionProvider provider = part.getSite().getSelectionProvider();
			if (provider != null) {
				ISelection sel = provider.getSelection();
				if (sel instanceof ITextSelection) {
					return ((ITextSelection)sel).getText();
				}
			}
		}
		return null;
	}
	
	protected void updateAction() {
		IAction action= getAction();
		if (action != null) {
			action.setEnabled(isEnabled());
		}
	}
	/**
	 * Returns true if the current stack frame context can be used for an
	 * evaluation, false otherwise.  For a Snippet editor, always returns true.
	 */
	protected boolean isValidStackFrame() {
		if (getWorkbenchPart() instanceof JavaSnippetEditor) {
			return true;
		}
		IStackFrame stackFrame = getContext();
		if (stackFrame == null) {
			return false;
		}
		if (isUsedInEditor()) {
			return compareToEditorInput(stackFrame);
		} else {
			return true;
		}
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
		if (getWorkbenchPart() instanceof IEditorPart) {
			return ((IEditorPart)getWorkbenchPart()).getEditorInput().equals(sfEditorInput);
		}
		return false;
	}
	
	protected Shell getShell() {
		return getWorkbenchPart().getSite().getShell();
	}
	
	protected IDataDisplay getDataDisplay() {
		IWorkbenchPage page= JDIDebugUIPlugin.getDefault().getActivePage();
		if (page != null) {
			IWorkbenchPart activePart= page.getActivePart();
			if (activePart != null) {
				IDataDisplay display= (IDataDisplay)activePart.getAdapter(IDataDisplay.class);
				if (display != null) {
					return display;
				}	
				ITextViewer viewer = (ITextViewer)activePart.getAdapter(ITextViewer.class);
				if (viewer != null) {
					return new DataDisplay(viewer);
				}
			}
			IViewPart view = page.findView(DisplayView.ID_DISPLAY_VIEW);;
			if (view == null) {
				try {
					view= page.showView(DisplayView.ID_DISPLAY_VIEW);
				} catch (PartInitException e) {
					MessageDialog.openError(getShell(), DisplayMessages.getString("EditorDisplayAction.Cannot_open_Display_viewer_1"), e.getMessage()); //$NON-NLS-1$
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
		Status status= new Status(IStatus.ERROR, JDIDebugUIPlugin.getPluginId(), IStatus.ERROR, message, null);
		reportError(status);
	}
	
	protected void reportError(IStatus status) {
		ErrorDialog.openError(getShell(), DisplayMessages.getString("EvaluationAction.Error_evaluating_1"), null, status); //$NON-NLS-1$
	}
	
	protected void reportError(Throwable exception) {
		if (exception instanceof DebugException) {
			DebugException de = (DebugException)exception;
			Throwable t= de.getStatus().getException();
			if (t != null) {
				reportWrappedException(t);
				return;
			}
		}
		
		if (exception instanceof CoreException) {
			CoreException ce= (CoreException) exception;
			reportError(ce.getStatus());
			return;
		}
		
		String message= MessageFormat.format(DisplayMessages.getString("Evaluate.error.message.direct_exception"), new Object[] { exception.getClass() }); //$NON-NLS-1$
		if (exception.getMessage() != null)
			message= MessageFormat.format(DisplayMessages.getString("Evaluate.error.message.exception.pattern"), new Object[] { message, exception.getMessage() }); //$NON-NLS-1$
		reportError(message);
	}
	
	protected boolean reportProblems(IEvaluationResult result) {
		IMarker[] problems= result.getProblems();
		boolean severeProblems= true;
		if (problems.length == 0) {
			reportError(result.getException());
		} else {
			severeProblems= reportProblems(problems);
		}
		return severeProblems;
	}
	
	protected boolean reportProblems(IMarker[] problems) {
		
		String defaultMsg= DisplayMessages.getString("Evaluate.error.message.unqualified_error"); //$NON-NLS-1$
		
		String message= ""; //$NON-NLS-1$
		for (int i= 0; i < problems.length; i++) {
			IMarker problem= problems[i];
			if (problem.getAttribute(IMarker.SEVERITY, -1) == IMarker.SEVERITY_ERROR) {
				//more than a warning
				String msg= problems[i].getAttribute(IMarker.MESSAGE, defaultMsg);
				if (i == 0) {
					message= msg;
				} else {
					message= MessageFormat.format(DisplayMessages.getString("Evaluate.error.problem_append_pattern"), new Object[] { message, msg }); //$NON-NLS-1$
				}
			}
		}
		
		if (message.length() != 0) {
			reportError(message);
			return true;
		}
		return false;
	}
	
	protected void reportWrappedException(Throwable exception) {
		if (exception instanceof com.sun.jdi.InvocationException) {
			InvocationException ie= (InvocationException) exception;
			ObjectReference ref= ie.exception();
			reportError(MessageFormat.format(DisplayMessages.getString("Evaluate.error.message.wrapped_exception"), new Object[] { ref.referenceType().name() })); //$NON-NLS-1$
		} else
			reportError(exception);
	}
	
	protected boolean isUsedInEditor() {
		return getWorkbenchPart() instanceof IEditorPart;
	}
		
	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		setAction(action);
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
		IWorkbenchWindow win = getWorkbenchWindow();
		if (win != null) {
			win.getPartService().removePartListener(this);
		}
	}

	/**
	 * @see IWorkbenchWindowActionDelegate#init(IWorkbenchWindow)
	 */
	public void init(IWorkbenchWindow window) {
		window.getPartService().addPartListener(this);
		update();
	}
	
	protected IWorkbenchWindow getWorkbenchWindow() {
		return JDIDebugUIPlugin.getActiveWorkbenchWindow();
	}
	
	protected IAction getAction() {
		return fAction;
	}

	protected void setAction(IAction action) {
		fAction = action;
	}
	
	/**
	 * Returns the workbench part that the evaluation is
	 * being performed from (i.e. the source of the expression),
	 * or <code>null</code> if none.
	 * 
	 * @return the workbench part that the evaluation is
	 * being performed from (i.e. the source of the expression),
	 * or <code>null</code> if none
	 */
	protected IWorkbenchPart getWorkbenchPart() {
		IWorkbenchWindow window = getWorkbenchWindow();
		if (window != null) {
			IWorkbenchPage page = window.getActivePage();
			if (page != null) {
				return page.getActivePart();
			}
		}
		return null;
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
		update();
	}

	/**
	 * @see IPartListener#partActivated(IWorkbenchPart)
	 */
	public void partActivated(IWorkbenchPart part) {
		ISelectionProvider provider = part.getSite().getSelectionProvider();
		if (provider != null) {
			provider.addSelectionChangedListener(this);
		}
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
	}

	/**
	 * @see IPartListener#partDeactivated(IWorkbenchPart)
	 */
	public void partDeactivated(IWorkbenchPart part) {
		ISelectionProvider provider = part.getSite().getSelectionProvider();
		if (provider != null) {
			provider.removeSelectionChangedListener(this);
		}		
	}

	/**
	 * @see IPartListener#partOpened(IWorkbenchPart)
	 */
	public void partOpened(IWorkbenchPart part) {
	}

	/**
	 * @see ISelectionChangedListener#selectionChanged(SelectionChangedEvent)
	 */
	public void selectionChanged(SelectionChangedEvent event) {
		selectionChanged(getAction(), event.getSelection());
	}
}
