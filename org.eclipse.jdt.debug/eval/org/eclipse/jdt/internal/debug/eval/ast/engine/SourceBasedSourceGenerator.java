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
package org.eclipse.jdt.internal.debug.eval.ast.engine;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.SourceRange;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
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
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
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
import org.eclipse.jdt.core.dom.LineComment;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MemberRef;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.MethodRef;
import org.eclipse.jdt.core.dom.MethodRefParameter;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
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
import org.eclipse.jdt.core.dom.Type;
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
import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;

public class SourceBasedSourceGenerator extends ASTVisitor {

	private static final String RUN_METHOD_NAME = "___run"; //$NON-NLS-1$
	private static final String EVAL_METHOD_NAME = "___eval"; //$NON-NLS-1$
	private static final String EVAL_FIELD_NAME = "___field"; //$NON-NLS-1$

	private String[] fLocalVariableTypeNames;
	private String[] fLocalVariableNames;
	private String fCodeSnippet;

	private boolean fRightTypeFound;

	private boolean fCreateInAStaticMethod;

	private boolean fEvaluateNextEndTypeDeclaration;

	private String fError;

	private IType fType;

	private StringBuffer fSource;

	private String fLastTypeName;

	private String fCompilationUnitName;

	private int fSnippetStartPosition;
	private int fRunMethodStartOffset;
	private int fRunMethodLength;

	/**
	 * Level of source code to generate (major, minor). For example 1 and 4
	 * indicates 1.4.
	 */
	private int fSourceMajorLevel;
	private int fSourceMinorLevel;

	private Set<String> fTypeParameters = new HashSet<String>();

	/**
	 * if the <code>createInAnInstanceMethod</code> flag is set, the method
	 * created which contains the code snippet is an no-static method, even if
	 * <code>position</code> is in a static method.
	 * 
	 * @param type
	 *            the root {@link IType}
	 * @param createInAStaticMethod
	 *            if the source should be generated
	 * @param localTypesNames
	 *            the array of local type names
	 * @param localVariables
	 *            the listing of local variable names
	 * @param codeSnippet
	 *            the code snippet
	 * @param sourceLevel
	 *            the desired source level
	 */
	public SourceBasedSourceGenerator(IType type,
			boolean createInAStaticMethod, String[] localTypesNames,
			String[] localVariables, String codeSnippet, String sourceLevel) {
		fRightTypeFound = false;
		fType = type;
		fLocalVariableTypeNames = localTypesNames;
		fLocalVariableNames = localVariables;
		fCodeSnippet = codeSnippet;
		fCreateInAStaticMethod = createInAStaticMethod;
		int index = sourceLevel.indexOf('.');
		String num = sourceLevel.substring(0, index);
		fSourceMajorLevel = Integer.valueOf(num).intValue();
		num = sourceLevel.substring(index + 1);
		fSourceMinorLevel = Integer.valueOf(num).intValue();
	}

	/**
	 * Returns the generated source or <code>null</code> if no source can be
	 * generated.
	 * 
	 * @return returns the backing source from the generator
	 */
	public String getSource() {
		if (fSource == null) {
			return null;
		}
		return fSource.toString();
	}

	public String getCompilationUnitName() {
		return fCompilationUnitName;
	}

	public int getSnippetStart() {
		return fSnippetStartPosition;
	}

	public int getRunMethodStart() {
		return fSnippetStartPosition - fRunMethodStartOffset;
	}

	public int getRunMethodLength() {
		return fRunMethodLength;
	}

	private boolean rightTypeFound() {
		return fRightTypeFound;
	}

	private void setRightTypeFound(boolean value) {
		fRightTypeFound = value;
	}

	public boolean hasError() {
		return fError != null;
	}

	public void setError(String errorDesc) {
		fError = errorDesc;
	}

	public String getError() {
		return fError;
	}

	private StringBuffer buildRunMethod(List<BodyDeclaration> bodyDeclarations) {
		StringBuffer buffer = new StringBuffer();

		if (fCreateInAStaticMethod) {
			buffer.append("static "); //$NON-NLS-1$
		}

		// add type parameters as required
		if (isSourceLevelGreaterOrEqual(1, 5)) {
			if (!fTypeParameters.isEmpty()) {
				Iterator<String> iterator = fTypeParameters.iterator();
				buffer.append(Signature.C_GENERIC_START);
				while (iterator.hasNext()) {
					String name = iterator.next();
					buffer.append(name);
					if (iterator.hasNext()) {
						buffer.append(", "); //$NON-NLS-1$
					}
				}
				buffer.append(Signature.C_GENERIC_END);
			}
		}

		buffer.append("void "); //$NON-NLS-1$
		buffer.append(getUniqueMethodName(RUN_METHOD_NAME, bodyDeclarations));
		buffer.append('(');
		for (int i = 0, length = fLocalVariableNames.length; i < length; i++) {
			buffer.append(getDotName(fLocalVariableTypeNames[i]));
			buffer.append(' ');
			buffer.append(fLocalVariableNames[i]);
			if (i + 1 < length)
				buffer.append(", "); //$NON-NLS-1$
		}
		buffer.append(") throws Throwable {"); //$NON-NLS-1$
		buffer.append('\n');
		fSnippetStartPosition = buffer.length() - 2;
		fRunMethodStartOffset = fSnippetStartPosition;
		String codeSnippet = new String(fCodeSnippet).trim();

		buffer.append(codeSnippet);

		buffer.append('\n');
		buffer.append('}').append('\n');
		fRunMethodLength = buffer.length();
		return buffer;
	}

	private String getDotName(String typeName) {
		return typeName.replace('$', '.');
	}

