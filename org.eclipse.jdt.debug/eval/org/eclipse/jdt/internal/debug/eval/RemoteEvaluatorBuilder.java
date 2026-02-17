/*******************************************************************************
 * Copyright (c) 2019, 2020 Jesper Steen Møller and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Jesper Steen Møller - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.eval;

import static org.eclipse.jdt.core.eval.ICodeSnippetRequestor.LOCAL_VAR_PREFIX;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.eval.ICodeSnippetRequestor;
import org.eclipse.jdt.core.eval.IEvaluationContext;
import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;
import org.eclipse.jdt.internal.debug.eval.ast.engine.EvaluationEngineMessages;

/**
 * A builder for a reuseable expression evaluator against a runnng VM.
 */

@SuppressWarnings("rawtypes")
public class RemoteEvaluatorBuilder {

	private final IJavaProject javaProject;
	private final ExpressionBinder binder;
	private final String enclosingTypeName;
	private final String packageName;
	private final boolean isStatic;
	private final boolean isConstructor;
	private final List<String> argumentNames = new ArrayList<>();
	private final List<String> argumentTypeNames = new ArrayList<>();

	/**
	 * The names and bytecodes of the code snippet class to instantiate
	 */
	private final LinkedHashMap<String, byte[]> classFiles = new LinkedHashMap<>();

	/**
	 * The name of the code snippet class to instantiate
	 */
	private String codeSnippetClassName = null;
	private String snippet = null;
	private final ITypeBinding enclosingClass;

	public RemoteEvaluatorBuilder(IJavaProject javaProject, ExpressionBinder binder, ITypeBinding enclosingClass, boolean isStatic, boolean isConstructor) {
		this.javaProject = javaProject;
		this.binder = binder;
		this.enclosingClass = enclosingClass;
		this.enclosingTypeName = enclosingClass.getQualifiedName();
		this.packageName = enclosingClass.getPackage().getName();
		this.isStatic = isStatic;
		this.isConstructor = isConstructor;
	}

	public void acceptLambda(LambdaExpression lambda, ITypeBinding expectedResult) {
		acceptFunctionalExpression(lambda, expectedResult);
	}

	public void acceptMethodReference(MethodReference node, ITypeBinding expectedResult) {
		acceptFunctionalExpression(node, expectedResult);
	}

	public void acceptAnonymousClass(ClassInstanceCreation node, ITypeBinding expectedResult) {
		acceptFunctionalExpression(node, expectedResult);
	}


	private void acceptFunctionalExpression(Expression node, ITypeBinding expectedResult) {
		FunctionalEvalVisitor visitor = new FunctionalEvalVisitor();
		node.accept(visitor);
		String castExpression = "(" + expectedResult.getQualifiedName() + ")"; //$NON-NLS-1$//$NON-NLS-2$
		this.snippet = castExpression + "(" + visitor.buffer.toString() + ")"; //$NON-NLS-1$//$NON-NLS-2$
	}

	public String getSnippet() {
		return snippet;
	}

	private static Object EVALUATE_CODE_SNIPPET_LOCK = new Object();

	public RemoteEvaluator build() throws JavaModelException, DebugException {

		List<String> boundVariableNames = getVariableNames();
		List<String> boundVariableTypeNames = getVariableTypeNames();

		List<String> errors = new ArrayList<>();
		IType enclosingType = this.javaProject.findType(enclosingTypeName);

		if (enclosingType == null) {
			throw new DebugException(new Status(IStatus.ERROR, JDIDebugPlugin.getUniqueIdentifier(), EvaluationEngineMessages.ASTInstructionCompiler_Functional_expressions_cannot_be_evaluated_inside_local_and_or_anonymous_classes));
		}

		synchronized (EVALUATE_CODE_SNIPPET_LOCK) {
			IEvaluationContext context = this.javaProject.newEvaluationContext();
			if (!packageName.startsWith("java.")) { //$NON-NLS-1$
				context.setPackageName(this.packageName);
			}
			// System.out.println(this.snippet);
			context.evaluateCodeSnippet(this.snippet, boundVariableTypeNames.toArray(new String[boundVariableNames.size()]), boundVariableNames.toArray(new String[boundVariableNames.size()]), new int[boundVariableNames.size()], enclosingType, isStatic, isConstructor, new ICodeSnippetRequestor() {

				@Override
				public void acceptProblem(IMarker problemMarker, String fragmentSource, int fragmentKind) {
					if (problemMarker.getAttribute(IMarker.SEVERITY, IMarker.SEVERITY_INFO) >= IMarker.SEVERITY_ERROR) {
						errors.add(toString(problemMarker));
					}
				}

				private String toString(IMarker marker) {
					return marker.getAttribute(IMarker.MESSAGE, ""); //$NON-NLS-1$
				}

				@Override
				public boolean acceptClassFiles(byte[][] classFileBytes, String[][] classFileCompoundNames, String mainCodeSnippetClassName) {
					for (int i = 0; i < classFileCompoundNames.length; ++i) {
						String className = makeClassName(classFileCompoundNames[i]);
						classFiles.put(className, classFileBytes[i]);
					}
					if (mainCodeSnippetClassName != null) {
						setCodeSnippetClassName(mainCodeSnippetClassName);
					}
					return true;
				}
			}, null);
		}

		if (!errors.isEmpty()) {
			throw new DebugException(new Status(IStatus.ERROR, JDIDebugPlugin.getUniqueIdentifier(), errors.toString()));
		}

		return new RemoteEvaluator(classFiles, codeSnippetClassName, getVariableNames(), enclosingType.getFullyQualifiedName('$'));
	}

	private void setCodeSnippetClassName(String codeSnippetClassName) {
		this.codeSnippetClassName = codeSnippetClassName;
	}

	private static String makeClassName(String[] names) {
		return String.join(String.valueOf('/'), names);
	}

	public List<String> getVariableTypeNames() {
		return Collections.unmodifiableList(argumentTypeNames);
	}

	public List<String> getVariableNames() {
		return Collections.unmodifiableList(argumentNames);
	}

	public String allocateNewVariable(ITypeBinding binding, String hint) {
		String varName = hint + "$" + argumentNames.size(); //$NON-NLS-1$
		argumentNames.add(varName);
		argumentTypeNames.add(binding.getQualifiedName());
		return varName;
	}

	public IJavaProject getJavaProject() {
		return javaProject;
	}

	public String getEnclosingTypeName() {
		return enclosingTypeName;
	}

	public boolean isStatic() {
		return isStatic;
	}

	/**
	 * Internal synonym for {@link MethodDeclaration#getReturnType()}. Use to alleviate deprecation warnings.
	 *
	 * @deprecated
	 * @since 3.4
	 */
	@Deprecated
	private static Type getReturnType(MethodDeclaration node) {
		return node.getReturnType();
	}

	/**
	 * Internal synonym for {@link TypeDeclaration#getSuperclass()}. Use to alleviate deprecation warnings.
	 *
	 * @deprecated
	 * @since 3.4
	 */
	@Deprecated
	private static Name getSuperclass(TypeDeclaration node) {
		return node.getSuperclass();
	}

	/**
	 * Internal synonym for {@link TypeDeclarationStatement#getTypeDeclaration()}. Use to alleviate deprecation warnings.
	 *
	 * @deprecated
	 * @since 3.4
	 */
	@Deprecated
	private static TypeDeclaration getTypeDeclaration(TypeDeclarationStatement node) {
		return node.getTypeDeclaration();
	}

	/**
	 * Internal synonym for {@link MethodDeclaration#thrownExceptions()}. Use to alleviate deprecation warnings.
	 *
	 * @deprecated
	 * @since 3.10
	 */
	@Deprecated
	private static List<?> thrownExceptions(MethodDeclaration node) {
		return node.thrownExceptions();
	}

	private class FunctionalEvalVisitor extends ASTVisitor {

		/**
		 * The string buffer into which the serialized representation of the AST is written.
		 */
		protected StringBuilder buffer = new StringBuilder();

		private int indent = 2;

		private final Map<IBinding, String> localBindings = new HashMap<>();

		public FunctionalEvalVisitor() {
		}

		boolean isLocalBinding(IBinding binding) {
			return localBindings.containsKey(binding);
		}

