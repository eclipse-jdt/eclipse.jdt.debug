package org.eclipse.jdt.internal.debug.ui.actions.breakpoints;

/**********************************************************************
Copyright (c) 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.actions.PartEventAction;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * NOTE: This class is yet experimental. Investigating breakpoint creation
 * and location verification via the use of an AST. This could be used to
 * support breakpoints in external source (i.e. without the knowlegde of
 * Java model elements).
 */
public abstract class AbstractBreakpointActionDelegate extends PartEventAction implements IWorkbenchWindowActionDelegate {
	
	/**
	 * Window this action is active in.	 */
	private IWorkbenchWindow fWorkbenchWindow = null;
	
	/**
	 * The previous active part, or <code>null</code>.	 */
	private IWorkbenchPart fPreviousPart = null;
	
	/**
	 * This delegate's action or <code>null</code>.	 */
	private IAction fAction;

	/**
	 * @see PartEventAction#PartEventAction(java.lang.String)
	 */
	public AbstractBreakpointActionDelegate(String text) {
		super(text);
	}

	/**
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
	 */
	public void dispose() {
		if (getWorkbenchWindow() != null) {
			getWorkbenchWindow().getPartService().removePartListener(this);
		}
	}

	/**
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
	 */
	public void init(IWorkbenchWindow window) {
		fWorkbenchWindow = window;
		window.getPartService().addPartListener(this);
	}
	
	/**
	 * Returns the workbench window this action was installed in or <code>null</code>
	 * if none.
	 * 	 * @return the workbench window this action was installed in or <code>null</code>
	 * if none	 */
	protected IWorkbenchWindow getWorkbenchWindow() {
		return fWorkbenchWindow;
	}

	/**
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		fAction = action;
		ITextEditor editor = (ITextEditor)getActivePart();
		ISelectionProvider provider = editor.getSelectionProvider();
		ITextSelection selection = (ITextSelection)provider.getSelection();
		CompilationUnit compilationUnit = null;
		try {
			compilationUnit = createCompilationUnit(editor);
		} catch (CoreException e) {
			errorDialog(e);
			return;
		}
		int offset = selection.getOffset();

		ASTNode node = locateTargetNode(compilationUnit, offset);
		if (node != null) {
			IJavaBreakpoint breakpoint = getExistingBreakpoint(node);
			if (breakpoint == null) {
				IJavaElement javaElement = (IJavaElement)editor.getEditorInput().getAdapter(IJavaElement.class);
				try {
					createBreakpoint(node, javaElement);
				} catch (CoreException e) {
					errorDialog(e);
				}
			} else {
				// remove breakpoint
				try {
					breakpoint.delete();
				} catch (CoreException e) {
					errorDialog(e);
				}
			}
		} else {
			JDIDebugUIPlugin.getStandardDisplay().beep();
		}		
		
	}
	
	/**
	 * Returns any existing breakpoint for the given node, or <code>null</code>
	 * if none.
	 * 
	 * @param node node at which a breakpoint has been requested
	 * @return breakpoint or <code>null</code>
	 */
	protected abstract IJavaBreakpoint getExistingBreakpoint(ASTNode node);
	
	/**
	 * Creates and returns a breakpoint for the given node in the given class
	 * file or compilation unit.
	 * 
	 * @param node
	 * @param element
	 * @return IJavaBreakpoint
	 * @exception CoreException if an exception occurrs creating the breakpoint
	 */
	protected abstract IJavaBreakpoint createBreakpoint(ASTNode node, IJavaElement element) throws CoreException;
	
	/**
	 * Returns a node in the given AST associated with the given offset at which
	 * a breakpoint can be created, or <code>null</code> if none.
	 * 
	 * @param compilationUnit
	 * @param offset
	 * @return a node in the given AST associated with the given offset at which
	 * a breakpoint can be created, or <code>null</code> if none
	 */
	protected ASTNode locateTargetNode(CompilationUnit compilationUnit,int offset) {
		AbstractBreakpointVisitor visitor = getVisitor();
		visitor.setOffset(offset);
		compilationUnit.accept(visitor);
		List nodes = visitor.getNodes();
		if (nodes.isEmpty()) {
			return null;
		}
		int end = nodes.size() - 1;
		return (ASTNode)nodes.get(end);	
	}

	/**
	 * Returns the AST visitor for this type of breakpoint.
	 * 
	 * @return AbstractBreakpointVisitor
	 */
	protected abstract AbstractBreakpointVisitor getVisitor();
	