	/**
	 * Returns if the specified {@link ASTNode} has the 'correct' parent type to
	 * match the current type name context
	 * 
	 * @param node
	 *            the {@link ASTNode} to check source ranges for
	 * @return true if the parent type of the given node matches the current
	 *         type name context, false otherwise
	 */
	private boolean isRightType(ASTNode node) {
		SourceRange range = new SourceRange(node.getStartPosition(),
				node.getLength());
		try {
			return fType.getSourceRange().equals(range);
		} catch (JavaModelException e) {
			JDIDebugPlugin.log(e.getStatus());
		}
		return false;
	}

	private StringBuffer buildTypeBody(StringBuffer buffer, List<BodyDeclaration> list) {
		StringBuffer source = new StringBuffer();

		source.append('{').append('\n');

		if (buffer != null) {
			fSnippetStartPosition += source.length();
		}

		source.append(buildBody(buffer, list));
		source.append('}').append('\n');

		return source;
	}

	private StringBuffer buildEnumBody(StringBuffer buffer,
			List<EnumConstantDeclaration> constantDeclarations, List<BodyDeclaration> bodyDeclarations) {
		StringBuffer source = new StringBuffer();

		source.append('{').append('\n');
		if (constantDeclarations.isEmpty()) {
			source.append(';').append('\n');
		} else {
			for (Iterator<EnumConstantDeclaration> iter = constantDeclarations.iterator(); iter
					.hasNext();) {
				source.append(iter.next().getName()
						.getIdentifier());
				if (iter.hasNext()) {
					source.append(',');
				} else {
					source.append(';');
				}
				source.append('\n');
			}
		}

		if (buffer != null) {
			fSnippetStartPosition += source.length();
		}

		source.append(buildBody(buffer, bodyDeclarations));
		source.append('}').append('\n');

		return source;

	}

	/**
	 * Builds up the given buffer with the source from each of
	 * {@link BodyDeclaration}s in the given list
	 * 
	 * @param buffer
	 *            the buffer to clone and append to
	 * @param list
	 *            the list of {@link BodyDeclaration}s
	 * @return the new source buffer
	 */
	private StringBuffer buildBody(StringBuffer buffer, List<BodyDeclaration> list) {
		StringBuffer source = new StringBuffer();
		if (buffer != null) {
			fSnippetStartPosition += source.length();
			source.append(buffer.toString());
		}
		for (Iterator<BodyDeclaration> iterator = list.iterator(); iterator.hasNext();) {
			BodyDeclaration bodyDeclaration = iterator.next();
			if (bodyDeclaration instanceof FieldDeclaration) {
				source.append(buildFieldDeclaration((FieldDeclaration) bodyDeclaration));
			} else if (bodyDeclaration instanceof MethodDeclaration) {
				source.append(buildMethodDeclaration((MethodDeclaration) bodyDeclaration));
			} else if (bodyDeclaration instanceof TypeDeclaration) {
				TypeDeclaration typeDeclaration = (TypeDeclaration) bodyDeclaration;
				if (!typeDeclaration.getName().getIdentifier()
						.equals(fLastTypeName)) {
					source.append(buildTypeDeclaration(null, typeDeclaration));
				}
			} else if (bodyDeclaration instanceof EnumDeclaration) {
				EnumDeclaration enumDeclaration = (EnumDeclaration) bodyDeclaration;
				if (!enumDeclaration.getName().getIdentifier()
						.equals(fLastTypeName)) {
					source.append(buildEnumDeclaration(null, enumDeclaration));
				}
			}
		}
		return source;
	}

	private StringBuffer buildFieldDeclaration(FieldDeclaration fieldDeclaration) {
		StringBuffer source = new StringBuffer();

		source.append(Flags.toString(fieldDeclaration.getModifiers()));
		source.append(' ');
		source.append(getDotName(getTypeName(fieldDeclaration.getType())));
		source.append(' ');

		boolean first = true;
		for (Iterator<VariableDeclarationFragment> iterator = fieldDeclaration.fragments().iterator(); iterator
				.hasNext();) {
			VariableDeclarationFragment variableDeclarationFragment = iterator
					.next();
			if (first) {
				first = false;
			} else {
				source.append(',');
			}
			source.append(variableDeclarationFragment.getName().getIdentifier());
			for (int i = 0, dim = variableDeclarationFragment
					.getExtraDimensions(); i < dim; i++) {
				source.append('[').append(']');
			}
		}

		source.append(';').append('\n');

		return source;
	}

	private StringBuffer buildMethodDeclaration(
			MethodDeclaration methodDeclaration) {
		StringBuffer source = new StringBuffer();
		int modifiers = methodDeclaration.getModifiers();
		source.append(Flags.toString(modifiers));
		source.append(' ');

		appendTypeParameters(source, methodDeclaration.typeParameters());

		boolean isConstructor = methodDeclaration.isConstructor();
		if (!isConstructor) {
			source.append(getDotName(getTypeName(methodDeclaration
					.getReturnType2())));
			source.append(' ');
		}

		source.append(methodDeclaration.getName().getIdentifier());
		source.append(' ').append('(');

		boolean first = true;
		for (Iterator<SingleVariableDeclaration> iterator = methodDeclaration.parameters().iterator(); iterator
				.hasNext();) {
			SingleVariableDeclaration singleVariableDeclaration = iterator
					.next();
			if (first) {
				first = false;
			} else {
				source.append(',');
			}
			source.append(getDotName(getTypeName(singleVariableDeclaration
					.getType())));
			if (singleVariableDeclaration.isVarargs()) {
				source.append("..."); //$NON-NLS-1$
			}
			source.append(' ');
			source.append(singleVariableDeclaration.getName().getIdentifier());
			appendExtraDimensions(source,
					singleVariableDeclaration.getExtraDimensions());
		}

		source.append(')');

		appendExtraDimensions(source, methodDeclaration.getExtraDimensions());

		first = true;
		for (Iterator<Name> iterator = methodDeclaration.thrownExceptions()
				.iterator(); iterator.hasNext();) {
			Name name = iterator.next();
			if (first) {
				first = false;
				source.append(" throws "); //$NON-NLS-1$
			} else {
				source.append(',');
			}
			source.append(getQualifiedIdentifier(name));
		}

		if (Flags.isAbstract(modifiers) || Flags.isNative(modifiers)) {
			// No body for abstract and native methods
			source.append(";\n"); //$NON-NLS-1$
		} else {
			source.append('{').append('\n');
			if (!isConstructor) {
				source.append(getReturnExpression(methodDeclaration
						.getReturnType2()));
			}
			source.append('}').append('\n');
		}

		return source;
	}