		private boolean isParentInLocalBinding(ASTNode parent) {
			if (parent instanceof QualifiedName) {
				// this will avoid unwanted upward traversals
				if (isLocalBinding(((QualifiedName) parent).getQualifier().resolveBinding())) {
					return true;
				}
				// traverse upstream to see if a parent is already handled
				return isParentInLocalBinding(parent.getParent());
			}
			return false;
		}

		void addLocalBinding(IBinding binding, String name) {
			localBindings.put(binding, name);
		}

		void printIndent() {
			for (int i = 0; i < indent; i++) {
				buffer.append("  ");//$NON-NLS-1$
			}
		}

		/**
		 * Internal synonym for {@link AST#JLS2}. Use to alleviate deprecation warnings.
		 *
		 * @deprecated
		 */
		@Deprecated
		private static final int JLS2 = AST.JLS2;

		/**
		 * Internal synonym for {@link AST#JLS3}. Use to alleviate deprecation warnings.
		 *
		 * @deprecated
		 */
		@Deprecated
		private static final int JLS3 = AST.JLS3;

		/**
		 * Internal synonym for {@link AST#JLS4}. Use to alleviate deprecation warnings.
		 *
		 * @deprecated
		 */
		@Deprecated
		private static final int JLS4 = AST.JLS4;

		/**
		 * Internal synonym for {@link AST#JLS8}. Use to alleviate deprecation warnings.
		 *
		 * @deprecated
		 */
		@Deprecated
		private static final int JLS8 = AST.JLS8;

		/**
		 * Internal synonym for {@link AST#JLS9}. Use to alleviate deprecation warnings.
		 *
		 * @deprecated
		 */
		@Deprecated
		private static final int JLS9 = AST.JLS9;

		/**
		 * Internal synonym for {@link ClassInstanceCreation#getName()}. Use to alleviate deprecation warnings.
		 *
		 * @deprecated
		 */
		@Deprecated
		private Name getName(ClassInstanceCreation node) {
			return node.getName();
		}

		/**
		 * Appends the text representation of the given modifier flags, followed by a single space. Used for JLS2 modifiers.
		 *
		 * @param modifiers
		 *            the modifier flags
		 */
		void printModifiers(int modifiers) {
			if (Modifier.isPublic(modifiers)) {
				buffer.append("public ");//$NON-NLS-1$
			}
			if (Modifier.isProtected(modifiers)) {
				buffer.append("protected ");//$NON-NLS-1$
			}
			if (Modifier.isPrivate(modifiers)) {
				buffer.append("private ");//$NON-NLS-1$
			}
			if (Modifier.isStatic(modifiers)) {
				buffer.append("static ");//$NON-NLS-1$
			}
			if (Modifier.isAbstract(modifiers)) {
				buffer.append("abstract ");//$NON-NLS-1$
			}
			if (Modifier.isFinal(modifiers)) {
				buffer.append("final ");//$NON-NLS-1$
			}
			if (Modifier.isSynchronized(modifiers)) {
				buffer.append("synchronized ");//$NON-NLS-1$
			}
			if (Modifier.isVolatile(modifiers)) {
				buffer.append("volatile ");//$NON-NLS-1$
			}
			if (Modifier.isNative(modifiers)) {
				buffer.append("native ");//$NON-NLS-1$
			}
			if (Modifier.isStrictfp(modifiers)) {
				buffer.append("strictfp ");//$NON-NLS-1$
			}
			if (Modifier.isTransient(modifiers)) {
				buffer.append("transient ");//$NON-NLS-1$
			}
		}

		/**
		 * Appends the text representation of the given modifier flags, followed by a single space. Used for 3.0 modifiers and annotations.
		 *
		 * @param ext
		 *            the list of modifier and annotation nodes (element type: <code>IExtendedModifiers</code>)
		 */
		void printModifiers(List ext) {
			for (Iterator it = ext.iterator(); it.hasNext();) {
				ASTNode p = (ASTNode) it.next();
				p.accept(this);
				buffer.append(" ");//$NON-NLS-1$
			}
		}

		private void printTypes(List<Type> types, String prefix) {
			if (types.size() > 0) {
				buffer.append(" " + prefix + " ");//$NON-NLS-1$ //$NON-NLS-2$
				Type type = types.get(0);
				type.accept(this);
				for (int i = 1, l = types.size(); i < l; ++i) {
					buffer.append(","); //$NON-NLS-1$
					type = types.get(0);
					type.accept(this);
				}
			}
		}

		/**
		 * reference node helper function that is common to all the difference reference nodes.
		 *
		 * @param typeArguments
		 *            list of type arguments
		 */
		private void visitReferenceTypeArguments(List typeArguments) {
			buffer.append("::");//$NON-NLS-1$
			if (!typeArguments.isEmpty()) {
				buffer.append('<');
				for (Iterator it = typeArguments.iterator(); it.hasNext();) {
					Type t = (Type) it.next();
					t.accept(this);
					if (it.hasNext()) {
						buffer.append(',');
					}
				}
				buffer.append('>');
			}
		}

		private void visitTypeAnnotations(AnnotatableType node) {
			if (node.getAST().apiLevel() >= JLS8) {
				visitAnnotationsList(node.annotations());
			}
		}

		private void visitAnnotationsList(List annotations) {
			for (Iterator it = annotations.iterator(); it.hasNext();) {
				Annotation annotation = (Annotation) it.next();
				annotation.accept(this);
				buffer.append(' ');
			}
		}

		/**
		 * Internal synonym for {@link TypeDeclaration#superInterfaces()}. Use to alleviate deprecation warnings.
		 *
		 * @deprecated
		 * @since 3.4
		 */
		@Deprecated
		private List superInterfaces(TypeDeclaration node) {
			return node.superInterfaces();
		}

		@Override
		public boolean visit(AnnotationTypeDeclaration node) {
			if (node.getJavadoc() != null) {
				node.getJavadoc().accept(this);
			}
			printIndent();
			printModifiers(node.modifiers());
			buffer.append("@interface ");//$NON-NLS-1$
			node.getName().accept(this);
			buffer.append(" {");//$NON-NLS-1$
			for (Iterator it = node.bodyDeclarations().iterator(); it.hasNext();) {
				BodyDeclaration d = (BodyDeclaration) it.next();
				d.accept(this);
			}
			buffer.append("}\n");//$NON-NLS-1$
			return false;
		}

		@Override
		public boolean visit(AnnotationTypeMemberDeclaration node) {
			if (node.getJavadoc() != null) {
				node.getJavadoc().accept(this);
			}
			printIndent();
			printModifiers(node.modifiers());
			node.getType().accept(this);
			buffer.append(" ");//$NON-NLS-1$
			node.getName().accept(this);
			buffer.append("()");//$NON-NLS-1$
			if (node.getDefault() != null) {
				buffer.append(" default ");//$NON-NLS-1$
				node.getDefault().accept(this);
			}
			buffer.append(";\n");//$NON-NLS-1$
			return false;
		}

		@Override
		public boolean visit(AnonymousClassDeclaration node) {
			buffer.append("{\n");//$NON-NLS-1$
			indent++;
			for (Iterator it = node.bodyDeclarations().iterator(); it.hasNext();) {
				BodyDeclaration b = (BodyDeclaration) it.next();
				b.accept(this);
			}
			indent--;
			printIndent();
			buffer.append("}\n");//$NON-NLS-1$
			return false;
		}

		@Override
		public boolean visit(ArrayAccess node) {
			node.getArray().accept(this);
			buffer.append("[");//$NON-NLS-1$
			node.getIndex().accept(this);
			buffer.append("]");//$NON-NLS-1$
			return false;
		}

		@Override
		public boolean visit(ArrayCreation node) {
			buffer.append("new ");//$NON-NLS-1$
			ArrayType at = node.getType();
			int dims = at.getDimensions();
			Type elementType = at.getElementType();
			elementType.accept(this);
			for (Iterator it = node.dimensions().iterator(); it.hasNext();) {
				buffer.append("[");//$NON-NLS-1$
				Expression e = (Expression) it.next();
				e.accept(this);
				buffer.append("]");//$NON-NLS-1$
				dims--;
			}
			// add empty "[]" for each extra array dimension
			for (int i = 0; i < dims; i++) {
				buffer.append("[]");//$NON-NLS-1$
			}
			if (node.getInitializer() != null) {
				node.getInitializer().accept(this);
			}
			return false;
		}

