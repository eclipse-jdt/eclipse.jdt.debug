/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.core.hcr;

import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BlockComment;
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
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.LineComment;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MemberRef;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.MethodRef;
import org.eclipse.jdt.core.dom.MethodRefParameter;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.TextElement;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.TypeParameter;
import org.eclipse.jdt.core.dom.UnionType;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.eclipse.jdt.core.dom.WildcardType;

/**
 * Visits an AST to find a method declaration
 */
public class MethodSearchVisitor extends ASTVisitor {

	/**
	 * Method to search for
	 */
	private String fName;
	private String[] fParameterTypes;

	/**
	 * The search result, or <code>null</code> if none
	 */
	private MethodDeclaration fMatch;

	/**
	 * Sets the signature of the method to search for
	 * 
	 * @param methodName
	 *            name of method to search for
	 * @param methodSignature
	 *            signature of the method to search for
	 */
	public void setTargetMethod(String methodName, String methodSignature) {
		fName = methodName;
		fParameterTypes = Signature.getParameterTypes(methodSignature);
		// convert parameter types same format that we get from the AST type
		// bindings
		for (int i = 0; i < fParameterTypes.length; i++) {
			String type = fParameterTypes[i];
			type = type.replace('/', '.');
			fParameterTypes[i] = type;
		}
		fMatch = null;
	}

	/**
	 * Returns the search result, or <code>null</code> if none
	 * 
	 * @return matching method declartion or <code>null</code>
	 */
	public MethodDeclaration getMatch() {
		return fMatch;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
	 * MethodDeclaration)
	 */
	@Override
	public boolean visit(MethodDeclaration node) {
		if (node.getName().getIdentifier().equals(fName)) {
			IMethodBinding methodBinding = node.resolveBinding();
			if (methodBinding != null) {
				ITypeBinding[] typeBindings = methodBinding.getParameterTypes();
				if (typeBindings.length == fParameterTypes.length) {
					for (int i = 0; i < typeBindings.length; i++) {
						ITypeBinding typeBinding = typeBindings[i];
						String typeSignature = Signature.createTypeSignature(
								typeBinding.getQualifiedName(), true);
						if (!fParameterTypes[i].equals(typeSignature)) {
							return true;
						}
					}
					fMatch = node;
				}
			}
		}
		return isSearching();
	}

	/**
	 * Returns whether this visitor is still searching for a match.
	 * 
	 * @return whether this visitor is still searching for a match
	 */
	private boolean isSearching() {
		return fMatch == null;
	}

	@Override
	public boolean visit(AnnotationTypeDeclaration node) {
		return isSearching();
	}

	@Override
	public boolean visit(AnnotationTypeMemberDeclaration node) {
		return isSearching();
	}

	@Override
	public boolean visit(AnonymousClassDeclaration node) {
		return isSearching();
	}

	@Override
	public boolean visit(ArrayAccess node) {
		return isSearching();
	}

	@Override
	public boolean visit(ArrayCreation node) {
		return isSearching();
	}

	@Override
	public boolean visit(ArrayInitializer node) {
		return isSearching();
	}

	@Override
	public boolean visit(ArrayType node) {
		return isSearching();
	}

	@Override
	public boolean visit(AssertStatement node) {
		return isSearching();
	}

	@Override
	public boolean visit(Assignment node) {
		return isSearching();
	}

	@Override
	public boolean visit(Block node) {
		return isSearching();
	}

	@Override
	public boolean visit(BlockComment node) {
		return isSearching();
	}

	@Override
	public boolean visit(BooleanLiteral node) {
		return isSearching();
	}

	@Override
	public boolean visit(BreakStatement node) {
		return isSearching();
	}

	@Override
	public boolean visit(CastExpression node) {
		return isSearching();
	}

	@Override
	public boolean visit(CatchClause node) {
		return isSearching();
	}

	@Override
	public boolean visit(CharacterLiteral node) {
		return isSearching();
	}

	@Override
	public boolean visit(ClassInstanceCreation node) {
		return isSearching();
	}

	@Override
	public boolean visit(CompilationUnit node) {
		return isSearching();
	}

	@Override
	public boolean visit(ConditionalExpression node) {
		return isSearching();
	}

	@Override
	public boolean visit(ConstructorInvocation node) {
		return isSearching();
	}

	@Override
	public boolean visit(ContinueStatement node) {
		return isSearching();
	}

	@Override
	public boolean visit(DoStatement node) {
		return isSearching();
	}

	@Override
	public boolean visit(EmptyStatement node) {
		return isSearching();
	}

	@Override
	public boolean visit(EnhancedForStatement node) {
		return isSearching();
	}

	@Override
	public boolean visit(EnumConstantDeclaration node) {
		return isSearching();
	}

	@Override
	public boolean visit(EnumDeclaration node) {
		return isSearching();
	}

	@Override
	public boolean visit(ExpressionStatement node) {
		return isSearching();
	}

