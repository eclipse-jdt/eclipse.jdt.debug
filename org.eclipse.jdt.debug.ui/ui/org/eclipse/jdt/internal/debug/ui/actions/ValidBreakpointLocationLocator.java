/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.actions;

import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EmptyStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.eclipse.jdt.core.dom.PrefixExpression.Operator;

/**
 * Compute a valid location where to put a breakpoint from an JDOM CompilationUnit.
 * The result is the first valid location with a line number greater or equals than the given position.
 */
public class ValidBreakpointLocationLocator extends ASTVisitor {
	
	private CompilationUnit fCompilationUnit;
	private int fLineNumber;

	private int fLocation;
	private boolean fLocationFound;
	private String fTypeName;

	/**
	 * @param compilationUnit the JDOM CompilationUnit of the source code.
	 * @param lineNumber the line number in the source code where to put the breakpoint.
	 */
	public ValidBreakpointLocationLocator(CompilationUnit compilationUnit, int lineNumber) {
		fCompilationUnit= compilationUnit;
		fLineNumber= lineNumber;
		fLocationFound= false;
	}
	
	/**
	 * Return the line number of the computed valid location, or -1 if no valid location has been found.
	 */
	public int getValidLocation() {
		if (fLocationFound) {
			return fLocation;
		} else {
			return -1;
		}
	}
	
	/**
	 * Return of the type where the valid location is, or null if no valid location has been found.
	 */
	public String getFullyQualifiedTypeName() {
		return fTypeName;
	}
	