		@Override
		public boolean visit(ArrayInitializer node) {
			buffer.append("{");//$NON-NLS-1$
			for (Iterator it = node.expressions().iterator(); it.hasNext();) {
				Expression e = (Expression) it.next();
				e.accept(this);
				if (it.hasNext()) {
					buffer.append(",");//$NON-NLS-1$
				}
			}
			buffer.append("}");//$NON-NLS-1$
			return false;
		}

		@Override
		public boolean visit(ArrayType node) {
			if (node.getAST().apiLevel() < JLS8) {
				visitComponentType(node);
				buffer.append("[]");//$NON-NLS-1$
			} else {
				node.getElementType().accept(this);
				List dimensions = node.dimensions();
				int size = dimensions.size();
				for (int i = 0; i < size; i++) {
					Dimension aDimension = (Dimension) dimensions.get(i);
					aDimension.accept(this);
				}
			}
			return false;
		}

		@Override
		public boolean visit(AssertStatement node) {
			printIndent();
			buffer.append("assert ");//$NON-NLS-1$
			node.getExpression().accept(this);
			if (node.getMessage() != null) {
				buffer.append(" : ");//$NON-NLS-1$
				node.getMessage().accept(this);
			}
			buffer.append(";\n");//$NON-NLS-1$
			return false;
		}

		@Override
		public boolean visit(Assignment node) {
			node.getLeftHandSide().accept(this);
			buffer.append(node.getOperator().toString());
			node.getRightHandSide().accept(this);
			return false;
		}

		@Override
		public boolean visit(Block node) {
			buffer.append("{\n");//$NON-NLS-1$
			indent++;
			for (Iterator it = node.statements().iterator(); it.hasNext();) {
				Statement s = (Statement) it.next();
				s.accept(this);
			}
			indent--;
			printIndent();
			buffer.append("}\n");//$NON-NLS-1$
			return false;
		}

		@Override
		public boolean visit(BlockComment node) {
			printIndent();
			buffer.append("/* */");//$NON-NLS-1$
			return false;
		}

		@Override
		public boolean visit(BooleanLiteral node) {
			if (node.booleanValue() == true) {
				buffer.append("true");//$NON-NLS-1$
			} else {
				buffer.append("false");//$NON-NLS-1$
			}
			return false;
		}

		@Override
		public boolean visit(BreakStatement node) {
			printIndent();
			buffer.append("break");//$NON-NLS-1$
			if (node.getLabel() != null) {
				buffer.append(" ");//$NON-NLS-1$
				node.getLabel().accept(this);
			}
			buffer.append(";\n");//$NON-NLS-1$
			return false;
		}

		@Override
		public boolean visit(CastExpression node) {
			buffer.append("(");//$NON-NLS-1$
			node.getType().accept(this);
			buffer.append(")");//$NON-NLS-1$
			node.getExpression().accept(this);
			return false;
		}

		@Override
		public boolean visit(CatchClause node) {
			buffer.append("catch (");//$NON-NLS-1$
			node.getException().accept(this);
			buffer.append(") ");//$NON-NLS-1$
			node.getBody().accept(this);
			return false;
		}

		@Override
		public boolean visit(CharacterLiteral node) {
			buffer.append(node.getEscapedValue());
			return false;
		}

		@Override
		public boolean visit(ClassInstanceCreation node) {
			if (node.getExpression() != null) {
				node.getExpression().accept(this);
				buffer.append(".");//$NON-NLS-1$
			}
			buffer.append("new ");//$NON-NLS-1$
			if (node.getAST().apiLevel() == JLS2) {
				getName(node).accept(this);
			}
			if (node.getAST().apiLevel() >= JLS3) {
				if (!node.typeArguments().isEmpty()) {
					buffer.append("<");//$NON-NLS-1$
					for (Iterator it = node.typeArguments().iterator(); it.hasNext();) {
						Type t = (Type) it.next();
						t.accept(this);
						if (it.hasNext()) {
							buffer.append(",");//$NON-NLS-1$
						}
					}
					buffer.append(">");//$NON-NLS-1$
				}
				node.getType().accept(this);
			}
			buffer.append("(");//$NON-NLS-1$
			for (Iterator it = node.arguments().iterator(); it.hasNext();) {
				Expression e = (Expression) it.next();
				e.accept(this);
				if (it.hasNext()) {
					buffer.append(",");//$NON-NLS-1$
				}
			}
			buffer.append(")");//$NON-NLS-1$
			if (node.getAnonymousClassDeclaration() != null) {
				node.getAnonymousClassDeclaration().accept(this);
			}
			return false;
		}

		@Override
		public boolean visit(CompilationUnit node) {
			if (node.getAST().apiLevel() >= JLS9) {
				if (node.getModule() != null) {
					node.getModule().accept(this);
				}
			}
			if (node.getPackage() != null) {
				node.getPackage().accept(this);
			}
			for (Iterator it = node.imports().iterator(); it.hasNext();) {
				ImportDeclaration d = (ImportDeclaration) it.next();
				d.accept(this);
			}
			for (Iterator it = node.types().iterator(); it.hasNext();) {
				AbstractTypeDeclaration d = (AbstractTypeDeclaration) it.next();
				d.accept(this);
			}
			return false;
		}

		@Override
		public boolean visit(ConditionalExpression node) {
			node.getExpression().accept(this);
			buffer.append(" ? ");//$NON-NLS-1$
			node.getThenExpression().accept(this);
			buffer.append(" : ");//$NON-NLS-1$
			node.getElseExpression().accept(this);
			return false;
		}

		@Override
		public boolean visit(ConstructorInvocation node) {
			printIndent();
			if (node.getAST().apiLevel() >= JLS3) {
				if (!node.typeArguments().isEmpty()) {
					buffer.append("<");//$NON-NLS-1$
					for (Iterator it = node.typeArguments().iterator(); it.hasNext();) {
						Type t = (Type) it.next();
						t.accept(this);
						if (it.hasNext()) {
							buffer.append(",");//$NON-NLS-1$
						}
					}
					buffer.append(">");//$NON-NLS-1$
				}
			}
			buffer.append("this(");//$NON-NLS-1$
			for (Iterator it = node.arguments().iterator(); it.hasNext();) {
				Expression e = (Expression) it.next();
				e.accept(this);
				if (it.hasNext()) {
					buffer.append(",");//$NON-NLS-1$
				}
			}
			buffer.append(");\n");//$NON-NLS-1$
			return false;
		}

		@Override
		public boolean visit(ContinueStatement node) {
			printIndent();
			buffer.append("continue");//$NON-NLS-1$
			if (node.getLabel() != null) {
				buffer.append(" ");//$NON-NLS-1$
				node.getLabel().accept(this);
			}
			buffer.append(";\n");//$NON-NLS-1$
			return false;
		}

		@Override
		public boolean visit(CreationReference node) {
			node.getType().accept(this);
			visitReferenceTypeArguments(node.typeArguments());
			buffer.append("new");//$NON-NLS-1$
			return false;
		}

		@Override
		public boolean visit(Dimension node) {
			List annotations = node.annotations();
			if (annotations.size() > 0) {
				buffer.append(' ');
			}
			visitAnnotationsList(annotations);
			buffer.append("[]"); //$NON-NLS-1$
			return false;
		}

		@Override
		public boolean visit(DoStatement node) {
			printIndent();
			buffer.append("do ");//$NON-NLS-1$
			node.getBody().accept(this);
			buffer.append(" while (");//$NON-NLS-1$
			node.getExpression().accept(this);
			buffer.append(");\n");//$NON-NLS-1$
			return false;
		}

		@Override
		public boolean visit(EmptyStatement node) {
			printIndent();
			buffer.append(";\n");//$NON-NLS-1$
			return false;
		}

		@Override
		public boolean visit(EnhancedForStatement node) {
			printIndent();
			buffer.append("for (");//$NON-NLS-1$
			node.getParameter().accept(this);
			buffer.append(" : ");//$NON-NLS-1$
			node.getExpression().accept(this);
			buffer.append(") ");//$NON-NLS-1$
			node.getBody().accept(this);
			return false;
		}

