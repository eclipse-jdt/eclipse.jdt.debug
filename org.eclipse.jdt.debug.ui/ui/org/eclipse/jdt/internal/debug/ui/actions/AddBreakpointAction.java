package org.eclipse.jdt.internal.debug.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointListener;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.internal.debug.ui.BreakpointUtils;
import org.eclipse.jdt.internal.debug.ui.ExceptionHandler;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.ui.IWorkingCopyManager;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Action for setting breakpoints for a given text selection.
 */
public class AddBreakpointAction implements IEditorActionDelegate, IBreakpointListener, IPartListener {
	
	private IAction fAction= null;
	private int fLineNumber;
	private IType fType= null;
	private ITextEditor fTextEditor= null;
	
	public AddBreakpointAction() {
	}
	
	/**
	 * Creates a breakpoint.
	 */
	protected void createBreakpoint(IEditorInput editorInput) {
		IDocument document= getTextEditor().getDocumentProvider().getDocument(editorInput);
		BreakpointLocationVerifier bv = new BreakpointLocationVerifier();
		ISelectionProvider sp= getTextEditor().getSelectionProvider();
		if (sp == null) {
			return;
		}
		ISelection selection= sp.getSelection();
		if (selection instanceof ITextSelection) {
			int lineNumber = bv.getValidBreakpointLocation(document, ((ITextSelection)selection).getStartLine());		
			try {
				if (JDIDebugModel.lineBreakpointExists(getType().getFullyQualifiedName(), lineNumber)) {
					return;
				}
				Map attributes = new HashMap(10);
				BreakpointUtils.addJavaBreakpointAttributes(attributes, getType());
				JDIDebugModel.createLineBreakpoint(BreakpointUtils.getBreakpointResource(getType()), getType().getFullyQualifiedName(), lineNumber, -1, -1, 0, true, attributes);
			} catch (CoreException ce) {
				ExceptionHandler.handle(ce, ActionMessages.getString("AddBreakpoint.error.title1"), ActionMessages.getString("AddBreakpoint.error.message1")); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
	}
	
	/**
	 * Creates a breakpoint marker.
	 */
	protected boolean breakpointCanBeCreated(IEditorInput editorInput) {
		ISelectionProvider sp= getTextEditor().getSelectionProvider();
		if (sp == null) {
			return false;
		}
		ISelection s= sp.getSelection();
		if (!s.isEmpty() && s instanceof ITextSelection) {
			IType type= getType(editorInput);
			if (type != null) {
				try {
					return !JDIDebugModel.lineBreakpointExists(type.getFullyQualifiedName(), getLineNumber());
				} catch (CoreException ce) {
					JDIDebugUIPlugin.log(ce);
				}
			}
		}	
		return false;
	}
	
	protected IType getType(IEditorInput editorInput) {
		if (getType() != null) {
			return getType();
		}
		IType type = null;
		ISelectionProvider sp= getTextEditor().getSelectionProvider();
		if (sp != null) {
			ISelection s= sp.getSelection();
			if (!s.isEmpty() && s instanceof ITextSelection) {
				ITextSelection selection= (ITextSelection) s;
				type= getType0(selection, editorInput);
			}
		}
		return type;
	}
	
	protected IType getType0(ITextSelection selection, IEditorInput editorInput) {
		setLineNumber(selection.getStartLine() + 1);
		IType type= null;
		try {
			IClassFile classFile= (IClassFile)editorInput.getAdapter(IClassFile.class);
			if (classFile != null) {
				type = classFile.getType();
				setType(type);
			} else {
				IWorkingCopyManager manager= JavaUI.getWorkingCopyManager();
				ICompilationUnit unit= manager.getWorkingCopy(editorInput);
				if (unit == null) {
					return null;
				}
				IJavaElement e = unit.getElementAt(selection.getOffset());
				if (e instanceof IType) {
					type = (IType)e;
				}
				else if (e != null && e instanceof IMember) {
					type = ((IMember) e).getDeclaringType();
				}
				if (unit.getAllTypes().length == 1) {
					//cache the type as there is only one 
					setType(type);
				}
			}
		} catch (JavaModelException jme) {
			JDIDebugUIPlugin.log(jme);
		}
		return type;
	}
	/**
	 * @see IEditorActionDelegate#setActiveEditor(IAction, IEditorPart)
	 */
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		setPluginAction(action);
		if (targetEditor instanceof ITextEditor) {
			setTextEditor((ITextEditor)targetEditor);
			targetEditor.getSite().getPage().addPartListener(this);
			//see Bug 7012
			DebugPlugin.getDefault().getBreakpointManager().addBreakpointListener(this);
		} else {
			DebugPlugin.getDefault().getBreakpointManager().removeBreakpointListener(this);
		}
		setType(null);
		update();
	}
	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		if (getTextEditor() != null) {
			createBreakpoint(getTextEditor().getEditorInput());
		}
		action.setEnabled(false);
	}
	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof ITextSelection) {
			update();
		}
	}
		
	public void update() {
		IAction action= getPluginAction();
		if (action != null) {
			action.setEnabled(getTextEditor()!= null && breakpointCanBeCreated(getTextEditor().getEditorInput()));
		}
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
		asyncUpdate();
	}
	/**
	 * @see IBreakpointListener#breakpointRemoved(IBreakpoint, IMarkerDelta)
	 */
	public void breakpointRemoved(IBreakpoint breakpoint, IMarkerDelta delta) {
		asyncUpdate();
	}
	/**
	 * @see IBreakpointListener#breakpointChanged(IBreakpoint, IMarkerDelta)
	 */
	public void breakpointChanged(IBreakpoint breakpoint, IMarkerDelta delta) {
	}
	
	/**
	 * Update in the UI thread
	 */
	protected void asyncUpdate() {
		final Display d = JDIDebugUIPlugin.getStandardDisplay();
		if (d != null && !d.isDisposed()) {
			Runnable r = new Runnable() {
				public void run() {
					if (!d.isDisposed()) {
						update();
					}
				}
			};
			d.asyncExec(r);
		}
	}
	
	/**
	 * @see IPartListener#partActivated(IWorkbenchPart)
	 */
	public void partActivated(final IWorkbenchPart part) {
		if (part.getSite().getShell().getDisplay().isDisposed()) {
			return;
		}
		if (part.equals(getTextEditor())) {
			part.getSite().getShell().getDisplay().asyncExec(new Runnable() {
				/**
				 * @see Runnable#run()
				 */
				public void run() {
					if (!part.getSite().getShell().getDisplay().isDisposed()) {
						//must call after the editor is fully realized else we
						//create the working copy...revisist XXX
						update();
					}
				}
			});

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
		if (part.equals(getTextEditor())) {
			DebugPlugin.getDefault().getBreakpointManager().removeBreakpointListener(this);
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
	
	protected ITextEditor getTextEditor() {
		return fTextEditor;
	}

	protected void setTextEditor(ITextEditor editor) {
		fTextEditor = editor;
	}
}