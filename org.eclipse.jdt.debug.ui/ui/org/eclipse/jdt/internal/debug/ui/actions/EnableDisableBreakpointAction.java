package org.eclipse.jdt.internal.debug.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.Iterator;

import org.eclipse.core.resources.IFile;
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
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Enables or disables a breakpoint
 */
public class EnableDisableBreakpointAction extends AddBreakpointAction implements IWorkbenchWindowActionDelegate {
	
	private MouseListener fMouseListener= new MouseAdapter() {
		public void mouseDown(MouseEvent event) {
			if (event.button == 3) {
				update(getBreakpoint());
				updateAction(getAction());
			}
		}
	};
	
	private IAction fAction= null;
	/**
	 * Creates the action to enable/disable breakpoints
	 */
	public EnableDisableBreakpointAction() {
		setText("&Enable");
		//setToolTipText(ActionMessages.getString("RunToLine.tooltip")); //$NON-NLS-1$
		//setDescription(ActionMessages.getString("RunToLine.description")); //$NON-NLS-1$
		
		update();
//		setHelpContextId(IHelpContextIds.ENABLE_DISABLE_BREAKPOINT_ACTION );					
	}

	
	/**
	 * @see Action#run()
	 */
	public void run() {
		IBreakpoint breakpoint= getBreakpoint();
		if (breakpoint != null) {
			try {
				breakpoint.setEnabled(!breakpoint.isEnabled());
				update(breakpoint);
			} catch (CoreException e) {
				ErrorDialog.openError(JDIDebugUIPlugin.getActiveWorkbenchWindow().getShell(), "Enabling/disabling breakpoints", "Exceptions occurred enabling disabling the breakpoint.", e.getStatus());
			}
		}
	}

	protected IBreakpoint getBreakpoint() {
		
		IType type= getType();
		//if (type == null) {
		//	type= getType(getTextEditor().getEditorInput());
	//		setType(type);
//		}
		if (type == null) {
			return null;
		}
		
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
					JDIDebugUIPlugin.logError(ce);
					continue;
				}
				if (match) {
					IType breakpointType= null;
					try {
						breakpointType= jBreakpoint.getType();
					} catch (CoreException ce) {
						JDIDebugUIPlugin.logError(ce);
						continue;
					}
					if (breakpointType.equals(getType())) {
						return breakpoint;
					}
				}
			}
		}
		return null;
	
	}
	protected void update(IBreakpoint breakpoint) {
		if (breakpoint == null) {
			setEnabled(false);
			return;
		}
		setEnabled(true);
		try {
			boolean enabled= breakpoint.isEnabled();
			setText(enabled ? "&Disable" : "&Enable");
		} catch (CoreException ce) {
			JDIDebugUIPlugin.logError(ce);
		}
	}
	
	protected boolean breakpointAtRulerLine(int breakpointLineNumber) {
		IVerticalRuler ruler= (IVerticalRuler)getTextEditor().getAdapter(IVerticalRuler.class);
		if (ruler != null) {
			int line= ruler.getLineOfLastMouseButtonActivity();
			return (line + 1) == breakpointLineNumber;
		} 
		return false;
	}
	/**
	 * @see SelectionProviderAction#selectionChanged(IStructuredSelection)
	 */
	public void selectionChanged(IStructuredSelection sel) {
		Iterator enum= sel.iterator();
		if (!enum.hasNext()) {
			//No selection
			setEnabled(false);
			return;
		}
		IBreakpoint bp= (IBreakpoint)enum.next();
		if (!enum.hasNext()) {
			//single selection
			try {
				if (bp.isEnabled()) {
					setText("&Disable");
				} else {
					setText("&Enable"); 
				}
			} catch (CoreException ce) {
				JDIDebugUIPlugin.logError(ce);
			}
		} 
		
		setEnabled(true);
	}

	/**
	 * @see IWorkbenchWindowActionDelegate#dispose()
	 */
	public void dispose() {
		ITextEditor editor= getTextEditor();
		if (editor != null) {
			IVerticalRuler ruler= (IVerticalRuler)getTextEditor().getAdapter(IVerticalRuler.class);
			if (ruler != null) {
				Control control= ruler.getControl();
				control.removeMouseListener(getMouseListener());
			}
		}
	}

	/**
	 * @see IWorkbenchWindowActionDelegate#init(IWorkbenchWindow)
	 */
	public void init(IWorkbenchWindow window) {
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		run();
		updateAction(action);
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			selectionChanged((IStructuredSelection)selection);
			updateAction(action);
		}
	}
	
	protected void updateAction(IAction action) {
		action.setText(getText());
		action.setEnabled(isEnabled());
	}
	
	/**
	 * @see IEditorActionDelegate#setActiveEditor(IAction, IEditorPart)
	 */
	public void setActiveEditor(final IAction action, IEditorPart targetEditor) {
		setAction(action);
		if (targetEditor instanceof ITextEditor) {
			setEditor((ITextEditor)targetEditor);
			IVerticalRuler ruler= (IVerticalRuler)getTextEditor().getAdapter(IVerticalRuler.class);
			if (ruler != null) {
				Control control= ruler.getControl();
				control.addMouseListener(getMouseListener());
			}
			setType(getType(getTextEditor().getEditorInput()));
		}
	}
	
	protected IAction getAction() {
		return fAction;
	}

	protected void setAction(IAction action) {
		fAction = action;
	}
	protected MouseListener getMouseListener() {
		return fMouseListener;
	}
	
	protected IType getType(IEditorInput editorInput) {
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
	}
}