		@Override
		public boolean visit(EnumConstantDeclaration node) {
			if (node.getJavadoc() != null) {
				node.getJavadoc().accept(this);
			}
			printIndent();
			printModifiers(node.modifiers());
			node.getName().accept(this);
			if (!node.arguments().isEmpty()) {
				buffer.append("(");//$NON-NLS-1$
				for (Iterator it = node.arguments().iterator(); it.hasNext();) {
					Expression e = (Expression) it.next();
					e.accept(this);
					if (it.hasNext()) {
						buffer.append(",");//$NON-NLS-1$
					}
				}
				buffer.append(")");//$NON-NLS-1$
			}
			if (node.getAnonymousClassDeclaration() != null) {
				node.getAnonymousClassDeclaration().accept(this);
			}
			return false;
		}

		@Override
		public boolean visit(EnumDeclaration node) {
			if (node.getJavadoc() != null) {
				node.getJavadoc().accept(this);
			}
			printIndent();
			printModifiers(node.modifiers());
			buffer.append("enum ");//$NON-NLS-1$
			node.getName().accept(this);
			buffer.append(" ");//$NON-NLS-1$
			if (!node.superInterfaceTypes().isEmpty()) {
				buffer.append("implements ");//$NON-NLS-1$
				for (Iterator it = node.superInterfaceTypes().iterator(); it.hasNext();) {
					Type t = (Type) it.next();
					t.accept(this);
					if (it.hasNext()) {
						buffer.append(", ");//$NON-NLS-1$
					}
				}
				buffer.append(" ");//$NON-NLS-1$
			}
			buffer.append("{");//$NON-NLS-1$
			for (Iterator it = node.enumConstants().iterator(); it.hasNext();) {
				EnumConstantDeclaration d = (EnumConstantDeclaration) it.next();
				d.accept(this);
				// enum constant declarations do not include punctuation
				if (it.hasNext()) {
					// enum constant declarations are separated by commas
					buffer.append(", ");//$NON-NLS-1$
				}
			}
			if (!node.bodyDeclarations().isEmpty()) {
				buffer.append("; ");//$NON-NLS-1$
				for (Iterator it = node.bodyDeclarations().iterator(); it.hasNext();) {
					BodyDeclaration d = (BodyDeclaration) it.next();
					d.accept(this);
					// other body declarations include trailing punctuation
				}
			}
			buffer.append("}\n");//$NON-NLS-1$
			return false;
		}

		@Override
		public boolean visit(ExportsDirective node) {
			return visit(node, "exports"); //$NON-NLS-1$
		}

		@Override
		public boolean visit(ExpressionMethodReference node) {
			node.getExpression().accept(this);
			visitReferenceTypeArguments(node.typeArguments());
			node.getName().accept(this);
			return false;
		}

		@Override
		public boolean visit(ExpressionStatement node) {
			printIndent();
			node.getExpression().accept(this);
			buffer.append(";\n");//$NON-NLS-1$
			return false;
		}

		@Override
		public boolean visit(FieldAccess node) {
			/* TODO: Make tricks here when we access fields where we have non-public access */
			ITypeBinding instanceType = node.getExpression().resolveTypeBinding();
			if (instanceType.isAssignmentCompatible(RemoteEvaluatorBuilder.this.enclosingClass)) {
				node.getExpression().accept(this);
				buffer.append(".");//$NON-NLS-1$ */
				buffer.append(node.getName().getIdentifier());
				return false;
			}
			node.getExpression().accept(this);
			buffer.append(".");//$NON-NLS-1$
			node.getName().accept(this);
			return false;
		}

		@Override
		public boolean visit(FieldDeclaration node) {
			if (node.getJavadoc() != null) {
				node.getJavadoc().accept(this);
			}
			printIndent();
			if (node.getAST().apiLevel() == JLS2) {
				printModifiers(node.getModifiers());
			}
			if (node.getAST().apiLevel() >= JLS3) {
				printModifiers(node.modifiers());
			}
			node.getType().accept(this);
			buffer.append(" ");//$NON-NLS-1$
			for (Iterator it = node.fragments().iterator(); it.hasNext();) {
				VariableDeclarationFragment f = (VariableDeclarationFragment) it.next();
				f.accept(this);
				if (it.hasNext()) {
					buffer.append(", ");//$NON-NLS-1$
				}
			}
			buffer.append(";\n");//$NON-NLS-1$
			return false;
		}

		@Override
		public boolean visit(ForStatement node) {
			printIndent();
			buffer.append("for (");//$NON-NLS-1$
			for (Iterator it = node.initializers().iterator(); it.hasNext();) {
				Expression e = (Expression) it.next();
				e.accept(this);
				if (it.hasNext()) {
					buffer.append(", ");//$NON-NLS-1$
				}
			}
			buffer.append("; ");//$NON-NLS-1$
			if (node.getExpression() != null) {
				node.getExpression().accept(this);
			}
			buffer.append("; ");//$NON-NLS-1$
			for (Iterator it = node.updaters().iterator(); it.hasNext();) {
				Expression e = (Expression) it.next();
				e.accept(this);
				if (it.hasNext()) {
					buffer.append(", ");//$NON-NLS-1$
				}
			}
			buffer.append(") ");//$NON-NLS-1$
			node.getBody().accept(this);
			return false;
		}

		@Override
		public boolean visit(IfStatement node) {
			printIndent();
			buffer.append("if (");//$NON-NLS-1$
			node.getExpression().accept(this);
			buffer.append(") ");//$NON-NLS-1$
			node.getThenStatement().accept(this);
			if (node.getElseStatement() != null) {
				buffer.append(" else ");//$NON-NLS-1$
				node.getElseStatement().accept(this);
			}
			return false;
		}

		@Override
		public boolean visit(ImportDeclaration node) {
			printIndent();
			buffer.append("import ");//$NON-NLS-1$
			if (node.getAST().apiLevel() >= JLS3) {
				if (node.isStatic()) {
					buffer.append("static ");//$NON-NLS-1$
				}
			}
			node.getName().accept(this);
			if (node.isOnDemand()) {
				buffer.append(".*");//$NON-NLS-1$
			}
			buffer.append(";\n");//$NON-NLS-1$
			return false;
		}

		@Override
		public boolean visit(InfixExpression node) {
			node.getLeftOperand().accept(this);
			buffer.append(' '); // for cases like x= i - -1; or x= i++ + ++i;
			buffer.append(node.getOperator().toString());
			buffer.append(' ');
			node.getRightOperand().accept(this);
			final List extendedOperands = node.extendedOperands();
			if (extendedOperands.size() != 0) {
				buffer.append(' ');
				for (Iterator it = extendedOperands.iterator(); it.hasNext();) {
					buffer.append(node.getOperator().toString()).append(' ');
					Expression e = (Expression) it.next();
					e.accept(this);
				}
			}
			return false;
		}

		@Override
		public boolean visit(Initializer node) {
			if (node.getJavadoc() != null) {
				node.getJavadoc().accept(this);
			}
			if (node.getAST().apiLevel() == JLS2) {
				printModifiers(node.getModifiers());
			}
			if (node.getAST().apiLevel() >= JLS3) {
				printModifiers(node.modifiers());
			}
			node.getBody().accept(this);
			return false;
		}

		@Override
		public boolean visit(InstanceofExpression node) {
			node.getLeftOperand().accept(this);
			buffer.append(" instanceof ");//$NON-NLS-1$
			node.getRightOperand().accept(this);
			return false;
		}

		@Override
		public boolean visit(IntersectionType node) {
			for (Iterator it = node.types().iterator(); it.hasNext();) {
				Type t = (Type) it.next();
				t.accept(this);
				if (it.hasNext()) {
					buffer.append(" & "); //$NON-NLS-1$
				}
			}
			return false;
		}

		@Override
		public boolean visit(Javadoc node) {
			printIndent();
			buffer.append("/** ");//$NON-NLS-1$
			for (Iterator it = node.tags().iterator(); it.hasNext();) {
				ASTNode e = (ASTNode) it.next();
				e.accept(this);
			}
			buffer.append("\n */\n");//$NON-NLS-1$
			return false;
		}

		@Override
		public boolean visit(LabeledStatement node) {
			printIndent();
			node.getLabel().accept(this);
			buffer.append(": ");//$NON-NLS-1$
			node.getBody().accept(this);
			return false;
		}