	private void appendExtraDimensions(StringBuffer source, int extraDimension) {
		if (extraDimension > 0) {
			source.append(' ');
			for (int i = 0; i < extraDimension; i++) {
				source.append("[]"); //$NON-NLS-1$
			}
		}
	}

	private StringBuffer buildEnumDeclaration(StringBuffer buffer,
			EnumDeclaration enumDeclaration) {
		StringBuffer source = new StringBuffer();
		source.append(Flags.toString(enumDeclaration.getModifiers()));
		source.append(" enum "); //$NON-NLS-1$

		source.append(enumDeclaration.getName().getIdentifier());

		Iterator<Type> iterator = enumDeclaration.superInterfaceTypes().iterator();
		if (iterator.hasNext()) {
			source.append(" implements "); //$NON-NLS-1$
			source.append(getTypeName(iterator.next()));
			while (iterator.hasNext()) {
				source.append(',');
				source.append(getTypeName(iterator.next()));
			}
		}

		if (buffer != null) {
			fSnippetStartPosition += source.length();
		}
		source.append(buildEnumBody(buffer, enumDeclaration.enumConstants(),
				enumDeclaration.bodyDeclarations()));

		return source;
	}

	private StringBuffer buildTypeDeclaration(StringBuffer buffer,
			TypeDeclaration typeDeclaration) {

		StringBuffer source = new StringBuffer();
		source.append(Flags.toString(typeDeclaration.getModifiers()));
		if (typeDeclaration.isInterface()) {
			source.append(" interface "); //$NON-NLS-1$
		} else {
			source.append(" class "); //$NON-NLS-1$
		}

		source.append(typeDeclaration.getName().getIdentifier());

		List<TypeParameter> typeParameters = typeDeclaration.typeParameters();
		if (!typeParameters.isEmpty() && isSourceLevelGreaterOrEqual(1, 5)) {
			source.append('<');
			Iterator<TypeParameter> iter = typeParameters.iterator();
			TypeParameter typeParameter = iter.next();
			source.append(typeParameter.getName().getIdentifier());
			List<Type> typeBounds = typeParameter.typeBounds();
			if (!typeBounds.isEmpty()) {
				source.append(" extends "); //$NON-NLS-1$
				Iterator<Type> iter2 = typeBounds.iterator();
				source.append(getTypeName(iter2.next()));
				while (iter2.hasNext()) {
					source.append('&');
					source.append(getTypeName(iter2.next()));
				}
			}
			while (iter.hasNext()) {
				source.append(',');
				typeParameter = iter.next();
				source.append(typeParameter.getName().getIdentifier());
				typeBounds = typeParameter.typeBounds();
				if (!typeBounds.isEmpty()) {
					source.append(" extends "); //$NON-NLS-1$
					Iterator<Type> iter2 = typeBounds.iterator();
					source.append(getTypeName(iter2.next()));
					while (iter2.hasNext()) {
						source.append('&');
						source.append(getTypeName(iter2.next()));
					}
				}
			}
			source.append('>');
		}

		Type superClass = typeDeclaration.getSuperclassType();
		if (superClass != null) {
			source.append(" extends "); //$NON-NLS-1$
			source.append(getTypeName(superClass));
		}

		Iterator<Type> iter = typeDeclaration.superInterfaceTypes().iterator();
		if (iter.hasNext()) {
			if (typeDeclaration.isInterface()) {
				source.append(" extends "); //$NON-NLS-1$
			} else {
				source.append(" implements "); //$NON-NLS-1$
			}
			source.append(getTypeName(iter.next()));
			while (iter.hasNext()) {
				source.append(',');
				source.append(getTypeName(iter.next()));
			}
		}

		if (buffer != null) {
			fSnippetStartPosition += source.length();
		}
		source.append(buildTypeBody(buffer, typeDeclaration.bodyDeclarations()));

		return source;
	}