	/**
	 * Compute the name of the type which contains this node.
	 * Result will be the name of the type or the inner type which contains this node, but not of the local or anonymous type.
	 */
	static protected String computeTypeName(ASTNode node) {
		String typeName = null;
		while (!(node instanceof CompilationUnit)) {
			if (node instanceof TypeDeclaration) {
				String identifier= ((TypeDeclaration)node).getName().getIdentifier();
				if (typeName == null) {
					typeName= identifier;
				} else {
					typeName= identifier + "$" + typeName; //$NON-NLS-1$
				}
			} else {
				typeName= null;
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

	/**
	 * Return <code>true</code> if this node children may contain a valid location
	 * for the breakpoint.
	 * @param node the node.
	 * @param isCode true indicated that the first line of the given node always
	 *	contains some executable code, even if split in multiple lines.
	 */
	private boolean visit(ASTNode node, boolean isCode) {
		int startPosition= node.getStartPosition();
		int startLine = fCompilationUnit.lineNumber(startPosition);
		int endLine= fCompilationUnit.lineNumber(startPosition + node.getLength() - 1);
		// if we already found a correct location, or if the position is not in this part of the code,
		// no need to check the element inside.
		if (fLocationFound || endLine < fLineNumber) {
			return false;
		}
		// if the first line of this node always represents some executable code and the
		// breakpoint is requested on this line or on a previous line, this is a valid 
		// location
		if (isCode && (fLineNumber <= startLine)) {
			fLocation= startLine;
			fLocationFound= true;
			fTypeName= computeTypeName(node);
			return false;
		}
		return true;
	}
	
	private boolean isReplacedByConstantValue(Expression node) {
		switch (node.getNodeType()) {
			// litterals are constant
			case ASTNode.BOOLEAN_LITERAL:
			case ASTNode.CHARACTER_LITERAL:
			case ASTNode.NUMBER_LITERAL:
			case ASTNode.STRING_LITERAL:
				return true;
			case ASTNode.INFIX_EXPRESSION:
				return isReplacedByConstantValue((InfixExpression)node);
			case ASTNode.PREFIX_EXPRESSION:
				return isReplacedByConstantValue((PrefixExpression)node);
			default:
				return false;
		}
	}
	
	private boolean isReplacedByConstantValue(InfixExpression node) {
		// if all operands are constant value, the expression is replaced by a constant value
		if (!(isReplacedByConstantValue(node.getLeftOperand()) && isReplacedByConstantValue(node.getRightOperand()))) {
			return false;
		}
		if (node.hasExtendedOperands()) {
			for (Iterator iter = node.extendedOperands().iterator(); iter.hasNext(); ) {
				if (!isReplacedByConstantValue((Expression) iter.next())) {
					return false;
				}
			}
		}
		return true;
	}
	
	private boolean isReplacedByConstantValue(PrefixExpression node) {
		// for '-', '+', '~' and '!', if the operand is a constant value,
		// the expression is replaced by a constant value
		Operator operator = node.getOperator();
		if (operator != PrefixExpression.Operator.INCREMENT && operator != PrefixExpression.Operator.DECREMENT) {
			return isReplacedByConstantValue(node.getOperand());
		}
		return false;
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.AnonymousClassDeclaration)
	 */
	public boolean visit(AnonymousClassDeclaration node) {
		return visit(node, false);
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.ArrayAccess)
	 */
	public boolean visit(ArrayAccess node) {
		return visit(node, true);
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.ArrayCreation)
	 */
	public boolean visit(ArrayCreation node) {
		return visit(node, node.getInitializer() == null);
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.ArrayInitializer)
	 */
	public boolean visit(ArrayInitializer node) {
		return visit(node, true);
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.ArrayType)
	 */
	public boolean visit(ArrayType node) {
		return false;
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.AssertStatement)
	 */
	public boolean visit(AssertStatement node) {
		return visit(node, true);
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.Assignment)
	 */
	public boolean visit(Assignment node) {
		return visit(node, true);
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.Block)
	 */
	public boolean visit(Block node) {
		if (visit(node, false)) {
			if (node.statements().isEmpty() && node.getParent().getNodeType() == ASTNode.METHOD_DECLARATION) {
				// in case of an empty method, set the breakpoint on the last line of the empty block.
				fLocation= fCompilationUnit.lineNumber(node.getStartPosition() + node.getLength() - 1);
				fLocationFound= true;
				fTypeName= computeTypeName(node);
				return false;
			}
			return true;
		}
		return false;
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.BooleanLiteral)
	 */
	public boolean visit(BooleanLiteral node) {
		return visit(node, true);
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.BreakStatement)
	 */
	public boolean visit(BreakStatement node) {
		return visit(node, true);
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.CastExpression)
	 */
	public boolean visit(CastExpression node) {
		return visit(node, true);
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.CatchClause)
	 */
	public boolean visit(CatchClause node) {
		return visit(node, false);
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.CharacterLiteral)
	 */
	public boolean visit(CharacterLiteral node) {
		return visit(node, true);
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.ClassInstanceCreation)
	 */
	public boolean visit(ClassInstanceCreation node) {
		return visit(node, true);
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.CompilationUnit)
	 */
	public boolean visit(CompilationUnit node) {
		return visit(node, false);
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.ConditionalExpression)
	 */
	public boolean visit(ConditionalExpression node) {
		return visit(node, true);
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.ConstructorInvocation)
	 */
	public boolean visit(ConstructorInvocation node) {
		return visit(node, true);
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.ContinueStatement)
	 */
	public boolean visit(ContinueStatement node) {
		return visit(node, true);
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.DoStatement)
	 */
	public boolean visit(DoStatement node) {
		return visit(node, false);
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.EmptyStatement)
	 */
	public boolean visit(EmptyStatement node) {
		return false;
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.ExpressionStatement)
	 */
	public boolean visit(ExpressionStatement node) {
		return visit(node, false);
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.FieldAccess)
	 */
	public boolean visit(FieldAccess node) {
		return visit(node, false);
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.FieldDeclaration)
	 */
	public boolean visit(FieldDeclaration node) {
		if (visit(node, false)) {
			// visit only the variable declaration fragments, no the variable names.
			List fragments= node.fragments();
			for (Iterator iter= fragments.iterator(); iter.hasNext();) {
				((VariableDeclarationFragment)iter.next()).accept(this);
			}
		}
		return false;
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.ForStatement)
	 */
	public boolean visit(ForStatement node) {
		// in case on a "for(;;)", the breakpoint can be set on the first token of the node.
		return visit(node, node.initializers().isEmpty() && node.getExpression() == null && node.updaters().isEmpty());
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.IfStatement)
	 */
	public boolean visit(IfStatement node) {
		return visit(node, false);
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.ImportDeclaration)
	 */
	public boolean visit(ImportDeclaration node) {
		return false;
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.InfixExpression)
	 */
	public boolean visit(InfixExpression node) {
		if (visit(node, false)) {
			if (isReplacedByConstantValue(node)) {
				fLocation= fCompilationUnit.lineNumber(node.getStartPosition());
				fLocationFound= true;
				fTypeName= computeTypeName(node);
				return false;
			}
			return true;
		}
		return false;
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.Initializer)
	 */
	public boolean visit(Initializer node) {
		return visit(node, false);
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.InstanceofExpression)
	 */
	public boolean visit(InstanceofExpression node) {
		return visit(node, true);
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.Javadoc)
	 */
	public boolean visit(Javadoc node) {
		return false;
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.LabeledStatement)
	 */
	public boolean visit(LabeledStatement node) {
		return visit(node, false);
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.MethodDeclaration)
	 */
	public boolean visit(MethodDeclaration node) {
		if (visit(node, false)) {
			// visit only the body
			Block body = node.getBody();
			if (body != null) { // body is null for abstract methods
				body.accept(this);
			}
		}
		return false;
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.MethodInvocation)
	 */
	public boolean visit(MethodInvocation node) {
		return visit(node, true);
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.NullLiteral)
	 */
	public boolean visit(NullLiteral node) {
		return visit(node, true);
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.NumberLiteral)
	 */
	public boolean visit(NumberLiteral node) {
		return visit(node, true);
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.PackageDeclaration)
	 */
	public boolean visit(PackageDeclaration node) {
		return false;
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.ParenthesizedExpression)
	 */
	public boolean visit(ParenthesizedExpression node) {
		return visit(node, false);
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.PostfixExpression)
	 */
	public boolean visit(PostfixExpression node) {
		return visit(node, true);
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.PrefixExpression)
	 */
	public boolean visit(PrefixExpression node) {
		if (visit(node, false)) {
			if (isReplacedByConstantValue(node)) {
				fLocation= fCompilationUnit.lineNumber(node.getStartPosition());
				fLocationFound= true;
				fTypeName= computeTypeName(node);
				return false;
			}
			return true;
		}
		return false;
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.PrimitiveType)
	 */
	public boolean visit(PrimitiveType node) {
		return false;
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.QualifiedName)
	 */
	public boolean visit(QualifiedName node) {
		visit(node, true);
		return false;
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.ReturnStatement)
	 */
	public boolean visit(ReturnStatement node) {
		return visit(node, true);
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.SimpleName)
	 */
	public boolean visit(SimpleName node) {
		return visit(node, true);
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.SimpleType)
	 */
	public boolean visit(SimpleType node) {
		return false;
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.SingleVariableDeclaration)
	 */
	public boolean visit(SingleVariableDeclaration node) {
		return visit(node, false);
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.StringLiteral)
	 */
	public boolean visit(StringLiteral node) {
		return visit(node, true);
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.SuperConstructorInvocation)
	 */
	public boolean visit(SuperConstructorInvocation node) {
		return visit(node, true);
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.SuperFieldAccess)
	 */
	public boolean visit(SuperFieldAccess node) {
		return visit(node, true);
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.SuperMethodInvocation)
	 */
	public boolean visit(SuperMethodInvocation node) {
		return visit(node, true);
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.SwitchCase)
	 */
	public boolean visit(SwitchCase node) {
		return false;
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.SwitchStatement)
	 */
	public boolean visit(SwitchStatement node) {
		return visit(node, false);
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.SynchronizedStatement)
	 */
	public boolean visit(SynchronizedStatement node) {
		return visit(node, false);
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.ThisExpression)
	 */
	public boolean visit(ThisExpression node) {
		return visit(node, true);
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.ThrowStatement)
	 */
	public boolean visit(ThrowStatement node) {
		return visit(node, true);
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.TryStatement)
	 */
	public boolean visit(TryStatement node) {
		return visit(node, false);
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.TypeDeclaration)
	 */
	public boolean visit(TypeDeclaration node) {
		if (visit(node, false)) {
			// visit only the elements of the type declaration
			List bodyDeclaration= node.bodyDeclarations();
			for (Iterator iter= bodyDeclaration.iterator(); iter.hasNext();) {
				((BodyDeclaration)iter.next()).accept(this);
			}
		}
		return false;
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.TypeDeclarationStatement)
	 */
	public boolean visit(TypeDeclarationStatement node) {
		return visit(node, false);
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.TypeLiteral)
	 */
	public boolean visit(TypeLiteral node) {
		return false;
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.VariableDeclarationExpression)
	 */
	public boolean visit(VariableDeclarationExpression node) {
		return visit(node, false);
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.VariableDeclarationFragment)
	 */
	public boolean visit(VariableDeclarationFragment node) {
		Expression initializer = node.getInitializer();
		if (visit(node, false) && initializer != null) {
			visit(node.getName(), true);
			initializer.accept(this);
		}
		return false;
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.VariableDeclarationStatement)
	 */
	public boolean visit(VariableDeclarationStatement node) {
		return visit(node, false);
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.WhileStatement)
	 */
	public boolean visit(WhileStatement node) {
		return visit(node, false);
	}

}