		@Override
		public boolean visit(LambdaExpression node) {
			boolean hasParentheses = node.hasParentheses();
			if (hasParentheses) {
				buffer.append('(');
			}
			for (Iterator it = node.parameters().iterator(); it.hasNext();) {
				VariableDeclaration v = (VariableDeclaration) it.next();
				v.accept(this);
				if (it.hasNext()) {
					buffer.append(",");//$NON-NLS-1$
				}
			}
			if (hasParentheses) {
				buffer.append(')');
			}
			buffer.append(" -> "); //$NON-NLS-1$
			node.getBody().accept(this);
			return false;
		}

		@Override
		public boolean visit(LineComment node) {
			buffer.append("//\n");//$NON-NLS-1$
			return false;
		}

		@Override
		public boolean visit(MarkerAnnotation node) {
			buffer.append("@");//$NON-NLS-1$
			node.getTypeName().accept(this);
			return false;
		}

		@Override
		public boolean visit(MemberRef node) {
			if (node.getQualifier() != null) {
				node.getQualifier().accept(this);
			}
			buffer.append("#");//$NON-NLS-1$
			node.getName().accept(this);
			return false;
		}

		@Override
		public boolean visit(MemberValuePair node) {
			node.getName().accept(this);
			buffer.append("=");//$NON-NLS-1$
			node.getValue().accept(this);
			return false;
		}

		@Override
		public boolean visit(MethodDeclaration node) {
			if (node.getJavadoc() != null) {
				node.getJavadoc().accept(this);
			}
			printIndent();
			if (node.getAST().apiLevel() == JLS2) {
				printModifiers(node.getModifiers());
			}
			if (node.getAST().apiLevel() >= JLS3) {
				printModifiers(node.modifiers());
				if (!node.typeParameters().isEmpty()) {
					buffer.append("<");//$NON-NLS-1$
					for (Iterator it = node.typeParameters().iterator(); it.hasNext();) {
						TypeParameter t = (TypeParameter) it.next();
						t.accept(this);
						if (it.hasNext()) {
							buffer.append(",");//$NON-NLS-1$
						}
					}
					buffer.append(">");//$NON-NLS-1$
				}
			}
			if (!node.isConstructor()) {
				if (node.getAST().apiLevel() == JLS2) {
					getReturnType(node).accept(this);
				} else {
					if (node.getReturnType2() != null) {
						node.getReturnType2().accept(this);
					} else {
						// methods really ought to have a return type
						buffer.append("void");//$NON-NLS-1$
					}
				}
				buffer.append(" ");//$NON-NLS-1$
			}
			node.getName().accept(this);
			buffer.append("(");//$NON-NLS-1$
			if (node.getAST().apiLevel() >= JLS8) {
				Type receiverType = node.getReceiverType();
				if (receiverType != null) {
					receiverType.accept(this);
					buffer.append(' ');
					SimpleName qualifier = node.getReceiverQualifier();
					if (qualifier != null) {
						qualifier.accept(this);
						buffer.append('.');
					}
					buffer.append("this"); //$NON-NLS-1$
					if (node.parameters().size() > 0) {
						buffer.append(',');
					}
				}
			}
			for (Iterator it = node.parameters().iterator(); it.hasNext();) {
				SingleVariableDeclaration v = (SingleVariableDeclaration) it.next();
				v.accept(this);
				if (it.hasNext()) {
					buffer.append(",");//$NON-NLS-1$
				}
			}
			buffer.append(")");//$NON-NLS-1$
			int size = node.getExtraDimensions();
			if (node.getAST().apiLevel() >= JLS8) {
				List dimensions = node.extraDimensions();
				for (int i = 0; i < size; i++) {
					visit((Dimension) dimensions.get(i));
				}
			} else {
				for (int i = 0; i < size; i++) {
					buffer.append("[]"); //$NON-NLS-1$
				}
			}
			if (node.getAST().apiLevel() < JLS8) {
				if (!thrownExceptions(node).isEmpty()) {
					buffer.append(" throws ");//$NON-NLS-1$
					for (Iterator it = thrownExceptions(node).iterator(); it.hasNext();) {
						Name n = (Name) it.next();
						n.accept(this);
						if (it.hasNext()) {
							buffer.append(", ");//$NON-NLS-1$
						}
					}
					buffer.append(" ");//$NON-NLS-1$
				}
			} else {
				if (!node.thrownExceptionTypes().isEmpty()) {
					buffer.append(" throws ");//$NON-NLS-1$
					for (Iterator it = node.thrownExceptionTypes().iterator(); it.hasNext();) {
						Type n = (Type) it.next();
						n.accept(this);
						if (it.hasNext()) {
							buffer.append(", ");//$NON-NLS-1$
						}
					}
					buffer.append(" ");//$NON-NLS-1$
				}
			}
			if (node.getBody() == null) {
				buffer.append(";\n");//$NON-NLS-1$
			} else {
				node.getBody().accept(this);
			}
			return false;
		}

		@Override
		public boolean visit(MethodInvocation node) {
			if (node.getExpression() != null) {
				node.getExpression().accept(this);
				buffer.append(".");//$NON-NLS-1$
			} else {
				String newVarName = new String(LOCAL_VAR_PREFIX) + allocateNewVariable(node.resolveMethodBinding().getDeclaringClass(), "this"); //$NON-NLS-1$
				binder.bindThis(RemoteEvaluatorBuilder.this.enclosingClass, newVarName);
				// buffer.append("this."); //$NON-NLS-1$
				buffer.append(newVarName);
				buffer.append(".");//$NON-NLS-1$
			}
			if (node.getAST().apiLevel() >= JLS3) {
				if (!node.typeArguments().isEmpty()) {
					buffer.append("<");//$NON-NLS-1$
					for (Iterator it = node.typeArguments().iterator(); it.hasNext();) {
						Type t = (Type) it.next();
						t.accept(this);
						if (it.hasNext()) {
							buffer.append(",");//$NON-NLS-1$
						}
					}
					buffer.append(">");//$NON-NLS-1$
				}
			}
			node.getName().accept(this);
			buffer.append("(");//$NON-NLS-1$
			for (Iterator it = node.arguments().iterator(); it.hasNext();) {
				Expression e = (Expression) it.next();
				e.accept(this);
				if (it.hasNext()) {
					buffer.append(",");//$NON-NLS-1$
				}
			}
			buffer.append(")");//$NON-NLS-1$
			return false;
		}

		@Override
		public boolean visit(MethodRef node) {
			if (node.getQualifier() != null) {
				node.getQualifier().accept(this);
			}
			buffer.append("#");//$NON-NLS-1$
			node.getName().accept(this);
			buffer.append("(");//$NON-NLS-1$
			for (Iterator it = node.parameters().iterator(); it.hasNext();) {
				MethodRefParameter e = (MethodRefParameter) it.next();
				e.accept(this);
				if (it.hasNext()) {
					buffer.append(",");//$NON-NLS-1$
				}
			}
			buffer.append(")");//$NON-NLS-1$
			return false;
		}

		@Override
		public boolean visit(MethodRefParameter node) {
			node.getType().accept(this);
			if (node.getAST().apiLevel() >= JLS3) {
				if (node.isVarargs()) {
					buffer.append("...");//$NON-NLS-1$
				}
			}
			if (node.getName() != null) {
				buffer.append(" ");//$NON-NLS-1$
				node.getName().accept(this);
			}
			return false;
		}

		@Override
		public boolean visit(Modifier node) {
			buffer.append(node.getKeyword().toString());
			return false;
		}

		@Override
		public boolean visit(ModuleDeclaration node) {
			if (node.getJavadoc() != null) {
				node.getJavadoc().accept(this);
			}
			printModifiers(node.annotations());
			if (node.isOpen()) {
				buffer.append("open "); //$NON-NLS-1$
			}
			buffer.append("module"); //$NON-NLS-1$
			buffer.append(" "); //$NON-NLS-1$
			node.getName().accept(this);
			buffer.append(" {\n"); //$NON-NLS-1$
			indent++;
			for (ModuleDirective stmt : (List<ModuleDirective>) node.moduleStatements()) {
				stmt.accept(this);
			}
			indent--;
			buffer.append("}"); //$NON-NLS-1$
			return false;
		}

