package org.eclipse.jdt.internal.debug.ui.actions.breakpoints;

/**********************************************************************
Copyright (c) 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * NOTE: This class is yet experimental. Investigating breakpoint creation
 * and location verification via the use of an AST. This could be used to
 * support breakpoints in external source (i.e. without the knowlegde of
 * Java model elements).
 */
public class AddRemoveLineBreakpointActionDelegate extends AbstractAddRemoveBreakpointActionDelegate {
	
	/**
	 * Constructs a new action to add/remove a line breakpoint
	 */
	public AddRemoveLineBreakpointActionDelegate() {
		super(null);
	}

	/**
	 * @see org.eclipse.jdt.internal.debug.ui.actions.breakpoints.AbstractAddRemoveBreakpointActionDelegate#doAction()
	 */
	protected void doAction() {
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
			int lineNumber = compilationUnit.lineNumber(node.getStartPosition());
			// construct breakpoint
			PackageDeclaration packageDeclaration = compilationUnit.getPackage();
			System.out.println(packageDeclaration.getName().toString() + " " + lineNumber);
		} else {
			// TODO: unable to add breakpoint
		}
	}
	
	/**
	 * Returns AST node in the given complation unit, that is closest to the
	 * specified offset, at which a line breakpoint may be placed, or <code>null</code>
	 * if no node can be found.
	 *
	 * @param compilationUnit
	 * @param offset
	 * @return node at which breakpoint can be placed
	 */
	protected ASTNode locateTargetNode(CompilationUnit compilationUnit, int offset) {
		LineBreakpointVisitor visitor = new LineBreakpointVisitor(offset);
		compilationUnit.accept(visitor);
		List nodes = visitor.getNodes();
		int end = nodes.size() - 1;
		for (int i = end; i >= 0; i--) {
			ASTNode node = (ASTNode)nodes.get(i);
			ASTNode targetNode = verifyNode(node);
			if (targetNode != null) {
				return targetNode;
			}
		}
		// nothing
		return null;
	}	
	
	protected ASTNode verifyNode(ASTNode node) {
		if (node instanceof Statement) {
			return verifyStatement((Statement)node);
		}
		if (node instanceof MethodDeclaration) {
			// look for the first statement
			MethodDeclaration declaration = (MethodDeclaration)node;
			return verifyBlock(declaration.getBody());
		}
		if (node instanceof Initializer) {
			Initializer initializer = (Initializer)node;
			return verifyBlock(initializer.getBody());
		}
		if (node instanceof CatchClause) {
			CatchClause clause = (CatchClause)node;
			return verifyBlock(clause.getBody());
		}
		if (node instanceof FieldDeclaration) {
			return verifyFieldDeclaration((FieldDeclaration)node);
		}
		return null;
	}
	
	protected ASTNode verifyFieldDeclaration(FieldDeclaration fieldDeclaration) {
		return verifyVariableDeclarationFragments(fieldDeclaration.fragments());
	}

	protected ASTNode verifyVariableDeclarationFragments(List fragments) {
		Iterator iter = fragments.iterator();
		while (iter.hasNext()) {
			ASTNode node = verifyFragment((VariableDeclarationFragment)iter.next());
			if (node != null) {
				return node;
			}
		}
		return null;		
	}
	
	protected ASTNode verifyStatement(Statement statement) {
		if (statement instanceof Block) {
			return verifyBlock((Block)statement);
		}
		if (statement instanceof TryStatement) {
			return verifyBlock(((TryStatement)statement).getBody());
		}
		if (statement instanceof VariableDeclarationStatement) {
			VariableDeclarationStatement decl = (VariableDeclarationStatement)statement;
			return verifyVariableDeclarationFragments(decl.fragments());
		}
		return statement;
	}

	protected ASTNode verifyFragment(VariableDeclarationFragment fragment) {
		if (fragment.getInitializer() != null) {
			return fragment.getParent();
		}
		return null;
	}

	protected ASTNode verifyBlock(Block block) {
		if (block != null) {
			Iterator statements = block.statements().iterator();
			while (statements.hasNext()) {
				ASTNode node = verifyStatement((Statement)statements.next());
				if (node != null) {
					return node;
				}
			}
		}
		return null;
	}
}