	private StringBuffer buildCompilationUnit(StringBuffer buffer,
			CompilationUnit compilationUnit) {
		StringBuffer source = new StringBuffer();

		PackageDeclaration packageDeclaration = compilationUnit.getPackage();
		if (packageDeclaration != null) {
			source.append("package "); //$NON-NLS-1$
			source.append(getQualifiedIdentifier(packageDeclaration.getName()));
			source.append(";\n"); //$NON-NLS-1$
		}

		for (Iterator<ImportDeclaration> iterator = compilationUnit.imports().iterator(); iterator
				.hasNext();) {
			ImportDeclaration importDeclaration = iterator
					.next();
			source.append("import "); //$NON-NLS-1$
			if (importDeclaration.isStatic()) {
				source.append("static "); //$NON-NLS-1$
			}
			source.append(getQualifiedIdentifier(importDeclaration.getName()));
			if (importDeclaration.isOnDemand()) {
				source.append(".*"); //$NON-NLS-1$
			}
			source.append(";\n"); //$NON-NLS-1$
		}

		fSnippetStartPosition += source.length();
		source.append(buffer);

		for (Iterator<TypeDeclaration> iterator = compilationUnit.types().iterator(); iterator
				.hasNext();) {
			AbstractTypeDeclaration typeDeclaration = iterator.next();
			if (Flags.isPublic(typeDeclaration.getModifiers())) {
				fCompilationUnitName = typeDeclaration.getName()
						.getIdentifier();
			}
			if (!fLastTypeName
					.equals(typeDeclaration.getName().getIdentifier())) {
				if (typeDeclaration instanceof TypeDeclaration) {
					source.append(buildTypeDeclaration(null,
							(TypeDeclaration) typeDeclaration));
				} else if (typeDeclaration instanceof EnumDeclaration) {
					source.append(buildEnumDeclaration(null,
							(EnumDeclaration) typeDeclaration));
				}
			}
		}
		if (fCompilationUnitName == null) {
			// If no public class was found, the compilation unit
			// name doesn't matter.
			fCompilationUnitName = "Eval"; //$NON-NLS-1$
		}
		return source;
	}

	/**
	 * Returns a method name that will be unique in the generated source. The
	 * generated name is baseName plus as many '_' characters as necessary to
	 * not duplicate an existing method name.
	 * 
	 * @param methodName
	 *            the method name to look for
	 * @param bodyDeclarations
	 *            the listing of {@link BodyDeclaration}s to search through
	 * @return the unique method name
	 */
	private String getUniqueMethodName(String methodName, List<BodyDeclaration> bodyDeclarations) {
		Iterator<BodyDeclaration> iter = bodyDeclarations.iterator();
		BodyDeclaration bodyDeclaration;
		MethodDeclaration method;
		String foundName;
		while (iter.hasNext()) {
			bodyDeclaration = iter.next();
			if (bodyDeclaration instanceof MethodDeclaration) {
				method = (MethodDeclaration) bodyDeclaration;
				foundName = method.getName().getIdentifier();
				if (foundName.startsWith(methodName)) {
					methodName = foundName + '_';
				}
			}
		}
		return methodName;
	}

	/**
	 * Returns a field name that will be unique in the generated source. The
	 * generated name is baseName plus as many '_' characters as necessary to
	 * not duplicate an existing method name.
	 * 
	 * @param fieldName
	 *            the name of the field to look for
	 * @param bodyDeclarations
	 *            the list of {@link BodyDeclaration}s to search through
	 * @return the unique field name
	 */
	private String getUniqueFieldName(String fieldName, List<BodyDeclaration> bodyDeclarations) {
		Iterator<BodyDeclaration> iter = bodyDeclarations.iterator();
		BodyDeclaration bodyDeclaration;
		FieldDeclaration fieldDeclaration;
		String foundName;
		while (iter.hasNext()) {
			bodyDeclaration = iter.next();
			if (bodyDeclaration instanceof FieldDeclaration) {
				fieldDeclaration = (FieldDeclaration) bodyDeclaration;
				for (Iterator<VariableDeclarationFragment> iterator = fieldDeclaration.fragments()
						.iterator(); iterator.hasNext();) {
					foundName = iterator.next()
							.getName().getIdentifier();
					if (foundName.startsWith(fieldName)) {
						fieldName = foundName + '_';
					}
				}
			}
		}
		return fieldName;
	}

	private String getQualifiedIdentifier(Name name) {
		String typeName = ""; //$NON-NLS-1$
		while (name.isQualifiedName()) {
			QualifiedName qualifiedName = (QualifiedName) name;
			typeName = "." + qualifiedName.getName().getIdentifier() + typeName; //$NON-NLS-1$
			name = qualifiedName.getQualifier();
		}
		if (name.isSimpleName()) {
			typeName = ((SimpleName) name).getIdentifier() + typeName;
		} else {
			return null;
		}
		return typeName;
	}

	public String getTypeName(Type type) {
		if (type.isSimpleType()) {
			String name = getQualifiedIdentifier(((SimpleType) type).getName());
			if (!isSourceLevelGreaterOrEqual(1, 5)
					&& fTypeParameters.contains(name)) {
				return "Object"; //$NON-NLS-1$
			}
			return name;
		} else if (type.isArrayType()) {
			return getTypeName(((ArrayType) type).getComponentType()) + "[]"; //$NON-NLS-1$
		} else if (type.isPrimitiveType()) {
			return ((PrimitiveType) type).getPrimitiveTypeCode().toString();
		} else if (type.isQualifiedType()) {
			QualifiedType qualifiedType = (QualifiedType) type;
			return getTypeName(qualifiedType.getQualifier()) + '.'
					+ qualifiedType.getName().getIdentifier();
		} else if (type.isParameterizedType()) {
			ParameterizedType parameterizedType = (ParameterizedType) type;
			StringBuffer buff = new StringBuffer(
					getTypeName(parameterizedType.getType()));
			Iterator<Type> iter = parameterizedType.typeArguments().iterator();
			if (iter.hasNext() && isSourceLevelGreaterOrEqual(1, 5)) {
				buff.append('<');
				buff.append(getTypeName(iter.next()));
				while (iter.hasNext()) {
					buff.append(',');
					buff.append(getTypeName(iter.next()));
				}
				buff.append('>');
			}
			return buff.toString();
		} else if (type.isWildcardType()) {
			WildcardType wildcardType = (WildcardType) type;
			StringBuffer buff = new StringBuffer("?"); //$NON-NLS-1$
			Type bound = wildcardType.getBound();
			if (bound != null) {
				buff.append(wildcardType.isUpperBound() ? " extends " : " super "); //$NON-NLS-1$ //$NON-NLS-2$
				buff.append(getTypeName(bound));
			}
			return buff.toString();
		}
		return null;

	}