		@Override
		/*
		 * @see ASTVisitor#visit(ModuleModifier)
		 *
		 * @since 3.14
		 */
		public boolean visit(ModuleModifier node) {
			buffer.append(node.getKeyword().toString());
			return false;
		}

		private boolean visit(ModulePackageAccess node, String keyword) {
			printIndent();
			buffer.append(keyword);
			buffer.append(" ");//$NON-NLS-1$
			node.getName().accept(this);
			printTypes(node.modules(), "to"); //$NON-NLS-1$
			buffer.append(";\n");//$NON-NLS-1$
			return false;
		}

		@Override
		public boolean visit(NameQualifiedType node) {
			node.getQualifier().accept(this);
			buffer.append('.');
			visitTypeAnnotations(node);
			node.getName().accept(this);
			return false;
		}

		@Override
		public boolean visit(NormalAnnotation node) {
			buffer.append("@");//$NON-NLS-1$
			node.getTypeName().accept(this);
			buffer.append("(");//$NON-NLS-1$
			for (Iterator it = node.values().iterator(); it.hasNext();) {
				MemberValuePair p = (MemberValuePair) it.next();
				p.accept(this);
				if (it.hasNext()) {
					buffer.append(",");//$NON-NLS-1$
				}
			}
			buffer.append(")");//$NON-NLS-1$
			return false;
		}

		@Override
		public boolean visit(NullLiteral node) {
			buffer.append("null");//$NON-NLS-1$
			return false;
		}

		@Override
		public boolean visit(NumberLiteral node) {
			buffer.append(node.getToken());
			return false;
		}

		@Override
		public boolean visit(OpensDirective node) {
			return visit(node, "opens"); //$NON-NLS-1$
		}

		@Override
		public boolean visit(PackageDeclaration node) {
			if (node.getAST().apiLevel() >= JLS3) {
				if (node.getJavadoc() != null) {
					node.getJavadoc().accept(this);
				}
				for (Iterator it = node.annotations().iterator(); it.hasNext();) {
					Annotation p = (Annotation) it.next();
					p.accept(this);
					buffer.append(" ");//$NON-NLS-1$
				}
			}
			printIndent();
			buffer.append("package ");//$NON-NLS-1$
			node.getName().accept(this);
			buffer.append(";\n");//$NON-NLS-1$
			return false;
		}

		@Override
		public boolean visit(ParameterizedType node) {
			node.getType().accept(this);
			buffer.append("<");//$NON-NLS-1$
			for (Iterator it = node.typeArguments().iterator(); it.hasNext();) {
				Type t = (Type) it.next();
				t.accept(this);
				if (it.hasNext()) {
					buffer.append(",");//$NON-NLS-1$
				}
			}
			buffer.append(">");//$NON-NLS-1$
			return false;
		}

		@Override
		public boolean visit(ParenthesizedExpression node) {
			buffer.append("(");//$NON-NLS-1$
			node.getExpression().accept(this);
			buffer.append(")");//$NON-NLS-1$
			return false;
		}

		@Override
		public boolean visit(PostfixExpression node) {
			node.getOperand().accept(this);
			buffer.append(node.getOperator().toString());
			return false;
		}

		@Override
		public boolean visit(PrefixExpression node) {
			buffer.append(node.getOperator().toString());
			node.getOperand().accept(this);
			return false;
		}

		@Override
		public boolean visit(PrimitiveType node) {
			visitTypeAnnotations(node);
			buffer.append(node.getPrimitiveTypeCode().toString());
			return false;
		}

		@Override
		public boolean visit(ProvidesDirective node) {
			printIndent();
			buffer.append("provides");//$NON-NLS-1$
			buffer.append(" ");//$NON-NLS-1$
			node.getName().accept(this);
			printTypes(node.implementations(), "with"); //$NON-NLS-1$
			buffer.append(";\n");//$NON-NLS-1$
			return false;
		}

		@Override
		public boolean visit(QualifiedName node) {
			node.getQualifier().accept(this);
			buffer.append(".");//$NON-NLS-1$
			node.getName().accept(this);
			return false;
		}

		@Override
		public boolean visit(QualifiedType node) {
			node.getQualifier().accept(this);
			buffer.append(".");//$NON-NLS-1$
			visitTypeAnnotations(node);
			node.getName().accept(this);
			return false;
		}

		@Override
		public boolean visit(RequiresDirective node) {
			printIndent();
			buffer.append("requires");//$NON-NLS-1$
			buffer.append(" ");//$NON-NLS-1$
			printModifiers(node.modifiers());
			node.getName().accept(this);
			buffer.append(";\n");//$NON-NLS-1$
			return false;
		}

		@Override
		public boolean visit(ReturnStatement node) {
			printIndent();
			buffer.append("return");//$NON-NLS-1$
			if (node.getExpression() != null) {
				buffer.append(" ");//$NON-NLS-1$
				node.getExpression().accept(this);
			}
			buffer.append(";\n");//$NON-NLS-1$
			return false;
		}

		private boolean needToQualify(SimpleName node) {
			if (node.getParent() instanceof QualifiedName || node.getParent() instanceof QualifiedType) {
				return false;
			}
			return true;
		}
		@Override
		public boolean visit(SimpleName node) {
			IBinding binding = node.resolveBinding();
			// when having code like arr.length the length is identified as a field variable. But since the arr is
			// already pushed as a variable we don't need to handle length here. So if we have chained field access like
			// obj.f1.f2 we will only push the obj as a variable.
			if (!isLocalBinding(binding) && !isParentInLocalBinding(node.getParent())) {
				if (binding instanceof IVariableBinding vb) {
					// For future optimization: Check for duplicates, so same value is only bound once
					if (vb.isField()) {
						if (Modifier.isStatic(vb.getModifiers())) {
							if (needToQualify(node)) {
								ITypeBinding declaringClass = vb.getDeclaringClass();
								buffer.append(declaringClass.getQualifiedName());
								buffer.append("."); //$NON-NLS-1$
							}

							buffer.append(node.getIdentifier());

						} else {
							// TODO: Fix this to use same method as visit(FieldAccess)
							ITypeBinding declaringClass = vb.getDeclaringClass();
							String newVarName = allocateNewVariable(declaringClass, LOCAL_VAR_PREFIX.concat("this")); //$NON-NLS-1$
							binder.bindThis(declaringClass, newVarName);
							// buffer.append("this."); //$NON-NLS-1$
							buffer.append(newVarName);
							buffer.append("."); //$NON-NLS-1$
							buffer.append(node.getIdentifier());
						}
					} else {
						String newVarName = new String(LOCAL_VAR_PREFIX) + allocateNewVariable(vb.getType(), node.getIdentifier());
						binder.bind((IVariableBinding) binding, newVarName);
						// buffer.append("this."); //$NON-NLS-1$
						buffer.append(newVarName);
					}
					return false;
				}
			}

			buffer.append(node.getIdentifier());
			return false;
		}

		@Override
		public boolean visit(SimpleType node) {
			visitTypeAnnotations(node);
			node.getName().accept(this);
			return false;
		}

		@Override
		public boolean visit(SingleMemberAnnotation node) {
			buffer.append("@");//$NON-NLS-1$
			node.getTypeName().accept(this);
			buffer.append("(");//$NON-NLS-1$
			node.getValue().accept(this);
			buffer.append(")");//$NON-NLS-1$
			return false;
		}

		@Override
		public boolean visit(SingleVariableDeclaration node) {
			printIndent();
			if (node.getAST().apiLevel() == JLS2) {
				printModifiers(node.getModifiers());
			}
			if (node.getAST().apiLevel() >= JLS3) {
				printModifiers(node.modifiers());
			}
			node.getType().accept(this);
			if (node.getAST().apiLevel() >= JLS3) {
				if (node.isVarargs()) {
					if (node.getAST().apiLevel() >= JLS8) {
						List annotations = node.varargsAnnotations();
						if (annotations.size() > 0) {
							buffer.append(' ');
						}
						visitAnnotationsList(annotations);
					}
					buffer.append("...");//$NON-NLS-1$
				}
			}
			buffer.append(" ");//$NON-NLS-1$
			node.getName().accept(this);
			int size = node.getExtraDimensions();
			if (node.getAST().apiLevel() >= JLS8) {
				List dimensions = node.extraDimensions();
				for (int i = 0; i < size; i++) {
					visit((Dimension) dimensions.get(i));
				}
			} else {
				for (int i = 0; i < size; i++) {
					buffer.append("[]"); //$NON-NLS-1$
				}
			}
			if (node.getInitializer() != null) {
				buffer.append("=");//$NON-NLS-1$
				node.getInitializer().accept(this);
			}
			return false;
		}

