package org.eclipse.jdt.internal.debug.ui.actions.breakpoints;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;

/**********************************************************************
Copyright (c) 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/


/**
 * NOTE: This class is yet experimental. Investigating breakpoint creation
 * and location verification via the use of an AST. This could be used to
 * support breakpoints in external source (i.e. without the knowlegde of
 * Java model elements).
 */
public class AddRemoveWatchpointActionDelegate extends AbstractAddRemoveBreakpointActionDelegate {
	
	/**
	 * Constructs a new action to add/remove a watchpoint
	 */
	public AddRemoveWatchpointActionDelegate() {
		super(null);
	}
	
	/**
	 * @see org.eclipse.jdt.internal.debug.ui.actions.breakpoints.AbstractAddRemoveBreakpointActionDelegate#getVisitor()
	 */
	protected AbstractBreakpointVisitor getVisitor() {
		return new WatchpointVisitor();
	}

	/**
	 * @see org.eclipse.jdt.internal.debug.ui.actions.breakpoints.AbstractAddRemoveBreakpointActionDelegate#createBreakpoint(org.eclipse.jdt.core.dom.ASTNode, org.eclipse.jdt.core.IJavaElement)
	 */
	protected IJavaBreakpoint createBreakpoint(ASTNode node, IJavaElement element) throws CoreException {
		// get the type name for the node
		TypeDeclaration typeDeclaration = getTypeDeclaration(node);
		String name = getQualifiedName(typeDeclaration);
		System.out.println(name + " " + getCopmilationUnit(node).lineNumber(node.getStartPosition()));
		return null;
	}

}
