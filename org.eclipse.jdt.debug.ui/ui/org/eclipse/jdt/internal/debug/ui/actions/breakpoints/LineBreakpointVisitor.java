package org.eclipse.jdt.internal.debug.ui.actions.breakpoints;

/**********************************************************************
Copyright (c) 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.EmptyStatement;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;

/**
 * Identifies AST nodes valid for line breakpoints
 */
public class LineBreakpointVisitor extends AbstractBreakpointVisitor {
		
	/**
	 * Constructs a line breakpoint visitor
	 * 
	 * @see AbstractBreakpointVisitor#AbstractBreakpointVisitor(int)
	 */
	public LineBreakpointVisitor(int offset) {
		super(offset);
	}

	/**
	 * A line breakpoint can be placed on a statement or a field declartion that
	 * has an initializer.
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#preVisit(org.eclipse.jdt.core.dom.ASTNode)
	 */
	public void preVisit(ASTNode node) {
		super.preVisit(node);
		if (node instanceof Statement ||
			node instanceof FieldDeclaration ||
			node instanceof CatchClause) {
			if (node instanceof EmptyStatement ||
				node instanceof TypeDeclarationStatement) {
				// these statements not valid line breakpoint locations
				return;
			}
			int start = node.getStartPosition();
			int nodeLineStart = getCompilationUnit().lineNumber(start);
			int nodeLineEnd = getCompilationUnit().lineNumber(start + node.getLength());
			if (getLineNumber() >= nodeLineStart && getLineNumber() <= nodeLineEnd) {
				addNode(node);			
			}
		}
	}
	
}