		@Override
		public boolean visit(StringLiteral node) {
			buffer.append(node.getEscapedValue());
			return false;
		}

		@Override
		public boolean visit(SuperConstructorInvocation node) {
			printIndent();
			if (node.getExpression() != null) {
				node.getExpression().accept(this);
				buffer.append(".");//$NON-NLS-1$
			}
			if (node.getAST().apiLevel() >= JLS3) {
				if (!node.typeArguments().isEmpty()) {
					buffer.append("<");//$NON-NLS-1$
					for (Iterator it = node.typeArguments().iterator(); it.hasNext();) {
						Type t = (Type) it.next();
						t.accept(this);
						if (it.hasNext()) {
							buffer.append(",");//$NON-NLS-1$
						}
					}
					buffer.append(">");//$NON-NLS-1$
				}
			}
			buffer.append("super(");//$NON-NLS-1$
			for (Iterator it = node.arguments().iterator(); it.hasNext();) {
				Expression e = (Expression) it.next();
				e.accept(this);
				if (it.hasNext()) {
					buffer.append(",");//$NON-NLS-1$
				}
			}
			buffer.append(");\n");//$NON-NLS-1$
			return false;
		}

		@Override
		public boolean visit(SuperFieldAccess node) {
			if (node.getQualifier() != null) {
				node.getQualifier().accept(this);
				buffer.append(".");//$NON-NLS-1$
			}
			buffer.append("super.");//$NON-NLS-1$
			node.getName().accept(this);
			return false;
		}

		@Override
		public boolean visit(SuperMethodInvocation node) {
			if (node.getQualifier() != null) {
				node.getQualifier().accept(this);
				buffer.append(".");//$NON-NLS-1$
			}
			buffer.append("super.");//$NON-NLS-1$
			if (node.getAST().apiLevel() >= JLS3) {
				if (!node.typeArguments().isEmpty()) {
					buffer.append("<");//$NON-NLS-1$
					for (Iterator it = node.typeArguments().iterator(); it.hasNext();) {
						Type t = (Type) it.next();
						t.accept(this);
						if (it.hasNext()) {
							buffer.append(",");//$NON-NLS-1$
						}
					}
					buffer.append(">");//$NON-NLS-1$
				}
			}
			node.getName().accept(this);
			buffer.append("(");//$NON-NLS-1$
			for (Iterator it = node.arguments().iterator(); it.hasNext();) {
				Expression e = (Expression) it.next();
				e.accept(this);
				if (it.hasNext()) {
					buffer.append(",");//$NON-NLS-1$
				}
			}
			buffer.append(")");//$NON-NLS-1$
			return false;
		}

		/*
		 * @see ASTVisitor#visit(SuperMethodReference)
		 *
		 * @since 3.10
		 */
		@Override
		public boolean visit(SuperMethodReference node) {
			if (node.getQualifier() != null) {
				node.getQualifier().accept(this);
				buffer.append('.');
			}
			buffer.append("super");//$NON-NLS-1$
			visitReferenceTypeArguments(node.typeArguments());
			node.getName().accept(this);
			return false;
		}

		@Override
		public boolean visit(SwitchCase node) {
			if ((node.getAST().isPreviewEnabled())) {
				if (node.isDefault()) {
					buffer.append("default");//$NON-NLS-1$
					buffer.append(node.isSwitchLabeledRule() ? " ->" : ":");//$NON-NLS-1$ //$NON-NLS-2$
				} else {
					buffer.append("case ");//$NON-NLS-1$
					for (Iterator it = node.expressions().iterator(); it.hasNext();) {
						Expression t = (Expression) it.next();
						t.accept(this);
						buffer.append(it.hasNext() ? ", " : //$NON-NLS-1$
								node.isSwitchLabeledRule() ? " ->" : ":");//$NON-NLS-1$ //$NON-NLS-2$
					}
				}
			} else {
				if (node.isDefault()) {
					buffer.append("default :\n");//$NON-NLS-1$
				} else {
					buffer.append("case ");//$NON-NLS-1$
					getSwitchExpression(node).accept(this);
					buffer.append(":\n");//$NON-NLS-1$
				}
			}
			indent++; // decremented in visit(SwitchStatement)
			return false;
		}

		/**
		 * @deprecated
		 */
		@Deprecated
		private Expression getSwitchExpression(SwitchCase node) {
			return node.getExpression();
		}

		private void visitSwitchNode(ASTNode node) {
			buffer.append("switch (");//$NON-NLS-1$
			if (node instanceof SwitchExpression) {
				((SwitchExpression) node).getExpression().accept(this);
			} else if (node instanceof SwitchStatement) {
				((SwitchStatement) node).getExpression().accept(this);
			}
			buffer.append(") ");//$NON-NLS-1$
			buffer.append("{\n");//$NON-NLS-1$
			indent++;
			if (node instanceof SwitchExpression) {
				for (Iterator it = ((SwitchExpression) node).statements().iterator(); it.hasNext();) {
					Statement s = (Statement) it.next();
					s.accept(this);
					indent--; // incremented in visit(SwitchCase)
				}
			} else if (node instanceof SwitchStatement) {
				for (Iterator it = ((SwitchStatement) node).statements().iterator(); it.hasNext();) {
					Statement s = (Statement) it.next();
					s.accept(this);
					indent--; // incremented in visit(SwitchCase)
				}
			}
			indent--;
			printIndent();
			buffer.append("}\n");//$NON-NLS-1$

		}

		@Override
		public boolean visit(SwitchExpression node) {
			visitSwitchNode(node);
			return false;
		}

		@Override
		public boolean visit(SwitchStatement node) {
			visitSwitchNode(node);
			return false;
		}

		@Override
		public boolean visit(SynchronizedStatement node) {
			buffer.append("synchronized (");//$NON-NLS-1$
			node.getExpression().accept(this);
			buffer.append(") ");//$NON-NLS-1$
			node.getBody().accept(this);
			return false;
		}

		@Override
		public boolean visit(TagElement node) {
			return false;
		}

		@Override
		public boolean visit(TextBlock node) {
			buffer.append(node.getEscapedValue());
			return false;
		}

		@Override
		public boolean visit(TextElement node) {
			buffer.append(node.getText());
			return false;
		}

		@Override
		public boolean visit(ThisExpression node) {
			ITypeBinding thisType = node.resolveTypeBinding();

			String newVarName = allocateNewVariable(thisType, LOCAL_VAR_PREFIX.concat("this")); //$NON-NLS-1$
			binder.bindThis(thisType, newVarName);
			// buffer.append("this."); //$NON-NLS-1$
			buffer.append(newVarName);
			return false;
		}

		@Override
		public boolean visit(ThrowStatement node) {
			printIndent();
			buffer.append("throw ");//$NON-NLS-1$
			node.getExpression().accept(this);
			buffer.append(";\n");//$NON-NLS-1$
			return false;
		}

		@Override
		public boolean visit(TryStatement node) {
			printIndent();
			buffer.append("try ");//$NON-NLS-1$
			if (node.getAST().apiLevel() >= JLS4) {
				List resources = node.resources();
				if (!resources.isEmpty()) {
					buffer.append('(');
					for (Iterator it = resources.iterator(); it.hasNext();) {
						Expression variable = (Expression) it.next();
						variable.accept(this);
						if (it.hasNext()) {
							buffer.append(';');
						}
					}
					buffer.append(')');
				}
			}
			node.getBody().accept(this);
			buffer.append(" ");//$NON-NLS-1$
			for (Iterator it = node.catchClauses().iterator(); it.hasNext();) {
				CatchClause cc = (CatchClause) it.next();
				cc.accept(this);
			}
			if (node.getFinally() != null) {
				buffer.append(" finally ");//$NON-NLS-1$
				node.getFinally().accept(this);
			}
			return false;
		}

