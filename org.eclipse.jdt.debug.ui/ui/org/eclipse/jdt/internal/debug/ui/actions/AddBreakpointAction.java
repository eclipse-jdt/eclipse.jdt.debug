package org.eclipse.jdt.internal.debug.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.TextEditorAction;

/**
 * Action for setting breakpoints for a given text selection.
 */
public class AddBreakpointAction extends TextEditorAction implements IEditorActionDelegate {
	
	private int fLineNumber;
	private IType fType= null;
	
	public AddBreakpointAction() {
		super(ActionMessages.getResourceBundle(), "AddBreakpoint.", null); //$NON-NLS-1$
	}
	
	/**
	 * @see Action#run()
	 */
	public void run() {
		if (getTextEditor() != null) 
			createBreakpoint(getTextEditor().getEditorInput());
	}
	/**
	 * Creates a breakpoint marker.
	 */
	protected IBreakpoint createBreakpoint(IEditorInput editorInput) {
		if (breakpointCanBeCreated(editorInput)) {
			try {
				return JDIDebugModel.createLineBreakpoint(getType(), getLineNumber(), -1, -1, 0);
			} catch (CoreException ce) {
			}
		}
		return null;
	}
	
	/**
	 * Creates a breakpoint marker.
	 */
	protected boolean breakpointCanBeCreated(IEditorInput editorInput) {
		IType type= getType(editorInput);
		setType(type);
		if (type != null) {
			try {
				return !JDIDebugModel.lineBreakpointExists(type, getLineNumber());
			} catch (CoreException ce) {
				JDIDebugUIPlugin.logError(ce);
			}
		}
		return false;
	}
	
	protected IType getType(IEditorInput editorInput) {
		IType type = null;
		ISelection s= getTextEditor().getSelectionProvider().getSelection();
		if (!s.isEmpty() && s instanceof ITextSelection) {
			ITextSelection selection= (ITextSelection) s;
			try {
				IDocument document= getTextEditor().getDocumentProvider().getDocument(editorInput);
				BreakpointLocationVerifier bv = new BreakpointLocationVerifier();
				int lineNumber = bv.getValidBreakpointLocation(document, selection.getStartLine());
				if (lineNumber > 0) {
					setLineNumber(lineNumber);
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
				}
			} catch (JavaModelException jme) {
			}
		}
		return type;
	}
	/**
	 * @see IEditorActionDelegate#setActiveEditor(IAction, IEditorPart)
	 */
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		if (targetEditor instanceof ITextEditor) {
			setEditor((ITextEditor)targetEditor);
		}
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
	}
		
	public void update() {
		setEnabled(getTextEditor()!= null && breakpointCanBeCreated(getTextEditor().getEditorInput()));
	}
	protected int getLineNumber() {
		return fLineNumber;
	}

	protected void setLineNumber(int lineNumber) {
		fLineNumber = lineNumber;
	}

	protected IType getType() {
		return fType;
	}

	protected void setType(IType type) {
		fType = type;
	}
}