/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.actions;

import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.PrimitiveType;
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
		StringBuffer signature= new StringBuffer();
		signature.append('(');
		List parameters = node.parameters();
		for (Iterator iter = parameters.iterator(); iter.hasNext();) {
			Type type = ((SingleVariableDeclaration) iter.next()).getType();
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
	
	private void appendTypeLetter(StringBuffer signature, PrimitiveType type) {
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
	public boolean visit(MethodDeclaration node) {
		if (containsPosition(node)) {
			if (node.isConstructor()) {
				fMethodName= "<init>"; //$NON-NLS-1$
			} else {
				fMethodName= node.getName().getIdentifier();
			}
			fMethodSignature= computeMethodSignature(node);
			fTypeName= ValidBreakpointLocationLocator.computeTypeName(node);
			fFound= true;
		}
		return false;
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.TypeDeclaration)
	 */
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

}
