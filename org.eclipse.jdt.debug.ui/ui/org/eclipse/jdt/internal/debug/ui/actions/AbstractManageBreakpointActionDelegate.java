package org.eclipse.jdt.internal.debug.ui.actions;

/*
 * (c) Copyright IBM Corp. 2002.
 * All Rights Reserved.
 */

import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.ui.IJavaElementSearchConstants;
import org.eclipse.jdt.ui.IWorkingCopyManager;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;


public abstract class AbstractManageBreakpointActionDelegate extends ManageBreakpointActionDelegate implements IObjectActionDelegate {

	protected String fRemoveText;
	protected String fRemoveDescription;
	protected String fAddText;
	protected String fAddDescription;
	private IAction fAction;
	private IMember fMember;
	private IJavaBreakpoint fBreakpoint;
	
	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		setAction(action);
		if (selection == null) {
			return;
		}
		update(selection);
	}

	protected IAction getAction() {
		return fAction;
	}

	protected void setAction(IAction action) {
		fAction = action;
	}

	protected IMember getMember() {
		return fMember;
	}

	protected void setMember(IMember element) {
		fMember = element;
	}
	
	protected abstract IMember getMember(ISelection s);
	
	protected void update(ISelection selection) {
		setMember(getMember(selection));
		update();
	}	
	
	protected abstract IJavaBreakpoint getBreakpoint(IMember element);
	
	protected void update() {
		if(getMember() == null) {
			setBreakpoint(null);
		} else {
			setBreakpoint(getBreakpoint(getMember()));
		}
		boolean doesNotExist= getBreakpoint() == null;
		getAction().setText(doesNotExist ? fAddText : fRemoveText);
		getAction().setDescription(doesNotExist ? fAddDescription : fRemoveDescription);
	}
	
	protected IJavaBreakpoint getBreakpoint() {
		return fBreakpoint;
	}

	protected void setBreakpoint(IJavaBreakpoint breakpoint) {
		fBreakpoint = breakpoint;
	}
	
	protected IMember getMember0(ITextSelection selection, IEditorInput editorInput) {
		setLineNumber(selection.getStartLine() + 1);
		IMember m= null;
		try {
			IClassFile classFile= (IClassFile)editorInput.getAdapter(IClassFile.class);
			if (classFile != null) {
				IJavaElement e= classFile.getElementAt(selection.getOffset());
				if (e instanceof IMember) {
					m= (IMember)e;
					setMember(m);
				}
			} else {
				IWorkingCopyManager manager= JavaUI.getWorkingCopyManager();
				ICompilationUnit unit= manager.getWorkingCopy(editorInput);
				if (unit == null) {
					return null;
				}
				IJavaElement e = unit.getElementAt(selection.getOffset());
				if (e instanceof IMember) {
					m= (IMember)e;
					setMember(m);
				}
			}
		} catch (JavaModelException jme) {
			JDIDebugUIPlugin.log(jme);
		}
		return m;
	}
}
