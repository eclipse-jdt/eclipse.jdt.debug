/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.actions;


import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.texteditor.ITextEditor;


public abstract class AbstractManageBreakpointActionDelegate extends ManageBreakpointActionDelegate implements IObjectActionDelegate {

	private IMember[] fMembers;
	private IWorkbenchPart fTargetPart;
	
	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		setTargetPart(targetPart);
	}

	protected IMember[] getMembers() {
		return fMembers;
	}

	protected void setMembers(IMember[] elements) {
		fMembers = elements;
	}
	
	protected abstract IMember[] getMembers(ISelection s);
	
	protected void updateForRun() {
		IMember[] members;
		IWorkbenchPage page= getPage();
		if (page != null) {
			ISelection selection= page.getSelection();
			if (selection instanceof ITextSelection) {
				members= new IMember[] {ActionDelegateHelper.getDefault().getCurrentMember(selection)};
			} else {
				members= getMembers(selection);
			}
		} else {
			members= new IMember[0];
		}
		
		setMembers(members);
		update();
	}
	
	protected IWorkbenchPage getPage() {
		if (getWorkbenchWindow() != null) {
			return getWorkbenchWindow().getActivePage();
		} else if (getTargetPart() != null) {
			return getTargetPart().getSite().getPage();
		}
		return null;
	}

	protected abstract IJavaBreakpoint getBreakpoint(IMember element);
	
	protected abstract boolean enableForMember(IMember member);
	
	protected IWorkbenchPart getTargetPart() {
		return fTargetPart;
	}

	protected void setTargetPart(IWorkbenchPart targetPart) {
		fTargetPart = targetPart;
		setEnabledState(null);
	}
	
	protected void update(ISelection selection) {
		setEnabledState(null);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.actions.ManageBreakpointActionDelegate#report(java.lang.String)
	 */
	protected void report(String message) {
		IWorkbenchPart part= getTargetPart();
		IStatusLineManager statusLineManager= null;
		if (part instanceof IViewPart) {
			statusLineManager= ((IViewPart)part).getViewSite().getActionBars().getStatusLineManager();
		} else if (part instanceof IEditorPart) {
			statusLineManager= ((IEditorPart)part).getEditorSite().getActionBars().getStatusLineManager();
		}
		if (statusLineManager != null) {
			statusLineManager.setErrorMessage(message);
			if (message != null && JDIDebugUIPlugin.getActiveWorkbenchShell() != null) {
				JDIDebugUIPlugin.getActiveWorkbenchShell().getDisplay().beep();
			}
		} else {
			super.report(message);
		}
	}
	
	protected CompilationUnit parseCompilationUnit() {
		IEditorInput editorInput = getTextEditor().getEditorInput();
		IDocument document= getTextEditor().getDocumentProvider().getDocument(editorInput);
		ASTParser c = ASTParser.newParser(AST.LEVEL_2_0);
		c.setSource(document.get().toCharArray());
		return (CompilationUnit) c.createAST(null);
	}
	
	protected IResource getResource() {
		IResource resource;
		IEditorInput editorInput = getTextEditor().getEditorInput();
		if (editorInput instanceof IFileEditorInput) {
			resource= ((IFileEditorInput)editorInput).getFile();
		} else {
			resource= ResourcesPlugin.getWorkspace().getRoot();
		}
		return resource;
	}
	
	protected ITextSelection getTextSelection() {
		IWorkbenchPage page= getPage();
		if (page != null) {
			ISelection selection= page.getSelection();
			if (selection instanceof ITextSelection) {
				return (ITextSelection) selection;
			}
		}
		return null;
	}

	protected void setEnabledState(ITextEditor editor) {
		if (getAction() != null && getPage() != null) {
			IWorkbenchPart part = getPage().getActivePart();
			if (part == null) {
				getAction().setEnabled(false);
			} else {
				if (part == getPage().getActiveEditor()) {
					if (getPage().getActiveEditor() instanceof ITextEditor) {
						super.setEnabledState((ITextEditor)getPage().getActiveEditor());
					} else {
						getAction().setEnabled(false);
					}
				}
			}
		}	
	}
}