	public String getReturnExpression(Type type) {
		if (type.isSimpleType() || type.isArrayType() || type.isQualifiedType()
				|| type.isWildcardType() || type.isParameterizedType()) {
			return "return null;"; //$NON-NLS-1$
		} else if (type.isPrimitiveType()) {
			String typeName = ((PrimitiveType) type).getPrimitiveTypeCode()
					.toString();
			char char0 = typeName.charAt(0);
			if (char0 == 'v') {
				return ""; //$NON-NLS-1$
			}
			char char1 = typeName.charAt(1);
			if (char0 == 'b' && char1 == 'o') {
				return "return false;"; //$NON-NLS-1$
			}
			return "return 0;"; //$NON-NLS-1$
		}
		return null;
	}

	// ----------------------

	/**
	 * @see ASTVisitor#endVisit(ClassInstanceCreation)
	 */
	@Override
	public void endVisit(ClassInstanceCreation node) {
		if (hasError()) {
			return;
		}
		AnonymousClassDeclaration anonymousClassDeclaration = node
				.getAnonymousClassDeclaration();
		if (anonymousClassDeclaration != null) {
			if (!rightTypeFound() && isRightType(node)) {
				setRightTypeFound(true);

				fSource = buildRunMethod(anonymousClassDeclaration
						.bodyDeclarations());
				fEvaluateNextEndTypeDeclaration = true;
			}

			if (rightTypeFound()) {

				List<BodyDeclaration> bodyDeclarations = anonymousClassDeclaration
						.bodyDeclarations();

				StringBuffer source = buildTypeBody(fSource, bodyDeclarations);

				ASTNode parent = node.getParent();
				while (!(parent instanceof MethodDeclaration
						|| parent instanceof FieldDeclaration || parent instanceof Initializer)
						&& parent != null) {
					parent = parent.getParent();
				}

				fSource = new StringBuffer();

				if (parent instanceof Initializer) {
					createAnonymousEvalMethod(true, bodyDeclarations,
							getTypeName(node.getType()), source);
				} else if (parent instanceof MethodDeclaration) {
					MethodDeclaration enclosingMethodDeclaration = (MethodDeclaration) parent;
					createAnonymousEvalMethod(
							Flags.isStatic(enclosingMethodDeclaration
									.getModifiers()), bodyDeclarations,
							getTypeName(node.getType()), source);

				} else if (parent instanceof FieldDeclaration) {
					FieldDeclaration enclosingFieldDeclaration = (FieldDeclaration) parent;

					if (Flags
							.isStatic(enclosingFieldDeclaration.getModifiers())) {
						fSource.append("static "); //$NON-NLS-1$
					}

					Type type = getParentType(enclosingFieldDeclaration
							.getType());
					fSource.append(getQualifiedIdentifier(((SimpleType) type)
							.getName()));
					fSource.append(' ');
					fSource.append(getUniqueFieldName(EVAL_FIELD_NAME,
							bodyDeclarations));
					fSource.append(" = new "); //$NON-NLS-1$
					fSource.append(getTypeName(node.getType()));
					fSource.append("()"); //$NON-NLS-1$

					fSnippetStartPosition += fSource.length();
					fSource.append(source);
					fSource.append(";\n"); //$NON-NLS-1$

				}
				fLastTypeName = ""; //$NON-NLS-1$
			}
		}
	}

	/**
	 * Create a <code>void ____eval()</code> method considering the given
	 * {@link BodyDeclaration}s, type name and existing body source when an
	 * anonymous {@link ClassInstanceCreation} is visited. <br>
	 * <br>
	 * This method adds the new <code>___eval</code> method source to the root
	 * {@link #fSource} variable directly
	 * 
	 * @param isstatic
	 *            if the keyword <code>static</code> should be added to the
	 *            method source
	 * @param bodydecls
	 *            the existing listing of {@link BodyDeclaration}s to consider
	 *            when creating the <code>___eval</code> method name
	 * @param typename
	 *            the raw type name of the type to instantiate in the
	 *            <code>___eval</code> method
	 * @param body
	 *            the existing body of source to append to the remainder of the
	 *            new method
	 * @since 3.7
	 */
	void createAnonymousEvalMethod(boolean isstatic, List<BodyDeclaration> bodydecls,
			String typename, StringBuffer body) {
		if (isstatic) {
			fSource.append("static "); //$NON-NLS-1$
		}
		fSource.append("void "); //$NON-NLS-1$
		fSource.append(getUniqueMethodName(EVAL_METHOD_NAME, bodydecls));
		fSource.append("() {\n"); //$NON-NLS-1$
		fSource.append("new "); //$NON-NLS-1$
		fSource.append(typename);
		fSource.append("()"); //$NON-NLS-1$

		fSnippetStartPosition += fSource.length();
		fSource.append(body);
		fSource.append(";}\n"); //$NON-NLS-1$
	}

	/**
	 * Recursively finds the parent {@link Type} from the given type, in the
	 * cases where the type is an {@link ArrayType} or a
	 * {@link ParameterizedType}
	 * 
	 * @param type
	 *            the {@link Type}
	 * @return the parent {@link Type}
	 */
	private Type getParentType(Type type) {
		if (type instanceof ArrayType) {
			return getParentType(((ArrayType) type).getComponentType());
		}
		if (type instanceof ParameterizedType) {
			return getParentType(((ParameterizedType) type).getType());
		}
		return type;
	}

