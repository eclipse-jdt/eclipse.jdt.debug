package org.eclipse.jdt.internal.debug.ui.actions;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html

Contributors:
    IBM Corporation - Initial implementation
**********************************************************************/

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.IUpdate;

public class EnableDisableBreakpointRulerAction extends Action implements IUpdate {
	
	private IVerticalRulerInfo fInfo;
	private ITextEditor fTextEditor;
	private IBreakpoint fBreakpoint;
	/**
	 * Creates the action to enable/disable breakpoints
	 */
	public EnableDisableBreakpointRulerAction(ITextEditor editor, IVerticalRulerInfo info) {
		fInfo= info;
		fTextEditor= editor;
		setText("&Enable Breakpoint");
	}

	/**
	 * @see Action#run()
	 */
	public void run() {
		if (fBreakpoint != null) {
			try {
				fBreakpoint.setEnabled(!fBreakpoint.isEnabled());
			} catch (CoreException e) {
				ErrorDialog.openError(JDIDebugUIPlugin.getActiveWorkbenchWindow().getShell(), "Enabling/disabling breakpoints", "Exceptions occurred enabling disabling the breakpoint.", e.getStatus());
			}
		}
	}

	protected IBreakpoint getBreakpoint() {
		IBreakpointManager breakpointManager= DebugPlugin.getDefault().getBreakpointManager();
		IBreakpoint[] breakpoints= breakpointManager.getBreakpoints();
		for (int i= 0; i < breakpoints.length; i++) {
			IBreakpoint breakpoint= breakpoints[i];
			if (breakpoint instanceof IJavaLineBreakpoint) {
				IJavaLineBreakpoint jBreakpoint= (IJavaLineBreakpoint)breakpoint;
				boolean match= false;
				try {
					match= breakpointAtRulerLine(jBreakpoint.getLineNumber());
				} catch (CoreException ce) {
					JDIDebugUIPlugin.log(ce);
					continue;
				}
				if (match) {
					IResource breakpointResource= jBreakpoint.getMarker().getResource();
					IResource editorResource= getResource();
					if (breakpointResource.equals(editorResource)) {
						return breakpoint;
					}
				}
			}
		}
		return null;
	
	}
	
	protected boolean breakpointAtRulerLine(int breakpointLineNumber) {
		int line= getInfo().getLineOfLastMouseButtonActivity();
		return (line + 1) == breakpointLineNumber;
	}
	
	/*protected IType getType(IEditorInput editorInput) {
		IType type = null;
		ISelection s= getTextEditor().getSelectionProvider().getSelection();
		if (s instanceof ITextSelection) {
			ITextSelection selection= (ITextSelection) s;
	
			try {
				IClassFile classFile= (IClassFile)editorInput.getAdapter(IClassFile.class);
				if (classFile != null) {
					type = classFile.getType();
				} else {
					IFile file= (IFile)editorInput.getAdapter(IFile.class);
					if (file != null) {
						IJavaElement element= JavaCore.create(file);
						if (element instanceof ICompilationUnit) {
							ICompilationUnit cu = (ICompilationUnit) element;
							IJavaElement e = cu.getElementAt(selection.getOffset());
							if (e instanceof IType)
								type = (IType)e;
							else if (e != null && e instanceof IMember) {
								type = ((IMember) e).getDeclaringType();
							}
						}
					}
				}
			} catch (JavaModelException jme) {
			}
		}
		return type;
	}*/
	
	protected IVerticalRulerInfo getInfo() {
		return fInfo;
	}

	protected void setInfo(IVerticalRulerInfo info) {
		fInfo = info;
	}

	protected ITextEditor getTextEditor() {
		return fTextEditor;
	}

	protected void setTextEditor(ITextEditor textEditor) {
		fTextEditor = textEditor;
	}
	
	/** 
	 * Returns the resource for which to create the marker, 
	 * or <code>null</code> if there is no applicable resource.
	 *
	 * @return the resource for which to create the marker or <code>null</code>
	 */
	protected IResource getResource() {
		IEditorInput input= fTextEditor.getEditorInput();
		IResource resource= (IResource) input.getAdapter(IFile.class);
		if (resource == null) {
			resource= (IResource) input.getAdapter(IResource.class);
		}	
		return resource;
	}
	/**
	 * @see IUpdate#update()
	 */
	public void update() {
		fBreakpoint= getBreakpoint();
		if (fBreakpoint == null) {
			setEnabled(false);
			return;
		}
		setEnabled(true);
		try {
			boolean enabled= fBreakpoint.isEnabled();
			setText(enabled ? "&Disable Breakpoint" : "&Enable Breakpoint");
		} catch (CoreException ce) {
			JDIDebugUIPlugin.log(ce);
		}
	}
}
