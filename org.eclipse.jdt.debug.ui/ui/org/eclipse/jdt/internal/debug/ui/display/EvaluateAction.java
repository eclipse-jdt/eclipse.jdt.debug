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
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.debug.eval.EvaluationManager;
import org.eclipse.jdt.debug.eval.IEvaluationEngine;
import org.eclipse.jdt.debug.eval.IEvaluationListener;
import org.eclipse.jdt.debug.eval.IEvaluationResult;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.snippeteditor.JavaSnippetEditor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.texteditor.IUpdate;

import com.sun.jdi.InvocationException;
import com.sun.jdi.ObjectReference;


/**
 * Action to do simple code evaluation. The evaluation
 * is done in the UI thread and the expression and result are
 * displayed using the IDataDisplay.
 */
public abstract class EvaluateAction extends Action implements IUpdate, IEvaluationListener, IEditorActionDelegate, IWorkbenchWindowActionDelegate, IPartListener {
		
	private IWorkbenchPart fWorkbenchPart;
	private String fExpression;
	private IWorkbenchWindow fWorkbenchWindow;
	private IAction fAction;
	
	/**
	 * Used to resolve editor input for selected stack frame
	 */
	private IDebugModelPresentation fPresentation;
	
	/**
	 * Indicates whether this action is used from within an editor.  If so,
	 * then this action is enabled only when the editor's input matches the
	 * editor input corresponding to the currently selected stack frame.
	 * If this flag is false, then this action is enabled whenever there is
	 * a stack frame selected in the UI.
	 */
	private boolean fUsedInEditor;
		
	public EvaluateAction(IWorkbenchPart workbenchPart, boolean usedInEditor) {
		super();
		setWorkbenchPart(workbenchPart);
		setUsedInEditor(usedInEditor);
	}
	
	/**
	 * Finds the currently selected stack frame in the UI
	 */
	protected IStackFrame getContext() {
		IDebugElement context = DebugUITools.getDebugContext();
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
	 * @see Action#run()
	 */
	public void run() {
		
		fExpression= null;
		
		IStackFrame stackFrame= getContext();
		if (stackFrame == null) {
			reportError(DisplayMessages.getString("Evaluate.error.message.stack_frame_context")); //$NON-NLS-1$
			return;
		}
		
		IJavaStackFrame adapter= (IJavaStackFrame) stackFrame.getAdapter(IJavaStackFrame.class);
		if (adapter != null) {
			IJavaElement javaElement= getJavaElement(stackFrame);
			if (javaElement != null) {
				IJavaProject project = javaElement.getJavaProject();
				try {
					
					ITextSelection selection = (ITextSelection) fWorkbenchPart.getSite().getSelectionProvider().getSelection();
					fExpression= selection.getText();
					
					IDataDisplay dataDisplay= getDataDisplay();
					if (dataDisplay != null && displayExpression())
						dataDisplay.displayExpression(fExpression);
					
					IEvaluationEngine engine = getEvauationEngine((IJavaDebugTarget)adapter.getDebugTarget(), project);
					engine.evaluate(fExpression, adapter, this);
					
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
	protected IEvaluationEngine getEvauationEngine(IJavaDebugTarget vm, IJavaProject project) throws CoreException {
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
		boolean enabled = false;
		if (getWorkbenchPart() != null && isValidStackFrame()) {
			ISelectionProvider provider = getWorkbenchPart().getSite().getSelectionProvider();
			if (provider != null)  {
				if (textHasContent(((ITextSelection)provider.getSelection()).getText())) {
					enabled = true;
				}
			}
		}
		setEnabled(enabled);
		updateAction();
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
		
		Object value= getWorkbenchPart().getAdapter(IDataDisplay.class);
		if (value instanceof IDataDisplay)
			return (IDataDisplay) value;
		
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
	
	/**
	 * Returns whether to display the expression via
	 * the data display.
	 */
	protected boolean displayExpression() {
		return true;
	}
	
	protected boolean isUsedInEditor() {
		return fUsedInEditor;
	}
	
	protected void setUsedInEditor(boolean used) {
		fUsedInEditor= used;
	}
	
	/**
	 * @see IEditorActionDelegate#setActiveEditor(IAction, IEditorPart)
	 */
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		setWorkbenchPart(targetEditor);
		setAction(action);
		update();
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
	}

	/**
	 * @see IWorkbenchWindowActionDelegate#init(IWorkbenchWindow)
	 */
	public void init(IWorkbenchWindow window) {
		setWorkbenchWindow(window);
		setWorkbenchPart(window.getActivePage().getActiveEditor());
		window.getPartService().addPartListener(this);
	}
	
	/**
	 * @see IPartListener#partActivated(IWorkbenchPart)
	 */
	public void partActivated(IWorkbenchPart part) {
		if (part instanceof IEditorPart) {
			setWorkbenchPart(part);
			update();
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
		if (part == getWorkbenchPart()) {
			setWorkbenchPart(part);
			update();
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
	
	protected IWorkbenchWindow getWorkbenchWindow() {
		return fWorkbenchWindow;
	}

	protected void setWorkbenchWindow(IWorkbenchWindow workbenchWindow) {
		fWorkbenchWindow = workbenchWindow;
	}
	
	protected IAction getAction() {
		return fAction;
	}

	protected void setAction(IAction action) {
		fAction = action;
	}
	
	protected IWorkbenchPart getWorkbenchPart() {
		return fWorkbenchPart;
	}

	protected void setWorkbenchPart(IWorkbenchPart workbenchPart) {
		fWorkbenchPart = workbenchPart;
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
}
