package org.eclipse.jdt.internal.debug.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.internal.debug.core.JDIDebugTarget;
import org.eclipse.jdt.internal.debug.ui.IHelpContextIds;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Action to support run to line (i.e. where the cursor is in the active editor)
 */
public class RunToLineAction extends AddBreakpointAction implements IWorkbenchWindowActionDelegate, IPartListener {
	
	private IWorkbenchWindow fWorkbenchWindow= null;
	public RunToLineAction() {
		setText(ActionMessages.getString("RunToLine.label")); //$NON-NLS-1$
		setToolTipText(ActionMessages.getString("RunToLine.tooltip")); //$NON-NLS-1$
		setDescription(ActionMessages.getString("RunToLine.description")); //$NON-NLS-1$
		update();
		setHelpContextId(IHelpContextIds.RUN_TO_LINE_ACTION );					
	}
	
	public void run() {
		try {
			IDebugTarget target= getContext();
			if (target == null) {
				getTextEditor().getSite().getShell().getDisplay().beep();
				return;
			}
			
			IType type= getType(getTextEditor().getEditorInput());
			if (type == null) {
				return;
			}
	
			IBreakpoint breakpoint= null;
			try {
				breakpoint= JDIDebugModel.createRunToLineBreakpoint(type, getLineNumber(), -1, -1);
			} catch (DebugException de) {
				errorDialog(de.getStatus());
				return;
			} 
			target.breakpointAdded(breakpoint);
			IThread[] threads= target.getThreads(); 
			for (int i= 0; i < threads.length; i++) {
				IThread thread= threads[i];
				if (thread.canResume()) {
					try {
						thread.resume();
					} catch (DebugException de) {
					}
					break;
				}
			}
		} catch(DebugException de) {
			JDIDebugUIPlugin.logError(de);
		}
	}
	/**
	 * Resolves the debug target context to set the run to line
	 */
	protected IDebugTarget getContext() throws DebugException{
		IDebugTarget target= getContextFromUI();
		if (target == null) {
			target= getContextFromModel();
		}
		if (target == null) {
			return null;
		}
		IThread[] threads= target.getThreads();
		boolean threadSuspended= false;
		for (int i= 0; i < threads.length; i++) {
			IThread thread= threads[i];
			if (thread.canResume()) {
				threadSuspended=true;
				break;
			}
		}
		if (threadSuspended) {
			return target;
		}
		return null;
	}
	/**
	 * Resolves a debug target context from the model
	 */
	protected JDIDebugTarget getContextFromModel() throws DebugException {
		IDebugTarget[] dts= DebugPlugin.getDefault().getLaunchManager().getDebugTargets();
		for (int i= 0; i < dts.length; i++) {
			JDIDebugTarget dt= (JDIDebugTarget)dts[i];
			if (getContextFromDebugTarget(dt) != null) {
				return dt;
			}
		}
		return null;
	}
	/**
	 * Resolves a debug target context from the model
	 */
	protected JDIDebugTarget getContextFromThread(IThread thread) throws DebugException {
		if (thread.isSuspended()) {
			return (JDIDebugTarget) thread.getDebugTarget();
		}
		return null;
	}
	/**
	 * Resolves a stack frame context from the UI
	 */
	protected IDebugTarget getContextFromUI() throws DebugException {
		IAdaptable de= DebugUITools.getDebugContext();
		if (de != null) {
			if (de instanceof IThread) {
				return getContextFromThread((IThread) de);
			} else if (de instanceof IDebugElement) {
				return ((IDebugElement)de).getDebugTarget();
			}
		}
		
		return null;
	}
	protected void errorDialog(IStatus status) {
		Shell shell= getTextEditor().getSite().getShell();
		ErrorDialog.openError(shell, ActionMessages.getString("RunToLine.error.title1"), ActionMessages.getString("RunToLine.error.message1"), status); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/**
	 * Updates the enabled state of this action and the plugin action
	 * this action is the delegate for.
	 */
	public void update() {
		try {
			setEnabled(getContext() != null && getTextEditor() != null);
		} catch (DebugException de) {
			setEnabled(false);
			JDIDebugUIPlugin.logError(de);
		}
		updateAction();
	}
	
	public void updateAction() {
		IAction action= getPluginAction();
		if (action != null) {
			action.setEnabled(isEnabled());
		}
	}
	
	/**
	 * Resolves a stack frame context from the model
	 */
	protected JDIDebugTarget getContextFromDebugTarget(JDIDebugTarget dt) throws DebugException {
		if (dt.isTerminated() || dt.isDisconnected()) {
			return null;
		}
		IThread[] threads= dt.getThreads();
		for (int i= 0; i < threads.length; i++) {
			IThread thread= threads[i];
			if (thread.isSuspended()) {
				return dt;
			}
		}
		return null;
	}
	
	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		setPluginAction(action);
		update();
	}
	
	/**
	 * @see IWorkbenchWindowActionDelegate#dispose()
	 */
	public void dispose() {
		getWorkbenchWindow().getPartService().removePartListener(this);
	}
	/**
	 * @see IWorkbenchWindowActionDelegate#init(IWorkbenchWindow)
	 */
	public void init(IWorkbenchWindow window) {
		setWorkbenchWindow(window);
		IEditorPart part= window.getActivePage().getActiveEditor();
		if (part instanceof ITextEditor) {
			setEditor((ITextEditor)part);
		}
		window.getPartService().addPartListener(this);
	}
	
	
	protected IWorkbenchWindow getWorkbenchWindow() {
		return fWorkbenchWindow;
	}
	protected void setWorkbenchWindow(IWorkbenchWindow workbenchWindow) {
		fWorkbenchWindow = workbenchWindow;
	}
	
	/**
	 * @see IEditorActionDelegate#setActiveEditor(IAction, IEditorPart)
	 */
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		if (targetEditor instanceof ITextEditor) {
			setEditor((ITextEditor)targetEditor);
		}
		setPluginAction(action);
		update();
	}
	
	/**
	 * @see IPartListener#partActivated(IWorkbenchPart)
	 */
	public void partActivated(IWorkbenchPart part) {
		if (part instanceof ITextEditor) {
			setEditor((ITextEditor)part);
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
		if (part == getTextEditor()) {
			setEditor(null);
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
}