/*******************************************************************************
 *  Copyright (c) 2003, 2008 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.breakpoints;

import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

/**
 * Compute the name of field declared at a given position from an JDOM CompilationUnit.
 */
public class BreakpointFieldLocator extends ASTVisitor {
	
	private int fPosition;
	
	private String fTypeName;
	
	private String fFieldName;

	private boolean fFound;

	/**
	 * Constructor
	 * @param position the position in the compilation unit.
	 */
	public BreakpointFieldLocator(int position) {
		fPosition= position;
		fFound= false;
	}

	/**
	 * Return the name of the field declared at the given position.
	 * Return <code>null</code> if there is no field declaration at the given position.
	 */
	public String getFieldName() {
		return fFieldName;
	}

	/**
	 * Return the name of type in which the field is declared.
	 * Return <code>null</code> if there is no field declaration at the given position.
	 */
	public String getTypeName() {
		return fTypeName;
	}
	
	private boolean containsPosition(ASTNode node) {
		int startPosition= node.getStartPosition();
		int endPosition = startPosition + node.getLength();
		return startPosition <= fPosition && fPosition <= endPosition;
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.CompilationUnit)
	 */
	public boolean visit(CompilationUnit node) {
		// visit only the type declarations
		List types = node.types();
		for (Iterator iter = types.iterator(); iter.hasNext() && !fFound;) {
			((TypeDeclaration) iter.next()).accept(this);
		}
		return false;
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.FieldDeclaration)
	 */
	public boolean visit(FieldDeclaration node) {
		if (containsPosition(node)) {
			// visit only the variable declaration fragments
			List fragments = node.fragments();
			if (fragments.size() == 1) {
				fFieldName= ((VariableDeclarationFragment)fragments.get(0)).getName().getIdentifier();
				fTypeName= computeTypeName(node);
				fFound= true;
				return false;
			}
			for (Iterator iter = fragments.iterator(); iter.hasNext() && !fFound;) {
				((VariableDeclarationFragment) iter.next()).accept(this);
			}
		}
		return false;
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.TypeDeclaration)
	 */
	public boolean visit(TypeDeclaration node) {
		if (containsPosition(node)) {
			// visit the field declarations
			FieldDeclaration[] fields = node.getFields();
			for (int i = 0, length = fields.length; i < length && !fFound; i++) {
				fields[i].accept(this);
			}
			if (!fFound) {
				// visit inner types
				TypeDeclaration[] types = node.getTypes();
				for (int i = 0, length = types.length; i < length && !fFound; i++) {
					types[i].accept(this);
				}
			}
		}
		return false;
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.VariableDeclarationFragment)
	 */
	public boolean visit(VariableDeclarationFragment node) {
		if (containsPosition(node)) {
			fFieldName= node.getName().getIdentifier();
			fTypeName= computeTypeName(node);
			fFound= true;
		}
		return false;
	}

	/**
	 * Compute the name of the type which contains this node.
	 * Result will be the name of the type or the inner type which contains this node, but not of the local or anonymous type.
	 */
	private String computeTypeName(ASTNode node) {
		String typeName = null;
		while (!(node instanceof CompilationUnit)) {
			if (node instanceof AbstractTypeDeclaration) {
				String identifier= ((AbstractTypeDeclaration)node).getName().getIdentifier();
				if (typeName == null) {
					typeName= identifier;
				} else {
					typeName= identifier + "$" + typeName; //$NON-NLS-1$
				}
			}
			node= node.getParent();
		}
		PackageDeclaration packageDecl= ((CompilationUnit)node).getPackage();
		String packageIdentifier= ""; //$NON-NLS-1$
		if (packageDecl != null) {
			Name packageName= packageDecl.getName();
			while (packageName.isQualifiedName()) {
				QualifiedName qualifiedName= (QualifiedName) packageName;
				packageIdentifier= qualifiedName.getName().getIdentifier() + "." + packageIdentifier; //$NON-NLS-1$
				packageName= qualifiedName.getQualifier();
			}
			packageIdentifier= ((SimpleName)packageName).getIdentifier() + "." + packageIdentifier; //$NON-NLS-1$
		}
		return packageIdentifier + typeName;
	}
}