		@Override
		public boolean visit(TypeDeclaration node) {
			if (node.getJavadoc() != null) {
				node.getJavadoc().accept(this);
			}
			if (node.getAST().apiLevel() == JLS2) {
				printModifiers(node.getModifiers());
			}
			if (node.getAST().apiLevel() >= JLS3) {
				printModifiers(node.modifiers());
			}
			buffer.append(node.isInterface() ? "interface " : "class ");//$NON-NLS-2$//$NON-NLS-1$
			node.getName().accept(this);
			if (node.getAST().apiLevel() >= JLS3) {
				if (!node.typeParameters().isEmpty()) {
					buffer.append("<");//$NON-NLS-1$
					for (Iterator it = node.typeParameters().iterator(); it.hasNext();) {
						TypeParameter t = (TypeParameter) it.next();
						t.accept(this);
						if (it.hasNext()) {
							buffer.append(",");//$NON-NLS-1$
						}
					}
					buffer.append(">");//$NON-NLS-1$
				}
			}
			buffer.append(" ");//$NON-NLS-1$
			if (node.getAST().apiLevel() == JLS2) {
				if (getSuperclass(node) != null) {
					buffer.append("extends ");//$NON-NLS-1$
					getSuperclass(node).accept(this);
					buffer.append(" ");//$NON-NLS-1$
				}
				if (!superInterfaces(node).isEmpty()) {
					buffer.append(node.isInterface() ? "extends " : "implements ");//$NON-NLS-2$//$NON-NLS-1$
					for (Iterator it = superInterfaces(node).iterator(); it.hasNext();) {
						Name n = (Name) it.next();
						n.accept(this);
						if (it.hasNext()) {
							buffer.append(", ");//$NON-NLS-1$
						}
					}
					buffer.append(" ");//$NON-NLS-1$
				}
			}
			if (node.getAST().apiLevel() >= JLS3) {
				if (node.getSuperclassType() != null) {
					buffer.append("extends ");//$NON-NLS-1$
					node.getSuperclassType().accept(this);
					buffer.append(" ");//$NON-NLS-1$
				}
				if (!node.superInterfaceTypes().isEmpty()) {
					buffer.append(node.isInterface() ? "extends " : "implements ");//$NON-NLS-2$//$NON-NLS-1$
					for (Iterator it = node.superInterfaceTypes().iterator(); it.hasNext();) {
						Type t = (Type) it.next();
						t.accept(this);
						if (it.hasNext()) {
							buffer.append(", ");//$NON-NLS-1$
						}
					}
					buffer.append(" ");//$NON-NLS-1$
				}
			}
			buffer.append("{\n");//$NON-NLS-1$
			indent++;
			for (Iterator it = node.bodyDeclarations().iterator(); it.hasNext();) {
				BodyDeclaration d = (BodyDeclaration) it.next();
				d.accept(this);
			}
			indent--;
			printIndent();
			buffer.append("}\n");//$NON-NLS-1$
			return false;
		}

		@Override
		public boolean visit(TypeDeclarationStatement node) {
			if (node.getAST().apiLevel() == JLS2) {
				getTypeDeclaration(node).accept(this);
			}
			if (node.getAST().apiLevel() >= JLS3) {
				node.getDeclaration().accept(this);
			}
			return false;
		}

		@Override
		public boolean visit(TypeLiteral node) {
			node.getType().accept(this);
			buffer.append(".class");//$NON-NLS-1$
			return false;
		}

		/*
		 * @see ASTVisitor#visit(TypeMethodReference)
		 *
		 * @since 3.10
		 */
		@Override
		public boolean visit(TypeMethodReference node) {
			node.getType().accept(this);
			visitReferenceTypeArguments(node.typeArguments());
			node.getName().accept(this);
			return false;
		}

		@Override
		public boolean visit(TypeParameter node) {
			if (node.getAST().apiLevel() >= JLS8) {
				printModifiers(node.modifiers());
			}
			node.getName().accept(this);
			if (!node.typeBounds().isEmpty()) {
				buffer.append(" extends ");//$NON-NLS-1$
				for (Iterator it = node.typeBounds().iterator(); it.hasNext();) {
					Type t = (Type) it.next();
					t.accept(this);
					if (it.hasNext()) {
						buffer.append(" & ");//$NON-NLS-1$
					}
				}
			}
			return false;
		}

		@Override
		public boolean visit(UnionType node) {
			for (Iterator it = node.types().iterator(); it.hasNext();) {
				Type t = (Type) it.next();
				t.accept(this);
				if (it.hasNext()) {
					buffer.append('|');
				}
			}
			return false;
		}

		@Override
		public boolean visit(UsesDirective node) {
			printIndent();
			buffer.append("uses");//$NON-NLS-1$
			buffer.append(" ");//$NON-NLS-1$
			node.getName().accept(this);
			buffer.append(";\n");//$NON-NLS-1$
			return false;
		}

		@Override
		public boolean visit(VariableDeclarationExpression node) {
			if (node.getAST().apiLevel() == JLS2) {
				printModifiers(node.getModifiers());
			}
			if (node.getAST().apiLevel() >= JLS3) {
				printModifiers(node.modifiers());
			}
			node.getType().accept(this);
			buffer.append(" ");//$NON-NLS-1$
			for (Iterator it = node.fragments().iterator(); it.hasNext();) {
				VariableDeclarationFragment f = (VariableDeclarationFragment) it.next();
				f.accept(this);
				if (it.hasNext()) {
					buffer.append(", ");//$NON-NLS-1$
				}
			}
			return false;
		}

		@Override
		public boolean visit(VariableDeclarationFragment node) {
			addLocalBinding(node.resolveBinding(), node.getName().getIdentifier());

			buffer.append(node.getName().getIdentifier());
			int size = node.getExtraDimensions();
			if (node.getAST().apiLevel() >= JLS8) {
				List dimensions = node.extraDimensions();
				for (int i = 0; i < size; i++) {
					visit((Dimension) dimensions.get(i));
				}
			} else {
				for (int i = 0; i < size; i++) {
					buffer.append("[]");//$NON-NLS-1$
				}
			}
			if (node.getInitializer() != null) {
				buffer.append("=");//$NON-NLS-1$
				node.getInitializer().accept(this);
			}
			return false;
		}

		@Override
		public boolean visit(VariableDeclarationStatement node) {
			printIndent();
			if (node.getAST().apiLevel() == JLS2) {
				printModifiers(node.getModifiers());
			}
			if (node.getAST().apiLevel() >= JLS3) {
				printModifiers(node.modifiers());
			}
			node.getType().accept(this);
			buffer.append(" ");//$NON-NLS-1$
			for (Iterator it = node.fragments().iterator(); it.hasNext();) {
				VariableDeclarationFragment f = (VariableDeclarationFragment) it.next();
				f.accept(this);
				if (it.hasNext()) {
					buffer.append(", ");//$NON-NLS-1$
				}
			}
			buffer.append(";\n");//$NON-NLS-1$
			return false;
		}

		@Override
		public boolean visit(WhileStatement node) {
			printIndent();
			buffer.append("while (");//$NON-NLS-1$
			node.getExpression().accept(this);
			buffer.append(") ");//$NON-NLS-1$
			node.getBody().accept(this);
			return false;
		}

		@Override
		public boolean visit(WildcardType node) {
			visitTypeAnnotations(node);
			buffer.append("?");//$NON-NLS-1$
			Type bound = node.getBound();
			if (bound != null) {
				if (node.isUpperBound()) {
					buffer.append(" extends ");//$NON-NLS-1$
				} else {
					buffer.append(" super ");//$NON-NLS-1$
				}
				bound.accept(this);
			}
			return false;
		}

		@Override
		public boolean visit(YieldStatement node) {
			if ((node.getAST().isPreviewEnabled()) && node.isImplicit() && node.getExpression() == null) {
				return false;
			}
			printIndent();
			buffer.append("yield"); //$NON-NLS-1$
			if (node.getExpression() != null) {
				buffer.append(" ");//$NON-NLS-1$
				node.getExpression().accept(this);
			}
			buffer.append(";\n");//$NON-NLS-1$
			return false;
		}

		/**
		 * @deprecated
		 */
		@Deprecated
		private void visitComponentType(ArrayType node) {
			node.getComponentType().accept(this);
		}
	}

}