	/**
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		fAction = action;
		fAction.setEnabled(computeEnabled());
	}
	
	/**
	 * Creates an AST based on the contents of the given text editor.
	 * 	 * @param editor	 * @return ast
	 * @exception CoreException if unable to retrieve the contents of the given
	 *  text editor	 */
	protected CompilationUnit createCompilationUnit(ITextEditor editor) throws CoreException {
		IDocumentProvider provider = editor.getDocumentProvider();
		IEditorInput input = editor.getEditorInput();
		provider.connect(input);
		IDocument document =  provider.getDocument(editor.getEditorInput());
		String content = document.get();
		provider.disconnect(input);
		return AST.parseCompilationUnit(content.toCharArray());
	}
	
	/**
	 * @see org.eclipse.ui.IPartListener#partActivated(org.eclipse.ui.IWorkbenchPart)
	 */
	public void partActivated(IWorkbenchPart part) {
		fPreviousPart = getActivePart();
		super.partActivated(part);
		checkActivePartChange();
	}

	/**
	 * @see org.eclipse.ui.IPartListener#partClosed(org.eclipse.ui.IWorkbenchPart)
	 */
	public void partClosed(IWorkbenchPart part) {
		fPreviousPart = getActivePart();
		super.partClosed(part);
		checkActivePartChange();
	}
	
	/**
	 * Check if the active part has changed, and if so, update this actions
	 * enablement.
	 */
	protected void checkActivePartChange() {
		if (fPreviousPart != getActivePart()) {
			if (getAction() != null) {
				getAction().setEnabled(computeEnabled());
			}
		}
	}
	
	/**
	 * Returns this delegate's action or <code>null</code>.
	 * 	 * @return IAction or <code>null</code>	 */
	protected IAction getAction() {
		return fAction;
	}
	
	/**
	 * Returns whether this action should currently be enabled.
	 * 	 * @return boolean	 */
	protected boolean computeEnabled() {
		if (getActivePart() instanceof ITextEditor) {
			ITextEditor editor = (ITextEditor)getActivePart();
			String id = editor.getSite().getId();
			if (id.equals(JavaUI.ID_CF_EDITOR) || id.equals(JavaUI.ID_CU_EDITOR)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Displays the given exception in an error dialog.
	 */
	protected void errorDialog(CoreException e) {
		ErrorDialog.openError(JDIDebugUIPlugin.getActiveWorkbenchShell(), "Error", null, e.getStatus());
	}
	
	/**
	 * Returns first type declaration which is a parent of the given node,
	 * or <code>null</code> if none
	 * 
	 * @param node node at which to start searching for a type declaration
	 * @return first type declaration which is a parent of the given node,
	 * or <code>null</code> if none
	 */
	protected TypeDeclaration getTypeDeclaration(ASTNode node) {
		node = node.getParent();
		while (node != null && !(node instanceof TypeDeclaration)) {
			node = node.getParent();
		}
		return (TypeDeclaration)node;
	}	
	
	/**
	 * Returns the compilation unit the given node is contained in.
	 * 
	 * @param node
	 * @return CompilationUnit
	 */
	protected CompilationUnit getCopmilationUnit(ASTNode node) {
		return (CompilationUnit)node.getRoot();
	}
	
	/**
	 * Returns the fully qualified name of the given type declaration
	 * 
	 * @param typeDeclaration
	 * @return String
	 */
	protected String getQualifiedName(TypeDeclaration typeDeclaration) {
		StringBuffer typeName = new StringBuffer();
		CompilationUnit compilationUnit = getCopmilationUnit(typeDeclaration);
		// get the package
		PackageDeclaration packageDeclaration = compilationUnit.getPackage();
		if (packageDeclaration != null) {
			Name name = packageDeclaration.getName();
			while (name.isQualifiedName()) {
				QualifiedName qName = (QualifiedName)name;
				typeName.insert(0, qName.getName().getIdentifier());
				typeName.insert(0, '.');
				name = qName.getQualifier();
			}
			typeName.insert(0, ((SimpleName)name).getIdentifier());
			typeName.append('.');
		}
		// get any enclosing types
		TypeDeclaration enclosingType = getTypeDeclaration(typeDeclaration);
		int insertOffset = typeName.length();
		int dollarOffset = insertOffset;
		while (enclosingType != null) {
			String identifier = enclosingType.getName().getIdentifier();
			typeName.insert(insertOffset, identifier);
			dollarOffset = insertOffset + identifier.length();
			typeName.insert(dollarOffset, '$');
			enclosingType = getTypeDeclaration(enclosingType);
		}
		typeName.append(typeDeclaration.getName().getIdentifier());
		return typeName.toString();
	}	
	
	/**
	 * Returns the line number the given node begins on.
	 * 
	 * @param node
	 * @return line number
	 */
	protected int getStartLineNumber(ASTNode node) {
		CompilationUnit compilationUnit = getCopmilationUnit(node);
		return compilationUnit.lineNumber(node.getStartPosition());
	}
}