	@Override
	public boolean visit(FieldAccess node) {
		return isSearching();
	}

	@Override
	public boolean visit(FieldDeclaration node) {
		return isSearching();
	}

	@Override
	public boolean visit(ForStatement node) {
		return isSearching();
	}

	@Override
	public boolean visit(IfStatement node) {
		return isSearching();
	}

	@Override
	public boolean visit(ImportDeclaration node) {
		return isSearching();
	}

	@Override
	public boolean visit(InfixExpression node) {
		return isSearching();
	}

	@Override
	public boolean visit(Initializer node) {
		return isSearching();
	}

	@Override
	public boolean visit(InstanceofExpression node) {
		return isSearching();
	}

	@Override
	public boolean visit(Javadoc node) {
		return isSearching();
	}

	@Override
	public boolean visit(LabeledStatement node) {
		return isSearching();
	}

	@Override
	public boolean visit(LineComment node) {
		return isSearching();
	}

	@Override
	public boolean visit(MarkerAnnotation node) {
		return isSearching();
	}

	@Override
	public boolean visit(MemberRef node) {
		return isSearching();
	}

	@Override
	public boolean visit(MemberValuePair node) {
		return isSearching();
	}

	@Override
	public boolean visit(MethodInvocation node) {
		return isSearching();
	}

	@Override
	public boolean visit(MethodRef node) {
		return isSearching();
	}

	@Override
	public boolean visit(MethodRefParameter node) {
		return isSearching();
	}

	@Override
	public boolean visit(Modifier node) {
		return isSearching();
	}

	@Override
	public boolean visit(NormalAnnotation node) {
		return isSearching();
	}

	@Override
	public boolean visit(NullLiteral node) {
		return isSearching();
	}

	@Override
	public boolean visit(NumberLiteral node) {
		return isSearching();
	}

	@Override
	public boolean visit(PackageDeclaration node) {
		return isSearching();
	}

	@Override
	public boolean visit(ParameterizedType node) {
		return isSearching();
	}

	@Override
	public boolean visit(ParenthesizedExpression node) {
		return isSearching();
	}

	@Override
	public boolean visit(PostfixExpression node) {
		return isSearching();
	}

	@Override
	public boolean visit(PrefixExpression node) {
		return isSearching();
	}

	@Override
	public boolean visit(PrimitiveType node) {
		return isSearching();
	}

	@Override
	public boolean visit(QualifiedName node) {
		return isSearching();
	}

	@Override
	public boolean visit(QualifiedType node) {
		return isSearching();
	}

	@Override
	public boolean visit(ReturnStatement node) {
		return isSearching();
	}

	@Override
	public boolean visit(SimpleName node) {
		return isSearching();
	}

	@Override
	public boolean visit(SimpleType node) {
		return isSearching();
	}

	@Override
	public boolean visit(SingleMemberAnnotation node) {
		return isSearching();
	}

	@Override
	public boolean visit(SingleVariableDeclaration node) {
		return isSearching();
	}

	@Override
	public boolean visit(StringLiteral node) {
		return isSearching();
	}

	@Override
	public boolean visit(SuperConstructorInvocation node) {
		return isSearching();
	}

	@Override
	public boolean visit(SuperFieldAccess node) {
		return isSearching();
	}

	@Override
	public boolean visit(SuperMethodInvocation node) {
		return isSearching();
	}

	@Override
	public boolean visit(SwitchCase node) {
		return isSearching();
	}

	@Override
	public boolean visit(SwitchStatement node) {
		return isSearching();
	}

	@Override
	public boolean visit(SynchronizedStatement node) {
		return isSearching();
	}

	@Override
	public boolean visit(TagElement node) {
		return isSearching();
	}

	@Override
	public boolean visit(TextElement node) {
		return isSearching();
	}

	@Override
	public boolean visit(ThisExpression node) {
		return isSearching();
	}

	@Override
	public boolean visit(ThrowStatement node) {
		return isSearching();
	}

	@Override
	public boolean visit(TryStatement node) {
		return isSearching();
	}

	@Override
	public boolean visit(TypeDeclaration node) {
		return isSearching();
	}

	@Override
	public boolean visit(TypeDeclarationStatement node) {
		return isSearching();
	}

	@Override
	public boolean visit(TypeLiteral node) {
		return isSearching();
	}

	@Override
	public boolean visit(TypeParameter node) {
		return isSearching();
	}

	@Override
	public boolean visit(UnionType node) {
		return super.visit(node);
	}

	@Override
	public boolean visit(VariableDeclarationExpression node) {
		return isSearching();
	}

	@Override
	public boolean visit(VariableDeclarationFragment node) {
		return isSearching();
	}

	@Override
	public boolean visit(VariableDeclarationStatement node) {
		return isSearching();
	}

	@Override
	public boolean visit(WhileStatement node) {
		return isSearching();
	}

	@Override
	public boolean visit(WildcardType node) {
		return isSearching();
	}
}
