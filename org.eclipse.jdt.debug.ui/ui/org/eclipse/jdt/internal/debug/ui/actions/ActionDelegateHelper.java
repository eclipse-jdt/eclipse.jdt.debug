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


import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.snippeteditor.JavaSnippetEditor;
import org.eclipse.jdt.ui.IWorkingCopyManager;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.texteditor.ITextEditor;

public class ActionDelegateHelper implements IPartListener, IWindowListener {

	private static ActionDelegateHelper fgDefault;
	private IMember fMember= null;
	private ITextEditor fTextEditor= null;
	private ISelection fCurrentSelection= null;
	private IWorkbenchWindow fCurrentWindow= null;
	
	public static ActionDelegateHelper getDefault() {
		if (fgDefault == null) {
			fgDefault= new ActionDelegateHelper();
		} 
		return fgDefault;
	}
	
	private ActionDelegateHelper() {
		fCurrentWindow= JDIDebugUIPlugin.getActiveWorkbenchWindow();
		if (fCurrentWindow != null) {
			fCurrentWindow.getWorkbench().addWindowListener(this);
			fCurrentWindow.getPartService().addPartListener(this);
			IWorkbenchPage page= fCurrentWindow.getActivePage();
			if (page != null) {
				IEditorPart part= page.getActiveEditor();
				checkToSetTextEditor(part);
			}
		}
	}
	
	/**
	 * @see IPartListener#partActivated(IWorkbenchPart)
	 */
	public void partActivated(IWorkbenchPart part) {
		checkToSetTextEditor(part);
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
			cleanup();
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

	protected IMember getMember() {
		return fMember;
	}

	protected void setMember(IMember member) {
		fMember = member;
	}
	
	protected void checkToSetTextEditor(IWorkbenchPart part) {
		if (part instanceof ITextEditor) {
			if (part instanceof JavaSnippetEditor) {
				cleanup();
			} else {
				setTextEditor((ITextEditor)part);
			}	
		} 
	}
	
	public IMember getCurrentMember(ITextSelection currentSelection) {
		if (currentSelection == getCurrentSelection()) {
			return getMember();
		}
		setCurrentSelection(currentSelection);
		ITextEditor editor= getTextEditor();
		if (editor == null) {
			return null;
		}
		IEditorInput editorInput= editor.getEditorInput();
		IMember m= null;
		try {
			IClassFile classFile= (IClassFile)editorInput.getAdapter(IClassFile.class);
			if (classFile != null) {
				IJavaElement e= classFile.getElementAt(currentSelection.getOffset());
				if (e instanceof IMember) {
					m= (IMember)e;
				}
			} else {
				IWorkingCopyManager manager= JavaUI.getWorkingCopyManager();
				ICompilationUnit unit= manager.getWorkingCopy(editorInput);
				if (unit != null) {
					synchronized (unit) {
						unit.reconcile(ICompilationUnit.NO_AST /*don't create ast*/, false/*don't force problem detection*/, null/*use primary owner*/, null/*no progress monitor*/);
					}
					IJavaElement e = unit.getElementAt(currentSelection.getOffset());
					if (e instanceof IMember) {
						m= (IMember)e;
					}
				}
			}

		} catch (JavaModelException jme) {
			JDIDebugUIPlugin.log(jme);
		}
		
		setMember(m);
		return m;
	}
	
	protected ITextEditor getTextEditor() {
		return fTextEditor;
	}

	protected void setTextEditor(ITextEditor textEditor) {
		fTextEditor = textEditor;
	}
	
	protected ISelection getCurrentSelection() {
		return fCurrentSelection;
	}

	protected void setCurrentSelection(ISelection currentSelection) {
		fCurrentSelection = currentSelection;
	}
	
	/**
	 * @see IWindowListener#windowActivated(IWorkbenchWindow)
	 */
	public void windowActivated(IWorkbenchWindow window) {
		if (fCurrentWindow != null) {
			fCurrentWindow.getPartService().removePartListener(this);
			cleanup();
		}
		fCurrentWindow= window;
		fCurrentWindow.getPartService().addPartListener(this);
		IWorkbenchPage page= window.getActivePage();
		if (page != null) {
			checkToSetTextEditor(page.getActiveEditor());
		}
	}

	/**
	 * @see IWindowListener#windowClosed(IWorkbenchWindow)
	 */
	public void windowClosed(IWorkbenchWindow window) {
		if (fCurrentWindow == window) {
			fCurrentWindow= null;
			cleanup();
		}
	}

	/**
	 * @see IWindowListener#windowDeactivated(IWorkbenchWindow)
	 */
	public void windowDeactivated(IWorkbenchWindow window) {
	}

	/**
	 * @see IWindowListener#windowOpened(IWorkbenchWindow)
	 */
	public void windowOpened(IWorkbenchWindow window) {
	}
	
	protected void cleanup() {
		setTextEditor(null);
		setCurrentSelection(null);
		setMember(null);
	}
}
