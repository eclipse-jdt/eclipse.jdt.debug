/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.actions;


import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.internal.debug.ui.BreakpointUtils;
import org.eclipse.jdt.internal.debug.ui.ExceptionHandler;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.snippeteditor.JavaSnippetEditor;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.texteditor.IEditorStatusLine;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Action for adding/removing breakpoints at a line in a type represented
 * by the source shown in the active Java Editor.
 */
public class ManageBreakpointActionDelegate implements IWorkbenchWindowActionDelegate, IPartListener {
	
	protected boolean fInitialized= false;
	private IAction fAction= null;
	private int fLineNumber;
	private IType fType= null;
	private ITextEditor fTextEditor= null;
	private IWorkbenchWindow fWorkbenchWindow= null;

	public ManageBreakpointActionDelegate() {
	}
	
	/**
	 * Manages a breakpoint.
	 */
	protected void manageBreakpoint(IEditorInput editorInput) {
		ISelectionProvider sp= getTextEditor().getSelectionProvider();
		IType type = getType();
		if (sp == null) {
			report(ActionMessages.getString("ManageBreakpointActionDelegate.No_Breakpoint")); //$NON-NLS-1$
			return;
		}
		report(null);
		ISelection selection= sp.getSelection();
		if (selection instanceof ITextSelection) {
			IDocument document= getTextEditor().getDocumentProvider().getDocument(editorInput);
			
			int lineNumber= ((ITextSelection)selection).getStartLine() + 1;

			
			int offset= ((ITextSelection)selection).getOffset();
			try {
				if (type == null) {
					IClassFile classFile= (IClassFile)editorInput.getAdapter(IClassFile.class);
					if (classFile != null) {
						type= classFile.getType();
						// bug 34856 - if this is an inner type, ensure the breakpoint is not
						// being added to the outer type
						if (type.getDeclaringType() != null) {
							ISourceRange sourceRange= type.getSourceRange();
							int start= sourceRange.getOffset();
							int end= start + sourceRange.getLength();
							if (offset < start || offset > end) {
								// not in the inner type
								IStatusLineManager manager= getTextEditor().getEditorSite().getActionBars().getStatusLineManager();
								manager.setErrorMessage(MessageFormat.format(ActionMessages.getString("ManageBreakpointRulerAction.Breakpoints_can_only_be_created_within_the_type_associated_with_the_editor__{0}._1"), new String[] { type.getTypeQualifiedName()})); //$NON-NLS-1$
								Display.getCurrent().beep();
								return;
							}
						}
					}
				}
			
				String typeName= null;
				IResource resource;
				IJavaLineBreakpoint breakpoint= null;
				if (type == null) {
					if (editorInput instanceof IFileEditorInput) {
						resource= ((IFileEditorInput)editorInput).getFile();
					} else {
						resource= ResourcesPlugin.getWorkspace().getRoot();
					}
				} else {
					typeName= type.getFullyQualifiedName();
					IJavaLineBreakpoint existingBreakpoint= JDIDebugModel.lineBreakpointExists(typeName, lineNumber);
					if (existingBreakpoint != null) {
						DebugPlugin.getDefault().getBreakpointManager().removeBreakpoint(existingBreakpoint, true);
						return;
					}
					resource= BreakpointUtils.getBreakpointResource(type);
					Map attributes = new HashMap(10);
					try {
						IRegion line= document.getLineInformation(lineNumber - 1);
						int start= line.getOffset();
						int end= start + line.getLength() - 1;
						BreakpointUtils.addJavaBreakpointAttributesWithMemberDetails(attributes, type, start, end);
					} catch (BadLocationException ble) {
						JDIDebugUIPlugin.log(ble);
					}
					breakpoint= JDIDebugModel.createLineBreakpoint(resource, typeName, lineNumber, -1, -1, 0, true, attributes);
				}
				new BreakpointLocationVerifierJob(document, offset, breakpoint, lineNumber, typeName, type, resource).schedule();
			} catch (CoreException ce) {
				ExceptionHandler.handle(ce, ActionMessages.getString("ManageBreakpointActionDelegate.error.title1"), ActionMessages.getString("ManageBreakpointActionDelegate.error.message1")); //$NON-NLS-1$ //$NON-NLS-2$
				return;
			}
		}
	}
	
	/**
	 * Determines if a breakpoint exists on the line of the current selection.
	 */
	protected boolean breakpointExists() {
		IType type= retrieveType();
		if (type != null) {
			try {
				return JDIDebugModel.lineBreakpointExists(type.getFullyQualifiedName(), getLineNumber()) == null;
			} catch (CoreException ce) {
				JDIDebugUIPlugin.log(ce);
			}
		}
	
		return false;
	}
	
