package org.eclipse.jdt.internal.debug.ui.actions.breakpoints;

/**********************************************************************
Copyright (c) 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

/**
 * NOTE: This class is yet experimental. Investigating breakpoint creation
 * and location verification via the use of an AST. This could be used to
 * support breakpoints in external source (i.e. without the knowlegde of
 * Java model elements).
 */
public class BreakpointLocationVerifier extends ASTVisitor {
	
	/**
	 * Compilation unit	 */
	private CompilationUnit fCompilationUnit;
	
	/**
	 * Breakpoint offset/location	 */
	private int fOffset;
	
	/**
	 * List of nodes that contain the line number/start offset	 */
	private List fApplicableNodes = new ArrayList();

	/**
	 * Constructor a breakpoint location verifier
	 * 
	 * @param offset the character offset of the line number which the
	 * 	breakpoint is being placed on
	 */
	public BreakpointLocationVerifier(int offset, CompilationUnit compilationUnit) {
		super();
		fOffset = offset;
		fCompilationUnit = compilationUnit;
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#preVisit(org.eclipse.jdt.core.dom.ASTNode)
	 */
	public void preVisit(ASTNode node) {
		super.preVisit(node);
		super.postVisit(node);
		int start = node.getStartPosition();
		int end = start + node.getLength();
		if (start <= fOffset && fOffset <= end) {
			fApplicableNodes.add(node);
		}		
	}
	
	/**
	 * Returns the closest valid offset for a line breakpoint.	 */
	public int verifyLocation() {
		fCompilationUnit.accept(this);
		int length = fApplicableNodes.size();
		for (int i = length - 1; i >= 0; i--) {
			ASTNode node = (ASTNode)fApplicableNodes.get(i);
			int offset = verifyNode(node);
			if (offset >= 0) {
				return offset;
			}
		} 
		// nothing
		return -1;
	}
	
	protected int verifyNode(ASTNode node) {
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
		return -1;
	}

	protected int verifyStatement(Statement statement) {
		if (statement instanceof Block) {
			return verifyBlock((Block)statement);
		}
		if (statement instanceof TryStatement) {
			return verifyBlock(((TryStatement)statement).getBody());
		}
		if (statement instanceof VariableDeclarationStatement) {
			VariableDeclarationStatement decl = (VariableDeclarationStatement)statement;
			Iterator fragments = decl.fragments().iterator();
			while (fragments.hasNext()) {
				int offset = verifyFragment((VariableDeclarationFragment)fragments.next());
				if (offset >= 0) {
					return offset;
				}
			}
			return -1;
		}
		
		return statement.getStartPosition();
	}
	
	protected int verifyFragment(VariableDeclarationFragment fragment) {
		if (fragment.getInitializer() != null) {
			return fragment.getParent().getStartPosition();
		}
		return -1;
	}
	
	protected int verifyBlock(Block block) {
		if (block != null) {
			Iterator statements = block.statements().iterator();
			while (statements.hasNext()) {
				int offset = verifyStatement((Statement)statements.next());
				if (offset >= 0) {
					return offset;
				}
			}
		}	
		return -1;
	}
}
