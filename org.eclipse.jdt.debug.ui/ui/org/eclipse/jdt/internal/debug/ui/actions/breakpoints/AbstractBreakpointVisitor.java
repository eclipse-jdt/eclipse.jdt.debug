package org.eclipse.jdt.internal.debug.ui.actions.breakpoints;

/**********************************************************************
Copyright (c) 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

/**
 * Visits an AST, assembling a collection of nodes on which a breakpoint could
 * be created.
 * <p>
 * NOTE: This class is yet experimental. Investigating breakpoint creation
 * and location verification via the use of an AST. This could be used to
 * support breakpoints in external source (i.e. without the knowlegde of Java
 * model elements).
 * </p>
 */
public class AbstractBreakpointVisitor extends ASTVisitor {
	
	/**
	 * Compilation unit	 */
	private CompilationUnit fCompilationUnit;
	
	/**
	 * Breakpoint offset/location	 */
	private int fOffset;
	
	/**
	 * Line number associated with offset
	 */
	private int fLineNumber;
	
	/**
	 * List of nodes that contain the line number/start offset	 */
	private List fTargetNodes = new ArrayList();

	/**
	 * Sets the offset at which the breakpoint has been requested.
	 * 
	 * @param offset the character offset in a compiliation unit at which a
	 * breakpoint has been requested
	 */
	public void setOffset(int offset) {
		fOffset = offset;
	}

	/**
	 * Returns the line number in the associated compilation unit at which the
	 * breakpoint has been requested.
	 * 
	 * @return  the line number in the associated compilation unit at which the
	 * breakpoint has been requested
	 */
	public int getLineNumber() {
		return fLineNumber;
	}
	
	/**
	 * Returns the AST being visited
	 * 
	 * @return the AST being visited
	 */
	public CompilationUnit getCompilationUnit() {
		return fCompilationUnit;
	}
	
	/**
	 * Returns the offset at which the breakpoint was requested
	 * 
	 * @return the offset at which the breakpoint was requested
	 */
	public int getOffset() {
		return fOffset;
	}
	
	/**
	 * Adds the given node to the list of potential targets for the breakpoint
	 * 
	 * @param node the node to add
	 */
	public void addNode(ASTNode node) {
		fTargetNodes.add(node);
	}
	
	/**
	 * Returns a list of potential AST nodes for the breakpoint
	 *
	 * @return a list of potential AST nodes for the breakpoint
	 */
	public List getNodes() {
		return fTargetNodes;
	}	
	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.CompilationUnit)
	 */
	public boolean visit(CompilationUnit node) {
		fLineNumber = node.lineNumber(getOffset());
		fTargetNodes.clear();
		fCompilationUnit = node;
		return true;
	}
	
	/**
	 * Returns whether the line number at which the breakpoint has been
	 * requested is included in the line number range of the given node.
	 * 
	 * @param node
	 * @return whether the line number at which the breakpoint has been
	 * requested is included in the line number range of the given node
	 */
	public boolean includesLineNumber(ASTNode node) {
		int start = node.getStartPosition();
		int nodeLineStart = getCompilationUnit().lineNumber(start);
		int nodeLineEnd = getCompilationUnit().lineNumber(start + node.getLength() - 1);
		return getLineNumber() >= nodeLineStart && getLineNumber() <= nodeLineEnd;
	}
	
	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.TypeDeclaration)
	 */
	public boolean visit(TypeDeclaration node) {
		return includesLineNumber(node);
	}	

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.PackageDeclaration)
	 */
	public boolean visit(PackageDeclaration node) {
		return false;
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.ImportDeclaration)
	 */
	public boolean visit(ImportDeclaration node) {
		return false;
	}

}
