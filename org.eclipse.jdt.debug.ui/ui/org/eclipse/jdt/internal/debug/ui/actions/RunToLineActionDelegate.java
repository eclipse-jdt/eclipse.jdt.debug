package org.eclipse.jdt.internal.debug.ui.actions;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.internal.debug.ui.BreakpointUtils;
import org.eclipse.jdt.internal.debug.ui.ExceptionHandler;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.snippeteditor.ScrapbookLauncher;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Action to support run to line (i.e. where the cursor is in the active editor)
 */
public class RunToLineActionDelegate extends ManageBreakpointActionDelegate implements IEditorActionDelegate {
	
	public RunToLineActionDelegate() {
	}
	
	/**
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		try {
			IDebugTarget target= getContext();
			if (target == null) {
				if (getTextEditor() != null) {
					getTextEditor().getSite().getShell().getDisplay().beep();
				}
				return;
			}
			
			ITextSelection selection= (ITextSelection)getTextEditor().getSelectionProvider().getSelection();
			setLineNumber(selection.getStartLine() + 1);
			IType type= getType(getTextEditor().getEditorInput());
			if (type == null) {
				return;
			}
	
			IBreakpoint breakpoint= null;
			try {
				Map attributes = new HashMap(4);
				BreakpointUtils.addJavaBreakpointAttributes(attributes, type);
				BreakpointUtils.addRunToLineAttributes(attributes);
				breakpoint= JDIDebugModel.createLineBreakpoint(BreakpointUtils.getBreakpointResource(type), type.getFullyQualifiedName(), getLineNumber(), -1, -1, 1, false, attributes);
			} catch (CoreException ce) {
				ExceptionHandler.handle(ce, ActionMessages.getString("RunToLine.error.title1"), ActionMessages.getString("RunToLine.error.message1")); //$NON-NLS-1$ //$NON-NLS-2$
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
						JDIDebugUIPlugin.log(de);
					}
					break;
				}
			}
		} catch(DebugException de) {
			ExceptionHandler.handle(de, ActionMessages.getString("RunToLine.error.title1"), ActionMessages.getString("RunToLine.error.message1")); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
	/**
	 * Resolves the debug target context to set the run to line
	 */
	protected IDebugTarget getContext() throws DebugException{
		IDebugTarget target= getContextFromUI();
		if (target == null) {
			target= getContextFromModel();
			//target has already been checked for suspended thread
			return target;
		}
		if (target == null) {
			return null;
		}
		if (target.getLaunch().getAttribute(ScrapbookLauncher.SCRAPBOOK_LAUNCH) != null) {
			//can't set run to line in scrapbook context
			return null;
		}
		IThread[] threads= target.getThreads();
		for (int i= 0; i < threads.length; i++) {
			IThread thread= threads[i];
			if (thread.canResume()) {
				return target;
			}
		}
		
		return null;
	}
	/**
	 * Resolves a debug target context from the model
	 */
	protected IDebugTarget getContextFromModel() throws DebugException {
		IDebugTarget[] dts= DebugPlugin.getDefault().getLaunchManager().getDebugTargets();
		for (int i= 0; i < dts.length; i++) {
			IDebugTarget dt= dts[i];
			if (getContextFromDebugTarget(dt) != null) {
				return dt;
			}
		}
		return null;
	}
	/**
	 * Resolves a debug target context from the model
	 */
	protected IDebugTarget getContextFromThread(IThread thread) throws DebugException {
		if (thread.isSuspended()) {
			return thread.getDebugTarget();
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
	
	/**
	 * Updates the enabled state of this action and the plugin action
	 * this action is the delegate for.
	 */
	protected void update() {
		setEnabledState(getTextEditor());
	}
	
	/**
	 * Resolves a stack frame context from the model.
	 */
	protected IDebugTarget getContextFromDebugTarget(IDebugTarget dt) throws DebugException {
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
	 * @see IEditorActionDelegate#setActiveEditor(IAction, IEditorPart)
	 */
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		setAction(action);
		if (targetEditor instanceof ITextEditor) {
			setTextEditor((ITextEditor)targetEditor);
		}
		if (!action.isEnabled()) {
			//the xml specified enabler has set the action to be disabled
			return;
		}
		setEnabledState(getTextEditor());
	}
}