package org.eclipse.jdt.internal.debug.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.internal.ui.javaeditor.BreakpointLocationVerifier;
import org.eclipse.jdt.internal.ui.javaeditor.IClassFileEditorInput;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.texteditor.AddMarkerAction;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Action for setting breakpoints for a given text selection.
 */
public class AddBreakpointAction extends AddMarkerAction implements IEditorActionDelegate{
	
	public AddBreakpointAction() {
		super(ActionMessages.getResourceBundle(), "AddBreakpoint.", null, IBreakpoint.BREAKPOINT_MARKER, false); //$NON-NLS-1$
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
		ISelection s= getTextEditor().getSelectionProvider().getSelection();
		if (!s.isEmpty()) {
			ITextSelection selection= (ITextSelection) s;
			try {
				IDocument document= getTextEditor().getDocumentProvider().getDocument(editorInput);
				BreakpointLocationVerifier bv = new BreakpointLocationVerifier();
				int lineNumber = bv.getValidBreakpointLocation(document, selection.getStartLine());
				if (lineNumber > 0) {
					
					IRegion line= document.getLineInformation(lineNumber - 1);
					
					IType type = null;
					if (editorInput instanceof IClassFileEditorInput) {
						
						IClassFileEditorInput input= (IClassFileEditorInput) editorInput;
						type = input.getClassFile().getType();
					
					} else if (editorInput instanceof IFileEditorInput) {
						
						IFileEditorInput input= (IFileEditorInput) editorInput;
						IJavaElement element= JavaCore.create(input.getFile());
						if (element instanceof ICompilationUnit) {
							ICompilationUnit cu = (ICompilationUnit) element;
							IJavaElement e = cu.getElementAt(line.getOffset());
							if (e instanceof IType)
								type = (IType)e;
							else if (e != null && e instanceof IMember)
								type = ((IMember) e).getDeclaringType();
						}
					}
					if (type != null) {
						if (!JDIDebugModel.lineBreakpointExists(type, lineNumber)) {
							return JDIDebugModel.createLineBreakpoint(type, lineNumber, line.getOffset(), line.getOffset() + line.getLength(), 0);
						}
					}
					
				}
			} catch (DebugException e) {
				Shell shell= getTextEditor().getSite().getShell();
				ErrorDialog.openError(shell, ActionMessages.getString("AddBreakpoint.error.title1"), ActionMessages.getString("AddBreakpoint.error.message1"), e.getStatus()); //$NON-NLS-2$ //$NON-NLS-1$
			} catch (CoreException e) {
				Shell shell= getTextEditor().getSite().getShell();
				ErrorDialog.openError(shell, ActionMessages.getString("AddBreakpoint.error.title1"), ActionMessages.getString("AddBreakpoint.error.message1"), e.getStatus()); //$NON-NLS-2$ //$NON-NLS-1$
			} catch (BadLocationException e) {
				Shell shell= getTextEditor().getSite().getShell();
				ErrorDialog.openError(shell, ActionMessages.getString("AddBreakpoint.error.title1"), ActionMessages.getString("AddBreakpoint.error.message1"), null); //$NON-NLS-2$ //$NON-NLS-1$
			}

		}
		return null;
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
}