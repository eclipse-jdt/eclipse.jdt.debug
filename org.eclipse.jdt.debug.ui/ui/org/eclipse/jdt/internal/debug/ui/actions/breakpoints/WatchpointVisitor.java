package org.eclipse.jdt.internal.debug.ui.actions.breakpoints;

/**********************************************************************
Copyright (c) 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;

/**
 * Identifies AST nodes valid for watchpoints
 */
public class WatchpointVisitor extends AbstractBreakpointVisitor {
		
	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
	 * FieldDeclaration)
	 */
	public boolean visit(FieldDeclaration node) {
		if (includesLineNumber(node)) {
			addNode(node);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.MethodDeclaration)
	 */
	public boolean visit(MethodDeclaration node) {
		return false;
	}

}
