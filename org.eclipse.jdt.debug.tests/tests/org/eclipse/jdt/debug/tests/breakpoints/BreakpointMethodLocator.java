/*******************************************************************************
 *  Copyright (c) 2003, 2012 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
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
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;

/**
 * Compute the name of field declared at a given position from an JDOM CompilationUnit.
 */
public class BreakpointMethodLocator extends ASTVisitor {

	private int fPosition;

	private String fTypeName;

	private String fMethodName;

	private String fMethodSignature;

	private boolean fFound;

	/**
	 * Constructor
	 * @param position the position in the compilation unit.
	 */
	public BreakpointMethodLocator(int position) {
		fPosition= position;
		fFound= false;
	}

	/**
	 * Return the name of the method declared at the given position.
	 * Return <code>null</code> if there is no method declaration at the given position.
	 * .
	 */
	public String getMethodName() {
		return fMethodName;
	}

	/**
	 * Return the name of the method declared at the given position.
	 * Return <code>null</code> if there is no method declaration at the given position or
	 * if not possible to compute the signature of the method declared at the given
	 * position.
	 * @see BreakpointFieldLocator#getMethodName()
	 */
	public String getMethodSignature() {
		return fMethodSignature;
	}

	/**
	 * Return the name of type in which the method is declared.
	 * Return <code>null</code> if there is no method declaration at the given position.
	 * @see BreakpointFieldLocator#getMethodName()
	 */
	public String getTypeName() {
		return fTypeName;
	}

	private boolean containsPosition(ASTNode node) {
		int startPosition= node.getStartPosition();
		int endPosition = startPosition + node.getLength();
		return startPosition <= fPosition && fPosition <= endPosition;
	}

	private String computeMethodSignature(MethodDeclaration node) {
		if (node.getExtraDimensions() != 0 || Modifier.isAbstract(node.getModifiers())) {
			return null;
		}
		StringBuilder signature= new StringBuilder();
		signature.append('(');
		List<SingleVariableDeclaration> parameters = node.parameters();
		for (Iterator<SingleVariableDeclaration> iter = parameters.iterator(); iter.hasNext();) {
			Type type = iter.next().getType();
			if (type instanceof PrimitiveType) {
				appendTypeLetter(signature, (PrimitiveType)type);
			} else {
				return null;
			}
		}
		signature.append(')');
		Type returnType;
		returnType= node.getReturnType2();
		if (returnType instanceof PrimitiveType) {
			appendTypeLetter(signature, (PrimitiveType)returnType);
		} else {
			return null;
		}
		return signature.toString();
	}

	private void appendTypeLetter(StringBuilder signature, PrimitiveType type) {
		PrimitiveType.Code code= type.getPrimitiveTypeCode();
		if (code == PrimitiveType.BYTE) {
			signature.append('B');
		} else if (code == PrimitiveType.CHAR) {
			signature.append('C');
		} else if (code == PrimitiveType.DOUBLE) {
			signature.append('D');
		} else if (code == PrimitiveType.FLOAT) {
			signature.append('F');
		} else if (code == PrimitiveType.INT) {
			signature.append('I');
		} else if (code == PrimitiveType.LONG) {
			signature.append('J');
		} else if (code == PrimitiveType.SHORT) {
			signature.append('S');
		} else if (code == PrimitiveType.VOID) {
			signature.append('V');
		} else if (code == PrimitiveType.BOOLEAN) {
			signature.append('Z');
		}
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.CompilationUnit)
	 */
	@Override
	public boolean visit(CompilationUnit node) {
		// visit only the type declarations
		List<TypeDeclaration> types = node.types();
		for (Iterator<TypeDeclaration> iter = types.iterator(); iter.hasNext() && !fFound;) {
			iter.next().accept(this);
		}
		return false;
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.FieldDeclaration)
	 */
	@Override
	public boolean visit(MethodDeclaration node) {
		if (containsPosition(node)) {
			if (node.isConstructor()) {
				fMethodName= "<init>"; //$NON-NLS-1$
			} else {
				fMethodName= node.getName().getIdentifier();
			}
			fMethodSignature= computeMethodSignature(node);
			fTypeName= computeTypeName(node);
			fFound= true;
		}
		return false;
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.TypeDeclaration)
	 */
	@Override
	public boolean visit(TypeDeclaration node) {
		if (containsPosition(node)) {
			// visit the methode declarations
			MethodDeclaration[] methods = node.getMethods();
			for (int i = 0, length = methods.length; i < length && !fFound; i++) {
				methods[i].accept(this);
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
	 * Compute the name of the type which contains this node.
	 * Result will be the name of the type or the inner type which contains this node, but not of the local or anonymous type.
	 */
	private String computeTypeName(ASTNode node) {
		String typeName = null;
		ASTNode newnode = node;
		while (!(newnode instanceof CompilationUnit)) {
			if (newnode instanceof AbstractTypeDeclaration) {
				String identifier= ((AbstractTypeDeclaration)newnode).getName().getIdentifier();
				if (typeName == null) {
					typeName= identifier;
				} else {
					typeName= identifier + "$" + typeName; //$NON-NLS-1$
				}
			}
			newnode= newnode.getParent();
		}
		PackageDeclaration packageDecl= ((CompilationUnit)newnode).getPackage();
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