	protected IType retrieveType() {
		IType type= null;
		ISelectionProvider sp= getTextEditor().getSelectionProvider();
		if (sp != null) {
			ISelection s= sp.getSelection();
			if (s instanceof ITextSelection) {
				ITextSelection selection= (ITextSelection) s;
				setLineNumber(selection.getStartLine() + 1);
				type= getType();
				if (type != null && type.exists()) {
					try {
						ISourceRange sourceRange= type.getSourceRange();
						if (selection.getOffset() >= sourceRange.getOffset() && selection.getOffset() <= (sourceRange.getOffset() + sourceRange.getLength() - 1)) {
							return type;
						}
					} catch(JavaModelException e) {
						JDIDebugUIPlugin.log(e);
					}	
				}
				type= getType0(selection);
			}
		}
		return type;
	}
	
	protected IType getType0(ITextSelection selection) {
		IMember member= ActionDelegateHelper.getDefault().getCurrentMember(selection);
		IType type= null;
		if (member instanceof IType) {
			type = (IType)member;
		} else if (member != null) {
			type= member.getDeclaringType();
		}
	
		setType(type);
		return type;
	}
	
	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		if (getTextEditor() != null) {
			update();
			manageBreakpoint(getTextEditor().getEditorInput());
		}
	}
	
	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		if (!fInitialized) {
			initialize(action);
		} 
	}

	protected void initialize(IAction action) {
		setAction(action);
		if (getWorkbenchWindow() != null) {
			IWorkbenchPage page= getWorkbenchWindow().getActivePage();
			if (page != null) {
				IEditorPart part= page.getActiveEditor();
				if (part instanceof ITextEditor) {
					if (!(part instanceof JavaSnippetEditor)) {
						setTextEditor((ITextEditor)part);
					}
				}
			}
		}
		fInitialized= true;
	}
	
	protected void update() {
		IAction action= getAction();
		if (action != null) {
			if (getTextEditor() != null) {
				breakpointExists();
			}
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
	
	protected IAction getAction() {
		return fAction;
	}

	protected void setAction(IAction action) {
		fAction = action;
	}
	
	/**
	 * @see IPartListener#partActivated(IWorkbenchPart)
	 */
	public void partActivated(IWorkbenchPart part) {
		if (part instanceof ITextEditor) {
			if (part instanceof JavaSnippetEditor) {
				setTextEditor(null);
			} else {
				setTextEditor((ITextEditor)part);
			}	
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
		if (part == getTextEditor()) {
			setTextEditor(null);
			if (getAction() != null) {
				getAction().setEnabled(false);
			}
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
		if (part instanceof ITextEditor) {
			if (getTextEditor() == null) {
				if (!(part instanceof JavaSnippetEditor)) {
					setTextEditor((ITextEditor)part);
				}
			}
		}
	}
	
	protected ITextEditor getTextEditor() {
		return fTextEditor;
	}

	protected void setTextEditor(ITextEditor editor) {
		fTextEditor = editor;
		setType(null);
		setEnabledState(editor);
	}

	protected void setEnabledState(ITextEditor editor) {
		if (getAction() != null) {
			getAction().setEnabled(editor != null  
			&& (editor.getSite().getId().equals(JavaUI.ID_CF_EDITOR)
			|| editor.getSite().getId().equals(JavaUI.ID_CU_EDITOR)));
		} 
	}
	
	/**
	 * @see IWorkbenchWindowActionDelegate#init(IWorkbenchWindow)
	 */
	public void init(IWorkbenchWindow window) {
		setWorkbenchWindow(window);
		window.getPartService().addPartListener(this);
	}

	/**
	 * @see IWorkbenchWindowActionDelegate#dispose()
	 */
	public void dispose() {
		getWorkbenchWindow().getPartService().removePartListener(this);
	}

	protected IWorkbenchWindow getWorkbenchWindow() {
		return fWorkbenchWindow;
	}

	protected void setWorkbenchWindow(IWorkbenchWindow workbenchWindow) {
		fWorkbenchWindow = workbenchWindow;
	}

	protected void report(String message) {
		if (getTextEditor() != null) {
			IEditorStatusLine statusLine= (IEditorStatusLine) getTextEditor().getAdapter(IEditorStatusLine.class);
			if (statusLine != null) {
				if (message != null) {
					statusLine.setMessage(true, message, null);
				} else {
					statusLine.setMessage(true, null, null);
				}
			}
		}		
		if (message != null && JDIDebugUIPlugin.getActiveWorkbenchShell() != null) {
			JDIDebugUIPlugin.getActiveWorkbenchShell().getDisplay().beep();
		}
	}
}