	/**
	 * @see ASTVisitor#endVisit(CompilationUnit)
	 */
	@Override
	public void endVisit(CompilationUnit node) {
		if (hasError()) {
			return;
		}
		if (!rightTypeFound()) { // if the right type hasn't been found
			fSource = null;
			return;
		}
		fSource = buildCompilationUnit(fSource, node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jdt.core.dom.ASTVisitor#endVisit(org.eclipse.jdt.core.dom
	 * .EnumDeclaration)
	 */
	@Override
	public void endVisit(EnumDeclaration node) {

		if (hasError()) {
			return;
		}

		if (!rightTypeFound() && isRightType(node)) {
			setRightTypeFound(true);

			fSource = buildRunMethod(node.bodyDeclarations());
			fEvaluateNextEndTypeDeclaration = true;
		}

		if (!fEvaluateNextEndTypeDeclaration) {
			fEvaluateNextEndTypeDeclaration = true;
			return;
		}

		if (rightTypeFound()) {

			StringBuffer source = buildEnumDeclaration(fSource, node);

			if (node.isLocalTypeDeclaration()) {
				// enclose in a method if necessary

				ASTNode parent = node.getParent();
				while (!(parent instanceof MethodDeclaration)) {
					parent = parent.getParent();
				}
				MethodDeclaration enclosingMethodDeclaration = (MethodDeclaration) parent;

				fSource = new StringBuffer();

				if (Flags.isStatic(enclosingMethodDeclaration.getModifiers())) {
					fSource.append("static "); //$NON-NLS-1$
				}

				fSource.append("void ___eval() {\n"); //$NON-NLS-1$
				fSnippetStartPosition += fSource.length();
				fSource.append(source);
				fSource.append("}\n"); //$NON-NLS-1$

				fLastTypeName = ""; //$NON-NLS-1$
			} else {
				fSource = source;
				fLastTypeName = node.getName().getIdentifier();
			}
		}
	}

	/**
	 * @see ASTVisitor#endVisit(TypeDeclaration)
	 */
	@Override
	public void endVisit(TypeDeclaration node) {

		if (hasError()) {
			return;
		}

		if (!rightTypeFound() && isRightType(node)) {
			setRightTypeFound(true);

			fSource = buildRunMethod(node.bodyDeclarations());
			fEvaluateNextEndTypeDeclaration = true;
		}

		if (!fEvaluateNextEndTypeDeclaration) {
			fEvaluateNextEndTypeDeclaration = true;
			return;
		}

		if (rightTypeFound()) {

			StringBuffer source = buildTypeDeclaration(fSource, node);

			if (node.isLocalTypeDeclaration()) {
				// enclose in a method if nessecary

				ASTNode parent = node.getParent();
				while (!(parent instanceof MethodDeclaration)) {
					parent = parent.getParent();
				}
				MethodDeclaration enclosingMethodDeclaration = (MethodDeclaration) parent;

				fSource = new StringBuffer();

				if (Flags.isStatic(enclosingMethodDeclaration.getModifiers())) {
					fSource.append("static "); //$NON-NLS-1$
				}

				fSource.append("void ___eval() {\n"); //$NON-NLS-1$
				fSnippetStartPosition += fSource.length();
				fSource.append(source);
				fSource.append("}\n"); //$NON-NLS-1$

				fLastTypeName = ""; //$NON-NLS-1$
			} else {
				fSource = source;
				fLastTypeName = node.getName().getIdentifier();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
	 * AnnotationTypeDeclaration)
	 */
	@Override
	public boolean visit(AnnotationTypeDeclaration node) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
	 * AnnotationTypeMemberDeclaration)
	 */
	@Override
	public boolean visit(AnnotationTypeMemberDeclaration node) {
		return false;
	}

	/**
	 * @see ASTVisitor#visit(AnonymousClassDeclaration)
	 */
	@Override
	public boolean visit(AnonymousClassDeclaration node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/**
	 * @see ASTVisitor#visit(ArrayAccess)
	 */
	@Override
	public boolean visit(ArrayAccess node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/**
	 * @see ASTVisitor#visit(ArrayCreation)
	 */
	@Override
	public boolean visit(ArrayCreation node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/**
	 * @see ASTVisitor#visit(ArrayInitializer)
	 */
	@Override
	public boolean visit(ArrayInitializer node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/**
	 * @see ASTVisitor#visit(ArrayType)
	 */
	@Override
	public boolean visit(ArrayType node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/**
	 * @see ASTVisitor#visit(AssertStatement)
	 */
	@Override
	public boolean visit(AssertStatement node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/**
	 * @see ASTVisitor#visit(Assignment)
	 */
	@Override
	public boolean visit(Assignment node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/**
	 * @see ASTVisitor#visit(Block)
	 */
	@Override
	public boolean visit(Block node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
	 * BlockComment)
	 */
	@Override
	public boolean visit(BlockComment node) {
		return false;
	}

	/**
	 * @see ASTVisitor#visit(BooleanLiteral)
	 */
	@Override
	public boolean visit(BooleanLiteral node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/**
	 * @see ASTVisitor#visit(BreakStatement)
	 */
	@Override
	public boolean visit(BreakStatement node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/**
	 * @see ASTVisitor#visit(CastExpression)
	 */
	@Override
	public boolean visit(CastExpression node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/**
	 * @see ASTVisitor#visit(CatchClause)
	 */
	@Override
	public boolean visit(CatchClause node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/**
	 * @see ASTVisitor#visit(CharacterLiteral)
	 */
	@Override
	public boolean visit(CharacterLiteral node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/**
	 * @see ASTVisitor#visit(ClassInstanceCreation)
	 */
	@Override
	public boolean visit(ClassInstanceCreation node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/**
	 * @see ASTVisitor#visit(CompilationUnit)
	 */
	@Override
	public boolean visit(CompilationUnit node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/**
	 * @see ASTVisitor#visit(ConditionalExpression)
	 */
	@Override
	public boolean visit(ConditionalExpression node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/**
	 * @see ASTVisitor#visit(ConstructorInvocation)
	 */
	@Override
	public boolean visit(ConstructorInvocation node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/**
	 * @see ASTVisitor#visit(ContinueStatement)
	 */
	@Override
	public boolean visit(ContinueStatement node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/**
	 * @see ASTVisitor#visit(DoStatement)
	 */
	@Override
	public boolean visit(DoStatement node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/**
	 * @see ASTVisitor#visit(EmptyStatement)
	 */
	@Override
	public boolean visit(EmptyStatement node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
	 * EnhancedForStatement)
	 */
	@Override
	public boolean visit(EnhancedForStatement node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
	 * EnumConstantDeclaration)
	 */
	@Override
	public boolean visit(EnumConstantDeclaration node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
	 * EnumDeclaration)
	 */
	@Override
	public boolean visit(EnumDeclaration node) {
		if (rightTypeFound()) {
			fEvaluateNextEndTypeDeclaration = false;
			return false;
		}
		return true;
	}

	/**
	 * @see ASTVisitor#visit(ExpressionStatement)
	 */
	@Override
	public boolean visit(ExpressionStatement node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/**
	 * @see ASTVisitor#visit(FieldAccess)
	 */
	@Override
	public boolean visit(FieldAccess node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/**
	 * @see ASTVisitor#visit(FieldDeclaration)
	 */
	@Override
	public boolean visit(FieldDeclaration node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/**
	 * @see ASTVisitor#visit(ForStatement)
	 */
	@Override
	public boolean visit(ForStatement node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/**
	 * @see ASTVisitor#visit(IfStatement)
	 */
	@Override
	public boolean visit(IfStatement node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/**
	 * @see ASTVisitor#visit(ImportDeclaration)
	 */
	@Override
	public boolean visit(ImportDeclaration node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/**
	 * @see ASTVisitor#visit(InfixExpression)
	 */
	@Override
	public boolean visit(InfixExpression node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/**
	 * @see ASTVisitor#visit(Initializer)
	 */
	@Override
	public boolean visit(Initializer node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
	 * InstanceofExpression)
	 */
	@Override
	public boolean visit(InstanceofExpression node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/**
	 * @see ASTVisitor#visit(Javadoc)
	 */
	@Override
	public boolean visit(Javadoc node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/**
	 * @see ASTVisitor#visit(LabeledStatement)
	 */
	@Override
	public boolean visit(LabeledStatement node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
	 * LineComment)
	 */
	@Override
	public boolean visit(LineComment node) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
	 * MarkerAnnotation)
	 */
	@Override
	public boolean visit(MarkerAnnotation node) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.MemberRef
	 * )
	 */
	@Override
	public boolean visit(MemberRef node) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
	 * MemberValuePair)
	 */
	@Override
	public boolean visit(MemberValuePair node) {
		return false;
	}

	/**
	 * @see ASTVisitor#visit(MethodDeclaration)
	 */
	@Override
	public boolean visit(MethodDeclaration node) {
		List<TypeParameter> typeParameters = node.typeParameters();
		if (!typeParameters.isEmpty()) {
			Iterator<TypeParameter> iterator = typeParameters.iterator();
			while (iterator.hasNext()) {
				TypeParameter typeParameter = iterator.next();
				fTypeParameters.add(typeParameter.toString());
			}
		}
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/**
	 * @see ASTVisitor#visit(MethodInvocation)
	 */
	@Override
	public boolean visit(MethodInvocation node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.MethodRef
	 * )
	 */
	@Override
	public boolean visit(MethodRef node) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
	 * MethodRefParameter)
	 */
	@Override
	public boolean visit(MethodRefParameter node) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.Modifier
	 * )
	 */
	@Override
	public boolean visit(Modifier node) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
	 * NormalAnnotation)
	 */
	@Override
	public boolean visit(NormalAnnotation node) {
		return false;
	}

	/**
	 * @see ASTVisitor#visit(NullLiteral)
	 */
	@Override
	public boolean visit(NullLiteral node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/**
	 * @see ASTVisitor#visit(NumberLiteral)
	 */
	@Override
	public boolean visit(NumberLiteral node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/**
	 * @see ASTVisitor#visit(PackageDeclaration)
	 */
	@Override
	public boolean visit(PackageDeclaration node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
	 * ParameterizedType)
	 */
	@Override
	public boolean visit(ParameterizedType node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/**
	 * @see ASTVisitor#visit(ParenthesizedExpression)
	 */
	@Override
	public boolean visit(ParenthesizedExpression node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/**
	 * @see ASTVisitor#visit(PostfixExpression)
	 */
	@Override
	public boolean visit(PostfixExpression node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/**
	 * @see ASTVisitor#visit(PrefixExpression)
	 */
	@Override
	public boolean visit(PrefixExpression node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/**
	 * @see ASTVisitor#visit(PrimitiveType)
	 */
	@Override
	public boolean visit(PrimitiveType node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/**
	 * @see ASTVisitor#visit(QualifiedName)
	 */
	@Override
	public boolean visit(QualifiedName node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
	 * QualifiedType)
	 */
	@Override
	public boolean visit(QualifiedType node) {
		return false;
	}

	/**
	 * @see ASTVisitor#visit(ReturnStatement)
	 */
	@Override
	public boolean visit(ReturnStatement node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/**
	 * @see ASTVisitor#visit(SimpleName)
	 */
	@Override
	public boolean visit(SimpleName node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/**
	 * @see ASTVisitor#visit(SimpleType)
	 */
	@Override
	public boolean visit(SimpleType node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
	 * SingleMemberAnnotation)
	 */
	@Override
	public boolean visit(SingleMemberAnnotation node) {
		return false;
	}

	/**
	 * @see ASTVisitor#visit(SingleVariableDeclaration)
	 */
	@Override
	public boolean visit(SingleVariableDeclaration node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/**
	 * @see ASTVisitor#visit(StringLiteral)
	 */
	@Override
	public boolean visit(StringLiteral node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/**
	 * @see ASTVisitor#visit(SuperConstructorInvocation)
	 */
	@Override
	public boolean visit(SuperConstructorInvocation node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/**
	 * @see ASTVisitor#visit(SuperFieldAccess)
	 */
	@Override
	public boolean visit(SuperFieldAccess node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/**
	 * @see ASTVisitor#visit(SuperMethodInvocation)
	 */
	@Override
	public boolean visit(SuperMethodInvocation node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/**
	 * @see ASTVisitor#visit(SwitchCase)
	 */
	@Override
	public boolean visit(SwitchCase node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/**
	 * @see ASTVisitor#visit(SwitchStatement)
	 */
	@Override
	public boolean visit(SwitchStatement node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/**
	 * @see ASTVisitor#visit(SynchronizedStatement)
	 */
	@Override
	public boolean visit(SynchronizedStatement node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.TagElement
	 * )
	 */
	@Override
	public boolean visit(TagElement node) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
	 * TextElement)
	 */
	@Override
	public boolean visit(TextElement node) {
		return false;
	}

	/**
	 * @see ASTVisitor#visit(ThisExpression)
	 */
	@Override
	public boolean visit(ThisExpression node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/**
	 * @see ASTVisitor#visit(ThrowStatement)
	 */
	@Override
	public boolean visit(ThrowStatement node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/**
	 * @see ASTVisitor#visit(TryStatement)
	 */
	@Override
	public boolean visit(TryStatement node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.UnionType
	 * )
	 */
	@Override
	public boolean visit(UnionType node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/**
	 * @see ASTVisitor#visit(TypeDeclaration)
	 */
	@Override
	public boolean visit(TypeDeclaration node) {
		List<TypeParameter> typeParameters = node.typeParameters();
		if (!typeParameters.isEmpty()) {
			Iterator<TypeParameter> iterator = typeParameters.iterator();
			while (iterator.hasNext()) {
				TypeParameter typeParameter = iterator.next();
				fTypeParameters.add(typeParameter.getName().getIdentifier());
			}
		}
		if (rightTypeFound()) {
			fEvaluateNextEndTypeDeclaration = false;
			return false;
		}
		return true;
	}

	/**
	 * @see ASTVisitor#visit(TypeDeclarationStatement)
	 */
	@Override
	public boolean visit(TypeDeclarationStatement node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/**
	 * @see ASTVisitor#visit(TypeLiteral)
	 */
	@Override
	public boolean visit(TypeLiteral node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
	 * TypeParameter)
	 */
	@Override
	public boolean visit(TypeParameter node) {
		return false;
	}

	/**
	 * @see ASTVisitor#visit(VariableDeclarationExpression)
	 */
	@Override
	public boolean visit(VariableDeclarationExpression node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/**
	 * @see ASTVisitor#visit(VariableDeclarationFragment)
	 */
	@Override
	public boolean visit(VariableDeclarationFragment node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/**
	 * @see ASTVisitor#visit(VariableDeclarationStatement)
	 */
	@Override
	public boolean visit(VariableDeclarationStatement node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/**
	 * @see ASTVisitor#visit(WhileStatement)
	 */
	@Override
	public boolean visit(WhileStatement node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
	 * WildcardType)
	 */
	@Override
	public boolean visit(WildcardType node) {
		return false;
	}

	/**
	 * Returns whether the source to be generated is greater than or equal to
	 * the given source level.
	 * 
	 * @param major
	 *            major level - e.g. 1 from 1.4
	 * @param minor
	 *            minor level - e.g. 4 from 1.4
	 * @return <code>true</code> if the given major / minor version is less than
	 *         or equal to the backing source level
	 */
	public boolean isSourceLevelGreaterOrEqual(int major, int minor) {
		return (fSourceMajorLevel > major)
				|| (fSourceMajorLevel == major && fSourceMinorLevel >= minor);
	}

	/**
	 * Appends type parameters to source.
	 * 
	 * @param source
	 *            the current buffer of source to append to
	 * @param typeParameters
	 *            the list of {@link TypeParameter}s to add
	 */
	private void appendTypeParameters(StringBuffer source, List<TypeParameter> typeParameters) {
		if (!typeParameters.isEmpty() && isSourceLevelGreaterOrEqual(1, 5)) {
			source.append('<');
			Iterator<TypeParameter> iter = typeParameters.iterator();
			TypeParameter typeParameter = iter.next();
			source.append(typeParameter.getName().getIdentifier());
			List<Type> typeBounds = typeParameter.typeBounds();
			if (!typeBounds.isEmpty()) {
				source.append(" extends "); //$NON-NLS-1$
				Iterator<Type> iter2 = typeBounds.iterator();
				source.append(getTypeName(iter2.next()));
				while (iter2.hasNext()) {
					source.append('&');
					source.append(getTypeName(iter2.next()));
				}
			}
			while (iter.hasNext()) {
				source.append(',');
				typeParameter = iter.next();
				source.append(typeParameter.getName().getIdentifier());
				typeBounds = typeParameter.typeBounds();
				if (!typeBounds.isEmpty()) {
					source.append(" extends "); //$NON-NLS-1$
					Iterator<Type> iter2 = typeBounds.iterator();
					source.append(getTypeName(iter2.next()));
					while (iter2.hasNext()) {
						source.append('&');
						source.append(getTypeName(iter2.next()));
					}
				}
			}
			source.append('>');
			source.append(' ');
		}
	}
}
