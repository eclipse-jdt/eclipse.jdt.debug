package org.eclipse.jdt.internal.debug.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.HashMap;
import java.util.Map;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.IBreakpointListener;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.internal.debug.ui.BreakpointUtils;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.TextEditorAction;

/**
 * Action for setting breakpoints for a given text selection.
 */
public class AddBreakpointAction extends TextEditorAction implements IEditorActionDelegate, IBreakpointListener {
	
	private IAction fAction= null;
	private int fLineNumber;
	private IType fType= null;
	
	public AddBreakpointAction() {
		super(ActionMessages.getResourceBundle(), "AddBreakpoint.", null); //$NON-NLS-1$
	}
	
	/**
	 * @see Action#run()
	 */
	public void run() {
		if (getTextEditor() != null) {
			createBreakpoint(getTextEditor().getEditorInput());
		}
		update();
	}
	/**
	 * Creates a breakpoint.
	 */
	protected IBreakpoint createBreakpoint(IEditorInput editorInput) {
		if (breakpointCanBeCreated(editorInput)) {
			try {
				Map attributes = new HashMap(10);
				BreakpointUtils.addJavaBreakpointAttributes(attributes, getType());
				IJavaLineBreakpoint bp = JDIDebugModel.createLineBreakpoint(BreakpointUtils.getBreakpointResource(getType()), getType().getFullyQualifiedName(), getLineNumber(), -1, -1, 0, true, attributes);
				return bp;
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
				return !JDIDebugModel.lineBreakpointExists(type.getFullyQualifiedName(), getLineNumber());
			} catch (CoreException ce) {
				JDIDebugUIPlugin.logError(ce);
			}
		}
		return false;
	}
	
	protected IType getType(IEditorInput editorInput) {
		IType type = null;
		ISelectionProvider sp= getTextEditor().getSelectionProvider();
		if (sp != null) {
			ISelection s= sp.getSelection();
			if (!s.isEmpty() && s instanceof ITextSelection) {
				ITextSelection selection= (ITextSelection) s;
				IDocument document= getTextEditor().getDocumentProvider().getDocument(editorInput);
				BreakpointLocationVerifier bv = new BreakpointLocationVerifier();
				int lineNumber = bv.getValidBreakpointLocation(document, selection.getStartLine());
				if (lineNumber > 0) {
					setLineNumber(lineNumber);
					type= getType0(selection, editorInput);
				}
			}
		}
		return type;
	}
	
	protected IType getType0(ITextSelection selection, IEditorInput editorInput) {
		IType type= null;
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
			JDIDebugUIPlugin.logError(jme);
		}
		return type;
	}
	/**
	 * @see IEditorActionDelegate#setActiveEditor(IAction, IEditorPart)
	 */
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		setPluginAction(action);
		if (targetEditor instanceof ITextEditor) {
			setEditor((ITextEditor)targetEditor);
			//see Bug 7012
			//DebugPlugin.getDefault().getBreakpointManager().addBreakpointListener(this);
		} else {
			//DebugPlugin.getDefault().getBreakpointManager().removeBreakpointListener(this);
		}
		update();
	}
	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		setPluginAction(action);
		run();
	}
	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		setPluginAction(action);
		update();
	}
	
	public void updatePluginAction() {
		IAction action= getPluginAction();
		if (action != null) {
			action.setEnabled(isEnabled());
		}
	}
		
	public void update() {
		setEnabled(getTextEditor()!= null); //@see bug 7012 && breakpointCanBeCreated(getTextEditor().getEditorInput()));
		updatePluginAction();
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
	
	protected IAction getPluginAction() {
		return fAction;
	}
	protected void setPluginAction(IAction action) {
		fAction = action;
	}
	/**
	 * @see IBreakpointListener#breakpointAdded(IBreakpoint)
	 */
	public void breakpointAdded(IBreakpoint breakpoint) {
		update();
	}
	/**
	 * @see IBreakpointListener#breakpointRemoved(IBreakpoint, IMarkerDelta)
	 */
	public void breakpointRemoved(IBreakpoint breakpoint, IMarkerDelta delta) {
		update();
	}
	/**
	 * @see IBreakpointListener#breakpointChanged(IBreakpoint, IMarkerDelta)
	 */
	public void breakpointChanged(IBreakpoint breakpoint, IMarkerDelta delta) {
	}
	
}